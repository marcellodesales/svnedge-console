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
package com.collabnet.svnedge.console.ui

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.CantBindPortException

import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.replica.manager.ReplicaConfig
import com.collabnet.svnedge.replica.manager.ApprovalState

import java.text.SimpleDateFormat
import com.collabnet.svnedge.console.ServerMode
import com.collabnet.svnedge.teamforge.CtfServer
import com.collabnet.svnedge.replication.ReplicaConfiguration;


@Secured(['ROLE_USER'])
class StatusController {

    def operatingSystemService
    def networkingService
    def svnRepoService
    def quartzScheduler
    def statisticsService
    def lifecycleService
    def packagesUpdateService
    def setupReplicaService

    // start and stop actions use POST requests
    static allowedMethods = [start:'POST', stop:'POST']

    def getUpdateMessage() {
        return message(code: 'packagesUpdate.status.updates.available.download', 
            args: ['/csvn/packagesUpdate/available'])
    }

    def index = {
        def server = Server.getServer()
        def ctfUrl
        def currentReplica = ReplicaConfiguration.getCurrentConfig()
        def ctfServer = CtfServer.getServer()

        if (server.mode == ServerMode.REPLICA) {

            if(!currentReplica) {
               flash.warn = message(code: 'replica.error.notStarted')
            }
            if (currentReplica.approvalState == ApprovalState.PENDING) {
               flash.warn = message(code: 'replica.error.notApproved')
            } else if (currentReplica.approvalState == ApprovalState.DENIED) {
               flash.warn = message(code: 'replica.error.denied')
            } else if (currentReplica.approvalState == ApprovalState.NOT_FOUND
                      || currentReplica.approvalState == ApprovalState
                      .REGISTRATION_FAILED) {
               flash.error = message(code: 'replica.error.cantRegister')

            } else if (currentReplica.approvalState == ApprovalState.APPROVED &&
                    setupReplicaService.hasRegistrationErrors()) {

                flash.error = message(
                    code: 'replica.error.registration.serverCantRestart')
                // clear the message until the user has restarted.
                setupReplicaService.clearRegistrationError()
            }

            ctfUrl = ctfServer.getWebAppUrl()
        }
        else if (server.mode == ServerMode.MANAGED) {
            ctfUrl = ctfServer.getWebAppUrl()
        }

        // if this is a "reverted" replica, add a flash message and then delete 
        // ReplicaConfiguration
        if (currentReplica?.approvalState == ApprovalState.REMOVED) {
            flash.warn = message(code: 'replica.error.removed')
            currentReplica.delete()
        }

        boolean isStarted = lifecycleService.isStarted()
        params.max = 
            Math.min( params.max ? params.max.toInteger() : 10,  100)
        Repository[] repos = Repository.list(params)
        Repository sampleRepo = 
            (repos.length > 0) ? repos[0] : null

        def sfVersion = this.packagesUpdateService.getInstalledVersionNumber()
        def svnVer = this.packagesUpdateService.getInstalledSvnVersionNumber()
        try {
           if (this.packagesUpdateService.hasBeenBootstraped()) {
               if (this.packagesUpdateService.areThereUpdatesAvailable()) {
                   if (!flash.error) {
                       flash.unfiltered_warn = getUpdateMessage()
                   }
               }
               //system restart has priority over the updates
               if (this.packagesUpdateService.systemNeedsRestart()) {
                   flash.warn = message(
                       code: 'packagesUpdate.status.updates.requiresRestart')
               }
               //if the system has recently been updated
               if (this.packagesUpdateService.hasTheSystemBeenUpdated() || 
                       this.packagesUpdateService.hasTheSvnServerBeenUpdated()){
                   this.packagesUpdateService.setTheSystemToNotBeenUpdated()
                   flash.message = message(
                       code: 'packagesUpdate.success.installed.updates')
               } else
               if (this.packagesUpdateService.hasNewPackagesBeenInstalled()) {
                   this.packagesUpdateService.setTheSystemToNotBeenUpdated()
                   flash.message = message(
                       code: 'packagesUpdate.success.installed.newPackages')
               }
           }

       } catch (NoRouteToHostException nrth) {
           if (!flash.error) {
               flash.error = message(
                   code: 'packagesUpdate.error.server.noConnection')
           }
       } catch (Exception e) {
           e.printStackTrace()
           def msg = message(code: 'packagesUpdate.error.general')
           flash.error = msg + ":" + e.getMessage()
       }

       return [isStarted: isStarted,
               isDefaultPortAllowed: lifecycleService.isDefaultPortAllowed(),
               sampleRepo: sampleRepo,
               isReplicaMode: server.mode == ServerMode.REPLICA,
               currentReplica: currentReplica,
               server: server,
               perfStats: getPerfStats(currentReplica, server),
               softwareVersion: sfVersion,
               svnVersion: svnVer,
               ctfUrl: ctfUrl
               ]
    }

