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
import com.collabnet.svnedge.util.ConfigUtil
import org.apache.commons.logging.LogFactory
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.User;

class CloudServicesRemoteClientServiceIntegrationTests extends GrailsUnitTestCase {

    def log = LogFactory.getLog(CloudServicesRemoteClientServiceIntegrationTests.class)

    def grailsApplication
    def config
    def cloudServicesRemoteClientService
    def securityService
    boolean skipTests
    CloudServicesConfiguration csConf

    
    @Override
    protected void setUp() {
        super.setUp()
        this.config = grailsApplication.config
        
        csConf = CloudServicesConfiguration.getCurrentConfig()
        if (!csConf) {
            File f = new File(ConfigUtil.appHome(), "testCred.properties")
            if (f.exists()) {
                Properties up = new Properties()
                f.withReader { up.load(it) }
                csConf = new CloudServicesConfiguration(username: up['username'], 
                        password: up['password'], domain: up['domain'], 
                        enabled: true)
                csConf.save()
                skipTests = false
            } else {
                skipTests = true
                log.warn("Skipping unit tests")
            }
        }        
     }

    void testCreateSvnAndDeleteProject() {
        if (skipTests) {
            return
        }
        def repo = new Repository(name: "testRepo1")
        String projectId = cloudServicesRemoteClientService.createProject(repo)
        assertNotNull "Could not create test project", projectId
        
        String serviceId = cloudServicesRemoteClientService.addSvnToProject(projectId)
        
        assertTrue "Was unable to delete test project, id=" + projectId, 
            cloudServicesRemoteClientService.deleteProject(projectId)
            
        assertNotNull "Could not add svn to the test project", serviceId
    }

    void testIsDomainAvailable() {
        if (skipTests) {
            return
        }
        def domain = csConf.domain
        boolean result = cloudServicesRemoteClientService.isDomainAvailable(domain)
        assertFalse "Domain '${domain}' should not be available", result

        domain = "${domain}334324"
        result = cloudServicesRemoteClientService.isDomainAvailable(domain)
        assertTrue "Domain '${domain}' should be availabled", result
    }

    void testIsLoginAvailable() {
        if (skipTests) {
            return
        }
        def login = csConf.username
        boolean result = cloudServicesRemoteClientService.isLoginNameAvailable(login, null)
        assertFalse "Login '${login}' should not be available", result

        login = "${login}123443"
        result = cloudServicesRemoteClientService.isLoginNameAvailable(login, null)
        assertTrue "Login '${login}' should be availabled", result
    }

    void testListUsers() {
        if (skipTests) {
            return
        }
        def remoteUsers = cloudServicesRemoteClientService.listUsers()
        def matchingUser = remoteUsers.find { remoteItem -> remoteItem.login == csConf.username}
        assertTrue "should have found our sign-in user", matchingUser.login == csConf.username
    }

    void testCreateAndDeleteUser() {
        if (skipTests) {
            return
        }
        def user = new User(username: "unitTestUser", realUserName: "unit test bits", email: "unit@test.com")
        def login = user.username
        def counter = 0
        while (!cloudServicesRemoteClientService.isLoginNameAvailable(login, null) && counter < 10) {
            login = "${user.username}${++counter}"
        }

        def result = cloudServicesRemoteClientService.createUser(user, login)
        assertTrue "the test user should have been successfully created", result

        def matchingUser = cloudServicesRemoteClientService.listUsers().find { remoteItem -> remoteItem.login == login}
        result = cloudServicesRemoteClientService.deleteUser(matchingUser.userId)
        assertTrue "the test user should have been successfully deleted", result
    }
}
