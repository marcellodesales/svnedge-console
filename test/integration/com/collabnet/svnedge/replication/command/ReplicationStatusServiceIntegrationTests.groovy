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

import com.collabnet.svnedge.domain.Server
import grails.test.GrailsUnitTestCase
import groovy.mock.interceptor.MockFor;

import java.util.concurrent.CountDownLatch 
import java.util.concurrent.TimeUnit;

import com.collabnet.svnedge.domain.integration.*
import com.collabnet.svnedge.domain.ServerMode
import com.collabnet.svnedge.integration.command.AbstractCommand
import com.collabnet.svnedge.integration.command.CommandState
import com.collabnet.svnedge.integration.command.CommandsExecutionContext;
import com.collabnet.svnedge.integration.command.event.CommandReadyForExecutionEvent
import com.collabnet.svnedge.integration.command.event.LongRunningCommandQueuedEvent
import com.collabnet.svnedge.integration.command.event.ReplicaCommandsExecutionEvent;
import com.collabnet.svnedge.integration.command.impl.RepoAddCommand;

import org.cometd.Client;
import org.cometd.Message;
import org.cometd.MessageListener;
import org.mortbay.cometd.ChannelImpl;
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener 
import static com.collabnet.svnedge.integration.CtfRemoteClientService.COMMAND_ID_PREFIX
import groovy.time.TimeCategory
import com.collabnet.svnedge.integration.command.event.CommandTerminatedEvent

/**
 * This test case verifies command threading, blocking, etc in the
 * ReplicaCommand processing components. It uses mock command implementation
 * classes for long and short run times, and scans the command execution log
 * to validate expectations
 */
class ReplicationStatusServiceIntegrationTests extends GrailsUnitTestCase {
    
    def grailsApplication
    def applicationEventMulticaster
    def replicaServerStatusService
    def replicaCommandSchedulerService

    def REPO_NAME = "testproject2"
    def EXSY_ID = "exsy9876"
    def rConf

    def remotecmdexecs = Collections.synchronizedList(new LinkedList<Map<String, String>>())

    def ReplicationStatusServiceIntegrationTests() {
        remotecmdexecs << [id:'cmdexec1001', repoName:'repo1', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1009', repoName:null, code:'replicaPropsUpdate',
                    params:[until:'2011-01-22']]
        remotecmdexecs << [id:'cmdexec1002', repoName:'repo1', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1006', repoName:'repo2', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1004', repoName:null, code:'replicaPropsUpdate'
                    , params:[name:'Replica Brisbane']]
        remotecmdexecs << [id:'cmdexec1000', repoName:null, code:'replicaApprove',
                    params:[name:'replica title', desc:'super replica']]
        remotecmdexecs << [id:'cmdexec1007', repoName:'repo3', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1008', repoName:null, code:'replicaPropsUpdate'
                    , params:[maxReplicacmdexecs:3, maxRepositorycmdexecs: 10]]
        remotecmdexecs << [id:'cmdexec1005', repoName:'repo2', code:'repoSync']
        remotecmdexecs << [id:'cmdexec1003', repoName:'repo3', code:'repoSync']
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

    void testSpringEventsListener() {
        def latch = new CountDownLatch(1)
        def listener = { ev ->
            latch.countDown()
        } as ApplicationListener<DummyEvent>
        applicationEventMulticaster.addApplicationListener listener
        assert listener in applicationEventMulticaster.applicationListeners
        grailsApplication.mainContext.publishEvent(new DummyEvent())
        assert latch.await(1000, TimeUnit.MILLISECONDS) : 'timeout waiting for event'
    }

    class DummyEvent extends ApplicationEvent {
        DummyEvent() {
            super([])
        }
    }

    class RepoTestCommand extends RepoAddCommand {

        def constraints() {
            println "Verifying constraints..."
        }

        def execute() {
            println "Executing Test Command for 3 seconds..."
            Thread.sleep 3000
        }

        def undo() {
            log.debug("Undoing...")
        }
    }

    void testStatusChangeForCommands() {
        assert replicaServerStatusService in applicationEventMulticaster.applicationListeners
        assert replicaCommandSchedulerService in applicationEventMulticaster.applicationListeners

        def longRunningCommand = new RepoTestCommand()
        longRunningCommand.id = "cmdexec10001" 
        longRunningCommand.repoName = "/tmp/repo1"
        longRunningCommand.state = CommandState.SCHEDULED
        longRunningCommand.context = new CommandsExecutionContext()
        longRunningCommand.context.logsDir = System.getProperty("java.io.tmpdir")
        longRunningCommand.context.appContext = grailsApplication.mainContext

        // fire the event
        grailsApplication.mainContext.publishEvent(new LongRunningCommandQueuedEvent(this, 
                longRunningCommand))

        // wait up to 1 minute for new command to show up on queue 
        use(TimeCategory) {
            timeout = new Date() + 1.minute
        }
        while (!replicaServerStatusService.areThereAnyCommands() && new Date().getTime() < timeout.getTime()) {
            Thread.sleep(250)
        }

        assertTrue "There should be commands after scheduling",
            replicaServerStatusService.areThereAnyCommands()
        def scheduledCmds = replicaServerStatusService.getCommands(CommandState.SCHEDULED)

        // verifying all comments of different states as empty
        for (cmdState in CommandState.values()) {
            if (cmdState == CommandState.SCHEDULED) {
                assertTrue ("The test command should be in the state ${cmdState}", scheduledCmds?.contains(longRunningCommand))
            } else {
                def cmds = replicaServerStatusService.getCommands(cmdState)
                assertFalse ("The test command should not be in the state ${cmdState}", cmds?.contains(longRunningCommand))
            }
        }

        // fire the running event
        grailsApplication.mainContext.publishEvent(
            new CommandReadyForExecutionEvent(this, longRunningCommand))

        use(TimeCategory) {
            timeout = new Date() + 1.minute
        }
        while (!replicaServerStatusService.areThereAnyCommands(CommandState.RUNNING) && new Date().getTime() < timeout.getTime()) {
            Thread.sleep(250)
        }

        def runningCmds = replicaServerStatusService.getCommands(
            CommandState.RUNNING)

        // verifying all comments of different states as empty
        for (cmdState in CommandState.values()) {
            if (cmdState == CommandState.RUNNING) {
                assertTrue ("The test command should be in the state ${cmdState}", runningCmds?.contains(longRunningCommand))
            } else {
                def cmds = replicaServerStatusService.getCommands(cmdState)
                assertFalse ("The test command should not be in the state ${cmdState}", cmds?.contains(longRunningCommand))
            }
        }

        // fire the terminated event
        grailsApplication.mainContext.publishEvent(
            new CommandTerminatedEvent(this, longRunningCommand))

        use(TimeCategory) {
            timeout = new Date() + 1.minute
        }
        while (!replicaServerStatusService.areThereAnyCommands(CommandState.TERMINATED) && new Date().getTime() < timeout.getTime()) {
            Thread.sleep(250)
        }

        def terminatedCmds = replicaServerStatusService.getCommands(
            CommandState.TERMINATED)

        // verifying all comments of different states as empty
        for (cmdState in CommandState.values()) {
            if (cmdState == CommandState.TERMINATED) {
                assertTrue ("The test command should be in the state ${cmdState}", terminatedCmds?.contains(longRunningCommand))

            } else {
                def cmds = replicaServerStatusService.getCommands(cmdState)
                assertFalse ("The test command should not be in the state ${cmdState}", cmds?.contains(longRunningCommand))
            }
        }
    }
}
