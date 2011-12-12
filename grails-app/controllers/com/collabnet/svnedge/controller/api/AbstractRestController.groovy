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
import grails.converters.deep.XML
import org.json.JSONObject
import groovy.util.slurpersupport.GPathResult

/**
 * Default "not-implemented" endpoints for rest controllers
 */
abstract class AbstractRestController {

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
     * This helper will read the JSON or XML request body into a request parameter for use
     * by the <code>getRestParam</code> method
     */
    void parseRestRequest() {
        // return immediately if request has already been parsed
        if (params.requestParsed) {
            return
        }
        params.requestParsed = true
        try {
            if (request.format == "json") {
                params.bodyJson = grails.converters.JSON.parse(request)
            }
            else if (request.format == "xml") {
                params.bodyXml = grails.converters.deep.XML.parse(request)
            }
        } 
        catch (Exception e) {
            log.warn("Unable to parse JSON or XML body in request: ${e.message}")
        }
    }

    /**
     * Convenience method to find an XML or JSON element in the request body. Ensures that
     * <code>parseRestRequest</code> has been run.
     * @param elementKey the key we seek
     * @return the corresponding value, or null
     */
    String getRestParam(String elementKey) {
        
        parseRestRequest()
        
        def elementValue = null
        if (params.bodyXml)  {
            elementValue = params.bodyXml.entry.find({ it.@key == elementKey })?.text() 
        }
        else if (params.bodyJson) {
            elementValue = params.bodyJson[elementKey] 
        }
        return elementValue
    }
}
