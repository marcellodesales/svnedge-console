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

import com.collabnet.svnedge.replica.manager.ApprovalState;
import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.replica.manager.RepoStatus
import com.collabnet.svnedge.teamforge.CtfServer;


import grails.test.*

class ReplicaCommandsExecutorIntegrationTests extends GrailsUnitTestCase {

    def replicaCommandExecutorService
    def commandLineService
    def securityService
    def grailsApplication
    def config

    def REPO_NAME = "testproject2"
    def EXSY_ID = "exsy9876"
    def rConf
    File repoParentDir
        
    public ReplicaCommandsExecutorIntegrationTests() {
        this.rConf = ReplicaConfiguration.getCurrentConfig()
        if (!this.rConf) {
            rConf = new ReplicaConfiguration(svnMasterUrl: null, 
                name: "Test Replica", description: "Super replica", 
                message: "Auto-approved", systemId: "replica1001", 
                svnSyncRate: 5, approvalState: ApprovalState.APPROVED)
            this.rConf.save()
        }
        // Setup a test repository parent
        repoParentDir = createTestDir("repo")
    }

    protected void setUp() {
        super.setUp()

        assertNotNull("The replica instance must exist", this.rConf)
        this.config = grailsApplication.config
        this.rConf.svnMasterUrl = makeCtfBaseUrl() + "/svn/repos"
        this.rConf.save()

        Server server = Server.getServer()
        server.repoParentDir = repoParentDir.getCanonicalPath()
        server.save()
        
        // delete the repo directory for the repo we are adding.
        def repoFileDir = new File(repoParentDir, REPO_NAME)
        repoFileDir.deleteDir()
        
        CtfServer ctfServer = CtfServer.getServer()    
        ctfServer.baseUrl = makeCtfBaseUrl()
        ctfServer.mySystemId = EXSY_ID
        ctfServer.ctfUsername = "admin"
        ctfServer.ctfPassword = "n3TEQWKEjpY="
        ctfServer.save()
    }
    
    private def makeCtfBaseUrl() {
        def ctfProto = config.svnedge.ctfMaster.ssl ? "https://" : "http://"
        def ctfHost = config.svnedge.ctfMaster.domainName
        def ctfPort = config.svnedge.ctfMaster.port == "80" ? "" : ":" +
                config.svnedge.ctfMaster.port
        return ctfProto + ctfHost + ctfPort
    }

    /**
     * Test processing a bad command.
     */
    void testProcessBadCommand() {
        def badCommand = [code: 'Notacommand', id: 0, params: []]
        def result = replicaCommandExecutorService.processCommandRequest(
            badCommand)
        assertNotNull("Processing a bad command should not return null.", 
                      result)
        assertNotNull("Processing a bad command should return an exception.", 
                      result['exception'])
        assertFalse("Processing a bad command should return a false succeeded.",
                    result['succeeded'])
    }

    /**
     * Test processing a good add command.
     */
    void testProcessAddCommand() {
        def cmdParams = [:]
        cmdParams["repoName"] = REPO_NAME
        cmdParams["masterId"] = EXSY_ID

        def command = [code: 'repoAdd', id: 0, params: cmdParams]
        def result = replicaCommandExecutorService.processCommandRequest(
            command)
        assertNotNull("Processing a command should not return null.", 
                      result)
        if (result['exception']) {
            println result['exception']
        }
        assertNull("Processing a command should not return an exception.\n" + 
                   result['exception'], result['exception'])
        assertTrue("Processing a command should return a true succeeded.",
                   result['succeeded'])
    }

    /**
     * Test processing a good remove command.
     */
    void testProcessRemoveCommand() {
        def cmdParams = [:]
        cmdParams["repoName"] = REPO_NAME
        cmdParams["masterId"] = EXSY_ID

        // add first
        def command = [code: 'repoAdd', id: 0, params: cmdParams]
        def result = replicaCommandExecutorService.processCommandRequest(
            command)
        // then remove
        command = [code: 'repoRemove', id: 0, params: cmdParams]
        result = replicaCommandExecutorService.processCommandRequest(command)
        assertNotNull("Processing a command should not return null.", result)
        if (result['exception']) {
            println result['exception']
        }
        assertNull("Processing a command should not return an exception.\n" + 
                   result['exception'], result['exception'])
        assertTrue("Processing a command should return a true succeeded.",
                   result['succeeded'])
    }

