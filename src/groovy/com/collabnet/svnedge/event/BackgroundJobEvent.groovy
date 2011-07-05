/*
 * CollabNet Subversion Edge
 * Copyright (C) 2011, CollabNet Inc. All rights reserved.
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
package com.collabnet.svnedge.event

import org.springframework.context.ApplicationEvent;

import com.collabnet.svnedge.integration.command.CommandsExecutionContext;

/**
 * This is the base class for Csvn background job events
 */
class BackgroundJobEvent extends SvnEdgeApplicationEvent {

    Map properties
    def procId

    /**
     * constructor for the event
     * @param source object from which event is fired
     * @param procId the process id associated with the background job (thread.id, eg)
     * @param properties other properties or parameters describing the job
     */
    public BackgroundJobEvent(source, procId, Map properties) {
        super(source)
        this.properties = [ timestamp: this.timestamp ]
        if (properties) {
            this.properties << properties
        }
        this.procId = procId
    }
}
