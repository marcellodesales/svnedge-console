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

import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.replica.manager.RepoStatus
import com.collabnet.svnedge.replication.ReplicaConfiguration
import com.collabnet.svnedge.replication.jobs.FetchReplicaCommandsJob
import com.collabnet.svnedge.teamforge.CtfServer
import static com.collabnet.svnedge.console.services.JobsAdminService.REPLICA_GROUP

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
    /**
     * The parameters to execute the method.
     */
    protected originalParameters
    /**
     * The parameters sent for the commands. These are updated with the values
     * for any exceptions and results.
     */
    protected Map params

    /**
    * Constructs a new abstract replica command.
    */
   def AbstractReplicaCommand() {
       succeeded = false
       params = new HashMap<String, Object>()
   }

    /**
     * @param serviceName the service name.
     * @return the instance of the service bean.
     */
    public getService(serviceName) {
        return appContext.getBean(serviceName)
    }

    /**
     * Initializes the command with the received parameters and the application
     * context.
     * @param initialParameter the initial parameters received for the command.
     * @param appCtx the application context used to acquire service instances.
     */
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

    /**
     * Adds the repository on the database.  If the repository has no db
     * record, it will be added.  If it's been previously removed, it's
     * status will be changed back to NOT_READY_YET and enabled.
     */
    def addRepositoryOnDatabase(repoName) {
        def repoRecord = Repository.findByName(repoName)
        if (repoRecord) {
            def repo = ReplicatedRepository.findByRepo(repoRecord)
            repo.enabled = true;
            repo.status = RepoStatus.NOT_READY_YET
            repo.statusMsg = null
            repo.save()
        } else {
            Repository repository = new Repository(name:repoName)
            repository.save()
            new ReplicatedRepository(repo: repository, 
                lastSyncTime: -1, lastSyncRev:-1, enabled: true, 
                status: RepoStatus.NOT_READY_YET).save(flush:true)
        }
    }

    /*
     * Creates local replica repositories
     */
    def createRepositoryOnFileSystem(repoName) {
        def repo = Repository.findByName(repoName)
        def replRepo = ReplicatedRepository.findByRepo(repo)
        def svnRepoService = getService("svnRepoService")
        def repoPath = svnRepoService.getRepositoryHomePath(repo)
        if (new File(repoPath).exists()) {
            if (svnRepoService.verifyRepository(repo)) {
                log.info("createRepositoryOnFileSystem found an existing repo: "
                         + repoName)
                replRepo.status = RepoStatus.IN_PROGRESS
                replRepo.statusMsg = null
                replRepo.save()
            }  else {
                def msg = "createRepositoryOnFileSystem found existing directory " +
                    repoPath + ", but it would not verify as a valid repository."
                log.error(msg)
                replRepo.status = RepoStatus.ERROR
                replRepo.statusMsg = msg
                replRepo.save()
                throw new IllegalStateException(msg)
            }
        } else {
            if (svnRepoService.createRepository(repo, false) == 0) {
                log.info("Created the repo with svnadmin.")
                replRepo.status = RepoStatus.IN_PROGRESS
                replRepo.statusMsg = null
                replRepo.save()
                
                prepareHookScripts(repoPath, replRepo)
                prepareSyncRepo(repoPath, replRepo, repoName)
            } else {
                def msg = "Svnadmin failed to create repository: " + repoName
                log.error(msg)
                replRepo.status = RepoStatus.ERROR
                replRepo.statusMsg = msg
                replRepo.save()
                throw new IllegalStateException(msg)
            }
        }
    }

    /**
     * @return if the current server running is a windows box.
     */
    private boolean isWindows() {
        def operatingSystemService = getService("operatingSystemService")
        return operatingSystemService.isWindows()
    }

    /**
     * Prepares the hook scripts for the given repository path.
     * @param repoPath
     * @param repo
     */
    private def prepareHookScripts(repoPath, repo) {
        log.info("Changing the rev prop hooks.")
        def dummyPreRevPropChangeScript = repoPath +
                                          '/hooks/pre-revprop-change'
        if (isWindows()) {
            dummyPreRevPropChangeScript += ".bat"
        }
        new File("${dummyPreRevPropChangeScript}").withWriter { out ->
            out.writeLine("#!/bin/bash\nexit 0;\n")
        }
        if (!isWindows()) {
            def commandLineService = getService("commandLineService")
            commandLineService.executeWithStatus("chmod", "755", 
                dummyPreRevPropChangeScript)
        }
        log.info("Done changing the rev prop hooks.")
    }

    private def prepareSyncRepo(repoPath, repo, repoName) {
        log.info("Initing the repo...: " + repoName)
        def replicaConfig = ReplicaConfiguration.getCurrentConfig()
        def masterRepoUrl = replicaConfig.getSvnMasterUrl() + "/" + repoName
        def commandLineService = getService("commandLineService")
        def syncRepoURI = commandLineService.createSvnFileURI(
            new File(Server.getServer().repoParentDir, repoName))
        def ctfServer = CtfServer.getServer()
        def username = ctfServer.ctfUsername
        def securityService = getService("securityService")
        def password = securityService.decrypt(ctfServer.ctfPassword)
        def command = [ConfigUtil.svnsyncPath(), "init", syncRepoURI, 
            masterRepoUrl,
            "--source-username", username, "--source-password", password,
            "--non-interactive", "--no-auth-cache"] // "--config-dir=/tmp"

        execCommand(command, repo)
        log.info("Done initing the repo.")
        repo.lastSyncRev = 0

        def masterUUID = getMasterUUID(masterRepoUrl, username, password, 
            repoName)
        if (masterUUID) {
            command = [ConfigUtil.svnadminPath(), "setuuid", repoPath, 
                masterUUID]
            execCommand(command, repo)
            log.info("Done setting uuid ${masterUUID} of the repo as that " +
                "of master.")
            execSvnSync(repo, System.currentTimeMillis(), username, password, 
                syncRepoURI)
        }
    }

    /**
     * Returns Master Repository's UUID.
     */
    private def getMasterUUID(masterRepoUrl, username, password, repoName) {
        def uuid = null
        def retVal = 1
        def command = [ConfigUtil.svnPath(), "info", masterRepoUrl,
            "--username", username,"--password", password,
            "--non-interactive", "--no-auth-cache"] //"--config-dir=/tmp"
        def output = execCommand(command, null)
        int start = output.indexOf("Repository UUID: ") + 17
        if (start >= 17) {
            uuid = output.substring(start, output.indexOf("\n", start))
        } else {
            String msg = "Unable to get master UUID for repo: " + repoName
            log.warn(msg)
            throw new IllegalStateException(msg)
        }
        return uuid
    }
    
    /**
     * Returns revision number of last successful sync.
     * If there is *no* commit since the last sync this function itself
     * should not be called. But it would be called in situations when the
     * initial setup of '0' revision repositories(Not possible in CEE but
     * possible in CTF.).
     * In such situations it would return 0 indicating
     * do *not* update the lastSyncRev in DB.
     * If sync fails return -1 and updates the Repo record
     * in the db indicating failure.
     */
    def execSvnSync(repo, masterTimestamp, username, password, syncRepoURI) {
        log.info("Syncing repo '${repo.repo.name}' at " +
                " master timestamp: ${masterTimestamp}...")
        def command = [ConfigUtil.svnsyncPath(), "sync", syncRepoURI,
            "--source-username", username, "--source-password", password,
            "--non-interactive", "--no-auth-cache"] // "--config-dir=/tmp"
        
        def revision = -1
        def retVal = 1
        def msg = "${command} failed. "
        try {
            def commandLineService = getService("commandLineService")
            String[] result = commandLineService.execute(command.toArray(
                new String[0]))
            retVal = Integer.parseInt(result[0])
            msg += result[2]
            def output = result[1]
            if (output.length() > 0) {
                def numBuffer = output.substring(
                        output.lastIndexOf(' ') + 1, output.length() - 2)
                revision = java.lang.Long.parseLong(numBuffer)
            }
            if (retVal == 0 && revision == -1) {
                revision = 0
            }
        } catch (Exception e) {
            retVal = -1
            log.warn(msg, e)
            msg += e.getMessage()
        }
        if (retVal != 0) {
            log.warn(msg)
            repo.status = RepoStatus.ERROR
            repo.statusMsg = msg
            repo.save()
        }
        if (revision != -1) {
            repo.status = RepoStatus.OK
            repo.statusMsg = null
            repo.lastSyncTime = masterTimestamp
            if (revision)
                repo.lastSyncRev = revision
            repo.save()
        }
        log.info("Done syncing repo '${repo.repo.name}'.")
    }

    private String execCommand(command, repo) {
        def retVal = 1
        def msg
        String[] result = null
        try {
            def commandLineService = getService("commandLineService")
            result = commandLineService.execute(command.toArray(new String[0]))
            retVal = Integer.parseInt(result[0])
            msg = result[2]
        } catch (Exception e) {
            retVal = -1
            msg = "${command} failed: ${e.getMessage()}"
        }
        if (retVal != 0) {
            log.warn(msg)
            if (null != repo) {
                repo.status = RepoStatus.ERROR
                repo.statusMsg = msg
                repo.save()
            }
            throw new IllegalStateException(msg)
        }
        return result[1]
    }   

    def removeReplicatedRepository(repoName) {
        def repoDir = new File(Server.getServer().repoParentDir, repoName)
        if (repoDir && repoDir.exists()) {
            repoDir.deleteDir()
        }

        removeRepositoryOnDatabase(repoName)
    }

    /**
     * Removes the repository on the database.  This involves changing the
     * status, sync time/revs and disabling the repo.
     */
    private def removeRepositoryOnDatabase(repoName) {
        def repoRecord = Repository.findByName(repoName)
        if (!repoRecord) {
            log.error("removeRepositoryOnDatabase: No repo found for name " 
                            + "${repoName}")
        } else {
            def repo = ReplicatedRepository.findByRepo(repoRecord)
            if (repo) {
                repo.enabled = false;
                repo.status = RepoStatus.REMOVED
                repo.lastSyncTime = -1
                repo.lastSyncRev = -1
                repo.statusMsg = "Repository removed at " + new Date()
                repo.save()
            } else {
                log.error("removeRepositoryOnDatabase: No repo found for name " 
                          + "${repoName}")
            }
        }
    }

    /**
     * Updates replica configuration with non-null values of command parameters.
     * Used by ReplicaApprove and ReplicaPropsUpdate commands
     */
    protected def updateProps() {
        log.debug("Acquiring the replica configuration instance...")

        def replica = ReplicaConfiguration.getCurrentConfig()

        // update the name property
        if (this.params.name) {
            replica.name = this.params.name
        }

        // update the description property
        if (this.params.description) {
            replica.description = this.params.description
        }

        // update the command pool rate
        def poolRate = this.params.commandPollPeriod
        if (poolRate && poolRate.toInteger() > 0 && 
                poolRate.toInteger() != replica.commandPollRate) {

            replica.commandPollRate = poolRate.toInteger()

            // reschedule the job with the updated rate
            def jobsAdminService = getService("jobsAdminService")
            try {
                def interval = poolRate.toInteger() * 1000L
                jobsAdminService.rescheduleJob(
                    FetchReplicaCommandsJob.TRIGGER_NAME,
                    FetchReplicaCommandsJob.TRIGGER_GROUP, interval)

            } catch (Exception e) {
                log.error("Tried to reschedule the trigger and nothing happened", e)
                throw new IllegalStateException(e)
            }
        }

        // update the max number of long-running commands property
        def maxLongRunningCmds = this.params.commandConcurrencyLong
        if (maxLongRunningCmds && maxLongRunningCmds.toInteger() > 0 && 
                maxLongRunningCmds.toInteger() != replica.maxLongRunningCmds) {

            replica.maxLongRunningCmds = maxLongRunningCmds.toInteger()
        }

        // update the max number of short-running commands property
        def maxShortRunningCmds = this.params.commandConcurrencyShort
        if (maxShortRunningCmds && maxShortRunningCmds.toInteger() > 0 && 
                maxShortRunningCmds.toInteger() != replica.maxShortRunningCmds) {

            replica.maxShortRunningCmds = maxShortRunningCmds.toInteger()
        }

        log.debug("Trying to flush the saved replica properties...")
        replica.save(flush:true)
    }
}
