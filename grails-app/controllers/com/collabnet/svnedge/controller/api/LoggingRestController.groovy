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

import com.collabnet.svnedge.admin.LogManagementService.ConsoleLogLevel
import com.collabnet.svnedge.admin.LogManagementService.ApacheLogLevel

/**
 * Logging configuration REST API controller
 */
@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_SYSTEM'])
class LoggingRestController extends AbstractRestController {

    /**
     * Rest method to view the logging configuration for the Subversion console and server. The log levels are
     * one of the following: DEBUG, INFO, WARN, ERROR. The DaysToKeep field indicates how many days of
     * log to keep (0 means keep all).
     * 
     * URL:
     * <code>
     *   /csvn/api/1/logging
     * </code>
     * 
     * HTTP Method:
     * <code>
     *     GET
     * </code>
     * 
     * Json return example:
     * <code>
     * {
     *   "ConsoleLogLevel": "WARN",
     *   "ServerLogLevel": "WARN",
     *   "DaysToKeep": 3
     * }
     * </code>
     */
    def restRetrieve = {
        def result = [:]
        Server server = Server.getServer()

        result.put "ConsoleLogLevel", server.consoleLogLevel.toString()
        result.put "ServerLogLevel", server.apacheLogLevel.toString()
        result.put "DaysToKeep", server.pruneLogsOlderThan 
        
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    /**
     * Rest method to update the logging configuration for the Subversion server and console. The log levels can be
     * one of the following: DEBUG, INFO, WARN, ERROR. The DaysToKeep field indicates how many days of
     * log to keep (use 0 to keep all). Returns Status Code 201 on success.
     * 
     * URL:
     * <code>
     *   /csvn/api/1/logging
     * </code>
     * 
     * HTTP Method:
     * <code>
     *   PUT
     * </code>    
     * 
     * Request body example (JSON):
     * <code>
     * {
     *   "ConsoleLogLevel": "WARN",
     *   "ServerLogLevel": "WARN",
     *   "DaysToKeep": 3
     * }
     * </code>
     */
    def restUpdate = {
        def consoleLogLevel = getRestParam("ConsoleLogLevel")
        def apacheLogLevel = getRestParam("ServerLogLevel")
        def daysToKeep = getRestParam("DaysToKeep")
        
        def result = [:]
        try {
            Server s = Server.getServer()
            s.consoleLogLevel = ConsoleLogLevel.valueOf(consoleLogLevel)
            s.apacheLogLevel = ApacheLogLevel.valueOf(apacheLogLevel)
            s.pruneLogsOlderThan = Integer.valueOf(daysToKeep)
            s.save()
            response.status = 201
            result['message'] = message(code: "api.message.201")
        }
        catch (Exception e) {
            response.status = 400
            result['errorMessage'] = message(code: "api.error.400")
            log.warn("Exception handling a REST PUT request", e)
        }
        
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }
}

