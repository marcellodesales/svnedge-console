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
package com.collabnet.svnedge.replication.job

import grails.test.*

class CacheFlusherJobTests extends GrailsUnitTestCase {

    def cacheFlusherJob

    def cacheManagementService

    def grailsApplication

    protected void setUp() {
        cacheFlusherJob = grailsApplication.mainContext.getBean(
                "com.collabnet.svnedge.integration.CacheFlusherJob")
        cacheManagementService.flushAllCache() 
    }

    void testFlushOfAllUserCacheExecution() {
        cacheManagementService.authenticateUser(username:"marcello", 
                password:"1234")
        cacheManagementService.authenticateUser(username:"wrong", 
                password:"incorrect")

        // TODO CTF REPLICA 'www' and 'view' are random strings; this
        // needs work 
        cacheManagementService.authorizeUser(username:"marcello",
                accessType:"view", repoPath:"www")
        cacheManagementService.authorizeUser(username:"no-user",
                accessType:"no-Action", repoPath:"www")

        assertTrue("The cache must be updated after the call to the " +
                   "authentication method for the current Master. Expected " +
                   "is 2 but got " + 
                   cacheManagementService.getAuthenticationCacheSize(), 
                   2 == cacheManagementService.getAuthenticationCacheSize())

        assertTrue("The cache must be updated after the call to " +
                   "authorization method for the current Master. Expected " + 
                   "is 2 but got " + 
                   cacheManagementService.getAuthorizationCacheSize(),
                   2 == cacheManagementService.getAuthorizationCacheSize())

        cacheFlusherJob.execute()

        assertEquals("The authentication cache must have been flushed after " +
                   "the flusher job execution", 
                   0, cacheManagementService.getAuthenticationCacheSize())
        assertEquals("The authorization cache must have been flushed after " +
                   "the flusher job execution",
                   0, cacheManagementService.getAuthorizationCacheSize())
    }
}
