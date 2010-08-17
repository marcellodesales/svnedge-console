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
package com.collabnet.svnedge.replica.event

import java.util.concurrent.LinkedBlockingQueue

class AsyncEventListenerService {
    boolean transactional = false
    def backgroundService
    def eventQueue = new LinkedBlockingQueue<ReplicaEvent>();

    def bootStrap() {
        log.info("Starting AsyncEventHander.");
        def handler = new AsyncEventHandler(eventQueue)
        backgroundService.execute("AsyncEventHandler background task",
                                  { handler.run() })
    }

    def fireEvent(event) {
        def success = eventQueue.offer(event)
        if (!success) {
            log.error("The AsynchronousEventQueue is completely full.  " +
                          "Forced to block the event thread.")
            eventQueue.put(event)
        }
    }
}
