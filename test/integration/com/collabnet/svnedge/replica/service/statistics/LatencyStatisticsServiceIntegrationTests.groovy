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

import com.collabnet.svnedge.statistics.StatValue

class LatencyStatisticsServiceIntegrationTests extends GrailsUnitTestCase {
    def latencyStatisticsService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testGetLatency() {
        def latency = latencyStatisticsService.getLatency()
        assertNotNull("A latency should be returned.", latency)
        assertTrue("The latency value should be greater than 0.", latency > 0)
    }

    void testCollectData() {
        latencyStatisticsService.collectData()
        def now = new Date().getTime()
        def stat = latencyStatisticsService.getStat()
        def interval = latencyStatisticsService.getStatGroup()
            .getRawInterval() * 1000
        def statValue = latencyStatisticsService.getStatValue(stat,
                                                              now, interval)
        assertNotNull("The latency statvalue should not be null.", statValue)
    }

    void testGetChartValues() {
        // make sure there is data
        latencyStatisticsService.collectData()
        def start = (new Date() - 1).getTime()
        def end = (new Date() + 1).getTime()
        def values = latencyStatisticsService.getChartValues(start, end)
        assertNotNull("The chart values should not be null.", values)
        assertTrue("There should be at least one chart value.", 
                   values.size() > 0)
    }

}
