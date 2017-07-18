/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
 * EndpointMonitorRegistry.java
 *
 * Created on March 16, 2005, 9:46 AM
 */

package com.sun.enterprise.tools.wsmonitoring;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.sun.enterprise.webservice.monitoring.WebServiceEngineFactory;
import com.sun.enterprise.webservice.monitoring.WebServiceEngine;
import com.sun.enterprise.webservice.monitoring.EndpointLifecycleListener;
import com.sun.enterprise.webservice.monitoring.Endpoint;
import com.sun.enterprise.deployment.WebServiceEndpoint;

/**
 *
 * @author dochez
 */
public class EndpointMonitorRegistry implements EndpointLifecycleListener {
    
    static EndpointMonitorRegistry instance;
    Map<String, EndpointMonitor> monitoredEndpoints;
    
    
    /** Creates a new instance of EndpointMonitorRegistry */
    EndpointMonitorRegistry() {
    }
    
    public static EndpointMonitorRegistry getInstance() {
        if (instance==null) {
            instance = new EndpointMonitorRegistry();
            instance.init();
        }
        return instance;
    }
    
    private void init() {
        monitoredEndpoints = new HashMap<String, EndpointMonitor>();
        WebServiceEngine engine = WebServiceEngineFactory.getInstance().getEngine();
        Iterator<Endpoint> createdEndpoints = engine.getEndpoints();
        while (createdEndpoints.hasNext()) {
            Endpoint endpoint = createdEndpoints.next();
            endpointAdded(endpoint);
        }
        engine.addLifecycleListener(this);
    }
    
    /**
     * Notification of a new Web Service endpoint installation in the 
     * appserver. 
     * @param endpoint endpoint to register SOAPMessageListener if needed.
     */
    public void endpointAdded(Endpoint endpoint) {

        EndpointMonitor monitor = new EndpointMonitor(endpoint);
        endpoint.addListener(monitor);
        monitoredEndpoints.put(endpoint.getEndpointSelector(), monitor);        
    }
    
    /**
     * Notification of a Web Service endpoint removal from the appserver
     * @param endpoint handler to register SOAPMessageListener if needed.
     */
    public void endpointRemoved(Endpoint endpoint) {
        
        EndpointMonitor monitor = monitoredEndpoints.remove(endpoint.getEndpointSelector());
        if (monitor!=null) {
            endpoint.removeListener(monitor);
        }        
    }
    
    public EndpointMonitor getEndpointMonitor(String selector) {
        return monitoredEndpoints.get(selector);
    }
}
