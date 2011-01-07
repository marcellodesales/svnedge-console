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
package com.collabnet.svnedge.replication

import java.util.List;
import java.util.Map;

import com.collabnet.svnedge.console.services.AbstractSvnEdgeService;
import com.collabnet.svnedge.master.RemoteMasterException;
import com.collabnet.svnedge.replication.command.CommandExecutionException
import com.collabnet.svnedge.replication.command.CommandNotImplementedException
import com.collabnet.svnedge.teamforge.CtfServer;

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

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
        implements ApplicationContextAware {

    boolean transactional = true

    ApplicationContext applicationContext

    def ctfRemoteClientService
    def securityService

    /** The default Master instance */
    def defaultMaster
    
    /** The current Replica instance */
    def currentReplica

    def bootStrap = { initialMaster, initialReplica ->
        this.defaultMaster = initialMaster
        this.currentReplica = initialReplica
    }

    def retrieveAndExecuteReplicaCommands(ctfUrl, userSessionId, masterSystemId,
            locale) {

        ReplicaConfiguration replica = ReplicaConfiguration.getCurrentConfig()
        if (!replica) {
            def msg = getMessage("filter.probihited.mode.replica", locale)
            throw new IllegalStateException(msg)
        }

        //receive the commands from ctf
        def queuedCommands = ctfRemoteClientService.getReplicaQueuedCommands(
            ctfUrl, userSessionId, masterSystemId, locale)

        //execute each of them
        executeCommands(queuedCommands, ctfUrl, userSessionId, masterSystemId,
            locale)
    }

    /**
     * @return boolean if the local cache contains the given username and 
     * password as a key. 
     * @throws RemoteAccessException if the communication fails with the remote
     * Master host for any reason.
     */
    def retrieveAndExecuteReplicaCommands(){

        def locale = Locale.getDefault()
        ReplicaConfiguration replica = ReplicaConfiguration.getCurrentConfig()
        if (!replica) {
            def msg = getMessage("filter.probihited.mode.replica", locale)
            throw new IllegalStateException(msg)
        }
        def ctfServer = CtfServer.getServer()
        def ctfPassword = securityService.decrypt(ctfServer.ctfPassword)
        def userSessionId = ctfRemoteClientService.login(ctfServer.baseUrl,
            ctfServer.ctfUsername, ctfPassword, locale)

        //receive the commands from ctf
        def queuedCommands = ctfRemoteClientService.getReplicaQueuedCommands(
            ctfServer.baseUrl, userSessionId, replica.systemId, locale)

        //execute each of them
        executeCommands(queuedCommands, ctfServer.baseUrl, userSessionId, 
            replica.systemId, locale)
    }

    def executeCommands(queuedCommands, ctfUrl, userSessionId, masterSystemId,
            locale) {

        //execute each command, having them being updated with status
        for(command in queuedCommands) {
            def commandWithResult = processCommandRequest(command)

            try {
                //upload the commands results back to ctf
                ctfRemoteClientService.uploadCommandResult(ctfUrl,
                    userSessionId, masterSystemId, commandWithResult.id,
                    commandWithResult.succeeded, locale)
                log.debug("Command successfully acknowledged: " + command)

            } catch (Exception remoteError) {
                log.error("Error while acknowledging the command: " + command, 
                    remoteError)
                //TODO: Add the error to the error list to guarantee delivery.
            }
        }
    }

    /**
     * @param command is a map instance with the following properties:
     * <li>commands['id'] = the identification of the command
     * <li>commands['code'] = the code of the command, which is necessary to 
     * load a command class called "CodeCommand".
     * <li>commands['params'] = a list of parameters, each of them with a 
     * a name property (commands['params'].name) and the list of values as
     * commands['params'].values.
     * 
     * @return the updated value of the command with the following properties:
     * 
     * <li>command['succeeded'] = the status of the command, which is a boolean
     * value that determines if the command executed successfully or not.
     * <li>command['exception'] = the exception that happened during the
     * execution of the command, if any.
     * 
     */
    def processCommandRequest(command) {
        def commandPackage = "com.collabnet.svnedge.replication.command"

        def className = command['code'].capitalize() + "Command"

        def classObject = null
        try {
            classObject = getClass().getClassLoader().loadClass(
                    "$commandPackage.$className")
        } catch (ClassNotFoundException clne) {
            command['exception'] = 
                new CommandNotImplementedException(clne, className)
            command['succeeded'] = false
            return command
        }
        log.debug("Instantiating the class for the command " + command)
        def commandInstance = classObject.newInstance();
        log.debug("Class " + commandInstance)

        commandInstance.init(command['params'], applicationContext)
        log.debug("Initialized the parameters " + command['params'])
        try {
            log.debug("Ready to run the command " + command)
            commandInstance.run()
            log.debug("Command successfully run: " + command)

        } catch (CommandExecutionException ceex) {
            command['exception'] = ceex
            log.error("The command failed: " + command)
        }
        command['succeeded'] = commandInstance.succeeded
        return command
    }
}
