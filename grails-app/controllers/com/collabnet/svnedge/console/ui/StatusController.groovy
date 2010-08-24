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
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.CantBindPortException

import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.replica.manager.ReplicaConfig
import com.collabnet.svnedge.replica.manager.ApprovalState

import java.net.NoRouteToHostException
import com.collabnet.svnedge.console.ServerMode

@Secured(['ROLE_USER'])
class StatusController {

    def operatingSystemService
    def networkingService
    def svnRepoService
    def quartzScheduler
    def realTimeStatisticsService
    def lifecycleService
    def packagesUpdateService

    // start and stop actions use POST requests
    static allowedMethods = [start:'POST', stop:'POST']

    def index = {
       def server = Server.getServer()
       def defaultMaster = Master.getDefaultMaster()
       def currentReplica = ReplicaConfig.getCurrentConfig()
       if (server.replica) { 
           if (!defaultMaster.isActive && !currentReplica.getState()) {
               redirect(controller:"wizard", action:"index")
           }

           if(!currentReplica) {
               flash.error = "Replica not bootstraped!"
           }
           if (currentReplica.getState() == ApprovalState.PENDING) {
               flash.message = "The Replica has not yet been approved by the " \
               + "Master." 
           } else if (currentReplica.getState() == ApprovalState.DENIED) {
               flash.error = "The Replica has been denied access to this " \
               + "Master.  Please change the master or consult with the administrator of the master."
           } else if (currentReplica.getState() == ApprovalState.NOT_FOUND 
                      || currentReplica.getState() == ApprovalState
                      .REGISTRATION_FAILED) {
               flash.error = "The Replica cannot be registered to the Master." +
                   " Please change the master or consult with the administrator of the master."
           }
        }
        def ctfUrl
        if (server.managedByCtf()) {
            ctfUrl = server.getManagedServer().getWebAppUrl()
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
                       flash.warn = this.packagesUpdateService.
                           getUpgradeAvailableMessage()
                   }
               }
               //system restart has priority over the updates
               if (this.packagesUpdateService.systemNeedsRestart()) {
                   flash.warn = this.packagesUpdateService.
                           getSystemNeedsRestartMessage()
               }
               //if the system has recently been updated
               if (this.packagesUpdateService.hasTheSystemBeenUpdated() || 
                       this.packagesUpdateService.hasTheSvnServerBeenUpdated()){
                   this.packagesUpdateService.setTheSystemToNotBeenUpdated()
                   flash.message = "Software updates installed successfully!"
               } else
               if (this.packagesUpdateService.hasNewPackagesBeenInstalled()) {
                   this.packagesUpdateService.setTheSystemToNotBeenUpdated()
                   flash.message = "New packages installed successfully!"
               }
           }

       } catch (NoRouteToHostException nrth) {
           if (!flash.error) {
               flash.error = "There's no network connection to the packages " +
                   "repository server."
           }

       } catch (Exception e) {
           e.printStackTrace()
           flash.error = "An error occurred while checking for software " +
                   "updates: " + e.getMessage()
       }

       return [isStarted: isStarted,
               isDefaultPortAllowed: lifecycleService.isDefaultPortAllowed(),
               sampleRepo: sampleRepo,
               defaultMaster: defaultMaster, 
               currentReplica: currentReplica,
               server: server,
               perfStats: getPerfStats(currentReplica, server),
               softwareVersion: sfVersion,
               svnVersion: svnVer,
               ctfUrl: ctfUrl
               ]
    }

   def getPerfStats(currentConfig, server) {
       def model = [
           [label:"Running since", 
            value: quartzScheduler.getMetaData().runningSince]]
       if (!server.managedByCtf()) {
           model << [label:"Repo health", value: 
               svnRepoService.formatRepoStatus(realTimeStatisticsService
                                               ?.getReposStatus())]
       }
        if (operatingSystemService.isReady()) {
            model << [label:"Throughput on primary interface", value:
                networkingService.formatThroughput(realTimeStatisticsService
                                                    ?.getThroughput())]
             model << [label:"Used space on root volume", value:
                operatingSystemService.formatBytes(realTimeStatisticsService
                                                    ?.getSystemUsedDiskspace())]
             model << [label:"Used space by repositories", value:
                operatingSystemService.formatBytes(realTimeStatisticsService
                                                    ?.getRepoUsedDiskspace())]
             model << [label:"Free space on repository volume", value:
                operatingSystemService.formatBytes(realTimeStatisticsService
                                                 ?.getRepoAvailableDiskspace())]
        }
        if (server.replica) {
            model << [label:"Latency from master", value: 
                operatingSystemService.truncate(
                    realTimeStatisticsService?.getLatency(), 2) + " ms"]
            model << [label:"Users in local cache", value: 
                realTimeStatisticsService?.getNumUsersCached()]
            model << [label:"User cache timeout", value: 
                currentConfig.positiveExpirationRate + " minutes"]
            model << [label:"User cache hit %", value: 
                operatingSystemService.truncate(realTimeStatisticsService
                         ?.getUserCachePercentageHit() * 100, 2) + " %"]
        }
        return model
   }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
    def start = {
        def server = lifecycleService.getServer()
        if (!server) {
            flash.error = "Server data not found."
            redirect action: 'index', id: params.id
            return
        }

        server.properties = params
        if (!server.hasErrors() && server.save()) {
            try {
                def result = lifecycleService.startServer()
                if (result < 0) {
                    flash.warn = "Subversion server was already running."
                } else if (result == 0) {
                    flash.message = "Subversion server is running."
                } else {
                    flash.error = "There was a problem starting the " +
                        "Subversion server!"
                }
            } catch (CantBindPortException startServiceException) {
                flash.error = startServiceException.getMessage()
            }

        } else {
            flash.error = "Invalid Subversion server settings!"
        }

        redirect(action:'index')
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
    def stop = {
        lifecycleService.stopServer()
        flash.message = "The Subversion server has been stopped."
        redirect(action:'index')
    }
}
