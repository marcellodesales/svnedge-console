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

import grails.test.*

class AsyncEventListenerIntegrationTests extends GrailsUnitTestCase {
    def asyncEventListenerService
    long WAIT_TIME = 1000

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testFireReplicaEvent() {
        ReplicaEvent event = new ReplicaEvent()
        asyncEventListenerService.fireEvent(event)
        // test will have error if exception is thrown
    }

    void testFireUserCacheEvent() {
        UserCacheEvent event = new UserCacheEvent("test", UserCacheEvent.AUTHN,
                                                  UserCacheEvent.HIT)
        //asyncEventListenerService.fireEvent(event)
        // test will have error if exception is thrown
    }

    void testRegisterUnregisterListener() {
        def testListener = new TestEventListener();
        testListener.listen(ReplicaEvent.class)
        ReplicaEvent event = new ReplicaEvent()
        asyncEventListenerService.fireEvent(event)
        // it's async, so we'd better wait a sec
        synchronized(testListener) {
            testListener.wait(WAIT_TIME)
        }
        assertTrue("TestListener should have been notified of one event.",
                    testListener.events.size() == 1)
        // now unregister the listener and make sure it doesn't get another
        // event.
        AsyncEventHandler.deregister(ReplicaEvent.class, 
                                     TestEventListener.class)
        event = new ReplicaEvent()
        asyncEventListenerService.fireEvent(event)
        synchronized(testListener) {
            testListener.wait(WAIT_TIME)
        }
        assertTrue("TestListener should have been notified of only one event.",
                    testListener.events.size() == 1)
    }

    void testCatchUserCacheEvent() {
        def testListener = new TestEventListener();
        testListener.listen(UserCacheEvent.class)
        ReplicaEvent event = new UserCacheEvent("test", UserCacheEvent.AUTHN,
                                                UserCacheEvent.HIT)
        asyncEventListenerService.fireEvent(event)
        // it's async, so we'd better wait a sec
        synchronized(testListener) {
            testListener.wait(WAIT_TIME)
        }
        assertTrue("TestListener should have been notified of one event.",
                    testListener.events.size() == 1)
    }
}
