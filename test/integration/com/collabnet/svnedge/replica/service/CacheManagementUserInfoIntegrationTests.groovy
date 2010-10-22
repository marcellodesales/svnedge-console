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
package com.collabnet.svnedge.replica.service

import java.util.Locale;

import grails.test.*

import com.collabnet.svnedge.replica.cache.ProxyCache
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class CacheManagementUserInfoIntegrationTests extends GrailsUnitTestCase {

    def cacheManagementService
    def ctfRemoteClientService
    def config = ConfigurationHolder.config

    def TEST_USERNAME = "mdesales"
    def TEST_PASSWORD = "Coll@b123"

    protected void setUp() {
        cacheManagementService.flushAllCache()

        try {
            def ctfProto = config.svnedge.ctfMaster.ssl ? "https://" : "http://"
            def ctfHost = config.svnedge.ctfMaster.domainName
            def ctfPort = config.svnedge.ctfMaster.port == "80" ? "" : ":" +
                    config.svnedge.ctfMaster.port
            def ctfUrl = ctfProto + ctfHost + ctfPort
            def adminUsername = config.svnedge.ctfMaster.username
            def adminPassword = config.svnedge.ctfMaster.password

            def adminSessionId = ctfRemoteClientService.login(ctfUrl, 
                adminUsername, adminPassword, Locale.getDefault())
            ctfRemoteClientService.createUser(ctfUrl, adminSessionId, 
                TEST_USERNAME, TEST_PASSWORD, "mdesales@collab.net", 
                "Marcello de Sales", true, false)
        } catch (Exception e) {
            println(e.getMessage())
            // do not do anything if the user already exists.
        }
    }

    protected void tearDown() {
        cacheManagementService.flushAllCache()
    }

    /**
     * Tests the current state of the cache at bootstrap
     */
    void testUserInfoCacheStateAtBootStrap() {
        assertEquals("The user info cache should be empty at bootstrap", 
                     0, cacheManagementService.getUserInfoCacheSize())
    }

    /**
     * Tests getting a valid user's info from cache.
     */
    void testGetUserInfoCache() {
        def username = TEST_USERNAME
        def password = TEST_PASSWORD
        def realName = "Marcello de Sales"
        cacheManagementService.authenticateUser(username, password)
        def cacheKey = ProxyCache.newSimpleKey(username)
        def userInfo = cacheManagementService.getUserInfo(username)
        assertNotNull("The userInfo for a valid user should not be null.", 
                      userInfo)
        assertEquals("The user info cache should contain one element", 
                     1, cacheManagementService.getUserInfoCacheSize())
        assertEquals("The real name for $username should be $realName", 
                     realName, userInfo.realUserName)
        def firstCacheValue = cacheManagementService.userInfoCache[cacheKey]
        cacheManagementService.getUserInfo(username)
        assertEquals("The user info cache should still contain one element", 
                     1, cacheManagementService.getUserInfoCacheSize())
        def secondCacheValue = cacheManagementService.userInfoCache[cacheKey]
        assertEquals("Both cache values should be the same.", firstCacheValue,
                     secondCacheValue)
        // force expiration
        firstCacheValue.expiresOn = System.currentTimeMillis() - 1
        cacheManagementService.userInfoCache[cacheKey] = firstCacheValue
        cacheManagementService.getUserInfo(username)
        assertEquals("The user info cache should still contain one element", 
                     1, cacheManagementService.getUserInfoCacheSize())
        def thirdCacheValue = cacheManagementService.userInfoCache[cacheKey]
        assertTrue("The cache value after forced expiration should be " +
                       "different", !firstCacheValue.equals(thirdCacheValue))
    }

    /**
     * Tests flushing expired user info cache value.
     */
    void testFlushExpiredUserInfoCache() {
        def username = TEST_USERNAME
        def password = TEST_PASSWORD
        def cacheKey = ProxyCache.newSimpleKey(username)
        cacheManagementService.authenticateUser(username, password)
        cacheManagementService.getUserInfo(username)
        assertEquals("The user info cache should contain one element", 
                     1, cacheManagementService.getUserInfoCacheSize())
        def cacheValue = cacheManagementService.userInfoCache[cacheKey]
        cacheValue.expiresOn = System.currentTimeMillis() - 1
        cacheManagementService.userInfoCache[cacheKey] = cacheValue
        assertEquals("The user info cache should still contain one element", 
                     1, cacheManagementService.getUserInfoCacheSize())
        cacheManagementService.flushExpiredCacheEntries()
        assertEquals("The user info cache should be empty after flush.", 
                     0, cacheManagementService.getUserInfoCacheSize())
    }
}
