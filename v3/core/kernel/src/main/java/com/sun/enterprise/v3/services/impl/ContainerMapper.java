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

import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.http.DefaultProcessorTask;
import com.sun.grizzly.http.HttpWorkerThread;
import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.InputReader;
import com.sun.grizzly.util.WorkerThread;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.deployment.ApplicationContainer;

import com.sun.grizzly.util.http.mapper.Mapper;
import com.sun.grizzly.util.http.mapper.MappingData;
import com.sun.grizzly.util.http.HttpRequestURIDecoder;
import com.sun.grizzly.util.buf.MessageBytes;
import com.sun.grizzly.util.buf.UDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Contaier's mapper which maps {@link ByteBuffer} bytes representation 
 * to an  {@link Adapter}, {@link ApplicationContainer} and 
 * {@link ProtocolFilter} chain. The mapping result is stored inside
 * {@link MappindData} which is eventually shared with the {@link CoyoteAdapter}, 
 * which is the entry point with the Catalina Servlet Container.
 * 
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class ContainerMapper {

    private final static String ROOT = "/";
    private Mapper mapper;
    private GrizzlyEmbeddedHttp grizzlyEmbeddedHttp;
    private String defaultHostName = "server";
    private Logger logger;
    private UDecoder urlDecoder = new UDecoder();

    private ConcurrentLinkedQueue<HttpParserState> parserStates;
    
    public ContainerMapper(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this.grizzlyEmbeddedHttp = grizzlyEmbeddedHttp;
        parserStates = new ConcurrentLinkedQueue<HttpParserState>();
        logger = GrizzlyEmbeddedHttp.logger();
    }

    
    /**
     * Set the default host that will be used when we map.
     * @param defaultHost
     */
    protected void setDefaultHost(String defaultHost) {
        defaultHostName = defaultHost;
    }

    
    protected void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    
    /**
     * Configure the {@link V3Mapper}. 
     */
    protected synchronized void configureMapper() {
        mapper.setDefaultHostName(defaultHostName);
        // Container deployed have the right to override the default setting.
        Mapper.setAllowReplacement(true);
    }

    
    public void register(String contextRoot, Collection<String> vs, Adapter adapter,
            ApplicationContainer container, List<ProtocolFilter> contextProtocolFilters) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAPPER(" + this + ") REGISTER contextRoot: " + contextRoot +
                    " adapter: " + adapter + " container: " + container +
                    " contextProtocolFilters: " + contextProtocolFilters);
        }

        /*
         * In the case of CoyoteAdapter, return, because the context will
         * have already been registered with the mapper by the connector's
         * MapperListener, in response to a JMX event
         */
        if (adapter.getClass().getName().equals("org.apache.catalina.connector.CoyoteAdapter")) {
            return;
        }

        for (String host : vs) {
            mapper.addContext(host, slash(contextRoot),
                    new ContextRootInfo(adapter, container, contextProtocolFilters), new String[0], null);
        }
    }

    
    private String slash(String path) {
        if (!path.startsWith(ROOT)) {
            return ROOT + path;
        }

        return path;
    }

    
    public void unregister(String contextRoot) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAPPER (" + this + ") UNREGISTER contextRoot: " + contextRoot);
        }
        mapper.removeContext(defaultHostName,slash(contextRoot));
    }

    
    /**
     * Based on the context-root, configure Grizzly's ProtocolChain with the 
     * proper ProtocolFilter, and if available, proper Adapter.
     * @return true if the ProtocolFilter was properly set.
     */
    public boolean map(SelectionKey selectionKey, ByteBuffer byteBuffer, GlassfishProtocolChain protocolChain,
            List<ProtocolFilter> defaultProtocolFilters,
            ContextRootInfo fallbackContextRootInfo) throws Exception {

        HttpParserState state = parserStates.poll();
        if (state == null) {
            state = new HttpParserState();
        } else {
            state.reset();
        }
        
        state.setBuffer(byteBuffer);
        
        byte[] contextBytes = null;
        byte[] hostBytes = null;
        
        try {
            // Read the request line, and parse the context root by removing 
            // all trailling // or ?
            contextBytes = HttpUtils.readRequestLine(selectionKey, state,
                    InputReader.getDefaultReadTimeout());

            if (contextBytes != null) {
                state.setState(0);
                // Read available bytes and try to find the host header.
                hostBytes = HttpUtils.readHost(selectionKey, state,
                        InputReader.getDefaultReadTimeout());
            }
        } finally {
            parserStates.offer(state);
        }
                        
        // No bytes then fail.
        if (contextBytes == null) {
            return false;
        }

        MessageBytes decodedURI = MessageBytes.newInstance();
        decodedURI.setBytes(contextBytes, 0, contextBytes.length);

        MessageBytes hostMB = MessageBytes.newInstance();
        if (hostBytes != null) {
            hostMB.setBytes(hostBytes, 0, hostBytes.length);
        }
        
        // Decode the request to make sure this is not an attack like
        // a directory traversal vulnerability.
        try{
            HttpRequestURIDecoder.decode(decodedURI,urlDecoder,null,null);
        } catch (Exception ex){
            // We fail to decode the request, hence we don't service it.
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Invalid url", ex);          
            }  
            return false;
        }
        // Parse the host. If not found, add it based on the current host.
        HttpUtils.parseHost(hostMB, ((SocketChannel)selectionKey.channel()).socket());

        //TODO: Use ThreadAttachment instead.
        MappingData mappingData = new MappingData();
        
        // Map the request to its Adapter/Container and also it's Servlet if 
        // the request is targetted to the CoyoteAdapter.
        mapper.map(hostMB, decodedURI, mappingData);

        Adapter adapter = null;
        ContextRootInfo contextRootInfo = null;

        // First, let's see if the request is NOT for the CoyoteAdapter, but for
        // another adapter like grail/rail.
        if (mappingData.context != null && mappingData.context instanceof ContextRootInfo) {
            contextRootInfo = (ContextRootInfo) mappingData.context;
            adapter = contextRootInfo.getAdapter();
        } else if (mappingData.context != null && mappingData.context.getClass()
                .getName().equals("com.sun.enterprise.web.WebModule")) {
            
            // Copy the decoded bytes so it can be later re-used by the CoyoteAdapter
            MessageBytes fullDecodedUri = MessageBytes.newInstance();
            fullDecodedUri.duplicate(decodedURI);
            fullDecodedUri.toBytes();

            // We bind the current information to the WorkerThread so CoyoteAdapter
            // can read it and avoid trying to map. Note that we cannot re-use
            // the object as Grizzly ARP might used them from a different Thread.
            WorkerThread workerThread = (WorkerThread) Thread.currentThread();
            workerThread.getAttachment().setAttribute("mappingData", mappingData);
            workerThread.getAttachment().setAttribute("decodedURI", fullDecodedUri);

            adapter = ((V3Mapper) mapper).getAdapter();
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAP (" + this + ") contextRoot: " + new String(contextBytes) +
                    " defaultProtocolFilters: " + defaultProtocolFilters +
                    " fallback: " + fallbackContextRootInfo
                    + " adapter: " + adapter +
                    " mappingData.context " + mappingData.context);
        }                
        
        if (adapter == null && fallbackContextRootInfo != null) {
            adapter = fallbackContextRootInfo.getAdapter();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Fallback adapter is taken: " + adapter);
            }
        }

        if (adapter == null) {
            return false;
        }

        bindAdapter(adapter);

        List<ProtocolFilter> filtersToInject = null;

        // If we have a container, let the request flow throw the default http path
        if (contextRootInfo != null && contextRootInfo.getContainer() != null) {
            List<ProtocolFilter> contextRootProtocolFilters =
                    contextRootInfo.getProtocolFilters();

            filtersToInject = contextRootProtocolFilters != null ? contextRootProtocolFilters : defaultProtocolFilters;

            if (filtersToInject != null) {
                protocolChain.setDynamicProtocolFilters(filtersToInject);
            }
        } else if (fallbackContextRootInfo != null) {
            List<ProtocolFilter> fallbackProtocolFilters =
                    fallbackContextRootInfo.getProtocolFilters();
            if (fallbackProtocolFilters != null && !fallbackProtocolFilters.isEmpty()) {
                protocolChain.setDynamicProtocolFilters(fallbackProtocolFilters);
            }
        }

        return true;
    }

    /**
     * Return the Container associated with the current context-root, null
     * if not found. If the Adapter is found, bind it to the current 
     * ProcessorTask.
     */
    private Adapter bindAdapter(Adapter adapter) {
        // If no Adapter has been found, add a default one. This is the equivalent
        // of having virtual host.
        bindProcessorTask(adapter);

        return adapter;
    }

    // -------------------------------------------------------------------- //
    /**
     * Bind to the current WorkerThread the proper instance of ProcessorTask. 
     * @param adapter The Adapter associated with the ProcessorTask
     */
    private void bindProcessorTask(Adapter adapter) {
        HttpWorkerThread workerThread =
                (HttpWorkerThread) Thread.currentThread();
        DefaultProcessorTask processorTask =
                (DefaultProcessorTask) workerThread.getProcessorTask();
        if (processorTask == null) {
            try {
                //TODO: Promote setAdapter to ProcessorTask?
                processorTask = (DefaultProcessorTask) grizzlyEmbeddedHttp.getProcessorTask();
            } catch (ClassCastException ex) {
                logger.log(Level.SEVERE,
                        "Invalid ProcessorTask instance", ex);
            }
            workerThread.setProcessorTask(processorTask);
        }
        processorTask.setAdapter(adapter);
    }

    /**
     * Class represents context-root associated information
     */
    public static class ContextRootInfo {

        protected Adapter adapter;
        protected ApplicationContainer container;
        protected List<ProtocolFilter> protocolFilters;

        public ContextRootInfo() {
        }

        public ContextRootInfo(Adapter adapter, ApplicationContainer container, List<ProtocolFilter> protocolFilters) {
            this.adapter = adapter;
            this.container = container;
            this.protocolFilters = protocolFilters;
        }

        public Adapter getAdapter() {
            return adapter;
        }

        public void setAdapter(Adapter adapter) {
            this.adapter = adapter;
        }

        public ApplicationContainer getContainer() {
            return container;
        }

        public void setContainer(ApplicationContainer container) {
            this.container = container;
        }

        public List<ProtocolFilter> getProtocolFilters() {
            return protocolFilters;
        }

        public void setProtocolFilters(List<ProtocolFilter> protocolFilters) {
            this.protocolFilters = protocolFilters;
        }
    }
}
