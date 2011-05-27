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
package com.collabnet.svnedge.integration.command.handler

import com.collabnet.svnedge.console.ReplicaServerStatusService.CommandAtState;
import com.collabnet.svnedge.integration.command.AbstractCommand 
import com.collabnet.svnedge.integration.command.CommandState;
import com.collabnet.svnedge.integration.command.LongRunningCommand;

import grails.converters.JSON;

import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger


/**
 * The Commands State Delayed Notifier is responsible for publishing
 * the state of the different types of commands at every given delay.
 * Namely, {@link LongRunningCommand} or {@link ShortRunningCommand} states
 * changes will be published with certain delay.
 * 
 * This handler was created because the client can't update its UI (browser)
 * due to the overload of messages that can happen at a small period of
 * time between the events for commands running. In this way, a DELAY for
 * delivery the messages is introduced here.
 *
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
class CommandStateDelayedNotifierHandler implements Runnable {

    static Logger log = Logger.getLogger(
        CommandStateDelayedNotifierHandler.class)

    def statusService
    /**
     * The scheduled commands queue for the given type T
     */
    BlockingQueue<CommandAtState> commandsStateChange
    /**  
     * All commands currently processing (not delivered).
     */
    ConcurrentHashMap<AbstractCommand, CommandState> allCommands
    /**
     * The delay in milliseconds for publishing the status changes.
     */
    private static final long DELIVERY_DELAY = 4000

    def CommandStateDelayedNotifierHandler(service, commandsQueue, commands) {
        statusService = service
        commandsStateChange = commandsQueue
        allCommands = commands
    }

    @Override
    public void run() {
        while(true) {
            // blocks until one or more commands are queued.
            def commandAndState = commandsStateChange.take()
            log.debug("Command changed state: $commandAndState.command to " +
                "$commandAndState.state")

            def cmdStateJson = makeCommandStateChangeMessage(commandAndState)
            statusService.publishBayeuxMessage(cmdStateJson)
            Thread.sleep(DELIVERY_DELAY)
        }
    }

    /**
     * An asynchronous publisher that publishes the given message in the 
     * Bayeux server with the following Json doc:
     * 
     *  {
     *     id: the id of the command.
     *     code: the code of the command to differentiate the type of command.
     *     state: the new state of the command.
     *     type: "long" or "short"
     *     startedAt: the timestamp when it started running
     *     succeeded: the result of the command, only shows if the command
     *      terminated.
     *     totalCommands: the total number of commands running.
     *  }
     */
    def makeCommandStateChangeMessage(CommandAtState cmdAtState) {
        def writer = new StringWriter();
        def cmd = cmdAtState.command
        def cmdCode = AbstractCommand.makeCodeName(cmd)
        def cmdType = cmd instanceof LongRunningCommand ? "long" : "short"
        def cmdState = cmdAtState.state

        def resp = [id: cmd.id, code: cmdCode, state: cmdState.toString(), 
            type: cmdType, totalCommands: allCommands.size()]

        if (cmdState == CommandState.RUNNING) {
            def runTime = new Date()
            runTime.setTime(cmd.getStateTransitionTime(CommandState.RUNNING))
            def dtFormat = statusService.getMessage(
                "default.dateTime.format.withZone")
            resp << [startedAt: runTime.format(dtFormat)]
        }
        if (cmdState == CommandState.TERMINATED || 
                cmdState == CommandState.REPORTED) {
            resp << [succeeded: cmd.succeeded]
        }
        if (cmdState != CommandState.REPORTED) {
            // terminated, running, scheduled commands show the parameters
            resp << [params: cmd.params]
        }
        return (resp as JSON).toString()
    }
}
