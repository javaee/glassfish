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
 * ReplicationState.java
 *
 * Created on November 22, 2005, 11:45 AM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicBoolean;
import com.sun.logging.LogDomains;
import com.sun.enterprise.web.ServerConfigLookup;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import org.apache.catalina.Session;
import org.apache.catalina.session.*;

import com.sun.appserv.ha.spi.*;

/**
 *
 * @author Larry White
 */
public class ReplicationState implements Serializable {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private final static Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    
    public final static String LOGGER_MEM_REP 
        = "com.sun.enterprise.ee.web.sessmgmt";

    public final static String MODE_WEB = "web";
    public final static String MODE_SSO = "sso";
    public final static String MODE_EJB = "ejb";
    public final static String MODE_STARTUP = "startup";      
    public final static String MESSAGE_MODE = "message_mode";
    public final static String BULK_MESSAGE_MODE = "bulk_message_mode";
    public final static String BULK_MESSAGE_ID = "bulk_message_id";    
    public final static String MESSAGE_ID = "message_id";
    final static String MESSAGE_APPID = "message_appid";
    final static String MESSAGE_VERSION = "message_version";
    final static String MESSAGE_COMMAND = "message_command";
    final static String MESSAGE_LAST_ACCESS = "message_last_access";
    final static String MESSAGE_MAX_INACTIVE = "message_max_inactive";
    //final static String MESSAGE_SSO_ID = "message_sso_id";
    //final static String MESSAGE_USER_NAME = "message_user_name";
    final static String MESSAGE_EXTRA_PARAM = "message_extra_param";
    public final static String MESSAGE_ACK_REQUIRED = "message_ack_required";
    public final static String MESSAGE_ACK_LIST_PROPERTY = "message_ack_list_property";
    public final static String MESSAGE_SEND_START_TIME = "message_send_start_time";
    final static String MESSAGE_QUERY_RESULT = "message_query_result";
    final static String MESSAGE_INSTANCE_NAME = "message_instance_name";    
    final static String MESSAGE_DATA = "message_data";
    final static String MESSAGE_TOTAL_STATES = "message_total_states";
    final static String MESSAGE_ACK_IDS_LIST = "message_ack_ids_list";
    final static String MESSAGE_TRUNK_DATA = "message_trunk_data";
    final static String MESSAGE_READY = "ready";
    final static String ReadyMessage = "ready";
    
    final static String RETURN_MSG_COMMAND = "response";
    final static String MESSAGE_BROADCAST_QUERY = "broadcastFindSession";
    final static String RETURN_BROADCAST_MSG_COMMAND = "broadcastResponse";
    final static String InstanceNameMessage = "instance_name";
    final static boolean METHOD_RETURN_VOID = true;
    //commands
    public final static String SAVE_COMMAND = "save";
    public final static String VALVE_SAVE_COMMAND = "valveSave";
    public final static String REMOVE_COMMAND = "remove";    
    public final static String UNDEPLOY_COMMAND = "undeploy";
    public final static String REMOVE_EXPIRED_COMMAND = "removeExpired";
    public final static String REMOVE_SYNCHRONIZED_COMMAND = "removeSynchronized";
    public final static String UPDATE_LAST_ACCESS_TIME_COMMAND = "updateLastAccessTime";
    public final static String SIZE_COMMAND = "size";
    public final static String COMPOSITE_SAVE_COMMAND = "compositeSave";
    public final static String REMOVE_IDS_COMMAND = "removeIds";
    final static String BULK_MESSAGE_COMMAND = "bulk_message_command";
    
    public final static String HC_COMMAND = "healthCheck";
    final static String RETURN_HC_MSG_COMMAND = "healthCheckResponse";
    
    public final static String DUPLICATE_IDS_SEMANTICS_PROPERTY 
        = "duplicate_ids_semantics_property";
    public final static String WAIT_FOR_ACK_PROPERTY 
        = "wait_for_ack_property"; 
    
    public static final String ID = "id";
    
    /**
     * Creates a new instance of ReplicationState
     */
    public ReplicationState() {
    }
    
    /**
     * Creates a new instance of ReplicationState
     * want this package protected
     */
    ReplicationState(Object id) {
        this();
        _id = id;
    }    
    
    /**
     * Creates a new instance of ReplicationState
     */
    public ReplicationState(String mode, Object id, String appId, long version, long lastAccess, long maxInactiveInterval, String extraParam, Object queryResult, String instanceName, String command, byte[] state, byte[] trunkState) {
        _mode = mode;
        _id = id;
        _appId = appId;
        _version = version;
        _maxInactiveInterval = maxInactiveInterval;
        _lastAccess = lastAccess;
        _extraParam = extraParam;
        _queryResult = queryResult;
        _instanceName = instanceName;
        _command = command;
        _state = state;
        _trunkState = trunkState;
        _hc = _id.hashCode();
    }

    /**
     * the list of method names that are removes
     */ 
    private static List removeMethods 
        = Arrays.asList(REMOVE_COMMAND, REMOVE_SYNCHRONIZED_COMMAND);

    
    /**
     * the list of method names with void return
     */    
    private static List voidReturnsMethods 
        = Arrays.asList(SAVE_COMMAND, 
            VALVE_SAVE_COMMAND, REMOVE_SYNCHRONIZED_COMMAND, 
            UPDATE_LAST_ACCESS_TIME_COMMAND, COMPOSITE_SAVE_COMMAND,
            HC_COMMAND);
    
    /**
     * the list of method names that are hc (health check)
     */    
    private static List hcMethods 
        = Arrays.asList(HC_COMMAND);     

    /**
     * the list of method names that are responses
     */    
    private static List responseMethods 
        = Arrays.asList(RETURN_MSG_COMMAND, RETURN_BROADCAST_MSG_COMMAND);   
   
    /**
     * create a response ReplicationState from the input ReplicationState
     * @param input
     */     
    public static ReplicationState createResponseFrom(ReplicationState input) {
        return new ReplicationState(
            input.getMode(),
            input.getId(), 
            input.getAppId(),
            input.getVersion(),
            input.getLastAccess(),
            input.getMaxInactiveInterval(),
            input.getExtraParam(),
            input.getQueryResult(),
            input.getInstanceName(), 
            RETURN_MSG_COMMAND,
            input.getState(),
            input.getTrunkState());
    }  

    /**
     * create a query response ReplicationState from the input ReplicationState
     * @param input
     */     
    public static ReplicationState createQueryResponseFrom(ReplicationState input) {
        return new ReplicationState(
            input.getMode(),
            input.getId(), 
            input.getAppId(),
            input.getVersion(),
            input.getLastAccess(),
            input.getMaxInactiveInterval(),
            input.getExtraParam(),
            input.getQueryResult(),
            input.getInstanceName(), 
            RETURN_BROADCAST_MSG_COMMAND,
            input.getState(),
            input.getTrunkState());
    }
    
