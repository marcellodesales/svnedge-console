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
package com.collabnet.svnedge.replication.command

import org.apache.log4j.Logger

/**
 * Defines the Abstract Action Command to be executed by the Action Commands
 * Executor Service. Any command implementation must extend this class, which
 * must be started from the method 'run()'. The execution of a command updates
 * the params with the following information:
 * 
 * params.succeeded = a boolean value that indicates whether the command run
 * successfully.
 * params.exeption = in the case of an unsuccessful execution, this property
 * contains the exception that occurred.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */
public abstract class AbstractReplicaCommand {

    private Logger log = Logger.getLogger(getClass())

    /**
     * The final state of the command. If it is false, the params property
     * will contain an exception
     */
    protected boolean succeeded
    /**
     * The originating exception thrown during the execution, if any occurs.
     */
    protected Throwable executionException
    /**
     * The originating exception thrown during the undo, if any occurs.
     */
    protected Throwable undoException
    /**
     * The Grails app context, which is needed to get instances of services
     * in the command classes, where methods are called from.
     */
    private appContext

    public getService(serviceName) {
        return appContext.getBean(serviceName)
    }

    /**
     * The parameters to execute the method.
     */
    protected originalParameters

    protected Map params

    def AbstractReplicaCommand() {
        succeeded = false
        params = new HashMap<String, Object>()
    }

    def init(initialParameter, appCtx) {
        appContext = appCtx
        params = initialParameter
        log.debug("Instantiating the command " + getClass().getName() + 
                " with the parameters " + initialParameter)
    }

    /**
     * The command execution constraints that must be met before the command
     * is executed.
     */
    public abstract constraints() throws Throwable;

    /**
     * The command execution constraints that must be met before the command
     * is executed.
     * @param params is the list of parameters in the following data structure,
     * having each item as a hash of the following structure:
     * 
     * params['name'] = the name of the parameter
     * params['values'] = is a list of values that can be .string or .int
     * value[string] = string representation
     * value[int] = int representation.
     */
    public abstract execute() throws Throwable;

    /**
     * The command execution constraints that must be met before the command
     * is executed.
     */
    public abstract undo() throws Throwable;
    
    /**
     * Before the execution of a command, the method 'constraints()' is defined
     * to verify any pre-conditions that must be met before the execution of 
     * the implementing Command class. If the constraints fail, the method 
     * 'execute()' is NOT executed.
     * 
     * When the method 'constraints()' finishes, the method 'execute()' runs 
     * with the given parameters, as instructed by the method.
     * 
     * If any exception is thrown from the methods 'constraints()' and 
     * 'execute()', the properties of the params will include the exception, 
     * and the succeeded property contains a false value. Right after that, 
     * the undo() method is executed to undo anything done. It's important to 
     * clean anything that changed the state of the system by the 'execute()' 
     * method.
     * @throws CommandExecutionException if any Exception occurs while executing
     * the methods 'contraints()' or 'execute()'.
     */
    public final void run() throws CommandExecutionException {
        try {
            log.debug("Verifying the constraints for the command...")
            constraints()
            log.debug("Constraints passed... executing the command...")
            execute()
            log.debug("Command execution was successful...")
            succeeded = true
        } catch (Throwable t) {
            succeeded = false
            executionException = t
            log.error("Failed to execute command: ${t.getMessage()}", t)
        }

        if (executionException) {
            try {
                log.debug("Undoing the command because the exception " +
                        "${executionException.getClass().getName()}: " + 
                        executionException.getMessage())
                undo()
                log.debug("Undid the command successful...")
            } catch (Throwable t) {
                undoException = t
                log.error("Failed to undo the execution of the command: " + 
                        t.getMessage())
            }
        }

        if (executionException || undoException) {
            log.debug("Preparing to throw the exceptions: ")
            if (executionException) {
                log.debug(executionException.getClass().getName())
            }
            if (undoException) {
                log.debug(undoException.getClass().getName())
            }
            throw new CommandExecutionException(this, executionException, 
                    undoException)
        }
    }
    
    /**
     * Takes the "repoName" parameter and strips any parent paths
     * @return just the path name after the final /
     */
    protected String getRepoName() {
        String repoName = this.params["repoName"]
        int pos = repoName.lastIndexOf('/');
        if (pos >= 0 && repoName.length() > pos + 1) {
            repoName = repoName.substring(pos + 1)
        }
        return repoName
    }
}
