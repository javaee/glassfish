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
 * ReplicatedEjbStore.java
 *
 * Created on June 1, 2006, 10:03 AM
 *
 */

package com.sun.ejb.ee.sfsb.store;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

//import com.sun.appserv.ha.impl.*;
import com.sun.appserv.ha.spi.*;

import com.sun.ejb.Container;
import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;

import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.ee.web.sessmgmt.JxtaBackingStoreImpl;
import com.sun.enterprise.ee.web.sessmgmt.JxtaReplicationSender;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationManager;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationMessageRouter;
import com.sun.enterprise.ee.web.sessmgmt.ReplicationState;

import com.sun.logging.LogDomains;

/**
 *
 * @author Larry White
 */
public class ReplicatedEjbStore extends EJBStoreBase {
    
    public final static String LOGGER_MEM_REP 
        = "com.sun.ejb.ee.sfsb.store";    
    
    /** Logger for logging
     */
    /*
    private final static Logger _logger 
        = LogDomains.getLogger(LogDomains.EJB_LOGGER);
     */
    
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);
    
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "ReplicatedEjbStore/1.0";

    /**
     * Name to register for this Store, used for logging.
     */    
    private static final String storeName = "ReplicatedEjbStore";
    
    final static String MODE_EJB
        = ReplicationState.MODE_EJB;    
   
    final static String UNDEPLOY_COMMAND 
        = ReplicationState.UNDEPLOY_COMMAND;
    final static String REMOVE_EXPIRED_COMMAND 
        = ReplicationState.REMOVE_EXPIRED_COMMAND; 
    final static String UPDATE_LAST_ACCESS_TIME_COMMAND 
        = ReplicationState.UPDATE_LAST_ACCESS_TIME_COMMAND;
    final static String REMOVE_COMMAND 
        = ReplicationState.REMOVE_COMMAND;    

    static {
        //_logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
        //info = "ReplicatedEjbStore/1.0";
        //storeName = "ReplicatedEjbStore";        
    }    
    
    /** Creates a new instance of ReplicatedEjbStore */
    public ReplicatedEjbStore() {
        threadName = "ReplicatedEjbStore";        
    }
    
    /** Manager to which the store is assigned
     */
    protected SFSBStoreManager manager;
    
    /** Container to which this store belongs
     */
    private Container container = null;
    
    /** clusterid of the instance
     */
    private String clusterID = null;
    
    /** containerid of this container
     */
    private String containerID = null;
    
    /**
     * Controls the verbosity of the web container subsystem's debug messages.
     *
     * This value is non-zero only when the level is one of FINE, FINER
     * or FINEST.
     *
     */
    protected int _debug = 1;
    
    /** 
     * return if monitoring is enabled
     * @return if monitoring is enabled
     */    
    protected boolean isMonitoringEnabled() {                   
        BaseSFSBStoreManager mgr = (BaseSFSBStoreManager)getSFSBStoreManager();
        return mgr.isMonitoringEnabled();
    }
    
    /** 
     * return EJBModuleStatistics
     * @return EJBModuleStatistics
     */    
    protected EJBModuleStatistics getEJBModuleStatistics() {                   
        BaseSFSBStoreManager mgr = (BaseSFSBStoreManager)getSFSBStoreManager();
        return mgr.getEJBModuleStatistics();
    } 
    
    /** prints the debug messages
     * @param message
     */
    protected void debug(String message) {
        System.out.println(message);
    }
    
    /** Returns the Container
     * @return
     */
    public Container getContainer() {
        return container;
    }

    /** Returns the containerID
     * @return
     */
    protected String getContainerId() {
        return this.containerID;
    }
    
    protected long getContainerIdAsLong() {
        Long longContId = new Long(getContainerId());
        return longContId.longValue();
    }

    /** Sets the containerId for this container
     * @param containerId  */
    protected void setContainerId(String containerId) {
        this.containerID = containerId;
    }

    /** Sets the container
     * @param container  */
    public void setContainer(Container container) {
        this.container = container;
    } 
    
    /**
     * This returns the number of all the beans corresponding to the "containerId" 
     */    
    public int getContainerSize() {
        //FIXME for given instance or across all instances in cluster?
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "ReplicatedEjbStore.getContainerSize containerId = " + this.containerID);
        }        
        int result = 0;                
        return result;
    }
    
    /** aggregate save method
     * @param beanStates
     * @param startTime
     * @throws IOException
     */
    public void save(SFSBBeanState[] beanStates, long startTime) throws IOException {
        long eachStartTime = startTime;
        long eachEndTime = 0L;        
        for(int i=0; i<beanStates.length; i++) {
            SFSBBeanState sfsBean = beanStates[i];
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                    "ReplicatedEjbStore aggregate save bean[" + i + "]=" + sfsBean);
            }
            BackingStore replicator = this.getBackingStoreForBean(sfsBean);
            if(replicator == null) {
                //this should not happen
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,
                        "ReplicatedEjbStore aggregate save bean[" + i + "]=" + "unable to find replicator");
                }                
                return;
            }
            save(sfsBean, replicator);
            if(this.isMonitoringEnabled()) {
                //increment storage duration for each bean
                eachEndTime = System.currentTimeMillis();
                long storeDuration = eachEndTime - eachStartTime;
                sfsBean.setTxCheckpointDuration(sfsBean.getTxCheckpointDuration() + storeDuration);
                eachStartTime = eachEndTime;
            }
        }        
    }
    
    /** Saves the beanstate
     * @param sfsBean
     * @param mgr
     * @throws IOException
     */
    public void save(SFSBBeanState sfsBean, SFSBStoreManager mgr) throws
        IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "save", new Object[] {sfsBean, mgr});
        }
        //System.out.println("ReplicatedEjbStore>>save:mgr:" + mgr + " bean:" + sfsBean.getId());        
        this.manager = mgr;
        save(sfsBean);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "save");
        }
    }
    
    /** Saves the beanstate
     * @param sfsBean
     * @throws IOException  */
    public void saveOldAPI(SFSBBeanState sfsBean) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "save", sfsBean);
        }        
        long tempStartTime = System.currentTimeMillis();
        this.transmitSession(sfsBean, "save", true);
        //this.transmitSession(sfsBean, "save", false);
        System.out.println("ReplicatedEjbStore>>save time = " + (System.currentTimeMillis() - tempStartTime));         
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "save");
        }
    }
    
    /** Saves the beanstate
     * @param sfsBean
     * @throws IOException  */
    public void save(SFSBBeanState sfsBean) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "save", sfsBean);
        }
        //System.out.println("ReplicatedEjbStore>>save:bean:" + sfsBean.getId());
        long tempStartTime = System.currentTimeMillis();
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return;
        }
        BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API save: replicator: " + replicator);
        SimpleMetadata simpleMetadata =
            createSimpleMetadata(sfsBean);
        try {
            replicator.save(sfsBean.getId().toString(), //id
                    simpleMetadata);  //SimpleMetadata
        } catch (BackingStoreException ex) {
            //FIXME
        }
        //System.out.println("ReplicatedEjbStore>>save time = " + (System.currentTimeMillis() - tempStartTime));         
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "save");
        }
    }
    
   /** Saves the beanstate
     * @param sfsBean
     * @param replicator 
     * @throws IOException  */
    public void save(SFSBBeanState sfsBean, BackingStore replicator) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "save", sfsBean);
        }        
        long tempStartTime = System.currentTimeMillis();
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return;
        }
        //BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API save: passed in replicator: " + replicator);
        SimpleMetadata simpleMetadata =
            createSimpleMetadata(sfsBean);
        try {
            replicator.save(sfsBean.getId().toString(), //id
                    simpleMetadata);  //SimpleMetadata
        } catch (BackingStoreException ex) {
            //FIXME
        }
        //System.out.println("ReplicatedEjbStore>>save time = " + (System.currentTimeMillis() - tempStartTime));         
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "save");
        }
    }       
    
    /** Saves the state
     * @param state
     * @param mgr
     * @throws IOException
     */
    public void saveForRepair(ReplicationState state, SFSBStoreManager mgr) throws
        IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "saveForRepair", new Object[] {state, mgr});
        }
        this.manager = mgr;
        saveForRepair(state);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "saveForRepair");
        }
    }    
    
    /** Saves the state
     * @param state
     * @throws IOException  */
    public void saveForRepair(ReplicationState state) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "saveForRepair", state);
        }        
        long tempStartTime = System.currentTimeMillis();
        this.transmitState(state, "save", true);
        //this.transmitState(state, "save", false);
        //System.out.println("ReplicatedEjbStore>>saveForRepair time = " + (System.currentTimeMillis() - tempStartTime));         
        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "saveForRepair");
        }
    }
    
    private BackingStore getBackingStoreForBean(SFSBBeanState beanState) {
        BackingStore result = null;
        long longContId = beanState.getContainerId();
        String containerId = getContainerIDAsString(longContId);
        ReplicationMessageRouter messageRouter 
            = ReplicationMessageRouter.createInstance();
        ReplicationManager mgr 
            = messageRouter.findApp(containerId);
        if(mgr != null && mgr instanceof ReplicatedSFSBStoreManager) {
            result = ((ReplicatedSFSBStoreManager)mgr).getBackingStore();
        }
        return result;
    }
        
    public String getContainerIDAsString(long containerID) {
        Long longContId = new Long(containerID);
        return longContId.toString();
    }        
    
    /** Loads the state of the bean from the DB
     * @param id
     * @throws IOException
     * @return
     */
    public SFSBBeanState loadBean(Object id) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "loadBean", id);
        }
        if (id == null) {
             return null;
        }
        String idString = id.toString();
        SFSBBeanState bean = null;
        
        //System.out.println("ReplicatedEjbStore>>loadBean:id: " + id.toString() + " stacktrace follows:");
        //Thread.dumpStack();
        ReplicatedSFSBStoreManager repMgr =
            (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        //ReplicationState localCachedState = repMgr.getFromReplicationCache(idString);
        ReplicationState localCachedState = repMgr.transferFromReplicationCache(idString);
        //System.out.println("ReplicatedEjbStore>>loadBean:localCachedState =" + localCachedState);    
        //get broadcast result state
        ReplicationState broadcastResultState  = findBeanViaBroadcast(idString);
        //System.out.println("ReplicatedEjbStore>>loadBean:broadcastResultState from broadcast=" + broadcastResultState);
        ReplicationState bestState 
            = getBestResult(localCachedState, broadcastResultState);
        //System.out.println("ReplicatedEjbStore>>loadBean:bestState from broadcast=" + bestState);    
        if(bestState != null && bestState.getState() != null) {
            bean = createBeanState(bestState);
        }
        //System.out.println("ReplicatedEjbStore>>loadBean:id " + idString + " bean: " + bean);        

        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "loadBean", bean);
        }
        return bean;
    }
    
    private ReplicationState getBestResult(ReplicationState localState, ReplicationState broadcastResultState) {
        //System.out.println("getBestResult:localState=" + localState + "other=" + broadcastResultState);        
        if(localState == null) {
            return broadcastResultState;
        }
        //localState is not null
        if(broadcastResultState == null) {
            return localState;
        }
        //both are non-null
        if(broadcastResultState.getVersion() >= localState.getVersion()) {
            return broadcastResultState;
        } else {
            return localState;
        }
    }    
    
    private ReplicationState findBeanViaBroadcastOldAPI(String id) {

        //FIXME this is from web tier do multi-cast to cluster member and get back state
        //insure the result will come back correctly
        ReplicationState state = 
            ReplicationState.createBroadcastQueryState(MODE_EJB, id, this.getContainerId(), this.getInstanceName());
        JxtaReplicationSender sender 
            = JxtaReplicationSender.createInstance();
        ReplicationState result = sender.sendReplicationStateQuery(state);
        return result;
    } 
    
    /**
     * return the name of this instance
     * @returns instanceName
     */    
    public String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }    
    
    private ReplicationState findBeanViaBroadcast(String id) {

        //FIXME this is from web tier do multi-cast to cluster member and get back state
        //insure the result will come back correctly       
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return null;
        }
        BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API findBeanViaBroadcast: replicator: " + replicator);
        SimpleMetadata simpleMetadata = null;
        try {
            simpleMetadata = (SimpleMetadata)replicator.load(id); //id
        } catch (BackingStoreException ex) {
            //FIXME
        }
        ReplicationState result = null;
        if(simpleMetadata != null) {
            result = ReplicationState.createReplicationState(MODE_EJB, id, null, simpleMetadata);
        }
        return result;
    }     
    
    /**
     * Remove the Session with the specified session identifier from
     * this Store, if present.  If no such Bean is present, this method
     * takes no action.
     *
     * @param id Bean identifier of the SFSB to be removed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean removeOldAPI(Object id) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "remove", id);
        }
        //FIXME add rest
        boolean result = false;
        if(id == null) {
            return result;
        }
        ReplicationState transmitState 
            = createSparseReplicationState(id.toString(), REMOVE_COMMAND, -1L);          
        this.doTransmit(transmitState);        

        if(_logger.isLoggable(Level.FINE)) {
            _logger.exiting("ReplicatedEjbStore", "remove", new Boolean(result));
        }
        return result;
    }
    
    /**
     * Remove the Session with the specified session identifier from
     * this Store, if present.  If no such Bean is present, this method
     * takes no action.
     *
     * @param id Bean identifier of the SFSB to be removed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean remove(Object id) throws IOException {
        //System.out.println("ReplicatEjbStore>>remove");
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "remove", id);
        }
        //FIXME add rest
        boolean result = false;
        if(id == null) {
            return result;
        }
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return result;
        }
        BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API remove: replicator: " + replicator);
        try {
            replicator.remove(id.toString());    //bean id 
        } catch (BackingStoreException ex) {
            //FIXME
        }
        if(_logger.isLoggable(Level.FINE)) { 
            _logger.exiting("ReplicatedEjbStore", "remove", new Boolean(result));
        }
        return result;       
    }    
    
    /** This method deletes all the beans corresponding to the "containerId"
     * that should be expired
     * @return number of removed beans
     */    
    public int removeExpiredSessionsOldAPI() {
        _logger.finest("IN ReplicatedEjbStore>>removeExpiredSessions");
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "ReplicatedEjbStore.removeExpiredSessions containerId = " + this.containerID);
        }
        int result = 0;
        ReplicationState transmitState 
            = createSparseReplicationState(this.getContainerId(), REMOVE_EXPIRED_COMMAND, -1L);         
        this.doTransmit(transmitState);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicatedEjbStore>>removeExpiredSessions():number of expired beans = " + result);
        }
        return result;

    } 
    
    /** This method deletes all the beans corresponding to the "containerId"
     * that should be expired
     * @return number of removed beans
     */    
    public int removeExpiredSessions() {        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN ReplicatedEjbStore>>removeExpiredSessions");
            _logger.log(Level.FINE,
                        "ReplicatedEjbStore.removeExpiredSessions containerId = " + this.containerID);
        }
        int result = 0;
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return result;
        }
        BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API removeExpiredSessions: replicator: " + replicator); 
        try {
            replicator.removeExpired();
        } catch (BackingStoreException ex) {
            //FIXME
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedEjbStore>>removeExpiredSessions():number of expired beans = " + result);
        }
        return result;

    }     
    
    /**
     * This deletes all the beans corresponding to the "containerId" 
     */    
    public void undeployContainerOldAPI() {
        _logger.finest("IN ReplicatedEjbStore>>undeployContainer");
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "ReplicatedEjbStore.undeployContainer containerId = " + this.containerID);
        }
        ReplicationState transmitState 
            = createSparseReplicationState(this.getContainerId(), UNDEPLOY_COMMAND, -1L);
        this.doTransmit(transmitState);
        return;

    }
    
    /**
     * This deletes all the beans corresponding to the "containerId" 
     */    
    public void undeployContainer() {        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN ReplicatedEjbStore>>undeployContainer");
            _logger.log(Level.FINE,
                        "ReplicatedEjbStore.undeployContainer containerId = " + this.containerID);
        }
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return;
        }
        BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API undeployContainer: replicator: " + replicator);
        try {
            replicator.destroy();
        } catch (BackingStoreException ex) {
            //FIXME
        }
        return;

    }    
    
    /** This method updates only the last access time for a bean
     * @param sessionKey
     * @param time
     * @throws IOException
     */
    public void updateLastAccessTimeOldAPI(Object sessionKey, long time) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "ReplicatedEjbStore.updateLastAccessTime id = " + sessionKey);
        }
        ReplicationState transmitState 
            = createSparseReplicationState(sessionKey.toString(), UPDATE_LAST_ACCESS_TIME_COMMAND, time);
        this.doTransmit(transmitState);        
        
    } 
    
    /** This method updates only the last access time for a bean
     * @param sessionKey
     * @param time
     * @throws IOException
     */
    public void updateLastAccessTime(Object sessionKey, long time) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
                        "ReplicatedEjbStore.updateLastAccessTime id = " + sessionKey);
        }
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return;
        }
        BackingStore replicator = mgr.getBackingStore();
        //System.out.println("in new API updateLastAccessTime: replicator: " + replicator);
        try {
            //FIXME when Mahesh fix for version comes in
            replicator.updateLastAccessTime(sessionKey.toString(), time, 0L);
        } catch (BackingStoreException ex) {
            //FIXME
        }
        
    }     
    
    /**
     * Helper routine that cleans up and resets sessions cache
     * FIXME
     */
    public void cleanup() {
        /*FIXME
        closeStatements();
        closeConnection();
         */
    }
    
    // communication related methods
    protected void transmitSession(SFSBBeanState beanState, String command) throws IOException {
        ReplicationState transmitState =
            createReplicationState(beanState, command);

        //System.out.println("ReplicatedEjbStore>>transmitSession");
        this.doTransmit(transmitState);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitSession(SFSBBeanState beanState, String command, boolean wait) throws IOException {
        ReplicationState transmitState =
            createReplicationState(beanState, command);

        //System.out.println("ReplicatedEjbStore>>transmitSession");
        this.doTransmit(transmitState, wait);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitState(ReplicationState state, String command) throws IOException {
        ReplicationState transmitState =
            createRepairReplicationState(state, command);

        //System.out.println("ReplicatedEjbStore>>transmitState");
        this.doTransmit(transmitState);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitState(ReplicationState state, String command, boolean wait) throws IOException {
        ReplicationState transmitState =
            createRepairReplicationState(state, command);

        //System.out.println("ReplicatedEjbStore>>transmitState");
        this.doTransmit(transmitState, wait);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }     
    
    /**
     * @param transmitState state to be transmitted
     */    
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doTransmit(ReplicationState transmitState) {
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationState(transmitState);
        return resultState;
    }
    
    /**
     * @param transmitState state to be transmitted
     * @param wait
     */
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doTransmit(ReplicationState transmitState, boolean wait) {
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationState(transmitState, wait);
        return resultState;
    }
    
    //state is already formatted as a response
    public void sendResponse(ReplicationState state) {
        //System.out.println("ReplicationStore>>sendResponse");
        //FIXME
        this.doReturnTransmit(state);
    }
    
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doReturnTransmit(ReplicationState transmitState) {
        //FIXME for now test version
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationStateResponse(transmitState);
        return resultState;
    }
    
    public void sendQueryResponse(ReplicationState state, String returnInstance) {
        System.out.println("ReplicationEjbStore>>sendQueryResponse");
        //FIXME
        this.doReturnQueryTransmit(state, returnInstance);
    }
    
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doReturnQueryTransmit(ReplicationState transmitState, String returnInstance) {
        //FIXME for now test version
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationStateQueryResponse(transmitState, returnInstance);
        return resultState;
    }     
    
    protected ReplicationState createReplicationState(SFSBBeanState beanState, String command) 
        throws IOException {

        byte[] sfsbState = beanState.getState();
        
        ReplicationState transmitState =
            new ReplicationState(MODE_EJB,  //ejb mode
                beanState.getId().toString(), //id  
                this.getContainerId(),     //containerId analog of appid here
                beanState.getVersion(),    //version
                beanState.getLastAccess(),  //lastaccesstime
                this.getIdleTimeoutInSeconds(), //maxInactiveInterval (seconds)
                null,  //extraParam (not used here)
                null,   //queryResult (not used here)
                null,  //FIXME instanceName
                command,         //command
                sfsbState,      //state
                null);          //trunkState (not used here)
        //System.out.println("ReplicatedEjbStore>>createReplicationState");
        return transmitState;

    }
    
    protected ReplicationState createSparseReplicationState(String id, String command, long lastAccessTime) {

        ReplicationState transmitState =
            new ReplicationState(MODE_EJB,  //ejb mode
                id,        //id
                this.getContainerId(),     //containerId analog of appid here
                0L,             //version FIXME later to support version
                lastAccessTime,  //lastaccesstime
                this.getIdleTimeoutInSeconds(), //maxInactiveInterval (seconds)
                null,  //extraParam (not used here)
                null,   //queryResult (not used here)
                null,  //FIXME instanceName
                command,    //command
                null,      //state
                null);      //trunkState (not used here)
        //System.out.println("ReplicatedEjbStore>>createSparseReplicationState");
        return transmitState;

    }
    
    protected ReplicationState createRepairReplicationState(ReplicationState state, String command) 
        throws IOException {

        ReplicationState transmitState =
            new ReplicationState(MODE_EJB,  //ejb mode
                state.getId().toString(), //id  
                state.getAppId(),     //containerId analog of appid here
                state.getVersion(),     //version
                state.getLastAccess(),  //lastaccesstime
                state.getMaxInactiveInterval(), //maxInactiveInterval (seconds)
                null,  //extraParam not used here
                null,   //queryResult (not used here)
                null,  //FIXME instanceName
                command,         //command
                state.getState(),      //state
                null);              //trunkState (not used here)
        //System.out.println("ReplicatedEjbStore>>createRepairReplicationState");
        return transmitState;

    }    
    
    protected SFSBBeanState createBeanState(ReplicationState replicationState) {
        SFSBBeanState resultBeanState = new SFSBBeanState(getClusterID(),    //clusterid
                getContainerIdAsLong(), //containerid (long)
                replicationState.getId(), //sessionid
                replicationState.getLastAccess(), //lastAccess
                false, //isNew
                replicationState.getState(), //state
                getSFSBStoreManager() //storeManager
                );
        resultBeanState.setVersion(replicationState.getVersion());
        return resultBeanState;
                
    }
    
    protected SimpleMetadata createSimpleMetadata(SFSBBeanState beanState) 
        throws IOException {

        byte[] sfsbState = beanState.getState();
        SimpleMetadata metaData =
            new SimpleMetadata(beanState.getVersion(), //version
                beanState.getLastAccess(), //lastaccesstime
                this.getIdleTimeoutInSeconds(), //maxInactiveInterval (seconds)
                sfsbState,      //state
                null);           //extraParam not used here
        //System.out.println("ReplicatedEjbStore>>createSimpleMetadata");
        return metaData;
    }    
    
    long getIdleTimeoutInSeconds() {
        long result = -1L;
        ReplicatedSFSBStoreManager mgr
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr != null) {
            result = Long.valueOf(mgr.getIdleTimeoutInSeconds()).longValue();
        }
        return result;
    }
    
    /**
    * Remove the list of bean ids in removedIdsData from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param msgID message id for this remove all message
    * @param removedIdsData serialized list of ids to remove
    *
    * @exception IOException if an input/output error occurs
    */
    public void removeIds(long msgID, byte[] removedIdsData) throws IOException  {
        
        //System.out.println("ReplicatEjbStore>>removeIds");
        if(_logger.isLoggable(Level.FINE)) {
            _logger.entering("ReplicatedEjbStore", "removeIds", msgID);
        }
        ReplicatedSFSBStoreManager mgr 
            = (ReplicatedSFSBStoreManager)this.getSFSBStoreManager();
        if(mgr == null) {
            return;
        }
        JxtaBackingStoreImpl replicator = (JxtaBackingStoreImpl)mgr.getBackingStore();
        //System.out.println("in new API remove: replicator: " + replicator);
        try {
            replicator.removeIds(msgID, removedIdsData);
        } catch (BackingStoreException ex) {
            //FIXME
        }
        if(_logger.isLoggable(Level.FINE)) { 
            _logger.exiting("ReplicatedEjbStore", "removeIds");
        }
        return;        
    }         
    
}
