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

import com.collabnet.svnedge.replica.event.UserCacheEvent
import com.collabnet.svnedge.replica.service.CacheManagementService
import com.collabnet.svnedge.replica.manager.ReplicatedRepository

import java.net.InetAddress

/**
 * Service for getting up-to-date statistics values.
 */
class RealTimeStatisticsService {
    def userCacheStatisticsService
    def networkStatisticsService
    def cacheManagementService
    def fileSystemStatisticsService
    def latencyStatisticsService

    boolean transactional = true

    /**
     * Returns the number of users currently stored in the user cache.
     * @return number of users cached (int).
     */
    def getNumUsersCached() {
        return cacheManagementService?.getAuthenticationCacheSize()
    }

    /** 
     * Returns the percentage of hits (vs. misses) for the user cache (for
     * the past day)
     * @return the percentage (float value between 0 and 1) of cache hits. 
     */
    def getUserCachePercentageHit() {
        def now = new Date()
        def getTotal = { cacheType, eventType ->
            userCacheStatisticsService.getTotal(cacheType, eventType, now - 1, 
                                                now)
        }
        def totalHits = UserCacheEvent.CACHE_TYPES.collect { 
            getTotal(it.type, UserCacheEvent.HIT)
        }.sum()
        def totalMisses = UserCacheEvent.CACHE_TYPES.collect { 
            getTotal(it.type, UserCacheEvent.MISS)
        }.sum()
        // avoid death by divide by zero error
        if ((totalHits + totalMisses) == 0) {
            return 0
        }
        return totalHits.floatValue() / (totalHits + totalMisses)
    }

    /**
     * Returns the list of repository status and the number of repos in
     * that state.
     * @return a list of repoStatus and number of repos.
     */
    def getReposStatus() {
        def crit = ReplicatedRepository.createCriteria()
        def results = crit.list {
            projections {
                groupProperty("status")
                rowCount()
            }
        }
        def mapResults = []
        results.each { 
            mapResults.add([status: it[0], count: it[1]])
        }
        return mapResults
    }

    /**
     * Returns the amount of disk space used by the repositories.
     * @return the space for repositories (in bytes)
     */
    def getRepoUsedDiskspace() {
        fileSystemStatisticsService.getRepoUsedDiskspace()
    }

    /**
     * Returns the amount of disk space available for the repositories.
     * @return the space for repositories (in bytes)
     */
    def getRepoAvailableDiskspace() {
        fileSystemStatisticsService.getRepoAvailableDiskspace()
    }

    /**
     * Returns the total space in the partition containing the repo storage.
     * @return the space for repo storage (in bytes)
     */
    def getRepoTotalDiskspace() {
        fileSystemStatisticsService.getRepoTotalDiskspace()
    }

    /**
     * Returns the total amount of diskspace in use.
     * @return the total diskspace in use (in bytes)
     */
    def getSystemUsedDiskspace() {
        fileSystemStatisticsService.getSystemUsedDiskspace()
    }

    /**
     * Returns the total amount of diskspace total.
     * @return the total diskspace (in bytes)
     */
    def getSystemTotalDiskspace() {
        fileSystemStatisticsService.getSystemTotalDiskspace()
    }

    /**
     * Returns the throughputs (in, out) on the primary interface, if 
     * available.
     * @return an array containing throughputIn, timeIntervalIn, throughputOut,
     *         timeIntervalOut.  The throughputs are in b/s, the timeIntervals
     *         are in s.
     */
    def getThroughput() {
        def throughputIn
        def timeIntervalIn
        def throughputOut
        def timeIntervalOut
        // values from the statistics service are in b/ms and ms
        def throughput = networkStatisticsService.getCurrentThroughput()
        if (throughput == null || throughput[0] == null) {
            throughputIn = null
            timeIntervalIn = null
        } else {
            throughputIn = throughput[0][0] * 1000
            timeIntervalIn = throughput[0][1] / 1000
        }
        if (throughput == null || throughput[1] == null) {
            throughputOut = null
            timeIntervalOut = null
        } else {
            throughputOut = throughput[1][0] * 1000
            timeIntervalOut = throughput[1][1] / 1000
        }
        return [throughputIn, timeIntervalIn, throughputOut, timeIntervalOut]
    }

    /**
     * Returns the latency from the given hostname (or the default master if
     * hostname is unset).
     * @return the latency in ms.
     */
    def getLatency(hostname) {
        latencyStatisticsService.getLatency(hostname)
    }
}
