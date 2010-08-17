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
package com.collabnet.svnedge.replica.cache.oauth

import com.collabnet.svnedge.replica.cache.*

import grails.test.*

/**
 * @author mdesales
 *
 */
public class CTFAuthorizationProxyCacheTests extends GrailsUnitTestCase {

    ProxyCache oauthCache

    protected void setUp() {
        oauthCache = new ProxyCache()
    }

    protected void tearDown() {
        oauthCache.clear()
    }

    void testAuthenticationCacheCreation() {
        assertNotNull("The cache must be created with no parameters", 
                oauthCache)
        assertTrue("The cache must be empty when first created", 
                oauthCache.size() == 0)
        assertFalse(oauthCache.containsUsername("non-existent"))
    }

    void testCTFAddValidKeyValuePairs() {
        def key1 = ProxyCache.newCTFOauthKey("marcello", "integration/", "view")
        def value1 = ProxyCache.newValueExpiresInSeconds(30, ["", "/a.b"])
        assertNotNull(key1)
        assertNotNull(value1)
        oauthCache.put(key1, value1)
        assertTrue("The cache must contain the username marcello", 
                oauthCache.containsUsername("marcello"))
        assertFalse(oauthCache.containsUsername("marcello-desales"))
        assertFalse(oauthCache.containsUsername("marc"))
        assertEquals("The size of the cache must be incremented after adding", 
                1, oauthCache.size())
        def retrievedValue1 = oauthCache.get(key1)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value1, retrievedValue1)
        assertEquals("The value of the cache must be the same used at the " +
                 "factory method", ["", "/a.b"], retrievedValue1.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue1.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())

        def key2 = ProxyCache.newCTFOauthKey("adam", "integration/", "view")
        def value2 = ProxyCache.newValueExpiresInSeconds(44, ["", "/a.b"])
        assertNotNull(key2)
        assertNotNull(value2)
        oauthCache.put(key2, value2)
        assertTrue("The cache must contain the username adam", 
                oauthCache.containsUsername("adam"))
        assertFalse(oauthCache.containsUsername("adam-ambrose"))
        assertFalse(oauthCache.containsUsername("ada"))
        assertEquals("The size of the cache must be incremented after adding", 
                2, oauthCache.size())
        def retrievedValue2 = oauthCache.get(key2)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value2, retrievedValue2)
        assertEquals("The value of the cache must be the same used at the " +
                 "factory method", ["", "/a.b"], retrievedValue2.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue2.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue2.hasExpired())
                 
        def key3 = ProxyCache.newCTFOauthKey("nora", "other/", "")
        def value3 = ProxyCache.newValueExpiresInMinutes(5, [])
        assertNotNull(key3)
        assertNotNull(value3)
        oauthCache.put(key3, value3)
        assertTrue("The cache must contain the username nora", 
                oauthCache.containsUsername("nora"))
        assertFalse(oauthCache.containsUsername("norah-jones"))
        assertFalse(oauthCache.containsUsername("nor"))
        assertEquals("The size of the cache must be incremented after adding", 
                3, oauthCache.size())
        def retrievedValue3 = oauthCache.get(key3)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value3, retrievedValue3)
        assertEquals("The value of the cache must be the same used at the " +
                "factory method", [], retrievedValue3.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue3.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue3.hasExpired())
    }

    void testAddRepeatedKeyValuePairs() {
        def key3 = ProxyCache.newCTFOauthKey("marcello", "integration/", "view")
        def value3 = ProxyCache.newValueExpiresInMinutes(5, false)
        assertNotNull(key3)
        assertNotNull(value3)
        oauthCache.put(key3, value3)
        oauthCache.put(key3, value3)
        assertTrue("The cache must contain the username nora", 
                oauthCache.containsUsername("marcello"))
        assertEquals("The size of the cache must be incremented only once " + 
                "when adding the same object multiple times", 
                1, oauthCache.size())
    }
    
    void testUpdateKeyValuePair() {
        def key1 = ProxyCache.newCTFOauthKey("marcello", "integration/", "view")
        def value1 = ProxyCache.newValueExpiresInMinutes(5, ["1"])
        assertNotNull(key1)
        assertNotNull(value1)
        oauthCache.put(key1, value1)
        
        def retrievedValue1 = oauthCache.get(key1)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value1, retrievedValue1)
        assertEquals("The value of the cache must be the same used at the " +
                "factory method", ["1"], retrievedValue1.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue1.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())

        def value2 = ProxyCache.newValueExpiresInSeconds(13, ["2"])
        oauthCache.put(key1, value2)
        def retrievedValue2 = oauthCache.get(key1)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value2, retrievedValue2)
        assertFalse("The retrieved value must be different when updating " +
                "values with the same key", value1 == value2)
        assertEquals("The value of the cache must be the same used at the " +
                "factory method", ["2"], retrievedValue2.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue2.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())
        assertEquals("The size of the cache must be incremented only once " + 
                "when updating the same key multiple times", 
                1, oauthCache.size())
    }

    void testCreateKeysWithIncorrectParameters() {
        try {
            def key1 = ProxyCache.newCTFOauthKey("", "", "")
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def key1 = ProxyCache.newCTFOauthKey("", "", "view")
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def key1 = ProxyCache.newCTFOauthKey(null, null, "view")
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
    }

    void testCreateValuesWithIncorrectParameters() {
        try {
            def value1 = ProxyCache.newValueExpiresInSeconds(-1, false)
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def value1 = ProxyCache.newValueExpiresInSeconds(0, false)
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def value1 = ProxyCache.newValueExpiresInSeconds(null, false)
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def value1 = ProxyCache.newValueExpiresInMinutes(-1, false)
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def value1 = ProxyCache.newValueExpiresInMinutes(0, false)
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
        try {
            def value1 = ProxyCache.newValueExpiresInMinutes(null, false)
            fail("The CEE authorization key must have the action, " +
                    "username and project name as parameters.")
        } catch (Exception e) {
            assertNotNull(e)
        }
    }

    void testValueExpiration() {
        def key1 = ProxyCache.newCTFOauthKey("marcello", "integration/", "view")
        def value1 = ProxyCache.newValueExpiresInSeconds(1, ["22"])
        assertNotNull(key1)
        assertNotNull(value1)
        oauthCache.put(key1, value1)
        def retrievedValue1 = oauthCache.get(key1)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())
        Thread.sleep(1022)
        retrievedValue1 = oauthCache.get(key1)
        assertTrue("The value of the cache must be expired after it has " +
                "elapsed", retrievedValue1.hasExpired())
    }
}
