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
package com.collabnet.svnedge.replication


import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.collabnet.svnedge.console.JobsAdminService

/**
 * The Cache Flusher job will run everyday at 00:00. 
 * It will clean the entire cache.
 */
class CacheFlusherJob {
    static def group = JobsAdminService.REPLICA_GROUP

    static triggers = { 
    /*
        simple name: "cacheFlusherJobTrigger", group: group + "_Triggers", \
        startDelay: 9880, \
        repeatInterval: \
        ConfigurationHolder.config.svnedge.replica.cache.cacheFlushPeriod * 60000
    */
    }

    // avoid re-entrance in case jobs are delayed. This will prevent multiple
    // calls to the Master.
    def concurrent = false
    
    // svnsync the replica repositories
    def cacheManagementService

    def execute() {
        log.info("Flushing all authentication and authorization cache entries")
        log.info("Current Authentication Cache size: " + 
                cacheManagementService.getAuthenticationCacheSize())
        log.info("Current Authorization Cache size: " + 
                cacheManagementService.getAuthorizationCacheSize())
        
        cacheManagementService.flushAllCache()
        log.info("Cache empty")
    }
}
