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
package com.collabnet.svnedge.domain

import com.collabnet.svnedge.admin.LogManagementService.ConsoleLogLevel
import com.collabnet.svnedge.admin.LogManagementService.ApacheLogLevel
import com.collabnet.svnedge.domain.integration.CtfServer 
import com.collabnet.svnedge.util.ConfigUtil

/**
 * Data on how to send mail. 
 * We expect there to be only one config object defined.
 */
class MailConfiguration {

    boolean enabled = false
    String serverName = "localhost"
    int port = 25
    String authUsername
    String authPassword
    MailSecurityMethod securityMethod = MailSecurityMethod.NONE
    MailAuthMethod authMethod = MailAuthMethod.NONE
    
    static constraints = {
        serverName(nullable: false, blank: false, unique: true)
        port(min:1)
        authUsername(nullable:true)
        authPassword(nullable:true)
        authMethod(nullable:false)
        securityMethod(nullable:false)
    }
    
    static MailConfiguration getConfiguration() {
        return MailConfiguration.get(1) ?: new MailConfiguration()
    }
}

enum MailAuthMethod { NONE, PLAINTEXT, ENCRYPTED, NTLM, KERBEROS }
enum MailSecurityMethod { NONE, STARTTLS, SSL }

