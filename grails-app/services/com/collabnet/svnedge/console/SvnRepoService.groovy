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
package com.collabnet.svnedge.console

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.collabnet.svnedge.ValidationException;
import com.collabnet.svnedge.domain.Repository 
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.ServerMode 
import com.collabnet.svnedge.domain.integration.ReplicatedRepository 
import com.collabnet.svnedge.domain.statistics.StatValue 
import com.collabnet.svnedge.domain.statistics.Statistic 
import com.collabnet.svnedge.util.ConfigUtil;

class SvnRepoService extends AbstractSvnEdgeService {

    // service dependencies
    def operatingSystemService
    def lifecycleService
    def commandLineService
    def serverConfService
    def statisticsService

    boolean transactional = false


    /**
     * Returns repository feature for give FS format.
     *
     * @param repo is the instance of a repository.
     * @param fsFormat is fsformat number of given repository. 
     * @return String
     *
     */
    def getRepoFeatures(Repository repo, int fsFormat) {
        def list = [
         "",
         "svndiff0",
         "svndiff1",
         "svndiff1, sharding, mergeinfo",
         "svndiff1, sharding, mergeinfo, rep-sharing, packed revs",
         "svndiff1, sharding, mergeinfo, rep-sharing, packed revs, packed revprops",
        ]

        def feature = ""

        if (fsFormat <=1) {
          feature = list.get(1)
        } else if (fsFormat >= list.size()) {
          feature = list.get(list.size() -1) 
        } else {
          feature = list.get(fsFormat)
        }

        return feature
    }


    /**
     * Returns repository UUID.
     *
     * @param repo is the instance of a repository.
     * @return uuid string
     *
     */
    def getReposUUID(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        def uuid = ""

        def f = new File(new File(repoPath, "db/uuid").canonicalPath)
        if (f.exists()) {
           uuid = f.readLines()[0]
        } else {
           log.warn("Missing $repoPath/db/uuid file...")
        }
        return uuid
    }


    /**
     * Returns repository fstype.
     *
     * @param repo is the instance of a repository.
     * @return fstype string
     *
     */
    def getReposFsType(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        def fsType = "FSFS"

        def f = new File(new File(repoPath, "db/fs-type").canonicalPath)
        if (f.exists()) {
           try {
               fsType = f.readLines()[0].toUpperCase()
           } catch (e) {
               log.debug("Reading from $repoPath/db/fs-type (" +
                         e.getMessage() +  "), Assuming FSFS as FS-Type.")
           }
        } else {
           log.warn("Missing $repoPath/db/fs-type file..." +
                    ", Assuming FSFS as FS-Type.")
        }
          
        return fsType
    }

    /**
     * Returns repository fs format
     *
     * @param repo is the instance of a repository.
     * @return repository fs format integer.
     */
    def getReposFsFormat(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        // As per the Subversion FS design docs, Svn asssumes fsformat as 1
        // in case db/format file is missing from repository. We follow the
        // same.
        def fsFormat = 1
        def f = new File(new File(repoPath, "db/format").canonicalPath)
        if (f.exists()) {
           try {
               fsFormat = f.readLines()[0].toInteger()
           } catch (e) {
               log.debug("Reading from $repoPath/db/format (" + e.getMessage() +
                         "), Assuming repository fs format to be '1'.")
               fsFormat = 1
           }
        } else {
           log.warn("Missing $repoPath/db/format file..." +
                    ", Assuming repository fs format to be '1'.")
        }
        return fsFormat
    }


    /**
     * Checks rep-sharing is enabled or disabled.
     *
     * @param repo is the instance of a repository.
     * @return Boolean
     */
    def getReposRepSharing(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        def repSharing = true
        def f = new File(new File(repoPath, "db/fsfs.conf").canonicalPath)
        if (!f.exists()) {
           log.warn("Missing $repoPath/db/fsfs.conf file...")
           return false
        }
        f.withReader {reader ->
             String line
             while ( (line = reader.readLine() ) != null ) {
                    line = line.trim()
                    if (line.matches("[# ]*enable-rep-sharing[ ]*=.*")) {
                        if (line.startsWith("#")) {
                            repSharing = true
                        } else {
                           String[] strsplit = line.split("=")
                           if (strsplit.length <= 2){
                              repSharing = Boolean.parseBoolean(strsplit[1].trim())
                           } else {
                              repSharing = true
                           } 
                        }
                        break
                    }
             }
        }

        return repSharing
    }


