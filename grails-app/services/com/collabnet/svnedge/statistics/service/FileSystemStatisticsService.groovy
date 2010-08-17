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

import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.jobs.StatCollectJob
import com.collabnet.svnedge.statistics.Category
import com.collabnet.svnedge.statistics.StatGroup
import com.collabnet.svnedge.statistics.Statistic
import com.collabnet.svnedge.statistics.StatValue
import com.collabnet.svnedge.statistics.StatisticType
import com.collabnet.svnedge.statistics.Unit

import org.quartz.SchedulerException
import com.collabnet.svnedge.console.Repository

/**
 * Service for getting and storing filesystem statistics.
 */
class FileSystemStatisticsService extends AbstractStatisticsService {

    def operatingSystemService

    public static String CATEGORY_NAME = "System"
    public static String STATGROUP_NAME = "FileSystem"
    public static String SYSUSED_NAME = "sysUsed"
    public static String REPOFREE_NAME = "repoFree"
    public static String REPOUSED_NAME = "repoUsed"
    public static String TRIGGER_NAME = "fileSystemTrigger"
    public static String DISKSPACE_UNIT_NAME = "Bytes"

    def statGroupId
    def sysUsedStatId
    def repoFreeStatId
    def repoUsedStatId

    boolean transactional = true

    def bootStrap = {
        if (!getStatGroup()) {
            createFileSystemStatistics()
        }
        def interval = getStatGroup().getRawInterval() * 1000
        def params = ["serviceName": "fileSystemStatisticsService"]
        def statCollectJob = new StatCollectJob()
        try {
            statCollectJob.schedule(StatCollectJob
                .createTrigger(TRIGGER_NAME, interval, params, 12400L))
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

    def getSysUsedStat() {
        def stat
        if (sysUsedStatId) { 
            stat = Statistic.get(sysUsedStatId)
        }
        if (!stat) {
            stat = Statistic.findByName(SYSUSED_NAME)
            sysUsedStatId = stat?.getId()
        }
        stat
    }

    def getRepoFreeStat() {
        def stat
        if (repoFreeStatId) { 
            stat = Statistic.get(repoFreeStatId)
        }
        if (!stat) {
            stat = Statistic.findByName(REPOFREE_NAME)
            repoFreeStatId = stat?.getId()
        }
        stat
    }

    def getRepoUsedStat() {
        def stat
        if (repoUsedStatId) { 
            stat = Statistic.get(repoUsedStatId)
        }
        if (!stat) {
            stat = Statistic.findByName(REPOUSED_NAME)
            repoUsedStatId = stat?.getId()
        }
        stat
    }
    
    // return the magnitude and prefix for a byte value
    public static getByteMagPrefix(number) {
       def prefixes = ['', 'K', 'M', 'G', 'T', 'P', 'E']
       def mag = prefixes.size() - 1
       for (int i = 0; i < prefixes.size(); i++) {
           if (number < 1024**(i + 1)) {
               mag = i
               break
           }
       }
       
       return [mag, prefixes[mag]]
   }

    def collectData() {
        def now = new Date().getTime()
        def interval = getStatGroup().getRawInterval() * 1000
        log.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
        def sysUsed = 0
        def repoFree = 0
        try {
            sysUsed = getSystemUsedDiskspace()
            repoFree = getRepoAvailableDiskspace()
            operatingSystemService.sigar.getFileSystemUsage(
                operatingSystemService.appRootVolumePath).toMap().each { k, v ->
                log.debug("% ${k}=${v}")
            }
            log.debug("%%%%%%%%%%%%%%%%%% Captured values ##################")
            log.debug("System Free: ${repoFree}${getByteMagPrefix(repoFree)};"+
            "System Volume Used: ${sysUsed}${getByteMagPrefix(sysUsed)};")
        
        } catch (Exception e) {
            log.error("There was an error capturing the file system " +
                "statistics: " + e.message)
            return
        }

        def repoStatValue
        long repoUsed
        long repoUsedTotal = 0
        
        // save per-repo space used
        log.debug("%%%%%%%%%%%%%%%%%%%%%%%% Repo Disk Space ##################")
        Repository.list().each { it ->
            repoUsed = getRepoUsedDiskspace(it)
            repoUsedTotal += repoUsed
            repoStatValue = new StatValue(timestamp: idealStartTime(interval, now),
                                      interval: interval,
                                      minValue: repoUsed,
                                      maxValue: repoUsed,
                                      averageValue: repoUsed,
                                      lastValue: repoUsed,
                                      statistic: getRepoUsedStat(),
                                      repo: it)
            repoStatValue.save()
            log.debug("Repo '${it.name}' Used: ${repoUsed}${getByteMagPrefix(repoUsed)}")
        }

        repoStatValue = new StatValue(timestamp: idealStartTime(interval, now),
                                      interval: interval,
                                      minValue: repoUsedTotal,
                                      maxValue: repoUsedTotal,
                                      averageValue: repoUsedTotal,
                                      lastValue: repoUsedTotal,
                                      statistic: getRepoUsedStat(),
                                      repo: null)
        repoStatValue.save()
        log.debug("Repo Total Used: ${repoUsedTotal}${getByteMagPrefix(repoUsedTotal)}")


        def dbRepoVal = StatValue.get(repoStatValue.id)
        log.debug("%%%%%%%%%%%%%%%%%%%%%%%% DB values ##################")
        log.debug("DB Repo Used Value: ${dbRepoVal.averageValue}" +
            "${getByteMagPrefix(dbRepoVal.averageValue)};")

        def sysValue = new StatValue(timestamp: idealStartTime(interval, now),
                                     interval: interval,
                                     minValue: sysUsed,
                                     maxValue: sysUsed,
                                     averageValue: sysUsed,
                                     lastValue: sysUsed,
                                     statistic: getSysUsedStat())
        sysValue.save()
        def dbSysValue = StatValue.get(sysValue.id)
        log.debug("DB System Volume Used: ${dbSysValue.averageValue}" +
            "${getByteMagPrefix(dbSysValue.averageValue)};")

        def repoFreeValue = new StatValue(timestamp: idealStartTime(interval, now),
                                          interval: interval,
                                          minValue: repoFree,
                                          maxValue: repoFree,
                                          averageValue: repoFree,
                                          lastValue: repoFree,
                                          statistic: getRepoFreeStat())
        repoFreeValue.save()
        def dbRepoFreeValue = StatValue.get(repoFreeValue.id)
        log.debug("DB Repo Free: ${dbRepoFreeValue.averageValue}" +
            "${getByteMagPrefix(dbRepoFreeValue.averageValue)};")
        log.debug("%%%%%%%%%%%%%%%%%%%%%%%% ############### ##################")
    }

    /**
     * Returns the amount of disk space used by the repositories.
     * @return the space for repositories (in bytes)
     */
    def long getRepoUsedDiskspace() {
        return operatingSystemService.sigar.getDirUsage(
            getRepoSystemRoot().canonicalPath).diskUsage
    }

    /**
     * Returns the amount of disk space used by an individual repository
     * @return the space for repositories (in bytes)
     */
    def long getRepoUsedDiskspace(Repository repo) {
        def repoDir  = new File(getRepoSystemRoot(), repo.name).canonicalPath
        return operatingSystemService.sigar.getDirUsage(repoDir).diskUsage
    }

    /**
      * Returns the amount of disk space available for the repositories.
     * @return the space for repositories (in bytes)
     */
    def long getRepoAvailableDiskspace() {
        operatingSystemService.sigar.getFileSystemUsage(
            operatingSystemService.appRootVolumePath).avail * 1024
    }

        /**
     * Returns the total space in the partition containing the repo storage.
     * @return the space for repo storage (in bytes)
     */
    def long getRepoTotalDiskspace() {
        operatingSystemService.sigar.getFileSystemUsage(
            operatingSystemService.appRootVolumePath).total * 1024
    }

    /**
     * Gets the file system for repository storage.
     * @return a file object for the repository storage.
     */
    def File getRepoSystemRoot() {
        return new File(Server.getServer().repoParentDir)
    }

    /**
     * Returns the total amount of diskspace in use.
     * @return the total diskspace in use (in bytes)
     */
    def long getSystemUsedDiskspace() {
        operatingSystemService.sigar.getFileSystemUsage(
            operatingSystemService.sysRootVolumePath).used * 1024
    }

    /**
     * Returns the total amount of diskspace total.
     * @return the total diskspace (in bytes)
     */
    def long getSystemTotalDiskspace() {
        operatingSystemService.sigar.getFileSystemUsage(
            operatingSystemService.sysRootVolumePath).total * 1024
    }

    /**
     * Get the file object for the system root.
     * @return the File for the system root.
     */
    private File getSystemRoot() {
        return new File(operatingSystemService.sysRootVolumePath)
    }

    def getChartValues(Long startTime, Long endTime) {
        def interval = getBestDisplayInterval(getStatGroup(), 
            (endTime - startTime) / 1000) * 1000
        getValuesKeyedByIdealTime(getStatGroup().getStatistics(), 
                                  startTime, endTime, interval, null)
    }

    def getChartValues(Long startTime, Long endTime, Repository repo)  {
        def interval = getBestDisplayInterval(getStatGroup(),
                (endTime - startTime) / 1000) * 1000
        getValuesKeyedByIdealTime( [ getRepoUsedStat() ] , startTime,
                endTime, interval, repo)
    }

    def createFileSystemStatistics() {
        log.info("Creating file system statistics...")
        def category = Category.findByName(CATEGORY_NAME)
        if (!category) {
            category = new Category(name: CATEGORY_NAME)
            check(category)
            category.save()
        }
        def diskspaceUnit = Unit.findByName(DISKSPACE_UNIT_NAME)
        if (!diskspaceUnit) {
            diskspaceUnit = new Unit(name: DISKSPACE_UNIT_NAME, minValue: 0)
            check(diskspaceUnit)
            diskspaceUnit.save()
        }
        def statGroup = getStatGroup()
        if (!statGroup) {
            statGroup = new StatGroup(name: STATGROUP_NAME, 
                                      title: "Disk space on the Filesystem",
                                      unit: diskspaceUnit, 
                                      category: category)
            check(statGroup)
            category.addToGroups(statGroup).save()
            statGroup.save()
            addDefaultActions(statGroup)
        }
        // save the statGroup so that we can get id info
        statGroupId = statGroup.getId()
        def sysUsedStat = Statistic.findByName(SYSUSED_NAME)
        if (!sysUsedStat) {
            sysUsedStat = new Statistic(name: SYSUSED_NAME,
                                        title: "Used space on root volume",
                                        type: StatisticType.GAUGE,
                                        group: statGroup)
            check(sysUsedStat)
            statGroup.addToStatistics(sysUsedStat).save()
            sysUsedStat.save()
        }
        sysUsedStatId = sysUsedStat.getId()

        def repoFreeStat = Statistic.findByName(REPOFREE_NAME)
        if (!repoFreeStat) {
            repoFreeStat = new Statistic(name: REPOFREE_NAME,
                                        title: "Free space on the repository volume",
                                        type: StatisticType.GAUGE,
                                        group: statGroup)
            check(repoFreeStat)
            statGroup.addToStatistics(repoFreeStat).save()
            repoFreeStat.save()
        }
        repoFreeStatId = repoFreeStat.getId()

        def repoUsedStat = Statistic.findByName(REPOUSED_NAME)
        if (!repoUsedStat) {
            repoUsedStat = new Statistic(name: REPOUSED_NAME,
                                         title: "Diskspace Used by the " +
                                         "Repositories",
                                         type: StatisticType.GAUGE,
                                         group: statGroup)
            check(repoUsedStat)
            statGroup.addToStatistics(repoUsedStat).save()
            repoUsedStat.save()
        }
        repoUsedStatId = repoUsedStat.getId()
        log.info("Successfully created filesystem statistics.")
    }
}
