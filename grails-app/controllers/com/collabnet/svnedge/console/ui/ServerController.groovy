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

import java.net.NetworkInterface
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.master.ctf.CtfAuthenticationException;
import com.collabnet.svnedge.teamforge.CtfServer
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.CantBindPortException

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

    def index = { redirect(action:edit, params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [update:'POST', revert: 'POST']

    def revert = { CtfCredentialCommand ctfCredentials ->
        ctfCredentials.validate()
        def ctfServer = CtfServer.getServer()
        if (ctfCredentials.hasErrors()) {
            def formError = "You need to provide credentials with " +
                "permission to convert this server."
            render(view: 'editIntegration',
                    model: [ctfServerBaseUrl: CtfServer.getServer().baseUrl,
                        ctfCredentials: ctfCredentials, formError: formError])
        } else {
            try {
                def errors = []
                this.setupTeamForgeService.revertFromCtfMode(
                    ctfCredentials.ctfUsername, ctfCredentials.ctfPassword,
                        errors)
                if (errors) {
                    def formError = "Errors occurred while converting to " +
                        "Standalone Mode."
                    render(view: 'editIntegration',
                        model: [ctfServerBaseUrl: ctfServer.baseUrl,
                            ctfCredentials: ctfCredentials,
                            formError: formError, errorCause: errors])
                } else {
                    flash.message = "This Subversion Edge server has been " +
                        "reverted successfully from Managed to Standalone mode."
                    redirect(controller: "status", action: "index")
                }
            } catch (CtfAuthenticationException wrongCredentials) {
                def ctfBaseUrl = ctfServer.baseUrl
                def formError = "An error occurred while trying to contact " +
                    "the given TeamForge server '${ctfBaseUrl}'"
                render(view: 'editIntegration',
                        model: [ctfServerBaseUrl: ctfBaseUrl,
                            ctfCredentials: ctfCredentials,
                            formError: formError, 
                            errorCause: wrongCredentials.getMessage()])
            }
        }
    }

    def editIntegration = {
        flash.warn = "By submitting this form with the administrator's " +
            "credentials, this Subversion Edge server can be " +
            "converted back to Standalone mode..."
        def ctfCredentialsCmd = new CtfCredentialCommand()
        return [ctfServerBaseUrl: CtfServer.getServer().baseUrl,
                ctfCredentials: ctfCredentialsCmd]
    }

    def edit = {
        def server = Server.getServer()
        prepareServerViewModel(server)

    }

    def editAuthentication = {
        def server = Server.getServer()
        def msg = message(code: "server.page.edit.authentication.confirm")
        return [server: server,
                editAuthConfirmMessage: msg,
                csvnConf: serverConfService.confDirPath,
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
        // copy form params to the entity
        server.properties = params
        //In editAuthentication UI repoParentDir does not exist in Params.
        if (params.repoParentDir != null) {
          // canonicalize repo parent dir (esp to remove trailing "/" or "\"
          // that server.validate would allow)
          String repoParent = new File(params.repoParentDir).canonicalPath
          server.repoParentDir = repoParent
        }

        server.validate()
        if (server.port < 1024 ) {
           lifecycleService.clearCachedResults() 
           if (!lifecycleService.isDefaultPortAllowed()) {
            server.errors.rejectValue("port",
                "server.port.defaultValue.rejected")
           }
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
                    flash.error = cantStopRunningServer.getMessage()
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
        def showPortInstructions = Integer.parseInt(portValue) < 1024 &&
            !lifecycleService.isDefaultPortAllowed()
        def config = ConfigurationHolder.config
        
        def msg = message(code:"server.page.edit.authentication.confirm")
        return [
            server : server,
            editAuthConfirmMessage: msg,
            isStarted: lifecycleService.isStarted(),
            networkInterfaces: networkInterfaces,
            ipv4Addresses: networkingService.getIPv4Addresses(),
            ipv6Addresses: networkingService.getIPv6Addresses(),
            addrInterfaceMap: networkingService
                .getInetAddressNetworkInterfaceMap(),
            csvnHome: config.svnedge.appHome ?
                config.svnedge.appHome : '<AppHome>',
            csvnConf: serverConfService.confDirPath,
            standardPortInstructions: showPortInstructions,
            console_user: System.getProperty("user.name"),
            httpd_group: serverConfService.httpdGroup,
            isConfigurable: serverConfService.createOrValidateHttpdConf()
        ]
    }
}
