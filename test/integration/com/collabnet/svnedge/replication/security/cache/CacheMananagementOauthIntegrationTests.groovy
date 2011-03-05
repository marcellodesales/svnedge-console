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

import com.collabnet.svnedge.integration.security.cache.ProxyCache 
import grails.test.*

class CacheMananagementOauthIntegrationTests extends GrailsUnitTestCase {

    def cacheManagementService

    protected void setUp() {
        cacheManagementService.flushAllCache()
    }

    protected void tearDown() {
        cacheManagementService.flushAllCache()
    }

    /**
     * Tests the current state of the resources cache at bootstrap
     */
    void testAuthorizationCacheAtBootStrap() {
        assertEquals("The cache should be empty at bootstrap", 
                     0, cacheManagementService.getAuthorizationCacheSize())
    }

    /**
     * Same Subsequent calls to the resources cache must update the cache
     * only once, but the cache size must maintain the same.
     */
    void testAuthorizationNotUpdatingTheCache() {
        // TODO CTF REPLICA 'www' and 'view' are random strings; this needs
        // more work
        def type = "view"
        def usr = "marcello"
        def path = "www"
        def value1 = cacheManagementService.authorizeUser(username:usr,
            repoPath:path, accessType:type)
        println "1st call: $usr/$path/$type: $value1"
        assertNotNull("The response should not be null", value1)
        assertTrue("The response of getRolePaths() must be of String" +
                   "array type", value1 instanceof String)
        assertTrue("The cache must be updated after the call to " +
                   "getRolePaths() for a new key",
                   1 == cacheManagementService.getAuthorizationCacheSize())
        def oauthCache = cacheManagementService.getCurrentOauthCache()
        def key1 = ProxyCache.newCTFOauthKey(usr, path, type) 
        assertTrue("The cache must contain the key", 
                oauthCache.containsKey(key1))

        def value2 = cacheManagementService.authorizeUser(username:usr,
            repoPath:path, accessType:type)
        println "2st call: Action for $usr/$path/$type: $value2"
        assertNotNull("The response should not be null", value2)
        assertTrue("The response of getRolePaths() must be of String" +
                   " type", value2 instanceof String)
        assertEquals("Subsequent calls with the same input should have the " +
                     "same result (assuming no changes on server-side", value1,
                     value2)
        assertTrue("The cache must NOT be updated after the call to " +
                   "getRolePaths() for existing usr/pwd key-pair",
                   1 == cacheManagementService.getAuthorizationCacheSize())
        oauthCache = cacheManagementService.getCurrentOauthCache()
        def key2 = ProxyCache.newCTFOauthKey(usr, path, type) 
        assertTrue("The cache must contain the key", 
                oauthCache.containsKey(key2))
    }

    /**
     * Different Subsequent calls to the resources cache must update the cache
     * and the cache size must be incremented for each call.
     */
    void testAuthorizationUpdatingTheCache() {
        // TODO CTF REPLICA 'www' and 'view' are random strings; this needs
        // more work
        def type = "view"
        def usr = "marcello"
        def path = "www"
        def value1 = cacheManagementService.authorizeUser(username:usr,
            repoPath:path, accessType:type)
        println "Action for $usr/$path/$type: $value1"
        assertNotNull("The response should not be null", value1)
        assertTrue("The response of getRolePaths() must be of String" +
                   " type", value1 instanceof String)
        assertTrue("The cache must be updated after the call to " +
                   "getRolePaths() for a new key",
                   1 == cacheManagementService.getAuthorizationCacheSize())
        def oauthCache = cacheManagementService.getCurrentOauthCache()
        def key1 = ProxyCache.newCTFOauthKey(usr, path, type) 
        assertTrue("The cache must contain the key", 
                oauthCache.containsKey(key1))
                
        def usr2 = "adam"
        def value2 = cacheManagementService.authorizeUser(username:usr2,
            repoPath:path, accessType:type)
        println "Action for $usr2/$path/%type: $value2"
        assertNotNull("The response should not be null", value2)
        assertTrue("The response of getRolePaths() must be of String" +
                   " type", value2 instanceof String)
        assertTrue("The cache must NOT be updated after the call to " +
                   "getRolePaths() for existing usr/pwd key-pair",
                   2 == cacheManagementService.getAuthorizationCacheSize())
        oauthCache = cacheManagementService.getCurrentOauthCache()
        def key2 = ProxyCache.newCTFOauthKey(usr2, path, type) 
        assertTrue("The cache must contain the key", 
                oauthCache.containsKey(key2))
    }
}