    /**
     * Returns repository format
     *
     * @param repo is the instance of a repository.
     * @return repository format integer.
     */
    def getReposFormat(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        def repoFormat = 0
        def f = new File(new File(repoPath, "format").canonicalPath)
        if (f.exists()) {
           try {
               repoFormat = f.readLines()[0].toInteger()
           } catch (e) {
               repoFormat = 0
               log.debug("Reading from $repoPath/format (" + e.getMessage() +
                         "), Assuming repository format to be '0'.")
           }
        } else {
           log.warn("Missing $repoPath/format file..." +
                    ", Assuming repository format to be '0'.")
        }
        return repoFormat
    }


    /**
     * Returns Sharding information.
     *
     * @param repo is the instance of a repository.
     * @return Sharding revision number in case its enabled else
     *         return -1.
     */
    def getReposSharding(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)
        def sharded = -1

        def f = new File(new File(repoPath, "db/format").canonicalPath)
        if (! f.exists()) {
            log.warn("Missing $repoPath/db/format file...")
            return -1
        }

        try { 
            f.withReader { reader ->
                 String line
                 while ( (line = reader.readLine() ) != null ) {
                        line = line.trim()
                        if (line.matches("^layout\\ sharded.*")) {
                            String[] strsplit = line.split("\\ ")
                            if (strsplit.length == 3) {
                                sharded = strsplit[2].toLong()
                            }
                            break
                        }
                 }
            }
        } catch (e) {
            sharded = -1
            log.debug("Reading from $repoPath/db/format (" + e.getMessage() +
                      "), Assuming repository sharding disabled.")
        }

