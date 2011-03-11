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
