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

package com.sun.enterprise.web.portunif.util;

import com.sun.enterprise.web.connector.grizzly.OutputWriter;
import com.sun.enterprise.web.connector.grizzly.ssl.SSLOutputWriter;
import com.sun.enterprise.web.connector.grizzly.ssl.SSLSelectorThread;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tomcat.util.buf.Ascii;

/**
 * Utility class used to redirect an HTTP(s) request to another protocol and
 * endpoint. The following scenarios are supported:
 *
 * (1) http://host:port1 to https://host:port1
 * (2) https://host:port1 to http://host:port1
 * (3) http://host:port2 to https://host:port1
 * (4) https://host:port2 to https://host:port1
 * (5) http://host:port2 to http://host:port1
 * (6) https://host:port2 to http://host:port1
 *
 * This class internally start an NIO Selector listening on an 'external' port
 * to a 'redirect' port. All requests to the external port,
 * independently of the protocol are redirected to the 'redirect' port.
 *
 * @author Jeanfrancois Arcand
 */
public class Redirector {
    
    /**
     * HTTP end line.
     */
    private final static String NEWLINE = "\r\n";
    
    /**
     * The IP address the server is running on.
     */
    private static String ipAddress = "127.0.0.1";

    
    static {
        try{
            ipAddress = InetAddress.getLocalHost().getHostName();
        } catch (Throwable t){
            ;
        }
    }
    
    
    /**
     * Header String.
     */
    private final static String headers = NEWLINE + "Connection:close" + NEWLINE
                + "Cache-control: private" + NEWLINE
                + NEWLINE;
    
    
    /**
     * Header String.
     */
    private final static ByteBuffer SC_FOUND =
            ByteBuffer.wrap( ("HTTP/1.1 302 Moved Temporarily" 
                + NEWLINE).getBytes());
    
    
    // --------------------------------------------------------- Constructor --/
    
    
    public Redirector(){
    }

    
    /**
     * Redirect a secure request (https) to http or https.
     * @param protocolInfo the ProtocolInfo that contains the information about
     *               the current protocol state.
     */
    public final void redirectSSL(ProtocolInfo protocolInfo) throws IOException{
        String host = parseHost(protocolInfo.byteBuffer);
        if (host == null){
            host = ipAddress + ":" + 
                    protocolInfo.socketChannel.socket().getLocalPort();
        }      
        redirectSSL(protocolInfo,
                protocolInfo.isRequestedTransportSecure ? 
                    new String("Location: https://" + host): 
                    new String("Location: http://" + host));
    }
    
    
    /**
     * Redirect a secure request (https) to http or https.
     * @param protocolInfo the ProtocolInfo that contains the information about
     *               the current protocol state.
     * @param byteBuffer the bytes response.
     */
    private final void redirectSSL(ProtocolInfo protocolInfo, 
            String httpHeaders) throws IOException{

        SSLOutputWriter.flushChannel(protocolInfo.socketChannel,
                SC_FOUND.slice(),
                protocolInfo.outputBB,
                protocolInfo.sslEngine);
        SSLOutputWriter.flushChannel(protocolInfo.socketChannel,
                ByteBuffer.wrap((httpHeaders + protocolInfo.requestURI + headers)
                    .getBytes()),
                protocolInfo.outputBB,
                protocolInfo.sslEngine);
    }
   
    
    /**
     * Redirect a un-secure request (http) to http or https.
     * @param protocolInfo the ProtocolInfo that contains the information about
     *               the current protocol state.
     */
    public final void redirect(ProtocolInfo protocolInfo)
            throws IOException{
        String host = parseHost(protocolInfo.byteBuffer);
        if (host == null){
            host = ipAddress + ":" + 
                    protocolInfo.socketChannel.socket().getLocalPort();
        }
        redirect(protocolInfo,protocolInfo.isRequestedTransportSecure 
                    ? new String("Location: https://" + host)
                    : new String("Location: http://" + host));
    }
    
    
    /**
     * Redirect a secure request (http) to http or https.
     * @param protocolInfo the ProtocolInfo that contains the information about
     *               the current protocol state.
     * @param byteBuffer the bytes response.
     */
    private final void redirect(ProtocolInfo protocolInfo,String httpHeaders)
            throws IOException{

        OutputWriter.flushChannel(protocolInfo.socketChannel,SC_FOUND.slice());
        OutputWriter.flushChannel(protocolInfo.socketChannel,
                ByteBuffer.wrap((httpHeaders 
                    + protocolInfo.requestURI + headers).getBytes()));
    }
 
    
    /**
     * Return the host value, or null if not found.
     * @param byteBuffer the request bytes.
     */
    private String parseHost(ByteBuffer byteBuffer){
         boolean isFound = false;
                          
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();

        // Rule a - If nothing, return to the Selector.
        if (byteBuffer.position() == 0)
            return ipAddress;
       
        byteBuffer.position(0);
        byteBuffer.limit(curPosition);
        

        int state =0;
        int start =0;
        int end = 0;        
        
        try {                         
            byte c;            
            
            // Rule b - try to determine the host header
            while(byteBuffer.hasRemaining()) {
                c = (byte)Ascii.toLower(byteBuffer.get());
                switch(state) {
                    case 0: // Search for first 'h'
                        if (c == 0x68){
                            state = 1;      
                        } else {
                            state = 0;
                        }
                        break;
                    case 1: // Search for next 'o'
                        if (c == 0x6f){
                            state = 2;
                        } else {
                            state = 0;
                        }
                        break;
                    case 2: // Search for next 's'
                        if (c == 0x73){
                            state = 3;
                        } else {
                            state = 0;
                        }
                        break;
                    case 3: // Search for next 't'
                        if (c == 0x74){
                            state = 4;
                        } else {
                            state = 0;
                        }
                        break; 
                    case 4: // Search for next ':'
                        if (c == 0x3a){
                            state = 5;
                        } else {
                            state = 0;
                        }
                        break;     
                    case 5: // Get the Host                  
                        StringBuilder sb = new StringBuilder();
                        while (c != 0x0d && c != 0x0a) {
                            sb.append((char) c);
                            c = byteBuffer.get();
                        }
                        return sb.toString().trim();                        
                    default:
                        throw new IllegalArgumentException("Unexpected state");
                }      
            }
            return null;
        } catch (BufferUnderflowException bue) {
            return null;
        } finally {     
            if ( end > 0 ){
                byteBuffer.position(start);
                byteBuffer.limit(end);
            } else {
                byteBuffer.limit(curLimit);
                byteBuffer.position(curPosition);                               
            }
        }       
    }
    
    
    /**
     * Log an exception.
     */
    private void log(Throwable ex){
        Logger logger = SSLSelectorThread.logger();
        if ( logger.isLoggable(Level.WARNING) ){
            logger.log(Level.WARNING,"Redirector",ex);
        }
    }
}

