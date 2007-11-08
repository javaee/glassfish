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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.coyote.Response;
import org.apache.coyote.http11.InternalOutputBuffer;

/**
 * Output buffer.
 * Buffer the bytes until the <code>ByteChunk</code> is full or the request
 * is completed.
 * 
 * @author Jean-Francois Arcand
 * @author Scott Oaks
 */
public class SocketChannelOutputBuffer extends InternalOutputBuffer{

    /**
     * Underlying output socketChannel.
     */
    protected SocketChannel socketChannel;
    
    
    /**
     * Underlying ByteByteBuffer
     */
    private ByteBuffer outputByteBuffer;

    
    /**
     * ACK static bytes.
     */
    private final static ByteBuffer ACK = 
            ByteBuffer.wrap("HTTP/1.1 100 Continue\r\n\r\n".getBytes());
    
    // ----------------------------------------------------------- Constructors
    

    /**
     * Alternate constructor.
     */
    public SocketChannelOutputBuffer(Response response, 
            int headerBufferSize, boolean useSocketBuffer) {
        super(response,headerBufferSize, useSocketBuffer); 
        
        if (!useSocketBuffer){
            outputStream = new NIOOutputStream();
        }
        
        outputByteBuffer = ByteBuffer.allocate(headerBufferSize * 10);
    }

    
    // ------------------------------------------------------------- Properties


    /**
     * Set the underlying socket output stream.
     */
    public void setChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    
    /**
     * Return the underlying SocketChannel
     */
    public SocketChannel getChannel(){
        return socketChannel;
    }
    
    // --------------------------------------------------------- Public Methods

    /**
     * Send an acknoledgement without buffering.
     */
    public void sendAck() throws IOException {
        if (!committed)
            flushChannel(ACK.slice());
    }
    
    
    /**
     * Callback to write data from the buffer.
     */
    public void realWriteBytes(byte cbuf[], int off, int len)
        throws IOException {
        if (len > 0) {
            if (len > outputByteBuffer.remaining()){
                int size = Math.max(outputByteBuffer.capacity() * 2,
                                    len + outputByteBuffer.position());
                ByteBuffer tmp = ByteBuffer.allocate(size);
                outputByteBuffer.flip();
                tmp.put(outputByteBuffer);
                outputByteBuffer = tmp;
            }
            outputByteBuffer.put(cbuf, off, len);
        }
    }
    
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */   
    public void flushChannel(ByteBuffer bb) throws IOException{
        OutputWriter.flushChannel(socketChannel, bb);
        bb.clear();
    }
    
    
    /**
     * Flush the buffered bytes,
     */
    public void flush() throws IOException{
        super.flush();
        if (outputByteBuffer.position() != 0){
            flushBuffer();
        }
    }
    
    
     /**
     * End request.
     * 
     * @throws IOException an undelying I/O error occured
     */
    public void endRequest()
        throws IOException {
        super.endRequest();
        if (!useSocketBuffer && outputByteBuffer.position() != 0) {
            flushBuffer();
        }
    }  
    
    /**
     * Writes bytes to the underlying socketChannel.
     */
    public void flushBuffer() throws IOException{
        if (outputByteBuffer.position() != 0){
            outputByteBuffer.flip();
            flushChannel(outputByteBuffer);
            outputByteBuffer.clear();
        }
    }

    
    /**
     * Recycle the output buffer. This should be called when closing the 
     * connection.
     */
    public void recycle() {        
        response.recycle();
        socketBuffer.recycle();
        pos = 0;
        lastActiveFilter = -1;
        committed = false;
        finished = false;
        outputByteBuffer.clear();

        socketChannel = null;
    }
    
    
    // ---------------------------------------------- Class helper ----------//
    
    /**
     * OutputBuffer delegating all writes to the <code>OutputWriter</code>
     */
    private final class NIOOutputStream extends OutputStream{   
        
        public void write(byte[] b, int off, int len) throws IOException{           
            realWriteBytes(b,off,len);
        }
        
        public void write(int b) throws IOException {
            write((byte)b);
        }
        
        public void write(byte b) throws IOException {
            if(!outputByteBuffer.hasRemaining()) {
                ByteBuffer tmp = ByteBuffer.allocate(
                        outputByteBuffer.capacity() * 2);
                outputByteBuffer.flip();
                tmp.put(outputByteBuffer);
                outputByteBuffer = tmp;
            }
            outputByteBuffer.put(b);
            return;
        }
    }
}
