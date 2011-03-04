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
package com.collabnet.svnedge.controller.admin

import com.collabnet.svnedge.domain.Server 
import com.collabnet.svnedge.domain.integration.CtfServer 
import grails.test.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import java.io.File

class ServerControllerTests extends ControllerUnitTestCase {

    def operatingSystemService
    def lifecycleService
    def networkingService
    def serverConfService
    def grailsApplication
    def config

    protected void setUp() {
        super.setUp()

        // mock the i18n "message" map available to controller
        controller.metaClass.messageSource = [getMessage: { errors, locale ->
            return "message" }]
        controller.metaClass.message = { it -> return "message" }

        this.config = grailsApplication.config
        controller.lifecycleService = lifecycleService
        controller.networkingService = networkingService
        controller.serverConfService = serverConfService
        ConfigurationHolder.config = grailsApplication.config
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIndex() {
        controller.index()
    }
    
    void testEdit() {
        controller.edit()
    }
    
    private def defaultParams() {
        def params = controller.params
        Server server = lifecycleService.getServer()
        params.hostname = "localhost"
        params.port = "80"
        params.repoParentDir = "/tmp"
        params.adminName ="Nobody"
        params.adminEmail = "devnull@example.com"
        params
    }
    
    void testUpdate() {
        lifecycleService.stopServer()
        def params = defaultParams()
        params.port = "987652"
        controller.update()

        File f = new File(config.svnedge.svn.dataDirPath, "conf/csvn_main_httpd.conf")
        assertTrue "${f.absolutePath} does not exist", f.exists()
        assertTrue "Port directive was not updated.", (f.text.indexOf("Listen 987652") > 0)
    }
    
    void testEditAuthentication() {
        controller.editAuthentication()
    }

    void testEditIntegration() {
        if (!CtfServer.getServer()) {

            CtfServer s = new CtfServer(baseUrl: "http://ctf", mySystemId: "exsy1000",
                    internalApiKey: "testApiKey",
                    ctfUsername: "myCtfUser",
                    ctfPassword: "encrypted")
            if (!s.validate()) {
                s.errors.each { println(it)}
            }
            s.save(flush:true)
        }
        controller.editIntegration()
    }
}
