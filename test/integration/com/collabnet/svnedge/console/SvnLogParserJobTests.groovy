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
package com.collabnet.svnedge.console

import grails.test.*

class SvnLogParserJobTests extends GrailsUnitTestCase {
    def svnLogParserJob

    def grailsApplication

    protected void setUp() {
        super.setUp()
        svnLogParserJob = grailsApplication.mainContext
            .getBean("com.collabnet.svnedge.jobs.SvnLogParserJob")
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testExecute() {
        // Confirming no exceptions
        svnLogParserJob.execute()
    }    
}
