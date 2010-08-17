/*
 * CVSViewCGIServlet.java
 * $Revision: 1.35 $, $Date: 2007/06/21 21:11:43 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 *
 */
package com.collabnet.svnedge.teamforge

import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.apache.axis.AxisFault
import javax.net.ssl.SSLSession;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import javax.servlet.ServletConfig
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.ServletOutputStream
import javax.servlet.UnavailableException
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.xml.rpc.ServiceException
import javax.net.ssl.SSLSession
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder
import java.net.UnknownHostException
import java.rmi.RemoteException
import java.util.ArrayList
import java.util.Date
import java.util.Enumeration
import java.util.Hashtable
import java.util.Locale
import java.util.StringTokenizer
import java.util.Vector

/**
 * Code modified from Apache Tomcat's CGIServlet to invoke viewvc.cgi 
 * Original code is under the Apache Software License, Version 1.1
 */
class IntegrationController {
    
    def serverConfService
    
    def index = { redirect(action:viewvc, params:params) }

    private static final String SF_REFERER_COOKIE_NAME= "sf_main_host"
    private AuthCookie.AuthParams mAuthParams
    private String mProjectPath
    
    /**
     * Provides CGI Gateway service
     */
    def viewvc = {
        
        log.error("Found viewvc")
        String cgiPath = serverConfService.viewvcScriptDirPath
    	TeamForgeConfig teamForgeConfig = new TeamForgeConfig(CtfServer.getServer().baseUrl)
	    CGIEnvironment cgiEnv = new CGIEnvironment(request, cgiPath, 
	        "/viewvc.cgi/" + (params.cgiPathInfo ?: ''), "/integration/viewvc")
log.error("cgiPathInfo param=/viewvc.cgi/" + (params.cgiPathInfo ?: ''))
        if (cgiEnv.isValid()) {

            // If we have redirect pending, execute it
            String cookieRedirect = setupCookie(request, response)
            if (cookieRedirect) {
	            response.sendRedirect(getReturnToUrlForUrl(cookieRedirect, request))
            } else {

                String errorCode = verifySession(cgiEnv, request, response, response.getOutputStream(), 
                                                 teamForgeConfig)
                if (!errorCode) {
                    updateCGIEnvironment(cgiEnv, request, teamForgeConfig)

cgiEnv.getEnvironment().put("CSVN_HOME", serverConfService.appHome)
                  CGIRunner cgi = new CGIRunner(cgiEnv.getCommand(),
                        cgiEnv.getEnvironment(),
                        cgiEnv.getWorkingDirectory(),
                        cgiEnv.getParameters())
                    // if POST, we need to cgi.setInput
                    // REMIND: how does this interact with Servlet API 2.3's Filters?!
                    if ("POST".equalsIgnoreCase(request.getMethod())) {
                        cgi.setInput(request.getInputStream())
                    }
                    response.outputStream.flush()
                    cgi.setResponse(response)
                    cgi.run()
	            } else {
		            redirectToSfErrorPage(errorCode, request, response, 
                        response.getOutputStream(), teamForgeConfig)
	            }
    	    } 
        } else {
	        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid URL.")
	    }
        //response.outputStream << "done"
    }

    def test = {
        response.outputStream << "done"
    }
    /**
     * For the given url and request, return either the same url, if there is no need for host
     * substitution, or the url with the hostname replaced based on the request.
     */
    private String getReturnToUrlForUrl(String url, HttpServletRequest request) {
        String hostHeader = request.getHeader("host")
        String forwardHostHeader = request.getHeader("x-forwarded-host")
        String nUrl = url; //NetworkUtil.externalizeIntegrationsUrl(url)

        /* While NetworkUtil.externalizeIntegrationsUrl(String) should handle most cases, these two
         * steps below handle the edge case where the configured HOSTNAME is 'localhost'. */
        if (hostHeader != null && hostHeader.equals("localhost:7080") && forwardHostHeader != null) {
            /* We are only interested in replacing where the host is 'localhost:7080' */
            nUrl = nUrl.replace("localhost:7080", forwardHostHeader)
        }

        if (hostHeader != null && hostHeader.equals("localhost:8080") && forwardHostHeader != null) {
            /* We are only interested in replacing where the host is 'localhost:8080' */
            nUrl = nUrl.replace("localhost:8080", forwardHostHeader)
        }

        return nUrl
    }

    /**
     * @return redirect URL if cookie header is set based on query parameters
     */
    private String setupCookie(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String redirectURL = null
        String filteredQueryString = AuthCookie.filterQueryString(request)
        if (filteredQueryString != null) {

	    mAuthParams = AuthCookie.getAuthParams(request)
            if (mAuthParams != null) {
                AuthCookie.setAuthParams(mAuthParams, response)

                redirectURL = request.forwardURI // grails modifies this: getRequestURI()
                if (!"".equals(filteredQueryString)) {
                    redirectURL += "?" + filteredQueryString
                }
            }
	}
        log.debug("Cookie set, query string cleaned up: " + redirectURL)
        return redirectURL
    }

    /** The adapter name from subversion_adapter.xml */
    private static final String ADAPTER_NAME = "Subversion"

