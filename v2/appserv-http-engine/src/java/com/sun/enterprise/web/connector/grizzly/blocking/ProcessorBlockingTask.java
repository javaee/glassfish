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

import com.sun.enterprise.web.connector.grizzly.Constants;
import com.sun.enterprise.web.connector.grizzly.DefaultProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.TaskContext;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http11.InternalInputBuffer;
import org.apache.coyote.http11.InternalOutputBuffer;
import org.apache.tomcat.util.net.SSLImplementation;

/**
 * Process HTTP request. This class is based on
 * <code>org.apache.coyote.http11.Http11Processor</code>. This class
 * must be used when NIO Blocking is enabled.
 *
 * @author Jean-Francois Arcand
 */
public class ProcessorBlockingTask extends DefaultProcessorTask {

    /**
     * Max keep-alive request before timing out.
     */
    protected int maxKeepAliveRequests = Constants.DEFAULT_MAX_KEEP_ALIVE;    
 
    
    /**
     * The wrapper used to support SSL.
     */
    protected SSLImplementation sslImplementation = null;
   
    // ----------------------------------------------------- Constructor ---- //

    public ProcessorBlockingTask(){
        this(true);
    }
        
    public ProcessorBlockingTask(boolean init){    
        
        type = PROCESSOR_TASK;
        if (init) {
            initialize();
        }
    }

    
    /**
     * Initialize the stream and the buffer used to parse the request.
     */
    public void initialize(){
        started = true;   
        request = new Request();

        response = new Response();
        response.setHook(this);
        
        inputBuffer = new InternalInputBuffer(request,requestBufferSize);
        outputBuffer = new InternalOutputBuffer(response,
                                                maxHttpHeaderSize);
        
        request.setInputBuffer(inputBuffer);
       
        response.setOutputBuffer(outputBuffer);
        request.setResponse(response);

        initializeFilters();

    }


    // ----------------------------------------------------- Thread run ---- //
    
    
     /**
     * Execute the HTTP request by parsing the header/body,
     * and then by delegating the process to the Catalina container.
     */
    public void doTask() throws IOException{
        try {
            process(socket.getInputStream(),socket.getOutputStream());
        } catch(Throwable ex){
            ex.printStackTrace();
            SelectorThread.logger().log(Level.FINE,
                    "processorTask.errorProcessingRequest", ex);
        } finally {
            terminateProcess();        
        }
    }

 
     // --------------------------------------------------------- TaskEvent ---// 
        
     
    public void taskEvent(TaskEvent event){
        if ( event.getStatus() == TaskEvent.START) {
            taskContext = (TaskContext)event.attachement();
            if (  taskEvent == null ) {
                taskEvent = new TaskEvent<TaskContext>();
            }
            
            taskEvent.attach(taskContext);
            execute();
        }
    }

    
    // -------------------------------------------------------------------- //
    
    
    /**
     * Process pipelined HTTP requests using the specified input and output
     * streams.
     * 
     * @param input stream from which the HTTP requests will be read
     * @param output stream which will be used to output the HTTP
     * responses
     * @return true is an error occured.
     * @throws Exception error during an I/O operation
     */
    public boolean process(InputStream input, OutputStream output)
            throws Exception {
        preProcess(input,output);            
        if (sslImplementation != null) {
            sslSupport = sslImplementation.getSSLSupport(socket);
        }        
        doProcess(input,output);
        postProcess(input,output);
        return keepAlive;
    }
    
    
    /**
     * Pre process the request by decoding the request line and the header.
     * @param input the InputStream to read bytes
     * @param output the OutputStream to write bytes
     */     
    public void preProcess(InputStream input, OutputStream output)
                                                            throws Exception {        
        // Make sure this object has been initialized.
        if ( !started ){
            initialize();
        }               
        // Setting up the I/O
        inputBuffer.setInputStream(input);
        outputBuffer.setOutputStream(output);  
        configPreProcess();
    }
    
    
    /**
     * Process an HTTP request using a blocking <code>socket</code>
     * @param input the InputStream to read bytes
     * @param output the OutputStream to write bytes
     */      
    protected boolean doProcess(InputStream input, OutputStream output)
                                                            throws Exception {
        boolean keptAlive = false;    
        while (started && !error && keepAlive) {                
            boolean exitWhile = parseRequest(input,output,keptAlive);
            if (exitWhile) break;                     
            invokeAdapter();            
            postResponse();
        }      
        return true;
    }

    
    /**
     * Parse the request line and the http header.
     */
    public boolean parseRequest(InputStream input, OutputStream output,
            boolean keptAlive) throws Exception {
        boolean exitWhile = super.parseRequest(input,output,keptAlive);  
        
        if (maxKeepAliveRequests > 0 && --keepAliveLeft == 0)
            keepAlive = false;
        return exitWhile;
    }    
    

    /**
     * Notify the <code>TaskListener</code> that the request has been 
     * fully processed.
     */
    public void terminateProcess(){
        taskEvent.setStatus(TaskEvent.COMPLETED);
        fireTaskEvent(taskEvent); 
    }
    
    
    // -------------------------------------------------------------------- //
    

    
    /**
     * Set the maximum number of Keep-Alive requests to honor.
     * This is to safeguard from DoS attacks.  Setting to a negative
     * value disables the check.
     */
    public void setMaxKeepAliveRequests(int maxKeepAliveRequests) {
        this.maxKeepAliveRequests = maxKeepAliveRequests;
    }


    /**
     * Return the number of Keep-Alive requests that we will honor.
     */
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }
    
    
    /**
     * Return the number of blocking keep-alive connection
     */
    public int countBlockingKeepAlive(){
        if (maxKeepAliveRequests == -1) return -1;
                
        return maxKeepAliveRequests - keepAliveLeft; 
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
    
    
    /**
     * Recyle this object.
     */
    public void recycle(){
        socket = null;
        dropConnection = false;
    }
}

