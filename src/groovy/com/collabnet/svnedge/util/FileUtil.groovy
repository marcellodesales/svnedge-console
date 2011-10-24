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

/**
 * general file-handling util methods
 */
public class FileUtil {

    static Log log = LogFactory.getLog(FileUtil.class)

    /**
     * Utility method to unzip an archive to a given directory
     * @param zipFile the archive to unzip
     * @param parentDir the location into which to unzip
     */
    public static void unzipFileIntoDirectory(File zipFile, File parentDir) {
        def ant = new AntBuilder()
        ant.unzip(src: zipFile.absolutePath,
              dest: parentDir.absolutePath,
              overwrite: "true")
    }
}