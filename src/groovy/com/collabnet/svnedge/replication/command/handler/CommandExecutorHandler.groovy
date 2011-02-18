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
package com.collabnet.svnedge.replication.command.handler

import java.util.concurrent.BlockingQueue

import org.apache.log4j.Logger

import com.collabnet.svnedge.replication.command.AbstractCommand
import com.collabnet.svnedge.replication.command.event.CommandReadyForExecutionEvent

/**
 * The Commands Executor Handler is responsible for executing commands
 * from the instance of the scheduled commands that can be an instance of
 * {@link LongRunningCommand} or {@link ShortRunningCommand}.
 *
 * An eligible command to run will be selected by verifying if there are
 * no other commands running for the command group (replica server or
 * repository name). An event CommandReadyForExecutionEvent will be published
 * with the command to be executed.
 *
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
class CommandExecutorHandler<T extends AbstractCommand> implements Runnable {

    static Logger log = Logger.getLogger(CommandExecutorHandler.class)

    def executorService
    /**
     * The class type of the handler elements
     */
    Class<T> handlerType
    /**
     * The scheduled commands queue for the given type T
     */
    BlockingQueue<T> scheduledCommandsQueue

    def CommandExecutorHandler(Class<T> cT, service, commandsQueue) {
        handlerType = cT
        executorService = service
        scheduledCommandsQueue = commandsQueue
    }

    @Override
    public void run() {
        while(true) {
            def className = handlerType.getSimpleName()
            log.debug("Waiting for eligible $className...")

            // blocks until one or more commands are queued.
            def eligibleCommand = scheduledCommandsQueue.take()
            log.debug("Command scheduled to run: $eligibleCommand.")

            executorService.publishEvent(new CommandReadyForExecutionEvent(
                this, eligibleCommand))
        }
    }
}
