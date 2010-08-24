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
package com.collabnet.svnedge.console.services

import org.apache.commons.io.FileUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import com.collabnet.svnedge.console.ConfigUtil
import com.collabnet.svnedge.console.Repository
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode
import com.collabnet.svnedge.console.security.User
import com.collabnet.svnedge.replica.manager.Master
import com.collabnet.svnedge.teamforge.CtfServer

import grails.util.GrailsUtil

import java.util.Calendar
import com.collabnet.svnedge.console.services.LogManagementService.ApacheLogLevel

class ServerConfService {

    boolean transactional = true
    def commandLineService
    def securityService
    def networkingService
    
    private static final String DONT_EDIT = """#
#
# DO NOT EDIT THIS FILE IT WILL BE REGENERATED AUTOMATICALLY BY COLLABNET SUBVERSION
#
#
"""
    private static final String DEFAULT_SVN_ACCESS = """#
#
# DEFAULT ACCESS PERMISSIONS GRANTING ALL USERS FULL CONTROL OF ALL REPOS
#
[/]
* = rw
"""

    private static final String CTF_AUTHORIZER = """
[authz-teamforge]
"""
    
    def appHome
    private def svnPath
    def httpdUser
    def httpdGroup
    def confDirPath
    def distDirPath
    def modPythonDirPath
    def viewvcLibDirPath
    private def viewvcTemplateDirPath
    private def dataDirPath
    private def winServiceName

    def bootstrap = { config, server ->
        appHome = new File(ConfigUtil.appHome(config)).absolutePath
        confDirPath = new File(ConfigUtil.confDirPath(config)).absolutePath

        log.debug("CSVN_HOME will be " + appHome)
        log.debug("Bootstrapping serverConfService")

        log.debug("Setting up data artifacts directory...")
        bootstrapDataDirectory(ConfigUtil.appHome(config))
        bootstrapConfiguration()

        svnPath = new File(ConfigUtil.svnPath(config)).absolutePath
        viewvcLibDirPath = new File(
            ConfigUtil.viewvcLibPath(config)).absolutePath
        modPythonDirPath = new File(
            ConfigUtil.modPythonPath(config)).absolutePath
        distDirPath = new File(ConfigUtil.distDir(config)).absolutePath
        viewvcTemplateDirPath = new File(
            ConfigUtil.viewvcTemplateDir(config)).absolutePath

        // serviceName should only be set for windows
        def serviceName = ConfigUtil.serviceName(config)
        if (null != serviceName) {
            winServiceName = serviceName
        } else {
            // user and group aren't relevant on windows (yet)
            setUserAndGroup()
        }
        dataDirPath = new File(ConfigUtil.dataDirPath(config)).absolutePath
        conditionalWriteHttpdConf()
        deployPythonBindings()

        try {
            this.writeConfigFiles()
        } catch (Exception e) {
            log.error("Unable to write initial set of configuration " +
                "files", e)
        }
    }
    
    private void bootstrapConfiguration() {
        // remove unnecessary scm artifact
        switch(GrailsUtil.environment) {
            case "development":
            case "test":
                log.debug("Deleting older httpd.conf file")
                new File(confDirPath, "httpd.conf").delete()

                File scmProps = new File(server.repoParentDir,".scm.properties")
                if (scmProps.exists() && scmProps.delete()) {
                    log.debug("Removed previous .scm.properties file")
                }
                break
        }
    }

