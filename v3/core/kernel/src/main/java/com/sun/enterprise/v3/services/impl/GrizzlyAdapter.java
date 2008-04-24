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

import com.sun.enterprise.config.serverbeans.HttpListener;
import com.sun.grizzly.Controller;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.api.deployment.ApplicationContainer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jvnet.hk2.component.Habitat;

/**
 * The Grizzly Service is responsible for starting grizzly and register the 
 * top level adapter. It is also providing a runtime service where other 
 * services (like admin for instance) can register endpoints adapter to 
 * particular context root. 
 *
 * @author Jerome Dochez
 */
public class GrizzlyAdapter extends AbstractAdapter implements NetworkProxy {
    
    final SelectorThread selectorThread;

    final Logger logger;
    final HttpListener httpListener;

    private Map<String, com.sun.grizzly.tcp.Adapter> endpoints = new HashMap<String, com.sun.grizzly.tcp.Adapter>();

    int portNumber;
    
    private VirtualHostMapper vsMapper;
    

    public GrizzlyAdapter(final Logger logger, 
            Habitat habitat, HttpListener listener, Controller controller) {

        this.logger = logger;
        this.httpListener = listener;

        // ToDo : arcand : how do we start in https mode ?
        Boolean state = Boolean.valueOf(listener.getSecurityEnabled());
        String port = listener.getPort();

        if (port==null) {
            logger.severe("Cannot find port information from domain.xml");
            throw new RuntimeException("Cannot find port information from domain configuration");
        }

        try {
            portNumber = Integer.parseInt(port);
        } catch(java.lang.NumberFormatException e) {
            portNumber = 8080;
            logger.severe("Cannot parse port value : " + port + ", using port 8080");
        }

        selectorThread = new SelectorThread();
        selectorThread.setPort(portNumber);
        selectorThread.setAlgorithmClassName(StaticStreamAlgorithm.class.getName());
        selectorThread.setBufferResponse(false);
    }

    /**
     * Stops the grizzly service.
     */
    public void stop() {
        selectorThread.stopEndpoint();
    }
    
    @Override
    public String toString() {
        return "Grizzly on port " + portNumber;
    }

    /*
     * Registers a new endpoint (adapter implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the adapter instance passed in.
     * @param contextRoot for the adapter
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers,
                                 com.sun.grizzly.tcp.Adapter endpointAdapter,
                                 ApplicationContainer container) throws EndpointRegistrationException {
        
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        
        if (vsMapper.getEndpoint(contextRoot) != null) {
            throw new EndpointRegistrationException("Application with context-root '" + 
                    contextRoot + "' is already registered on Adapter '" + toString() + "'!");
        }
        
        vsMapper.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        vsMapper.unregisterEndpoint(contextRoot, app);
    }

    /**
     * Returns an Adapter instance regitered for handling request coming with
     * this context-root or null if none is registered.
     */
    public com.sun.grizzly.tcp.Adapter getEndpoint(String contextRoot) {
        return endpoints.get(contextRoot);
    }

    /**
     * Returns the context root for this adapter.
     * @return the context root
     */
    public String getContextRoot() {
        return "/";
    }

    
    public void start() {
        selectorThread.setAdapter(vsMapper);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    selectorThread.initEndpoint();
                    selectorThread.startEndpoint();
                } catch(InstantiationException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly selector", e);
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly selector", e);
                } catch (RuntimeException e) {
                    logger.log(Level.INFO, "Exception in grizzly thread", e);
                }
            }
        };
        thread.start();
        logger.info("Listening on port " + portNumber);
    }

    
    public void setVsMapper(VirtualHostMapper vsMapper) {
        this.vsMapper = vsMapper;
    }

    
    public VirtualHostMapper getVsMapper() {
        return vsMapper;
    }

    
    public int getPort() {
        return portNumber;
    }
        
}
