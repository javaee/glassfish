/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.webservices.monitoring;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.handler.MessageContext;

import org.glassfish.webservices.SOAPMessageContext;

import java.util.logging.Logger;
import java.util.ResourceBundle;

import com.sun.logging.LogDomains;


public class JAXWSEndpointImpl extends EndpointImpl {

    protected Logger logger = LogDomains.getLogger(this.getClass(),LogDomains.WEBSERVICES_LOGGER);

    private ResourceBundle rb = logger.getResourceBundle()   ;
        
    JAXWSEndpointImpl(String endpointSelector, EndpointType type) {
        super(endpointSelector, type);
    }
    
    public boolean processRequest(SOAPMessageContext messageContext)
                    throws Exception {

	boolean status = true;

        // let's get our thread local context
        WebServiceEngineImpl wsEngine = WebServiceEngineImpl.getInstance();
        try {            
            if (!listeners.isEmpty() || wsEngine.hasGlobalMessageListener()) {

                String messageID = (String) messageContext.get(EndpointImpl.MESSAGE_ID);
                
                // someone is listening ?
                if (messageID!=null) {
                    HttpServletRequest httpReq = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
                    HttpRequestInfoImpl info = new HttpRequestInfoImpl(httpReq);
                    wsEngine.processRequest(messageID, messageContext, info);
                } 
                
                // any local listeners ?
                if (!listeners.isEmpty()) {
                    // create the message trace and save it to our message context
                    MessageTraceImpl request = new MessageTraceImpl();
                    request.setEndpoint(this);
                    request.setMessageContext(messageContext);       
                    HttpServletRequest httpReq = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
                    request.setTransportInfo(new HttpRequestInfoImpl(httpReq));
                    messageContext.put(EndpointImpl.REQUEST_TRACE, request);                    
                }
            }
        } catch(Exception e) {
            wsEngine.sLogger.warning(rb.getString("exception.tracing.request") + e.getMessage());
	    throw e;
        }
        return status;
    }

    public void processResponse(SOAPMessageContext messageContext) throws Exception {   
        
        // let's get our thread local context
        WebServiceEngineImpl wsEngine = WebServiceEngineImpl.getInstance();
        try {
            
            if (wsEngine.hasGlobalMessageListener() || !listeners.isEmpty()) {
                
                String messageID = (String) messageContext.get(EndpointImpl.MESSAGE_ID);
                // do we have a global listener ?
                if (messageID!=null) {
                    wsEngine.processResponse(messageID,  messageContext);
                }
                
                // local listeners
                if (!listeners.isEmpty()) {
                    MessageTraceImpl response = new MessageTraceImpl();
                    response.setEndpoint(this);
                    response.setMessageContext(messageContext);
                    //TODO BM check regarding this method
                    for (org.glassfish.webservices.monitoring.MessageListener listener : listeners) {
                        listener.invocationProcessed((MessageTrace) messageContext.get(REQUEST_TRACE), response);
                    }
                }
            }
        } catch(Exception e) {
            wsEngine.sLogger.warning(rb.getString("exception.tracing.response") + e.getMessage());
	    throw e;
        } 
    }
   
}
