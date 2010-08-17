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

/**
 * Cache manager will maintain a cache of users from CEE after
 * the inputs are verified before. 
 * 
 * @author mdesales
 */
public class UserCacheController {
    
    // the add user and addRes4Act actions only accept POST requests
    static allowedMethods = [authUser:'POST', oauthUser:'POST']

    // Inject HelmWSClientService
    def cacheManagementService
    
    /**
     * Closure for the index page 
     */
    @Secured(['ROLE_USER'])
    def index = { }

    /**
     * Closure for the view current cache of the unique Master.
     * 
     * @param String domainName is the current domain name of the Master
     * @param String uiRefreshRate is the refresh rate on the UI
     * @param String usersFromMaster is the Set of CachedUser
     */
    @Secured(['ROLE_USER'])
    def view = {
        def currentConfig = ReplicaConfig.getCurrentConfig()
        return [ 
              domainName: cacheManagementService?.defaultMaster.hostName, 
              uiRefreshRate: currentConfig?.cacheFlushPeriod * 500,
              authCache: cacheManagementService?.getCurrentAuthCache(),
              oauthCache: cacheManagementService?.getCurrentOauthCache()
        ]
    }

    /**
     * Closure for the action of the attempt to add a new user to the cache, if
     * the username/password is correctly submitted. It uses the regular HTTP
     * form protocol by passing the form parameters in the default "params"
     * variable. After updating the cache, this action redirects the user to the
     * main view of the cache.
     * 
     * The cache is updated by using the method isUserValid
     * 
     * @param String params.username is the value of the username submitted
     * @param String params.password is the value of the password submitted
     */
    @Secured(['ROLE_ADMIN'])
    def authUser = {
        try {
            cacheManagementService?.authenticateUser(params)
            flash.message = "User ${params.username} authentication cached"
        } catch (Exception e){
            flash.message = "An error occurred while authenticating user " +
                "${params.username}: " + e.getMessage()
        }
        redirect(action:'view')
    }
    
    /**
     * Closure for the action of the attempt to authorize a user, if
     * the username/password is correctly submitted. It uses the regular HTTP
     * form protocol by passing the form parameters in the default "params"
     * variable. After updating the cache, this action redirects the user to the
     * main view of the cache.
     * 
     * @param String params.username is the value of the username submitted
     * @param String params.action is the name of the action submitted
     * @param String projectName is the name of the project
     */
    @Secured(['ROLE_ADMIN'])
    def oauthUser = {
        try {
            cacheManagementService?.authorizeUser(params)
            flash.message = "User ${params.username} authorization cached"
        } catch (Exception e) {
            flash.message = "An error occurred while authorizing user " +
            "${params.username}: " + e.getMessage()
        }
        redirect(action:'view')
    }
}
