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
import com.sun.grizzly.http.DefaultProtocolFilter;
import com.sun.grizzly.http.HtmlHelper;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.OutputWriter;
import com.sun.grizzly.util.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * Specialized ProtocolFilter that properly configure the Http Adapter on the fly.
 * 
 * @author Jeanfrancois Arcand
 */
public class HttpProtocolFilter extends DefaultProtocolFilter implements EndpointMapper<Adapter>{
    
    
    private final static String ROOT = "/";
    
    
    /**
     * Grizzly's Adapter associated with its context-root.
     */
    private Map<String, Adapter> adapters = new HashMap<String, Adapter>();

    
    /**
     * Grizzly's Adapter associated with it respective GlassFish Container.
     */
    private Map<Adapter, ApplicationContainer> applicationContainers 
            = new HashMap<Adapter, ApplicationContainer>();
    
    
    /**
     * The Mapper used to find and configure the endpoint.
     */
    private Mapper mapper;
    
    
    private GrizzlyServiceListener grizzlyListener;
    
    
    public HttpProtocolFilter(Class algorithmClass, int port, GrizzlyServiceListener grizzlyListener) {
        super(algorithmClass,port);
        this.grizzlyListener = grizzlyListener;
        this.mapper = new Mapper(grizzlyListener);        
    }

    
    @Override
    public boolean execute(Context ctx) throws IOException {
        ByteBuffer byteBuffer = 
                ((WorkerThread)Thread.currentThread()).getByteBuffer();
        
        try{
            String contextRoot = mapper.mapContextRoot(byteBuffer);
            Adapter adapter = mapper.mapAdapter(contextRoot);
            if (adapter == null){
                //TODO: Some Application might not have Adapter. Might want to
                //add a dummy one instead of sending a 404.
                try {
                    ByteBuffer bb = HtmlHelper.getErrorPage("Not Found", "HTTP/1.1 404 Not Found\n");
                    OutputWriter.flushChannel
                            (ctx.getSelectionKey().channel(),bb);
                } catch (IOException ex){
                    GrizzlyServiceListener.logger().log(Level.FINE,"Send Error failed", ex);
                } finally {
                    ((WorkerThread)Thread.currentThread()).getByteBuffer().clear();
                }
                return false;               
            }
        } catch (IOException ex){
            GrizzlyServiceListener.logger().severe(ex.getMessage());
        }

        return super.execute(ctx);
    }
    
    
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
        mapper.register(adapters,applicationContainers,null);
    }

    
    /**
     * Removes the context-root from our list of adapters.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        if (applicationContainers.containsValue(app)) {
            adapters.remove(contextRoot);
            applicationContainers.remove(app);
        }
        mapper.register(adapters,applicationContainers,null);
    }
    
}
