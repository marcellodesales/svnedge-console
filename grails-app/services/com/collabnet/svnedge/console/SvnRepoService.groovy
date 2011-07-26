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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import groovy.io.FileType;

import com.collabnet.svnedge.ValidationException;
import com.collabnet.svnedge.console.SchedulerBean
import com.collabnet.svnedge.console.SchedulerBean.Frequency
import com.collabnet.svnedge.domain.Repository 
import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.ServerMode 
import com.collabnet.svnedge.domain.integration.ReplicatedRepository 
import com.collabnet.svnedge.domain.statistics.StatValue 
import com.collabnet.svnedge.domain.statistics.Statistic 
import com.collabnet.svnedge.util.ConfigUtil;

import org.quartz.CronTrigger;
import org.quartz.SimpleTrigger
import com.collabnet.svnedge.admin.RepoDumpJob
import org.quartz.JobDataMap

class SvnRepoService extends AbstractSvnEdgeService {

    private static final String BACKUP_TRIGGER_GROUP = "Backup"
    private static final boolean ASCENDING = true
    private static final boolean DESCENDING = false
    

    // service dependencies
    def operatingSystemService
    def lifecycleService
    def commandLineService
    def serverConfService
    def statisticsService
    def jobsAdminService

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
     * Lists all the dump files generated for the given repository
     * @param repo A Repository object
     * @return List of File objects
     */
    List<File> listDumpFiles(repo, sortBy = "date", isAscending = false) {
        def files = []

        Server server = Server.getServer()
        File dumpDir = new File(server.dumpDir, repo.name)
        if (dumpDir.exists()) {
            dumpDir.eachFile(FileType.FILES) { f ->
                def name = f.name
                if (!name.endsWith("-processing") && !name.endsWith("-processing.zip")) {
                    files << f
                }
            }
        }
        int sign = isAscending ? 1 : -1
        switch (sortBy) {
            case "name":
                files = files.sort { a, b -> sign * (a.name <=> b.name) }
                break
            case "size":
                files = files.sort { f -> sign * f.length() }
                break
            case "date":
                files = files.sort { f -> sign * f.lastModified() }
                break
            default:
                files = files.sort { f -> -1 * f.lastModified() }
        }
        return files
    }

    /**
     * Hard delete of the specified repository dump file
     * 
     * @param filename
     * @param repo Repository object
     * @return true, if the delete was successful; false otherwise
     */
    boolean deleteDumpFile(filename, repo) throws FileNotFoundException {
        return getDumpFile(filename, repo).delete()
    }

    /**
     * Copies the contents of the specified repository dump file to the 
     * given stream
     *     
     * @param filename
     * @param repo Repository object
     * @param outputStream
     * @return true, if the file could be read
     */
    boolean copyDumpFile(filename, repo, outputStream) 
            throws FileNotFoundException {
        File dumpFile = getDumpFile(filename, repo)
        if (dumpFile.canRead()) {
            dumpFile.withInputStream {
                outputStream << it
            }
            return true
        } 
        return false
    }

    private File getDumpFile(filename, repo) throws FileNotFoundException {
        Server server = Server.getServer()
        File dumpDir = new File(server.dumpDir, repo.name)
        File dumpFile = new File(dumpDir, filename)
        if (dumpFile.exists()) {
            return dumpFile
        }
        throw new FileNotFoundException(filename)
    }

    List retrieveScheduledBackups(repo) {
        List backups = []
        def tName = "RepoDump-${repo.name}"
        def tGroup = "Backup"
        def trigger = jobsAdminService.getTrigger(tName, tGroup)
        if (trigger) {
            DumpBean bean = DumpBean.fromMap(trigger.jobDataMap)
            backups << bean
        }
        return backups
    }
    
