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
package com.collabnet.svnedge.console.services

import java.net.InetAddress;
import javax.jmdns.*;

/**
 * The MulticastDnsService registers "collabnetsvn" service advertising
 * host:port/path (service type is _csvn._tcp).
 */
class MulticastDnsService {

    boolean transactional = false

    def networkingService

    private static final String SERVICE_TYPE__CSVN = "_csvn._tcp.local."
    private static final String SERVICE_TYPE__HTTP = "_http._tcp.local."
    private static final int DEFAULT_PORT = 8080;
    private static final String PROPERTY_PATH = "path"
    private static final String PROPERTY_TFPATH = "tfpath"
    public static final String SERVICE_NAME = "collabnetsvn"

    def jmdns

    def bootStrap = { config ->

        def serviceName = config.svnedge.mdns.serviceName
        def hostAddr = networkingService.ipAddress
        def port = config.svnedge.mdns.port
        def path = config.grails.app.context
        def tfPath = config.svnedge.mdns.teamForgeRegistrationPath

        log.info("Bootstrapping Multi-cast DNS service...")

        try {
            jmdns = JmDNS.create(hostAddr as InetAddress)
            java.util.Map<String, ?> params = new java.util.HashMap<String, String>()
            params.put(PROPERTY_TFPATH, (path == null || tfPath == null) ? "" : tfPath)
            params.put(PROPERTY_PATH, path == null ? "" : path)

            ServiceInfo infoCsvn = ServiceInfo.create(
                              SERVICE_TYPE__CSVN,
                              serviceName instanceof String ? (serviceName as String): SERVICE_NAME,
                              port,
                              0,
                              0,
                              params
                              )
            //register _csvn._tcp.local
            jmdns.registerService(infoCsvn)

            ServiceInfo infoHttp = ServiceInfo.create(SERVICE_TYPE__HTTP,
                    serviceName instanceof String ? (serviceName as String) : SERVICE_NAME, 
                    port, 
                    PROPERTY_PATH + "=" + (path == null ? "" : path)
            )

            //register _http._tcp.local
            jmdns.registerService(infoHttp)
        } catch (Exception e) {
            log.error(e)
        }
    }

    /**
     * shut down the responder
     * unregister all the services 
     */
    def close = {
        jmdns?.close();	
    }

    /**
     * Handles the server update
     * closes current responder
     * creates a new responder with updated config
     * TODO not called yet
     */
    def serverUpdated = {serviceName, hostAddr, port, path, tfPath ->
        jmdns?.close()
        bootstrap(serviceName, hostAddr, port, path, tfPath)
    }
}

