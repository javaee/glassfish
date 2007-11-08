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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

/**
 * Read available data on a non blocking <code>SocketChannel</code>. 
 * <code>StreamAlgorithm</code> stategy will decide if more bytes are required
 * or not. Once the <code>StreamAlgorithm</code> is ready, the 
 * <code>ProcessorTask</code> attached to this class will be executed.
 *
 * @author Scott Oaks
 * @author Jean-Francois Arcand
 */
public class DefaultReadTask extends TaskBase implements ReadTask {
    
    /**
     * The time in milliseconds before this <code>Task</code> can stay idle.
     * This is only used when the SelectionKey.attach() is used.
     */
    private long idleTime = 30 * 1000; // 30 seconds

    
    /**
     * The <code>TaskContext</code> instance associated with this object.
     * The <code>TaskContext</code> is initialized at startup and then recycled.
     */
    protected TaskContext taskContext;
    
    
    /**
     * The <code>TaskEvent</code> instance used by this object to notify its 
     * listeners
     */
    protected TaskEvent taskEvent;
    
    
    /**
     * The <code>ByteBuffer</code> used by this task to buffer the request
     * stream.
     */
    protected ByteBuffer byteBuffer;   
    
    
    /**
     * The <code>ProcessorTask</code> used by this class.
     */
    protected ProcessorTask processorTask;
  
    
    /**
     * Max post size.
     */
    protected int maxPostSize = 25 * 1024 * 1024;
     
    
    /**
     * The recycled <code>OutputStream</code> used by this buffer.
     */
    protected ByteBufferInputStream inputStream;


    /**
     * The <code>Algorithm</code> used to parse the request and determine
     * of the bytes has been all read from the <code>SocketChannel</code>
     */
    protected StreamAlgorithm algorithm;
    
    
    /**
     * <code>true</code> only when another object has already read bytes
     * from the channel.
     */
    protected boolean bytesAvailable = false;

    
    /**
     * Is the <code>ByteBuffer</code> used by the <code>ReadTask</code> use
     * direct <code>ByteBuffer</code> or not.
     */
    protected boolean useDirectByteBuffer = true;
    
      
    /**
     * Create view <code>ByteBuffer</code> from another <code>ByteBuffer</code>
     */    
    protected boolean useByteBufferView = false;


