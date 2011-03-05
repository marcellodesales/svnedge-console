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
package com.collabnet.svnedge.integration.command

import java.util.Map

import org.apache.log4j.Logger

/**
 * Defines the Abstract Command to be instantiated based on the map
 * received from the Replica manager and executed by the Executor Service.
 *
 * After the execution of the command, a value is available that indicates if
 * the command succeeded or not. If a command failed, two exceptions might
 * be available: executionException and/or undoException.
 *
 * @author Marcello de Sales (mdesales@collab.net)
 */
abstract class AbstractCommand {

    private Logger log = Logger.getLogger(getClass())

    /**
     * The command execution ID
     */
    protected String id
    /**
     * The command execution ID
     */
    protected String repoName
    /**
     * The final state of the command. If it is false, the params property
     * will contain an exception
     */
    protected Boolean succeeded
    /**
     * The originating exception thrown during the execution, if any occurs.
     */
    protected Throwable executionException
    /**
     * The originating exception thrown during the undo, if any occurs.
     */
    protected Throwable undoException
    /**
     * The parameters sent for the commands. These are updated with the values
     * for any exceptions and results.
     */
    protected Map<String, String> params
    /**
     * The execution context for commands containing the information needed
     * to communicate with the Replica Manager (TeamForge).
     */
    protected CommandsExecutionContext context

    /**
     * Constructs a new abstract replica command.
     */
    def AbstractCommand() {
        succeeded = false
        params = new HashMap<String, String>()
    }

    @Override
    public String toString() {
        if (!succeeded) {
            return "${getClass().getSimpleName()}($id): params=${params}"
        }
        return "${getClass().getSimpleName()}" +
            "($id-${succeeded?'suceeded':'failed'}): params=${params}"
    }

    @Override
    public int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + ((id == null) ? 0 : id.hashCode())
        return result
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true
        if (obj == null)
            return false
        if (getClass() != obj.getClass())
            return false
        AbstractCommand other = (AbstractCommand) obj
        if (id == null) {
            if (other.id != null)
                return false
        } else if (!id.equals(other.id))
            return false
        return true
    }

    /**
     * Transforms the given Command Map into an instance of an 
     * Abstract Replica Command given the commandMap[code] property.
     * @param commandMap is an instance of a Map with the properties of a
     * command.
     * @param executionContext is the execution context of the command
     * @return an instance of Abstract Replica Command.
     */
    def static AbstractCommand makeCommand(classLoader, commandMap) {

        def commandPackage = "com.collabnet.svnedge.integration.command.impl"

        if (!commandMap['code']) {
            throw new IllegalArgumentException("The name (code) of the " +
                "command is missing.")
        }

        def className = commandMap['code'].capitalize() + "Command"
        def classObject = null
        try {
            logExecution("LOAD", commandMap, null)
            classObject = classLoader.loadClass("$commandPackage.$className")

        } catch (ClassNotFoundException clne) {
            commandMap['exception'] = 
                new CommandNotImplementedException(clne, className)
            commandMap['succeeded'] = false
            logExecution("LOAD-FAILED", commandMap, clne)
            return commandMap
        }
        def commandInstance = classObject.newInstance();

        commandInstance.init(commandMap.id, commandMap['params'], 
            commandMap.context)

        return commandInstance
    }

    /**
     * Initializes the command with the received parameters and the application
     * context.
     * @param initialParameter the initial parameters received for the command.
     * @param appCtx the application context used to acquire service instances.
     */
    def init(id, initialParameters, CommandsExecutionContext executionContext) {
        this.context = executionContext
        this.id = id
        this.params = initialParameters
        log.debug("Instantiating the command " + getClass().getName() + 
                " with the parameters " + initialParameters)
    }

    /**
     * The command execution constraints that must be met before the command
     * is executed.
     */
    public abstract constraints() throws Throwable

    /**
     * The main command intent implementation.
     */
    public abstract execute() throws Throwable

    /**
     * The command execution constraints that must be met before the command
     * is executed.
     */
    public abstract undo() throws Throwable

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
            logExecution("BEFORE-CONSTAINTS")
            constraints()
            logExecution("AFTER-CONSTAINTS")
            log.debug("Constraints passed... executing the command...")
            logExecution("BEFORE-EXECUTE")
            execute()
            logExecution("AFTER-EXECUTE")
            log.debug("Command execution was successful...")
            succeeded = true

        } catch (Throwable t) {
            succeeded = false
            executionException = t
            log.error("Failed to execute command: " + t.getMessage())
            if (t.cause) {
                executionException = t.cause
                logExecution("EXECUTION-EXCEPTION", t.cause)
            } else {
                logExecution("EXECUTION-EXCEPTION", t)
            }
        }

        if (executionException) {
            try {
                log.debug("Undoing the command because the exception " +
                        "${executionException.getClass().getName()}: " + 
                        executionException.getMessage())
                logExecution("BEFORE-UNDO")
                undo()
                logExecution("AFTER-UNDO")
                log.debug("Undid the command successful...")

            } catch (Throwable t) {
                undoException = t
                logExecution("UNDO-EXCEPTION", t)
                log.error("Failed to undo the execution of the command: " + 
                    t.getMessage())
            }
        }
        logExecution("END-RUN")

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
     * @param serviceName the service name.
     * @return the instance of the service bean.
     */
    public getService(serviceName) {
        if (!context) {
            throw new IllegalStateException("The CommandsExecutionContext " +
                "must be provided with the Grails Application Context property")
        }
        return context.appContext.getBean(serviceName)
    }

    /**
     * @param executionStep is a TOKEN of the execution step.
     */
    def logExecution(String executionStep) {
        logExecution(executionStep, this, null)
    }

    /**
     * @param executionStep is a TOKEN of the execution step
     * @param exception the instance of the exception thrown.
     */
    def logExecution(String executionStep, Throwable exception) {
        logExecution(executionStep, this, exception)
    }

    /**
     * @param executionStep is a TOKEN of the execution step
     * @param command the instance of the command.
     */
    def static logExecution(String executionStep, AbstractCommand command) {
        logExecution(executionStep, command, null)
    }

    /**
     * Logs the execution of a command into the file 
     * "data/logs/replica_cmds_YYYY_MM_DD.log".
     * @param executionStep is a TOKEN of the execution step
     * @param command is the instance of a replica command execution.
     * @param exception is an optional execution thrown.
     */
    def static logExecution(executionStep, command, exception) {
        def now = new Date()
        //creates the file for the current day
        def logName = "replica_cmds_" + String.format('%tY_%<tm_%<td', now) +
            ".log"
        if (!command?.context?.logsDir) {
            return
        }
        new File(command.context.logsDir, logName).withWriterAppend("UTF-8") {

            def timeToken = String.format('%tH:%<tM:%<tS,%<tL', now)

            def logEntry = timeToken + " " + executionStep + "-" + command.id +
                " " + command
            it.write(logEntry + "\n")

            if (exception) {
                def sw = new StringWriter();
                def pw = new PrintWriter(sw, true);
                exception.printStackTrace(pw);
                pw.flush();
                sw.flush();
                it.write(sw.toString() + "\n")
            }
        }
    }
}
