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

package com.sun.enterprise.web.connector.grizzly.comet;

import com.sun.enterprise.web.connector.grizzly.DefaultReadTask;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.WorkerThread;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

/**
 * A <code>Task</code> implementation that allow Grizzly ARP to notify
 * <code>CometHandler</code> when new data (bytes) are available from the 
 * <code>CometSelector</code>.
 *
 * @author Jeanfrancois Arcand
 */
public class CometTask extends TaskBase{
    
    public enum OP_EVENT { READ, WRITE }
    
   
    /**
     * The current non blocking operation.
     */
    protected OP_EVENT upcoming_op = OP_EVENT.READ;
    
    
    /**
     * The <code>CometContext</code> associated with this instance.
     */
    private CometContext cometContext;
    
    
    /**
     * The <code>SelectionKey</code>.
     */
    private SelectionKey key;     
    
    
    /**
     * The <code>CometSelector</code> .
     */
    private CometSelector cometSelector;
    
    
    /**
     * The time in milliseconds before this object was registered the 
     * <code>SelectionKey</code> on the <code>CometSelector</code>
     */
    private long expireTime = 0L;


    /**
     * The delay before interrupting the polled request and cancelling 
     * the <code>SelectionKey</code>.
     */
    private long expirationDelay = 30 * 1000;
    
    
    /**
     * The InputStream used to read bytes from the <code>CometSelector</code>
     */
    private CometInputStream cometInputStream;
    
    
    private ByteBuffer byteBuffer;
    
    
    private SelectionKey cometKey;
    

    /**
     * <tt>true</tt> if socketChannel.read no longer produces data (-1) or throw
     * an IOException.
     */
    private boolean connectionClosed;
   
    
    /**
     * The CometSelector registered key.
     */
    private SelectionKey cometKey;
    

