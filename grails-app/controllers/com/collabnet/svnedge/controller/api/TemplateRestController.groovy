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

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.domain.RepoTemplate
import grails.converters.JSON
import grails.converters.XML
import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.ServerMode
import com.collabnet.svnedge.util.ControllerUtil

/**
 * REST API controller for creating and listing repository templates
 * <p><bold>URL:</bold></p>
 * <code>
 *   /csvn/api/1/template
 * </code>
 */
@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_REPO'])
class TemplateRestController extends AbstractRestController {

    def repoTemplateService

    /**
     * <p>API to retrieve the list of repository templates. For each template, the name and id are returned. The
     * id may then be used to create new repositories ({@link RepositoryRestController#restSave}). The "showHidden" 
     * parameter indicates whether to return all or just "active" repository templates.</p>
     *
     * <p><bold>HTTP Method:</bold></p>
     * <code>
     *     GET
     * </code>
     * 
     * <p><bold>XML-formatted request body example:</bold></p>
     * <pre>
     * &lt;map&gt;
     *   &lt;entry key="showInactive"&gt;false&lt;/entry&gt;
     * &lt;/map&gt;
     * </pre> 
     * 
     * <p><bold>XML-formatted return example:</bold></p>
     * <pre>
     * &lt;map&gt;
     *   &lt;entry key="templates"&gt;
     *     &lt;map&gt;
     *       &lt;entry key="id"&gt;1&lt;/entry&gt;
     *       &lt;entry key="name"&gt;api-test&lt;/entry&gt;
     *       &lt;entry key="active"&gt;true&lt;/entry&gt;
     *     &lt;/map&gt;
     *   &lt;/entry&gt;
     * &lt;/map&gt;  
     * </pre>    
     */
    def restRetrieve = {
        def result = [:]
        def server = Server.getServer()
        
        // only list repos in standalone mode; no detail view allowed yet
        if (server.mode != ServerMode.STANDALONE || params.id) {
            response.status = 405
            result = [errorMessage: message(code: "api.error.405")]
        }
        else { 
            def templateList = []
            def showInactive = Boolean.valueOf(getRestParam("showInactive"))
            def listing = showInactive ?
                    RepoTemplate.list([sort: 'displayOrder']) :
                    RepoTemplate.findAllByActive(true, [sort: 'displayOrder']) 
            for (RepoTemplate t : listing) {
                repoTemplateService.substituteL10nName(t, request.locale)
                def templateMap = [id: t.id, name: t.name]
                if (showInactive) {
                    templateMap['active'] = t.active
                }
                templateList << templateMap
            }
            result.put("templates", templateList)
        }
        
        withFormat {
            json { render result as JSON }
            xml { render result as XML }
        }
    }

    /**
     * <p>Rest method to create or replace a given repo template with the file contents
     * of the request. The request body is streamed in its entirety to a temporary file
     * and transferred to the templates location. Zip files ("application/zip") of a repo or
     * plain text ("text/plain") dump files are accepted.</p>
     *
     * <p><bold>HTTP Method:</bold></p>
     * <code>
     *   PUT
     * </code>
     *
     * <p><bold>URL:</bold></p>
     * <code>
     *   /csvn/api/1/template/{filename}
     * </code>
     */
    def restUpdate = {
        def result = [:]
        try {
            String destinationFileName = params.id
            if (!destinationFileName) {
                log.warn("Template name must be provided in url")
                throw new IllegalArgumentException(message(code: "api.error.400"))
            }
            File templateDir = repoTemplateService.getUploadDirectory()
            File templateFile = new File(templateDir, destinationFileName)
            File uploadedFile = ControllerUtil.getFileFromRequest(request, templateFile)

            if (!uploadedFile?.bytes.length) {
                log.warn("File upload request contained no file data")
                throw new IllegalArgumentException(message(code: "api.error.400.missingFile"))
            }
            else {
                // move upload to the template staging area
                def success = uploadedFile.renameTo(templateFile)
                // may need to copy from one FS to another
                if (!success) {
                    templateFile.withOutputStream { it << new FileInputStream(uploadedFile) }
                    uploadedFile.delete()
                }
                RepoTemplate t = new RepoTemplate(name: destinationFileName ?: uploadedFile.name)
                repoTemplateService.saveTemplate(t, templateFile, false)
                response.status = 201
                result['message'] = message(code: "api.message.201")
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
}