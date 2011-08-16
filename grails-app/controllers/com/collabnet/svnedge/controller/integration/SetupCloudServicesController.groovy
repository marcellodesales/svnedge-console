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

    static constraints = {
        username(blank: false)
        password(blank: false)
        passwordConfirm (blank: false,
            validator: { String val, CloudServicesAccountCommand cmd ->
                if (val != cmd.password) {
                    return "cloudServicesAccountCommand.passwordConfirm.mismatch"
                }
            }
        )
        domain (blank: false)
        firstName (blank: false)
        lastName (blank: false)
        emailAddress (blank: false, email: true)
        phoneNumber (blank: true, )
        organization (blank: false)
        acceptTerms (
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

    def index = {
        if (CloudServicesConfiguration.getCurrentConfig()) {
            redirect(action:'credentials')
        }
    }

    def getStarted = {
        render(view: "signup", model: [ cmd: new CloudServicesAccountCommand()])
    }

    def createAccount = { CloudServicesAccountCommand cmd ->
        cmd.validate()
        if (!cmd.hasErrors()) {
            def cloudConfig = new CloudServicesConfiguration()
            cloudConfig.username = cmd.username
            cloudConfig.password = securityService.encrypt(cmd.password)
            cloudConfig.domain = cmd.domain
            cloudConfig.save()
            render(view: "confirm", model: [ cmd: cmd ])
        }
        else {
            render(view: "signup", model: [ cmd: cmd ])
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
        render(view: "credentials", model: [ cmd: cmd, existingCredentials: (cloudConfig != null) ])
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

        // validate input command, but only concerned about subset of fields
        cmd.validate()
        if (cmd.hasErrors() && (cmd.errors.hasFieldErrors("username") || cmd.errors.hasFieldErrors("password") ||
                cmd.errors.hasFieldErrors("domain"))) {
            render(view: "credentials", model: [ cmd: cmd, existingCredentials: existingCredentials ])
        }
        else {
            cloudConfig.username = cmd.username
            cloudConfig.password = securityService.encrypt(cmd.password)
            cloudConfig.domain = cmd.domain
            cloudConfig.save()
            render(view: "confirm", model: [ cmd: cmd ])
        }
    }
}
