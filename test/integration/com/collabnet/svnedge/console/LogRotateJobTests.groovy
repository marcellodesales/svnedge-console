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
package com.collabnet.svnedge.console

import grails.test.*

class LogRotateJobTests extends GrailsUnitTestCase {
    def LogRotateJob
    def lifecycleService

    def grailsApplication

    protected void setUp() {
        super.setUp()
        LogRotateJob = grailsApplication.mainContext
            .getBean("com.collabnet.svnedge.jobs.LogRotateJob")
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testPruneLog() {
        def dataDir = lifecycleService.dataDirPath
        long pruneOlderThanToday = 2
        def file1 = new File(dataDir, "logs/" + "subversion.log.1.days.older")
        def file2 = new File(dataDir, "logs/" + "subversion.log.2.days.older")
        def file3 = new File(dataDir, "logs/" + "subversion.log.3.days.older")

        Date today = new Date()
        //Safely date back by x days and and hour.
        long oneDayBack = (new Date(today.getTime() - 1440*60000 - 60*60000)).getTime()
        long twoDaysBack = (new Date(today.getTime() - 2*1440*60000 - 60*60000)).getTime()
        long threeDaysBack = (new Date(today.getTime() - 3*1440*60000 - 60*60000)).getTime()

        file1.createNewFile()
        file2.createNewFile()
        file3.createNewFile()

        file1.setLastModified(oneDayBack)
        file2.setLastModified(twoDaysBack)
        file3.setLastModified(threeDaysBack)

        LogRotateJob.pruneLog(pruneOlderThanToday)

        def newfile1 = new File(dataDir, "logs/" + "subversion.log.1.days.older")
        def newfile2 = new File(dataDir, "logs/" + "subversion.log.2.days.older")
        def newfile3 = new File(dataDir, "logs/" + "subversion.log.3.days.older")

        assertTrue "1 day old file should exist", newfile1.exists()
        assertFalse "More than 2 day old file should not exist", newfile2.exists()
        assertFalse "More than 2 day old file should not exist", newfile3.exists()
    }
}
