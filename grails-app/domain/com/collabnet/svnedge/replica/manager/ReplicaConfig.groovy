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
package com.collabnet.svnedge.replica.manager

/**
 * Defines various configuration values for the Replica.  We expect there
 * to only be one row for this, which will be updated as data changes.
 */
public class ReplicaConfig {
    
    String name
    String locationName
    Float latitude
    Float longitude
    // state is relative to the current master.  
    ApprovalState state
    
    /** 
     * Defined the refresh rate in minutes of refreshing the User Accounts from
     * the Master site.
     */
    Integer positiveExpirationRate
    /** 
     * Defines the refresh rate in minutes for the file transfer from the 
     * Master to the current replica. 
     */
    Integer negativeExpirationRate
    /**
     * Defines the refresh rate, in minutes, for the flusher to refresh 
     * all the cache data. This will verify if the expiresOn property of a
     * cached object is smaller than a determined point in time.
     */
    Integer cacheFlushPeriod

    /**
     * Defines how often the svn sync should be done (in minutes).
     */
    Integer svnSyncRate

    static constraints = {
        name(nullable: false, blank: false)
        locationName(nullable: false, blank: false)
        latitude(min: -90F, max: 90F)
        longitude(min: -180F, max: 180F)
        state(nullable: true)
        
        positiveExpirationRate(nullable:false)
        negativeExpirationRate(nullable:false)
        cacheFlushPeriod(nullable:false)
        svnSyncRate(nullable:false)
    }

    static ReplicaConfig getCurrentConfig() {
        return ReplicaConfig.get(1)
    }
}
