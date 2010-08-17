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
package com.collabnet.svnedge.replica.service

import com.collabnet.svnedge.replica.manager.ApprovalState
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.replica.manager.ReplicaConfig
import com.collabnet.svnedge.replica.jobs.CheckStateJob
import com.collabnet.svnedge.console.services.JobsAdminService

/**
 * This service handles registering the replica to the master.  It should also
 * start/stop jobs (through the jobsAdminService) based on whether or not
 * the master has approved the replica yet.
 */
class RegistrationService {

    boolean transactional = true

    def ctfRemoteClientService
    def userCacheStatisticsService
    def latencyStatisticsService
    def jobsAdminService

    /**
     * This can be called to change a master from active to inactive.
     */
    def inactivateMaster() {
        def defaultMaster = Master.getDefaultMaster()
        defaultMaster.isActive = false
        defaultMaster.save()
    }

    /**
     * This can be used to change a master from inactive to active.
     */
    def activateMaster() {
        def defaultMaster = Master.getDefaultMaster()
        defaultMaster.isActive = true
        defaultMaster.save()
    }

    /**
     * Resets the current replica to the 'PENDING' state.
     */
    def resetReplica() {
        def replica = ReplicaConfig.getCurrentConfig()
        replica.setState(ApprovalState.PENDING)
        replica.save()
    }

    /**
     * Attempt to register (or re-register) the Replica with the Master.
     * This should be called after the Replica is first setup, or any time
     * the Replica is updated or the Master is changed.
     */
    def registerReplica() {
        def server = Server.getServer()
        def replica = ReplicaConfig.getCurrentConfig()
        def state = ctfRemoteClientService.registerReplica(server, replica)
        handleState(ApprovalState.valueOf(state))
    }

    /**
     * Check the current state of the registration and handle it.
     * This method is called by the checkStateJob.
     */
    def checkAndHandleState() {
        def state = ctfRemoteClientService.getReplicaApprovalState()
        handleState(ApprovalState.valueOf(state))
    }

    /**
     * Handle the actions the replica should take, due to a change in the
     * Replica state.  
     */
    def handleState(state) {
        def replica = ReplicaConfig.getCurrentConfig()
        if (replica.state != state) {
            handleChangedState(state, replica)
        } else  if (state == ApprovalState.APPROVED) {
            jobsAdminService.pauseGroup(CheckStateJob.group)
            jobsAdminService.resumeGroup(JobsAdminService.REPLICA_GROUP)
        }
    }

    private def handleChangedState(state, replica) {
        replica.setState(state)
        replica.save()
        if (state == ApprovalState.APPROVED) {
            // Remove the checkState job (if present) and start up the other
            // jobs. Also activate the master.
            activateMaster()
            userCacheStatisticsService.initTrigger()
            latencyStatisticsService.initTrigger()
            jobsAdminService.pauseGroup(CheckStateJob.group)
            jobsAdminService.resumeGroup(JobsAdminService.REPLICA_GROUP)
        } else if (state == ApprovalState.DENIED 
                   || state == ApprovalState.NOT_FOUND 
                   || state == ApprovalState.REGISTRATION_FAILED) {
            // stop the checkState job, inactivate the master
            jobsAdminService.pauseGroup(JobsAdminService.REPLICA_GROUP)
            jobsAdminService.pauseGroup(CheckStateJob.group)
            inactivateMaster()
        } else if (state == ApprovalState.PENDING) {
            // ensure the checkState job is actively running
            jobsAdminService.resumeGroup(CheckStateJob.group)
        }
    }
}
