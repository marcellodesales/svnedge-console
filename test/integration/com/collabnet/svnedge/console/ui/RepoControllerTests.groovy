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
package com.collabnet.svnedge.console.ui

import grails.test.*
import com.collabnet.svnedge.console.Repository

class RepoControllerTests extends ControllerUnitTestCase {


    def svnRepoService
    def serverConfService

    def repoNameNew = "integration_test_new_repo"
    def repoNameExisting = "integration_test_existing_repo"
    
    def repoNew 
    def repoExisting 

    protected void setUp() {

        super.setUp()
        // mock the i18n "message" map available to controller
        controller.metaClass.messageSource = [getMessage: { errors, locale -> return "message" }]
        controller.metaClass.message = { it -> return "message" }
        
        repoNew = new Repository(name: repoNameNew)
        repoExisting = new Repository(name: repoNameExisting)
        
       
        // make sure the supposedly new repo is not in the way
        svnRepoService.archivePhysicalRepository(repoNew)

        // make sure the supposedly existing repo is in the way
        svnRepoService.createRepository(repoExisting, false)

    }

    protected void tearDown() {
        super.tearDown()

        // cleanup repo
        svnRepoService.archivePhysicalRepository(repoNew)
        svnRepoService.archivePhysicalRepository(repoExisting)

    }

    void testIndex() {
        controller.index()
    }


    void testSave() {

        controller.svnRepoService = svnRepoService

        // this repo create should succeed
        controller.params.name = repoNameNew
        def model = controller.save()
        def redirArg = controller.redirectArgs["action"]
        assertEquals "Expected redirect to 'show' view on successful repo create", controller.show, redirArg

        // this should fail (validation error)
        model = controller.save()
        assertTrue "Expected error for creating a known existing repo", model.repo.hasErrors()

        // this should fail (validation error)
        controller.params.name = repoNameExisting
        model = controller.save()
        assertTrue "Expected error for creating an unknown existing repo", model.repo.hasErrors()


    }


    void testEditAuthorization() {

        controller.serverConfService = serverConfService

        // should fetch an svn_access_file from services
        def model = controller.editAuthorization()
        assertNotNull "Expected 'authRulesCommand' model object", model.authRulesCommand


    }

    void testSaveAuthorization() {

        controller.serverConfService = serverConfService

        // save the original file to restore after test
        String original = serverConfService.readSvnAccessFile()

        // content we will submit to controller
        String testFile = """
[/]
* =
[/testrepo]
* = rw
"""

        def cmd = new AuthzRulesCommand(accessRules: testFile)
        def model = controller.saveAuthorization(cmd)
        assertNotNull "Controller should provide a success message", controller.flash.message
        assertNull "Controller should not return errors", controller.flash.error

        String modified = serverConfService.readSvnAccessFile()
        assertEquals "Access file should now equal the parameter given to controller", testFile.trim(), modified

        // restore original
        serverConfService.writeSvnAccessFile(original)


    }


}
