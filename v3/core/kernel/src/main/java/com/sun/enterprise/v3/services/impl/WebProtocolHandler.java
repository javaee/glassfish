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

import com.sun.grizzly.Context;
import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.http.HtmlHelper;
import com.sun.grizzly.portunif.PUProtocolRequest;
import com.sun.grizzly.portunif.ProtocolHandler;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.OutputWriter;
import com.sun.grizzly.util.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;



/**
 * This class maps the current request to its associated Container.
 * 
 * NOTE: <strong>This ServicesHandler only suport HTTP and SIP as protocol.</strong>
 * 
 * This class is not thread-safe (for now), which means all the add* method
 * cannot be invoked once the associated GrizzlyServiceListener has been 
 * started.
 * 
 * TODO: Make it work dynamically like MInnow, allocating Service on the fly.
 * @author Jeanfrancois Arcand
 */
public class WebProtocolHandler implements ProtocolHandler, EndpointMapper<Adapter>{
    
    private final static String ROOT = "/";
    
    
    public enum Mode {
        HTTP, HTTPS, HTTP_HTTPS, SIP, SIP_TLS;
    }
    
    /**
     * The protocols supported by this handler.
     */
    protected String[][] protocols = {{"http"}, {"https"}, 
                {"https", "http"}, {"sip"},{"sip","sip_tls"}};
    
    
    private Mode mode;
    
    
    private GrizzlyServiceListener grizzlyListener;

    
    /**
     * Grizzly's Adapter associated with its context-root.
     */
    private Map<String, Adapter> adapters = new HashMap<String, Adapter>();
    
    /** 
     * Grizzly's ProtocolFilter associated with their respective Container.
     */
    private HashMap<String,List<ProtocolFilter>> contextProtocolFilters
            = new HashMap<String,List<ProtocolFilter>>();
    
    
    /**
     * Grizzly's Adapter associated with it respective GlassFish Container.
     */
    private Map<Adapter, ApplicationContainer> applicationContainers 
            = new HashMap<Adapter, ApplicationContainer>();
     
    
    /** 
     * The number of default ProcessorFilter a ProtocolChain contains.
     */
    private ArrayList<ProtocolFilter> defaultProtocolFilters;
    
    
    /**
     *  Fallback on that ProtocolFilter when no Container has been defined.
     */
    private ProtocolFilter fallbackProtocolFilter;
    
    
    /**
     * The Mapper used to find and configure the endpoint.
     */
    private Mapper mapper;

    
    // --------------------------------------------------------------------//
    
    
    public WebProtocolHandler(GrizzlyServiceListener grizzlyListener) {
        this(Mode.HTTP,grizzlyListener);
    }
    
    
    public WebProtocolHandler(Mode mode,GrizzlyServiceListener grizzlyListener) {
        this.mode = mode;
        this.grizzlyListener = grizzlyListener;
        this.mapper = new Mapper(grizzlyListener);
    }

    
    // --------------------------------------------------------------------//

    
    /**
     * Based on the context-root, configure Grizzly's ProtocolChain with the 
     * proper ProtocolFilter, and if available, proper Adapter.
     * @return true if the ProtocolFilter was properly set.
     */
    public boolean handle(Context context, PUProtocolRequest protocolRequest) 
            throws IOException {
        
        boolean wasMap = mapper.map(
                (GlassfishProtocolChain)context.getProtocolChain(),
                protocolRequest.getByteBuffer());        
        if (!wasMap){
            //TODO: Some Application might not have Adapter. Might want to
            //add a dummy one instead of sending a 404.
             try {
                ByteBuffer bb = HtmlHelper.getErrorPage("Not Found", "HTTP/1.1 404 Not Found\n");
                OutputWriter.flushChannel(protocolRequest.getChannel(), bb);
            } catch (IOException ex){
                GrizzlyServiceListener.logger().log(Level.FINE,"Send Error failed", ex);
            } finally{
                ((WorkerThread)Thread.currentThread()).getByteBuffer().clear();
            }
            return false;
        }
        
        // Grizzly will invoke take care of invoking the Container.
        protocolRequest.setExecuteFilterChain(true);                
        return wasMap;
    }
        

    // -------------------------------------------------------------------- //
    
    
    /*
     * Registers a new endpoint (adapter implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the adapter instance passed in.
     * @param contextRoot for the adapter
     * @param adapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vs, Adapter adapter,
                                 ApplicationContainer container) {
        if (!contextRoot.startsWith(ROOT)) {
            contextRoot = ROOT + contextRoot;
        }
        adapters.put(contextRoot, adapter);
        if (container!=null) {
            applicationContainers.put(adapter, container);
        }
        ArrayList<ProtocolFilter> filter = new ArrayList<ProtocolFilter>(1);
        filter.add(grizzlyListener.getHttpProtocolFilter());
        contextProtocolFilters.put(contextRoot,filter);
        mapper.register(adapters,applicationContainers,contextProtocolFilters);
    }

    
    /**
     * Removes the context-root from our list of adapters.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        if (applicationContainers.containsValue(app)) {
            adapters.remove(contextRoot);
            applicationContainers.remove(app);
            contextProtocolFilters.remove(contextRoot);
        }
        mapper.register(adapters,applicationContainers,contextProtocolFilters);
    }
    
    
    /**filter
     * Returns an array of supported protocols.
     * @return an array of supported protocols.
     */
    public String[] getProtocols() {
        return protocols[mode.ordinal()];
    }
    
    
    /**
     * Invoked when the SelectorThread is about to expire a SelectionKey.
     * @return true if the SelectorThread should expire the SelectionKey, false
     *              if not.
     */
    public boolean expireKey(SelectionKey key){
        return true;
    }

    
    public ArrayList<ProtocolFilter> getDefaultProtocolFilters() {
        return defaultProtocolFilters;
    }

    
    public void setDefaultProtocolFilters(ArrayList<ProtocolFilter> defaultProtocolFilters) {
        this.defaultProtocolFilters = defaultProtocolFilters;
    }

    
    public ProtocolFilter getFallbackProtocolFilter() {
        return fallbackProtocolFilter;
    }

    
    public void setFallbackProtocolFilter(ProtocolFilter fallbackProtocolFilter) {
        this.fallbackProtocolFilter = fallbackProtocolFilter;
    }



}