    /**
     * Bootstraps the temp-data director to data moving the data directory as 
     * discussed on artf62798
     * @param config is the configuration properties
     */
    private def bootstrapDataDirectory(appHome) {
        def tempDataDir = new File(appHome, "temp-data")
        if (tempDataDir.exists()) {
            log.info("Bootstrapping the temporary data directory...")
            def dataDir = new File(appHome, "data")
            //The data exists, it's a possible update. Copy each
            //existing ones, maintaining the version .new-1, .new-2, ...
            boolean recur = true
            def allTmpFiles = FileUtils.listFiles(tempDataDir, null, recur)
            for (fileOnTemp in allTmpFiles) {
                //only returns file names, no dirs and subdirs
                def fileOnDataPath = fileOnTemp.canonicalPath.replace(
                        "temp-data", "data")
                def fileOnDataDir = new File(fileOnDataPath)
                if (!fileOnDataDir.exists()) {
                    def parentDir = fileOnDataDir.getParentFile()
                    //create all dirs and subdirs for the file.
                    parentDir.mkdirs()
                    fileOnTemp.renameTo(fileOnDataDir)
                } else {
                    //skip if it's a directory, but rename the file.
                    if (!fileOnTemp.isDirectory()) {
                        int numberOfNewFiles = 0
                        //gets the name of the next "new" artifact n
                        while((new File(fileOnTemp.canonicalPath + ".new-"  + 
                                (++numberOfNewFiles))).exists());
                        fileOnTemp.renameTo(new File(fileOnTemp.canonicalPath + 
                                ".new-" + numberOfNewFiles))
                    }
                }
            }
            //delete the temp-data's empty dirs and subdirs
            FileUtils.deleteDirectory(tempDataDir)
        }
    }

    private boolean isWindows() {
        return null != winServiceName
    }
    
    /**
     * Will run httpd as owner of the repository directory as httpd will 
     * write to it.
     * Might want to eventually add configuration for this.
     */
    private def setUserAndGroup() {
        def server = getServer()
        def repoPath = server ? (String) server.repoParentDir :
            (String) ConfigurationHolder.config.svnedge.svn.repositoriesParentPath
        //Sometimes ls -ld output coloumns are separated by double space.
        //For ex drwxr-xr-x  7 rajeswari __cubitu 4096 May 14 01:45 data/
        String[] result = commandLineService
            .executeWithOutput("ls", "-dl", repoPath).replaceAll(" +", " ").split(" ")

        httpdUser = result.length > 2 ? result[2] : "nobody"
        httpdGroup = result.length > 3 ? result[3] : "nobody"
    }
    
    private def deployPythonBindings() {
        if (isWindows()) {
            return
        }
        String libDir = new File(appHome, "lib").absolutePath
        File pythonBindingDir = new File(libDir, "svn-python")
        String[] result1 = commandLineService
            .executeWithOutput("rm", "-rf", pythonBindingDir.absolutePath)
        File actualPythonBindingDir = new File(libDir,
            "svn-python${getPythonVersion()}")
        String[] result2 = commandLineService
            .executeWithOutput("ln", "-s", actualPythonBindingDir.absolutePath,
                               pythonBindingDir.absolutePath)
        File swigPythonLibPath = new File(libDir, "libsvn_swig_py-1.so.0.0.0")
        File actualSwigPythonLibPath = new File(libDir, 
            "libsvn_swig_py-1.so.0.0.0${getPythonVersion()}")
        String[] result3 = commandLineService
            .executeWithOutput("ln", "-sf",actualSwigPythonLibPath.absolutePath,
                               swigPythonLibPath.absolutePath)
        File swigPythonLibPath1 = new File(libDir, "libsvn_swig_py-1.so.0")
        String[] result4 = commandLineService
            .executeWithOutput("ln", "-sf", swigPythonLibPath.absolutePath,
                               swigPythonLibPath1.absolutePath)
    }
    
    private def conditionalWriteHttpdConf() {
        def confDir = new File(confDirPath)
        File dest = new File(confDir, "httpd.conf")
        if (!dest.exists()) {
            writeHttpdConf(dest)
        }
    }
    
    private def writeHttpdConf(destConfFile) {
        def distFile = new File(distDirPath, "httpd.conf.dist")
        if (!distFile.exists()) {
            distFile = new File(confDirPath, "httpd.conf.dist")
        }
        String s = distFile.getText()
        s = s.replace("__CSVN_HOME__", appHome)
        s = s.replace("__CSVN_CONF__", confDirPath)
        destConfFile.write(s)
    }
    
