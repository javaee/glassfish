/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package org.glassfish.webservices;

/*
import com.sun.enterprise.server.event.ApplicationLoaderEventListener;
import com.sun.enterprise.server.event.ApplicationEvent;
import com.sun.enterprise.server.event.EjbContainerEvent;
import com.sun.enterprise.deployment.*;
import com.sun.logging.LogDomains;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
*/

/**
 * Class that listens for deployment notifications. This class is used to store
 * the webservice endpoints in the EndpointRegistry that are later used in the
 * creation of JBIAdapter's.
 * Related Issue: 6519371
 * 
 * @author Vikas Awasthi
 */
public class ApplicationLoaderEventListenerImpl {
       // implements ApplicationLoaderEventListener {

    /*private final Logger logger = LogDomains.getLogger(LogDomains.SERVER_LOGGER);

    public void handleApplicationEvent(ApplicationEvent event) {
        Application application = event.getApplication();

        if(event.getEventType() == ApplicationEvent.AFTER_APPLICATION_LOAD) {
            List<WebServiceEndpoint> list = new ArrayList<WebServiceEndpoint>();
            for (Object bundle : application.getWebBundleDescriptors()) {
                WebServicesDescriptor webServices =
                        ((WebBundleDescriptor) bundle).getWebServices();
                list.addAll(webServices.getEndpoints());
            }
            if(!list.isEmpty()) {
                String appName = application.getRegistrationName();
                endpoints.put(appName,list);
                logger.log(Level.FINE,
                        "serviceengine.websvc_endpoints_added",
                        new Object[]{appName});
            }
        }

        if(event.getEventType() == ApplicationEvent.AFTER_APPLICATION_UNLOAD) {
            endpoints.remove(application.getRegistrationName());
        }
    }

    public void handleEjbContainerEvent(EjbContainerEvent event) {
    }

    public List<WebServiceEndpoint> getEndpoints(String appName) {
        return endpoints.get(appName);
    }

    *//**
     * During appserver restart applications are loaded before Java EE service
     * engine is installed. This method will be called once during the
     * startup of Java EE service engine. At this time, we need to merge all
     * the endpoints collected so far with the map maintained in
     * EndpointRegistry.
     *//*
    public void mergeEndpointRegistry(Map<String,
            List<WebServiceEndpoint>> ws_endpoints) {
        ws_endpoints.putAll(endpoints);
        endpoints = ws_endpoints;
    }

    private Map<String, List<WebServiceEndpoint>> endpoints =
                                new HashMap<String, List<WebServiceEndpoint>>();
*/
}
