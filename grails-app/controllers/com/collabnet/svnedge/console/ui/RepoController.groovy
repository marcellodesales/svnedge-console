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

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.replica.manager.ReplicatedRepository
import com.collabnet.svnedge.console.Server

/**
 * Command class for 'saveAuthorization' action provides validation
 */
class AuthzRulesCommand {
   String accessRules
   def errors
   static constraints = {
       accessRules(blank:false)
   }
}

@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
class RepoController {

    def svnRepoService
    def serverConfService

    @Secured(['ROLE_USER'])
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    @Secured(['ROLE_USER'])
    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        params.offset = params.offset ? params.offset.toInteger() : 0
        if (!params.sort) {
            params.sort = "name"
        }
        def server = Server.getServer()
        def repoList
        if (server.replica) {
            if (params.sort == "name") {
                repoList = ReplicatedRepository.listSortedByName(params)
            } else {
                repoList = ReplicatedRepository.list(params)
            }
        } else {
            repoList = Repository.list(params)
        }
        [ repositoryInstanceList: repoList,
          repositoryInstanceTotal: Repository.count(),
          server: server, isReplica: server.replica ]
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def discover = {

        try {
            svnRepoService.syncRepositories();
            flash.message = message(code: 'repository.action.discover.success')

        }
        catch (Exception e) {
            log.error("Unable to discover repositories", e)
            flash.error = message(code: 'repository.action.discover.failure')
        }
        redirect(action:list,params:params)
    }

    @Secured(['ROLE_USER'])
    def show = {
        def repo = Repository.get( params.id )

        if(!repo) {
            flash.error = message(code: 'repository.action.not.found',
                [params.id] as String[])
            redirect(action:list)

        } else {

            def repoParentDir = serverConfService.server.repoParentDir
            def repoPath = new File(repoParentDir, repo.name).absolutePath
            def username = serverConfService.httpdUser
            def group = serverConfService.httpdGroup

            def timespans = [[index: 0, 
                title: message(code: "statistics.graph.timespan.lastHour"),
                        seconds: 60*60, pattern: "HH:mm"],
                [index: 1, 
                    title: message(code: "statistics.graph.timespan.lastDay"),
                    seconds: 60*60*24, pattern: "HH:mm"],
                [index: 2, 
                    title: message(code: "statistics.graph.timespan.lastWeek"),
                    seconds: 60*60*24*7, pattern: "MM/dd HH:mm"],
                [index: 3, 
                    title: message(code: "statistics.graph.timespan.lastMonth"),
                    seconds: 60*60*24*30, pattern: "MM/dd"]]

            def timespanSelect = timespans.inject([:]) { map, ts ->
                map[ts.index] = ts.title 
                map
            }

            return [ timespanSelect: timespanSelect, 
                initialGraph : "DISKSPACE_CHART",
                repositoryInstance : repo,
                svnUser : username,
                svnGroup : group,
                repoPath : repoPath ]
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def delete = {
        def repo = Repository.get( params.id )
        if(repo) {
            try {
                repo.delete(flush:true)
                def msg = svnRepoService.deleteRepository(repo)
                flash.message = msg
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.error = message(code:'repository.action.cant.delete',
                    [params.id] as String[])
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.error = message(code: 'repository.action.not.found',
                [params.id] as String[])
            redirect(action:list)
        }
    }


    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def updatePermissions = {

        def repo = Repository.get( params.id )
        if (repo && svnRepoService.validateRepositoryPermissions(repo)) {
            repo.setPermissionsOk (true)
            repo.save(validate:false, flush:true)
            flash.message = message(code:'repository.action.permissions.set.ok')
            redirect(action:show,id:params.id)
        }
        else {
            flash.error =message(code: 'repository.action.permissions.set.notOk')
            redirect(action:show,id:params.id)
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def create = {
        def repo = new Repository()
        repo.properties = params
        return [repo:repo]
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def save = {
        def repo = new Repository(params)
        def success = false
        repo.validate()

        if (repo.validateName() && !repo.hasErrors()) {
            def result = svnRepoService
                .createRepository(repo, params.isTemplate == 'true')
            if (result == 0) {
                flash.message = message(code:'repository.action.save.success')
                repo.save()
                redirect(action:show, id:repo.id)
                success = true
            } else {
                repo.errors.reject('repository.action.save.failure')
            }
        }
        if (!success) {
            flash.error = message(code: 'default.errors.summary')
            render(view:'create', model:[repo:repo])
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def editAuthorization = {

        String accessRules = serverConfService.readSvnAccessFile()
        def command = new AuthzRulesCommand(accessRules: accessRules)
        [ authRulesCommand : command]

    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def saveAuthorization = { AuthzRulesCommand cmd ->

        if (!cmd.hasErrors() && serverConfService.writeSvnAccessFile(
                cmd.accessRules)) {
            flash.message = message(
                code: 'repository.action.saveAuthorization.success')
        }
        else {
            flash.error = message(
                code: 'repository.action.saveAuthorization.failure')
        }
        render(view : 'editAuthorization', model : [authRulesCommand : cmd])

    }
}
