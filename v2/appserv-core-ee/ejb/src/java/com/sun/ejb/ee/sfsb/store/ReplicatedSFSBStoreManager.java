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
 * ReplicatedSFSBStoreManager.java
 *
 * Created on June 1, 2006, 10:58 AM
 *
 */

package com.sun.ejb.ee.sfsb.store;

import java.lang.reflect.InvocationTargetException;

import com.sun.appserv.ha.spi.*;
//import com.sun.appserv.ha.impl.*;

import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;
import com.sun.ejb.spi.sfsb.store.SFSBTxStoreManager;
import com.sun.ejb.spi.stats.MonitorableSFSBStoreManager;

import com.sun.enterprise.ee.web.sessmgmt.JxtaBackingStoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.JxtaBackingStoreImpl;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationHealthChecker;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationResponseRepository;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationManager;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationMessageRouter;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationState;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationStateQueryResponse;
import com.sun.enterprise.ee.web.sessmgmt.StoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.StorePool;
import com.sun.enterprise.ee.web.sessmgmt.StorePoolElement;

import com.sun.enterprise.web.ServerConfigLookup;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.sun.logging.LogDomains;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
import java.util.*;

import com.sun.appserv.util.cache.BaseCache;


/**
 *
 * @author Larry White
 */
