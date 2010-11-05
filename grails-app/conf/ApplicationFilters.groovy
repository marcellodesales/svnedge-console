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
import grails.util.GrailsUtil
import com.collabnet.svnedge.console.Server
import com.collabnet.svnedge.console.ServerMode

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class ApplicationFilters {

    def operatingSystemService
    def authenticateService
    def lifecycleService
    def config = ConfigurationHolder.config
    def app = ApplicationHolder.application

    def filters = {

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

                boolean isManagedMode = (ServerMode.MANAGED == Server.getServer().mode
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
                ("server" == controllerName && "editAuthentication" == actionName))) {
                    flash.error = app.getMainContext().getMessage(
                            "filter.probihited.mode.managed", null,
                            Locale.getDefault())
                    redirect(controller: "status")
                    return false
                }
            }
            
            // after running the action, add "featureList" to the page model
            after = {model ->
                
                boolean isManagedMode = (ServerMode.MANAGED == Server.getServer().mode
                        || ServerMode.REPLICA == Server.getServer().mode)

                // default list of features for all users
                def featureList = ["status"]
                if (!isManagedMode) {
                    featureList << "repo"
                }

                if (lifecycleService.getServer().replica) {
                    featureList << "userCache"
                }
                else if (!isManagedMode) {
                    featureList << "user"
                }

                featureList << "statistics"

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
                        "packagesUpdate" : "/topic/csvn/action/upgradecsvn.html",
                        "statistics" : "/topic/csvn/action/maintainserver_csvn.html"
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
                model.put("helpUrl", helpBase + helpPath)
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
                    redirect(uri:'/')
                    return false
                }
            }
        }
    }

}
