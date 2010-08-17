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
package com.collabnet.svnedge.jobs

import org.codehaus.groovy.grails.commons.ConfigurationHolder

class SvnLogParserJob {
    def svnLogService

    static def name = "SvnLogParserJob"
    static def group = "SVN_operations"

    static triggers = { 
//        simple name: "svnLogParserJobTrigger", group: group + "_Triggers", \
//        startDelay: 18770, \
    /* Disabling the svn operational log parser job as we do
     * not need this in 1.0 release. Commenting out 'triggers'
     * makes it get triggered for every one minute.
     * So increasing repeatInterval to 1 day. */
//        repeatInterval: 86400000
    }

    def execute() {
//        if (svnLogService.svnLogFile) {
//            svnLogService.parseFile()
//        } else {
//            log.warn("Unable to parse subversion log file as the " +
//                     "service has not been bootstrapped yet.")
//        }
    }
}
