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
package com.collabnet.svnedge.integration




import com.collabnet.svnedge.integration.security.ReplicaUserDetails 
import org.springframework.security.userdetails.UserDetailsService

import org.springframework.security.GrantedAuthority
import org.springframework.security.GrantedAuthorityImpl

class ReplicaUserDetailsService implements UserDetailsService {

    def cacheManagementService

    ReplicaUserDetails loadUserByUsername(String username) {
        def userInfo = cacheManagementService?.getUserInfo(username)
        return new ReplicaUserDetails(username, userInfo.realUserName,
                                      getGrantedAuthorities(username))
    }

    private GrantedAuthority[] getGrantedAuthorities(username) {
        Collection <GrantedAuthority> auth = 
            new ArrayList<GrantedAuthority>(2)
        
        def roles = cacheManagementService?.getUserRoles(username)
        if (!roles) {
            log.info("username " + username + " has no roles!")
        }
        for (role in roles) {
            log.debug("username " + username + " has role '" + role + "'")
            auth.add(new GrantedAuthorityImpl(role))
        }
        
        return auth.toArray(new GrantedAuthority[0])
    }
}
