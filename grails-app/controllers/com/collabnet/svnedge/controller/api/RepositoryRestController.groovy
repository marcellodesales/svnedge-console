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



package com.collabnet.svnedge.controller.api

import com.collabnet.svnedge.domain.Server
import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.domain.ServerMode
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.domain.integration.ReplicatedRepository

/**
 * Repository info
 */
@Secured(['ROLE_USER'])
class RepositoryRestController extends AbstractRestController {

    def svnRepoService

    /**
     * API to retrieve the list of repositories. For each repository, the name, status,
     * svnUrl, and viewvcUrl are returned. Status currently indicates whether the 
     * Unix permissions are set correctly, so it will always be "OK" on Windows.
     * 
     * URL:
     * <code>
     *   /csvn/api/1/repository
     * </code>
     *
     * HTTP Method:
     * <code>
     *     GET
     * </code>
     * 
     * XML-formatted return example:
     * <pre>
     * {@code
     * <map>
     *   <entry key="repositories">
     *     <map>
     *       <entry key="id">1</entry>
     *       <entry key="name">api-test</entry>
     *       <entry key="status">OK</entry>
     *       <entry key="svnUrl">http://Homegrown/svn/api-test</entry>
     *       <entry key="viewvcUrl">http://Homegrown/viewvc/api-test/</entry>
     *     </map>
     *   </entry>
     * </map>  
     * }
     * </pre>    
     */
    def restRetrieve = {
        def result
        Server server = Server.getServer()

        // only list repos in standalone mode; no detail view allowed yet
        if (server.mode != ServerMode.STANDALONE || params.id) {
            response.status = 405
            result = [errorMessage: message(code: "api.error.405")]
        }
        else {
            def repositories = []
            def params = [sort: "name"]
            Repository.list(params)?.each {
                def repository = [id: it.id,
                        name: it.name,
                        status: (it.permissionsOk ?
                            message(code: "repository.page.list.instance.permission.ok") :
                            message(code: "repository.page.list.instance.permission.needFix")),
                        svnUrl: "${server.svnURL()}${it.name}",
                        viewvcUrl: "${server.viewvcURL(it.name)}"]
                repositories.add(repository)
            }
            result = [repositories: repositories]
        }

        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    /**
     * API to create a repository. 
     *
     * URL:
     * <code>
     *   /csvn/api/1/repository
     * </code>
     *
     * HTTP Method:
     * <code>
     *     POST
     * </code>
     *
     * XML-formatted request body example
     * <pre>
     * {@code
     * <map>
     *   <entry key="name">new-repo</entry>
     *   <entry key="useTemplate">false</entry>
     * </map>
     * }
     * </pre>    
     * 
     * XML-formatted return example:
     * <pre>
     * {@code
     * <map>
     *   <entry key="message">Successfully created</entry>
     *   <entry key="repository">
     *      <map>
     *        <entry key="id">1</entry>
     *        <entry key="name">new-repo</entry>
     *        <entry key="status">OK</entry>
     *        <entry key="svnUrl">http://Homegrown/svn/api-test</entry>
     *        <entry key="viewvcUrl">http://Homegrown/viewvc/api-test/</entry>
     *      </map>
     *   </entry>
     * </map>
     * }
     * </pre>    
     */
    @Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
    def restSave = {
        def result = [:]
        def server = Server.getServer()
        def repo = new Repository(name: getRestParam("name"))
        def useTemplate = Boolean.valueOf(getRestParam("useTemplate"))
        
        // multi-stage validation for repositories
        repo.validate()
        if (!repo.hasErrors()) {
            repo.validateName()
        }
        if (!repo.hasErrors()) {
            boolean errorCode = svnRepoService.createRepository(repo, useTemplate)
            if (errorCode) {
                repo.errors.rejectValue("name", "repository.not.created.message", [repo.name])
            }
        } 
        // no errors? 
        if (!repo.hasErrors()) {
            repo.save()
            repo.refresh()
            def repository = [id: repo.id,
                    name: repo.name,
                    status: (repo.permissionsOk ?
                        message(code: "repository.page.list.instance.permission.ok") :
                        message(code: "repository.page.list.instance.permission.needFix")),
                    svnUrl: "${server.svnURL()}${repo.name}",
                    viewvcUrl: "${server.viewvcURL(repo.name)}"]

            response.status = 201
            result['message'] = message(code: "api.message.201")
            result['repository'] = repository
            log.info("Repository created via API: ${repo.name}")
        }
        else {
            response.status = 400
            result['errorMessage'] = message(code: "api.error.400")
            log.warn("Failed to create repository '${getRestParam("name")}': ${repo.errors}")
        }

        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }
}
