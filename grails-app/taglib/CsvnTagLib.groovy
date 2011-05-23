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
import com.collabnet.svnedge.integration.command.AbstractCommand
 
class CsvnTagLib {

     def securityService

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

    /**
     * this custom tag will create a password field like the standard input tag,
     * but the "value" will be replaced with a same-length placeholder. A hidden
     * field by the same name (+ '_changed') will indicate a user edit
     */
    def passwordFieldWithChangeNotification = { attrs ->

      String fieldName = attrs.name ?: ""
      String fieldValue = attrs.value ?: ""
      String size = attrs.size ?: ""
      def fieldValueLength = fieldValue?.length()
      String pwdToken = (fieldValueLength) ? securityService.generateAlphaNumericPassword(fieldValueLength) : ""

      out << """
        <input type="hidden" name="${fieldName}_changed" id="${fieldName}_changed" value="false"/>
        <input type="password" name="${fieldName}" id="${fieldName}" value="${pwdToken}" size="${size}"/>
        <script>
        \$('${fieldName}').observe('change', function(event){
            \$('${fieldName}_changed').value = 'true'
        })
       </script>
       """
    }

    /**
     * Given 
     * @param command
     * @return the code name of the commad.
     */
    def makeCommandCodeName = { attrs ->
        def className = attrs.className ?: ""
        out << className ? AbstractCommand.makeCodeName(className).trim() : className.trim()
    }

}
