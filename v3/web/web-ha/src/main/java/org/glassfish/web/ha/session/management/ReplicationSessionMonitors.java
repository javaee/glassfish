/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.web.ha.session.management;

import java.lang.ref.WeakReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

/**
 * Enables fine grain session level synchronization by maintaining a cache of synchronization monitors for sessions.  
 * Monitors are looked up by session id, type being String.
 * Key constraint maintained by this class is that at any point in time, 
 * there is only one monitor per session id.
 * 
 * The monitors in the cache are aggressively recovered whenever a monitor is no longer being used.
 * All threads operating atomically on session related to session id must use same monitor.
 * When monitor is no longer referenced by any thread, it is okay for it to be reclaimed.
 * 
 * @author ha team
 */
public class ReplicationSessionMonitors {
    
    /**
     * Our cache of monitor object keyed by session id(string), value is KeyedReference that references
     * a monitor to be used in synchronization blocks to ensure atomic operations relating to a session.
     * Map is ConcurrentHashMap<String, KeyedReference>
     */
    private ConcurrentHashMap replicatedSessionMonitors = null;
    private ReferenceQueue replicatedSessionMonitorsRefQueue = new ReferenceQueue();
    
    private static final int CONCURRENCY_LEVEL = 100; 
    
    final private Logger _logger;
            
    private static class KeyedReference extends WeakReference {
        private String key;
        private KeyedReference(Object objRef, ReferenceQueue q, String key) {
            super(objRef, q);
            this.key = key;
        }
        public String toString() {
            return "key=" + key + " resolvesTo=" + get();
        }
    }
    
    /**
     * 
     * @param logger      logger to use for log events.
     * @param size        initial size for Map of session ids to monitor objects
     * @param loadFactor  loadfactor for map
     */
    public ReplicationSessionMonitors(Logger logger, int size, float loadFactor) {
        _logger = logger;
        replicatedSessionMonitors = new ConcurrentHashMap(size, loadFactor, CONCURRENCY_LEVEL);
        replicatedSessionMonitorsRefQueue = new ReferenceQueue();
    }
    
    /**
     * Return an existing or new ReplicationSession monitor for id.
     * 
     * @param id - a session id
     * @return Monitor for session <code>id</code>
     */
    public Object get(String id) {
        Object result = null;
        //first look in map - most of the time this will succeed
        Reference resultRef = (Reference)replicatedSessionMonitors.get(id);
        if(resultRef != null) {
            result = resultRef.get();
            if(result != null) {                
                return result;
            } else {
                // cover case that GC released weak reference already.
                // remove map entry to released monitor and use this method 
                //again to insert a new monitor for id.
                //only remove if value is still resultRef to null - thread safe
                remove(id, resultRef);
                return get(id);
            }
        }
        Object keepReachable 
            = new Object();  // not likely but keep strong ref to new Monitor so not gc'ed
        Reference ref 
            = new KeyedReference(keepReachable, replicatedSessionMonitorsRefQueue, id);
        Reference refToResult 
            = (Reference)replicatedSessionMonitors.putIfAbsent(id, ref);
        //this means we just put ref into the map so we return keepReachable
        if(refToResult == null) {
            return keepReachable;
        }
        result = refToResult.get();
        if(result != null) {
            return result;
        } else {
            // cover case that reference was released already.
            // remove map entry to released monitor and use this method 
            //again to insert a new monitor for id.
            //only remove if value is still resultRef to null - thread safe
            remove(id, refToResult);
            return get(id);
        }        
    }    
    
    private Object remove(String id, Object toRemove) {
        boolean removed = replicatedSessionMonitors.remove(id, toRemove);
        if(removed) {
            ((KeyedReference)toRemove).key = null;
        }
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("replicatedSession monitors remove id=" + id + " succeeded=" + removed);
        }
        return removed;
    }
    
    private void assertOnReferenceQueue(KeyedReference ref) {
        if (_logger.isLoggable(Level.FINE)) {
            if (ref.get() != null) {
                _logger.fine("assertion failed: monitor for session id: " + ref.key + " was on ReferenceQueue but it" +
                        " but its get() returned non-null");
            }
        }
    }
    
    /**
     * Remove entries from cache with values of weak references that no longer reference an object. 
     */
    public void processExpired() {
        KeyedReference toBeRemoved = null;
        while ((toBeRemoved = (KeyedReference)this.replicatedSessionMonitorsRefQueue.poll()) != null) {
            if(toBeRemoved.key == null) continue;
            assertOnReferenceQueue(toBeRemoved);
            Object removed = remove(toBeRemoved.key, toBeRemoved);    
            if (removed == null && _logger.isLoggable(Level.FINE)) {
                _logger.fine("processExpiredReplicaMonitor: unable to remove " + toBeRemoved + " sessionId=" + toBeRemoved.key);
            }
        }
        if (_logger.isLoggable(Level.FINER)) {
            int exitSize = replicatedSessionMonitors.size();
            if (exitSize != 0) {
                _logger.finer("exit ReplicationSessionMonitors.processExpired numberOfEntries=" + exitSize);
                if (_logger.isLoggable(Level.FINEST)) {
                    int i = 1;
                    for (Object keyRef : replicatedSessionMonitors.values()) {
                        _logger.finest("ReplicationSessionMonitors.processExpired: replicatedSessionMonitor[" + i + "]: " + ((KeyedReference) keyRef));
                        i++;
                    }
                }
            }
        }
    }
}
