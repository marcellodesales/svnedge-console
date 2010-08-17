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
package com.collabnet.svnedge.replica.service.statistics

import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.statistics.Category
import com.collabnet.svnedge.jobs.StatCollectJob
import com.collabnet.svnedge.statistics.StatGroup
import com.collabnet.svnedge.statistics.Statistic
import com.collabnet.svnedge.statistics.StatValue
import com.collabnet.svnedge.statistics.StatisticType
import com.collabnet.svnedge.statistics.Unit
import com.collabnet.svnedge.statistics.service.AbstractStatisticsService

import org.quartz.SchedulerException

/**
 * Service for getting and storing latency statistics.
 */
class LatencyStatisticsService extends AbstractStatisticsService {

    public static String TRIGGER_NAME = "latencyTrigger"
    public static String CATEGORY_NAME = "System"
    public static String STATGROUP_NAME = "Latency"
    public static String STAT_NAME = "Latency"
    public static String MS_UNIT_NAME = "Milliseconds"

    def statId

    def jobsAdminService
    boolean transactional = true

    def bootStrap = {
        log.info("Latency statistics bootstrap...")
        if (!getStatGroup()) {
            createLatencyStatistics()
        }
    }

    def initTrigger() {
        def trigger = jobsAdminService
            .getTrigger(TRIGGER_NAME, StatCollectJob.triggerGroup)
        if (!trigger) {
            def statGroup = getStatGroup()
            def interval = statGroup.getRawInterval() * 1000
            def params = ["serviceName": "latencyStatisticsService"]
            try {
                new StatCollectJob().schedule(StatCollectJob
                    .createTrigger(TRIGGER_NAME, interval, params, 12600L))
            } catch (SchedulerException ex) {
                log.error("Failed to start StatCollectJob.", ex)
            }
            addDeleteJob(statGroup)
            addConsolidateJob(statGroup)
        }
    }

    def getStatGroup() {
        StatGroup.findByName(STATGROUP_NAME)
    }

    def getStat() {
        if (statId) {
            Statistic.get(statId)
        } else {
            Statistic.findByName(STAT_NAME)
        }
    }

    def collectData() {
        def now = new Date().getTime()
        def latValue = getLatency()
        def interval = getStatGroup().getRawInterval() * 1000
        def statValue = new StatValue(timestamp: idealStartTime(interval, now),
                                      interval: interval,
                                      minValue: latValue,
                                      maxValue: latValue,
                                      averageValue: latValue,
                                      lastValue: latValue,
                                      statistic: getStat())
        statValue.save()
    }

    /**
     * Returns the latency from the given hostname (or the default master if
     * hostname is unset).
     * @return the latency in ms.
     */
    def getLatency(hostname) {
        int numPings = 10
        int timeout = 3000
        
        if (!hostname) {
            hostname = Master.getDefaultMaster().getHostName()
        }
        def netInterface = InetAddress.getByName(hostname)
        def sumTime = 0
        for (int i = 0; i < numPings; i++) {
            def startTime = System.nanoTime()
            netInterface.isReachable(timeout)
            def endTime = System.nanoTime()
            sumTime += endTime - startTime
        }
        return sumTime/(numPings * 10**6)
    }

    def getChartValues(Long startTime, Long endTime) {
        def interval = getBestDisplayInterval(getStatGroup(), 
            (endTime - startTime) / 1000) * 1000
        getValuesKeyedByIdealTime([getStat()], startTime, endTime, interval, null)
    }

    def createLatencyStatistics() {
        log.info("Creating system statistics...")
        def category = Category.findByName(CATEGORY_NAME)
        if (!category) {
            category = new Category(name: CATEGORY_NAME)
            check(category)
            category.save()
        }
        def latencyUnit = Unit.findByName(MS_UNIT_NAME)
        if (!latencyUnit) {
            latencyUnit = new Unit(name: MS_UNIT_NAME, minValue: 0)
            check(latencyUnit)
            latencyUnit.save()
        }
        def statGroup = StatGroup.findByName(STATGROUP_NAME)
        if (!statGroup) {
            statGroup = new StatGroup(name: STATGROUP_NAME, 
                                      title: "Latency from Replica to Master",
                                      unit: latencyUnit, 
                                      category: category,
                                      isReplica: true)
            check(statGroup)
            category.addToGroups(statGroup).save()
            addDefaultActions(statGroup)
        }
        def latencyStat = Statistic.findByName(STAT_NAME)
        if (!latencyStat) {
            latencyStat = new Statistic(name: STAT_NAME,
                                        title: "Latency from Replica to " +
                                               "Master",
                                        type: StatisticType.GAUGE,
                                        group: statGroup)
            check(latencyStat)
            statGroup.addToStatistics(latencyStat).save()
            latencyStat.save()
        }
        statId = latencyStat.getId()
        log.info("Successfully created system statistics.")
    }
}
