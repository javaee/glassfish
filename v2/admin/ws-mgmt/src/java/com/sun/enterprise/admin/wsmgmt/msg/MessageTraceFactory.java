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
package com.sun.enterprise.admin.wsmgmt.msg;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.Principal;
import java.io.ByteArrayOutputStream;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPFault;
import com.sun.enterprise.security.SecurityContext;
import com.sun.appserv.management.ext.wsmgmt.MessageTrace;
import com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterContext;
import com.sun.enterprise.webservice.monitoring.TransportInfo;
import com.sun.enterprise.webservice.monitoring.TransportType;

import javax.xml.soap.SOAPException;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.wsmgmt.SOAPMessageContext;
import org.apache.coyote.tomcat5.CoyoteRequestFacade;
//import com.sun.xml.rpc.server.http.HttpServletRequest;

/**
 * Factory class to that generates a message trace object from the SOAP
 * invocation events.
 */
class MessageTraceFactory {

    /**
     * Returns the singleton instance.
     *
     * @return  singleton instance of this class
     */
    static MessageTraceFactory getInstance() {
        return _instance;
    }

    /**
     * Constructor.
     */
    private MessageTraceFactory() {
        _messages = new HashMap();
        _msgBodyPattern = Pattern.compile("<env:Body>.*</env:Body>");
    }

    /**
     * Processes the SOAP request.
     * 
     * @param  ctx  filter context
     */
    void processRequest(FilterContext ctx, String appId) {

        // message id
        String messageId = ctx.getMessageId();
        com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace = new
        com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl(messageId,
        MessageTrace.CLASS_NAME); 

        // sets the time stamp
        trace.setTimeStamp( System.currentTimeMillis() );

        // set application id, FQ endpoint name
        trace.setEndpointName( ctx.getFullyQualifiedName() );
        trace.setApplicationID(appId);
        trace.setCallFlowEnabled(ctx.isCallFlowEnabled());

        // set request content, size and http header 
        setRequestInfo(ctx.getMessageContext(), trace);

        // set transport type
        setTransportType(ctx, trace);

        // sets client host
        setClientHost(ctx.getMessageContext(), trace);

        _messages.put(messageId, trace);
    }

    /**
     * Sets client host.
     */
    private void setClientHost(SOAPMessageContext smc, 
            com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace) {

        if (smc!= null) {
            Iterator iter = smc.getPropertyNames();
            if ( iter == null) {
                return;
            }
             CoyoteRequestFacade httpReq =
                  (CoyoteRequestFacade) smc.getProperty(HTTP_REQUEST);
             String clientHost = null;
             if (httpReq != null) {
                 clientHost = httpReq.getRemoteAddr();
                  trace.setClientHost(clientHost);
            }
        }
    }

