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

/**
 * This is the base class for Csvn application events
 */
abstract class SvnEdgeApplicationEvent extends ApplicationEvent {

    def id

    public SvnEdgeApplicationEvent(source) {
        super(source)
        this.id = createEventId()
    }

    /**
     * creates a random event id in the form "eventNNNN"
     * @return
     */
    private static String createEventId() {
        def prefix = "event"
        def id = Math.round(Math.random() * 8999) + 1000
        return prefix + id
    }
}
