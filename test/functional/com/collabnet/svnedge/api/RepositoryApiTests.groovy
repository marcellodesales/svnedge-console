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
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.ServerMode

class RepositoryApiTests extends AbstractSvnEdgeFunctionalTests {

    def svnRepoService
    
    void testRepositoryGet() {
        
        def server = Server.getServer()
        def testRepoName = "api-test" + Math.random() * 10000
        def repo = new Repository(name: testRepoName)
        assertEquals "Failed to create repository.", 0,
                svnRepoService.createRepository(repo, true)
        repo.save(flush: true)
        repo = Repository.findByName(testRepoName) 
        assertNotNull "Repo should be created", repo

        // without auth, GET is protected
        get('/api/1/repository?format=xml')
        assertStatus 401

        // authorized request contains repo information
        get('/api/1/repository?format=json') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
        }
        assertContentContains '{"repositories":['
        assertContentContains "\"id\":${repo.id},"        
        assertContentContains "\"name\":\"${repo.name}\","        
        assertContentContains "\"status\":\"OK\","        
        assertContentContains "\"svnUrl\":\"${server.svnURL()}${repo.name}\","        
        assertContentContains "\"viewvcUrl\":\"${server.viewvcURL(repo.name)}\""     
        
        svnRepoService.removeRepository(repo)
        svnRepoService.deletePhysicalRepository(repo)
    }

    void testRepositoryUnsupportedMethods() {
        // unauthorized calls receive 401
        put('/api/1/repository') {
            body { "" }
        }
        assertStatus 401

        post('/api/1/repository') {
            body { "" }
        }
        assertStatus 401

        delete('/api/1/repository')
        assertStatus 401

        // authorized calls receive 405 (not implemented)
        put('/api/1/repository') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
            body { "" }
        }
        assertStatus 405

        post('/api/1/repository') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
            body { "" }
        }
        assertStatus 405

        delete('/api/1/repository') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
        }
        assertStatus 405

        // a repo detail view is not yet supported
        get('/api/1/repository/1') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
        }
        assertStatus 405
    }
    
    void testRepositoryIntegratedServer() {
        
        def server = Server.getServer()
        server.setMode(ServerMode.MANAGED)
        server.save(flush: true)
        
        // without auth, GET is protected
        get('/api/1/repository?format=xml')
        assertStatus 401
        
        // authorized request should respond 405
        get('/api/1/repository?format=json') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
        }
        assertStatus 405
         
        server.setMode(ServerMode.REPLICA)
        server.save(flush: true)
         
        // authorized request should respond 405
        get('/api/1/repository?format=json') {
            headers['Authorization'] = "Basic ${ApiTestHelper.makeUserAuthorization()}"
        }
        assertStatus 405
         
        server.setMode(ServerMode.STANDALONE)
        server.save(flush: true)
    }
}
