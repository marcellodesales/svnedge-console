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
import grails.util.GrailsUtil

import java.net.URL

import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode
import com.collabnet.svnedge.console.security.User
import com.collabnet.svnedge.console.security.Role
import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.replica.manager.ReplicaConfig
import com.collabnet.svnedge.teamforge.CtfServer

import org.codehaus.groovy.grails.commons.ApplicationAttributes 
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.springframework.core.io.ResourceLoader

/**
 * Regular BootStrap for Grails.
 */
class BootStrap {

    //####### Services that are always instantiated #############
    def operatingSystemService
    def networkingService
    def userAccountService
    def networkStatisticsService
    def fileSystemStatisticsService

    def packagesUpdateService
    def discoveryService

    // Management console services
    def authenticateService
    def lifecycleService
    def serverConfService
    def setupTeamForgeService
    def setupReplicaService
    def replicaCommandExecutorService
    def logManagementService

    // Alternate auth mechanism for CTF mode
    def authenticationManager
    def ctfAuthenticationProvider

    // Replication-related services for future CTF versions
    def replicationBootstrapService
    def jobsAdminService
    
    def grailsApplication
    
    def init = { servletContext ->

        def config = grailsApplication.config
        log.debug("Bootstrap config: " + config)
        ConfigUtil.configuration = config
        
        log.info("Bootstrap logging with consoleLogLevel from the Server...")
        try {
            logManagementService.bootstrap()
        } catch (Exception e) {
            log.error ("Failed to intitialize LogManagementService: " + e.getMessage(), e)
        }

        def env = GrailsUtil.environment
        log.info("#### Starting up the ${env} environment...")

        def appHome = ConfigUtil.appHome()
        log.info("Application Home: " + appHome)

        log.info("Bootstrapping OS services...")
        try {
            operatingSystemService.bootstrap(appHome)
        } catch (Exception e) {
            log.error ("Failed to intitialize OperatingSystemService: " + e.getMessage(), e)
        }

        log.info("Bootstrapping Network Information services...")
        try {
            networkingService.bootStrap()
        } catch (Exception e) {
            log.error ("Failed to intitialize NetworkingService: " + e.getMessage(), e)
        }

        log.info("Bootstrapping Statistics services...")
        try {
            networkStatisticsService.bootStrap()
        } catch (Exception e) {
            log.error ("Failed to intitialize NetworkStatisticsService: " + e.getMessage(), e)
        }
        try {
            fileSystemStatisticsService.bootStrap()
        } catch (Exception e) {
            log.error ("Failed to intitialize FileSystemStatisticsService: " + e.getMessage(), e)
        }

        log.info("Bootstrapping Servers...")
        def server
        try {
            server = lifecycleService.bootstrapServer(config)
        } catch (Exception e) {
            log.error ("Failed to intitialize Server instance: " + e.getMessage(), e)
        }

        log.info("Bootstrapping the ServerConfigService...")
        try {
            serverConfService.bootstrap(server)
        } catch (Exception e) {
            log.error ("Failed to intitialize ServerConfService: " + e.getMessage(), e)
        }

        log.info("Bootstrap integration server configuration...")
        try {
            setupTeamForgeService.bootStrap(appHome)
            setupReplicaService.bootStrap()
            replicaCommandExecutorService.bootStrap(ConfigUtil.dataDirPath())

        } catch (Exception e) {
            log.error ("Failed to intitialize SetupTeamForgeService: " + e.getMessage(), e)
        }

        log.info("Bootstrapping packagesUpdateService...")
        try {
            packagesUpdateService.bootstrap(config)
        } catch (Exception e) {
            log.error ("Failed to intitialize PackagesUpdateService: " + e.getMessage(), e)
        }

        if (server.managedByCtf()) {
            log.info("Changing auth to use CTF")
            authenticationManager.providers = [ctfAuthenticationProvider]
        }

        log.info("Bootstrapping userAccountService...")
        try {
            userAccountService.bootStrap(GrailsUtil.environment)
        } catch (Exception e) {
            log.error ("Failed to intitialize UserAccountService: " + e.getMessage(), e)
        }

        // If the svn server is configured to start with the console app,
        // start it now
        if (server && server.defaultStart && !lifecycleService.isStarted()) {
            lifecycleService.startServer()
        }

        if (GrailsUtil.environment == "test") {
            log.info("Bootstrapping replication Services...")
            replicationBootstrapService.bootstrap(config, servletContext, 
                server)
        }

        log.info("Bootstrapping discoveryService...")
        try {
            discoveryService.bootStrap(config)
        } catch (Exception e) {
            log.error ("Failed to intitialize DiscoveryService: " + e.getMessage(), e)
        }
        
        log.info("Bootstrapping jobsAdminService...")
        try {
            jobsAdminService.bootStrap()
        } catch (Exception e) {
            log.error ("Failed to intitialize JobsAdminService: " + e.getMessage(), e)
        }
    }

    /**
     * Called when application is shutdown.  Unfortunately it isn't called
     * during development using run-app, but it will be called when
     * the webapp is shutdown normally in production and it is called
     * in grails interactive mode
     */
    def destroy = {
        log.info("Releasing resources from the discovery service.")
        discoveryService.close()

        log.info("Releasing resources from the Operating System service.")
        operatingSystemService.destroy()
    }
} 
