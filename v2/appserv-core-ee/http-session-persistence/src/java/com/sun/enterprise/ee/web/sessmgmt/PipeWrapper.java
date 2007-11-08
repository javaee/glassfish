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
 * PipeWrapper.java
 * generic class that wraps a pipe
 *
 * Created on March 6, 2006, 4:45 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.OutgoingMessageEvent;
import net.jxta.endpoint.OutgoingMessageEventListener;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.util.PipeEventListener;

import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author lwhite
 */
public class PipeWrapper extends AbstractPipeWrapper implements PipeMsgListener, PipeEventListener, OutgoingMessageEventListener, PipePoolElement {
    
    private final static String SenderMessage = "pipe_tutorial";
    private final static String MESSAGE_MODE =
        ReplicationState.MESSAGE_MODE; 
    private final static String BULK_MESSAGE_MODE =
        ReplicationState.BULK_MESSAGE_MODE;    
    private final static String MESSAGE_ID =
        ReplicationState.MESSAGE_ID;
    
    final static String MESSAGE_ACK_REQUIRED = 
        ReplicationState.MESSAGE_ACK_REQUIRED;
    final static String MESSAGE_ACK_LIST_PROPERTY = 
        ReplicationState.MESSAGE_ACK_LIST_PROPERTY;
    public final static String MESSAGE_SEND_START_TIME =
        ReplicationState.MESSAGE_SEND_START_TIME;
    public final static String BULK_MESSAGE_ID =
        ReplicationState.BULK_MESSAGE_ID;
    private final static String ID = ReplicationState.ID;
    private final static int PIPE_QUEUE_CAPACITY = 20;
    private final static Level TRACE_LEVEL = Level.FINE;
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private final static Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);    
    
    /** Creates a new instance of PipeWrapper */
    public PipeWrapper(String name, String senderOrReceiver, 
            String partnerInstanceName, JxtaBiDiPipe pipe) {
        _name = name;
        _senderOrReceiver = senderOrReceiver;
        _partnerInstanceName = partnerInstanceName;
        _pipe = pipe;
        //pipe.setMessageListener(this);
    }
    
    /**
     *  This is the PipeListener interface. Expect a call to this method
     *  When a message is received.
     *  when we get a message, print out the message on the console
     *
     *@param  event  message event
     */
    public void pipeMsgEvent(PipeMsgEvent event) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("PipeWrapper>>pipeMsgEvent");
        }        
        //System.out.println("PipeWrapper>>pipeMsgEvent");
        Message msg = null;
        try {
            // grab the message from the event
            msg = event.getMessage();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("PipeWrapper>>pipeMsgEvent:msg=" + msg);
            }             
            //System.out.println("PipeWrapper>>pipeMsgEvent:msg=" + msg);            
            if (msg == null) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Received an empty message, returning");
                }                
                return;
            }

            // get the message element named SenderMessage
            MessageElement msgElement = msg.getMessageElement(SenderMessage, SenderMessage);
            MessageElement idMsgElement = msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
            MessageElement bulkMsgElement = msg.getMessageElement(BULK_MESSAGE_MODE, BULK_MESSAGE_MODE);
            if(msgElement != null) {
                //System.out.println("msgElement=" + msgElement.toString());
            } 
            if(idMsgElement != null) {
                //System.out.println("idMsgElement=" + idMsgElement.toString());
            } 
            if(bulkMsgElement != null) {
                //System.out.println("bulkMsgElement=" + bulkMsgElement.toString());
            }             

            // Get message
            
            if (msgElement != null) {
                this.processStartupMessage(msg, msgElement);
            } else if (bulkMsgElement != null) {
                processBulkMessage(msg, bulkMsgElement);
            } else {
                if (idMsgElement != null) {
                    this.processIdMessage(msg, idMsgElement); 
                } else {
                    //this shouldn't happen
                }
            }            
        } catch (Exception e) {
            e.printStackTrace();
            /* FIXME
            if (LOG.isEnabledFor(Level.DEBUG)) {
                LOG.debug(e);
            }
             */
            return;
        }
    }    
    
    private void processStartupMessage(Message msg, MessageElement msgElement) {
        // Get message
        if (msgElement.toString() == null) {
            System.out.println("null msg received");
        } else {
            //Date date = new Date(System.currentTimeMillis());
            System.out.println("Message  :"+ msgElement.toString());
            //put comment start back here
            //send back response if it isn't already a response
            if( !(msgElement.toString()).startsWith("RETURN_MSG_COMMAND")) {
                Message returnMsg = new Message();
                String returnData = "ReturnMessage  :"+ msgElement.toString();
                returnMsg.addMessageElement(SenderMessage,
                                      new StringMessageElement(SenderMessage,
                                                               returnData,
                                                               null));
                System.out.println("SendingResponse :" + returnData);

                //pipe.sendMessage(returnMsg);
                /* don't send return message during startup - 
                 *pools not initialized yet
                JxtaReplicationSender jxtaReplicationSender =
                    JxtaReplicationSender.createInstance();
                jxtaReplicationSender.sendOverPipe(returnMsg);
                 */
                
            }

            //end send back response
             //put comment end back here
            //count ++;
        }
    }

    private void checkSendImmediateAck(Message msg) {
        //do not proceed unless waitForFastAck not configured or health
        //check message which must always be acked
        /*
        System.out.println("<<checkSendImmediateAck:isWaitForFastAckConfigured=" + isWaitForFastAckConfigured());
        System.out.println("<<checkSendImmediateAck:isHCMessage=" + ReplicationState.isHCMessage(msg));
        System.out.println("<<checkSendImmediateAck:isVoidReturnMessage=" + ReplicationState.isVoidMethodReturnMessage(msg));
        System.out.println("<<checkSendImmediateAck:isAckRequiredForMessage=" + ReplicationState.isAckRequiredForMessage(msg));
         */        
        if(isWaitForFastAckConfigured() && !ReplicationState.isHCMessage(msg)) {
            return;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("<<checkSendImmediateAck:isVoidReturnMessage=" + ReplicationState.isVoidMethodReturnMessage(msg));
            _logger.fine("<<checkSendImmediateAck:isAckRequiredForMessage=" + ReplicationState.isAckRequiredForMessage(msg));
        }        
        //System.out.println("<<checkSendImmediateAck:isVoidReturnMessage=" + ReplicationState.isVoidMethodReturnMessage(msg));
        //System.out.println("<<checkSendImmediateAck:isAckRequiredForMessage=" + ReplicationState.isAckRequiredForMessage(msg));
        if (ReplicationState.isVoidMethodReturnMessage(msg)
            && ReplicationState.isAckRequiredForMessage(msg)) {
            //send immediate ack
            Message ackMsg = ReplicationState.createAckMessageFrom(msg);
            JxtaBiDiPipe thePipe = this.getPipe();
            //long tempStart = System.currentTimeMillis();
            //FIXME - we are trying this w/o synchronization
            //synchronized(thePipe) {
                try {
                    thePipe.sendMessage(ackMsg);
                } catch (IOException ex) {
                    if(_logger.isLoggable(Level.FINE)) {
                        _logger.fine("PipeWrapper:IOException sending ack message");
                        ex.printStackTrace();
                    }                    
                    //System.out.println("PipeWrapper:IOException sending ack message");
                    //ex.printStackTrace();
                }
            //}
            /*
            long duration = System.currentTimeMillis() - tempStart;
            if(duration > 30) {
                System.out.println("sending ack took " + duration + " msecs");
            }
             */
        }
    }
        
    private void checkSendImmediateBulkAck(Message msg, List<ReplicationState> states) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("<<checkSendImmediateBulkAck:isVoidReturnMessage=" + ReplicationState.isVoidMethodReturnMessage(msg));
            _logger.fine("<<checkSendImmediateBulkAck:isAckRequiredForMessage=" + ReplicationState.isAckRequiredForMessage(msg));
        }        
        //System.out.println("<<checkSendImmediateBulkAck:isVoidReturnMessage=" + ReplicationState.isVoidMethodReturnMessage(msg));
        //System.out.println("<<checkSendImmediateBulkAck:isAckRequiredForMessage=" + ReplicationState.isAckRequiredForMessage(msg));
        if (ReplicationState.isVoidMethodReturnMessage(msg)
            && ReplicationState.isAckRequiredForMessage(msg)) {
            //FIXME send immediate ack
            Message ackMsg = ReplicationState.createBulkAckMessageFrom(msg, states);
            JxtaBiDiPipe thePipe = this.getPipe();
            //long tempStart = System.currentTimeMillis();
            //FIXME - we are trying this w/o synchronization
            //synchronized(thePipe) {
                try {
                    thePipe.sendMessage(ackMsg);
                } catch (IOException ex) {
                    if(_logger.isLoggable(Level.FINE)) {
                        _logger.fine("PipeWrapper:IOException sending ack message");
                        ex.printStackTrace();
                    }                    
                    //System.out.println("PipeWrapper:IOException sending ack message");
                    //ex.printStackTrace();
                }
            //}
            /*
            long duration = System.currentTimeMillis() - tempStart;
            if(duration > 30) {
                System.out.println("sending ack took " + duration + " msecs");
            }
             */
        }
    }            
    
    private void processBulkMessage(Message msg, MessageElement idMsgElement) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, ">>PipeWrapper:  processBulkMessage...");
        }
        //_logger.log(Level.INFO, ">>PipeWrapper:  processBulkMessage...");
        printMessageReceiptStats(msg);        
        ReplicationMessageRouter receiver = 
            ReplicationMessageRouter.createInstance();
        //check if this is a return bulk message and act accordingly
        //System.out.println("<<processBulkMessage:isVoidReturnMessage=" + ReplicationState.isVoidMethodReturnMessage(msg));
        //System.out.println("<<processBulkMessage:isAckRequiredForMessage=" + ReplicationState.isAckRequiredForMessage(msg));
        //System.out.println("<<processBulkMessage:isResponseMessage=" + ReplicationState.isResponseMessage(msg));
        if(!ReplicationState.isAckRequiredForMessage(msg)
            && !ReplicationState.isVoidMethodReturnMessage(msg)
            && ReplicationState.isResponseMessage(msg)) {
            List<String> ackIds = ReplicationState.extractAckIdsListFromMessage(msg);
            //iterate and deliver acks to each id in ackIds list and return
            processAcks(ackIds, receiver);
            return;
        }
        //send ack if required       
        List<ReplicationState> states = ReplicationState.extractBulkReplicationStatesFromMessage(msg); 
        if(!isWaitForFastAckConfigured()) {
            this.checkSendImmediateBulkAck(msg, states);
        }
        //_logger.log(Level.INFO, ">>PipeWrapper:  states size = " + states.size());         
        for (ReplicationState state : states) {
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "<<PipeWrapper:  receiving id: " + state.getId() + "[ver:" + state.getVersion() + "]");
            }
            receiver.processMessage(state);             
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "<<PipeWrapper:  processBulkMessage complete...");
        }
    }
    
    private void processAcks(List<String> acksList, ReplicationMessageRouter receiver) {
        //_logger.log(Level.INFO, ">>PipeWrapper:processAcks:  acksList size = " + acksList.size());
        for(int i=0; i<acksList.size(); i++) {
            //System.out.println("processAcks:nextIdToAck:" + acksList.get(i));
            ReplicationState nextState = new ReplicationState(acksList.get(i));
            receiver.processResponse(nextState); 
        }
    }
    
    private void processIdMessage(Message msg, MessageElement idMsgElement) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("PipeWrapper>>processIdMessage");
        }         
        //System.out.println("PipeWrapper>>processIdMessage");
        printIdMessageReceiptStats(msg);
        if (idMsgElement.toString() == null) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("PipeWrapper>>processIdMessage:null id msg received");
            }            
            //System.out.println("PipeWrapper>>processIdMessage:null id msg received");
            return;
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("PipeWrapper:ID Message id:"+ idMsgElement.toString());
        }        
        //System.out.println("PipeWrapper:ID Message id:"+ idMsgElement.toString());
        this.checkSendImmediateAck(msg);
        ReplicationState state = this.createReplicationState(msg);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("PipeWrapper>>processIdMessage:incomingState=" + state);
        }        
        //System.out.println("PipeWrapper>>processIdMessage:incomingState=" + state);
        ReplicationMessageRouter receiver = 
            ReplicationMessageRouter.createInstance();        
        receiver.processMessage(state);        
        
    }
    
    public boolean isWaitForFastAckConfigured() {
        if(_waitForFastAckConfigured == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            boolean waitForFastAckProp = lookup.getWaitForFastAckPropertyFromConfig();
            _waitForFastAckConfigured = new Boolean(waitForFastAckProp);
            //System.out.println("isWaitForFastAckConfigured = " + _waitForFastAckConfigured.booleanValue());
        }
        return _waitForFastAckConfigured.booleanValue();
    }    
    
    /**
     * is wait_for_fast_ack_property = "true"
     */ 
    private Boolean _waitForFastAckConfigured = null;    
    
    private ReplicationState createReplicationState(Message msg) {
        return ReplicationState.createReplicationState(msg); 
    }
    
    public void cleanup() {
        this.cleanup(true);
    }
    
    public synchronized void cleanup(boolean doClose) {
        if(this.isPipeClosed()) {
            return;
        }
        //disable listener - we do not want this event
        //since we are closing
        _pipe.setPipeEventListener(null);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("PipeWrapper>>cleanup called");
        }         
        //System.out.println("PipeWrapper>>cleanup called");
        if(doClose) {
            try {
                _pipe.close();
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("PipeWrapper>>_pipe.close() called");
                }                
                //System.out.println("PipeWrapper>>_pipe.close() called");
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("PipeWrapper:exception closing pipe during cleanup");
                    ex.printStackTrace();
                }                
                //System.out.println("PipeWrapper:exception closing pipe during cleanup");
                //ex.printStackTrace();
            }
        }
        setPipeClosed(true);
        _pipe = null;
        _name = null;
    }    
    
    public void pipeEvent(int event) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("PipeWrapper:in pipeEvent: event=" + event);
        }        
        //System.out.println("PipeWrapper:in pipeEvent: event=" + event);
        if(event == JxtaBiDiPipe.PIPE_CLOSED_EVENT) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("handling pipe event pipe closed:is this end closed:" + isPipeClosed());            
                _logger.fine("handling pipe event pipe closed:pipeWrapper:" + this);                
            }             
            //System.out.println("handling pipe event pipe closed:is this end closed:" + isPipeClosed());            
            //System.out.println("handling pipe event pipe closed:pipeWrapper:" + this);
            //you close your side's pipe too if not already closed
            if(!isPipeClosed()) {
                cleanup();
            }  
            //if not deliberately stopping then health is bad and
            //we must reinitialize pipes
            if(!ReplicationHealthChecker.isStopping()) {
                String partnerInstanceName = this.getPartnerInstanceName();
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("PipeWrapper close event causing pipes reInit:partnerInstance: " + partnerInstanceName);
                }
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "PipeWrapper close event causing pipes reInit:partnerInstance: " + partnerInstanceName); 
                }
                JxtaReplicationReceiver jxtaReplicationReceiver
                    = (JxtaReplicationReceiver) ReplicationHealthChecker.getReplicationReceiver();
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("PipeWrapper:about to call reInit: jxtaReplicationReceiver: " + jxtaReplicationReceiver + "_senderOrReceiver=" + _senderOrReceiver);
                }
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "PipeWrapper:about to call reInit: jxtaReplicationReceiver: " + jxtaReplicationReceiver + "_senderOrReceiver=" + _senderOrReceiver);
                }
                jxtaReplicationReceiver.reInit(getSenderOrReceiver(), partnerInstanceName);
            }          
        }
    }
    
    JxtaBiDiPipe getPipe() {
        return _pipe;
    }
    
    public synchronized boolean isPipeClosed() {
        return _pipeClosed;
    }    
    
    private synchronized void setPipeClosed(boolean value) {
        _pipeClosed = value;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append(super.toString());
        sb.append("isPipeClosed=" + isPipeClosed() + "\n");
        sb.append("_pipe=" + _pipe + "\n");
        return sb.toString();       
    }
    
    //begin mimic OutgoingMessageEventListener code
    //but without event just use the message directly
    
   /**
    * The message identified by the event was successfully sent. Successfully
    * sending a message is not a guarantee that it will be successfully
    * received by the destination.
    * @param sentMessage the sent message directly
    */
    public void messageSendSucceeded(Message sentMessage) {

        //System.out.println("messageSendSucceeded");
        decrementQueuedMessageCount();
        //Message sentMessage = (Message)event.getSource();
        String ackRequiredString = (String)sentMessage.getMessageProperty(MESSAGE_ACK_REQUIRED);        
        printFastAckStats(sentMessage, true);            
        if(ackRequiredString.equalsIgnoreCase("Y")) {
            //System.out.println("doing fast ack...");
            //for bulk case
            MessageElement bulkMsgElement = sentMessage.getMessageElement(BULK_MESSAGE_MODE, BULK_MESSAGE_MODE);
            if(bulkMsgElement != null) {
                doFastBulkAcksFor(sentMessage, bulkMsgElement);
            } else {
                //for non bulk case
                MessageElement idMsgElement = sentMessage.getMessageElement(MESSAGE_ID, MESSAGE_ID);
                if(idMsgElement != null) {
                    doFastAckFor(sentMessage, idMsgElement);
                }
            }
        }

    } 
    
   /**
    * The message identified by the event could not be sent.
    * <p/>
    * The cause of the failure, if any, is available from
    * {@link OutgoingMessageEvent#getFailure()}. Failures while sending
    * messages have several causes :
    * <p/>
    * <ul>
    * <li>An {@link java.io.IOException} means that the messenger cannot
    * send the message and the messenger will accept no further messages
    * to be sent.</li>
    * <p/>
    * <li>A {@link java.lang.RuntimeException} mean that the message was
    * not sent, but retries may or may not be possible based upon the
    * status returned by {@link Messenger#isClosed()}.</li>
    * <p/>
    * <li><code>null</code> means that the message was not sent, but may
    * be retried. Usually the failure is due to local resource limits
    * being exceeded. Attempts may be made to later resend the message,
    * usually after waiting for congestion to clear.</li>
    * </ul>
    * @param sentMessage the message that was not able to be sent
    */
    public void messageSendFailed(Message sentMessage) {
        printFastAckStats(sentMessage, false);
        decrementQueuedMessageCount();
    }    
    
    //begin OutgoingMessageEventListener code
    
   /**
    * The message identified by the event could not be sent.
    * <p/>
    * The cause of the failure, if any, is available from
    * {@link OutgoingMessageEvent#getFailure()}. Failures while sending
    * messages have several causes :
    * <p/>
    * <ul>
    * <li>An {@link java.io.IOException} means that the messenger cannot
    * send the message and the messenger will accept no further messages
    * to be sent.</li>
    * <p/>
    * <li>A {@link java.lang.RuntimeException} mean that the message was
    * not sent, but retries may or may not be possible based upon the
    * status returned by {@link Messenger#isClosed()}.</li>
    * <p/>
    * <li><code>null</code> means that the message was not sent, but may
    * be retried. Usually the failure is due to local resource limits
    * being exceeded. Attempts may be made to later resend the message,
    * usually after waiting for congestion to clear.</li>
    * </ul>
    * @param event the event
    */
    public void messageSendFailed(OutgoingMessageEvent event) {
        Message sentMessage = (Message)event.getSource();
        printFastAckStats(sentMessage, false);
        decrementQueuedMessageCount();
    }

   /**
    * The message identified by the event was successfully sent. Successfully
    * sending a message is not a guarantee that it will be successfully
    * received by the destination.
    * @param event the event
    */
    public void messageSendSucceeded(OutgoingMessageEvent event) {

        //System.out.println("messageSendSucceeded");
        decrementQueuedMessageCount();
        Message sentMessage = (Message)event.getSource();
        String ackRequiredString = (String)sentMessage.getMessageProperty(MESSAGE_ACK_REQUIRED);        
        printFastAckStats(sentMessage, true);            
        if(ackRequiredString.equalsIgnoreCase("Y")) {
            //System.out.println("doing fast ack...");
            //for bulk case
            MessageElement bulkMsgElement = sentMessage.getMessageElement(BULK_MESSAGE_MODE, BULK_MESSAGE_MODE);
            if(bulkMsgElement != null) {
                doFastBulkAcksFor(sentMessage, bulkMsgElement);
            } else {
                //for non bulk case
                MessageElement idMsgElement = sentMessage.getMessageElement(MESSAGE_ID, MESSAGE_ID);
                if(idMsgElement != null) {
                    doFastAckFor(sentMessage, idMsgElement);
                }
            }
        }

    }
    
    private void doFastBulkAcksFor(Message sentMessage, MessageElement bulkMsgElement) {
        //System.out.println("doFastBulkAcksFor...");
        if(ReplicationState.isHCMessage(sentMessage)) {
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "HC message do not do fast ack for this...");
            }
            return;
        }
        //fast ack the bulk message itself
        processResponse(sentMessage);
        ReplicationMessageRouter receiver = 
            ReplicationMessageRouter.createInstance();
        List ackIdsList = (List)sentMessage.getMessageProperty(MESSAGE_ACK_LIST_PROPERTY);
        //System.out.println("doFastBulkAcksFor:ackIdsList size = " + ackIdsList.size());
        //iterate and deliver acks to each id in ackIds list and return
        processAcks(ackIdsList, receiver);        
    }
    
    private void doFastAckFor(Message sentMessage, MessageElement bulkMsgElement) {
        //System.out.println("doFastAckFor...");
        if(ReplicationState.isHCMessage(sentMessage)) {
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "HC message do not do fast ack for this..."); 
            }
            return;
        }
        processResponse(sentMessage);        
    }
    
    //utility methods for measurements
    
    private void printFastAckStats(Message sentMessage, boolean succeeded) {
        if(!this.getReplicationMeasurementEnabled()) {
            return;
        }
        boolean useLong = true;
        int measurementInterval = this.getReplicationMeasurementInterval();
        long id = -1L;
        String stringId = "";
        MessageElement idMsgElement = 
            sentMessage.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        if(idMsgElement != null) {
            try {
                id = (Long.decode(idMsgElement.toString())).longValue();
            } catch (NumberFormatException ex) {
                useLong = false;
                stringId = idMsgElement.toString();
            }            
            //System.out.println("messageSendSucceeded: bulkId = " + id);
        }
        if(useLong) {
            stringId = ""+id;
        }        
        if(useLong && (id == -1 || id % measurementInterval != 0)) {
            return;
        }        
        String sendStartTimeString = (String)sentMessage.getMessageProperty(MESSAGE_SEND_START_TIME);       
        long sendStartTime = 
            (Long.decode(sendStartTimeString)).longValue();
        String prefix = "messageSendSucceeded: id = ";
        if(!succeeded) {
            prefix = "messageSendFailed: id = ";
        }
        _logger.log(Level.INFO, prefix + stringId + " fastAckTime = " 
            + (System.currentTimeMillis() - sendStartTime) + " to partner: " + getPartnerInstanceName());        
    } 

    private void printMessageReceiptStats(Message receivedMessage) {
        if(!this.getReplicationMeasurementEnabled()) {
            return;
        }
        int measurementInterval = this.getReplicationMeasurementInterval();
        long id = -1L;
        MessageElement idMsgElement = 
            receivedMessage.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        if(idMsgElement != null) { 
            id = (Long.decode(idMsgElement.toString())).longValue();
            //System.out.println("messageReceived: bulkId = " + id);
        } 
        if(id % measurementInterval != 0) {
            return;
        }
        //get send start time for measurements
        long sendStartTime = -1L;
        MessageElement sendStartMsgElement = 
            receivedMessage.getMessageElement(MESSAGE_SEND_START_TIME, MESSAGE_SEND_START_TIME);
        if(sendStartMsgElement != null) {
            sendStartTime = 
                (Long.decode(sendStartMsgElement.toString())).longValue();
            if(sendStartTime > 0L) {
                //System.out.println("message receipt time: " + (System.currentTimeMillis() - sendStartTime));
            }
        }
        _logger.log(Level.INFO, "messageReceiptSucceeded: bulkId = " + id + " receiptTime = " 
            + (System.currentTimeMillis() - sendStartTime) + " from partner: " + getPartnerInstanceName());       
    }
    
    private void printIdMessageReceiptStats(Message receivedMessage) {
        if(!this.getReplicationMeasurementEnabled()) {
            return;
        }
        String id = "";
        MessageElement idMsgElement = 
            receivedMessage.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        if(idMsgElement != null) { 
            id = idMsgElement.toString();
            //System.out.println("messageReceived: id = " + id);
        } 
        //get send start time for measurements
        long sendStartTime = -1L;
        MessageElement sendStartMsgElement = 
            receivedMessage.getMessageElement(MESSAGE_SEND_START_TIME, MESSAGE_SEND_START_TIME);
        if(sendStartMsgElement != null) {
            sendStartTime = 
                (Long.decode(sendStartMsgElement.toString())).longValue();
            if(sendStartTime > 0L) {
                //System.out.println("message receipt time: " + (System.currentTimeMillis() - sendStartTime));
            }
        }
        _logger.log(Level.INFO, "messageReceiptSucceeded: id = " + id + " receiptTime = " 
            + (System.currentTimeMillis() - sendStartTime));         
    }    
    
    Boolean _replicationMeasurementEnabled = null;
    private boolean getReplicationMeasurementEnabled() {
        if(_replicationMeasurementEnabled == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            _replicationMeasurementEnabled 
                = new Boolean(lookup.getReplicationMeasurementEnabledFromConfig());                        
        }
        return _replicationMeasurementEnabled.booleanValue();
    }

    int _replicationMeasurementInterval = -1;
    private int getReplicationMeasurementInterval() {
        if(_replicationMeasurementInterval == -1) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            _replicationMeasurementInterval 
                = lookup.getReplicationMeasurementIntervalFromConfig();                        
        }
        return _replicationMeasurementInterval;
    }    
    
    //end utility methods for diagnostics         
       
    public void processResponse(Message sentMessage) {
        ReplicationState sentState = createReplicationState(sentMessage);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("IN" + this.getClass().getName() + ">>processResponse");            
        }        
        ReplicationResponseRepository.putEntry(sentState);
    }   
   
   //end OutgoingMessageEventListener code
    
    public boolean isPipeOverStressed() {
        return (_locallyQueuedMessages.get() > (PIPE_QUEUE_CAPACITY - 2));
    }
    
    int incrementQueuedMessageCount() {
        return _locallyQueuedMessages.incrementAndGet();
    }    
    
    int decrementQueuedMessageCount() {
        return _locallyQueuedMessages.decrementAndGet();
    }
    
    private JxtaBiDiPipe _pipe = null;
    private String _name = null;
    private boolean _pipeClosed = false;
    private final AtomicInteger _locallyQueuedMessages = new AtomicInteger(0);
    
}