    /**
     * <tt>true</tt> if socketChannel.read no longer produces data (-1) or throw
     * an IOException.
     */
    private boolean connectionClosed;
    
    
    /**
     * The <code>CometEvent</code> associated with this task.
     */
    private CometEvent event;
        
    
    /**
     * The CometWriter associated with this task.
     */
    private CometWriter writer;
    
    
    /**
     * The CometReader associated with this task.
     */
    private CometReader reader;
    
    
    /**
     * <tt>true</tt> if the CometHandler has been registered for OP_READ 
     * events.
     */
    private boolean asyncReadSupported = false;
    
    
    /**
     * New <code>CometTask</code>.
     */
    public CometTask() {  
    }
    
    
    /**
     * Notify the <code>CometHandler</code> that bytes are available for read.
     * The notification will invoke all <code>CometContext</code>
     */
    public void doTask() throws IOException{     
        /**
         * The CometHandler in that case is **always** invoked using this
         * thread so we can re-use the Thread attribute safely.
         */
        ByteBuffer byteBuffer = 
                ((WorkerThread)Thread.currentThread()).getByteBuffer();

        boolean clearBuffer = true;
        try{
            if (cometKey == null || key == null) return;
            
            if (cometInputStream == null){
                cometInputStream = new CometInputStream();
            }
            
            if (event == null){
                event = new CometEvent();
            }
            
            connectionClosed = false;
            cometInputStream.setSelectionKey(cometKey);            
            if (byteBuffer == null){
                byteBuffer = ByteBuffer.allocate(selectorThread.getBufferSize());
                ((WorkerThread)Thread.currentThread()).setByteBuffer(byteBuffer);
            } else {
                byteBuffer.clear();
            }

            cometInputStream.setByteBuffer(byteBuffer);            
            SocketChannel socketChannel = (SocketChannel)cometKey.channel();
            if (upcoming_op == OP_EVENT.READ){      
                event.type = CometEvent.READ;
                int nRead = 0;
                /*
                 * We must execute the first read to prevent client abort.
                 */
                if ( (nRead = socketChannel.read(byteBuffer)) == -1){
                    connectionClosed = true;
                } else {
                    /* 
                     * This is an HTTP pipelined request. We need to resume
                     * the continuation and invoke the http parsing 
                     * request code.
                     */
                    if (!asyncReadSupported){
                        // Don't let the main Selector (SelectorThread) starts
                        // handling the pipelined request.
                        selectorThread.addBannedSelectionKey(key);
                        
                        // Resume the previous request.
                        CometHandler cometHandler = 
                                cometContext.getCometHandler(key);
                        
                        /**
                         * Something when wrong, most probably the CometHandler
                         * has been resumed or removed by the Comet implementation.
                         */
                        if (cometHandler == null){
                            return;
                        }
                        
                        cometContext.resumeCometHandler(cometHandler, false);
                        clearBuffer = false;
                        
                        DefaultReadTask readTask = 
                                (DefaultReadTask)selectorThread.getReadTask(key);

                        readTask.setByteBuffer(byteBuffer);
                        readTask.setBytesAvailable(true);
                        // Re-use the same Thread.
                        readTask.doTask();
                    } else {
                        byteBuffer.flip(); 
                        reader = new CometReader();
                        reader.setNRead(nRead);
                        reader.setByteBuffer(byteBuffer);
                        event.attach(reader);
                        cometContext.notify(event,CometEvent.READ,key); 
                        reader.setByteBuffer(null);
                        
                        // This Reader is now invalid. Any attempt to use
                        // it will results in an IllegalStateException.
                        reader.setReady(false);
                    }
                }
            } else if (upcoming_op == OP_EVENT.WRITE){  
                event.type = CometEvent.WRITE;
                writer = new CometWriter();
                writer.setChannel(socketChannel);
                event.attach(writer);
                cometContext.notify(event,CometEvent.WRITE,key);  
                        
                // This Writer is now invalid. Any attempt to use
                // it will results in an IllegalStateException.                
                writer.setReady(false);
           }
        } catch (IOException ex){
            connectionClosed = true;
            // Bug 6403933 & GlassFish 2013
            if (selectorThread.logger().isLoggable(Level.FINEST)){
                selectorThread.logger().log(Level.FINEST,"Comet exception",ex);
            }
        } catch (Throwable t){
            selectorThread.logger().log(Level.SEVERE,"Comet exception",t);
            connectionClosed = true;
        } finally {   
            // Bug 6403933
            if (connectionClosed){
                cometSelector.cancelKey(cometKey);
            }
            
            if (clearBuffer){
                byteBuffer.clear();
            }
            asyncReadSupported = false;
        }
    }
    
    
    /**
     * Not used.
     */
    public void taskEvent(TaskEvent event) {
    }

    
    /**
     * Return the <code>CometContext</code> associated with this instance.
     * @return CometContext the <code>CometContext</code> associated with this 
     *         instance.
     */
    public CometContext getCometContext() {
        return cometContext;
    }
    
    
    /**
     * Set the <code>CometContext</code> used to notify <code>CometHandler</code>.
     * @param cometContext the <code>CometContext</code> used to notify <code>CometHandler</code>
     */
    public void setCometContext(CometContext cometContext) {
        this.cometContext = cometContext;
    }

    
    /**
     * Return the <code>SelectionKey</code>
     * @return SelectionKey <code>SelectionKey</code>
     */
    public SelectionKey getSelectionKey() {
        return key;
    }

    
    /**
     * Set the <code>SelectionKey</code>
     * @param SelectionKey <code>SelectionKey</code>
     */    
    public void setSelectionKey(SelectionKey key) {
        this.key = key;
    }
    
    
    /**
     * Recycle this object.
     */
    public void recycle(){
        super.recycle();
        key = null;
        cometContext = null;
        asyncReadSupported = false;
        if(cometInputStream != null) {
            cometInputStream.recycle();
        }
    }

    
    /**
     * Return the <code>CometSelector</code>
     * @return CometSelector the <code>CometSelector</code>
     */
    public CometSelector getCometSelector() {
        return cometSelector;
    }

    
    /**
     * Set the <code>CometSelector</code>
     * @param cometSelector the <code>CometSelector</code>
     */   
    public void setCometSelector(CometSelector cometSelector) {
        this.cometSelector = cometSelector;
    }
    
    
    /**
     * Return the time in milliseconds before this object was registered the 
     * <code>SelectionKey</code> on the <code>CometSelector</code>
     * @return long Return the time in milliseconds before this object was
     *         registered the <code>SelectionKey</code> on the
     *         <code>CometSelector</code>
     */
    public long getExpireTime() {
        return expireTime;
    }

    
    /**
     * Set the time in milliseconds before this object was registered the 
     * <code>SelectionKey</code> on the <code>CometSelector</code>
     * @param expireTime Return the time in milliseconds before this object was
     *                   registered the <code>SelectionKey</code> on the
     *                   <code>CometSelector</code>
     */   
    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
    
    
    /**
     * Return the delay before interrupting the polled request and cancelling 
     * the <code>SelectionKey</code>.
     * @return long Return the delay before interrupting the polled request and cancelling 
     *              the <code>SelectionKey</code>.
     */
    public long getExpirationDelay() {
        return expirationDelay;
    }

    
    /**
     * Set the delay before interrupting the polled request and cancelling 
     * the <code>SelectionKey</code>.
     * @param expirationDelay Return the delay before interrupting the polled 
     *                        request and cancelling 
     *                        the <code>SelectionKey</code>.
     */    
    public void setExpirationDelay(long expirationDelay) {
        this.expirationDelay = expirationDelay;
    }

    
    /**
     * Return the <code>CometSelector</code>'s <code>SelectionKey</code>.
     */
    public SelectionKey getCometKey() {
        return cometKey;
    }

    
    /**
     * Set the <code>CometSelector</code>'s <code>SelectionKey</code>.
     */
    public void setCometKey(SelectionKey cometKey) {
        this.cometKey = cometKey;
    }

    
    public boolean isAsyncReadSupported() {
        return asyncReadSupported;
    }

    
    public void setAsyncReadSupported(boolean asyncReadSupported) {
        this.asyncReadSupported = asyncReadSupported;
    }
}
