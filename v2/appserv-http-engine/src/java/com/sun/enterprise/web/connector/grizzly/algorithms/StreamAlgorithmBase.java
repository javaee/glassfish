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

package com.sun.enterprise.web.connector.grizzly.algorithms;

import com.sun.enterprise.web.connector.grizzly.Constants;
import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.ByteBufferFactory;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;


/**
 * Base class for <code>StreamAlgorithm</code> implementation.
 * @author Jeanfrancois Arcand
 */
public abstract class StreamAlgorithmBase implements StreamAlgorithm{
 
    /**
     * Is Grizzly embedded in GlassFish.
     */
    protected static boolean embeddedInGlassFish = false;
    
    
    // Temporary hack to find if Grizzly is enbedded in GlassFish or not.
    static{
        try{
            embeddedInGlassFish = 
                (Class.forName("org.apache.coyote.tomcat5.Constants") != null);
        } catch(Exception ex){
            ; //Swallow
        }
    }
    
    
    private int port = 8080;
    
    /**
     * Manipulate the bytes stream and determine if the process can continue.
     *    
     * @return true if the algorithm determines the process can continue.
     */    
    public abstract boolean parse(ByteBuffer byteBuffer);
    
    
    /**
     * The actual length of the stream we are able to read.
     */
    protected int contentLength = -1;
    
           
    /**
     * The <code>ByteBuffer</code> current limit value
     */    
    protected int curLimit = -1;
    
    
    /**
     * The <code>ByteBuffer</code> current position value
     */    
    protected int curPosition = -1;
    
    
    /**
     * The position, within the <code>ByteBuffer</code>, when the HTTP
     * headers are completely reads.
     */
    protected int headerLength = -1;
    
    
    /**
     * In case we were'nt able to parse the request line, we will continue
     * parsing from that position.
     */
    protected int lastStatePosition = -1;    
   
    
    /**
     * If the stream wasn't read fully, keep the state of the http parsing.
     */
    protected int state = 0;
    
   
    /**
     * If a new <code>ByteBuffer</code> is created because the stream is 
     * too small, cache the original byteBuffer and reuse it once the 
     * transaction has completed.
     */
    protected ByteBuffer primaryByteBuffer = null;
    
    
    /**
     * If <code>true</code>, use a <code>ByteBuffer</code> view instead of 
     * <code>ByteBuffer</code>
     */    
    protected boolean useByteBufferView = false;
   
    
    /**
     * Are we using direct <code>ByteBuffer</code>
     */
    protected boolean useDirectByteBuffer;
    
    
    /**
     * The <code>SocketChannel</code> associated with this algorithm.
     */
    protected SocketChannel socketChannel;
    
    
    /**
     * An <code>Handler</code> implementation used to implement a 
     * static resources cache.
     */
    protected Handler handler;

    
    public StreamAlgorithmBase() {
        handler = new DummyHandler();
    }
    
    // ------------------------------------------------------- Methods ------//
    
    
    /**
     * Return the stream content-length. If the content-length wasn't parsed,
     * return -1.
     */
    public int contentLength(){
        return contentLength;
    }
    
    
    /**
     * Return the stream header length. The header length is the length between
     * the start of the stream and the first occurance of character '\r\n' .
     */
    public int headerLength(){
        return headerLength;
    }
    
    
    /**
     * Before parsing the bytes, initialize and prepare the algorithm.
     * @param byteBuffer the <code>ByteBuffer</code> used by this algorithm
     * @return <code>ByteBuffer</code> used by this algorithm
     */
    public ByteBuffer preParse(ByteBuffer byteBuffer){
        if (byteBuffer.position() == byteBuffer.capacity()){
            // Add space at the end for \n\r
            int bufferSize = contentLength > 0 ?
                                    contentLength + headerLength + 5: 
                                    byteBuffer.capacity() * 2;
            byteBuffer = swapBuffer(byteBuffer,bufferSize);
        }  
        return byteBuffer;
    }
    
