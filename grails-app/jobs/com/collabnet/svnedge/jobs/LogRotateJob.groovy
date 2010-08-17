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
package com.collabnet.svnedge.jobs

import java.util.Date
import java.util.Calendar
import com.collabnet.svnedge.console.Server

class LogRotateJob {

    def lifecycleService
    def serverConfService
    static def name = "com.collabnet.svnedge.jobs.LogRotateJob"
    static def group = "Maintenance"

    static triggers = { 
        cron name: "LogRotateTrigger", group: group + "_Triggers", \
        startDelay: 60000, \
        cronExpression: "0 5 0 * * ?"
    }

    def pruneLog(olderThanToday) {
        def dataDir = lifecycleService.dataDirPath
        def dataDirObj = new File(dataDir, "logs")
        Date today = new Date()
        Date oldday = new Date(today.getTime() - olderThanToday*1440*60000)
        long oldDayTimeStamp = oldday.getTime()
        String[] entries = dataDirObj.listFiles()
        for (int i = 0; i < entries.length; i++) {
            String entry = entries[i]
            def entryObj = new File(entry)
            long lastModified = entryObj.lastModified()
            if (lastModified < oldDayTimeStamp) {
                if (entryObj.delete() == false) {
                    log.info("Pruning: ${entry} failed.")
                }
            }
        }
    }

    def execute() {
        serverConfService.writeLogConf()
        lifecycleService.gracefulRestartServer()
        def server = Server.getServer()
        if (server.pruneLogsOlderThan != 0) {
            pruneLog(server.pruneLogsOlderThan)
        }
    }
}
