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
import com.collabnet.svnedge.console.SvnRepoService
import com.collabnet.svnedge.console.DumpBean 
import com.collabnet.svnedge.domain.Repository 
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.util.ConfigUtil;

class SvnRepoServiceIntegrationTests extends GrailsUnitTestCase {

    SvnRepoService svnRepoService
    CommandLineService commandLineService
    LifecycleService lifecycleService
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
            "--no-auth-cache", "--non-interactive", commandLineService
            .createSvnFileURI(new File(repoParentDir, testRepoName)),
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
