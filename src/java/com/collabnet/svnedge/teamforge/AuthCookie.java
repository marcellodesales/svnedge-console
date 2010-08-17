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
package com.collabnet.svnedge.teamforge;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Static class to handle cookie for third-party webapps (like SCM viewers) authentication.
 *
 * @author Paul Sokolovsky <psokolovsky@vasoftware.com>
 * @version $Revision$ $Date$
 */
public class AuthCookie {

    /** Name of the cookie */
    public static final String SF_AUTH_COOKIE_NAME = "sf_auth";

    /**
     * Wrapper for information stored in cookie
     */
    public static class AuthParams {
        private String mUserSessionId;
        private String mJavaSessionId;

        /**
         * Get guid of UserSessionKey for logged in user.
         * @return Guid of UserSessionKey
         */
        public String getUserSessionId() {
            return mUserSessionId;
        }
        /**
         * Set guid of UserSessionKey for logged in user.
         * @param id guid
         */
        public void setUserSessionId(String id) {
            mUserSessionId = id;
        }

        /**
         * Get value of 'jsessionid' cookie/URL param.
         * @return jsessionid
         */
        public String getJavaSessionId() {
            return mJavaSessionId;
        }
        /**
         * Set value of 'jsessionid' cookie/URL param.
         * @param id jsessionid
         */
        public void setJavaSessionId(String id) {
            mJavaSessionId = id;
        }
    }

    /**
     * Get decoded authentication parameters from cookie.
     * @param request HttpServletRequest
     * @return AuthParams
     */
    public static AuthParams getAuthParams(HttpServletRequest request) {
        String userSession = request.getParameter("us");
        String jSession = request.getParameter("js");
        if (userSession != null && jSession != null) {
            AuthParams authParams = new AuthParams();
            authParams.setUserSessionId(userSession);
            authParams.setJavaSessionId(jSession);
            return authParams;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (SF_AUTH_COOKIE_NAME.equals(cookies[i].getName())) {
                    String cookie = cookies[i].getValue();
                    String [] sa = cookie.split("&", 2);
                    AuthParams authParams = new AuthParams();
                    authParams.setUserSessionId(sa[0]);
                    authParams.setJavaSessionId(sa[1]);
                    return authParams;
                }
            }
        }


        return null;
    }

    /**
     * Store authentication parameters from the active session onto HTTP response.
     * @param guid User's session GUID
     * @param id http servlet Session Id
     * @param response HttpServletResponse
     */
    public static void setAuthParams(String guid, String id, HttpServletResponse response) {
	Cookie cookie = new Cookie(SF_AUTH_COOKIE_NAME, guid + "&" + id);
        cookie.setMaxAge(-1);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * Re-Store authentication parameters onto the current HTTP response.
     * @param authParams AuthParams object
     * @param response HttpServletResponse
     */
    public static void setAuthParams(AuthParams authParams, HttpServletResponse response) {
        setAuthParams(authParams.getUserSessionId(), authParams.getJavaSessionId(), response);
    }

    /**
     * Store authentication parameters from the active session onto HTTP response.
     * @param userSession User's session GUID
     * @param jSession http servlet Session Id
     * @return Query string to append to URL
     */
    public static String makeQueryParams(String userSession, String jSession) {
        return "us=" + userSession + "&js=" + jSession;
    }

    /**
     * Filter query string out of Auth params
     * @param request HttpServletRequest
     * @return Filtered string if there were Auth params, or null otherwise
     */
    public static String filterQueryString(HttpServletRequest request) {
        String userSession = request.getParameter("us");
        String jSession = request.getParameter("js");
        if (userSession != null && jSession != null) {
            StringBuffer result = new StringBuffer();
            Map params = new HashMap(request.getParameterMap());
            params.remove("us");
            params.remove("js");
            for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry)i.next();
                if (result.length() != 0) {
                    result.append("&");
                }
                result.append(entry.getKey() + "=");
                try {
                    result.append(URLEncoder.encode(((String[])entry.getValue())[0], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    ; // not possible
                }
            }
            return result.toString();
        }

        return null;
    }
}
