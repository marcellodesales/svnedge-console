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
package com.collabnet.svnedge.services
;

import com.collabnet.svnedge.console.NetworkingService;

import grails.test.GrailsUnitTestCase;

class NetworkingServiceTests extends GrailsUnitTestCase {
    def networkingService

    protected void setUp() {
        super.setUp()
        networkingService = new NetworkingService()
    }

    void testGetNetworkInterfacesWithIPAddresses() {
        def interfaces = networkingService
            .getNetworkInterfacesWithIPAddresses()
        assertTrue "Should be at least one network interface", 
            !interfaces.isEmpty()
        assertTrue "Last interface should be the loopback", 
            interfaces.get(interfaces.size() - 1).isLoopback()
    }

    void testGetIPv4Addresses() {
        def addresses = networkingService.getIPv4Addresses()
        assertTrue "Should be at least one IP address", 
            !addresses.isEmpty()
        assertTrue "Last address should be the loopback", 
            addresses.get(addresses.size() - 1).isLoopbackAddress()
        assertTrue "Link local addresses should not be included",
            addresses.findAll({it.isLinkLocalAddress()}).isEmpty()
    }

    void testGetInetAddressNetworkInterfaceMap() {
        def addrInterfaceMap = networkingService
            .getInetAddressNetworkInterfaceMap()
        assertTrue "Should be at least one IP address", 
            !addrInterfaceMap.isEmpty()
        for (addrInts in addrInterfaceMap.entrySet()) {
            String addr = addrInts.key
            Collection interfaces = addrInts.value
            if (addr.startsWith("127")) {
                interfaces.each { 
                    assertTrue "Expect lo for loopback addresses", it.startsWith("lo")
                }
            } else if (addr.startsWith("169") || addr.startsWith("fe80")) {
                fail "Link-local address found"
            } else {
                assertFalse "Address expected to have an interface", 
                    interfaces.isEmpty()
            }
        }
    }
}
