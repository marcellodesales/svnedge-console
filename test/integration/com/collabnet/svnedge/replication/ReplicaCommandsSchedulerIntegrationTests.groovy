/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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


import com.collabnet.svnedge.replication.ReplicaCommandSchedulerService;

import grails.test.*

class ReplicaCommandsSchedulerIntegrationTests extends GrailsUnitTestCase {

    def replicaCommandSchedulerService

    def remotecmdexecs = Collections.synchronizedList(new LinkedList<Map<String, String>>())

    def ReplicaCommandsSchedulerIntegrationTests() {
        remotecmdexecs << [id:'cmdexec1001', repoName:'repo1', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1009', repoName:'null', code:'replicaPause', 
            params:[until:'2011-01-22']]
        remotecmdexecs << [id:'cmdexec1002', repoName:'repo1', code:'repoUpdateProp']
        remotecmdexecs << [id:'cmdexec1006', repoName:'repo2', code:'repoUpdateProp']
        remotecmdexecs << [id:'cmdexec1004', repoName:'null', code:'replicaUpdateProp'
            , params:[name:'Replica Brisbane']]
        remotecmdexecs << [id:'cmdexec1000', repoName:'null', code:'replicaApprove', 
            params:[name:'replica title', desc:'super replica']]
        remotecmdexecs << [id:'cmdexec1007', repoName:'repo3', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1008', repoName:'null', code:'replicaUpdateProp'
            , params:[maxReplicacmdexecs:3, maxRepositorycmdexecs: 10]]
        remotecmdexecs << [id:'cmdexec1005', repoName:'repo2', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1003', repoName:'repo3', code:'repoSync']
    }
    
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testInitialState() {
        assertEquals "The initial queued commands size is incorrect", 0,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The initial executing number of commnads is incorrect", 0,
            replicaCommandSchedulerService.getExecutingCommandsSize()
    }

    void testOffer() {
        replicaCommandSchedulerService.offer(remotecmdexecs)
        assertEquals "The size is incorrect", 10,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The size is incorrect", 0,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        def categories = replicaCommandSchedulerService.getCategorizedCommandQueues().keySet()
        assertTrue categories.containsAll(["replicaServer", "repo1", "repo2", "repo3"])

        def cat = "replicaServer"
        def cmd = replicaCommandSchedulerService.getNextCommandFromCategory(cat)
        def nextId = "cmdexec1000"
        assertEquals "The next command is incorrect for $cat", nextId, cmd.id
        assertFalse "No command should be running after offer for $cat",
            replicaCommandSchedulerService.isThereCommandRunning(cat)

        cat = "repo1"
        cmd = replicaCommandSchedulerService.getNextCommandFromCategory(cat)
        nextId = "cmdexec1001"
        assertEquals "The next command is incorrect for $cat", nextId, cmd.id
        assertFalse "No command should be running after offer for $cat",
            replicaCommandSchedulerService.isThereCommandRunning(cat)

        cat = "repo2"
        cmd = replicaCommandSchedulerService.getNextCommandFromCategory(cat)
        nextId = "cmdexec1005"
        assertEquals "The next command is incorrect for $cat", nextId, cmd.id
        assertFalse "No command should be running after offer for $cat",
            replicaCommandSchedulerService.isThereCommandRunning(cat)

        cat = "repo3"
        cmd = replicaCommandSchedulerService.getNextCommandFromCategory(cat)
        nextId = "cmdexec1003"
        assertEquals "The next command is incorrect for $cat", nextId, cmd.id
        assertFalse "No command should be running after offer for $cat",
            replicaCommandSchedulerService.isThereCommandRunning(cat)
    }

    void testSchedulingFirstCommandsAllCategory() {
        replicaCommandSchedulerService.cleanCommands()
        replicaCommandSchedulerService.offer(remotecmdexecs)
        def schledCmd = replicaCommandSchedulerService.scheduleNextCommand()

        assertEquals "The size is incorrect", 9,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The size is incorrect", 1,
            replicaCommandSchedulerService.getExecutingCommandsSize()
        assertEquals "The next command should be the first one", "cmdexec1001",
            schledCmd.id
        assertEquals "The next command category is repo1", "repo1",
            schledCmd.repoName

        schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        assertEquals "The size is incorrect", 8,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The size is incorrect", 2,
            replicaCommandSchedulerService.getExecutingCommandsSize()
        assertEquals "The next command should be the second one", "cmdexec1000",
            schledCmd.id
        assertEquals "The next command category is replica server", "null",
            schledCmd.repoName

        schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        assertEquals "The size is incorrect", 7,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The size is incorrect", 3,
            replicaCommandSchedulerService.getExecutingCommandsSize()
        assertEquals "The next command should be the second one", "cmdexec1005",
            schledCmd.id
        assertEquals "The next command category is repo1", "repo2",
            schledCmd.repoName

        schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        assertEquals "The size is incorrect", 6,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The size is incorrect", 4,
            replicaCommandSchedulerService.getExecutingCommandsSize()
        assertEquals "The next command should be the second one", "cmdexec1003",
            schledCmd.id
        assertEquals "The next command category is repo1", "repo3",
            schledCmd.repoName
    }

    void testExecuteAllCommands() {
        replicaCommandSchedulerService.cleanCommands()
        replicaCommandSchedulerService.offer(remotecmdexecs)
        def schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        def executingCommands = []
        while (schledCmd) {
            executingCommands << schledCmd
            schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        }
        println "Executing commands: $executingCommands"
        // first step executing 4 commands for the 4 categories
        assertEquals "The queued commands must be empty", 6,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 4,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        // terminating the execution of 4 commands
        executingCommands.reverse().each { command ->
            println(command)
            replicaCommandSchedulerService.removeTerminatedCommand(command.id)
        }
        println "Terminated commands: $executingCommands"
        assertEquals "The queued commands must be empty", 6,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 0,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        executingCommands = []
        schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        while (schledCmd) {
            executingCommands << schledCmd
            schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        }
        println "Executing commands: $executingCommands"
        assertEquals "The queued commands must be empty", 2,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 4,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        executingCommands.reverse().each { command ->
            println(command)
            replicaCommandSchedulerService.removeTerminatedCommand(command.id)
        }
        println "Terminated commands: $executingCommands"
        assertEquals "The queued commands must be empty", 2,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 0,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        executingCommands = []
        schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        while (schledCmd) {
            executingCommands << schledCmd
            schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        }
        // since 2 replica commands can't be executing at the same time
        println "Executing commands: $executingCommands"
        assertEquals "The queued commands must be empty", 1,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 1,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        executingCommands.reverse().each { command ->
            println(command)
            replicaCommandSchedulerService.removeTerminatedCommand(command.id)
        }
        println "Terminated commands: $executingCommands"
        assertEquals "The queued commands must be empty", 1,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 0,
            replicaCommandSchedulerService.getExecutingCommandsSize()
            
        executingCommands = []
        schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        while (schledCmd) {
            executingCommands << schledCmd
            schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        }
        // the last command to be executing 
        println "Executing commands: $executingCommands"
        assertEquals "The queued commands must be empty", 0,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 1,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        executingCommands.reverse().each { command ->
            println(command)
            replicaCommandSchedulerService.removeTerminatedCommand(command.id)
        }
        println "Terminated commands: $executingCommands"
        assertEquals "The queued commands must be empty", 0,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 0,
            replicaCommandSchedulerService.getExecutingCommandsSize()
    }

    void testOfferExistingCommands() {
        // commands may be re-sent from the master in case there is no ack
        replicaCommandSchedulerService.cleanCommands()
        replicaCommandSchedulerService.offer(remotecmdexecs)
        def schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        def executingCommands = []
        while (schledCmd) {
            executingCommands << schledCmd
            schledCmd = replicaCommandSchedulerService.scheduleNextCommand()
        }
        println "Executing commands: $executingCommands"
        // first step executing 4 commands for the 4 categories
        assertEquals "The queued commands must be empty", 6,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 4,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        // repeated commands can't be offered until they are terminated.
        replicaCommandSchedulerService.offer(remotecmdexecs)
        assertEquals "The queued commands must be empty", 6,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 4,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        // terminating the execution of 2 command
        replicaCommandSchedulerService.removeTerminatedCommand(
            executingCommands[0].id)
        replicaCommandSchedulerService.removeTerminatedCommand(
            executingCommands[2].id)
        println "Terminating commands: $executingCommands[0] and $executingCommands[2]"
        assertEquals "The queued commands must be empty", 6,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 2,
            replicaCommandSchedulerService.getExecutingCommandsSize()

        // Only terminated commands are offered again. 2 out of 10 are re-added.
        replicaCommandSchedulerService.offer(remotecmdexecs)
        assertEquals "The queued commands must be empty", 8,
            replicaCommandSchedulerService.getQueuedCommandsSize()
        assertEquals "The executing commands must be empty", 2,
            replicaCommandSchedulerService.getExecutingCommandsSize()
    }
}
