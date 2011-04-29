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
package com.collabnet.svnedge.integration.command

import grails.util.GrailsUtil

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Semaphore

import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.ServerMode 
import com.collabnet.svnedge.integration.FetchReplicaCommandsJob 
import com.collabnet.svnedge.integration.command.event.AppliedExecutorSemaphoresUpdateEvent 
import com.collabnet.svnedge.integration.command.event.CommandReadyForExecutionEvent 
import com.collabnet.svnedge.integration.command.event.CommandTerminatedEvent 
import com.collabnet.svnedge.integration.command.event.LongRunningCommandQueuedEvent 
import com.collabnet.svnedge.integration.command.event.NoCommandsRunningUpdateSemaphoresEvent 
import com.collabnet.svnedge.integration.command.event.ReplicaCommandsExecutionEvent 
import com.collabnet.svnedge.integration.command.event.ShortRunningCommandQueuedEvent 
import com.collabnet.svnedge.integration.command.handler.CommandExecutorHandler 

import org.springframework.context.ApplicationListener

/**
 * The ReplicaCommandExecutiorService is responsible for retrieving the
 * queued commands from the master server, interpret and transform them
 * into Groovy Classes based on the name of the command, and execute each
 * of them. The result of the command is returned for each of the executed
 * command, being uploaded back to the server.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */
