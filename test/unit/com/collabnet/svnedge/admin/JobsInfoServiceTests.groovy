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

/**
 * Tests for the JobsInfoService
 */
class JobsInfoServiceTests extends GrailsUnitTestCase {

    def jobsInfoService

    public void setUp() {
        super.setUp()
        this.jobsInfoService = new JobsInfoService()
    }

    /**
     * validates that JobInfoService correctly registers running and finishing of jobs
     */
    public void testJobsRunningAndFinished() {

        def e1 = createMockJobExecutionContext()
        jobsInfoService.jobStarted(e1)

        def e2 =  createMockJobExecutionContext()
        jobsInfoService.jobStarted(e2)

        assertEquals("there should be 2 running jobs according to the service", 2,
                jobsInfoService.runningJobs.size())

        jobsInfoService.jobFinished(e2)

        assertEquals("there should be 1 running job according to the service", 1,
                jobsInfoService.runningJobs.size())

        assertEquals("there should be 1 finished job according to the service", 1,
                jobsInfoService.runningJobs.size())

    }

    /**
     * validates that JobInfoService only tracks jobs of interest
     */
    public void testInterestingJobsOnly() {

        def jobOfInterest = createMockJobExecutionContext(true)
        jobsInfoService.jobStarted(jobOfInterest)

        def notInterested =  createMockJobExecutionContext(false)
        jobsInfoService.jobStarted(notInterested)

        assertEquals("there should be 1 running jobs according to the service", 1,
                jobsInfoService.runningJobs.size())

    }


    /**
     * validates that info of only 5 recent jobs are retained
     */
    public void testFinishedJobsPruning() {

        def jobContexts = []
        (1..6).each {
            jobContexts << createMockJobExecutionContext()
        }

        jobContexts.each {
            jobsInfoService.jobStarted(it)
        }

        assertEquals("there should be 6 running jobs according to the service", 6,
                jobsInfoService.runningJobs.size())

        jobContexts.each {
            jobsInfoService.jobFinished(it)
        }

        assertEquals("there should be 0 running jobs according to the service", 0,
                jobsInfoService.runningJobs.size())

        assertEquals("there should be 5 finished job according to the service (oldest dropped)", 5,
                jobsInfoService.finishedJobs.size())

    }

    /**
     * helper to create a mock JobExecutionContext
     * @param isObserved boolean whether to provide a context of the type that JobInfoService is observing
     * @return
     */
    def createMockJobExecutionContext(boolean isObserved = true) {

        def jobDetail = new Expando()
        jobDetail.name = (isObserved) ?
                jobsInfoService.interestingJobNames[0] :
                "TestJob${Math.floor(Math.random() * 1000)}"

        def job = new Expando()
        job.jobDetail = jobDetail
        job.jobInstance = new Object()

        return job
    }
    
}
