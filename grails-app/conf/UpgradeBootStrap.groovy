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

import com.collabnet.svnedge.console.DumpBean
import com.collabnet.svnedge.admin.LogManagementService.ApacheLogLevel
import com.collabnet.svnedge.admin.LogManagementService.ConsoleLogLevel
import com.collabnet.svnedge.admin.RepoDumpJob
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.SchemaVersion
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.ServerMode
import com.collabnet.svnedge.domain.statistics.Statistic
import com.collabnet.svnedge.util.ConfigUtil

/**
 * Bootstrap script for handling any special conditions associated with upgrades
 */
class UpgradeBootStrap {

    def operatingSystemService
    def fileSystemStatisticsService
    def dataSource
    def jobsAdminService
    def lifecycleService
    def serverConfService
    def svnRepoService

    def init = { servletContext ->
        log.info("Applying updates")
        release1_1_0()
        release1_2_0()
        release1_3_1()
        release2_1_0()
        release3_0_2()
    }

    private boolean isSchemaCurrent(int major, int minor, int revision) {

        def v = SchemaVersion.createCriteria()
        def resultCount = v.get {
            and {
                eq("major", major)
                eq("minor", minor)
                eq("revision", revision)
            }
            projections {
                rowCount()
            }
        }
        return (resultCount > 0)
    }

    private def release1_1_0() {

        if (isSchemaCurrent(1, 1, 0)) {
            // result found at version, assume this is applied
            return
        }

        log.info("Applying 1.1.0 updates")

        def server = Server.getServer()
        if (server) {
            log.info("Initializing new fields on Server instance")
            server.mode = ServerMode.STANDALONE
            server.consoleLogLevel = ConsoleLogLevel.WARN
            server.apacheLogLevel = ApacheLogLevel.WARN
            server.save()
        }

        SchemaVersion v = new SchemaVersion(major: 1, minor: 1, revision: 0,
                description: "1.1.0 added Server fields: mode, consoleLogLevel, " +
                        "apacheLogLevel")
        v.save()
    }

    def void release1_2_0() {

        if (isSchemaCurrent(1, 2, 0)) {
            return
        }

        // the current changes necessary are only for windows.
        if (!operatingSystemService.isWindows()) {
            return
        }

        log.info("Applying 1.2.0 updates")

        Statistic.executeUpdate("UPDATE Statistic s SET s.name='BytesIn' " +
                "WHERE s.name='WinBytesIn'")
        Statistic.executeUpdate("UPDATE Statistic s SET " +
                "s.name='BytesOut' WHERE s.name='WinBytesOut'")

        SchemaVersion v = new SchemaVersion(major: 1, minor: 2, revision: 0,
                description: "1.2.0 updated Statistic values: name. " +
                        "(WinBytesIn -> BytesIn), (WinBytesOut -> BytesOut).")
        v.save()
    }

    def void release1_3_1() {

        if (isSchemaCurrent(1, 3, 1)) {
            return
        }

        log.info("Applying 1.3.1 updates")

        Server.executeUpdate("UPDATE Server s SET s.ldapEnabledConsole = " +
                "s.ldapEnabled")

        SchemaVersion v = new SchemaVersion(major: 1, minor: 3, revision: 1,
                description: "1.3.1 updated Server adding field " +
                        "'ldapEnabledConsole'.")
        v.save()
    }

    def void release2_1_0() {
        if (isSchemaCurrent(2, 1, 0)) {
            return
        }
        log.info("Applying 2.1.0 updates")

        // initialize new useHttpV2 field; 
        // if we are a replica and have replicated repos, the local HttpV2 usage needs to be 
        // synced with the master
        Server s = Server.getServer()
        if (s) {
            File dumpDir = new File(ConfigUtil.dumpDirPath())
            if (!dumpDir.exists()) {
                dumpDir.mkdir()
            }

            s.useHttpV2 = true
            s.dumpDir = ConfigUtil.dumpDirPath()
            s.save(flush: true)

            if (serverConfService.syncReplicaConfigurationWithMaster()) {
                lifecycleService.gracefulRestartServer()
            }
        }

        SchemaVersion v = new SchemaVersion(major: 2, minor: 1, revision: 0,
                description: "2.1.0 added Quartz tables and data; initialized Server.useHttpV2 and Server.dumpDir fields")

        v.save()

    }

    def void release3_0_2() {
        if (isSchemaCurrent(3, 0, 2)) {
            return
        }
        log.info("Applying 3.0.2 updates")

        def triggers = jobsAdminService
                .getTriggers(RepoDumpJob.jobName, RepoDumpJob.group)
        for (trigger in triggers) {
            if (trigger.group == "Backup") {
                String oldName = trigger.name
                try {
                    def dataMap = trigger.jobDataMap
                    Repository repo = Repository.get(dataMap.get("repoId"))
                    DumpBean bean = DumpBean.fromMap(dataMap)
                    if (repo && bean) {
                        String newName = 
                                svnRepoService.generateTriggerName(repo, bean)
                        if (oldName != newName) {
                            jobsAdminService
                                    .removeTrigger(oldName, trigger.group)
                            trigger.name = newName
                            dataMap.id = newName
                            jobsAdminService.scheduleTrigger(trigger)
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to update trigger name for '" + oldName +
                            "', so it may show up in Schedule Backup existing" +
                            "jobs, but not in the Scheduled Jobs of the " +
                            "individual repository.  To correct this, delete" +
                            " the backup job from Schedule Backup screen.", e)
                }
            }
        }

        SchemaVersion v = new SchemaVersion(major: 3, minor: 0, revision: 2,
                description: "3.0.2 Renamed quartz triggers for backup jobs")
        v.save()
    }
}
