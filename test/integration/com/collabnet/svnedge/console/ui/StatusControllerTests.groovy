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

import grails.test.*

class StatusControllerTests extends SvnEdgeAbstractControllerTests {

    def quartzScheduler
    def realTimeStatisticsService
    def lifecycleService
    def packagesUpdateService
    def operatingSystemService
    def networkingService
    def svnRepoService

    protected void setUp() {
        super.setUp()
        controller.quartzScheduler = quartzScheduler
        controller.realTimeStatisticsService = realTimeStatisticsService
        controller.lifecycleService = lifecycleService 
        controller.packagesUpdateService = packagesUpdateService
        controller.operatingSystemService = operatingSystemService
        controller.networkingService = networkingService
        controller.svnRepoService = svnRepoService
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIndex() {
        controller.index()
    }

}
