/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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

package com.collabnet.svnedge.api

import com.collabnet.svnedge.AbstractSvnEdgeFunctionalTests
 
class TemplateApiTests extends AbstractSvnEdgeFunctionalTests {
    
    def url = "/api/1/template"
    
    void testTemplateGet() {
        
        // without auth, GET is protected
        get("${url}?format=xml")
        assertStatus 401
        
        // without admin auth, GET is protected
        get("${url}?format=json") {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
        }
        assertStatus 401

        // authorized request contains repo information
        get("${url}?format=json") {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
        }
        assertContentContains '{"templates": ['
        assertContentContains "\"id\": 1"        
        assertContentContains "\"name\": \"Empty repository\""        
    }
    
     void testTemplateUnsupportedMethods() {
        // unauthorized calls receive 401
        put(url) {
            body { "" }
        }
        assertStatus 401

        post(url) {
            body { "" }
        }
        assertStatus 401
         
        delete(url)
        assertStatus 401

        // authorized calls receive 405 (not implemented)
        put(url) {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
            body { "" }
        }
        assertStatus 405
         
        post(url) {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
            body { "" }
        }
        assertStatus 405 

        delete(url) {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
        }
        assertStatus 405

        // a template detail view is not yet supported
        get("${url}/1") {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
        }
        assertStatus 405
    }
}
