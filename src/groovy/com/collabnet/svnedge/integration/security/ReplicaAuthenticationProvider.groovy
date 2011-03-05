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
package com.collabnet.svnedge.integration.security


import com.collabnet.svnedge.domain.Server 
import org.apache.log4j.Logger

import org.springframework.security.providers.AuthenticationProvider
import org.springframework.security.Authentication
import org.springframework.security.AuthenticationException
import org.springframework.security.BadCredentialsException

class ReplicaAuthenticationProvider implements AuthenticationProvider {
    Logger log = Logger.getLogger(this.getClass())

    boolean transactional = true

    // Because of the way we're defining this as a bean in resources.groovy,
    // any injected beans need to be set explicitly there.
    def cacheManagementService
    def replicaUserDetailsService

    Authentication authenticate(Authentication authentication) 
        throws AuthenticationException {
        boolean isValid = cacheManagementService?. \
            authenticateUser(authentication.getPrincipal(),
                             authentication.getCredentials())
        if (!isValid) {
            throw new BadCredentialsException("Authentication failed for " + 
                authentication.getPrincipal())
        }
        new ReplicaAuthentication(isValid, 
                                  replicaUserDetailsService?. \
                                  loadUserByUsername(authentication. \
                                                     getPrincipal()))
    }

    boolean supports(Class authentication) {
        return Server.getServer().replica
    }
} 