    /**
     * method to schedule a RepoDump quartz job
     * @param bean the parameters for the dump job
     * @param repo the repo ni question
     * @return the filename the dumpfile is expected to have
     */
    String scheduleDump(DumpBean bean, repo) {
        def tName = "RepoDump-${repo.name}"
        def tGroup = bean.backup ? BACKUP_TRIGGER_GROUP : "AdhocDump"
        SchedulerBean schedule = bean.schedule
        def trigger
        if (!schedule.frequency || schedule.frequency == Frequency.NOW) {
            schedule.frequency = Frequency.ONCE
            Calendar cal = Calendar.getInstance()
            cal.setTimeInMillis(System.currentTimeMillis() + 1000)
            schedule.second = cal.get(Calendar.SECOND)
            schedule.minute = cal.get(Calendar.MINUTE)
            schedule.hour = cal.get(Calendar.HOUR_OF_DAY)
            schedule.dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            schedule.month = cal.get(Calendar.MONTH) + 1 // Calendar uses 0 for first month
            schedule.year = cal.get(Calendar.YEAR)
        }

        String seconds = (schedule.second < 0) ? "0" : "${schedule.second}"
        String minute = " ${schedule.minute}"
        String hour = " ${schedule.hour}"
        String dayOfMonth = " *"
        String month = " *"
        String dayOfWeek = " ?"
        String year = ""
        switch(schedule.frequency) {
            case Frequency.WEEKLY:
                dayOfWeek = " ${schedule.dayOfWeek}"
                dayOfMonth = " ?"
                break
            case Frequency.HOURLY:
                hour = " *"
                break
            case Frequency.DAILY:
                break
            case Frequency.ONCE:
                dayOfMonth = " ${schedule.dayOfMonth}"
                month = " ${schedule.month}"
                year = " ${schedule.year}"
        }
        String cron = seconds + minute + hour + dayOfMonth +
            month + dayOfWeek + year
        log.debug("Scheduling backup dump using cron expression: " + cron)
        trigger = new CronTrigger(tName, tGroup, cron)
        log.debug("cron expression summary:\n" + trigger.expressionSummary)
        trigger.setJobName(RepoDumpJob.name)
        trigger.setJobGroup(RepoDumpJob.group)

        // data for reporting status to quartz job listeners
        def progressLogFileName = "dump-progress-" + repo.name + ".log"
        def jobDataMap =
                [id: "repoDump-${repo.name}", repoId: repo.id,
                description: getMessage("repository.action.createDumpfile.job.description", [repo.name],
                        bean.userLocale),
                urlProgress: "/csvn/log/show?fileName=/temp/${progressLogFileName}&view=tail",
                urlResult: "/csvn/repo/dumpFileList/${repo.id}",
                urlConfigure: "/csvn/repo/bkupSchedule/${repo.id}" ]
        // data for generating the dump file
        jobDataMap.putAll(bean.toMap())
        trigger.setJobDataMap(new JobDataMap(jobDataMap))

        jobsAdminService.createOrReplaceTrigger(trigger)
        return dumpFilename(bean, repo)
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
        if (!bean.revisionRange) {
            bean.revisionRange = "0:" + findHeadRev(repo)
        }
        cmd << "-r"
        cmd << bean.revisionRange
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
        def progressLogFileName = "dump-progress-" + repo.name + ".log"
        File progressLogFile =
            new File(tempLogDir, progressLogFileName)
        if (progressLogFile.exists()) {
            throw new ValidationException("repository.action.createDumpfile.alreadyInProgress")
        }
    
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
                } finally {
                    out.close()
                }
                finishDumpFile(finalDumpFile, tempDumpFile, progress, progressLogFile)
                cleanupOldBackups(bean, finalDumpFile)
            }

        } else {
            log.debug("Dump: No filter")
            runAsync {
                try {
                    dumpProcess.waitForProcessOutput(out, progress)
                } finally {
                    out.close()
                }
                finishDumpFile(finalDumpFile, tempDumpFile, progress, progressLogFile)
                cleanupOldBackups(bean, repo, finalDumpFile)
            }
        }
        return filename
    }
    
    private finishDumpFile(finalDumpFile, tempDumpFile, progress, progressLogFile) {
        def dumpFilename = finalDumpFile.name
        if (dumpFilename.endsWith('.zip')) {
            progress << "Compressing dump file...\n"
            File tempZipFile = new File(tempDumpFile.parentFile, 
                                        tempDumpFile.name + ".zip")
            def baseDumpFilename = 
                dumpFilename.substring(0, dumpFilename.length() - 4)       
            ZipOutputStream zos = null
            try {
                zos = new ZipOutputStream(tempZipFile.newOutputStream())
                ZipEntry ze = new ZipEntry(baseDumpFilename)
                zos.putNextEntry(ze)
                tempDumpFile.withInputStream { zos << it }
                zos.closeEntry()
            } finally {
                if (zos) {
                    zos.close()
                }
            }
            progress << "Finished compressing dump file.\n"
            tempDumpFile.delete()
            tempDumpFile = tempZipFile
        }
        progress << "Moving dump file to final location.\n"
        if (!tempDumpFile.renameTo(finalDumpFile)) {
            log.warn("Rename of dump file " + tempDumpFile?.name + " to " +
                finalDumpFile?.name + " failed.")
        }
        progress << "Dump file " + finalDumpFile?.name + " is complete.\n"
        progress.close()
        progressLogFile.delete()
    }
    
    private cleanupOldBackups(dumpBean, repo, finalDumpFile) {
        int numToKeep = dumpBean.numberToKeep
        if (dumpBean.backup && numToKeep > 0) {
            def dumps = listDumpFiles(repo, "date", DESCENDING)
            int i = 0
            for (dumpFile in dumps) {
                def name = dumpFile.name
                if (name.startsWith(repo.name + "-bkup") && 
                    !name.endsWith("-processing") && 
                    !name.endsWith("-processing.zip") &&
                    (++i > numToKeep)) {
                    
                    dumpFile.delete()    
                }
            }
        }
    }
    
    private String pad(int value) {
        return (value < 10) ? "0" + value : String.valueOf(value)
    }
    
    private String dumpFilename(DumpBean bean, repo) {
        Calendar cal = Calendar.getInstance()
        SchedulerBean sched = bean.schedule
        String ts = ""
        ts += (sched.year < 1) ? cal.get(Calendar.YEAR) : sched.year
        ts += pad((sched.month < 1) ? cal.get(Calendar.MONTH) : sched.month)
        ts += pad((sched.dayOfMonth < 1) ? 
            cal.get(Calendar.DAY_OF_MONTH) : sched.dayOfMonth)
        ts += pad((sched.hour < 0) ? cal.get(Calendar.HOUR_OF_DAY) : sched.hour)
        ts += pad((sched.minute < 0) ? cal.get(Calendar.MINUTE): sched.minute)
        ts += pad((sched.second < 0) ? cal.get(Calendar.SECOND) : sched.second)
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
        def prefix = bean.backup ? repo.name + "-bkup" : repo.name
        return prefix + range + options + "-" + ts + ".dump" + zip
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
    
    /**
     * Removes all triggers for backup jobs on the given repository
     * 
     * @param repo Repository domain object
     */
    def clearScheduledDumps(repo) {
        def triggerNames = jobsAdminService
            .getTriggerNamesInGroup(BACKUP_TRIGGER_GROUP)
        if (triggerNames) {
            triggerNames.each {
                if (it.startsWith("RepoDump-${repo.name}")) {
                    jobsAdminService.removeTrigger(it, BACKUP_TRIGGER_GROUP)
                }
            }
        }

    }

   /**
    * helper to evaluate an svn instance url for support of httpv2
    * @param repoUrl
    * @param uname
    * @param password
    * @return boolean true if verified, false if unknown or verified no support
    */
    def boolean svnServerSupportsHttpV2(String repoUrl, String uname, String password) {

        // svn ls against the url, with config options exposed, will reveal markers of 1.7+ server
        def out = new StringBuffer()
        def err = new StringBuffer()
        def proc = "${ConfigUtil.svnPath()} ls ${repoUrl} --config-option servers:global:neon-debug-mask=130 --username ${uname} --password ${password}".execute()
        proc.waitForProcessOutput(out, err)

        // look for options headers returned only by 1.7+ server
        String errput = err.toString()
        if (errput.contains("SVN-Youngest-Rev") && errput.contains("SVN-Repository-UUID")) {
            return true
        }
        else {
            return false
        }


    }
}