    def writeConfigFiles() {
        Server server = getServer()
        conditionalWriteHttpdConf()
        writeMainConf(server)
        writeLogConf()
        writeSvnViewvcConf(server)
        File teamforgePropsTemplate = 
            new File(confDirPath, "teamforge.properties.dist")
        if (teamforgePropsTemplate.exists()) {
            writeTeamforgeConf(server, teamforgePropsTemplate)
        }

        String s = new File(confDirPath, "viewvc.conf.dist").getText()
        s = s.replace("__CSVN_REPO_ROOT__", server.repoParentDir)
        s = s.replace("__CSVN_CONF__", confDirPath)
        s = s.replace("__CSVN_VIEWVC_TEMPLATES__", viewvcTemplateDirPath)
        s = s.replace("__CSVN_SVN_CLIENT__", svnPath)
        s = s.replace("__SERVER_ADMIN__", server.adminEmail)
        
        String serverMode = server.mode.toString()
        String appServerUrl, docroot, authorizer, isRootInUrl

        // CTF default ViewVC "allowed_views"
        String allowedViews = "annotate, co, diff, markup"
        if (ServerMode.MANAGED.equals(server.mode)) {
            appServerUrl = CtfServer.getServer().baseUrl
            docroot = server.urlPrefix() + "/viewvc-static"
            authorizer = "teamforge"
            isRootInUrl = "0"
            s += CTF_AUTHORIZER
        } else {
            appServerUrl = ''
            docroot = "/viewvc-static"
            authorizer = "svnauthz"
            isRootInUrl = "1"
            // in standalone mode, add "roots" view in ViewVC
            allowedViews += ", roots"
        }

        s = s.replace("__CSVN_SERVERMODE__", serverMode)
        s = s.replace("__CSVN_APP_SERVER_ROOT_URL__", appServerUrl)
        s = s.replace("__CSVN_VIEWVC_DOCROOT__", docroot)
        s = s.replace("__CSVN_VIEWVC_AUTHORIZER__", authorizer)
        s = s.replace("__CSVN_ROOT_IN_URL__", isRootInUrl)
        s = s.replace("__CSVN_ALLOWED_VIEWS__", allowedViews)

        if (isWindows()) {
            s = s.replace("__DIFF__", "diff.exe")
        } else {
            s = s.replace("__DIFF__", "/usr/bin/diff")
        }            
        new File(confDirPath, "viewvc.conf").write(DONT_EDIT + s)
    }




    /**
     * service method to fetch the text of the svn_acccess_file (path-based permissions)
     */
    String readSvnAccessFile() {

        File f = new File(confDirPath, "svn_access_file")
        if (f.exists()) {
            return f.text
        }
        else {
            return DEFAULT_SVN_ACCESS
        }

    }

    /**
     * service method to write the input text to the svn_acccess_file (path-based permissions)
     */
    boolean writeSvnAccessFile(String content) {

        File f = new File(confDirPath, "svn_access_file")
        if (f.canWrite()) {
            f.write content.trim()
            return true
        }
        else {
            return false
        }
    }

    private def writeMainConf(server) {
        def hostname, port
        if (server) {
            hostname = server.hostname
            port = String.valueOf(server.port)
        } else {
            def config = ConfigurationHolder.config
            hostname = networkingService.hostname
            port = String.valueOf(config.svnedge.defaultHighPort)
        }        
        def serverName = "${hostname}:${port}"
        def serverPort = "${port}"
        
        def ldapConfSnippet = getLdapHttpdConf(server)
        def fileAuthConfSnippet = (server.mode == ServerMode.MANAGED) ? 
            "" : getFileAuthHttpdConf(server)
        def sslSnippet = getSSLHttpdConf(server)
        def conf = """${DONT_EDIT}
ServerAdmin "${server.adminEmail}"
ServerName "${serverName}"
Listen ${serverPort}
${getDefaultHttpdSnippets()}
${ldapConfSnippet}
${fileAuthConfSnippet}
${sslSnippet}
"""
        new File(confDirPath, "csvn_main_httpd.conf").write(conf)
    }

    def getLogFileSuffix() {
        Calendar cal = Calendar.getInstance()
        String suffix = String.valueOf(cal.get(Calendar.YEAR))
        suffix = suffix + "_"

        int month = cal.get(Calendar.MONTH)
        /* Month index is starting from '0' so compare against 9.
         * For october it is '9' below if condition would fail but
         * month+1 i.e '10' would set the right format value.
         */
        if (month < 9) {
            /*We need to prepend the 0*/
            suffix = suffix + "0"
        }
        suffix = suffix + String.valueOf(month + 1)
        suffix = suffix + "_"

        int day = cal.get(Calendar.DAY_OF_MONTH)
        if (day < 10) {
            /*We need to prepend the 0*/
            suffix = suffix + "0"
        }

        suffix = suffix + String.valueOf(day)
        return suffix
    }

