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
package com.collabnet.svnedge.jobs

import org.quartz.JobDataMap
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import com.collabnet.svnedge.statistics.StatGroup

/**
 * This job consolidates StatValues.
 */
class ConsolidateStatJob {
    static String name = "ConsolidateStatJob"
    static String group = "Statistics"
    static String triggerGroup = "Statistics_Triggers"

    def consolidateStatisticsService

    static triggers = {}

    def execute(context) {
        def dataMap = context.getMergedJobDataMap()
        def statGroupName = dataMap.get("statGroupName")
        def statGroup = StatGroup.findByName(statGroupName)
        consolidateStatisticsService.consolidate(statGroup)
    }
    
    /** 
     * Create an infinitely repeating simple trigger with the given name
     * and interval.
     */
    static Trigger createTrigger(triggerName, interval, params) {
        def trigger = new SimpleTrigger(triggerName, triggerGroup, 
                                        SimpleTrigger.REPEAT_INDEFINITELY, 
                                        interval)
        trigger.setJobName(name)
        trigger.setJobGroup(group)
        trigger.setJobDataMap(new JobDataMap(params))
        trigger
    }
}
