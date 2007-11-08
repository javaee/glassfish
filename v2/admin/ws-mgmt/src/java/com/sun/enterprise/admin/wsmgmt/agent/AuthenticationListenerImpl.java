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
 * AuthenticationListenerImpl.java
 */

package com.sun.enterprise.admin.wsmgmt.agent;

import java.security.Principal;
import com.sun.enterprise.webservice.monitoring.AuthenticationListener;
import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.admin.wsmgmt.WebServiceMgrBackEnd;
import com.sun.enterprise.admin.wsmgmt.stats.spi.StatsProviderManager;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigProvider;
import com.sun.enterprise.admin.wsmgmt.config.spi.ConfigFactory;
import com.sun.enterprise.admin.wsmgmt.config.spi.WebServiceConfig;
import com.sun.enterprise.admin.wsmgmt.stats.impl.WebServiceEndpointStatsProviderImpl;

import com.sun.enterprise.admin.wsmgmt.filter.spi.Filter;
import com.sun.enterprise.admin.wsmgmt.filter.spi.FilterRegistry;
import com.sun.enterprise.admin.wsmgmt.msg.MessageFilter;
import com.sun.enterprise.admin.wsmgmt.agent.GlobalMessageListenerImpl;
import com.sun.appserv.management.ext.wsmgmt.MessageTraceImpl;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.Iterator;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This interface allows to register interest in authentication events 
 * in the web service container. 
 *
 * @author Satish Viswanatham 
 */
public class AuthenticationListenerImpl implements AuthenticationListener {
    
    public AuthenticationListenerImpl() {
    }


    /**
     * notification that a user properly authenticated while making 
     * a web service invocation.
     */
    public void authSucess(BundleDescriptor desc, Endpoint ep, Principal principal) {
        // all the data is collected by Message listener for the success case
    }
    
    /**
     * notification that a user authentication attempt has failed.
     * @param endpointSelector the endpoint selector 
     * @param principal Optional principal that failed
     */
    public void authFailure(BundleDescriptor desc, Endpoint ep, Principal principal) {
        if (ep == null) {
            _logger.fine("Endpoint is null for " + desc.getModuleID());
            return;
        }
        // get the endpoint's fully qualified name
        String fqn =WebServiceMgrBackEnd.getManager().getFullyQualifiedName(ep);
        if (fqn == null) {
            _logger.fine("Fully Qualified could not be computed for the selector " +
            ep.getEndpointSelector());
            return;
        }

        WebServiceEndpointStatsProviderImpl impl = (
             WebServiceEndpointStatsProviderImpl) StatsProviderManager.
             getInstance().getEndpointStatsProvider(fqn);
        
        // set auth failure time stamp
        if (impl != null) {
            impl.setAuthFailure(System.currentTimeMillis());
        }
        try {
            ConfigProvider cfgProv = ConfigFactory.getConfigFactory().getConfigProvider();
            if (cfgProv != null) {
                WebServiceConfig wsc = cfgProv.getWebServiceConfig(fqn);
                if ((wsc == null) || (wsc.getMonitoringLevel() == null) 
                    || (wsc.getMonitoringLevel().equals("OFF"))) {
                // in this case, there wont be any stats 
                    _logger.fine("Monitoring is OFF for webservice endpoint " +
                        fqn);
                    return;
                }
            }
        // get its corresponding stats provider
            if (impl == null) {
                if (cfgProv != null) {
                    String msg = _stringMgr.getString("Auth.StatsNotReg", fqn);
                    throw new RuntimeException(msg);
                }
                return;
            }
        } catch (Exception e) {
            _logger.fine("Config provider could not be initialized " +
                e.getMessage());
        }
        

        /** enable the following code, once bug# 6418025 is fixed.
         ** Need a way to HTTP headers and client host information.
         ** SOAPMessageContext needed to be passed to this method.
         **
        // create empty message trace for this element
        List l =
        FilterRegistry.getInstance().getFilters(Filter.POST_PROCESS_RESPONSE,
                        fqn); 
        Iterator itr = l.iterator();
        while (itr.hasNext()) {
            Object o = itr.next();
            if (o instanceof MessageFilter) {
                MessageTraceImpl m = new MessageTraceImpl(
                   new Integer(GlobalMessageListenerImpl.newSequenceNumber())
                   .toString());
                m.setRequestSize(0);
                m.setResponseSize(0);
                m.setRequestContent(null);
                m.setResponseContent(null);
                //m.setTransportType();
                //m.setHTTPRequestHeaders();
                //m.setHTTPResponseHeaders();
                //m.setClientHost();
                // Setting principal name to principal.getName() causing issues
                m.setPrincipalName(null);
                m.setResponseTime(0);
                m.setFaultCode("999");
                m.setFaultString(null);
                m.setFaultActor(null);
                m.setTimeStamp(System.currentTimeMillis());
                //m.setCallFlowEnabled(false);
                m.setEndpointName(fqn);
                m.setApplicationID(null);

                ((MessageFilter)o)._handler.addMessage(m);
                return;
            }
        }
        */
    }

    /** PRIVATE VARIABLES */
    private static final Logger _logger =
          Logger.getLogger(LogDomains.ADMIN_LOGGER);
    private static final StringManager _stringMgr =
            StringManager.getManager(AuthenticationListenerImpl.class);
}