    def writeLogConf() {
        def logNameSuffix = getLogFileSuffix()
        def logLevel = getServer().apacheLogLevel ?: ApacheLogLevel.WARN 
        def conf = """${DONT_EDIT}
LogLevel ${logLevel.toString().toLowerCase()}
LogFormat "%h %l %u %t \\\"%r\\\" %>s %b" common
ErrorLog "${dataDirPath}/logs/error_${logNameSuffix}.log"
CustomLog "${dataDirPath}/logs/access_${logNameSuffix}.log" common
CustomLog "${dataDirPath}/logs/subversion_${logNameSuffix}.log" "%t %u %{SVN-REPOS-NAME}e %{SVN-ACTION}e %T" env=SVN-ACTION
"""
        new File(confDirPath, "csvn_logging.conf").write(conf)
    }

    private def writeSvnViewvcConf(server) {
        boolean ctfMode = server.mode == ServerMode.MANAGED ||
            server.mode == ServerMode.CONVERTING_TO_MANAGED
        def conf = """${DONT_EDIT}
LoadModule python_module lib/modules/mod_python.so${getPythonVersion()}
# Work around authz and SVNListParentPath issue
RedirectMatch ^(/svn)\$ \$1/
<Location /svn/>
   DAV svn
   SVNParentPath "${server.repoParentDir}"
   SVNReposName "CollabNet Subversion Repository"
"""
        conf += ctfMode ? getCtfSvnHttpdConf(server) : 
            getSVNHttpdConf(server)
        conf += "</Location>\n\n"

        conf += """
<Directory "${viewvcTemplateDirPath}/docroot">
  AllowOverride None
  Options None
  Order allow,deny
  Allow from all
</Directory>
Alias /viewvc-static "${viewvcTemplateDirPath}/docroot"
"""

        conf += ctfMode ? """
# Required for SCRIPT_URI/URL in viewvc libs, not just rewrite rules
LoadModule rewrite_module lib/modules/mod_rewrite.so
RewriteEngine on
RewriteRule ^/viewcvs(.*)\$ /viewvc\$1 [R,L]
""" : """
ScriptAlias /viewvc "${modPythonDirPath}/viewvc.py"
"""
        conf += """
<Location /viewvc>
  SetHandler mod_python
  PythonDebug on
  AddDefaultCharset UTF-8
  SetEnv CSVN_HOME "${appHome}"
"""
        conf += ctfMode ? getCtfViewVCHttpdConf(server) : 
            getViewVCHttpdConf(server)
        conf += "</Location>"

        new File(confDirPath, "svn_viewvc_httpd.conf").write(conf)
    }
    
    private def getDefaultHttpdSnippets() {
        def conf = ""
        if (isWindows() == false) {
            conf = """User ${httpdUser}
Group ${httpdGroup}
"""
        } else {
        	// Windows-specific memory optimizations
        	conf = """ThreadsPerChild 64
MaxRequestsPerChild 0
MaxMemFree 512
"""
        }
        conf += """PidFile "${dataDirPath}/run/httpd.pid"\n"""
    }    
        
    private def getLdapURL(server) {
        def ldapUrl
        if (server.ldapSecurityLevel != "NONE") {
            ldapUrl = "ldaps://"
        } else {
            ldapUrl = "ldap://"
        }
        ldapUrl += "${server.ldapServerHost}"
        if (server.ldapServerPort != 389) {
            ldapUrl += ":${server.ldapServerPort}"
        } 
        if (server.ldapAuthBasedn) {
            ldapUrl += "/${server.ldapAuthBasedn}"
        }
        if (server.ldapLoginAttribute) {
            ldapUrl += "?${server.ldapLoginAttribute}"
        } else {
            ldapUrl += "?uid"
        }
        if (server.ldapSearchScope) {
            ldapUrl += "?${server.ldapSearchScope}"
        } else {
            ldapUrl += "?sub"
        }
        if (server.ldapFilter) {
            ldapUrl += "?${server.ldapFilter}"
        }
        ldapUrl
    }

