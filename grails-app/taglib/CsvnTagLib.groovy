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

import com.collabnet.svnedge.domain.Server

class CsvnTagLib {

    /**
     * this custom tag will format the "size" input (bytes from file.length) into
     * a human-readable string (eg, "17.5 KB")
     */
    def formatFileSize = { attrs ->

        def labels = [' bytes', 'KB', 'MB', 'GB', 'TB']
        def size = attrs.size
        def label = labels.find {
            if (size < 1024) {
                true
            }
            else {
                size /= 1024
                false
            }
        }
        out << "${new java.text.DecimalFormat(attrs.format ?: '0.##').format(size)} $label"
    }

    def sslRedirect = { attrs ->

        // redirect to https if configured to do so
        if (request.scheme == 'http' && Server.getServer().useSslConsole) {
            def port = System.getProperty("jetty.ssl.port", "4434")
            def sslUrl = "https://${request.serverName}${port != "443" ? ":" + port : ""}${request.forwardURI}"
            out << "<meta http-equiv=\"refresh\" content=\"0;url=${sslUrl}\"/>"
        }
    }
}
