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

class CTFUserAuthorizationKeyTests extends GrailsUnitTestCase {

    void testKeyCreationWithCorrectParameters() {
        try {
            def key1 = CTFUserAuthorizationKey.newInstance("user",
                    "/repo/path/1", "view-all")
            assertNotNull("The key must be created with correct parameters", 
                          key1)
            assertEquals("user", key1.getUsername())
            assertEquals("/repo/path/1", key1.getRepoPath())
            assertEquals("view-all", key1.getAccessType())

            //with a null access type
            def key2 = CTFUserAuthorizationKey.newInstance("user2",
                                                    "/repo/path/2", null)
            assertNotNull("The key must be created with correct parameters", 
                          key2)
            assertEquals("user2", key2.getUsername())
            assertEquals("/repo/path/2", key2.getRepoPath())
            assertEquals("", key2.getAccessType())

            //with an empty access type
            def key3 = CTFUserAuthorizationKey.newInstance("user3", 
                                                    "/repo/path/3", "")
            assertNotNull("The key must be created with correct parameters", 
                          key3)
            assertEquals("user3", key3.getUsername())
            assertEquals("/repo/path/3", key3.getRepoPath())
            assertEquals("", key3.getAccessType())
        } catch (Exception e) {
            fail("No exception should be thrown after creating a cache key " +
                    "with correct parameters")
        }
    }

    void testKeyCreationWithIncorrectParameters() {
        try {
            def key1 = CTFUserAuthorizationKey.newInstance(null, null, "")
            fail("A cache key should not be created with null values")
        } catch (Exception e) {
            assertNotNull("The IllegalArgumetException must be thrown when " +
                     "not providing parameters to create a cache key", e)
            assertTrue(e instanceof IllegalArgumentException)
        }

        try {
            def key2 = CTFUserAuthorizationKey.newInstance("", "", null)
            fail("A cache key should not be created with empty values")
        } catch (Exception e) {
            assertNotNull("The IllegalArgumetException must be thrown when " +
                     "not providing parameters to create a cache key", e)
            assertTrue(e instanceof IllegalArgumentException)
        }
    }

    void testKeysAreTheSame() {
        def key1 = CTFUserAuthorizationKey.newInstance("user",
                "/repo/path/1", "view-all")
        def key2 = CTFUserAuthorizationKey.newInstance("user", 
                "/repo/path/1", "view-all")

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
        
        //with null values for the access-type
        def key3 = CTFUserAuthorizationKey.newInstance("user", 
                                                    "/repo/path/2", null)
        def key4 = CTFUserAuthorizationKey.newInstance("user", 
                                                    "/repo/path/2", null)

        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the equals method", key3.equals(key4))

        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the equals operator", key3 == key4)

        //As the key are used to be a sorted map key)
        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the compareTo method", 
                   0 == key3.compareTo(key4))

        //The keys must have the same hashCode)
        assertEquals("Cache keys having the same properties MUST have the " +
                   "same hashcode", key3.hashCode(), key4.hashCode())

        //with empty value for the access-type
        def key5 = CTFUserAuthorizationKey.newInstance("user", 
                                                    "/repo/path/2", "")
        def key6 = CTFUserAuthorizationKey.newInstance("user", 
                                                    "/repo/path/2", "")

        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the equals method", key5.equals(key6))

        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the equals operator", key5 == key6)

        //As the key are used to be a sorted map key)
        assertTrue("Cache keys having the same properties MUST be the same " +
                   "when compared with the compareTo method", 
                   0 == key5.compareTo(key6))

        //The keys must have the same hashCode)
        assertEquals("Cache keys having the same properties MUST have the " +
                     "same hashcode", key5.hashCode(), key6.hashCode())
                     
        assertTrue("Cache keys having the same properties with null or enpty" +
                   "access type MUST be the same", 
                   (key3 == key5) && (key4 == key6))
    }

    void testKeysAreDifferent() {
        //Testing same username and different passwords
        def key1 = CTFUserAuthorizationKey.newInstance("user1",
                "/repo/path/2", "view")
        def key2 = CTFUserAuthorizationKey.newInstance("user2",
                "/repo/path/2", "view")

        assertFalse("Cache keys having different properties MUST be " +
                    "different when compared with equals operator", 
                    key1 == key2)

        //As the key are used to be a sorted map key)
        assertFalse("Cache keys having different properties MUST be " +
                    "different when compared with the method compareTo",
                    0 == key1.compareTo(key2))

        assertFalse("Cache keys having different properties MUST be " +
                    "different when compared with the method equals",
                    key1.equals(key2))

        //The keys must have the same hashCode)
        assertFalse("Cache keys having different properties MUST have " +
                    "different hashcode", key1.hashCode() == key2.hashCode())

        //Testing different username and same passwords
        def key3 = CTFUserAuthorizationKey.newInstance("user3",
                "/repo/path/3", "")
        def key4 = CTFUserAuthorizationKey.newInstance("user4",
                "/repo/path/4", "")

        assertFalse("Cache keys having different properties MUST be " +
                    "different when compared with equals operator", 
                    key3 == key4)

        //As the key are used to be a sorted map key)
        assertFalse("Cache keys having different properties MUST be " +
                    "different when compared with the method compareTo",
                    0 == key3.compareTo(key4))

        assertFalse("Cache keys having different properties MUST be " +
                    "different when compared with the method equals",
                    key3.equals(key4))

        //The keys must have the same hashCode)
        assertFalse("Cache keys having different properties MUST have " +
                    "different hashcode", key3.hashCode() == key4.hashCode())
        
        assertFalse("Cache keys must not be the same when compared with " +
                        "instances of other classes", key3.equals("string"))
    }

    void testFormatWithParameters() {
        def str1 = CTFUserAuthorizationKey.format("user1", "/repo/path/1", 
                "view")
        def str2 = CTFUserAuthorizationKey.format("user2", "/repo/path/2", 
                null)
        def str3 = CTFUserAuthorizationKey.format("user3", "/repo/path/3", 
                "")
        //Objects are the same by equals method
        assertEquals("user1|/repo/path/1|view", str1)
        assertEquals("user2|/repo/path/2", str2)

        def key4 = CTFUserAuthorizationKey.newInstance("user1",
                "/repo/path/1", "view")
        assertEquals(str1, key4.toString())
    }
    
    void testInheritedMethods() {
        def key1 = CTFUserAuthorizationKey.newInstance("user",
                "/repo/path/1", "view-all")
        assertNotNull("The key must be created with correct parameters", 
                      key1)

        def props = [username:"user", repoPath:"/repo/path/1", 
                     accessType:"view-all"]
        assertTrue(key1.hasProperties(props))
        assertTrue(key1.containsUsername("user"))
        
        def Wrongprops = [username:"333", repoPath:"/repo/path/1", 
                     accessType:"view-all"]
        def missingProps = [username:"333"]
        assertFalse(key1.hasProperties(missingProps))
        assertFalse(key1.containsUsername(""))
    }
}
