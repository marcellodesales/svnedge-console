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
import com.collabnet.svnedge.replication.ReplicaConfiguration;

/**
 * Fetch the replica commands from the Master server.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
class FetchReplicaCommandsJob {

    def replicaCommandExecutorService

    def static final JOB_NAME = 
        "com.collabnet.svnedge.replication.jobs.FetchReplicaCommandsJob"

    def static final JOB_GROUP_NAME = JobsAdminService.REPLICA_GROUP

    def static final TRIGGER_GROUP = JOB_GROUP_NAME + "_Triggers"

    def static final TRIGGER_NAME = "FetchReplicaCommandsTrigger"

    def static final INITIAL_DELAY_SEC = 2

    /**
     * If the job has been started.
     */
    static boolean isStarted = false

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
     * Schedule with the current command pool rate from the 
     * ReplicaConfiguration.
     */
    void start() {
        if (!isStarted) {
            def triggerInstance = makeTrigger()
            schedule(triggerInstance)
            isStarted = true
            log.info("Started FetchReplicaCommandsJob")
        } else {
            log.debug("FetchReplicaCommandsJob is already started")
        }
    }

    /** 
     * Create an infinitely repeating simple trigger with the current
     * replica server commandPoolRate with a delay of 3 seconds
     */
    def Trigger makeTrigger() {
        def replica = ReplicaConfiguration.getCurrentConfig()
        return makeTrigger(replica.commandPollRate)
    }

    /** 
     * Create an infinitely repeating simple trigger with the current
     * replica server commandPoolRate with a delay of INITIAL_DELAY_SEC seconds
     */
    def static Trigger makeTrigger(commandPollInterval) {
        def interval = commandPollInterval * 1000L
        def startDelay = INITIAL_DELAY_SEC * 1000L
        def trigger = new SimpleTrigger(TRIGGER_NAME, TRIGGER_GROUP, 
            SimpleTrigger.REPEAT_INDEFINITELY, interval)
        trigger.setJobName(JOB_NAME)
        trigger.setJobGroup(JOB_GROUP_NAME)
        trigger.setStartTime(new Date(System.currentTimeMillis() + startDelay))
        return trigger
    }

    /**
     * Called by the quartzService once it is read to be fired.
     */
    def execute() {
        def server = Server.getServer()
        if (server.mode == ServerMode.REPLICA) {
            log.info("Checking for replication commands")
            replicaCommandExecutorService.retrieveAndExecuteReplicaCommands()

        } else {
            log.debug("Skipping fetch of replication commands")
        }
    }
}
