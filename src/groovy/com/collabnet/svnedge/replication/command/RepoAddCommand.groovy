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
 * This command adds a repository into the system, by using the svn service.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public class RepoAddCommand extends AbstractReplicaCommand {

    private Logger log = Logger.getLogger(getClass())

    def repoFileDir
    def repoDbTuple
    
    def constraints() {
        def repoName = getRepoName()
        log.debug("Acquiring the replica commands executor service...")
        def svn = getService("replicaCommandExecutorService")
        // do we want to check anything here?
    }

    def execute() {
        log.debug("Acquiring the command executor service...")
        def rceService = getService("replicaCommandExecutorService")

        def repoName = getRepoName()
        rceService.addReplicatedRepository(repoName)
    }

    def undo() {
        log.debug("Acquiring the command executor service...")
        def rceService = getService("replicaCommandExecutorService")
        def repoName = getRepoName()
        rceService.removeReplicatedRepository(repoName)
    }
    
    private String getRepoName() {
        String repoName = this.params["repoName"]
        int pos = repoName.lastIndexOf('/');
        if (pos >= 0 && repoName.length() > pos + 1) {
            repoName = repoName.substring(pos + 1)
        }
        return repoName
    }
}