    private def getLdapHttpdConf(server) {
        def conf = ""
        if (server.ldapEnabled) {
            def ldapURL = getLdapURL(server)
            conf = """LoadModule ldap_module lib/modules/mod_ldap.so
LoadModule authnz_ldap_module lib/modules/mod_authnz_ldap.so
<AuthnProviderAlias ldap ldap-users>
  AuthLDAPUrl "${ldapURL}" "${server.ldapSecurityLevel}"
"""

            if (server.ldapAuthBinddn && server.ldapAuthBindPassword) {
                conf = """${conf}
  AuthLDAPBindDN "${server.ldapAuthBinddn}"
  AuthLDAPBindPassword "${server.ldapAuthBindPassword}"
"""
            }
            conf = """${conf}
</AuthnProviderAlias>
"""
            if (!server.ldapServerCertVerificationNeeded) {
            conf = """${conf}
LDAPVerifyServerCert Off
"""
            }
        }
        conf
    }

    private def getFileAuthHttpdConf(server) {
        def conf = ""
        if (server.fileLoginEnabled) {
            conf = """
<AuthnProviderAlias file csvn-file-users>
  AuthUserFile "${confDirPath}/svn_auth_file"
</AuthnProviderAlias>
"""
        }
        conf
    }

    private def getPythonVersion() {
        String version;
        if (isWindows()) {
            version = ""
        } else {
            version = commandLineService
                .executeWithOutput("python", "-c", "import platform; " + 
                "print str(platform.python_version_tuple()[0]) + '.' + " +
                "str(platform.python_version_tuple()[1])")
            if (version.length()) {
                version = "." + version
                /*Remove the trailing new line */
                version = version.trim()
            }
        }
        version
    }

    private def getCtfSvnHttpdConf(server) {
        def ctfServer = CtfServer.getServer()
        def appServerPort = System.getProperty("jetty.port", "8080")
        def conf = """AuthType Basic
   AuthName "Authorization Realm"
   AuthBasicAuthoritative Off
   Require valid-user
   AddHandler mod_python .py
   PythonAuthenHandler sfauth
   PythonDebug On
   PythonPath "['"""
        conf += escapePath(new File(appHome, "lib").absolutePath) + "', '"
        conf += escapePath(new File(appHome, "lib/integration").absolutePath) + "', '"
        conf += escapePath(new File(appHome, "lib/svn-python").absolutePath)
        conf += """']+sys.path"
   # Provide variables to sfauth.py
   PythonOption svn.root.path "${escapePath(server.repoParentDir)}"
   PythonOption svn.root.uri /svn
   PythonOption sourceforge.properties.path "${escapePath(new File(confDirPath, "teamforge.properties").absolutePath)}"
   # Allows these operations to be disallowed higher up,
   # then enabled here for this Location
   <Limit COPY DELETE GET OPTIONS PROPFIND PROPPATCH PUT MKACTIVITY CHECKOUT MERGE REPORT>
      Order allow,deny
      Allow from all
   </Limit>
"""
        conf
    }

    private def getSSLHttpdConf(server) {
        def conf = ""
        def extraconf = ""
        if (server.useSsl) {
            File f = new File(confDirPath, "server.ca")
            def chainFilePath = f.absolutePath
            if (f.exists()) {
                extraconf = "SSLCertificateChainFile \"${chainFilePath}\""
            }

            conf = """
LoadModule ssl_module lib/modules/mod_ssl.so
SSLRandomSeed startup builtin
SSLRandomSeed connect builtin
SSLEngine on
SSLCertificateFile    "${confDirPath}/server.crt"
SSLCertificateKeyFile "${confDirPath}/server.key"
${extraconf}
"""
        }
        conf
    }

    private def getSVNHttpdConf(server) {
        def conf = """  AuthzSVNAccessFile "${confDirPath}/svn_access_file"
  SVNListParentPath On
"""
        conf += getAuthBasic(server)
        if (server.allowAnonymousReadAccess) {
            conf += """
#Authentication needed for *write* requests to all repositories irrespective of
#Anonymous access setting.
  <LimitExcept OPTIONS GET PROPFIND REPORT>
    Require valid-user
  </LimitExcept>
"""
        } else {
            conf += """  Require valid-user
"""
}
        if (server.replica) {
            def master = Master.getDefaultMaster()
            def masterSVNURI = (master.sslEnabled ? "https" : "http") +
                 "://" + master.hostName + "/svn/repos"
        conf += """
  SVNMasterURI ${masterSVNURI}
"""
        }
        conf
    }
    
