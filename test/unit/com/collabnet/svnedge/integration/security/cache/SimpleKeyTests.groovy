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
package com.collabnet.svnedge.integration.security.cache

import com.collabnet.svnedge.integration.security.cache.SimpleCacheKey 
import grails.test.*

class SimpleKeyTests extends GrailsUnitTestCase {

    void testKeyCreationWithCorrectParameter() {
        def username = "username"
        try {
            def key1 = SimpleCacheKey.newInstance(username)
            assertEquals(username, key1.getUsername())
        } catch (Exception e) {
            fail("No exception should be thrown after creating a cache key " +
                     "with correct parameters")
        }
    }

    void testKeyCreationWithNullParameter() {
        def username = null
        try {
            def key1 = SimpleCacheKey.newInstance(username)
            fail("A cache key should not be created with null values")
        } catch (Exception e) {
            assertNotNull("The IllegalArgumetException must be thrown when " +
                     "not providing parameters to create a cache key", e)
            assertTrue(e instanceof IllegalArgumentException)
        }
    }

    void testKeyCreationWithEmptyParameter() {
        def username = ""
        try {
            def key1 = SimpleCacheKey.newInstance(username)
            fail("A cache key should not be created with empty values")
        } catch (Exception e) {
            assertNotNull("The IllegalArgumetException must be thrown when " +
                              "providing empty parameters to create a cache " +
                              "key", e)
            assertTrue(e instanceof IllegalArgumentException)
        }
    }

    void testKeysAreTheSame() {
        def username = "username"
        def key1 = SimpleCacheKey.newInstance(username)
        def key2 = SimpleCacheKey.newInstance(username)
        
        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the equals method", key1.equals(key2))

        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the equals operator", key1 == key2)

        //As the key are used to be a sorted map key)
        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the compareTo method", 
                   0 == key1.compareTo(key2))
                   
        //The keys must have the same hashCode)
        assertEquals("Cache keys having the same properties MUST have the " +
                   "same hashcode", key1.hashCode(), key2.hashCode())
    }

    void testKeysAreDifferent() {
        def username1 = "username1"
        def username2 = "username2"
        def key1 = SimpleCacheKey.newInstance(username1)
        def key2 = SimpleCacheKey.newInstance(username2)
        
        assertFalse("Cache keys having the same project name and different " +
                    "action and username MUST NOT be the same by equals " +
                    "operator", key1 == key2)

        //As the key are used to be a sorted map key)
        assertFalse("Cache keys having the same project name and different " +
                    "action and username MUST be different when compared " +
                    "with the method compareTo", 0 == key1.compareTo(key2))

        assertFalse("Cache keys having the same project name and different " +
                    "action and username MUST be different when compared " +
                    "using the method equals", key1.equals(key2))

        //The keys must have the same hashCode)
        assertFalse("Cache keys having different properties MUST have " +
                    "different hashcode", key1.hashCode() == key2.hashCode())
    }
}
