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
 * Class for common utility methods shared by controllers
 */
public class ControllerUtil {
    
    static Log log = LogFactory.getLog(ControllerUtil.class)

    /**
     * Helper to extract "list view selected ids" from a request parameter collection
     * @param params
     * @return list of id string corresponding to the selected item ids of a list view
     */
    public static List getListViewSelectedIds(Map params) {
        def ids = []
        params.each() {
            def matcher = it.key =~ /listViewItem_(.+)/
            if (matcher && matcher[0][1]) {
                def id = matcher[0][1]
                if (it.value == "on") {
                    ids << id
                }
            }
        }
        return ids
    }

    /**
     * Helper to stream a request body into a temporary file
     * @param request the HttpServletRequest whose body will be written as a file
     * @param targetFile empty file into which to write the file (optional -- temp file created
     * otherwise)
     * @return File handle to the temporary file
     */
    public static File getFileFromRequest(request, targetFile = null) {
        if (!targetFile) {
            targetFile = File.createTempFile("requestBody", ".tmp")
        }
        log.debug("Writing request body to file: ${targetFile.canonicalPath}")
        targetFile.withOutputStream { it << request.inputStream }
        return targetFile
    }


}
