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
import com.sun.grizzly.http.DefaultProtocolFilter;
import com.sun.grizzly.http.HtmlHelper;
import com.sun.grizzly.portunif.PUProtocolRequest;
import com.sun.grizzly.portunif.ProtocolHandler;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.util.ByteBufferInputStream;
import com.sun.grizzly.util.OutputWriter;
import com.sun.grizzly.util.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



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
public class WebProtocolHandler implements ProtocolHandler {
    
    public enum Mode {
        HTTP, HTTPS, HTTP_HTTPS, SIP, SIP_TLS;
    }
    
    /**
     * The protocols supported by this handler.
     */
    protected String[][] protocols = {{"http"}, {"https"}, 
                {"https", "http"}, {"sip"},{"sip","sip_tls"}};
    
    
    private Mode mode;
    
    
    private GrizzlyEmbeddedHttp grizzlyEmbeddedHttp;

    
    /** 
     * The number of default ProcessorFilter a ProtocolChain contains.
     */
    private volatile List<ProtocolFilter> defaultProtocolFilters;
    
    
    /**
     *  Fallback context-root information
     */
    private ContextRootMapper.ContextRootInfo fallbackContextRootInfo;
    
    
    /**
     * Logger
     */
    private Logger logger;
    
    // --------------------------------------------------------------------//
    
    
    public WebProtocolHandler(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this(Mode.HTTP, grizzlyEmbeddedHttp);
    }
    
    
    public WebProtocolHandler(Mode mode, GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this.mode = mode;
        this.grizzlyEmbeddedHttp = grizzlyEmbeddedHttp;
        logger = GrizzlyEmbeddedHttp.logger();
    }

    
    // --------------------------------------------------------------------//

    
    /**
     * Based on the context-root, configure Grizzly's ProtocolChain with the 
     * proper ProtocolFilter, and if available, proper Adapter.
     * @return true if the ProtocolFilter was properly set.
     */
    public boolean handle(Context context, PUProtocolRequest protocolRequest) 
            throws IOException {
        
        initDefaultHttpArtifactsIfRequired();
        
        // Make sure we have enough bytes to parse context-root
        if (protocolRequest.getByteBuffer().position() < 
                ContextRootMapper.MIN_CONTEXT_ROOT_READ_BYTES) {
            if (GrizzlyUtils.readToWorkerThreadBuffers(context.getSelectionKey(), 
                    ByteBufferInputStream.getDefaultReadTimeout()) == -1) {
                    context.setKeyRegistrationState(
                        Context.KeyRegistrationState.CANCEL);
                    return false;
            }
        }

        boolean wasMap = grizzlyEmbeddedHttp.getContextRootMapper().map(
                (GlassfishProtocolChain)context.getProtocolChain(),
                protocolRequest.getByteBuffer(), defaultProtocolFilters, 
                fallbackContextRootInfo);
        if (!wasMap){
            //TODO: Some Application might not have Adapter. Might want to
            //add a dummy one instead of sending a 404.
             try {
                ByteBuffer bb = HtmlHelper.getErrorPage("Not Found", "HTTP/1.1 404 Not Found\n");
                OutputWriter.flushChannel(protocolRequest.getChannel(), bb);
            } catch (IOException ex){
                logger.log(Level.FINE, "Send Error failed", ex);
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
    
    private void initDefaultHttpArtifactsIfRequired() {
        if (defaultProtocolFilters == null) {
            synchronized (this) {
                if (defaultProtocolFilters == null) {
                    grizzlyEmbeddedHttp.initAlgorithm();
                    List<ProtocolFilter> tmpProtocolFilters = new ArrayList<ProtocolFilter>(4);
                    tmpProtocolFilters.addAll(grizzlyEmbeddedHttp.getDefaultHttpProtocolFilters());

                    StaticResourcesAdapter adapter = new StaticResourcesAdapter();
                    adapter.setRootFolder(GrizzlyEmbeddedHttp.getWebAppRootPath());

                    fallbackContextRootInfo = new ContextRootMapper.ContextRootInfo(adapter,
                            null, Collections.<ProtocolFilter>singletonList(new DefaultProtocolFilter(
                            StaticStreamAlgorithm.class, grizzlyEmbeddedHttp.getPort())));
                    
                    defaultProtocolFilters = tmpProtocolFilters;
                }
            }
        }
    }
    
    /**
     * Returns <code>ByteBuffer</code>, where PUReadFilter will read data
     * @return <code>ByteBuffer</code>
     */    
    public ByteBuffer getByteBuffer(){
        return null;
    }
}
