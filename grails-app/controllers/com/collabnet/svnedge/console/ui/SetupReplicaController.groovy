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

import org.codehaus.groovy.grails.plugins.springsecurity.Secured

import com.collabnet.svnedge.master.ctf.CtfAuthenticationException
import com.collabnet.svnedge.teamforge.CtfConversionBean
import com.collabnet.svnedge.replica.manager.ReplicaConfig
import com.collabnet.svnedge.master.RemoteMasterException
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.teamforge.CtfConnectionBean
import com.collabnet.svnedge.replication.ReplicaConversionBean
import org.springframework.beans.BeanUtils


class CtfConnectionCommand {

    String ctfURL
    String ctfUsername
    String ctfPassword

    static constraints = {
        ctfURL(blank: false)
        ctfUsername(blank: false)
        ctfPassword(blank: false)
    }
}

class ReplicaInfoCommand {

    String svnMasterURL
    String description
    String message

    static constraints = {
        svnMasterURL(blank: false)
        description(blank: false)
    }
}

@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_SYSTEM'])
class SetupReplicaController {

    private static String CTF_CONNECTION_SESSION_KEY = "ctfConnectionCommand"
    private static String REPLICA_INFO_SESSION_KEY = "replicaInfoCommand"
    private static String REPLICA_CONVERSION_BEAN_SESSION_KEY = "replicaConversionBean"

    def replicaService
    def authenticateService
    
    /**
     * default view is actually the "TeamForge Mode" intro, so forward 
     */
    def index = {
        forward(controller: "setupTeamForge", action: "index")
    }

    /**
     * Collect CTF credentials 
     */
    def ctfInfo = {
        [cmd: getCtfConnectionCommand()]
    }

    /**
     * Collect replica info 
     */
    def replicaSetup = { CtfConnectionCommand input ->

        if (!input.hasErrors()) {

            try {
                // copy input params to the conversion bean
                CtfConnectionBean conn = getConversionBean(input)
                // verify connection
                replicaService.confirmCtfConnection(conn)
                
                // save form input to session (in case tab is re-enterd)
                def cmd = getCtfConnectionCommand()
                BeanUtils.copyProperties(input, cmd)
            }
            catch (MalformedURLException e) {
                input.errors.rejectValue('ctfURL', 'ctfRemoteClientService.host.malformedUrl',
                        [input.ctfURL] as Object[], 'bad url')
            }
            catch (UnknownHostException e) {
                input.errors.rejectValue('ctfURL', 'ctfRemoteClientService.host.unknown.error',
                        [new URL(input.ctfURL).host] as Object[], 'unknown host')
            }
            catch (NoRouteToHostException e) {
                input.errors.rejectValue('ctfURL', 'ctfRemoteClientService.host.unreachable.error',
                        [input.ctfURL] as Object[], 'no route')
            }
            catch (CtfAuthenticationException e) {
                input.errors.rejectValue('ctfUsername', 'ctfRemoteClientService.auth.error',
                        [input.ctfUsername] as Object[], 'bad credentials')
            }
        }

        if (input.hasErrors()) {
            // return to input view with errors
            render([view: "ctfInfo", model: [cmd: input]])
        }

        // success logging in 
        [cmd: getReplicaInfoCommand(), integrationServers: getIntegrationServers()]
    }

    /**
     * Do conversion, show confirmation 
     */
    def confirm = { ReplicaInfoCommand input ->

        def server
        def repoName
        def userName
        
        if (!input.hasErrors()) {
            
            try {
                // copy input params to the conversion bean
                ReplicaConversionBean conn = getConversionBean(input)

                // register the replica
                replicaService.registerReplica(conn)
                
                // save input for form re-entry
                def cmd = getReplicaInfoCommand()
                BeanUtils.copyProperties(input, cmd)

                // prepare confirmation data
                server = Server.getServer()
                repoName = (Repository.list()) ? Repository.list()[0].name : "example"
                userName = authenticateService.principal().getUsername()
                
                return [ctfURL: getCtfConnectionCommand().ctfURL,
                        svnMasterURL: getReplicaInfoCommand().svnMasterURL,
                        svnReplicaCheckout: "svn co ${server.svnURL()}${repoName} ${repoName} --username=${userName}"
                        ]
            }
            catch (Exception e) {
                log.error("Unable to register replica", e)
                input.errors.rejectValue('svnMasterURL', 'setupTeamForge.page.error.general',
                        [new URL(input.svnMasterURL).host] as Object[], 'error registering')
            }
        }
        
        if (input.hasErrors()) {
            // return to input view with errors
            render([view: "replicaSetup", model: [cmd: input, integrationServers: getIntegrationServers()]])
        }
        else {
        
            [ctfURL: getCtfConnectionCommand().ctfURL,
                    svnMasterURL: getReplicaInfoCommand().svnMasterURL,
                    svnReplicaCheckout: "svn co ${server.svnURL()}${repoName} ${repoName} --username=${userName}"
                    ]
        }
    }

    private List getIntegrationServers() {

        // todo: real data
        ["https://system1/svn", "https://system2/svn", "https://system3/svn"]

    }

    private CtfConnectionCommand getCtfConnectionCommand() {

        CtfConnectionCommand cmd = session.getAttribute(CTF_CONNECTION_SESSION_KEY)
        if (!cmd) {
            cmd = new CtfConnectionCommand()
            session.setAttribute(CTF_CONNECTION_SESSION_KEY, cmd)
        }
        return cmd

    }

    private ReplicaInfoCommand getReplicaInfoCommand() {

        ReplicaInfoCommand cmd = session.getAttribute(REPLICA_INFO_SESSION_KEY)
        if (!cmd) {
            cmd = new ReplicaInfoCommand()
            session.setAttribute(REPLICA_INFO_SESSION_KEY, cmd)
        }
        return cmd
    }
    
    /**
     * obtains a ctf conversion bean from the session 
     * @return 
     */
    private ReplicaConversionBean getConversionBean() {
        
        ReplicaConversionBean bean = session.getAttribute(REPLICA_CONVERSION_BEAN_SESSION_KEY)
        if (!bean) {
            bean = new ReplicaConversionBean()
            bean.userLocale = request.locale
            session.setAttribute(REPLICA_CONVERSION_BEAN_SESSION_KEY, bean)
        }
        return bean
        
    }

    /**
     * obtain a ctf conersion bean from the session, and apply properties from command
     * @param cmd the controller action command bean 
     */
    private ReplicaConversionBean getConversionBean(CtfConnectionCommand cmd) {

        ReplicaConversionBean b = getConversionBean();
        b.ctfURL = cmd.ctfURL
        b.ctfUsername = cmd.ctfUsername
        b.ctfPassword = cmd.ctfPassword
        return b
    }

    /**
     * obtain a ctf conersion bean from the session, and apply properties from command
     * @param cmd the controller action command bean 
     */
    private ReplicaConversionBean getConversionBean(ReplicaInfoCommand cmd) {

        ReplicaConversionBean b = getConversionBean();
        b.svnMasterURL = cmd.svnMasterURL
        b.description = cmd.description
        b.message = cmd.message
        return b
    }

}