    /**
     * Test processing the replica props update command.
     */
    void testProcessReplicaPropsUpdateCommand() {
        def newName = "Updated Replica Name"
        def newDescription = "Description Updated"
        def cmdParams = [:]
        cmdParams["name"] = newName
        cmdParams["description"] = newDescription

        // add first
        def command = [code: 'replicaPropsUpdate', id: 0, params: cmdParams]
        def result = replicaCommandExecutorService.processCommandRequest(
            command)

        if (result['exception']) {
            println result['exception']
        }
        assertNull("Processing a command should not return an exception.\n" + 
                   result['exception'], result['exception'])
        assertTrue("Processing a command should return a true succeeded.",
                   result['succeeded'])

        this.rConf = ReplicaConfiguration.getCurrentConfig()
        assertEquals("The name should have been changed with the command " +
            "update", this.rConf.name, newName)
        assertEquals("The description should have been changed with the " +
            "command update", this.rConf.description, newDescription)
    }

    void testAddReplicatedRepository() {
        replicaCommandExecutorService.addReplicatedRepository(REPO_NAME)

        def repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertNotNull("The repo should exist.", repo)
        
        File repoDir = new File(repoParentDir, REPO_NAME)
        assertTrue("Repository directory should exist: " + repoDir.getCanonicalPath(), repoDir.exists())
    }

    void testRemoveReplicatedRepository() {
        // create first
        replicaCommandExecutorService.addReplicatedRepository(REPO_NAME)
        
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertNotNull("The repo should exist.", repo)
        replicaCommandExecutorService.removeReplicatedRepository(REPO_NAME)
        repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertNotNull("The repo should exist.", repo)
        assertEquals("The repo's status should be REMOVED.",
                     RepoStatus.REMOVED, repo.getStatus())

        File repoDir = new File(repoParentDir, REPO_NAME)
        assertFalse("Repository directory should not exist: " + repoDir.getCanonicalPath(), repoDir.exists())
    }
    
    void testAddRemoveReAddRepoOnDatabase() {
        // create first
        replicaCommandExecutorService.addReplicatedRepository(REPO_NAME)

        def repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertNotNull("The repo should exist.", repo)
        // removed
        replicaCommandExecutorService.removeReplicatedRepository(REPO_NAME)
        repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertNotNull("The repo should exist.", repo)
        // add again
        try {
            replicaCommandExecutorService.addReplicatedRepository(REPO_NAME)
        } catch (Exception e) {
            fail("Should be able to re-add a removed repository.")
        }
        repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertTrue("The repository should be enabled.", repo.getEnabled())
    }

    void testSyncReplicatedRepository() {
        replicaCommandExecutorService.addReplicatedRepository(REPO_NAME)
        
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(REPO_NAME))
        assertNotNull("The repo should exist.", repo)
        
        File repoDir = new File(repoParentDir, REPO_NAME)
        assertTrue("Repository directory should exist: " + repoDir.getCanonicalPath(), repoDir.exists())
        
        File wcDir = createTestDir("wc")
        
        ReplicaConfiguration replicaConfig = ReplicaConfiguration.getCurrentConfig()
        def masterRepoUrl = replicaConfig.getSvnMasterUrl() + "/" + REPO_NAME
        
        def ctfServer = CtfServer.getServer()
        def username = ctfServer.ctfUsername
        def password = securityService.decrypt(ctfServer.ctfPassword)
        def command = [ConfigUtil.svnPath(), "co", masterRepoUrl, wcDir.canonicalPath,
            "--username", username, "--password", password,
            "--non-interactive", "--no-auth-cache"] // "--config-dir=/tmp"
        commandLineService.execute(command.toArray(new String[0]))
        
