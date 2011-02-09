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
 * This stores the replication configuration.
 */
public class ReplicaConfiguration {

    /**
     * The URL of the Master SVN
     */
    String svnMasterUrl
    /**
     * The name of the replica.
     */
    String name
    /**
     * The description of the replica: location, purpose, etc.
     */
    String description
    /**
     * The message for the admin TODO: NOT NECESSARY
     */
    String message
    /**
     * the id assigned by CTF to this replica server
     */
    String systemId
    /**
     * state is relative to the current master.
     */
    ApprovalState approvalState
    /**
     * The pool rate in seconds.
     */
    Integer commandPollRate = 1

    static constraints = {
        svnMasterUrl(nullable:true)
        systemId(nullable:false)
        description(nullable:false)
        commandPollRate(nullable:false)
    }

    /**
     * @return pseudo singleton provider
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
