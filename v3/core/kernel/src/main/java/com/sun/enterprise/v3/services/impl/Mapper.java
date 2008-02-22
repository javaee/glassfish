/*
 * AdapterConfig.java
 * 
 * Created on Feb 8, 2008, 4:09:00 PM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.ProtocolFilter;
import com.sun.grizzly.http.DefaultProcessorTask;
import com.sun.grizzly.http.DefaultProtocolFilter;
import com.sun.grizzly.http.HttpWorkerThread;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.standalone.StaticStreamAlgorithm;
import com.sun.grizzly.tcp.Adapter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 *
 * @author Jeanfrancois Arcand
 */
public class Mapper {
    private final static String ROOT = "/";

    
    private GrizzlyServiceListener grizzlyListener;

    /**
     * Grizzly's Adapter associated with its context-root.
     */
    private Map<String, Adapter> adapters = null;
    
    /** 
     * Grizzly's ProtocolFilter associated with their respective Container.
     */
    private Map<String,List<ProtocolFilter>> contextProtocolFilters = null;
    
    
    /**
     * Grizzly's Adapter associated with it respective GlassFish Container.
     */
    private Map<Adapter, ApplicationContainer> applicationContainers = null;
     
    
    /** 
     * The number of default ProcessorFilter a ProtocolChain contains.
     */
    private ArrayList<ProtocolFilter> defaultProtocolFilters;
    
    
    /**
     *  Fallback on that ProtocolFilter when no Container has been defined.
     */
    private ProtocolFilter fallbackProtocolFilter;
    
    
    public Mapper(GrizzlyServiceListener grizzlyListener) {
        this.grizzlyListener = grizzlyListener;
    }

    
    void register( Map<String, Adapter> adapters,
            Map<Adapter, ApplicationContainer> applicationContainers,
            Map<String,List<ProtocolFilter>> contextProtocolFilters){
        this.adapters = adapters;
        this.applicationContainers = applicationContainers;
        this.contextProtocolFilters = contextProtocolFilters;                      
    }
    
    
    /**
     * Based on the context-root, configure Grizzly's ProtocolChain with the 
     * proper ProtocolFilter, and if available, proper Adapter.
     * @return true if the ProtocolFilter was properly set.
     */    
    boolean map(GlassfishProtocolChain protocolChain,
            ByteBuffer byteBuffer) throws IOException {
        //Now we are ready to inject our own ProcessorFilter, but first, we
        //must remove the previous one. The ProtocolChain are statefull, so
        //we can safely add our ProtocolFilter (which are stateless) to it.
        //TODO: Add support for statefull ProtocolFilter

        List<ProtocolFilter> protocolFilters = protocolChain.protocolFilters();
        final Logger logger = GrizzlyServiceListener.logger();
        // Set the Default ProtocolFilter than cannot be removed.
        
        if (defaultProtocolFilters == null){
            defaultProtocolFilters = new ArrayList<ProtocolFilter>();
            defaultProtocolFilters.addAll(protocolFilters);
        }
        
        if (logger.isLoggable(Level.FINE)){
            logger.fine(dump(byteBuffer));
        }
             
        String contextRoot = mapContextRoot(byteBuffer);
        Adapter adapter = mapAdapter(contextRoot);
        if (adapter == null){
            return false;
        }
        List<ProtocolFilter> filtersToInject = null;
        ApplicationContainer container = applicationContainers.get(adapter);
        
        // If we have a container, let the request flow throw the default http path
        if (container == null){
            if (fallbackProtocolFilter == null){
                fallbackProtocolFilter = new DefaultProtocolFilter
                        (StaticStreamAlgorithm.class, grizzlyListener.getPort());
            }
        } else {
            filtersToInject = contextProtocolFilters.get(contextRoot);
        }
        
        // TODO: We need to avoid calling clear and have a better algorithm
        // for performance reason.
        if (protocolFilters.size() != defaultProtocolFilters.size()){
            protocolFilters.clear();
            protocolFilters.addAll(defaultProtocolFilters);          
        }
        
        if (filtersToInject != null){
            protocolFilters.addAll(filtersToInject);
        } else {
            protocolFilters.add(fallbackProtocolFilter);
        }

        return true;
    }
    
    
    /** 
     * Extract the Context Root from the ByteBuffer.
     */
    String mapContextRoot(ByteBuffer byteBuffer) throws IOException{
        // TODO: Right now we work at the String level, we should work with bytes.
        return ROOT + HttpUtils.findContextRoot(byteBuffer);
    }
    
    
    /**
     * Return the Container associated with the current context-root, null
     * if not found. If the Adapter is found, bind it to the current 
     * ProcessorTask.
     */
    Adapter mapAdapter(String contextRoot){                
        Adapter adapter = lookupAdapter(contextRoot);
        
        // If no Adapter has been found, add a default one. This is the equivalent
        // of having virtual host.
        // TODO: Allow configuring the docroot of those virtual host
        if (adapter == null && contextRoot.equals(ROOT)){
            adapter = new StaticResourcesAdapter();
            ((StaticResourcesAdapter)adapter)
                    .setRootFolder(GrizzlyServiceListener.getWebAppRootPath());
            adapters.put(contextRoot, adapter);
        } 
        
        // Some extension (like SIP) aren't using the Adapter interface, hence
        // no need to configure the Adapter.
        if (adapter != null) {
            bindProcessorTask(adapter);
        } else {
            GrizzlyServiceListener.logger().log(Level.WARNING,
                    "Adapter was null for: " + contextRoot);  
        }        
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
                processorTask = (DefaultProcessorTask)grizzlyListener.getProcessorTask();
            } catch (ClassCastException ex){
                GrizzlyServiceListener.logger().log(Level.SEVERE,
                        "Invalid ProcessorTask instance",ex);
            }
            workerThread.setProcessorTask(processorTask);
        }
        processorTask.setAdapter(adapter);
    }
    
    
    /**
     * Bind ProcessorTask's Adapter.
     */
    private Adapter lookupAdapter(String contextRoot){ 
        Adapter adapter = null;
        for(;;) {            
            adapter = adapters.get(contextRoot);
            ApplicationContainer container = applicationContainers.get(adapter);
            if (adapter!=null) {
                ClassLoader cl = null;
                if (container!=null) {
                    cl = container.getClassLoader();
                }
                ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
                try {
                    if (cl==null) {
                        cl = adapter.getClass().getClassLoader();
                    }
                    Thread.currentThread().setContextClassLoader(cl);
                } catch(Exception e) {
                    
                } finally {
                    Thread.currentThread().setContextClassLoader(currentCL);
                }
               
                break;
            }
            
            if (!contextRoot.equals(ROOT)) {
                if (contextRoot.lastIndexOf(ROOT)!=-1) {
                    contextRoot = contextRoot.substring(0, contextRoot.lastIndexOf(ROOT));
                }

            } 
            if (contextRoot.length() == 0) {
                contextRoot = ROOT;
            } else {
                break;
            }
        }
        return adapter;
    }
    
    
}
