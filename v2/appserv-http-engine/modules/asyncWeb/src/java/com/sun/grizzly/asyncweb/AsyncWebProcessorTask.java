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

import com.sun.enterprise.web.connector.grizzly.ByteBufferInputStream;
import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorFactory;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import com.sun.enterprise.web.connector.grizzly.TaskContext;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.SessionConfig;
import org.apache.mina.common.TransportType;
import org.apache.mina.io.IoFilterChain;
import org.apache.mina.io.IoHandler;
import org.apache.mina.io.IoSession;
import org.safehaus.asyncweb.transport.nio.HttpIOHandler;

/**
 * <code>ProcessorTask</code> that delegates the request/response handling
 * to AsyncWeb's <code>HttpIoHandler</code>.
 *
 * @author Jeanfrancois Arcand
 */
public class AsyncWebProcessorTask extends TaskBase implements ProcessorTask{


    /**
     * The <code>taskContext</code> associated with this object. The 
     * <code>taskContext</code> contains information about the current
     * connection.
     */
    protected TaskContext taskContext;    
    
    
    /**
     * The <code>TaskEvent</code> associated with this task.
     */
    protected TaskEvent<TaskContext> taskEvent;
    
    /**
     * Not used right since AsyncWeb has its own config file
     */
    private int requestBufferSize;
    private boolean dropConnection = false;
    private Handler handler;
    private int maxHttpHeaderSize = 4096;
    private int timeouts;
    
    
    /**
     * Keep-alive flag.
     */
    private boolean keepAlive = true;
    
    
    /**
     * Has erros occured?
     */
    private boolean error = false;
    
    
    /**
     * The Mina IoSession used to bridge AsyncWeb with Grizzly.
     */
    private MinaIoSession ioSession;
    
    
    /**
     * Mina <code>ByteBuffer</code> implementation.
     */
    private ByteBuffer minaByteBuffer;
    
    
    /**
     * Read as much bytes as we can before invoking AsyncWeb to avoid
     * attaching to the SelectionKey.attach().
     */
    private boolean forcedRead = true;
    
    
    /**
     * The AsyncWeb entry point to handle request.
     */
    private HttpIOHandler httpIOHandler;
    
    
    public AsyncWebProcessorTask() {
        super();
    }
    
    
    /**
     * Used when Grizzly ARP is enabled. 
     * XXX Not Yet supported.
     */
    public void doTask() throws IOException {
        try {
            process(taskContext.getInputStream(),
                    taskContext.getOutputStream());
        } catch(Throwable ex){
            SelectorThread.logger().log(Level.FINE,
                    "processorTask.errorProcessingRequest", ex);
        } finally {
            terminateProcess();        
        }        
    }
    
    
    /**
     * Receive notifications from <code>Task</code>
     */
    public void taskEvent(TaskEvent event){
        if ( event.getStatus() == TaskEvent.START) {
            taskContext = (TaskContext)event.attachement();
            if (  taskEvent == null ) {
                taskEvent = new TaskEvent<TaskContext>();
            }
            
            taskEvent.attach(taskContext);
        }
    }
    
    
    /**
     * Initialize the Mina session.
     */
    public void initialize() {
        ioSession = new MinaIoSession();
    }

 
    /**
     * Delegate the request to <code>AsyncWeb</code> by simulating 
     */
    public boolean process(InputStream input, OutputStream output) 
        throws Exception {
          
        ByteBufferInputStream bbInputStream =
                (ByteBufferInputStream)input;
        java.nio.ByteBuffer byteBuffer = bbInputStream.getByteBuffer();
        
        if ( forcedRead ) {
            readAllBytes(byteBuffer);
        }
        byteBuffer.flip();
        
        httpIOHandler.sessionOpened(ioSession);    
        
        // We MUST avoid creating a wrapper on every request, but recycle the 
        // one used the previous transaction.
        minaByteBuffer = ByteBuffer.wrap(byteBuffer);
        httpIOHandler.dataRead(ioSession,minaByteBuffer);

        return keepAlive;
    }

    
    /**
     * Use the temporary <code>Selector</code> to try to loads as much as we
     * can available bytes before delegating the request processing to 
     * <code>AsyncWeb</code>
     */
    private void readAllBytes(java.nio.ByteBuffer byteBuffer) throws IOException{
        int count = 1;
        int byteRead = 0;
        Selector readSelector = null;
        SelectionKey tmpKey = null;

        try{
            SocketChannel socketChannel = (SocketChannel)key.channel();
            while (count > 0){
                count = socketChannel.read(byteBuffer);
                if ( count > 0 )
                    byteRead += count;
            }            

            if ( byteRead == 0 ){
                readSelector = SelectorFactory.getSelector();

                if ( readSelector == null ){
                    return;
                }
                count = 1;
                tmpKey = socketChannel
                        .register(readSelector,SelectionKey.OP_READ);               
                tmpKey.interestOps(tmpKey.interestOps() | SelectionKey.OP_READ);
                int code = readSelector.selectNow();
                tmpKey.interestOps(
                    tmpKey.interestOps() & (~SelectionKey.OP_READ));

                if ( code == 0 ){
                    return;
                }

                while (count > 0){
                    count = socketChannel.read(byteBuffer);
                    if ( count > 0 )
                        byteRead += count;                 
                }
            }
        } finally {
            if (tmpKey != null)
                tmpKey.cancel();

            if ( readSelector != null){
                // Bug 6403933
                try{
                    readSelector.selectNow();
                } catch (IOException ex){
                    ;
                }
                SelectorFactory.returnSelector(readSelector);
            }
        }
    }
    
    
    /**
     * Set the <code>HttpIoHandler</code>.
     */
    public void setIoHandler(HttpIOHandler httpIOHandler){
        this.httpIOHandler = httpIOHandler;
    }
    
    
    /**
     * Stop keep-aliving the connection.
     */
    public void terminateProcess() {
        keepAlive = false;
    }

    
    /**
     * Always called when the connection is closed.
     */
    public void recycle(){
        super.recycle();
        keepAlive = true;        
        
        httpIOHandler.sessionClosed(ioSession);
        ioSession.recycle();
    }
    
