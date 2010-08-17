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
package com.collabnet.svnedge.console.ui

import grails.converters.JSON
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.net.ConnectException

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import org.codehaus.groovy.grails.commons.ConfigurationHolder

/**
 * The packages update controller is used to manage the packages 
 * update and installed packages.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */
@Secured(['ROLE_ADMIN','ROLE_ADMIN_SYSTEM'])
class PackagesUpdateController {

    def defaultAction = 'available'

    def packagesUpdateService

    def config = ConfigurationHolder.config

    // start and stop actions use POST requests
    static allowedMethods = [installUpdates: 'POST', installAddOns:'POST', 
                             reloadUpdates:'POST', reloadAddOns:'POST', 
                             confirmStart:'POST', restartServer:'POST']

    def getSortedOrderedPackages(packagesInfo, params) {
        if (!packagesInfo || packagesInfo.length == 0) {
            return packagesInfo
        }
        if (params.sort) {
            packagesInfo = packagesInfo.sort { pkg -> pkg."${params.sort}"}
        } else {
            packagesInfo = packagesInfo.sort { pkg -> pkg.summary}
        }
        if (params.order == "desc") {
            packagesInfo = packagesInfo.reverse()
        }
        return packagesInfo
    }

    def available = {
        if (!flash.error) {
            if (this.packagesUpdateService.areThereUpdatesAvailable()) {
                flash.warn = "New Updates Available!"
            }
            if (this.packagesUpdateService.systemNeedsRestart()) {
                flash.warn = this.packagesUpdateService.
                        getSystemNeedsRestartMessage()
            }
        }

        def originUrl = this.packagesUpdateService.getImageOriginUrl()
        def proxyURL = this.packagesUpdateService.getImageProxyToOriginUrl()
        def pkgsInf = this.packagesUpdateService.getUpgradablePackagesInfo()
        pkgsInf = this.getSortedOrderedPackages(pkgsInf, params)

        def hadConnectionProblems = false
        if (session["connectionProblems"] != null) {
            hadConnectionProblems = true
            session["connectionProblems"] = null
        }
        return [packagesInfo: pkgsInf, 
                imageOriginUrl: originUrl,
                proxyToOriginURL: proxyURL,
                anyConnectionProblem: hadConnectionProblems
        ]
    }

    def addOns = {
        if (!flash.error) {
            if (this.packagesUpdateService.areThereUpdatesAvailable()) {
                flash.warn = this.packagesUpdateService.
                        getUpgradeAvailableMessage()
            }
            if (this.packagesUpdateService.systemNeedsRestart()) {
                flash.warn = this.packagesUpdateService.
                        getSystemNeedsRestartMessage()
            }
        }

        def originUrl = this.packagesUpdateService.getImageOriginUrl()
        def proxyURL = this.packagesUpdateService.getImageProxyToOriginUrl()
        def pkgsInf = this.packagesUpdateService.getNewPackagesInfo()
        pkgsInf = this.getSortedOrderedPackages(pkgsInf, params)

        def hadConnectionProblems = false
        if (session["connectionProblems"] != null) {
            hadConnectionProblems = true
            session["connectionProblems"] = null
        }
        return [packagesInfo: pkgsInf,
                imageOriginUrl: originUrl,
                proxyToOriginURL: proxyURL,
                anyConnectionProblem: hadConnectionProblems
        ]
    }

    def installed = {
        if (!flash.error) {
            if (this.packagesUpdateService.areThereUpdatesAvailable()) {
                flash.warn = this.packagesUpdateService.
                        getUpgradeAvailableMessage()
            }
            if (this.packagesUpdateService.systemNeedsRestart()) {
                flash.warn = this.packagesUpdateService.
                        getSystemNeedsRestartMessage()
            }
        }

        def originUrl = this.packagesUpdateService.getImageOriginUrl()
        def proxyURL = this.packagesUpdateService.getImageProxyToOriginUrl()
        def pkgsInf = this.packagesUpdateService.getInstalledPackagesInfo()
        if (!pkgsInf) {
            //if this happens, the application started without any 
            //connectivity with the originURL, resulting with an empty cache
            if (!flash.error) {
                flash.error = this.getNoConnectionErrorMessage(
                    "reloadInstalled")
            }
        } else {
            pkgsInf = this.getSortedOrderedPackages(pkgsInf, params)
        }

        return [packagesInfo: pkgsInf,
                imageOriginUrl: originUrl,
                proxyToOriginURL: proxyURL
        ]
    }

    private String getNoConnectionErrorMessage(packagesType) {
        def action = ""
        if (packagesType == "reloadInstalled") {
            action = " <a href='/csvn/packagesUpdate/reloadInstalled'>" +
                    "Reload</a> after checking the network connectivity."
        } else {
            action = " Reload after checking the network connectivity."
        }
        def server = this.packagesUpdateService.getImageOriginUrl() ?: ""
        server = (server != "") ? " '${server}'" : ""
        return "There's no network connection with the packages repository " +
                "server${server}.${action}"
    }

