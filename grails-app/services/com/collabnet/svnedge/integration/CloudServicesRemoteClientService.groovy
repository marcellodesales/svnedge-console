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

package com.collabnet.svnedge.integration

import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.util.*

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import com.collabnet.svnedge.controller.integration.CloudServicesAccountCommand
import com.collabnet.svnedge.domain.integration.CloudServicesConfiguration

/**
 * This class provides remote access to the Cloud  Services api
 */
class CloudServicesRemoteClientService extends AbstractSvnEdgeService {

    def securityService

    /**
     * validates the provided credentials
     * @param username
     * @param password
     * @param organization
     * @return boolean indicating success or failure
     */
    def validateCredentials(String username, String password, String organization) {
        def restClient = createRestClient()
        def body = createFullCredentialsMap(username, password, organization)
        try {
            def resp = restClient.post(path: "login.json",
                    body: body,
                    requestContentType: URLENC)

            // will be 401 if login fails
            return resp.status == 200
        }
        catch (Exception e) {
            log.warn("Unexpected exception while attempting login", e)
            return false
        }
    }

    /**
     * creates the cloud services account if possible (organization + admin user)
     * @param cmd CloudServicesAccountCommand from the controller
     * @return boolean indicating success or failure
     */
    def createAccount(CloudServicesAccountCommand cmd) {

        def restClient = createRestClient()
        def body = createApiCredentialsMap()
        body.put("user[firstName]", cmd.firstName)
        body.put("user[lastName]", cmd.lastName)
        body.put("user[email]", cmd.emailAddress)
        body.put("user[contactPhone]", cmd.phoneNumber)
        body.put("user[login]", cmd.username)
        body.put("user[password]]", cmd.password)
        body.put("organizationName", cmd.organization)
        body.put("domain", cmd.domain)

        body.put("affirmations[termsOfService]", cmd.acceptTerms)
        body.put("affirmations[privacyPolicy]", cmd.acceptTerms)
        try {
            def resp = restClient.post(path: "organizations.json",
                    body: body,
                    requestContentType: URLENC)

            // sc 201 = created
            if (resp.status != 201) {
                return false
            }

            def cloudConfig = new CloudServicesConfiguration()
            cloudConfig.username = cmd.username
            cloudConfig.password = securityService.encrypt(cmd.password)
            cloudConfig.domain = cmd.domain
            cloudConfig.save()
            return true
        }
        catch (Exception e) {
            log.error("Unable to create Cloud account: " + e.getMessage(), e)
            return false
        }
    }

    /**
     * Adds a project within the configured domain. 
     * @param projectName Short and long names are the same.
     * @return the projectId
     */
    String createProject(projectName) {
        def restClient = createRestClient()
        def body = createFullCredentialsMap()
        body.put("shortName", projectName)
        body.put("longName", projectName)
        try {
            def resp = restClient.post(path: "projects.json",
                    body: body,
                    requestContentType: URLENC)

            // sc 201 = created
            if (resp.status != 201) {
                return null
            }
            
            def data = resp.data
            log.debug("REST data " + data)

            return data['responseHeader']['projectId']
        }
        catch (Exception e) {
            log.warn("Unable to create Cloud project: " + projectName, e)
            throw e
        }
        return null
    }
        
    /**
     * Deletes the project given by the ID.
     * @param projectId 
     * @return true if the deletion was successful
     */
    boolean deleteProject(projectId) {
        def restClient = createRestClient()
        def body = createFullCredentialsMap()        
        try {
            def resp = restClient.post(path: "login.json",
                    body: body,
                    requestContentType: URLENC)

            // will be 401 if login fails
            if (resp.status != 200) {
                log.warn("Unable to delete Cloud projectId: " + projectId +
                    " due to failure to login", e)
                return false
            }

            resp = restClient.delete(path: "projects/" + projectId + ".json")

            // sc 200 = deleted
            if (resp.status == 200) {
                return true
            }
        }
        catch (Exception e) {
            log.warn("Unable to delete Cloud projectId: " + projectId, e)
        }
        return false
    }
    
    /**
     * Adds the Subversion service to a project
     * @param projectId
     * @return the serviceId
     */
    String addSvnToProject(projectId) {
        def restClient = createRestClient()
        def body = createFullCredentialsMap()
        body.put("projectId", projectId)
        body.put("serviceType", "svn")
        try {
            def resp = restClient.post(path: "services.json",
                    body: body,
                    requestContentType: URLENC)

            // sc 201 = created
            if (resp.status != 201) {
                return null
            }
            
            def data = resp.data
            log.debug("REST data " + data)

            return data['responseHeader']['serviceId']
        }
        catch (Exception e) {
            log.warn("Unable to create Cloud service for projectId: " + projectId, e)
            throw e
        }
        return null
    }
    
    /**
     * creates a RESTClient for the codesion API
     * @return a RESTClient
     */
    private RESTClient createRestClient() {
        def restClient = new RESTClient(ConfigUtil.configuration.svnedge.cloudServices.baseUrl)
        def keyStore = SSLUtil.applicationKeyStore
        restClient.client.connectionManager.schemeRegistry.register(
                new Scheme("https", new SSLSocketFactory(keyStore), 443))
        return restClient
    }

    /**
    * creates the initial params map of credentials for authenticated requests to the api
    * based on previously stored values
    * @return map of credentials
    */
   private Map createFullCredentialsMap() {
       CloudServicesConfiguration csConf = CloudServicesConfiguration.getCurrentConfig()
       if (!csConf) {
           throw IllegalStateException("Credentials are unavailable")
       }
       String password = securityService.decrypt(csConf.password)
       return createFullCredentialsMap(csConf.username, password, csConf.domain)   
   }

    /**
     * creates the initial params map of credentials for authenticated requests to the api
     * @param username
     * @param password
     * @param domain
     * @return map of credentials
     */
    private Map createFullCredentialsMap(String username, String password, String domain) {
        def creds  = [
            "credentials[login]": username,
            "credentials[password]": password,
            "credentials[domain]": domain
        ]
        creds.putAll(createApiCredentialsMap())
        return creds
    }

    /**
     * creates the initial params map of credentials of non-authenticated requests to the api (eg, create org)
     * @return map of credentials
     */
    private Map createApiCredentialsMap() {
        [
            "credentials[developerOrganization]": ConfigUtil.configuration.svnedge.cloudServices.credentials.developerOrganization,
            "credentials[developerKey]": ConfigUtil.configuration.svnedge.cloudServices.credentials.developerKey
        ]
    }
}
