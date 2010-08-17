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
package com.collabnet.svnedge.replica.jobs

import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.quartz.Trigger

/**
 * This job queries the master to see if the replica has been approved (or
 * denied).  Once the state changes to something definitive, the 
 * registrationService should stop this job.
 */
class CheckStateJob {
    static String name = "com.collabnet.svnedge.replica.jobs.CheckStateJob"
    static String group = "Replica_registration"
    static String triggerName = "checkStateTrigger"
    static String triggerGroup = group + "_Triggers"
    static Long delay = 60000
    static Long interval = 60000

    def registrationService

    static triggers = { 
    /*
        simple name: triggerName, group: triggerGroup, startDelay: delay, \
        repeatInterval: interval
    */
    }

    def concurrent = false

    def execute() {
        log.info("Executing CheckStateJob")
        registrationService.checkAndHandleState()
    }

    /**
     * Creates a new trigger associated with this job and returns it.
     * It is the same trigger defined in the static triggers.
     * This method exists so that this job can be re-added to the scheduler
     * after it is unscheduled.
     */
    public static Trigger createTrigger() {
        def trigger = new SimpleTrigger(triggerName, triggerGroup, 
                                        SimpleTrigger.REPEAT_INDEFINITELY, 
                                        interval)
        trigger.setJobName(name)
        trigger.setJobGroup(group)
        trigger
    }
}
