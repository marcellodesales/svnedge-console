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

import com.collabnet.svnedge.statistics.Category
import com.collabnet.svnedge.statistics.StatGroup
import com.collabnet.svnedge.statistics.StatValue
import com.collabnet.svnedge.statistics.Statistic
import com.collabnet.svnedge.statistics.StatisticType
import com.collabnet.svnedge.statistics.Unit

class UploadStatisticsServiceIntegrationTests extends GrailsUnitTestCase {
    def uploadStatisticsService

    def NUM_VALUES = 3
    def statistic

    protected void setUp() {
        super.setUp()
        setupTestStatistics()
    }

    protected void tearDown() {
        super.tearDown()
    }

    // this test requires the test statistic "TestUploadStat" to exist on the 
    // master side.
    // we only check that those stat values associated with that stat are
    // uploaded; others may fail if they are missing on the master.
    void testUploadStatistics() {
        uploadStatisticsService?.uploadStats()
        def values = StatValue.findAllByStatistic(statistic)
        assertEquals("There should be 3 StatValues.", 3,
                     values.size())
        // TODO CTF REPLICA
        /* This assertion is commented out until 
           ctfRemoteClientService.uploadStatistics(valuesByStats)
           service is properly implemented.

        values.each {
            it.refresh()
            assertTrue("Each StatValue should now be uploaded.", 
                       it.getUploaded())
        }
        */
    }

    private setupTestStatistics() {
        def category = new Category(name:"TestUploadCategory")
        category.save()
        def unit = new Unit(name:"TestUpload Unit", minValue: 0)
        unit.save()
        def statGroup = new StatGroup(name: "TestUploadGroup", 
                                      title: "Test Upload Group",
                                      unit: unit, 
                                      rawInterval: 300, category: category,
                                      derivedIntervals: [])
        statGroup.save()
        statistic = new Statistic(name: "TestUploadStat", 
                                  title: "TestUploadStat",
                                  type: StatisticType.COUNTER,
                                  group: statGroup)
        statGroup.addToStatistics(statistic).save()
        statistic.save()
        (1..NUM_VALUES).each{ 
            def statValue = new StatValue(timestamp: new Date().getTime(),
                                          interval: 300000,
                                          minValue: 1,
                                          maxValue: 1,
                                          averageValue: 1,
                                          lastValue: 1,
                                          statistic: statistic).save()
        }

        // this stat will fail to upload because it is not present on the
        // master.  It's failure should not interfere with the success
        // of the other stat upload.
        def statistic2 = new Statistic(name: "TestUploadStatFail", 
                                       title: "TestUploadStatFail",
                                       type: StatisticType.COUNTER,
                                       group: statGroup)
        statGroup.addToStatistics(statistic2).save()
        statistic2.save()
        (1..NUM_VALUES).each{ 
            def statValue = new StatValue(timestamp: new Date().getTime(),
                                          interval: 300000,
                                          minValue: 1,
                                          maxValue: 1,
                                          averageValue: 1,
                                          lastValue: 1,
                                          statistic: statistic2).save()
        }
    }
}
