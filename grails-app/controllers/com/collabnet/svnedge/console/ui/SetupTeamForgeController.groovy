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

import com.collabnet.svnedge.console.CantBindPortException
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.master.RemoteMasterException
import com.collabnet.svnedge.master.RemoteAndLocalConversationException
import com.collabnet.svnedge.master.ctf.CtfSessionExpiredException;
import com.collabnet.svnedge.master.ctf.CtfAuthenticationException
import com.collabnet.svnedge.teamforge.CtfConversionBean;

import java.net.NoRouteToHostException
import java.net.UnknownHostException

@Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
class SetupTeamForgeController {
    private static final String WIZARD_BEAN_KEY = "ctfConversionData"

    def setupTeamForgeService
    def svnRepoService
    
    /**
     * Introduction screen
     */
    def index = { 
        initSessionBean()
        [isFreshInstall: setupTeamForgeService.isFreshInstall()]
    }

    private CtfConversionBean initSessionBean() {
        CtfConversionBean con = session[WIZARD_BEAN_KEY]
        if (!con) {
            con = new CtfConversionBean()
        }
        if (!params.ctfURL && params.ctfUrl) {
            params.ctfURL = params.ctfUrl
        }
        if (params.ctfURL) {
            con.ctfURL = params.ctfURL
            session[WIZARD_BEAN_KEY] = con
        }
        con
    }

    /**
     * CTF credentials screen.  May also be the final screen.
     */
    def ctfInfo = {
        CtfConversionBean con = initSessionBean()
        def savedCon = session[WIZARD_BEAN_KEY]

        def errorCause = null
        def generalError = null
        if (savedCon?.errorMessage) {
            errorCause = savedCon.errorMessage
            def msg = message(code: 
                "setupTeamForge.action.ctfInfo.ctfConnection.error")
            generalError = msg + ": '${savedCon.ctfURL}'"
            session[WIZARD_BEAN_KEY] = null
        }
        [isFreshInstall: setupTeamForgeService.isFreshInstall(), con: con, 
            errorCause: errorCause, connectionErrors: savedCon?.errors, 
            generalError: generalError]
    }

    def confirmCredentials = { CtfConversionBean con ->
        session[WIZARD_BEAN_KEY] = con
        if(!con.hasErrors() && setupTeamForgeService.confirmConnection(con)) {
            con.clearCredentials()
            redirect(action:'ctfProject')
        } else {
            forward(action: 'ctfInfo')
        }
    }
    
