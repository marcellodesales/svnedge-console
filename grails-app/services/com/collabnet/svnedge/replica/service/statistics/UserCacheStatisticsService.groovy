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

import com.collabnet.svnedge.replica.event.UserCacheEventListener
import com.collabnet.svnedge.replica.event.UserCacheEvent

import com.collabnet.svnedge.statistics.Category
import com.collabnet.svnedge.jobs.StatCountJob
import com.collabnet.svnedge.statistics.StatGroup
import com.collabnet.svnedge.statistics.Statistic
import com.collabnet.svnedge.statistics.StatValue
import com.collabnet.svnedge.statistics.StatisticType
import com.collabnet.svnedge.statistics.Unit
import com.collabnet.svnedge.statistics.service.AbstractStatisticsService

import org.quartz.SimpleTrigger
import org.quartz.SchedulerException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class UserCacheStatisticsService extends AbstractStatisticsService
    implements ApplicationContextAware {

    def jobsAdminService
    boolean transactional = true

    public static String CATEGORY_NAME = "Cache"
    public static String STATGROUP_NAME = "UserCache"
    public static String USERS_UNIT_NAME = "Users"
    public static String TRIGGER_NAME = "userCacheTrigger"

    ApplicationContext appCtx
    def statGroupId
    def listener

    void setApplicationContext(ApplicationContext appContext) {
        appCtx = appContext
    }

    def bootStrap = {
        listener = new UserCacheEventListener(appCtx)
        listener.listen(UserCacheEvent.class)
        def statGroup = getStatGroup()
        if (!statGroup) {
            createUserCacheStatistics()
        }
    }

    def initTrigger() {
        def trigger = jobsAdminService
            .getTrigger(TRIGGER_NAME, StatCountJob.triggerGroup)
        if (!trigger) {
            def statGroup = getStatGroup()
            def interval = statGroup.getRawInterval() * 1000
            Map params = new HashMap(1)
            params.put("statGroupName", statGroup.getName())
            try {
                new StatCountJob().schedule(StatCountJob
                    .createTrigger(TRIGGER_NAME, interval, params, 13600L))
            } catch (SchedulerException ex) {
                log.error("Failed to start StatCountJob.", ex)
            }
            addDeleteJob(statGroup)
            addConsolidateJob(statGroup)
        }
    }        

    def getStatGroup() {
        def sg
        if (statGroupId) {
            sg = StatGroup.get(statGroupId)
        } else {
            sg = StatGroup.findByName("UserCache")
            if (sg) {
                statGroupId = sg.id
            } else {
                log.warn("Unable to load UserCache StatGroup")
            }
        }
        return sg
    }

    @Override
    def handleEvent(userCacheEvent) {
        log.info("UserCacheStatisticsService: got event: " + userCacheEvent)
        /**
         * Synchronizing on the stat (we want an atomic add on the statValue)
         * for 2 reasons:
         * 1) if we end up with more than one AsyncEventHandler thread, we
         *    may be getting more than one handleEvent simultaneously, so 
         *    we shouldn't assume atomicity.
         * 2) we should have a job that inserts zeros (in case no events at
         *    all come through during a certain interval).  That job will be
         *    a separate thread.
         */
        def eventTime = userCacheEvent.getTimestamp().getTime()
            def interval = getStatGroup().getRawInterval() * 1000
            def stat = getStat(userCacheEvent)
            synchronized(stat) {
                def statValue = getStatValue(stat, eventTime, interval)
                if (!statValue) {
                    statValue = new StatValue(timestamp: 
                                                  idealStartTime(interval, 
                                                                 eventTime),
                                              interval: interval,
                                              minValue: 1,
                                              maxValue: 1,
                                              averageValue: 1,
                                              lastValue: 1,
                                              statistic: stat)
                    statValue.save()
                    log.debug("Saved initial user cache stat 1. " + statValue)
                } else {
                    statValue.minValue += 1
                    statValue.maxValue += 1
                    statValue.averageValue += 1
                    statValue.lastValue += 1
                    statValue.save()
                    log.debug("Saved user cache stat. " + 
                              statValue.lastValue + " " + statValue)
                }
            }
    }

    /**
`    * Returns the total number of events that occurred during the time period.
     * @param cacheType is the type of cache (authn, authz, userinfo)
     * @param eventType is the event type (hit/miss).
     * @param startTime is the earliest time we should grab values for. (Date)
     * @param endTime is the latest time we should grab values for. (Date)
     */
    def getTotal(cacheType, eventType, startTime, endTime) {
        def stat = getStat(cacheType, eventType)
        return sumStatValue(stat, startTime.getTime(), endTime.getTime())
    }

    /**
     * Returns the value hit rates between the start and end time.
     * This would require a fairly complicated query (and it would have
     * to be in SQL directly) to get this data straight from the db).
     * For now, I'll do it the less efficient, but more built-in way.
     * If it proves inefficient, we should look into writing a sql statement
     * through groovy.
     * @param cacheType is the type of cache (authn, authz, userinfo)
     * @param startTime is the earliest time we should grab values for. (Date)
     * @param endTime is the latest time we should grab values for. (Date)
     * @param interval is the interval (in ms) to get values for.  If not
     *        provided, it will use the best display interval.
     */
    def getHitRates(String cacheType, Date startTime, Date endTime, 
                    Long interval = null) {
        def hitStat = getStat(cacheType, UserCacheEvent.HIT)
        def missStat = getStat(cacheType, UserCacheEvent.MISS)
        if (!interval) {
            interval = 
                getBestDisplayInterval(getStatGroup(), 
                                       dateDiffInSec(startTime, 
                                                     endTime)) * 1000
        }
        def hitValues = getStatValues(hitStat, startTime.getTime(), 
                                      endTime.getTime() + interval, interval)
        def missValues = getStatValues(missStat, startTime.getTime(), 
                                       endTime.getTime() + interval, interval)
        // make a map of the miss average values, keyed by timestamp
        def missMap = missValues.inject([:]) { map, miss ->
            map[miss.getTimestamp()] = miss.getAverageValue()
            map
        }
        // for every hit value, calculate the rate, using the missMap
        def rates = hitValues \
            .inject(new LinkedHashMap(hitValues.size(), 
                                      new Float(0.75), false)) { map, it ->
            def missValue = missMap.get(it.getTimestamp())
            if (!missValue) {
                missValue = 0
            }
            def rate = it.getAverageValue() / (missValue 
                                               + it.getAverageValue()) 
            log.info("cacheType: " + cacheType)
            log.info("hit value: " + it.getAverageValue())
            log.info("miss value: " + missValue)
            log.info("rate = " + rate)
            map[it.getTimestamp()] = rate
            map
        }
        return rates
    }

    def getStat(userCacheEvent) {
        return getStat(userCacheEvent.cacheType, userCacheEvent.eventType)
    }

    def getStat(cacheType, eventType) {
        return Statistic.findByName(cacheType + " " + eventType)
    }

    // eventually this will be loaded by grabbing data from the master
    // part of the bootstrap
    def createUserCacheStatistics() {
        log.info("Bootstrapping user cache statistics")
        def check = { domain ->
            if (!domain.validate()) {
                domain.errors.allErrors.each {
                    log.info("Creation failed: " + it)
                }
            }   
        }
        def category = Category.findByName(CATEGORY_NAME)
        if (!category) {
            category = new Category(name:CATEGORY_NAME)
            check(category)
            category.save()
        }
        def unit = Unit.findByName(USERS_UNIT_NAME)
        if (!unit) {
            unit = new Unit(name:USERS_UNIT_NAME, minValue: 0)
            check(unit)
            unit.save()
        }
        def statGroup = StatGroup.findByName(STATGROUP_NAME)
        if (!statGroup) {
            statGroup = new StatGroup(name: STATGROUP_NAME,
                                      title: "User Cache",
                                      unit: unit, 
                                      category: category,
                                      isReplica: true)
            check(statGroup)
            if (!category.addToGroups(statGroup).save()) {
                log.error("category addToGroups fails!")
            }
            statGroup.save()
            addDefaultActions(statGroup)
        }
        statGroupId = statGroup.getId()
        UserCacheEvent.CACHE_TYPES.each { cacheType ->
            UserCacheEvent.EVENT_TYPES.each { eventType ->
                def statName = cacheType.type + " " + eventType.type
                def stat = Statistic.findByName(statName)
                if (!stat) {
                    stat = new Statistic(name: statName,
                                         title: cacheType.name + " " + 
                                             " Cache " + eventType.name,
                                         type: StatisticType.COUNTER,
                                         group: statGroup)
                    check(stat)
                    statGroup.addToStatistics(stat).save()
                }
            }
        }
        log.info("Bootstrapping user cache statistics... done")
    }
}
