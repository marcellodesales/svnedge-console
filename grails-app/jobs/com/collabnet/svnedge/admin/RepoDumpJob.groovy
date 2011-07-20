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

import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.console.DumpBean

/**
 * When triggered, the RepoDumpJob will invoke the {@link com.collabnet.svnedge.console.SvnRepoService#createDump}
 * create dump service, with specifics supplied in the trigger JobDataMap
 */
class RepoDumpJob {

    def svnRepoService

    static def jobName = "com.collabnet.svnedge.admin.RepoDumpJob"
    static def group = "Maintenance"
    def volatility = false

    // scheduled dynamically -- no static triggers
    static triggers = { }

    // the Job execute method
    def execute(context) {
        log.info("Executing scheduled RepoDump ...")
        def dataMap = context.getMergedJobDataMap()
        Repository repo = Repository.get(dataMap.get("repoId"))
        DumpBean dumpBean = DumpBean.fromMap(dataMap)
        if (repo && dumpBean) {
            String file = svnRepoService.createDump(dumpBean, repo)
            log.info("Creating repo dump file: " + file)
            return
        }
        else {
            log.warn("Unable to execute the repo dump")
        }
    }
}
