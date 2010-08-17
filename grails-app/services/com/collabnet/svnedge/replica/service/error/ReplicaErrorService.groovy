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

import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggingEvent

import com.collabnet.svnedge.replica.error.ReplicaError
import com.collabnet.svnedge.console.Server

/**
 * The ReplicaErrorService is a log4j appender.  It catches LoggingEvents
 * and adds them to the local database.  These errors can later be passed
 * to the master.
 */
class ReplicaErrorService extends AppenderSkeleton {

    boolean transactional = false

    def minLevel
    
    def bootStrap = { initialMinLevel ->
            minLevel = initialMinLevel
            Logger root = Logger.getRootLogger()
            root.addAppender(this)
    }

    void close() {
        Logger root = Logger.getRootLogger()
        root.removeAppender(this)
    }

    void append(LoggingEvent logEvent) {
        if (logEvent.getLevel().toInt() < minLevel || 
            !Server.getServer().replica) {
            return;
        }
        long eventTime = System.currentTimeMillis()
        if (logEvent.getTimeStamp() != null) {
            eventTime = logEvent.getTimeStamp()
        }
        new ReplicaError(timestamp: eventTime, 
                level: logEvent.getLevel().toInt(), 
                message: logEvent.getRenderedMessage(), 
                className: logEvent.getLocationInformation()?.getClassName(),
                fileName: logEvent.getLocationInformation()?.getFileName(),
                lineNumber: logEvent.getLocationInformation()?.getLineNumber(),
                methodName: logEvent.getLocationInformation()?.getMethodName(),
                exceptionRep: logEvent.getThrowableInformation()?.\
                        getThrowableStrRep()).save()
    }

    boolean requiresLayout() {
        false
    }
}
