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

    def restRetrieve = {
        def repoList
        def params = [sort: "name"]
        Server server = Server.getServer()
        if (server.mode == ServerMode.REPLICA) {
            repoList = ReplicatedRepository.listSortedByName(params).collect { it.repo }
        } 
        else {
            repoList = Repository.list(params)
        }
        def repositories = []
        repoList.each {
            def repository = [id: it.id, 
                    name: it.name, 
                    status: (it.permissionsOk ? 
                        message(code: "repository.page.list.instance.permission.ok") : 
                        message(code: "repository.page.list.instance.permission.needFix")),
                    svnUrl: "${server.svnURL()}${it.name}", 
                    viewvcUrl: "${server.viewvcURL(it.name)}" ]
            repositories.add(repository)
        }
        def result = [ repositories: repositories ]
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
            html { render result as XML }
        }
    }
}
