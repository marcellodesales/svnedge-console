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

import com.collabnet.svnedge.domain.Repository 
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.integration.CtfServer 
import com.collabnet.svnedge.domain.integration.ReplicaConfiguration 
import com.collabnet.svnedge.domain.integration.ReplicatedRepository 
import com.collabnet.svnedge.domain.integration.RepoStatus 
import com.collabnet.svnedge.util.ConfigUtil;

/**
 * Defines the Abstract Repository Command used to manage the replicated
 * repositories.
 * 
 * @author John Mcnally (jmcnally@collab.net)
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
abstract class AbstractRepositoryCommand extends AbstractCommand {

    /**
     * Takes the "repoName" parameter and strips any parent paths
     * @return just the path name after the final /
     */
    protected String getRepoName() {
        String repoName = this.params["repoName"]
        if (!repoName) {
            return null
        }
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
            def repoReplica = ReplicatedRepository.findByRepo(repoRecord)
            if (repoReplica) {
                repoReplica.enabled = true;
                repoReplica.status = RepoStatus.NOT_READY_YET
                repoReplica.statusMsg = null
                repoReplica.save()

            } else {
                repoReplica = new ReplicatedRepository(repo: repoRecord,
                    lastSyncTime: -1, lastSyncRev:-1, enabled: true, 
                    status: RepoStatus.NOT_READY_YET)
                repoReplica.save(flush:true)
            }

        } else {
            Repository repository = new Repository(name:repoName)
            repository.save()
            ReplicatedRepository repoReplica = new ReplicatedRepository(
                repo: repository, lastSyncTime: -1, lastSyncRev:-1,
                enabled: true, status: RepoStatus.NOT_READY_YET)
            repoReplica.save(flush:true)
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
        replRepo.status = RepoStatus.OK
        replRepo.statusMsg = null
        replRepo.save()
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

        executeShellCommand(command, repo)
        log.info("Done initing the repo.")
        repo.lastSyncRev = 0

        def masterUUID = getMasterUUID(masterRepoUrl, username, password, 
            repoName)
        if (masterUUID) {
            command = [ConfigUtil.svnadminPath(), "setuuid", repoPath, 
                masterUUID]
            executeShellCommand(command, repo)
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
        def command = [ConfigUtil.svnPath(), "info", masterRepoUrl,
            "--username", username,"--password", password,
            "--non-interactive", "--no-auth-cache"] //"--config-dir=/tmp"
        def output = executeShellCommand(command, null)
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

    private String executeShellCommand(command, repo) {
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
}