        File dotSvn = new File(wcDir, ".svn")
        assertTrue("Working copy missing .svn folder", dotSvn.exists())
        
        def testFile = File.createTempFile("sync-test", ".txt", wcDir)
        String filename = testFile.name
        testFile.text = "This is a test file"
        command = [ConfigUtil.svnPath(), "add", testFile.canonicalPath,
            "--non-interactive"]
        commandLineService.execute(command.toArray(new String[0]))
        
        command = [ConfigUtil.svnPath(), "ci", testFile.canonicalPath,
            "--username", username, "--password", password,
            "--non-interactive", "-m", "Test commit"]
        commandLineService.execute(command.toArray(new String[0]))
        
        def repoUri = commandLineService.createSvnFileURI(repoDir)
        File wcDir2 = createTestDir("wc2")
        command = [ConfigUtil.svnPath(), "co", repoUri, wcDir2.canonicalPath,
            //"--username", username, "--password", password,
            "--non-interactive", "--no-auth-cache"]
        commandLineService.execute(command.toArray(new String[0]))
        File testFile2 = new File(wcDir2, filename)
        assertFalse("Test file should not exist yet: " + testFile2.canonicalPath, testFile2.exists())
        
        replicaCommandExecutorService.syncReplicatedRepository(REPO_NAME)
        
        boolean fileExists = false
        for (int i = 0; i < 30 && !fileExists; i++) {
            command = [ConfigUtil.svnPath(), "up", wcDir2.canonicalPath,
                //"--username", username, "--password", password,
                "--non-interactive", "--no-auth-cache"]
            commandLineService.execute(command.toArray(new String[0]))
            fileExists = testFile2.exists()
            if (!fileExists) {
                Thread.sleep(1000)
            }
        }
        assertTrue("Test file should exist: " + testFile2.canonicalPath, fileExists)
    }
    
    //TODO: Enable these tests when Replication is
    void skip_testCreateRepoOnFS() {
        def replicaDir = File.createTempFile("repo-test", null)
        log.info("replicaDir = " + replicaDir.getCanonicalPath())
        // we want a dir, not a file, so delete and mkdir
        replicaDir.delete()
        replicaDir.mkdir()
        replicaDir.deleteOnExit()
        svnNotificationService.svnReplicaParentPath = replicaDir
            .getCanonicalPath()
        def repoName = "testproject"
        svnNotificationService.addRepositoryOnDatabase(repoName)
        svnNotificationService.createRepositoryOnFileSystem(repoName)
        def repoDir = new File(replicaDir.getCanonicalPath() + "/" + repoName)
        assertTrue("The repo directory should exist.", repoDir.exists())
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        log.info("Repo status msg: " + repo.getStatusMsg())
        assertTrue("The repository should have OK status.",
                   repo.status.equals(RepoStatus.OK))
        def defaultMaster = svnNotificationService.getMaster()
        def masterUUID = svnNotificationService.getMasterUUID(defaultMaster,
                                                              repoName)
        def replicaUUID = null

        try {
            def replicaUUIDFile = replicaDir.getCanonicalPath() +
                                  "/" + repoName + "/db/uuid"
            new File(replicaUUIDFile).withReader { reader ->
                replicaUUID = reader.readLine()
            }
        } catch (Exception e) {
            fail("Not able to read the replica's UUID file.")
        }
        assertEquals("UUID of replica repo and Master repo should be same.",
                     masterUUID, replicaUUID)
    }

    private File createTestDir(String prefix) {
        def testDir = File.createTempFile(prefix + "-test", null)
        log.info("testDir = " + testDir.getCanonicalPath())
        // we want a dir, not a file, so delete and mkdir
        testDir.delete()
        testDir.mkdir()
        // TODO This doesn't seem to work, might need to delete in teardown
        testDir.deleteOnExit()
        return testDir
    }
}
