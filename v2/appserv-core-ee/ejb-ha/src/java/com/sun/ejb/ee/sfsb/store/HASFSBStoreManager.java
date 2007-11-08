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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.ejb.ee.sfsb.store;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import com.sun.ejb.Container;
import com.sun.logging.LogDomains;

import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBTxStoreManager;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManagerException;
import com.sun.ejb.spi.sfsb.util.SFSBUUIDUtil;
//FIXME: remove this interface later
import com.sun.ejb.spi.monitorable.sfsb.MonitorableSFSBStore;
import com.sun.ejb.spi.stats.MonitorableSFSBStoreManager;

//import com.sun.ejb.ee.sfsb.util.HASFSBUUIDUtilImpl;

import com.sun.enterprise.ee.web.sessmgmt.StorePool;
import com.sun.enterprise.ee.web.sessmgmt.StoreFactory;
import com.sun.enterprise.ee.web.sessmgmt.StorePoolElement;
import com.sun.enterprise.ee.web.sessmgmt.HAStorePoolElement;
import com.sun.enterprise.ee.web.sessmgmt.EEHADBHealthChecker;
import com.sun.enterprise.config.serverbeans.EjbContainer;

import java.util.logging.Level;

/** HASFSBStoreManager.java
 * Manager which handle the pool of ha ejb stores. Also responsible for shutting down all the connections
 * @author Sridhar Satuloori<Sridhar.Satuloori@Sun.Com>
 */

