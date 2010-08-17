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
package com.collabnet.svnedge.replica.service

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.apache.commons.lang.StringEscapeUtils

import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.replica.manager.RepoStatus
import com.collabnet.svnedge.replica.manager.Master

class SvnNotificationService {

    boolean transactional = true
    def commandLineService

    private static final LAST_UPDATE_FILE = ".lastupdate"

    def svnReplicaParentPath
    def svn
    def svnsync
    def svnadmin
    //sync rate in mintes
    def svnSyncRate
    def replicaHostName
    def recentMasterTimeStamp

    // Inject CtfRemoteClientService
    def ctfRemoteClientService
    
    def actionCommandsExecutorService

    def config = ConfigurationHolder.config

    def isWindows = false
    
    def bootStrap = { repoDirectoryPath, svnPath, svnAdminPath, svnSyncPath, 
            syncRate, initialtMaster ->
        if (System.getProperty("os.name").startsWith("Windows")) {
            isWindows = true
        }
        svn = svnPath
        svnadmin = svnAdminPath
        svnsync = svnSyncPath
        svnSyncRate = syncRate
        svnReplicaParentPath = repoDirectoryPath
        def repoDirFile = new File(repoDirectoryPath)

        if (!(repoDirFile.exists())) {
            if (!(repoDirFile.mkdir())) {
                def errorMsg = "Svn Notification Service failed: make sure " +
                        "the user running grails has write permission to the " +
                        "path '$repoDirFile'."
                log.error(errorMsg)
                throw new RuntimeException(errorMsg)
            } else {
                log.info("'$repoDirectoryPath' successfully created...")
            }
        } else
        if (!repoDirFile.canWrite()) {
            def errorMsg = "Svn Notification Service failed: make sure the " +
                    "user running grails has write permission to the path " +
                    "'$repoDirFile'."
            log.error(errorMsg)
            throw new RuntimeException(errorMsg)
        }
        log.info("SVN Replicas will be located at " + getSyncStatusFilePath())
    }

    def getMaster() {
        return Master.getDefaultMaster()
    }

    def getSvnSyncRate() {
        return svnSyncRate
    }

    def getReplicaParentDirPath() {
        return svnReplicaParentPath
    }

    def getSyncStatusFilePath() {
        return getReplicaParentDirPath() + "/" + LAST_UPDATE_FILE
    }

    private execCommandWithResults(command, repo) {
        def retVal = 1
        def msg
        String[] result = null
        try {
            result = commandLineService.execute(command.split(" "))
            retVal = Integer.parseInt(result[0])
            msg = result[2]
        } catch (Exception e) {
            retVal = -1
            msg = "Configured ${command} failed: ${e.getMessage()}"
        }
        if (retVal != 0) {
            log.error(msg)
            if (null != repo) {
                repo.status = RepoStatus.ERROR
                repo.statusMsg = msg
                repo.save()
            }
        }
        return result
    }

    private def execCommand(command, repo) {
        return execCommandWithResults(command, repo)[0] == '0'
    }

