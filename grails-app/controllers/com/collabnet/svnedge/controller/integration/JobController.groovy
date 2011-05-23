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
package com.collabnet.svnedge.controller.integration


import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import org.springframework.web.servlet.support.RequestContextUtils as RCU

import com.collabnet.svnedge.CantBindPortException;
import com.collabnet.svnedge.console.Command;
import com.collabnet.svnedge.domain.Repository 
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.ServerMode 
import com.collabnet.svnedge.domain.integration.ApprovalState 
import com.collabnet.svnedge.domain.integration.CtfServer 
import com.collabnet.svnedge.domain.integration.ReplicaConfiguration 
import com.collabnet.svnedge.integration.command.AbstractCommand;
import com.collabnet.svnedge.integration.command.CommandState;
import com.collabnet.svnedge.integration.command.CommandsExecutionContext;
import com.collabnet.svnedge.integration.command.LongRunningCommand;
import com.collabnet.svnedge.integration.command.ShortRunningCommand;

import java.text.SimpleDateFormat
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Secured(['ROLE_USER'])
class JobController {

    def replicaServerStatusService
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

    def sortRemoteCommands(commands) {
        def idComparator = [
        compare: {a,b->
            (a.id.replace("cmdexec","") as Integer) -
                (b.id.replace("cmdexec","") as Integer)
        }
        ] as Comparator
        commands.sort(idComparator)
    }

    def scheduleNew = {
        def server = Server.getServer()
        if (server.mode != ServerMode.REPLICA) {
            return
        }
        log.debug("Checking for replica commands...")

        def locale = Locale.getDefault()
        def ctfServer = CtfServer.getServer()
        def ctfPassword = securityService.decrypt(ctfServer.ctfPassword)

        boolean isCloseSoapSession = true
        def soapId, userSessionId

        try {
            soapId = ctfRemoteClientService.login(ctfServer.baseUrl,
                ctfServer.ctfUsername, ctfPassword, locale)
            userSessionId = ctfRemoteClientService.cnSoap(ctfServer.baseUrl)
                .getUserSessionBySoapId(soapId)

        } catch (Exception cantConnectCtfMaster) {
            log.error "Can't retrieve queued commands from the CTF replica " +
                "manager ${ctfServer.baseUrl}: " + cantConnectCtfMaster.getMessage()
        }

        def nameUpdate = params.newName
        def executionContext = new CommandsExecutionContext()
        executionContext.appContext = applicationContext
        executionContext.soapSessionId = soapId
        executionContext.userSessionId = userSessionId
        executionContext.ctfBaseUrl = ctfServer.baseUrl
        executionContext.locale = locale
        executionContext.logsDir = new File(config.svnedge.logsDirPath + "")
        def replica = ReplicaConfiguration.getCurrentConfig()
        executionContext.replicaSystemId = replica.systemId
        executionContext.activeCommands = new AtomicInteger(0)

        def remotecmdexecs = [[id:'cmdexec1022', repoName:null, code:'replicaPropsUpdate',
            params:[name: nameUpdate + (new Random().nextInt())]]]

        replicaCommandSchedulerService.offer(remotecmdexecs,
            executionContext)
        render {
            form(action: "scheduleNew")
                input(type: "text", name: "newName")
                input(type: "submit", value: "Update Replica Name")
            
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
    def list = {
        def longRunning = new LinkedList<Map<String, String>>()
        longRunning << [id:'cmdexec1003', code:'repoAdd', params: [repoName: "repo3"]]
        longRunning << [id:'cmdexec1002', code:'repoAdd', params: [repoName: "repo4"]]

        def shortRunning = new LinkedList<Map<String, String>>()
        shortRunning << [id:'cmdexec1001', repoName:null, code:'replicaApprove',
            params:[name:'replica title', desc:'super replica']]
        shortRunning << [id:'cmdexec1009', repoName:null, code:'replicaPropsUpdate',
            params:[name:'Super Replica', maxReplicacmdexecs:3]]
        shortRunning << [id:'cmdexec1012', params: [repoName: "repo1"], code:'repoSync']
        shortRunning << [id:'cmdexec1014', repoName:null, code:'replicaPropsUpdate'
            , params:[name:'Replica Brisbane']]
        shortRunning << [id:'cmdexec1008', repoName:null, code:'replicaPropsUpdate'
            , params:[maxReplicacmdexecs:3, maxRepositorycmdexecs: 10]]
        shortRunning << [id:'cmdexec1006', params: [repoName: "repo2"], code:'repoSync']

        def executionContext = new CommandsExecutionContext()
        def scheduledCommands = new LinkedHashSet<AbstractCommand>()

        def longRunningCommands = new ArrayList<AbstractCommand>()
        for (cmdMap in longRunning) { 
            cmdMap.context = executionContext
            def cmd = AbstractCommand.makeCommand(this.getClass().getClassLoader(), cmdMap)
            def statix = new Random().nextInt(1000)
            cmd.makeTransitionToState(CommandState.values()[1])
            longRunningCommands << cmd
        }
        def shortRunningCommands = new ArrayList<AbstractCommand>()
        for (cmdMap in shortRunning) {
            cmdMap.context = executionContext
            def cmd = AbstractCommand.makeCommand(this.getClass().getClassLoader(), cmdMap)
            def statix = new Random().nextInt(1000)
            cmd.makeTransitionToState(CommandState.values()[statix <= 500 ? 0 : 1])
            if (cmd.state == CommandState.RUNNING) {
                shortRunningCommands << cmd

            } else {
                scheduledCommands << cmd
            }
        }

//        def longRunningCommands = replicaServerStatusService.getCommandsByType(
//            LongRunningCommand.class)
//        def shortRunningCommands = replicaServerStatusService.getCommandsByType(
//            ShortRunningCommand.class)

        def replicaConfig = new ReplicaConfiguration()
//        def runningCmdsSize = replicaServerStatusService.getAllCommandsSize()

        def dtFormat = message(code: "default.dateTime.format.withZone")
        def allCommands = []
        allCommands.addAll(longRunning)
        allCommands.addAll(shortRunning)

        return [commandPollRate: replicaConfig.commandPollRate,
            maxLongRunningCmds: replicaConfig.maxLongRunningCmds,
            maxShortRunningCmds: replicaConfig.maxShortRunningCmds,
            totalCommandsRunning: longRunningCommands.size() + 
                shortRunningCommands.size(),
            longRunningCommands: longRunningCommands,
            shortRunningCommands: shortRunningCommands,
            allCommands: allCommands,
            scheduledCommands: scheduledCommands,
            logDateFormat: dtFormat]
    }
}
