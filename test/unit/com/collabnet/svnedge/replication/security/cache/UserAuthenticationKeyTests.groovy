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

class UserAuthenticationKeyTests extends GrailsUnitTestCase {

    void testKeyCreationWithCorrectParameters() {
        try {
            def key1 = UserAuthenticationKey.newInstance("marcello", "1234")
            assertNotNull("The key must be created with correct parameters", 
                          key1)
            assertEquals("marcello", key1.getUsername())
            assertEquals(UserAuthenticationKey.encrypt("1234"), 
                    key1.getPassword())
        } catch (Exception e) {
            fail("No exception should be thrown after creating a cache key " +
                    "with correct parameters")
        }
    }

    void testKeyCreationWithIncorrectParameters() {
        try {
            def key1 = UserAuthenticationKey.newInstance(null, null)
            fail("A cache key should not be created with null values")
        } catch (Exception e) {
            assertNotNull("The NullPointerException must be thrown when " +
                     "not providing parameters to create a cache key", e)
            assertTrue(e instanceof NullPointerException)
        }

        try {
            def key1 = UserAuthenticationKey.newInstance("", "")
            assertEquals("", key1.getUsername())
            assertEquals(UserAuthenticationKey.encrypt(""),
                         key1.getPassword())

        } catch (Exception e) {
            fail("No exception should be thrown after creating a cache key " +
                    "with empty parameters")
        }
    }

    void testKeysAreTheSame() {
        def key1 = UserAuthenticationKey.newInstance("marcello", "1234")
        def key2 = UserAuthenticationKey.newInstance("marcello", "1234")

        assertTrue("Cache keys having the same username and password MUST be " +
                   "the same when compared with the equals method", 
                   key1.equals(key2))

        assertTrue("Cache keys having the same username and password MUST " +
                   "be the same when compared with the equals operator", 
                   key1 == key2)

        //As the key are used to be a sorted map key)
        assertTrue("Cache keys having the same username and password MUST " +
                   "be the same when compared with the compareTo method", 
                   0 == key1.compareTo(key2))

        //The keys must have the same hashCode)
        assertEquals("Cache keys having the same username and password MUST " +
                     "have the same hashcode", key1.hashCode(), key2.hashCode())
    }

    void testKeysAreDifferent() {
        //Testing same username and different passwords
        def key1 = UserAuthenticationKey.newInstance("marcello", "1234")
        def key2 = UserAuthenticationKey.newInstance("marcello", "abcd")

        assertFalse("Cache keys having the same username and different " +
                    "passwords MUST NOT be the same by equals operator", 
                    key1 == key2)

        //As the key are used to be a sorted map key)
        assertFalse("Cache keys having the same username and different " +
                    "passwords MUST be different when compared with the " +
                    "compareTo method" + 0 == key1.compareTo(key2))

        assertFalse("Cache keys having the same username and different " +
                    "passwords MUST be different when compared using the " +
                    "method equals", key1.equals(key2))

        //The keys must have the same hashCode)
        assertFalse("Cache keys having different properties MUST have " +
                    "different hashcode", key1.hashCode() == key2.hashCode())

        //Testing different username and same passwords
        def key3 = UserAuthenticationKey.newInstance("collabnet", "1234")
        def key4 = UserAuthenticationKey.newInstance("google", "1234")

        assertFalse("Cache keys having different usernames and same " +
                    "password MUST NOT be the same by equals operator", 
                    key3 == key4)

        //As the key are used to be a sorted map key)
        assertFalse("Cache keys having different usernames and same " +
                    "password MUST be different when compared with the" +
                    "method compareTo", 0 == key3.compareTo(key4))

        assertFalse("Cache keys having different usernames and same " +
                    "password MUST NOT be the same by the equals method", 
                    key3.equals(key4))

        //The keys must have the same hashCode)
        assertFalse("Cache keys having different properties MUST have " +
                    "different hashcode", key3.hashCode() == key4.hashCode())

        assertFalse("Cache keys must not be the same when compared with " +
                    "instances of other classes", key3.equals("string"))
    }

    void testFormatMethodWithParameters() {
        def str1 = UserAuthenticationKey.format("marcello", 
                UserAuthenticationKey.encrypt("1234"))
        def str2 = UserAuthenticationKey.format("marcello", 
                UserAuthenticationKey.encrypt("abcd"))
        //Objects are the same by equals method
        assertEquals("marcello|" + UserAuthenticationKey.encrypt("1234"), str1)
        assertEquals("marcello|" + UserAuthenticationKey.encrypt("abcd"), str2)

        def key1 = UserAuthenticationKey.newInstance("marcello", "1234")
        assertEquals(str1, key1.toString())
    }
}
