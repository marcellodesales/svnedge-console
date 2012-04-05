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

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import com.collabnet.svnedge.console.AbstractSvnEdgeService

import org.quartz.JobExecutionContext
import org.quartz.JobListener
import org.quartz.Trigger

/**
 * Provides queue support for and info about Backup and Load jobs in the console
 */
class JobsInfoService extends AbstractSvnEdgeService {

    // the maximum number of finished jobs to hold info about
    public static final int MAX_FINISHED_JOBS_SIZE = 5

    private static final int MAX_CONCURRENT_JOBS = 3
    
    ExecutorService queue = Executors.newFixedThreadPool(MAX_CONCURRENT_JOBS)

    // Jobs we wish to observe
    def interestingJobs = [RepoDumpJob, RepoLoadJob]

    def quartzScheduler
    
    // current running jobs
    Map runningJobs = Collections.synchronizedMap(new HashMap())
    Map queuedJobs = Collections.synchronizedMap(new HashMap())
    
    // recently finished jobs, represented as queue with eldest removed
    Map finishedJobs =  Collections.synchronizedMap(
            new LinkedHashMap(MAX_FINISHED_JOBS_SIZE + 1) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > JobsInfoService.MAX_FINISHED_JOBS_SIZE;
                }
            });

    void queueJob(def job, Date scheduledFireTime) {
        
        def jobCtx = [scheduledFireTime: scheduledFireTime,
                      jobRunTime: -1,
                      mergedJobDataMap: job.dataMap]
        queuedJobs.put(job, jobCtx)
        queue.execute( {
            queuedJobs.remove(job)
            Date startDate = new Date()
            jobCtx.fireTime = startDate
            runningJobs.put(job, jobCtx)
            try {
                job.run()
                jobCtx.jobRunTime = System.currentTimeMillis() - startDate.time
                finishedJobs.put(job, jobCtx)
            } catch (Exception e) {
                log.warn("Job execution failed.", e)
            } finally {
                runningJobs.remove(job)
            }
        } as Runnable)
    }
        
    /**
     * fetch a map of trigger info pertaining to interesting jobs (only includes
     * those with "nextFireTime" property
     * @return list of Map containing info about the job, keyed by Trigger fullname
     */
    Map getScheduledJobs() {

        def triggerInfo = [:]
        
        queuedJobs.values().each {
            it.nextFireTime = it.scheduledFireTime
            triggerInfo << ["${it.mergedJobDataMap.id}": it]
        }
        
        interestingJobs.each { it ->
            Trigger[] t = quartzScheduler.getTriggersOfJob(it.name, it.group)
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
        return triggerInfo
    }
}
