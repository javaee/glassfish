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
 * JxtaBackingStoreImpl.java
 *
 * Created on October 6, 2006, 1:19 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import org.apache.catalina.session.*;

import com.sun.appserv.ha.spi.BackingStore;
import com.sun.appserv.ha.spi.BackingStoreException;

import com.sun.appserv.ha.spi.CompositeMetadata;
import com.sun.appserv.ha.spi.Metadata;
import com.sun.appserv.ha.spi.SimpleMetadata;

import com.sun.enterprise.web.ServerConfigLookup;

/**
 * This class now used in simple web, sso, ejb cases
 * //sso and ejb not done yet
 * @author Larry White
 */
public class JxtaBackingStoreImpl extends BackingStore {
    
    final static String MODE_WEB = ReplicationState.MODE_WEB;
    final static String MODE_SSO = ReplicationState.MODE_SSO;
    //final static String MODE_EJB = ReplicationState.MODE_EJB;
    
    final static String COMPOSITE_SAVE_COMMAND 
        = ReplicationState.COMPOSITE_SAVE_COMMAND;
    final static String VALVE_SAVE_COMMAND 
        = ReplicationState.VALVE_SAVE_COMMAND;    
    final static String REMOVE_COMMAND 
        = ReplicationState.REMOVE_COMMAND;
    final static String REMOVE_EXPIRED_COMMAND
        = ReplicationState.REMOVE_EXPIRED_COMMAND;
    final static String UPDATE_LAST_ACCESS_TIME_COMMAND
        = ReplicationState.UPDATE_LAST_ACCESS_TIME_COMMAND;
    final static String SIZE_COMMAND
        = ReplicationState.SIZE_COMMAND;
    final static String REMOVE_IDS_COMMAND
        = ReplicationState.REMOVE_IDS_COMMAND;
    
    final static String DUPLICATE_IDS_SEMANTICS_PROPERTY 
        = ReplicationState.DUPLICATE_IDS_SEMANTICS_PROPERTY;     
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    
    /**
     * Creates a new instance of JxtaBackingStoreImpl
     */
    public JxtaBackingStoreImpl() {
    }
    
    /**
     * Creates a new instance of JxtaBackingStoreImpl
     */
    public JxtaBackingStoreImpl(String appid, Properties env) {
        _mode = MODE_WEB;
        _appid = appid;
        //duplicate id semantics allowed - used by batch replication
        if(env != null) {
            _duplicateIdsSemanticsAllowed = ((Boolean)env.get(DUPLICATE_IDS_SEMANTICS_PROPERTY)).booleanValue();
        } else {
            _duplicateIdsSemanticsAllowed = true;
        }
    }    
    
