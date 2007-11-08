/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
/*
 * ReplicationResponseRepository.java
 *
 * Created on January 4, 2006, 12:42 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Larry White
 */
public class ReplicationResponseRepository implements Runnable {
    
    /**
     * The sleep interval in seconds
     */    
    private static int _sleepIntervalSeconds = 10;
    
    /**
     * The singleton instance of ReplicationResponseRepository
     */    
    private static ReplicationResponseRepository _soleInstance 
        = new ReplicationResponseRepository();    
    
    /** Creates a new instance of ReplicationResponseRepository */
    public ReplicationResponseRepository() {
        _repository = new ConcurrentHashMap();
        _queueForQueues = new ConcurrentLinkedQueue();
        _federatedQueryRepository = new ConcurrentHashMap();
    }   

    /** 
     *  the map of ReplicationState responses to queries 
     *  key is id, value is associated ReplicationState
     */    
    public ConcurrentHashMap getRepository() {
        return _repository;
    }
    
    public static LinkedBlockingQueue putEmptyQueueEntry(ReplicationState state) {
        ReplicationResponseRepository repos = getInstance();
        //LinkedBlockingQueue aQueue = repos.getQueue();
        LinkedBlockingQueue aQueue = 
            repos.getClearedQueueEntry((String)state.getId());
        //just put an empty queue to be filled by server side
        repos.getRepository().put(state.getId(), new ReplicationResponseRepositoryEntry(aQueue));
        //FIXME remove next line after testing
        //repos.getRepository().put(state.getId(), aQueue);
        return aQueue;
    }
    
    public static FederatedRequestProcessor putWrappedEmptyQueueEntry(ReplicationState state, FederatedRequestProcessor aWrapper) {
        LinkedBlockingQueue aQueue = putEmptyQueueEntry(state);
        //FederatedRequestProcessor aWrapper = new FederatedRequestProcessor(1, 500);
        ReplicationResponseRepository repos = getInstance();
        repos.getFederatedQueryWrapperMap().put(state.getId(), aWrapper);
        return aWrapper;
    }    
    
    //return cleared queue entry (if it exists) or get one from queue of queues
    private LinkedBlockingQueue getClearedQueueEntry(String id) {
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap reposMap = repos.getMap();
        ReplicationResponseRepositoryEntry entry 
            = (ReplicationResponseRepositoryEntry) reposMap.get(id);
        if(entry == null) {
            return repos.getQueue();
        }
        LinkedBlockingQueue aQueue = entry.getQueue();
        this.removeEntry(id, entry);
        //FIXME remove next line after testing
        //LinkedBlockingQueue aQueue = (LinkedBlockingQueue) reposMap.get(id);
        //clear out queue if it exists
        if(aQueue != null) {
            //handled by previous removeEntry - remove after testing
            //aQueue.clear(); 
            return aQueue;
        } else {
            return repos.getQueue();
        }
    }

    /**
     * clean up and remove entry
     * @param id
     * @param entry
     */     
    private void removeEntry(String id, ReplicationResponseRepositoryEntry entry) {    
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap reposMap = repos.getMap();
        entry.clean();
        reposMap.remove(id);       
    }
    
    /**
     * clean up and remove entry
     * @param id
     */     
    public static void removeEntry(String id) {    
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap reposMap = repos.getMap();
        Object entry = reposMap.get(id);
        if(entry != null && entry instanceof ReplicationResponseRepositoryEntry) {
            ReplicationResponseRepositoryEntry reposEntry 
                = (ReplicationResponseRepositoryEntry) entry;
            reposEntry.clean();
        }
        reposMap.remove(id);
        /*
        Set entries = reposMap.entrySet();
        System.out.println("after removeEntry:entries count = " + entries.size()); 
         */  
    }    
    
    public static void returnQueueEntry(String id, LinkedBlockingQueue aQueue) {        
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap reposMap = repos.getMap();
        reposMap.remove(id);
        //put queue back in _queueForQueues
        if(aQueue != null) {
            aQueue.clear();
            repos.getQueueForQueues().offer(aQueue);
        }
    }
    
    public static void putEntry(ReplicationState state) {
        //System.out.println("ReplicationResponseRepository>>putEntry: id = " + state.getId());
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap reposMap = repos.getMap();
        ReplicationResponseRepositoryEntry entry 
            = (ReplicationResponseRepositoryEntry) reposMap.get(state.getId());
        //System.out.println("ReplicationResponseRepository>>putEntry: entry = " + entry);
        if(entry == null) {
            return;
        }
        LinkedBlockingQueue aQueue = entry.getQueue();
        //System.out.println("ReplicationResponseRepository>>putEntry: aQueue = " + aQueue);
        //FIXME remove next line after testing
        //LinkedBlockingQueue aQueue = (LinkedBlockingQueue) reposMap.get(state.getId());
        //FIXME for testing and might leave in for safety
        if(aQueue == null) {
            return;
        }
        if(aQueue.size() > 0) {
            aQueue.clear();
        }
        aQueue.add(state);
        //repos.getRepository().put(state.getId(), state);
    }
    
    public static void putFederatedEntry(ReplicationState state) {
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap wrapperMap = repos.getFederatedQueryWrapperMap();
        FederatedRequestProcessor aWrapper = (FederatedRequestProcessor) wrapperMap.get(state.getId());
        //FIXME for testing and might leave in for safety
        if(aWrapper == null) {
            return;
        }
        aWrapper.processQueryResponse(state);
    }
    
