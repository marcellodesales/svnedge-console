/*
 * CollabNet Subversion Edge
 * Copyright (C) 2012, CollabNet Inc. All rights reserved.
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

import com.collabnet.svnedge.console.BackgroundJobUtil
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.event.VerifyRepositoryEvent

/**
 * When triggered, this job will verify the repo indicated in the context.
 */
class RepoVerifyJob {

    def svnRepoService
    def jobsInfoService

    static def jobName = "com.collabnet.svnedge.admin.RepoVerifyJob"
    static def group = "Maintenance"
    def volatility = false

    // scheduled dynamically -- no static triggers
    static triggers = { }

    // the Job execute method
    def execute(context) {
        def dataMap = context.getMergedJobDataMap()
        jobsInfoService
                .queueJob(verifyRunnable(dataMap), context.scheduledFireTime)
       
    }
    
    def verifyRunnable = {dataMap ->
        return [dataMap: dataMap,
                run: {
                    Repository repo = Repository.get(dataMap.get("repoId"))
                    def progressLog = BackgroundJobUtil
                            .prepareProgressLogFile(repo.name, BackgroundJobUtil.JobType.VERIFY)
                    log.info("Verifying repo '${repo.name}'")
                                
                    boolean result = false
                    Exception e = null
                    try {
                        result = svnRepoService
                                .verifyRepositoryPath(svnRepoService.getRepositoryHomePath(repo),
                                        new FileOutputStream(progressLog))
                        log.info("Finished verification of repo '${repo.name}' with result: ${result}")
                        // delete log on success
                        if (result) {
                            progressLog.delete()
                        }
                    }
                    catch (Exception ex) {
                        log.warn ("Caught exception verifying repo '${repo.name}': ${ex.message}", ex)
                        e = ex
                    }
                    // publish event
                    svnRepoService.publishEvent(new VerifyRepositoryEvent(this, repo,
                            (result ? VerifyRepositoryEvent.SUCCESS : VerifyRepositoryEvent.FAILED),
                            dataMap['userId'], dataMap['locale'], progressLog, e))
                }
        ]
    } 

    

}


