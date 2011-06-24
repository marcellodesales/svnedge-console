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
     * @return the code name of the command.
     */
    def makeCommandCodeName = { attrs ->
        def className = attrs.className ?: ""
        out << className ? AbstractCommand.makeCodeName(className).trim() : className.trim()
    }
    
    /**
     * @return the description of a command with a link to the repo name.
     */
   def replicaCommandDescription = { attrs ->
       def repoName = attrs.repoName ?: ""
       def masterUrl = attrs.masterUrl ?: ""
       def commandDescription = attrs.commandDescription ?: ""
       def repoNameLink = "<a target=\"${repoName}\" href=\"${masterUrl}/${repoName}\">${repoName}</a>"
       out << commandDescription.replace(repoName, repoNameLink)
   }

    /**
     * this tag will output the check box for a list view "select all" checkbox
     * @return the select all checkbox
     */
    def listViewSelectAll = { attrs ->
        out << "<input type='checkbox' id='listViewSelectAll' name='listViewSelectAll'/>"
    }

    /**
     * this tag will output the check box for a list view item row
     * @attr item REQUIRED the item whose "id" attribute will be used for naming this field
     * @return the item row checkbox
     */
    def listViewSelectItem = { attrs ->
        out << "<input type='checkbox' class='listViewSelectItem' id='listViewItem_${attrs.item.id}' name='listViewItem_${attrs.item.id}'/>"
    }

    /**
     * This tag will create a "multi-select" action button for use
     * in list views
     * @attr action REQUIRED the controller action to execut
     * @attr minSelected the min number of items selected to enable the button
     * @attr maxSelected the max number of items selected to enable the button
     * @attr confirmMessage the confirmation message to display before allowing the action
     * @body the value to display as the button text
     * @return the button html
     */
    def listViewActionButton = { attrs, body ->
        out << "<input type='submit' class='Button ${attrs.action} listViewAction' "
        out << " name='_action_${attrs.action}' minSelected='${attrs.minSelected ?: 1}' maxSelected='${attrs.maxSelected ?: 100}'"
        if (attrs.confirmMessage) {
            out << " confirmMessage='${attrs.confirmMessage}'"
        }
        out << " value='${body().trim()}'/>"
    }


}
