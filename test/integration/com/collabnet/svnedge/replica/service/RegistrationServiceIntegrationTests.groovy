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

import grails.test.*

import com.collabnet.svnedge.replica.manager.ApprovalState
import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.replica.manager.ReplicaConfig

class RegistrationServiceIntegrationTests extends GrailsUnitTestCase {
    def jobsAdminService
    def registrationService

    def quartzScheduler

    void testActivateInactivateMaster() {
        registrationService.activateMaster()
        def master = Master.getDefaultMaster()
        assertTrue("The master should be active.", master.isActive)
        registrationService.inactivateMaster()
        master = Master.getDefaultMaster()
        assertFalse("The master should be inactive.", master.isActive)
    }

    void testResetReplica() {
        registrationService.resetReplica()
        def replica = ReplicaConfig.getCurrentConfig()
        assertEquals("The replica state should be 'PENDING'.", 
                     ApprovalState.PENDING,
                     replica.getState())
    }

    void testRegisterReplica() {
        // Will give error if an exception is thrown
        registrationService.registerReplica()
    }

    void testCheckAndHandleState() {
        // Will give test error if an exception is thrown
        registrationService.checkAndHandleState()
    }

    void testHandleState() {
        def triggers1 = jobsAdminService.getTriggerNamesInGroup("Replica")
        def triggers2 = jobsAdminService
            .getTriggerNamesInGroup("Replica_registration")
        if ((!triggers1 || triggers1.size() == 0) &&
            (!triggers2 || triggers2.size() == 0)) {
            return
        }
        // approved should leave all jobs running
        registrationService.handleState(ApprovalState.APPROVED)
        def paused = jobsAdminService.getPausedGroups()
        // TODO CTF REPLICA - should check that Replica group is running and CheckState job
        // maybe should be paused.  But for now replica related jobs are not triggered.
        // assertTrue("All jobs but one should be running." + paused, 
        //     paused.size() == 1)
        assertTrue("Registration trigger should be paused." + paused, 
            paused.contains("Replica_registration") || 
            null == jobsAdminService.getTrigger(
            "checkStateTrigger", "Registration_replica_Triggers"))

        // denied, not found, and registration failed should leave all no
        // replica jobs running
        [ApprovalState.DENIED, ApprovalState.NOT_FOUND, 
            ApprovalState.REGISTRATION_FAILED].each {

            registrationService.handleState(it)
            paused = jobsAdminService.getPausedGroups()
            assertTrue("Replica triggers should be paused. " + paused,
                       paused.contains("Replica"))
            assertTrue("Replica registration triggers should be paused." +
                paused, paused.contains("Replica_registration"))
        }

        // pending should mean only checkStateJob is running
        registrationService.handleState(ApprovalState.PENDING)
        paused = jobsAdminService.getPausedGroups()
        assertTrue("Replica triggers should be paused. " + paused, 
                   paused.contains("Replica"))
        assertTrue("Only replica triggers should be paused. " + paused, 
                   paused.size() == 1)
    }

    def getNumberOfTriggerGroupsRunning() {
        def total = new HashSet(Arrays.asList(quartzScheduler
                                              .getTriggerGroupNames()))
        def paused = quartzScheduler.getPausedTriggerGroups()
        total.removeAll(paused)
        return total.size()
    }
}
