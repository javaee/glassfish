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

package com.sun.enterprise.web.portunif;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.ssl.SSLSelectorThread;
import com.sun.enterprise.web.connector.grizzly.ssl.SSLUtils;
import com.sun.enterprise.web.portunif.util.ProtocolInfo;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

/**
 * <code>ProtocolFinder</code> that will first try to execute an handshake.
 * If the handshake is succesfull, the https protocol will be assumed. If the 
 * handshake fail, the http protocol will be assumed.
 *
 * This object shoudn't be called by several threads simultaneously.
 *
 * @author Jeanfrancois Arcand
 */
public class TlsProtocolFinder implements ProtocolFinder{
    
    /**
     * Decrypted ByteBuffer default size.
     */
    private final static int appBBSize = 5 * 4096;

    
    /**
     * Require client Authentication.
     */
    private boolean needClientAuth = false;
    
    
    /** 
     * True when requesting authentication.
     */
    private boolean wantClientAuth = false; 
    
    // ---------------------------------------------------------------------- //
    
    
    public TlsProtocolFinder() {
        // Try to find a secure SelectorThread.
        Enumeration<SelectorThread> selectors 
            = SelectorThread.getSelectors();                          
        SelectorThread sel;
        while (selectors.hasMoreElements()){
            sel = selectors.nextElement();
            if (sel instanceof SSLSelectorThread){
                needClientAuth = ((SSLSelectorThread)sel).isNeedClientAuth();
                wantClientAuth = ((SSLSelectorThread)sel).isWantClientAuth();  
                break;
            }
        }        
    }
    
    
    /**
     * Try to initialize an SSL|TLS handshake to determine if https 
     */
    public void find(ProtocolInfo protocolInfo) throws IOException{
        if (protocolInfo.sslContext == null){
            return;
        }

        SelectionKey key = protocolInfo.key;    
        SocketChannel socketChannel = (SocketChannel)key.channel();

        SSLEngine sslEngine = protocolInfo.sslEngine;        
        if (protocolInfo.sslContext != null && sslEngine == null) {
            sslEngine = protocolInfo.sslContext.createSSLEngine();
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(needClientAuth);
            sslEngine.setWantClientAuth(wantClientAuth);
        }
        
        ByteBuffer inputBB = null;
        ByteBuffer outputBB =  null;
        ByteBuffer byteBuffer =  null;  
        int inputBBSize = sslEngine.getSession().getPacketBufferSize();        
        if (protocolInfo.inputBB == null 
                || (inputBB != null && inputBBSize > inputBB.capacity())){
            inputBB = ByteBuffer.allocate(inputBBSize * 2);
            outputBB = ByteBuffer.allocate(inputBBSize * 2);
            byteBuffer = ByteBuffer.allocate(inputBBSize * 2);  

            inputBBSize = sslEngine.getSession().getApplicationBufferSize();
            if ( inputBBSize > byteBuffer.capacity() ) {
                ByteBuffer newBB = ByteBuffer.allocate(inputBBSize);
                byteBuffer.flip();
                newBB.put(byteBuffer);
                byteBuffer = newBB;
            }   

        } else {
            inputBB = protocolInfo.inputBB;
            outputBB = protocolInfo.outputBB;
            byteBuffer = protocolInfo.byteBuffer;                     
        }
        outputBB.position(0);
        outputBB.limit(0); 
        
        if ( protocolInfo.bytesRead > 0){
            inputBB.put((ByteBuffer)protocolInfo.byteBuffer.flip());
        }

        HandshakeStatus handshakeStatus = HandshakeStatus.NEED_UNWRAP;
        boolean OK = true;  
        if (protocolInfo.handshake){
            try{
                byteBuffer = SSLUtils.doHandshake(key,byteBuffer,inputBB,outputBB,
                                                  sslEngine,handshakeStatus,
                                                  SSLUtils.getReadTimeout());
                protocolInfo.handshake = false;
            } catch (EOFException ex) {
                ; // DO nothing, as the client closed the connection
            } catch (Throwable ex) {            
                // An exception means the handshake failed.
                OK = false;
                byteBuffer.put(inputBB);            
            }
        }
          
        try {
            if (OK) {  
                int byteRead = SSLUtils.doRead(key,inputBB,sslEngine,
                        SSLUtils.getReadTimeout());
                if (byteRead > -1){
                    protocolInfo.byteBuffer = 
                            SSLUtils.unwrapAll(byteBuffer,inputBB,sslEngine);
                    protocolInfo.bytesRead = byteBuffer.position();
                    
                    // Not enough bytes to decrypt the request.
                    if (protocolInfo.bytesRead == 0){
                        OK = false;
                    }                  
                } else {
                   throw new EOFException(); 
                }
            }
        } finally {
            String protocol = protocolInfo.protocol;  
            protocolInfo.inputBB = inputBB;
            protocolInfo.byteBuffer = byteBuffer;
            protocolInfo.outputBB = outputBB; 
            protocolInfo.sslEngine = sslEngine;     
            protocolInfo.isSecure = true;            
            if (!OK){
                protocolInfo.bytesRead = byteBuffer.position();
                protocolInfo.protocol = null;
                protocolInfo.isSecure = false;
            }
            protocolInfo.socketChannel = (SocketChannel)key.channel();                
        }
        return;
    }
}
