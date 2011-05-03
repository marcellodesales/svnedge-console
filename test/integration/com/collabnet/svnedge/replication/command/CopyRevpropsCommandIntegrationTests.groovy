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
package com.collabnet.svnedge.replication.command

import com.collabnet.svnedge.domain.Server

import com.collabnet.svnedge.integration.command.AbstractCommand
import com.collabnet.svnedge.integration.command.CommandsExecutionContext

import com.collabnet.svnedge.util.ConfigUtil
import grails.test.GrailsUnitTestCase

import com.collabnet.svnedge.domain.integration.*

class CopyRevpropsCommandIntegrationTests extends GrailsUnitTestCase {

    def replicaCommandExecutorService
    def commandLineService
    def securityService
    def jobsAdminService
    def grailsApplication
    def config

    def REPO_NAME = "testproject2"
    def EXSY_ID = "exsy9876"
    def rConf
    File repoParentDir
    CommandsExecutionContext executionContext

    public CopyRevpropsCommandIntegrationTests() {
        this.rConf = ReplicaConfiguration.getCurrentConfig()
        if (!this.rConf) {
            rConf = new ReplicaConfiguration(svnMasterUrl: null,
                name: "Test Replica", description: "Super replica",
                message: "Auto-approved", systemId: "replica1001",
                commandPollRate: 5, approvalState: ApprovalState.APPROVED)
            this.rConf.save()
        }
        // Setup a test repository parent
        repoParentDir = createTestDir("repo")
    }

    protected void tearDown() {
        super.tearDown()
        repoParentDir.deleteDir()

        // delete any log file
        AbstractCommand.getExecutionLogFile(executionContext)?.delete()
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

        executionContext = new CommandsExecutionContext()
        executionContext.logsDir = System.getProperty("java.io.tmpdir")
        executionContext.appContext = grailsApplication.mainContext
    }

    private def makeCtfBaseUrl() {
        def ctfProto = config.svnedge.ctfMaster.ssl ? "https://" : "http://"
        def ctfHost = config.svnedge.ctfMaster.domainName
        def ctfPort = config.svnedge.ctfMaster.port == "80" ? "" : ":" +
                config.svnedge.ctfMaster.port
        return ctfProto + ctfHost + ctfPort
    }


