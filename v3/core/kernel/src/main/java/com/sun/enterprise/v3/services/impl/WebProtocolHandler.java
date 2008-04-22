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
import com.sun.grizzly.portunif.PUProtocolRequest;
import com.sun.grizzly.portunif.ProtocolHandler;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.util.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



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
public class WebProtocolHandler extends AbstractHttpHandler 
        implements ProtocolHandler {
    public enum Mode {
        HTTP, HTTPS, HTTP_HTTPS, SIP, SIP_TLS;
    }
    
    /**
     * The protocols supported by this handler.
     */
    protected String[][] protocols = {{"http"}, {"https"}, 
                {"https", "http"}, {"sip"},{"sip","sip_tls"}};
    
    
    private Mode mode;
    
    
    // --------------------------------------------------------------------//
    
    
    public WebProtocolHandler(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this(Mode.HTTP, grizzlyEmbeddedHttp);
    }
    
    
    public WebProtocolHandler(Mode mode, GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this.mode = mode;
        this.grizzlyEmbeddedHttp = grizzlyEmbeddedHttp;
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
        
        ByteBuffer byteBuffer = protocolRequest.getByteBuffer();

        boolean mappedOk = initializeHttpRequestProcessing(context, byteBuffer);
        // Grizzly will invoke take care of invoking the Container.
        protocolRequest.setExecuteFilterChain(mappedOk);
        
        return mappedOk;
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
        WorkerThread workerThread = (WorkerThread) Thread.currentThread();
        if (workerThread.getSSLEngine() != null) {
            return workerThread.getInputBB();
        }
        
        return null;
    }
}
