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
package com.collabnet.svnedge.replica.cache.auth

import com.collabnet.svnedge.replica.cache.*
import com.collabnet.svnedge.replication.auth.cache.ProxyCache 

import grails.test.*

/**
 * @author mdesales
 *
 */
public class CTFAuthenticationProxyCacheTests extends GrailsUnitTestCase {

    ProxyCache authCache

    protected void setUp() {
        authCache = new ProxyCache()
    }

    protected void tearDown() {
        authCache.clear()
    }

    void testAuthenticationCacheCreation() {
        assertNotNull("The cache must be created with no parameters", 
                authCache)
        assertTrue("The cache must be empty when first created", 
                authCache.size() == 0)
    }
    
    /**
     * CTF keys are username and password
     * CTF values are in the range of [0..2]
     */
    void testAddValidKeyValuePairs() {
        def key1 = ProxyCache.newAuthKey("marcello", "1234")
        def value1 = ProxyCache.newValueExpiresInSeconds(30, 0)
        assertNotNull(key1)
        assertNotNull(value1)
        authCache.put(key1, value1)
        assertTrue("The cache must contain the key $key1", 
                authCache.containsKey(key1))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key1))
        assertTrue("The cache must contain the username marcello", 
                authCache.containsUsername("marcello"))
        assertEquals("The size of the cache must be incremented after adding", 
                1, authCache.size())
        def retrievedValue1 = authCache.get(key1)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value1, retrievedValue1)
        assertEquals("The value of the cache must be the same used at the " +
                 "factory method", 0, retrievedValue1.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue1.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())
                
        def key2 = ProxyCache.newAuthKey("adam", "1234")
        def value2 = ProxyCache.newValueExpiresInSeconds(44, 1)
        assertNotNull(key2)
        assertNotNull(value2)
        authCache.put(key2, value2)
        assertTrue("The cache must contain the key $key2", 
                authCache.containsKey(key2))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key2))
        assertTrue("The cache must contain the username adam", 
                authCache.containsUsername("adam"))
        assertEquals("The size of the cache must be incremented after adding", 
                2, authCache.size())
        def retrievedValue2 = authCache.get(key2)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value2, retrievedValue2)
        assertEquals("The value of the cache must be the same used at the " +
                 "factory method", 1, retrievedValue2.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue2.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue2.hasExpired())
                 
        def key3 = ProxyCache.newAuthKey("nora", "abcd")
        def value3 = ProxyCache.newValueExpiresInMinutes(5, 2)
        assertNotNull(key3)
        assertNotNull(value3)
        authCache.put(key3, value3)
        assertTrue("The cache must contain the key $key3", 
                authCache.containsKey(key3))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key3))
        assertTrue("The cache must contain the username nora", 
                authCache.containsUsername("nora"))
        assertEquals("The size of the cache must be incremented after adding", 
                3, authCache.size())
        def retrievedValue3 = authCache.get(key3)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value3, retrievedValue3)
        assertEquals("The value of the cache must be the same used at the " +
                "factory method", 2, retrievedValue3.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue3.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue3.hasExpired())
    }

    void testAddRepeatedKeyValuePairs() {
        def key3 = ProxyCache.newAuthKey("nora", "abcd")
        def value3 = ProxyCache.newValueExpiresInMinutes(5, 1)
        assertNotNull(key3)
        assertNotNull(value3)
        authCache.put(key3, value3)
        authCache.put(key3, value3)
        assertTrue("The cache must contain the key $key3", 
                authCache.containsKey(key3))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key3))
        assertTrue("The cache must contain the username nora", 
                authCache.containsUsername("nora"))
        assertEquals("The size of the cache must be incremented only once " + 
                "when adding the same object multiple times", 
                1, authCache.size())
    }

    void testUpdateKeyValuePair() {
        def key1 = ProxyCache.newAuthKey("marcello", "1234")
        def value1 = ProxyCache.newValueExpiresInMinutes(5, 2)
        assertNotNull(key1)
        assertNotNull(value1)
        authCache.put(key1, value1)
        assertTrue("The cache must contain the key $key1", 
                authCache.containsKey(key1))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key1))
        def retrievedValue1 = authCache.get(key1)
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value1, retrievedValue1)
        assertEquals("The value of the cache must be the same used at the " +
                "factory method", 2, retrievedValue1.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue1.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())

        def value2 = ProxyCache.newValueExpiresInSeconds(13, 1)
        authCache.put(key1, value2)
        def retrievedValue2 = authCache.get(key1)
        assertTrue("The cache must contain the key $key1", 
                authCache.containsKey(key1))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key1))
        assertEquals("The retrieved value must be the same when retrieving " +
                "values with the same key", value2, retrievedValue2)
        assertFalse("The retrieved value must be different when updating " +
                "values with the same key", value1 == value2)
        assertEquals("The value of the cache must be the same used at the " +
                "factory method", 1, retrievedValue2.responseValue)
        assertNotNull("The value expiration must have been created " +
                "period of time", retrievedValue2.expiresOn)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())
        assertEquals("The size of the cache must be incremented only once " + 
                "when updating the same key multiple times", 
                1, authCache.size())
    }

    void testValueExpiration() {
        def key1 = ProxyCache.newAuthKey("marcello", "1234")
        def value1 = ProxyCache.newValueExpiresInSeconds(1, 0)
        assertNotNull(key1)
        assertNotNull(value1)
        authCache.put(key1, value1)
        assertTrue("The cache must contain the key $key1", 
                authCache.containsKey(key1))
        assertFalse("The cache key must not be expired", 
                authCache.hasKeyValueExpired(key1))
        def retrievedValue1 = authCache.get(key1)
        assertFalse("The value of the cache must not be expired for short " +
                "period of time", retrievedValue1.hasExpired())
        Thread.sleep(1022)
        retrievedValue1 = authCache.get(key1)
        assertTrue("The value of the cache must be expired after it has " +
                "elapsed", retrievedValue1.hasExpired())
    }
}
