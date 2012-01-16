/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.admin

import com.collabnet.svnedge.ConcurrentBackupException
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.ServerMode
import com.collabnet.svnedge.domain.User
import com.collabnet.svnedge.event.DumpRepositoryEvent
import com.collabnet.svnedge.console.DumpBean

/**
 * When triggered, the RepoDumpJob will invoke the {@link com.collabnet.svnedge.console.SvnRepoService#createDump}
 * create dump service, with specifics supplied in the trigger JobDataMap
 */
class RepoDumpJob {

    def svnRepoService
    def cloudServicesRemoteClientService

    static def jobName = "com.collabnet.svnedge.admin.RepoDumpJob"
    static def group = "Maintenance"
    def volatility = false

    // scheduled dynamically -- no static triggers
    static triggers = { }

    // the Job execute method
    def execute(context) {
        if (Server.getServer().mode == ServerMode.STANDALONE) {
            dumpOrBackup(context)
        }
    }
    
    private void dumpOrBackup(context) {
        log.info("Executing scheduled RepoDump ...")
        def dataMap = context.getMergedJobDataMap()
        Repository repo = Repository.get(dataMap.get("repoId"))
        DumpBean dumpBean = DumpBean.fromMap(dataMap)
        if (repo && dumpBean) {
            User user = retrieveUserForEvent(dumpBean)
            try {
                if (dumpBean.cloud) {
                    cloudServicesRemoteClientService
                        .synchronizeRepository(repo, dumpBean.userLocale)
                    log.info("Synchronized cloud backup for " + repo.name)
                } else if (dumpBean.hotcopy) {
                    String file = svnRepoService.createHotcopy(dumpBean, repo)
                    log.info("Creating repo hotcopy file: " + file)
                } else {
                    String file = svnRepoService.createDump(dumpBean, repo)
                    log.info("Creating repo dump file: " + file)
                }
                svnRepoService.publishEvent(new DumpRepositoryEvent(this, 
                        dumpBean, repo, true, user, null))
            } catch (ConcurrentBackupException e) {
                log.warn("Backup skipped: " + e.message)
            } catch (Exception e) {
                log.warn("Repository dump failed", e)
                svnRepoService.publishEvent(new DumpRepositoryEvent(this,
                        dumpBean, repo, false, user, e))
            }
        }
        else {
            log.warn("Unable to execute the repo backup")
        }
    }
    
    private User retrieveUserForEvent(dumpBean) {
        User user = null
        if (!dumpBean.isBackup()) {
            try {
                user = dumpBean.userId ? User.get(dumpBean.userId) : null
            } catch (Exception e) {
                log.warn("Error in user lookup", e)
            }
        }
        return user
    }
}

