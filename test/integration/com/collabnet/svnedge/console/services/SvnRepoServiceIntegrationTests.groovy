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
package com.collabnet.svnedge.console.services

import grails.test.GrailsUnitTestCase
import com.collabnet.svnedge.console.CommandLineService
import com.collabnet.svnedge.console.LifecycleService
import com.collabnet.svnedge.console.OperatingSystemService
import com.collabnet.svnedge.console.SvnRepoService
import com.collabnet.svnedge.console.DumpBean
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.util.ConfigUtil;

class SvnRepoServiceIntegrationTests extends GrailsUnitTestCase {

    SvnRepoService svnRepoService
    CommandLineService commandLineService
    OperatingSystemService operatingSystemService
    LifecycleService lifecycleService
    def jobsAdminService
    def quartzScheduler
    def repoParentDir
    boolean initialStarted

    protected void setUp() {
        super.setUp()
        initialStarted = lifecycleService.isStarted()
        // start tests with the server off
        if (initialStarted) {
            lifecycleService.stopServer()
        }

        // Setup a test repository parent
        repoParentDir = createTestDir("repo")
        Server server = lifecycleService.getServer()
        server.repoParentDir = repoParentDir.getCanonicalPath()
        server.save()


    }

    protected void tearDown() {
        super.tearDown()
        repoParentDir.deleteDir()
    }


    void testUpdateRepositoryPermissions() {

        // first create a repository
        def testRepository = new Repository(name: "testrepo")

        int exitStatus = svnRepoService.createRepository(testRepository, false)
        // assertEquals("Create Repo should succeed", 0, exitStatus)

        svnRepoService.updateRepositoryPermissions(testRepository)

    }

    void testCreateRepository() {
        def testRepoName = "lifecycle-test"
        Repository repo = new Repository(name: testRepoName)
        assertEquals "Failed to create repository.", 0,
                svnRepoService.createRepository(repo, true)

        // checkout the repo
        def wcDir = createTestDir("wc")
        def testRepoFile = new File(wcDir, testRepoName)
        def status = commandLineService.executeWithStatus(
                ConfigUtil.svnPath(), "checkout",
                "--no-auth-cache", "--non-interactive", commandLineService.createSvnFileURI(new File(repoParentDir, testRepoName)),
                testRepoFile.canonicalPath)
        assertEquals "Failed to checkout repository.", 0, status
        def topDirs = testRepoFile.listFiles()
        def expectedDirs = ['.svn', 'branches', 'tags', 'trunk']
        assertEquals "Wrong number of files." + topDirs, 4, topDirs.length
        for (d in expectedDirs) {
            boolean b = false
            for (td in topDirs) {
                if (d == td.name) {
                    b = true
                    break
                }
            }
            assertTrue "Expected '" + d + "' directory not found", b
        }
        wcDir.deleteDir()
    }

