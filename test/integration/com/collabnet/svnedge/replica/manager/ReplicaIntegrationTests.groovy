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
package com.collabnet.svnedge.replica.manager

import grails.test.*
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.teamforge.CtfServer

class ReplicaIntegrationTests extends GrailsUnitTestCase {
    def grailsApplication
    protected void setUp() {
        super.setUp()
        def config = grailsApplication.config
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
        super.tearDown()
    }

    void testCreate() {

        def inetAddress = findIpAddress();

        def server = new Server(
                hostname: "test.host.name",
                port: 1025,
                authHelperPort: 1026,
                fileLoginEnabled: true,
                ipAddress: inetAddress.getHostAddress(),
                netInterface: "eth0",
                repoParentDir: "/tmp",
                adminName: "Nobody",
                adminEmail: "devnull@collab.net",
                ldapServerPort: 389,
                ldapEnabledConsole: true)
        assertNotNull("The server instance should not be null", server)
        if (!server.validate()) {
            server.errors.allErrors.each { 
                log.error(it)
            }
            fail("The validation to create a default server failed.")
        }
        if (!server.save()) {
            fail("Server should have been saved successfully save.")
        }

        def replica = new ReplicaConfig(name: "Test Replica",
                locationName: "Brisbane, CA, USA.",
                latitude: 37.674423,
                longitude: -122.38494,
                positiveExpirationRate: 10,
                negativeExpirationRate: 2,
                cacheFlushPeriod: 23,
                svnSyncRate: 5
        )
        assertNotNull("The replica should not be null", replica)
        if (!replica.validate()) {
            replica.errors.allErrors.each { 
                println(it)
            }
        }
        if (!replica.save()) {
            fail("Replica should successfully save.")
        }
    }

    /**
     * Find the first non-loopback IPV4 address of this machine on any interface
     */
    private InetAddress findIpAddress() {

        Enumeration<NetworkInterface> en = 
                NetworkInterface.getNetworkInterfaces();

        while (en.hasMoreElements()) {
            NetworkInterface i = en.nextElement();
            for (Enumeration<InetAddress> en2 = i.getInetAddresses(); 
                    en2.hasMoreElements();) {
                InetAddress addr = en2.nextElement();
                if (!addr.isLoopbackAddress()) {
                    if (addr instanceof Inet4Address) {
                        return addr;
                    }
                }
            }
        }
        return null;
    }
}
