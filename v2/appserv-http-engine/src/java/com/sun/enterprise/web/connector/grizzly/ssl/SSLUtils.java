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
import com.sun.enterprise.web.connector.grizzly.SelectorFactory;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

/**
 * SSL over NIO utility class. The class handle the SSLEngine operations 
 * needed to support SSL over NIO. This class MUST be executed using 
 * an SSLWorkerThread as it rely on some SSLWorkerThread buffers and
 * SSLEngine.
 *
 * TODO: Create an object that Wrap SSLEngine and its associated buffers.
 *
 * @author Jeanfrancois Arcand
 */
public class SSLUtils {
    
    /**
     * The maximum size a ByteBuffer can take.
     */
    public final static int MAX_BB_SIZE = 48 * 4096;

    
    /*
     * An empty ByteBuffer used for handshaking
     */
    protected final static ByteBuffer hsBB = ByteBuffer.allocate(0);
   
    
    /**
     * The time to wait before timing out when reading bytes
     */
    private static int readTimeout = 30000;    
    
    
    /**
     * Read encrypted bytes using an <code>SSLEngine</code>.
     * @param key The SelectionKey
     * @param inputBB The byteBuffer to store encrypted bytes
     * @param sslEngine The SSLEngine uses to manage the SSL operations.
     * @param timeout The Selector.select() timeout value. A value of 0 will
     *                be exectuted as a Selector.selectNow();
     * @return the bytes read.
     */
    public static int doRead(SelectionKey key, ByteBuffer inputBB, 
            SSLEngine sslEngine, int timeout){ 
        
        if (key == null) return -1;

    	SSLEngineResult result = null;
        int count = 1;
        int byteRead = 0;
        Selector readSelector = null;
        SelectionKey tmpKey = null;
        try{
            SocketChannel socketChannel = (SocketChannel)key.channel();
            while (count > 0){
                count = socketChannel.read(inputBB);
                
                if (count == -1) {
                    try{
                        sslEngine.closeInbound();  
                    } catch (IOException ex){
                        ;//
                    }
                    return -1;
                }
                byteRead += count;
            }            
            
            if (byteRead == 0 && inputBB.position() == 0){
                readSelector = SelectorFactory.getSelector();

                if (readSelector == null){
                    return 0;
                }
                count = 1;
                tmpKey = socketChannel
                           .register(readSelector,SelectionKey.OP_READ);               
                tmpKey.interestOps(tmpKey.interestOps() | SelectionKey.OP_READ);
                
                int code = 0;
                if (timeout > 0) {
                    code = readSelector.select(timeout);
                } else {
                    code = readSelector.selectNow();
                }
                tmpKey.interestOps(
                    tmpKey.interestOps() & (~SelectionKey.OP_READ));

                if (code == 0){
                    return 0; // Return on the main Selector and try again.
                }

                while (count > 0){
                    count = socketChannel.read(inputBB);
                                                    
                    if (count == -1) {
                        try{
                            sslEngine.closeInbound();  
                        } catch (IOException ex){
                            ;//
                        }
                        return -1;
                    }
                    byteRead += count;                    
                }
            } else if (byteRead == 0) {
                byteRead += inputBB.position();
            }
        } catch (Throwable t){
            Logger logger = SSLSelectorThread.logger();
            if ( logger.isLoggable(Level.FINEST) ){
                logger.log(Level.FINEST,"doRead",t);
            }            
            return -1;
        } finally {
            if (tmpKey != null)
                tmpKey.cancel();

            if (readSelector != null){
                // Bug 6403933
                try{
                    readSelector.selectNow();
                } catch (IOException ex){
                    ;
                }
                SelectorFactory.returnSelector(readSelector);
            }
        }
        return byteRead;
    } 
    
    
    /**
     * Read encrypted bytes using an <code>SSLEngine</code>.
     * @param key The SelectionKey
     * @param inputBB The byteBuffer to store encrypted bytes
     * @param sslEngine The SSLEngine uses to manage the SSL operations.
     * @param timeout The Selector.select() timeout value. A value of 0 will
     *                be exectuted as a Selector.selectNow();
     * @return the bytes read.
     */
    public static int doHandshakeRead(SelectionKey key, ByteBuffer inputBB, 
            SSLEngine sslEngine, int timeout){ 
        
        if (key == null) return -1;

    	SSLEngineResult result = null;
        int count = 1;
        int byteRead = 0;
        int preReadInputBBPos = inputBB.position();
        Selector readSelector = null;
        SelectionKey tmpKey = null;
        try{
            SocketChannel socketChannel = (SocketChannel)key.channel();
            while (count > 0){
                count = socketChannel.read(inputBB);
                
                if (count == -1) {
                    try{
                        sslEngine.closeInbound();  
                    } catch (IOException ex){
                        ;//
                    }
                    return -1;
                }
                byteRead += count;
            }            
            
            if (byteRead == 0 && inputBB.position() == preReadInputBBPos){
                readSelector = SelectorFactory.getSelector();

                if (readSelector == null){
                    return 0;
                }
                count = 1;
                tmpKey = socketChannel
                           .register(readSelector,SelectionKey.OP_READ);               
                tmpKey.interestOps(tmpKey.interestOps() | SelectionKey.OP_READ);
                
                int code = 0;
                if (timeout > 0) {
                    code = readSelector.select(timeout);
                } else {
                    code = readSelector.selectNow();
                }
                tmpKey.interestOps(
                    tmpKey.interestOps() & (~SelectionKey.OP_READ));

                if (code == 0){
                    return 0; // Return on the main Selector and try again.
                }

                while (count > 0){
                    count = socketChannel.read(inputBB);
                                                    
                    if (count == -1) {
                        try{
                            sslEngine.closeInbound();  
                        } catch (IOException ex){
                            ;//
                        }
                        return -1;
                    }
                    byteRead += count;                    
                }
            } else if (byteRead == 0 && inputBB.position() != preReadInputBBPos) {
                byteRead += (inputBB.position() - preReadInputBBPos);
            }
        } catch (Throwable t){
            Logger logger = SSLSelectorThread.logger();
            if ( logger.isLoggable(Level.FINEST) ){
                logger.log(Level.FINEST,"doRead",t);
            }            
            return -1;
        } finally {
            if (tmpKey != null)
                tmpKey.cancel();

            if (readSelector != null){
                // Bug 6403933
                try{
                    readSelector.selectNow();
                } catch (IOException ex){
                    ;
                }
                SelectorFactory.returnSelector(readSelector);
            }
        }
        return byteRead;
    } 