    public void testDump() {
        // Give this test 30s max to finish
        long timeLimit = System.currentTimeMillis() + 30000
        def testRepoName = "dump-test"
        Repository repo = new Repository(name: testRepoName)
        assertEquals "Failed to create repository.", 0,
                svnRepoService.createRepository(repo, true)

        // Earlier checkin was not deleting this file, so make sure it is
        // gone before testing
        File tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        File progressLogFile =
        new File(tempLogDir, "dump-progress-" + repo.name + ".log")
        if (progressLogFile.exists()) {
            progressLogFile.delete()
        }

        DumpBean params = new DumpBean()
        params.compress = false
        def filename = svnRepoService.createDump(params, repo)
        File dumpFile = newDumpFile(filename, repo)
        // Async so wait for it
        while (!dumpFile.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile.name, dumpFile.exists()
        String contents = dumpFile.text
        assertTrue "Missing trunk in dump", contents.contains("Node-path: trunk")
        assertTrue "Missing branches in dump", contents.contains("Node-path: branches")
        assertTrue "Missing tags in dump", contents.contains("Node-path: tags")
        Thread.sleep(250)

        // test exclusion filter
        params = new DumpBean()
        params.compress = false
        params.filter = true
        params.excludePath = "branches"
        filename = svnRepoService.createDump(params, repo)
        File dumpFile2 = newDumpFile(filename, repo)
        // Async so wait for it
        while (!dumpFile2.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile2.name, dumpFile2.exists()
        contents = dumpFile2.text
        assertTrue "Missing trunk in dump", contents.contains("Node-path: trunk")
        assertFalse "branches exists in dump", contents.contains("Node-path: branches")
        assertTrue "Missing tags in dump", contents.contains("Node-path: tags")
        Thread.sleep(1000)

        // test inclusion filter
        params = new DumpBean()
        params.compress = false
        params.filter = true
        params.includePath = "trunk tags"
        filename = svnRepoService.createDump(params, repo)
        File dumpFile3 = newDumpFile(filename, repo)
        // Async so wait for it
        while (!dumpFile3.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile3.name, dumpFile3.exists()
        contents = dumpFile3.text
        assertTrue "Missing trunk in dump", contents.contains("Node-path: trunk")
        assertFalse "branches exists in dump", contents.contains("Node-path: branches")
        assertTrue "Missing tags in dump", contents.contains("Node-path: tags")

        dumpFile.delete()
        dumpFile2.delete()
        dumpFile3.delete()
        Thread.sleep(200)
    }

    private File newDumpFile(filename, repo) {
        return new File(new File(Server.getServer().dumpDir, repo.name), filename)
    }

    public void testHotcopy() {
        def testRepoName = "dump-test"
        Repository repo = new Repository(name: testRepoName)
        assertEquals "Failed to create repository.", 0,
                svnRepoService.createRepository(repo, true)

        if (!operatingSystemService.isWindows()) {
            File hookScript = new File(svnRepoService.getRepositoryHomePath(repo),
                    "hooks/pre-commit.tmpl")
            hookScript.setExecutable(true)
        }

        // Earlier checkin was not deleting this file, so make sure it is
        // gone before testing
        File tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        File progressLogFile =
        new File(tempLogDir, "dump-progress-" + repo.name + ".log")
        if (progressLogFile.exists()) {
            progressLogFile.delete()
        }

        DumpBean params = new DumpBean()
        def filename = svnRepoService.createHotcopy(params, repo)
        File dumpFile = newDumpFile(filename, repo)
        assertTrue "Dump file does not exist: " + dumpFile.name, dumpFile.exists()

        if (!operatingSystemService.isWindows()) {
            // not sure how to test a native zip extraction on windows

            File extractDir = createTestDir("hotcopy")
            def exitStatus = commandLineService.executeWithStatus(
                    "unzip", dumpFile.canonicalPath, "-d", extractDir.canonicalPath)
            assertEquals "Unable to extract archive", 0, exitStatus

            File copiedHookScript = new File(extractDir, "hooks/pre-commit.tmpl")
            assertTrue "pre-commit.tmpl does not exist", copiedHookScript.exists()
            if (!operatingSystemService.isWindows()) {
                assertTrue "pre-commit.tmpl should be executable",
                        copiedHookScript.canExecute()
            }
            def contents = ['conf', 'db', 'format', 'hooks', 'locks', 'README.txt']
            int i = 0
            extractDir.eachFile { f ->
                assertTrue "Found unexpected file/dir: " + f.name,
                        contents.contains(f.name)
                i++
            }
            assertEquals "Number of files/directories did not match",
                    contents.size(), i

            extractDir.deleteDir()
        }

        dumpFile.delete()
    }

    public void testLoad() {

        // upper limit on time to run async code
        long timeLimit = System.currentTimeMillis() + 30000
        // make sure the quartz scheduler is running, is put in standby by other tests
        quartzScheduler.start()
        // but pause jobs likely to start running to ensure a thread is available
        jobsAdminService.pauseGroup("Statistics")

        // create a dump file of a src repo with branches/tags/trunk nodes
        def testRepoNameSrc = "load-test-src"
        Repository repoSrc = new Repository(name: testRepoNameSrc)
        assertEquals "Failed to create src repository.", 0,
                svnRepoService.createRepository(repoSrc, true)
        repoSrc.save()

        DumpBean params = new DumpBean()
        params.compress = false
        def filename = svnRepoService.createDump(params, repoSrc)
        File dumpFile = newDumpFile(filename, repoSrc)
        // Async so wait for it
        while (!dumpFile.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile.name, dumpFile.exists()
        log.info "The dumpfile original location: '${dumpFile.absolutePath}'"
        String contents = dumpFile.text
        assertTrue "Missing trunk in dump", contents.contains("Node-path: trunk")
        assertTrue "Missing branches in dump", contents.contains("Node-path: branches")
        assertTrue "Missing tags in dump", contents.contains("Node-path: tags")

        // create a target repo WITHOUT branches/tags/trunk nodes
        def testRepoNameTarget = "load-test-target"

        Repository repoTarget = new Repository(name: testRepoNameTarget)
        assertEquals "Failed to create target repository.", 0,
                svnRepoService.createRepository(repoTarget, false)
        repoTarget.save(flush: true)

        def output = commandLineService.executeWithOutput(
                ConfigUtil.svnPath(), "info",
                "--no-auth-cache", "--non-interactive",
                commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

        // verify no 'trunk' folder
        assertFalse "svn info output should not contain node info for 'trunk'", output.contains("Node Kind: directory")

        // move src dump file to the expected load location for target
        File loadDir = svnRepoService.getLoadDirectory(repoTarget)
        // delete any residual load files
        loadDir.eachFile {
            it.delete()
        }
        File loadFile = new File(loadDir, dumpFile.name)
        boolean loadFileCreated = dumpFile.renameTo(loadFile)
        assertTrue "should be able to move dumpfile to load directory", loadFileCreated
        assertTrue "loadFile should exist", loadFile.exists()
        log.info "The loadfile to be imported to the target repo: '${loadFile.absolutePath}'"

        // load it
        def tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        def progressFile = File.createTempFile("load-progress", ".txt", tempLogDir)
        def options = ["progressLogFile": progressFile.absolutePath]
        options << ["ignoreUuid": false]
        svnRepoService.scheduleLoad(repoTarget, options)

        // verify that repo load has run (trunk/tags/branches which should have been imported)
        boolean loadSuccess = false
        timeLimit = System.currentTimeMillis() + 60000
        while (!loadSuccess && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(5000)
            output = commandLineService.executeWithOutput(
                    ConfigUtil.svnPath(), "info",
                    "--no-auth-cache", "--non-interactive",
                    commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

            loadSuccess = output.contains("Node Kind: directory")
        }

        assertFalse "load file should be deleted after loading", loadFile.exists()
        assertTrue "the target repo should now have nodes from the src repo after loading", loadSuccess
    }

    public void testLoadZip() {

        // upper limit on time to run async code
        long timeLimit = System.currentTimeMillis() + 30000
        // make sure the quartz scheduler is running, is put in standby by other tests
        quartzScheduler.start()
        // but pause jobs likely to start running to ensure a thread is available
        jobsAdminService.pauseGroup("Statistics")

        // create a dump file of a src repo with branches/tags/trunk nodes
        def testRepoNameSrc = "load-test-src"
        Repository repoSrc = new Repository(name: testRepoNameSrc)
        assertEquals "Failed to create src repository.", 0,
                svnRepoService.createRepository(repoSrc, true)
        repoSrc.save()

        DumpBean params = new DumpBean()
        params.compress = true
        def filename = svnRepoService.createDump(params, repoSrc)
        File dumpFile = newDumpFile(filename, repoSrc)
        // Async so wait for it
        while (!dumpFile.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile.name, dumpFile.exists()
        assertTrue "The Dump file should be a zip file", dumpFile.name.endsWith(".zip")
        log.info "The dumpfile original location: '${dumpFile.absolutePath}'"

        // create a target repo WITHOUT branches/tags/trunk nodes
        def testRepoNameTarget = "load-test-target"

        Repository repoTarget = new Repository(name: testRepoNameTarget)
        assertEquals "Failed to create target repository.", 0,
                svnRepoService.createRepository(repoTarget, false)
        repoTarget.save(flush: true)

        def output = commandLineService.executeWithOutput(
                ConfigUtil.svnPath(), "info",
                "--no-auth-cache", "--non-interactive",
                commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

        // verify no 'trunk' folder
        assertFalse "svn info output should not contain node info for 'trunk'", output.contains("Node Kind: directory")

        // move src dump file to the expected load location for target
        File loadDir = svnRepoService.getLoadDirectory(repoTarget)
        // delete any residual load files
        loadDir.eachFile {
            it.delete()
        }
        File loadFile = new File(loadDir, dumpFile.name)
        boolean loadFileCreated = dumpFile.renameTo(loadFile)
        assertTrue "should be able to move dumpfile to load directory", loadFileCreated
        assertTrue "loadFile should exist", loadFile.exists()
        log.info "The loadfile to be imported to the target repo: '${loadFile.absolutePath}'"

        // load it
        def tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        def progressFile = File.createTempFile("load-progress", ".txt", tempLogDir)
        def options = ["progressLogFile": progressFile.absolutePath]
        options << ["ignoreUuid": false]
        svnRepoService.scheduleLoad(repoTarget, options)

        // verify that repo load has run (trunk/tags/branches which should have been imported)
        boolean loadSuccess = false
        timeLimit = System.currentTimeMillis() + 60000
        while (!loadSuccess && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(5000)
            output = commandLineService.executeWithOutput(
                    ConfigUtil.svnPath(), "info",
                    "--no-auth-cache", "--non-interactive",
                    commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

            loadSuccess = output.contains("Node Kind: directory")
        }

        assertFalse "load file should be deleted after loading", loadFile.exists()
        assertTrue "the target repo should now have nodes from the src repo after loading", loadSuccess
    }

    public void testLoadHotcopyAsBackup() {

        // upper limit on time to run async code
        long timeLimit = System.currentTimeMillis() + 30000
        // make sure the quartz scheduler is running, is put in standby by other tests
        quartzScheduler.start()
        // but pause jobs likely to start running to ensure a thread is available
        jobsAdminService.pauseGroup("Statistics")

        // create a dump file of a src repo with branches/tags/trunk nodes
        def testRepoNameSrc = "load-test-src"
        Repository repoSrc = new Repository(name: testRepoNameSrc)
        assertEquals "Failed to create src repository.", 0,
                svnRepoService.createRepository(repoSrc, true)
        repoSrc.save()
        String sourceUuid = svnRepoService.getReposUUID(repoSrc)

        DumpBean params = new DumpBean()
        params.compress = true
        def filename = svnRepoService.createHotcopy(params, repoSrc)
        File dumpFile = newDumpFile(filename, repoSrc)
        // Async so wait for it
        while (!dumpFile.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile.name, dumpFile.exists()
        assertTrue "The Dump file should be a zip file", dumpFile.name.endsWith(".zip")
        log.info "The dumpfile original location: '${dumpFile.absolutePath}'"

        // create a target repo WITHOUT branches/tags/trunk nodes
        def testRepoNameTarget = "load-test-target"

        Repository repoTarget = new Repository(name: testRepoNameTarget)
        assertEquals "Failed to create target repository.", 0,
                svnRepoService.createRepository(repoTarget, false)
        repoTarget.save(flush: true)
        String targetUuid = svnRepoService.getReposUUID(repoTarget)

        def output = commandLineService.executeWithOutput(
                ConfigUtil.svnPath(), "info",
                "--no-auth-cache", "--non-interactive",
                commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

        // verify no 'trunk' folder
        assertFalse "svn info output should not contain node info for 'trunk'", output.contains("Node Kind: directory")

        // move src dump file to the expected load location for target
        File loadDir = svnRepoService.getLoadDirectory(repoTarget)
        // delete any residual load files
        loadDir.eachFile {
            it.delete()
        }
        File loadFile = new File(loadDir, dumpFile.name)
        boolean loadFileCreated = dumpFile.renameTo(loadFile)
        assertTrue "should be able to move dumpfile to load directory", loadFileCreated
        assertTrue "loadFile should exist", loadFile.exists()
        log.info "The loadfile to be imported to the target repo: '${loadFile.absolutePath}'"

        // load it
        def tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        def progressFile = File.createTempFile("load-progress", ".txt", tempLogDir)
        def options = ["progressLogFile": progressFile.absolutePath]
        options << ["ignoreUuid": false]   // our target repo should get it's UUID from the hotcopy
        svnRepoService.scheduleLoad(repoTarget, options)

        // verify that repo load has run (trunk/tags/branches which should have been imported)
        boolean loadSuccess = false
        timeLimit = System.currentTimeMillis() + 60000
        while (!loadSuccess && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(5000)
            output = commandLineService.executeWithOutput(
                    ConfigUtil.svnPath(), "info",
                    "--no-auth-cache", "--non-interactive",
                    commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

            loadSuccess = output.contains("Node Kind: directory")
        }

        assertFalse "load file should be deleted after loading", loadFile.exists()
        assertTrue "the target repo should now have nodes from the src repo after loading", loadSuccess
        assertEquals "the target repo should now have the original source UUID", sourceUuid, svnRepoService.getReposUUID(repoTarget)
    }

    public void testLoadHotcopyAsTemplate() {

        // upper limit on time to run async code
        long timeLimit = System.currentTimeMillis() + 30000
        // make sure the quartz scheduler is running, is put in standby by other tests
        quartzScheduler.start()
        // but pause jobs likely to start running to ensure a thread is available
        jobsAdminService.pauseGroup("Statistics")

        // create a dump file of a src repo with branches/tags/trunk nodes
        def testRepoNameSrc = "load-test-src"
        Repository repoSrc = new Repository(name: testRepoNameSrc)
        assertEquals "Failed to create src repository.", 0,
                svnRepoService.createRepository(repoSrc, true)
        repoSrc.save()
        String sourceUuid = svnRepoService.getReposUUID(repoSrc)

        DumpBean params = new DumpBean()
        params.compress = true
        def filename = svnRepoService.createHotcopy(params, repoSrc)
        File dumpFile = newDumpFile(filename, repoSrc)
        // Async so wait for it
        while (!dumpFile.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        assertTrue "Dump file does not exist: " + dumpFile.name, dumpFile.exists()
        assertTrue "The Dump file should be a zip file", dumpFile.name.endsWith(".zip")
        log.info "The dumpfile original location: '${dumpFile.absolutePath}'"

        // create a target repo WITHOUT branches/tags/trunk nodes
        def testRepoNameTarget = "load-test-target"

        Repository repoTarget = new Repository(name: testRepoNameTarget)
        assertEquals "Failed to create target repository.", 0,
                svnRepoService.createRepository(repoTarget, false)
        repoTarget.save(flush: true)
        String targetUuid = svnRepoService.getReposUUID(repoTarget)

        def output = commandLineService.executeWithOutput(
                ConfigUtil.svnPath(), "info",
                "--no-auth-cache", "--non-interactive",
                commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

        // verify no 'trunk' folder
        assertFalse "svn info output should not contain node info for 'trunk'", output.contains("Node Kind: directory")

        // move src dump file to the expected load location for target
        File loadDir = svnRepoService.getLoadDirectory(repoTarget)
        // delete any residual load files
        loadDir.eachFile {
            it.delete()
        }
        File loadFile = new File(loadDir, dumpFile.name)
        boolean loadFileCreated = dumpFile.renameTo(loadFile)
        assertTrue "should be able to move dumpfile to load directory", loadFileCreated
        assertTrue "loadFile should exist", loadFile.exists()
        log.info "The loadfile to be imported to the target repo: '${loadFile.absolutePath}'"

        // load it
        def tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        def progressFile = File.createTempFile("load-progress", ".txt", tempLogDir)
        def options = ["progressLogFile": progressFile.absolutePath]
        options << ["ignoreUuid": true]   // our target repo should keep its original uuid
        svnRepoService.scheduleLoad(repoTarget, options)

        // verify that repo load has run (trunk/tags/branches which should have been imported)
        boolean loadSuccess = false
        timeLimit = System.currentTimeMillis() + 60000
        while (!loadSuccess && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(5000)
            output = commandLineService.executeWithOutput(
                    ConfigUtil.svnPath(), "info",
                    "--no-auth-cache", "--non-interactive",
                    commandLineService.createSvnFileURI(new File(repoParentDir, testRepoNameTarget)) + "trunk")

            loadSuccess = output.contains("Node Kind: directory")
        }

        assertFalse "load file should be deleted after loading", loadFile.exists()
        assertTrue "the target repo should now have nodes from the src repo after loading", loadSuccess
        assertEquals "the target repo should still have its own UUID", targetUuid, svnRepoService.getReposUUID(repoTarget)
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
