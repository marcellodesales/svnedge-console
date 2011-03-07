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
package com.collabnet.svnedge.replication.command

import com.collabnet.svnedge.domain.integration.CommandResult 
import grails.test.GrailsUnitTestCase;

import static com.collabnet.svnedge.integration.CtfRemoteClientService.COMMAND_ID_PREFIX

class CommandResultDeliveryServiceIntegrationTests extends GrailsUnitTestCase {

    def commandResultDeliveryService

    def remotecmdexecs = Collections.synchronizedList(
        new LinkedList<Map<String, String>>())

    def CommandResultDeliveryServiceIntegrationTests() {
        remotecmdexecs << [id:'cmdexec1001', repoName:'repo1', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1009', repoName:null, code:'replicaPause',
            params:[until:'2011-01-22']]
        remotecmdexecs << [id:'cmdexec1002', repoName:'repo1', code:'repoUpdateProp']
        remotecmdexecs << [id:'cmdexec1006', repoName:'repo2', code:'repoUpdateProp']
        remotecmdexecs << [id:'cmdexec1004', repoName:null, code:'replicaUpdateProp'
            , params:[name:'Replica Brisbane']]
        remotecmdexecs << [id:'cmdexec1000', repoName:null, code:'replicaApprove',
            params:[name:'replica title', desc:'super replica']]
        remotecmdexecs << [id:'cmdexec1007', repoName:'repo3', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1008', repoName:null, code:'replicaUpdateProp'
            , params:[maxReplicacmdexecs:3, maxRepositorycmdexecs: 10]]
        remotecmdexecs << [id:'cmdexec1005', repoName:'repo2', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1003', repoName:'repo3', code:'repoSync']
        sortRemoteCommands(remotecmdexecs)
    }

    def sortRemoteCommands(commands) {
        def idComparator = [
        compare: {a,b->
            (a.id.replace(COMMAND_ID_PREFIX,"") as Integer) -
                (b.id.replace(COMMAND_ID_PREFIX,"") as Integer)
        }
        ] as Comparator
        commands.sort(idComparator)
    }

    protected void setUp() {
        CommandResult.executeUpdate("delete CommandResult")
        assertEquals "No commands must exist", 0, CommandResult.count()
    }

    protected void tearDown() {
        CommandResult.executeUpdate("delete CommandResult")
    }

    /**
     * Test processing a good add command.
     */
    void testCommandResultProcess() {
        // commands are received to be processed, and first scheduled.
        // the command result objects are created
        remotecmdexecs.each{ command ->
            commandResultDeliveryService.makePersistedCommandResult(command.id)
        }
        assertEquals "The number of persisted results must be the same as " +
            "the number of partial commands", remotecmdexecs.size(),
            CommandResult.count()

        // verifying the command results are received to be processed
        def persistedResults = CommandResult.list()
        remotecmdexecs.each{ command ->
            def persistedResult = CommandResult.findWhere(commandId:command.id)
            assertNotNull "The command result must exist by Id", persistedResult
            assertEquals "The command ID must be the same", command.id,
                persistedResult.commandId
            assertNull "The command result must be null until processing",
                persistedResult.succeeded
            assertFalse "The transmitted value should be false",
                persistedResult.transmitted
            assertNotNull "The command result must have a Date created",
                persistedResult.dateCreated
            println "C: " + persistedResult.dateCreated
            assertNotNull "The command result must not be have a last Updated",
                persistedResult.lastUpdated
            println "U: " + persistedResult.lastUpdated
        }

        // results processed. Give results for each of them
        persistedResults.eachWithIndex{ cmdResult, i ->
            def result = cmdResult.commandId.hashCode()
            commandResultDeliveryService.saveCommandResult(cmdResult,
                (result + i) % 2 == 0)
        }

        Thread.sleep(1000)

        println "Results ready for transmission..."
        // verify the changed values, specially the transmitted and succeeded
        remotecmdexecs.eachWithIndex{ command, i ->
            def persistedResult = CommandResult.findByCommandId(command.id)
            def result = persistedResult.commandId.hashCode()
            assertNotNull "The command result must exist by Id", persistedResult
            assertEquals "The command ID must be the same", command.id,
                persistedResult.commandId
            assertEquals "The command result must not be null after processing",
                (result + i) % 2 == 0, persistedResult.succeeded
            assertFalse "The transmitted value should still be false",
                persistedResult.transmitted
            assertNotNull "The command result must have a Date created",
                persistedResult.dateCreated
            println "C: " + persistedResult.dateCreated
            assertNotNull "The command result must have a last Updated after " +
                "the update", persistedResult.lastUpdated
            println "U: " + persistedResult.lastUpdated
        }

        // connection with replica manager (TF) open, transmit them
        persistedResults.each{ cmdResult ->
            commandResultDeliveryService.saveTransmittedCommandResult(cmdResult)
        }

        // verify the changed values, specially the transmitted and succeeded
        println "Results transmitted... verifying results"
        remotecmdexecs.eachWithIndex{ command, i ->
            def persistedResult = CommandResult.findByCommandId(command.id)
            def result = persistedResult.commandId.hashCode()
            assertNotNull "The command result must exist by Id", persistedResult
            assertEquals "The command ID must be the same", command.id,
                persistedResult.commandId
            assertEquals "The command result must be maintained",
                (result + i) % 2 == 0, persistedResult.succeeded
            assertTrue "The transmitted value must be true",
                persistedResult.transmitted
            assertNotNull "The command result must have a Date created",
                persistedResult.dateCreated
            assertNotNull "The command result must have a last Updated after " +
                "the update", persistedResult.lastUpdated
        }
    }

    /**
     * Test processing a good add command.
     */
    void testNoCommandresultDuplicationNorChanges() {
        def command = remotecmdexecs[0]

        commandResultDeliveryService.makePersistedCommandResult(command.id)
        assertEquals "There must have a command result added", 1, 
            CommandResult.count()

        commandResultDeliveryService.makePersistedCommandResult(command.id)
        assertEquals "Two comands can't be created with the same ID", 1, 
            CommandResult.count()
    }

    void testAttemptToRegisterExistingCommandResultsRegisterClearExisting() {
        def partialCommands = remotecmdexecs[1..5]
        commandResultDeliveryService.registerClearExistingCommands(
            partialCommands)
        assertEquals "The number of persisted results must be the same as " +
            "the number of partial commands", 5, CommandResult.count()

        def previousSize = CommandResult.count()
        partialCommands = remotecmdexecs[3..7]
        // cleaning the partial commands
        commandResultDeliveryService.registerClearExistingCommands(
            partialCommands)

        assertEquals "The number of partial commands must be 2", 2,
            partialCommands.size()
        assertEquals "The number of persisted results must be only the new " +
            "commands, excluding the existing ones", previousSize + 2,
            CommandResult.count()
    }

    void testIncorrectValues() {
        try {
            commandResultDeliveryService.makePersistedCommandResult(null)
            fail("No result should exist with null command ID")
        } catch (Exception e) {
            
        }
        try {
            commandResultDeliveryService.makePersistedCommandResult("")
            fail("No result should exist with empty command ID")
        } catch (Exception e) {
            
        }

        try {
            commandResultDeliveryService.saveCommandResult(null)
            fail("Should not be able to save command result with null values")
        } catch (Exception e) {
            
        }
        try {
            commandResultDeliveryService.saveCommandResult("")
            fail("Should not be able to save command result with empty values")
        } catch (Exception e) {
            
        }

        try {
            commandResultDeliveryService.saveTransmittedCommandResult(null)
            fail("Should not be able to save command result with transmitted " +
                "value with null object")
        } catch (Exception e) {
            
        }
        try {
            commandResultDeliveryService.saveTransmittedCommandResult("")
            fail("Should not be able to save command result with transmitted " +
                "value with empty object")
        } catch (Exception e) {
            
        }
    }
}
