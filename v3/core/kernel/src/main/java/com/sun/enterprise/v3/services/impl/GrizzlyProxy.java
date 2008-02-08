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
import com.sun.grizzly.http.portunif.HttpProtocolFinder;
import com.sun.grizzly.portunif.PUPreProcessor;
import com.sun.grizzly.portunif.ProtocolFinder;
import com.sun.grizzly.portunif.ProtocolHandler;
import com.sun.grizzly.portunif.TLSPUPreProcessor;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.net.SSLImplementation;
import com.sun.grizzly.util.net.ServerSocketFactory;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;

import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
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
public class GrizzlyProxy implements NetworkProxy{
    
    
    protected GrizzlyServiceListener grizzlyListener;

    
    final Logger logger;
    
    
    final HttpListener httpListener;

    
    private EndpointMapper<Adapter> endPointMapper;
    
    
    //TODO: This must be configurable.
    private final static boolean isWebProfile = 
            Boolean.parseBoolean(System.getProperty("v3.grizzly.webProfile", "true"));
    
    
    /**
     * TODO: We must configure Grizzly using the HttpService element,
     * <strong>not HttpListener only</strong>.
     */
    public GrizzlyProxy(final Logger logger, 
            Habitat habitat, HttpListener httpListener, Controller controller) {
        this.logger = logger;
        this.httpListener = httpListener;

        String port = httpListener.getPort();
        int portNumber = 8080;

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
      
        //TODO: Enabled SSL.
   /*     try{
            if (Boolean.parseBoolean(httpListener.getSecurityEnabled())){
                SSLImplementation sslHelper = SSLImplementation.getInstance();
                ServerSocketFactory serverSF = 
                        sslHelper.getServerSocketFactory();
                serverSF.setAttribute("keystoreType","JKS");
                serverSF.setAttribute("keystore",
                        System.getProperty("javax.net.ssl.keyStore"));
                serverSF.setAttribute("truststoreType","JKS");
                serverSF.setAttribute("truststore",
                        System.getProperty("javax.net.ssl.trustStore"));                    
                serverSF.init();
                grizzlyListener.setSSLContext(serverSF.getSSLContext());                
            }
        } catch (Throwable t){
            logger.severe("Unable to configure SSL");
        }*/
    }

    
    /**
     * Create a <code>GrizzlyServiceListener</code> based on a HttpService
     * configuration object.
     * @param port the port on which we need to listen.
     */
    private void configureGrizzly(int port,Controller controller){
        grizzlyListener = GrizzlyHttpEmbed.createListener(null, port, controller);
        
        if (!isWebProfile){
            PUPreProcessor preProcessor = null;
            SSLContext sslContext = grizzlyListener.getSSLContext();

            // [1] Detect TLS requests.
            // If sslContext is null, that means TLS is not enabled on that port.
            // We need to revisit the way GlassFish is configured and make
            // sure TLS is always enabled. We can always do what we did for 
            // GlassFish v2, which is to located the keystore/trustore by ourself.
            // TODO: Enable TLS support on all ports using com.sun.Grizzly.SSLConfig
            if (sslContext != null) {
                preProcessor = new TLSPUPreProcessor(sslContext);
            }
            ArrayList<PUPreProcessor> puPreProcessors = new ArrayList<PUPreProcessor>();
            if (preProcessor != null){
                puPreProcessors.add(preProcessor);
            }

            // [2] Add our supported ProtocolFinder. By default, we support http/sip
            // TODO: The list of ProtocolFinder is retrieved using System.getProperties().
            ArrayList<ProtocolFinder> protocolFinders = new ArrayList<ProtocolFinder>();
            protocolFinders.add(new HttpProtocolFinder());

            // [3] Add our supported ProtocolHandler. By default we support http/sip.
            ArrayList<ProtocolHandler> 
                    protocolHandlers = new ArrayList<ProtocolHandler>();
            endPointMapper = new WebProtocolHandler(grizzlyListener);
            protocolHandlers.add((ProtocolHandler)endPointMapper);
            
            grizzlyListener.configurePortUnification
                   (protocolFinders, protocolHandlers, puPreProcessors); 
        } else {
            endPointMapper = grizzlyListener.createHttpProtocolFilter();
        }
    }
    
  
    /**
     * Stops the Grizzly service.
     */
    public void stop() {
        grizzlyListener.stopEndpoint();
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
        endPointMapper.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        endPointMapper.unregisterEndpoint(contextRoot, app);
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
                    grizzlyListener.initEndpoint();
                    grizzlyListener.startEndpoint();
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

}
