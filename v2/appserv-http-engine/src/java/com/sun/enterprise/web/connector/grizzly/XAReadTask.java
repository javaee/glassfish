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

import com.sun.enterprise.web.connector.grizzly.DefaultReadTask;
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
public class XAReadTask extends DefaultReadTask {
    
    /**
     * If the last request was processed successfully and we need to
     * keep-alive the connection, unattach thsi object from the
     * <code>SelectionKey</code> and return it to the pool.
     */
    protected boolean inKeepAliveProcess = false;


    /**
     * Read data from the socket and process it using this thread, and only if
     * the <code>StreamAlgorith</code> stategy determine no more bytes are
     * are needed.
     */
    public void doTask() throws IOException {
        int count = 0;
        Socket socket = null;
        SocketChannel socketChannel = null;
        boolean keepAlive = false;
        Exception exception = null;
        key.attach(null);

        if ( byteBuffer == null ){
            byteBuffer = algorithm
                    .allocate(useDirectByteBuffer,useByteBufferView
                              ,selectorThread.getBufferSize());
        }
        
        try {
            inKeepAliveProcess = true;
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
                        inKeepAliveProcess = false;
                        break;
                    }
                } else {
                    // We must call the Selector since we don't have all the
                    // bytes
                    keepAlive = true;
                    // We should not detach this task from the SelectionKey
                    // since the bytes weren't all read.
                    inKeepAliveProcess = false;
                    break;
                }
                
                // If the content-length is found, and if it higher than
                // maxPostSize, cancel the task to avoid DoS.
                if ( algorithm.contentLength() > maxPostSize ){
                    cancelTask("Maximum POST size reached: " + maxPostSize,
                            HtmlHelper.OK);
                    keepAlive = false;
                    break;
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
    
    
    /*
     * Two scenarios:
     *  - inKeepAliveProcess = false
     *    the bytes aren't fully read, so we must register again
     *  - inKeepAliveProcess = true
     *    we keep-alive the key, but unattach this object from
     *    the SelectionKey so it can be resused.
     */
    protected void manageKeepAlive(boolean keepAlive,int count,
            Exception exception){
        
        // The key is invalid when the Task has been cancelled.
        if ( count == -1 || !key.isValid() || exception != null ){
            inKeepAliveProcess = false;
            keepAlive = false;
            
            if ( exception != null){
                // Make sure we have detached the processorTask
                detachProcessor();
                SelectorThread.logger().log(Level.FINEST, 
                        "SocketChannel Read Exception: ",exception);
            }
        }
        
        final boolean attached = !inKeepAliveProcess;
        if (keepAlive) {
            // (1) First remove the attachement.
            if ( inKeepAliveProcess ) {
                key.attach(null);
            } else {
                key.attach(this);
            }
            
            // (2) Register the key
            registerKey();
            
            // We must return since the key has been registered and this
            // task can be reused.
            if ( attached ) return;
          
            // (3) Return that task to the pool.
            if ( inKeepAliveProcess ) {
                terminate(keepAlive);
            }
        } else {
            terminate(keepAlive);
        }
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
            SelectorThread.logger().log(Level.SEVERE,"readTask.processException", e);
            registerKey = true;
        } finally {
            // if registerKey, that means we were'nt able to parse the request
            // properly because the bytes were not all read, so we need to
            // call again the Selector.
            if (registerKey && processorTask.isError()) {
                byteBuffer = algorithm.rollbackParseState(byteBuffer);
                inKeepAliveProcess = false;
                return registerKey;
            }
        }

        detachProcessor();
        return registerKey;
    }
    
    
    /**
     * Complete the processing.
     */
    public void terminate(boolean keepAlive){     
        super.terminate(keepAlive);
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
    
    
}
