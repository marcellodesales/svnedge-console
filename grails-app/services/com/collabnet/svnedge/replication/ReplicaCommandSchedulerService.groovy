package com.collabnet.svnedge.replication


import java.util.LinkedList
import java.util.Collections

import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener

import com.collabnet.svnedge.console.services.AbstractSvnEdgeService

class ReplicaCommandSchedulerService extends AbstractSvnEdgeService {

    /**
     * The command category for the replica server.
     */
    private static final String REPLICA_COMMAND_CATEGORY = "replicaServer"
    /**
     * The prefix of the command ids.
     */
    private static final String COMMAND_ID_PREFIX = "cmdexec"
    /**
     * This is the list of commands received from the master.
     * [cmd[id:"cmd1001", code:"addRepo"], ...]
     */
    LinkedList<Map> queuedCommands
    /**
     * The execution mutex is a map [category, commandID]
     */
    Map<String, String> executionMutex
    /**
     * The command ID index to speed up the offer method.
     */
    Set<String> commandIdIndex

    /**
     * Bootstraps the service
     */
    def bootStrap = {
        cleanCommands()
    }

    /**
     * Initializes all the data structures for the queued commands.
     */
    void cleanCommands() {
        queuedCommands = Collections.synchronizedList(new LinkedList<Map>())
        executionMutex = Collections.synchronizedMap(new LinkedHashMap<String, String>())
        commandIdIndex = Collections.synchronizedSet(new LinkedHashSet<String>())
    }

    /**
     * @return the number of commands queued, not yet executing.
     */
    def getQueuedCommandsSize() {
        return queuedCommands.size()
    }

    /**
     * @return the number of categories of commands executing.
     */
    def getExecutingCommandsSize() {
        return executionMutex.size()
    }

    /**
     * Schedules the given command for execution.
     * @param category the name of the category or "replicaServer"
     * @param command is the command map with id, code, etc.
     */
    def synchronized void scheduleCommandForExecution(category, command) {
        addCommandToCategoryMutex(category, command.id)
        removeQueuedCommand(command.id)
    }

    /**
     * Adds a mutex for the commandID in the given category.
     * @param category is the name of the repo or "replicaServer"
     * @param commandId is the ID of the command.
     */
    def synchronized void addCommandToCategoryMutex(category, commandId) {
        if (!category) {
            throw new IllegalArgumentException("The category must be provided")
        }
        if (!commandId) {
            throw new IllegalArgumentException("The category must be provided")
        }
        synchronized(executionMutex) {
            executionMutex[category] = commandId
        }
    }

    /**
     * @param category is the name of the repo or "replicaServer".
     * @return the command ID of the command executing for the given category.
     */
    def getExecutingCommand(category) {
        return executionMutex[category]
    }

    /**
     * @param category is the name of the category or 
     * @return if there is a command running in the given category.
     */
    def synchronized boolean isThereCommandRunning(category) {
        if (!category) {
            throw new IllegalArgumentException("The category must be provided")
        }
        return executionMutex[category] != null
    }

    /**
     * Removes the given command ID from the queued commands list
     * @param queuedCommandId is the ID of the command to be removed.
     */
    def synchronized void removeQueuedCommand(queuedCommandId) {
        // removes from the queued commands
        synchronized (queuedCommands) {
            def foundAt = -1
            for (int i = 0; i < queuedCommands.size(); i++) {
                if (queuedCommands.get(i).id == queuedCommandId) {
                    foundAt = i
                    break
                }
            }
            if (foundAt != -1) {
                queuedCommands.remove(foundAt)
            }
        }
    }

    /**
     * Remove terminated command from the mutex map.
     * @param finishedCommandId the ID of the command.
     */
    def synchronized void removeTerminatedCommand(finishedCommandId) {
        if (!finishedCommandId) {
            return
        }
        // removes from the category mutex
        synchronized (executionMutex) {
            def categoryKey = null
            executionMutex.each{ category, commandId ->
                if (commandId.equals(finishedCommandId)) {
                    categoryKey = category
                }
            }
            if (categoryKey) {
                executionMutex.remove(categoryKey)
            }
        }
        // removes the command ID from the index.
        synchronized(commandIdIndex) {
            commandIdIndex.remove(finishedCommandId)
        }
    }

    /**
     * @param cmd a given command object.
     * @return the name of the repository name or the constant 
     * REPLICA_COMMAND_CATEGORY.
     */
    def getCommandCategory(cmd) {
        return cmd.repoName == "null" ? REPLICA_COMMAND_CATEGORY : cmd.repoName
    }

    /**
     * @param category the repository name.
     * @param command the command object.
     * @return if the given command is the next to be executed. That is, if the
     * given command is the first command to be executed given the command ID.
     */
    def synchronized isCommandNextForCategory(category, command) {
        return getNextCommandFromCategory(category).id == command.id
    }

    /**
     * @return the groups of commands by the repository name or by the constant 
     * REPLICA_COMMAND_CATEGORY.
     */
    def synchronized getCategorizedCommandQueues() {
        // map grouping the tasks by granularity (repoName or replica server)
        synchronized(queuedCommands) {
            return queuedCommands.groupBy{ cmd ->
                getCommandCategory(cmd)
            }
        }
    }

    /**
     * @param category is the name of a repository or "replicaServer".
     * @return the next command to be executed from queue of commands in the
     * given category. The queue of commands is sorted by the id.
     */
    def synchronized getNextCommandFromCategory(category) {
        if (!category) {
            return
        }
        // map grouping the tasks by granularity (repoName or replica server)
        synchronized(queuedCommands) {
            def categorizedQueuedCommands = getCategorizedCommandQueues()
            // compares the value of the keys "id" from the list of maps
            // removing the prefix.
            def idComparator= [
                compare: {a,b-> 
                    (a.id.replace(COMMAND_ID_PREFIX,"") as Integer) -
                    (b.id.replace(COMMAND_ID_PREFIX,"") as Integer)
                }
              ] as Comparator
            return categorizedQueuedCommands[category].sort(idComparator)[0]
        }
    }

    /**
     * Offer new commands to the current queue.
     * @param newCommands the set of commands received from the remote master.
     */
    def synchronized offer(newCommands) {
        if (!newCommands || newCommands == []) {
            return
        }
        synchronized(queuedCommands) {
            for (command in newCommands) {
                if (!commandIdIndex.contains(command.id)) {
                    queuedCommands << command
                    commandIdIndex << command.id
                }
            }
        }
    }

   /**
    * The next command from the list for a given index.
    * @param commandIndex is the index in the list.
    * @return the command in the given position in the list. If the
    * commandIndex is null, the first element of the list is returned.
    */
   def synchronized scheduleNextCommand() {
       synchronized(queuedCommands) {
           for(int i=0; i < getQueuedCommandsSize(); i++) {
               def command = queuedCommands.get(i)
               def category = getCommandCategory(command)
               if (isCommandNextForCategory(category, command) &&
                       !isThereCommandRunning(category)) {
                   scheduleCommandForExecution(category, command)
                   return command
               }
           }
       }
       return null
   }
}
