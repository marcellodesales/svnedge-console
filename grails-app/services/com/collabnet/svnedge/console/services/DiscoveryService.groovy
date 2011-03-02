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

import com.collabnet.svnedge.console.Server

import com.collabnet.svnedge.discovery.SvnEdgeBonjourRegister
import com.collabnet.svnedge.discovery.mdns.SvnEdgeCsvnServiceKey
import com.collabnet.svnedge.discovery.mdns.SvnEdgeHttpServiceKey
import com.collabnet.svnedge.discovery.mdns.SvnEdgeServiceType

import java.net.InetAddress

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

/**
 * The discovery service uses the SvnEdge Discovery API to publish Bonjour 
 * services types.
 * The MulticastDnsService registers "collabnetsvn" service advertising
 * host:port/path (service type is _csvn._tcp). More information on those
 * services at http://developer.apple.com/mac/library/documentation/Darwin/
 * Reference/ManPages/man1/mDNS.1.html
 */
class DiscoveryService {

    boolean transactional = false

    def networkingService
    
    def boolean bootstrapped

    /**
     * The instance of the SvnEdge register client.
     */
    def SvnEdgeBonjourRegister register

    def bootStrap = { config ->

        log.info("Bootstrapping the Discovery service...")

        if (System.getProperty("csvn.discovery.disabled")) {
            log.info("Discovery is disabled by the system properties " +
                "'csvn.discovery.disabled'...")
            return
        }

        def serviceName = config.svnedge.mdns.serviceName
        serviceName = serviceName as String
        def hostAddr = networkingService.ipAddress
        def port = config.svnedge.mdns.port
        def path = config.grails.app.context
        def tfPath = config.svnedge.mdns.teamForgeRegistrationPath

        try {
            register = SvnEdgeBonjourRegister.getInstance(
                hostAddr as InetAddress)

            registerServices(config)

        } catch (Exception e) {
            log.error(e)
        }
    }

    private void registerServices(config) throws IOException {

        if (register) {
            def serviceName = config.svnedge.mdns.serviceName
            serviceName = serviceName as String
            def port = config.svnedge.mdns.port
            def path = config.grails.app.context
            def tfPath = config.svnedge.mdns.teamForgeRegistrationPath
            def server = Server.getServer()
            if (server.managedByCtf()) {
                tfPath = "" // clients interpret as managed
            }

            def params = [:]
            params[SvnEdgeCsvnServiceKey.TEAMFORGE_PATH] = tfPath
            params[SvnEdgeCsvnServiceKey.CONTEXT_PATH] = path
            params[SvnEdgeCsvnServiceKey.SERVER_MODE] = server.mode

            register.registerService(port, SvnEdgeServiceType.CSVN, params);

            params = [:]
            params[SvnEdgeHttpServiceKey.PATH] = path ?: "/"

            register.registerService(port, SvnEdgeServiceType.HTTP, params);

            bootstrapped = true
        }
    }

    /**
     * shut down the responder
     * unregister all the services 
     */
    def close = {
        if (bootstrapped) {
            log.info("The Discovery Service will announce the server " + 
                "shutdown...")
            register.close()
            log.debug("The announcement was sent...")
        }
    }

    /**
     * Handles the server update
     * closes current responder
     * creates a new responder with updated config
     */
    def serverUpdated = {
        if (!bootstrapped) {
            return
        }
        log.info("Updating discovery service information...")
        def currentConfig = ConfigurationHolder.config;

        try {
            register.unregisterServices()
            registerServices(currentConfig)

        } catch (Exception e) {
            log.error(e)
        }
    }
}