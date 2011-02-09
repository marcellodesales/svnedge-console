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
package com.collabnet.svnedge.replication.command

import org.apache.log4j.Logger

import com.collabnet.svnedge.replica.manager.ApprovalState
import com.collabnet.svnedge.replication.ReplicaConfiguration
import com.collabnet.svnedge.replication.jobs.FetchReplicaCommandsJob
import static com.collabnet.svnedge.console.services.JobsAdminService.REPLICA_GROUP

/**
 * This command updates the state of the replica server, changing the name and
 * description of the replica server.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public class ReplicaPropsUpdateCommand extends AbstractReplicaCommand {

    private Logger log = Logger.getLogger(getClass())

    def constraints() {
        def replica = ReplicaConfiguration.getCurrentConfig()
        if (replica.approvalState != ApprovalState.APPROVED) {
            throw new IllegalStateException("The replica needs to be " +
                "approved before updating its properties.")
        }

        // Verify if the parameter "scmUrl" exists.
        if (!this.params["name"] && !this.params["description"] && 
                !this.params.commandPollPeriod && 
                !this.params.commandConcurrencyLong && 
                !this.params.commandConcurrencyShort) {
            throw new IllegalStateException("The command does not have any " +
                "of the required parameters.")
        }
    }

    def execute() {
        log.debug("Acquiring the replica configuration instance...")

        def replica = ReplicaConfiguration.getCurrentConfig()

        // update the name property
        if (this.params.name) {
            replica.name = this.params.name
        }

        // update the description property
        if (this.params.description) {
            replica.description = this.params.description
        }

        // update the command pool rate
        def poolRate = this.params.commandPollPeriod
        if (poolRate && poolRate.toInteger() > 0 && 
                poolRate.toInteger() != replica.commandPollRate) {

            replica.commandPollRate = poolRate.toInteger()

            // reschedule the job with the updated rate
            def jobsAdminService = getService("jobsAdminService")
            try {
                def interval = poolRate.toInteger() * 1000L
                jobsAdminService.rescheduleJob(
                    FetchReplicaCommandsJob.TRIGGER_NAME,
                    FetchReplicaCommandsJob.TRIGGER_GROUP, interval)

            } catch (Exception e) {
                log.error("Tried to reschedule the trigger and nothing happened"
                    , e.getCause())
                throw new IllegalStateException(e.getCause())
            }
        }

        // update the max number of long-running commands property
        def maxLongRunningCmds = this.params.commandConcurrencyLong
        if (maxLongRunningCmds && maxLongRunningCmds.toInteger() > 0 && 
                maxLongRunningCmds.toInteger() != replica.maxLongRunningCmds) {

            replica.maxLongRunningCmds = maxLongRunningCmds.toInteger()
        }

        // update the max number of short-running commands property
        def maxShortRunningCmds = this.params.commandConcurrencyShort
        if (maxShortRunningCmds && maxShortRunningCmds.toInteger() > 0 && 
                maxShortRunningCmds.toInteger() != replica.maxShortRunningCmds) {

            replica.maxShortRunningCmds = maxShortRunningCmds.toInteger()
        }

        log.debug("Trying to flush the saved replica properties...")
        replica.save(flush:true)
    }

    def undo() {
       log.error("Execute failed... Nothing to undo...")
    }
}
