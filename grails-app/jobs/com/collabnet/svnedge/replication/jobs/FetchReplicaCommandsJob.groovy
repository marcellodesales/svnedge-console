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

import org.quartz.SimpleTrigger
import org.quartz.Trigger
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
    static boolean isStarted = false

    static def name = 
        "com.collabnet.svnedge.replication.jobs.FetchReplicaCommandsJob"
    static def group = JobsAdminService.REPLICA_GROUP
    static def triggerGroup = group + "_Triggers"

    // avoid re-entrance in case jobs are delayed. This will prevent multiple
    // calls to the Master.
    def concurrent = false

    static triggers = { 
        // See artf4934 static method doesn't compile correctly on 64 bit boxes
        //simple name: "FetchReplicaCommandsTrigger", group: triggerGroup, 
        //startDelay: 120000, 
        //repeatInterval:  5 * 60000
    }

    /** 
     * Schedule repeating trigger on 5 minute interval
     */
   void start() {
        if (!isStarted) {
            schedule(createTrigger("FetchReplicaCommandsTrigger", 
                     1 * 60000L, 20000L))
            isStarted = true
            log.info("Started FetchReplicaCommandsJob")
        } else {
            log.debug("FetchReplicaCommandsJob is already started")
        }
    }

    /** 
     * Create an infinitely repeating simple trigger with the given name
     * and interval.
     */
    private Trigger createTrigger(triggerName, interval, startDelay) {
        def trigger = new SimpleTrigger(triggerName, triggerGroup, 
                                        SimpleTrigger.REPEAT_INDEFINITELY, 
                                        interval)
        trigger.setJobName(name)
        trigger.setJobGroup(group)
        trigger.setStartTime(new Date(System.currentTimeMillis() + startDelay))
        return trigger
    }

    def execute() {
        def server = Server.getServer()
        if (server.mode == ServerMode.REPLICA) {
            doExecute()
        } else {
            log.debug("Skipping fetch of replication commands")
        }
    }

    private def doExecute() {
        log.info("Checking for replication commands")
        replicaCommandExecutorService.retrieveAndExecuteReplicaCommands()
    }
}
