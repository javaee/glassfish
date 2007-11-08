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
package com.sun.enterprise.admin.wsmgmt.agent;

import com.sun.enterprise.webservice.monitoring.GlobalMessageListener;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.webservice.monitoring.TransportInfo;
import com.sun.xml.rpc.spi.runtime.*;
import com.sun.enterprise.deployment.WebServiceEndpoint;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Application;

import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterRegistry;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterRouter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterContext;
import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;

import java.util.HashMap;

import com.sun.enterprise.admin.wsmgmt.stats.spi.StatsProviderManager;
import com.sun.enterprise.admin.wsmgmt.stats.impl.WebServiceEndpointStatsProviderImpl;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;
import com.sun.enterprise.admin.monitor.callflow.Agent;
import com.sun.enterprise.admin.monitor.callflow.ThreadLocalData;
import com.sun.enterprise.Switch;


/**
 * This singleton class receives all the callbacks for web service endpoints 
 */
public class GlobalMessageListenerImpl implements GlobalMessageListener {

    /**
     * Callback when a web service response has finished being processed by the
     * container and was sent back to the client 
     *
     * @param messageID returned by the preProcessRequest call
     */
    public void postProcessResponse(String messageID, TransportInfo info) {
        FilterContext  fc = (FilterContext) msgId2fc.get(messageID);
        long startTime = fc.getExecutionTime();
        fc.setExecutionTime(System.currentTimeMillis() - startTime);
        FilterRouter.getInstance().applyFilters(Filter.POST_PROCESS_RESPONSE, 
                fc);
        // Web service execution is ended, remove this message id information
        msgId2fc.remove(messageID);
    }

    /**
     * Callback when a web service request entered the web service container and
     * before any system processing is done.
     *
     * @param endpoint is the endpoint the web service request is targeted to 
     */
    public String preProcessRequest(Endpoint endpoint) {

        String ep = null;
        WebServiceEndpoint wse = endpoint.getDescriptor();
        
        if ( wse != null) {
            ep = wse.getEndpointName();
        }
        //wse.resolveComponentLink();

        BundleDescriptor bundle = wse.getBundleDescriptor();
        Application app = bundle.getApplication();

        String fqn =  
        WebServiceMgrBackEnd.getManager().getFullyQualifiedName(
           app.getRegistrationName() ,
           bundle.getModuleDescriptor().getArchiveUri(),
           bundle.getModuleDescriptor().isStandalone(), ep); 

        if (FilterRegistry.getInstance().isManaged(fqn) == false) {
            return null;
        }
        String mId = null;
        String cfId = null;
        boolean isCallFlowEnabled = false;

        // call flow request id
        Agent agent = Switch.getSwitch().getCallFlowAgent();
        if (agent != null) {
            if (agent.isEnabled()) {
                ThreadLocalData data = agent.getThreadLocalData();
                if (data != null) {
                    cfId = data.getRequestId();
                }
            }
        }

        if ( (cfId == null) || ("".equals(cfId)) ) {

            // call flow id is not available; use own id
            int newNumber = newSequenceNumber();
            mId = Integer.valueOf(newNumber).toString();

        } else {
            // use id from call flow if it is not null
            mId = cfId;
            isCallFlowEnabled = true;
        }
        FilterContext fc = new FilterContext(endpoint, isCallFlowEnabled, 
            (TransportInfo)null, 
            (com.sun.enterprise.admin.wsmgmt.SOAPMessageContext)null, mId, fqn);

        FilterRouter.getInstance().applyFilters(Filter.PRE_PROCESS_REQUEST,fc);

        // update the endpoint 2 filter context mapping
        msgId2fc.put(mId, fc);
        
        return mId;
    }

    /**
     * Callback when a 1.X web service request is about the be delivered to the Web
     * Service Implementation Bean. 
     *
     * @param messageID - returned by preProcessRequest call
     * @param context - the jaxrpc message trace, transport dependent.
     */
    public void processRequest(String messageID, com.sun.xml.rpc.spi.runtime.SOAPMessageContext context, 
        TransportInfo info) {
        com.sun.enterprise.admin.wsmgmt.SOAPMessageContext smc = 
            new com.sun.enterprise.admin.wsmgmt.SOAPMessageContext_1_0(context);

         FilterContext fc = (FilterContext) msgId2fc.get(messageID);
        fc.setTransportInfo(info); 
        fc.setMessageContext(smc); 
        FilterRouter.getInstance().applyFilters(Filter.PROCESS_REQUEST,fc);
    }

    /**
     * Callback when a 1.X web service response was returned by the Web Service
     * Implementation Bean 
     *
     * @param messageID - returned by preProcessRequest call
     * @param context - the jaxrpc message trace, transport dependent.
     */
    public void processResponse(String messageID, com.sun.xml.rpc.spi.runtime.SOAPMessageContext  context) {
        String ep = null;
        FilterContext fc = (FilterContext) msgId2fc.get(messageID);
        FilterRouter.getInstance().applyFilters(Filter.PROCESS_RESPONSE,fc);
    }

    /**
     * Callback when a 2.X web service request is about the be delivered to the Web
     * Service Implementation Bean. 
     *
     * @param messageID - returned by preProcessRequest call
     * @param context - the jaxrpc message trace, transport dependent.
     */
    public void processRequest(String messageID, com.sun.enterprise.webservice.SOAPMessageContext  context, 
        TransportInfo info) {
        com.sun.enterprise.admin.wsmgmt.SOAPMessageContext smc = 
            new com.sun.enterprise.admin.wsmgmt.SOAPMessageContext_2_0(context);

         FilterContext fc = (FilterContext) msgId2fc.get(messageID);
        fc.setTransportInfo(info); 
        fc.setMessageContext(smc); 
        FilterRouter.getInstance().applyFilters(Filter.PROCESS_REQUEST,fc);
    }

    /**
     * Callback when a 2.X web service response was returned by the Web Service
     * Implementation Bean 
     *
     * @param messageID - returned by preProcessRequest call
     * @param context - the jaxrpc message trace, transport dependent.
     */
    public void processResponse(String messageID, com.sun.enterprise.webservice.SOAPMessageContext  context) {
        String ep = null;
        FilterContext fc = (FilterContext) msgId2fc.get(messageID);
        FilterRouter.getInstance().applyFilters(Filter.PROCESS_RESPONSE,fc);
    }
    
    // XXX The following ID generation needs to be integrated with call flow
    // ID generation

    public static synchronized int newSequenceNumber() {
        sequenceNumber++;
        return sequenceNumber;
    }

    public static synchronized int getSequenceNumber() {
        return sequenceNumber;
    }

    public static int sequenceNumber = 0;

    // XXX need to optimized this lookup.

    static HashMap  msgId2fc = new HashMap();
}
