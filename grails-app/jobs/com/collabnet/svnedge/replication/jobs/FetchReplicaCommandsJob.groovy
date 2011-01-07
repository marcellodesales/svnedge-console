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
package com.collabnet.svnedge.replication.jobs

import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.services.JobsAdminService
import com.collabnet.svnedge.console.ServerMode

/**
 * Fetch the replica commands from the server.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
class FetchReplicaCommandsJob {

    def replicaCommandExecutorService

    static def group = JobsAdminService.REPLICA_GROUP

    // avoid re-entrance in case jobs are delayed. This will prevent multiple
    // calls to the Master.
    def concurrent = false

    static triggers = { 
    
        simple name: "fetchActionCommandsTrigger", group: group + "_Triggers", 
        startDelay: 120000, 
        repeatInterval:  5 * 60000
   
    }

    def execute() {
        def server = Server.getServer()
        if (server.mode == ServerMode.REPLICA) {
            doExecute()
        }
    }

    private def doExecute() {
        log.info("Checking for replication commands")
        replicaCommandExecutorService.retrieveAndExecuteReplicaCommands()
    }
}
