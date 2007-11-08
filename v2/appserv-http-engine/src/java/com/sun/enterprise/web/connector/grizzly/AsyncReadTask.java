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
package com.sun.enterprise.web.connector.grizzly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

/**
 * This task execute and then notify its listener, which usualy execute on 
 * their own thread. The task perform the same operation as its parent,
 * but the attached <code>ProcessorTask</code> execute on its own thread.
 *
 * @author Jean-Francois Arcand
 */
public class AsyncReadTask extends DefaultReadTask {
    
    
    public AsyncReadTask(){
        super();
    }

    // ----------------------------------------------------------------------//
    
    public void initialize(StreamAlgorithm algorithm,
                      boolean useDirectByteBuffer, boolean useByteBufferView){
        super.initialize(algorithm,useDirectByteBuffer,useByteBufferView);
        byteBuffer = algorithm.allocate(useDirectByteBuffer,
                useByteBufferView,selectorThread.getBufferSize());
    }
    
    
    /**
     * Read data from the socket and process it using this thread, and only if 
     * the <code>StreamAlgorith</code> stategy determine no more bytes are 
     * are needed.
     */
    public void doTask() throws IOException {  
        doTask(byteBuffer);
    }
    
    
    /**
     * Manage the <code>SelectionKey</code>
     */
    protected void manageKeepAlive(boolean keepAlive,int count, 
            Exception exception){         

        // The key is invalid when the Task has been cancelled.
        if ( count <= 0 || exception != null){
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


    /**
     * Execute the <code>ProcessorTask</code> 
     * @return false if the request wasn't fully read by the channel.
     *         so we need to respin the key on the Selector.
     */
    public boolean executeProcessorTask() throws IOException{                  
        boolean registerKey = false;
        
        if (SelectorThread.logger().isLoggable(Level.FINEST))
            SelectorThread.logger().log(Level.FINEST,"executeProcessorTask");
        
        if (  algorithm.getHandler() != null && algorithm.getHandler()
                .handle(null, Handler.REQUEST_BUFFERED) == Handler.BREAK ){
            return true;
        }
        
        // Get a processor task. If the processorTask != null, that means we
        // failed to load all the bytes in a single channel.read().
        if (processorTask == null){
            attachProcessor(selectorThread.getProcessorTask());
        } else {
            // When spawnProcessorTask is true, we must reconfigure the 
            // ProcessorTask
            configureProcessorTask();
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
    }
    

    /**
     * Set the underlying <code>ByteBuffer</code> used by this class.
     */    
    public void setByteBuffer(ByteBuffer srcBB){
        if ( srcBB != byteBuffer){
            if (srcBB.capacity() > byteBuffer.capacity()) {
                byteBuffer = algorithm.allocate(useDirectByteBuffer,
                                                useByteBufferView,srcBB.capacity()); 
            }
            srcBB.flip();
            byteBuffer.put(srcBB);
            srcBB.clear();
        } else {
            byteBuffer = srcBB;
        }
    }
      
    /**
     * Complete the transaction.
     */
    public void terminate(boolean keepAlive){     
        if (keepAlive){
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
    } 
}