    /**
     * Load and return the value for the given id
     * @param id the id whose value must be returned
     * @return the value if this store contains it or null
     */
    public Metadata load(String id) throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            System.out.println("TEST:load called during reshape");
            //return null;
        }        
        return this.findSessionViaBroadcast(id);
    }
    
    /**
     * Load and return the value for the given id
     * @param id the id whose value must be returned
     * @param version the version requested
     * @return the value if this store contains it or null
     */
    public Metadata load(String id, String version) throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            System.out.println("TEST:load called during reshape");
            //return null;
        }
        long versionLong = parseLong(version);
        return this.findSessionViaBroadcast(id, versionLong);
    }    
    
    /**
     * Save the value whose Key is id
     * @param id the id
     * @param value The Metadata
     */
    public void save(String id, Metadata value) 
        throws BackingStoreException {
        //health check done in saveSimple and saveComposite
        if(value instanceof SimpleMetadata) {
            saveSimple(id, (SimpleMetadata)value);
        } else {
            if(value instanceof CompositeMetadata) {
                saveComposite(id, (CompositeMetadata)value);
            }
        }
    }
    
    /**
     * Save the value whose Key is id
     * @param id the id
     * @param value The Metadata
     */
    public void saveSimple(String id, SimpleMetadata value) 
        throws BackingStoreException {
    //long startTime = System.currentTimeMillis();
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("JxtaBackingStore>>saveSimple():id = " + id + "unable to proceed due to health check");
            }             
            return;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStore>>saveSimple():id = " + id);
        }        
        //System.out.println("JxtaBackingStore>>saveSimple():id = " + id);
        // FIXME
        /*
        String theUserName = null;
        String theSsoId = null;
        if(MODE_WEB.equals(getMode())) {
            theSsoId = value.getExtraParam();
        } else {
            if(MODE_SSO.equals(getMode())) {
                theUserName = value.getExtraParam();
            }
        }
         */       
        ReplicationState transmitState = this.createHttpReplicationState(
            id, 
            value.getVersion(), 
            value.getLastAccessTime(), 
            value.getMaxInactiveInterval(),
            value.getExtraParam(), //if WEB-ssoid -- if SSO-username
            null,    //FIXME this should be instance name
            value.getState(),
            null,           //trunkState - not used here
            VALVE_SAVE_COMMAND);
        //this.doTransmit(transmitState, true);
        //this.doTransmit(transmitState, false);
        //use configurable wait for ack here
        this.doTransmit(transmitState, isWaitForAckConfigured());
    //System.out.println("saveSimple Time:" + (System.currentTimeMillis() - startTime) + " msecs");
    }
    
    /**
     * Save the value whose Key is id
     * @param id the id
     * @param value The Metadata
     */
    public void saveComposite(String id, CompositeMetadata value) 
        throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("JxtaBackingStore>>saveComposite():id = " + id + "unable to proceed due to health check");
            }             
            return;
        }        
        /*
        String theUserName = null;
        String theSsoId = null;
        if(MODE_WEB.equals(getMode())) {
            theSsoId = value.getExtraParam();
        } else {
            if(MODE_SSO.equals(getMode())) {
                theUserName = value.getExtraParam();
            }
        }
         */ 
        byte[] compositeState = null;
        try {
            //compositeState = this.getByteArray(value.getEntries());
            compositeState = this.getByteArrayFromCollection(value.getEntries());
        } catch (IOException ex) {}
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>saveComposite:trunkState=" + value.getState());
            _logger.fine("JxtaBackingStoreImpl>>saveComposite:attrState=" + compositeState);             
        }        
        //System.out.println("JxtaBackingStoreImpl>>saveComposite:trunkState=" + value.getState());
        //System.out.println("JxtaBackingStoreImpl>>saveComposite:attrState=" + compositeState);        
        ReplicationState transmitState = this.createHttpReplicationState(
            id, 
            value.getVersion(), 
            value.getLastAccessTime(), 
            value.getMaxInactiveInterval(),
            value.getExtraParam(), //if WEB-ssoid -- if SSO-username
            null,    //FIXME this should be instance name
            compositeState,     // in simple case it is value.getState(),
            value.getState(),   // trunkState for composite case
            COMPOSITE_SAVE_COMMAND);
        //this.doTransmit(transmitState, true);
        //use configurable wait for ack here
        this.doTransmit(transmitState, isWaitForAckConfigured());        
    }     
    
    /**
     * Update the last access time for this id.
     * @param id the id for the Metadata
     * @param time the last access time
     */
    public void updateLastAccessTime(String id, long time, long version) 
        throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>updateLastAccessTime");             
        }         
        //System.out.println("JxtaBackingStoreImpl>>updateLastAccessTime");
        ReplicationState transmitState =
            new ReplicationState(_mode,  //mode
                id, //id
                getApplicationId(), //appid
                version,     //version
                time,     //lastaccesstime
                0L,     //maxInactiveInterval
                null,  //extraParam
                null,   //queryResult
                null,  //FIXME instance name                 
                UPDATE_LAST_ACCESS_TIME_COMMAND, //command
                null,  //state - not needed
                null);  //trunkState - not needed
        //this.doTransmit(transmitState);
        this.doTransmit(transmitState, isWaitForAckConfigured());
    }
    
    /**
     * Remove the Metadata for the id. After this call, any call to load(id)
     *    must return null
     * @param id the id
     */
    public void remove(String id) throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>remove:id = " + id);             
        }         
        //System.out.println("JxtaBackingStoreImpl>>remove:id = " + id);
        ReplicationState transmitState =
            new ReplicationState(_mode,  //mode
                id, //id
                getApplicationId(), //appid
                0L,     //version
                0L,     //lastaccesstime
                0L,     //maxInactiveInterval
                null,  //extraParam
                null,   //queryResult
                null,  //FIXME instanceName                
                REMOVE_COMMAND, //command
                null,  //state - not needed 
                null); //trunkState - not needed
        //this.doTransmit(transmitState);
        //this.doTransmit(transmitState, false);
        //use configurable wait for ack here
        this.doTransmit(transmitState, isWaitForAckConfigured());
    } 
    
    /**
     * Remove all instances that are idle.
     */
    public int removeExpired() throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return 0;
        }        
        // FIXME for getting return
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStore>>removeExpired");
        }        
        //System.out.println("JxtaBackingStoreImpl>>removeExpired");
        ReplicationState transmitState =
            new ReplicationState(_mode,  //mode
                getApplicationId(), //id there is not a real id so use appid
                getApplicationId(), //appid
                0L,     //version
                0L,     //lastaccesstime
                0L,     //maxInactiveInterval
                null,  //extraParam
                null,   //queryResult
                null,  //FIXME instanceName              
                REMOVE_EXPIRED_COMMAND, //command
                null,  //state - not needed
                null);  //trunkState - not needed
        //this.doTransmit(transmitState);
        //use configurable wait for ack here
        this.doTransmit(transmitState, isWaitForAckConfigured());        
        return 0;
    }
    
    /**
     * Get the current size of the store
     * @retrun the number of entries in the storee
     */
    public int size() throws BackingStoreException {
        int result = -1;
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return result;
        }        
        ReplicationState transmitState 
            = ReplicationState.createQueryState(_mode, getApplicationId(), getApplicationId(), SIZE_COMMAND);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>size:transmittedState=" + transmitState);
        }        
        //System.out.println("JxtaBackingStoreImpl>>size:transmittedState=" + transmitState);
        JxtaReplicationSender sender 
            = JxtaReplicationSender.createInstance();
        ReplicationState resultState = sender.sendReplicationState(transmitState);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>size:resultState=" + resultState);
        }        
        //System.out.println("JxtaBackingStoreImpl>>size:resultState=" + resultState);     
        if(resultState != null) {
            try {
                result = ((Integer)resultState.getQueryResult()).intValue();
            } catch(Exception ex) {}
        }
        return result;
    }
    
    /**
     * Called when the store is no longer needed. Must clean up or close
     *    any opened resource.
     */
    public void destroy() throws BackingStoreException {
        // deliberate no-op
        return;
    }
    
    /**
     * Remove all instances that are idle.
     * @param removedIdsData  serialized list of ids to remove
     */
    public void removeIds(long msgID, byte[] removedIdsData) throws BackingStoreException {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return;
        }        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStore>>removeIds");
        }        
        //System.out.println("JxtaBackingStoreImpl>>removeIds");
        String messageId = Long.toString(msgID);
        ReplicationState transmitState =
            new ReplicationState(_mode,  //mode
                messageId, //id use passed in msgID
                getApplicationId(), //appid
                0L,     //version
                0L,     //lastaccesstime
                0L,     //maxInactiveInterval
                null,  //extraParam
                null,   //queryResult
                null,  //FIXME instanceName              
                REMOVE_IDS_COMMAND, //command
                removedIdsData,  //state - serialized list of ids to remove
                null);  //trunkState - not needed
        //this.doTransmit(transmitState, false);
        //use configurable wait for ack here
        this.doTransmit(transmitState, isWaitForAckConfigured());
        return;
    }    
    
    //Helper methods
    protected ReplicationState createHttpReplicationState(String id,
        long version, long lastAccessTime, long maxInactiveInterval, 
        String extraParam, String instanceName,
        byte[] sessionState, byte[] sessionTrunkState, String command) {
        ReplicationState transmitState =
            new ReplicationState(_mode, //web mode in this case
                id,                 //id
                _appid,       //appid
                version,            //version
                lastAccessTime,     //lastaccesstime
                maxInactiveInterval, //maxInactiveInterval (seconds)
                extraParam,           //extraParam
                null,                   //queryResult
                instanceName,         //instanceName
                command,            //command
                sessionState,      //state
                sessionTrunkState); //sessionTrunkState (for composite case)
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>createHttpReplicationState:transmitState = " + transmitState);
        }        
        //System.out.println("JxtaBackingStoreImpl>>createHttpReplicationState:transmitState = " + transmitState");
        return transmitState;
    }
    
    /**
    * Create an byte[] for the object that we can then pass to
    * the ReplicationState.
    *
    * @param obj
    *   The attribute value we are serializing
    *
    */
    protected byte[] getByteArray(Object obj)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(bos)); 
            }            
            oos.writeObject(obj);
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
    * Create an byte[] for the session that we can then pass to
    * the HA Store.
    *
    * @param attributeValue
    *   The attribute value we are serializing
    *
    */
    protected byte[] getByteArrayFromCollection(Collection entries)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;

        IOUtilsCaller utilsCaller = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            //HERCULES FIXME - for now reverting back
            //need to re-examine EJBUtils and related serialization classes
            //Bug 4832603 : EJB Reference Failover
            /*  was this
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             end was this */
              //oos = EJBUtils.getOutputStream(new BufferedOutputStream(bos), true);	
            //end - Bug 4832603 
            
            if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                try {
                    oos = utilsCaller.createObjectOutputStream(new BufferedOutputStream(bos), true);
                } catch (Exception ex) {}
            }
            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(bos)); 
            }
            //first write out the entriesSize
            int entriesSize = entries.size();
            oos.writeObject(Integer.valueOf(entriesSize));
            //then write out the entries
            Iterator it = entries.iterator();
            while(it.hasNext()) {
                oos.writeObject(it.next());
            }
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
     * get the utility class used to call into services from IOUtils
     */
    protected IOUtilsCaller getWebUtilsCaller() {
        if(webUtilsCaller == null) {
            WebIOUtilsFactory factory = new WebIOUtilsFactory();
            webUtilsCaller = factory.createWebIOUtil();            
        }
        return webUtilsCaller;
    }    
    
    protected ReplicationState doTransmit(ReplicationState transmitState) {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return null;
        }         
        return this.doTransmit(transmitState, true);
    }    

    protected ReplicationState doTransmit(ReplicationState transmitState, boolean wait) {
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return null;
        }         
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        //this call relies on env property to allow dupIds during
        //replication or not
        ReplicationState resultState = 
                replicationSender.sendReplicationState(transmitState, 
                    wait, isDuplicateIdsSemanticsAllowed());
        return resultState;
    }
    
    private Metadata findSessionViaBroadcast(String id) {
        //begin test of size
        /*
        System.out.println("calling size()");
        int theSize = -2;
        try {
            theSize = size();
        } catch(BackingStoreException ex) {}
        System.out.println("result of size:" + theSize);
         */
        //end test of size
        
        //do multi-cast to cluster member and get back state
        ReplicationState state = 
            ReplicationState.createBroadcastQueryState(this.getMode(), id, this.getApplicationId(), this.getInstanceName());
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryInputState=" + state);        
            _logger.fine("JxtaBackingStoreImpl>>findSessionViaBroadcast:state = " + state);                        
        }         
        //System.out.println("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryInputState=" + state);        
        //System.out.println("JxtaBackingStoreImpl>>findSessionViaBroadcast:state = " + state);    
        JxtaReplicationSender sender 
            = JxtaReplicationSender.createInstance();
        ReplicationState queryResult = sender.sendReplicationStateQuery(state);
        if(_logger.isLoggable(Level.FINE)) {       
            _logger.fine("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryResult=" + queryResult);                        
        }         
        //System.out.println("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryResult=" + queryResult);        
        Metadata result = null;
        String userOrSsoId = null;         
        if(queryResult != null) {
            //extra parameter is ssoid in WEB case
            //extra parameter is userName in SSO case
            /* just put it in now
            if(MODE_WEB.equals(getMode())) {
                userOrSsoId = queryResult.getSsoId();
            } else {
                if(MODE_SSO.equals(getMode())) {
                    userOrSsoId = queryResult.getUsername();
                }
            }
             */
            userOrSsoId = queryResult.getExtraParam();
            if(!this.isCompositeBackingStore()) {
                result = new SimpleMetadata(queryResult.getVersion(),
                    queryResult.getLastAccess(), queryResult.getMaxInactiveInterval(),
                    queryResult.getState(), userOrSsoId);
            } else {
                result = ReplicationState.createCompositeMetadataFrom(queryResult);
            }
        }        
        return result;
    }
    
    private long parseLong(String s) {
        long result = -1L;
        try {
            result = Long.parseLong(s);
        } catch (NumberFormatException ex) {
            ;
        }
        return result;
    }
    
    private Metadata findSessionViaBroadcast(String id, long version) {        
        //do multi-cast to cluster member and get back state
        ReplicationState state = 
            ReplicationState.createBroadcastQueryState(this.getMode(), id, this.getApplicationId(), version, this.getInstanceName());
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryInputState=" + state);        
            _logger.fine("JxtaBackingStoreImpl>>findSessionViaBroadcast:state = " + state);                        
        }         
        //System.out.println("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryInputState=" + state);        
        //System.out.println("JxtaBackingStoreImpl>>findSessionViaBroadcast:state = " + state);    
        JxtaReplicationSender sender 
            = JxtaReplicationSender.createInstance();
        ReplicationState queryResult = sender.sendReplicationStateQuery(state);
        if(_logger.isLoggable(Level.FINE)) {       
            _logger.fine("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryResult=" + queryResult);                        
        }         
        //System.out.println("JxtaBackingStoreImpl>>findSessionViaBroadcast:queryResult=" + queryResult);        
        Metadata result = null;
        String userOrSsoId = null;         
        if(queryResult != null) {
            //extra parameter is ssoid in WEB case
            //extra parameter is userName in SSO case
            userOrSsoId = queryResult.getExtraParam();
            if(!this.isCompositeBackingStore()) {
                result = new SimpleMetadata(queryResult.getVersion(),
                    queryResult.getLastAccess(), queryResult.getMaxInactiveInterval(),
                    queryResult.getState(), userOrSsoId);
            } else {
                result = ReplicationState.createCompositeMetadataFrom(queryResult);
            }
        }        
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
    
    String getMode() {
        return _mode;
    } 
    
    public void setMode(String mode) {
        _mode = mode;
    }    
    
    String getApplicationId() {
        return _appid;
    }
    
    public boolean isCompositeBackingStore() {
        return _isCompositeBackingStore;
    }
    
    public void setCompositeBackingStore(boolean value) {
        _isCompositeBackingStore = value;
    }    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("_mode = " + _mode + "\n");
        sb.append("_appid = " + _appid + "\n");
        sb.append("_idleTimeoutInSeconds = " + _idleTimeoutInSeconds);
        return sb.toString();
    }
    
    public boolean isDuplicateIdsSemanticsAllowed() {
        return _duplicateIdsSemanticsAllowed;
    }    
    
    public void setDuplicateIdsSemanticsAllowed(boolean value) {
        _duplicateIdsSemanticsAllowed = value;
    } 
    
    private boolean isWaitForAckConfigured() {
        if(_waitForAckConfigured == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            boolean waitForAckProp = lookup.getWaitForAckPropertyFromConfig();
            _waitForAckConfigured = new Boolean(waitForAckProp);
        }
        return _waitForAckConfigured.booleanValue();
    }    
    
    /**
     * A utility class used to call into services from IOUtils
     */
    private IOUtilsCaller webUtilsCaller = null;     

    protected boolean _duplicateIdsSemanticsAllowed = false; 
    protected Boolean _waitForAckConfigured = null;
    private String _mode = null;
    private String _appid = null;
    private int _idleTimeoutInSeconds = 0;
    private boolean _isCompositeBackingStore = false;
    
}