public class ReplicaCommandExecutorService extends AbstractSvnEdgeService 
        implements ApplicationListener<ReplicaCommandsExecutionEvent> {

    static transactional = false

    def ctfRemoteClientService
    def backgroundService
    def bgThreadManager
    def replicaCommandSchedulerService
    def longRunningHandler
    def shortRunningHandler

    /**
     * The default name of the replica server category.
     */
    private static final String REPLICA_COMMAND_CATEGORY = "replicaServer"
    /**
     * The thread pool semaphore that controls the number of permitted
     * long-running commands can be executed at the same time.
     */
    Semaphore longRunningSemaphore
    /**
     * The thread pool semaphore that controls the number of permitted
     * short-running commands can be executed at the same time.
     */
    Semaphore shortRunningSemaphore
    /**
     * The blocking queue of long-running scheduled commands
     */
    BlockingQueue<LongRunningCommand> longRunningScheduledCommands
    /**
     * The blocking queue of short-running scheduled commands
     */
    BlockingQueue<ShortRunningCommand> shortRunningScheduledCommands

    def bootStrap = {
        if (Server.getServer().mode == ServerMode.REPLICA) {
            new FetchReplicaCommandsJob().start()
        }
        // initialize the queues
        longRunningScheduledCommands = new LinkedBlockingQueue<LongRunningCommand>()
        shortRunningScheduledCommands = new LinkedBlockingQueue<ShortRunningCommand>()
        // initialize the semaphores
        updateLongRunningSemaphore(2, 0)
        updateShortRunningSemaphore(10, 0)

        // initialize the long-running command executor handler.
        longRunningHandler =
            new CommandExecutorHandler<LongRunningCommand>(
                LongRunningCommand.class, this, longRunningScheduledCommands)
        // initialize the short-running command executor handler.
        shortRunningHandler =
            new CommandExecutorHandler<ShortRunningCommand>(
                ShortRunningCommand.class, this, shortRunningScheduledCommands)

        if (GrailsUtil.environment != "test") {
            startBackgroundHandlers()
        }
    }

    /**
     * Starts the background handlers for the long-running and short-running commands.
     */
    def startBackgroundHandlers() {
         bgThreadManager.queueRunnable(longRunningHandler)
         bgThreadManager.queueRunnable(shortRunningHandler)
    }

    /**
     * Setups up a new value for the total number of permits available in the
     * Semaphore that controls the concurrent number commands executing.
     * @param semaphore is a given semaphore
     * @param newPermits the new number of permits
     * @param oldPermits the old number of permits
     */
    def updateLongRunningSemaphore(newPermits, oldPermits) {
        if (!longRunningSemaphore) {
            longRunningSemaphore = new Semaphore(newPermits)

        } else {
            if (newPermits < 1) {
                // permits must be positive integers
                return
            }
            if (newPermits == oldPermits) {
                // no changes to the number of permits
                return
            }
            synchronized(longRunningSemaphore) {
                longRunningSemaphore = new Semaphore(newPermits)
            }
        }
    }

    /**
     * Setups up a new value for the total number of permits available in the
     * Semaphore that controls the concurrent number commands executing.
     * @param semaphore is a given semaphore
     * @param newPermits the new number of permits
     * @param oldPermits the old number of permits
     */
    def updateShortRunningSemaphore(newPermits, oldPermits) {
        if (!shortRunningSemaphore) {
            shortRunningSemaphore = new Semaphore(newPermits)

        } else {
            if (newPermits < 1) {
                // permits must be positive integers
                return
            }
            if (newPermits == oldPermits) {
                // no changes to the number of permits
                return
            }
            synchronized(shortRunningSemaphore) {
                shortRunningSemaphore = new Semaphore(newPermits)
            }
        }
    }

    /**
     * The event handler of all {@link ReplicaCommandsExecutionEvent} to 
     * process the different events.
     * @param executionEvent is the instance of an execution event.
     */
    void onApplicationEvent(ReplicaCommandsExecutionEvent executionEvent) {
        switch(executionEvent) {
            case LongRunningCommandQueuedEvent:
                def queuedCommand = executionEvent.queuedCommand
                log.debug "LongRunningCommandQueuedEvent: $queuedCommand"
                longRunningScheduledCommands.offer(queuedCommand)
                break

            case ShortRunningCommandQueuedEvent:
                def queuedCommand = executionEvent.queuedCommand
                log.debug "ShortRunningCommandQueuedEvent: $queuedCommand"
                shortRunningScheduledCommands.offer(queuedCommand)
                break

            case CommandReadyForExecutionEvent:
                def commandToExecute = executionEvent.commandToExecute
                log.debug "CommandReadyForExecutionEvent: $commandToExecute"
                // Execute the command in parallel
                backgroundService.execute("Executing ${commandToExecute}", {
                    executeCommand(commandToExecute)
                })
                break

            case CommandTerminatedEvent:
                def terminatedCommand = executionEvent.terminatedCommand
                log.debug "CommandTerminatedEvent: ${terminatedCommand}"
                switch(terminatedCommand) {
                    case LongRunningCommand:
                        longRunningSemaphore.release()
                        break

                    case ShortRunningCommand:
                        shortRunningSemaphore.release()
                        break
                }
                break

            case NoCommandsRunningUpdateSemaphoresEvent:
                log.debug("NoCommandsRunningEvent: updating the " +
                    "semaphores and counting down the latch")
                log.debug("MaxNumberCommandsRunningUpdatedEvent: updating " +
                    "the executor semaphores")
                def maxChangesEvent = executionEvent.maxNumberCommandsRunningUpdatedEvent
                if (maxChangesEvent.newMaxLongRunningCmds != -1) {
                    updateLongRunningSemaphore(
                        maxChangesEvent.newMaxLongRunningCmds,
                        maxChangesEvent.oldMaxLongRunningCmds)
                }
                if (maxChangesEvent.newMaxShortRunningCmds != -1) {
                    updateShortRunningSemaphore(
                        maxChangesEvent.newMaxShortRunningCmds,
                        maxChangesEvent.oldMaxShortRunningCmds)
                }
                publishEvent(new AppliedExecutorSemaphoresUpdateEvent(this))
                break
        }
    }

    /**
     * Executes the command instance after acquiring the related permit from
     * the related semaphore.
     * @param commandInstance is the command instance.
     */
    def executeCommand(commandInstance) {
        def selectedSemaphore = null
        switch(commandInstance) {
            case LongRunningCommand:
                selectedSemaphore = longRunningSemaphore
                break

            case ShortRunningCommand:
                selectedSemaphore = shortRunningSemaphore
                break
        }
        def numPermits = selectedSemaphore.availablePermits()
        log.debug "Number of semaphore's permits: $numPermits. Trying " +
            "to acquire..."
        selectedSemaphore.acquire()

        log.debug "Permit acquired... Executing the command lifecycle..."
        commandLifecycleExecutor(commandInstance)
    }

    /**
     * Closure used to execute a command. When the execution is finished, 
     * the event {@link CommandTerminatedEvent} is fired.
     * @param commandInstance an instance of {@link AbstractReplicaCommand}
     */
    def commandLifecycleExecutor(commandExec) {
        try {
            AbstractCommand.logExecution("RUN-BEGIN", commandExec)
            commandExec.run()
            log.debug("Command successfully run: " + commandExec)
            AbstractCommand.logExecution("RUN-END-SUCCESS", commandExec)

        } catch (CommandExecutionException ceex) {
            log.error("The command failed: " + ceex.getMessage())
            AbstractCommand.logExecution("RUN-END-FAILURE", commandExec, ceex)
        }
        publishEvent(new CommandTerminatedEvent(this, commandExec))
    }
}
