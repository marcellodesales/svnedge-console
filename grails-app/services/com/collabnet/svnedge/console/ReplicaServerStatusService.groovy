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
package com.collabnet.svnedge.console

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import grails.converters.JSON;

import org.cometd.Client;
import org.mortbay.cometd.ChannelImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;


import com.collabnet.svnedge.domain.integration.CommandResult;
import com.collabnet.svnedge.integration.command.AbstractCommand;
import com.collabnet.svnedge.integration.command.CommandState;
import com.collabnet.svnedge.integration.command.LongRunningCommand;
import com.collabnet.svnedge.integration.command.ShortRunningCommand;
import com.collabnet.svnedge.integration.command.event.CommandReadyForExecutionEvent;
import com.collabnet.svnedge.integration.command.event.CommandTerminatedEvent;
import com.collabnet.svnedge.integration.command.event.CommandResultReportedEvent;
import com.collabnet.svnedge.integration.command.event.LongRunningCommandQueuedEvent;
import com.collabnet.svnedge.integration.command.event.ReplicaCommandsExecutionEvent;
import com.collabnet.svnedge.integration.command.event.ShortRunningCommandQueuedEvent;

/**
 * This service is responsible for maintaining the current status of the 
 * commands being executed. It will use Cometd to push the information on 
 * each command status change.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public final class ReplicaServerStatusService extends AbstractSvnEdgeService 
        implements InitializingBean, ApplicationListener<ReplicaCommandsExecutionEvent> {

    boolean transactional = false

    def commandLineService
    /**
     * Auto-wired Cometd bayeux server
     */
    def bayeux
    /**
     * The bayeux publisher client
     */
    private Client bayeuxPublisherClient
    /**
     * The Bayeux publisher Status message channel
     */
    private ChannelImpl commandStatusChannel
    /**
     * The Bayeux publisher Counter message channel
     */
    private ChannelImpl commandsCounterChannel
    /**
     * The current set of commands by the state.
     */
    private ConcurrentMap<CommandState, Set<AbstractCommand>> commandsByState
    /**
     * All the commands running with their associated state.
     */
    private ConcurrentMap<AbstractCommand, CommandState> allCommands
    /**
     * The index of long-running commands
     */
    private Set<LongRunningCommand> allLongRunning
    /**
     * The index of short-running commands
     */
    private Set<ShortRunningCommand> allShortRunning

    public ReplicaServerStatusService() {
        commandsByState = new ConcurrentHashMap<CommandState, 
            Set<AbstractCommand>>()
        allCommands = new ConcurrentHashMap<AbstractCommand, 
            CommandState>()
        allLongRunning = Collections.synchronizedSet(
            new HashSet<LongRunningCommand>())
        allShortRunning = Collections.synchronizedSet(
            new HashSet<ShortRunningCommand>())
    }

    def bootStrap = { 
    }

    // just like @PostConstruct
    void afterPropertiesSet() {
        this.bayeuxPublisherClient = this.bayeux.newClient(this.class.name)
        def statusChannel = "/replica/status/all"
        def create = true
        this.commandStatusChannel = this.bayeux.getChannel(statusChannel,
            create)
        def counterChannel = "/replica/status/counter"
        this.commandsCounterChannel = this.bayeux.getChannel(counterChannel,
            create)
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
     *  }
     *  
     */
    private void publishCommandTransition(AbstractCommand command, 
           CommandState newState) {

        def writer = new StringWriter();
        def cmdState = newState.toString()
        def cmdCode = AbstractCommand.makeCodeName(command)

        def resp = [id: command.id, code: cmdCode, state: cmdState]

        def cmdType = command instanceof LongRunningCommand ? "long" : "short"
        if (newState == CommandState.RUNNING) {
            def runTime = new Date()
            if (command.stateTransitions.get(CommandState.RUNNING)) {
                runTime.setTime(command.stateTransitions.get(CommandState.RUNNING))
            }
            def dtFormat = getMessage("default.dateTime.format.withZone")
            resp << [startedAt: runTime.format(dtFormat)]
            resp << [type: cmdType]
        }
        if (newState == CommandState.TERMINATED) {
            resp << [succeeded: command.succeeded]
            resp << [type: cmdType]
        }
        if (newState == CommandState.REPORTED) {
            resp << [type: cmdType]

        } else if (newState != CommandState.REPORTED) {
            // terminated, running, scheduled commands show the parameters
            resp << [params: command.params]
        }

        def jsonRes = (resp as JSON).toString()
        log.debug("Command transition to publish: " + jsonRes)
        this.commandStatusChannel.publish(this.bayeuxPublisherClient, 
            jsonRes, null)
        def sizeCmdds = allCommands.size()
        def tresp = [total : sizeCmdds, longRunning: allLongRunning.size(),
            shortRunning: allShortRunning.size()]
        def tjsonRes = (tresp as JSON).toString()
        this.commandsCounterChannel.publish(this.bayeuxPublisherClient, 
            tjsonRes, null)
    }

    /**
     * @return whether there are commands in either state (
     * CommandState.SCHEDULED, RUNNING, TERMINATED, REPORTED...)
     */
    public boolean areThereAnyCommands() {
        return this.allCommands.size() > 0
    }

    /**
     * @param state is the state of a command.
     * @return whether there are commands in the given state.
     */
    public boolean areThereAnyCommands(CommandState state) {
        def cmdsInState = this.commandsByState.get(state)
        return cmdsInState && cmdsInState.size() > 0
    }

    /**
     * @param commandId is the command Id.
     * @return a given command with the given ID.
     */
    public AbstractCommand getCommand(String commandId) {
        for(command in allCommands.keySet()) {
            if (command.id.equals(commandId)) {
                return command
            }
        }
        return null
    }

    /**
     * @param state the current state.
     * @return the set of commands on the given state.
     */
    public Set<AbstractCommand> getCommands(CommandState state) {
        Set<AbstractCommand> commandsInState = this.commandsByState.get(state)
        Set<AbstractCommand> result = new LinkedHashSet<AbstractCommand>()
        if (!commandsInState || commandsInState.size() == 0) {
            return result
        }
        // as a concurrent hash set.
        synchronized (commandsByState) {
            Iterator<AbstractCommand> iter = commandsInState.iterator()
            while(iter.hasNext()) {
                result.add(iter.next())
            }
        }
        return result
    }

    /**
     * The getCommands for the state
     * @param state is the set of commands.
     * @return the commands for the given set of state.
     */
    public Set<AbstractCommand> getCommands(CommandState ... state) {
        if (state != null && state.length > 0) {
            Set<AbstractCommand> cmds = new LinkedHashSet<AbstractCommand>()
            for (CommandState commandState : state) {
                cmds.addAll(getCommands(commandState))
            }
            return cmds

        } else {
            return new LinkedHashSet<AbstractCommand>()
        }
    }

    /**
     * @return The total size of commands.
     */
    public int getAllCommandsSize() {
        return this.allCommands.size()
    }

    /**
     * @param commandType Is one of the marker interfaces 
     * {@link LongRunningCommand} or {@link ShortRunningCommand}.
     * @return All the current commands for the given type.
     */
    public Set<AbstractCommand> getCommandsByType(commandType) {
        Set<AbstractCommand> all = new LinkedHashSet<AbstractCommand>()
        if (commandType == LongRunningCommand.class) {
            synchronized (allLongRunning) {
                for (cmd in allLongRunning) {
                    all << cmd
                }
            }
        } else if (commandType == ShortRunningCommand.class) {
            synchronized (allShortRunning) {
                for (cmd in allShortRunning) {
                    all << cmd
                }
            }
        }
        return all
    }

    /**
     * @return All the current commands running in all states.
     */
    public Set<AbstractCommand> getAllCommands() {
        Set<AbstractCommand> all = new LinkedHashSet<AbstractCommand>()
        for (state in CommandState.values()) {
            all.addAll(getCommands(state))
        }
        return all
    }

     /**
      * Updates the command Maps for the commands.
      * @param command is the command to be executed.
      * @param state is the state of the command.
      */
    private void updateOrRemoveCommandState(AbstractCommand command, 
            CommandState state) {

        if (state == null) {
            def commandState = allCommands.remove(command)

            if (command.class == LongRunningCommand.class) {
                allLongRunning.remove(command)

            } else if (command.class == ShortRunningCommand.class) {
                allShortRunning.remove(command)
            }

            Set<AbstractCommand> commands = commandsByState.get(commandState)
            if (commands && commands.size() > 0) {
                synchronized (commands) {
                    Iterator<AbstractCommand> iter = commands.iterator()
                    while (iter.hasNext()) {
                        def cmd = iter.next()
                        if (cmd.id == command.id) {
                            iter.remove()
                            break
                        }
                    }
                }
            }

        } else {
            // override the state getting the previous value
            def previousState = allCommands.put(command, state)

            if (command.class == LongRunningCommand.class) {
                allLongRunning << command

            } else if (command.class == ShortRunningCommand.class) {
                allShortRunning << command
            }


            // command had not been registered before
            if (!previousState) {
                Set<AbstractCommand> commands = commandsByState.get(state)
                if (!commands) {
                    Set<AbstractCommand> newSet = Collections.synchronizedSet(
                        new LinkedHashSet<AbstractCommand>())
                    commands = commandsByState.putIfAbsent(state, newSet)
                    if (!commands) {
                        commands = newSet
                    }
                }
                commands.add(command)

            } else {
                // remove from the previous state
                Set<AbstractCommand> commands = commandsByState.get(
                    previousState)
                if (commands && commands.size() > 0) {
                    synchronized (commands) {
                        Iterator<AbstractCommand> iter = commands.iterator()
                        while (iter.hasNext()) {
                            def cmd = iter.next()
                            if (cmd == command) {
                                iter.remove()
                                break
                            }
                        }
                    }
                }
                Set<AbstractCommand> newStateCmds = commandsByState.get(state)
                if (!newStateCmds) {
                    Set<AbstractCommand> newSet = Collections.synchronizedSet(
                        new LinkedHashSet<AbstractCommand>())
                    newStateCmds = commandsByState.putIfAbsent(state, newSet)
                    if (!newStateCmds) {
                        newStateCmds = newSet
                    }
                }
                newStateCmds.add(command)
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
            case ShortRunningCommandQueuedEvent:
                def scheduledCommand = executionEvent.queuedCommand
                log.debug "Command scheduled: $scheduledCommand"
                def state = CommandState.SCHEDULED
                updateOrRemoveCommandState(scheduledCommand, state)
                publishCommandTransition(scheduledCommand, state)
                break;

            case CommandReadyForExecutionEvent:
                def commandToExecute = executionEvent.commandToExecute
                log.debug "Command executing: $commandToExecute"
                def state = CommandState.RUNNING
                updateOrRemoveCommandState(commandToExecute, state)
                publishCommandTransition(commandToExecute, state)
                break

            case CommandTerminatedEvent:
                def terminatedCommand = executionEvent.terminatedCommand
                log.debug "Command terminated: ${terminatedCommand}"
                def state = CommandState.TERMINATED
                updateOrRemoveCommandState(terminatedCommand, state)
                publishCommandTransition(terminatedCommand, state)
                break

            case CommandResultReportedEvent:
                def cmdResult = executionEvent.commandResult
                def state = CommandState.REPORTED
                def reportedCommand = AbstractCommand.makeCommand(cmdResult,
                    state)
                log.debug "Command reported: ${reportedCommand}"
                updateOrRemoveCommandState(reportedCommand, null)
                publishCommandTransition(reportedCommand, state)
                break
        }
    }

}