    private String verifySession(CGIEnvironment cgiEnv, HttpServletRequest request, 
        HttpServletResponse response, ServletOutputStream out, TeamForgeConfig teamForgeConfig) 
        throws IOException {
        
        mAuthParams = AuthCookie.getAuthParams(request)
        // If not to find repo auth params, redirect to error page
        if (mAuthParams == null || mAuthParams.getUserSessionId() == null) {
            return "NoAuth"
        }

        Hashtable params = cgiEnv.getParameters()
        String systemId = (String)params.get("system")
        String root = (String)params.get("root")

        SoapClientHelper soapHelper =
            new SoapClientHelper(teamForgeConfig.getBaseUrl() + "/sf-soap/services/ScmListener")

        def returnCode = null
        try {
            String[] infoArray = (String[]) soapHelper.invoke("getViewVCInformation",
                [mAuthParams.getUserSessionId(), systemId, root] as Object[])

            mProjectPath = infoArray[0]
            //mRepositoryRoot = infoArray[1]

            String adapterName = infoArray[2]
            String username = infoArray[3]

            if (!ADAPTER_NAME.equals(adapterName)) {
                return "InvRepo"
            }

            /* Now that we have a valid session, let's get the username from the session
               information to pass along to ViewVC */
            if (username != null && !username.trim().equals("")) {
                cgiEnv.getEnvironment().put("REMOTE_USER", username)
            }

        } catch (AxisFault e) {
            if ("Server.PermissionDenied".equals(e.getFaultCode().getLocalPart())) {
                returnCode = "PermDenied"
            } else if ("RBACPermissionDeniedException".equals(e.getFaultCode().getLocalPart())) {
                returnCode = "PermDenied"
            } else if ("InvalidSessionFault".equals(e.getFaultCode().getLocalPart())) {
                returnCode = "InvSession"
            } else if ("Client.NoSuchObject".equals(e.getFaultCode().getLocalPart())) {
                returnCode = "InvRepo"
            } else {
                returnCode = "Unk" + e.getFaultCode().getLocalPart() + "-InvalidSessionFault"
            }
            log.warn("Get exception when calling back to application server: ", e)
        } catch (ServiceException e) {
            returnCode = "ServiceException"
            log.warn("Get exception when calling back to application server: ", e)
        } catch (RemoteException e) {
            returnCode = "RemoteException"
            log.warn("Get exception when calling back to application server: ", e)
        }
        
        returnCode
    }

    /**
     * Redirect to the correct error page.  If we had no referrer, show an error.
     * @param request The current request
     * @param response The current response
     * @param out The ouput stream for responses
     * @throws IOException If the response could not be written out.
     */
    private void redirectToSfErrorPage(String authFailureCode, 
    	HttpServletRequest request, HttpServletResponse response,
        ServletOutputStream out, TeamForgeConfig teamForgeConfig) throws IOException {

        String rootUrl = teamForgeConfig.getWebAppUrl()
        StringBuilder returnTo = new StringBuilder(request.forwardURI) // request.getRequestURL()
        if (request.getQueryString() != null) {
            returnTo.append("?")
            returnTo.append(request.getQueryString())
        }
log.error(URLEncoder.encode(getReturnToUrlForUrl(returnTo.toString(), request)))
log.error(rootUrl)
log.error(authFailureCode)
        String redirect =  rootUrl + "/scm/do/viewRepositoryError?code=" + authFailureCode +
            "&url=" + URLEncoder.encode(getReturnToUrlForUrl(returnTo.toString(), request), "UTF-8")
log.info("Redirecting to error page: " + redirect)
        response.sendRedirect(response.encodeRedirectURL(redirect))
    }
    
    private void updateCGIEnvironment(CGIEnvironment cgiEnv, 
        HttpServletRequest req, TeamForgeConfig teamForgeConfig) 
        throws IOException {
        StringBuffer returnTo = req.getRequestURL();
        if (req.getQueryString() != null) {
            returnTo.append("?");
            returnTo.append(req.getQueryString());
        }
        
        String rootUrl = teamForgeConfig.getBaseUrl();
        URL ctfHeaderURL = new URL(teamForgeConfig.getWebAppUrl() +
                        "/sfmain/do/topInclude/" + mProjectPath +
                        ";jsessionid=" + mAuthParams.getJavaSessionId() +
                        "?base=" + rootUrl +
                        "&returnTo=" + URLEncoder.encode(getReturnToUrlForUrl(returnTo.toString(), req), "UTF-8") +
                        "&helpTopicId=26");
        String line;
        StringBuilder ctfHeader = new StringBuilder();
        
        /* Need to figure out why these are necessary. Groovy doesn't do anon inner classes and
           closure coercion doesn't seem to work either
        
        HostnameVerifier hv = [
            verify: { String urlHostName, SSLSession session ->
                log("Warning: URL Host: "+urlHostName+" vs. "+session.getPeerHost());
                return true;
            }
        ]        
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
        
        com.sun.net.ssl.HostnameVerifier hv2 = [
            verify: { String urlHostName, String certHostname ->
                log("Warning: URL Host: "+urlHostName+" vs. " + certHostname);
                return true;
            }
        ]
        com.sun.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(hv2);
        */
        
        URLConnection conn = ctfHeaderURL.openConnection();
        conn.setRequestProperty("Accept-Language", req.getLocale().toString());
        InputStreamReader isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
        try {
            BufferedReader br = new BufferedReader(isr);
            try {
                while ((line = br.readLine()) != null) {
                    ctfHeader.append(line).append("\r\n");
                }
            } finally {
                br.close();
            }
        } finally {
            isr.close();
        }
        cgiEnv.updateCGIEnvironment(req, ctfHeader.toString())
    }
}