    private def getAuthBasic(server) {
        def conf = "  Allow from all\n"
        def authProviders = ""
        if (server.fileLoginEnabled) {
            authProviders = "csvn-file-users"
        }
        if (server.ldapEnabled) {
            authProviders += " ldap-users"
        }
        conf += """  AuthType Basic
  AuthName "CollabNet Subversion Repository"
  AuthBasicProvider ${authProviders}
"""
        conf
    }    
    
    private def getViewVCHttpdConf(server) {
        def conf = """
  PythonPath "[r'"""
        conf += escapePath(new File(appHome, "lib").absolutePath) + "', r'" +
            escapePath(modPythonDirPath) + "', r'" + 
            escapePath(viewvcLibDirPath)
        conf += """']+sys.path"
  PythonHandler handler
"""

        conf += getAuthBasic(server)
        if (!server.allowAnonymousReadAccess) {
            conf += """  Require valid-user
"""
        }
        return conf
    }
    
    private def getCtfViewVCHttpdConf(server) {
        def conf = """
  PythonPath "[r'"""
        conf += escapePath(new File(appHome, "lib").absolutePath) + "', r'"
        conf += escapePath(new File(appHome, "lib/svn-python").absolutePath) + "', r'"
        conf += escapePath(new File(appHome, "lib/integration").absolutePath) + 
            "', r'" + modPythonDirPath + "', r'" + viewvcLibDirPath
        conf += """']+sys.path"
  PythonHandler viewvc_ctf_handler
  PythonOption viewvc.root.uri /viewvc
  PythonOption sourceforge.properties.path "${escapePath(new File(confDirPath, "teamforge.properties").absolutePath)}"
  PythonOption sourceforge.home "${escapePath(new File(dataDirPath, "teamforge").absolutePath)}"
"""
        return conf
    }


    def createOrValidateHttpdConf() {
        File httpdConf = new File(confDirPath, "httpd.conf")
        boolean isValid = false
        if (httpdConf.exists()) {
            String conf = httpdConf.getText()
            String include = "Include \"${confDirPath}/csvn_main_httpd.conf\""
            httpdConf.eachLine { isValid |= include.equals(it) }
            if (isValid) {
                isValid = false
                include = "Include \"${confDirPath}/svn_viewvc_httpd.conf\""
                httpdConf.eachLine { isValid |= include.equals(it) }
            }
        } else {
            log.info("httpd.conf was missing; recreating it from template.")
            writeConfigFiles()
            isValid = true
        }
        isValid
    }

    private def writeTeamforgeConf(server, teamforgePropsTemplate) {
        CtfServer ctfServer = CtfServer.getServer()
        String s = getTeamforgeConf(
            teamforgePropsTemplate.text, ctfServer, server)
        if (ctfServer && server.mode == ServerMode.MANAGED) {
            s += "\nsfmain.integration.subversion.csvn=csvn-managed\n"
        } else {
            s += "\nsfmain.integration.subversion.csvn=csvn-standalone\n"
        }
        new File(confDirPath, "teamforge.properties").write(s)
        System.setProperty("csvnedge.resetTeamforgeProperties", "true")
    }

