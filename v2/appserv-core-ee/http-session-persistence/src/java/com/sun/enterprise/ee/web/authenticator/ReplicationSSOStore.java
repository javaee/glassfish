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
 * ReplicationSSOStore.java
 *
 * Created on December 5, 2005, 12:56 PM
 *
 */

package com.sun.enterprise.ee.web.authenticator;

import java.io.*;

import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.ee.web.sessmgmt.*;
import com.sun.enterprise.security.web.SingleSignOnEntry;
import org.apache.catalina.Container;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.session.StoreBase;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

//import com.sun.appserv.ha.impl.*;
import com.sun.appserv.ha.spi.*;

/**
 *
 * @author Larry White
 */
public class ReplicationSSOStore implements StorePoolElement, ReplicationSSOStorePoolElement {

    final static String MODE_SSO = ReplicationState.MODE_SSO;
    final static String MODE_WEB = ReplicationState.MODE_WEB;
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    protected static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);     
    
    /** Creates a new instance of ReplicationSSOStore */
    public ReplicationSSOStore() {         
    }  
    
    //begin StorePoolElement methods
    
    public void cleanup() {
        //FIXME
        //closeStatements();
        //closeConnection();
    } 
    
    //end StorePoolElement methods
    
    //begin ReplicationSSOStorePoolElement methods

    /** Loads the sso entry
     * @param ssoId
     * @param repSingleSignOn
     * @throws IOException
     * @return
     */    
    public SingleSignOnEntry loadSSO(String ssoId, ReplicationSingleSignOn repSingleSignOn) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>loadSSO:ssoId: " + ssoId + " stacktrace follows:");
            //Thread.dumpStack();
        }
        //System.out.println("ReplicationSSOStore>>loadSSO:ssoId: " + ssoId + " stacktrace follows:");
        //Thread.dumpStack();        
        if(ssoId == null) {
            return null;
        }        
        //ReplicationState localCachedState = repSingleSignOn.getFromReplicationCache(ssoId);
        ReplicationState localCachedState = repSingleSignOn.transferFromReplicationCache(ssoId);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>loadSSO:localCachedState=" + localCachedState);
        }        
        //System.out.println("ReplicationSSOStore>>loadSSO:localCachedState=" + localCachedState);    
        ReplicationState broadcastResultState = findSSOEntryViaBroadcast(ssoId);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>loadSSO:broadcastResultState from broadcast=" + broadcastResultState);
        }        
        //System.out.println("ReplicationSSOStore>>loadSSO:broadcastResultState from broadcast=" + broadcastResultState);
        ReplicationState bestState 
            = getBestResult(localCachedState, broadcastResultState);
        HASingleSignOnEntry result = null;
        if(bestState != null && bestState.getState() != null) {
            try {
                result = this.getSSOEntry(bestState);
            } catch (Exception ex) {}
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>loadSSO:ssoId " + ssoId + " ssoentry: " + result);
        }        
        //System.out.println("ReplicationSSOStore>>loadSSO:ssoId " + ssoId + " ssoentry: " + result);
        //save immediately to re-replicate
        if(result != null) {
            this.save(ssoId, result);
        }
        return result;        
    }
    
    private ReplicationState getBestResult(ReplicationState localState, ReplicationState broadcastResultState) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("getBestResult:localState=" + localState + "other=" + broadcastResultState);
        }         
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
    
    private ReplicationState findSSOEntryViaBroadcastOldAPI(String ssoId) {
        //FIXME do multi-cast to cluster member and get back state
        //ReplicationState state = 
        //    ReplicationState.createBroadcastQueryState(MODE_SSO, ssoId, this.getApplicationId());
        ReplicationState state = 
            ReplicationState.createBroadcastQueryState(MODE_SSO, ssoId, null, this.getInstanceName());        
        //ReplicationState state = new ReplicationState(id, this.getApplicationId(), 0L, 0L, null, null, "loadQuery", null);
        //Object id, String appId, long lastAccess, long maxInactiveInterval, String username, String ssoid, String command, byte[] state
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
    
    private ReplicationState findSSOEntryViaBroadcast(String ssoId) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>findSSOEntryViaBroadcast");
        }         
        //System.out.println("ReplicationSSOStore>>findSSOEntryViaBroadcast");
        if(container == null) {
            System.out.println("error: ReplicationSSOStore>>findSSOEntryViaBroadcast: container null");
        }
        if(parent == null) {
            return null;
        }
        BackingStore replicator = parent.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("in findSSOEntryViaBroadcast: replicator: " + replicator);
        }        
        //System.out.println("in findSSOEntryViaBroadcast: replicator: " + replicator);        
        
        SimpleMetadata queryResult = null;
        try {
            queryResult = (SimpleMetadata)replicator.load(ssoId);
        } catch (BackingStoreException ex) {
            //FIXME log this
        }
        if(queryResult == null) {
            return null;
        }
        //queryResult.getExtraParam() here is userName
        ReplicationState result 
            = new ReplicationState(MODE_SSO, ssoId, this.getApplicationId(), 
                queryResult.getVersion(), 0L, 0L, queryResult.getExtraParam(), null,  
                null, null, queryResult.getState(), null);
        return result;
    }       
    
    public void saveOldAPI(String ssoId, SingleSignOnEntry ssoEntry) throws IOException {
        System.out.println("ReplicationSSOStore>>save");
        this.transmitSSOEntry(ssoId, ssoEntry, "save");         
    }
    
    public void save(String ssoId, SingleSignOnEntry ssoEntry) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>save:ssoId = " + ssoId + " :ssoEntry = " + ssoEntry);
        }         
        //System.out.println("ReplicationSSOStore>>save:ssoId = " + ssoId + " :ssoEntry = " + ssoEntry);
        if(container == null) {
            System.out.println("error: ReplicationSSOStore>>save: container null");
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>save:parent=" + parent);
        }        
        //System.out.println("ReplicationSSOStore>>save:parent=" + parent);
        if(parent == null) {
            return;
        }
        BackingStore replicator = parent.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore: in save: replicator: " + replicator);
        }        
        //System.out.println("ReplicationSSOStore: in save: replicator: " + replicator);
        SimpleMetadata simpleMetadata =
            createSimpleMetadata(ssoId, (HASingleSignOnEntry)ssoEntry);
        try {
            replicator.save(ssoId, //id 
                    simpleMetadata);  //SimpleMetadata
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }    
    
    /** Saves the state
     * @param state
     * @throws IOException  */
    public void saveForRepair(ReplicationState state) throws IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("ReplicationSSOStore", "saveForRepair", state);
        }        
        long tempStartTime = System.currentTimeMillis();
        this.transmitState(state, "save", true);
        //this.transmitState(state, "save", false);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>saveForRepair time = " + (System.currentTimeMillis() - tempStartTime));
        }         
        //System.out.println("ReplicationSSOStore>>saveForRepair time = " + (System.currentTimeMillis() - tempStartTime));         
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("ReplicationSSOStore", "saveForRepair");
        }
    }    
    
    public void associateOldAPI(StandardSession session, String ssoId) throws IOException {
        //FIXME - change MODE to web and route directly to container for session
        String sessionId = session.getIdInternal();
        ReplicationManagerBase mgr = (ReplicationManagerBase)session.getManager();
        String appId = mgr.getApplicationId();
        ReplicationState transmitState =
            new ReplicationState(MODE_WEB,  //web mode - sending this to web container
                sessionId,  //id
                appId,      //appid
                ((HASession)session).getVersion(), //version
                0L,         //lastaccesstime
                0L,         //maxInactiveInterval
                ssoId,      //extraParam - here it is ssoId for associate
                null,       //queryResult - not used here
                null,       //FIXME instanceName              
                "associate", //command
                null,       //state - not needed 
                null);      //trunkState - not needed
        System.out.println("ReplicationSSOStore>>associate");
        this.doTransmit(transmitState); 
    }
    
    public void associate(StandardSession session, String ssoId) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>associate");
        }        
        //System.out.println("ReplicationSSOStore>>associate");
        if(session instanceof HASession) {
            ((HASession)session).setSsoId(ssoId);
            //((HASession)session).incrementVersion();
        }
        ReplicationManagerBase mgr = (ReplicationManagerBase)session.getManager();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>associate: mgr = " + mgr);
        }         
        //System.out.println("ReplicationSSOStore>>associate: mgr = " + mgr);
        //FIXME: for time-based different manager class
        if(mgr instanceof ReplicationWebEventPersistentManager) {
            ((ReplicationWebEventPersistentManager)mgr).doValveSave(session);
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("exiting ReplicationSSOStore>>associate");
        }        
        //System.out.println("exiting ReplicationSSOStore>>associate");
    }     
    
    public void removeOldAPI(String ssoId) throws IOException {      
        ReplicationState transmitState =
            new ReplicationState(MODE_SSO,  //sso mode
                ssoId, //id
                getApplicationId(), //appid - using virtual server name here
                0L,     //version
                0L,     //lastaccesstime
                0L,     //maxInactiveInterval
                ssoId,  //extraParam (ssoId here but not needed)
                null,   //queryResult (not used here)
                null,  //FIXME instanceName                
                "remove", //command
                null,  //state - not needed
                null);  //trunkState - not needed
        System.out.println("ReplicationSSOStore>>remove");
        this.doTransmit(transmitState);        
    }
    
    public void remove(String ssoId) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>remove");
        }        
        //System.out.println("ReplicationSSOStore>>remove");
        if(container == null) {
            System.out.println("error: ReplicationSSOStore>>remove: container null");
        }
        if(parent == null) {
            return;
        }
        BackingStore replicator = parent.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>remove: replicator: " + replicator);
        }        
        //System.out.println("ReplicationSSOStore>>remove: replicator: " + replicator); 
        try {
            replicator.remove(ssoId);    //ssoid 
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }    
    
    /**
    * The application id
    */  
    protected String applicationId = null;    
    
    protected String getApplicationId() {
        //injected by ReplicationSingleSignOn
        return applicationId;
    }
    
    public void setApplicationId(String value) {
        applicationId = value;
    }    
    
    public void removeInActiveSessions(String ssoId) throws IOException {
        //FIXME need new version
        ReplicationState transmitState =
            new ReplicationState(MODE_SSO,  //sso mode
                ssoId, //id
                getApplicationId(), //appid  using virtual server name
                0L,     //version
                0L,     //lastaccesstime
                0L,     //maxInactiveInterval
                ssoId,  //extraParam (ssoId here but not needed)
                null,   //queryResult (not used here)
                null,  //FIXME instanceName               
                "removeInActiveSessions", //command
                null,  //state - not needed
                null);  //trunkState - not needed
        System.out.println("ReplicationSSOStore>>removeInActiveSessions");
        this.doTransmit(transmitState);  
    }
    
    /** set the container */
    public void setContainer(Container container) {
	this.container = container;
	debug("   container:    "+container);
    }
    
    /** set the parent of this store */
    public void setParent(ReplicationSingleSignOn parent) {
	this.parent = parent;
	debug("   parent:    "+parent);
    }    
    
    public void updateLastAccessTimeOldAPI(String ssoId, long lat) throws IOException {
        ReplicationState transmitState =
            new ReplicationState(MODE_SSO,  //sso mode
                ssoId, //id
                getApplicationId(), //appid  using virtual server name
                0L,      //version
                lat,     //lastaccesstime
                0L,     //maxInactiveInterval
                ssoId,  //extraParam (ssoid here)
                null,   //queryResult (not used here)
                null,  //FIXME instanceName                
                "updateLastAccessTime", //command
                null,  //state - not needed 
                null);  //trunkState - not needed
        System.out.println("ReplicationSSOStore>>updateLastAccessTime");
        this.doTransmit(transmitState); 
    }
    
    public void updateLastAccessTime(String ssoId, long lat) throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>updateLastAccessTime");
        }         
        //System.out.println("ReplicationSSOStore>>updateLastAccessTime");
        if(container == null) {
            System.out.println("error: ReplicationSSOStore>>updateLastAccessTime: container null");
        }
        if(parent == null) {
            return;
        }
        BackingStore replicator = parent.getBackingStore();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>updateLastAccessTime: replicator: " + replicator);
        }         
        //System.out.println("ReplicationSSOStore>>updateLastAccessTime: replicator: " + replicator); 
        try {
            replicator.updateLastAccessTime(ssoId,   //ssoId
                                            lat,    //last access time
                                            0L);    //version 
        } catch (BackingStoreException ex) {
            //FIXME
        }
    }       
    
    //end ReplicationSSOStorePoolElement methods
    
    protected void transmitSSOEntry(String ssoId, SingleSignOnEntry ssoEntry, String command) throws IOException {
        ReplicationState transmitState =
            createReplicationState(ssoId, (HASingleSignOnEntry)ssoEntry, command);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>transmitSSOEntry");
        }
        //System.out.println("ReplicationSSOStore>>transmitSSOEntry");
        this.doTransmit(transmitState);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitSSOEntry(String ssoId, SingleSignOnEntry ssoEntry, String command, boolean wait) throws IOException {
        ReplicationState transmitState =
            createReplicationState(ssoId, (HASingleSignOnEntry)ssoEntry, command);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>transmitSSOEntry");
        }
        //System.out.println("ReplicationSSOStore>>transmitSSOEntry");
        this.doTransmit(transmitState, wait);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitState(ReplicationState state, String command) throws IOException {
        ReplicationState transmitState =
            createRepairReplicationState(state, command);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSSOStore>>transmitState");
        }
        //System.out.println("ReplicatedSSOStore>>transmitState");
        this.doTransmit(transmitState);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected void transmitState(ReplicationState state, String command, boolean wait) throws IOException {
        ReplicationState transmitState =
            createRepairReplicationState(state, command);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicatedSSOStore>>transmitState");
        }
        //System.out.println("ReplicatedSSOStore>>transmitState");
        this.doTransmit(transmitState, wait);
        
        //remove after testing
        //this.testDeserializeState(transmitState);
    }
    
    protected ReplicationState createRepairReplicationState(ReplicationState state, String command) 
        throws IOException {

        ReplicationState transmitState =
            new ReplicationState(MODE_SSO,  //sso mode
                state.getId().toString(), //id (will be ssoId here)  
                state.getAppId(),     //containerId analog of appid here
                state.getVersion(),   //version
                state.getLastAccess(),  //lastaccesstime
                state.getMaxInactiveInterval(), //maxInactiveInterval (seconds)
                state.getExtraParam(),  //extraParam (is username here)
                state.getQueryResult(), //queryResult
                state.getInstanceName(),  //instanceName
                command,         //command
                state.getState(),      //state
                state.getTrunkState()); //trunkState
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>createRepairReplicationState:transmitState=" + transmitState);
        }        
        //System.out.println("ReplicationSSOStore>>createRepairReplicationState=" + transmitState);
        return transmitState;

    }        
    
    protected ReplicationState createReplicationState(String ssoId, HASingleSignOnEntry ssoEntry, String command) 
        throws IOException {

        byte[] ssoEntryState = this.getByteArray(ssoEntry);
        ReplicationState transmitState =
            new ReplicationState(MODE_SSO, //sso mode
                ssoId,                  //id (ssoId for this) 
                null,                   //appid
                0L,
                ssoEntry.lastAccessTime, //lastaccesstime
                0L,                   //maxInactiveInterval (seconds)
                ssoEntry.username,       //extraParam (username here)
                null,                   //queryResult (not used here)
                null,                  //FIXME instanceName
                command,            //command
                ssoEntryState,      //state
                null);              //trunkState - not needed
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>createReplicationState:transmitState=" + transmitState);
        }        
        //System.out.println("ReplicationSSOStore>>createReplicationState:transmitState=" + transmitState);
        return transmitState;

    }
    
    protected SimpleMetadata createSimpleMetadata(String ssoId, HASingleSignOnEntry ssoEntry) 
        throws IOException {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSsOStore>>createSimpleMetadata:ssoEntry=" + ssoEntry);
        }        
        //System.out.println("ReplicationSsOStore>>createSimpleMetadata:ssoEntry=" + ssoEntry);
        byte[] ssoEntryState = this.getByteArray(ssoEntry);
        String userName = null;
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>createSimpleMetadata:ssoEntry.principal=" + ssoEntry.principal);
        }        
        System.out.println("ReplicationSSOStore>>createSimpleMetadata:ssoEntry.principal=" + ssoEntry.principal);        
        if(ssoEntry.principal != null) {
            userName = ssoEntry.principal.getName();
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSsOStore>>createSimpleMetadata:userName=" + userName);
        }         
        System.out.println("ReplicationSsOStore>>createSimpleMetadata:userName=" + userName);
        SimpleMetadata metaData =
            new SimpleMetadata(ssoEntry.getVersion(), //version
                ssoEntry.lastAccessTime, //lastaccesstime
                0L,                      //maxInactiveInterval (seconds)
                ssoEntryState,           //state
                userName);              //userName
        //System.out.println("ReplicationSSOStore>>createSimpleMetadata");
        return metaData;
    }   
    
    protected SimpleMetadata createSparseSimpleMetadata(String ssoId) 
        throws IOException {

        SimpleMetadata metaData =
            new SimpleMetadata(0L, //version
                0L,                 //lastaccesstime
                0L,                 //maxInactiveInterval (seconds)
                null,               //state
                ssoId);             //ssoid
        //System.out.println("ReplicationSSOStore>>createSparseSimpleMetadata");
        return metaData;
    }      
    
    private void testDeserializeState(ReplicationState transmitState) {
        try {
            HASingleSignOnEntry ssoEntry = this.getSSOEntry(transmitState);
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ssoEntry = " + ssoEntry);
            }             
            //System.out.println("ssoEntry = " + ssoEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
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
    protected HASingleSignOnEntry getSSOEntry(ReplicationState replicationState) 
        throws IOException, ClassNotFoundException 
    {
        byte[] state = replicationState.getState();
        String userName = replicationState.getExtraParam();
        HASingleSignOnEntry _ssoEntry = null;
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        String ssoId = null;	            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            
            //Get the username, ssoId from replicationState
            String id = (String)replicationState.getId();
            //String username = replicationState.getUsername();
            userName = replicationState.getExtraParam(); //extraParam is username here
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationSSOStore>>getSSOEntry:userName=" + userName);
            }            
            //System.out.println("ReplicationSSOStore>>getSSOEntry:userName=" + userName);
            //ssoId = replicationState.getSsoId();           	
            //debug("ReplicationStore.getSSOEntry()  id="+id+"  username ="+username+";");  
 
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("loaded ssoEntry from replicationssostore, length = "+state.length);
            }
                    
            ois = new ObjectInputStream(bis); 
            
            if(ois != null) {
                try {
                    _ssoEntry = readSSOEntry(ois);
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
            System.err.println("getSSOEntry :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getSSOEntry IOException :"+e.getMessage());
            throw e;
        }
        if(_ssoEntry.username == null) {
            _ssoEntry.username = userName;
        }
        return _ssoEntry;
    }
    
    /**
    * Create a sso entry object from an input stream.
    *
    * @param ois
    *   The input stream containing the serialized session
    *
    * @return 
    *   The resulting sso entry object
    */
    public HASingleSignOnEntry readSSOEntry(ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
      HASingleSignOnEntry ssoEntry 
          = new HASingleSignOnEntry(null, null, null, null, null);
      ssoEntry.readObjectData(ois);      
      return ssoEntry;
    }
    
    //state is already formatted as a response
    public void sendResponse(ReplicationState state) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>sendResponse");
        }        
        //System.out.println("ReplicationSSOStore>>sendResponse");
        this.doReturnTransmit(state);
    }
    
    public void sendQueryResponse(ReplicationState state, String returnInstance) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationSSOStore>>sendQueryResponse:returnInstance: " + returnInstance);
        }        
        //System.out.println("ReplicationSSOStore>>sendQueryResponse:returnInstance: " + returnInstance);
        this.doReturnQueryTransmit(state, returnInstance);
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
    
    //FIXME: this could return null which would mean a problem
    //might want to throw exception in that case
    protected ReplicationState doTransmit(ReplicationState transmitState) {
        //FIXME
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationState(transmitState);
        return resultState;
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
    
    /**
    * Create an byte[] for the ssoEntry
    *
    * @param ssoEntry
    *   The ssoEntry we are serializing
    *
    */
    protected byte[] getByteArray(HASingleSignOnEntry ssoEntry)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));             

            writeSSOEntry(ssoEntry, oos);
            oos.close();
            oos = null;
            obs = bos.toByteArray();
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }
        }

        return obs;
    } 
    
    /**
    * Serialize an ssoEntry into an output stream.  
    *
    * @param ssoEntry
    *   The ssoEntry to be serialized
    *
    * @param oos
    *   The output stream the session should be written to
    */
    public void writeSSOEntry(HASingleSignOnEntry ssoEntry, ObjectOutputStream oos)
        throws IOException {
      if ( ssoEntry == null )  {
        return;
      }      
      ssoEntry.writeObjectData(oos);
      //oos.writeObject(ssoEntry);
    }    
    
    public void debug(String s){
        if(debug)
        System.out.println("ReplicationSSOStore: "+s);
    } 
    
    private boolean debug = false; //set this to true for debug info
    protected Container container = null;
    protected ReplicationSingleSignOn parent = null;
    
}
