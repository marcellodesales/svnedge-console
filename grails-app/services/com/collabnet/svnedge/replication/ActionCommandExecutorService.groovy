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

import com.collabnet.svnedge.replica.commands.CommandExecutionException
import com.collabnet.svnedge.replica.commands.CommandNotImplementedException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.remoting.RemoteAccessException

/**
 * The cache management manages the cache structures for the user authentication
 * and the file system artifacts.
 * @author mdesales
 */
public class ActionCommandExecutorService implements ApplicationContextAware {

    boolean transactional = true

    ApplicationContext applicationContext

    def ctfRemoteClientService

    /** The default Master instance */
    def defaultMaster
    
    /** The current Replica instance */
    def currentReplica

    def bootStrap = { initialMaster, initialReplica ->
        this.defaultMaster = initialMaster
        this.currentReplica = initialReplica
    }

    /**
     * @param username is the username on the CEE site
     * @param password is the password associated with the username on the
     * CEE site
     * @return boolean if the local cache contains the given username and 
     * password as a key. 
     * @throws RemoteAccessException if the communication fails with the remote
     * Master host for any reason.
     */
    def retrieveAndExecuteActionCommands(){
    
        //receive the commands from cee
        def remoteResponse = ctfRemoteClientService?.getActionCommands()

        //translate the commands for execution
        def actionCommands = translateWsCommandsForExecution(remoteResponse.cmd)

        def actionCommandsResults = []
        //execute each command, having them being updated with status
        for(command in actionCommands) {
            actionCommandsResults << processCommandRequest(command)
        }
        //upload the commands results back to ctf
        ctfRemoteClientService.uploadActionCommandResults(actionCommandsResults)
    }

    /**
     * @param wsCommandsList is an array of ReplicaWs.CommandType
     * @return a list commands represented as the following conversion:
     * 
     * commands['id'] = wsCommand.id
     * commands['code'] = wsCommand.code
     * commands['params'] = wsCommand.param[]
     * 
     * The type commands['params'] is a list of the following:
     * 
     * commands['params']['name'] = wsCommand.param[].name
     * commands['params']['values'] = wsCommand.param[].val[].string or .int
     * 
     * The wsCommand.param[].val[] has types: string has precedence over the
     * others.
     * 
     */
    def translateWsCommandsForExecution(wsCommandsList) {
        def commands = []
        for(wsCommand in wsCommandsList) {
            def command = [:]
            command['id'] = wsCommand.id
            command['code'] = wsCommand.code
            def params = []
            for(wsCommandParameter in wsCommand.param) {
                def param = [:]
                param['name'] = wsCommandParameter.name
                def paramValues = []
                for (wsCommandParameterValue in wsCommandParameter.val) {
                    //the only implemented types are string and int... If 
                    //the webservice type Replica.CommandParameterValueType 
                    //adds support to more, this needs to be changed.
                    //String has precedence over int here.
                    def paramValue = wsCommandParameterValue.string ?: 
                            (int) wsCommandParameterValue.int
                    paramValues << paramValue
                }
                param['values'] = paramValues
                params << param
            }
            command['params'] = params
            commands << command
        }
        return commands
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
     * <li>command['status'] = the status of the command, which is a boolean
     * value that determines if the command executed successfully or not.
     * <li>command['exception'] = the exception that happened during the
     * execution of the command, if any.
     * 
     */
    def processCommandRequest(command) {
        def commandPackage = "com.collabnet.svnedge.replica.commands"
        
        def className = command['code'][0].toUpperCase() + 
            command['code'][1..-1] + "Command"

        def classObject = null
        try {
            classObject = getClass().getClassLoader().loadClass(
                    "$commandPackage.$className")
        } catch (ClassNotFoundException clne) {
            command['exception'] = 
                new CommandNotImplementedException(clne, className)
            command['status'] = false
            return command
        }
        
        def commandInstance = classObject.newInstance();
        commandInstance.init(command['params'], applicationContext)
        try {
            commandInstance.run()
        } catch (CommandExecutionException ceex) {
            command['exception'] = ceex
        }
        command['status'] = commandInstance.succeeded
        return command
    }
}
