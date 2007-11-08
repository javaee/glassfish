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
package com.sun.enterprise.web.connector.grizzly.ssl;

import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.Task;
import com.sun.enterprise.web.connector.grizzly.WorkerThreadImpl;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import javax.net.ssl.SSLEngine;

/**
 * Simple <code>WorkerThread</code> used to execute SSL over NIO requests. All
 * <code>ByteBuffer</code> used by this class MUST be created by client
 * of this classes.
 *
 * @author Jean-Francois Arcand
 */
public class SSLWorkerThread extends WorkerThreadImpl{

    /**
     * The encrypted ByteBuffer used for handshaking and reading request bytes.
     */
    private ByteBuffer inputBB;
    

    /**
     * The encrypted ByteBuffer used for handshaking and writing response bytes.
     */    
    private ByteBuffer outputBB;

    
    /**
     * The <code>SSLEngine</code> used to manage the SSL over NIO request.
     */
    private SSLEngine sslEngine;

    
    /** 
     * Create a Thread that will synchronizes/block on 
     * <code>SSLPipeline</code> instance.
     */
    public SSLWorkerThread(ThreadGroup threadGroup, Runnable runnable){
        super(threadGroup, runnable);    
    }    
    
    
    /** 
     * Create a Thread that will synchronizes/block on 
     * <code>SSLPipeline</code> instance.
     */
    public SSLWorkerThread(Pipeline pipeline, String name){
        super(pipeline, name);  
    }
    
    /**
     * Perform a <code>Task</code> processing.
     */
    public void run(){

        if ( target != null ){
            target.run();
            return;
        }
        
        while (doTask) {
            try{
                // Wait for a Task to be added to the pipeline.
                Task t = pipeline.getTask();
                if (t != null){
                    t.run();                
                    t = null;
                }
            } catch (Throwable t) {         
                // Make sure we aren't leaving any bytes after an exception.
                if (byteBuffer != null){
                    byteBuffer.clear();
                }
                if (inputBB != null){
                    inputBB.clear();
                }
                if (outputBB != null){
                    outputBB.clear();
                }     
                SelectorThread.logger().log(Level.FINE,
                        "workerThread.httpException",t);
            } finally {
                sslEngine = null;
            }
        }
    }

    
    /**
     * Return the encrypted <code>ByteBuffer</code> used to handle request.
     */
    public ByteBuffer getInputBB(){
        return inputBB;
    }
    
    
    /**
     * Set the encrypted <code>ByteBuffer</code> used to handle request.
     */    
    public void setInputBB(ByteBuffer inputBB){
        this.inputBB = inputBB;
    }
 
    
    /**
     * Return the encrypted <code>ByteBuffer</code> used to handle response.
     */    
    public ByteBuffer getOutputBB(){
        return outputBB;
    }
    
    
    /**
     * Set the encrypted <code>ByteBuffer</code> used to handle response.
     */   
    public void setOutputBB(ByteBuffer outputBB){
        this.outputBB = outputBB;
    }
    
         
    /**
     * Set the <code>SSLEngine</code>.
     */
    public SSLEngine getSSLEngine() {
        return sslEngine;
    }

        
    /**
     * Get the <code>SSLEngine</code>.
     */
    public void setSSLEngine(SSLEngine sslEngine) {
        this.sslEngine = sslEngine;
    }
}