    // ----------------------------------------------- Mina IoSession Hook ---/
    
    
    /**
     * Since AsyncWeb is based on MINA, delegate all Mina calls to Grizzly under
     * the hood.
     */
    private class MinaIoSession implements IoSession{
        private HashMap<String,Object> attributes;
        private Object attachment;
        
        public MinaIoSession(){
            attributes = new HashMap<String,Object>();
        }
        
        public IoFilterChain getFilterChain() {
            return null;
        }

        public void write(ByteBuffer byteBuffer, Object object) {
            try{
                // The Object is a marker used by Mina when 
                // writting asynchronously. We might want to re-introduce
                // the WriteTask in Grizzly to simulate the non-blocking 
                // operation.
                
                OutputWriter.flushChannel(
                        (SocketChannel)key.channel(),byteBuffer.buf());

                // Tell AsyncWeb we are done.
                httpIOHandler.dataWritten(this,object);
            } catch (IOException ex){
                SelectorThread.logger().log(Level.WARNING,"IoSession.write",ex);
            }
        }

        public void close() {
            terminateProcess();
        }

        public void close(boolean b) {
            terminateProcess();        
        }

        public Object getAttachment() {
            return attachment;
        }

        public Object setAttachment(Object attachment) {
            Object oldAttachment = this.attachment;
            this.attachment = attachment;
            return oldAttachment;
        }

        public Object getAttribute(String key) {
            return attributes.get(key);
        }

        public Object setAttribute(String key, Object value) {
            return attributes.put(key,value);
        }

        public Object removeAttribute(String key) {
            return attributes.remove(key);
        }

        public Set getAttributeKeys() {
            return attributes.keySet();
        }

        public TransportType getTransportType() {
            return TransportType.SOCKET;
        }

