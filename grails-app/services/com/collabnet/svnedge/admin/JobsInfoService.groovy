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

import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.event.BackgroundJobEvent

import com.collabnet.svnedge.event.BackgroundJobStartedEvent
import com.collabnet.svnedge.event.BackgroundJobTerminatedEvent

import org.quartz.JobExecutionContext
import org.springframework.beans.factory.InitializingBean
import org.quartz.JobListener
import org.quartz.JobExecutionException

/**
 * Provides info about Quartz jobs in the console
 */
class JobsInfoService extends AbstractSvnEdgeService
        implements JobListener, InitializingBean {

    // required field for observing Quartz events
    String name = "JobsInfoService"

    def quartzScheduler

    // current running jobs
    Map runningJobs = Collections.synchronizedMap(new HashMap())

    // recently finished jobs, represented as queue with eldest removed
    Map finishedJobs =  Collections.synchronizedMap(
            new LinkedHashMap(MAX_FINISHED_JOBS_SIZE + 1) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > JobsInfoService.MAX_FINISHED_JOBS_SIZE;
                }
            });

    // the maximum number of finished jobs to hold info about
    public static final int MAX_FINISHED_JOBS_SIZE = 5


    void jobToBeExecuted(JobExecutionContext jobExecutionContext) {
        if(!interested(jobExecutionContext)) {
            return
        }
        runningJobs.put(jobExecutionContext.jobInstance, jobExecutionContext)
    }

    void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {
        if(!interested(jobExecutionContext)) {
            return
        }
        runningJobs.remove(jobExecutionContext.jobInstance)
    }

    void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        if(!interested(jobExecutionContext)) {
            return
        }
        if (runningJobs.containsKey(jobExecutionContext.jobInstance)) {
            runningJobs.remove(jobExecutionContext.jobInstance)
            finishedJobs.put(jobExecutionContext.jobInstance, jobExecutionContext)
        }
    }

    /**
     * initializing bean -- after injection, we need to register with the quartz scheduler to receive events
     */
    void afterPropertiesSet() {
        quartzScheduler.addGlobalJobListener(this)
    }

    /**
     * are we keeping track of this job execution?
     * @param ctx the JobExecutionContext
     * @return boolean yes or no
     */
    private boolean interested(JobExecutionContext ctx) {

        def interestingJobNames = [RepoDumpJob.name]
        return interestingJobNames.contains(ctx.jobDetail.name)

    }


}
