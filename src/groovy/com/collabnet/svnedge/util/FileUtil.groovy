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

import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log

/**
 * general file-handling util methods
 */
public class FileUtil {

    static Log log = LogFactory.getLog(FileUtil.class)

    /**
     * Utility method to unzip an archive to a given directory
     * @param zipFile
     * @param parentDir
     */
    public static void unzipFileIntoDirectory(ZipFile zipFile, File parentDir) {

        File f
        InputStream eis
        byte[] buffer
        int bytesRead

        zipFile.entries().each { entry ->
            try {
                eis = zipFile.getInputStream(entry)
                buffer = new byte[1024]
                bytesRead = 0

                f = new File(parentDir.getAbsolutePath() + File.separator + entry.getName())
                if (entry.isDirectory()) {
                    f.mkdirs()
                }
                else {
                    f.parentFile.mkdirs()
                    while ((bytesRead = eis.read(buffer)) != -1) {
                        f.append(buffer)
                    }
                }
            } catch (IOException e) {
                log.error ("unable to write zip entry: " + e.getMessage(), e)
            }
        }
        try {
            zipFile.close()
        } catch (IOException e) {
            log.error ("unable to close zip file", e)
        }
    }

}