    private def getTeamforgeConf(contents, ctfServer, server) {
        if (isWindows()) {
            contents = contents.replace("\${os.name}", "windows")
        } else {
            contents = contents.replace("\${user.name}", httpdUser)
            contents = contents.replace("\${user.group}", httpdGroup)
            contents = contents.replace("\${os.name}", "linux")
        }
        contents = contents.replace("\${svn.binary}", escapePath(svnPath))
        contents = contents.replace("\${svnadmin.binary}", 
            escapePath(new File(appHome, "bin/svnadmin").absolutePath))
        contents = contents.replace("\${svnlook.binary}", 
            escapePath(new File(appHome, "bin/svnlook").absolutePath))
        contents = contents.replace("\${repo.parent.dir}", 
            escapePath(server.repoParentDir))
        contents = contents.replace("\${repository.archive.dir}", 
            escapePath(new File(dataDirPath, "deleted-repos").absolutePath))
        File tfHome = new File(dataDirPath, "teamforge")
        if (!tfHome.exists()) {
            tfHome.mkdir()
        }
        contents = contents.replace("\${app.data}",
                                    escapePath(tfHome.absolutePath))
        File tfTmp = new File(tfHome, "tmp")
        if (!tfTmp.exists()) {
            tfTmp.mkdir()
        }
        contents = contents.replace("\${temp.dir}", escapePath(tfTmp.absolutePath))
        contents = contents.replace("\${integration.dir}",
            escapePath(new File(appHome, "lib/integration").absolutePath))

        def libDir = new File(appHome, "lib").absolutePath
        def pythonPath = "${libDir}${File.pathSeparator}${libDir}${File.separator}svn-python"
        contents = contents.replace("\${python.path}", escapePath(pythonPath))


        if (ctfServer && (server.mode == ServerMode.MANAGED ||
            server.mode == ServerMode.CONVERTING_TO_MANAGED)) {
            contents = contents.replace("\${ctf.webserver.url}", 
                                        ctfServer.getWebAppUrl())
            String baseUrl = ctfServer.baseUrl
            boolean useSsl = baseUrl.startsWith("https")
            contents = contents.replace("\${ctf.useSsl}", 
                                        String.valueOf(useSsl))
            int start = baseUrl.indexOf("://") + 3
            int end = baseUrl.endsWith("/") ? baseUrl.length() - 1 :
                baseUrl.length()
            String hostAndPort = baseUrl.substring(start, end)
            int colon = hostAndPort.indexOf(":")
            String host = (colon > 0) ? hostAndPort.substring(0, colon) :
                hostAndPort
            String port = (colon > 0) ? hostAndPort.substring(colon + 1) :
                (useSsl ? "443" : "80") 
            contents = contents.replace("\${ctf.hostname}", host)
            contents = contents.replace("\${ctf.port}", port)
            contents = contents.replace("\${ctf.soap.url}", baseUrl)
            if (ctfServer.internalApiKey) {
                contents += "\nsfmain.integration.security.shared_secret=" +
                    ctfServer.internalApiKey
            }
        } else {
            // Use a random api key, until/unless the server is being
            // or has been converted to CTF managed mode. 
            String internalApiKey = securityService.generatePassword(16, 24)
            contents += "\nsfmain.integration.security.shared_secret=" +
                internalApiKey
        }
        contents
    }

    /**
     * Renames the the current httpd.conf, then rewrites it based on
     * the dist version.
     */
    void backupAndOverwriteHttpdConf() {
        File confFile = new File(confDirPath, "httpd.conf")
        File bkupFile = new File(confDirPath, "httpd.conf.bkup")
        archiveFile(bkupFile)
        confFile.renameTo(bkupFile)        
        writeHttpdConf(confFile)
    }

    /**
     * If httpd.conf.bkup exists, use it to replace httpd.conf
     */
    void restoreHttpdConfFromBackup() {
        File confFile = new File(confDirPath, "httpd.conf")
        File bkupFile = new File(confDirPath, "httpd.conf.bkup")
        if (bkupFile.exists()) {
            archiveFile(confFile)
            bkupFile.renameTo(confFile)
        }
    }

    /**
     * Renames the file to have a unique .ddd suffix
     */
    private void archiveFile(File f) {
        if (f.exists()) {
            def name = f.name
            int i = 1
            File archiveFile = null
            while (!archiveFile || archiveFile.exists()) {
                archiveFile = new File(f.parentFile, f.name + "." + i++)
            } 
            f.renameTo(archiveFile)
        }
    }

    /**
     * escapes "\" (back slash) path separators in an input
     * @param inputPath string to escape
     * @return same string but with backslashes escaped
     */
    private String escapePath(String inputPath) {
        return inputPath?.replace('\\', '/')
    }


    /**
     * Retrieves the single instance of Server
     */
    private Server getServer() {
        Server.getServer()
    }
}
