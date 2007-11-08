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
 * ReplicationManagerBase.java
 *
 * Created on October 31, 2006, 10:55 AM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.lang.reflect.InvocationTargetException;

import java.io.IOException;
import com.sun.appserv.ha.spi.*;
import com.sun.appserv.util.cache.BaseCache;
import com.sun.enterprise.web.MonitorStatsCapable;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.ShutdownCleanupCapable;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.Session;
import org.apache.catalina.LifecycleException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.ee.web.initialization.ServerConfigReader;

/**
 *
 * @author Larry White
 */
public abstract class ReplicationManagerBase extends PersistentManagerBase 
        implements MonitorStatsCapable{
    
    public final static String LOGGER_MEM_REP 
        = ReplicationState.LOGGER_MEM_REP;
    
    private final static Level TRACE_LEVEL = Level.FINE;
    
    final static String DUPLICATE_IDS_SEMANTICS_PROPERTY 
        = ReplicationState.DUPLICATE_IDS_SEMANTICS_PROPERTY;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    //protected static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);    
    
    protected static int _maxBaseCacheSize = 4096;
    protected static float _loadFactor = 0.75f;    
    
    static
	{
            checkSessionCacheProperties();
	}     

    protected static boolean checkSessionCacheProperties() {
        boolean result = false;
	try
        {
            Properties props = System.getProperties();
            String cacheSize=props.getProperty("HTTP_SESSION_CACHE_MAX_BASE_CACHE_SIZE");
            if(null!=cacheSize) {
                _maxBaseCacheSize = (new Integer (cacheSize).intValue());
            }  
            String loadFactor=props.getProperty("HTTP_SESSION_CACHE_MAX_BASE_LOAD_FACTOR");
            if(null!=loadFactor) {
                _loadFactor = (new Float (loadFactor).floatValue());
            }
            /*
            System.out.println("_maxBaseCacheSize=" + _maxBaseCacheSize);
            System.out.println("_loadFactor=" + _loadFactor);
             */
        } catch(Exception e)
        {
            //do nothing accept defaults
        }
        return result;
    }    
    
    protected final static String MODE_WEB = ReplicationState.MODE_WEB;
    
    /** Creates a new instance of ReplicationManagerBase */
    public ReplicationManagerBase() {
        super();
        //initialize replicated sessions cache
        replicatedSessions = new BaseCache();
        replicatedSessions.init(_maxBaseCacheSize, _loadFactor, null);
        replicatedSessionUpdates = new BaseCache();
        replicatedSessionUpdates.init(_maxBaseCacheSize, _loadFactor, null);         
    }
    
    /**
     * Clear all sessions from the manager
     */
    public void clearSessions() {
        //un-register this app from ReplicationMessageRouter
        System.out.println("ReplicationManagerBase>>clearSessions:getApplicationId()" + getApplicationId());
        ReplicationMessageRouter.createInstance().removeReplicationManager(getApplicationId());
    }
    
    /**
     * Clear all sessions from the Store.
     * in this case a no-op
     */
    public void clearStore() {
        //deliberate no-op
        return;
    }   
    
    /**
    * Our cache of replicated ReplicationState objects
    * keyed by id
    */
    protected BaseCache replicatedSessions = null;
    
    /**
    * Our cache of replicated ReplicationStateUpdate objects
    * keyed by id
    */
    protected BaseCache replicatedSessionUpdates = null;    

    /**
    * get the replicated sessions cache
    */ 
    public BaseCache getReplicatedSessions() {
        return replicatedSessions;
    }
    
    void printReplicatedSessionIds() {
        Iterator it = replicatedSessions.keys();
        while(it.hasNext()) {
            String nextId = (String) it.next();
            System.out.println("nextSessionId = " + nextId);
        }
    }
  
    /**
    * set the replicated sessions cache
    * @param sesstable
    */ 
    public void setReplicatedSessions(BaseCache sesstable) {
        replicatedSessions = sesstable;
    }

    /**
     * Put session State in replica cache
     * @param sessionState
     */    
    protected synchronized void putInReplicationCache(ReplicationState sessionState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>putInReplicationCache id: " + sessionState.getId());
        } 
        String id = (String)sessionState.getId();
        if(id == null) {
            return;
        }
        
        ReplicationState currentState = (ReplicationState)replicatedSessions.get(id);
        if(_logger.isLoggable(Level.FINE)) {
            if(currentState != null) {
                _logger.fine("currentVersion: " + currentState.getVersion() + " newVersion: " + sessionState.getVersion());
            }
        }
        if((currentState != null) && currentState.getVersion() > sessionState.getVersion()) {
            return;
        }
        replicatedSessions.put(sessionState.getId(), sessionState);
        if(_logger.isLoggable(TRACE_LEVEL)) {
            _logger.log(TRACE_LEVEL, "in " + this.getClass().getName() + ">>putInReplicationCache complete id: " + sessionState.getId() + "[ver:" + sessionState.getVersion() + "]");
        }        
    }
    
    /**
     * Put session State in replica cache
     * @param sessionState
     */    
    protected synchronized void putInReplicationUpdateCache(ReplicationStateUpdate sessionStateUpdate) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>putInReplicationUpdateCache id: " + sessionStateUpdate.getId());
        }
        if(sessionStateUpdate == null) {
            return;
        }
        String id = (String)sessionStateUpdate.getId();
        if(id == null) {
            return;
        }
        
        ReplicationStateUpdate currentStateUpdate = this.getFromReplicationUpdatesCache(id);
        if(_logger.isLoggable(Level.FINE)) {
            if(currentStateUpdate != null) {
                _logger.fine("currentVersion: " + currentStateUpdate.getVersion() + " newVersion: " + sessionStateUpdate.getVersion());
            }
        }
        if((currentStateUpdate != null) && currentStateUpdate.getVersion() > sessionStateUpdate.getVersion()) {
            return;
        }
        replicatedSessionUpdates.put(sessionStateUpdate.getId(), sessionStateUpdate);
    }    

    /**
     * get session State from replica cache based on the id
     * @param id
     */    
    protected ReplicationState getFromReplicationCache(String id) {
        ReplicationState returnState 
            = (ReplicationState)replicatedSessions.get(id);
        if(returnState != null) {
            ReplicationStateUpdate returnStateUpdate
                = this.getFromReplicationUpdatesCache(id);
            //apply update if it exists and has higher version
            if(returnStateUpdate != null) {
                if(returnStateUpdate.getVersion() > returnState.getVersion()) {
                    returnState.setVersion(returnStateUpdate.getVersion());
                    returnState.setLastAccess(returnStateUpdate.getLastAccess());
                }
                this.removeFromReplicationUpdateCache(returnStateUpdate.getId());
            }            
        }
        return returnState;
    }
    
    /**
     * get session State from replica cache based on the id
     * @param id
     */    
    protected ReplicationStateUpdate getFromReplicationUpdatesCache(String id) {    
        return (ReplicationStateUpdate)replicatedSessionUpdates.get(id);
    }    

    /**
     * remove session State from replica cache based on the id of sessionState
     * @param sessionState
     */    
    protected void removeFromReplicationCache(ReplicationState sessionState) {
        if(sessionState == null) {
            return;
        }
        replicatedSessions.remove(sessionState.getId());
        removeFromReplicationUpdateCache((String)sessionState.getId());
    }
    
    /**
     * remove session State update from replica cache based on the id of sessionStateUpdate
     * @param sessionStateUpdate
     */    
    protected void removeFromReplicationUpdateCache(String id) {
        if(id == null) {
            return;
        }
        replicatedSessionUpdates.remove(id);
    }    

    /**
     * remove session State from replica cache based on the id and return it
     * @param id
     */     
    protected synchronized ReplicationState transferFromReplicationCache(String id) { 
        ReplicationState result = this.getFromReplicationCache(id);
        removeFromReplicationCache(result);
        return result;
    }     
    
    /**
    * Our Replicator instance (for SimpleMetadata)
    */
    protected BackingStore backingStore = null;
    
    /**
    * Our Replicator instance (for CompositeMetadata)
    */
    protected BackingStore compositeBackingStore = null;    
    
    /**
    * get the backingStore
    */ 
    public BackingStore getBackingStore() {
        if(backingStore == null) {
            this.createBackingStore();
        }
        return backingStore;
    }
    
    /**
    * get the backingStore (for CompositeMetadata)
    */ 
    public BackingStore getCompositeBackingStore() {
        if(compositeBackingStore == null) {
            this.createCompositeBackingStore();
        }
        return compositeBackingStore;
    }    
    
    /**
    * set the backing store
    * @param aBackingStore
    */ 
    public void setBackingStore(BackingStore aBackingStore) {
        backingStore = aBackingStore;
    } 
    
    /**
    * set the backing store for CompositeMetadata
    * @param aBackingStore
    */ 
    public void setCompositeBackingStore(BackingStore aBackingStore) {
        compositeBackingStore = aBackingStore;
    }     

    /**
    * create and set the backing store
    */     
    void createBackingStore() {
        //BackingStoreFactory testStoreFactory = getBackingStoreFactory();
        //System.out.println("testStoreFactory:" + testStoreFactory);
        BackingStoreFactory storeFactory = new JxtaBackingStoreFactory();
        //System.out.println("storeFactory: " + storeFactory);
        BackingStoreRegistry backingStoreRegistry 
            = BackingStoreRegistry.getInstance();
        Properties env 
            = backingStoreRegistry.getFactoryClassEnv(getPassedInPersistenceType());
        //does this manager & backing store support duplicate id semantics
        //for batch replication usage
        env.put(DUPLICATE_IDS_SEMANTICS_PROPERTY, Boolean.valueOf(this.isDuplicateIdsSemanticsAllowed()));
        BackingStore backingStore = null;
        try {
            backingStore = storeFactory.createBackingStore(
                        SimpleMetadata.class,     //type
                        this.getApplicationId(), //appid
                        env);
        } catch (BackingStoreException ex) {
            //deliberate no-op
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("backingStore: " + backingStore);
        }         
        if(backingStore != null) {
            if(backingStore instanceof JxtaBackingStoreImpl) {
                ((JxtaBackingStoreImpl)backingStore).setMode(MODE_WEB);
            }        
            this.setBackingStore(backingStore);
        }
    } 

    /**
    * create and set the backing store for CompositeMetadata
    */     
    void createCompositeBackingStore() {
        //BackingStoreFactory testStoreFactory = getBackingStoreFactory();
        //System.out.println("testStoreFactory:" + testStoreFactory);
        BackingStoreFactory storeFactory = new JxtaBackingStoreFactory();
        //System.out.println("storeFactory: " + storeFactory);
        BackingStoreRegistry backingStoreRegistry 
            = BackingStoreRegistry.getInstance();
        Properties env 
            = backingStoreRegistry.getFactoryClassEnv(getPassedInPersistenceType());
        env.put(DUPLICATE_IDS_SEMANTICS_PROPERTY, Boolean.valueOf(this.isDuplicateIdsSemanticsAllowed()));
        BackingStore compositeBackingStore = null;
        try {
            compositeBackingStore = storeFactory.createBackingStore(
                        CompositeMetadata.class,     //type
                        this.getApplicationId(), //appid
                        env);
        } catch (BackingStoreException ex) {
            //deliberate no-op
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("compositeBackingStore: " + compositeBackingStore);
        }         
        if(compositeBackingStore != null) {
            if(compositeBackingStore instanceof JxtaBackingStoreImpl) {
                ((JxtaBackingStoreImpl)compositeBackingStore).setMode(MODE_WEB);
            }        
            this.setCompositeBackingStore(compositeBackingStore);
        }
    }     

    /**
    * return the backing store factory
    */     
    protected BackingStoreFactory getBackingStoreFactory() {
        BackingStoreRegistry backingStoreRegistry
            = BackingStoreRegistry.getInstance();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("getBackingStoreFactory:passedInPersistenceType=" + getPassedInPersistenceType());
        }         
        String factoryClassName 
            = backingStoreRegistry.getFactoryClassName(this.getPassedInPersistenceType());
        return getBackingStoreFactoryFromName(factoryClassName);
    }

    /**
    * helper method to return the backing store factory based on className
    * @param className
    */    
    private BackingStoreFactory getBackingStoreFactoryFromName(String className) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("getBackingStoreFactoryFromName:className: " + className);
        }        
        BackingStoreFactory backingStoreFactory = null;
        try {
            backingStoreFactory = 
                (BackingStoreFactory) (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            //FIXME - throw exception?
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("unable to create backing store factory");
            }            
        } 
        return backingStoreFactory;
    }    
    
    /** create and return a new session; delegates to session factory */
    protected Session createNewSession() {
        Session sess = getSessionFactory().createSession(this);
        return sess;
    }
    
    /**
     * Get new session class (new method in ManagerBase that we over-ride
     * instead of createNewSession()
     */
    public Session createEmptySession() {       
        Session sess = getSessionFactory().createSession(this);
        return sess;
    }
    
    /**
    * get the application id
    */         
    public String getApplicationId() {
        HAStoreBase store = (HAStoreBase)getStore();
        return store.getApplicationId();
    } 

    /**
    * get the server instance name
    */    
    public String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }
    
    /** subclasses will over-ride this method */
    public String getMonitorAttributeValues() {
        return "testing..1..2..3";
    }
    
    public int getSessionsCacheSize() {
        return sessions.size();
    }    
    
    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     * This method checks the persistence store if persistence is enabled,
     * otherwise just uses the functionality from ManagerBase.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id) throws IOException {
        //System.out.println("ReplicationManagerBase>>findSession:id:" + id + " isValid:" + this.isSessionIdValid(id));
        //Thread.dumpStack();
        if(!this.isSessionIdValid(id)) {
            return null;
        }        
        WebModuleStatistics stats = this.getWebModuleStatistics();
        Session session = findSessionFromCacheOnly(id);
        //System.out.println("ReplicationManagerBase>>findSession:id:" + id + " cachedSession:" + session);
        if (session != null) {
            if(isMonitoringEnabled()) {
                stats.processCacheHit(true);
            }
            return (session);
        }

        // See if the Session is in the Store
        if(isMonitoringEnabled()) {
            stats.processCacheHit(false);
        }
        session = swapIn(id);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN ReplicationManagerBase>>findSession: returned sess = " + (BaseHASession)session);        
        }
        return (session);

    }
    
    /**
     * This method is deliberately over-riding this same method
     * in PersistentManagerBase.  It is ignoring the removeCachedCopy
     * parameter.  (see org.apache.catalina.session.PersistentManagerBase
     * for more details on this method).
     *
     * @param id The session id for the session to be returned
     * @param removeCachedCopy
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id, boolean removeCachedCopy) throws IOException {
        //System.out.println("ReplicationManagerBase>>findSession:id:" + id + " isValid:" + this.isSessionIdValid(id));
        //Thread.dumpStack();
        if(!this.isSessionIdValid(id)) {
            return null;
        }        
        Session theSession = this.findSession(id);   
        return theSession;
    }     
    
    /**
     * This method is a clone of the ManagerBase>>findSession.
     * It is used here to avoid problems with calling super.findSession
     * from this class.
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */  
    public Session findSessionFromCacheOnly(String id) throws IOException {

        if (id == null)
            return (null);
        synchronized (sessions) {
            Session session = (Session) sessions.get(id);
            return (session);
        }

    }
    
    public void repair(long repairStartTime) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationManager>>repair");                       
        }         
        //System.out.println("Replicationanager>>repair");
        /*
        if (!started)
            return;
         */
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }

        Session sessions[] = findSessions();

        for (int i = 0; i < sessions.length; i++) {
            StandardSession session = (StandardSession) sessions[i];            
            if(session.getIsValid()
                && (session.getIdInternal() != null)
                && !session.hasExpired() 
                && isSessionOlderThan(session, repairStartTime)) {           
                if(session.lockBackground()) { 
                    try {
                        ((HASession)session).setPersistent(false);
                        ((HASession)session).setDirty(true);
                        doValveSave(session);
                    } finally {
                        session.unlockBackground();
                    }
                }                                
	    }            
        }
    }        
    
    public void repair(long repairStartTime, boolean checkForStopping) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationManagerBase>>repair: checkForStopping: " + checkForStopping);
        }         
        //System.out.println("ReplicationManagerBase>>repair: checkForStopping: " + checkForStopping);
        /*
        if (!started)
            return;
         */
        if(checkForStopping && ReplicationHealthChecker.isStopping()) {
            return;
        }

        Session sessions[] = findSessions();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationManagerBase>>repair sessions size = " + sessions.length);
        }         
        //System.out.println("ReplicationManagerBase>>repair sessions size = " + sessions.length);
        for (int i = 0; i < sessions.length; i++) {
            StandardSession session = (StandardSession) sessions[i];
            //System.out.println("ReplicationManagerBase>>repair session isValid = " + session.getIsValid());
            //System.out.println("ReplicationManagerBase>>repair session id = " + session.getIdInternal());
            //System.out.println("ReplicationManagerBase>>repair session hasExpired = " + session.hasExpired());
            boolean condition = true;
            if(checkForStopping) {
                condition = (session.getIsValid()
                && (session.getIdInternal() != null)
                && !session.hasExpired() 
                && isSessionOlderThan(session, repairStartTime));
            } else {
                //during flush ignore age check
                condition = (session.getIsValid()
                && (session.getIdInternal() != null)
                && !session.hasExpired() );                
            }
            //System.out.println("ReplicationManagerBase>>repair condition = " + condition);            
            if(condition) {          
                if(session.lockBackground()) { 
                    try {
                        //System.out.println("ReplicationManagerBase>>repair: about to do doValveSave");
                        ((HASession)session).setPersistent(false);
                        ((HASession)session).setDirty(true);
                        doValveSave(session);
                    } catch(Throwable t) {
                        System.out.println("Throwable during force flush");
                        //t.printStackTrace();
                        break;
                    } finally {
                        session.unlockBackground();
                    }
                }                                
	    }            
        }
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
    }    
    
   //SJSAS 6406580 START
    
   /**
   * does the remove of session
   *
   * @param sessionId 
   *   The session id to remove
   */    
    protected void doRemove(String sessionId) {
        
        long startTime = 0L;
        StorePool storePool = this.getStorePool();
        HAStorePoolElement haStore = null;

        try
        {                        
            haStore = (HAStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("GOT HAStore from pool");
            }
            try
            {
                haStore.setManager(this);
                //_logger.fine("HAStore has manager = " + this);
                //_logger.fine("ENTERING haStore.doRemove");
                ((HAStoreBase)haStore).doRemove(sessionId);              
                //_logger.fine("FINISHED haStore.doRemove");
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
    //SJSAS 6406580 END

   /**
   * remove sessions in the array of sessionIds from manager cache
   *
   * @param sessionIds 
   *   The session ids to remove
   */     
    protected void removeSessionIdsFromManagerCache(String[] sessionIds) {
        synchronized(sessions) {
            for(int i=0; i<sessionIds.length; i++) {
                String nextId = sessionIds[i];
                // Take it out of the cache - remove does not handle nulls
                if(nextId != null) {
                    sessions.remove(nextId);
                }
            }
        }
    }    

   /**
   * remove sessions from manager cache
   *
   * @param session 
   *   The session to remove
   */    
    public void removeSessionFromManagerCache(Session session) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>removeSessionFromManagerCache:session = " + session);
        }        
        if(session == null) {
            return;
        }
	synchronized (sessions) {
            sessions.remove(session.getIdInternal());
        }
    } 
    
    //begin processing methods
    
    public void processSave(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processSave");
        }        
        this.putInReplicationCache(message);
    } 
    
    public ReplicationState processSize(ReplicationState message) {
        int result = replicatedSessions.getEntryCount();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processSize: entryCount=" + result);
        }         
        ReplicationState resultState 
            = ReplicationState.createQueryStateResponse(MODE_WEB, message.getAppId(), message.getAppId(), message.getInstanceName(), Integer.valueOf(result));
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processSize:resultState=" + resultState);
        }        
        return resultState;
    }    
    
    public void processValvesave(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processValvesave");
            _logger.fine("processValvesave:id:" + message.getId());
            _logger.fine("processValvesave:version:" + message.getVersion());
        }
        //System.out.println("IN" + this.getClass().getName() + ">>processValvesave");
        //System.out.println("processValvesave:version:" + message.getVersion());        
        this.putInReplicationCache(message);
        //System.out.println("after processValvesave: cache dump");
        //printReplicatedSessionIds();
    } 
    
    public void processCompositesave(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processCompositesave");
            _logger.fine("processCompositesave:version:" + message.getVersion());
        } 
        //System.out.println("IN" + this.getClass().getName() + ">>processCompositesave");
        //System.out.println("processCompositesave:version:" + message.getVersion());        
        ReplicationState currentState 
            = this.getFromReplicationCache((String)message.getId());
        //System.out.println("processCompositesave:made it this far");
        //if we do not have it in cache, just put it in
        if(currentState == null) {
            this.putInReplicationCache(message);
            return;
        }
        //otherwise update with deltas
        ReplicationState updatedState = this.updateReplicationState(currentState, message);
        this.putInReplicationCache(updatedState);
    } 
    
    private ReplicationState updateReplicationState(ReplicationState currentState, ReplicationState newState) {
        CompositeMetadata currentComposite 
            = ReplicationState.createCompositeMetadataFrom(currentState);
        //System.out.println("updateReplicationState:current entries: " + currentComposite.getEntries().size());
        CompositeMetadata newComposite 
            = ReplicationState.createCompositeMetadataFrom(newState);
        //System.out.println("updateReplicationState:new entries: " + newComposite.getEntries().size());
        CompositeMetadata resultComposite 
            = this.applyCompositeMetadataDeltas(currentComposite, newComposite);
        //System.out.println("updateReplicationState:result entries: " + resultComposite.getEntries().size());        
        byte[] resultAttributeState = null;
        try {            
            resultAttributeState 
                = ReplicationState.getByteArrayFromCollection(resultComposite.getEntries());
            /*
            resultAttributeState 
                = ReplicationState.getByteArray(resultComposite.getEntries());
             */
        } catch (IOException ex) {}
        ReplicationState updatedState 
            = ReplicationState.createUpdatedStateFrom(newState, resultAttributeState);
        return updatedState;
    }
    
    private CompositeMetadata applyCompositeMetadataDeltas(CompositeMetadata current, CompositeMetadata deltas) {
        Collection<AttributeMetadata> currentCollection = current.getEntries();
        Collection<AttributeMetadata> deltasCollection = deltas.getEntries();
        Iterator deltasIterator = deltasCollection.iterator();
        while(deltasIterator.hasNext()) {
            AttributeMetadata nextAttributeMetadata 
                = (AttributeMetadata)deltasIterator.next();
            this.applyCompositeMetadataDelta(nextAttributeMetadata, currentCollection);
        }
        return current;
    } 
    
    private void applyCompositeMetadataDelta(AttributeMetadata nextAttributeMetadata, Collection<AttributeMetadata> currentCollection) {
        //System.out.println("in applyCompositeMetadataDelta nextAttributeMetadata = " + nextAttributeMetadata);       
        switch(nextAttributeMetadata.getOperation()) {
            case DELETE: currentCollection.remove(nextAttributeMetadata); 
            case ADD: currentCollection.add(nextAttributeMetadata); 
            case UPDATE: 
                if(currentCollection.contains(nextAttributeMetadata)) {
                    currentCollection.remove(nextAttributeMetadata);
                }
                currentCollection.add(nextAttributeMetadata);
                break;
        }
    }
    
    public void processUpdatelastaccesstime(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processUpdatelastaccesstime");
            _logger.fine("processUpdatelastaccesstime:version:" + message.getVersion());
        }
        String id = (String)message.getId();
        ReplicationState state = this.getFromReplicationCache(id);
        if(state != null) {
            state.setLastAccess(message.getLastAccess());
            state.setVersion(message.getVersion());
            this.putInReplicationCache(state);
        } else {
            if(_logger.isLoggable(Level.FINE)) {                
                _logger.fine("processUpdatelastaccesstime: attempting to update a session not yet stored:id:" + message.getId());
            }
            this.putInReplicationUpdateCache(new ReplicationStateUpdate(id, message.getVersion(), message.getLastAccess()) );
        }        
    }    
    
    public void processRemove(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processRemove");
        }         
        this.removeFromReplicationCache(message);
    }
    
    public void processRemoveids(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processRemoveids");
        }
        //System.out.println("IN" + this.getClass().getName() + ">>processRemoveids");
        //state of this message contains serialized list of ids to remove
        byte[] idsToRemoveState = message.getState();
        List removedIdsList = new ArrayList();
        try {
            removedIdsList = (List)ReplicationState.getObjectValue(idsToRemoveState);
        } catch (Exception ex) {
            //deliberately do nothing
        }
        //ReplicationState.displayStringList(removedIdsList);
        for(int i=0; i<removedIdsList.size(); i++) {
            String nextIdToRemove = (String)removedIdsList.get(i);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine(">>processRemoveids:nextIdToRemove=" + nextIdToRemove);
            }            
            //System.out.println(">>processRemoveids:nextIdToRemove=" + nextIdToRemove);
            replicatedSessions.remove(nextIdToRemove);
        }
    }    
    
    //this message sent from sso valve
    public void processAssociate(ReplicationState message) {
        //FIXME have moved this to have this processed in 
        //web container for session
        //when session is loaded from replica need to apply
        //the ssoId from replica - same with lastAccess
        //System.out.println("IN" + this.getClass().getName() + ">>processAssociate");
        ReplicationState storedReplica 
            = this.getFromReplicationCache((String)message.getId());
        //storedReplica.setSsoId(message.getSsoId()); ssoid is in extraParam
        storedReplica.setExtraParam(message.getExtraParam());
    }
    
    public void processMessage(ReplicationState message) {
        //handle broadcast methods
        if(ReplicationState.isBroadcastState(message)) {
            processBroadcastMessage(message);
            return;
        }
        
        //handle non-void methods
        ReplicationStateQueryResponse queryResult = null;
        //do process non-void message (and cannot be response either)
        if(!message.isResponseState() && !message.isVoidMethodReturnState()) {
            //do non-void processing including sending response
            queryResult = this.doProcessQueryMessage(message);
            ReplicationState qResponse = queryResult.getState();
            //System.out.println("RepMgrBase:qResponse=" + qResponse);
            if(qResponse != null) {
                //sourceInstanceName is preserved in the response
                ReplicationState response = 
                    ReplicationState.createResponseFrom(qResponse);
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("RepMgrBase:responseState=" + response);
                }                
                //System.out.println("RepMgrBase:responseState=" + response);
                this.doSendResponse(response);
            }
            return;
        }
        //end do process non-void message
        
        /*
        ReplicationState response = 
            ReplicationState.createResponseFrom(message);
         */
        //send a response before further processing only if processed 
        //msg is not itself a response and if method is a void return type
        //FIXME this send will be removed if upstream ack send works
        /* removing this for test
        if(!message.isReturnMessage() && message.isVoidMethodReturnState()) {
            this.doSendResponse(response);
        }
        */        

        boolean isResponse = this.doProcessMessage(message);
        
        //send a response only if processed msg is not itself a response
        //and if method is not void return type (in that case ack was
        //already sent)
        /*
        if(!isResponse && !message.isVoidMethodReturnState()) {
            //ReplicationState response = 
            //    ReplicationState.createResponseFrom(message);
            this.doSendResponse(response);
        }
         */
    } 
    
    /**
    * send the response
    *
    * @param sessionState 
    *   The replication state response
    */    
    public void doSendResponse(ReplicationState sessionState) {
               
        StorePool storePool = this.getStorePool();
        HAStorePoolElement repStore = null;
        //FIXME find out why this happens some times during shutdown
        if(storePool == null) {
            return;
        }

        try
        {                        
            repStore = (HAStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.fine("GOT ReplicationStore from pool");
            }
            try
            {
                repStore.setManager(this);
                //_logger.fine("ReplicationStore has manager = " + this);
                //_logger.fine("ENTERING repStore.sendResponse");
                ((ReplicationStore)repStore).sendResponse(sessionState);
                //get the main store instance FIXME don't need this cache remove
                //after testing
                //ReplicationStore backgroundStore = (ReplicationStore) this.getStore();
                //update this one's cache
                //backgroundStore.getSessions().put(session.getIdInternal(), session);
                //_logger.fine("FINISHED repStore.sendResponse");
            } catch (Exception ex) {
                //FIXME handle exception from sendResponse
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                repStore.setManager(null);
                if(repStore != null) {
                    try
                    {
                        storePool.put((StorePoolElement)repStore);
                        //_logger.fine("PUT ReplicationStore into pool");
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }    
    
    //return true if message is processResponse
    public boolean doProcessMessage(ReplicationState message) {
        boolean result = false;
        String methodName = getProcessMethodName(message);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>doProcessMessageName=" + methodName);
        }        
        //System.out.println("in " + this.getClass().getName() + ">>doProcessMessageName=" + methodName);
        try {
            Class myClass = this.getClass();
            myClass.getMethod(
                methodName,
                    new Class[]{ message.getClass() }).invoke(
                        this, new Object[]{ message });            
        } catch (IllegalAccessException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + ">>doProcessMessage:methodName=" + methodName + "illegalAccessException");
            }             
        } catch (NoSuchMethodException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + ">>doProcessMessage:methodName=" + methodName + "noSuchMethodException");
            }            
        } catch (InvocationTargetException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + ">>doProcessMessage:methodName=" + methodName + "invocationTargetException");
            }            
            System.out.println("in " + this.getClass().getName() + ">>doProcessMessage:methodName=" + methodName + "invocationTargetException");
            System.out.println("invocationException.getCause()= " + ex.getCause());
            ex.printStackTrace();
        } 
        if(methodName.equals("processResponse")) {
            result = true;
        }
        return result;
    }
    
    //return true if message is processQueryResponse
    public ReplicationStateQueryResponse doProcessQueryMessage(ReplicationState message) {
        ReplicationState resultState = null;
        String methodName = getProcessMethodName(message);
        //System.out.println("in " + this.getClass().getName() + ">>doProcessQueryMessage:methodName=" + methodName);
        //System.out.println("in " + this.getClass().getName() + ">>doProcessQueryMessage:thisInstance=" + getInstanceName() + "SASEreturnInstance=" + message.getInstanceName() );
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>doProcessQueryMessage:methodName=" + methodName);
            _logger.fine("in " + this.getClass().getName() + ">>doProcessQueryMessage:thisInstance=" + getInstanceName() + "SASEreturnInstance=" + message.getInstanceName() );
        }         
        try {
            Class myClass = this.getClass();
            resultState = (ReplicationState) myClass.getMethod(
                methodName,
                    new Class[]{ message.getClass() }).invoke(
                        this, new Object[]{ message });            
        } catch (IllegalAccessException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + ">>doProcessQueryMessage:methodName=" + methodName + "illegalAccessException");
            }                          
        } catch (NoSuchMethodException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + ">>doProcessQueryMessage:methodName=" + methodName + "noSuchMethodException");
            }              
        } catch (InvocationTargetException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + ">>doProcessQueryMessage:methodName=" + methodName + "invocationTargetException");
            }            
            System.out.println("in " + this.getClass().getName() + ">>doProcessQueryMessage:methodName=" + methodName + "invocationTargetException");
            System.out.println("invocationException.getCause()= " + ex.getCause());
            ex.printStackTrace();
        }
        boolean isResponse = methodName.equals("processBroadcastresponse");
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>doProcessQueryMessage:resultState=" + resultState);
        }        
        //System.out.println("in " + this.getClass().getName() + ">>doProcessQueryMessage:resultState=" + resultState);
        return new ReplicationStateQueryResponse(resultState, isResponse);
    }
    
    public void processBroadcastMessage(ReplicationState message) {
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseState=" + responseState);
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseStateTrunk=" + responseState.getTrunkState());
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseStateAttr=" + responseState.getState());
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseStateVer=" + responseState.getVersion());
        }
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:response=" + isResponse);
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseState=" + responseState);
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseStateTrunk=" + responseState.getTrunkState());
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseStateAttr=" + responseState.getState());
        //System.out.println("processBroadcastMessage:after doProcessQueryMessage:responseStateVer=" + responseState.getVersion());

        //don't send a response to a response
        if(!isResponse) {
            //point-to-point response back to sender
            //System.out.println("processBroadcastMessage - need to send back result");
            //doSendQueryResponse(responseState, this.getInstanceName());
            doSendQueryResponse(responseState, message.getInstanceName());
            /*
            ReplicationState response = 
                ReplicationState.createResponseFrom(message);
            this.doSendResponse(response);
             */
        }
    }
    
    public void processQueryMessage(ReplicationState message, String returnInstance) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processQueryMessage:returnInstance= " + returnInstance);
        }         
        //System.out.println("in " + this.getClass().getName() + ">>processQueryMessage:returnInstance= " + returnInstance);
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processQueryMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("processQueryMessage:after doProcessQueryMessage:responseState=" + responseState);            
        }        
        //System.out.println("processQueryMessage:after doProcessQueryMessage:response=" + isResponse);
        //System.out.println("processQueryMessage:after doProcessQueryMessage:responseState=" + responseState);
        //don't send a response to a response
        if(!isResponse && responseState != null) {
            //point-to-point response back to sender
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("processQueryMessage - need to send back result to " + returnInstance);            
            }             
            //System.out.println("processQueryMessage - need to send back result to " + returnInstance);
            doSendQueryResponse(responseState, returnInstance);
        }
    }
    
    /**
    * send the response
    *
    * @param sessionState 
    *   The replication state response
    * @param instanceName  the name of the target instance
    */    
    public void doSendQueryResponse(ReplicationState sessionState, String instanceName) {
               
        StorePool storePool = this.getStorePool();
        HAStorePoolElement repStore = null;

        try
        {                        
            repStore = (HAStorePoolElement) storePool.take();
            //repStore.setSessions(cachedStore.getSessions());
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("GOT ReplicationStore from pool");
            }
            try
            {
                repStore.setManager(this);
                //_logger.fine("ReplicationStore has manager = " + this);
                //_logger.fine("ENTERING repStore.sendResponse");
                ((ReplicationStore)repStore).sendQueryResponse(sessionState, instanceName);
                //_logger.fine("FINISHED repStore.sendResponse");
            } catch (Exception ex) {
                //FIXME handle exception from sendResponse
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                repStore.setManager(null);
                if(repStore != null) {
                    try
                    {
                        storePool.put((StorePoolElement)repStore);
                        //_logger.fine("PUT ReplicationStore into pool");
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }     
    
    private String getProcessMethodName(ReplicationState message) {
        String command = message.getCommand();
        return "process" + camelCase(command);
    }
    
    /**
     * this method strips out all non-alpha characters; camelCases the result
     *
     * @param inputString
     */     
    private String camelCase(String inputString) {
        String strippedString = stripNonAlphas(inputString);
        String firstLetter = (strippedString.substring(0, 1)).toUpperCase();
        String remainingPart = 
            (strippedString.substring(1, strippedString.length())).toLowerCase();
        return firstLetter + remainingPart;
    }
    
    /**
     * this method strips out all non-alpha characters
     *
     * @param inputString
     */     
    private String stripNonAlphas(String inputString) {
        StringBuffer sb = new StringBuffer(50);
        for(int i=0; i<inputString.length(); i++) {
            char nextChar = inputString.charAt(i);
            if(Character.isLetter(nextChar)) {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }       
    
    public void processResponse(ReplicationState message) {
        //complete processing response - not sending response to a response
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processResponse");            
        }        
        ReplicationResponseRepository.putEntry(message);
    }
    
    public ReplicationState processBroadcastresponse(ReplicationState queryResponseState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastresponse:queryResponseState=" + queryResponseState);            
        }
        //ReplicationResponseRepository.putEntry(queryResponseState);
        ReplicationResponseRepository.putFederatedEntry(queryResponseState);
        return queryResponseState;
    }
    
    void processExpiredReplicas() {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processExpiredReplicas");            
        }
        ArrayList expiredReplicas = new ArrayList(30);
        BaseCache replicasCache = this.getReplicatedSessions();
        for(Iterator it = replicasCache.values(); it.hasNext();) {
            ReplicationState nextState = (ReplicationState)it.next();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in " + this.getClass().getName() + "nextState=" + nextState);            
            }            
            if(nextState.isExpired()) {
                expiredReplicas.add(nextState);
            }
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processExpiredReplicas:expiredReplicas.size=" + expiredReplicas.size());            
        }        
        for(int i=0; i<expiredReplicas.size(); i++) {
            ReplicationState nextState = 
                (ReplicationState)expiredReplicas.get(i);
            this.removeFromReplicationCache(nextState);
        }
    }
    
    public ReplicationState processBroadcastfindsession(ReplicationState queryState) {
        //complete query and send back response
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:instance: " + getInstanceName());
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:id=" + queryState.getId());                        
        }        
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:instance: " + getInstanceName());
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:id=" + queryState.getId() + "dumping:");
        //Thread.dumpStack();
        //System.out.println("beginning processBroadcastfindsession: cache dump");
        //printReplicatedSessionIds();        
        ReplicationState replicaState 
            = findReplicatedState(queryState);
        ReplicationState returnState = null;
        if(replicaState != null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
                _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
                _logger.fine("processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());                     
            }            
            //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
            //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
            //System.out.println("processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());
            returnState = ReplicationState.createQueryResponseFrom(replicaState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());                  
            }            
            //System.out.println("processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());
            //System.out.println("processBroadcastfindsession:replicaStateResponseState:" + returnState.getState());
            //FIXME may want to wait for ack before doing this
            //FIXME waiting for Jxta fix to put this next line back in
            //replicatedSessions.remove(replicaState.getId());
            //FIXME move this outside if statement - should alway occur
            //while here check and remove from manager cache if present
            this.clearFromManagerCache((String)queryState.getId());
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);
        }          
        //System.out.println("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);
        return returnState;

    }       
    
    //end processing methods
    
    /**
    * send the response
    * //FIXME this should get removed after testing
     * we'll be using ReplicationState not converting from Session
    * @param session
    *   The session
    * @return ReplicationState
    */    
    public ReplicationState createQueryResponse(Session session) {    
        String command = ReplicationState.RETURN_BROADCAST_MSG_COMMAND;
        ReplicationStore store = (ReplicationStore)this.getStore();
        ReplicationState transmitState = null;
        try {
            transmitState = store.createReplicationState(session, command);
        } catch (IOException ex) {}
        return transmitState;
    }        
    
    protected ReplicationState findReplicatedState(ReplicationState queryState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findReplicatedState:id = " + queryState.getId());
        }              
        return this.getFromReplicationCache( (String)queryState.getId() );
    }
    
    protected void clearFromManagerCache(String id) {
        Session sess = null;
        try {
            sess = this.findSessionFromCacheOnly(id);
        } catch (IOException ex) {}
        if(sess != null) {                               
            this.removeSessionFromManagerCache(sess);
        } 
    }
    
   /**
    * Finds and returns the session with the given id that also satisfies
    * the given version requirement.
    *
    * This overloaded version of findSession() will be invoked only if
    * isSessionVersioningSupported() returns true. By default, this method
    * delegates to the version of findSession() that does not take any
    * session version number.
    *
    * @param id The session id to match
    * @param version The session version requirement to satisfy
    *
    * @return The session that matches the given id and also satisfies the
    * given version requirement, or null if no such session could be found
    * by this session manager
    *
    * @exception IOException if an IO error occurred
    */
    public Session findSession(String id, String version) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in new findSession: version=" + version);
        }
        //Thread.dumpStack();
        //System.out.println("ReplicationManagerBase>>findSession:id:" + id + " reqVersion: " + version + " isValid:" + this.isSessionIdValid(id));
        if(!this.isSessionIdValid(id) || version == null) {
            return null;
        }
        //System.out.println("in new findSession dumping: required version=" + version);
        //Thread.dumpStack();
        Session session = null;
        long requiredVersion = 0L;
        long cachedVersion = -1L;
        try {
            requiredVersion = (Long.valueOf(version)).longValue();
        } catch (NumberFormatException ex) {
            //deliberately do nothing
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findSession:requiredVersion=" + requiredVersion);
        }         
        //System.out.println("findSession:requiredVersion=" + requiredVersion);       
        Session cachedSession = this.findSessionFromCacheOnly(id);
        if(cachedSession != null) {
            cachedVersion = ((HASession)cachedSession).getVersion();
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findSession:cachedVersion=" + cachedVersion);
        }        
        //System.out.println("findSession:cachedVersion=" + cachedVersion); 
        //if version match return cached session else purge it from cache
        if(cachedVersion == requiredVersion) {
            return cachedSession;
        } else {
            if(cachedVersion < requiredVersion) {
                //System.out.println("findSession:removing stale cachedVersion=" + cachedVersion);
                this.removeSessionFromManagerCache(cachedSession);
                cachedSession = null;
                cachedVersion = -1L;
            }
        }
        /*
        if(cachedVersion == requiredVersion) {
            return cachedSession;
        }
         */        
        // See if the Session is in the Store
        if(requiredVersion != -1L) {
            session = swapIn(id, version);
        } else {
            session = swapIn(id);
        }                
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findSession:swappedInSession=" + session);
        }
        //System.out.println("findSession:swappedInSession=" + session);
        /*
        if(cachedVersion == -1) {
            return session;
        }
         */
        
        if(session == null || ((HASession)session).getVersion() < cachedVersion) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationManagerBase>>findSession:returning cached version:" + cachedVersion);
            }            
            //System.out.println("ReplicationManagerBase>>findSession:returning cached version:" + cachedVersion);
            return cachedSession;
        }
        if( ((HASession)session).getVersion() < requiredVersion) {            
            session = null;            
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationManagerBase>>findSession:returning:" + session);
        }        
        //System.out.println("ReplicationManagerBase>>findSession:returning " + session); 
        return (session);       
    }    
    
    protected boolean isSessionOlderThan(StandardSession session, long aTime) {
        return (session.getLastAccessedTime() <= aTime);
    }    
    
    protected boolean isMonitoringEnabled() {
        return ServerConfigReader.isMonitoringEnabled();
    }
    
    public void resetMonitorStats() {
        WebModuleStatistics stats = this.getWebModuleStatistics();
        stats.resetStats();
    }     
    
    //SJSAS 6406580 START
    /** return the StorePool */
    public StorePool getStorePool() {
        return _pool;
    }
    
    /** set the StorePool */
    public void setStorePool(StorePool pool) {
        _pool = pool;
    }
    //SJSAS 6406580 END    
    
    /** return the session factory */
    SessionFactory getSessionFactory() {
        return _sessionFactory;
    }
    
    /**
     * set the session factory
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    }
    
    /** return the web module statistics */
    public WebModuleStatistics getWebModuleStatistics() {
        return _statistics;
    }
    
    /**
     * set the web module statistics
     * @param statistics
     */    
    public void setWebModuleStatistics(WebModuleStatistics statistics) {
        _statistics = statistics;
    } 
    
    public boolean isSessionVersioningSupported() {
        return true;
    }
    
    public String getPassedInPersistenceType() {
        return _passedInPersistenceType;
    }    
    
    public void setPassedInPersistenceType(String persistenceType) {
        _passedInPersistenceType = persistenceType;
    }
    
    public boolean isDuplicateIdsSemanticsAllowed() {
        return _duplicateIdsSemanticsAllowed;
    }    
    
    public void setDuplicateIdsSemanticsAllowed(boolean value) {
        _duplicateIdsSemanticsAllowed = value;
    }    
    
    protected String _passedInPersistenceType = null;
    protected boolean _duplicateIdsSemanticsAllowed = false;
    protected SessionFactory _sessionFactory = null;
    protected WebModuleStatistics _statistics = new WebModuleStatistics();
    //SJSAS 6406580 START
    StorePool _pool = null;
    //SJSAS 6406580 END
    
    private class ReplicationStateUpdate {

        private String _id = null;
        private long _lastAccess = 0L;
        private long _version = -1L;
        
        String getId() {
            return _id;
        }        
        
        long getLastAccess() {
            return _lastAccess;
        }
        
        long getVersion() {
            return _version;
        }       
        
        public ReplicationStateUpdate(String id, long version, long lastAccess) {
            _id = id;
            _version = version;
            _lastAccess = lastAccess;
        }
        
    }    
    
}
