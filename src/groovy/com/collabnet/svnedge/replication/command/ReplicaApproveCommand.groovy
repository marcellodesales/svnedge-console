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
package com.collabnet.svnedge.replication.command

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.replica.manager.ReplicatedRepository

/**
 * This command updates the state of the replica server with the URL of the
 * Master repository after the Master has approved this server as a replica
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public class ReplicaApproveCommand extends AbstractActionCommand {

    private Logger log = Logger.getLogger(getClass())

    def repoFileDir
    def repoDbTuple

    def constraints() {
        // Verify if the parameter "scmUrl" exists.
        if (!this.params["scmUrl"]) {
            throw new IllegalStateException("The command does not have the " +
                "required parameter 'scmUrl'.")
        }
    }

    def execute() {
        log.debug("Acquiring the replica setup service...")
        def replicaService = getService("setupReplicaService")

        log.debug("Creating a new repository on the database...")
        replicaService.updateServerAfterApproval(this.params["scmUrl"])
   }

   def undo() {
       log.error("Execute failed... Undoing the command...")
    }
}