    private def execCommandWithOutput(command, repo) {
        return execCommandWithResults(command, repo)[1]
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
        def command = "${svnsync} sync --source-username" +
            " ${username} --source-password" +
            " ${password} ${syncRepoURI}" +
            " --non-interactive --no-auth-cache --config-dir=/tmp"

        def revision = -1
        def retVal = 1
        def msg = "Configured ${command} failed."
        try {
            String[] result = commandLineService.execute(command.split(" "))
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
            log.warn("Configured ${command} failed.", e)
            msg = "Configured ${command} failed: ${e.getMessage()}"
        }
        if (retVal != 0) {
            log.error(msg)
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
    /**
     * Returns Master Repository's UUID.
     */
    def getMasterUUID(defaultMaster, repoName) {
        def UUID = null
        def retVal = 1
        def protocol = defaultMaster.sslEnabled ? "https" : "http"
        def masterRepoUrl = "${protocol}://${defaultMaster.hostName}/" +
                            "svn/repos/${repoName}"
        def password = defaultMaster.accessPassword.replaceAll(/"/, /\\"/)
        password = quoteIfWindows(password)
        def command = "${svn} info ${masterRepoUrl}" +
                  " --username ${defaultMaster.accessUsername}" +
                  " --password ${password} --non-interactive --no-auth-cache" +
                  " --config-dir=/tmp"
        def output = execCommandWithOutput(command, null)
        int start = output.indexOf("Repository UUID: ") + 17
        if (start >= 17) {
            UUID = output.substring(start, output.indexOf("\n", start))
        }
        return UUID
    }
    /**
     * Makes the getSVNNotifications SOAP call and processes the results.
     */
    def retrieveAndProcessSVNNotifications() {

        def lastUpdateTimeFile = svnReplicaParentPath + "/" + LAST_UPDATE_FILE
        def lastupdatetime = 0L

        try {
            new File(lastUpdateTimeFile).withReader { reader ->
                lastupdatetime = (reader.readLine()).toLong()
            }
        } catch (Exception e) {
            lastupdatetime = 0L
        }

        def notifications = getSvnNotifications(lastupdatetime)
        def masterTimestamp = processNotifications(notifications)
        def hasCommands = notifications.hasCommands
        new File("${lastUpdateTimeFile}").withWriter { out ->
            out.writeLine("${masterTimestamp}")
        }
        def result = [:]
        result['masterTimestamp'] = masterTimestamp
        recentMasterTimeStamp = masterTimestamp
        result['hasCommands'] = hasCommands
        return result
    }

    /*
     * Retrieves the svn notifications from the master
     */
    def getSvnNotifications(ts) {
        return ctfRemoteClientService?.getSVNNotifications(ts)
    }

    /*
     * Handles the response from getSVNNotifications
     */
    def processNotifications(notices) {
        // The format for "notices":
        // notices.masterTimestamp:  the master's timestamp value
        //(in milli secs).
        // notices.repoUpdates.notifyItem:  array of NotifyItem objects,
        // each NotifyItem object has a repoName and a revision property.
        def svnReplicaSyncRootURI = commandLineService
            .createSvnFileURI(new File(svnReplicaParentPath))
        def defaultMaster = getMaster()
        def username = defaultMaster.accessUsername
        def password = defaultMaster.accessPassword.replaceAll(/"/, /\\"/)
        password = quoteIfWindows(password)

        for (notifyItem in notices.repoUpdates.notifyItem) {
            def repo = ReplicatedRepository.findByRepo(Repository.findByName(notifyItem.repoName))
            if (repo) {
                repo.status = RepoStatus.IN_PROGRESS
                repo.save()
                def syncRepoURI = "${svnReplicaSyncRootURI}/${notifyItem.repoName}"
                syncRepoURI = quoteIfWindows(syncRepoURI)
                if (notifyItem.revision == -1) {
                    execSvnSync(repo, notices.masterTimestamp, username, 
                                password, syncRepoURI)

                } else {
                    log.debug("Syncing revprops for rev ${notifyItem.revision}" +
                              " in repo ${notifyItem.repoName};" +
                              " TS from master: ${notices.masterTimestamp}")

                    def command = "${svnsync} copy-revprops --source-username" +
                        " ${username} --source-password" +
                        " ${password} ${syncRepoURI}" +
                        " --non-interactive --no-auth-cache --config-dir=/tmp" +
                        " ${notifyItem.revision}"
                    if (execCommand(command, repo)) {
                        repo.status = RepoStatus.OK
                        repo.statusMsg = null
                        repo.lastSyncTime = notices.masterTimestamp
                        repo.save()
                    }
                }
            } else {
                log.warn("${notifyItem.repoName} not found in local repo list")
            }
        }
        return notices.masterTimestamp
    }

    /*
     * Creates local replica repositories
     */
    def createRepositoryOnFileSystem(repoName) {
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        def repoPath = svnReplicaParentPath + "/" + repoName

        def command = "${svnadmin} create ${quoteIfWindows(repoPath)}" //+ 
//            "--config-dir=/tmp"
        if (execCommand(command, repo)) {
            log.info("Created the repo with svnadmin.")
            repo.status = RepoStatus.IN_PROGRESS
            repo.statusMsg = null
            repo.save()

            prepareHookScripts(repoPath, repo)
            prepareSyncRepo(repoPath, repo, repoName)
        } else {
            def msg = "Svnadmin failed to create repository."
            log.error(msg)
            repo.status = RepoStatus.ERROR
            repo.statusMsg = msg
            repo.save()
        }
    }

    private def prepareHookScripts(repoPath, repo) {
        log.info("Changing the rev prop hooks.")
        def dummyPreRevPropChangeScript = repoPath +
                                          '/hooks/pre-revprop-change'
        if (isWindows) {
            dummyPreRevPropChangeScript += ".bat"
        }
        new File("${dummyPreRevPropChangeScript}").withWriter { out ->
            out.writeLine("#!/bin/bash\nexit 0;\n")
        }
        if (!isWindows) {
            def command = "chmod 755 ${dummyPreRevPropChangeScript}"
            execCommand(command, repo)
        }
        log.info("Done changing the rev prop hooks.")
    }

    private def prepareSyncRepo(repoPath, repo, repoName) {
        log.info("Initing the repo...")
        def defaultMaster = getMaster()
        def protocol = defaultMaster.sslEnabled ? "https" : "http"
        def masterRepoUrl = "${protocol}://${defaultMaster.hostName}/" +
                            "svn/repos/${repoName}"
        def syncRepoURI = commandLineService.createSvnFileURI(
            new File(svnReplicaParentPath, repoName))
        syncRepoURI = quoteIfWindows(syncRepoURI)
        def password = defaultMaster.accessPassword.replaceAll(/"/, /\\"/)
        password = quoteIfWindows(password)
        def command = "${svnsync} init ${syncRepoURI} ${masterRepoUrl}" +
           " --source-username ${defaultMaster.accessUsername}" +
            " --source-password ${password}" +
            " --non-interactive --no-auth-cache --config-dir=/tmp"
        execCommand(command, repo)
        log.info("Done initing the repo.")
        repo.lastSyncRev = 0

        def masterUUID = getMasterUUID(defaultMaster, repoName)
        command = "${svnadmin} setuuid ${quoteIfWindows(repoPath)} ${masterUUID}"
        execCommand(command, repo)
        log.info("Done setting uuid ${masterUUID} of the repo as that of master.")

        execSvnSync(repo, recentMasterTimeStamp, 
            defaultMaster.accessUsername, password, syncRepoURI)
    }

    /**
     * Remove the repository on the filesystem, if it exists.
     */
    def removeRepositoryOnFileSystem(repoName) {
        def repoDir = new File(svnReplicaParentPath, repoName)
        repoDir.deleteDir()
    }

    /**
     * Adds the repository on the database.  If the repository has no db
     * record, it will be added.  If it's been previously removed, it's
     * status will be changed back to NOT_READY_YET and enabled.
     */
    def addRepositoryOnDatabase(repoName) {
        def repoRecord = Repository.findByName(repoName)
        if (!repoRecord) {
            createRepositoryOnDatabase(repoName)
        } else {
            def repo = ReplicatedRepository.findByRepo(repoRecord)
            repo.enabled = true;
            repo.status = RepoStatus.NOT_READY_YET
            repo.statusMsg = null
            repo.save()
        }
    }

    /**
     * Removes the repository on the database.  This involves changing the
     * status, sync time/revs and disabling the repo.
     */
    def removeRepositoryOnDatabase(repoName) {
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
     * Creates a new repository instance in the database layer, with the 
     * default properties.
     */
    def createRepositoryOnDatabase(repoName) {
        new ReplicatedRepository(repo: new Repository(name:repoName).save(), 
            lastSyncTime: -1, lastSyncRev:-1, enabled: true, 
            status: RepoStatus.NOT_READY_YET).save(flush:true)
    }

    /**
     * Wrap the given string in quotes, if the platform is windows.
     */
    def quoteIfWindows(str) {
        isWindows? "\"${str}\"" : str
    }
}