public class HASFSBStoreManager extends HASFSBStoreManagerBase 
    implements SFSBStoreManager, SFSBTxStoreManager, MonitorableSFSBStoreManager, MonitorableSFSBStore {
    
    //protected SFSBUUIDUtil _uuidUtil = null;
    
    /** Pool of HAEjbStore StorePool elements
     */
    StorePool _pool = null;
    
    //	private HAEjbStore store=null;
    
    /** Creates the pool of HAEjbStore elements
     */
    public HASFSBStoreManager() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "HASFSBStoreManager  loaded successfully......");
        }
        StoreFactory haStoreFactory = new HAEjbStoreFactory();
        _pool = new StorePool(StorePool.DEFAULT_INITIAL_SIZE,
        StorePool.DEFAULT_UPPER_SIZE,
        StorePool.DEFAULT_POLL_TIME, haStoreFactory);
        //_uuidUtil = new HASFSBUUIDUtilImpl();
    }
    
    /** creates the pool of store elements and attached to a container
     * @param container Container to which this manager is attached to
     */
    public HASFSBStoreManager(Container container) {
        this();
        super.setContainer(container);
    }
    
    /** return the StorePool this manager holds
     * @return Returns the pool it holds
     */
    public StorePool getStorePool() {
        return _pool;
    }
    
    /** set the StorePool
     * @param pool pool of HAEjbStore
     */
    public void setStorePool(StorePool pool) {
        _pool = pool;
    }
    
    /** dummy methods part of the SFSBStore Manager
     * doesnt apply here
     * YES IT DOES - see better version below
     *  remove when satisfied with replacement below
     */
    /*
    public void removeExpired() {
        
    }
     */
    
    /** will be called at the time of graceful shutdown
     */
    public void shutdown() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "HASFSBStoreManager.shutdown");
        }
        this.doShutdownCleanup();
    }
 
    /** UUID generator attached to this manager
     * @return
     */
    /*
    public SFSBUUIDUtil getUUIDUtil() {    
        return _uuidUtil;
    }
     */
    
    /** Saves the beanstate to the HADB
     * @param beanState SFSBBeanState
     * @throws SFSBStoreManagerException
     */
    public void passivateSave(SFSBBeanState beanState) throws SFSBStoreManagerException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            //FIXME which of these next 2 lines is right??
            //throw new SFSBStoreManagerException("Error during passivateSave: HADB is unhealthy - id =" + beanState.getId().toString());
            return;
        }        
        //passivateSave(beanState, false);
        //fix take isNew cue from beanState - not default to false
        //added for monitoring
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring
        passivateSave(beanState, beanState.isNew());
        //added for monitoring      
        if(this.isMonitoringEnabled()) {
            long saveEndTime = System.currentTimeMillis();
            stats.processPassivateSave(saveEndTime - saveStartTime);
            stats.processBeanSize(this.getBeanSize(beanState));
        }
        //end added for monitoring        
    }   
    
    /** Saves the state of the hadb
     * @param beanState SFSBBeanState
     * @param isNew bean is newly created or already saved to DB
     * @throws SFSBStoreManagerException
     */
    public void passivateSave(SFSBBeanState beanState, boolean isNew) 
        throws SFSBStoreManagerException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            //FIXME which of next 2 lines is right???
            //throw new SFSBStoreManagerException("Error during passivateSave: HADB is unhealthy - id =" + beanState.getId().toString());        
            return;
        }        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "passivateSave",
                new Object[] {beanState, new Boolean(isNew)});
        }
        StorePool storePool = this.getStorePool();
        
        HAEjbStore store = null;
        try {
            store = getStore();            
            ( (HAEjbStore) store).save(beanState, this, beanState.isNew());
        }
        catch (IOException e) {
            //e.printStackTrace();
            throw new SFSBStoreManagerException("Error during passivateSave: isNew =" + isNew + ": id =" + beanState.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
        }
        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "passivateSave");
        }
    }     
    
    /** Returns a store from the pool This method intializes the store with right parameters
     * @return returns HAEjbStore
     */
    private HAEjbStore getStore() {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "getStore");
        }
        HAEjbStore store = null;
        try {
            store = (HAEjbStore) _pool.take();
            store.setContainer(this.getContainer());
            store.setClusterID(this.getClusterID());
            store.setContainerId(this.getContainerID());
            store.setSFSBStoreManager(this);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "HASFSBStoreManager.getStore returning   " + store);
            }
            return store;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "getStore", store);
        }
        return store;
    }
    
    /** 
     *  Returns (puts) a store back to the pool
     */
    private void putStore(HAEjbStore store) {    
        ( (HAEjbStore) store).setContainer(null);
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
    
    /** gets the state of the bean from HADB
     * @param id ID of the bean to be loaded from the HADB
     * @throws SFSBStoreManagerException
     * @return returns the bean with the state
     */
    public SFSBBeanState getState(Object id) throws SFSBStoreManagerException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "getState", id);
        }
        SFSBBeanState sfsbState = null;
        StorePool storePool = this.getStorePool();
        HAEjbStore store = null;
        try {
            store = getStore();
            
            sfsbState = store.loadBean(id);
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "getStore", sfsbState);
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
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "getStore", sfsbState);
            }
            //return sfsbState;
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
        //System.out.println("Printing Checkpoint Durations");
        for(int i=0; i<beanStates.length; i++) {
            SFSBBeanState nextBeanState = beanStates[i];
            System.out.println("printTxCheckpointDurations for beanState[" + i + "]: "
                + nextBeanState.getTxCheckpointDuration()); 
        }
        return;
    }    
    
    /**
     * @param beanStates
     * @throws SFSBStoreManagerException
     */
    public void checkpointSave(SFSBBeanState[] beanStates) throws SFSBStoreManagerException {        
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }   
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
        _logger.entering("HASFSBStoreManager", "checkpointSave",
        new Object[] {beanStates, new Boolean(transactionFlag)});
        _logger.log(Level.INFO, "HASFSBStoreManager.checkpointSave",
        new Object[] {beanStates, new Boolean(transactionFlag)});
        _logger.exiting("HASFSBStoreManager", "checkpointSave",
        new Object[] {beanStates, new Boolean(transactionFlag)});
         */
        
        /*
        _logger.entering("HASFSBStoreManager", "checkpointSave",
        new Object[] {beanState, new Boolean(isNew)});
         */
        StorePool storePool = this.getStorePool();
        
        HAEjbStore store = null;
        try {
            store = getStore();            
            ( (HAEjbStore) store).save(beanStates, startTime);
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
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "checkpointSave");
        }

    }
    
    /**
     * Remove all session data for this container
     * called during undeployment
     * @throws SFSBStoreManagerException
     */    
    public void removeAll() throws SFSBStoreManagerException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "removeAll", containerId);
        }
        
        StorePool storePool = this.getStorePool();
        HAEjbStore store = null;
        try {
            store = getStore();
            
            store.undeployContainer();
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "removeAll");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during HASFSBStoreManager>>removeAll for container: " + containerId, e);
        }
        finally {
            //un-register with health checker
            EEHADBHealthChecker.removeHASFSBStoreManager(this.getContainerID());            
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "removeAll");
        }
        return;
        
    }    
    
    /** Removes the bean from the hadb will be called when the client removes the bean
     * @param id ID of the bean to be removed from the DB
     */
    public void remove(Object id) {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }         
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "remove", id);
        }
        HAEjbStore store = null;
        try {
            store = getStore();
            
            store.remove(id);
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "remove");
            }
            
        }
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.putStore(store);
        }        
        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "remove");
        }
    }
    
    //++++++++++++++++NEW VERSIONS OF METHODS+++++++++++++++
    
    /**
     * Store session data in this beanState
     * This method used only for checkpointing; use passivateSave for passivating
     * @param beanState SFSBBeanState
     * @throws SFSBStoreManagerException
     */
    public void checkpointSave(SFSBBeanState beanState)
        throws SFSBStoreManagerException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }         
        //added for monitoring
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring            
        checkpointSave(beanState, beanState.isNew());
        //added for monitoring      
        if(this.isMonitoringEnabled()) {
            long saveEndTime = System.currentTimeMillis();
            stats.processCheckpointSave(saveEndTime - saveStartTime);
            stats.processBeanSize(this.getBeanSize(beanState));
        }
        //end added for monitoring         
    }
    
    /** Saves the state of the hadb
     * @param beanState SFSBBeanState
     * @param isNew bean is newly created or already saved to DB
     * @throws SFSBStoreManagerException
     */
    public void checkpointSave(SFSBBeanState beanState, boolean isNew) 
        throws SFSBStoreManagerException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "checkpointSave",
                new Object[] {beanState, new Boolean(isNew)});
        }
        StorePool storePool = this.getStorePool();
        
        HAEjbStore store = null;
        try {
            store = getStore();            
            ( (HAEjbStore) store).save(beanState, this, beanState.isNew());
        }
        catch (IOException e) {
            //e.printStackTrace();
            throw new SFSBStoreManagerException("Error during checkpointSave: isNew =" + isNew + ": id =" + beanState.getId().toString(), e); 
        }
        finally {
            this.putStore(store);
        }
        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "passivateSave");
        }
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
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "updateLastAccessTime", sessionKey.toString());
        }
        
        StorePool storePool = this.getStorePool();
        HAEjbStore store = null;
        try {
            store = getStore();
            
            store.updateLastAccessTime(sessionKey, time);
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "updateLastAccessTime");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during HASFSBStoreManager>>updateLastAccessTime for key: " 
                + sessionKey + " errMsg: " + e.getMessage(), e);
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "updateLastAccessTime");
        }
        return;              
    }
    
    /**
     * Remove all the idle/expired session data 
     * that are idle for idleTimeoutInSeconds (passed during initSessionStore())
     * @throws SFSBStoreManagerException
     */
    public void removeExpired() throws SFSBStoreManagerException {
        //FIXME remove this later it will be deprecated
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "removeExpired", containerId);
        }
        this.removeExpiredSessions();
    }
    
    /**
     * Remove all the idle/expired session data 
     * that are idle for idleTimeoutInSeconds (passed during initSessionStore())
     * @throws SFSBStoreManagerException
     */
    public void removeExpiredLastGood() throws SFSBStoreManagerException {
        //FIXME remove along with void removeExpired after testing
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "removeExpired", containerId);
        }
        
        StorePool storePool = this.getStorePool();
        HAEjbStore store = null;
        try {
            store = getStore();
            
            store.removeExpired();
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "removeExpired");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during HASFSBStoreManager>>removeExpired for container: " + containerId, e);
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "removeExpired");
        }
        return;        
    }    
    
    /**
     * Remove all the idle/expired session data 
     * that are idle for idleTimeoutInSeconds (passed during initSessionStore())
     * @throws SFSBStoreManagerException
     */
    public int removeExpiredSessions() throws SFSBStoreManagerException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return 0;
        }        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "removeExpired", containerId);
        }
        int result = 0;
        StorePool storePool = this.getStorePool();
        HAEjbStore store = null;
        try {
            store = getStore();
            
            result = store.removeExpiredSessions();
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "removeExpired");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new SFSBStoreManagerException("Error during HASFSBStoreManager>>removeExpired for container: " + containerId, e);
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "removeExpired");
        }
        return result;        
    }    
    
    // Monitoring related
    
    /**
     *Get the SFSBStoreManagerMonitor
     */
    public MonitorableSFSBStore getMonitorableSFSBStore() {
        return this;
    }
    
    /**
     *Get the SFSBStoreManagerMonitor
     */
    public MonitorableSFSBStoreManager getMonitorableSFSBStoreManager() {
        return this;
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
        
        sb.append("\nGET_CONN_LOW=" + stats.getGetConnectionLow());
        sb.append("\nGET_CONN_HIGH=" + stats.getGetConnectionHigh());
        sb.append("\nGET_CONN_AVG=" + stats.getGetConnectionAverage());
        /*
        sb.append("\nPUT_CONN_LOW=" + stats.getPutConnectionLow());
        sb.append("\nPUT_CONN_HIGH=" + stats.getPutConnectionHigh());
        sb.append("\nPUT_CONN_AVG=" + stats.getPutConnectionAverage());
         */        
        sb.append("\nSTMT_PREP_LOW=" + stats.getStatementPrepLow());
        sb.append("\nSTMT_PREP_HIGH=" + stats.getStatementPrepHigh());
        sb.append("\nSTMT_PREP_AVG=" + stats.getStatementPrepAverage());
        
        sb.append("\nEXECUTE_STMT_LOW=" + stats.getExecuteStatementLow());
        sb.append("\nEXECUTE_STMT_HIGH=" + stats.getExecuteStatementHigh());
        sb.append("\nEXECUTE_STMT_AVG=" + stats.getExecuteStatementAverage());
        /*
        sb.append("\nCOMMIT_LOW=" + stats.getCommitLow());
        sb.append("\nCOMMIT_HIGH=" + stats.getCommitHigh());
        sb.append("\nCOMMIT_AVG=" + stats.getCommitAverage()); 
        */
        sb.append("\nBEAN_SIZE_LOW=" + stats.getBeanSizeLow());
        sb.append("\nBEAN_SIZE_HIGH=" + stats.getBeanSizeHigh());
        sb.append("\nBEAN_SIZE_AVG=" + stats.getBeanSizeAverage());
        
        stats.resetStats();       
    }
    
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
    
    /**
    * return the current number of beans stored for this container
    */    
    public long getCurrentStoreSize() {
        /*
        HAEjbStore store = (HAEjbStore) this.getStore();
        return store.getContainerSize();
         */
        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HASFSBStoreManager", "getCurrentStoreSize");
        }
        int result = 0;
        //StorePool storePool = this.getStorePool();
        HAEjbStore store = null;
        try {
            store = getStore();
            
            result = store.getContainerSize();
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HASFSBStoreManager", "getCurrentStoreSize");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            this.putStore(store);
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HASFSBStoreManager", "getCurrentStoreSize");
        }
        return result;            
        
    } 
    
    /**
    * return the current number of beans stored for this container
    */    
    public long getCurrentStoreSizePrevious() {
        HAEjbStore store = (HAEjbStore) this.getStore();
        return store.getContainerSize();
    }     
    
    //All these method should be removed later when
    //the MonitorableSFSBStore interface is removed

    /**
    * return the current number of beans stored for this container
    * FIXME: remove this later it is part of old interface that 
    * is being removed MonitorableSFSBStore
    */    
    public int getCurrentSize() {
        HAEjbStore store = (HAEjbStore) this.getStore();
        return store.getContainerSize();
    }
    
    public void monitoringLevelChanged(boolean newValue) {
        //true means on -- false means off
        //FIXME do something
    }
    
    public int getCheckpointCount() {
        return 0;
    }
    
    public int getCheckpointErrorCount() {
        return 0;        
    }
    
    public int getCheckpointSuccessCount() {
        return 0;        
    }
    
    public int getExpiredSessionCount() {
        return 0;        
    }
    
    public int getLoadCount() {
        return 0;        
    }
    
    public int getLoadErrorCount() {
        return 0;        
    }
    
    public int getLoadSuccessCount() {
        return 0;        
    }
    
    public int getPassivationCount() {
        return 0;        
    }
    
    public int getPassivationErrorCount() {
        return 0;        
    }
    
    public int getPassivationSuccessCount() {
        return 0;        
    }
    
}
