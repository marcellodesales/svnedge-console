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
package com.collabnet.svnedge.console

/**
 * The configuration utility class is used during the bootstrap and services
 * that needs the values from the configuration as shortcuts.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 *
 */
public final class ConfigUtil {

    def static appHome(config) {
        return new File(config.svnedge.appHome).absolutePath
    }

    def static httpdPath(config) {
        return config.svnedge.svn.httpdPath ? config.svnedge.svn.httpdPath :
            new File(appHome(config), "bin/httpd").absolutePath
    }

    def static httpdBindPath(config) {
        return new File(appHome(config), "/lib/httpd_bind").absolutePath
    }

    def static exeHttpdBindPath(config) {
        return new File(httpdBindPath(config), "httpd_bind").absolutePath
    }

    def static libHttpdBindPath(config) {
        return new File(httpdBindPath(config), "libhttpd_bind.so.1").absolutePath
    }

    def static htpasswdPath(config) {
        return config.svnedge.svn.htpasswdPath ?
            config.svnedge.svn.htpasswdPath : 
            new File(appHome(config), "bin/htpasswd").absolutePath
    }

    def static opensslPath(config) {
        return config.svnedge.opensslPath ?
            config.svnedge.opensslPath :
            new File(appHome(config), "bin/openssl").absolutePath
    }

    def static svnPath(config) {
        return config.svnedge.svn.svnPath ? config.svnedge.svn.svnPath :
            new File(appHome(config), "bin/svn").absolutePath
    }

    def static svnadminPath(config) {
        return config.svnedge.svn.svnadminPath ? 
            config.svnedge.svn.svnadminPath : 
            new File(appHome(config), "bin/svnadmin").absolutePath
    }

    def static svnsyncPath(config) {
        return config.svnedge.replica.svn.svnsyncPath ?
            config.svnedge.replica.svn.svnsyncPath : 
            new File(appHome(config), "bin/svnsync").absolutePath
    }

    def static dataDirPath(config) {
        return new File(appHome(config), "data").absolutePath
    }

    def static viewvcTemplateDir(config) {
        return config.svnedge.svn.viewvcTemplatesPath ? 
            config.svnedge.svn.viewvcTemplatesPath : 
            new File(appHome(config), "www/viewvc").absolutePath
    }

    def static viewvcTemplatesDirPath(config) {
        return config.svnedge.svn.viewvcTemplatesPath ?
            config.svnedge.svn.viewvcTemplatesPath : 
            new File(appHome(config), "www/viewvc").absolutePath
    }

    def static cgiDirPath(config) {
        return config.svnedge.svn.cgiDirPath ? config.svnedge.svn.cgiDirPath :
            new File(appHome(config), "bin/cgi-bin").absolutePath
    }

    def static viewvcScriptDir(config) {
        return cgiDirPath(config)
    }

    def static confDirPath(config) {
        return config.svnedge.svn.confDirPath ?
            config.svnedge.svn.confDirPath :
            new File(appHome(config), "etc/conf").absolutePath
    }

    def static distDir(config) {
        return config.svnedge.svn.distDirPath ?:
            new File(appHome(config), "dist").absolutePath
    }

    def static serviceName(config) {
        return config.svnedge.osName == "Win" ? 
            config.svnedge.svn.serviceName : null
    }

    def static viewvcLibPath(config) {
        return config.svnedge.svn.viewvcLibPath ?
            config.svnedge.svn.viewvcLibPath :
            new File(appHome(config), "lib/viewvc").absolutePath
    }

    def static modPythonPath(config) {
        return config.svnedge.svn.modPythonPath ? 
            config.svnedge.svn.modPythonPath : 
            new File(appHome(config), "bin/mod_python").absolutePath
    }
}
