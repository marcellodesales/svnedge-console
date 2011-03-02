/*
 * CollabNet Subversion Edge
 * Copyright (C) 2010, CollabNet Inc. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.collabnet.svnedge.console.ui

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.replication.ReplicaConfiguration
import com.collabnet.svnedge.teamforge.CtfServer
import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.CantBindPortException
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import com.collabnet.svnedge.console.ServerMode
import com.collabnet.svnedge.teamforge.CtfAuthenticationException

class CtfCredentialCommand {
    String ctfUsername
    String ctfPassword
    static constraints = {
        ctfUsername(blank:false)
        ctfPassword(blank:false)
    }
}

@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_SYSTEM'])
class ServerController {

    def operatingSystemService
    def lifecycleService
    def networkingService
    def serverConfService
    def setupTeamForgeService
    def setupReplicaService
    def csvnAuthenticationProvider

    def index = { redirect(action:edit, params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [update:'POST', revert: 'POST']

    def revert = { CtfCredentialCommand ctfCredentials ->
        ctfCredentials.validate()
        def ctfServer = CtfServer.getServer()
        def server = Server.getServer()
        boolean isReplica = (server.mode == ServerMode.REPLICA)
        String errorView = isReplica ?
                "editReplica" :
                "editIntegration"
        
        if (ctfCredentials.hasErrors()) {
            def formError = message(code: 
                "server.action.revert.error.credentials")
            render(view: errorView,
                    model: [ctfServerBaseUrl: CtfServer.getServer().baseUrl,
                        ctfCredentials: ctfCredentials, formError: formError,
                        canEditCredentials: isReplica])
        } else {
            try {
                def errors = []
                if (isReplica) {
                    this.setupReplicaService.revertFromReplicaMode(
                        ctfCredentials.ctfUsername, ctfCredentials.ctfPassword,
                        errors, RCU.getLocale(request))
                }
                else {
                    this.setupTeamForgeService.revertFromCtfMode(
                        ctfCredentials.ctfUsername, ctfCredentials.ctfPassword,
                        errors, RCU.getLocale(request))
                }
                
                if (errors) {
                    def formError = 
                        message(code: "server.action.revert.error.general")
                    render(view: errorView,
                        model: [ctfServerBaseUrl: ctfServer.baseUrl,
                            ctfCredentials: ctfCredentials,
                            formError: formError, errorCause: errors,
                            canEditCredentials: isReplica])
                } else {
                    flash.message = message(code: 
                        "server.action.revert.success")
                    // since we have a success flash message, we can delete the
                    // ReplicaConfiguration, which signals a duplicate message
                    ReplicaConfiguration.getCurrentConfig()?.delete(flush:true);
                    redirect(controller: "status", action: "index")
                }
            } catch (CtfAuthenticationException wrongCredentials) {
                def ctfBaseUrl = ctfServer.baseUrl
                def formError = message(code:
                    "server.action.revert.error.connection", args: [ctfBaseUrl])
                render(view: errorView,
                        model: [ctfServerBaseUrl: ctfBaseUrl,
                            ctfCredentials: ctfCredentials,
                            formError: formError, 
                            errorCause: wrongCredentials.getMessage(),
                            canEditCredentials: isReplica])
            }
        }
    }

    def editIntegration = {
        flash.warn = message(code: "server.action.revert.warn")
        def ctfCredentialsCmd = new CtfCredentialCommand()
        Server s = Server.getServer()
        String view = (s.mode == ServerMode.REPLICA) ?
                "editReplica" :
                "editIntegration"

        render (view : view, model: [ctfServerBaseUrl: CtfServer.getServer()?.baseUrl,
                ctfCredentials: ctfCredentialsCmd,
                canEditCredentials: (s.mode == ServerMode.REPLICA)])
    }

    def edit = {
        def server = Server.getServer()
        prepareServerViewModel(server)

    }

    def editAuthentication = {
        def server = Server.getServer()
        if (!server.authHelperPort) {
           server.authHelperPort =     
                csvnAuthenticationProvider.getAuthHelperPort(server, server.ldapEnabled) 
        }
        return [server: server,
                csvnConf: ConfigUtil.confDirPath(),
                isConfigurable: serverConfService.createOrValidateHttpdConf()
        ]
    }

    def update = {
        flash.clear()
        def server = Server.getServer()
        params.each{ key, value -> 
            if (params[key] instanceof String) {
                params[key] = ((String)(params[key])).trim()
            }
        }
        if (params.version) {
            def version = params.version.toLong()
            if (server.version > version) {
                server.errors.rejectValue("version",
                    "server.optimistic.locking.failure")
                    render(view:'edit', model:[server:server])
                    return
            }
        }

        // checking for port error before params are applied
        // since changes to Server seem to persist automatically with
        // call to service methods
        boolean portError = false
        if (params.port?.isNumber() && params.port.toInteger() < 1024 ) {
            lifecycleService.clearCachedResults()
            if (!lifecycleService.isDefaultPortAllowed()) {
                portError = true
            }
        }
        
        // copy form params to the entity and validate
        server.properties = params
        //In editAuthentication UI repoParentDir does not exist in Params.
        if (params.repoParentDir != null) {
          // canonicalize repo parent dir (esp to remove trailing "/" or "\"
          // that server.validate would allow)
          String repoParent = new File(params.repoParentDir).canonicalPath
          server.repoParentDir = repoParent
        }

        // validate, and reject port value if needed
        server.validate()
        if (portError) {
            server.errors.rejectValue("port",
                "server.port.defaultValue.rejected")
        }

        if(!server.hasErrors() && server.save(flush:true)) {
            if (lifecycleService.isStarted()) {
                try {
                    def result = lifecycleService.stopServer()
                    if (result <= 0) {
                        result = lifecycleService.startServer()
                        if (result < 0) {
                            flash.message = message(code:
                         "server.action.update.changesMightNotHaveBeenApplied")
                        } else if (result == 0) {
                            flash.message = message(code:
                                "server.action.update.svnRunning")
                        } else {
                            flash.error = message(code:
                                "server.action.update.generalError")
                        }
                    } else {
                        serverConfService.writeConfigFiles()
                        flash.error = message(code:
                                "server.action.update.cantRestartServer")
                    }
                } catch (CantBindPortException cantStopRunningServer) {
                    flash.error = cantStopRunningServer.getMessage(
                        RCU.getLocale(request))
                }
            } else {
                serverConfService.writeConfigFiles()
                flash.message = message(code:"server.action.update.changesMade")
            }
            render(view: params.view, model : prepareServerViewModel(server))

        } else {
            flash.error = message(code:"server.action.update.invalidSettings")
            // discard entity changes and redisplay the edit screen
            server.discard()
            render(view: params.view, model : prepareServerViewModel(server))
        }
    }

  
    private Map prepareServerViewModel (Server server) {

        def networkInterfaces = networkingService
            .getNetworkInterfacesWithIPAddresses()
            .collect{ it.getName() }
        Collections.sort(networkInterfaces)

        String portValue = server.port.toString()
        boolean isPrivatePort = Integer.parseInt(portValue) < 1024
        def showPortInstructions = isPrivatePort && 
            !lifecycleService.isDefaultPortAllowed()
        def isSolaris = operatingSystemService.isSolaris()
        def config = ConfigurationHolder.config

        return [
            server : server,
            isStarted: lifecycleService.isStarted(),
            networkInterfaces: networkInterfaces,
            ipv4Addresses: networkingService.getIPv4Addresses(),
            ipv6Addresses: networkingService.getIPv6Addresses(),
            addrInterfaceMap: networkingService
                .getInetAddressNetworkInterfaceMap(),
            csvnHome: config.svnedge.appHome ?
                config.svnedge.appHome : '<AppHome>',
            csvnConf: ConfigUtil.confDirPath(),
            standardPortInstructions: showPortInstructions,
            isSolaris: isSolaris,
            console_user: System.getProperty("user.name"),
            httpd_group: serverConfService.httpdGroup,
            isConfigurable: serverConfService.createOrValidateHttpdConf()
        ]
    }
}
