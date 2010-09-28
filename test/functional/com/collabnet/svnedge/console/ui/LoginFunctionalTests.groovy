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

import org.codehaus.groovy.grails.commons.ApplicationHolder;

import com.collabnet.svnedge.LoggedOutAbstractSvnEdgeFunctionalTests;

class LoginFunctionalTests extends LoggedOutAbstractSvnEdgeFunctionalTests {

    @Override
    protected void setUp() {
        super.setUp();
    }

    @Override
    protected void tearDown() {
        super.tearDown();
    }

    void testRootLogin() {
        this.loginAdmin()
    }

    void testRegularLogin() {
        this.loginUser()
    }

    void testDotsLogin() {
        this.loginUserDot()
    }

    void testFailLogin() {
        this.login("marcello", "xyzt")
        assertContentContains getMessage("user.credential.incorrect", 
            ["marcello"] as String[])
    }
}