        public boolean isConnected() {
            if (key == null) return false;
            return ((SocketChannel)key.channel()).isConnected();
        }

        public SessionConfig getConfig() {
            return null;
        }

        public SocketAddress getRemoteAddress() {
            if (key == null) return null;
            return ((SocketChannel)key.channel()).socket().getRemoteSocketAddress();
        }

        public SocketAddress getLocalAddress() {
            if (key == null) return null;
            return ((SocketChannel)key.channel()).socket().getLocalSocketAddress();              
        }

        public long getReadBytes() {
            return -1;
        }

        public long getWrittenBytes() {
            return -1;
        }

        public long getWrittenWriteRequests() {
            return -1;        
        }

        public int getScheduledWriteRequests() {
            return -1;       
        }

        public long getCreationTime() {
            return -1;        
        }

        public long getLastIoTime() {
            return -1;        
        }

        public long getLastReadTime() {
            return -1;        
        }

        public long getLastWriteTime() {
            return -1;        
        }

        public boolean isIdle(IdleStatus idleStatus) {
            return false;
        }

        public int getIdleCount(IdleStatus idleStatus) {
            return -1;
        }

        public long getLastIdleTime(IdleStatus idleStatus) {
            return -1;
        }

        public IoHandler getHandler() {
            return null;
        }
        
        public void recycle(){
            attributes.clear();
            attachment = null;
        }
    }
    
       
    // ------------------------------------------------------ Setter/Getter ---/
    
    
    public void setBufferSize(int requestBufferSize) {
        this.requestBufferSize= requestBufferSize;
    }

    
    public void setDropConnection(boolean dropConnection) {
        this.dropConnection = dropConnection;
    }

    
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    
    public Handler getHandler() {
        return handler;
    }

    
    public void setMaxHttpHeaderSize(int maxHttpHeaderSize) {
        this.maxHttpHeaderSize = maxHttpHeaderSize;
    }

    public void setTimeout(int timeouts) {
        this.timeouts = timeouts;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    
    public boolean isError() {
        return error;
    }

    
    public int getBufferSize() {
        return requestBufferSize;
    }

    public boolean getDropConnection() {
        return dropConnection;
    }

    public void setSocket(Socket socket) {
    }
    
    // ----------------------------------------- Not used -----------------//

    public int getMaxPostSize() {
         throw new UnsupportedOperationException();
    }  
    
    
    public void setMaxPostSize(int mps) {
         throw new UnsupportedOperationException();
    }

    public String getRequestURI() {
        throw new UnsupportedOperationException();
    }

    
    public long getWorkerThreadID() {
        throw new UnsupportedOperationException();
    }

    
    public void invokeAdapter() {
         throw new UnsupportedOperationException();
    }

    public void parseRequest() throws Exception {
         throw new UnsupportedOperationException();
    }

    public boolean parseRequest(InputStream input, OutputStream output, 
            boolean keptAlive) throws Exception {
         throw new UnsupportedOperationException();
    }

    public void postProcess() throws Exception {
         throw new UnsupportedOperationException();
    }

    public void postProcess(InputStream input, OutputStream output) 
        throws Exception {
         throw new UnsupportedOperationException();
    }

    public void postResponse() throws Exception {
         throw new UnsupportedOperationException();
    }

    public void preProcess() throws Exception {
         throw new UnsupportedOperationException();
    }

    public void preProcess(InputStream input, OutputStream output) 
        throws Exception {
         throw new UnsupportedOperationException();
    }    

    public boolean parseRequest(
            AbstractSelectableChannel abstractSelectableChannel, boolean b) 
                throws Exception {
        return false;
    }

    public void postProcess(AbstractSelectableChannel abstractSelectableChannel) throws Exception {
    }

    public void preProcess(AbstractSelectableChannel abstractSelectableChannel) throws Exception {
    }

    public boolean process(AbstractSelectableChannel abstractSelectableChannel)
        throws Exception {
        return false;
    }

}
