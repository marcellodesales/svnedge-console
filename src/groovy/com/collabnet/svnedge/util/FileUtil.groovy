/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.util

import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.hyperic.sigar.FileInfo
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream

/**
 * General file-handling util methods. This class must be used as via spring bean "fileUtil", as it has
 * service dependencies
 */
public class FileUtil {

    static Log log = LogFactory.getLog(FileUtil.class)
    def operatingSystemService
    def commandLineService


    /**
     * Utility method to unzip an archive to a given directory
     * @param zipFile the archive to unzip
     * @param parentDir the location into which to unzip
     * @param progress the output stream for capturing output
     */
    public void extractArchive(File zipFile, File parentDir, OutputStream progress = null) {

        // use ant builder for windows
        if (operatingSystemService.isWindows()) {
            def ant = new AntBuilder()
            ant.unzip(src: zipFile.absolutePath,
                  dest: parentDir.absolutePath,
                  overwrite: "true")
            if (progress) {
                progress << "Unzipping '${zipFile.name}' to '${parentDir.absolutePath}'"
            }
        }
        // for unix, use the unzip util
        else {
            def cmd = ["unzip", zipFile.absolutePath, "-d", parentDir.absolutePath]
            commandLineService.execute(cmd, progress, progress)
        }

    }


    /**
     * Utility method to unzip an archive to a given directory using Apache Commons Compress
     * @param zipFile the archive to unzip
     * @param parentDir the location into which to unzip
     * @param progress the output stream for capturing output
     */
    public void extractArchiveCommonsCompress(File zipFile, File parentDir, OutputStream progress = null) {

        ZipArchiveInputStream zis = null
        try {
            zis = new ZipArchiveInputStream(new FileInputStream(zipFile))
            ZipArchiveEntry entry = null
            while ((entry = zis.getNextZipEntry()) != null) {

                File f = new File(parentDir.getAbsolutePath() + File.separator + entry.name)
                if (entry.isDirectory()) {
                    f.mkdirs()
                    if (progress) {
                        progress << "Creating dir: ${f.absolutePath}"
                    }
                }
                else {
                    f.parentFile.mkdirs()
                    if (progress) {
                        progress << "Inflating file: ${f.absolutePath}"
                    }
                    FileOutputStream out = new FileOutputStream(f)
                    out << zis
                }

                def mode = entry.getUnixMode()
                if (!operatingSystemService.isWindows() && mode > 0) {
                    def chmodCmd = ["chmod", "${mode}", "${f.absolutePath}"]
                    String[] result = commandLineService.execute(chmodCmd, progress, progress)
                }
            }
        }
        finally {
            zis?.close()
        }
    }

    /**
     * Utility method to zip a directory
     * @param directory
     * @param zipFile
     * @param progress the stream into which output is sent (optional)
     */
    public void archiveDirectory(File directory, File zipFile, OutputStream progress = null) {
        if (zipFile.parentFile.equals(directory)) {
            throw new IllegalArgumentException(
                    "Archive should not be written into the directory being archived")
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("directory argument was not a directory")
        }
        boolean storePermissions = !operatingSystemService.isWindows()

        ZipArchiveOutputStream zos = null
        try {
            zos = new ZipArchiveOutputStream(zipFile)
            recursiveArchiveDirectory(directory.canonicalPath.length() + 1,
                    directory, zos, storePermissions, progress)
        } finally {
            zos?.close()
        }
    }

    private void recursiveArchiveDirectory(int topLevelPathLength, File directory,
                                           ZipArchiveOutputStream zos, boolean storePermissions, OutputStream progress) {
        directory.eachFile { f ->
            String fullPath = f.canonicalPath
            String relativePath = fullPath.substring(topLevelPathLength, fullPath.length())
            if (f.isDirectory()) {
                relativePath += "/"
            }
            if (progress) {
                progress << relativePath + "\n"
            }
            ZipArchiveEntry ze = new ZipArchiveEntry(f, relativePath)
            if (storePermissions) {
                int perms = 0444
                try {
                    FileInfo fi = operatingSystemService.getSigar().getFileInfo(fullPath)
                    log.debug(relativePath + " permString=" + fi.permissionsString +
                            " asInt=" + fi.mode)
                    perms = Integer.parseInt(fi.mode.toString(), 8)
                } catch (IllegalStateException e) {
                    log.info("Sigar library not loaded, setting hotcopy file " +
                            "permissions for owner only with group and other as defaults")
                    if (f.isFile()) {
                        perms = 0444
                        if (f.canWrite() && f.canExecute()) {
                            perms = 0744
                        } else if (f.canWrite()) {
                            perms = 0644
                        } else if (f.canExecute()) {
                            perms = 0544
                        }
                    } else {
                        perms = 0755
                    }
                }
                ze.setUnixMode(perms)
            }
            zos.putArchiveEntry(ze)
            if (f.isFile()) {
                f.withInputStream { zos << it }
            }
            zos.closeArchiveEntry()

            if (f.isDirectory()) {
                recursiveArchiveDirectory(topLevelPathLength, f, zos,
                        storePermissions, progress)
            }
        }
    }
}