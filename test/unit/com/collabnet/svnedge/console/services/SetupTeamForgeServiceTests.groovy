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
package com.collabnet.svnedge.console.services;

import java.util.Random
import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.collabnet.svnedge.teamforge.CtfConversionBean

class SetupTeamForgeServiceTests extends GrailsUnitTestCase {
    def setupTeamForgeService
    File testDir

    def config = ConfigurationHolder.config

    protected void setUp() {
        super.setUp()

        // mock the service and its dependencies
        mockLogging (SetupTeamForgeService, true)
        setupTeamForgeService = new SetupTeamForgeService()
    }

    protected void tearDown() {
        super.tearDown()

        if (testDir) {
            testDir.eachFile { it.delete() }
            testDir.delete()
        }
        testDir = null
    }

    private File createTestDir() {
        File dir = File.createTempFile("repo-hooks", null)
        String path = dir.absolutePath
        dir.delete()
        dir = new File(path)
        dir.mkdir()
        testDir = dir
    }

    void testArchiveCurrentHooks() {
        createTestDir()
        File f1 = new File(testDir, "test1.txt")
        def f1Contents = "Some random text to have compressed."
        f1.write(f1Contents)
        File f2 = new File(testDir, "test2.txt")
        def f2Contents = 
"""Actually, trunk can now be used for 1.1 development, 
so new features can go there.   In fact, if John/Jeremy feel like merging 
CTF_MODE to trunk, that'd be fine."""
        f2.write(f2Contents)
        assertEquals "Only the created files should be present", 2, 
            testDir.listFiles().length
        assertTrue "${f1.name} wasn't created", f1.exists()
        assertTrue "${f2.name} wasn't created", f2.exists()
        f2.setExecutable(true)
        
        setupTeamForgeService.archiveCurrentHooks(testDir)
        File archive = new File(testDir, "pre-ctf-hooks.zip")
        assertTrue "Hookscript archive ${archive.absolutePath} is missing",
            archive.exists()
        assertEquals "Only archive file should be present", 1, 
            testDir.listFiles().length

        // check that contents can be restored
        setupTeamForgeService.restoreNonCtfHooks(testDir)
        assertEquals "Only the original files should be present", 2, 
            testDir.listFiles().length
        assertTrue "${f1.name} doesn't exist", f1.exists()
        assertTrue "${f2.name} doesn't exist", f2.exists()
        assertEquals "${f1.name} content is corrupted", f1Contents, f1.text
        assertEquals "${f2.name} content is corrupted", f2Contents, f2.text
        if (System.getProperty("os.name").substring(0,3).toLowerCase() != "win") {
            assertTrue "${f1.name} should not be executable", !f1.canExecute()
        }
        assertTrue "${f2.name} should be executable", f2.canExecute()
    }
}
