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

import grails.converters.JSON
import grails.converters.XML
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.web.converters.JSONParsingParameterCreationListener
import org.codehaus.groovy.grails.web.converters.XMLParsingParameterCreationListener
import grails.converters.deep.XML

/**
 * Default "not-implemented" endpoints for rest controllers
 */
abstract class AbstractRestController {

    JSONParsingParameterCreationListener jppcl = null
    XMLParsingParameterCreationListener xppcl = null
    
    def restRetrieve = {
        response.status = 405
        def result = [errorMessage: message(code: "api.error.405")]
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    def restUpdate = {
        response.status = 405
        def result = [errorMessage: message(code: "api.error.405")]
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    def restDelete = {
        response.status = 405
        def result = [errorMessage: message(code: "api.error.405")]
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    def restSave = {
        response.status = 405
        def result = [errorMessage: message(code: "api.error.405")]
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    /**
     * This helper provides the same service as "parseRequest: true" in UrlMappings 
     * (disabled in UrlMappings to avoid stacktraces) 
     * @param params the request params
     */
    void parseRequest(GrailsParameterMap params) {
        if (request.format == "json") {
            if (!jppcl) {
                jppcl = new JSONParsingParameterCreationListener()    
            } 
            jppcl.paramsCreated(params)
        }
        else if (request.format == "xml") {
            if (!xppcl) {
                xppcl = new XMLParsingParameterCreationListener()    
            } 
            xppcl.paramsCreated(params)
        }
    }
}