    // ----------------------------------------------------- Constructor ----/

    
    public DefaultReadTask(){
        ;//
    }
    
    
    public void initialize(StreamAlgorithm algorithm,
                      boolean useDirectByteBuffer, boolean useByteBufferView){
        type = READ_TASK;    
        this.algorithm = algorithm;       
        inputStream = new ByteBufferInputStream();
        
        this.useDirectByteBuffer = useDirectByteBuffer;
        this.useByteBufferView = useByteBufferView;
    }
    
    
    /**
     * Force this task to always use the same <code>ProcessorTask</code> instance.
     */
    public void attachProcessor(ProcessorTask processorTask){
        this.processorTask = processorTask;        
        configureProcessorTask();
    }  
    
    
    /**
     * Set appropriate attribute on the <code>ProcessorTask</code>.
     */
    protected void configureProcessorTask(){
        // Disable blocking keep-alive mechanism. Keep-Alive mechanism
        // will be managed by this class instead.
        processorTask.setSelectionKey(key);
        processorTask.setSocket(((SocketChannel)key.channel()).socket());
        processorTask.setHandler(algorithm.getHandler());
    }  
    
      
    /**
     * Return the <code>ProcessorTask</code> to the pool.
     */
    public void detachProcessor(){
        if (processorTask != null){
            processorTask.recycle();           
        }
        
        // Notify listeners
        if ( listeners != null ) {
            for (int i=listeners.size()-1; i > -1; i--){
                if ( taskEvent == null ) {
                    taskEvent = new TaskEvent<ReadTask>();
                }
                taskEvent.attach(this);            
                taskEvent.setStatus(TaskEvent.COMPLETED);           
                listeners.get(i).taskEvent(taskEvent);
            }                
            clearTaskListeners();
        }
        
        if (recycle && processorTask != null){
            selectorThread.returnTask(processorTask);
            processorTask = null;
        }
    }
    
    
    /**
     * Read data from the socket and process it using this thread, and only if 
     * the <code>StreamAlgorith</code> stategy determine no more bytes are 
     * are needed.
     */
    public void doTask() throws IOException {   
        if ( byteBuffer == null) {
            WorkerThread workerThread = (WorkerThread)Thread.currentThread();
            byteBuffer = workerThread.getByteBuffer();

            if ( workerThread.getByteBuffer() == null){
                byteBuffer = algorithm.allocate(useDirectByteBuffer,
                        useByteBufferView,selectorThread.getBufferSize());
                workerThread.setByteBuffer(byteBuffer);
            }
        }
        doTask(byteBuffer);
    }
                 
    
    /**
     * Pull data from the socket and store it inside the <code>ByteBuffer</code>.
     * The <code>StreamAlgorithM</code> implementation will take care of
     * determining if we need to register again to the main <code>Selector</code>
     * or execute the request using temporary <code>Selector</code>
     *
     * @param byteBuffer
     */               
    protected void doTask(ByteBuffer byteBuffer){
        int count = 0;
        Socket socket = null;
        SocketChannel socketChannel = null;
        boolean keepAlive = false;
        Exception exception = null;
        key.attach(null);
     
        try {
            socketChannel = (SocketChannel)key.channel();
            socket = socketChannel.socket();
            algorithm.setSocketChannel(socketChannel);            
           
            int loop = 0;
            int bufferSize = 0;
            while ( socketChannel.isOpen() && (bytesAvailable || 
                    ((count = socketChannel.read(byteBuffer))> -1))){

                // Avoid calling the Selector.
                if ( count == 0 && !bytesAvailable){
                    loop++;
                    if (loop > 2){
                        break;
                    }
                    continue;
                }
                if (bytesAvailable){
                    count = byteBuffer.position();
                }
                bytesAvailable = false;
                
                byteBuffer = algorithm.preParse(byteBuffer);
                inputStream.setByteBuffer(byteBuffer);
                inputStream.setSelectionKey(key);
                
                // try to predict which HTTP method we are processing
                if ( algorithm.parse(byteBuffer) ){ 
                    keepAlive = executeProcessorTask();
                    if (!keepAlive) {
                        break;
                    }
                } else {
                    // We must call the Selector since we don't have all the 
                    // bytes
                    keepAlive = true;
                }
            } 
        // Catch IO AND NIO exception
        } catch (IOException ex) {
            exception = ex;
        } catch (RuntimeException ex) {
            exception = ex;    
        } finally {                   
            manageKeepAlive(keepAlive,count,exception);
        }
    }


    /**
     * Evaluate if the <code>SelectionKey</code> needs to be registered to 
     * the main <code>Selector</code>
     */
    protected void manageKeepAlive(boolean keepAlive,int count, 
            Exception exception){         

        // The key is invalid when the Task has been cancelled.
        if ( count == -1 || !key.isValid() || exception != null ){
            keepAlive = false;
            
            if ( exception != null){
                // Make sure we have detached the processorTask
                detachProcessor();
                if (SelectorThread.logger().isLoggable(Level.FINE)){
                    SelectorThread.logger().log
                       (Level.FINE, 
                           "SocketChannel Read Exception:",exception);
                }
            }
        }

        if (keepAlive) {    
            registerKey(); 
        } 
            
        terminate(keepAlive);
    }
 
    
    /**
     * Execute the <code>ProcessorTask</code> only if the request has
     * been fully read. Guest the size of the request by using the 
     * content-type HTTP headers.
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
        }
        
        try {           
            // The socket might not have been read entirely and the parsing
            // will fail, so we need to respin another event.
            registerKey = processorTask.process(inputStream,null);           
        } catch (Exception e) {
            SelectorThread.logger()
                .log(Level.SEVERE,"readTask.processException", e);
        } 
        detachProcessor();
        return registerKey;
    }

    
    /**
     * Return this object to the pool
     */
    protected void returnTask(){
        if (recycle) {
            recycle();
            selectorThread.returnTask(this);
        } 
    }
    

