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
 *InputPipeWrapper.java
 *
 * Created on March 22, 2006, 12:02 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;

/**
 *
 * @author Larry White
 */
public class InputPipeWrapper extends AbstractPipeWrapper implements PipeMsgListener {
    
    private final static String InstanceNameMessage 
        = ReplicationState.InstanceNameMessage;
    private final static String ReadyMessage 
        = ReplicationState.ReadyMessage;    
    private final static String MESSAGE_MODE =
        ReplicationState.MESSAGE_MODE;     
    private final static String MESSAGE_ID
        = ReplicationState.MESSAGE_ID;    
    private final static String MESSAGE_COMMAND
        = ReplicationState.MESSAGE_COMMAND;
    private final static String MESSAGE_BROADCAST_QUERY 
        = ReplicationState.MESSAGE_BROADCAST_QUERY; 
    private final static String RETURN_BROADCAST_MSG_COMMAND
        = ReplicationState.RETURN_BROADCAST_MSG_COMMAND;    
    private final static String MESSAGE_READY
        = ReplicationState.MESSAGE_READY;  
    
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
            
    /**
     * Creates a new instance of InputPipeWrapper
     */
    public InputPipeWrapper() {
    }
    
    /** Creates a new instance of InputPipeWrapper */
    public InputPipeWrapper(String name, InputPipe pipe) {
        _name = name;
        _pipe = pipe;
    }  
    
    /**
     *  This is the PipeListener interface. Expect a call to this method
     *  When a message is received.
     *  when we get a message, print out the message on the console
     *  all incoming messages must have an InstanceNameMessage element
     *@param  event  message event
     */
    public void pipeMsgEvent(PipeMsgEvent event) {

        Message msg = null;
        try {
            // grab the message from the event
            msg = event.getMessage();
            //displayKeyMessageElements(msg);
           
            if (msg == null) {
                 if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Received an empty message, returning");
                 }                
                return;
            }

            // get the message element named InstanceNameMessage

            MessageElement instanceNameMsgElement = 
                msg.getMessageElement(InstanceNameMessage, InstanceNameMessage);
            //ignore broadcasts from yourself
            String returnInstance = null;
            if(instanceNameMsgElement != null) {
                returnInstance = instanceNameMsgElement.toString();
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("InputPipeWrapper:incoming propagated msg from: " + returnInstance);
                }                
                //System.out.println("InputPipeWrapper:incoming propagated msg from: " + returnInstance);
            }
            if(returnInstance.equalsIgnoreCase(getInstanceName())) {
                return;
            }
            
            MessageElement readyMsgElement = msg.getMessageElement(ReadyMessage, ReadyMessage);
            if(readyMsgElement != null) {
                System.out.println("readyMsgElement=" + readyMsgElement.toString() + " from: " + returnInstance);
                //this following code was part of "ready after receiving join logic"
                //commenting out for now
                //System.out.println("about to do join check for " + returnInstance);                
                JoinNotificationEventHandler.checkAndDoJoinFor(returnInstance); 
            }            
                   
            MessageElement idMsgElement = msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
            if(idMsgElement != null) {
                //System.out.println("idMsgElement=" + idMsgElement.toString());
            }
            MessageElement commandMsgElement = 
                msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);             
            if(commandMsgElement != null) {
                String theCommand = commandMsgElement.toString();
                if(theCommand.equals(MESSAGE_BROADCAST_QUERY) || theCommand.equals(RETURN_BROADCAST_MSG_COMMAND)) {
                    processQueryMessage(msg, idMsgElement, returnInstance);
                }                
            }            

        } catch (Exception e) {
            if (_logger.isLoggable(Level.FINEST)) {
                e.printStackTrace();
            }             
            return;
        }
    }
    
    private void displayKeyMessageElements(Message msg) {
        System.out.println("PropagatedPipeWrapper>>pipeMsgEvent:msg=" + msg);
        MessageElement instanceNameMsgElement = 
            msg.getMessageElement(InstanceNameMessage, InstanceNameMessage);
        String returnInstance = null;
        if(instanceNameMsgElement != null) {
            returnInstance = instanceNameMsgElement.toString();
        System.out.println("PropagatedPipeWrapper>>pipeMsgEvent:fromInstance=" + returnInstance);
            if(!isMessageFromYourself(returnInstance)) {
                System.out.println("incoming propagated msg from: " + returnInstance);
            }
        }
        
        MessageElement idMsgElement = msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
        if(idMsgElement != null) {
            System.out.println("idMsgElement=" + idMsgElement.toString());
        }
        
        MessageElement commandMsgElement = 
            msg.getMessageElement(MESSAGE_COMMAND, MESSAGE_COMMAND);             
        if(commandMsgElement != null) {
            String theCommand = commandMsgElement.toString();
            System.out.println("incoming msg to broadcast pipe:command=" + theCommand);               
        }         
    }
    
    private void processQueryMessage(Message msg, MessageElement idMsgElement, String returnInstance) {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("InputPipeWrapper>>processQueryMessage");
        }        
        //System.out.println("InputPipeWrapper>>processQueryMessage");
        if (idMsgElement.toString() == null) {
            System.out.println("null id msg received");
            return;
        }
        ReplicationState state = this.createReplicationState(msg);
        
        ReplicationMessageRouter receiver = 
            ReplicationMessageRouter.createInstance();        
        receiver.processQueryMessage(state, returnInstance);        
       
    }   
          
    private ReplicationState createReplicationState(Message msg) {
        return ReplicationState.createBroadcastReplicationState(msg); 
    }    
    
    public void cleanup() {

        System.out.println("InputPipeWrapper>>cleanup called");
        if(_pipe != null) {
            _pipe.close();
            System.out.println("InputPipeWrapper>>_pipe.close() called");
        }
        _pipe = null;
        _name = null;
    }
    
    void setPipe(InputPipe pipe) {
        _pipe = pipe;
    }
    
    private InputPipe _pipe = null;
    private String _name = null;
    
}
