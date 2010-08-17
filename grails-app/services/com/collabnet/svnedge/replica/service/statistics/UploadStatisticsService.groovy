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

import com.collabnet.svnedge.statistics.Statistic
import com.collabnet.svnedge.statistics.StatValue

class UploadStatisticsService {

    boolean transactional = true

    def ctfRemoteClientService

    def uploadStats() {
        def statistics = Statistic.list()
        def valuesByStats = statistics.inject([]) { valuesByStat, stat ->
            def statValues = StatValue.findAllByStatisticAndUploaded(stat, 
                                                                     false)
            if (statValues) {
                valuesByStat << [statistic: stat, values: statValues]
            }
            valuesByStat
        }
        if (!valuesByStats) {
            log.info("No statistics values to upload at this time.")
            return
        }
        def results = ctfRemoteClientService.uploadStatistics(valuesByStats)
        valuesByStats.each { valuesByStat ->
            def statName = valuesByStat["statistic"].getName()
            if (results[statName]) {
                valuesByStat["values"].each { value ->
                    value.setUploaded(true)
                    value.save()
                }
            } else {
                log.error("Upload of statistics values failed for " 
                          + valuesByStat["statistic"].name)
            }
        }
    }
}
