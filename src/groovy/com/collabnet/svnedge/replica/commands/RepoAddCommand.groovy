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
package com.collabnet.svnedge.replica.commands

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.springframework.context.ApplicationContext
//import com.collabnet.svnedge.replica.service.ActionCommandsIF
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
/**
 * This command adds a repository into the system, by using the svn service.
 * 
 * @author mdesales
 *
 */
public class RepoAddCommand extends AbstractActionCommand {

    private Logger log = Logger.getLogger(getClass())

    def repoFileDir
    def repoDbTuple
    
    def constraints() {
        log.debug("Acquiring the svn notifications service...")
        def svn = getService("svnNotificationService")

        this.repoFileDir = new File(svn.getReplicaParentDirPath() + "/" + 
                this.params["repoName"])

        //Verify if the file-system does not contain the repo dir.
        assert !this.repoFileDir.exists()

        def repoRecord = Repository.findByName(this.params["repoName"])
        if (repoRecord) {
            this.repoDbTuple = ReplicatedRepository.findByRepo(repoRecord)
        }
        // Verify if the command has not been created in the database or
        // has been created and removed.
        if (this.repoDbTuple) {
            assert this.repoDbTuple.getStatus() == RepoStatus.REMOVED
        }
        
    }

    def execute() {
        log.debug("Acquiring the svn notifications service...")
        def svn = getService("svnNotificationService")

        log.debug("Creating a new repository on the database...")
        svn.addRepositoryOnDatabase(this.params["repoName"])

        log.debug("Creating a new repository on the file system...")
        svn.createRepositoryOnFileSystem(this.params["repoName"])
   }

   def undo() {
       log.error("Execute failed... Undoing the command...")
       def svn = getService("svnNotificationService")

       //delete directory
       if (this.repoFileDir && this.repoFileDir.exists()) {
           svn.deleteDirectory(this.repoFileDir)
       }

       //delete db instance.
       this.repoDbTuple.delete()
    }
}
