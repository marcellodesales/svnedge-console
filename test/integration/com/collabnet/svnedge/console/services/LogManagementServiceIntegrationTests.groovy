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
package com.collabnet.svnedge.console.services

import grails.test.*
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode

/**
 * this test class validates the persistence of LogLevel elections
 */
class LogManagementServiceIntegrationTests extends GrailsUnitTestCase {

    def logManagementService

    protected void setUp() {
        super.setUp()

    }

    protected void tearDown() {
        super.tearDown()

    }

    void testConsoleLogLevelSet() {

        def c = logManagementService.getConsoleLevel()
        assertEquals ("Default level for console should be DEBUG for test", LogManagementService.ConsoleLogLevel.DEBUG, c )

        logManagementService.setConsoleLevel(LogManagementService.ConsoleLogLevel.INFO)
        c = logManagementService.getConsoleLevel()

        assertEquals ("Console should be INFO after setting", LogManagementService.ConsoleLogLevel.INFO, c )

    }

    void testUpdateLogConfiguration() {


        logManagementService.updateLogConfiguration(LogManagementService.ConsoleLogLevel.WARN,
                LogManagementService.ApacheLogLevel.ERROR, 5)

        // verify console update
        def c = logManagementService.getConsoleLevel()
        assertEquals ("Console should be WARN after setting", LogManagementService.ConsoleLogLevel.WARN, c )

        // verify the apache conf update
        def confFile = new File(ConfigUtil.confDirPath(), "csvn_logging.conf")

        boolean foundLogLevel = false
        confFile.eachLine {
            it -> if (it.startsWith("LogLevel")) {
                foundLogLevel = true
                assertTrue ("Apache conf should contain 'LogLevel error'", it.equals("LogLevel error"))
            }
        }

        if (!foundLogLevel) {
            fail ("Did not find Apache LogLevel config")
        }

        def s = Server.getServer()
        assertEquals ("Expected persistence of pruneLogsOlderThan field", 5, s.pruneLogsOlderThan)
        assertEquals ("Expected persistence of consoleLevel", LogManagementService.ConsoleLogLevel.WARN,
                s.consoleLogLevel)

        assertEquals ("Expected persistence of apacheLevel", LogManagementService.ApacheLogLevel.ERROR,
                s.apacheLogLevel)


    }

}

