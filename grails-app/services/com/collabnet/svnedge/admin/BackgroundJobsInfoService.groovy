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
import org.springframework.context.ApplicationListener
import com.collabnet.svnedge.event.BackgroundJobStartedEvent
import com.collabnet.svnedge.event.BackgroundJobTerminatedEvent

/**
 * Provides info about background tasks in the console
 */
class BackgroundJobsInfoService extends AbstractSvnEdgeService
        implements ApplicationListener<BackgroundJobEvent> {

    // current running jobs
    Map runningJobs = Collections.synchronizedMap(new HashMap())

    // recently finished jobs, represented as queue with eldest removed
    Map finishedJobs =  Collections.synchronizedMap(
            new LinkedHashMap(MAX_FINISHED_JOBS_SIZE + 1) {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > BackgroundJobsInfoService.MAX_FINISHED_JOBS_SIZE;
                }
            });

    // the maximum number of finished jobs to hold info about
    public static final int MAX_FINISHED_JOBS_SIZE = 5

    /**
    * The event handler of all {@link BackgroundJobEvent} to
    * process the different events.
    * @param applicationEvent is the instance of a BackgroundJobEvent
    */
    void onApplicationEvent(BackgroundJobEvent applicationEvent) {
        switch(applicationEvent) {
            case BackgroundJobStartedEvent:
                def jobProperties = [:]
                jobProperties << applicationEvent.properties
                jobProperties << [ started: applicationEvent.timestamp]
                runningJobs.put(applicationEvent.procId, jobProperties)
                break;
            case BackgroundJobTerminatedEvent:
                def jobProperties = runningJobs.get(applicationEvent.procId)
                if (jobProperties) {
                    jobProperties << [ finished: applicationEvent.timestamp]
                    runningJobs.remove(applicationEvent.procId)
                    finishedJobs.put(applicationEvent.procId, jobProperties)
                }
                break;
        }
    }
}
