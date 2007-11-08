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

import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.TaskContext;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.TaskListener;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

/**
 * Asynchronous SSL support over NIO. This <code>Task</code> handles the SSL 
 * requests using a non blocking socket. The SSL handshake is done using this 
 * class. Once the handshake is successful, the <code>SSLProcessorTask</code> is 
 * executed. 
 *
 * @author Jean-Francois Arcand
 */
public class SSLAsyncReadTask extends SSLReadTask {  
    
    /**
     * Allocate themandatory <code>ByteBuffer</code>s. Since the ByteBuffer
     * are maintaned on the <code>SSLWorkerThread</code> lazily, this method
     * makes sure the ByteBuffers are properly allocated and configured.
     */
    public void allocateBuffers(){       
        int expectedSize = sslEngine.getSession().getPacketBufferSize();
        if (inputBBSize < expectedSize){
            inputBBSize = expectedSize;
        }

        if (inputBB != null && inputBB.capacity() < inputBBSize) {
            ByteBuffer newBB = ByteBuffer.allocate(inputBBSize);
            inputBB.flip();
            newBB.put(inputBB);
            inputBB = newBB;                                
        } else if (inputBB == null){
            inputBB = ByteBuffer.allocate(inputBBSize);
        }      
        
        outputBB = ByteBuffer.allocate(inputBBSize);       
        if (byteBuffer == null){
            byteBuffer = ByteBuffer.allocate(inputBBSize * 2);
        }

        expectedSize = sslEngine.getSession().getApplicationBufferSize();
        if (expectedSize > byteBuffer.capacity()) {
            ByteBuffer newBB = ByteBuffer.allocate(expectedSize);
            byteBuffer.flip();
            newBB.put(byteBuffer);
            byteBuffer = newBB;
        }      
        outputBB.position(0);
        outputBB.limit(0); 
    }   
    
    
    /**
     * Initialize this object.
     */
    public void initialize(StreamAlgorithm algorithm,
                      boolean useDirectByteBuffer, boolean useByteBufferView){
        type = READ_TASK;    
        this.algorithm = algorithm;       
        inputStream = new SSLAsyncStream();
        
        this.useDirectByteBuffer = useDirectByteBuffer;
        this.useByteBufferView = useByteBufferView; 
    }
   
    
    /**
     * Manage the <code>SelectionKey</code>
     */
    protected void manageKeepAlive(boolean keepAlive,int count, 
            Exception exception){         

        // The key is invalid when the Task has been cancelled.
        if (count == -1 || exception != null){
            if ( exception != null){
                // Make sure we have detached the processorTask
                detachProcessor();
                if (SelectorThread.logger().isLoggable(Level.FINE)){
                    SelectorThread.logger().log
                       (Level.FINE, 
                           "SocketChannel Read Exception:",exception);
                }
            }
            terminate(false);
        }
    }
    
    
    protected boolean process() throws IOException{
        boolean keepAlive = false;     
        SocketChannel socketChannel = (SocketChannel)key.channel();
        Socket socket = socketChannel.socket();
        algorithm.setSocketChannel(socketChannel);    
        inputStream.setSelectionKey(key);
        ((SSLAsyncStream)inputStream).setSslEngine(sslEngine);
        ((SSLAsyncStream)inputStream).setInputBB(inputBB);        
                        
        // Get a processor task. If the processorTask != null, that means we
        // failed to load all the bytes in a single channel.read().
        if (processorTask == null){
            attachProcessor(selectorThread.getProcessorTask());
        } 
        
        // Always true with the NoParsingAlgorithm
        if (algorithm.parse(byteBuffer)){ 
            return executeProcessorTask();
        } else {
            // Never happens with the default StreamAlgorithm
            return true;
        }
    }
  
    
    /**
     * Execute the <code>ProcessorTask</code> 
     * @return false if the request wasn't fully read by the channel.
     *         so we need to respin the key on the Selector.
     */
    public boolean executeProcessorTask() throws IOException{                  
        if (SelectorThread.logger().isLoggable(Level.FINEST))
            SelectorThread.logger().log(Level.FINEST,"executeProcessorTask");
        
        if (algorithm.getHandler() != null && algorithm.getHandler()
                .handle(null,Handler.REQUEST_BUFFERED) == Handler.BREAK){
            return true;
        }
             
        if (taskEvent == null){
            taskContext = new TaskContext();
            taskEvent = new TaskEvent<TaskContext>(taskContext);
        }
        
        // Call the listener and execute the task on it's own pipeline.
        taskEvent.setStatus(TaskEvent.START);        
        taskContext.setInputStream(inputStream);
        taskEvent.attach(taskContext);
        fireTaskEvent(taskEvent); 
        return false;
    }
    
    
    /**
     * Clear the current state and make this object ready for another request.
     */
    public void recycle(){
        byteBuffer = algorithm.postParse(byteBuffer);   
        byteBuffer.clear(); 
        inputStream.recycle();
        algorithm.recycle();
        key = null;
        inputStream.setSelectionKey(null);           
        handshake = true;
        
        inputBB.clear();
        outputBB.clear();
        outputBB.position(0);
        outputBB.limit(0);             
        sslEngine = null;
        detachProcessor();
    }
        
             
    /**
     * Complete the transaction.
     */
    public void terminate(boolean keepAlive){     
        if (processorTask != null && processorTask.isKeepAlive()){
            detachProcessor();        
            registerKey();         
            returnTask();
        } else {
            super.terminate(keepAlive);    
        }
    }
    
    
    /**
     * Set appropriate attribute on the <code>ProcessorTask</code>.
     */
    public void configureProcessorTask(){
        super.configureProcessorTask();
        if ( !getTaskListeners().contains(processorTask) ){
            processorTask.addTaskListener(this);
            addTaskListener((TaskListener)processorTask);
        }
        processorTask.initialize();
        SSLAsyncOutputBuffer outputBuffer = 
                ((SSLAsyncProcessorTask)processorTask).getSSLAsyncOutputBuffer();

        outputBuffer.setSSLEngine(sslEngine);
        outputBuffer.setOutputBB(outputBB);
    } 
}
