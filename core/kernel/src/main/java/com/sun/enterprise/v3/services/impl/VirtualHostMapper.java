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
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.api.deployment.ApplicationContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Grizzly Service is responsible for starting grizzly and register the 
 * top level adapter. It is also providing a runtime service where other 
 * services (like admin for instance) can register endpoints adapter to 
 * particular context root. 
 * 
 * TODO: We must uses bytes, not String. Also we need a much better mapping algorithm
 *       for performance reason (jfa).
 *
 * @author Jerome Dochez
 */
public class VirtualHostMapper extends AbstractAdapter implements com.sun.grizzly.tcp.Adapter{
    
    final Logger logger;
    final HttpListener httpListener;

    private Map<String, com.sun.grizzly.tcp.Adapter> endpoints = new HashMap<String, com.sun.grizzly.tcp.Adapter>();

    int portNumber;

    Map<String, VsAdapter> vsAdapters = new HashMap<String, VsAdapter>();

    public VirtualHostMapper(final Logger logger, HttpListener listener) {

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
    }

    public void addVirtualServer(VirtualServer vs) {
        vsAdapters.put(vs.getId(), new VsAdapter(vs));
    }

    
    /**
     * Call the service method, and notify all listeners
     *
     * @exception Exception if an error happens during handling of
     *   the request. Common errors are:
     *   <ul><li>IOException if an input/output error occurs and we are
     *   processing an included servlet (otherwise it is swallowed and
     *   handled by the top level error handler mechanism)
     *       <li>ServletException if a servlet throws an exception and
     *  we are processing an included servlet (otherwise it is swallowed
     *  and handled by the top level error handler mechanism)
     *  </ul>
     *  Tomcat should be able to handle and log any other exception ( including
     *  runtime exceptions )
     */
    public void service(Request req, Response res)
        throws Exception {

        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Received something on " + req.requestURI());
        }

        // find the right virtual server this is intented to
        final String serverName = req.serverName().toString();
        for (VsAdapter adapter : vsAdapters.values()) {
            if (adapter.handles(serverName)) {
                adapter.service(req,res);
                return;
            }
        }
        // default virtual server dispatching
        if (httpListener.getDefaultVirtualServer()!=null) {
            VsAdapter adapter = vsAdapters.get(httpListener.getDefaultVirtualServer());
            if (adapter!=null) {
                adapter.service(req,res);
                return;
            }
        }

        // if I am here, I could not find the right VirtualServer.
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Invalid server name " + serverName + " for request " + req.requestURI().toString());
        }
        sendError(res, "Glassfish v3 Error : Server name Not Found : " + serverName);        

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
                                 ApplicationContainer container) {
        
        StringBuilder buffer = null;

        if (!contextRoot.startsWith("/")) {
            contextRoot = "/" + contextRoot;
        }
        if (logger.isLoggable(Level.FINE)) {
            buffer = new StringBuilder("Endpoint registered at ").append(contextRoot);
            buffer.append(" on virtual server(s) : ");
        }
        if (vsServers==null) {
            for (VsAdapter adapter : vsAdapters.values()) {
                adapter.registerEndpoint(contextRoot, endpointAdapter, container);
                if (logger.isLoggable(Level.FINE)) {
                    buffer.append(adapter.getVirtualServer().getId()).append(" ");
                }
            }        
        } else {
            for (String vsId : vsServers) {
                VsAdapter vsAdapter = vsAdapters.get(vsId);
                if (vsAdapter!=null) {
                    vsAdapter.registerEndpoint(contextRoot, endpointAdapter, container);       
                    if (logger.isLoggable(Level.FINE)) {
                        buffer.append(vsAdapter.getVirtualServer().getId()).append(" ");
                    }
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(buffer.toString());
        }
    }

    /**
     * Removes the contex-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        for (VsAdapter adapter : vsAdapters.values()) {
            adapter.unregisterEndpoint(contextRoot, app);
        }
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
}
