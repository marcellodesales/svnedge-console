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
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode

/**
 * this test class validates the configuration files being modified
 * by the ServerConfService
 */
class ServerConfServiceIntegrationTests extends GrailsUnitTestCase {

    def serverConfService
    

    protected void setUp() {
        super.setUp()

    }

    protected void tearDown() {
        super.tearDown()

    }


    void testViewvcConf() {

        // write viewvc.conf for the server in Standalone
        def server = Server.get(1)
        server.setMode(ServerMode.STANDALONE)

        serverConfService.writeConfigFiles();

        // validate expectations
        def confFile = new File(serverConfService.confDirPath, "viewvc.conf")
        String viewVcConf = confFile?.text

        // verfiy file is created and all placeholder tokens are replaced
        assertNotNull("The viewvc.conf file should exist", confFile)
        assertEquals("No replacement tokens should be found", -1, viewVcConf.lastIndexOf("__CSVN"))

        // spot-check some properties
        assertTrue("root_parents should equal ${server.repoParentDir}",
            validateProperty(confFile, "root_parents", server.repoParentDir ))

        assertTrue("csvn_servermode should equal ${ServerMode.STANDALONE.toString()}",
            validateProperty(confFile, "csvn_servermode", ServerMode.STANDALONE.toString() ))

        String views = "annotate, co, diff, markup, roots"
        assertTrue("allowed_views should equal ${views}",
            validateProperty(confFile, "allowed_views", views ))


        // now regen conf in managed mode
        server.setMode(ServerMode.MANAGED)

        serverConfService.writeConfigFiles();

        // verfiy file is created and all placeholder tokens are replaced
        assertNotNull("The viewvc.conf file should exist", confFile)
        assertEquals("No replacement tokens should be found", -1, viewVcConf.lastIndexOf("__CSVN"))

        // and spot check
        assertTrue("csvn_servermode should equal ${ServerMode.MANAGED.toString()}",
            validateProperty(confFile, "csvn_servermode", ServerMode.MANAGED.toString() ))

        views = "annotate, co, diff, markup"
        assertTrue("allowed_views should equal ${views}",
            validateProperty(confFile, "allowed_views", views ))


    }


    private boolean validateProperty(File f, String propertyName, String expectedValue) {

        boolean valueMatchFound = false
        f.eachLine {
            it -> if (it.startsWith(propertyName)) {
                valueMatchFound = it.contains(expectedValue)
            }
        }

        return valueMatchFound
    }




}
