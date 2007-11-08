/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.grizzly.asyncweb;


import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.Set;
import org.safehaus.asyncweb.container.ServiceContainer;
import org.safehaus.asyncweb.container.basic.HttpServiceHandler;
import org.safehaus.asyncweb.transport.Transport;
import java.util.logging.Level;

import org.safehaus.asyncweb.http.HttpRequest;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.http.HttpService;
import org.safehaus.asyncweb.transport.nio.HttpIOHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * AsyncWeb Integration into Grizzly.
 *
 * @author Dave Irving
 * @author Jeanfrancois Arcand
 */
public class AsyncWebSelectorThread extends SelectorThread implements Transport {
    
    /**
     * Is Grizzly embedded in GlassFish.
     */
    protected static boolean embeddedInGlassFish = false;
    
    // Are we running embedded or not.
    static{
        try{
            embeddedInGlassFish = 
                (Class.forName("org.apache.coyote.tomcat5.Constants") != null);
        } catch(Exception ex){
            ; //Swallow
        }
    }
    
    
    private static final String CONFIG_PROPERTY         = "asyncWeb.config";
    private static final String SERVICE_CONFIG_PROPERTY = "asyncWeb.config.services";
    private static final String DEFAULT_SERVICE_CONFIG  = "httpServiceDefinitions";    
    
    /**
     * The default context for receiving requests.
     */
    private final static String SERVICE_NAME = "";
    
    
    /**
     * The <code>BasicServiceContainer</code> which handle non blocking
     * request.
     */
    private ServiceContainer container;
    
    
    /**
     * The default <code>IoHandler</code> 
     */
    private HttpIOHandler httpIOHandler;
  
    
    /**
     * SelectorThread implementation.
     */
    public AsyncWebSelectorThread() {
        super();
        algorithmClassName = AsyncWebStreamAlgorithm.class.getName();
    }
    
    
    /**
     * Init the <code>AsyncWeb</code> runtime and this 
     * <code>SelectorThread</code>
     */
    public void initEndpoint() throws IOException, InstantiationException {
        configureAsyncWebRuntime();
        httpIOHandler = new HttpIOHandler();
        httpIOHandler.setContainer(container);
        super.initEndpoint();
    }
    
    
    /**
     * Start the <code>SelectorThread
     */
    public void startEndpoint() throws IOException, InstantiationException {
        super.startEndpoint();
        try{
            container.start();
        } catch (Exception ex){
            logger.log(Level.SEVERE,"AsyncWeb startup exception.", ex);
            throw new IOException(ex.getMessage());
        }
    }
    
    
    /**
     * Stop the <code>SelectorThread</code>
     */
    public void stoptEndpoint() throws IOException, InstantiationException {
        try{
            container.stop();
        } catch (Exception ex){
            logger.log(Level.SEVERE,"AsyncWeb stop exception.", ex);
            throw new IOException(ex.getMessage());
        }
        super.stopEndpoint();
    }   
    
    
    /**
     * Creates a new <code>AsyncWebProcessorTask</code> and configure it 
     * to delegate AsyncWeb requests to its associated <code>HttpIoHandler</code>
     * 
     * @return ProcessorTask an instance of AsyncWebProcessorTask
     */
    public ProcessorTask newProcessorTask(boolean initialize){
        AsyncWebProcessorTask task = new AsyncWebProcessorTask();
        task.setMaxHttpHeaderSize(maxHttpHeaderSize);
        task.setBufferSize(requestBufferSize);
        task.setSelectorThread(this);              
        task.setRecycle(recycleTasks);
        task.setIoHandler(httpIOHandler);
        
        task.initialize();
 
        if ( keepAlivePipeline.dropConnection() ) {
            task.setDropConnection(true);
        }    
        task.setPipeline(processorPipeline); 
        return task;
    }
    
    
    /**
     * Cancel keep-alive connections.
     */
    protected void expireIdleKeys(){
        if ( keepAliveTimeoutInSeconds <= 0 || !selector.isOpen()) return;
        long current = System.currentTimeMillis();

        if (current < getNextKeysExpiration()) {
            return;
        }
        setNextKeysExpiration(current + getKaTimeout());
        
        Set<SelectionKey> readyKeys = selector.keys();
        if (readyKeys.isEmpty()){
            return;
        }
        Iterator<SelectionKey> iterator = readyKeys.iterator();
        SelectionKey key;
        while (iterator.hasNext()) {
            key = iterator.next();
            if ( !key.isValid() ) {
                keepAlivePipeline.untrap(key); 
                continue;
            }  
                        
            // Keep-alive expired
            if ( key.attachment() != null ) {   
                long expire = 0L;
                if (key.attachment() instanceof Long){
                   expire = (Long) key.attachment(); 
                } else {
                   expire = (Long) ((ReadTask)key.attachment()).getIdleTime();
                }
                
                if (current - expire >= getKaTimeout()) {
                    cancelKey(key);
                } else if (expire + getKaTimeout() < getNextKeysExpiration()){
                     setNextKeysExpiration(expire + getKaTimeout());
                }
            }
        }                    
    }  

    
    // ------------------------------------------- AsyncWeb Implementation --//
    
    
    /**
     * Builds an async web container - adding transport and
     * services
     *
     * @return  The built container
     */
    private void configureAsyncWebRuntime() {
        String configDir = null;
        
        if ( embeddedInGlassFish ){
            configDir ="file://" 
                + System.getProperty("com.sun.aas.instanceRoot") 
                + File.separator + "config";
        } else if ( rootFolder != null){
            try{
                 configDir ="file://" 
                    + new File(rootFolder).getCanonicalPath() 
                    + File.separator + "conf";     
            } catch (IOException ex){
                logger.log(Level.SEVERE,"Config error", ex);
            }
        } else {
            throw new IllegalStateException("rootFolder cannot be null");
        }
        
        String[] configs = new String[] { configDir + File.separator
                                  + "AsyncWeb.xml", configDir
                                  + File.separator
                                  + DEFAULT_SERVICE_CONFIG
                                  + File.separator + "*.xml"};
        ApplicationContext ctx = new FileSystemXmlApplicationContext(configs);
        container = (ServiceContainer) ctx.getBean("container");
        container.addTransport(this);
        HttpServiceHandler httpServiceHandler = 
                (HttpServiceHandler)ctx.getBean("httpServiceHandler");
        
        HttpService rooService = new RootAsyncService();
        httpServiceHandler.addHttpService(SERVICE_NAME, rooService);            
    }

    
    /**
     * Set the <code>ServiceContainer</code> used by this 
     * <code>SelectorThread</code>
     */
    public void setServiceContainer(ServiceContainer container) {
        this.container = container;
    }

    
    // -------------------------------------------------- Default Service ----//
    
    
    /**
     * An example service - just to show that we can indeed respond asynchronously.
     * We queue up requests on to a Timer, and respond after a set "latency" period
     *
     * I.e, this example demonstrates that our replacement for servlets can respond
     * when they like and free up the container thread which calls in to them
     *
     * @author irvingd
     *
     */
    private static class RootAsyncService implements HttpService {
        
