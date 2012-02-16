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

import sun.misc.BASE64Encoder
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.console.DumpBean
import com.collabnet.svnedge.console.SvnRepoService
import com.collabnet.svnedge.domain.RepoTemplate
import com.collabnet.svnedge.console.RepoTemplateService
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import groovy.sql.Sql

/**
 * Helper for API functional tests
 */
class ApiTestHelper {
    
    static def getSchemeHostPort() {
        "http://localhost:${Server.getConsolePort()}"
    }

    static def encodeBase64(input) {
        BASE64Encoder encoder = new BASE64Encoder();
        String output = encoder.encode(input.toString().getBytes());
        return output
    }

    static def makeAuthorization(username, password) {
        return encodeBase64("${username}:${password}")
    }

    static def makeAdminAuthorization() {
        return makeAuthorization("admin", "admin")
    }

    static def makeUserAuthorization() {
        return makeAuthorization("user", "admin")

    }

    /**
     * Creates a test repo
     * @return new Repository instance
     */
    static Repository createRepo(SvnRepoService svnRepoService) {
        // create a repo with branches/tags/trunk nodes
        def repo
        Repository.withTransaction {
            def repoName = "test-repo-" + (Math.random() * 4000)
            repo = new Repository(name: repoName)
            svnRepoService.createRepository(repo, true)
            repo.save()  
        }
        return repo
    }

    /**
     * creates a test repo and dumps it 
     * @param svnRepoService the configured service
     * @return File referencing the dump file
     */
    static File createDumpFile(SvnRepoService svnRepoService) {
        
        Repository repo = createRepo(svnRepoService)

        // create dump file of this
        DumpBean params = new DumpBean()
        params.compress = true
        def filename = svnRepoService.createDump(params, repo)
        File dumpFile = new File(new File(Server.getServer().dumpDir, repo.name), filename)
        // create dump is async, so wait
        def timeLimit = System.currentTimeMillis() + 60000
        while (!dumpFile.exists() && System.currentTimeMillis() < timeLimit) {
            Thread.sleep(250)
        }
        return dumpFile 
    }

    /**
     * creates a template from a test dump file
     * @param repoTemplateService the configured service
     * @param svnRepoService the configured service
     * @return RepoTemplate referencing the dump file
     */
    static RepoTemplate createTemplate(RepoTemplateService repoTemplateService, SvnRepoService svnRepoService) {

        File dump = createDumpFile(svnRepoService)
        
        File templateDir = repoTemplateService.getUploadDirectory()
        File templateFile = new File(templateDir, dump.name)
        dump.renameTo(templateFile)
        
        def rt
        RepoTemplate.withTransaction {
            rt = new RepoTemplate()
            rt.name = "test-template-" + (Math.random() * 4000)
            rt.active = true
            rt.displayOrder = 0
            rt.dumpFile = true
        }
        repoTemplateService.saveTemplate(rt, templateFile, true)
        return rt
    }
    
    static Sql getSqlInstance() {
        def config = ConfigurationHolder.config
        String url = config.dataSource.url
        String driver = config.dataSource.driverClassName
        String username = config.dataSource.username
        String password = config.dataSource.password
        Sql.newInstance(url, username, password, driver)
    }
    
    static boolean executeSql(sql) {
        return getSqlInstance().execute(sql)
    }
}

