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

package com.collabnet.svnedge.domain.quartz

/**
 * This domain class supports Quartz job and scheduling persistence
 */
class QrtzTriggers {

    String triggerName
    String triggerGroup
    String jobName
    String jobGroup
    Boolean isVolatile
    String description
    Long nextFireTime
    Long prevFireTime
    Integer priority
    String triggerState
    String triggerType
    Long startTime
    Long endTime
    String calendarName
    Integer misfireInstr
    byte[] jobData

    Integer version
    static constraints = {
        version(nullable: true)
        isVolatile(nullable: true)
        description(nullable: true)
        nextFireTime(nullable: true)
        prevFireTime(nullable: true)
        priority(nullable: true)
        endTime(nullable: true)
        calendarName(nullable: true)
        misfireInstr(nullable: true)
        jobData(nullable: true, maxSize: 5000)

    }



}
