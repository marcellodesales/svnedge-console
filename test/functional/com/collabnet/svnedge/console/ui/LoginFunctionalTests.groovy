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

class LoginFunctionalTests extends functionaltestplugin.FunctionalTestCase {

    /**
     * The default grails application instance.
     * The same as adding the autowire of the grails application bean.
     * def grailsApplication
     */
    private app = ApplicationHolder.application

    /**
     * Gets an i18n message from the messages.properties file without providing
     * parameters using the default locale.
     * @param key is the key in the messages.properties file.
     * @return the message related to the key in the messages.properties file
     * using the default locale.
     */
    public String getMessage(String key, params) {
        def appCtx = app.getMainContext()
        return appCtx.getMessage(key, params, Locale.getDefault())
    }

    public String getMessage(String key) {
        return this.getMessage(key, null)
    }

    void testRootLogin() {
        get('/login/auth')
        assertStatus 200

        def login = getMessage("layout.page.login")
        form('loginForm') {
            j_username = 'admin'
            j_password = 'admin'
            click login
        }

        assertStatus 200
        assertContentContains getMessage("layout.page.loggedAs")
        assertContentContains 'Administrator&nbsp;(admin)'

        click getMessage("layout.page.logout")
        assertStatus 200
        assertContentContains login
    }

/* Will we have non-admin logins?
    void testUserLogin() {
        get('/login/auth')
        assertStatus 200
        
        form('loginForm') {
            j_username = 'marcello'
            j_password = '1234'
            click 'Login'
        }

        assertStatus 200
        assertContentContains 'Logged in as:' 
        assertContentContains 'Marcello de Sales&nbsp;(marcello)'

        click 'LOGOUT'
        assertStatus 200
        assertContentContains 'Login'
    }
*/

    void testFailLogin() {
        get('/login/auth')
        assertStatus 200

        def login = getMessage("layout.page.login")
        form('loginForm') {
            j_username = 'marcello'
            j_password = 'xyzt'
            click login
        }

        assertContentContains getMessage("user.credential.incorrect", 
            ["marcello"] as String[])
    }
}