    def getPerfStats(currentConfig, server) {
       def dateTimeFormat = message(code:"default.dateTime.format.withZone")
       def runningSinceDate = quartzScheduler.getMetaData().runningSince
       runningSinceDate = runningSinceDate ?: new Date()
       def currentLocale = RCU.getLocale(request)
       def model = [
           [label: message(code: 'status.page.status.running_since'),
            value: new SimpleDateFormat(dateTimeFormat, 
                currentLocale).format(runningSinceDate)]]
       if (!server.managedByCtf()) {
           model << [label: message(code: 'status.page.status.repo_health'), 
               value: svnRepoService.formatRepoStatus(statisticsService
                   ?.getReposStatus(), currentLocale)]
       }
        if (operatingSystemService.isReady()) {

            def timestampDate = statisticsService.getTimestampFileSystemData()
            String timestampString = timestampDate ?
                message(code: "status.page.status.timestamp",
                        args: [new SimpleDateFormat(dateTimeFormat,
                        currentLocale).format(timestampDate)]) :
                message(code: "status.page.status.noData")

            def usedDisk = operatingSystemService.formatBytes(
                     statisticsService.getSystemUsedDiskspace())
            def usedRepo = operatingSystemService.formatBytes(
                     statisticsService.getRepoUsedDiskspace())
            def freeRepo = operatingSystemService.formatBytes(
                     statisticsService.getRepoAvailableDiskspace())

            model << [label: message(code: 'status.page.status.throughput'),
                value: networkingService.formatThroughput(
                    statisticsService.getThroughput(), currentLocale)]

            model << [label: message(code: 'status.page.status.space.header'),
                 value: timestampString]
            model << [label: message(code: 'status.page.status.space.system'),
                 value: usedDisk]
            model << [label: message(code: 'status.page.status.space.repos'),
                 value: usedRepo]
            model << [label: message(code: 'status.page.status.space.avail'),
                 value: freeRepo]
        }
        if (server.replica) {
            model << [label: message(code: 'status.page.status.master_latency'),
                value:  operatingSystemService.truncate(
                    statisticsService.getLatency(), 2) + " " +
                    message(code: 'general.measurement.milliseconds.short')]
            model << [label: message(
                code: 'status.page.status.users_cache.number'),
                value: statisticsService.getNumUsersCached()]
            model << [label: message(
                code: 'status.page.status.users_cache.timeout'),
                value: currentConfig.positiveExpirationRate + " " + 
                    message(code: 'general.measurement.minutes')]
            model << [label: message(
                code: 'status.page.status.users_cache.percent'),
                value: operatingSystemService.truncate(
                    statisticsService.getUserCachePercentageHit() *
                        100, 2) + " %"]
        }
        return model
   }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
    def start = {
        def server = lifecycleService.getServer()
        if (!server) {
            flash.error = message(code: 'server.error.general')
            redirect action: 'index', id: params.id
            return
        }

        server.properties = params
        if (!server.hasErrors() && server.save()) {
            try {
                def result = lifecycleService.startServer()
                if (result < 0) {
                    flash.warn = message(code: 'server.status.alreadyRunning')
                } else if (result == 0) {
                    flash.message = message(code: 'server.status.isRunning')
                } else {
                    flash.error = message(code: 'server.status.errorStarting')
                }
            } catch (CantBindPortException startServiceException) {
                flash.error = startServiceException.getMessage(
                    RCU.getLocale(request))
            }

        } else {
            flash.error = message(code: 'server.status.invalidSettings')
        }

        redirect(action:'index')
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
    def stop = {
        lifecycleService.stopServer()
        flash.message = message(code: 'server.status.stopped')
        redirect(action:'index')
    }
}
