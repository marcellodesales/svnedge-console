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

class SecurityFunctionalTests extends functionaltestplugin.FunctionalTestCase {

    void testAdminAuthority() {
        get('/login/auth')
        assertStatus 200

        form('loginForm') {
            j_username = 'admin'
            j_password = 'admin'
            click 'Login'
        }

        assertStatus 200
        assertContentContains 'Logged in as:' 
        assertContentContains 'Administrator&nbsp;(admin)'

        get('/user/index')
        assertStatus 200
        assertContentContains 'User List'
    }


    void testUserAuthority() {
        get('/login/auth')
        assertStatus 200
        
        form('loginForm') {
            j_username = 'user'
            j_password = 'admin'
            click 'Login'
        }

        assertStatus 200
        assertContentContains 'Logged in as:' 
        assertContentContains 'Regular User&nbsp;(user)'

        redirectEnabled = false
        get('/server/index')
        assertRedirectUrlContains "login/denied"
        //  Sorry, you're not authorized to view this page.

    }


    void testAdminUsersAuthority() {
        get('/login/auth')
        assertStatus 200

        form('loginForm') {
            j_username = 'adminUsers'
            j_password = 'admin'
            click 'Login'
        }

        assertStatus 200
        assertContentContains 'Logged in as:'

        get('/user/index')
        assertStatus 200
        assertContentContains 'User List'

        redirectEnabled = false
        get('/server/index')
        assertRedirectUrlContains "login/denied"
    }

    void testAdminSystemAuthority() {
        get('/login/auth')
        assertStatus 200

        form('loginForm') {
            j_username = 'adminSystem'
            j_password = 'admin'
            click 'Login'
        }

        assertStatus 200
        assertContentContains 'Logged in as:'

        get('/user/index')
        assertStatus 200
        assertContentContains 'Show User'

        get('/server/edit')
        assertStatus 200
        assertContentContains 'Server Settings'
    }

      void testAdminReposAuthority() {
        get('/login/auth')
        assertStatus 200

        form('loginForm') {
            j_username = 'adminRepo'
            j_password = 'admin'
            click 'Login'
        }

        assertStatus 200
        assertContentContains 'Logged in as:'

        redirectEnabled = false
        get('/server/index')
        assertRedirectUrlContains "login/denied"

        get('/repo/create')
        assertStatus 200
        assertContentContains 'Create Repository'
    }

}
