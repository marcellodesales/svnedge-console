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

import org.springframework.beans.factory.InitializingBean
import org.quartz.JobExecutionContext
import org.quartz.JobListener
import org.quartz.Trigger


/**
 * Provides info about Quartz jobs in the console
 */
class JobsInfoService extends AbstractSvnEdgeService implements InitializingBean {

    // Observer name supplied to Quartz
    String name = "JobsInfoService"

    // Job names and groups we wish to observe
    def interestingJobNames = [RepoDumpJob.name]
    def interestingJobGroups = [RepoDumpJob.group]

    // scheduler upon which to register listener
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

    /**
     * call this method with JobExecutionContext (or other containing similar properties)
     * to indicate a Job has started
     * @see JobExecutionContext
     * @param jobExecutionContext
     */
    void jobStarted(jobExecutionContext) {
        if(!interested(jobExecutionContext)) {
            return
        }
        runningJobs.put(jobExecutionContext.jobInstance, jobExecutionContext)
    }

    /**
     * call this method with JobExecutionContext (or other containing similar properties)
     * to indicate a Job has vetoed
     * @see JobExecutionContext
     * @param jobExecutionContext
     */
    void jobVetoed(jobExecutionContext)  {

        if(!interested(jobExecutionContext)) {
            return
        }
        runningJobs.remove(jobExecutionContext.jobInstance)
    }

    /**
     * call this method with JobExecutionContext (or other containing similar properties)
     * to indicate a Job has completed
     * @see JobExecutionContext
     * @param jobExecutionContext
     */
    void jobFinished(jobExecutionContext) {

        if(!interested(jobExecutionContext)) {
            return
        }
        if (runningJobs.containsKey(jobExecutionContext.jobInstance)) {
            runningJobs.remove(jobExecutionContext.jobInstance)
            finishedJobs.put(jobExecutionContext.jobInstance, jobExecutionContext)
        }
    }

    /**
     * fetch a map of trigger info pertaining to interesting jobs (only includes
     * those with "nextFireTime" property
     * @return list of Map containing info about the job, keyed by Trigger fullname
     */
    Map getScheduledJobs() {

        def triggerInfo = [:]
        interestingJobGroups.each { jobGroup ->
            interestingJobNames.each { jobName ->
                Trigger[] t = quartzScheduler.getTriggersOfJob(jobName, jobGroup)
                t.each {
                    if (it.nextFireTime) {
                        triggerInfo << ["${it.fullName}": [
                                nextFireTime: it.nextFireTime,
                                jobRunTime: -1,
                                mergedJobDataMap: it.jobDataMap
                        ]]
                    }
                }
            }
        }
        return triggerInfo
    }

    /**
     * @see InitializingBean#afterPropertiesSet
     * initializing bean -- after injection, we need to register with the quartz scheduler to receive events
     */
    void afterPropertiesSet() {

        // create JobListener interface impl to register with quartzScheduler
        def listener = [
                getName: { name },
                jobToBeExecuted: {p1 -> jobStarted(p1)},
                jobExecutionVetoed: {p1 -> jobVetoed(p1)},
                jobWasExecuted: {p1, e -> jobFinished(p1)}
                ] as JobListener
        quartzScheduler.addGlobalJobListener(listener)
    }

    /**
     * are we keeping track of this job execution?
     * @param ctx the JobExecutionContext (or other with jobDetail.name & .group property)
     * @return boolean yes or no
     */
    private boolean interested(ctx) {

        return interestingJobNames.contains(ctx.jobDetail.name) &&
                interestingJobGroups.contains(ctx.jobDetail.group)

    }


}
