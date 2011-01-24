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
        repoOnDiskMarkerFile = new File(repoOnDisk, "db")
        repoOnDiskMarkerFile.mkdir()

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
        repoParentDir.deleteDir()
    }

    private File createMockRepo(String repoName, boolean doSync) {

        //TODO: files/directories listed here is bare minimum needed for 
        //      present set of tests to work. In future if need more
        //      file/dir add it at will.
        def entities = [ [dir:'conf'], [dir:'db'], [dir:'hooks'], [dir:'locks'],
                         [file:'format'], [file:'db/current'],
                         [file:'db/fsfs.conf'], [file:'db/min-unpacked-rev'],
                         [file:'format'], [file:'db/uuid'], [file:'db/fs-type'],
                         [file:'db/txn-current'], [file:'db/txn-current-lock']
                       ]
        File repoDir
        File repoFile

        def newRepo = new File(repoParentDir.absolutePath, repoName)
        newRepo.mkdir()

        entities.each {
           if (it['dir'] != null) {
              repoDir = new File(newRepo, it['dir'])
              repoDir.mkdir()
           } else if (it['file'] != null) {
              repoFile = new File(newRepo, it['file'])
              repoFile.createNewFile()
           }
        }

        // run the sync method
        if (doSync) {
          svc.syncRepositories()
        }

        return newRepo
    }

    private void removeMockRepo(String repoName, boolean doSync) {

        def newRepo = new File(repoParentDir.absolutePath, repoName)
        assert newRepo.deleteDir() == true

        if (doSync) {
          svc.syncRepositories()
        }
    }

    void testGetReposUUID() {
        def str="55288c01-efc9-40d0-b6bb-08ab79949e00"

        assertEquals("One repository expected at startup", 1, Repository.count())

        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)

        // run the sync method
        assertEquals("Two repositories expected after sync", 2, Repository.count())

        File repoUUIDFile = new File(newRepo, "db/uuid")
        repoUUIDFile.write(str)


        // getting uuid and comparing
        def testRepo = new Repository(name: "newRepo")
        def uuid = svc.getReposUUID(testRepo)
        assertEquals ("Repository UUID expected", uuid, str)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testGetReposFsType() {
        def str="FSFS"

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals ("Two repositories expected after sync", 2, Repository.count())

        File repoFsTypeFile = new File(newRepo, "db/fs-type")
        repoFsTypeFile.write(str)

        // getting fstpe and comparing
        def testRepo = new Repository(name: "newRepo")
        def fsType = svc.getReposFsType(testRepo)
        assertEquals("Repository FSFS type expected", fsType, str)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testGetReposFsFormat() {

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals("Two repositories expected after sync", 2, Repository.count())

        def testRepo = new Repository(name: "newRepo")
        File repoDBFormatFile = new File(newRepo, "db/format")

        // testing against empty db/format file
        def format = svc.getReposFsFormat(testRepo)
        assertEquals ("Repository FS format when empty", 1, format)

        // testing a specific db/format schema
        repoDBFormatFile.write("4")
        format = svc.getReposFsFormat(testRepo)
        assertEquals ("Repository FS format", 4, format)

        // testing against missing db/format file
        repoDBFormatFile.delete()
        format = svc.getReposFsFormat(testRepo)
        assertEquals ("Repository FS format when missing", 1, format)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testGetRepoFeatures() {

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals("Two repositories expected after sync", 2, Repository.count())

        def testRepo = new Repository(name: "newRepo")
        File repoDBFormatFile = new File(newRepo, "db/format")

        // testing against empty db/format file
        def format = svc.getReposFsFormat(testRepo)
        def features = svc.getRepoFeatures(testRepo, format)
        assertEquals ("Repository features expected", "svndiff0", features)

        // testing a specific db/format schema
        repoDBFormatFile.write("5")
        format = svc.getReposFsFormat(testRepo)
        features = svc.getRepoFeatures(testRepo, format)
        assertEquals ("Repository features expected",
            "svndiff1, sharding, mergeinfo, rep-sharing, packed revs, packed revprops",
            features)

        // testing against missing db/format file
        repoDBFormatFile.delete()
        format = svc.getReposFsFormat(testRepo)
        features = svc.getRepoFeatures(testRepo, format)
        assertEquals ("Repository FS format when missing", "svndiff0", features)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testGetReposFormat() {

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals ("Two repositories expected after sync", 2, Repository.count())

        def testRepo = new Repository( name: "newRepo")
        File repoFormatFile = new File(newRepo, "format")

        // testing against empty format file
        def format = svc.getReposFormat(testRepo)
        assertEquals ("Repository format when empty", 0, format)

        // testing a specific format schema
        repoFormatFile.write("5")
        format = svc.getReposFormat(testRepo)
        assertEquals ("Repository format", 5, format)

        // testing against missing format file
        repoFormatFile.delete()
        format = svc.getReposFormat(testRepo)
        assertEquals ("Repository format when missing", 0, format)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testFindHeadRev() {

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals ("Two repositories expected after sync", 2, Repository.count())

        def testRepo = new Repository( name: "newRepo")
        File repoCurrentFile = new File(newRepo, "db/current")

        // testing against empty db/current file
        def rev = svc.findHeadRev(testRepo)
        assertEquals ("Repository head revision", 0, rev)

        // testing a specific revision
        repoCurrentFile.write("1000")
        rev = svc.findHeadRev(testRepo)
        assertEquals ("Repository head revision", 1000, rev)

        // testing against missing db/current file
        repoCurrentFile.delete()
        rev = svc.findHeadRev(testRepo)
        assertEquals ("Repository head revision", 0, rev)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testGetReposRepSharing() {

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals("Two repositories expected after sync", 2, Repository.count())

        def testRepo = new Repository(name: "newRepo")
        File repoDBConfFile = new File(newRepo, "db/fsfs.conf")

        // testing against empty db/fsfs.conf file
        def isEnabled = svc.getReposRepSharing(testRepo)
        assertEquals ("Repository rep-sharing", true, isEnabled)

        // testing a commented rep-sharing line
        repoDBConfFile.write("###enable-rep-sharing   = ")
        isEnabled = svc.getReposRepSharing(testRepo)
        assertEquals ("Repository rep-sharing", true, isEnabled)

        // testing by setting true to enable-rep-sharing
        repoDBConfFile.write("enable-rep-sharing  = True ")
        isEnabled = svc.getReposRepSharing(testRepo)
        assertEquals ("Repository rep-sharing", true, isEnabled)

        // testing by setting false to enable-rep-sharing
        repoDBConfFile.write("enable-rep-sharing  = False")
        isEnabled = svc.getReposRepSharing(testRepo)
        assertEquals ("Repository rep-sharing", false, isEnabled)

        // testing against missing db/fsfs.conf file
        repoDBConfFile.delete()
        isEnabled = svc.getReposRepSharing(testRepo)
        assertEquals ("Repository rep-sharing", false, isEnabled)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testGetReposSharding() {

        assertEquals ("One repository expected at startup", 1, Repository.count())
        // create a new repo "externally" / out of band
        def newRepo = createMockRepo("newRepo", true)
        assertEquals("Two repositories expected after sync", 2, Repository.count())

        def testRepo = new Repository(name: "newRepo")
        File repoDBFormatFile = new File(newRepo, "db/format")

        // testing against empty db/format file
        def shard = svc.getReposSharding(testRepo)
        assertEquals ("Repository sharding", -1, shard)

        // testing by setting shard to specific value
        repoDBFormatFile.write("layout sharded 1000")
        shard = svc.getReposSharding(testRepo)
        assertEquals ("Repository sharding", 1000, shard)

        // testing by setting shard to invalid value
        repoDBFormatFile.write("layout sharded foo")
        shard = svc.getReposSharding(testRepo)
        assertEquals ("Repository sharding", -1, shard)

        // testing against missing db/format file
        repoDBFormatFile.delete()
        shard = svc.getReposSharding(testRepo)
        assertEquals ("Repository sharding", -1, shard)

        // cleanup.
        removeMockRepo("newRepo", true)
    }

    void testSyncRepositoriesCreate() {

        assertEquals ("One repository expected at startup", 1, Repository.count())

        // create a new repo "externally" / out of band
        def repoOnDisk1 = createMockRepo("existingRepoFile", false)
        
        // run the sync method
        svc.syncRepositories()
        assertEquals ("Two repositories expected after sync", 2, Repository.count())

        removeMockRepo("existingRepoFile", true)
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
