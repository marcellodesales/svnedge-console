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

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory

import com.collabnet.svnedge.ConcurrentBackupException;
import com.collabnet.svnedge.console.AbstractSvnEdgeService
import com.collabnet.svnedge.util.*
import com.collabnet.svnedge.domain.Repository

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import com.collabnet.svnedge.controller.integration.CloudServicesAccountCommand
import com.collabnet.svnedge.domain.integration.CloudServicesConfiguration

/**
 * This class provides remote access to the Cloud  Services api
 */
class CloudServicesRemoteClientService extends AbstractSvnEdgeService {

    def commandLineService
    def securityService
    def svnRepoService
    
    private static final long CACHED_CLIENT_TIME_LIMIT = 300L
    private static final long CACHED_CLIENT_LAST_ACCESS_TIME_LIMIT = 60L
    private long mLastAccessTimestamp = 0L
    private long mCreatedTimestamp = 0L
    private RESTClient mAuthenticatedRestClient
    
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
            if (e.message != "Unauthorized") {
                log.warn("Unexpected exception while attempting login", e)
            }
            return false
        }
    }

    /**
     * Find out if a given login name is already in use or not
     * @param loginName to check for availability
     * @param domain in which to search availability (optional -- global search if none provided)
     * @return boolean indicating availability
     */
    def isLoginNameAvailable(String loginName, String domain) throws CloudServicesException {

        def restClient = createRestClient()
        def params = createApiCredentialsMap()
        params["login"] = loginName        
        if (domain) {
            params["domain"] = domain
        }
        try {
            def resp = restClient.get(path: "organizations/isLoginUnique.json",
                    query: params,
                    requestContentType: URLENC)

            return Boolean.valueOf(resp.responseData["loginIsUnique"])
        }
        catch (Exception e) {
            String error = e.response.responseData.error
            log.error("Unable to evaluate login name uniqueness: ${e.message} ${error} ")
        }
        return false
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

            String error = e.response.responseData.error
            log.error("Unable to create Cloud account: ${e.message} - ${error}", e)

            // add error messages to command fields if possible
            if (error.contains("User login unavailable")) {
                cmd.errors.rejectValue("username", getMessage("cloudServicesAccountCommand.username.inUse", cmd.requestLocale))
            }
            if (error.contains("Organization name already in use")) {
                cmd.errors.rejectValue("organization", getMessage("cloudServicesAccountCommand.organization.inUse", cmd.requestLocale))
            }
            if (error.contains("Organization alias already in use")) {
                cmd.errors.rejectValue("domain", getMessage("cloudServicesAccountCommand.domain.inUse", cmd.requestLocale))
            }
            if (error.contains("Organization alias invalid")) {
                cmd.errors.rejectValue("domain", getMessage("cloudServicesAccountCommand.domain.matches.invalid", cmd.requestLocale))
            }

            return false
        }
    }

    /**
    * Lists projects within the configured domain.
    */
   def listProjects(RESTClient restClient = null) throws CloudServicesException {
       if (!restClient) {
           restClient = getAuthenticatedRestClient()
       }
       try {
           def resp = restClient.get(path: "projects.json",
               requestContentType: URLENC)

           if (resp.status != 200) {
               throw CloudServicesException("project.listing.failure")
           }

           def data = resp.data
           log.debug("REST data " + data)

           return data
       }
       catch (Exception e) {
           log.warn("Unable to list Cloud projects", e)
           throw e
       }
       return null
   }

    /**
     * Adds a project within the configured domain.
     * @param projectName Short and long names are the same.
     * @return the projectId
     */
    String createProject(Repository repo, RESTClient restClient = null)
            throws CloudServicesException {
        def body = [:]
        if (!restClient) {
            restClient = createRestClient()
            body = createFullCredentialsMap()
        }
        body.put("shortName", getProjectShortNameForRepository(repo))
        body.put("longName", getProjectLongNameForRepository(repo))
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
            
            // If a project has just been created, then the svn URI should
            // still be unknown, but if the cloud project was deleted, we might
            // have a stale reference, so clear it
            if (repo.cloudSvnUri) {
                repo.cloudSvnUri = null
                repo.save()
            }

            return data['responseHeader']['projectId']
        }
        catch (HttpResponseException hre) {
            def resp = hre.response
            def data = resp.data
            def error = resp.data['error']
            if (error?.contains('Invalid project shortName')) {
                throw new InvalidNameCloudServicesException()
                // current error message is:
                // Failed to create project: You already have NN out of NN projects.
                // if there are other errors which start the same, this might
                // need to be made more rigorous.
            } else if (error?.startsWith('Failed to create project')) {
                throw new QuotaCloudServicesException()
            }
            log.debug("REST data " + data)
            throw new CloudServicesException("Unknown error: " + data.toString())
        }
        catch (Exception e) {
            log.warn("Unable to create Cloud project for repo: " + repo.name, e)
            throw e
        }
        return null
    }

    def retrieveProjectMap(repo, restClient = null) throws CloudServicesException {
        if (!restClient) {
            restClient = getAuthenticatedRestClient()
        }
        def projectName = getProjectShortNameForRepository(repo)
        for (Map projectMap : listProjects(restClient)) {
            log.debug("ProjectMap: " + projectMap)
            if (projectMap['shortName'] == projectName) {
                return projectMap
            }
        }
        return null
    }

    /**
     * Deletes the project given by the ID.
     * @param projectId
     * @return true if the deletion was successful
     */
    boolean deleteProject(projectId) throws CloudServicesException {
        def restClient = getAuthenticatedRestClient()
        try {
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

    private synchronized RESTClient getAuthenticatedRestClient() 
            throws CloudServicesException {
        long now = System.currentTimeMillis()
        if (mAuthenticatedRestClient) {
            if (now - mLastAccessTimestamp < CACHED_CLIENT_LAST_ACCESS_TIME_LIMIT * 1000 &&
                now - mCreatedTimestamp < CACHED_CLIENT_TIME_LIMIT * 1000) {
                 mLastAccessTimestamp = now
                 log.debug("Using cached RESTClient")
                 return mAuthenticatedRestClient    
            } else {
                mAuthenticatedRestClient = null
            }
        }
        
        log.debug("Creating new authenticated RESTClient")
        RESTClient restClient = createRestClient()
        def body = createFullCredentialsMap()
        try {
            def resp = restClient.post(path: "login.json",
                    body: body,
                    requestContentType: URLENC)

            // will be 401 if login fails
            if (resp.status != 200) {
                throw new AuthenticationCloudServicesException()
            }
        } catch (Exception e) {
            if (e.message == "Unauthorized") {
                throw new AuthenticationCloudServicesException()
            } else {
                log.warn("Unexpected exception while attempting login", e)
                throw e
            }
        }
        mAuthenticatedRestClient = restClient
        mCreatedTimestamp = mLastAccessTimestamp = now
        return restClient
    }

    /**
    * Lists services within the configured domain.
    */
   def listServices(RESTClient restClient) throws CloudServicesException {
       try {
           def resp = restClient.get(path: "services.json",
               requestContentType: URLENC)

           if (resp.status != 200) {
               throw CloudServicesException("cloud.services.service.listing.failure")
           }

           def data = resp.data
           log.debug("REST data " + data)
           return data
       }
       catch (Exception e) {
           if (e.message != "Not Found") {
               log.warn("Unable to list Cloud services", e)
               throw e
           }
       }
       return []
   }


    /**
     * Adds the Subversion service to a project
     * @param projectId
     * @return the serviceId
     */
    String addSvnToProject(projectId, RESTClient restClient = null) {
        def body = [:]
        if (!restClient) {
            restClient = createRestClient()
            body = createFullCredentialsMap()
        }
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

    private getProjectShortNameForRepository(Repository repo) {
        return repo.cloudName ?: repo.name
    }

    private getProjectLongNameForRepository(Repository repo) {
        return repo.name
    }

    boolean synchronizeRepository(Repository repo) 
        throws CloudServicesException, ConcurrentBackupException {

        File progressFile = svnRepoService.prepareProgressLogFile(repo.name)
        if (progressFile.exists()) {
            throw new ConcurrentBackupException("repository.action.cloudSync.alreadyInProgress")
        }
        FileOutputStream fos
        try {
            fos = new FileOutputStream(progressFile)
            String preamble = new Date().toString() +
                " Synchronizing repository '" +
                repo.name + "' with cloud services for backup" +
                "\nExecution output below\n----------------------\n"
            write(preamble, fos)

            return synchronizeRepositoryWithProgress(repo, fos)
        } finally {
            fos?.close()
            progressFile.delete()
        }
    }

    /**
     * Lists user accounts within the configured domain.
     */
    def listUsers() throws CloudServicesException {
        def restClient = getAuthenticatedRestClient()
        try {
            def resp = restClient.get(path: "users.json",
                                      requestContentType: URLENC)

            // return the user data as JSON object
            return resp.responseData
        }
        catch (Exception e) {
            if (e.message != "Unauthorized") {
                log.error("Unexpected exception while attempting to fetch Cloud Services users", e)
            }
            else {
                log.error("Credentials not accepted")
            }
        }
        return null
    }
    
    /**
     * creates a cloud services user from the given input
     * @param user the User or map of user properties
     * @param login the cloud login name to use (if null, user.username is tried)
     * @return boolean indicating success or failure
     */
    def createUser(user, login) {

        def restClient = getAuthenticatedRestClient()
        String[] names = user.realUserName?.split(" ")
        def body = [:]
        if (names && names.length > 0) {
            body.put("firstName", names[0])
        }
        if (names && names.length > 1) {
            body.put("lastName", names[names.length - 1])
        }
        body.put("login", (login) ?: user.username)
        body.put("preferredName", user.realUserName)
        body.put("email", user.email)

        try {
            def resp = restClient.post(path: "users.json",
                    body: body,
                    requestContentType: URLENC)

            // sc 201 = created
            return resp.status == 201
        }
        catch (Exception e) {
            String error = e.response.responseData.error
            log.error("Unable to create Cloud account: ${e.message} - ${error}", e)
        }
        return false
    }


    private void write(String s, OutputStream os) {
        os.write((s + "\n").getBytes("UTF-8"))
    }

    private boolean synchronizeRepositoryWithProgress(repo, fos) 
            throws CloudServicesException {
        // confirm that the project exists
        String projectId = null
        def projectName = null
        boolean projectExists = false
        RESTClient restClient = getAuthenticatedRestClient()
        def projectMap = retrieveProjectMap(repo, restClient)
        if (projectMap) {
            projectId = projectMap['projectId']
            projectName = projectMap['shortName']
            projectExists = true
            write("Sync'ing project: " + projectName, fos)
        } else {
            projectName = getProjectShortNameForRepository(repo)
            write("Project did not exist, creating a new project: " +
                projectName, fos)
            projectId = createProject(repo, restClient)
            if (!projectId) {
                throw new CloudServicesException('cloud.services.unable.to.create.project')
            }
        }

        def credMap = createFullCredentialsMap()
        def username = credMap.get('credentials[login]')
        def password = credMap.get('credentials[password]')

        boolean serviceExists = false
        String serviceId = null
        String cloudSvnURI = null
        // check for svn service, if project is new it won't be created yet
        if (projectExists) {
            for (def serviceMap : listServices(restClient)) {
                if (serviceMap['projectId'].toString() == projectId && 
                    serviceMap['serviceType'] == 'svn') {
                    
                    serviceExists = true
                    serviceId = serviceMap['serviceId']
                    if (serviceMap['ready']) {
                        cloudSvnURI = serviceMap['access_url']
                    }
                }
            }
        }

        if (!serviceExists) {
            write("SVN service did not exist, creating it.", fos)
            serviceId = addSvnToProject(projectId, restClient)
            if (!serviceId) {
                throw new CloudServicesException('cloud.services.unable.to.create.svn')
            }
        }
            
        if (!cloudSvnURI) {
            cloudSvnURI = getCloudSvnURI(repo.name, serviceId, restClient)
            if (!cloudSvnURI) {
                throw new CloudServicesException('cloud.services.unable.to.access.svn')
            }
        }

        if (!repo.cloudSvnUri) {
            // prepare sync, the cloud service is not always completely ready
            // when it indicates that it is, so we'll retry this until success 
            // or timeout
            long waitTime = 100
            long maxWaitTime = 300000
            // gives about 6 min 45 seconds for service to initialize
            while (waitTime < maxWaitTime) {
                Thread.sleep(waitTime)
                try {
                    svnsyncInit(repo, cloudSvnURI, username, password, fos)
                    break
                } catch (CloudServicesException e) {
                    write("Cloud service was not ready, trying svnsync init again", fos)
                    waitTime *= 2
                }
            }
            if (waitTime >= maxWaitTime) {
                throw new CloudServicesException('cloud.services.svnsync.init.failure')
            }
        }

        // if access url has changed, update our copy
        if (repo.cloudSvnUri != cloudSvnURI) {
            repo.cloudSvnUri = cloudSvnURI
            repo.save()
        }

        String startSyncMessage = "Syncing repo '${repo.name}' at " +
            "local timestamp: ${new Date()}..."
        log.debug(startSyncMessage)
        write(startSyncMessage, fos)
        def command = [ConfigUtil.svnsyncPath(), "sync", cloudSvnURI,
            "--sync-username", username, "--sync-password", password,
            "--trust-server-cert",
            "--non-interactive", "--no-auth-cache", "--disable-locking",
            "--config-dir", ConfigUtil.svnConfigDirPath()]
        def result =
            commandLineService.execute(command, fos, fos, null, null, true)
        if (result[0] != "0") {
            log.warn("Unable to svnsync sync.  stderr=" + result[2])
            throw new CloudServicesException("cloud.services.svnsync.sync.failure")
        }
    }

    private void svnsyncInit(repo, cloudSvnURI, username, password, fos) {
        write("Initializing cloud repository for sync.", fos)
        File repoPath = new File(svnRepoService.getRepositoryHomePath(repo))
        def localRepoURI = commandLineService.createSvnFileURI(repoPath)
        def command = [ConfigUtil.svnsyncPath(), "init",
            cloudSvnURI, localRepoURI, "--allow-non-empty",
            "--trust-server-cert",
            "--sync-username", username, "--sync-password", password,
            "--non-interactive", "--no-auth-cache",
            "--config-dir", ConfigUtil.svnConfigDirPath()]
        def result =
            commandLineService.execute(command, fos, fos, null, null, true)
        if (result[0] != "0") {
            log.warn("Unable to svnsync init.  stderr=" + result[2])
            throw new CloudServicesException("cloud.services.svnsync.init.failure")
        }
    }
    
    private String getCloudSvnURI(repoName, serviceId, restClient) {
        long waitTime = 100
        try {
            // gives about 6 min 45 seconds for service to initialize
            while (waitTime < 300000) {
                Thread.sleep(waitTime)
                def resp = restClient.get(path: "services/${serviceId}.json",
                                          requestContentType: URLENC)
                if (resp.status == 200) {
                    def data = resp.data
                    log.debug("REST data " + data)                    
                    boolean isReady = data['service']['ready']
                    if (isReady) {
                        return data['service']['access_url']
                    }
                }
                waitTime *= 2
            }
            throw new CloudServicesException('cloud.services.svn.not.ready')
        }
        catch (Exception e) {
            log.warn("Unable to get svn service URI for repository: " + repoName, e)
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
        def creds = [
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