        return sharded
    }


    /**
     * Returns the current head revision.
     *
     * @param repo is the instance of a repository.
     * @return revision number.
     */
    def findHeadRev(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        def f = new File(new File(repoPath, "db/current").canonicalPath)
        if (!f.exists()) {
            log.warn("Missing $repoPath/db/current file...")
            return 0
        }

        try {
           String[] strsplit = f.readLines()[0].split("\\ ")
           return strsplit[0].toInteger()
        } catch (Exception e) {
            log.error("Can't find head revision for repository " +  repoPath)
            return 0
        }
    }

    /**
     * Finds least packed revision
     *
     * @param repo is the instance of a repository.
     * @return revision number.
     */
    def findMinPackedRev(Repository repo) {
        def repoPath = this.getRepositoryHomePath(repo)

        def f = new File(new File(repoPath, "db/min-unpacked-rev").canonicalPath)
        if (!f.exists()) {
            log.warn("Missing $repoPath/db/min-unpacked-rev file...")
            return 0
        }

        try {
            return (f.readLines()[0]).toInteger()
        } catch (e) {  
            log.debug("Reading from $repoPath/db/min-unpacked-rev (" +
                     e.getMessage() + "), Assuming min-unpacked-rev to be '0'.")
            return 0
        }
    }

    /**
     * Creates a new repository.
     *
     * @param useTemplate If true a basic trunk/branches/tags structure
     * will be generated.
     */
    def createRepository(Repository repo, boolean useTemplate) {
        def repoPath = this.getRepositoryHomePath(repo)
        def exitStatus = commandLineService.executeWithStatus(
            ConfigUtil.svnadminPath(), "create", repoPath)
        if (exitStatus == 0 && useTemplate) {
            log.debug("Created repository " + repoPath +
                    ". Adding default paths...")
            def fileURL = {
                return commandLineService.createSvnFileURI(
                        new File(repoPath, it))
            }
            exitStatus = commandLineService.executeWithStatus(
                    ConfigUtil.svnPath(), "mkdir",
                    fileURL("trunk"), fileURL("branches"), fileURL("tags"),
                    "-m", "Creating_initial_branch_structure",
                    "--no-auth-cache", "--non-interactive") // --quiet"
        }
        return exitStatus
    }

    /**
    * Runs svnadmin verify on a repository.
    *
    * @return true, if no errors
    */
   def verifyRepository(Repository repo) {
       def repoPath = this.getRepositoryHomePath(repo)
       def exitStatus = commandLineService.executeWithStatus(
           ConfigUtil.svnadminPath(), "verify", repoPath)
       return (exitStatus == 0)
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
     * @param repo whose folder to move
     * @return String message about the new location
     */
    def archivePhysicalRepository(Repository repo) {
        def server = lifecycleService.getServer()
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
        return "Moved repository " + repo.name + " new location is " +
                repoArchiveLocation.getAbsolutePath()
    }

    /**
     * Deletes the repository contents from the file system
     * @param repo
     * @return boolean indicating success or failure
     */
    def deletePhysicalRepository(Repository repo) {
        File repoToDelete = new File(this.getRepositoryHomePath(repo))
        return repoToDelete.deleteDir();
    }

   /**
    * Removes a Repository from the DB (no SVN or filesystem action is taken) by
    * properly handling constraints that prevent direct delete
    * @param r
    */
    def removeRepository(Repository repo) {

        // delete FK'd stats first
        def stats = StatValue.findAllByRepo(repo)
        stats.each() {

            Statistic stat = it.statistic
            stat.removeFromStatValues(it)
            repo.removeFromStatValues(it)
            it.delete()
        }
        
        // delete FK'd ReplicatedRepository
        def replicatedRepos = ReplicatedRepository.findAllByRepo(repo)
        replicatedRepos.each() {
           it.delete()
        } 

        // delete the repo entity
        repo.delete()
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
        
        reposToDelete.each { it ->
           removeRepository(Repository.findByName(it))
        }

    }

    /**
     * determine if a File resembles an SVN repo
     */
    boolean isRepository(File f) {

        boolean isDirectory = f.exists() && f.isDirectory()
        boolean hasFormat = false
        boolean hasDb = false
        if (isDirectory) {
            f.eachFile {file -> 
                hasFormat |= (file.isFile() && file.name == "format") 
                hasDb |= (file.isDirectory() && file.name == "db") 
            }
        }
        return hasFormat && hasDb
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
    def formatRepoStatus(repoStatus, locale) {
        def server = lifecycleService.getServer()
        def buffer = new StringBuilder()
        if (repoStatus.size() == 0) {
            if (server.mode == ServerMode.REPLICA) {
                return getMessage("repository.status.notAdded", locale)
            } else {
                def num = Repository.list().size()
                if (num == 0) {
                    buffer.append getMessage("repository.status.noRepos", locale)
                } else {
                    buffer.append getMessage("repository.status.totalNumber",
                        [num], locale)
                }
                if (server.mode == ServerMode.STANDALONE) {
                   buffer.append " " + 
                       getMessage("repository.status.createNew", locale)
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
    
    /**
     * Method to invoke "svnadmin dump" possibly piped through svndumpfilter
     * 
     * @param bean dump options
     * @param repo domain object
     * @return dump filename
     */
    String createDump(DumpBean bean, repo) {
        Server server = Server.getServer()
        def cmd = [ConfigUtil.svnadminPath(), "dump"]
        cmd << new File(server.repoParentDir, repo.name).canonicalPath
        if (bean.revisionRange) {
            cmd << "-r"
            cmd << bean.revisionRange
        }
        if (bean.incremental) {
            cmd << "--incremental"
        }
        if (bean.deltas) {
            cmd << "--deltas"
        }

        File dumpDir = new File(server.dumpDir, repo.name)
        if (!dumpDir.exists()) {
            dumpDir.mkdirs()
        }        
        File tempLogDir = new File(ConfigUtil.logsDirPath(), "temp")
        if (!tempLogDir.exists()) {
            tempLogDir.mkdir()
        }
        File progressLogFile = 
            new File(tempLogDir, "dump-progress-" + repo.name + ".log")
        
        String filename = dumpFilename(bean, repo)
        File tempDumpFile = new File(dumpDir, filename + "-processing")
        File finalDumpFile = new File(dumpDir, filename)
        if (tempDumpFile.exists() || finalDumpFile.exists()) {
            throw new ValidationException("dumpBean.filename.exists", "filename")
        }
        
        log.debug("Dump command: " + cmd)
        Process dumpProcess = commandLineService.startProcess(cmd)
        FileOutputStream progress = new FileOutputStream(progressLogFile)
        FileOutputStream out = new FileOutputStream(tempDumpFile)

        if (!bean.deltas && bean.filter && (bean.includePath || bean.excludePath)) {
            log.debug("Dump: With filter")
            String svndumpfilterPath = new File(new File(
                ConfigUtil.svnadminPath()).parent, "svndumpfilter").canonicalPath
            def threads = []
            threads << dumpProcess.consumeProcessErrorStream(progress)
            if (bean.includePath) {
                def filterCmd = [svndumpfilterPath, "include"]
                addFilterOptions(bean, filterCmd)
                filterCmd.addAll(bean.includePathPrefixes)
                dumpProcess = dumpProcess.pipeTo(commandLineService.startProcess(filterCmd))
                threads << dumpProcess.consumeProcessErrorStream(progress)
            }
            if (bean.excludePath) {
                def filterCmd = [svndumpfilterPath, "exclude"]
                addFilterOptions(bean, filterCmd)
                filterCmd.addAll(bean.excludePathPrefixes)
                dumpProcess = dumpProcess.pipeTo(commandLineService.startProcess(filterCmd))
                threads << dumpProcess.consumeProcessErrorStream(progress)
            }
            threads << dumpProcess.consumeProcessOutputStream(out)
            runAsync {
                try {
                    for (t in threads) {
                        try {
                            t.join()
                        } catch (InterruptedException e) {
                             log.debug("Process consuming thread was interrupted")
                        }
                    }
                    finishDumpFile(finalDumpFile, tempDumpFile)
                } finally {
                    out.close()
                    progress.close()
                }
            }

        } else {
            log.debug("Dump: No filter")
            runAsync {
                try {
                    dumpProcess.waitForProcessOutput(out, progress)
                    finishDumpFile(finalDumpFile, tempDumpFile)
                } finally {
                    out.close()
                    progress.close()
                }
            }
        }
        return filename
    }
    
    private finishDumpFile(finalDumpFile, tempDumpFile) {
        def dumpFilename = finalDumpFile.name
        if (dumpFilename.endsWith('.zip')) {
            def baseDumpFilename = 
                dumpFilename.substring(0, dumpFilename.length() - 4)       
            ZipOutputStream zos = null
            try {
                zos = new ZipOutputStream(finalDumpFile.newOutputStream())
                ZipEntry ze = new ZipEntry(baseDumpFilename)
                zos.putNextEntry(ze)
                tempDumpFile.withInputStream { zos << it }
                zos.closeEntry()
            } finally {
                if (zos) {
                    zos.close()
                }
            }
            tempDumpFile.delete()
        } else {
            tempDumpFile.renameTo(finalDumpFile)
        }
    }
    
    private String dumpFilename(DumpBean bean, repo) {
        def ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
        def range = bean.revisionRange ?  
            "-r" + bean.revisionRange.replace(":", "_") : ""
        def options = ""
        if (bean.incremental) {
            options += "-incremental"
        }
        if (bean.deltas) {
            options += "-deltas"
        }
        if (bean.filter) {
            options += "-filtered"
        }
        def zip = bean.compress ? ".zip" : ""
        return repo.name + range + options + "-" + ts + ".dump" + zip
    }
    
    private addFilterOptions(DumpBean bean, filterCmd) {
        if (bean.dropEmptyRevs) {
            filterCmd << "--drop-empty-revs"
            if (bean.renumberRevs) {
                filterCmd << "--renumber-revs"
            }
        } else if (bean.preserveRevprops) {
            filterCmd << "--preserve-revprops"
        }
        
        if (bean.skipMissingMergeSources) {
            filterCmd << "--skip-missing-merge-sources"
        }
    }
}