    private String getNoRouteErrorMessage(packagesType) {
        def helpUrl = config.svnedge.helpUrl
        def helpPath = "/topic/csvn/action/upgradecsvn_proxy.html"
        def helpLink = "<a href='${helpUrl}${helpPath}' target='csvnHelp'>"
        return "The packages repository server '" + 
                this.packagesUpdateService.getImageOriginUrl() + "' is " +
                "unreachable. This usually happens behind a ${helpLink}" +
                "network proxy server</a>."
    }

    def reloadInstalled = {
        try {
            this.packagesUpdateService.reloadPackagesAndUpdates()

        } catch (UnknownHostException uhe) {
            session["connectionProblems"] = "installed"
            flash.error = this.getNoConnectionErrorMessage("reloadInstalled")
            log.error(flash.error)

        } catch (ConnectException ce) {
            session["connectionProblems"] = "installed"
            //if the connection times out. Same effect as NoRouteToHostException
            flash.error = this.getNoRouteErrorMessage("reloadInstalled")
            log.error(flash.error)

        } catch (NoRouteToHostException nrth) {
            session["connectionProblems"] = "installed"
            flash.error = this.getNoRouteErrorMessage("reloadInstalled")
            log.error(flash.error)

        } catch (Exception e) {
            session["connectionProblems"] = "installed"
            flash.error = "An error occurred while loading installed " +
                    "packages: " + e.getMessage()
            log.error(flash.error, e)
        }
        redirect(action:"installed")
    }

    def reloadUpdates = {
        try {
            this.packagesUpdateService.reloadPackagesAndUpdates()

        } catch (UnknownHostException uhe) {
            session["connectionProblems"] = "updates"
            flash.error = this.getNoConnectionErrorMessage()
            log.error(flash.error)

        } catch (ConnectException ce) {
            session["connectionProblems"] = "updates"
            //if the connection times out. Same as effect NoRouteToHostException
            flash.error = this.getNoRouteErrorMessage()
            log.error(flash.error)

        } catch (NoRouteToHostException nrth) {
            session["connectionProblems"] = "updates"
            flash.error = this.getNoRouteErrorMessage()
            log.error(flash.error)

        } catch (Exception e) {
            session["connectionProblems"] = "updates"
            flash.error = "An error occurred while loading software updates: " +
                    e.getMessage()
            log.error(flash.error, e)
        }
        redirect(action:"available")
    }

    def reloadAddOns = {
        try {
            this.packagesUpdateService.reloadPackagesAndUpdates()

        } catch (UnknownHostException uhe) {
            session["connectionProblems"] = "addOns"
            flash.error = this.getNoConnectionErrorMessage()
            log.error(flash.error)

        } catch (ConnectException ce) {
            session["connectionProblems"] = "addOns"
            //if the connection times out. Same as effect NoRouteToHostException
            flash.error = this.getNoRouteErrorMessage()
            log.error(flash.error)

        } catch (NoRouteToHostException nrth) {
            session["connectionProblems"] = "addOns"
            flash.error = this.getNoRouteErrorMessage()
            log.error(flash.error)

        } catch (Exception e) {
            session["connectionProblems"] = "addOns"
            flash.error = "An error occurred while loading new packages: " +
                    e.getMessage()
            log.error(flash.error, e)
        }
        redirect(action:"addOns")
    }

    def installAddOns = {
        if (this.packagesUpdateService.areThereNewPackagesAddOns()) {
            session["install"] = "addOns"
            redirect(action:"installUpdatesStatus")
        }
    }

    def installUpdates = {
        if (this.packagesUpdateService.areThereUpdatesAvailable()) {
            session["install"] = "updates"
            redirect(action:"installUpdatesStatus")
        }
    }

    def installUpdatesStatus = {
        if (session["install"] == null) {
            redirect(action:"installed")
        }
    }

    def confirmStart = {
        if (session["install"] == null) {
            redirect(action:"installed")
        }
        //non-blocking method call that redirects to the the status
        //page, which shows the current status from the cometd service.
        //The update process occurs in a separate daemon thread.
        if (session["install"].equals("addOns")) {
            this.packagesUpdateService.installPackagesAddOns()
        } else
        if (session["install"].equals("updates")){
            this.packagesUpdateService.installPackagesUpdates()
        }
        session["install"] = null

        String statusOk = "ok"
        def result = [status: statusOk]
        render result as JSON
    }

    def restartServer = {
        this.packagesUpdateService.restartServer()
        String statusOk = "ok"
        def result = [status: statusOk]
        render result as JSON
    }
}
