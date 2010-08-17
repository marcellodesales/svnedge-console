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
package com.collabnet.svnedge.master.service.ctf

import grails.test.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import com.collabnet.svnedge.master.ctf.CtfAuthenticationException

class CtfRemoteClientServiceIntegrationTests extends GrailsUnitTestCase {

    def ctfTestUrl
    
    def config = ConfigurationHolder.config

    def ctfRemoteClientService

    // FIXME:  for now, skip this test on Windows, since the current service only works on *nix 
    def isWindows = System.getProperty("os.name").substring(0,3) == "Win"

    def makeCtfBaseUrl() {
        def ctfProto = config.svnedge.ctfMaster.ssl ? "https://" : "http://"
        def ctfHost = config.svnedge.ctfMaster.domainName
        def ctfPort = config.svnedge.ctfMaster.port == "80" ? "" : ":" +
                config.svnedge.ctfMaster.port
        ctfTestUrl = ctfProto + ctfHost + ctfPort
        return ctfTestUrl
    }

    def getCtfUrl() {
        if (!ctfTestUrl) {
            ctfTestUrl = makeCtfBaseUrl()
        }
        return ctfTestUrl
    }

    void testIsUserValidWithValidUsers() {
        if (isWindows) {
            return
        }
        def username = config.svnedge.ctfMaster.username
        def password = config.svnedge.ctfMaster.password
        
        def response = ctfRemoteClientService.authenticateUser(username, password)
        assertNotNull("Authentication must succeed for valid user on CTF",
            response)
        response = ctfRemoteClientService
            .authenticateUser("admin", "wrong-password")
        assertNull("Authentication must NOT succeed for a user on " +
                        "CTF with a wrong password", response)
    }

    void testLoginWithCorrectValues() {
        if (isWindows) {
            return
        }
        def ctfUrl = this.getCtfUrl()
        def username = config.svnedge.ctfMaster.username
        def password = config.svnedge.ctfMaster.password

        println("CTF URL: ${ctfUrl}")
        println("Credentials: ${username}:${password}")

        try {
            def sessionId = ctfRemoteClientService.login(ctfUrl, username,
                password)
            assertNotNull("The session ID must have been created with " +
                "correct credentials", sessionId)
        } catch (CtfAuthenticationException loginFailedAsExpected) {
            fail("The login must NOT throw any exception with correct values")
        }
    }

    void testLoginWithIncorrectValues() {
        if (isWindows) {
            return
        }
        def ctfUrl = this.getCtfUrl()
        def username = "wrongUsername"
        def password = "wrongPasswd"

        println("SOAP call URL: ${ctfUrl}")
        println("Credentials: ${username}:${password}")

        try {
            def sessionId = ctfRemoteClientService.login(ctfUrl, username,
                password)
            fail("The login must throw a login fault", sessionId)
        } catch (CtfAuthenticationException loginFailedAsExpected) {
            println(loginFailedAsExpected.message)
        }
    }

    void testIsUserValidWithInvalidUsers() {
        if (isWindows) {
            return
        }
        def response = ctfRemoteClientService
            .authenticateUser("non-exist", "pwd")
        assertNull("Authentication must NOT succeed for non-existent " +
                   "user on CTF", response)
        response = ctfRemoteClientService.authenticateUser("", "")
        assertNull("Authentication must NOT be valid for empty user/pw " +
                   "on CTF", response)
        response = ctfRemoteClientService.authenticateUser("admin", "")
        assertNull("Authentication must NOT be valid for empty password" +
                   " on CTF", response)
        response = ctfRemoteClientService.authenticateUser("", "12345")
        assertNull("Authentication must NOT be valid for empty user", 
                    response)
    }

    void testGetRolePathsWithWithoutAccessType() {
        if (isWindows) {
            return
        }
        String response = ctfRemoteClientService.getRolePaths("admin", 
                                                              "internal/", "")
        println ("Roles Paths admin/internal/ = $response")
        assertEquals("Result MUST be in the format x:x when called without " +
                "accessType.", 0, response.tokenize(":").size())

        response = ctfRemoteClientService.getRolePaths("admin", "internal/", 
                null)
        println ("Roles Paths admin/exsy1006/internal/ = $response")
        assertEquals("Result MUST be in the format x:x when called without " +
                "accessType.", 0, response.tokenize(":").size())
    }

    void testGetRolePathsWithAccessType() {
        if (isWindows) {
            return
        }
        String response = ctfRemoteClientService.getRolePaths("admin", 
                "internal/", "view-all")
        println ("Roles Paths admin/exsy1006/internal/ = $response")
        assertEquals("Result MUST be in the format x:x:x when called without " +
                     "accessType.", 3, response.tokenize(":").size())
    }

    void testClearCacheOnMasterCTF() {
        if (isWindows) {
            return
        }
        assertTrue("Clear remote cache on a Master CTF must always be possible",
                ctfRemoteClientService.clearCacheOnMasterCTF())
    }
}
