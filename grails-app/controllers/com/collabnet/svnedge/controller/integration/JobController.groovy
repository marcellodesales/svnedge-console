/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.controller.integration


import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.codehaus.groovy.grails.plugins.springsecurity.Secured

import com.collabnet.svnedge.domain.integration.CtfServer;
import com.collabnet.svnedge.domain.integration.ReplicaConfiguration 
import com.collabnet.svnedge.integration.command.AbstractCommand;
import com.collabnet.svnedge.integration.command.CommandState;
import com.collabnet.svnedge.integration.command.LongRunningCommand;
import com.collabnet.svnedge.integration.command.ShortRunningCommand;


import com.collabnet.svnedge.domain.ServerMode
import com.collabnet.svnedge.domain.Server;

@Secured(['ROLE_USER'])
class JobController {

    def replicaServerStatusService
    def jobsInfoService
    def replicaCommandSchedulerService
    def applicationContext
    def ctfRemoteClientService
    def config = ConfigurationHolder.config
    def securityService

    // start and stop actions use POST requests
    //static allowedMethods = [start:'POST']

    def index = {
        redirect(action:'list')
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
    def list = {
        def longRunning = replicaServerStatusService.getCommandsByType(
            LongRunningCommand.class)

        def shortRunning = replicaServerStatusService.getCommandsByType(
            ShortRunningCommand.class)

        def backgroundJobsRunning = jobsInfoService.runningJobs.values()
        def backgroundJobsFinished = jobsInfoService.finishedJobs.values()

        def scheduledCommands = new LinkedHashSet<AbstractCommand>()

        def longRunningCommands = new ArrayList<AbstractCommand>()
        for (cmd in longRunning) {
            if (cmd.state == CommandState.RUNNING || cmd.state == CommandState.TERMINATED) {
                longRunningCommands << cmd

            } else {
                scheduledCommands << cmd
            }
        }
        def shortRunningCommands = new ArrayList<AbstractCommand>()
        for (cmd in shortRunning) {
            if (cmd.state == CommandState.RUNNING || cmd.state == CommandState.TERMINATED) {
                shortRunningCommands << cmd

            } else {
                scheduledCommands << cmd
            }
        }
        def replicaConfig = ReplicaConfiguration.getCurrentConfig()
        def runningCmdsSize = replicaServerStatusService.getAllCommandsSize()

        def dtFormat = message(code: "default.dateTime.format.withZone")
        def allCommands = []
        allCommands.addAll(longRunning)
        allCommands.addAll(shortRunning)
        
        // calling this after the queued command methods, so it is unlikely to contain
        // any commands which might be in the other lists.
        def unprocessedCommands = replicaCommandSchedulerService.listUnprocessedCommands()

        // default page model
        def modelMap = [

                logDateFormat: dtFormat,
                showLinksToCommandOutputLog: ConfigurationHolder.config.svnedge.replica.logging.commandOutput,
                unprocessedCommands: unprocessedCommands ,
                backgroundJobsRunning: backgroundJobsRunning,
                backgroundJobsFinished: backgroundJobsFinished,
        ]
        // replica-related additions
        if (Server.getServer().mode == ServerMode.REPLICA) {
            def replicaRelated = [
                    serverMode: ServerMode.REPLICA,
                    replicaName: replicaConfig.name,
                    totalCommandsRunning: longRunningCommands.size() +
                    shortRunningCommands.size(),
                    longRunningCommands: longRunningCommands,
                    shortRunningCommands: shortRunningCommands,
                    allCommands: allCommands,
                    scheduledCommands: scheduledCommands,
                    commandPollRate: replicaConfig.commandPollRate,
                    ctfUrl: CtfServer.getServer().getWebAppUrl(),
                    svnMasterUrl: replicaConfig.svnMasterUrl,
                    maxLongRunning: replicaConfig.maxLongRunningCmds,
                    maxShortRunning: replicaConfig.maxShortRunningCmds ]

            modelMap << replicaRelated
        }
        return modelMap
    }
}
