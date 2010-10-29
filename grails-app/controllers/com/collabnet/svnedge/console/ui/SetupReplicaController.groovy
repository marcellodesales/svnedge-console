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
import org.apache.commons.beanutils.BeanUtils
import com.collabnet.svnedge.master.ctf.CtfAuthenticationException
import com.collabnet.svnedge.teamforge.CtfConversionBean


class CtfConnectionCommand {

    String ctfUrl
    String ctfUsername
    String ctfPassword

    static constraints = {
        ctfUrl(blank: false)
        ctfUsername(blank: false)
        ctfPassword(blank: false)
    }
}

class ReplicaInfoCommand {

    String svnMasterUrl
    String description
    String message

    static constraints = {
        svnMasterUrl(blank: false)
        description(blank: false)
    }
}

@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_SYSTEM'])
class SetupReplicaController {

    private static CTF_CONNECTION_SESSION_KEY = "ctfConnection"
    private static REPLICA_INFO_SESSION_KEY = "replicaInfo"

    def setupTeamForgeService
    
    /**
     * default view is the "TeamForge Mode" intro 
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

            def cmd = getCtfConnectionCommand()
            BeanUtils.copyProperties(cmd, input)
            
            // proceed
            // 1. attempt auth at ctf
            // 2. store credentials / session key 
            // 2. fetch list of integration svrs
            try {
                setupTeamForgeService.confirmConnection(getConversionBean())
            }
            catch (MalformedURLException e) {
                input.errors.rejectValue('ctfUrl', 'ctfRemoteClientService.host.malformedUrl',
                        [input.ctfUrl] as Object[], 'bad url')
            }
            catch (UnknownHostException e) {
                input.errors.rejectValue('ctfUrl', 'ctfRemoteClientService.host.unknown.error',
                        [new URL(input.ctfUrl).host] as Object[], 'unknown host')
            }
            catch (NoRouteToHostException e) {
                input.errors.rejectValue('ctfUrl', 'ctfRemoteClientService.host.unreachable.error',
                        [input.ctfUrl] as Object[], 'no route')
            }
            catch (CtfAuthenticationException e) {
                input.errors.rejectValue('ctfUsername', 'ctfRemoteClientService.auth.error',
                        [input.ctfUrl] as Object[], 'bad credentials')
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

        if (input.hasErrors()) {
            // return to input view with errors
            render([view: "replicaSetup", model: [cmd: input, integrationServers: getIntegrationServers()]])
        }



        def cmd = getReplicaInfoCommand()
        BeanUtils.copyProperties(cmd, input)

        // todo: real data
        [ctfUrl: "https://path.to.ctf/",
                svnMasterUrl: "https://path.to.svn/svn/repos",
                svnReplicaCheckout: "svn co https://path.to.replica/svn/repos -username=admin"
        ]
    }

    private List getIntegrationServers() {

        // todo: real data
        ["system1", "system2", "system3"]

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
     * obtains a "ctf conversion bean" from the session command objects 
     * @return 
     */
    private CtfConversionBean getConversionBean() {
        
        def connectionCmd = getCtfConnectionCommand() 
        
        CtfConversionBean con = new CtfConversionBean()
        con.ctfURL = connectionCmd.ctfUrl
        con.ctfUsername = connectionCmd.ctfUsername
        con.ctfPassword = connectionCmd.ctfPassword
        con.userLocale = request.locale
        
        return con
        
    }

}