    public void taskEvent(TaskEvent event){
        if (event.getStatus() == TaskEvent.COMPLETED 
                || event.getStatus() == TaskEvent.ERROR){
            terminate(processorTask.isKeepAlive());
        }  
    }
    

    /**
     * Complete the processing.
     */
    public void terminate(boolean keepAlive){          
        if ( !keepAlive ){
            finishConnection();
        }
        returnTask();
    }
    
    
    /**
     * Clear the current state and make this object ready for another request.
     */
    public void recycle(){
        if (byteBuffer != null){ 
            try{
                final WorkerThread workerThread = 
                        (WorkerThread)Thread.currentThread();   
                if (workerThread.getByteBuffer() == null){
                    workerThread.setByteBuffer(byteBuffer);
                }
            } catch (ClassCastException ex){
                // Avoid failling if the Grizzly extension doesn't support
                // the WorkerThread interface.
                if (SelectorThread.logger().isLoggable(Level.FINEST))
                    SelectorThread.logger().log(Level.FINEST,"recycle",ex);                
            } finally{
                byteBuffer = algorithm.postParse(byteBuffer);   
                byteBuffer.clear();                 
            }
        }
        inputStream.recycle();
        algorithm.recycle();
        key = null;
        inputStream.setSelectionKey(null);       
        byteBuffer = null;
    }
    
    // -------------------------------------------------------- TaskEvent ---// 

       
    /**
     * Cancel the <code>SelectionKey</code> and close its underlying 
     * <code>SocketChannel</code>. Add this <code>Task</code> to the Keep-Alive
     * sub-system.
     */
    protected void finishConnection(){
        
        if (SelectorThread.logger().isLoggable(Level.FINEST))
            SelectorThread.logger().log(Level.FINEST,"finishConnection"); 
        
        try{
            if (taskContext != null){
                taskContext.recycle();
            }
        } catch (IOException ioe){
            ;
        }
        
        selectorThread.cancelKey(key);
    }

    
    /**
     * Register the <code>SelectionKey</code> with the <code>Selector</code>
     */
    public void registerKey(){
        if (key.isValid()){            
            if (SelectorThread.logger().isLoggable(Level.FINEST))
                SelectorThread.logger().log(Level.FINEST,"registerKey");           

            selectorThread.registerKey(key);
        } 
    }
    
    
    // -------------------------------------------------------- getter/setter--/
    

    /**
     * Return the associated <code>ProcessorTask</code>.
     * @return the associated <code>ProcessorTask</code>, null if not used.
     */
    public ProcessorTask getProcessorTask(){
        return processorTask;
    }
    
    
    /**
     * Return the underlying <code>ByteBuffer</code> used by this class.
     */
    public ByteBuffer getByteBuffer(){
        if ( byteBuffer == null) {
            byteBuffer = algorithm.allocate(useDirectByteBuffer,
                    useByteBufferView,selectorThread.getBufferSize());
        }
        return byteBuffer;
    }

    
    /**
     * Set the underlying <code>ByteBuffer</code> used by this class.
     */    
    public void setByteBuffer(ByteBuffer byteBuffer){
        this.byteBuffer = byteBuffer;
    }
    
    
    /**
     * If the attached byteBuffer was already filled, tell the
     * Algorithm to re-use the bytes.
     */ 
    public void setBytesAvailable(boolean bytesAvailable){
        this.bytesAvailable = bytesAvailable;
    }

    
    
    /**
     * Set the time in milliseconds this <code>Task</code> is allowed to be idle.
     */
    public void setIdleTime(long idleTime){
        this.idleTime = idleTime;
    }
    
    
    /**
     * Return the time in milliseconds this <code>Task</code> is allowed to be idle.
     */
    public long getIdleTime(){
        return idleTime;
    } 
}
