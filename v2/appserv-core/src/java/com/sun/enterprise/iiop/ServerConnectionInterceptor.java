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

package com.sun.enterprise.iiop;

import java.net.Socket;
import org.omg.PortableInterceptor.ServerRequestInfo;
import com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt;
import com.sun.corba.ee.spi.legacy.connection.Connection;
import com.sun.enterprise.iiop.security.SecurityMechanismSelector;
import com.sun.enterprise.iiop.security.ServerConnectionContext;

import com.sun.enterprise.J2EETransactionManager;
import com.sun.enterprise.Switch;
import com.sun.enterprise.distributedtx.J2EETransactionManagerOpt;

import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.RequestType;
import com.sun.enterprise.admin.monitor.callflow.RequestInfo;
import com.sun.enterprise.admin.monitor.callflow.ContainerTypeOrApplicationType;
import com.sun.enterprise.util.ORBManager;
import java.util.logging.*;
import com.sun.logging.*;


public class ServerConnectionInterceptor extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ServerRequestInterceptor, Comparable
{
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.CORBA_LOGGER);
        }
    public static final String baseMsg = "ServerConnectionInterceptor";
    public int order;

    /**
     * Construct the interceptor.
     * @param the order in which the interceptor should run.
     */
    public ServerConnectionInterceptor(int order) {
	this.order = order;
    }

    public String name() { return baseMsg; }

    public void receive_request_service_contexts(ServerRequestInfo sri)
    {
        Socket s = null;
        Connection c = ((RequestInfoExt)sri).connection();
        SecurityMechanismSelector sms = new SecurityMechanismSelector();
        ServerConnectionContext scc = null;
        if (c != null) {
            s = c.getSocket();
            if(_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,"RECEIVED request on connection: " + c);
                _logger.log(Level.FINE,"Socket =" + s);
            }
            scc = new ServerConnectionContext(s);
        } else {
            scc = new ServerConnectionContext();
        }
        sms.setServerConnectionContext(scc);
    }

    public int compareTo(Object o)
    {
	int otherOrder = -1;
	if( o instanceof ServerConnectionInterceptor) {
            otherOrder = ((ServerConnectionInterceptor)o).order;
	}
        if (order < otherOrder) {
            return -1;
        } else if (order == otherOrder) {
            return 0;
        }
        return 1;
    }

    public void destroy() {}

    public void receive_request(ServerRequestInfo sri)    
    {
        Socket s = null;
        Agent callFlowAgent = Switch.getSwitch().getCallFlowAgent();
        // callFlowAgent should never be null.
	// If the else block is executed, its a bug.
	// more investigation needed
	if (callFlowAgent != null) {
	    boolean callFlowEnabled = callFlowAgent.isEnabled();
	    if (callFlowEnabled){
	        // Only do callflow RequestStart,
	        // If it is a ejb call and not a is_a call. For everything else
	        // do a startTime for OTHER Container
	        if (isEjbCall(sri)){
		    try {
		        try{
			    Connection c = ((RequestInfoExt)sri).connection();
			    if (c != null) {
			        s = c.getSocket();
			    }
			} finally {
			    String callerIPAddress = null;
			    if (s != null) {
                                callerIPAddress = s.getInetAddress().getHostAddress();
			    }
			    callFlowAgent.requestStart(RequestType.REMOTE_EJB);
			    callFlowAgent.addRequestInfo(
							 RequestInfo.CALLER_IP_ADDRESS, callerIPAddress);
			}
		    } catch (Exception ex){
		        _logger.log( Level.WARNING,
				     "Callflow Agent's requestStart exception" + ex);
		    }
		} else {
		    try {
		        callFlowAgent.startTime(ContainerTypeOrApplicationType.ORB_CONTAINER);
		    } catch (Exception ex){
		        _logger.log( Level.WARNING,
				     "Callflow Agent's starttime exception" + ex);
		    }
		}
	    }
	} else {
	     _logger.log( Level.FINE, "CallFlow Agent not initialized. ");
	}	
    }
    public void send_reply(ServerRequestInfo sri)
    {
        try {
            checkTransaction(sri);
        } finally {
            if (isEjbCall(sri)) {
                Switch.getSwitch().getTransactionManager().cleanTxnTimeout();
            }
            Agent callFlowAgent = Switch.getSwitch().getCallFlowAgent();
	    // callFlowAgent should never be null.
	    // If the else block is executed, its a bug.
	    // more investigation needed
            if (callFlowAgent != null) {
	        boolean callFlowEnabled = callFlowAgent.isEnabled();
		if(callFlowEnabled){
		    if (isEjbCall(sri)){
		        try {
			    callFlowAgent.requestEnd();
			} catch (Exception ex) {
			    _logger.log(
				      Level.WARNING,
				      "Callflow Agent's requestEnd method exception" + ex);
			}
		    } else {
		        try {
			    callFlowAgent.endTime();
			} catch (Exception ex) {
			    _logger.log(
					Level.WARNING,
					"Callflow Agent's endtime method exception" + ex);
			}
		    }
		}	    
	    } else {
	        _logger.log( Level.FINE, "CallFlow Agent not initialized. ");
	    }
	}
    }

    public void send_exception(ServerRequestInfo sri)
    {
        try {
            checkTransaction(sri);
        } finally {
            if (isEjbCall(sri)) {
                Switch.getSwitch().getTransactionManager().cleanTxnTimeout();
            }
            Agent callFlowAgent = Switch.getSwitch().getCallFlowAgent();
	    // callFlowAgent should never be null.
	    // If the else block is executed, its a bug.
	    // more investigation needed
             if (callFlowAgent != null) {	       
	         boolean callFlowEnabled = callFlowAgent.isEnabled();
		 if(callFlowEnabled){
		     if (isEjbCall(sri)){
		         try {
			     callFlowAgent.requestEnd();
			 } catch (Exception ex) {
			     _logger.log(
				       Level.WARNING,
				       "Callflow Agent's requestEnd method exception" + ex);
			 }
		     } else {
		         try {
			     callFlowAgent.endTime();
			 } catch (Exception ex) {
			     _logger.log(
					 Level.WARNING,
					 "Callflow Agent's endtime method exception" + ex);
			 }
		     }
		 }
	     } else {
	         _logger.log( Level.FINE, "CallFlow Agent not initialized. ");
	     }	
	}
    }

    public void send_other(ServerRequestInfo sri)
    {
        try {
            checkTransaction(sri);
        } finally {
            if (isEjbCall(sri)) {
                Switch.getSwitch().getTransactionManager().cleanTxnTimeout();
            }
            Agent callFlowAgent = Switch.getSwitch().getCallFlowAgent();
	    // callFlowAgent should never be null.
	    // If the else block is executed, its a bug.
	    // more investigation needed
	    if (callFlowAgent != null) {
	        boolean callFlowEnabled = callFlowAgent.isEnabled();
		if(callFlowEnabled){
		    if (isEjbCall(sri)) {
		        try {
			    callFlowAgent.requestEnd();
			} catch (Exception ex) {
			    _logger.log(
					Level.WARNING,
					"Callflow Agent's requestEnd method exception" + ex);
			}
		    } else {
		        try {
			    callFlowAgent.endTime();
			} catch (Exception ex) {
			    _logger.log(
					Level.WARNING,
					"Callflow Agent's endtime method exception" + ex);
			}
		    }
		}
            } else {
	        _logger.log( Level.FINE, "CallFlow Agent not initialized. ");
	    }	    
        }
    }

    private void checkTransaction(ServerRequestInfo sri) {
	/**
	ObjectImpl target = (ObjectImpl)sri.effective_target();
        if ( !target._is_local() ) {
	    J2EETransactionManager tm = 
				    Switch.getSwitch().getTransactionManager();
	    if ( tm != null )
		tm.checkTransactionExport();
	}
	**/
	J2EETransactionManager tm = Switch.getSwitch().getTransactionManager();
	if ( tm != null )
	    tm.checkTransactionImport();
    }
    /**
     * Returns true, if the incoming call is a EJB method call. 
     * This checks for is_a calls and ignores those calls. In callflow analysis
     * when a component looks up another component, this lookup should be 
     * considered part of the same call coming in. 
     * Since a lookup triggers the iiop codebase, it will fire a new request start.
     * With this check, we consider the calls that are only new incoming ejb
     * method calls as new request starts.
     */
    private boolean isEjbCall (ServerRequestInfo sri) {
        if (ORBManager.isEjbAdapterName(sri.adapter_name()) &&
                (!ORBManager.isIsACall(sri.operation()))) {
            return true;
        } else 
            return false;
    }
}
