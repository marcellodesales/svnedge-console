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

import grails.test.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class CacheMananagementAuthIntegrationTests extends GrailsUnitTestCase {

    def cacheManagementService
    def ctfRemoteClientService
    def config = ConfigurationHolder.config

    def TEST_USERNAME = "marcello"
    def TEST_PASSWORD = "12345"

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
                adminUsername, adminPassword)
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
     * Tests the current state of the users cache at bootstrap
     */
    void testUsersCacheStateAtBootStrap() {
        assertEquals("The users cache should be empty at bootstrap", 
                     0, cacheManagementService.getAuthenticationCacheSize())
    }

    /**
     * Same Subsequent calls to the users cache must update the cache only once,
     * but the cache size must maintain the same.
     */
    void testIsUserValidMethodNotUpdatingCacheState() {
        def value1 = cacheManagementService.authenticateUser(
                username: TEST_USERNAME, password: TEST_PASSWORD)
        assertNotNull("The response should not be null", value1)
        assertTrue("The response of isUserValid must be of Boolean type", 
                        value1 instanceof Boolean)
        assertTrue("The value must be true for a valid user $value1", value1)
        assertTrue("The cache must be updated after the call to isUserValid()" +
                   " for a new usr/pwd key-pair", 
                   1 == cacheManagementService.getAuthenticationCacheSize())
        
        // Asserting that the same method call should not update the cache
        def value2 = cacheManagementService.authenticateUser(
                username: TEST_USERNAME, password: TEST_PASSWORD)
        assertNotNull("The response should not be null", value2)
        assertTrue("The response of isUserValid must be of Boolean type", 
                        value2 instanceof Boolean)
        assertEquals("Subsequent calls with the same input should have the " +
                     "same result (assuming no changes on server-side", value1,
                     value2)
        assertTrue("The value must be true for a valid user", value2)
        assertTrue("The cache must NOT be updated after the call to " +
                   "isUserValid() for existing usr/pwd key-pair",
                   1 == cacheManagementService.getAuthenticationCacheSize())
    }

    /**
     * Different Subsequent calls to the cache must update the cache for all
     * the different key-value pairs
     */
    void testIsUserValidMethodUpdatingCacheState() {
        // Asserting on correct value
        def value1 = cacheManagementService.authenticateUser(
                username: TEST_USERNAME, password: TEST_PASSWORD)
        assertNotNull("The response should not be null", value1)
        assertTrue("The response of isUserValid must be of Boolean type", 
                        value1 instanceof Boolean)
        assertTrue("The value must be true for a valid user", value1)
        assertTrue("The cache must be updated after the call to isUserValid()" +
                   " for a new usr/pwd key-pair", 
                   1 == cacheManagementService.getAuthenticationCacheSize())

        // Asserting on wrong value should update the cache
        def value2 = cacheManagementService.authenticateUser(
                username:"marcello", password:"xyzt")
        assertNotNull("The response should not be null", value2)
        assertTrue("The response of isUserValid must be of Boolean type", 
                        value2 instanceof Boolean)
        assertFalse("Subsequent calls with different input should NOT have " +
                    "the same result (considering no changes on server-side", 
                    value1 == value2)

        assertFalse("The value must be false for an invalid key", value2)
        assertTrue("The cache must be updated after the call to " +
                   "isUserValid() for different usr/pwd key-pair",
                   2 == cacheManagementService.getAuthenticationCacheSize())

        // Asserting on different values should also update the cache
        def value3 = cacheManagementService.authenticateUser(
                username:"adam", password:"abc")
        assertNotNull("The response should not be null", value3)
        assertTrue("The response of isUserValid must be of Boolean type", 
                        value3 instanceof Boolean)
        assertFalse("The value must be false for an invalid user", value3)
        assertTrue("The cache must be updated after the call to " +
                   "isUserValid() for different usr/pwd key-pair",
                   3 == cacheManagementService.getAuthenticationCacheSize())
    }
}
