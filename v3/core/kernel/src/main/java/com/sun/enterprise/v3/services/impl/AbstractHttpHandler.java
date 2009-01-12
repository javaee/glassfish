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
import com.sun.grizzly.util.http.HtmlHelper;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.OutputWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.List;
import java.util.logging.Level;

/**
 * Abstract HTTP request handler, which parses context-root and initializes
 * protocol filters, which will continue request processing
 * 
 * @author Alexey Stashok
 */
public abstract class AbstractHttpHandler {
    private final static char ROOT = '/';

    protected GrizzlyEmbeddedHttp grizzlyEmbeddedHttp;
    
    /**
     *  Fallback context-root information
     */
    protected ContainerMapper.ContextRootInfo fallbackContextRootInfo;
    
    /** 
     * The number of default ProcessorFilter a ProtocolChain contains.
     */
    protected volatile List<ProtocolFilter> defaultProtocolFilters;
    
    /**
     * Initialize HTTP request processing.
     * Recgnizes HTTP request and initializes filters, which will continue request
     * processing.
     * 
     * @param context request processing context
     * @param selectionKey
     * @param byteBuffer
     * @return true, if request was pre parsed and processing filters initialized. 
     *         false otherwise.
     */
    public boolean initializeHttpRequestProcessing(Context context,
            ByteBuffer byteBuffer) {
        try {
            if (GrizzlyEmbeddedHttp.logger().isLoggable(Level.FINE)) {
                GrizzlyEmbeddedHttp.logger().fine(dump(byteBuffer));
            }

            SelectionKey selectionKey = context.getSelectionKey();
                        
            boolean wasMap = true;
            try{
                wasMap = grizzlyEmbeddedHttp.getContainerMapper().map(
                        selectionKey, byteBuffer,
                        (GlassfishProtocolChain) context.getProtocolChain(),
                        null,fallbackContextRootInfo);
            } catch (Exception ex){
                GrizzlyEmbeddedHttp.logger().log(Level.WARNING, "Mapper exception", ex);
                wasMap = false;
            }

            if (!wasMap) {
                //TODO: Some Application might not have Adapter. Might want to
                //add a dummy one instead of sending a 404.
                try {
                    ByteBuffer bb = HtmlHelper.getErrorPage("Not Found", "HTTP/1.1 404 Not Found\n", "Glassfish/v3");
                    OutputWriter.flushChannel
                            (selectionKey.channel(),bb);
                } catch (IOException ex){
                    GrizzlyEmbeddedHttp.logger().log(Level.FINE, "Send Error failed", ex);
                } finally {
                    byteBuffer.clear();
                }
                return false;               
            }
            
            return true;
        } catch (Exception e) {
            GrizzlyEmbeddedHttp.logger().log(Level.WARNING, 
                    "Unexpected exception happened, when parsing context-root", e);
        }

        return false;
    }
    
    public List<ProtocolFilter> getDefaultProtocolFilters() {
        return defaultProtocolFilters;
    }

    
    public void setDefaultProtocolFilters(List<ProtocolFilter> defaultProtocolFilters) {
        this.defaultProtocolFilters = defaultProtocolFilters;
    }

    
    public List<ProtocolFilter> getFallbackProtocolFilters() {
        return fallbackContextRootInfo.getProtocolFilters();
    }

    
    public void setFallbackProtocolFilters(List<ProtocolFilter> fallbackProtocolFilters) {
        fallbackContextRootInfo.setProtocolFilters(fallbackProtocolFilters);
    }

    public void setFallbackAdapter(Adapter adapter) {
        fallbackContextRootInfo.setAdapter(adapter);
    }
    
    public Adapter getFallbackAdapter() {
        return fallbackContextRootInfo.getAdapter();
    }

    /**
     * Dump the ByteBuffer content. This is used only for debugging purpose.
     */
    protected final static String dump(ByteBuffer byteBuffer){                   
        ByteBuffer dd = byteBuffer.duplicate();
        dd.flip();       
        int length = dd.limit(); 
        byte[] dump = new byte[length];
        dd.get(dump,0,length);
        return(new String(dump)); 
    }
    
    protected final static String notSlashed(String s) {
        if (s != null && 
                (s.length() == 0 || s.charAt(0) == ROOT)) {
            return s.substring(1);
        }
        
        return s;
    }
}
