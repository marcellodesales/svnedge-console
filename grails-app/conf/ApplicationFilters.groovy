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

import com.collabnet.svnedge.domain.Server
import com.collabnet.svnedge.domain.ServerMode
import grails.util.GrailsUtil

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class ApplicationFilters {

    def operatingSystemService
    def authenticateService
    def lifecycleService
    def config = ConfigurationHolder.config
    def app = ApplicationHolder.application

    def filters = {

        /**
         * Filtering when requesting via http scheme and console is configured
         * to require ssl. This applies to all controllers/actions *except* the
         * rest api endpoint to fetch the securePort. This is because the mDNS service only
         * advertises the plain http port, and rest clients may not handle the redirect
         * which gets us from there to https
         */
        requireSsl(controller: '*', action: '*') {
            before = {
                // api to get securePort is always allowed 
                if (request.method == "GET" && request.forwardURI.contains("/api") &&
                        request.forwardURI.contains("/securePort")) {
                    return true
                }
                else if (request.scheme == 'http' && Server.getServer().useSslConsole) {
                    def port = System.getProperty("jetty.ssl.port", "4434")
                    def sslUrl = "https://${request.serverName}${port != "443" ? ":" + port : ""}${request.forwardURI}"
                    redirect(url: sslUrl)
                    return false
                }
                return true
            }
        }

        /**
         * Filtering when the server has not loaded the libraries correctly.
         */
        verifyOperatingSystemLibraries(controller: '*', action: '*') {
            after = {
                if (!operatingSystemService.isReady()) {
                    switch (controllerName) {
                        case "status":
                        case "statistics":
                        case "server":
                            flash.error = app.getMainContext().getMessage(
                                    "server.failed.loading.libraries", [] as String[],
                                    Locale.getDefault())
                            break;
                    }
                }
            }
        }

        /**
         * this filter defines the "features" available to the user 
         * (represented by buttons on the main toolbar) based on user roles and server mode
         */
        featureAvailability(controller: '*', action: '*') {

            before = {

                boolean isIntegrationServer = ServerMode.MANAGED == Server.getServer().mode
                boolean isManagedMode = (isIntegrationServer
                        || ServerMode.REPLICA == Server.getServer().mode)

                if (!isManagedMode &&
                        "server" == controllerName && (
                "editIntegration" == actionName || "revert" == actionName)) {

                    flash.error = app.getMainContext().getMessage(
                            "filter.probihited.mode.standalone", null,
                            Locale.getDefault())
                    redirect(controller: "status")
                    return false
                }
                if (isManagedMode &&
                        (["repo", "user", "role", "setupTeamForge"].contains(controllerName) ||
                         ("server" == controllerName && "editAuthentication" == actionName)) ||
                        (isIntegrationServer && "job" == controllerName)) {
                    flash.error = app.getMainContext().getMessage(
                            "filter.probihited.mode.managed", null,
                            Locale.getDefault())
                    redirect(controller: "status")
                    return false
                }
            }

            // after running the action, add "featureList" to the page model
            after = {model ->

                boolean isIntegrationServer = ServerMode.MANAGED == Server.getServer().mode
                boolean isManagedMode = (isIntegrationServer
                        || ServerMode.REPLICA == Server.getServer().mode)

                // default list of features for all users
                def featureList = []
                if (!isManagedMode) {
                    featureList << "repo"
                }

                if (lifecycleService.getServer().replica) {
                    featureList << "userCache"
                }
                else if (!isManagedMode) {
                    featureList << "user"
                }

                // role-based additions
                if (authenticateService.ifAnyGranted("ROLE_ADMIN,ROLE_ADMIN_SYSTEM")) {
                    if (lifecycleService.getServer().replica) {
                        featureList << "admin"
                    }
                    else {
                        featureList << "server"
                    }
                }

                // the OCN tab is always last
                featureList << "ocn"

                // add featurelist to the request model
                if (!model) {
                    model = new HashMap()
                }
                model.put("featureList", featureList)
                model.put("isManagedMode", isManagedMode)
                model.put("isIntegrationServer", isIntegrationServer)
            }
        }

        /**
         * this filter defines the help link target for the context
         */
        helpUrl(controller: '*', action: '*') {

            // after running the action, add "helpUrl" to the page model
            after = {model ->

                // per-controller help paths
                def controllerHelpPaths = [
                        "user": "/topic/csvn/action/manageusers_csvn.html",
                        "repo": "/topic/csvn/action/managerepositories.html",
                        "server": "/topic/csvn/action/configurecsvn.html",
                        "packagesUpdate": "/topic/csvn/action/upgradecsvn.html",
                        "statistics": "/topic/csvn/action/maintainserver_csvn.html"
                ]

                // default help url
                String helpBase = config.svnedge.helpUrl
                String helpPath = "/topic/csvn/faq/csvn_toc.html"

                // override if controller has specific url
                if (controllerHelpPaths[params.controller]) {
                    helpPath = controllerHelpPaths[params.controller]
                }

                // add the helpUrl to the page model
                if (!model) {
                    model = new HashMap()
                }
                model.put("helpBaseUrl", helpBase)
                model.put("helpUrl", helpBase + helpPath)
            }
        }

        redirectStatusToRepositoryList(controller: 'status', action: '*') {
            before = {
                if (!authenticateService
                        .ifAnyGranted("ROLE_ADMIN,ROLE_ADMIN_SYSTEM")) {
                    redirect(controller: 'repo', action: 'list')
                }
            }
        }
        
        /**
         * This filter restricts the access to the plug-in dbUtil under the
         * production environment for users with the roles "ROLE_ADMIN".
         */
        dbUtilPluginRestriction(controller: 'dbUtil', action: '*') {
            before = {
                if (GrailsUtil.environment != "production" ||
                        (authenticateService.isLoggedIn() &&
                                authenticateService.ifAnyGranted("ROLE_ADMIN"))) {
                    return true
                } else {
                    flash.error = app.getMainContext().getMessage(
                            "filter.probihited.credentials", null,
                            Locale.getDefault())
                    redirect(uri: '/')
                    return false
                }
            }
        }

        /**
         * This filter prevents access to the greenmail plug-in under the
         * production environment.
         */
        greenmailPluginRestriction(controller: 'greenmail', action: '*') {
            before = {
                if (GrailsUtil.environment != "production") {
                    return true
                } else {
                    redirect(controller: 'status', action: 'index')
                    return false
                }
            }
        }
    }

}