    /**
     * create a response ReplicationState from the input ReplicationState
     * @param input
     *
     * @param newState - updated state
     */     
    public static ReplicationState createUpdatedStateFrom(ReplicationState input, byte[] newState) {
        return new ReplicationState(
            input.getMode(),
            input.getId(), 
            input.getAppId(),
            input.getVersion(),
            input.getLastAccess(),
            input.getMaxInactiveInterval(),
            input.getExtraParam(),
            input.getQueryResult(),
            input.getInstanceName(), 
            RETURN_MSG_COMMAND,
            newState,
            input.getTrunkState());
    }
    
    static List<ReplicationState> extractBulkReplicationStatesFromMessage(Message msg) {
        List<ReplicationState> states = null;
        byte[] data = null;
        MessageElement dataMsgElement = 
            msg.getMessageElement(MESSAGE_DATA, MESSAGE_DATA);
        MessageElement totalStatesElement = 
            msg.getMessageElement(MESSAGE_TOTAL_STATES, MESSAGE_TOTAL_STATES);
        if(dataMsgElement != null) {
            data = dataMsgElement.getBytes(false);
            ObjectInputStream ois = null;
            ByteArrayInputStream bis = null;
            try {
                bis = new ByteArrayInputStream(data);
                ois = new ObjectInputStream(bis);
                states = (List<ReplicationState>) ois.readObject();
            } catch (IOException ioEx) {
                _logger.log(Level.INFO, "ReplicationState: IOEx ", ioEx);
            } catch (ClassNotFoundException cnfEx) {
                _logger.log(Level.INFO, "ReplicationState: CNF ", cnfEx);
            } finally {
                try { 
                    bis.close();
                } catch (Exception ex) {
                    if (_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("error closing stream");
                    }
                }
                try { 
                    ois.close(); 
                } catch (Exception ex) {
                    if (_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("error closing stream");
                    }                    
                }
            }
        }
        return states;
    }
    

