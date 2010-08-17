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
package com.collabnet.svnedge.replica.manager

import grails.test.*

class ReplicaConfigIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCreate() {
        def posRate = 60
        def negRate = 5
        def cachePeriod = 2
        def svnSyncRate = 4
        def replicaConfig = new ReplicaConfig(
            positiveExpirationRate: posRate,
            negativeExpirationRate: negRate,
            cacheFlushPeriod: cachePeriod,
            svnSyncRate: svnSyncRate,
            name: "Test Replica",
            locationName: "Brisbane, CA, USA.",
            latitude: 37.674423,
            longitude: -122.38494
        )
        if (!replicaConfig.validate()) {
            replicaConfig.errors.allErrors.each {
                println it
            }
            fail("ReplicaConfig should successfully validate.")
        }
        if (!replicaConfig.save()) {
            fail("ReplicaConfig should successfully save.")
        }
    }
}
