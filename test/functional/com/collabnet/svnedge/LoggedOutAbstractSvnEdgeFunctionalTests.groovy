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
package com.collabnet.svnedge

import java.util.Locale;

import com.collabnet.svnedge.console.security.User
import com.collabnet.svnedge.console.services.SetupTeamForgeService;
import com.collabnet.svnedge.console.util.FileDownloaderCategory;
import com.collabnet.svnedge.console.util.UntarCategory;

import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode;
import com.collabnet.svnedge.teamforge.CtfServer;

import functionaltestplugin.FunctionalTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication;

/**
 * This is the basic implementation of functional tests for the SvnEdge.
 * It contains supporting methods for the execution of common functional tests, 
 * exposing the references to internal objects and services.
 * 
 * References about the instance the FunctionaltestCase, see 
 * http://plugins.grails.org/grails-functional-test/tags/RELEASE_1_2_4/src/groovy/functionaltestplugin/FunctionalTestCase.groovy
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public abstract class LoggedOutAbstractSvnEdgeFunctionalTests extends AbstractSvnEdgeFunctionalTests {

    @Override
    protected void setUp() {
        //The web framework must be initialized.
        super.setUp()

        get('/')
        assertStatus(200)

        if (this.response.contentAsString.contains(
                getMessage("layout.page.login"))) {
            this.logout()
        }
    }

    @Override
    protected void tearDown() {
        //Stop Svn Server in case it is running
        this.stopSvnServer()

        this.logout()

        //The tear down method terminates all the web-related objects, and
        //therefore, must be performed in the end of the operation.
        super.tearDown()
    }
}
