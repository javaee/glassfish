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
 * ReplicationWebEventPersistentManager.java
 *
 * Created on November 18, 2005, 3:38 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.lang.reflect.InvocationTargetException;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Session;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.session.StandardSession;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

//START OF 6364900
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
//END OF 6364900

import com.sun.appserv.util.cache.BaseCache;
import com.sun.appserv.ha.spi.*;

/**
 *
 * @author Larry White
 */
public class ReplicationWebEventPersistentManager extends ReplicationWebEventPersistentManagerBase 
        implements WebEventPersistentManager, ReplicationManager {

    protected static int _maxBaseCacheSize = 4096;
    protected static float _loadFactor = 0.75f;
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "ReplicationWebEventPersistentManager/1.0";


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static final String name = "ReplicationWebEventPersistentManager";    


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }   
    
    /** Creates a new instance of ReplicationWebEventPersistentManager */
    public ReplicationWebEventPersistentManager() {
        super();        
    }
    
    /**
    * called from valve; does the save of session
    *
    * @param session 
    *   The session to store
    */    
    public void doValveSave(Session session) {
        //System.out.println("my replicator: " + this.getBackingStore());
        //temp remove after testing
        //long tempStartTime = System.currentTimeMillis();
        long startTime = 0L;
        if(isMonitoringEnabled()) {
            startTime = System.currentTimeMillis();
        }
        StorePool storePool = this.getStorePool();
        HAStorePoolElement repStore = null;

        try
        {                        
            repStore = (HAStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("GOT ReplicationStore from pool");
            }
            try
            {
                repStore.setManager(this);
                //_logger.finest("ReplicationStore has manager = " + this);
                //_logger.finest("ENTERING repStore.valveSave");
                repStore.valveSave(session);
                //get the main store instance FIXME don't need this cache remove
                //after testing
                //ReplicationStore backgroundStore = (ReplicationStore) this.getStore();
                //update this one's cache
                //backgroundStore.getSessions().put(session.getIdInternal(), session);
                //_logger.finest("FINISHED repStore.valveSave");
            } catch (Exception ex) {
                //FIXME handle exception from valveSave
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                repStore.setManager(null);
                if(repStore != null) {
                    try
                    {
                        storePool.put((StorePoolElement)repStore);
    //temp code remove after test                    
    //long tempEndTime = System.currentTimeMillis();
    //System.out.println("VALVE_TIME MILLIS = " + (tempEndTime - tempStartTime));                        
                        //_logger.finest("PUT ReplicationStore into pool");
                        if(isMonitoringEnabled()) {
                            long endTime = System.currentTimeMillis();
                            long elapsedTime = (endTime - startTime);
                            if(_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("VALVE_TIME MILLIS = " + elapsedTime);
                            }
                            WebModuleStatistics stats = this.getWebModuleStatistics();
                            stats.processValveSave(elapsedTime);
                            if(_logger.isLoggable(Level.FINEST)) {
                                _logger.finest("VALVE_TIME MILLIS = " + (endTime - startTime));
                            }
                        }
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        }
        //FIXME remove after testing
        //this is here just to test drive doRemove method
        //doRemove(session.getIdInternal());
    }
   
    /** insure that the store has a live cached connection */
    protected void insureValidConnection() throws java.io.IOException {
        /* FIXME
        HAStore store = (HAStore) getStore();
        store.getConnectionValidated(false);
         */
    }
    
    //START OF 6364900
    public void postRequestDispatcherProcess(ServletRequest request, ServletResponse response) {
        Context context = (Context)this.getContainer();
        Session sess = this.getSession(request);
        
        if(sess != null) {         
            doValveSave(sess);            
        }
        return;
    }
    
    private Session getSession(ServletRequest request) {
        javax.servlet.http.HttpServletRequest httpReq = 
            (javax.servlet.http.HttpServletRequest) request;
        javax.servlet.http.HttpSession httpSess = httpReq.getSession(false);
        if(httpSess == null) {
            return null;
        }
        String id = httpSess.getId();
        Session sess = null;
        try {
            sess = this.findSession(id);
        } catch (java.io.IOException ex) {}

        return sess;
    } 
    //END OF 6364900 
    
    //new code start    
    
   /**
   * does the remove of session
   *
   * @param sessionId 
   *   The session id to remove
   */
    /* FIXME temp going back to normal non-batched remove
    protected void doRemove(String sessionId) {
        //System.out.println("ReplicationWebEventPersistentManager>>doRemove");
        //rLock.lock();
        try {
            removedKeysMap.put(sessionId, sessionId);
            int requestCount = requestCounter.incrementAndGet();
            if ((( (Math.abs(requestCount)) % NUMBER_OF_REQUESTS_BEFORE_FLUSH) == 0)) {
                boolean wakeupDispatcher = timeToChange.compareAndSet(false, true); //expect false  set  to true
                if (wakeupDispatcher) {
                    dispatchThread.wakeup();
                }
            }
        } finally {
            //rLock.unlock();
        }
    }
     */
    
    //Called by the dispatcher
    void flushAllIdsFromCurrentMap(boolean waitForAck) {
        Map oldKeysMap = null;
        try {
            oldKeysMap = removedKeysMap;
            removedKeysMap = new ConcurrentHashMap<String, String>();
            timeToChange.set(false);
        } finally {
        }

        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine(">>ReplicationWebEventPersistentManager::flushAllIds: " + oldKeysMap.size());
        }         
        //_logger.log(Level.INFO, ">>ReplicationWebEventPersistentManager::flushAllIds: " + oldKeysMap.size());
        
        //Send sessions in currentMap into a message
        List<String> list = new ArrayList<String>(oldKeysMap.size()+1);
        Iterator<String> iter = oldKeysMap.keySet().iterator();
        int totalMessageSize = 0;
        while (iter.hasNext()) {
            String nextString = iter.next();
            list.add(nextString);
        }        
        if (list.size() > 0) {
            createRemoveIdsMessageAndSend(list, waitForAck, null);
        }
    }
    
    private void createRemoveIdsMessageAndSend(List<String> list, boolean waitForAck, Object signalObject) {
        byte[] data = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(list);
            oos.flush();
        } catch (IOException ioEx) {
            //FIXME
        } finally {
            if (oos != null) {
                try {
                    oos.flush(); oos.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
            
            if (bos != null) {
                try {
                    bos.flush();
                    data = bos.toByteArray();
                    bos.close();
                } catch (Exception ex) {
                    //FIXME
                }
            }
        }

        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("<<ReplicationWebEventPersistentManager::createRemoveIdsMessageAndSend: About to send " + data.length + " bytes...");
        }                
        removeIds(_messageIDCounter++, list.size(), data, signalObject);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("<<ReplicationWebEventPersistentManager::createRemoveIdsMessageAndSend: DONE!!");
        }
    }
    
   /**
   * does the remove of all session ids in removedIdsData
   *
   * @param msgID message id for this remove all message
   * @param removedIdsData serialized list of ids to remove
   */    
    protected void removeIds(long msgID, int totalStates, byte[] removedIdsData, Object signalObject) {
        
        long startTime = 0L;
        StorePool storePool = this.getStorePool();
        HAStorePoolElement haStore = null;

        try
        {                        
            haStore = (HAStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.fine("GOT HAStore from pool");
            }
            try
            {
                haStore.setManager(this);
                //_logger.fine("HAStore has manager = " + this);
                //_logger.fine("ENTERING haStore.removeAll");
                ((ReplicationStore)haStore).removeIds(msgID, removedIdsData);              
                //_logger.fine("FINISHED haStore.removeAll");
            } catch (Exception ex) {
                //FIXME handle exception from remove
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                haStore.setManager(null);
                if(haStore != null) {
                    try
                    {
                        storePool.put((StorePoolElement)haStore);
                        //_logger.fine("PUT HAStore into pool");
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }    
    
    private static int NUMBER_OF_REQUESTS_BEFORE_FLUSH = 1000;
    volatile Map<String, String> removedKeysMap = new ConcurrentHashMap<String, String>();
    private static AtomicInteger requestCounter = new AtomicInteger(0);
    private static int _messageIDCounter = 0;
    private AtomicBoolean  timeToChange = new AtomicBoolean(false);
    private DispatchThread dispatchThread = new DispatchThread();    
    
    private class DispatchThread implements Runnable {
        
        private volatile boolean done = false;
       
        private Thread thread;
        
        private LinkedBlockingQueue<Object> queue;
        
        public DispatchThread() {
            this.queue = new LinkedBlockingQueue<Object>();
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            thread.start();
        }
        
        public void wakeup() {
            queue.add(new Object());
        }
        
        public void run() {
            while (! done) {
                try {
                    Object ignorableToken = queue.take();
                    flushAllIdsFromCurrentMap(false);
                } catch (InterruptedException inEx) {
                    this.done = true;
                }
            }
        }

    }
    
    //new code end 
    
}   
