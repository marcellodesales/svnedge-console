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
package com.collabnet.svnedge.jobs


import java.net.NoRouteToHostException;

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.collabnet.svnedge.console.services.JobsAdminService

/**
 * The Packages Update Job job will run and update the service status regarding
 * the software updates.
 * 
 * @author Marcello de Sales (mdesales@collab.net)
 */
class PackagesUpdateJob {

    /**
     * The packages update service
     */
    def packagesUpdateService

    static def jobName = "PackagesUpdateJob"
    static def group = "Maintenance"

    static triggers = { 
        cron name: jobName + "Trigger", group: group + "_Triggers", \
        startDelay: 0, \
        cronExpression: "0 15 12 ? * *"
    }

    def execute() {
        if (this.packagesUpdateService.hasBeenBootstraped()) {
            log.info("Checking for Software Updates...")
            try {
                this.packagesUpdateService.reloadPackagesAndUpdates()
                if (this.packagesUpdateService.areThereUpdatesAvailable()) {
                    log.info("There are updates available...")
                } else {
                    log.info("There are NO updates available")
                }
            } catch (NoRouteToHostException nrthe) {
                log.error("Can't access the software updates server: " + 
                        nrthe.getMessage())
            } catch (Exception e) {
                log.error("Error while verifying for software updates: " + 
                        e.getMessage())
            }
        } else {
            log.error("The Packages Update Service has not been bootstraped.")
        }
    }
}
