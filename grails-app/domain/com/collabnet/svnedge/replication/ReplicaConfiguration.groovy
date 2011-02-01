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
package com.collabnet.svnedge.replication


import com.collabnet.svnedge.replica.manager.ApprovalState

/**
 * This stores the replication configuration
 */
public class ReplicaConfiguration {

    // server with which to sync
    String svnMasterUrl

    // simple title or name
    String name

    // description (eg, location)
    String description
    String message

    // the id assigned by CTF to this replica
    String systemId
    
    // defines how often the svn sync should be done (in minutes).
    Integer svnSyncRate
    
    // state is relative to the current master.  
    ApprovalState approvalState


    static constraints = {
        svnMasterUrl(nullable:true)
        systemId(nullable:false)
        description(nullable:false)
    }

    /**
     * pseudo singleton provider
     * @return
     */
    static ReplicaConfiguration getCurrentConfig() {
        def replicaConfigRows = ReplicaConfiguration.list()
        if (replicaConfigRows) {
            return replicaConfigRows.last()
        }
        else {
            return null
        }
    }
}
