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

package com.sun.enterprise.v3.services.impl;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.Startup;
import org.glassfish.api.deployment.ApplicationContainer;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.StringUtils;
import com.sun.grizzly.Controller;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.logging.Logger;

/**
 * The Network Service is responsible for starting grizzly and register the
 * top level proxy. It is also providing a runtime service where other
 * services (like admin for instance) can register endpoints proxy to
 * particular context root.
 *
 * @author Jerome Dochez
 */
@Service
@Scoped(Singleton.class)
public class GrizzlyService implements Startup, PostConstruct, PreDestroy {

    @Inject(name="server-config") // for now
    Config config;

    @Inject
    Logger logger;

    @Inject
    Habitat habitat;

    List<NetworkProxy> proxies = new ArrayList<NetworkProxy>();
    
    private final Controller controller  = new Controller();
    
    private final static boolean enablePU = 
            Boolean.parseBoolean(System.getProperty("v3.grizzly.enablePU", "true"));
    
    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;                
    }


    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        
        for (HttpListener listener : config.getHttpService().getHttpListener()) {
            // create the proxy for the port.
            NetworkProxy proxy = null;
            if (enablePU){
                proxy = new GrizzlyProxy(logger, habitat, listener, controller, 
                        config.getHttpService());
            } else {
                proxy = new GrizzlyAdapter(logger, habitat, listener, controller);
            }
            proxy.setVsMapper(new VirtualHostMapper(logger, listener));
            
             // attach all virtual servers to this port
            for (VirtualServer vs : config.getHttpService().getVirtualServer()) {
                List<String> vsListeners = StringUtils.parseStringList(vs.getHttpListeners()," ,");
                if (vsListeners.contains(listener.getId())) {
                    proxy.getVsMapper().addVirtualServer(vs);
                }
            }           
            proxy.start();
            
            // add the new proxy to our list of proxies.
            proxies.add(proxy);

            // todo : this neeed some rework...
            // now register all proxies you can find out there !
            // TODO : so far these qets registered everywhere, maybe not the right thing ;-)
            for (org.glassfish.api.container.Adapter subAdapter : habitat.getAllByContract(org.glassfish.api.container.Adapter.class)) {
                logger.fine("Registering proxy " + subAdapter.getContextRoot());
                registerEndpoint(subAdapter.getContextRoot(), null, subAdapter, null);
            }
        }
    }

    /**
     * The component is about to be removed from commission
     */
    public void preDestroy() {
        for (NetworkProxy proxy : proxies) {
            proxy.stop();
        }
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, com.sun.grizzly.tcp.Adapter endpointAdapter,
                                 ApplicationContainer container) {

        registerEndpoint(contextRoot, null, endpointAdapter, container);
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers, com.sun.grizzly.tcp.Adapter endpointAdapter,
                                 ApplicationContainer container) {

        for (NetworkProxy proxy : proxies) {
            proxy.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
        }
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot) {
        unregisterEndpoint(contextRoot, null);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        for (NetworkProxy proxy : proxies) {
            proxy.unregisterEndpoint(contextRoot, app);
        }

    }    
}