    /**
     * Sets SOAP message content, request size and HTTP headers.
     */
    private void setRequestInfo(SOAPMessageContext ctx, 
            com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace) {

        ByteArrayOutputStream baos = null;
        try {
            // sets the SOAP message header 
            trace.setHTTPRequestHeaders(ctx.getHTTPRequestHeaders());
            baos = getMsgAsBytes(ctx);

            // sets the request size
            trace.setRequestSize( baos.size() );
            String soapMsg = baos.toString();
            trace.setRequestContent(soapMsg);

        } catch (Exception e) {
            String msg =_stringMgr.getString(
                "MessageTraceFactory_NoMessageTrace", e.getMessage());
            _logger.log(Level.INFO, msg);
        }finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) { 
                _logger.log(Level.FINE, "Web Service message stream could not be closed : " + e.getMessage());
            }
        }
    }

    /** 
     * Sets the transport type.
     */
    private void setTransportType(FilterContext ctx, 
            com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace) {

        TransportInfo ti = ctx.getTransportInfo();
        if (ti != null) {
            TransportType type = ti.getTransportType();
            trace.setTransportType( type.name() );
        }
    }

    /**
     * Processes the SOAP response.
     *
     * @param  ctx  filter context
     */
    void processResponse(FilterContext ctx) {

        String messageId = ctx.getMessageId();

        com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace = 
            (com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl) 
            _messages.get(messageId);

        SOAPMessageContext mctx = ctx.getMessageContext();

        // sets response content, size and message header
        setResponseInfo(mctx, trace);

        // sets caller principal
        setCallerPrincipal(trace);

        // sets fault code, fault string and fault actor
        setFaultInfo(mctx, trace);

    }

    /**
     * Post Processing of the SOAP response.
     *
     * @param  ctx  filter context
     */
    MessageTrace postProcessResponse(FilterContext ctx) {

        String messageId = ctx.getMessageId();

        com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace = 
            (com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl) 
            _messages.remove(messageId);

        // sets response time
        setResponseTime(trace, ctx.getExecutionTime());

        return trace;
    }

    /**
     * Sets faults information for a response.
     */
    private void setFaultInfo(SOAPMessageContext smc,  
            com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace) {

        if (smc != null) {
            SOAPMessage sm = smc.getMessage();
            try {
                if (sm != null) {
                    SOAPBody sb = sm.getSOAPBody();
                    if (sb != null) {
                        SOAPFault fault = sb.getFault();

                        if ( fault != null) {
                            trace.setFaultCode( fault.getFaultCode() );
                            trace.setFaultString( fault.getFaultString() );
                            trace.setFaultActor( fault.getFaultString() );
                        }
                    }
                }
            } catch (SOAPException se) {
                String msg = "Error while reading SOAP fault information";
                _logger.log(Level.FINE, msg, se);
            }
        }
    }

    /**
     * Sets caller principal information.
     */
    private void setCallerPrincipal(
            com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace) {

        SecurityContext sc = SecurityContext.getCurrent();
        if (sc != null) {
            Principal p = sc.getCallerPrincipal();
            if (p != null) {
                trace.setPrincipalName( p.getName() );
                _logger.fine("[MTF] Caller Principal: " + p.getName());
            }
        }
    }

    /**
     * Sets response time for this request.
     */
    private void setResponseTime( 
        com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace, long t) {

        trace.setResponseTime(t);
    }

    /**
     * Returns the SOAP message as an output stream.
     */
    private ByteArrayOutputStream getMsgAsBytes(SOAPMessageContext smc) {

        ByteArrayOutputStream baos = null;

        if (smc!= null) {
            baos = new ByteArrayOutputStream();
            try {
                smc.getMessage().writeTo(baos);
            } catch (Exception e) {
                String msg = "Error while retrieving SOAP message";
                _logger.log(Level.FINE, msg, e);
            }
        }

        return baos;
    }

    /**
     * Sets the response content, size and message header
     */
    private void setResponseInfo(SOAPMessageContext ctx, 
            com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl trace) {

        ByteArrayOutputStream baos = null;
        try {
            baos = getMsgAsBytes(ctx);

            if (baos == null) {
                return;
            }

            // sets the request size
            trace.setResponseSize( baos.size() );
            String soapMsg = baos.toString();
            trace.setResponseContent(soapMsg);

            // sets the SOAP message header 
            trace.setHTTPResponseHeaders(ctx.getHTTPResponseHeaders());
        } catch (Exception e) {
            String msg =_stringMgr.getString(
                "MessageTraceFactory_NoMessageTrace", e.getMessage());
            _logger.log(Level.INFO, msg);
        }finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                _logger.log(Level.FINE, "Web Service message stream could not be closed : " + e.getMessage());
            }
        }
    }

    // ---- PRIVATE - VARIABLES --------------------
    private Map _messages                        = null;
    private static final MessageTraceFactory _instance = 
                                            new MessageTraceFactory();
    private Pattern _msgBodyPattern              = null;
    private static final String HTTP_REQUEST =
                "com.sun.xml.rpc.server.http.HttpServletRequest";
    private static final Logger _logger = 
        Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr = 
        StringManager.getManager(MessageTraceFactory.class);
}
