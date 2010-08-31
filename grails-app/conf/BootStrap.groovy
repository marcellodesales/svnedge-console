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

    def svnRepoService
    def commandLineService
    def packagesUpdateService
    def discoveryService

    // Management console services
    def authenticateService
    def lifecycleService
    def serverConfService
    def setupTeamForgeService
    def logManagementService

    // Alternate auth mechanism for CTF mode
    def authenticationManager
    def ctfAuthenticationProvider

    // Replication-related services for future CTF versions
    def replicationBootstrapService

    def init = { servletContext ->

        def config = ConfigurationHolder.config

        def env = GrailsUtil.environment
        log.info("#### Starting up the ${env} environment...")

        def appHome = ConfigUtil.appHome(config)
        log.info("Application Home: " + appHome)

        operatingSystemService.bootstrap(appHome)
        networkingService.bootStrap()

        log.info("Bootstrapping Statistics services...")
        networkStatisticsService.bootStrap()
        fileSystemStatisticsService.bootStrap()

        log.info("Bootstrapping the commandLineService...")
        commandLineService.bootstrap(config)

        log.info("Bootstrapping the lifecycleService...")
        lifecycleService.bootStrap(config)

        log.info("Bootstrapping Servers...")
        def initServer = lifecycleService.bootstrapServer(config)
        def server = initServer.server

        log.info("Bootstrap logging with consoleLogLevel from the Server...")
        logManagementService.bootstrap(ConfigUtil.dataDirPath(config),
            initServer.logLevel)

        log.info("Bootstrapping the ServerConfigService...")
        serverConfService.bootstrap(config, server)

        log.info("Bootstrap integration server configuration...")
        setupTeamForgeService.bootStrap(appHome)

        log.info("Bootstrapping packagesUpdateService...")
        packagesUpdateService.bootstrap(config)

        log.info("Bootstrapping svnRepoService...")
        svnRepoService.bootStrap(config)

        if (server.mode == ServerMode.MANAGED) {
            log.info("Changing auth to use CTF")
            authenticationManager.providers = [ctfAuthenticationProvider]
        }

        log.info("Bootstrapping userAccountService...")
        userAccountService.bootStrap(GrailsUtil.environment)

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
        discoveryService.bootStrap(config)
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
