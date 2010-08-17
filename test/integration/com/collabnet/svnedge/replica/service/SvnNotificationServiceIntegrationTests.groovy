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

import grails.test.*

import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.replica.manager.RepoStatus

class SvnNotificationServiceIntegrationTests extends GrailsUnitTestCase {
    def svnNotificationService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCreateRepoOnDatabase() {
        def repoName = "testRepo"
        svnNotificationService.createRepositoryOnDatabase(repoName)
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        assertNotNull("The repo should exist.", repo)
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

    void testRemoveRepoOnDatabase() {
        def repoName = "testRemoveRepo"
        // create first
        svnNotificationService.addRepositoryOnDatabase(repoName)
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        assertNotNull("The repo should exist.", repo)
        svnNotificationService.removeRepositoryOnDatabase(repoName)
        repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        assertNotNull("The repo should exist.", repo)
        assertEquals("The repo's status should be REMOVED.", 
                     RepoStatus.REMOVED,
                     repo.getStatus())
                     
    }

    void testAddRemoveReAddRepoOnDatabase() {
        def repoName = "testAddRemoveAddRepo"
        // create first
        svnNotificationService.addRepositoryOnDatabase(repoName)
        def repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        assertNotNull("The repo should exist.", repo)
        // removed
        svnNotificationService.removeRepositoryOnDatabase(repoName)
        repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        assertNotNull("The repo should exist.", repo)
        // add again
        try {
            svnNotificationService.addRepositoryOnDatabase(repoName)
        } catch (Exception e) {
            fail("Should be able to re-add a removed repository.")
        }
        repo = ReplicatedRepository.findByRepo(Repository.findByName(repoName))
        assertTrue("The repository should be enabled.", repo.getEnabled())
    }

    void testRemoveRepoOnFileSystem() {
        // create the repo first
        def replicaDir = File.createTempFile("repo-test", null)
        log.info("replicaDir = " + replicaDir.getCanonicalPath())
        replicaDir.delete()
        replicaDir.mkdir()
        replicaDir.deleteOnExit()
        svnNotificationService.svnReplicaParentPath = replicaDir
            .getCanonicalPath()
        def repoName = "testproject"
        svnNotificationService.addRepositoryOnDatabase(repoName)
        svnNotificationService.createRepositoryOnFileSystem(repoName)
        // remove on db
        svnNotificationService.removeRepositoryOnDatabase(repoName)
        // remove on FS
        svnNotificationService.removeRepositoryOnFileSystem(repoName)
        def repoDir = new File(replicaDir.getCanonicalPath() + "/" + repoName)
        assertFalse("The repo directory should not exist.", repoDir.exists())
    }
}
