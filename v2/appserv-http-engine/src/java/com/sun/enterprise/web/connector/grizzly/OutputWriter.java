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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * NIO utility to flush <code>ByteBuffer</code>
 *
 * @author Scott Oaks
 */
public class OutputWriter {
    
    /**
     * The default rime out before closing the connection
     */
    private static int defaultWriteTimeout = 30000;
    
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */   
    public static long flushChannel(SocketChannel socketChannel, ByteBuffer bb)
            throws IOException{
        return flushChannel(socketChannel,bb,defaultWriteTimeout);
    }
       
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */   
    public static long flushChannel(SocketChannel socketChannel, 
            ByteBuffer bb, long writeTimeout) throws IOException{    
        
        if (bb == null){
            throw new IllegalStateException("Invalid Response State. ByteBuffer" 
                    + " cannot be null.");
        }
        
        if (socketChannel == null){
            throw new IllegalStateException("Invalid Response State. " +
                    "SocketChannel cannot be null.");
        }    

        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        int bytesProduced = 0;
        try {
            while ( bb.hasRemaining() ) {
                int len = socketChannel.write(bb);
                attempts++;
                if (len < 0){
                    throw new EOFException();
                } 
            
                bytesProduced += len;
                
                if (len == 0) {
                    if ( writeSelector == null ){
                        writeSelector = SelectorFactory.getSelector();
                        if ( writeSelector == null){
                            // Continue using the main one.
                            continue;
                        }
                    }

                    key = socketChannel.register(writeSelector, key.OP_WRITE);
                    
                    if (writeSelector.select(writeTimeout) == 0) {
                        if (attempts > 2)
                            throw new IOException("Client disconnected");
                    } else {
                        attempts--;
                    }
                } else {
                    attempts = 0;
                }
            }
        } finally {
            if (key != null) {
                key.cancel();
                key = null;
            }

            if ( writeSelector != null ) {
                // Cancel the key.
                writeSelector.selectNow();
                SelectorFactory.returnSelector(writeSelector);
            }
        }
        return bytesProduced;
    }


    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */
    public static long flushChannel(SocketChannel socketChannel, ByteBuffer[] bb)
            throws IOException{
        return flushChannel(socketChannel,bb,defaultWriteTimeout);
    }


    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */
    public static long flushChannel(SocketChannel socketChannel,
            ByteBuffer[] bb, long writeTimeout) throws IOException{

        if (bb == null){
            throw new IllegalStateException("Invalid Response State. ByteBuffer"
                    + " cannot be null.");
        }

        if (socketChannel == null){
            throw new IllegalStateException("Invalid Response State. " +
                    "SocketChannel cannot be null.");
        }

        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        long totalBytes = 0;
        for (ByteBuffer aBb : bb) {
            totalBytes += aBb.remaining();
        }

        long byteProduced = 0;
        try {
            while (byteProduced < totalBytes ) {
                long len = socketChannel.write(bb);
                attempts++;
                byteProduced += len;
                if (len < 0){
                    throw new EOFException();
                }

                if (len == 0) {
                    if ( writeSelector == null ){
                        writeSelector = SelectorFactory.getSelector();
                        if ( writeSelector == null){
                            // Continue using the main one.
                            continue;
                        }
                    }

                    key = socketChannel.register(writeSelector, key.OP_WRITE);
                    
                    if (writeSelector.select(writeTimeout) == 0) {
                        if (attempts > 2)
                            throw new IOException("Client disconnected");
                    } else {
                        attempts--;
                    }
                } else {
                    attempts = 0;
                }
            }
        } finally {
            if (key != null) {
                key.cancel();
                key = null;
            }

            if ( writeSelector != null ) {
                // Cancel the key.
                writeSelector.selectNow();
                SelectorFactory.returnSelector(writeSelector);
            }
        }
        return bytesProduced;
    }  
    
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */   
    public static long flushChannel(SocketChannel socketChannel, ByteBuffer[] bb)
            throws IOException{
        return flushChannel(socketChannel,bb,defaultWriteTimeout);
    }    
     
    
    /**
     * Flush the buffer by looping until the <code>ByteBuffer</code> is empty
     * @param bb the ByteBuffer to write.
     */   
    public static long flushChannel(SocketChannel socketChannel,
            ByteBuffer[] bb, long writeTimeout) throws IOException{
      
        if (bb == null){
            throw new IllegalStateException("Invalid Response State. ByteBuffer" 
                    + " cannot be null.");
        }
        
        if (socketChannel == null){
            throw new IllegalStateException("Invalid Response State. " +
                    "SocketChannel cannot be null.");
        }   
        
        SelectionKey key = null;
        Selector writeSelector = null;
        int attempts = 0;
        long totalBytes = 0;
        for (ByteBuffer aBb : bb) {
            totalBytes += aBb.remaining();
        }
        
        long byteProduced = 0;
        try {
            while (byteProduced < totalBytes ) {
                long len = socketChannel.write(bb);
                attempts++;
                byteProduced += len;
                if (len < 0){
                    throw new EOFException();
                } 
            
                if (len == 0) {
                    if ( writeSelector == null ){
                        writeSelector = SelectorFactory.getSelector();
                        if ( writeSelector == null){
                            // Continue using the main one.
                            continue;
                        }
                    }
                    
                    key = socketChannel.register(writeSelector, key.OP_WRITE);
                    
                    if (writeSelector.select(writeTimeout) == 0) {
                        if (attempts > 2)
                            throw new IOException("Client disconnected");
                    } else {
                        attempts--;
                    }
                } else {
                    attempts = 0;
                }
            }   
        } finally {
            if (key != null) {
                key.cancel();
                key = null;
            }
            
            if ( writeSelector != null ) {
                // Cancel the key.
                writeSelector.selectNow();
                SelectorFactory.returnSelector(writeSelector);
            }
        }
        return byteProduced;
    }  

    
    public static int getDefaultWriteTimeout() {
        return defaultWriteTimeout;
    }

    
    public static void setDefaultWriteTimeout(int aDefaultWriteTimeout) {
        defaultWriteTimeout = aDefaultWriteTimeout;
    }
}
