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

/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.sun.enterprise.web.connector.grizzly.blocking;


import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.DefaultProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SecureSelector;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.SelectorThreadConfig;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.BindException;
import java.net.Socket;
import java.net.SocketException;
import java.security.AccessControlException;
import java.util.logging.Level;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.ServerSocketFactory;


/**
 * Blocking <code>SocketServer</code> implementation.
 *
 * @author jean-Francois Arcand
 */
public class SelectorBlockingThread extends SelectorThread 
        implements SecureSelector<SSLImplementation>{ 

    protected SSLImplementation sslImplementation = null;

    /**
     * Is SSL enabled
     */
    private boolean secure = false;

    private ServerSocketFactory factory;
   
    /**
     * initialized the endpoint by creating the <code>ServerSocketChannel</code>
     * and by initializing the server socket.
     */
    public void initEndpoint() throws IOException, InstantiationException {
        SelectorThreadConfig.configure(this);
        
        initFileCacheFactory();
        initAlgorithm();
        initPipeline();
        initMonitoringLevel();
        
        setName("SelectorThread-" + getPort());
        
        try{
            if (getInet() == null) {
                setServerSocket(getServerSocketFactory().createSocket(getPort(),
                        getSsBackLog()));
            } else {
                setServerSocket(getServerSocketFactory().createSocket(getPort(), 
                        getSsBackLog(), getInet()));
            }
            getServerSocket().setReuseAddress(true);            
        } catch (SocketException ex){
            throw new BindException(ex.getMessage() + ": " + getPort());
        }
        
        getServerSocket().setSoTimeout(getServerTimeout());
        initReadBlockingTask(getMinReadQueueLength());
        
        setInitialized(true);   
        getLogger().log(Level.FINE,"Initializing Grizzly Blocking Mode");            
    }
    
    
    /**
     * Create a pool of <code>ReadBlockingTask</code>
     */
    private void initReadBlockingTask(int size){         
        for (int i=0; i < size; i++){
            getReadTasks().offer(newReadBlockingTask(false));
        }
    }

    
    /**
     * Create a <code>ReadBlockingTask</code> instance used with blocking
     * socket.
     */
    private ReadBlockingTask newReadBlockingTask(boolean initialize){
                                                        
        ReadBlockingTask task = new ReadBlockingTask();
        task.setSelectorThread(this);
        task.setPipeline(getProcessorPipeline());
 
        task.setRecycle(isRecycleTasks());
        task.attachProcessor(newProcessorTask(initialize));
        task.setPipelineStatistic(getPipelineStat());
        task.setSecure(secure);
 
        return task;
    }
    
    
    /**
     * Return a <code>ReadBlockingTask</code> from the pool. 
     * If the pool is empty,
     * create a new instance.
     */
    protected ReadBlockingTask getReadBlockingTask(Socket socket){
        ReadBlockingTask task = null;
        if (isRecycleTasks()) {
            task = (ReadBlockingTask)getReadTasks().poll();
        }
               
        if (task == null){
            task = newReadBlockingTask(false);
        }   
        
        ProcessorTask processorTask = task.getProcessorTask(); 
        processorTask.setSocket(socket);
        
        return task;
    }    
        
    
    /**
     * Handle a blocking operation on the socket.
     */
    private void handleConnection(Socket socket) throws IOException{
                
        if (isMonitoringEnabled()) {
            getGlobalRequestProcessor().increaseCountOpenConnections();
            getPipelineStat().incrementTotalAcceptCount();
        }

        getReadBlockingTask(socket).execute();
    }   


    protected void setSocketOptions(Socket socket){
        super.setSocketOptions(socket);
        if( getKeepAliveTimeoutInSeconds() > 0){
            try{
                socket.setSoTimeout( getKeepAliveTimeoutInSeconds() * 1000 );
            } catch (SocketException ex){
                logger.log(Level.WARNING,
                        "setSoTimeout exception ",ex);                
            }
        }
    }
    
    
    /**
     * Start the Acceptor Thread and wait for incoming connection, in a non
     * blocking mode.
     */
    public void startEndpoint() throws IOException, InstantiationException {
        setRunning(true);
        
        rampUpProcessorTask();
        registerComponents();

        startPipelines();
        startListener();
    }    
    
    
    protected Socket acceptSocket() {
        if( !isRunning() || getServerSocket()==null ) return null;

        Socket socket = null;

    	try {
            if(getServerSocketFactory()==null) {
                socket = getServerSocketChannel().accept().socket();
            } else {
                socket = getServerSocketFactory().acceptSocket(getServerSocket());
            }
            if (null == socket) {
                getLogger().log(Level.WARNING,"selectorThread.acceptSocket");
            } else {
                if (!isRunning()) {
                    socket.close();  // rude, but unlikely!
                    socket = null;
                } else if (getServerSocketFactory() != null) {
                    getServerSocketFactory().initSocket( socket );
                }
            }
        } catch(InterruptedIOException iioe) {
            // normal part -- should happen regularly so
            // that the endpoint can release if the server
            // is shutdown.
        } catch (AccessControlException ace) {
            // When using the Java SecurityManager this exception
            // can be thrown if you are restricting access to the
            // socket with SocketPermission's.
            // Log the unauthorized access and continue
            getLogger().log(Level.WARNING,"selectorThread.wrongPermission",
                       new Object[]{getServerSocket(), ace});
        } catch (IOException e) {

            String msg = null;

            if (isRunning()) {
                getLogger().log(Level.SEVERE,"selectorThread.shutdownException", 
                           new Object[]{getServerSocket(), e});
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch(Throwable ex) {
                    getLogger().log(Level.SEVERE,"selectorThread.shutdownException", 
                               new Object[]{getServerSocket(), ex});
                }
                socket = null;
            }

            if( !isRunning() ) return null;
        } catch (Throwable t) {                                
            try{
                if (socket != null)
                    socket.close();
            } catch (IOException ex){
                ;
            }
            getLogger().log(Level.FINE,
                       "selectorThread.errorOnRequest",
                       t);
        }

        return socket;
    }  
      
            
    /**
     * Start a blocking server <code>Socket</code>
     */
    protected void startListener(){
        Socket socket = null;
        while (isRunning()){
            socket = acceptSocket();
            if (socket == null) {
                continue;
            }
            
            try {
                handleConnection(socket);
            } catch (Throwable ex) {
                getLogger().log(Level.FINE, 
                           "selectorThread.handleConnectionException",
                           ex);
                try {
                    socket.close();                   
                } catch (IOException ioe){
                    // Do nothing
                }
                continue;
            }
        }         
    }
    
        
    /**
     * Create <code>ProcessorTask</code> objects and configure it to be ready
     * to proceed request.
     */
    protected DefaultProcessorTask newProcessorTask(boolean initialize){                                                      
        ProcessorBlockingTask task = new ProcessorBlockingTask(initialize);
        configureProcessorTask(task);
        task.setMaxKeepAliveRequests(getMaxKeepAliveRequests());
        if (secure){
            task.setSSLImplementation(sslImplementation);
        }
        return task;        
    }

    
    /**
     * Return the <code>ServerSocketFactory</code> used when a blocking IO
     * is enabled.
     */   
    public ServerSocketFactory getServerSocketFactory(){
        return factory;
    }

    
    /**
     * Set the <code>ServerSocketFactory</code> used when a blocking IO
     * is enabled.
     */
    public void setServerSocketFactory(ServerSocketFactory factory){
        this.factory = factory;
    }
    
    
    public void setSecure(boolean secure){
        this.secure = secure;
    }

    
    /**
     * Return the current <code>SSLImplementation</code> this Thread
     */
    public SSLImplementation getSSLImplementation() {
        return sslImplementation;
    }


    /**
     * Set the <code>SSLImplementation</code> used by this thread.It usually
     * means HTTPS will be used.
     */
    public void setSSLImplementation( SSLImplementation sslImplementation) {
        this.sslImplementation = sslImplementation;
    }

    
    // --------------------------------------------------- Not used ----------//
    
    public String[] getEnabledCipherSuites() {
        return null;
    }

    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
    }

    public String[] getEnabledProtocols() {
        return null;
    }

    public void setEnabledProtocols(String[] enabledProtocols) {
    }

    public boolean isClientMode() {
        return false;
    }

    public void setClientMode(boolean clientMode) {
    }

    public boolean isNeedClientAuth() {
        return false;
    }

    public void setNeedClientAuth(boolean needClientAuth) {
    }

    public boolean isWantClientAuth() {
        return false;
    }

    public void setWantClientAuth(boolean wantClientAuth) {
    }
}