    public static ReplicationState getEntry(String id) {
        return getEntry(id, 1000L);
    }
    
    public static ReplicationState getEntry(String id, long waitTime) {       
        ReplicationState result = null;
        ReplicationResponseRepository repos = getInstance();
        ConcurrentHashMap reposMap = repos.getMap();
        ReplicationResponseRepositoryEntry entry 
            = (ReplicationResponseRepositoryEntry) reposMap.get(id);
        if(entry == null) {
            return result;
        }
        LinkedBlockingQueue aQueue = entry.getQueue();
        //FIXME remove next line after testing
        //LinkedBlockingQueue aQueue = (LinkedBlockingQueue) reposMap.get(id);

        //block and wait for result until timeout
        try {
            result = (ReplicationState)aQueue.poll(waitTime, TimeUnit.MILLISECONDS);
            /* for test only
            if(result != null) {
                System.out.println("ack received for: " + result.getId());
            }
             */
        } catch (InterruptedException ex) {
            System.out.println("ReplicationResponseRepository>>getEntry timed out");
            ex.printStackTrace();
        } finally {
            //clear entry from map
            reposMap.remove(id);
            //put queue back in _queueForQueues
            //System.out.println("getEntry: aQueue=" + aQueue + " id=" + id + " repos=" + repos + " repos.getQueueForQueues() =" +repos.getQueueForQueues());
            if(aQueue != null) {
                aQueue.clear();
                repos.getQueueForQueues().offer(aQueue);
            }
        }
        /*
        if (result == null) {
            System.out.println("ReplicationResponseRepository>>getEntry timed out returning empty");
        }
         */
        return result;
    }    
    
    private LinkedBlockingQueue getQueue() {
        LinkedBlockingQueue result = null;       
        result = (LinkedBlockingQueue)_queueForQueues.poll();
        if (result == null) {
            //if none in _queueForQueues, create new queue
            result = getNewQueue();
        }
        return result;
    }
    
    private LinkedBlockingQueue getNewQueue() {
        return new LinkedBlockingQueue();
    }    
    
    private ConcurrentHashMap getMap() {
        return _repository;
    }
    
    private ConcurrentHashMap getFederatedQueryWrapperMap() {
        return _federatedQueryRepository;
    }    
    
    private ConcurrentLinkedQueue getQueueForQueues() {
        return _queueForQueues;
    }    
    
    /**  
     *  Return the singleton instance of ReplicationResponseRepository
     */
    public static ReplicationResponseRepository getInstance() {
        return _soleInstance;
    }
    
    /**
     * The background thread that cleans up 
     * unused ReplicationResponseRepositoryEntries
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            doCleanup();
        }
    }
    
    /**
     * find and remove stale ReplicationResponseRepositoryEntries
     */
    protected void doCleanup() {
        ArrayList idsToRemove = new ArrayList(100);
        ConcurrentHashMap myMap = this.getRepository();
        Set entries = myMap.entrySet();
        //System.out.println("doCleanup:entries count = " + entries.size());
        Iterator it = entries.iterator();
        while(it.hasNext()) {
            Map.Entry nextEntry = (Map.Entry)it.next();
            Object nextValue = (Object) nextEntry.getValue();
            if(nextValue instanceof ReplicationResponseRepositoryEntry) {
                ReplicationResponseRepositoryEntry nextReposEntry
                    = (ReplicationResponseRepositoryEntry)nextValue;
                if(nextReposEntry.mayBeRemoved()) {
                    //System.out.println("next id to remove is: " + nextEntry.getKey());
                    nextReposEntry.cleanAndReturn(getQueueForQueues());
                    idsToRemove.add(nextEntry.getKey());
                }
            }
        }
        //System.out.println("removing " + idsToRemove.size() + " ReplicationResponseRepositoryEntries");
        for(int i=0; i<idsToRemove.size(); i++) {
            myMap.remove((String)idsToRemove.get(i));
        }
        
    } 
    
    /**
     * Sleep for the duration specified by the <code>_sleepIntervalSeconds</code>
     * property.
     */
    protected void threadSleep() {
        try {
            Thread.sleep(_sleepIntervalSeconds * 1000L);            
        } catch (InterruptedException e) {
            ;
        }
    }
    
    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     */
    public void start() {
        if(started) {
            return;
        }        
        // Start the background cleanup thread
        threadStart();
        started = true;
    }
    
    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     */
    public void stop() {
        if(!started) {
            return;
        }       
        threadStop();
        started = false;
    }        
    
    /**
     * Start the background thread that will periodically check
     * the health of replication.
     */
    protected void threadStart() {
        if (thread != null)
            return;

        threadDone = false;
        thread = new Thread(this, getThreadName());
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Stop the background thread that is periodically doing cleanup
     */
    protected void threadStop() {
        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }

        thread = null;
    }     
    
    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(_threadName);
    }     
    
    /**
     * The background thread.
     */
    protected Thread thread = null;
    
    /**
     * The background thread completion semaphore.
     */
    protected volatile boolean threadDone = false;     
    
    /**
     * Name to register for the background thread.
     */
    protected String _threadName = "ReplicationResponseRepository";
    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;     
    
    private ConcurrentHashMap _repository = null;
    private ConcurrentLinkedQueue _queueForQueues = null;
    private ConcurrentHashMap _federatedQueryRepository = null;
    
}
