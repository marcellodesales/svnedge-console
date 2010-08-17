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
class ReplicaController {
    
    def registrationService

    def index = { redirect(action:edit,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save:'POST', update:'POST']

    // This should reflect the fields available in the Replica domain object.
    def fields = [[field: "name", title:"Name", 
                desc: "A simple name for this Replica."],
            [field: "locationName", title:"Location", 
                desc: "Description of where this Replica is."],
            [field: "latitude", title:"Latitude", 
                desc: "The latitude for the Replica's location."],
            [field: "longitude", title:"Longitude", 
                desc: "The longitude for the Replica's location."],
        ]

    def show = {
        def replicaInstance = ReplicaConfig.get( params.id )

        if(!replicaInstance) {
            flash.message = "Replica not found with id ${params.id}"
            redirect(controller:'status', action:'index')
        }
        else { return [ replicaInstance : replicaInstance, fields: fields ] }
    }

    def edit = {
        def replicaInstance = ReplicaConfig.get( params.id )

        if(!replicaInstance) {
            flash.message = "Replica not found with id ${params.id}"
            redirect(controller:'status', action:'index')
        }
        else {
            return [ replicaInstance : replicaInstance, fields: fields ]
        }
    }

    def update = {
        def replicaInstance = ReplicaConfig.get( params.id )
        if(replicaInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(replicaInstance.version > version) {
                    
                    replicaInstance.errors.rejectValue("version", "replica.optimistic.locking.failure", "Another user has updated this Replica while you were editing.")
                    render(view:'edit',model:[replicaInstance:replicaInstance])
                    return
                }
            }
            replicaInstance.properties = params
            if(!replicaInstance.hasErrors() && replicaInstance.save()) {
                // we should reflect these changes on the master
                registrationService.registerReplica()
                flash.message = "Replica ${params.id} updated"
                redirect(action:show,id:replicaInstance.id)
            }
            else {
                render(view:'edit',model:[replicaInstance:replicaInstance])
            }
        }
        else {
            flash.message = "Replica not found with id ${params.id}"
            redirect(controller:'status', action:'index')
        }
    }

    def create = {
        def replicaInstance = new ReplicaConfig()
        replicaInstance.properties = params
        return ['replicaInstance':replicaInstance]
    }

    def save = {
        def replicaInstance = new ReplicaConfig(params)
        if(!replicaInstance.hasErrors() && replicaInstance.save()) {
            flash.message = "Replica ${replicaInstance.id} created"
            redirect(action:show,id:replicaInstance.id)
        }
        else {
            render(view:'create',model:[replicaInstance:replicaInstance])
        }
    }
}
