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

import grails.test.*
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.statistics.StatValue
import com.collabnet.svnedge.statistics.Statistic

class SvnRepoServiceTests extends GrailsUnitTestCase {

    SvnRepoService svc;
    File repoParentDir;
    File repoOnDisk;

    protected void setUp() {
        super.setUp()

        // mock the svn repo data
        repoParentDir = new File(System.getProperty("java.io.tmpdir"), "test-repo-data")
        repoParentDir.mkdir()
        repoParentDir.deleteOnExit()

        // create a new repo "externally"
        repoOnDisk = new File(repoParentDir.absolutePath, "existingRepo")
               repoOnDisk.mkdir()
        File repoOnDiskMarkerFile = new File(repoOnDisk, "format")
        repoOnDiskMarkerFile.createNewFile()

        // mock domain objects
        def testServer = new Server(
                repoParentDir: repoParentDir.absolutePath
        )
        def repoTest = new Repository( name: "existingRepo")
        def stat = new Statistic()
        def statValue = new StatValue(repo:repoTest, statistic: stat)
        

        mockDomain (Server, [testServer])
        mockDomain (Repository, [repoTest])
        mockDomain (StatValue, [statValue])
        mockDomain (Statistic, [stat])

        // mock the service and its dependencies
        mockLogging (SvnRepoService, true)
        mockLogging (CommandLineService, true)
        svc = new SvnRepoService()

        // mock the injected services
        LifecycleService lcs = new LifecycleService()
        def cls = new Expando()
        cls.executeWithOutput = { p1, p2, p3 -> ". nobody nobody nobody" }

        def repoSvc = new Expando()
        repoSvc.getHttpdUser = { "nobody" }
        repoSvc.getHttpdGroup = { "nobody" }

        def osSvc = new Expando()
        osSvc.isWindows = { false }

        svc.lifecycleService = lcs
        svc.commandLineService = cls
        svc.serverConfService = repoSvc
        svc.operatingSystemService = osSvc


    }

    protected void tearDown() {
        super.tearDown()

        repoOnDisk.delete()
        repoParentDir.delete()
    }


    void testSyncRepositoriesCreate() {

        assertEquals ("One repository expected at startup", 1, Repository.count())

        // create a new repo "externally" / out of band
        def repoOnDisk1 = new File(repoParentDir.absolutePath, "existingRepoFile")
        repoOnDisk1.mkdir()
        File repoOnDiskMarkerFile = new File(repoOnDisk1, "format")
        repoOnDiskMarkerFile.createNewFile()

        // run the sync method
        svc.syncRepositories()
        assertEquals ("Two repositories expected after sync", 2, Repository.count())

        repoOnDiskMarkerFile.delete()
        repoOnDisk1.delete()

    }

    void testSyncRepositoriesDelete() {

        assertEquals ("One repository expected at startup", 1, Repository.count())

        // delete a repo "externally" / out of band
        repoOnDisk.deleteDir()

        // run the sync method
        svc.syncRepositories()
        assertEquals ("Zero repositories expected after sync", 0, Repository.count())

    }



}
