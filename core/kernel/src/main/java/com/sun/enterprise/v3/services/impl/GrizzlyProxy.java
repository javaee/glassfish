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
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.grizzly.Controller;
import com.sun.grizzly.tcp.Adapter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;

import java.util.logging.Logger;
import org.jvnet.hk2.component.Habitat;

/**
 * The Grizzly Service is responsible for starting Grizzly Port Unification 
 * mechanism. It is also providing a runtime service where other
 * services (like admin for instance) can register endpoints adapter to 
 * particular context root. 
 *
 * @author Jerome Dochez
 * @author Jeanfrancois Arcand
 */
public class GrizzlyProxy implements NetworkProxy {
    
    
    protected GrizzlyServiceListener grizzlyListener;

    
    final Logger logger;
    
    
    final HttpListener httpListener;
    
    
    final HttpService httpService;

    
    private EndpointMapper<Adapter> endPointMapper;
    
        
    private VirtualHostMapper vsMapper;
    

    private int portNumber;

    
    //TODO: This must be configurable.
    private final static boolean isWebProfile = 
            Boolean.parseBoolean(System.getProperty("v3.grizzly.webProfile", "true"));
    
    
    private static List<String> nvVsMapper = new ArrayList<String>();
    
    
    // Those Adapter MUST not be mapped through a VirtualHostMapper, as our
    // WebContainer already supports it.
    static{
        nvVsMapper.add("org.apache.coyote.tomcat5.CoyoteAdapter");
        nvVsMapper.add("com.sun.enterprise.v3.admin.AdminAdapter");
    }
    
    
    /**
     * TODO: We must configure Grizzly using the HttpService element,
     * <strong>not HttpListener only</strong>.
     */
    public GrizzlyProxy(final Logger logger, Habitat habitat, 
            HttpListener httpListener, Controller controller, HttpService httpService) {
        this.logger = logger;
        this.httpListener = httpListener;
        this.httpService = httpService;

        String port = httpListener.getPort();
        portNumber = 8080;

        if (port==null) {
            logger.severe("Cannot find port information from domain.xml");
            throw new RuntimeException("Cannot find port information from domain configuration");
        }

        try {
            portNumber = Integer.parseInt(port);
        } catch(java.lang.NumberFormatException e) {
            logger.severe("Cannot parse port value : " + port + ", using port 8080");
        }
        
        configureGrizzly(portNumber, controller);  
    }

    
    /**
     * Create a <code>GrizzlyServiceListener</code> based on a HttpService
     * configuration object.
     * @param port the port on which we need to listen.
     */
    private void configureGrizzly(int port, Controller controller) {
        grizzlyListener = new GrizzlyServiceListener();
        
        GrizzlyEmbeddedHttpConfigurator.configureEmbeddedHttp(grizzlyListener, 
                httpService, httpListener, port, controller);
        
        endPointMapper = grizzlyListener.configureEndpointMapper(isWebProfile);
    }
    
  
    /**
     * Stops the Grizzly service.
     */
    public void stop() {
        grizzlyListener.stop();
    }
    
    
    @Override
    public String toString() {
        return "Grizzly on port " + httpListener.getPort();
    }
    

    /*
     * Registers a new endpoint (adapter implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the adapter instance passed in.
     * @param contextRoot for the adapter
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers,
                                 Adapter endpointAdapter,
                                 ApplicationContainer container) {
        
        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }        
        // THis is a hack, but we don't want to add virtual server support
        // for the Web Container as it already supports it.
        if (!nvVsMapper.contains(endpointAdapter.getClass().getName())) {
            vsMapper.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
            endpointAdapter = vsMapper;
        }
        endPointMapper.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
    }

    
    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        endPointMapper.unregisterEndpoint(contextRoot, app);
        vsMapper.unregisterEndpoint(contextRoot, app);
    }

    
    /**
     * Returns the context root for this adapter.
     * @return the context root
     */
    public String getContextRoot() {
        return "/";
    }

    
    public void start() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    grizzlyListener.start();
                } catch(InstantiationException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly listener", e);
                } catch(IOException e) {
                    logger.log(Level.SEVERE, "Cannot start grizzly listener", e);
                } catch (RuntimeException e) {
                    logger.log(Level.INFO, "Exception in grizzly thread", e);
                }
            }
        };
        thread.start();
        logger.info("Listening on port " + grizzlyListener.getPort());
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
