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
package com.collabnet.svnedge.integration


import org.mortbay.log.Log;
import org.springframework.security.GrantedAuthority
import org.springframework.security.GrantedAuthorityImpl
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserImpl
import grails.util.GrailsUtil
import org.apache.axis.AxisFault
import com.collabnet.ce.soap60.webservices.ClientSoapStubFactory as ClientSoapStubFactory60
import com.collabnet.ce.soap60.webservices.cemain.ICollabNetSoap as ICollabNetSoap60
import com.collabnet.ce.soap60.webservices.scm.IScmAppSoap as IScmAppSoap60
import com.collabnet.ce.soap60.fault.LoginFault as LoginFault60
import com.collabnet.ce.soap60.types.SoapNamedValues as SoapNamedValues60;
import com.collabnet.ce.soap50.webservices.ClientSoapStubFactory
import com.collabnet.ce.soap50.webservices.cemain.ICollabNetSoap
import com.collabnet.ce.soap50.webservices.cemain.UserSoapDO
import com.collabnet.ce.soap50.webservices.scm.IScmAppSoap;
import com.collabnet.ce.soap50.types.SoapNamedValues;
import com.collabnet.ce.soap50.fault.IllegalArgumentFault;
import com.collabnet.ce.soap50.fault.InvalidSessionFault;
import com.collabnet.ce.soap50.fault.LoginFault
import com.collabnet.ce.soap50.fault.ObjectAlreadyExistsFault;
import com.collabnet.ce.soap50.fault.PermissionDeniedFault;
import com.collabnet.ce.soap50.fault.SystemFault;
import com.collabnet.ce.soap50.fault.UserLimitExceededFault;
import com.collabnet.svnedge.console.AbstractSvnEdgeService;
import com.collabnet.svnedge.domain.User 
import com.collabnet.svnedge.domain.integration.ApprovalState 
import com.collabnet.svnedge.domain.integration.CtfServer 
import com.collabnet.svnedge.domain.integration.ReplicaConfiguration 

import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.net.MalformedURLException


/**
 * CTFWsClientService defines the service used by SVNEdge to communicate with 
 * a Master CTF based on the bootstrapped location information (url, port, ssl
 * system ID. Although this class is called a Web Service client, it exposes
 * proxy methods for one HTTP GET request method.
 * 
 * For user authentication, this service uses the CTF SDK, requesting a login
 * with the username and password. On the other hand, user authorization is 
 * requested by using the ScmPermissionsProxyServlet.
 * 
 * For the CTF SDK, 
 * visit http://www.open.collab.net/community/cif/ctf/52/sdk.tar.gz
 *
 * @author Marcello de Sales(mdesales@collab.net)
 */
public class CtfRemoteClientService extends AbstractSvnEdgeService {

    private static String ROLE_USER = "ROLE_USER"
    private static String ROLE_ADMIN = "ROLE_ADMIN"
    /**
     * The prefix of the command ids.
     */
    public static final String COMMAND_ID_PREFIX = "cmdexec"

    def securityService
    def networkingService

    boolean transactional = false

    /**
     * Closure to build the list of parameters for a URL
     */
    def buildParam = { map ->
        def allParams = ""
        map.each{ key, value -> allParams += "$key=$value&" }
        return allParams[0..-2]
    }

    def registerReplica(server, replica) {
        ApprovalState.APPROVED.getName()
    }

    def getReplicaApprovalState() {
        ApprovalState.APPROVED.getName()
    }

    /**
     * @return the URL to the CTF server based on the configuration.
     */
    private makeCtfBaseUrl(useSsl, hostname, port) {
        def ctfProto = useSsl ? "https://" : "http://"
        def ctfPort = port == 80 ? "" : ":" + port
        return ctfProto + hostname + ctfPort
    }

