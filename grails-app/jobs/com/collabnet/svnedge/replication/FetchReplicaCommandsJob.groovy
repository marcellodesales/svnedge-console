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

import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.collabnet.svnedge.console.JobsAdminService
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.ServerMode 
import com.collabnet.svnedge.domain.integration.CtfServer 
import com.collabnet.svnedge.domain.integration.ReplicaConfiguration 
import com.collabnet.svnedge.replication.command.CommandsExecutionContext

/**
 * Fetch the replica commands from the Replica manager server.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */
class FetchReplicaCommandsJob implements ApplicationContextAware {

    // avoid re-entrance in case jobs are delayed. This will prevent multiple
    // calls to the Master.
    def concurrent = false

    def backgroundService
    def securityService
    def ctfRemoteClientService
    def replicaCommandSchedulerService
    def commandResultDeliveryService

    def static final JOB_NAME = 
        "com.collabnet.svnedge.replication.jobs.FetchReplicaCommandsJob"

    def static final JOB_GROUP_NAME = JobsAdminService.REPLICA_GROUP

    def static final TRIGGER_GROUP = JOB_GROUP_NAME + "_Triggers"

    def static final TRIGGER_NAME = "FetchReplicaCommandsTrigger"

    def static final INITIAL_DELAY_SEC = 2

    def config = ConfigurationHolder.config

    static triggers = { 
        // See artf4934 static method doesn't compile correctly on 64 bit boxes
        //simple name: "FetchReplicaCommandsTrigger", group: triggerGroup, 
        //startDelay: 120000, 
        //repeatInterval:  5 * 60000
    }

    /**
     * If the job has been started.
     */
    static boolean isStarted = false
    /**
     * Taking advantage of the grails injection to write the 
     * setApplicationContext method from the attribute.
     */
    ApplicationContext applicationContext
    /**
     * The locale of the replica command execution.
     */
    CommandsExecutionContext executionContext

    /** 
     * Schedule with the current command pool rate from the 
     * ReplicaConfiguration.
     */
    void start() {
        if (!isStarted) {
            def replica = ReplicaConfiguration.getCurrentConfig()
            def triggerInstance = makeTrigger(replica.commandPollRate)
            schedule(triggerInstance)
            isStarted = true
            log.info("Started FetchReplicaCommandsJob")

        } else {
            log.debug("FetchReplicaCommandsJob is already started")
        }
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
        if (server.mode != ServerMode.REPLICA) {
            log.debug("Skipping fetch of replication commands")
            return
        }
        log.debug("Checking for replica commands...")

        def locale = Locale.getDefault()
        def ctfServer = CtfServer.getServer()
        def ctfPassword = securityService.decrypt(ctfServer.ctfPassword)

        def userSessionId
        try {
            userSessionId = ctfRemoteClientService.login(ctfServer.baseUrl,
                ctfServer.ctfUsername, ctfPassword, locale)

        } catch (Exception cantConnectCtfMaster) {
            log.error "Can't retrieve queued commands from the CTF replica " +
                "manager ${ctfServer.baseUrl}: " + cantConnectCtfMaster.getMessage()
            commandResultDeliveryService.stopDelivering()
            return
        }

        def executionContext = new CommandsExecutionContext()
        executionContext.appContext = applicationContext
        executionContext.userSessionId = userSessionId
        executionContext.ctfBaseUrl = ctfServer.baseUrl
        executionContext.locale = locale
        executionContext.logsDir = new File(config.svnedge.logsDirPath + "")
        def replica = ReplicaConfiguration.getCurrentConfig()
        executionContext.replicaSystemId = replica.systemId

        // As the fetch job can retrieve commands, there is communication.
        // Signal the delivery service to deliver any pending responses.
        commandResultDeliveryService.restartDelivering(executionContext)

        log.debug("Command Execution Context: $executionContext")
        try {
            //receive the commands from ctf
            def queuedCommands = ctfRemoteClientService.getReplicaQueuedCommands(
                executionContext.ctfBaseUrl, executionContext.userSessionId,
                executionContext.replicaSystemId, executionContext.locale)

            if (queuedCommands && queuedCommands.size() > 0) {
                log.debug("There are ${queuedCommands.size()} commands queued.")
                // execute the command using the background service.
                replicaCommandSchedulerService.offer(queuedCommands,
                        executionContext)

            } else {
                log.debug("No queued commands for this replica...")
            }

        } catch (Exception replicaManagerError) {
            log.error("There was a problem while trying to fetch queued " + 
                "commands: " + replicaManagerError.getMessage())
        }
    }

}
