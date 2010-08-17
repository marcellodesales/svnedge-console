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

import grails.test.*
import org.apache.log4j.Logger

import com.collabnet.svnedge.replica.event.UserCacheEvent
import com.collabnet.svnedge.statistics.StatValue

class UserCacheStatisticsServiceIntegrationTests extends GrailsUnitTestCase {
    def userCacheStatisticsService
    def asyncEventListenerService

    Logger logger = Logger.getLogger(this.getClass())

    protected void setUp() {
        super.setUp()
        // clean up, asynchronous database operations from other tests 
        // won't have been rolledback.
        logger.debug("Deleting stat values, if any.")
        StatValue.list().each {
            logger.debug("Deleting stat value " + it)
            it.delete(flush: true)
        }
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testEventHandling() {
        // appContext won't be set properly, so our bootstrapping won't work
        // correctly.  We need to give the UserCacheEventListener an
        // instance of the service explicitly.
        userCacheStatisticsService.listener
            .setUserCacheStatisticsService(userCacheStatisticsService)
        def username = "test"
        def olderEvent = new UserCacheEvent(username, UserCacheEvent.AUTHN,
                                            UserCacheEvent.HIT)
        def newerEvent = new UserCacheEvent(username, UserCacheEvent.AUTHN,
                                            UserCacheEvent.HIT)
        def interval = userCacheStatisticsService.getStatGroup()
            .getRawInterval() * 1000
        // this is a bit of a cheat, but prevents the test from needing
        // to sleep
        def olderEventTime = olderEvent.getTimestamp().getTime() - interval
        olderEvent.setTimestamp(new Date(olderEventTime))
        int testTimes = 3
        def stat = userCacheStatisticsService.getStat(olderEvent)
        def stat2 = userCacheStatisticsService.getStat(newerEvent)
        assertEquals("The older and newer event must have the same statistic.",
                    stat, stat2)
        def statValue = userCacheStatisticsService
            .getStatValue(stat, olderEventTime, interval)
        if (statValue) {
            assertEquals("The statValue (if it exists) should be zero " + 
                statValue, 0, statValue.getAverageValue())
        }
        for (int i = 0; i < testTimes; i++) {
            userCacheStatisticsService.handleEvent(olderEvent)
            statValue = userCacheStatisticsService
                .getStatValue(stat, olderEventTime, interval)
            assertNotNull("The statValue should exist after an event has " +
                          "been handled.", statValue)
            assertEquals("The statValue should be equal to the number of " +
                         "times an event has been handled.", 
                         i + 1, statValue.getAverageValue())
        }
        def newerEventTime = newerEvent.getTimestamp().getTime()
        statValue = userCacheStatisticsService
            .getStatValue(stat, newerEventTime, interval)
        if (statValue) {
            assertEquals("The statValue (if it exists) should be zero",
                         0, statValue.getAverageValue())
        }
        for (int i = 0; i < testTimes; i++) {
            userCacheStatisticsService.handleEvent(newerEvent)
            statValue = userCacheStatisticsService
                .getStatValue(stat, newerEventTime, interval)
            assertNotNull("The statValue should exist after an event has " +
                          "been handled.", statValue)
            assertEquals("The statValue should be equal to the number of " +
                         "times an event has been handled.", 
                         i + 1, statValue.getAverageValue())
        }
    }

    // make sure a fired event eventually updates the statValue
    void testEventListening() {
        def username = "test"
        def event = new UserCacheEvent(username, UserCacheEvent.AUTHN,
                                       UserCacheEvent.HIT)
        asyncEventListenerService.fireEvent(event)
        def interval = userCacheStatisticsService.getStatGroup()
            .getRawInterval() * 1000
        def eventTime = event.getTimestamp().getTime()
        def stat = userCacheStatisticsService.getStat(event)
        def statValue = userCacheStatisticsService
            .getStatValue(stat, eventTime, interval)
        if (!statValue || statValue.getAverageValue() == 0) {
            // wait for a bit for the async thread
            Thread.sleep(2000)
            statValue = userCacheStatisticsService
                .getStatValue(stat, eventTime, interval)
        }
        assertNotNull("StatValue should not be null.", statValue)
        assertEquals("StatValue should be equal to 1.", 
                     1, statValue.getAverageValue())
    }

    // make sure the totals work correctly
    void testEventTotals() {
        def username = "test"
        def cacheType = UserCacheEvent.AUTHZ
        def eventType = UserCacheEvent.HIT
        def now = new Date()
        def earlier = now - 1
        // check the zero case, before events are fired
        def totalZero = userCacheStatisticsService.getTotal(cacheType, 
                                                            eventType,
                                                            earlier, now)
        assertEquals("The total number of events should be zero before any " +
                         "events have occurred.", 0, totalZero)

        // make sure sums include events
        def event = new UserCacheEvent(username, cacheType, eventType)
        def times = 3
        for (int i = 0; i < times; i++) {
            userCacheStatisticsService.handleEvent(event)
            def total = userCacheStatisticsService.getTotal(cacheType, 
                                                            eventType,
                                                            earlier, 
                                                            new Date())
            assertEquals("The total number of events should be equal to " +
                         "the number of events fired.", i + 1, total)
        }
        
        // make sure a later time (w/o any events) will give zero
        totalZero = userCacheStatisticsService.getTotal(cacheType, 
                                                        eventType,
                                                        now + 1,
                                                        now + 2)
        assertEquals("The total number of events should be zero after any " +
                         "events have occurred.", 0, totalZero)
    }

    void testHitRates() {
        // check the no-data case first
        def cacheType = UserCacheEvent.INFO
        def tomorrow = new Date() + 1
        def yesterday = new Date() - 1
        def hitRates = userCacheStatisticsService.getHitRates(cacheType, 
                                                              yesterday, 
                                                              tomorrow)
        assertNotNull("The hit rates should not be null.", hitRates)
        assertEquals("The hit rates should be empty before data.", 0, 
                     hitRates.size())
        // now add some data -- 1 HIT, 3 MISSES
        def username = "test"
        def event = new UserCacheEvent(username, cacheType, UserCacheEvent.HIT)
        userCacheStatisticsService.handleEvent(event)
        event = new UserCacheEvent(username, cacheType, 
                                   UserCacheEvent.MISS)
        userCacheStatisticsService.handleEvent(event)
        userCacheStatisticsService.handleEvent(event)
        userCacheStatisticsService.handleEvent(event)
        def hitStat = userCacheStatisticsService.getStat(cacheType, 
                                                         UserCacheEvent.HIT)
        def missStat = userCacheStatisticsService.getStat(cacheType, 
                                                          UserCacheEvent.MISS)
        def rawInterval =  userCacheStatisticsService.getStatGroup()
            .getRawInterval() * 1000
        assertEquals("The number of hit values should be 1", 1, 
                     StatValue.findAllByStatistic(hitStat).size())
        assertEquals("The number of miss values should be 1", 1, 
                     StatValue.findAllByStatistic(missStat).size())
        hitRates = userCacheStatisticsService.getHitRates(cacheType, 
                                                          yesterday, 
                                                          tomorrow, 
                                                          rawInterval)
        assertNotNull("The hit rates should not be null.", hitRates)
        assertEquals("The hit rates should have a data point.", 1, 
                     hitRates.size())
        assertEquals ("The hit rate should be 25%", 0.25,
                   hitRates.values().toList()[0])
    }

    void testBestDisplayInterval() {
        def statGroup = userCacheStatisticsService.getStatGroup()
        def bestInt = userCacheStatisticsService
            .getBestDisplayInterval(statGroup, 1)
        assertEquals("The best display interval for a tiny timespan should " 
                     + "be the smallest interval.", 300, bestInt)
        bestInt = userCacheStatisticsService
            .getBestDisplayInterval(statGroup, 2592000 )
        assertEquals("The best display interval for 30 days should " 
                     + "be a day.", 86400, bestInt)
    }
}
