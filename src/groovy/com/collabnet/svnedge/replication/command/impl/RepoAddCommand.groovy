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
package com.collabnet.svnedge.replication.command.impl

import org.apache.log4j.Logger

import com.collabnet.svnedge.replication.command.AbstractRepositoryCommand
import com.collabnet.svnedge.replication.command.LongRunningCommand


/**
 * This command adds a repository into the system, by using the svn service.
 * 
 * @author John Mcnally (jmcnally@collab.net)
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public class RepoAddCommand extends AbstractRepositoryCommand 
        implements LongRunningCommand {

    private Logger log = Logger.getLogger(getClass())

    def constraints() {
        def repoName = getRepoName()
        log.debug("Acquiring the replica commands executor service...")
        if (!this.params["repoName"]) {
            throw new IllegalArgumentException("The repo path is missing.")
        }
    }

    def execute() {
        def repoName = getRepoName()

        log.debug("Creating a new repository on the database...")
        addRepositoryOnDatabase(repoName)

        log.debug("Creating a new repository on the file system...")
        createRepositoryOnFileSystem(repoName)
    }

    def undo() {
        log.debug("Acquiring the command executor service...")
        removeReplicatedRepository(getRepoName())
    }
}