    /**
     * After parsing the bytes, post process the <code>ByteBuffer</code> 
     * @param byteBuffer the <code>ByteBuffer</code> used by this algorithm
     * @return <code>ByteBuffer</code> used by this algorithm
     */
    public ByteBuffer postParse(ByteBuffer byteBuffer){
        // Swap buffer to its original size.
        if ( primaryByteBuffer != null) {
            primaryByteBuffer.clear();
            byteBuffer = primaryByteBuffer;
            primaryByteBuffer = null;
        }
        return byteBuffer;
    }

    
    /**
     * Reset this object to its default state.
     */
    public void recycle(){
        contentLength = -1;
        lastStatePosition= -1;
        headerLength = -1; 
        curLimit = -1;
        curPosition = -1;
        state = 0;
    }
    
    
    // --------------------------------------------------- Utils --------------/
    
    
    /**
     * Allocate a <code>ByteBuffer</code>
     * @param useDirectByteBuffer to create a direct <code>ByteBuffer</code>
     * @param useByteBufferView to create <code>ByteBuffer</code> view
     * @param size the size of the <code>ByteBuffer</code>
     * @return a <code>ByteBuffer</code>
     */   
    public ByteBuffer allocate(boolean useDirectByteBuffer, 
                               boolean useByteBufferView, 
                               int size){
        ByteBuffer byteBuffer;
        if (useByteBufferView){
            byteBuffer = ByteBufferFactory.allocateView(size,
                                                        useDirectByteBuffer);
        } else if ( useDirectByteBuffer ){
            byteBuffer = ByteBuffer.allocateDirect(size);            
        } else {
            byteBuffer = ByteBuffer.allocate(size);                       
        }
        return byteBuffer;
    }
    
    
    /**
     * Allocate a new <code>ByteBuffer</code> and put the content of the current
     * one into it.
     */
    private ByteBuffer swapBuffer(ByteBuffer byteBuffer, int size){
        ByteBuffer tmp = allocate(useDirectByteBuffer,useByteBufferView, size);

        byteBuffer.flip();
        tmp.put(byteBuffer);
        
        // Keep a pointer to the original one.
        if ( primaryByteBuffer == null) {
            primaryByteBuffer = byteBuffer;
        }
        byteBuffer = tmp;
        return byteBuffer;
    }        
    
    
    /**
     * Rollback the <code>ByteBuffer</code> to its previous state in case
     * an error as occured.
     */
    public ByteBuffer rollbackParseState(ByteBuffer byteBuffer){
        /**
         * Set the <code>ByteBuffer</code> position/limit to the value
         * they were before trying to process the stream.
         */
        if (curLimit != -1 && curPosition != -1){
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);

            // Reset so we can process a new request.
            contentLength = -1;
            headerLength = -1;  
        }
        return byteBuffer;
    }    
    
    
    /**
     * Dump the ByteBuffer content. This is used only for debugging purpose.
     */
    protected String dump(ByteBuffer byteBuffer){                   
        ByteBuffer dd = byteBuffer.duplicate();
        dd.flip();
        
        int length = dd.limit();
        byte[] dump = new byte[length];
        dd.get(dump,0,length);
        return(new String(dump) + "\n----------------------------" + dd 
        + "\ncontentLength: " + contentLength 
        + "\nheaderLength: " + headerLength); 
    }
    
    
    /**
     * The SocketChannel used by this class.
     */
    public void setSocketChannel(SocketChannel socketChannel){
        this.socketChannel = socketChannel;
        if ( socketChannel != null)
            handler.attachChannel(socketChannel); 
    }
    
    
    /**
     * Set the port
     */
    public void setPort(int port){
        this.port = port;
    }
    
    
    /**
     * Return the port
     */
    public int getPort(){
        return port;
    }
    
    
    /**
     * Return the full name of the class responsible for handling OP_READ.
     */
    public Class getReadTask(SelectorThread selectorThread){
        if ( selectorThread.getMaxReadWorkerThreads() <= 0 
                && !selectorThread.getEnableAsyncExecution())
            return com.sun.enterprise.web.connector.grizzly.DefaultReadTask.class;
        else 
            return com.sun.enterprise.web.connector.grizzly.AsyncReadTask.class;            
    } 
    
    
    /**
     * Empty handler used when Grizzly is bundled outside of GlassFish.
     *
     * @author Jeanfrancois Arcand
     */
    public static class DummyHandler implements Handler{

        public DummyHandler() {
        }

        public int handle(Object e, int handlerCode) throws IOException {
            return Handler.CONTINUE;
        }
        
        public void attachChannel(SocketChannel sc){
            ; // Not used.
        }

    }
}
