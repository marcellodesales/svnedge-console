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
package com.collabnet.svnedge.replication


import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import grails.util.GrailsUtil;

import com.collabnet.svnedge.console.ConfigUtil;
import com.collabnet.svnedge.replica.manager.ApprovalState;
import com.collabnet.svnedge.replica.manager.Master;
import com.collabnet.svnedge.replica.manager.ReplicaConfig;

/**
 * The Svn Server replication feature provides distributed replication
 * server support for Svn Edge servers.
 * 
 * @author Marcello de Sales(mdesales@collab.net)
 *
 */
class ReplicationBootstrapService {

    def cacheManagementService
    def replicaErrorService
    def uploadErrorsService

    //def svnLogService
    def svnStatisticsService
    def svnNotificationService
    def jobsAdminService

    def userCacheStatisticsService
    def latencyStatisticsService

    def bootstrap = { config, servletContext, server ->

        if (!config.svnedge.ctfMaster) {
            throw new RuntimeException("You need to provide a Master " +
                "configuration block: ceeMaster or ctfMaster.")
        }

        // note: this should be bootstrapped before jobAdminService
        userCacheStatisticsService.bootStrap()
        latencyStatisticsService.bootStrap()
        svnStatisticsService.bootStrap()

        def defaultMaster = Master.getDefaultMaster()
        def isSslEnabled = defaultMaster ? defaultMaster.sslEnabled :
            config.svnedge.ctfMaster.ssl ||
            config.svnedge.ctfMaster.ssl == "true"
        if (!defaultMaster) {
            defaultMaster = new Master(sslEnabled: isSslEnabled,
                    accessUsername: config.svnedge.ctfMaster.username,
                    accessPassword: config.svnedge.ctfMaster.password,
                    trustStorePassword:
                      config.svnedge.replica.ssl.trustStorePasswd,
                    hostName: config.svnedge.ctfMaster.domainName,
                    isActive:false)
            defaultMaster.save()
        }
        def master = Master.getDefaultMaster()

        def prot = isSslEnabled ? "https" : "http"
        log.info("Bootstrapping Replica for Master...")
        log.info("########## Target Master ##########")
        log.info("# Protocol: $prot")
        log.info("# Host: ${defaultMaster?.hostName}")
        log.info("# Usr/Pwd: ${defaultMaster?.accessUsername}/" +
                              "${defaultMaster?.accessPassword}")
        log.info("# isActive: ${defaultMaster?.isActive}")

        // Adding the truststore file URL to the configuration holder,
        // so that the Abstract Web Services API does not depend on the
        // ServletContext.
        String trustFile = config.svnedge.replica.ssl.trustStoreFileName
        def trustFilePath = GrailsUtil.environment == "test" ? 
            System.properties['base.dir'] + "/web-app" + trustFile :
            new URL(servletContext.getResource(trustFile)).getPath()
        def replicaConfig = ReplicaConfig.getCurrentConfig()
        if (!replicaConfig) {
            replicaConfig = new ReplicaConfig(
                name: "New Replica",
                locationName: "Brisbane, CA, USA.",
                latitude: 37.674423,
                longitude: -122.38494,
                positiveExpirationRate:
                  config.svnedge.replica.cache.positiveExpirationRate,
                negativeExpirationRate:
                  config.svnedge.replica.cache.negativeExpirationRate,
                cacheFlushPeriod:
                  config.svnedge.replica.cache.cacheFlushPeriod,
                svnSyncRate: config.svnedge.replica.svn.svnsyncRate
            )
            replicaConfig.save(flush:true)
        }

        servletContext.setAttribute("server", server)
        log.info("########## Current Replica ##########")
        log.info("# Replica Hostname: " + server.getHostname())
        log.info("# Cache Refresh at " +
                "${replicaConfig?.cacheFlushPeriod} every hour")
        log.info("# Positive Cache Expiration Rate: " +
                "${replicaConfig?.positiveExpirationRate} min")
        log.info("# Negative Cache Expiration Rate: " +
                "${replicaConfig?.negativeExpirationRate} sec")
        log.info("# XML-RPC server port: " +
                "${config.svnedge.replica.xmlrpc.serverPort}")
        log.info("# SSL Truststore File Path: $trustFilePath")

        /*log.info("Bootstrapping svnLogService...")
        svnLogService.bootStrap(new File(
            dataDirPath(config), "logs/subversion.log").absolutePath) */

        log.info("Bootstrapping cacheManagementService...")
        cacheManagementService.bootStrap(defaultMaster)

        log.info("Bootstrapping replicaErrorService...")
        replicaErrorService.bootStrap(config.svnedge.replica.error.minLevel)

        log.info("Bootstrapping uploadErrorsService...")
        uploadErrorsService.bootStrap(config.svnedge.replica.error.uploadRate)

        log.info("Bootstrapping svnNotificationService...")
        svnNotificationService.bootStrap(
            config.svnedge.svn.repositoriesParentPath,
            ConfigUtil.svnPath(), ConfigUtil.svnadminPath(),
            ConfigUtil.svnsyncPath(),
            config.svnedge.replica.svn.svnsyncRate, defaultMaster)
    }
}
