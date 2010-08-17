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
package com.collabnet.svnedge.replica.event;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

class AsyncEventHandler implements Runnable {
    static Logger log = Logger.getLogger(AsyncEventHandler.class);
    LinkedBlockingQueue<ReplicaEvent> queue;

    /** 
     *  Since register/unregister happens in a different thread than
     *  reading, the listener ArrayLists must be synchronized.
     *  Consider making the ReplicaEventListeners WeakReferences, 
     *  if we ever hold them elsewhere in the system.
     */
    static Map eventListeners = Collections.
        synchronizedMap(new HashMap<Class<? extends ReplicaEvent>, 
                        ArrayList<ReplicaEventListener>>());

    public AsyncEventHandler(LinkedBlockingQueue<ReplicaEvent> queue) {
        this.queue = queue;
    }
    
    public void run() {
        ReplicaEvent event;
        while(true) {
            try {
                try {
                    event = (ReplicaEvent) queue.take();
                } catch (InterruptedException ie) {
                    continue;
                }
                log.info("Handling event: " + event);
                ArrayList<ReplicaEventListener> listeners = 
                    getListeners(event.getClass());
                if (listeners.isEmpty()) {
                    log.info("No listeners registered for this event type: " + 
                             event.getClass());
                }
                synchronized(listeners) {
                    for (ReplicaEventListener listener: listeners) {
                        listener.notifyEvent(event);
                    }
                }
            } catch (Exception e) {
                log.error("Exception in event handler. Sleeping for 10s", e)
                Thread.sleep(10000)
            }
        }
    }

    /**
     * Return the list of listeners associated with a class.  If null, 
     * return an empty ArrayList.
     */
    public static ArrayList<ReplicaEventListener> \
        getListeners(Class<? extends ReplicaEvent> eventClass) {
        ArrayList<ReplicaEventListener> listeners = 
            (ArrayList<ReplicaEventListener>) eventListeners.get(eventClass);
        if (listeners == null) {
            listeners = Collections.
                synchronizedList(new ArrayList<ReplicaEventListener>());
        }
        return listeners;
    }

    /**
     *  An eventListener will call this method to request notifications for
     *  a particular type of event.  It will only get those events that are
     *  precisely that class; it should register for any super claseses
     *  separately.
     *  For now, I'm assuming register requests come from the main thread.
     *  If we had multiple threads requesting at the same time, we'd want
     *  our arrayList to also be synchronized.
     *  @param clazz to notifiy the eventListener for.
     *  @param eventListener requesting notification.
     */
    public static <E extends ReplicaEvent> void \
                             register(Class<E> eventClass, 
                                      ReplicaEventListener eventListener) {
        ArrayList<ReplicaEventListener> listenersForEvent = 
            getListeners(eventClass);
        listenersForEvent.add(eventListener);
        eventListeners.put(eventClass, listenersForEvent);
        log.info("Registered " + eventListener + " for " + eventClass);
    }

    /**
     * Since we're assuming the only instance is referenced within the 
     * arrayList, we'll have to deregister by class type (instead of instance).
     */
    public static void \
        deregister(Class<? extends ReplicaEvent> eventClazz, 
                   Class<? extends ReplicaEventListener> listenerClazz) {
        ArrayList<ReplicaEventListener> listenersForEvent = 
            getListeners(eventClazz);
        synchronized(listenersForEvent) {
            Iterator<ReplicaEventListener> it = listenersForEvent.iterator();
            while(it.hasNext()) {
                ReplicaEventListener next = it.next();
                if (next.getClass().equals(listenerClazz)) {
                    it.remove();
                }
            }
        }
        log.info("Deregistered " + listenerClazz + " for " + eventClazz);
    }
}