    /**
     * Screen to supply project name or possibly multiple projects
     */
    def ctfProject = {
        CtfConversionBean conversionObject = session[WIZARD_BEAN_KEY]
        if (!conversionObject) {
            flash.warn = message(code: 'ctfConversion.session.expired')
            redirect(action:'ctfInfo')
            return
        }
        def result = setupTeamForgeService.validateRepoNames()
        def unfixableRepoNames = result['unfixableRepoNames']
        def duplicatedReposIgnoringCase = 
            result['duplicatedReposIgnoringCase']
        def containsUpperCaseRepos = result['containsUpperCaseRepos']
        def containsReposWithInvalidFirstChar = 
            result['containsReposWithInvalidFirstChar']

        if (unfixableRepoNames || duplicatedReposIgnoringCase || 
            containsUpperCaseRepos || containsReposWithInvalidFirstChar) {

            conversionObject.errors.reject(
                "ctfConversion.ctfProject.invalidReposValidDef")
            conversionObject.errors.reject(
                "ctfConversion.ctfProject.invalidReposExist")
            if (unfixableRepoNames) {
                if (unfixableRepoNames.size() <= 10) {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposList",
                        [unfixableRepoNames].toArray())
                } else {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposSample",
                        [unfixableRepoNames.size(), 
                         unfixableRepoNames[0..<10]].toArray())
                }
                if (!duplicatedReposIgnoringCase) {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposDiscover",
                            ["discover"].toArray())
                }
            }

            if (duplicatedReposIgnoringCase) {
                if (duplicatedReposIgnoringCase.size() <= 10) {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposDupeList",
                        [duplicatedReposIgnoringCase].toArray())
                } else {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposDupeSample",
                        [duplicatedReposIgnoringCase.size(), 
                         duplicatedReposIgnoringCase[0..<10]].toArray())
                }
                conversionObject.errors.reject(
                    "ctfConversion.ctfProject.invalidReposDiscover",
                        ["discover"].toArray())
            }

            if (containsUpperCaseRepos || 
                containsReposWithInvalidFirstChar) {

                if (unfixableRepoNames || duplicatedReposIgnoringCase) {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposAutoFixSome")
                } else {
                    conversionObject.errors.reject(
                        "ctfConversion.ctfProject.invalidReposAutoFixAll")
                }
            }
        }

        def longRepoPath = result['longRepoPath']
        if (longRepoPath) {
            def pathLimit = SetupTeamForgeService.CTF_REPO_PATH_LIMIT
            conversionObject.errors.reject(
                "ctfConversion.ctfProject.invalidReposCtfPathConstraint",
                [pathLimit].toArray())
            conversionObject.errors.reject(
                "ctfConversion.ctfProject.invalidReposCtfPathConstraintEg",
                [longRepoPath].toArray(), longRepoPath)
                conversionObject.errors.reject(
                    "ctfConversion.ctfProject.invalidReposLongPathDiscover",
                        ["discover"].toArray())
            def parentDir = Server.getServer().repoParentDir
            def length = parentDir.length()
            if (length > pathLimit) {
                conversionObject.errors.reject(
                    "ctfConversion.ctfProject.invalidReposTooLongParentPath",
                [parentDir].toArray())

            } else if (length > (pathLimit - Repository.NAME_MAX_LENGTH)) {
                conversionObject.errors.reject(
                    "ctfConversion.ctfProject.invalidReposLongParentPath",
                [parentDir, length].toArray())
            }
        }

        [con: conversionObject, invalidRepoNames: result]
    }
    
    def updateProject = {
        CtfConversionBean con = session[WIZARD_BEAN_KEY]
        if (!con) {
            flash.warn = message(code: 'ctfConversion.session.expired')
            redirect(action:'ctfInfo')
            return
        }
        if (params["ctfProject"]) {
            con.ctfProject = params["ctfProject"].trim()
        }
        con.lowercaseRepos = false
        con.repoPrefix = null
        if (params["lowercaseRepos"]) {
            con.lowercaseRepos = params["lowercaseRepos"]
        }
        if (params["repoPrefix"]) { 
            def conflict = setupTeamForgeService
                .validateRepoPrefix(params["repoPrefix"])
            if (conflict) {
                con.errors.rejectValue("repoPrefix",
                    "ctfConversion.form.ctfProject.repoPrefix.invalid",
                    [conflict].toArray())
            } else {
                con.repoPrefix = params["repoPrefix"]
            }
        }

        def val = setupTeamForgeService.validateRepoNames()
        con.validate()
        if (con.hasErrors() || val.unfixableRepoNames || 
            val.duplicatedReposIgnoringCase ||
            (val.containsUpperCaseRepos && !con.lowercaseRepos) ||
            (val.containsReposWithInvalidFirstChar && !con.repoPrefix)) {

            forward(action:'ctfProject')
            return
        }

        if (params["projectType"] == "single") {
            def projectName = params["ctfProject"]?.trim()
            con.isProjectPerRepo = false
            con.ctfProject = projectName
            if (con.validateProjectName()) {
                def ctfProjectName = null
                try {
                    ctfProjectName = setupTeamForgeService.projectExists(con,
                        projectName)
                } catch (RemoteMasterException serverException) {
                    session[WIZARD_BEAN_KEY] = null
                    def msg = message(code:
                        "setupTeamForge.action.updateProject.creatingProject.error")
                    flash.error = msg + ": " + serverException.getMessage()
                    forward(action:'ctfInfo')
                    return
                }
                if (ctfProjectName) {
                    con.ctfProject = ctfProjectName
                    flash.message = message(code: 
                        "setupTeamForge.action.updateProject.reposRegistered",
                            args: [ctfProjectName])
                } else {
                    flash.message = message(code: 
                        "setupTeamForge.action.updateProject.initialCreation",
                            args: [projectName])
                }
            } else {
                con.errors.rejectValue("ctfProject",
                    "ctfConversionBean.ctfProjec.invalid")
                forward(action:'ctfProject')
                return
            }
        } else if (params.checkExisting) {
            con.isProjectPerRepo = true
            try {
                def conflictedProjects =
                    setupTeamForgeService.getProjectsWhichMatchRepoNames(con)
                if (conflictedProjects.size() == 0 || params.confirmed) {
                    flash.message = message(code: 
                        "setupTeamForge.action.updateProject.reposMatching")
                } else {
                    flash.existingProjects = conflictedProjects
                    redirect(action:"ctfProject")
                }

            } catch (CtfSessionExpiredException sessionExpired) {
                session[WIZARD_BEAN_KEY] = null
                def msg = message(code: 
                    "setupTeamForge.action.updateProject.creatingProject.error")
                flash.error = msg + ": " + sessionExpired.getMessage()
                forward(action:'ctfInfo')
                return
            }

        } else {
            con.isProjectPerRepo = false
        }
        redirect([action:'ctfUsers'])
    }

    /**
     * Screen might show any conflicts between names on the two systems
     */
    def ctfUsers = {
        CtfConversionBean con = session[WIZARD_BEAN_KEY]
        if (!con) {
            flash.warn = message(code: 'ctfConversion.session.expired')
            redirect(action:'ctfInfo')
            return
        }
        try {
            def usrs = setupTeamForgeService.getCsvnUsersComparedToCtfUsers(con)
            [existingUsers: usrs[0], csvnOnlyUsers: usrs[1],
                server: Server.getServer(), wizardBean: con]
        } catch (RemoteMasterException remoteError) {
            session[WIZARD_BEAN_KEY] = null
            def msg = message(code:
                "setupTeamForge.action.ctfUsers.loadingUsers.error")
            flash.error = msg + ": " + remoteError.getMessage()
            redirect(action:'ctfInfo')
        }
    }
    
    def updateUsers = {
        CtfConversionBean con = session[WIZARD_BEAN_KEY]
        if (!con) {
            flash.warn = message(code: 'ctfConversion.session.expired')
            redirect(action:'ctfInfo')
            return
        }
        con.importUsers = (params.importUsers == "true")
        if (con.importUsers) {
            con.assignMembership = (params.assignMembership == "true")
        } else {
            con.assignMembership = false
        }
        redirect(action:'confirm')
    }
    
    /**
     * For extended wizard this will be the final screen
     */
    def confirm = {
        def con = session[WIZARD_BEAN_KEY]
        if (!con) {
            flash.warn = message(code: 'ctfConversion.session.expired')
            redirect(action:'ctfInfo')
            return
        }
        [wizardBean: con]
    }

    /**
     * Handles "Convert" button
     */
    def convert = { CtfConversionBean freshCon ->
        def con = session[WIZARD_BEAN_KEY]
        def freshInstall = setupTeamForgeService.isFreshInstall()
        if (!con && !freshCon) {
            flash.warn = message(code: 'ctfConversion.session.expired')
            redirect(action:'ctfInfo')
            return

        } else if (freshInstall){
            // came from the fresh installation
            con = freshCon
            session[WIZARD_BEAN_KEY] = con
        }

        if (con.hasErrors()) {
            flash.error = message(code: 'setupTeamForge.action.general.error')
            log.error("An error occurred in the conversion process:" + 
                con.errors)
            forward(action : 'ctfInfo')
            return
        }

        if (params.serverKey) {
            con.serverKey = params.serverKey
        }

        // provide console/Jetty SSL status and port number in the conversion data
        // for "/integration" urls
        con.consoleSsl = request.isSecure()
        con.consolePort = request.serverPort

        def model = [wizardBean: con]
        def result = null
        try {
            result = setupTeamForgeService.convert(con)

            def errors = result['errors']
            def warnings = result['warnings']
            def exception = result['exception']
            if (exception) {
                throw exception
            }
            if (!errors) {
                if (con.ctfProject) {
                    def projectPath = con.ctfProjectPath ?: "projects." + 
                        con.ctfProject.replace(" ", "_") 
                    def link = con.ctfURL + 
                        "/sf/scm/do/listRepositories/" + 
                        projectPath + "/scm"
                    model['ctfProjectLink'] = link
                    flash.message = message(
                        code: 'setupTeamForge.action.convert.success')
                } else {
                    def link = con.ctfURL + "/sf/sfmain/do/listSystems"
                    model['ctfLink'] = link
                    flash.message = message(
                        code: 'setupTeamForge.action.convert.success.managed')
                }
                if (warnings) {
                    model['warnings'] = warnings
                }
                session[WIZARD_BEAN_KEY] = null
            } else { 
                for (def error in errors) {
                    if (error.indexOf("Security exception") >= 0) {
                        con.requiresServerKey = true
                    }
                }
                if (con.requiresServerKey) {
                    def apiMsg = message(code: 'ctfConversion.session.expired')
                    errors = [apiMsg]
                }
                flash.errors = errors
                if (errors.size() == 1) {
                    con.errorMessage = errors[0]
                }
                if (setupTeamForgeService.isFreshInstall()) {
                    redirect(action:'ctfInfo')
                } else {
                    redirect(action:'confirm')
                }
            }
            return model
            
        } catch (UnknownHostException unknownHost) {
            // Just display the error on the form
            con.errorMessage = unknownHost.message
            redirect(action: 'ctfInfo')

        } catch (NoRouteToHostException noRouteToServer) {
            // Just display the error on the form
            con.errorMessage = noRouteToServer.message
            redirect(action: 'ctfInfo')

        } catch (CtfAuthenticationException wrongCredentials) {
            // Just display the error on the form
            con.errorMessage = wrongCredentials.message
            redirect(action: 'ctfInfo')

        } catch (CantBindPortException cantConvertScm) {
            // general errors that the administrator needs to take action.
            // 1. BindException due to old httpd.pid.
            def msg = message(code: 'setupTeamForge.action.convert.svn.error')
            flash.error = msg + ": " + cantConvertScm.getMessage()
            redirect(action: 'ctfInfo')

        } catch (RemoteAndLocalConversationException remoteCommProblem) {
            // general errors that the administrator needs to take action.
            // 2. ViewVC or SVN URLs not reachable from the Remote Master.
            def msg = message(code: 
                'setupTeamForge.action.convert.ctfComm.error')
            flash.error = msg + ": " + remoteCommProblem.getMessage()
            session[WIZARD_BEAN_KEY] = null
            redirect(action: 'ctfInfo')

        } catch (CtfSessionExpiredException sessionExpiredDuringConversion) {
            def msg = message(code: 
                'setupTeamForge.action.convert.ctf.canceled')
            flash.error = msg + ": " + 
                sessionExpiredDuringConversion.getMessage()
            session[WIZARD_BEAN_KEY] = null
            redirect(action: 'ctfInfo')

        } catch (RemoteMasterException remoteCommProblem) {
            // general errors that the administrator needs to take action.
            // 2. ViewVC or SVN URLs not reachable from the Remote Master.
            def msg = message(code: 
                'setupTeamForge.action.convert.ctfComm.error')
            flash.error = msg + ": " + remoteCommProblem.getMessage()
            session[WIZARD_BEAN_KEY] = null
            redirect(action: 'ctfInfo')

        } catch (Exception otherException) {
            def msg = message(code: 
                'setupTeamForge.action.convert.cantConvert.error')
            flash.error = msg + ": " + otherException.getMessage()
            session[WIZARD_BEAN_KEY] = null
            redirect(action: 'ctfInfo')
        }
    }

    @Secured(['ROLE_ADMIN','ROLE_ADMIN_REPO'])
    def discover = {
        try {
            svnRepoService.syncRepositories();
            flash.message = message(code: 
                'setupTeamForge.action.discover.sync.repos')
        }
        catch (Exception e) {
            log.error("Unable to discover repositories", e)
            flash.error = message(code: 
                'setupTeamForge.action.discover.sync.cantSync')
        }
        redirect(action:ctfProject)
    }
}