    public void testSyncRevprops() {
        def classLoader = getClass().getClassLoader()

        def revNumberToAlter
        def originalCommitMsg = "ORIGINAL commit msg"
        def updatedCommitMsg = "UPDATED commit msg"

        def cmdParams = [:]
        cmdParams["repoName"] = REPO_NAME
        cmdParams["masterId"] = EXSY_ID

        // add and sync the repo
        def commandMap = [code: 'repoAdd', id: 0, params: cmdParams, 
            context: executionContext]
        def command = AbstractCommand.makeCommand(classLoader, commandMap)
        replicaCommandExecutorService.commandLifecycleExecutor(command)

        if (command.executionException) {
            println command.executionException
            fail("Should be able to add a repository for sync.")
        }

        File wcMaster = createTestDir("wcMaster")

        def replicaConfig = ReplicaConfiguration.getCurrentConfig()
        def masterRepoUrl = replicaConfig.getSvnMasterUrl() + "/" + REPO_NAME

        def ctfServer = CtfServer.getServer()
        def username = ctfServer.ctfUsername
        def password = securityService.decrypt(ctfServer.ctfPassword)
        command = [ConfigUtil.svnPath(), "co", masterRepoUrl, wcMaster.canonicalPath,
            "--username", username, "--password", password,
            "--non-interactive", "--no-auth-cache"] // "--config-dir=/tmp"
        commandLineService.execute(command.toArray(new String[0]))

        // create / update the reusable test file
        def testFileMaster = new File("copy-revprops-test.txt", wcMaster)
        boolean svnAdd = !testFileMaster.exists()
        testFileMaster.text = "copy revprops test file: ${new Date()}"

        if (svnAdd) {
            command = [ConfigUtil.svnPath(), "add", testFileMaster.canonicalPath,
                "--non-interactive"]
            commandLineService.execute(command.toArray(new String[0]))
        }

        // set a custom property
        def propVal = "property initial value"
        command = [ConfigUtil.svnPath(), "propset", "propKey", propVal, testFileMaster.canonicalPath,
            "--username", username, "--password", password,
            "--non-interactive"]
        commandLineService.execute(command.toArray(new String[0]))

        // commit the test file & capture the revision number to alter later
        command = [ConfigUtil.svnPath(), "ci", testFileMaster.canonicalPath,
            "--username", username, "--password", password,
            "--non-interactive", "-m", originalCommitMsg]
        def result = commandLineService.execute(command.toArray(new String[0]))
        def matcher = result =~ /Committed revision (\d+)/
        revNumberToAlter = matcher[0][1]

        def repoUri = commandLineService.createSvnFileURI(new File(repoParentDir, REPO_NAME))
        File wcReplica = createTestDir("wcReplica")
        command = [ConfigUtil.svnPath(), "co", repoUri, wcReplica.canonicalPath,
            //"--username", username, "--password", password,
            "--non-interactive", "--no-auth-cache"]
        commandLineService.execute(command.toArray(new String[0]))
        File testFileReplica = new File(wcReplica, testFileMaster.name)

        cmdParams = [:]
        cmdParams["repoName"] = REPO_NAME
        cmdParams["masterId"] = EXSY_ID

        // execute svn sync
        commandMap = [code: 'repoSync', id: "cmdSync011", params: cmdParams,
            context: executionContext]
        command = AbstractCommand.makeCommand(classLoader, commandMap)
        replicaCommandExecutorService.commandLifecycleExecutor(command)

        if (command.executionException) {
            println command.executionException
            fail("Should be able to sync a command.")
        }

        // validate the file in the replica working copy
        boolean fileExists = false
        def fileRevNumber = -1
        for (int i = 0; i < 30 && !fileExists; i++) {
            command = [ConfigUtil.svnPath(), "up", wcReplica.canonicalPath,
                //"--username", username, "--password", password,
                "--non-interactive", "--no-auth-cache"]
            commandLineService.execute(command.toArray(new String[0]))
            fileExists = testFileReplica.exists()
            if (!fileExists) {
                Thread.sleep(1000)
            }
        }
        command = [ConfigUtil.svnPath(), "info", testFileReplica.canonicalPath,
            //"--username", username, "--password", password,
            "--non-interactive", "--no-auth-cache"]
        result = commandLineService.execute(command.toArray(new String[0]))
        matcher = result =~ /Revision: (\d+)/
        fileRevNumber = matcher[0][1]

        assertTrue("Replicated test file should exist: " + testFileReplica.canonicalPath, fileExists)
        assertEquals("Replicated test file should have expected rev number: ${revNumberToAlter}", revNumberToAlter, fileRevNumber)

        // validate inital revprops
        command = [ConfigUtil.svnPath(), "propget", "svn:log", "--revprop", "-r", revNumberToAlter, testFileReplica.canonicalPath,
               //"--username", username, "--password", password,
                "--non-interactive", "--no-auth-cache"]
        def output = commandLineService.execute(command.toArray(new String[0]))
        assertTrue("Test file should have the ORIGINAL commit message", output[1].contains(originalCommitMsg))

        // update revprop and file
        command = [ConfigUtil.svnPath(), "propset", "-r", revNumberToAlter, "--revprop", "svn:log", updatedCommitMsg, 
                testFileMaster.canonicalPath,
                "--username", username, "--password", password,
                "--non-interactive"]
        commandLineService.execute(command.toArray(new String[0]))

        testFileMaster.text = "updating test file"
        command = [ConfigUtil.svnPath(), "ci", testFileMaster.canonicalPath,
            "--username", username, "--password", password,
            "--non-interactive", "-m", originalCommitMsg]
        result = commandLineService.execute(command.toArray(new String[0]))
        matcher = result =~ /Committed revision (\d+)/
        def nextRevNumber = matcher[0][1]

        assertNotSame("The file revision number should be updated", revNumberToAlter, nextRevNumber)

        // execute revprop sync command
        cmdParams = [:]
        cmdParams["repoName"] = REPO_NAME
        cmdParams["masterId"] = EXSY_ID
        cmdParams["revision"] = revNumberToAlter
        commandMap = [code: 'copyRevprops', id: "cmdexec7001", params: cmdParams,
            context: executionContext]
        command = AbstractCommand.makeCommand(classLoader, commandMap)
        replicaCommandExecutorService.commandLifecycleExecutor(command)
        Thread.sleep(1000)

        // validate updated revprops
        command = [ConfigUtil.svnPath(), "up", wcReplica.canonicalPath,
                //"--username", username, "--password", password,
                "--non-interactive", "--no-auth-cache"]
        commandLineService.execute(command.toArray(new String[0]))
        
        command = [ConfigUtil.svnPath(), "propget", "svn:log", "--revprop", "-r", revNumberToAlter, testFileReplica.canonicalPath,
               //"--username", username, "--password", password,
                "--non-interactive", "--no-auth-cache"]
        output = commandLineService.execute(command.toArray(new String[0]))
        assertTrue("Test file should now have the UPDATED commit message", output[1].contains(updatedCommitMsg))

        wcMaster.deleteDir()
        wcReplica.deleteDir()
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
