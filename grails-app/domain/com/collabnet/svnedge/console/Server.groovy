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
package com.collabnet.svnedge.console

import com.collabnet.svnedge.teamforge.CtfServer
import com.collabnet.svnedge.console.services.LogManagementService.ConsoleLogLevel
import com.collabnet.svnedge.console.services.LogManagementService.ApacheLogLevel

/**
 * Defines the svn server and console. 
 * We expect there to be only one Server defined.
 */
class Server {

    // this property represents the apache SSL state
    boolean useSsl = false;
    // this property represents the console SSL state
    Boolean useSslConsole = false;
    /**
     * When server is used as a replica, hostname uniquely identifies the 
     * Replica to the Master.  Care should be take when changing this value.
     */
    String hostname
    
    int port
    String repoParentDir
    boolean defaultStart
    boolean allowAnonymousReadAccess
    boolean ldapEnabled
    boolean fileLoginEnabled 
    String ldapServerHost
    int ldapServerPort
    String ldapAuthBasedn
    String ldapAuthBinddn
    String ldapAuthBindPassword
    String ldapLoginAttribute
    String ldapSearchScope
    String ldapFilter
    String ldapSecurityLevel
    boolean ldapServerCertVerificationNeeded
    boolean replica
    String netInterface
    String ipAddress
    String adminName
    String adminEmail
    String adminAltContact
    ServerMode mode = ServerMode.STANDALONE

    Integer pruneLogsOlderThan
    ApacheLogLevel apacheLogLevel = ApacheLogLevel.WARN
    ConsoleLogLevel consoleLogLevel = ConsoleLogLevel.WARN

    String svnURL() {
        return urlPrefix() + "/svn/"
    }
    
    String viewvcURL(String repoName) {
        String url = null
        if (ServerMode.MANAGED.equals(mode)) {
            // TODO We need ProjectPath, which means we need a user session id, as ctf won't give
            // this to us, if the user doesn't have access to the repo
            def projectPath = null
            if (projectPath) {
                url = CtfServer.getServer().webAppUrl + 
                    "/scm/do/viewRepositorySource/" + projectPath + "/scm." + 
                    repoName
            }
        } else {
            url =  urlPrefix() + "/viewvc" + (repoName ? "/" + repoName : "") + "/"   
        }
        url
    }
    
    String urlPrefix() {
        def scheme = useSsl ? "https" : "http"
        String port = useSsl ? 
            (port == 443) ? "" : ":" + port : (port == 80) ? "" : ":" + port
        return scheme + "://" + server.hostname + port   
    }

    static constraints = {
        hostname(nullable: false, blank: false, unique: true)
        port(min:80)
        repoParentDir(nullable: false, blank: false, 
                      validator: { val, obj ->
                      def dirFile = new File(val)
                      return dirFile.exists() && dirFile.isDirectory()
        })
        ipAddress(nullable: false, blank: false)
        netInterface(nullable: false, blank: false)
        adminName(nullable: true)
        adminEmail(nullable: false, blank: false, email: true)
        adminAltContact(nullable: true)
        ldapServerHost(nullable: true, validator: { val, obj ->
            if (obj.ldapEnabled) {
                if (!val || val.equals("")) {
                    return ['blank']
                }
            }
        })
        ldapServerPort(nullable: true, min:1, validator: { val, obj ->
            if (obj.ldapEnabled) {
                if (!val || val.equals("")) {
                    return ['blank']
                }
            }
        })
        ldapAuthBasedn(nullable: true)
        ldapAuthBinddn(nullable: true)
        ldapAuthBindPassword(nullable: true)
        ldapLoginAttribute(nullable: true)
        ldapSearchScope(nullable: true)
        ldapFilter(nullable: true)
        ldapSecurityLevel(nullable: true)
        mode(nullable:false)
        pruneLogsOlderThan(nullable: true, min:0)
        ldapEnabled(validator: { val, obj ->
            // Ensure that some authentication is chosen
            if (!val && !obj.fileLoginEnabled ) {
                return ['chooseAuth']
            }
        })
    }
    
    static Server getServer() {
        return Server.get(1)
    }
    
    boolean managedByCtf() {
        return this.mode == ServerMode.MANAGED
    }
    
    static CtfServer getManagedServer() {
        return CtfServer.getServer()
    }
}

enum ServerMode { STANDALONE, CONVERTING_TO_MANAGED, MANAGED, REPLICA }