    /**
     * create a ReplicationState from the input msg
     * @param msg
     */      
    public static ReplicationState createReplicationState(Message msg) {
        //FIXME all messages should have a non-null mode check all
        // message creating code later
        String mode = MODE_WEB;
        String id = "";
        String appid = "";
        String bulkMode = null;
        String bulkId = "";
        long version = 0L;
        long lastAccess = 0L;
        long maxInactive = 0L;
        String extraParam = null;
        Object queryResult = null;
        String instanceName = null;
        byte[] data = null;
        byte[] trunkData = null;
        
        MessageElement modeMsgElement = 
            msg.getMessageElement(MESSAGE_MODE, MESSAGE_MODE);
        if(modeMsgElement != null) {
            mode = modeMsgElement.toString();
        }
        MessageElement idMsgElement = 
            msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        if(idMsgElement != null) {
            id = idMsgElement.toString();
        }
        
        //added for bulk messages
        MessageElement bulkModeMsgElement = 
            msg.getMessageElement(BULK_MESSAGE_MODE, BULK_MESSAGE_MODE);
        if(bulkModeMsgElement != null) {
            bulkMode = bulkModeMsgElement.toString();
        }
        MessageElement bulkIdMsgElement = 
            msg.getMessageElement(BULK_MESSAGE_ID, BULK_MESSAGE_ID);
        if(bulkIdMsgElement != null) {
            bulkId = bulkIdMsgElement.toString();
        }
        //end added for bulk messages
        
        MessageElement appidMsgElement = 
            msg.getMessageElement(MESSAGE_APPID, MESSAGE_APPID);
        if(appidMsgElement != null) {
            appid = appidMsgElement.toString();
        }
        MessageElement versionMsgElement = 
            msg.getMessageElement(MESSAGE_VERSION, MESSAGE_VERSION);
        if(versionMsgElement != null) {
            version = 
                (Long.decode(versionMsgElement.toString())).longValue();
        }        
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String command = commandMsgElement.toString();                
        MessageElement lastAccessMsgElement = 
            msg.getMessageElement(MESSAGE_LAST_ACCESS, MESSAGE_LAST_ACCESS);
        if(lastAccessMsgElement != null) {
            lastAccess = 
                (Long.decode(lastAccessMsgElement.toString())).longValue();
        }
        MessageElement maxInactiveMsgElement = 
            msg.getMessageElement(MESSAGE_MAX_INACTIVE, MESSAGE_MAX_INACTIVE);
        if(maxInactiveMsgElement != null) {
            maxInactive = 
                (Long.decode(maxInactiveMsgElement.toString())).longValue();
        }
        MessageElement instanceNameMsgElement = 
            msg.getMessageElement(MESSAGE_INSTANCE_NAME, MESSAGE_INSTANCE_NAME);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>createReplicationState:instanceNameMsgElement: " + instanceNameMsgElement);
        }        
        //System.out.println("ReplicationState>>createReplicationState:instanceNameMsgElement: " + instanceNameMsgElement);
        if(instanceNameMsgElement != null) {
            instanceName = instanceNameMsgElement.toString();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("ReplicationState>>createReplicationState:instanceNameString: " + instanceName);
            }            
            //System.out.println("ReplicationState>>createReplicationState:instanceNameString: " + instanceName);
        }
        MessageElement extraParamMsgElement = 
            msg.getMessageElement(MESSAGE_EXTRA_PARAM, MESSAGE_EXTRA_PARAM);
        if(extraParamMsgElement != null) {
            extraParam = extraParamMsgElement.toString();
        }
        //FIXME assuming queryResult is a string encoding an Integer
        MessageElement queryResultMsgElement = 
            msg.getMessageElement(MESSAGE_QUERY_RESULT, MESSAGE_QUERY_RESULT);
        if(queryResultMsgElement != null) {
            queryResult = 
                Integer.decode(maxInactiveMsgElement.toString());
        }               
        MessageElement dataMsgElement = 
            msg.getMessageElement(MESSAGE_DATA, MESSAGE_DATA);
        if(dataMsgElement != null) {
            data = dataMsgElement.getBytes(false);
        }
        MessageElement trunkDataMsgElement = 
            msg.getMessageElement(MESSAGE_TRUNK_DATA, MESSAGE_TRUNK_DATA);
        if(trunkDataMsgElement != null) {
            trunkData = trunkDataMsgElement.getBytes(false);
        } 
        
        //added for bulk message support
        //System.out.println("ReplicationState>>createReplicationState:bulkId = " + bulkId + " id = " + id);
        //System.out.println("ReplicationState>>createReplicationState:bulkMode = " + bulkMode + " mode = " + mode);
        if(bulkId != null && bulkMode != null) {
            id = bulkId;
            mode = bulkMode;
        }
        //end added for bulk message support
        
        ReplicationState state = 
            new ReplicationState(mode, id, appid, version, lastAccess, maxInactive, extraParam, queryResult, instanceName, command, data, trunkData);        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>createReplicationState:creating ReplicationState: " + state);
        }               
        //System.out.println("ReplicationState>>createReplicationState:creating ReplicationState: " + state);
        return state;
    }
    
    /**
     * create a broadcast query ReplicationState from the input
     * @param mode
     * @param id
     * @param appid
     * @param instanceName
     */     
    public static ReplicationState createBroadcastQueryState(String mode, String id, String appid, String instanceName) {
        //if version is not specified it will be created as -1L for backward compatibility
        return createBroadcastQueryState(mode, id, appid, -1L, instanceName); 
    }
    
    /**
     * create a broadcast query ReplicationState from the input
     * @param mode
     * @param id
     * @param appid
     * @param version
     * @param instanceName
     */     
    public static ReplicationState createBroadcastQueryState(String mode, String id, String appid, long version, String instanceName) {
        return new ReplicationState(mode, id, appid, version, 0L, 0L, null, null, instanceName, MESSAGE_BROADCAST_QUERY, null, null);
    }        
    
    /**
     * create a query ReplicationState from the input
     * @param mode
     * @param id
     * @param appid
     * @param command
     */     
    public static ReplicationState createQueryState(String mode, String id, String appid, String command) {
        return new ReplicationState(mode, id, appid, 0L, 0L, 0L, null, null, lookupInstanceName(), command, null, null);
    }
    
    /**
     * create a query ReplicationState from the input
     * @param mode
     * @param id
     * @param appid
     * @param queryResult
     */     
    public static ReplicationState createQueryStateResponse(String mode, String id, String appid, String sourceInstanceName, Object queryResult) {
        return new ReplicationState(mode, id, appid, 0L, 0L, 0L, null, queryResult, sourceInstanceName, RETURN_MSG_COMMAND, null, null);
    }    

    /**
     * create a broadcast ReplicationState from the input Message
     * @param msg
     */      
    public static ReplicationState createBroadcastReplicationState(Message msg) {
        //FIXME all messages should have a non-null mode check all
        // message creating code later
        String mode = MODE_WEB;
        long version = 0L;
        String extraParam = null;
        String instanceName = null;
        MessageElement modeMsgElement = 
            msg.getMessageElement(MESSAGE_MODE, MESSAGE_MODE);
        if(modeMsgElement != null) {
            mode = modeMsgElement.toString();
        }
        MessageElement idMsgElement = 
            msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        String id = idMsgElement.toString();
        MessageElement appidMsgElement = 
            msg.getMessageElement(MESSAGE_APPID, MESSAGE_APPID);
        String appid = appidMsgElement.toString();        
        MessageElement versionMsgElement = 
            msg.getMessageElement(MESSAGE_VERSION, MESSAGE_VERSION);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState:createBroadcastReplicationState:versMsgElem=" + versionMsgElement);
        }         
        //System.out.println("ReplicationState:createBroadcastReplicationState:versMsgElem=" + versionMsgElement);
        if(versionMsgElement != null) {
            version = 
                (Long.decode(versionMsgElement.toString())).longValue();
        }
        MessageElement extraParamMsgElement = 
            msg.getMessageElement(MESSAGE_EXTRA_PARAM, MESSAGE_EXTRA_PARAM);
        if(extraParamMsgElement != null) {
            extraParam = extraParamMsgElement.toString();
        }
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String command = commandMsgElement.toString();
        MessageElement instanceNameMsgElement = 
            msg.getMessageElement(MESSAGE_INSTANCE_NAME, MESSAGE_INSTANCE_NAME);
        if(instanceNameMsgElement != null) {
            instanceName = instanceNameMsgElement.toString();
        }
        /* don't need these message element - remove after testing
        //FIXME will need to add more later
        MessageElement lastAccessMsgElement = 
            msg.getMessageElement(MESSAGE_LAST_ACCESS, MESSAGE_LAST_ACCESS);
        long lastAccess = 
            (Long.decode(lastAccessMsgElement.toString())).longValue();
        MessageElement maxInactiveMsgElement = 
            msg.getMessageElement(MESSAGE_MAX_INACTIVE, MESSAGE_MAX_INACTIVE);
        long maxInactive = 
            (Long.decode(maxInactiveMsgElement.toString())).longValue(); 
        MessageElement instanceNameMsgElement = 
            msg.getMessageElement(MESSAGE_INSTANCE_NAME, MESSAGE_INSTANCE_NAME);
        String instanceName = instanceNameMsgElement.toString();
        MessageElement extraParamMsgElement = 
            msg.getMessageElement(MESSAGE_EXTRA_PARAM, MESSAGE_EXTRA_PARAM);
        String extraParam = extraParamMsgElement.toString();
         */
        byte[] data = null;
        MessageElement dataMsgElement = 
            msg.getMessageElement(MESSAGE_DATA, MESSAGE_DATA);
        if(dataMsgElement != null) {
            data = dataMsgElement.getBytes(false);
        }
        byte[] trunkData = null;
        MessageElement trunkDataMsgElement = 
            msg.getMessageElement(MESSAGE_TRUNK_DATA, MESSAGE_TRUNK_DATA);
        if(trunkDataMsgElement != null) {
            trunkData = trunkDataMsgElement.getBytes(false);
        }        
        
        /*
        ReplicationState state = 
            new ReplicationState(id, appid, lastAccess, maxInactive, ssoId, userName, command, data);
         */ 
        ReplicationState state = 
            new ReplicationState(mode, id, appid, version, 0L, 0L, extraParam, null, instanceName, command, data, trunkData);       
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState:createBroadcastReplicationState:creating ReplicationState from broadcast: " + state);
        }              
        //System.out.println("ReplicationState:createBroadcastReplicationState:creating ReplicationState from broadcast: " + state);
        return state;
    }    

    /**
     * create a Message from the input state
     * @param state
     */    
    public static Message createMessage(ReplicationState state) {
        return createMessage(state, false);
    }     

    /**
     * create a Message from the input state
     * @param state
     * @param isResponse is the created message a response
     */     
    public static Message createMessage(ReplicationState state, boolean isResponse) {
        Message msg = new Message();
        String mode = state.getMode();
        String id = (String)state.getId();
        String appid = state.getAppId();
        String command = state.getCommand();
        Long version = new Long(state.getVersion());
        Long lastAccess = new Long(state.getLastAccess());
        Long maxInactive = new Long(state.getMaxInactiveInterval());
        String extraParam = state.getExtraParam();
        //FIXME for now assuming this is a Long
        //for generality will have to serialize and use bytearray
        Integer queryResult = (Integer)state.getQueryResult();
        String instanceName = state.getInstanceName();
        if(instanceName == null) {
            //put existing instanceName for source if it is missing here
            instanceName = lookupInstanceName();
        }        
        byte[] data = state.getState();
        byte[] trunkData = state.getTrunkState();
        boolean ackRequired = state.isAckRequired();
        long sendStartTime = state.getSendStartTime();
        //String data = "Message #" + i;
        msg.addMessageElement(MESSAGE_MODE,
                              new StringMessageElement(MESSAGE_MODE,
                                                       mode,
                                                       null));
        if(mode != null && mode.equalsIgnoreCase(BULK_MESSAGE_MODE)) {
            msg.addMessageElement(BULK_MESSAGE_MODE,
                    new StringMessageElement(BULK_MESSAGE_MODE,
                                             "BULK",
                                             null)); 
        }
        msg.addMessageElement(MESSAGE_ID,
                              new StringMessageElement(MESSAGE_ID,
                                                       id,
                                                       null));
        msg.addMessageElement(MESSAGE_APPID,
                              new StringMessageElement(MESSAGE_APPID,
                                                       appid,
                                                       null));
        msg.addMessageElement(MESSAGE_VERSION,
                              new StringMessageElement(MESSAGE_VERSION,
                                                       version.toString(),
                                                       null));         
        String theCommand = command;
        if(isResponse) {
            theCommand = RETURN_MSG_COMMAND;
        }
        msg.addMessageElement(MESSAGE_COMMAND,
                              new StringMessageElement(MESSAGE_COMMAND,
                                                       theCommand,
                                                       null));        
        msg.addMessageElement(MESSAGE_LAST_ACCESS,
                              new StringMessageElement(MESSAGE_LAST_ACCESS,
                                                       lastAccess.toString(),
                                                       null)); 
        msg.addMessageElement(MESSAGE_MAX_INACTIVE,
                              new StringMessageElement(MESSAGE_MAX_INACTIVE,
                                                       maxInactive.toString(),
                                                       null));
        if(extraParam != null) {
            msg.addMessageElement(MESSAGE_EXTRA_PARAM,
                              new StringMessageElement(MESSAGE_EXTRA_PARAM,
                                                       extraParam,
                                                       null));
        }
        if(queryResult != null) {
            msg.addMessageElement(MESSAGE_QUERY_RESULT,
                                  new StringMessageElement(MESSAGE_QUERY_RESULT,
                                                           queryResult.toString(),
                                                           null)); 
        }
        if(instanceName != null) {
            msg.addMessageElement(MESSAGE_INSTANCE_NAME,
                              new StringMessageElement(MESSAGE_INSTANCE_NAME,
                                                       instanceName,
                                                       null));
        }
        if(data != null) {
            msg.addMessageElement(MESSAGE_DATA,
                              new ByteArrayMessageElement(MESSAGE_DATA,
                                                       null,
                                                       data,
                                                       null));
        }
        if(trunkData != null) {
            msg.addMessageElement(MESSAGE_TRUNK_DATA,
                              new ByteArrayMessageElement(MESSAGE_TRUNK_DATA,
                                                       null,
                                                       trunkData,
                                                       null));
        }
        //is ack required
        String ackRequiredString = "N";
        if(ackRequired) {
            ackRequiredString = "Y";
        }
        msg.addMessageElement(MESSAGE_ACK_REQUIRED,
                              new StringMessageElement(MESSAGE_ACK_REQUIRED,
                                                       ackRequiredString,
                                                       null));
        //a property not sent but available for quick-ack case
        msg.setMessageProperty(MESSAGE_ACK_REQUIRED, ackRequiredString);
        List ackIdsList = state.getAckIdsList();
        if(state.getAckIdsList() != null) {
            msg.setMessageProperty(MESSAGE_ACK_LIST_PROPERTY, ackIdsList);
        }
 
        //send start time for measurements
        if(sendStartTime != -1) {
            msg.addMessageElement(MESSAGE_SEND_START_TIME,
                                  new StringMessageElement(MESSAGE_SEND_START_TIME,
                                                           "" + sendStartTime,
                                                           null));
            msg.setMessageProperty(MESSAGE_SEND_START_TIME, ""+sendStartTime);
        }
        
        
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState:createMessage:Sending Message id:" + id + " appid:" + appid + " command:" + command);
        }         
        //System.out.println("ReplicationState:createMessage:Sending Message id:" + id + " appid:" + appid + " command:" + command);
        return msg;
    }
    
    /**
     * create a Message from the input state
     * @param msgID 
     * @param totalStates the number of total states
     * @param data the serialized list of ReplicationStates
     * this version creates a message with no ack required
     */     
    public static Message createBulkMessage(long msgID, int totalStates, byte[] data) {
        return createBulkMessage(msgID, totalStates, data, false);
    }
    
    /**
     * create a Message from the input state
     * @param msgID 
     * @param totalStates the number of total states
     * @param data the serialized list of ReplicationStates
     * @param ackRequired does this message require an ack
     */     
    public static Message createBulkMessage(long msgID, int totalStates, byte[] data, boolean ackRequired) {
        Message msg = new Message();
        
        msg.addMessageElement(BULK_MESSAGE_MODE,
                new StringMessageElement(BULK_MESSAGE_MODE,
                                         "BULK",
                                         null)); 
        
        msg.addMessageElement(BULK_MESSAGE_ID,
                new StringMessageElement(BULK_MESSAGE_ID,
                                         ""+msgID,
                                         null));
        
        msg.setMessageProperty(BULK_MESSAGE_ID, ""+msgID);
        

        if (data != null) {
            msg.addMessageElement(MESSAGE_TOTAL_STATES,
                    new StringMessageElement(MESSAGE_TOTAL_STATES,
                                             ""+totalStates,
                                             null));
            
            msg.addMessageElement(MESSAGE_DATA,
                              new ByteArrayMessageElement(MESSAGE_DATA,
                                                       null,
                                                       data,
                                                       null));
        }
        
        //is ack required
        String ackRequiredString = "N";
        if(ackRequired) {
            ackRequiredString = "Y";
        }
        msg.addMessageElement(MESSAGE_ACK_REQUIRED,
                              new StringMessageElement(MESSAGE_ACK_REQUIRED,
                                                       ackRequiredString,
                                                       null));
        
        //send start time for measurements
        long sendStartTime = System.currentTimeMillis();
        System.out.println("bulk message send startTime=" + sendStartTime);
        msg.addMessageElement(MESSAGE_SEND_START_TIME,
                              new StringMessageElement(MESSAGE_SEND_START_TIME,
                                                       "" + sendStartTime,
                                                       null));
        msg.setMessageProperty(MESSAGE_SEND_START_TIME, ""+sendStartTime);

        if(_logger.isLoggable(Level.INFO)) {
            _logger.info("ReplicationState:createBulkMessage:Sending BULK_Message id:" + msgID + "   size: " + data.length);
        }
        
        return msg;
    } 
    
    /**
     * create a Message from the input state
     * @param msgID 
     * @param totalStates the number of total states
     * @param data the serialized list of ReplicationStates
     * @param ackRequired does this message require an ack
     */     
    public static ReplicationState createBulkReplicationState(long msgID, List<String> ackIdsList, byte[] data, boolean ackRequired) {
        String id = "" + msgID;
        ReplicationState resultState =
            new ReplicationState(BULK_MESSAGE_MODE, //bulk mode
                id, //id  
                id,     //appid (does not matter in this bulk case; must be non null)
                -1,  //version
                0L,  //lastaccesstime
                0L, //maxInactiveInterval (seconds)                
                null,  // (extraParam)
                null, //queryResult not used here
                null, //instanceName
                VALVE_SAVE_COMMAND, //command (does not matter in this bulk case; must be non-null)
                data,      //state
                null);     //trunkState
        resultState.setAckRequired(ackRequired);
        resultState.setAckIdsList(ackIdsList);
        return resultState;
    }       

    /**
     * create an ack Message from the input
     * @param msg
     */     
    public static Message createAckMessageFrom(Message msg) {
        Message ackMsg = new Message();
        
        //echo the mode
        String mode = MODE_WEB;
        MessageElement modeMsgElement = 
            msg.getMessageElement(MESSAGE_MODE, MESSAGE_MODE);
        if(modeMsgElement != null) {
            mode = modeMsgElement.toString();
        }        
        ackMsg.addMessageElement(MESSAGE_MODE,
                              new StringMessageElement(MESSAGE_MODE,
                                                       mode,
                                                       null));         
        //echo the id
        MessageElement idMsgElement = 
        msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        String id = idMsgElement.toString();
        ackMsg.addMessageElement(MESSAGE_ID,
                              new StringMessageElement(MESSAGE_ID,
                                                       id,
                                                       null));
        //echo the appid
        MessageElement appidMsgElement = 
            msg.getMessageElement(MESSAGE_APPID, MESSAGE_APPID);
        String appid = appidMsgElement.toString();
        ackMsg.addMessageElement(MESSAGE_APPID,
                              new StringMessageElement(MESSAGE_APPID,
                                                       appid,
                                                       null));
        //********** test begin**********************
        //get the current command
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String previousCommand = commandMsgElement.toString();
        String theCommand = RETURN_MSG_COMMAND;
        if(previousCommand.equals(ReplicationState.HC_COMMAND)) {
            theCommand = RETURN_HC_MSG_COMMAND;
        }
        //this is a return command
        /* FIXME replace next line with above - need to test
        String theCommand = RETURN_MSG_COMMAND;
         */        
        //********** test end  **********************
        

        ackMsg.addMessageElement(MESSAGE_COMMAND,
                              new StringMessageElement(MESSAGE_COMMAND,
                                                       theCommand,
                                                       null));        
        return ackMsg;
    }
    
    /**
     * create an bulk ack Message from the input
     * @param msg
     */     
    public static Message createBulkAckMessageFrom(Message msg, List<ReplicationState> states) {
        List<String> ackIdsList = extractAckIdsList(states);
        //displayStringList(ackIdsList);
        byte[] listAckIds = null;
        try {
            listAckIds = getByteArray(ackIdsList);
        } catch (IOException ex) {
            //deliberate no-op
            ;
        }
        
        Message ackMsg = new Message();
        
        //echo the mode
        String mode = MODE_WEB;
        MessageElement modeMsgElement = 
            msg.getMessageElement(MESSAGE_MODE, MESSAGE_MODE);
        if(modeMsgElement != null) {
            mode = modeMsgElement.toString();
        }        
        ackMsg.addMessageElement(MESSAGE_MODE,
                              new StringMessageElement(MESSAGE_MODE,
                                                       mode,
                                                       null));
        
        ackMsg.addMessageElement(BULK_MESSAGE_MODE,
                new StringMessageElement(BULK_MESSAGE_MODE,
                                         "BULK",
                                         null));
        
        //echo the id
        MessageElement idMsgElement = 
        msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        String id = idMsgElement.toString();
        ackMsg.addMessageElement(MESSAGE_ID,
                              new StringMessageElement(MESSAGE_ID,
                                                       id,
                                                       null));
        //echo the appid
        MessageElement appidMsgElement = 
            msg.getMessageElement(MESSAGE_APPID, MESSAGE_APPID);
        String appid = appidMsgElement.toString();
        ackMsg.addMessageElement(MESSAGE_APPID,
                              new StringMessageElement(MESSAGE_APPID,
                                                       appid,
                                                       null));
        //********** test begin**********************
        //get the current command
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String previousCommand = commandMsgElement.toString();
        String theCommand = RETURN_MSG_COMMAND;
        if(previousCommand.equals(ReplicationState.HC_COMMAND)) {
            theCommand = RETURN_HC_MSG_COMMAND;
        }
        //this is a return command
        /* FIXME replace next line with above - need to test
        String theCommand = RETURN_MSG_COMMAND;
         */        
        //********** test end  **********************
        

        ackMsg.addMessageElement(MESSAGE_COMMAND,
                              new StringMessageElement(MESSAGE_COMMAND,
                                                       theCommand,
                                                       null));
        //serialized list of ids to ack back to
        if(listAckIds != null) {
            ackMsg.addMessageElement(MESSAGE_ACK_IDS_LIST,
                              new ByteArrayMessageElement(MESSAGE_ACK_IDS_LIST,
                                                       null,
                                                       listAckIds,
                                                       null));
        }
               
        return ackMsg;
    }
    
    public static void displayStringList(List<String> stringList) {
        for(int i=0; i<stringList.size(); i++) {
            _logger.log(Level.INFO, "displayStringList:elem[" + i + "] = " + stringList.get(i));
        }
    }
    
    public static List<String> extractAckIdsList(List<ReplicationState> states) {
        List<String> ackIdsList = new ArrayList();
        for(int i=0; i<states.size(); i++) {
            ReplicationState nextState = states.get(i);
            //only add ids to ackIdsList that require an ack
            if(nextState != null && nextState.isAckRequired()) {
                ackIdsList.add((String)nextState.getId());
            }
        }
        return ackIdsList;
    }
    
    
    public static List<String> extractAllIdsList(List<ReplicationState> states) {
        List<String> allIdsList = new ArrayList();
        for(int i=0; i<states.size(); i++) {
            ReplicationState nextState = states.get(i);
            //provide the id[vers:xxx] for all states in list
            if(nextState != null) {
                allIdsList.add((String)nextState.getId() + "[ver:" + nextState.getVersion() + "]");
            }
        }
        return allIdsList;
    }
    
    static List<String> extractAckIdsListFromMessage(Message msg) {
        List<String> result = new ArrayList();
        byte[] data = null;
        MessageElement ackIdsMsgElement = 
            msg.getMessageElement(MESSAGE_ACK_IDS_LIST, MESSAGE_ACK_IDS_LIST);
        if(ackIdsMsgElement != null) {
            data = ackIdsMsgElement.getBytes(false);
        }
        try {
            result = (ArrayList)getObjectValue(data);
        } catch (Exception ex) {
            //deliberate no-op
            ;
        }
        return result;
    }

    /**
     * true means this is a broadcast message
     */    
    public static boolean isBroadcastState(ReplicationState state) {
        return (state.getCommand() != null 
            && state.getCommand().equals(MESSAGE_BROADCAST_QUERY));
    }
    
    /**
     * true means void return
     */    
    public boolean isVoidMethodReturnState() {
        String methodName = this.getCommand();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>isVoidMethodReturnState:methodName = " + methodName);
        }        
        //System.out.println("ReplicationState>>isVoidMethodReturnState:methodName = " + methodName);
        if(methodName == null) {
            return false;
        } else {
            return isMethodVoidReturn(methodName);
        }
    }    

    /**
     * true means void return command (i.e. method)
     */    
    static boolean isMethodVoidReturn(String methodName) {
        return voidReturnsMethods.contains(methodName);
    } 
    
    /**
     * true means void return command (i.e. method)
     */    
    public static boolean isVoidMethodReturnMessage(Message msg) {        
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String methodName = commandMsgElement.toString();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>isVoidMethodReturnMessage:methodName = " + methodName);
        }        
        //System.out.println("ReplicationState>>isVoidMethodReturnMessage:methodName = " + methodName);
        if(methodName == null) {
            return false;
        } else {
            return isMethodVoidReturn(methodName);
        }
    }
    
    /**
     * true means state command is one of remove methods
     */    
    public boolean isRemoveMethodState() {
        String methodName = this.getCommand();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>isRemoveMethodState:methodName = " + methodName);
        }        
        //System.out.println("ReplicationState>>isRemoveMethodState:methodName = " + methodName);
        if(methodName == null) {
            return false;
        } else {
            return isMethodRemove(methodName);
        }
    }     
    
    /**
     * true means one of remove commands (i.e. method)
     */    
    static boolean isMethodRemove(String methodName) {
        return removeMethods.contains(methodName);
    }
    
    ///
    
   /**
     * true means void return command (i.e. method)
     */    
    static boolean isMethodHC(String methodName) {
        return hcMethods.contains(methodName);
    }
    
    /**
     * true means void return command (i.e. method)
     */    
    public static boolean isHCMessage(Message msg) {        
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String methodName = commandMsgElement.toString();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>isHCMessage:methodName = " + methodName);
        }        
        //System.out.println("ReplicationState>>isHCMessage:methodName = " + methodName);
        if(methodName == null) {
            return false;
        } else {
            return isMethodHC(methodName);
        }
    }    
    ///

    /**
     * @param msg
     * true means a response message
     */    
    public static boolean isResponseMessage(Message msg) {        
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);
        String methodName = commandMsgElement.toString();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>isResponseMessage:methodName = " + methodName);
        }        
        //System.out.println("ReplicationState>>isResponseMessage:methodName = " + methodName);
        if(methodName == null) {
            return false;
        } else {
            return isMethodResponse(methodName);
        }
    }    
    
    /**
     * true means ack is required
     */    
    public static boolean isAckRequiredForMessage(Message msg) {
        String ackRequiredString = "N";
        MessageElement ackRequiredMsgElement = 
            msg.getMessageElement(MESSAGE_ACK_REQUIRED, MESSAGE_ACK_REQUIRED);
        if(ackRequiredMsgElement != null) {
            ackRequiredString = ackRequiredMsgElement.toString();
        }
        return("Y".equalsIgnoreCase(ackRequiredString));       
    }     
    
    /**
     * true means a response
     */    
    public boolean isResponseState() {
        String methodName = this.getCommand();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>isResponseState:methodName = " + methodName);
        }        
        //System.out.println("ReplicationState>>isResponseState:methodName = " + methodName);
        if(methodName == null) {
            return false;
        } else {
            return isMethodResponse(methodName);
        }
    }
    
    /**
     * true means void return command (i.e. method)
     */    
    static boolean isMethodResponse(String methodName) {
        return responseMethods.contains(methodName);
    }    
    
    private static String lookupInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceName = lookup.getServerName();
        return instanceName;
    }
    
    /**
     * create a new Message based on the input
     * this is for a from msg
     * @param state
     * @param isResponse
     */     
    public static Message createBroadcastMessage(ReplicationState state, boolean isResponse) {
        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceName = lookup.getServerName();
        return ReplicationState.createBroadcastMessage(state, isResponse, instanceName);
    }
    
    /**
     * create a new Message based on the input
     * this is for a return msg to the target instance
     * @param state
     * @param isResponse
     * @param instName
     */     
    public static Message createBroadcastMessage(ReplicationState state, boolean isResponse, String instName) {
        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceName = lookup.getServerName();
        //String instanceName = instName;
        Message msg = new Message();
        String mode = state.getMode();
        String id = (String)state.getId();
        String appid = state.getAppId();
        String versionString = Long.toString(state.getVersion());
        Long lastAccess = new Long(state.getLastAccess());
        Long maxInactive = new Long(state.getMaxInactiveInterval());        
        String extraParam = state.getExtraParam();
        String theCommand = MESSAGE_BROADCAST_QUERY;
        byte[] data = state.getState();
        byte[] trunkData = state.getTrunkState();
        if(isResponse) {
            theCommand = RETURN_BROADCAST_MSG_COMMAND;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>createBroadcastMessage:instanceName=" + instanceName + " theCommand=" + theCommand);
        }         
        //System.out.println("ReplicationState>>createBroadcastMessage:instanceName=" + instanceName + " theCommand=" + theCommand);
        msg.addMessageElement(MESSAGE_MODE,
                              new StringMessageElement(MESSAGE_MODE,
                                                       mode,
                                                       null));
        msg.addMessageElement(MESSAGE_ID,
                              new StringMessageElement(MESSAGE_ID,
                                                       id,
                                                       null));        
        msg.addMessageElement(MESSAGE_APPID,
                              new StringMessageElement(MESSAGE_APPID,
                                                       appid,
                                                       null)); 
        msg.addMessageElement(MESSAGE_VERSION,
                              new StringMessageElement(MESSAGE_VERSION,
                                                       versionString,
                                                       null));
        if(extraParam != null) {
            msg.addMessageElement(MESSAGE_EXTRA_PARAM,
                              new StringMessageElement(MESSAGE_EXTRA_PARAM,
                                                       extraParam,
                                                       null));
        }        
        msg.addMessageElement(MESSAGE_COMMAND,
                              new StringMessageElement(MESSAGE_COMMAND,
                                                       theCommand,
                                                       null));
        msg.addMessageElement(MESSAGE_LAST_ACCESS,
                              new StringMessageElement(MESSAGE_LAST_ACCESS,
                                                       lastAccess.toString(),
                                                       null)); 
        msg.addMessageElement(MESSAGE_MAX_INACTIVE,
                              new StringMessageElement(MESSAGE_MAX_INACTIVE,
                                                       maxInactive.toString(),
                                                       null));        
        msg.addMessageElement(InstanceNameMessage,
                              new StringMessageElement(InstanceNameMessage,
                                                       instanceName,
                                                       null)); 
        msg.addMessageElement(MESSAGE_INSTANCE_NAME,
                              new StringMessageElement(MESSAGE_INSTANCE_NAME,
                                                       instanceName,
                                                       null));
        if(data != null) {
            msg.addMessageElement(MESSAGE_DATA,
                              new ByteArrayMessageElement(MESSAGE_DATA,
                                                       null,
                                                       data,
                                                       null));
        } 
        if(trunkData != null) {
            msg.addMessageElement(MESSAGE_TRUNK_DATA,
                              new ByteArrayMessageElement(MESSAGE_TRUNK_DATA,
                                                       null,
                                                       trunkData,
                                                       null));
        }         
        return msg;
    }
    
    // begin Metadata related  

    /**
     * create a new ReplicationState based on the input
     * @param mode
     * @param id
     * @param appid
     * @param metaData can be SimpleMetadata
     */     
    public static ReplicationState createReplicationState(String mode, String id, String appid, Metadata metadata) {
        ReplicationState state = null;
        state = createReplicationStateFromSimpleMetadata(mode, id, appid, (SimpleMetadata)metadata);
        return state;
    }

    /**
     * create a new ReplicationState based on the input
     * @param mode
     * @param id
     * @param appid
     * @param simpleMetadata 
     */     
    public static ReplicationState createReplicationStateFromSimpleMetadata(String mode, String id, String appid, SimpleMetadata simpleMetadata) 
        {

        ReplicationState state = 
            new ReplicationState(mode,   //mode 
                id,                          //id 
                appid,                       //appid
                simpleMetadata.getVersion(), //version 
                simpleMetadata.getLastAccessTime(), //lastAccess 
                simpleMetadata.getMaxInactiveInterval(), //maxInactive, 
                null,                           //extraParam 
                null,                           //queryResult
                null,                           //instanceName
                null,                           //command FIXME 
                simpleMetadata.getState(),      //data
                null);                          //trunkState
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("ReplicationState>>createReplicationStateFromSimpleMetadata:state = " + state);
        }         
        //System.out.println("ReplicationState>>createReplicationStateFromSimpleMetadata:state = " + state);
        return state;
    } 
    
    /**
    * create a CompositeMetadata representing ReplicationState state
    *
    * @param state
    *   The ReplicationState
    *
    * @return
    *   A newly created CompositeMetadata object for the given state
    */ 
    public static CompositeMetadata createCompositeMetadataFrom(ReplicationState state) {
        Collection entries = deserializeStatesCollection(state.getState());
        CompositeMetadata result
            = new CompositeMetadata(
                state.getVersion(),  //version
                state.getLastAccess(), //lastAccess
                state.getMaxInactiveInterval(), //maxInactive
                entries,                        //entries
                state.getTrunkState(),          //trunkState
                state.getExtraParam());     //extraParam      
        return result;
    } 
    
    private static Collection deserializeStatesCollectionPrevious(byte[] entriesState) {
        Collection result = new ArrayList();
        try {
            result = (Collection)getObjectValue(entriesState);
        } catch (ClassNotFoundException ex1) {
        } catch (IOException ex2) {}        
        return result;
    }
    
    
    private static Collection deserializeStatesCollection(byte[] entriesState) {
        Collection result = new ArrayList();
        try {
            result = (Collection)getAttributeValueCollection(entriesState);
        } catch (ClassNotFoundException ex1) {
        } catch (IOException ex2) {}        
        return result;
    }    
    
    /**
    * Given a byte[] containing session data, return a session
    * object
    *
    * @param state
    *   The byte[] with the session attribute data
    *
    * @return
    *   A newly created object for the given session attribute data
    */
    public static Object getObjectValue(byte[] state) 
        throws IOException, ClassNotFoundException 
    {
        Object objectValue = null;
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            ois = new ObjectInputStream(bis); 
            
            if(ois != null) {
                try {
                    objectValue = ois.readObject();
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
            System.err.println("getAttributeValue :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getAttributeValue IOException :"+e.getMessage());
            throw e;
        }      

        return objectValue;
    }
        
    protected static Object getAttributeValueCollection(byte[] state)
        throws IOException, ClassNotFoundException
    {
        Collection attributeValueList = new ArrayList();
        BufferedInputStream bis = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
            
        try
        {
            bais = new ByteArrayInputStream(state);
            bis = new BufferedInputStream(bais);
            ois = new ObjectInputStream(bis); 
            
            if(ois != null) {
                try {                    
                    //first get List size
                    Object whatIsIt = ois.readObject();
                    //System.out.println("first obj: " + whatIsIt);
                    int entriesSize = 0;
                    if(whatIsIt instanceof Integer) {
                        entriesSize = ((Integer)whatIsIt).intValue();
                    } else {
                        System.out.println("first obj not integer:dumping stack:");
                        Thread.dumpStack();
                    }
                    //int entriesSize = ((Integer) ois.readObject()).intValue();
                    //System.out.println("entriesSize = " + entriesSize);
                    //attributeValueList = new ArrayList(entriesSize);
                    for (int i = 0; i < entriesSize; i++) {
                        Object nextAttributeValue = ois.readObject();
                        attributeValueList.add(nextAttributeValue);
                    }                    
                    
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
            System.err.println("getAttributeValue :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getAttributeValue IOException :"+e.getMessage());
            throw e;
        }      

        return attributeValueList;        
    }    
    
    /**
    * Create an byte[] for the object that we can then pass to
    * the ReplicationState.
    *
    * @param obj
    *   The attribute value we are serializing
    *
    */
    protected static byte[] getByteArray(Object obj)
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
    
    protected static byte[] getByteArrayFromCollection(Collection entries)
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
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,"size of attributes byte array: " + obs.length);
        }
        return obs;
    } 
    
    // end Metadata related

    /**
     * is this state expired
     */    
    public boolean isExpired() {
        //-1 means no expiration
        if(this.getMaxInactiveInterval() == -1) {
            return false;
        }
        return ( (System.currentTimeMillis() - this.getLastAccess()) 
            > (this.getMaxInactiveInterval() * 1000) );
    }

    /**
     * is this message a return message from normal message
     * true if command is RETURN_MSG_COMMAND
     */    
    public boolean isReturnMessage() {
        String theCommand = this.getCommand();
        if(theCommand == null) {
            return false;
        } else {
            return theCommand.equals(RETURN_MSG_COMMAND);
        }
    }
    
    /**
     * is this message a return message from normal message
     * true if command is HC_COMMAND
     */    
    public boolean isHCMessage() {
        String theCommand = this.getCommand();
        if(theCommand == null) {
            return false;
        } else {
            return theCommand.equals(HC_COMMAND);
        }
    }             
    
    /**
     * is this message a return message from normal message
     * true if command is RETURN_HC_MSG_COMMAND
     */    
    public boolean isHCReturnMessage() {
        String theCommand = this.getCommand();
        if(theCommand == null) {
            return false;
        } else {
            return theCommand.equals(RETURN_HC_MSG_COMMAND);
        }
    }    
    
    /**
     * is this message a return message from a broadcast
     * true if command is RETURN_BROADCAST_MSG_COMMAND
     */
    public boolean isReturnFromBroadcastMessage() {
        String theCommand = this.getCommand();
        if(theCommand == null) {
            return false;
        } else {
            return theCommand.equals(RETURN_BROADCAST_MSG_COMMAND);
        }
    }
    
    /**
     * @return mode (e.g. MODE_WEB, MODE_EJB, MODE_SSO)
     */    
    public String getMode() {
        return _mode;
    }    
    
    /**
     * @return _id (key)
     */    
    public Object getId() {
        return _id;
    }
    
    /**
     * 
     * @return _appId
     */    
    public String getAppId() {
        return _appId;
    }
    
    /**
     * @return version
     */    
    public long getVersion() {
        return _version;
    }
    
    /**
     * @param version the version
     */    
    public void setVersion(long version) {
        _version = version;
    }     
    
    /**
     * @return last access time
     */    
    public long getLastAccess() {
        return _lastAccess;
    }    
    
    /**
     * @param lastAccess last access time
     */    
    public void setLastAccess(long lastAccess) {
        _lastAccess = lastAccess;
    }    
    
    /**
     * @return max inactive interval (seconds)
     */    
    public long getMaxInactiveInterval() {
        return _maxInactiveInterval;
    }    
    
    /**
     * @return _extraParam
     */    
    public String getExtraParam() {
        return _extraParam;
    }
    
    /**
     * @param extraParam extraParam
     */    
    public void setExtraParam(String extraParam) {
        _extraParam = extraParam;
    } 
    
    /**
     * @return _queryResult
     */    
    public Object getQueryResult() {
        return _queryResult;
    }
    
    /**
     * @param queryResult queryResult
     */    
    public void setQueryResult(Object queryResult) {
        _queryResult = queryResult;
    }    
    
    /**
     * @return _instanceName
     */    
    public String getInstanceName() {
        return _instanceName;
    }    
    
    /**
     * @return _command
     */    
    public String getCommand() {
        return _command;
    }    

    /**
     * @return state
     */    
    public byte[] getState() {
        return _state;
    } 
    
    /**
     * @return trunkState
     */    
    public byte[] getTrunkState() {
        return _trunkState;
    }
    
     public int hashCode() {
         return _hc;
     }
     
     public boolean equals(Object obj) {
         boolean result = false;
         if (obj instanceof ReplicationState) {
             ReplicationState other = (ReplicationState) obj;
             if ((_id.equals(other._id) && (_appId.equals(other._appId)))) {
                 result = true;
             }
         }
         
         return result;
     }    
    
     public boolean isAckRequired() {
         return _ackRequired;
     }
 
     public void setAckRequired(boolean ackRequired) {
         this._ackRequired = ackRequired;
     }
         
     public long getSendStartTime() {
         return _sendStartTime;
     }
     
     public void setSendStartTime(long value) {
         _sendStartTime = value;
     }     
     
     public boolean isSent() {
         return _sentFlag.get();
     }
     
     public void setAckIdsList(List list) {
         _ackIdsList = list;
     }
     
     public List getAckIdsList() {
         return _ackIdsList;
     }
     
     public void setSent(boolean value) {
         _sentFlag.set(value);
     }     
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("_mode=" + _mode + "\n");
        sb.append("_id=" + _id + "\n");
        sb.append("_appId=" + _appId + "\n");
        sb.append("_version=" + _version + "\n");
        sb.append("_command=" + _command + "\n");
        sb.append("_lastAccess=" + _lastAccess + "\n");
        sb.append("_maxInactiveInterval=" + _maxInactiveInterval + "\n");
        sb.append("_extraParam=" + _extraParam + "\n");
        sb.append("_queryResult=" + _queryResult + "\n");
        sb.append("_instanceName=" + _instanceName + "\n");
        sb.append("isExpired=" + this.isExpired() + "\n");
        sb.append("_ackRequired=" + this.isAckRequired() + "\n");
        return sb.toString();
    }
    
    private List _ackIdsList = null;
    private String _mode = null;
    private Object _id = null;
    private String _appId = null;   
    private long _lastAccess = 0L;
    private long _maxInactiveInterval = 0L; //seconds
    private long _version = -1L;
    private String _extraParam = null;
    private Object _queryResult = null;
    private String _instanceName = null;
    private String _command = null;
    private byte[] _state = null;
    private byte[] _trunkState = null;
    private boolean _ackRequired = false;
    private int _hc;
    private volatile AtomicBoolean _sentFlag = new AtomicBoolean(false);
    private long _sendStartTime = -1L;
}
