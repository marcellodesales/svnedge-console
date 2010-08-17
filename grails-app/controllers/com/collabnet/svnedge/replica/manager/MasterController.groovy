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
package com.collabnet.svnedge.replica.manager

import org.codehaus.groovy.grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class MasterController {
    
    def registrationService

    def index = { redirect(action:edit,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [update:'POST']

    def edit = {
        def masterInstance = Master.getDefaultMaster()

        if(!masterInstance) {
            flash.message = "Master not found!"
            redirect(uri: "/admin/index")
        }
        else {
            return [ masterInstance : masterInstance, params:params ]
        }
    }

    def update = {
        def masterInstance = Master.getDefaultMaster()
        if(masterInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(masterInstance.version > version) {
                    
                    masterInstance.errors.rejectValue("version", "master.optimistic.locking.failure", "Another user has updated this Master while you were editing.")
                    render(view:'edit',model:[masterInstance:masterInstance])
                    return
                }
            }
            def masterChanged = false
            if (masterInstance.getHostName() != params["hostName"]) {
                masterChanged = true
            }
            masterInstance.properties = params
            if(!masterInstance.hasErrors() && masterInstance.save()) {
                
                if (params["onWizard"] == "true") {
                    flash.message = "Default Master now active"
                    redirect(uri:"/")
                } else {
                    def msg = "Master updated"
                    if (masterChanged) {
                        // we need to re-register the replica
                        // the master will be reactivated once the replica
                        // successfully registers itself.
                        registrationService.inactivateMaster()
                        registrationService.registerReplica()
                        msg += "; Re-registering replica"
                    }
                    flash.message = msg
                    redirect(uri: "/admin/index")
                }
            }
            else {
                render(view:'edit',model:[masterInstance:masterInstance])
            }
        }
        else {
            flash.message = "Master not bootstraped."
            redirect(uri: "/admin/index")
        }
    }
}
