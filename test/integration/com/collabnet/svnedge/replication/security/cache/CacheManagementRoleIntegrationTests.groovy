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
package com.collabnet.svnedge.replication.security.cache

import grails.test.*

import com.collabnet.svnedge.domain.integration.CtfServer;
import com.collabnet.svnedge.replication.security.cache.ProxyCache 


class CacheManagementRoleIntegrationTests extends GrailsUnitTestCase {

    def cacheManagementService
    def grailsApplication
    def config

    protected void setUp() {
        this.config = grailsApplication.config
        cacheManagementService.flushAllCache()
        def ctfProto = config.svnedge.ctfMaster.ssl ? "https://" : "http://"
        def ctfHost = config.svnedge.ctfMaster.domainName
        def ctfPort = config.svnedge.ctfMaster.port == "80" ? "" : ":" +
                config.svnedge.ctfMaster.port
        def ctfUrl = ctfProto + ctfHost + ctfPort
        def adminUsername = config.svnedge.ctfMaster.username
        def adminPassword = config.svnedge.ctfMaster.password

        if (!CtfServer.getServer()) {
            CtfServer s = new CtfServer(baseUrl: ctfUrl, mySystemId: "exsy1000",
                    internalApiKey: "testApiKey",
                    ctfUsername: adminUsername,
                    ctfPassword: adminPassword)
            s.save(flush:true)
        }
    }

    protected void tearDown() {
        cacheManagementService.flushAllCache()
    }

    /**
     * Tests the current state of the cache at bootstrap
     */
    void testRoleCacheStateAtBootStrap() {
        assertEquals("The role cache should be empty at bootstrap", 
                     0, cacheManagementService.getRoleCacheSize())
    }

    /**
     * Tests getting a user's role from the cache.
     */
    void testRoleCache() {
        def username = config.svnedge.ctfMaster.username
        def password = config.svnedge.ctfMaster.password
        def user_roles = ["ROLE_USER", "ROLE_ADMIN"]
        def cacheKey = ProxyCache.newSimpleKey(username)

        cacheManagementService.authenticateUser(username, password)
        def roles = cacheManagementService.getUserRoles(username)
        assertNotNull("The roles for a valid user should not be null.", 
                      roles)
        assertEquals("The role cache should contain one element", 
                     1, cacheManagementService.getRoleCacheSize())
        for (role in user_roles) {
            assertTrue("The roles should contain " + role, 
                       arrayContains(role, roles))
        }
        def firstCacheValue = cacheManagementService.roleCache[cacheKey]
        cacheManagementService.getUserRoles(username)
        assertEquals("The role cache should still contain one element", 
                     1, cacheManagementService.getRoleCacheSize())
        def secondCacheValue = cacheManagementService.roleCache[cacheKey]
        assertEquals("Both cache values should be the same.", firstCacheValue,
                     secondCacheValue)
        // force expiration
        firstCacheValue.expiresOn = System.currentTimeMillis() - 1
        cacheManagementService.roleCache[cacheKey] = firstCacheValue
        cacheManagementService.getUserRoles(username)
        assertEquals("The role cache should still contain one element", 
                     1, cacheManagementService.getRoleCacheSize())
        def thirdCacheValue = cacheManagementService.roleCache[cacheKey]
        assertTrue("The cache value after forced expiration should be " +
                       "different", !firstCacheValue.equals(thirdCacheValue))
    }

    /**
     * Tests flushing expired role cache value.
     */
    void testFlushExpiredRoleCache() {
        def username = "admin"
        def cacheKey = ProxyCache.newSimpleKey(username)
        cacheManagementService.authenticateUser("admin", "collab123")
        cacheManagementService.getUserRoles(username)
        assertEquals("The role cache should contain one element", 
                     1, cacheManagementService.getRoleCacheSize())
        def cacheValue = cacheManagementService.roleCache[cacheKey]
        cacheValue.expiresOn = System.currentTimeMillis() - 1
        cacheManagementService.roleCache[cacheKey] = cacheValue
        assertEquals("The role cache should still contain one element", 
                     1, cacheManagementService.getRoleCacheSize())
        cacheManagementService.flushExpiredCacheEntries()
        assertEquals("The role cache should be empty after flush.", 
                     0, cacheManagementService.getRoleCacheSize())
    }

    /**
     * Helper method for determining whether a string array contains a string.
     */
    boolean arrayContains(String str, String[] arr) {
        for (value in arr) {
            if (value.equals(str)) {
                return true
            }
        }
        return false
        
    }
}
