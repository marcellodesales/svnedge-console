/*
 * CollabNet Subversion Edge
 * Copyright (C) 2012, CollabNet Inc. All rights reserved.
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
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import static groovyx.net.http.ContentType.JSON
import com.collabnet.svnedge.console.SvnRepoService
import com.collabnet.svnedge.util.ConfigUtil
import groovyx.net.http.RESTClient
import org.apache.http.entity.FileEntity

class HookApiTests extends AbstractSvnEdgeFunctionalTests {

    def svnRepoService

    void testHookPutText() {
        
        def testFile = new File(ConfigUtil.confDirPath, "httpd.conf.dist")
        def testRepo = ApiTestHelper.createRepo(svnRepoService)

        def rest = new RESTClient( "http://localhost:8080/csvn/api/1/" )
        rest.headers["Authorization"] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
        def params = [ 'format': 'json']
        def resp = rest.put( path: "hook/${testRepo.id}/testScript.py",
                query: params,
                body: testFile,
                requestContentType: 'text/plain' )
        assert resp.status == 201
    }

    void testHookPutBinary() {

        def testFile = new File(ConfigUtil.svnPath())
        def testRepo = ApiTestHelper.createRepo(svnRepoService)
    
        def rest = new RESTClient( "http://localhost:8080/csvn/api/1/" )
        rest.encoder.'application/octet-stream' = {
            def entity = new FileEntity( (File) it, "application/octet-stream" );
            entity.setContentType( "application/octet-stream" );
            return entity
        }
        rest.headers["Authorization"] = "Basic ${ApiTestHelper.makeAdminAuthorization()}"
        def params = [ 'format': 'json']
        def resp = rest.put( path: "hook/${testRepo.id}/testScript.bin",
                query: params,
                body: testFile, 
                requestContentType: 'application/octet-stream' )
        assert resp.status == 201
    }
   
}
