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
package com.collabnet.svnedge.console.services

import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.ServerMode

import org.springframework.transaction.annotation.Transactional

class SvnRepoService {

    // service dependencies
    def operatingSystemService
    def lifecycleService
    def commandLineService
    def serverConfService

    // configuration properties
    String appHome
    String svnadminPath
    String svnPath

    boolean transactional = false

    def bootStrap = { config ->
        log.debug("Bootstrapping svnRepoService")

        appHome = ConfigUtil.appHome(config)
        svnPath = ConfigUtil.svnPath(config)
        svnadminPath = ConfigUtil.svnadminPath(config)
    }

    /**
     * Creates a new repository.
     *
     * @param useTemplate If true a basic trunk/branches/tags structure
     * will be generated.
     */
    def createRepository(Repository repo, boolean useTemplate) {
        Server server = lifecycleService.getServer()
        def repoPath = this.getRepositoryHomePath(repo)
        def exitStatus = commandLineService.executeWithStatus(svnadminPath,
                "create", repoPath)
        if (exitStatus == 0 && useTemplate) {
            log.debug("Created repository " + repoPath +
                    ". Adding default paths...")
            def fileURL = {
                return commandLineService.createSvnFileURI(
                        new File(repoPath, it))
            }
            exitStatus = commandLineService.executeWithStatus(svnPath, "mkdir",
                    fileURL("trunk"), fileURL("branches"), fileURL("tags"),
                    "-m", "Creating_initial_branch_structure",
                    "--no-auth-cache", "--non-interactive") // --quiet"
        }
        return exitStatus
    }

    /**
     * @param repo is the instance of a repository.
     * @return the canonical path to the repository in the file system.
     */
    def getRepositoryHomePath(Repository repo) {
        Server server = lifecycleService.getServer()
        return new File(server.repoParentDir, repo.name).canonicalPath
    }

    /**
     * Moves the repository contents to an inaccessible location to be
     * archived or otherwise further processing
     */
    def deleteRepository(Repository repo) {
        Server server = lifecycleService.getServer()
        File repoToDelete = new File(this.getRepositoryHomePath(repo))
        File f = new File(new File(server.repoParentDir).getParentFile(), 
            "deleted-repos")
        if (!f.exists()) {
            f.mkdir()
        }
        def count = 0
        File repoArchiveLocation = new File(f, repo.name)
        while (repoArchiveLocation.exists()) {
            repoArchiveLocation = new File(f, repo.name + "." + (++count))
        }
        repoToDelete.renameTo(repoArchiveLocation)
        return "Delete repository " + repo.name + " new location is " +
                repoArchiveLocation.getAbsolutePath()
    }

    /**
     * Syncs the Repository table with the contents of the svn server 
     * repositories directory
     */
    def syncRepositories() {

        log.info("Syncing SVN repo folders to database")
        Server server = lifecycleService.getServer()

        // fetch the repo directories (skipping hidden)
        def files = Arrays.asList(new File(server.repoParentDir).listFiles(
            {file -> isRepository(file) }  as FileFilter))

        def repoFolderNames = files.collect { it.name }

        // create DB rows for repositories IN SVN but not in DB
        repoFolderNames.each { folder ->
            if (!Repository.findByName(folder)) {
                log.debug("Adding Respository row to represent folder: " +
                    "${folder}")
                def r = new Repository(name : folder, permissionsOk : true)

                // if Windows, assume permissions are OK; otherwise, validate
                if (!operatingSystemService.isWindows()) {
                    r.permissionsOk = validateRepositoryPermissions(r)
                }
                r.save()
            }
        }

        // remove DB rows for repositories NOT IN SVN
        def reposToDelete = []
        Repository.list().each  { repo ->
            if (!repoFolderNames.contains(repo.name)) {
                log.debug("Deleting Repository row with no matching folder: " +
                    "${repo.name}")
                reposToDelete << repo.name
            }
        }
        reposToDelete.each { it -> Repository.findByName(it).delete() }


    }

    /**
     * determine if a File resembles an SVN repo
     */
    boolean isRepository(File f) {

        boolean isDirectory = f.exists() && f.isDirectory()
        boolean hasExpectedChildren = (f.listFiles(
            {file -> file.name == "format" }  as FileFilter))

        return isDirectory && hasExpectedChildren

    }


    /**
     * This action will merely set Repo.permissionsOk = true
     * TODO in a future story it should sudo chown the repo directory
     */
    def updateRepositoryPermissions(Repository repo) {
         repo.permissionsOk = true
         repo.save(flush:true)
    }

    /**
     * @param repoStatus is the current status.
     * @param server is the current server instance.
     * @return the formatted string for the repository.
     */
    def formatRepoStatus(repoStatus) {
        def server = lifecycleService.getServer()
        def buffer = new StringBuilder()
        if (repoStatus.size() == 0) {
            if (server.replica) {
                return "No repositories have been added yet."
            } else {
                def num = Repository.list().size()
                if (num == 0) {
                    buffer.append "There are no repositories yet."
                } else {
                    buffer.append "Total repositories: ${num}."
                }
                if (server.mode == ServerMode.STANDALONE) {
                   buffer.append " Go to the Repositories tab to create" 
                   buffer.append " or discover repositories."
                }
                return buffer.toString()
           }
       }
       repoStatus.eachWithIndex { it, index ->
           buffer.append(it.count + " " + it.status)
           if (index < repoStatus.size() - 1) {
               buffer.append(", ")
           }
       }
       return buffer.toString()
   }

     /**
      * Validates whether the httpdUser and group match the ownership of the 
      * input Repo dir
      * @param repo
      * @return boolean indicator
      */
    boolean validateRepositoryPermissions(Repository repo) {
        def server = lifecycleService.getServer()
        def repoPath = this.getRepositoryHomePath(repo)

        //Sometimes ls -ld output coloumns are separated by double space.
        //For ex drwxr-xr-x  7 rajeswari __cubitu 4096 May 14 01:45 data/

        String[] result = commandLineService.executeWithOutput("ls", "-dl",
            repoPath).replaceAll(" +", " ").split(" ")
        String user = result.length > 2 ? result[2] : "nobody"
        String group = result.length > 3 ? result[3] : "nobody"

        return (user == serverConfService.httpdUser && 
            group == serverConfService.httpdGroup)
    }
    

}
