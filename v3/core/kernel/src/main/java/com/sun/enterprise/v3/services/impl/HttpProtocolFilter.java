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
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.util.OutputWriter;
import com.sun.grizzly.util.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.logging.Level;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * Specialized ProtocolFilter that properly configure the Http Adapter on the fly.
 * 
 * @author Jeanfrancois Arcand
 */
public class HttpProtocolFilter implements ProtocolFilter, EndpointMapper<Adapter> {
    
    
    private final static String ROOT = "/";
    
    
    /**
     * The Mapper used to find and configure the endpoint.
     */
    private ContextRootMapper mapper;
    
    
    private GrizzlyServiceListener grizzlyListener;
    
    
    /**
     * The Grizzly's wrapped ProtocolFilter.
     */
    private final ProtocolFilter wrappedFilter;
    
    /**
     *  Fallback context-root information
     */
    private ContextRootMapper.ContextRootInfo fallbackContextRootInfo;

    
    public HttpProtocolFilter(ProtocolFilter wrappedFilter, GrizzlyServiceListener grizzlyListener) {
        this.grizzlyListener = grizzlyListener;
        this.mapper = new ContextRootMapper(grizzlyListener);  
        this.wrappedFilter = wrappedFilter;
        
        StaticResourcesAdapter adapter = new StaticResourcesAdapter();
        adapter.setRootFolder(GrizzlyServiceListener.getWebAppRootPath());
                        
        fallbackContextRootInfo = new ContextRootMapper.ContextRootInfo(adapter,
                null, null);
        
    }

    
    public boolean execute(Context ctx) throws IOException {
        ByteBuffer byteBuffer = 
                ((WorkerThread)Thread.currentThread()).getByteBuffer();
        
        try {
            boolean wasMap = mapper.map(
                    (GlassfishProtocolChain) ctx.getProtocolChain(),
                    byteBuffer, null,
                    fallbackContextRootInfo);
            if (!wasMap) {
                //TODO: Some Application might not have Adapter. Might want to
                //add a dummy one instead of sending a 404.
                try {
                    ByteBuffer bb = HtmlHelper.getErrorPage("Not Found", "HTTP/1.1 404 Not Found\n");
                    OutputWriter.flushChannel
                            (ctx.getSelectionKey().channel(),bb);
                } catch (IOException ex){
                    GrizzlyServiceListener.logger().log(Level.FINE, "Send Error failed", ex);
                } finally {
                    ((WorkerThread)Thread.currentThread()).getByteBuffer().clear();
                }
                return false;               
            }
        } catch (IOException ex) {
            GrizzlyServiceListener.logger().severe(ex.getMessage());
        }

        return wrappedFilter.execute(ctx);
    }

    
    /**
     * Execute the wrapped ProtocolFilter.
     */
    public boolean postExecute(Context ctx) throws IOException {
        return wrappedFilter.postExecute(ctx);
    }    
    
    public void setFallbackAdapter(Adapter adapter) {
        fallbackContextRootInfo.setAdapter(adapter);
    }
    
    public Adapter getFallbackAdapter() {
        return fallbackContextRootInfo.getAdapter();
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
        mapper.register(ensureStartsWithSlash(contextRoot), adapter, null, null);
    }

    
    /**
     * Removes the context-root from our list of adapters.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) {
        mapper.unregister(ensureStartsWithSlash(contextRoot));
    }
    
    private String ensureStartsWithSlash(String path) {
        if (!path.startsWith(ROOT)) {
            return ROOT + path;
        }
        
        return path;
    }
}
