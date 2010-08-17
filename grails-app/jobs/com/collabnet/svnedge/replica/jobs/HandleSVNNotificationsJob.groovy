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
package com.collabnet.svnedge.replica.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.collabnet.svnedge.console.services.JobsAdminService
import com.collabnet.svnedge.console.Server

class HandleSVNNotificationsJob {

    // svnsync the replica repositories
    def svnNotificationService
    def actionCommandsExecutorService

    static def group = JobsAdminService.REPLICA_GROUP

    // avoid re-entrance in case jobs are delayed. This will prevent multiple
    // calls to the Master.
    def concurrent = false

    static triggers = { 
    /*  
        simple name: "handleSVNNotificationTrigger", group: group + "_Triggers", \
        startDelay: 5660, \
        repeatInterval: \
        ConfigurationHolder.config.svnedge.replica.svn.svnsyncRate * 60000
    */
    }

    def execute() {
        log.info("Executing HandleSVNNotificationsJob")
        def server = Server.getServer()
        if (server && server.replica) {
            doExecute()
        }
    }

    private def doExecute() {
        println "Running svnNotificationService"
        def result = svnNotificationService.retrieveAndProcessSVNNotifications()
        def masterTimestamp = result.masterTimestamp
        def masterHasCommands = result.hasCommands
        if (masterHasCommands) {
            actionCommandsExecutorService.retrieveAndExecuteActionCommandsFromMaster()
        }
        println "Replica is in sync with master as of timestamp ${masterTimestamp}"
        println "Master has commands? " + masterHasCommands
    }
}