public class ReplicatedSFSBStoreManager extends BaseSFSBStoreManager 
    implements SFSBTxStoreManager, MonitorableSFSBStoreManager, ReplicationManager {
    
    final static String MODE_EJB
        = ReplicationState.MODE_EJB;
    
    public final static String LOGGER_MEM_REP 
        = "com.sun.ejb.ee.sfsb.store"; 
    
    private final static Level TRACE_LEVEL = Level.FINE;
    
    final static String DUPLICATE_IDS_SEMANTICS_PROPERTY 
        = ReplicationState.DUPLICATE_IDS_SEMANTICS_PROPERTY;    
    
    /** Pool of ReplicatedEjbStore StorePool elements
     */
    StorePool _pool = null;
    
    protected static int _maxBaseCacheSize = 4096;
    protected static float _loadFactor = 0.75f;
    
    /** Logger for logging
     */
    //private final static Logger _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);    


    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);

    
    static
	{
            checkSessionCacheProperties();
	}     

    protected static boolean checkSessionCacheProperties() {
        boolean result = false;
	try
        {
            Properties props = System.getProperties();
            String cacheSize=props.getProperty("EJB_SESSION_CACHE_MAX_BASE_CACHE_SIZE");
            if(null!=cacheSize) {
                _maxBaseCacheSize = (new Integer (cacheSize).intValue());
            }  
            String loadFactor=props.getProperty("EJB_SESSION_CACHE_MAX_BASE_LOAD_FACTOR");
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
    
    public String getPassedInPersistenceType() {
        return _passedInPersistenceType;
    }    
    
    public void setPassedInPersistenceType(String persistenceType) {
        _passedInPersistenceType = persistenceType;
    }    

    /**
    * the passed in persistence type may be replicated or extension type
    */    
    protected String _passedInPersistenceType = null;     
    
    /**
    * Our Replicator instance 
    */
    protected BackingStore backingStore = null;
    
    /** 
     * 	return the containerId as a String
     *  needed to implement ReplicationManager	
     */    
    public String getApplicationId() {
        return getContainerID();
    }    
    
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
    * set the backing store
    * @param aBackingStore
    */ 
    public void setBackingStore(BackingStore aBackingStore) {
        backingStore = aBackingStore;
    } 

    /**
    * create the backingStore
    */     
    void createBackingStore() {
        //BackingStoreFactory storeFactory = new JxtaBackingStoreFactory();
        BackingStoreFactory storeFactory = getBackingStoreFactory();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("storeFactory: " + storeFactory);
        }         
        BackingStoreRegistry backingStoreRegistry 
            = BackingStoreRegistry.getInstance();
        Properties env 
            = backingStoreRegistry.getFactoryClassEnv(getPassedInPersistenceType());
        //this is always true for sfsbs
        if(env != null) {
            env.put(DUPLICATE_IDS_SEMANTICS_PROPERTY, true);
        }
        BackingStore backingStore = null;
        try {
            backingStore = storeFactory.createBackingStore(
                        SimpleMetadata.class,     //type
                        this.getContainerID(), //appid
                        env);
        } catch (BackingStoreException ex) {
            //deliberate no-op
        }        
        if(backingStore != null) {
            if(backingStore instanceof JxtaBackingStoreImpl) {
                ((JxtaBackingStoreImpl)backingStore).setMode(MODE_EJB);
            }
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("backingStore: " + backingStore);
        }        
        this.setBackingStore(backingStore);
    }

    /**
    * create the backingStore factory based on the passed
    * in persistence-type
    */    
    protected BackingStoreFactory getBackingStoreFactory() {
        BackingStoreFactory backingStoreFactory = new JxtaBackingStoreFactory();
        BackingStoreRegistry backingStoreRegistry
            = BackingStoreRegistry.getInstance();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("getBackingStoreFactory:passedInPersistenceType=" + getPassedInPersistenceType());
        }        
        if(getPassedInPersistenceType() == null) {
            return backingStoreFactory;
        }
        String factoryClassName 
            = backingStoreRegistry.getFactoryClassName(this.getPassedInPersistenceType());
        return getBackingStoreFactoryFromName(factoryClassName);
    }
    
    private BackingStoreFactory getBackingStoreFactoryFromName(String className) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("getBackingStoreFactoryFromName:className: " + className);
        }           
        BackingStoreFactory backingStoreFactory = new JxtaBackingStoreFactory();
        try {
            backingStoreFactory = 
                (BackingStoreFactory) (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicatedSFSBStoreManager:unable to create backing store factory");
            }            
        } 
        return backingStoreFactory;
    }             
    
    /** Creates a new instance of ReplicatedSFSBStoreManager */
    public ReplicatedSFSBStoreManager() {
        super();
        /*
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
        }
         */
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "ReplicatedSFSBStoreManager  loaded successfully......");
        }
        StoreFactory haStoreFactory = new ReplicatedEjbStoreFactory();
        _pool = new StorePool(StorePool.DEFAULT_INITIAL_SIZE,
        StorePool.DEFAULT_UPPER_SIZE,
        StorePool.DEFAULT_POLL_TIME, haStoreFactory);
        
        //initialize replicated sessions cache
        replicatedSessions = new BaseCache();
        replicatedSessions.init(_maxBaseCacheSize, _loadFactor, null);
        //initialize locally passivated sessions cache
        locallyPassivatedSessions = new BaseCache();
        locallyPassivatedSessions.init(_maxBaseCacheSize, _loadFactor, null);
    }

    /**
    * get the server instance name
    */    
    public String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }    
    
    /**
    * Our cache of replicated ReplicationState objects
    * keyed by id
    */
    protected BaseCache replicatedSessions = new BaseCache();
    
    /**
    * Our cache of locally passivated SFSBBeanState objects
    * keyed by id
    */
    protected BaseCache locallyPassivatedSessions = new BaseCache();    

    /**
    * get the replicated sessions cache
    */ 
    public BaseCache getReplicatedSessions() {
        return replicatedSessions;
    }
  
    /**
    * set the replicated sessions cache
    * @param sesstable
    */ 
    public void setReplicatedSessions(BaseCache sesstable) {
        replicatedSessions = sesstable;
    }

    /**
    * put the replication state into the replica cache
    * @param sessionState
    */    
    protected synchronized void putInReplicationCache(ReplicationState sessionState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>putInReplicationCache id: " + sessionState.getId());
        } 
        String id = (String)sessionState.getId();
        if(replicatedSessions == null || id == null) {
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
    * get the replication state from the replica cache based on the id
    * @param id
    */    
    protected ReplicationState getFromReplicationCache(String id) {    
        return (ReplicationState)replicatedSessions.get(id);
    }

    /**
    * remove the replication state from the replica cache
    * @param sessionState
    */    
    protected void removeFromReplicationCache(ReplicationState sessionState) { 
        if(replicatedSessions == null  || sessionState == null) {
            return;
        }
        replicatedSessions.remove(sessionState.getId());
    }

    /**
    * return and remove the replication state from the replica cache 
    * based on the id
    * @param id
    */     
    protected synchronized ReplicationState transferFromReplicationCache(String id) { 
        ReplicationState result = this.getFromReplicationCache(id);
        removeFromReplicationCache(result);
        return result;
    }     
    
    //*****************************************************
    
    /**
    * get the locally passivated sessions cache
    */ 
    public BaseCache getLocallyPassivatedSessions() {
        return locallyPassivatedSessions;
    }
  
    /**
    * set the locally passivated sessions cache
    * @param sesstable
    */ 
    public void setLocallyPassivatedSessions(BaseCache sesstable) {
        locallyPassivatedSessions = sesstable;
    }

    /**
    * put the beanState in the locallyPassivatedSessions cache 
    * @param beanState
    */     
    protected void putInLocallyPassivatedCache(SFSBBeanState beanState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>putInLocallyPassivatedCache id: " + beanState.getContainerId());
        }        
        if(locallyPassivatedSessions == null) {
            return;
        }
        locallyPassivatedSessions.put(beanState.getId(), beanState);
    }

    /**
    * get the beanState in the locallyPassivatedSessions cache
    * based on the id 
    * @param id
    */     
    protected SFSBBeanState getFromLocallyPassivatedCache(String id) {    
        return (SFSBBeanState)locallyPassivatedSessions.get(id);
    }

    /**
    * remove the beanState from the locallyPassivatedSessions cache
    * @param beanState
    */    
    protected void removeFromLocallyPassivatedCache(SFSBBeanState beanState) { 
        if(locallyPassivatedSessions == null) {
            return;
        }
        locallyPassivatedSessions.remove(beanState.getId());
    } 
    
    /**
    * remove the beanState with id from the locallyPassivatedSessions cache
    * @param id
    */    
    protected void removeIdFromLocallyPassivatedCache(Object id) {
        if(locallyPassivatedSessions == null) {
            return;
        }
        locallyPassivatedSessions.remove(id);
    }    
    
    /** return the StorePool this manager holds
     * @return Returns the pool it holds
     */
    public StorePool getStorePool() {
        return _pool;
    }
    
    /** set the StorePool
     * @param pool pool of ReplicatedEjbStore
     */
    public void setStorePool(StorePool pool) {
        _pool = pool;
    }    
    
    /** Returns a store from the pool This method intializes the store with right parameters
     * @return returns ReplicatedEjbStore
     */
    private ReplicatedEjbStore getStore() {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "getStore");
        }
        ReplicatedEjbStore store = null;
        try {
            store = (ReplicatedEjbStore) _pool.take();
            store.setContainer(this.getContainer());
            store.setClusterID(this.getClusterID());
            store.setContainerId(this.getContainerID());
            store.setSFSBStoreManager(this);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "ReplicatedSFSBStoreManager.getStore returning   " + store);
            }
            return store;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "getStore", store);
        }
        return store;
    } 
    
    /** 
     *  Returns (puts) a store back to the pool
     */
    private void putStore(ReplicatedEjbStore store) {    
        ( (ReplicatedEjbStore) store).setContainer(null);
        if (store != null) {
            try {
                StorePool storePool = this.getStorePool();
                if(storePool != null) {
                    storePool.put( (StorePoolElement) store);
                }
            }
            catch (InterruptedException ex1) {
                //FIXME: log this
                ex1.printStackTrace();
            }
        }
    }
    
    // SFSBStoreManager methods
    
    /** Saves the state of the bean
     * @param beanState SFSBBeanState
     * @throws SFSBStoreManagerException
     */
    public void checkpointSave(SFSBBeanState beanState) 
        throws SFSBStoreManagerException {        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "checkpointSave",
                new Object[] {beanState});
        }
        //added for monitoring
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring         
        StorePool storePool = this.getStorePool();
        
        ReplicatedEjbStore store = null;
        try {
            store = getStore();            
            ( (ReplicatedEjbStore) store).save(beanState, this);
        }
        catch (IOException e) {
            //e.printStackTrace();
            throw new SFSBStoreManagerException("Error during checkpointSave: id =" + beanState.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
        }
        //added for monitoring      
        if(this.isMonitoringEnabled()) {
            long saveEndTime = System.currentTimeMillis();
            stats.processCheckpointSave(saveEndTime - saveStartTime);
            stats.processBeanSize(this.getBeanSize(beanState));
        }
        //end added for monitoring         
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "checkpointSave");
        }
    }
    
    /** Saves the state
     * @param beanState SFSBBeanState
     * @throws SFSBStoreManagerException
     */
    public void repairSave(ReplicationState state) 
        throws SFSBStoreManagerException {
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "repairSave",
                new Object[] {state});
        }
        //added for monitoring
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring         
        StorePool storePool = this.getStorePool();
        
        ReplicatedEjbStore store = null;
        try {
            store = getStore();            
            ( (ReplicatedEjbStore) store).saveForRepair(state, this);
        }
        catch (IOException e) {
            //e.printStackTrace();
            throw new SFSBStoreManagerException("Error during repairSave: id =" + state.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
        }
        //added for monitoring      
        if(this.isMonitoringEnabled()) {
            long saveEndTime = System.currentTimeMillis();
            stats.processCheckpointSave(saveEndTime - saveStartTime);
            //stats.processBeanSize(this.getBeanSize(beanState));
        }
        //end added for monitoring         
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "repairSave");
        }
    }       

    /** 
     * return the MonitorableSFSBStoreManager
     */    
    public MonitorableSFSBStoreManager getMonitorableSFSBStoreManager() {
        return this;
    }
    
    /** gets the state of the bean
     * @param id ID of the bean to be loaded from the HADB
     * @throws SFSBStoreManagerException
     * @return returns the bean with the state
     */
    public SFSBBeanState getState(Object id) throws SFSBStoreManagerException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "getState", id);
        }
        if(id == null) {
            return null;
        }
        SFSBBeanState sfsbState = null;
        //try first in locally passivated cache before going to backup
        sfsbState= this.getFromLocallyPassivatedCache(id.toString());
        if(sfsbState != null) {
            return sfsbState;
        }
        
        StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            sfsbState = store.loadBean(id);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "getStore", sfsbState);
            }
            return sfsbState;
            
        }
        catch (Exception e) {
            //e.printStackTrace();
            //throw e;            
            throw new SFSBStoreManagerException("Error loading SFSB state: id =" + id.toString(), e); 
        }
        finally {
            this.putStore(store);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "getStore", sfsbState);
            }
            //return sfsbState;
        }
        
    }    
    
    /** Saves the state of the sfsb
     * @param beanState SFSBBeanState
     * @throws SFSBStoreManagerException
     */
    public void passivateSave(SFSBBeanState beanState) 
        throws SFSBStoreManagerException {

        //store in local cache before pushing to backup
        putInLocallyPassivatedCache(beanState);
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "passivateSave",
                new Object[] {beanState});
        }
        //added for monitoring
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring        
        StorePool storePool = this.getStorePool();
        
        ReplicatedEjbStore store = null;
        try {
            store = getStore();            
            ( (ReplicatedEjbStore) store).save(beanState, this);
        }
        catch (IOException e) {
            //e.printStackTrace();
            throw new SFSBStoreManagerException("Error during passivateSave: id =" + beanState.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
        }
        //added for monitoring      
        if(this.isMonitoringEnabled()) {
            long saveEndTime = System.currentTimeMillis();
            stats.processPassivateSave(saveEndTime - saveStartTime);
            stats.processBeanSize(this.getBeanSize(beanState));
        }
        //end added for monitoring         
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "passivateSave");
        }
    }         

    /*
    public void remove(Object sessionKey) throws SFSBStoreManagerException {
    }
     */
    
    /** Removes the bean will be called when the client removes the bean
     * @param id ID of the bean to be removed
     */
    //FIXME reverting back to non-batch remove for testing
    public void remove(Object id) {
        
        this.removeIdFromLocallyPassivatedCache(id);        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "remove", id);
        }
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            store.remove(id);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "remove");
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.putStore(store);
        }        
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "remove");
        }
    }    
    
    /**
     * Remove all session data for this container
     * called during undeployment
     * @throws SFSBStoreManagerException
     */    
    public void removeAll() throws SFSBStoreManagerException {
       
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "removeAll", containerId);
        }
        
        StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            store.undeployContainer();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "removeAll");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during ReplicatedSFSBStoreManager>>removeAll for container: " + containerId, e);
        }
        finally {
            //un-register with message router
            ReplicationMessageRouter router = ReplicationMessageRouter.createInstance();
            if(router != null) {
                router.removeReplicationManager(this.getContainerID());
            }             
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "removeAll");
        }
        return;
        
    }
    
    /**
     * Remove all the idle/expired session data 
     * that are idle for idleTimeoutInSeconds (passed during initSessionStore())
     * @throws SFSBStoreManagerException
     */
    public int removeExpiredSessions() throws SFSBStoreManagerException {
        
        //remove any expired beans from locally passivated cache too
        removeExpiredFromLocallyPassivated();
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "removeExpired", containerId);
        }
        int result = 0;
        StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            result = store.removeExpiredSessions();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "removeExpired");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during ReplicatedSFSBStoreManager>>removeExpired for container: " + containerId, e);
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "removeExpired");
        }
        return result;        
    }       
    
    /**
     * update only the lastAccessTime to the value time
     * Used when the session has been accessed as well
     * as periodically to keep session alive
     * @param sessionKey
     * @param time
     * @throws SFSBStoreManagerException
     */
    public void updateLastAccessTime(Object sessionKey, long time)
        throws SFSBStoreManagerException {

        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "updateLastAccessTime", sessionKey.toString());
        }
        
        StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            store.updateLastAccessTime(sessionKey, time);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "updateLastAccessTime");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during ReplicatedSFSBStoreManager>>updateLastAccessTime for key: " 
                + sessionKey + " errMsg: " + e.getMessage(), e);
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "updateLastAccessTime");
        }
        return;              
    }    
    
    // SFSBTxStoreManager methods
    
    /**
     * @param beanStates
     * @throws SFSBStoreManagerException
     */
    public void checkpointSave(SFSBBeanState[] beanStates) throws SFSBStoreManagerException {
   
        //optimize if there is only a single beanState
        if(beanStates.length == 1) {
            this.checkpointSave(beanStates[0]);
            return;
        }
        
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        //FIXME: temp test code next 2 lines
        /*
        System.out.println("TxCheckpointDurations Before checkpointSave:");
        printTxCheckpointDurations(beanStates);
         */
        long startTime = 0L;
        Long[] originalTxCheckpointDurations = {new Long(0L)};

        //added for monitoring
        if(this.isMonitoringEnabled()) {
            //temporarily store original txCheckpointDurations
            originalTxCheckpointDurations = 
                this.storeOriginalTxCheckpointDurations(beanStates);
            startTime = System.currentTimeMillis();
        }
        //end added for monitoring
        
        /*
        _logger.entering("ReplicatedSFSBStoreManager", "checkpointSave",
        new Object[] {beanStates, new Boolean(isNew)});
         */
        StorePool storePool = this.getStorePool();
        
        ReplicatedEjbStore store = null;
        try {
            store = getStore();            
            ( (ReplicatedEjbStore) store).save(beanStates, startTime);
            //added for monitoring
            if(this.isMonitoringEnabled()) {
                for(int i=0; i<beanStates.length; i++) {
                    SFSBBeanState nextBeanState = beanStates[i];
                    stats.processBeanSize(this.getBeanSize(nextBeanState));
                }
            }
            //end added for monitoring           
        }
        catch (IOException e) {
            //e.printStackTrace();
            //added for monitoring
            if(this.isMonitoringEnabled()) {
                //restore original txCheckpointDurations
                this.restoreOriginalTxCheckpointDurations(beanStates, originalTxCheckpointDurations);
            }
            //end added for monitoring
            throw new SFSBStoreManagerException("Error during checkpointSave", e); 
        }
        finally {
            this.putStore(store);
        }
        //FIXME: temp test code next 2 lines
        /*
        System.out.println("TxCheckpointDurations After checkpointSave:");
        printTxCheckpointDurations(beanStates);
        */
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "checkpointSave");
        }

    } 
    
    private Long[] storeOriginalTxCheckpointDurations(SFSBBeanState[] beanStates) {    
        //return temporarily stored original txCheckpointDurations
        Long[] originalTxCheckpointDurations = new Long[beanStates.length];
        for(int i=0; i<beanStates.length; i++) {
            SFSBBeanState nextBeanState = beanStates[i];
            originalTxCheckpointDurations[i] = 
                new Long(nextBeanState.getTxCheckpointDuration());
        }
        return originalTxCheckpointDurations;
    }
    
    private void restoreOriginalTxCheckpointDurations(SFSBBeanState[] beanStates, Long[] originalTxCheckpointDurations) {    
        //restore temporarily stored original txCheckpointDurations
        //to beanStates from originalTxCheckpointDurations
        for(int i=0; i<beanStates.length; i++) {
            SFSBBeanState nextBeanState = beanStates[i];
            long originalDuration = originalTxCheckpointDurations[i].longValue();
            nextBeanState.setTxCheckpointDuration(originalDuration);
        }
        return;
    } 
    
    private void printTxCheckpointDurations(SFSBBeanState[] beanStates) {    
        //used for testing
        System.out.println("Printing Checkpoint Durations");
        for(int i=0; i<beanStates.length; i++) {
            SFSBBeanState nextBeanState = beanStates[i];
            System.out.println("printTxCheckpointDurations for beanState[" + i + "]: "
                + nextBeanState.getTxCheckpointDuration()); 
        }
        return;
    }

    /** 
     * gets the size (bytes) of the beanState
     * @param beanState
     * @return returns the size of the beanState
     */    
    protected long getBeanSize(SFSBBeanState beanState) {
        if (beanState == null) {
            return 0;
        }
        byte[] bytes = beanState.getState();
        if(bytes != null) {
            return bytes.length;
        } else {
            return 0;
        }
    }    
    
    // MonitorableSFSBStoreManager methods
    
    public void monitoringLevelChanged(boolean newValue) {
        //true means on -- false means off
        //FIXME do something
    }
    
    /**
    * return the current number of beans stored for this container
    */    
    public long getCurrentStoreSize() {
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "getCurrentStoreSize");
        }
        int result = 0;
        //StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            result = store.getContainerSize();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "getCurrentStoreSize");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "getCurrentStoreSize");
        }
        return result;            
        
    }
    
    public void repair(long repairStartTime) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>repair");
        }         
        /*
        if (!started)
            return;
         */
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }        

        SFSBBeanState beanStates[] = getLocallyPassivatedBeanStatesArray();
        for (int i = 0; i < beanStates.length; i++) {
            SFSBBeanState beanState = (SFSBBeanState) beanStates[i];
            if(beanState.getLastAccess() <= repairStartTime) {
                try {
                    checkpointSave(beanState);
                } catch(SFSBStoreManagerException ex) {
                    //FIXME log this
                    ex.printStackTrace();
                } catch(Throwable t) {
                    System.out.println("Throwable during force flush");
                    t.printStackTrace();
                    break;
                }
            }
        }
 
    } 
    
    public void repair(long repairStartTime, boolean checkForStopping) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>repair");
        }         
        /*
        if (!started)
            return;
         */
        if(checkForStopping && ReplicationHealthChecker.isStopping()) {
            return;
        }        

        SFSBBeanState beanStates[] = getLocallyPassivatedBeanStatesArray();
        for (int i = 0; i < beanStates.length; i++) {
            SFSBBeanState beanState = (SFSBBeanState) beanStates[i];
            if(beanState.getLastAccess() <= repairStartTime) {
                try {
                    checkpointSave(beanState); 
                } catch(SFSBStoreManagerException ex) {
                    //FIXME log this
                    ex.printStackTrace();
                } catch(Throwable t) {
                    System.out.println("Throwable during force flush");
                    //t.printStackTrace();
                    break;
                }
            }
        }
 
    }      
    
    public SFSBBeanState[] getLocallyPassivatedBeanStatesArray() {
        
        BaseCache passivatedSessions = locallyPassivatedSessions;
        SFSBBeanState[] beanStates = null;
        int numberOfIds = passivatedSessions.getEntryCount();
        ArrayList valuesList = new ArrayList(numberOfIds);
        Iterator valuesIter = passivatedSessions.values();
        while(valuesIter.hasNext()) {
            valuesList.add((SFSBBeanState)valuesIter.next());
        }
        SFSBBeanState[] template = new SFSBBeanState[valuesList.size()];
        beanStates = (SFSBBeanState[])valuesList.toArray(template);
        return beanStates;

    }
    
    /** 
     * remove any expired states from the locally passivated cache
     */ 
    public void removeExpiredFromLocallyPassivated() {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>removeExpiredFromLocallyPassivated");
        }        
        ArrayList expiredLocallyPassivated = new ArrayList(30);
        BaseCache locallyPassivatedCache = this.getLocallyPassivatedSessions();
        for(Iterator it = locallyPassivatedCache.values(); it.hasNext();) {
            SFSBBeanState nextBeanState = (SFSBBeanState)it.next();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicatedSFSBStoreManager>>removeExpiredFromLocallyPassivated:nextState=" + nextBeanState);
            }
            if(isBeanExpired(nextBeanState)) {
                expiredLocallyPassivated.add(nextBeanState);
            }
        }
        if(_logger.isLoggable(Level.FINE)) {           
            _logger.fine("removeExpiredFromLocallyPassivated:expiredReplicas.size=" + expiredLocallyPassivated.size());
        }                
        for(int i=0; i<expiredLocallyPassivated.size(); i++) {
            SFSBBeanState nextBeanState = 
                (SFSBBeanState)expiredLocallyPassivated.get(i);
            this.removeFromLocallyPassivatedCache(nextBeanState);
        }        
    }
    
    private boolean isBeanExpired(SFSBBeanState beanState) {
        long timeAlive = System.currentTimeMillis() - beanState.getLastAccess();
        return (timeAlive > (this.getIdleTimeoutInSeconds()* 1000L));        
    }
    
    //receive processing methods
    
    /** 
     * process the save of the replicationState
     * @param message
     */     
    public void processValvesave(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processValvesave");
            _logger.fine("processValvesave:version:" + message.getVersion());             
        } 
        //System.out.println("IN" + this.getClass().getName() + ">>processValvesave");
        this.putInReplicationCache(message);
    } 

    /** 
     * process the remove of the replicationState
     * @param message
     */     
    public void processRemove(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>processRemove");
        }        
        this.removeFromReplicationCache(message);
    }

    /** 
     * process the removeExpired for this container
     * @param message
     */     
    public void processRemoveExpired(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>processRemoveExpired");
        }        
        //FIXME finish this
        ArrayList expiredReplicas = new ArrayList(30);
        BaseCache replicasCache = this.getReplicatedSessions();
        for(Iterator it = replicasCache.values(); it.hasNext();) {
            ReplicationState nextState = (ReplicationState)it.next();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicatedSFSBStoreManager>>processRemoveExpired:nextState=" + nextState);
            }            
            if(nextState.isExpired()) {
                expiredReplicas.add(nextState);
            }
        }
        if(_logger.isLoggable(Level.FINE)) {           
            _logger.fine("processRemoveExpired:expiredReplicas.size=" + expiredReplicas.size());
        }                
        for(int i=0; i<expiredReplicas.size(); i++) {
            ReplicationState nextState = 
                (ReplicationState)expiredReplicas.get(i);
            this.removeFromReplicationCache(nextState);
        }        
    }

    /** 
     * process the updateLastAccessTime of the replicationState
     * @param message
     */     
    public void processUpdateLastAccessTime(ReplicationState message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicatedSFSBStoreManager>>processUpdateLastAccessTime");
        }                 
        ReplicationState cachedState 
            = this.getFromReplicationCache((String)message.getId());
        if(cachedState != null) {
            cachedState.setLastAccess(message.getLastAccess());
            //we are not updating version here for now
            //cachedState.setVersion(message.getVersion());
        }       
    }
    
    //query processing methods
    
    public ReplicationState processBroadcastfindsessionPrevious(ReplicationState queryState) {
        //FIXME complete query and send back response
        //System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:instance: " + getInstanceName());        
        ReplicationState replicaState 
            = findReplicatedState(queryState);
        ReplicationState returnState = null;
        if(replicaState != null) {
            returnState = ReplicationState.createQueryResponseFrom(replicaState);
        }
        //System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:returnState=" + returnState);
        /*
        //temp code returning from local cache - decide if that's ok
        if(returnState == null) {
            Session sess = null;
            try {
                sess = this.findSessionFromCacheOnly((String)queryState.getId());
            } catch (IOException ex) {}
            if(sess != null) {
                returnState = createQueryResponse(sess);           
            }
            System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:localCachedBean=" + sess);            
        }
        //end temp code
         */
        return returnState;

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
        ReplicationState replicaState 
            = findReplicatedState(queryState);
        ReplicationState returnState = null;
        if(replicaState != null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicatedSFSBStoreManager>>processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
                _logger.fine("ReplicatedSFSBStoreManager>>processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
                _logger.fine("ReplicatedSFSBStoreManager>>processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());              
            }
            //System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:REPLICA_FOUND:replicaStateVersion:" + replicaState.getVersion());
            //System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:REPLICA_FOUND:replicaState:" + replicaState.getTrunkState());
            //System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:REPLICA_FOUND:replicaAttrState" + replicaState.getState());               
            returnState = ReplicationState.createQueryResponseFrom(replicaState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicatedSFSBStoreManager>>processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());              
            }
            //System.out.println("ReplicatedSFSBStoreManager>>processBroadcastfindsession:replicaStateResponseVersion:" + returnState.getVersion());
            //FIXME may want to wait for ack before doing this       
            //replicatedSessions.remove(replicaState.getId());
            //while here check and remove from manager cache if present
            this.clearFromPassivatedCache((String)queryState.getId());
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in " + this.getClass().getName() + ">>processBroadcastfindSession:returnState=" + returnState);              
        }
        return returnState;

    }     
    
    protected ReplicationState findReplicatedState(ReplicationState queryState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("findReplicatedState:id = " + queryState.getId());              
        }              
        return this.getFromReplicationCache( (String)queryState.getId() );
    }
    
    protected void clearFromPassivatedCache(String id) {
        SFSBBeanState beanState = this.getFromLocallyPassivatedCache(id);
        if(beanState != null) {                               
            this.removeFromLocallyPassivatedCache(beanState);
        } 
    }    
    
    // message processing

    public void processMessage(ReplicationState message) {
        //FIXME complete
        if(ReplicationState.isBroadcastState(message)) {
            processBroadcastMessage(message);
            return;
        }
        
        ReplicationState response = 
            ReplicationState.createResponseFrom(message);
        //send a response before further processing only if processed 
        //msg is not itself a response and if method is a void return type
        //FIXME this send will be removed if upstream ack send works
        /* removing this for test
        if(!message.isReturnMessage() && message.isVoidMethodReturnState()) {
            try {
                this.doSendResponse(response);
            } catch (SFSBStoreManagerException ex) {
                //FIXME log this
            }             
        }
         */        

        boolean isResponse = this.doProcessMessage(message);
        //send a response only if processed msg is not itself a response
        //and if method is not void return type (in that case ack was
        //already sent)
        if(!isResponse && !message.isVoidMethodReturnState()) {
            /*
            ReplicationState response = 
                ReplicationState.createResponseFrom(message);
             */
            try {
                this.doSendResponse(response);
            } catch (SFSBStoreManagerException ex) {
                //FIXME log this
            }            
        }
    }
    
    //return true if message is processResponse
    public boolean doProcessMessage(ReplicationState message) {
        //FIXME complete
        boolean result = false;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicatedSFSBStoreManager>>doProcessMessage");              
        }        
        String methodName = getProcessMethodName(message);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicatedSFSBStoreManager>>doProcessMessage:methodName=" + methodName);              
        }        
        try {
            Class myClass = this.getClass();
            myClass.getMethod(
                methodName,
                    new Class[]{ message.getClass() }).invoke(
                        this, new Object[]{ message });            
        } catch (IllegalAccessException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicatedSFSBStoreManager>>doProcessMessage:methodName=" + methodName + "illegalAccessException");              
            }            
        } catch (NoSuchMethodException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicatedSFSBStoreManager>>doProcessMessage:methodName=" + methodName + "noSuchMethodException");              
            }             
        } catch (InvocationTargetException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicatedSFSBStoreManager>>doProcessMessage:methodName=" + methodName + "invocationTargetException");
                _logger.fine("invocationException.getCause()= " + ex.getCause());
            }                      
            ex.printStackTrace();
        } 
        if(methodName.equals("processResponse")) {
            result = true;
        }
        return result;
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
            _logger.fine("in ReplicationWebEventPersistentManager>>processResponse");
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
    
    public void processBroadcastMessage(ReplicationState message) {
        //FIXME complete
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("processBroadcastMessage:after doProcessQueryMessage:responseState=" + responseState);            
        }        
        //don't send a response to a response
        if(!isResponse) {
            //FIXME point-to-point response back to sender
            //System.out.println("processBroadcastMessage - need to send back result");
            try {
                doSendQueryResponse(responseState, this.getInstanceName());
            } catch (SFSBStoreManagerException ex) {
                //FIXME log this
            }
            /*
            ReplicationState response = 
                ReplicationState.createResponseFrom(message);
            this.doSendResponse(response);
             */
        }
    }
    
    public void processQueryMessage(ReplicationState message, String returnInstance) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSFSBStoreManager>>processQueryMessage:returnInstance= " + returnInstance);
        }        
        ReplicationStateQueryResponse response = this.doProcessQueryMessage(message);
        boolean isResponse = response.isResponse();
        ReplicationState responseState = response.getState();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("processQueryMessage:after doProcessQueryMessage:response=" + isResponse);
            _logger.fine("processQueryMessage:after doProcessQueryMessage:responseState=" + responseState);            
        }         
        //don't send a response to a response
        if(!isResponse && responseState != null) {
            //FIXME point-to-point response back to sender
            //System.out.println("processQueryMessage - need to send back result to " + returnInstance);
            try {
                //doSendQueryResponse(responseState, this.getInstanceName());
                doSendQueryResponse(responseState, returnInstance);
            } catch (SFSBStoreManagerException ex) {
                //FIXME log this
            }            
        }
    }     
    
    //return true if message is processQueryResponse
    public ReplicationStateQueryResponse doProcessQueryMessage(ReplicationState message) {
        ReplicationState resultState = null;
        String methodName = getProcessMethodName(message);
        //System.out.println("in ReplicatedSFSBStoreManager>>doProcessQueryMessage:methodName=" + methodName);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in ReplicatedSFSBStoreManager>>doProcessQueryMessage:methodName=" + methodName);
        }        
        try {
            Class myClass = this.getClass();
            resultState = (ReplicationState) myClass.getMethod(
                methodName,
                    new Class[]{ message.getClass() }).invoke(
                        this, new Object[]{ message });            
        } catch (IllegalAccessException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicatedSFSBStoreManager>>doProcessQueryMessage:methodName=" + methodName + "illegalAccessException");              
            }                        
        } catch (NoSuchMethodException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicatedSFSBStoreManager>>doProcessQueryMessage:methodName=" + methodName + "noSuchMethodException");              
            }                        
        } catch (InvocationTargetException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("in ReplicatedSFSBStoreManager>>doProcessQueryMessage:methodName=" + methodName + "invocationTargetException");
                _logger.fine("invocationException.getCause()= " + ex.getCause());
            }             
            ex.printStackTrace();
        }
        boolean isResponse = methodName.equals("processBroadcastresponse");
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationWebEventPresistentManager>>doProcessQueryMessage:resultState=" + resultState);
        }         
        return new ReplicationStateQueryResponse(resultState, isResponse);
    }      
    
    /**
    * send the response
    *
    * @param sessionState 
    *   The replication state response
    */    
    public void doSendResponse(ReplicationState sessionState) 
        throws SFSBStoreManagerException {
        
        StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            store.sendResponse(sessionState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "doSendResponse");
            }            
        }
        catch (Exception e) {
            //e.printStackTrace();
            //throw e;            
            throw new SFSBStoreManagerException("Error sending ReplicationState response: id =" + sessionState.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "doSendResponse");
            }
        }        
    } 
    
    /**
    * send the response
    *
    * @param sessionState the replication state response
    * @param instanceName  the name of the target instance
    */    
    public void doSendQueryResponse(ReplicationState sessionState, String instanceName) 
        throws SFSBStoreManagerException {
        
        StorePool storePool = this.getStorePool();
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            store.sendQueryResponse(sessionState, instanceName);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "doSendQueryResponse");
            }            
        }
        catch (Exception e) {
            //e.printStackTrace();
            //throw e;            
            throw new SFSBStoreManagerException("Error sending ReplicationState query response: id =" + sessionState.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "doSendQueryResponse");
            }
        }        
    }     
    
    /**
    * append the debug monitor statistics to the buffer
    */     
    public void appendStats(StringBuffer sb) {
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        sb.append("\nSAVE_LOW=" + stats.getSaveLow());
        sb.append("\nSAVE_HIGH=" + stats.getSaveHigh());
        sb.append("\nSAVE_AVG=" + stats.getSaveAverage());
        
        sb.append("\nPASSIVATE_SAVE_LOW=" + stats.getPassivateSaveLow());
        sb.append("\nPASSIVATE_SAVE_HIGH=" + stats.getPassivateSaveHigh());
        sb.append("\nPASSIVATE_SAVE_AVG=" + stats.getPassivateSaveAverage()); 
        
        sb.append("\nCHECKPOINT_SAVE_LOW=" + stats.getCheckpointSaveLow());
        sb.append("\nCHECKPOINT_SAVE_HIGH=" + stats.getCheckpointSaveHigh());
        sb.append("\nCHECKPOINT_SAVE_AVG=" + stats.getCheckpointSaveAverage());              

        sb.append("\nBEAN_SIZE_LOW=" + stats.getBeanSizeLow());
        sb.append("\nBEAN_SIZE_HIGH=" + stats.getBeanSizeHigh());
        sb.append("\nBEAN_SIZE_AVG=" + stats.getBeanSizeAverage());
        
        stats.resetStats();       
    }
    
    //new code start    
    
    /** Removes the bean will be called when the client removes the bean
     * @param id ID of the bean to be removed
     * (see removeNoBatch which removes immediately w/o batching)
     */
    /* FIXME reverting back to non-batch remove for testing
    public void remove(Object id) { 
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "remove", id);
        }
        if(id == null) {
            return;
        }
        String beanId = id.toString();
        //rLock.lock();
        try {
            removedKeysMap.put(beanId, beanId);
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
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "remove");
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
            _logger.fine(">>ReplicationSFSBStoreManager::flushAllIds: " + oldKeysMap.size());
        }         
        //_logger.log(Level.INFO, ">>ReplicationSFSBStoreManager::flushAllIds: " + oldKeysMap.size());
        
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
            _logger.fine("<<ReplicationWebEventPersistentManager::createRemoveAllMessageAndSend: About to send " + data.length + " bytes...");
        }                
        removeIds(_messageIDCounter++, list.size(), data, signalObject);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("<<ReplicationWebEventPersistentManager::createRemoveAllMessageAndSend: DONE!!");
        }
    }
    
   /**
   * does the remove of all session ids in removedIdsData
   *
   * @param msgID message id for this remove all message
   * @param removedIdsData serialized list of ids to remove
   */    
    protected void removeIds(long msgID, int totalStates, byte[] removedIdsData, Object signalObject) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedSFSBStoreManager", "removeIds");
        }
        ReplicatedEjbStore store = null;
        try {
            store = getStore();
            
            store.removeIds(msgID, removedIdsData);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.exiting("ReplicatedSFSBStoreManager", "removeIds");
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.putStore(store);
        }        
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedSFSBStoreManager", "removeIds");
        }
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
    
}
