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

import java.io.File;
import java.util.List;

import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode
import com.collabnet.svnedge.console.ConfigUtil;
import com.collabnet.svnedge.console.services.AbstractSvnEdgeService;
import com.collabnet.svnedge.replica.manager.RepoStatus
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.replication.command.CommandExecutionException
import com.collabnet.svnedge.replication.command.CommandNotImplementedException
import com.collabnet.svnedge.replication.jobs.FetchReplicaCommandsJob
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

    def commandLineService
    def ctfRemoteClientService
    def operatingSystemService
    def securityService
    def svnRepoService

    def cmdExecLogsDir

    def bootStrap = { dataDir ->
        cmdExecLogsDir = new File(dataDir + "/logs")
        log.debug("Commands execution will be logged to " + cmdExecLogsDir)

        if (Server.getServer().mode == ServerMode.REPLICA) {
            new FetchReplicaCommandsJob().start()
        }
    }

    /**
     * @return The current replica configuration.
     */
    private ReplicaConfiguration getCurrentReplica() {
        ReplicaConfiguration replica = ReplicaConfiguration.getCurrentConfig()
        if (!replica) {
            def msg = getMessage("filter.probihited.mode.replica", locale)
            throw new IllegalStateException(msg)
        }
        return replica
    }

    /**
     * Retrieves and executes the commands executions from the replica 
     * identified by the masterSystemId, managed by the given TeamForge URL.
     * @param ctfUrl is the CTF URL
     * @param userSessionId is a valid session ID
     * @param masterSystemId is the master external system.
     * @param locale is the local
     */
    def retrieveAndExecuteReplicaCommands(ctfUrl, userSessionId, masterSystemId,
            locale) {

        ReplicaConfiguration replica = getCurrentReplica()

        //receive the commands from ctf
        def queuedCommands = ctfRemoteClientService.getReplicaQueuedCommands(
            ctfUrl, userSessionId, masterSystemId, locale)

        log.debug("Retrieved " + (queuedCommands ? queuedCommands.size() : 0) + 
                  " commands for execution")
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
        ReplicaConfiguration replica = getCurrentReplica()

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
            logReplicaCommandExecution("LOAD", command, null)
            classObject = getClass().getClassLoader().loadClass(
                    "$commandPackage.$className")

        } catch (ClassNotFoundException clne) {
            command['exception'] = 
                new CommandNotImplementedException(clne, className)
            command['succeeded'] = false
            logReplicaCommandExecution("LOAD-FAILED", command, clne)
            return command
        }
        log.debug("Instantiating the class for the command " + command)
        def commandInstance = classObject.newInstance();
        log.debug("Class " + commandInstance)

        commandInstance.init(command['params'], applicationContext)
        log.debug("Initialized the parameters " + command['params'])
        try {
            logReplicaCommandExecution("RUN", command, null)
            commandInstance.run()
            log.debug("Command successfully run: " + command)
            logReplicaCommandExecution("RUN-SUCCESSED", command, null)

        } catch (CommandExecutionException ceex) {
            command['exception'] = ceex
            log.error("The command failed: " + command)
            logReplicaCommandExecution("RUN-FAILED", command, ceex)
        }
        command['succeeded'] = commandInstance.succeeded
        return command
    }

    def addReplicatedRepository(repoName) {
        log.debug("Creating a new repository on the database...")
        addRepositoryOnDatabase(repoName)

        log.debug("Creating a new repository on the file system...")
        createRepositoryOnFileSystem(repoName)
    }

    /**
     * Adds the repository on the database.  If the repository has no db
     * record, it will be added.  If it's been previously removed, it's
     * status will be changed back to NOT_READY_YET and enabled.
     */
    private def addRepositoryOnDatabase(repoName) {
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
    private def createRepositoryOnFileSystem(repoName) {
        def repo = Repository.findByName(repoName)
        def replRepo = ReplicatedRepository.findByRepo(repo)
        
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
            commandLineService.executeWithStatus("chmod", "755", 
                dummyPreRevPropChangeScript)
        }
        log.info("Done changing the rev prop hooks.")
    }

    private def prepareSyncRepo(repoPath, repo, repoName) {
        log.info("Initing the repo...: " + repoName)
        ReplicaConfiguration replicaConfig = ReplicaConfiguration.getCurrentConfig()
        def masterRepoUrl = replicaConfig.getSvnMasterUrl() + "/" + repoName
        def syncRepoURI = commandLineService.createSvnFileURI(
            new File(Server.getServer().repoParentDir, repoName))
        def ctfServer = CtfServer.getServer()
        def username = ctfServer.ctfUsername
        def password = securityService.decrypt(ctfServer.ctfPassword)
        def command = [ConfigUtil.svnsyncPath(), "init", syncRepoURI, masterRepoUrl,
            "--source-username", username, "--source-password", password,
            "--non-interactive", "--no-auth-cache"] // "--config-dir=/tmp"
        
        execCommand(command, repo)
        log.info("Done initing the repo.")
        repo.lastSyncRev = 0
        
        def masterUUID = getMasterUUID(masterRepoUrl, username, password, repoName)
        if (masterUUID) {
            command = [ConfigUtil.svnadminPath(), "setuuid", repoPath, masterUUID]
            execCommand(command, repo)
            log.info("Done setting uuid ${masterUUID} of the repo as that of master.")
            
            execSvnSync(repo, System.currentTimeMillis(), username, password, syncRepoURI)
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
            String[] result = commandLineService.execute(command.toArray(new String[0]))
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
     * Logs the execution of a command into the file 
     * "data/logs/replica_cmds_YYYY_MM_DD.log".
     * @param executionStep is a TOKEN of the execution step
     * @param command is the instance of a replica command execution.
     * @param exception is an optional execution thrown.
     */
    def logReplicaCommandExecution(executionStep, command, exception) {
        def now = new Date()
        //creates the file for the current day
        def logName = "replica_cmds_" + String.format('%tY_%<tm_%<td', now) +
            ".log"

        new File(cmdExecLogsDir, logName).withWriterAppend("UTF-8") {

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

    private boolean isWindows() {
        return operatingSystemService.isWindows()
    }
}