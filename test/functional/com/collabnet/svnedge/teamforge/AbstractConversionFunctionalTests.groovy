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
package com.collabnet.svnedge.teamforge

import groovy.lang.MetaClass;

import com.collabnet.svnedge.AbstractSvnEdgeFunctionalTests;
import com.collabnet.svnedge.console.Repository;
import com.collabnet.svnedge.console.security.User;
import com.collabnet.svnedge.statistics.StatValue;

/**
* This is an abstract class for the conversion process that provides the 
* "heavy-lifting" of the steps performed on the Web UI.
*
* @author Marcello de Sales (mdesales@collab.net)
*
*/
abstract class AbstractConversionFunctionalTests extends 
    AbstractSvnEdgeFunctionalTests {

    /**
     * It is the test URL based on the properties in the Config.groovy
     */
    def ctfTestUrl

    def ctfRemoteClientService

    @Override
    protected void setUp() {
        super.setUp();

        //running on a machine that does not have a name does not work.
        if (server.hostname == "localhost" || server.hostname == "127.0.0.1") {
            return
        }

        // setup the csvn image for testing the software version during 
        // conversion
        this.setupTestCsvnImage()

        // remove any repository created
        this.cleanRepositories()

        // convert if necessary
        this.convertToStandaloneMode()
    }

    @Override
    protected void tearDown() {
        this.convertToStandaloneMode()
        super.tearDown();
    }

    /**
     * Removes all the references of repositories from the database. It also
     * includes removing the references to statistics values from the db.
     */
    protected void cleanRepositories() {
        StatValue.list().each{
            it.delete()
        }
        Repository.list().each {
            it.delete()
        }
    }

    /**
     * Deletes the project created during the conversion process.
     */
    protected void removeProjectFromCtfServer() {
        def username = config.svnedge.ctfMaster.username
        def password = config.svnedge.ctfMaster.password
        def cnSoap = ctfRemoteClientService.cnSoap(this.getTestCtfUrl())
        def sessionId = cnSoap.login(username, password)

        def projects = ctfRemoteClientService.getProjectList(
            this.getTestCtfUrl(), sessionId)
        String projectId = null
        for (p in projects) {
            if (this.createdProjectName == p.title.toLowerCase()) {
                projectId = p.id
                break
            }
        }
        cnSoap.deleteProject(sessionId, projectId)
    }

    /**
     * @return the CTF URL based on the configuration structure.
     */
    public def getTestCtfUrl() {
        if (!ctfTestUrl) {
            ctfTestUrl = this.makeCtfUrl()
        }
        return ctfTestUrl
    }

    /**
     * @return the URL to the CTF server based on the configuration.
     */
    private makeCtfUrl() {
        def ctfProto = config.svnedge.ctfMaster.ssl ? "https://" : "http://"
        def ctfHost = config.svnedge.ctfMaster.domainName
        def ctfPort = config.svnedge.ctfMaster.port == "80" ? "" : ":" +
                config.svnedge.ctfMaster.port
        ctfTestUrl = ctfProto + ctfHost + ctfPort
        return ctfTestUrl
    }

   /**
    * @return if the status page contains the link to a TeamForge server.
    */
   protected boolean isServerOnTeamForgeMode() {
       get('/status/index')
       return this.response.contentAsString.contains(
           getMessage("status.page.url.teamforge"))
   }

   /**
    * @return if the status page DOES NOT contain the link to a TeamForge 
    * server.
    */
   protected boolean isServerOnStandaloneMode() {
       return !this.isServerOnTeamForgeMode()
   }

    /**
     * Performs the revert process. That is, the conversion process from the
     * TeamForge Mode (Managed Mode) back to the Standalone Mode.
     */
    protected void convertToStandaloneMode() {
        if (this.isServerOnStandaloneMode()) {
            return
        }
        def ctfSystemId = CtfServer.getServer().mySystemId
        // Step 1: Go to the edit integration page and use the admin 
        // credentials, making sure the conversion process works.
        get('/server/editIntegration')
        assertStatus 200

        assertContentContains("By submitting this form with the " +
            "administrator's credentials, this Subversion Edge server can " +
            "be converted back to Standalone mode...")
        assertContentContains("TeamForge server URL")
        assertContentContains(this.getTestCtfUrl())

        def username = config.svnedge.ctfMaster.username
        def password = config.svnedge.ctfMaster.password
        def button = getMessage("setupTeamForge.page.ctfInfo.button.convert")
        form {
            ctfUsername = username
            ctfPassword = password
            click button
        }
        assertStatus 200
        assertContentContains("This Subversion Edge server has been reverted " +
            "successfully from Managed to Standalone mode.")

        // Step 2: Verify that the revert process is persistent.
        assertRevertSucceeed(ctfSystemId)
    }

    /**
     * Assert that the revert process has been successfully performed. 
     * <li>The local server state must be on standalone mode.
     * <li>The CTF server cannot list any integration server with the given
     * system ID.
     * @param ctfSystemId is the system ID that was captured during the 
     * conversion process. It is shown in the links of the integration server.
     */
    protected void assertRevertSucceeed(ctfSystemId) {
        // Step 1: the revert process is persisted.
        this.logout()
        assertStatus 200

        this.loginAdmin()
        assertStatus 200

        assertContentDoesNotContain(getMessage("status.page.url.teamforge"))
        // verify that the software version is still shown
        assertContentContains(getMessage("status.page.status.version.software"))
        assertContentContains(
            getMessage("status.page.status.version.subversion"))

        get('/server/edit')
        assertStatus 200
        assertContentDoesNotContain(
            getMessage("server.page.leftNav.toStandalone"))

        // Step 2: verify that the CTF server DOES NOT list the system ID
        assertRevertSucceededOnCtfServer(ctfSystemId)
    }

    /**
     * Verify that the state of the conversion is persisted:
     * <li>The local server shows the TeamForge URL
     * <li>The CTF server shows the link to the server. This can be verified
     * by the current system ID on the list of integration servers.
     */
    protected void assertConversionSucceeded() {
        // Step 1: verify that the conversion is persistent
        this.logout()
        assertStatus 200

        this.loginAdmin()
        assertStatus 200

        assertContentContains(getMessage("status.page.url.teamforge"))
        // verify that the software version is still shown
        assertContentContains(getMessage("status.page.status.version.software"))
        assertContentContains(
            getMessage("status.page.status.version.subversion"))

        get('/server/edit')
        assertStatus 200
        assertContentContains(getMessage("server.page.leftNav.toStandalone"))

        // Step 2: verify that the CTF server DOES list the system ID
        assertConversionSucceededOnCtfServer()
    }

    /**
     * Verifies that the conversion did not succeed by verifying that the
     * status page does not show any TeamForge link.
     */
    protected void assertConversionDidNotSucceeded() {
        // verify that the box was not converted by mistake and did not affect
        this.logout()
        assertStatus 200

        this.loginAdmin()
        assertStatus 200

        assertContentDoesNotContain(getMessage("status.page.url.teamforge"))
        // verify that the software version is still shown
        assertContentContains(getMessage("status.page.status.version.software"))
        assertContentContains(
            getMessage("status.page.status.version.subversion"))
    }

    /**
     * Goes to the list of integrations on the CTF server
     */
    private void goToCtfListIntegrationsPage() {
        // Goes to the list integrations page
        // http://cu073.cloud.sp.collab.net/sf/sfmain/do/listSystems
        get(this.makeCtfUrl() + "/sf/sfmain/do/listSystems")
        this.loginToCtfServerIfNecessary()
    }

    /**
     * Verifies that the CTF server DOES NOT list the given system ID.
     */
    protected void assertRevertSucceededOnCtfServer(ctfSystemId) {
        this.goToCtfListIntegrationsPage()
        assertContentDoesNotContain(ctfSystemId)
    }

   /**
    * Makes login to CTF server from a given point that connects to the server.
    * In case the response content DOES NOT contains the string "Logged in as",
    * then make the login. The resulting page is the redirected page requested
    * earlier.
    */
    private void loginToCtfServerIfNecessary() {
        if (!this.response.contentAsString.contains("Logged in as")) {
            assertStatus 200
            def ctfUsername = config.svnedge.ctfMaster.username
            def ctfPassword = config.svnedge.ctfMaster.password
            form("login") {
                username = ctfUsername
                password = ctfPassword
            }
            // the button is a link instead of a form button. Use it outside
            // the form closure.
            click "Log In"
            assertStatus 200
        }
    }

    /**
     * Verifies that the CTF server lists the current ctf server system ID.
     */
    protected void assertConversionSucceededOnCtfServer() {
        this.goToCtfListIntegrationsPage()
        assertContentContains(CtfServer.getServer().mySystemId)

        assertContentContains("Site Administration")
        assertContentContains("SCM Integrations")
        def appServerPort = System.getProperty("jetty.port", "8080")
        def csvnHostAndPort = server.hostname + ":" + appServerPort
        assertContentContains("This is a CollabNet Subversion Edge server in " +
            "managed mode from ${csvnHostAndPort}.")
    }

    /**
     * Verifies that the CTF Server contains a project with the given 
     * project Name. This assertion makes sure the project is clickable as well.
     * @param createdProjectName is the name of the project that might be 
     * listed in the projects list on CTF.
     */
    protected void assertProjectWasCreatedOnCtfServer(createdProjectName) {
        // Goes to the list projects page
        // http://cu073.cloud.sp.collab.net/sf/sfmain/do/listProjectsAdmin
        get(this.makeCtfUrl() + "/sf/sfmain/do/listProjectsAdmin")
        this.loginToCtfServerIfNecessary()

        assertContentContains(createdProjectName)
        assertContentContains(
            getMessage("setupTeamForge.integration.container.existed"))
        click(createdProjectName)
        assertStatus 200
    }

    /**
     * Verifies that the CTF Server contains all the users with from the SVN
     * Edge server.
     */
    protected void assertUsersWereCreatedOnCtfServer() {
        get(this.makeCtfUrl() + "/sf/sfmain/do/listUsersAdmin")
        assertStatus 200

        User.list().each {
            assertContentContains(it.username)
            assertContentContains(it.realUserName)
            assertContentContains(it.email)
        }
    }

    /**
     * Verifies that the CTF Server contains all the repositories with the
     * given project Name. This assertion makes sure the repositories are
     * listed. It also clicks on each of them and verifies if viewvc is working.
     * @param createdProjectName is the name of the project created on CTF.
     */
    protected void assertRepositoriesWereCreatedOnCtfServer(createdProjName) {
        get(this.makeCtfUrl() + "/sf/scm/do/listRepositories/" + 
            "projects.${createdProjName}/scm ")
        assertStatus 200

        Repository.list().each {
            assertContentContains(it.name)
            // verifies if the repository link was created. 
            // svn checkout --username admin csvnHost:viewvcPort/svn/repoName
            assertContentContains("svn checkout --username admin " + 
                server.urlPrefix() + "/svn/" + it.name)

            // verify that the CTF+viewVC link is working
            get(this.makeCtfUrl() + "/sf/scm/do/viewRepositorySource/" +
                "projects.${createdProjName}/scm.${it.name}")
            assertStatus 200

            // verify that the direct link to the repository is working
            get(server.urlPrefix() + "/viewvc/?root=${it.name}&system=" + 
                "${CtfServer.getServer().mySystemId}")
            this.loginToCtfServerIfNecessary()

            assertContentContains("[${it.name}]")
            assertContentContains("Directory revision")
        }
    }

    /**
     * @return if the current state of the CSVN server is fresh. That is,
     * if there are no repositories created.
     */
    protected boolean isItAFreshConversion() {
       get('/repo/list')
       assertStatus 200
       return this.response.contentAsString.contains(
           getMessage("repository.page.list.noRepos"))
    }

    /**
     * Verifies the tabs for the fresh conversion.
     */
    private void assertTabsOnFreshConversionAreCorrect() {
        //TODO: Add parameter of the current tab
        //TODO: Verify if the correct tab is highlighted
        def introTab = getMessage("setupTeamForge.page.tabs.index", [1])
        assertContentContains(introTab.replaceAll(" ", "&nbsp;"))
        def convertTab = getMessage("setupTeamForge.page.tabs.confirm", [2])
        assertContentContains(convertTab.replaceAll(" ", "&nbsp;"))
    }

    /**
     * Verifies the tabs for the full/complete conversion.
     */
    private void assertTabsOnCompleteConversionAreCorrect() {
        //TODO: Add parameter of the current tab
        //TODO: Verify if the correct tab is highlighted.
        def introTab = getMessage("setupTeamForge.page.tabs.index", [1])
        assertContentContains(introTab.replaceAll(" ", "&nbsp;"))

        def credTab = getMessage("setupTeamForge.page.tabs.ctfInfo", [2])
        assertContentContains(credTab.replaceAll(" ", "&nbsp;"))

        def projTab = getMessage("setupTeamForge.page.tabs.ctfProject", [3])
        assertContentContains(projTab.replaceAll(" ", "&nbsp;"))

        def usersTab = getMessage("setupTeamForge.page.tabs.ctfUsers", [4])
        assertContentContains(usersTab.replaceAll(" ", "&nbsp;"))

        def convertTab = getMessage("setupTeamForge.page.tabs.confirm", [5])
        assertContentContains(convertTab.replaceAll(" ", "&nbsp;"))
    }

    /**
     * Goes to the credentials tab of the conversion process, verifying each 
     * of the steps.
     */
    protected void goToCredentialsTab() {

        def isFresh = this.isItAFreshConversion()

        // Step 1: verify the setup page is correct
        get('/setupTeamForge/index')
        assertStatus 200

        if (isFresh) {
            assertTabsOnFreshConversionAreCorrect()
        } else {
            assertTabsOnCompleteConversionAreCorrect()
        }

        assertContentContains(getMessage("setupTeamForge.page.index.almTitle"))

        // Step 2: verify that the setup page is correct after clicking on 
        // the continue button.
        def button = getMessage("setupTeamForge.page.index.button.continue")
        form {
            click button
        }
        assertStatus 200

        if (isFresh) {
            assertTabsOnFreshConversionAreCorrect()
        } else {
            assertTabsOnCompleteConversionAreCorrect()
        }

        assertContentContains(getMessage("setupTeamForge.page.ctfInfo.p1"))
    }
}
