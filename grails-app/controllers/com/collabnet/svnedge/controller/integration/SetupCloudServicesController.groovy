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
package com.collabnet.svnedge.controller.integration

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.domain.integration.CloudServicesConfiguration
import com.collabnet.svnedge.domain.User
import com.collabnet.svnedge.integration.AuthenticationCloudServicesException;
import com.collabnet.svnedge.util.ControllerUtil

class CloudServicesAccountCommand {
    String username
    String password
    String passwordConfirm
    String domain

    String firstName
    String lastName
    String emailAddress
    String phoneNumber
    String organization
    Boolean acceptTerms

    Locale requestLocale = Locale.default

    static constraints = {
        username(blank: false, matches: "[a-zA-Z0-9_]+", minSize: 3, maxSize: 16)
        password(blank: false, minSize: 6, maxSize: 64)
        passwordConfirm(blank: false,
                validator: { String val, CloudServicesAccountCommand cmd ->
                    if (val != cmd.password) {
                        return "cloudServicesAccountCommand.passwordConfirm.mismatch"
                    }
                }
        )
        domain(blank: false, matches: "[a-zA-Z0-9]+", minSize: 3, maxSize: 32)
        firstName(blank: false)
        lastName(blank: false)
        emailAddress(blank: false, email: true)
        phoneNumber(matches: "[0-9 #()\\+-]+")
        organization(blank: false, minSize: 1, maxSize: 256)
        acceptTerms(
                validator: { Boolean val ->
                    if (val != new Boolean(true)) {
                        return "cloudServicesAccountCommand.acceptTerms.notAccepted"
                    }
                }
        )
    }
}


@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
class SetupCloudServicesController {

    def securityService
    def cloudServicesRemoteClientService

    def index = {
        if (CloudServicesConfiguration.getCurrentConfig()) {
            redirect(action: 'credentials')
        }
    }

    def getStarted = {
        render(view: "signup", model: [cmd: new CloudServicesAccountCommand()])
    }

    def checkLoginAvailability = { CloudServicesAccountCommand cmd ->

        // first validate according to local rules to save roundtrip to api
        def loginAvailable = true
        cmd.validate()
        if (cmd.hasErrors() && (cmd.errors.hasFieldErrors("username"))) {
            loginAvailable = false
        }
        // else, check remote availability
        else {
            loginAvailable = cloudServicesRemoteClientService.isLoginNameAvailable(cmd.username, null)
        }

        render(contentType: "text/json") {
            result(loginAvailable: loginAvailable.toString())
        }
    }

    def createAccount = { CloudServicesAccountCommand cmd ->
        cmd.requestLocale = request.locale
        cmd.validate()
        if (cmd.hasErrors()) {
            render(view: "signup", model: [cmd: cmd])
        }
        else if (cloudServicesRemoteClientService.createAccount(cmd)) {
            // remove error message if it persists from previous submit (occasional grails bug)
            flash.error = null
            flash.message = message(code: "setupCloudServices.page.signup.accountCreation.mustValidate")
            render(view: "confirm", model: [cmd: cmd])
        }
        else {
            flash.error = message(code: "setupCloudServices.page.signup.accountCreation.error")
            render(view: "signup", model: [cmd: cmd])
        }
    }

    def credentials = {
        def cloudConfig = CloudServicesConfiguration.getCurrentConfig()
        def cmd = new CloudServicesAccountCommand()
        if (cloudConfig) {
            cmd.password = securityService.decrypt(cloudConfig.password)
            cmd.username = cloudConfig.username
            cmd.domain = cloudConfig.domain
        }
        render(view: "credentials", model: [cmd: cmd, existingCredentials: (cloudConfig != null)])
    }

    def updateCredentials = { CloudServicesAccountCommand cmd ->
        // with existing config, only password can be updated
        def cloudConfig = CloudServicesConfiguration.getCurrentConfig()
        def existingCredentials = (cloudConfig != null)
        if (cloudConfig) {
            cmd.username = cloudConfig.username
            cmd.domain = cloudConfig.domain
            if (params['password_changed'] != 'true') {
                cmd.password = securityService.decrypt(cloudConfig.password)
            }
        }
        // otherwise, all cmd fields are updateable and a new config is created
        else {
            cloudConfig = new CloudServicesConfiguration();
        }

        // first, field validation
        cmd.requestLocale = request.locale
        cmd.validate()
        if (cmd.hasErrors() && (cmd.errors.hasFieldErrors("username") || cmd.errors.hasFieldErrors("password") ||
                cmd.errors.hasFieldErrors("domain"))) {
            render(view: "credentials", model: [cmd: cmd, existingCredentials: existingCredentials])
        }
        // then validate cloudservices credential against api
        // persist the credentials when successful
        else if (cloudServicesRemoteClientService.validateCredentials(cmd.username, cmd.password, cmd.domain)) {
            cloudConfig.username = cmd.username
            cloudConfig.password = securityService.encrypt(cmd.password)
            cloudConfig.domain = cmd.domain
            cloudConfig.save()
            render(view: "confirm", model: [cmd: cmd])
        }
        else {
            flash.error = message(code: "setupCloudServices.page.credentials.validation.error")
            render(view: "credentials", model: [cmd: cmd, existingCredentials: existingCredentials])
        }
    }

    def selectUsers = {
        def listParams = [:]
        listParams.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        listParams.offset = params.offset ? params.offset.toInteger() : 0
        listParams.sort = "username"
        listParams.order = (params.sort == "username") ? params.order : "asc"
        def localUsers = User.list(listParams)

        def userList = []
        try {
            def remoteUsers = cloudServicesRemoteClientService.fetchUsers()
            localUsers.each() {
                def user = [:]
                user.userId = it.id
                user.username = it.username
                user.realUsername = it.realUserName
                user.emailAddress = it.email
                def remoteUserInfo = null
                def remoteData = remoteUsers.find { remoteItem -> remoteItem.login == it.username || remoteItem.email == it.email}
                if (remoteData) {
                    remoteUserInfo = "${remoteData.login} | ${remoteData.firstName} ${remoteData.lastName} | ${remoteData.email}"
                }
                user.matchingRemoteUser = remoteUserInfo
                user.selectForMigration = (remoteUserInfo == null && it.username != 'admin')
                userList << user
            }
    
            // sort the user list according to params
            userList = userList.sort { it."${params.sort}"}
            if (params.order == "desc") {
                userList = userList.reverse()
            }
        } catch (AuthenticationCloudServicesException auth) {
            flash.unfiltered_error = message(code: auth.message, args: [0])
            redirect(action: 'credentials')
        }
        [userList: userList]
    }

    def createUserLogins = {
        def userList = []
        ControllerUtil.getListViewSelectedIds(params).each {
            User u = User.get(it)
            userList << u
        }
        [userList: userList]
    }

    def migrateUsers = {
        boolean success = true
        ControllerUtil.getListViewSelectedIds(params).each {
            User u = User.get(it)
            def loginName = params."username_${it}"
            success &= cloudServicesRemoteClientService.createUser(u, loginName)
        }
        if (success) {
            flash.message = message(code: 'setupCloudServices.page.migrateUsers.success')
        }
        else {
            flash.error = message(code: 'setupCloudServices.page.migrateUsers.failure')
        }
        forward(action: 'selectUsers')
    }

}
