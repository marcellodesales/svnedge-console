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

import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.replica.manager.RepoStatus

import org.apache.log4j.Logger

/**
 * This action removes the repository from the filesystem, sets the
 * repository status to REMOVED, and disables sync for the repository.
 */
public class RepoRemoveCommand extends AbstractActionCommand {
    private Logger log = Logger.getLogger(getClass())
    
    def repoFileDir
    def repoDbTuple

    def constraints() {
        def repoRecord = Repository.findByName(this.params["repoName"])
        if (repoRecord) {
            this.repoDbTuple = ReplicatedRepository.findByRepo(repoRecord)
        }
        if (!this.repoDbTuple) {
            throw new IllegalStateException("The repository '" + 
                this.params["repoName"] + "' does not exist.")

        } else if (this.repoDbTuple.getStatus() == RepoStatus.REMOVED) {
            throw new IllegalStateException("The repository '" + 
                this.params["repoName"] + "' has already been removed.")
        }
    }

    def execute() {
        log.debug("Acquiring the svn notifications service...")
        def svn = getService("svnNotificationService")

        log.debug("Removing the repository '"+this.params["repoName"]+ 
            "' from the database...")
        svn.removeRepositoryOnDatabase(this.params["repoName"])

        log.debug("Removing the repository '"+this.params["repoName"]+ 
            "' from the file system...")
        svn.removeRepositoryOnFileSystem(this.params["repoName"])
    }

    def undo() {
       log.debug("No undo action for remove command.")
    }
}
