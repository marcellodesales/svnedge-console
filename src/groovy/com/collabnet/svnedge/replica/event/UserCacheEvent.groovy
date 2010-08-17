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
package com.collabnet.svnedge.replica.event

class UserCacheEvent extends ReplicaEvent {
    // event types
    static String HIT = "hit"
    static String MISS = "miss"

    // cache types
    static String AUTHN = "authn"
    static String AUTHZ = "authz"
    static String INFO = "info"
    static String ROLE = "role"
    
    static List CACHE_TYPES = [[name: "Authentication", type: AUTHN], 
        [name: "Authorization", type: AUTHZ], 
        [name: "User Info", type: INFO], 
        [name: "User Role", type: ROLE]]

    static List EVENT_TYPES = [[name: "Hits", type: HIT], 
        [name: "Misses", type: MISS]]

    def username
    def cacheType
    def eventType

    public UserCacheEvent(username, cacheType, eventType) {
        super()
        this.username = username
        this.cacheType = cacheType
        this.eventType = eventType
    }

    String toString() {
        return super.toString() + ": " + eventType + " for " + username + 
            " on " + cacheType + "."
    }
}
