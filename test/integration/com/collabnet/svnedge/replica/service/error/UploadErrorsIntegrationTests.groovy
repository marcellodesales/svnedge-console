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

import grails.test.*

import org.apache.log4j.Logger
import com.collabnet.svnedge.console.Server

class UploadErrorsIntegrationTests extends GrailsUnitTestCase {

    Logger logger = Logger.getRootLogger()

    def uploadErrorsService

    void testUploadErrors() {
        def msg = "Error message for upload"
        logger.error(msg)
        uploadErrorsService.uploadErrors()
    } 

    void testUploadErrorWithException() {
        def exception = new RuntimeException("Test exception");
        def msg = "Error message with exception for upload"
        def isReplica = Server.getServer().replica
        Server.getServer().replica = true
        logger.error(msg, exception)
        Server.getServer().replica = isReplica
        assertEquals("One exception should have been uploaded", 1,
                uploadErrorsService.uploadErrors())
    }
}