    /**
     * Unwrap all encrypted bytes from <code>inputBB</code> to 
     * <code>byteBuffer</code> using the <code>SSLEngine</code>
     * @param byteBuffer the decrypted ByteBuffer
     * @param inputBB the encrypted ByteBuffer
     * @param sslEngine The SSLEngine used to manage the SSL operations.
     * @return the decrypted ByteBuffer
     */
    public static ByteBuffer unwrapAll(ByteBuffer byteBuffer, 
            ByteBuffer inputBB, SSLEngine sslEngine) throws IOException{
        
        SSLEngineResult result = null;
        do{
            try{
               result = unwrap(byteBuffer,inputBB,sslEngine);
            } catch (Throwable ex){
                Logger logger = SSLSelectorThread.logger();
                if ( logger.isLoggable(Level.FINE) ){
                    logger.log(Level.FINE,"unwrap",ex);
                }
                inputBB.compact();
            }

            if (result != null){
                switch (result.getStatus()) {

                    case BUFFER_UNDERFLOW:
                        // Need more data.
                        break;
                    case OK:
                        if (result.getHandshakeStatus() 
                                == HandshakeStatus.NEED_TASK) {
                            executeDelegatedTask(sslEngine);
                        }
                        break;
                    case BUFFER_OVERFLOW:
                         byteBuffer = reallocate(byteBuffer);
                         break;
                    default:                       
                        throw new 
                             IOException("Unwrap error: "+ result.getStatus());
                 }   
             }
        } while ((inputBB.position() != 0) && result!= null &&
                result.getStatus() != Status.BUFFER_UNDERFLOW);
        return byteBuffer;
    }
    
    
    /**
     * Unwrap available encrypted bytes from <code>inputBB</code> to 
     * <code>byteBuffer</code> using the <code>SSLEngine</code>
     * @param byteBuffer the decrypted ByteBuffer
     * @param inputBB the encrypted ByteBuffer
     * @param sslEngine The SSLEngine used to manage the SSL operations.
     * @return SSLEngineResult of the SSLEngine.unwrap operation.
     */
    public static SSLEngineResult unwrap(ByteBuffer byteBuffer, 
            ByteBuffer inputBB, SSLEngine sslEngine) throws IOException{

        inputBB.flip();
        SSLEngineResult result = sslEngine.unwrap(inputBB, byteBuffer);
        inputBB.compact();
        return result;
    }
    
    
    /**
     * Encrypt bytes.
     * @param byteBuffer the decrypted ByteBuffer
     * @param outputBB the encrypted ByteBuffer
     * @param sslEngine The SSLEngine used to manage the SSL operations.
     * @return SSLEngineResult of the SSLEngine.wrap operation.
     */
    public static SSLEngineResult wrap(ByteBuffer byteBuffer,
            ByteBuffer outputBB, SSLEngine sslEngine) throws IOException {        
        
        outputBB.clear();   
        SSLEngineResult result = sslEngine.wrap(byteBuffer, outputBB);
        outputBB.flip();
        return result;
    }
    
    
    /**
     * Resize a ByteBuffer.
     */
    private static ByteBuffer reallocate(ByteBuffer byteBuffer) 
            throws IOException{
        
        if (byteBuffer.capacity() > MAX_BB_SIZE){
            throw new IOException("Unwrap error: BUFFER_OVERFLOW");
        }
        ByteBuffer tmp = ByteBuffer.allocate(byteBuffer.capacity() * 2);
        byteBuffer.flip();
        tmp.put(byteBuffer);
        byteBuffer = tmp;
        return byteBuffer;
    }
    
     
    /**
     * Complete hanshakes operations.
     * @param sslEngine The SSLEngine used to manage the SSL operations.
     * @return SSLEngineResult.HandshakeStatus
     */
    public static SSLEngineResult.HandshakeStatus 
            executeDelegatedTask(SSLEngine sslEngine) {

        Runnable runnable;
        while ((runnable = sslEngine.getDelegatedTask()) != null) {
            runnable.run();
        }
        return sslEngine.getHandshakeStatus();
    }
    
    
    /**
     * Perform an SSL handshake using the SSLEngine. 
     * @param key the <code>SelectionKey</code>
     * @param byteBuffer The application <code>ByteBuffer</code>
     * @param inputBB The encrypted input <code>ByteBuffer</code>
     * @param outputBB The encrypted output <code>ByteBuffer</code>
     * @param sslEngine The SSLEngine used.
     * @param handshakeStatus The current handshake status
     * @param timeout The time the Selector will block waiting for bytes
     * @return byteBuffer the new ByteBuffer
     * @throw IOException if the handshake fail.
     */
    public static ByteBuffer doHandshake(SelectionKey key,
            ByteBuffer byteBuffer, ByteBuffer inputBB, ByteBuffer outputBB,
            SSLEngine sslEngine, HandshakeStatus handshakeStatus) 
            throws IOException {
        return doHandshake(key,byteBuffer,inputBB,outputBB,sslEngine,
                           handshakeStatus,readTimeout);
    }

    
    /**
     * Perform an SSL handshake using the SSLEngine. 
     * @param key the <code>SelectionKey</code>
     * @param byteBuffer The application <code>ByteBuffer</code>
     * @param inputBB The encrypted input <code>ByteBuffer</code>
     * @param outputBB The encrypted output <code>ByteBuffer</code>
     * @param sslEngine The SSLEngine used.
     * @param handshakeStatus The current handshake status
     * @return byteBuffer the new ByteBuffer
     * @throw IOException if the handshake fail.
     */
    public static ByteBuffer doHandshake(SelectionKey key,
            ByteBuffer byteBuffer, ByteBuffer inputBB, ByteBuffer outputBB,
            SSLEngine sslEngine, HandshakeStatus handshakeStatus,int timeout) 
            throws IOException {
        
        SSLEngineResult result;
        int eof = timeout > 0 ? 0 : -1;
        while (handshakeStatus != HandshakeStatus.FINISHED){
            switch (handshakeStatus) {
               case NEED_UNWRAP:
                    if (doHandshakeRead(key,inputBB,sslEngine, timeout) <= eof) {
                        try{
                            sslEngine.closeInbound();
                        } catch (IOException ex){
                            Logger logger = SSLSelectorThread.logger();
                            if ( logger.isLoggable(Level.FINE) ){
                                logger.log(Level.FINE,"closeInbound",ex);
                            }
                        }
                        throw new EOFException("Connection closed");
                    }
                    
                    while (handshakeStatus == HandshakeStatus.NEED_UNWRAP) {
                        result = unwrap(byteBuffer,inputBB,sslEngine);
                        handshakeStatus = result.getHandshakeStatus();
                        
                        if (result.getStatus() == Status.BUFFER_UNDERFLOW){
                            break;
                        }
                        
                        switch (result.getStatus()) {
                            case OK:
                                switch (handshakeStatus) {
                                    case NOT_HANDSHAKING:
                                        throw new IOException("No Hanshake");

                                    case NEED_TASK:
                                        handshakeStatus = 
                                                executeDelegatedTask(sslEngine);
                                        break;                               

                                    case FINISHED:
                                       return byteBuffer;
                                }
                                break;
                            case BUFFER_OVERFLOW:
                                byteBuffer = reallocate(byteBuffer);     
                                break;
                            default: 
                                throw new IOException("Handshake exception: " + 
                                        result.getStatus());
                        }
                    }  

                    if (handshakeStatus != HandshakeStatus.NEED_WRAP) {
                        break;
                    }
                case NEED_WRAP:
                    result = wrap(hsBB,outputBB,sslEngine);
                    handshakeStatus = result.getHandshakeStatus();
                    switch (result.getStatus()) {
                        case OK:

                            if (handshakeStatus == HandshakeStatus.NEED_TASK) {
                                handshakeStatus = executeDelegatedTask(sslEngine);
                            }

                            // Flush all Server bytes to the client.
                            if (key != null) {
                                OutputWriter.flushChannel(
                                        (SocketChannel)key.channel(), outputBB);
                                outputBB.clear();
                            }
                            break;
                        default: 
                            throw new IOException("Handshaking error: " 
                                    + result.getStatus());
                        }
                        break;
                default: 
                    throw new RuntimeException("Invalid Handshaking State" +
                            handshakeStatus);
            } 
        }
        return byteBuffer;
    }

    
    public static int getReadTimeout() {
        return readTimeout;
    }

    
    public static void setReadTimeout(int aReadTimeout) {
        readTimeout = aReadTimeout;
    }

}
