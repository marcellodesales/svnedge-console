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
package com.collabnet.svnedge.replica.service.error

import com.collabnet.svnedge.replica.error.ReplicaError

import grails.test.*
import org.apache.log4j.Logger
import org.apache.log4j.Level
import com.collabnet.svnedge.console.Server

class ReplicaErrorIntegrationTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testManualCreate() {
        def testError = new ReplicaError(timestamp: \
                                         System.currentTimeMillis(), 
                                         level: 1, message: "A test error", 
                                         className: "className", 
                                         fileName: "fileName", 
                                         lineNumber: 3, 
                                         methodName: "methodName")
        testError.save()
        testError = ReplicaError.findById(testError.id)
        assertNotNull testError
    }

    void testLogCreate() {
        String msg = "This is just a text error message, no worries!"
        Logger logger = Logger.getRootLogger()
        def isReplica = Server.getServer().replica
        Server.getServer().replica = true
        logger.error(msg)
        Server.getServer().replica = isReplica
        ReplicaError error = ReplicaError.findByMessage(msg)
        assertNotNull error
        assertEquals error.getLevel(), Level.ERROR.toInt()
    }
}
