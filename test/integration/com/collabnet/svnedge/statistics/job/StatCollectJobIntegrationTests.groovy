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
package com.collabnet.svnedge.statistics.job

import com.collabnet.svnedge.statistics.StatCollectJob 
import grails.test.*

import org.quartz.JobListener
import org.quartz.JobExecutionContext

class StatCollectJobIntegrationTests extends GrailsUnitTestCase 
    implements JobListener {
    def networkStatisticsService
    def quartzScheduler
    def statCollectJob
    
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testTriggerNetworkStats() {
        statCollectJob = new StatCollectJob()
        Map params = new HashMap(1)
        params.put("serviceName", "networkStatisticsService")
        quartzScheduler.start()
        quartzScheduler.addGlobalJobListener(this)
        // make sure our job is unpaused
        quartzScheduler.resumeJobGroup(statCollectJob.getGroup())
        statCollectJob.triggerNow(params)
        synchronized(this) {
            this.wait(60000)
        }
        quartzScheduler.standby()
        // make sure the stat values appear
        def values = networkStatisticsService.getCurrentThroughput()
        assertNotNull("The bytesIn value should not be null.", values[0])
        assertNotNull("The bytesOut value should not be null.", values[1])
    }

    /** Listener methods **/
    public String getName() {
        return "StatCollectJobIntegration"
    }
    
    void jobToBeExecuted(JobExecutionContext context) {}
    void jobExecutionVetoed(JobExecutionContext context) {
        synchronized(this) {
            this.notify()
        }
        throw new RuntimeException("Did not expect job to be vetoed.")
    }

    void jobWasExecuted(JobExecutionContext context, 
                        org.quartz.JobExecutionException jobException) {
        if (context.getJobDetail().getName().equals(statCollectJob
                                                    .getName())) {
            synchronized(this) {
                this.notify()
            }
        }
    }
}
