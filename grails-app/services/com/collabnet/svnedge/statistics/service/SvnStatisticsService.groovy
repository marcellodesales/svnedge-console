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
package com.collabnet.svnedge.statistics.service

import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.SvnLog
import com.collabnet.svnedge.jobs.StatCollectJob
import com.collabnet.svnedge.statistics.Category
import com.collabnet.svnedge.statistics.StatGroup
import com.collabnet.svnedge.statistics.Statistic
import com.collabnet.svnedge.statistics.StatValue
import com.collabnet.svnedge.statistics.StatisticType
import com.collabnet.svnedge.statistics.Unit

import org.quartz.SchedulerException

class SvnStatisticsService extends AbstractStatisticsService{
    public static String CATEGORY_NAME = "Svn"
    public static String STATGROUP_NAME = "SvnRepoHits"
    public static String STAT_NAME = "SvnRepoHits"
    public static String TRIGGER_NAME = "svnStatTrigger"
    public static String HITS_UNIT_NAME = "Hits"

    def svnLogService

    boolean transactional = true

    // used to ensure multiple threads don't attempt simultaneous 
    // data collection
    def collectionInProgress = false

    def statGroupId
    def statId

    def bootStrap = {
        if (!getStatGroup()) {
            createStatistics()
        }
        def interval = getStatGroup().getRawInterval() * 1000
        def params = ["serviceName": "svnStatisticsService"]
        def statCollectJob = new StatCollectJob()
        try {
            statCollectJob.schedule(StatCollectJob
                                    .createTrigger(TRIGGER_NAME, interval, 
                                                   params, 12200L))
        } catch (SchedulerException ex) {
            log.error("Failed to start StatCollectJob due to exception.", ex)
        }
        addDeleteJob(getStatGroup())
        addConsolidateJob(getStatGroup())
    }

    def getStatGroup() {
        def group = StatGroup.get(statGroupId)
        if (!group) {
            group = StatGroup.findByName(STATGROUP_NAME)
            statGroupId = group?.getId()
        }
        group
    }

    def getStat() {
        def stat = Statistic.get(statId)
        if (!stat) {
            stat = Statistic.findByName(STAT_NAME)
            statId = stat?.getId()
        }
        return stat
    }

    def collectData() {
        def interval = getStatGroup().getRawInterval() * 1000
        def startTime = getStartTime(interval)
        def endTime = getEndTime(interval)
        if (!startTime || !endTime) {
            // no data yet
            log.info("svnStatisticsService: no data yet.")
            return
        }
        synchronized(collectionInProgress) {
            if (collectionInProgress) {
                log.info("svnStatisticsService: collection already in "
                         + "progress.")
                return;
            }
            collectionInProgress = true
        }
        try {
            for (long currentTime = startTime; currentTime < endTime; 
                 currentTime += interval) {
                Repository.list().each { repo ->
                    def hits = getHitsForRepo(repo, currentTime, 
                                              currentTime + interval)
                    def value = new StatValue(timestamp: currentTime, 
                                              interval: interval,
                                              minValue: hits, maxValue: hits, 
                                              averageValue: hits, 
                                              lastValue: hits,
                                              derived: false, uploaded: false,
                                              repo: repo,
                                              statistic: getStat())
                    value.save()
                }
            }   
        } catch (Exception e) {
            log.error("Exception while collecting data.", e)
            throw e
        } finally {
            synchronized(collectionInProgress) {
                collectionInProgress = false
            }
        }
    }

    /**
     * The start time should be after the last interval this service collected
     * data OR (if this service has not collected data yet) the earliest
     * time that the svnLogService has recorded data.
     * Returns time in ms since 1970.  This may return null.  If so,
     * there is no data to collect yet.
     */
    def getStartTime(interval) {
        def crit = StatValue.createCriteria()
        def stat = getStat()
        def lastValue = crit.list {
            eq("statistic", stat)           
            maxResults(1)
            order("timestamp", "desc")
        }
        if (lastValue) {
            return lastValue[0].getTimestamp()
        } else {
            def time = svnLogService.getFirstLog()?.getTimestamp()
            if (time) {
                return idealStartTime(interval, time)
            } else {
                return null
            }
        }
    }

    /**
     * The endtime should be the current system time closed interval.
     */
    def getEndTime(interval) {
        return idealStartTime(interval, (new Date()).getTime())
    }

    /**
     * Return a count of the number of hits for a given repo during
     * the time period.
     */
    def getHitsForRepo(repo, startTime, endTime) {
        def crit = SvnLog.createCriteria()
        def repoHitCount = crit.get {
            projections {
                rowCount()
            }
            and {
                eq("repo", repo)
                between('timestamp', startTime, endTime)
            }
        }
        return repoHitCount
    }

    /**
     * Return a TreeMap keyed by timestamp, containing the hit rates for
     * each repo.
     */
    def getChartValues(startTime, endTime) {
        def interval = getBestDisplayInterval(getStatGroup(), 
            (endTime - startTime) / 1000) * 1000
        return getRepoValuesKeyedByIdealTime(getStat(), startTime, endTime,
                                             interval)
    }

    def createStatistics() {
        log.info("Creating svn statistics...")
        def category = Category.findByName(CATEGORY_NAME)
        if (!category) {
            category = new Category(name: CATEGORY_NAME)
            check(category)
            category.save()
        }
        def unit = Unit.findByName(HITS_UNIT_NAME)
        if (!unit) {
            unit = new Unit(name:HITS_UNIT_NAME, minValue: 0)
            check(unit)
            unit.save()
        }
        def statGroup = StatGroup.findByName(STATGROUP_NAME)
        if (!statGroup) {
            statGroup = new StatGroup(name: STATGROUP_NAME, 
                                      title: "Subversion Repository Hits", 
                                      unit: unit, 
                                      category: category)
            check(statGroup)
            category.addToGroups(statGroup).save()
            statGroup.save()
            addDefaultActions(statGroup)
        }
        statGroupId = statGroup.getId()
        def hitStat = Statistic.findByName(STAT_NAME)
        if (!hitStat) {
            hitStat = new Statistic(name: STAT_NAME,
                                    title: "Subversion Repository Hits",
                                    type: StatisticType.COUNTER,
                                    group: statGroup)
            check(hitStat)
            statGroup.addToStatistics(hitStat).save()
            hitStat.save()
        }
        statId = hitStat.getId()
        log.info("Successfully created svn statistics.")
    }
}
