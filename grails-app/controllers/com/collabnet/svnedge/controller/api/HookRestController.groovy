/*
 * CollabNet Subversion Edge
 * Copyright (C) 2012, CollabNet Inc. All rights reserved.
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

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import grails.converters.JSON
import grails.converters.XML
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.Repository
import com.collabnet.svnedge.util.ControllerUtil

/**
 * REST API controller for managing repository hook scripts
 * <p><bold>URL:</bold></p>
 * <code>
 *   /csvn/api/1/hook
 * </code>
 */
@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_HOOKS'])
class HookRestController extends AbstractRestController {
    
    def svnRepoService

    /**
     * <p>Rest method to create or replace a given repo hook script with the file contents
     * of the request. The request body is streamed in its entirety to a temporary file
     * and transferred to the repo hooks directory, and can be of any content type.</p>
     *
     * <p><bold>HTTP Method:</bold></p>
     * <code>
     *   PUT
     * </code>
     *
     * <p><bold>URL:</bold></p>
     * <code>
     *   /csvn/api/1/hook/{repoId}/{filename}
     * </code>
     */
    def restUpdate = {
        def result = [:]
        try {
            def repo = Repository.get(params.id)
            String destinationFileName = params.cgiPathInfo
            File uploadedFile = ControllerUtil.getFileFromRequest(request)
            
            if (!uploadedFile?.length()) {
                log.warn("File upload request contained no file data")
                throw new IllegalArgumentException(message(code: "api.error.400.missingFile"))
            }
            else {
                def success = svnRepoService.createOrReplaceHook(repo, uploadedFile, destinationFileName)
                if (!success) {
                    response.status = 500
                    result['errorMessage'] = message(code: "api.error.500")
                    result['errorDetail'] = message(code: "api.error.500.filesystem")
                }
                else {
                    response.status = 201
                    result['message'] = message(code: "api.message.201")
                }
            }
        }
        catch (IllegalArgumentException e) {
            response.status = 400
            result['errorMessage'] = message(code: "api.error.400")
            result['errorDetail'] = e.toString()
            log.warn("Exception handling a REST PUT request", e)
        }
        catch (Exception e) {
            response.status = 500
            result['errorMessage'] = message(code: "api.error.500")
            result['errorDetail'] = e.toString()
            log.warn("Exception handling a REST PUT request", e)
        }

        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }


    /**
    * <p>Rest method to delete a given repo hook script.</p>
    *
    * <p><bold>HTTP Method:</bold></p>
    * <code>
    *   DELETE
    * </code>
    *
    * <p><bold>URL:</bold></p>
    * <code>
    *   /csvn/api/1/hook/{repoId}/{filename}
    * </code>
    */
   def restDelete = {
       def result = [:]
       try {
           def repo = Repository.get(params.id)
           String filename = params.cgiPathInfo
           
           def success = svnRepoService.deleteHookFile(repo, filename)
           if (success) {
               response.status = 200
               result['message'] = message(code: "api.message.200")
           } else {
               response.status = 500
               result['errorMessage'] = message(code: "api.error.500")
               result['errorDetail'] = message(code: "api.error.500.filesystem")
           }
       }
       catch (FileNotFoundException e) {
           response.status = 500
           result['errorMessage'] = message(code: "api.error.500.filesystem")
           result['errorDetail'] = e.toString()
           log.warn("Exception handling a REST DELETE request", e)
       }
       catch (Exception e) {
           response.status = 500
           result['errorMessage'] = message(code: "api.error.500")
           result['errorDetail'] = e.toString()
           log.warn("Exception handling a REST DELETE request", e)
       }

       withFormat {
           json { render result as JSON }
           xml { render result as XML }
       }
   }

}
