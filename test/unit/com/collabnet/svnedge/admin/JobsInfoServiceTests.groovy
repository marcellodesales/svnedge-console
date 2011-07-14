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
import org.junit.Ignore
import org.quartz.JobExecutionContext
import org.quartz.Job

/**
 * Tests for the JobsInfoService
 */
class JobsInfoServiceTests extends GrailsUnitTestCase {

    def jobsInfoService

    public void setUp() {
        this.jobsInfoService = new JobsInfoService()
    }

    @Ignore
    public void testJobsInfo() {

        def e1 = [:]
        e1.jobInstance = new Object()
        e1.jobDetail = [:]
        e1.jobDetail.name = "Somejob"
        jobsInfoService.jobToBeExecuted(e1 as JobExecutionContext)

        def e2 = [:]
        e2.jobInstance = new Object()
        e2.jobDetail = [:]
        e2.jobDetail.name = "Somejob"
        jobsInfoService.jobToBeExecuted(e2 as JobExecutionContext)

        assertEquals("there should be 2 running jobs according to the service", 2,
                jobsInfoService.runningJobs.size())

        jobsInfoService.jobWasExecuted(e2 as JobExecutionContext, null)

        assertEquals("there should be 1 running job according to the service", 1,
                jobsInfoService.runningJobs.size())

        assertEquals("there should be 1 finished job according to the service", 1,
                jobsInfoService.runningJobs.size())

    }

    @Ignore
    public void testFinishedJobsPruning() {

        def jobContexts = [new JobExecutionContext(null, null, null),
            new JobExecutionContext(null, null, null),
            new JobExecutionContext(null, null, null),
            new JobExecutionContext(null, null, null),
            new JobExecutionContext(null, null, null),
            new JobExecutionContext(null, null, null)]
        
        jobContexts.each {
            it.jobInstance = new Object()
            jobsInfoService.jobToBeExecuted(it)
        }

        assertEquals("there should be 6 running jobs according to the service", 6,
                jobsInfoService.runningJobs.size())


        jobContexts.each {
            jobsInfoService.jobWasExecuted(it)
        }

        assertEquals("there should be 0 running jobs according to the service", 0,
                jobsInfoService.runningJobs.size())

        assertEquals("there should be 5 finished job according to the service (oldest dropped)", 5,
                jobsInfoService.finishedJobs.size())

    }
    
}
