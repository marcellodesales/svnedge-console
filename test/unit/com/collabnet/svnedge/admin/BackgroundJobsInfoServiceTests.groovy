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
package com.collabnet.svnedge.admin

import grails.test.GrailsUnitTestCase
import com.collabnet.svnedge.event.BackgroundJobTerminatedEvent
import com.collabnet.svnedge.event.BackgroundJobStartedEvent

/**
 * Tests for the BackgroundJobsInfoService
 */
class BackgroundJobsInfoServiceTests extends GrailsUnitTestCase {

    def backgroundJobsInfoService

    public void setUp() {
        this.backgroundJobsInfoService = new BackgroundJobsInfoService()
    }

    public void testJobsInfo() {

        Map eventProperties = [jobType: "repoDump", repo: "testRepo", dumpFile: new File("myFile.txt")]
        def e1 = new BackgroundJobStartedEvent(this, "1234", eventProperties)
        def e2 = new BackgroundJobStartedEvent(this, "5678", eventProperties)
        backgroundJobsInfoService.onApplicationEvent(e1)
        backgroundJobsInfoService.onApplicationEvent(e2)

        assertEquals("there should be 2 running jobs according to the service", 2,
                backgroundJobsInfoService.runningJobs.size())

        def e3 = new BackgroundJobTerminatedEvent(this, "1234", eventProperties)
        backgroundJobsInfoService.onApplicationEvent(e3)


        assertEquals("there should be 1 running job according to the service", 1,
                backgroundJobsInfoService.runningJobs.size())

        assertEquals("there should be 1 finished job according to the service", 1,
                backgroundJobsInfoService.runningJobs.size())

    }
    
    public void testFinishedJobsPruning() {

        Map eventProperties = [jobType: "repoDump", repo: "testRepo", dumpFile: new File("myFile.txt")]
        def startEvents = [new BackgroundJobStartedEvent(this, "1", eventProperties), 
            new BackgroundJobStartedEvent(this, "2", eventProperties),
            new BackgroundJobStartedEvent(this, "3", eventProperties),
            new BackgroundJobStartedEvent(this, "4", eventProperties),
            new BackgroundJobStartedEvent(this, "5", eventProperties),
            new BackgroundJobStartedEvent(this, "6", eventProperties)]
        
        startEvents.each {
            backgroundJobsInfoService.onApplicationEvent(it)
        }

        assertEquals("there should be 6 running jobs according to the service", 6,
                backgroundJobsInfoService.runningJobs.size())

        def endEvents = [new BackgroundJobTerminatedEvent(this, "1", eventProperties), 
            new BackgroundJobTerminatedEvent(this, "2", eventProperties),
            new BackgroundJobTerminatedEvent(this, "3", eventProperties),
            new BackgroundJobTerminatedEvent(this, "4", eventProperties),
            new BackgroundJobTerminatedEvent(this, "5", eventProperties),
            new BackgroundJobTerminatedEvent(this, "6", eventProperties)]
        
        endEvents.each {
            backgroundJobsInfoService.onApplicationEvent(it)
        }


        assertEquals("there should be 0 running jobs according to the service", 0,
                backgroundJobsInfoService.runningJobs.size())

        assertEquals("there should be 5 finished job according to the service", 5,
                backgroundJobsInfoService.finishedJobs.size())

    }
    
}
