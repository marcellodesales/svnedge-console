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

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import com.collabnet.svnedge.console.Server
import org.codehaus.groovy.grails.plugins.springsecurity.Secured

import grails.converters.JSON;

import java.net.NetworkInterface

/**
 * Webflow for the Configuration of the Replica box. It will basically follow
 * a simple workflow as follows:
 * 
 * displayWelcomeMessage -> displayDefaultMaster -> validateMaster -- yes --> displayCurrentReplica -> validateReplica -- yes --> liveSetup
 *                                    ^                 |                                  ^                  |
 *                                    |------- no -------                                  |--------no---------
 */
@Secured(['ROLE_ADMIN'])
class WizardController {

    def defaultAction = 'index'

    def config = ConfigurationHolder.config

    def svnNotificationService
    def uploadErrorsService
    def registrationService
    
    def registerReplica() {
        log.info("Attempting to register the Replica.")
        registrationService.registerReplica()
        def server = Server.getServer()
        server.replica = true
        server.save(flush: true)
        servletContext.setAttribute("server", server)
        session.stepToGo = null
    }

    def index = {
        def masterBox
        if (config.svnedge.ceeMaster) {
            session.masterBox = "CEE"
            session.isCee = true
        } else if (config.svnedge.ctfMaster){
            session.masterBox = "CTF"
            session.isCee = false
        }
        def defaultMaster = Master.getDefaultMaster()
        if (!defaultMaster.isActive) {
            redirect(action:"welcome")
        } else {
            registerReplica()
            redirect(controller:"status", action:"index")
        }
    }

    def redirectToCorrectStep() {
        if (!session?.isCee) {
            redirect(action:"index")
        }
        if (session?.stepToGo < 2) {
            if (actionName != "welcome") {
                redirect(action:"welcome")
            }
        } else if (session?.stepToGo < 3) {
            if (actionName != "setupMaster") {
                if (!session?.isCee) {
                    flash.error = "Master CTF setup not implemented!"
                }
                redirect(action:"setupMaster")
            }
        } else if (session?.stepToGo < 4) {
            if (actionName != "setupReplica") { 
                redirect(action:"setupReplica")
            }
        }
    }

    def welcome = {
        redirectToCorrectStep()
        if (params.continue) {
            session.stepToGo = 2
            redirect(action:"setupMaster")
        }
    }

    /**
     * Displays the default Master information and validate the form; 
     */
    def setupMaster = {
        redirectToCorrectStep()
        def masterInstance = Master.getDefaultMaster()
        if(!masterInstance) {
            flash.error = "Master not bootstraped!"

        } else if (masterInstance){
          return [ masterInstance : masterInstance, params:params ]
        }
    }

    def updateMaster = {
      def masterInstance = Master.get( params.id )
      if(masterInstance) {
          if(params.version) {
              def version = params.version.toLong()
              if(masterInstance.version > version) {
                  
                  masterInstance.errors.rejectValue("version", 
                          "master.optimistic.locking.failure", 
                          "Another user has updated this Master "+
                          "${session.masterBox} while you were editing.")
                  render(view:'setupMaster',
                          model:[masterInstance:masterInstance])
                  return
              }
          }
          //TODO: Implementation of such mechanisms will be done on 
          //the job management side, which will have those API questions
          //answered, as well as the verification of the services in general .
//          if (params?.masterUnreachable) {
//              flash.error = "The Master host is unreachable!"
//              log.info("Master is unreachable!")
//              invalid()
//          } else if (params?.masterPermissionError) {
//              flash.error = "The Master authentication failed!"
//              log.info("The Master authentication failed!")
//              invalid()
//          } else if (params?.missingConfiguration) {
//              flash.error = "The Master host needs configuration!"
//              log.info("The Master host needs configuration!")
//              invalid()
//          }
         
          masterInstance.properties = params
          if(!masterInstance.hasErrors() && masterInstance.save()) {
              flash.message = "Master ${session.masterBox} is Setup... " + 
                  "Proceed with this Replica Setup..."
              session.stepToGo = 3
              redirect(action:"setupReplica")
          }
          else {
              flash.error = "Internal error while saving the Master " +
                  "${session.masterBox}!"
              render(view:'setupMaster',model:[masterInstance:masterInstance])
          }
      }
      else {
          flash.error = "Default Master ${session.masterBox} not bootstraped!"
          redirect(action:welcome)
      }
    }

    /**
     * Displays the default Master information and validate the form; 
     */
    def setupReplica = {
        redirectToCorrectStep()
        def currentConfig = ReplicaConfig.getCurrentConfig()
        def replicaInstance
        if (chainModel?.replicaInstance) {
            replicaInstance = chainModel.replicaInstance
        } else {
            replicaInstance = ReplicaConfig.getCurrentConfig()
        }
        if(!replicaInstance) {
            flash.error = "Replica not bootstraped!"

        } else if (replicaInstance){
            return [ replicaInstance : replicaInstance, 
                currentConfig: currentConfig,
                params:params, 
                replicaRepositoryPath : 
                    svnNotificationService.getReplicaParentDirPath(),
                svnSyncStatusFilePath : 
                    svnNotificationService.getSyncStatusFilePath(),
                svnSyncRate : svnNotificationService.getSvnSyncRate(),
                uploadErrorsRate : uploadErrorsService.uploadErrorsRate,
                uploadErrorRate : uploadErrorsService.getUploadErrorsRate()]
        }
    }

    /**
     * An AJAX call that supplies new values for the select boxes when a new
     * network interface is chosen.
     */
    def handleNetIntUpdate = {
        def ipAddresses = []
        def hostNames = []
        if (params.netIntName) {
            NetworkInterface netInt = NetworkInterface.getByName(params.netIntName)
            if (netInt) {
                ipAddresses = getIPAddresses(netInt)
            }
        }
        render ([ipAddresses: ipAddresses] as JSON)
    }

    /**
     * Return the IP addresses for the given NetworkInterface.
     */
    def getIPAddresses(netInt) {
        def inetAddresses = netInt.getInetAddresses()
        def ipAddresses = inetAddresses.collect{ it.getHostAddress() }
    }

    def updateReplica = {
        def replicaInstance = ReplicaConfig.get( params.id )
        if(replicaInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(replicaInstance.version > version) {

                    replicaInstance.errors.rejectValue("version", 
                             "replica.optimistic.locking.failure", 
                             "Another user has updated this Replica " +
                             "while you were editing.")
                    chain(view:'setupReplica',
                          model:[replicaInstance:replicaInstance])
                    return
                }
            }
            replicaInstance.properties = params
            if(!replicaInstance.hasErrors() && replicaInstance.save()) {
                registerReplica()
                flash.message = "Replica is now registered to the master " +
                                "${session.masterBox}..."
                // User will have to login via the master now.
                redirect(controller:"logout", action:"index")
            }
            else {
                flash.error = "Please correct the Replica errors below."
                chain(action: "setupReplica", 
                      model: [replicaInstance: replicaInstance])
            }
        }
        else {
            flash.error = "Current Replica not bootstraped!"
            session.stepToGo = 1
            redirect(action:welcome)
        }
    }
}
