/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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

/**
 * This class stores extra network configuration items
 */
public class NetworkConfiguration {

    String httpProxyHost
    Integer httpProxyPort
    String httpProxyUsername
    String httpProxyPassword
    
    /**
     * factory to obtain singleton row
     * @return pseudo singleton instance or null
     */
    static NetworkConfiguration getCurrentConfig() {
        def networkConfigs = NetworkConfiguration.list()
        if (networkConfigs) {
            return networkConfigs.last()
        }
        else {
            return null
        }
    }
    
     static constraints = {
        httpProxyHost(nullable: false, blank: false)
        httpProxyPort(nullable: false, min:1, max: 65535 )
        httpProxyUsername(nullable: true, blank: true)
        httpProxyPassword(nullable: true, blank: true)
     }

}

