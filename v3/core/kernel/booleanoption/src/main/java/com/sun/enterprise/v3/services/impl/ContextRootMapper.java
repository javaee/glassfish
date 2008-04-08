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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * Context-root map, which maps <code>String</code> context-root representation 
 * with its <code>Adapter</code>, <code>ApplicationContainer</code> and 
 * <code>ProtocolFilter</code> chain.
 * 
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class ContextRootMapper {
    public final static int MIN_CONTEXT_ROOT_READ_BYTES = 5;

    private final static String ROOT = "/";

    
    private GrizzlyEmbeddedHttp grizzlyEmbeddedHttp;

    /**
     * Grizzly's context-root associated artifacts
     */
    private Map<String, ContextRootInfo> contextRootInfoMap = 
            new HashMap<String, ContextRootInfo>();
    
    private Logger logger;
    
    public ContextRootMapper(GrizzlyEmbeddedHttp grizzlyEmbeddedHttp) {
        this.grizzlyEmbeddedHttp = grizzlyEmbeddedHttp;
        logger = GrizzlyEmbeddedHttp.logger();
    }

    
    public void register(String contextRoot, Adapter adapter, ApplicationContainer container,
            List<ProtocolFilter> contextProtocolFilters) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAPPER(" + this + ") REGISTER contextRoot: " + contextRoot +
                    " adapter: " + adapter + " container: " + container +
                    " contextProtocolFilters: " + contextProtocolFilters);
        }
        
        contextRootInfoMap.put(contextRoot, 
                new ContextRootInfo(adapter, container, contextProtocolFilters));
    }

    
    public void unregister(String contextRoot) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAPPER (" + this + ") UNREGISTER contextRoot: " + contextRoot);
        }
        
        contextRootInfoMap.remove(contextRoot);
    }

    /**
     * Based on the context-root, configure Grizzly's ProtocolChain with the 
     * proper ProtocolFilter, and if available, proper Adapter.
     * @return true if the ProtocolFilter was properly set.
     */    
    public boolean map(GlassfishProtocolChain protocolChain,
            ByteBuffer byteBuffer, List<ProtocolFilter> defaultProtocolFilters,
            ContextRootInfo fallbackContextRootInfo) throws IOException {
        //Now we are ready to inject our own ProcessorFilter, but first, we
        //must remove the previous one. The ProtocolChain are statefull, so
        //we can safely add our ProtocolFilter (which are stateless) to it.
        //TODO: Add support for statefull ProtocolFilter

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAP (" + this + ") defaultProtocolFilters: " + 
                    defaultProtocolFilters + 
                    " fallback: " + fallbackContextRootInfo + 
                    " CurrentMapState: " + contextRootInfoMap);

            logger.fine(dump(byteBuffer));
        }
                     
        String contextRoot = parseContextRoot(byteBuffer);
        ContextRootInfo contextRootInfo = lookupContextRootInfo(contextRoot);
        
        Adapter adapter = null;
        if (contextRootInfo != null) {
            adapter = contextRootInfo.getAdapter();
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("MAP (" + this + ") contextRoot: " + contextRoot + 
                    " info: " + contextRootInfo + " adapter: " + adapter);
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
            
            filtersToInject = contextRootProtocolFilters != null ? 
                contextRootProtocolFilters : defaultProtocolFilters;

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
     * Extract the Context Root from the ByteBuffer.
     */
    private String parseContextRoot(ByteBuffer byteBuffer) throws IOException{
        // TODO: Right now we work at the String level, we should work with bytes.
        return ROOT + HttpUtils.findContextRoot(byteBuffer);
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
     * Dump the ByteBuffer content. This is used only for debugging purpose.
     */
    private final static String dump(ByteBuffer byteBuffer){                   
        ByteBuffer dd = byteBuffer.duplicate();
        dd.flip();       
        int length = dd.limit(); 
        byte[] dump = new byte[length];
        dd.get(dump,0,length);
        return(new String(dump)); 
    }

    
    /**
     * Bind to the current WorkerThread the proper instance of ProcessorTask. 
     * @param adapter The Adapter associated with the ProcessorTask
     */
    private void bindProcessorTask(Adapter adapter){
        HttpWorkerThread workerThread = 
                (HttpWorkerThread)Thread.currentThread();
        DefaultProcessorTask processorTask= 
                (DefaultProcessorTask)workerThread.getProcessorTask();
        if (processorTask == null){
            try{
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
     * Looks up the correspondent context-root information
     * @param contextRoot
     */
    private ContextRootInfo lookupContextRootInfo(String contextRoot){ 
        ContextRootInfo contextRootInfo = null;

        for(;;) {            
            contextRootInfo = contextRootInfoMap.get(contextRoot);
            if (contextRootInfo != null) {
                Adapter adapter = contextRootInfo.getAdapter();
                ApplicationContainer container = contextRootInfo.getContainer();
                ClassLoader cl = null;
                if (container != null) {
                    cl = container.getClassLoader();
                }

                try {
                    if (cl==null) {
                        cl = adapter.getClass().getClassLoader();
                    }
                    Thread.currentThread().setContextClassLoader(cl);
                } catch(Exception e) {
                }
               
                break;
            }
            
            if (!contextRoot.equals(ROOT)) {
                int lastIndexOfRoot = contextRoot.lastIndexOf(ROOT);
                if (lastIndexOfRoot != -1) {
                    contextRoot = contextRoot.substring(0, lastIndexOfRoot);
                } else {
                    // Should not get here. Only if contextRoot is malformed
                    break;
                }

                if (contextRoot.length() == 0) {
                    contextRoot = ROOT;
                }
            } else {
                break;
            }
        }
        return contextRootInfo;
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