        private byte[] indexFile;
        
        public RootAsyncService(){
            String configDir = null;
            if ( embeddedInGlassFish ){
                configDir = System.getProperty("com.sun.aas.instanceRoot") 
                    + File.separator + "config" + File.separator 
                    + DEFAULT_SERVICE_CONFIG 
                    + File.separator + "index.html";
            } else if ( rootFolder != null){
                try{
                    configDir = new File(rootFolder).getCanonicalPath()
                        + File.separator + "conf" + File.separator
                        + DEFAULT_SERVICE_CONFIG 
                        + File.separator + "index.html";                             
                } catch (IOException ex){
                    logger.log(Level.SEVERE,"Config error", ex);
                }
            } else {
                throw new IllegalStateException("rootFolder cannot be null");
            }            
            
            File file = new File(configDir);
            FileChannel fileChannel = null;
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(file);
                fileChannel = stream.getChannel();
                long size = fileChannel.size();
                MappedByteBuffer map
                        = fileChannel.map(FileChannel.MapMode.READ_ONLY,0,size);  
                indexFile = new byte[(int)size];
                map.get(indexFile);             
            } catch (IOException ioe) {
                logger.log(Level.SEVERE,"RootAsyncService",ioe);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException ioe) {
                    }
                }
                if (fileChannel != null) {
                    try {
                        fileChannel.close();
                    } catch (IOException ioe) {
                    }
                }
            }           
        }
        
        
        /**
         * Handles a received request.
         * We schedule a response to be provided at some later time, and return.
         *
         * @param request  The request to be handled
         */
        public void handleRequest(HttpRequest request) {
            doRespond(request);
        }
        
        
        public void start() {
            // Lifecycle method - probably just use annotations one day
        }
        
        
        public void stop() {
            // Lifecycle method - probably just use annotations one day
        }
        
        
        /**
         * Actually write and commit a response using the default index.html
         *
         * @param request  The request to respond to
         */
        private void doRespond(HttpRequest request) {
            HttpResponse response = request.createHttpResponse();
            OutputStream out = response.getOutputStream();
            try{
                out.write(indexFile);
                out.flush();
            } catch (IOException ex){
                logger.log(Level.SEVERE,"RootAsyncService",ex);
            } finally{
                try {
                    out.close();
                } catch (IOException ex) {}
            }
            request.commitResponse(response);
        }
    }
}
