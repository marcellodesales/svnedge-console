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
package com.collabnet.svnedge.integration

import java.util.Locale;

import grails.test.*

import org.junit.Test

import com.collabnet.svnedge.domain.integration.CloudServicesConfiguration;
import com.collabnet.svnedge.util.ConfigUtil;

class CloudServicesRemoteClientServiceIntegrationTests extends GrailsUnitTestCase {

    def grailsApplication
    def config
    def cloudServicesRemoteClientService
    boolean skipTests
    
    @Override
    protected void setUp() {
        super.setUp()
        this.config = grailsApplication.config
        
        CloudServicesConfiguration csConf = CloudServicesConfiguration.getCurrentConfig()
        if (!csConf) {
            File f = new File(ConfigUtil.appHome(), "testCred.properties")
            if (f.exists()) {
                Properties up = new Properties()
                f.withReader { up.load(it) }
                csConf = new CloudServicesConfiguration(username: up['username'], 
                password: up['password'], domain: up['domain'])
                csConf.save()
                skipTests = false
            } else {
                skipTests = true
            }
        }        
     }

    void testCreateSvnAndDeleteProject() {
        if (skipTests) {
            System.out.println("skipped it")
            return
        }
        String projectId = cloudServicesRemoteClientService.createProject("testProject")
        assertNotNull "Could not create test project", projectId
        
        String serviceId = cloudServicesRemoteClientService.addSvnToProject(projectId)
        
        assertTrue "Was unable to delete test project, id=" + projectId, 
            cloudServicesRemoteClientService.deleteProject(projectId)
            
        assertNotNull "Could not add svn to the test project", serviceId
    }
}