    public ICollabNetSoap cnSoap(ctfBaseUrl) {
        return (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(
            ICollabNetSoap.class, ctfBaseUrl ?: CtfServer.getServer().baseUrl)
    }

    private ICollabNetSoap60 cnSoap60(ctfBaseUrl) {
        return (ICollabNetSoap60) ClientSoapStubFactory60.getSoapStub(
            ICollabNetSoap60.class, ctfBaseUrl ?: CtfServer.getServer().baseUrl)
    }

    public IScmAppSoap makeScmSoap(url) {
        return (IScmAppSoap) ClientSoapStubFactory.getSoapStub(
            IScmAppSoap.class, url ?: CtfServer.getServer().baseUrl)
    }

    public IScmAppSoap60 makeScmSoap60(url) {
        return (IScmAppSoap60) ClientSoapStubFactory60.getSoapStub(
            IScmAppSoap60.class, url ?: CtfServer.getServer().baseUrl)
    }

    private String authzBaseUrl() {
        // FIXME! This needs to be invoked on the local appserver, but
        // keeping it this way, so that tests continue to pass
        CtfServer.getServer().baseUrl +
            "/integration/servlet/ScmPermissionsProxyServlet?"
    }

    public String makeLogin(def cnSoap, ctfUrl, username, password, 
            locale) throws CtfAuthenticationException, RemoteMasterException, 
           UnknownHostException, NoRouteToHostException, MalformedURLException {

        try {
            return cnSoap.login(username, password)

        } catch (AxisFault e) {
            GrailsUtil.deepSanitize(e)
            if (e.faultString.contains("password was set by an admin")) {
                def msg = getMessage("ctfRemoteClientService.auth.needschange",
                    [ctfUrl.encodeAsHTML()], locale)
                log.info(msg)
                throw new CtfAuthenticationException(msg, 
                    "ctfRemoteClientService.auth.needschange")

            } else if (e.faultString.contains("Error logging in.")) {
                def msg = getMessage("ctfRemoteClientService.auth.error", 
                    [ctfUrl.encodeAsHTML()], locale)
                log.info(msg)
                throw new CtfAuthenticationException(msg, 
                    "ctfRemoteClientService.auth.error")

            } else if (e.faultString.contains("SSLHandshakeException")) {
                def msg = getMessage("ctfRemoteClientService.ssl.error", 
                    ["http://help.collab.net/index.jsp?topic=/csvn/action/" +
                        "csvntotf_ssl.html"], locale)
                log.warn(msg)
                throw new CtfAuthenticationException(msg,
                    "ctfRemoteClientService.ssl.error")

            } else if (e.detail instanceof UnknownHostException) {
                def hostname = new URL(ctfUrl).host
                throw new UnknownHostException(getMessage(
                    "ctfRemoteClientService.host.unknown.error", 
                    [hostname.encodeAsHTML()], locale))

            } else if (e.detail instanceof NoRouteToHostException) {
                def hostname = new URL(ctfUrl).host
                throw new NoRouteToHostException(getMessage(
                    "ctfRemoteClientService.host.unreachable.error", 
                    [hostname.encodeAsHTML()], locale))

            } else {
                def msg = getMessage("ctfRemoteClientService.auth.error",
                    [ctfUrl.encodeAsHTML()], locale)
                log.error(msg, e)
                throw new RemoteMasterException(ctfUrl, msg, e)
            }
        } catch (Exception otherErrors) {
            throw new MalformedURLException(getMessage(
                "ctfRemoteClientService.host.malformedUrl", 
                [ctfUrl.encodeAsHTML()], locale))
        }
    }

    /**
     * @param username is the username identification
     * @param password is the password.
     * @return the User Session ID from the CTF server. 
     */
    public String login(username, password, locale) {
        return makeLogin(cnSoap(), CtfServer.server.baseUrl, username, password,
            locale)
    }

    /**
     * @param ctfUrl is the URL for the CTF server in the format 
     * 'protocol://domainNAme:portNumber'.
     * @param username is the username identification
     * @param password is the password.
     * @return the User Session ID from the given CTF server URL.
     * @throws CtfAuthenticationException in case the login is incorrect
     * @throws RemoteMasterException in case any other exception occurs
     */
    public String login(ctfUrl, username, password, locale) {
        return makeLogin(cnSoap(ctfUrl), ctfUrl, username, password, locale)
    }

    public String login60(ctfUrl, username, password, locale) {
        return makeLogin(cnSoap60(ctfUrl), ctfUrl, username, password, locale)
    }

    public void logoff60(ctfUrl, username, sessionId) {
        try {
            cnSoap60(ctfUrl).logoff(username, sessionId)
        } catch (Exception e) {
            log.warn("Logging off from session " + sessionId + " failed.", e)
        }
    }
    
    /**
     * Authenticate against the CTF server.
     * WARNING: if the CTF master's admin pw is default, this may
     * return true for empty usernames.
     * @param username is an existing username on the Master CTF. 
     * @param password is the associated password for the given username. 
     * @return GrailsUser, if auth succeeds, null otherwise
     */
    GrailsUser authenticateUser(username, password) {
        GrailsUser gUser = null
        String sessionId = null
        try {
            sessionId = cnSoap().login(username, password)
        } catch (LoginFault e) {
            // no session
        } catch (AxisFault e) { 
            String msg = e.faultCode
            // don't log LoginFault even if converted to AxisFault
            if (!msg || msg.indexOf("LoginFault") < 0) {
                GrailsUtil.deepSanitize(e)
                log.warn(msg + " Unable to authenticate user='" + 
                    username + "' due to exception", e)
            }
        } catch (Exception e) {
            GrailsUtil.deepSanitize(e)
            // also no session, but log this one as it indicates a problem
            if (!(e instanceof LoginFault)) {
                log.warn("Unable to authenticate user='" + username +
                    "' due to exception", e)
            }
        }

        if (null != sessionId && sessionId.length() > 0) {
            gUser = getUserDetails(sessionId, username)

            try {
                cnSoap().logoff(username, sessionId)
            } catch (Exception e) {
                GrailsUtil.deepSanitize(e)
                log.warn("Unable to logoff from session for " + username + 
                    " due to exception", e)
            }
        }
        gUser
    }

    /**
     * @param username for the user.
     * @return information about the user
     */
   private GrailsUser getUserDetails(sessionId, username) {
        UserSoapDO ctfUser = cnSoap().getUserData(sessionId, username)

        // TODO CTF REPLICA
        // Using a domain object here to avoid introducing another user
        // object until it is decided whether we should maybe use 
        // ReplicaUser here.
        User u = new User(username: username, 
            realUserName: ctfUser.fullName,
            email: ctfUser.email)
        // not sure if this is needed on a new object, but we don't want
        // the data saved to the db, so adding it for safety
        u.discard()

        // trues =>  enabled, accountNonExpired, credentialsNonExpired,
        //           accountNonLocked,
        new GrailsUserImpl(username, "password", true, true, true, true, 
                           getGrantedAuthorities(ctfUser), u)
    }

    private GrantedAuthority[] getGrantedAuthorities(ctfUser) {
        GrantedAuthority[] auth
        if (ctfUser.superUser) {
            auth = new GrantedAuthority[2]
            auth[0] = new GrantedAuthorityImpl(ROLE_USER)
            auth[1] = new GrantedAuthorityImpl(ROLE_ADMIN)
        } else if (ctfUser.restrictedUser) {
            auth = new GrantedAuthority[0]
        } else {
            auth = new GrantedAuthority[1]
            auth[0] = new GrantedAuthorityImpl(ROLE_USER)
        }
        auth
    }

    private static String PERM_USER = "view"
    private static String PERM_ADMIN = "admin"

    def getReplicaPermissions(username) {
        // TODO CTF REPLICA
        def perms = []
        if (username == "root") {
            perms = [PERM_USER, PERM_ADMIN]
        } else if (username == "marcello") {
            perms = [PERM_USER]
        }
        log.info("CTF Perms=" + perms + " for username=" + username)
        perms
    }


    /**
     * This method defines the user authorization against a Master CTF. The
     * target Master url is constructed during the bootstrap, so that it can
     * process the given username, repository Path, and the optional access 
     * Type. The access to the Master CTF is performed through an HTTP GET 
     * request to a Servlet.
     * 
     * def getScmPermissionForPath(username, system_id, repo_path, access_type)
     * https://forge.collab.net/integration/viewvc/viewvc.cgi/trunk/core/
     * saturn/src/sourceforge_home/integration/SourceForge.py?revision=
     * 29863&root=ce&system=exsy1017&view=markup
     * 
     * @param username is the username of a given user on the Master CTF.
     * @param repoPath is the repository path. It can be reponame/path/to/smth
     * @param accessType is how the access must be granted. 
     * @throws IOException if any communication problem occurs with the Master
     * (Not reachable or unknown host). The server can return a 401 HTTP 
     * Response in case a Master CEE is used instead. (which is strange since
     * it's a bad request that results in Unauthorized)
     * 
     */
    String getRolePaths(username, repoPath, accessType) {
        def params = [:]
        String systemId = CtfServer.getServer().mySystemId
        if (accessType)
            params = ["username":username, "systemId":systemId, 
                      "repoPath":repoPath, "accessType":accessType]
        else
            params = ["username":username, "systemId":systemId, 
                      "repoPath":repoPath]
        def rolesPathRestUrl = authzBaseUrl() + buildParam(params)
        return rolesPathRestUrl.toURL().text
    }

    /**
     * Clears the cache at the SCM Permissions servlet. This requests clean
     * cache for the authentication servlet.
     * @return if the operation to clear the remote cache was successfully 
     * executed.
     * @throws IOException if any communication problem occurs with the Master
     * (Not reachable or unknown host). The server can return a 401 HTTP 
     * Response in case a Master CEE is used instead. (which is strange since
     * it's a bad request that results in Unauthorized)
     */
    boolean clearCacheOnMasterCTF() {
        def params = [clearCache:true]
        def rolesPathRestUrl = authzBaseUrl() + buildParam(params)
        return rolesPathRestUrl.toURL().text.trim() == 
            "permissions cache cleared"
    }

    def uploadStatistics(valuesByStats) {
        def results = [:]
        /* TODO CTF REPLICA
        def proxy = getReplicaProxy()
        def statRequests = valuesByStats.collect{ valuesByStat ->
            buildStatisticsRequestType(valuesByStat, proxy)
        }
        def uploadResults = proxy.uploadStatistics(getHostname(), 
                                                   statRequests)
        uploadResults.result.each {
            results[it.statistic] = it.succeeded
            if (!it.succeeded && it.failureMsg) {
                log.error("Uploading " + it.statistic + " failed due to " 
                          + it.failureMsg)
            } else if (!it.succeeded) {
                log.error("Uploading " + it.statistic + " failed")
            }
        }
        */
        results
    }

    private def buildStatisticsRequestType(valuesByStat, proxy) {
        def statRequest = proxy
            .create("com.collabnet.helm.ws.svnedge.StatisticsRequestType")
        statRequest.statistic = valuesByStat["statistic"].getName()
        statRequest.values = valuesByStat["values"].collect { value ->
            buildStatValueType(value, proxy)
        }
        return statRequest
    }

    private def buildStatValueType(value, proxy) {
        def statValue = proxy
            .create("com.collabnet.helm.ws.svnedge.StatValueType")
        statValue.timestamp = value.getTimestamp()
        statValue.interval = value.getInterval()
        statValue.minValue = value.getMinValue()
        statValue.maxValue = value.getMaxValue()
        statValue.averageValue = value.getAverageValue()
        statValue.lastValue = value.getLastValue()
        statValue.derived = value.getDerived()
        return statValue
    }

    def getSVNNotifications(timestamp) {
    }

    def uploadReplicaErrors(errors) {

    }

    /**
     * Adds this instance of SVN Edge as a new external system at a given
     * CTF located at the given ctfUrl.
     * 
     * @param ctfUrl is the url of the CTF system. 
     * @param userSessionId is sessionID of the default user to call the soap
     * client.
     * @param title is the title of the new external system
     * @param description is the description of the new external system
     * @param csvnProps is an instance of SoapNamedValues with the parameters
     * to the service.
     * 
     * @return the String value of the system ID
     * 
     * @throws CtfAuthenticationException if the authentication fails with 
     * TeamForge.
     * @throws RemoteMasterException if any error occurs during the method 
     * call.
     */
    def String addExternalSystem(ctfUrl, userSessionId, adapterType, title, 
            description, csvnProps, locale) {

        try {
           def scmSoap = this.makeScmSoap(ctfUrl)
           def sessionId = scmSoap.addExternalSystem(userSessionId, adapterType,
                   title, description, csvnProps)
           return sessionId
        } catch (LoginFault e) {
            def msg = getMessage("ctfRemoteClientService.auth.error", [ctfUrl],
                locale)
            throw new CtfAuthenticationException(userSessionId, ctfUrl, msg, e)
            log.error("Unable to create external system: " + msg, e)

        } catch (AxisFault e) {
            String faultMsg = e.faultString
            if (faultMsg.contains("The parameter type/value") && 
                faultMsg.contains("is invalid for the adapter type")) {
                // No such object: The parameter type/value 
                // 'RepositoryBaseUrl=http://cu064.cloud.sp.collab.net:18080/svn' 
                // is invalid for the adapter type 'Subversion'
                def typeValue = faultMsg.split("'")[1].split("=")
                def paramType = typeValue[0]
                def paramValue = typeValue[1]
                GrailsUtil.deepSanitize(e)
                if (paramType.equals("RepositoryBaseUrl")) {
                    throw new RemoteAndLocalConversationException(ctfUrl,
                        getMessage(
                            "ctfRemoteClientService.local.webdav.unreachable",
                            [paramValue], locale))

                } else if (paramType.equals("ScmViewerUrl")) {
                    throw new RemoteAndLocalConversationException(ctfUrl, 
                        getMessage(
                            "ctfRemoteClientService.local.viewvc.unreachable",
                            [paramValue], locale))

                } else {
                    def msg = getMessage(
                            "ctfRemoteClientService.local.remote.general.error",
                            [paramValue], locale)
                    throw new RemoteAndLocalConversationException(ctfUrl, msg +
                        " ${faultMsg}")
                }

            } else if (faultMsg.contains("Session is invalid or timed out")) {
                throw new CtfSessionExpiredException(ctfUrl, userSessionId,
                    getMessage("ctfRemoteClientService.remote.sessionExpired", 
                        locale), e)
            }

            // don't log LoginFault even if converted to AxisFault
            if (!faultMsg || faultMsg.indexOf("LoginFault") < 0) {
                GrailsUtil.deepSanitize(e)
            }
            def generalMsg = faultMsg + " " + getMessage(
                "ctfRemoteClientService.createExternalSystem.error", locale)
            log.error(generalMsg, e)
            throw new RemoteMasterException(ctfUrl, generalMsg, e)

        } catch (Exception e) {
            GrailsUtil.deepSanitize(e)
            // also no session, but log this one as it indicates a problem
            if (!(e instanceof LoginFault)) {
                def generalMsg = getMessage(
                    "ctfRemoteClientService.createExternalSystem.error", locale)
                log.error(generalMsg, e)
                throw new RemoteMasterException(ctfUrl, generalMsg, e)
            }
        }
    }
            
    /**
    * The list of replicable external systems element of the list is a
    * map of the properties of the integration server. Each elements has the
    * properties of the external system.
    *
    * @param ctfUrl is the ctf server complete URL, including protocol,
    * hostname and port.
    * @param sessionId is the user sessionId retrieved after logging in.
    * @param locale is the request locale for messaging
    * @return the list of replicable external systems in the TeamForge server
    * reached by the given ctfUrl, using the given sessionId.
    * @throws CtfSessionExpiredException if the given sessionId is expired.
    * @throws RemoteMasterException if any other error occurs during the
    * method execution.
    */
   def getReplicableScmExternalSystemList(ctfUrl, sessionId, locale) throws
          RemoteMasterException {
       try {
           def lt = this.makeScmSoap60(ctfUrl).getReplicableScmExternalSystemList(
               sessionId).dataRows
           def scmList = []
           if (lt && lt.length > 0) {
               lt.each { extSystem ->
                   def scmSys = [:]
                   scmSys.id = extSystem.id
                   scmSys.title = extSystem.title
                   scmSys.description = extSystem.description
                   scmSys.isSvnEdge = extSystem.isSvnEdge
                   scmList << scmSys
               }
           }
           return scmList

       } catch (AxisFault e) {
           String faultMsg = e.faultString
           GrailsUtil.deepSanitize(e)
           if (faultMsg.contains("Session is invalid or timed out")) {
               throw new CtfSessionExpiredException(ctfUrl, sessionId,
                   getMessage("ctfRemoteClientService.remote.sessionExpired",
                       locale), e)
           } 
           else if (faultMsg.contains("No such operation")) {
               def errorMessage = getMessage(
                  "ctfRemoteClientService.host.noReplicaSupport.error", 
                  [ctfUrl], locale) 
               log.error(errorMessage, e)
               throw new RemoteMasterException(ctfUrl, errorMessage, e)
           }
           else {
               def errorMessage = getMessage(
                  "ctfRemoteClientService.listProjects.error", locale) + " " +
                      faultMsg
              log.error(errorMessage, e)
              throw new RemoteMasterException(ctfUrl, errorMessage, e)
          }
       }
    }

    /**
    * Adds this instance of SVN Edge as a new external system at a given
    * CTF located at the given ctfUrl.
    *
    * @param ctfUrl is the url of the CTF system.
    * @param userSessionId is sessionID of the default user to call the soap
    * client.
    * @param title is the title of the new external system
    * @param description is the description of the new external system
    * @param replicaProps is an instance of SoapNamedValues with the parameters
    * to the service.
    *
    * @return the String value of the system ID
    *
    * @throws CtfAuthenticationException if the authentication fails with
    * TeamForge.
    * @throws RemoteMasterException if any error occurs during the method
    * call.
    */
    def String addExternalSystemReplica(ctfUrl, userSessionId, masterSystemId, 
            name, description, comment, replicaProps, locale)
            throws RemoteMasterException { 

        try {
            def scmSoap = this.makeScmSoap60(ctfUrl)
            def replicaId = scmSoap.addExternalSystemReplica(userSessionId,
                masterSystemId, name, description, comment, 
                makeSoapNamedValues60(replicaProps))

            return replicaId

        } catch (LoginFault60 e) {
            def msg = getMessage("ctfRemoteClientService.auth.error", [ctfUrl],
                locale)
            log.error("Unable to create external system: " + msg, e)
            throw new CtfAuthenticationException(userSessionId, ctfUrl, msg, e)

        } catch (AxisFault e) {
             String faultMsg = e.faultString
            
            // this code is silently swallowing exceptions -- disabled
//             if (faultMsg.contains("The parameter type/value") &&
//                faultMsg.contains("is invalid for the adapter type")) {
//                // No such object: The parameter type/value
//                //'RepositoryBaseUrl=http://cu064.cloud.sp.collab.net:18080/svn'
//                // is invalid for the adapter type 'Subversion'
//                def typeValue = faultMsg.split("'")[1].split("=")
//                def paramType = typeValue[0]
//                def paramValue = typeValue[1]
//                GrailsUtil.deepSanitize(e)

            if (faultMsg.contains("Session is invalid or timed out")) {
               throw new CtfSessionExpiredException(ctfUrl, userSessionId,
                  getMessage("ctfRemoteClientService.remote.sessionExpired",
                       locale), e)
            }
             else {
                throw new RemoteMasterException(e.faultString, e)
             }
            
         } catch (Exception e) {
           
             GrailsUtil.deepSanitize(e)
             def generalMsg = getMessage(
                 "ctfRemoteClientService.createExternalSystem.error", 
                 locale)
             log.error(generalMsg, e)
             throw new RemoteMasterException(ctfUrl, generalMsg, e)
         }
   }

    /**
     * Creates a new instance of SoapNamedValues based on the array lists
     * @param names are the name of the properties
     * @param values are the values for each of the properties
     * @return a new instance of SoapNamedValues
     */
    private SoapNamedValues60 makeSoapNamedValues60(props) {
        def soapNamedValues = new SoapNamedValues60()
	String[] names, values
	if (!props) {
	    names = values = new String[0]
        } else {
            names = new String[props.size()]
            values = new String[props.size()]
	    int i = 0;
            for (def entry: props.entrySet()) {
                names[i] = entry.key
                values[i++] = String.valueOf(entry.value) 
            }
        }
        soapNamedValues.setNames(names);
        soapNamedValues.setValues(values);
        return soapNamedValues
    }

    /**
     * Deletes this replica from the CTF system with the supplied credentials
     * (requires superuser)
     * @param ctfUsername
     * @param ctfPassword
     * @param errors
     * @param locale
     * @throws RemoteMasterException
     * @throws CtfAuthenticationException
     */
    def deleteReplica(ctfUsername, ctfPassword, errors, locale) throws CtfAuthenticationException, RemoteMasterException {

        def ctfUrl = CtfServer.getServer().baseUrl
        def replicaConfig = ReplicaConfiguration.getCurrentConfig()
        def replicaId = replicaConfig.systemId
        def sessionId
        try {
            sessionId = login60(ctfUrl, ctfUsername, ctfPassword, locale)
            def scmSoap = this.makeScmSoap60(ctfUrl)
            scmSoap.deleteExternalSystemReplica(sessionId, replicaId)
        }
        catch (LoginFault60 e) {
            GrailsUtil.deepSanitize(e)
            if (e.faultString.contains("password was set by an admin")) {
                def msg = getMessage("ctfRemoteClientService.auth.needschange",
                    [ctfUrl.encodeAsHTML()], locale)
                log.error(msg)
                errors << msg
                throw new CtfAuthenticationException(msg, 
                    "ctfRemoteClientService.auth.needschange")

            } else if (e.faultString.contains("Error logging in.")) {
                def msg = getMessage("ctfRemoteClientService.auth.error", 
                    [ctfUrl.encodeAsHTML()], locale)
                log.error(msg)
                errors << msg
                throw new CtfAuthenticationException(msg, 
                    "ctfRemoteClientService.auth.error")
            }
        }
        catch (AxisFault e) {
            if (e.faultCode.toString().contains("PermissionDeniedFault")) {
                def msg = getMessage("ctfRemoteClientService.permission.error", 
                    [ctfUrl.encodeAsHTML()], locale)
                log.error(msg)
                errors << msg
                throw new CtfAuthenticationException(msg, 
                    "ctfRemoteClientService.permission.error")
            }
            else if (e.faultString.contains("No such object: ${replicaId}")) {
                // this fault suggests that the replica server was already removed on the CTF side but not yet reverted
                // to standalone mode here
                log.warn("Attempting to delete, but this replica does not exist on the CTF master -- already deleted?")
            }
            else { 
                def errorMsg = getMessage(
                         "ctfRemoteClientService.general.error", [e.getMessage()],
                         locale)
                log.error(errorMsg, e)
                errors << errorMsg
                throw new RemoteMasterException(ctfUrl, errorMsg, e)
            }
        } finally {
            if (sessionId) {
                logoff60(ctfUrl, ctfUsername, sessionId)
            }
        }
    }

    /**
     * @param ctfUrl is the ctf server complete URL, including protocol, 
     * hostname and port.
     * @param sessionId is the user sessionId retrieved after logging in. This
     * can be a user session ID or a SOAP session ID.
     * @return the list of users in the TeamForge server reached by the
     * given ctfUrl, using the given sessionId.
     * @throws CtfSessionExpiredException if the given sessionId is expired.
     * @throws RemoteMasterException if any other error occurs during the
     * method execution.
     */
    def getUserList(ctfUrl, sessionId, locale) throws RemoteMasterException {
        try {
            def filter = null
            return this.cnSoap(ctfUrl).getUserList(sessionId, filter).dataRows

        } catch (AxisFault e) {
            String faultMsg = e.faultString
            GrailsUtil.deepSanitize(e)
            if (faultMsg.contains("Session is invalid or timed out")) {
                throw new CtfSessionExpiredException(ctfUrl, sessionId,
                    getMessage("ctfRemoteClientService.remote.sessionExpired",
                        locale), e)
            } else {
                def errorMessage = getMessage(
                    "ctfRemoteClientService.listUsers.error", locale) + " " +
                    faultMsg
                log.error(errorMessage, e)
                throw new RemoteMasterException(ctfUrl, errorMessage, e)
            }
        }
    }

    /**
     * @param ctfUrl is the ctf server complete URL, including protocol, 
     * hostname and port.
     * @param sessionId is the user sessionId retrieved after logging in.
     * @return the list of projects in the TeamForge server reached by the
     * given ctfUrl, using the given sessionId.
     * @throws CtfSessionExpiredException if the given sessionId is expired.
     * @throws RemoteMasterException if any other error occurs during the
     * method execution.
     */
    def getProjectList(ctfUrl, sessionId, locale) throws RemoteMasterException {
        try {
            return this.cnSoap(ctfUrl).getProjectList(sessionId).dataRows

        } catch (AxisFault e) {
            String faultMsg = e.faultString
            GrailsUtil.deepSanitize(e)
            if (faultMsg.contains("Session is invalid or timed out")) {
                throw new CtfSessionExpiredException(ctfUrl, sessionId,
                    getMessage("ctfRemoteClientService.remote.sessionExpired", 
                        locale), e)
            } else {
                def errorMessage = getMessage(
                    "ctfRemoteClientService.listProjects.error", locale) + " " +
                    faultMsg
                log.error(errorMessage, e)
                throw new RemoteMasterException(ctfUrl, errorMessage, e)
            }
        }
    }

    /**
     * Creates a new User at the given TeamForge site identified by the given
     * URL. The session ID of the user with permissions to create user must
     * also be provided. The new user will be created with the given username,
     * password, email and real name. Other properties of the user is if he/she
     * will be granted Super User credentials, or it will be restricted. 
     * @param ctfUrl is the URL of the CTF server, with the protocol, hostname
     * and port number.
     * @param userSessionId is the sesion ID of an authenticated user with
     * permissions to create a new user.
     * @param username is the username of the new user.
     * @param password is the password for the new user.
     * @param email is the email address of the new user.
     * @param realName is the real name associated with the new user.
     * @param isSuperUser if the user must be granted the super user.
     * @param isRestrictedUser if the user is restricted.
     * @return a UserDO from the created user.
     * @throws CtfSessionExpiredException in case the given session Id is 
     * expired.
     * @throws RemoteMasterException in case the user exists, if illegal 
     * properties are provided, if the creation is denied for the given 
     * sessionId, or the site exceeded the limit to create new users or other
     * general errors.
     */
    public def createUser(ctfUrl, userSessionId, username, password, email, 
        realName, boolean isSuperUser, boolean isRestrictedUser, locale) 
            throws RemoteMasterException {

        try {

            def ctfSoap = this.cnSoap(ctfUrl)
            return ctfSoap.createUser(userSessionId, username, email, realName,
                'en', 'GMT', isSuperUser, isRestrictedUser, password)

        } catch (ObjectAlreadyExistsFault userExists) {
            GrailsUtil.deepSanitize(userExists)
            throw new RemoteMasterException(ctfUrl, getMessage(
                "ctfRemoteClientService.createUser.alreadyExists",
                    [ctfUrl, username], locale), userExists)

        } catch (IllegalArgumentFault invalidProperties) {
            GrailsUtil.deepSanitize(invalidProperties)
            throw new RemoteMasterException(ctfUrl, getMessage(
                "ctfRemoteClientService.createUser.invalidProps", [username],
                locale), invalidProperties)

        } catch (InvalidSessionFault sessionExpired) {
            GrailsUtil.deepSanitize(sessionExpired)
            throw new CtfSessionExpiredException(ctfUrl, userSessionId,
                getMessage("ctfRemoteClientService.createUser.invalidProps",
                    [username, userSessionId], locale), sessionExpired)

        } catch (PermissionDeniedFault permissionDenied) {
            GrailsUtil.deepSanitize(permissionDenied)
            throw new RemoteMasterException(ctfUrl, userSessionId, getMessage(
                "ctfRemoteClientService.createUser.sessionIdHasNoPermission",
                    [username, userSessionId], locale), permissionDenied)

        } catch (UserLimitExceededFault noMoreUsers) {
            GrailsUtil.deepSanitize(noMoreUsers)
            throw new RemoteMasterException(ctfUrl, userSessionId, getMessage(
                "ctfRemoteClientService.createUser.limitExceeded",
                    [username, userSessionId], locale), noMoreUsers)

        } catch (SystemFault generalProblem) {
            GrailsUtil.deepSanitize(generalProblem)
            def msg = getMessage(
                "ctfRemoteClientService.createUser.generalError", [username],
                locale)
            throw new RemoteMasterException(ctfUrl, userSessionId, msg + " " +
                "${generalProblem.message}", generalProblem)
        }
    }

    /**
     * Checks for a project in CTF which matches the projectName or
     * projectPath parameters.  If found, returns the name of the project
     * as it exists in CTF, otherwise returns null
     */
    String projectExists(ctfUrl, sessionId, projectName, projectPath, locale)
        throws RemoteMasterException {

        def projects = this.getProjectList(ctfUrl, sessionId, locale)
        String realName
        for (p in projects) {
            if (projectName.toLowerCase() == p.title.toLowerCase() || 
                projectPath == p.path.substring(9)) {
                realName = p.title
                break
            }
        }
        realName
    }

    /**
     * Creates a new instance of SoapNamedValues based on the array lists
     * @param names are the name of the properties
     * @param values are the values for each of the properties
     * @return a new instance of SoapNamedValues
     */
    def SoapNamedValues makeSoapNamedValues(names, values) {
        def soapNamedValues = new SoapNamedValues()
        if (!names) {
            names = new String[0]
        }
        if (!values) {
            values = new String[0]
        }
        if (names instanceof ArrayList) {
            names = (String[])names
        }
        if (values instanceof ArrayList) {
            values = (String[])values
        }
        soapNamedValues.setNames(names);
        soapNamedValues.setValues(values);
        return soapNamedValues
    }
    /**
     * Retrieves the queued commands from the CTF server identified by the
     * ctfUrl for the given replicaServerId.
     * @param ctfUrl is the url of the CTF system.
     * @param userSessionId is sessionID of the default user to call the soap.
     * @param replicaServerId is ID of the replica server.
     * @param runningCommands is the list of command results that haven't beent
     * transmitted yet. Those commands will avoid the replica manager send the
     * already executing commands and also to NOT count as a retransmission.
     * @param locale is the locale defined for error messages.
     * @return List of replica command execution with Id, command and repository
     * name, if any.
     * @throws RemoteMasterException if any error occurs during the method
     * call.
     * @throws NoRouteToHostException if the server started without connection,
     * acquired connection, and then lost it in the middle of execution.
     * @throws UnknownHostException if the server started with connectivity but
     * is unable to the host due to proxy, etc.
     */
    def getReplicaQueuedCommands(ctfUrl, userSessionId, replicaServerId, 
            runningCommands, locale) throws RemoteMasterException, 
            NoRouteToHostException, UnknownHostException {

        try {
            def runningCommandIds = []
            for (cmd in runningCommands) {
                runningCommandIds << cmd.commandId
            }
            def scmSoap = this.makeScmSoap60(ctfUrl)
            def queuedCommands = scmSoap.getReplicaQueuedCommands(userSessionId,
                replicaServerId, runningCommandIds as String[])

            def cmdsList = []
            if (queuedCommands && queuedCommands.mDataRows.length > 0) {
                queuedCommands.mDataRows.each { cmd ->
                    def queuedCmd = [:]
                    queuedCmd.id = cmd.id
                    queuedCmd.code = cmd.command
                    // building the params, if any
                    def paramNames = cmd.parameters.names
                    def paramValues = cmd.parameters.values
                    def cmdParams = [:]
                    if (paramNames.length == paramValues.length) {
                        def param = [:]
                        for (int i = 0; i < paramNames.length; i++) {
                            param[paramNames[i]] = paramValues[i]
                        }
                        cmdParams << param
                    }
                    queuedCmd.repoName = cmd.repositoryName
                    // TODO: the repository name should be defined in the params
                    // TODO: Remove the repositoryName property when removed
                    if (cmd.repositoryName) {
                        cmdParams['repoName'] = cmd.repositoryName
                    }
                    queuedCmd.params = cmdParams
                    cmdsList << queuedCmd
                }
            }
            def idComparator = [
                   compare: {a,b->
                       (a.id.replace(COMMAND_ID_PREFIX,"") as Integer) -
                           (b.id.replace(COMMAND_ID_PREFIX,"") as Integer)
                   }
                 ] as Comparator
            // sort the received commands by ID before returning.
            return cmdsList.sort(idComparator)

        } catch (LoginFault60 e) {
            def msg = getMessage("ctfRemoteClientService.auth.error", [ctfUrl],
                locale)
            log.error("Unable to create external system: " + msg, e)
            throw new CtfAuthenticationException(userSessionId, ctfUrl, msg, e)

        } catch (AxisFault e) {
             String faultMsg = e.faultString

           if (e.detail instanceof NoRouteToHostException) {
                // server started without connection, recovered, and lost again
                // Network is unreachable
                def hostname = new URL(ctfUrl).host
                throw new NoRouteToHostException(getMessage(
                    "ctfRemoteClientService.host.unreachable.error", 
                    [hostname.encodeAsHTML()], locale))

           } else if (e.detail instanceof UnknownHostException) {
                 def hostname = new URL(ctfUrl).host
                 throw new UnknownHostException(getMessage(
                     "ctfRemoteClientService.host.unknown.error",
                     [hostname.encodeAsHTML()], locale))

           } else if (faultMsg.contains("Session is invalid or timed out")) {
                throw new CtfSessionExpiredException(ctfUrl, userSessionId,
                    getMessage("ctfRemoteClientService.remote.sessionExpired",
                    locale), e)

            } else if (faultMsg.contains("No such object: ${replicaServerId}")) {
                // this fault indicates that the replica server no longer exists
                // on the ctf instance (deleted) -- will respond by creating 
                // and executing the unregister command
                log.error "This replica is no longer supported by the CTF " +
                    "master; reverting to standalone mode"
                // create a virtual command to unregister the replica
                def id = Math.round(Math.random() * 10000)
                return [[id: id, code: "replicaUnregister"]]

            } else {
                 def generalMsg = getMessage(
                     "ctfRemoteClientService.general.error", [e.getMessage()],
                     locale)
                log.error(generalMsg, e)
                throw new RemoteMasterException(ctfUrl, generalMsg, e)
            }

        } catch (Exception e) {
             GrailsUtil.deepSanitize(e)
             // also no session, but log this one as it indicates a problem
             if (!(e instanceof LoginFault)) {
                 def generalMsg = getMessage(
                     "ctfRemoteClientService.general.error", [e.getMessage()],
                     locale)
                 log.error(generalMsg, e)
                 throw new RemoteMasterException(ctfUrl, generalMsg, e)
             }
        }
    }

    /**
     * Uploads the result of a given command to the master.
     * @param ctfUrl is the url of the CTF system.
     * @param userSessionId is sessionID of the default user to call the soap
     * @param replicaServerId is ID of the replica server
     * @param commandId is the ID of the command execution
     * @param succeeded defines if the command succeeded or not.
     * @param locale is the locale defined for error messages.
     * @throws RemoteMasterException if any error occurs during the method
     * call.
     */
    def uploadCommandResult(ctfUrl, userSessionId, replicaServerId, commandId,
            succeeded, locale) throws RemoteMasterException {

        try {
            def scmSoap = this.makeScmSoap60(ctfUrl)
            scmSoap.uploadCommandResult(userSessionId, replicaServerId,
                commandId, succeeded)

        } catch (LoginFault60 e) {
            def msg = getMessage("ctfRemoteClientService.auth.error", [ctfUrl],
                locale)
            log.error("Unable to create external system: " + msg, e)
            throw new CtfAuthenticationException(userSessionId, ctfUrl, msg, e)

        } catch (AxisFault e) {
            String faultMsg = e.faultString
            if (faultMsg.contains("The parameter type/value") &&
                faultMsg.contains("is invalid for the adapter type")) {
                // No such object: The parameter type/value
                //'RepositoryBaseUrl=http://cu064.cloud.sp.collab.net:18080/svn'
                // is invalid for the adapter type 'Subversion'
                GrailsUtil.deepSanitize(e)
    
                if (faultMsg.contains("Session is invalid or timed out")) {
                   throw new CtfSessionExpiredException(ctfUrl, userSessionId,
                       getMessage("ctfRemoteClientService.remote.sessionExpired"
                           , locale), e)
                }
             }

         } catch (Exception e) {
            GrailsUtil.deepSanitize(e)
            // also no session, but log this one as it indicates a problem
            def generalMsg = getMessage(
                "ctfRemoteClientService.uploadCommandResult.error", [commandId,
                    succeeded, replicaServerId], locale) + e.getMessage()
            log.error(generalMsg)
            throw new RemoteMasterException(ctfUrl, generalMsg, e)
        }
    }
}