package com.collabnet.svnedge.teamforge;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
     * Encapsulates the CGI environment and rules to derive
     * that environment from the servlet container and request information.
     *
     * <p>
     * </p>
     *
     * @author   Martin Dengler [root@martindengler.com]
     * @version  $Revision$, $Date$
     * @since    Tomcat 4.0
     *
     */
public class CGIEnvironment {

    private static final String URL_PATH_SEPARATOR = "/";
    
    private int debug = 4;

        /** context of the enclosing servlet */
        //private ServletContext context = null;

        /** context path of enclosing servlet */
        private String contextPath = null;

        /** servlet URI of the enclosing servlet */
        private String servletPath = null;

        /** pathInfo for the current request */
        private String pathInfo = null;

        /** derived cgi environment */
        private Map<String, String> env = null;

        /** cgi command to be invoked */
        private String command = null;

        /** cgi command's desired working directory */
        private File workingDirectory = null;

        /** cgi command's query parameters */
        private Map<String, String> queryParameters = null;

        /** whether or not this object is valid or not */
        private boolean valid = false;

	/**
         * Creates a CGIEnvironment and derives the necessary environment,
         * query parameters, working directory, cgi command, etc.
         *
         * @param  req       HttpServletRequest for information provided by
         *                   the Servlet API
         * @param  context   ServletContext for information provided by the
         *                   Servlet API
         *
         */
    CGIEnvironment(HttpServletRequest req, String cgiPath, String pathInfo, String servletPath) {
            setupFromRequest(req);
log("CgiEnv ctor");

            queryParameters = new HashMap<String, String>();
            Enumeration paramNames = req.getParameterNames();
            while (paramNames != null && paramNames.hasMoreElements()) {
                String param = paramNames.nextElement().toString();
                if (param != null) {
                    try {
                        queryParameters.put(
                            param, URLEncoder.encode(req.getParameter(param), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        ; // Cannot happen
                    }
                }
            }
log("Setting cgi env");
            this.pathInfo = pathInfo;
            if (null != servletPath) {
                this.servletPath = servletPath;
            }
            this.valid = setCGIEnvironment(req, cgiPath);
log("Set cgi env=" + this.valid);

            if (this.valid) {
                workingDirectory = new File(command.substring(0,
                      command.lastIndexOf(File.separator)));
            }

    }

        /**
         * Uses the HttpServletRequest to set most CGI variables
         *
         * @param  req   HttpServletRequest for information provided by
         *               the Servlet API
         */
        protected void setupFromRequest(HttpServletRequest req) {
            this.contextPath = req.getContextPath();
            this.pathInfo = req.getPathInfo();
            this.servletPath = req.getServletPath();
        }

        /**
         * Resolves core information about the cgi script.
         *
         * <p>
         * Example URI:
         * <PRE> /servlet/cgigateway/dir1/realCGIscript/pathinfo1 </PRE>
         * <ul>
         * <LI><b>path</b> = $CATALINA_HOME/mywebapp/dir1/realCGIscript
         * <LI><b>scriptName</b> = /servlet/cgigateway/dir1/realCGIscript
         * <LI><b>cgiName</b> = /dir1/realCGIscript
         * <LI><b>name</b> = realCGIscript
         * </ul>
         * </p>
         * <p>
         * CGI search algorithm: search the real path below
         *    &lt;my-webapp-root&gt; and find the first non-directory in
         *    the getPathTranslated("/"), sureeading/searching from left-to-right.
         *</p>
         *<p>
         *   The CGI search path will start at
         *   webAppRootDir + File.separator + cgiPathPrefix
         *   (or webAppRootDir alone if cgiPathPrefix is
         *   null).
         *</p>
         *<p>
         *   cgiPathPrefix is defined by setting
         *   this servlet's cgiPathPrefix init parameter
         *
         *</p>
         *
         * @param pathInfo       String from HttpServletRequest.getPathInfo()
         * @param webAppRootDir  String from context.getRealPath("/")
         * @param contextPath    String as from
         *                       HttpServletRequest.getContextPath()
         * @param servletPath    String as from
         *                       HttpServletRequest.getServletPath()
         * @param cgiPathPrefix  subdirectory of webAppRootDir below which
         *                       the web app's CGIs may be stored; can be null.
         *                       The CGI search path will start at
         *                       webAppRootDir + File.separator + cgiPathPrefix
         *                       (or webAppRootDir alone if cgiPathPrefix is
         *                       null).  cgiPathPrefix is defined by setting
         *                       the servlet's cgiPathPrefix init parameter.
         *
         *
         * @return
         * <ul>
         * <li>
         * <code>path</code> -    full file-system path to valid cgi script,
         *                        or null if no cgi was found
         * <li>
         * <code>scriptName</code> -
         *                        CGI variable SCRIPT_NAME; the full URL path
         *                        to valid cgi script or null if no cgi was
         *                        found
         * <li>
         * <code>cgiName</code> - servlet pathInfo fragment corresponding to
         *                        the cgi script itself, or null if not found
         * <li>
         * <code>name</code> -    simple name (no directories) of the
         *                        cgi script, or null if no cgi was found
         * </ul>
         *
         * author Martin Dengler [root@martindengler.com]
         * @since Tomcat 4.0
         */
        protected String[] findCGI(String pathInfo,
                                   String contextPath, String servletPath,
                                   String cgiPath) {
            String path = null;
            String name = null;
            String scriptname = null;
            String cginame = null;

            if (debug >= 2) {
                log("findCGI: path=" + pathInfo + ", " + cgiPath);
            }

            File currentLocation = new File(cgiPath);
            StringTokenizer dirWalker =
            new StringTokenizer(pathInfo, URL_PATH_SEPARATOR);
            if (debug >= 3) {
                log("findCGI: currentLoc=" + currentLocation);
            }
            while (!currentLocation.isFile() && dirWalker.hasMoreElements()) {
                if (debug >= 3) {
                    log("findCGI: currentLoc=" + currentLocation);
                }
                currentLocation = new File(currentLocation,
                                           (String) dirWalker.nextElement());
            }
            if (!currentLocation.isFile()) {
                return new String[] { null, null, null, null };
            } else {
                if (debug >= 2) {
                    log("findCGI: FOUND cgi at " + currentLocation);
                }
                path = currentLocation.getAbsolutePath();
                name = currentLocation.getName();
                cginame =
                currentLocation.getParent().substring(cgiPath.length())
                + File.separator
                + name;

                if (".".equals(contextPath)) {
                    scriptname = servletPath + cginame;
                } else {
                    scriptname = contextPath + servletPath + cginame;
                }
            }

            if (debug >= 1) {
                log("findCGI calc: name=" + name + ", path=" + path
                    + ", scriptname=" + scriptname + ", cginame=" + cginame);
            }
            return new String[] { path, scriptname, cginame, name };

        }



        /**
         * Constructs the CGI environment to be supplied to the invoked CGI
         * script; relies heavliy on Servlet API methods and findCGI
         *
         * @param    req request associated with the CGI
         *           invokation
         *
         * @return   true if environment was set OK, false if there
         *           was a problem and no environment was set
         */
        protected boolean setCGIEnvironment(HttpServletRequest req, String cgiPath) {

            /*
             * This method is slightly ugly; c'est la vie.
             * "You cannot stop [ugliness], you can only hope to contain [it]"
             * (apologies to Marv Albert regarding MJ)
             */

            Map<String, String> envp = new HashMap<String, String>();

            String sPathInfoOrig = null;
            String sPathTranslatedOrig = null;
            String sPathInfoCGI = null;
            String sCGIFullPath = null;
            String sCGIScriptName = null;
            String sCGIFullName = null;
            String sCGIName = null;
            String[] sCGINames;


/*            if (null != req.getAttribute(Globals.SSI_FLAG_ATTR)) {
                // invoked by SSIServlet, which eats our req.getPathInfo() data
                sPathInfoOrig = (String) req.getAttribute(Globals.PATH_INFO_ATTR);
            } else */{
                sPathInfoOrig = this.pathInfo;
            }
            sPathInfoOrig = sPathInfoOrig == null ? "" : sPathInfoOrig;

            sCGINames = findCGI(sPathInfoOrig,
                                contextPath,
                                servletPath,
                                cgiPath);

            sCGIFullPath = sCGINames[0];
            sCGIScriptName = sCGINames[1];
            sCGIFullName = sCGINames[2];
            sCGIName = sCGINames[3];

            if (sCGIFullPath == null
                || sCGIScriptName == null
                || sCGIFullName == null
                || sCGIName == null) {
                return false;
            }

            envp.put("SERVER_SOFTWARE", "TOMCAT");

            envp.put("SERVER_NAME", nullsToBlanks(req.getServerName()));

            envp.put("GATEWAY_INTERFACE", "CGI/1.1");

            envp.put("SERVER_PROTOCOL", nullsToBlanks(req.getProtocol()));

            int port = req.getServerPort();
            Integer iPort = (port == 0 ? new Integer(-1) : new Integer(port));
            envp.put("SERVER_PORT", iPort.toString());

            envp.put("REQUEST_METHOD", nullsToBlanks(req.getMethod()));



            /*-
             * PATH_INFO should be determined by using sCGIFullName:
             * 1) Let sCGIFullName not end in a "/" (see method findCGI)
             * 2) Let sCGIFullName equal the pathInfo fragment which
             *    corresponds to the actual cgi script.
             * 3) Thus, PATH_INFO = request.getPathInfo().substring(
             *                      sCGIFullName.length())
             *
             * (see method findCGI, where the real work is done)
             *
             */
            if (pathInfo == null
                || (pathInfo.substring(sCGIFullName.length()).length() <= 0)) {
                sPathInfoCGI = "";
            } else {
                sPathInfoCGI = pathInfo.substring(sCGIFullName.length());
            }
            envp.put("PATH_INFO", sPathInfoCGI);
            
            // PATH_TRANSLATED is not used by viewvc, so not bothering to set it

            envp.put("SCRIPT_NAME", nullsToBlanks(sCGIScriptName));

            String queryString = nullsToBlanks(req.getQueryString());
            envp.put("QUERY_STRING", queryString);

            envp.put("REMOTE_HOST", nullsToBlanks(req.getRemoteHost()));

            envp.put("REMOTE_ADDR", nullsToBlanks(req.getRemoteAddr()));

            envp.put("AUTH_TYPE", nullsToBlanks(req.getAuthType()));

            envp.put("REMOTE_USER", nullsToBlanks(req.getRemoteUser()));

            envp.put("REMOTE_IDENT", ""); //not necessary for full compliance

            envp.put("CONTENT_TYPE", nullsToBlanks(req.getContentType()));


            /* Note CGI spec says CONTENT_LENGTH must be NULL ("") or undefined
             * if there is no content, so we cannot put 0 or -1 in as per the
             * Servlet API spec.
             */
            int contentLength = req.getContentLength();
            String sContentLength = (contentLength <= 0 ? "" :
                                     (new Integer(contentLength)).toString());
            envp.put("CONTENT_LENGTH", sContentLength);


            Enumeration headers = req.getHeaderNames();
            String header = null;
            while (headers.hasMoreElements()) {
                header = null;
                header = ((String) headers.nextElement()).toUpperCase();
                //REMIND: rewrite multiple headers as if received as single
                //REMIND: change character set
                //REMIND: I forgot what the previous REMIND means
                if ("AUTHORIZATION".equalsIgnoreCase(header) ||
                    "PROXY_AUTHORIZATION".equalsIgnoreCase(header)) {
                    //NOOP per CGI specification section 11.2
                } else if("HOST".equalsIgnoreCase(header)) {
                    String host = req.getHeader(header);
                    int idx =  host.indexOf(":");
                    if (idx < 0) idx = host.length();
                    envp.put("HTTP_" + header.replace('-', '_'),
                             host.substring(0, idx));
                } else {
                    envp.put("HTTP_" + header.replace('-', '_'),
                             req.getHeader(header));
                }
            }

            command = sCGIFullPath;
            envp.put("X_TOMCAT_SCRIPT_PATH", command);  //for kicks

            this.env = envp;

            return true;
        }

    /**
     * Update QUERY_STRING with decoded auth params
     * @param req HttpServletRequest
     */
    public void updateCGIEnvironment(HttpServletRequest req, String ctfHeader) throws IOException {

	    this.env.put("SF_HEADER", URLEncoder.encode(ctfHeader, "UTF-8"));

	    String queryString = nullsToBlanks(req.getQueryString());
        this.env.put("QUERY_STRING", queryString);
        log("Constructed QUERY_STRING: " + queryString);

	    // Explicitly set a path because on solaris, if we don't explicitly set one, we don't get it.
        this.env.put("PATH", "/usr/local/bin:/bin:/usr/bin");
    }

        /**
         * Gets derived command string
         *
         * @return  command string
         *
         */
        protected String getCommand() {
            return command;
        }



        /**
         * Gets derived CGI working directory
         *
         * @return  working directory
         *
         */
        protected File getWorkingDirectory() {
            return workingDirectory;
        }



        /**
         * Gets derived CGI environment
         *
         * @return   CGI environment
         *
         */
        protected Map<String, String> getEnvironment() {
            return env;
        }

        /**
         * Gets derived CGI query parameters
         *
         * @return   CGI query parameters
         *
         */
        protected Map<String, String> getParameters() {
            return queryParameters;
        }

        /**
         * Gets validity status
         *
         * @return   true if this environment is valid, false
         *           otherwise
         *
         */
        protected boolean isValid() {
            return valid;
        }

        /**
         * Converts null strings to blank strings ("")
         *
         * @param    s string to be converted if necessary
         * @return   a non-null string, either the original or the empty string
         *           ("") if the original was <code>null</code>
         */
        protected String nullsToBlanks(String s) {
            return nullsToString(s, "");
        }

        /**
         * Converts null strings to another string
         *
         * @param    couldBeNull string to be converted if necessary
         * @param    subForNulls string to return instead of a null string
         * @return   a non-null string, either the original or the substitute
         *           string if the original was <code>null</code>
         */
        protected String nullsToString(String couldBeNull,
                                       String subForNulls) {
            return (couldBeNull == null ? subForNulls : couldBeNull);
        }

        /**
         * Converts blank strings to another string
         *
         * @param    couldBeBlank string to be converted if necessary
         * @param    subForBlanks string to return instead of a blank string
         * @return   a non-null string, either the original or the substitute
         *           string if the original was <code>null</code> or empty ("")
         */
        protected String blanksToString(String couldBeBlank,
                                      String subForBlanks) {
            return (("".equals(couldBeBlank) || couldBeBlank == null)
                    ? subForBlanks
                    : couldBeBlank);
        }
        
        private void log(String s) {
            System.out.println(s);
        }
}
