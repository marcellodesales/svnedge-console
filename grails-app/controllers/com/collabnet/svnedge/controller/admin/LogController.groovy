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
package com.collabnet.svnedge.controller.admin

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat

import org.codehaus.groovy.grails.plugins.springsecurity.Secured
import com.collabnet.svnedge.admin.LogManagementService.ConsoleLogLevel
import com.collabnet.svnedge.admin.LogManagementService.ApacheLogLevel

import org.springframework.web.servlet.support.RequestContextUtils as RCU

/**
 * Command class for 'saveConfiguration' action. Provides validation rules.
 */
class LogConfigurationCommand {

    def operatingSystemService
    ConsoleLogLevel consoleLevel
    ApacheLogLevel apacheLevel
    Integer pruneLogsOlderThan
    
    static constraints = {
        consoleLevel(nullable : false)
        apacheLevel(nullable : false)
        pruneLogsOlderThan(nullable : false, min : 0)
    }
}

/**
 * This Controller manages views and actions related to application logging.
 * This includes listing and viewing log files and configuring the apache and
 * console log levels.
 */
@Secured(['ROLE_ADMIN', 'ROLE_ADMIN_SYSTEM'])
class LogController {

    def operatingSystemService
    def logManagementService

    static allowedMethods = [saveConfiguration : 'POST']

    def list = {

        // fetch the log file list
        def files = logManagementService.getLogFiles()

        // add virtual bean-style properties to File for "date" and "size" --
        // allows easy sorting below
        File.metaClass.getDate = {-> delegate.lastModified() }
        File.metaClass.getSize = {-> delegate.length() }

        // sort log files based File property matching the sort param
        if (params.sort) {
          files = files.sort { f -> f."${params.sort}"}
        }

        if (params.order == "desc") {
          files = files.reverse()
        }
        def dtFormat = message(code: "default.dateTime.format.withZone")
        return [files: files, logDateFormat: dtFormat]
    }

    def saveConfiguration = { LogConfigurationCommand cmd ->

        if (!cmd.hasErrors()) {

            logManagementService.updateLogConfiguration(cmd.consoleLevel,
                    cmd.apacheLevel, 
                    cmd.pruneLogsOlderThan)
            flash.message = message(code: 
                'logs.action.saveConfiguration.success')
            redirect(action: 'configure')
        }
        else {

            flash.error = message(code: 'default.errors.summary')
            render(view: 'configure', model : [ logConfigurationCommand : cmd,
                    consoleLevels : ConsoleLogLevel.values(),
                    apacheLevels : ApacheLogLevel.values()
                    ])
        }
    }

    def configure = {

        def cmd = new LogConfigurationCommand(
            consoleLevel : logManagementService.consoleLevel,
            apacheLevel : logManagementService.apacheLevel,
            pruneLogsOlderThan : logManagementService.logDaysToKeep)

        render(view: "configure", model: [ logConfigurationCommand : cmd,
                consoleLevels : ConsoleLogLevel.values(),
                apacheLevels : ApacheLogLevel.values()
                ])
    }

    def show = {
        def logName = params.fileName
        if (!logName || logName.trim().equals("")) {
            flash.error = message(code: 'logs.action.show.fileName.empty')
            redirect(action: "list")
            return
        }
        try {
            def logFile = logManagementService.getLogFile(logName)

            def view = (params.rawView) ? "showRaw" : "show"
            def contentType = (params.rawView) ? "text/plain" : "text/html"

            def logSize = operatingSystemService.formatBytes(logFile.length())
            def modifiedTime = new Date(logFile.lastModified())
            def currentLocale = RCU.getLocale(request)
            def dtFormat = message(code: "default.dateTime.format.withZone")
            def requestFormatter = new SimpleDateFormat(dtFormat,
                currentLocale)
            def logModifiedTime = requestFormatter.format(modifiedTime);

            render(view: view, contentType: contentType,
                model: [ file: logFile, fileSize: logSize,
                    fileModification: logModifiedTime, dateTimeFormat:dtFormat])

        } catch (FileNotFoundException logDoesNotExist) {
            flash.error = message(code: 'logs.page.show.header.fileNotFound',
                args:[logName])
            redirect(action: "list")
            return
        }
    }

}
