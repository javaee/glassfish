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
 * ReplicationStore.java
 *
 * Created on November 17, 2005, 10:24 AM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import org.apache.catalina.*;
import org.apache.catalina.session.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.ee.web.authenticator.*;

import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.security.web.SingleSignOnEntry;

import com.sun.appserv.util.cache.BaseCache;
//import com.sun.appserv.ha.impl.*;
import com.sun.appserv.ha.spi.*;

/**
 *
 * @author Larry White
 */
public class ReplicationStore extends HAStoreBase implements HAStorePoolElement {

    public final static String LOGGER_MEM_REP 
        = ReplicationState.LOGGER_MEM_REP;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    //protected static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);    
    
    protected final static String MODE_WEB = ReplicationState.MODE_WEB;    
    
    /**
     * Creates a new instance of ReplicationStore
     */
    public ReplicationStore() { 
        setLogLevel();        
    }
    
    public String getApplicationId() {        
        if(applicationId != null) {
            return applicationId;
        }
        applicationId = "WEB:" + super.getApplicationId();
        return applicationId;
    }    

    // HAStorePoolElement methods begin
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void valveSave(Session session) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>valveSave");
        }         
        //System.out.println("ReplicationStore>>valveSave");
        HASession haSess = (HASession)session;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>valveSave:haSess isPersistent" + haSess.isPersistent());
            _logger.fine("ReplicationStore>>valveSave:haSess isDirty" + haSess.isDirty());
        }        
        //System.out.println("ReplicationStore>>valveSave:haSess isPersistent" + haSess.isPersistent());
        //System.out.println("ReplicationStore>>valveSave:haSess isDirty" + haSess.isDirty());
        if( haSess.isPersistent() && !haSess.isDirty() ) {
            this.updateLastAccessTime(session);
        } else {
            this.doValveSave(session);
            haSess.setPersistent(true);
        }
        haSess.setDirty(false);
    }    

    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void doValveSave(Session session) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>doValveSave:id =" + ((HASession)session).getIdInternal());
            _logger.fine("ReplicationStore>>doValveSave:valid =" + ((StandardSession)session).getIsValid());
            _logger.fine("ReplicationStore>>doValveSave:ssoId=" + ((HASession)session).getSsoId());            
        }        
        //System.out.println("ReplicationStore>>doValveSave:valid =" + ((StandardSession)session).getIsValid());
        //System.out.println("ReplicationStore>>doValveSave:ssoId=" + ((HASession)session).getSsoId());
        // begin 6470831 do not save if session is not valid
        if( !((StandardSession)session).getIsValid() ) {
            return;
        }
        // end 6470831         
        String userName = "";
        if(session.getPrincipal() !=null){
            userName = session.getPrincipal().getName();
            ((BaseHASession)session).setUserName(userName);
        }
        byte[] sessionState = this.getByteArray(session);
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>doValveSave replicator: " + replicator);
            _logger.fine("ReplicationStore>>doValveSave version:" + ((HASession)session).getVersion());                       
        }        
        //System.out.println("ReplicationStore>>doValveSave replicator: " + replicator);
        //System.out.println("ReplicationStore>>doValveSave version:" + ((HASession)session).getVersion());
        SimpleMetadata simpleMetadata =
            new SimpleMetadata(((HASession)session).getVersion(),  //version
                ((BaseHASession)session).getLastAccessedTimeInternal(), //lastaccesstime
                session.getMaxInactiveInterval(), //maxinactiveinterval
                sessionState, //state
                ((HASession)session).getSsoId() //ssoId
                );
                
        try {        
            replicator.save(session.getIdInternal(), //id
                    simpleMetadata);  //simpleMetadata 
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }    
    
    //state is already formatted as a response
    public void sendResponse(ReplicationState state) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>sendResponse");                       
        }        
        //System.out.println("ReplicationStore>>sendResponse");
        this.doReturnTransmit(state);
    }
    
    public void sendQueryResponse(ReplicationState state, String returnInstance) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>sendQueryResponse");                       
        }        
        //System.out.println("ReplicationStore>>sendQueryResponse");
        this.doReturnQueryTransmit(state, returnInstance);
    }
    
    protected void transmitSession(Session session, String command) throws IOException {
        ReplicationState transmitState =
            createReplicationState(session, command);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>transmitSession: command: " + command);                       
        }
        //System.out.println("ReplicationStore>>transmitSession: command: " + command);
        this.doTransmit(transmitState);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitSession(Session session, String command, boolean wait) throws IOException {
        ReplicationState transmitState =
            createReplicationState(session, command);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>transmitSession: command: " + command);                       
        }
        //System.out.println("ReplicationStore>>transmitSession: command: " + command);
        this.doTransmit(transmitState, wait);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    private boolean isMethodVoidReturn(String methodName) {
        return ReplicationState.isMethodVoidReturn(methodName);
    }
    
    protected ReplicationState createReplicationState(Session session, String command) 
        throws IOException {

        byte[] sessionState = this.getByteArray(session);
        ReplicationState transmitState =
            new ReplicationState(MODE_WEB, //web mode
                session.getIdInternal(), //id  
                getApplicationId(),     //appid
                ((HASession)session).getVersion(),  //version
                ((BaseHASession)session).getLastAccessedTimeInternal(),  //lastaccesstime
                session.getMaxInactiveInterval(), //maxInactiveInterval (seconds)                
                getSsoId((StandardSession)session),  //ssoid (extraParam)
                //getUsername((StandardSession)session),  //username
                null, //queryResult not used here
                null, //FIXME make it instanceName
                command,            //command
                sessionState,      //state
                null);        //trunkState
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>createReplicationState:transmitState = " + transmitState);                       
        }        
        //System.out.println("ReplicationStore>>createReplicationState:transmitState = " + transmitState);
        return transmitState;
    }    
    
    private void testDeserializeState(ReplicationState transmitState) {
        try {
            Session sess = this.getSession(transmitState);
            System.out.println("sess = " + sess);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void cleanup() {
        //FIXME;
    }
    
    public BaseCache getSessions(){
        //FIXME
        return null;
    }
  
    public void setSessions(BaseCache sesstable) {
        //FIXME;
    } 
    
    // HAStorePoolElement methods end
    
    // Store method begin
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void save(Session session) throws IOException {        
        HASession haSess = (HASession)session;
        if( haSess.isPersistent() && !haSess.isDirty() ) {
            this.updateLastAccessTime(session);
        } else {
            this.doSave(session);
            haSess.setPersistent(true);
        }
        haSess.setDirty(false);        
    }    
    
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void doSave(Session session) throws IOException {
        byte[] sessionState = this.getByteArray(session);
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>save: replicator: " + replicator);                       
        }         
        //System.out.println("ReplicationStore>>save: replicator: " + replicator);
        SimpleMetadata simpleMetadata =
            new SimpleMetadata(((HASession)session).getVersion(),  //version
                ((BaseHASession)session).getLastAccessedTimeInternal(), //lastaccesstime
                session.getMaxInactiveInterval(), //maxinactiveinterval
                sessionState, //state
                ((HASession)session).getSsoId() //ssoId
                );
                
        try {        
            replicator.save(session.getIdInternal(), //id
                    simpleMetadata);  //SimpleMetadata 
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }        
    
    /**
    * Clear sessions
    *
    * @exception IOException if an input/output error occurs
    */   
    public synchronized void clear() throws IOException {
        //FIXME

    }
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void doRemove(String id) throws IOException  {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>doRemove");                       
        }        
        //System.out.println("ReplicationStore>>doRemove");
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>doRemove: replicator: " + replicator);                       
        }        
        //System.out.println("ReplicationStore>>doRemove: replicator: " + replicator);
        try {
            replicator.remove(id);
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }     
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public synchronized void removeSynchronized(String id) throws IOException  {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>removeSynchronized");                       
        }         
        //System.out.println("ReplicationStore>>removeSynchronized");
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>removeSynchronized: replicator: " + replicator);                       
        }        
        //System.out.println("ReplicationStore>>removeSynchronized: replicator: " + replicator);
        try {
            replicator.remove(id);
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }
    
    /**
    * Remove the list of ids in removedIdsData from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param msgID message id for this remove all message
    * @param removedIdsData serialized list of ids to remove
    *
    * @exception IOException if an input/output error occurs
    */
    public void removeIds(long msgID, byte[] removedIdsData) throws IOException  {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>removeIds");                       
        }         
        //System.out.println("ReplicationStore>>removeIds");
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        JxtaBackingStoreImpl replicator = (JxtaBackingStoreImpl)mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>removeIds: replicator: " + replicator);                       
        }        
        //System.out.println("ReplicationStore>>removeIds: replicator: " + replicator);
        try {
            replicator.removeIds(msgID, removedIdsData);
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }     
    
    /**
     * Called by our background reaper thread to remove expired
     * sessions in the replica - this can be done on the same
     * instance - i.e. each instance will do its own
     *
     */
    public void processExpires() {        
        
        ReplicationManagerBase replicationMgr =
            (ReplicationManagerBase) this.getManager();
        replicationMgr.processExpiredReplicas();
    }    
    
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doTransmit(ReplicationState transmitState) {
        return doTransmit(transmitState, true);
        //FIXME above is just for testing put back original later
        /*
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationState(transmitState);
        return resultState;
         */
    }
    
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doTransmit(ReplicationState transmitState, boolean wait) {
        //FIXME
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationState(transmitState, wait);
        return resultState;
    }    
    
    protected ReplicationState doReturnTransmit(ReplicationState transmitState) {
        JxtaReplicationReceiver replicationReceiver = 
                JxtaReplicationReceiver.createInstance();
        ReplicationState resultState = 
                replicationReceiver.sendReplicationStateResponse(transmitState);        
        return resultState;
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
    
    /**
    * Load and return the Session associated with the specified session
    * identifier from this Store, without removing it.  If there is no
    * such stored Session, return <code>null</code>.
    *
    * @param id Session identifier of the session to load
    *
    * @exception ClassNotFoundException if a deserialization error occurs
    * @exception IOException if an input/output error occurs
    */
    public synchronized Session load(String id) throws ClassNotFoundException, IOException {
        return load(id, null);
    }    
    
    /**
    * Load and return the Session associated with the specified session
    * identifier from this Store, without removing it.  If there is no
    * such stored Session, return <code>null</code>.
    *
    * @param id Session identifier of the session to load
    *
    * @exception ClassNotFoundException if a deserialization error occurs
    * @exception IOException if an input/output error occurs
    */
    public synchronized Session load(String id, String version) throws ClassNotFoundException, IOException {
        //System.out.println("ReplicationStore>>load-dumping:");
        //Thread.dumpStack();
        Session result = null;
        if(id == null) {
            return result;
        }
        ReplicationManagerBase repMgr =
            (ReplicationManagerBase)this.getManager();
        //System.out.println("in load() - mgr identityhash: " + System.identityHashCode(repMgr));
        if(_logger.isLoggable(Level.FINE)) {
            repMgr.printReplicatedSessionIds();                      
        }        
        //repMgr.printReplicatedSessionIds();
        //ReplicationState localCachedState = repMgr.getFromReplicationCache(id);
        ReplicationState localCachedState = repMgr.transferFromReplicationCache(id);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>load:localCachedState=" + localCachedState);                       
        }
        //check if we got a hit from our own replica cache
        //and if so return it immediately
        if(version != null && localCachedState != null) {
            long versionLong = parseLong(version);
            if(localCachedState.getVersion() == versionLong) {
                return (this.getSession(localCachedState));
            }            
        }
        //System.out.println("ReplicationStore>>load:localCachedState=" + localCachedState);        
        ReplicationState broadcastResultState = findSessionViaBroadcast(id, version);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>load:broadcastResultState from broadcast=" + broadcastResultState);                       
        }        
        //System.out.println("ReplicationStore>>load:broadcastResultState from broadcast=" + broadcastResultState);
        ReplicationState bestState 
            = getBestResult(localCachedState, broadcastResultState);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>load:bestState=" + bestState);                       
        }
        //System.out.println("ReplicationStore>>load:bestState=" + bestState);
        if(bestState != null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationStore>>load:bestStateVERSION=" + bestState.getVersion());
                _logger.fine("ReplicationStore>>load:bestStateSTATE=" + bestState.getState());
            }            
            //System.out.println("ReplicationStore>>load:bestStateVERSION=" + bestState.getVersion());
            //System.out.println("ReplicationStore>>load:bestStateSTATE=" + bestState.getState());
        }

        if(bestState != null && bestState.getState() != null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationStore>>load:before deserializing bestState:ver=" + bestState.getVersion());                       
            }            
            //System.out.println("ReplicationStore>>load:before deserializing bestState:ver=" + bestState.getVersion());
            result = this.getSession(bestState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationStore>>load:after deserializing session:ver=" + ((HASession)result).getVersion());                       
            }            
            //System.out.println("ReplicationStore>>load:after deserializing session:ver=" + ((HASession)result).getVersion());
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>load:id " + id + " session: " + result);                       
        }         
        //System.out.println("ReplicationStore>>load:id " + id + " session: " + result);
        return result;
    }    
    
    protected ReplicationState getBestResult(ReplicationState localState, ReplicationState broadcastResultState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>getBestResult:localState=" + localState + "other=" + broadcastResultState);                       
        }        
        //System.out.println("ReplicationStore>>getBestResult:localState=" + localState + "other=" + broadcastResultState);        
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
    
    private ReplicationState findSessionViaBroadcast(String id, String version) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>findSessionViaBroadcast");                       
        }        
        //System.out.println("ReplicationStore>>findSessionViaBroadcast");
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        JxtaBackingStoreImpl jxtaReplicator = null;
        if(replicator instanceof JxtaBackingStoreImpl) {
            jxtaReplicator = (JxtaBackingStoreImpl)replicator;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>findSessionViaBroadcast: replicator: " + replicator);                       
        }        
        //System.out.println("ReplicationStore>>findSessionViaBroadcast: replicator: " + replicator);        
        
        SimpleMetadata queryResult = null;
        try {
            //use version aware load if possible
            if(jxtaReplicator != null && version != null) {
                queryResult = (SimpleMetadata)jxtaReplicator.load(id, version);
            } else {
                queryResult = (SimpleMetadata)replicator.load(id);
            }
        } catch (BackingStoreException ex) {
            //FIXME log this
        }
        if(queryResult == null) {
            return null;
        }
        /*
        ReplicationState result 
            = new ReplicationState(MODE_WEB, id, this.getApplicationId(), 
                queryResult.getVersion(), 0L, 0L, queryResult.getUsername(), queryResult.getSsoId(), 
                null, queryResult.getState());
         */
        //queryResult.getExtraParam() here is ssoid
        ReplicationState result 
            = new ReplicationState(MODE_WEB, id, this.getApplicationId(), 
                queryResult.getVersion(), 0L, 0L, null, queryResult.getExtraParam(), 
                null, null, queryResult.getState(), null);
        return result;
    }
    
    protected long parseLong(String s) {
        long result = -1L;
        try {
            result = Long.parseLong(s);
        } catch (NumberFormatException ex) {
            ;
        }
        return result;
    }    
    
    /**
     * update the lastaccess time of the specified Session into this Store.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */    
    public void updateLastAccessTime(Session session) throws IOException {
        
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>updateLastAccessTime: replicator: " + replicator);                       
        }         
        //System.out.println("ReplicationStore>>updateLastAccessTime: replicator: " + replicator);
        try {
            replicator.updateLastAccessTime(session.getIdInternal(), //id
                    ((BaseHASession)session).getLastAccessedTimeInternal(), //lastaccesstime
                    ((HASession)session).getVersion()); //version
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }        
    
    /**
    * Return an array containing the session identifiers of all Sessions
    * currently saved in this Store.  If there are no such Sessions, a
    * zero-length array is returned.
    *
    * @exception IOException if an input/output error occurred
    */  
    public String[] keys() throws IOException  {
        //FIXME
        return new String[0];
    }
    
    public int getSize() throws IOException {
        int result = 0;
        ReplicationManagerBase mgr
            = (ReplicationManagerBase)this.getManager();
        BackingStore replicator = mgr.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>getSize: replicator: " + replicator);                       
        }         
        //System.out.println("ReplicationStore>>getSize: replicator: " + replicator);
        try {
            result = replicator.size();
        } catch (BackingStoreException ex) {
            //nothing to do - ok to eat exception
        }
        return result;
    }    
    
    // Store methods end
    
    /**
    * Given a byte[] containing session data, return a session
    * object
    *
    * @param state
    *   The byte[] with the session data
    *
    * @return
    *   A newly created session for the given session data, and associated
    *   with this Manager
    */
    protected Session getSession(ReplicationState replicationState) 
        throws IOException, ClassNotFoundException 
    {
        byte[] state = replicationState.getState();
        Session _session = null;
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        Loader loader = null;    
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        Container container = manager.getContainer();
        java.security.Principal pal=null; //MERGE chg added
        String ssoId = null;
        long version = 0L;
        IOUtilsCaller utilsCaller = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            
            //Get the username, ssoId from replicationState
            String id = (String)replicationState.getId();
            //ssoId = replicationState.getSsoId();
            ssoId = replicationState.getExtraParam();
            version = replicationState.getVersion();
            //debug("ReplicationStore.getSession()  id="+id+"  username ="+username+";");    

            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("loaded session from replicationstore, length = "+state.length);
            }
            if (container != null) {
                loader = container.getLoader();
            }

            if (loader != null) {
                classLoader = loader.getClassLoader();
            }          
            //Bug 4832603 : EJB Reference Failover
            //HERCULES FIXME: for now reverting back
            //need to look at new EJBUtils and related serialization code
            /*
          if (classLoader != null) {
            ois = new CustomObjectInputStream(bis, classLoader);
          }
          else {
            ois = new ObjectInputStream(bis);
          }
          
            //ois = EJBUtils.getInputStream(bis, classLoader, true, true);
            //end - Bug 4832603  
             */          
            if (classLoader != null) {
                if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                    try {
                        ois = utilsCaller.createObjectInputStream(bis, true, classLoader);
                    } catch (Exception ex) {}
                }
            }
            if (ois == null) {
                ois = new ObjectInputStream(bis); 
            }
            if(ois != null) {               
                try {
                    _session = readSession(manager, ois);
                } 
                finally {
                    if (ois != null) {
                        try {
                            ois.close();
                            bis = null;
                        }
                        catch (IOException e) {
                        }
                    }
                }
            }
        }
        catch(ClassNotFoundException e)
        {
            System.err.println("getSession :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getSession IOException :"+e.getMessage());
            throw e;
        }
        String username = ((HASession)_session).getUserName();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>getSession: username=" + username);
            _logger.fine("ReplicationStore>>getSession: principal=" + _session.getPrincipal());                                  
        }        
        //System.out.println("ReplicationStore>>getSession: username=" + username);
        //System.out.println("ReplicationStore>>getSession: principal=" + _session.getPrincipal());
        if((username !=null) && (!username.equals("")) && _session.getPrincipal() == null) {
            if (_debug > 0) {
                debug("Username retrieved is "+username);
            }
            pal = ((com.sun.web.security.RealmAdapter)container.getRealm()).createFailOveredPrincipal(username);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationStore>>getSession:created pal=" + pal);                                  
            }             
            //System.out.println("ReplicationStore>>getSession:created pal=" + pal);
            if (_debug > 0) {
                debug("principal created using username  "+pal);
            }
            if(pal != null) {
                _session.setPrincipal(pal);
                if (_debug > 0) {
                    debug("getSession principal="+pal+" was added to session="+_session); 
                }                
            }
        }
        //--SRI        
        
        _session.setNew(false);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationStore>>getSession:ssoId=" + ssoId);                                  
        }         
        //System.out.println("ReplicationStore>>getSession:ssoId=" + ssoId);
        ((BaseHASession)_session).setSsoId(ssoId);
        if((ssoId !=null) && (!ssoId.equals("")))
            associate(ssoId, _session);
        ((HASession)_session).setVersion(version);
        ((HASession)_session).setDirty(false);
        ((HASession)_session).setPersistent(false);        
        return _session;
    }
    
    protected void associate(String ssoId, Session _session) {
        if (_debug > 0) {
            debug("Inside associate() -- HAStore");
        }
        Container parent = manager.getContainer();
        SingleSignOn sso = null;
        while ((sso == null) && (parent != null)) {
            if (_debug > 0) {
                 debug("Inside associate()  while loop -- HAStore");
            }
        if (!(parent instanceof Pipeline)) {
            if (_debug > 0) {
                 debug("Inside associate()  parent instanceof Pipeline -- HAStore");
            }
            parent = parent.getParent();
            continue;
        }
        Valve valves[] = ((Pipeline) parent).getValves();
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof SingleSignOn) {
                 if (_debug > 0) {
                    debug("Inside associate()  valves[i] instanceof SingleSignOn -- HAStore");
                 }
                 sso = (SingleSignOn) valves[i];
                 break;
             }
        }
        if (sso == null)
            parent = parent.getParent();
        }
        if (sso != null) {
            if (_debug > 0) {
                debug("Inside associate() sso != null");
            }
            //SingleSignOnEntry ssoEntry = ((ReplicationSingleSignOn)sso).lookup(ssoId);
            SingleSignOnEntry ssoEntry = ((ReplicationSingleSignOn)sso).lookupEntry(ssoId);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("ReplicationStore>>associate: ssoEntry = "+ssoEntry);
                _logger.finest("ReplicationStore>>associate: ssoEntry.principal = " + ssoEntry.principal + "_session.getPrincipal() =" + _session.getPrincipal());
            }
            //System.out.println("ReplicationStore>>associate: ssoEntry = "+ssoEntry);
            //System.out.println("ReplicationStore>>associate: ssoEntry.principal = " + ssoEntry.principal + "_session.getPrincipal() =" + _session.getPrincipal());
            if(ssoEntry!=null) {
                ssoEntry.addSession(sso, _session);
                if(ssoEntry.principal == null && _session.getPrincipal() != null) {
                    ssoEntry.principal = _session.getPrincipal();
                }
            }
        }

    }     
    
    protected String getUsername(StandardSession session) {
        String result = null;
        if(session.getPrincipal() !=null){
            result = session.getPrincipal().getName();
        } else {
            result = "";
        }
        return result;
    }
    
    protected String getSsoId(StandardSession session) {    
        return ((HASession)session).getSsoId();
    }   
    
}
