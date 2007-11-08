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

import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.handlers.NoParsingHandler;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

/**
 * Predict if the NIO channel has been fully read or not. This lagorithm will 
 * first search for the content-length header, and use that value to determine if
 * the bytes has been fully read or not. If the content-length isn't included,
 * it will search for the end of the HTTP stream, which is a '\r\n'
 *
 * @author Scott Oaks.
 * @author Jean-Francois Arcand
 */
public final class StateMachineAlgorithm extends StreamAlgorithmBase{


    // ---------------------------------------------- Constructor ----------/
    
    
    public StateMachineAlgorithm() {
        if (embeddedInGlassFish){
            handler = new NoParsingHandler();
        } else {
            handler = new DummyHandler();
        }      
    }
    

    /**
     * Parse the <code>ByteBuffer</code> and try to determine if the bytes
     * stream has been fully read from the <code>SocketChannel</code>.
     *    
     * Drain the <code>SocketChannel</code> and determine if the request bytes
     * has been fully read. For POST method, parse the bytes and seek for the
     * content-type header to determine the length of the request bytes.
     * @return true if we need to call back the <code>SelectorThread</code>
     *              This occurs when the stream doesn't contains all the 
     *              request bytes.
     *         false if the stream contains all request bytes.    
     *
     * @paran byteBuffer the bytes read.
     * @return true if the algorithm determines the end of the stream.
     */
    public boolean parse(ByteBuffer byteBuffer){               
        boolean isFound = false;
                          
        curLimit = byteBuffer.limit();
        curPosition = byteBuffer.position();
        
        // Rule a - if we know the content length, verify all bytes are read
        if ( contentLength != -1 ){
            isFound = ((contentLength + headerLength) <= byteBuffer.position());
            
            if (isFound)
                byteBuffer.flip();
            
            return isFound;
        }

        // Rule b - If nothing, return to the Selector.
        if (byteBuffer.position() == 0)
            return false;

        if (SelectorThread.logger().isLoggable(Level.FINEST))
            SelectorThread.logger().log(Level.FINEST,dump(byteBuffer));
        
        byteBuffer.flip();        
        try {                         
            byte c;
            byte prev_c = ' ';
            
            try{
                if ( state != 0 ){
                    byteBuffer.position(lastStatePosition);
                }
            } catch (RuntimeException ex){
                // If for any reason we aren't able to recover, use the bytes as
                // it is (so DoS are avoided).
                state = 0;
                lastStatePosition = -1;
            }
            
            // Rule c - try to determine the content-length
            while(byteBuffer.hasRemaining()) {
                c = byteBuffer.get();

                // State Machine
                // 0: no special characters have been read, but at beginning
                //	 of line
                // 1: not at beginning of line; parse until the next line
                // 2: read initial G
                // 3: read initial GE
                // 4: read initial GET -- now we know we have a GET request;
                //	   we can parse until we see \r\n\r\n
                //	   we stay state 4-7 until we're done
                // 8: read initial P
                // 9: read initial PO
                // 10: read initial POS
                // 11: read initial POST -- now we can look for content-length
                //	  we can parse until we see content-lenth
                //	   we stay in state 12-27 until we've got length
                // 28: read all of post; parse until \r\n\r\n (see state 4)
                
                // ************************************************************
                // XXX This algorithm needs to use 
                // InternalInputBuffer.parseRequestLine instead
                // ************************************************************
                switch(state) {
                    case 0: // looking for g or p
                        if (c == 0x47 || c == 0x67)
                            state = 2;
                        else if (c == 0x50 || c == 0x70)
                            state = 8;
                        else if (c != 0x0d && c != 0x0a)
                            state = 1;
                        break;
                    case 1: // looking for next line
                        if (c == 0x0a || c == 0x0d)
                            state = 0;
                        break;
                    case 2: // looking for e
                        if (c == 0x45 || c == 0x65)
                            state = 3;
                        else state = 1;
                        break;
                    case 3: // looking for t
                        if (c == 0x54 || c == 0x74)
                            state = 4;
                        else state = 1;
                        break;
                    case 4: // \r
                        if ( c == 0x0a ){
                            state = 5;
                        }
                        break;
                    case 5: // \n or \r
                        if ( c == 0x0d || c == 0x0a){
                            headerLength = byteBuffer.position();
                            isFound = true;
                            return isFound;
                        }
                        else state = 4;
                        break;
                    case 8: // looking for o
                        if (c == 0x4F || c == 0x6F)
                            state = 9;
                        else state = 1;
                        break;
                    case 9: // looking for s
                        if (c == 0x73 || c == 0x53)
                            state = 10;
                        else state = 1;
                        break;
                    case 10: // looking for t
                        if (c == 0x74 || c == 0x54)
                            state = 11;
                        else state = 1;
                        break;
                    case 11: // looking for new line
                        if (c == 0x0a || c == 0x0d)                   
                            state = 12;
                        else state = 11;
                        break;
                    case 12: // looking for c
                        if (c == 0x43 || c == 0x63)
                            state = 13;
                        else if (prev_c == 0x0a 
                            && byteBuffer.position() == curPosition) {
                            headerLength = byteBuffer.position();
                            // Content-length not specified.
                            isFound = true;
                            return isFound;
                        } else state = 12;
                        break;
                    case 13: // looking for o
                        if (c == 0x4F || c == 0x6F)
                            state = 14;
                        else state = 11;
                        break;
                    case 14: // looking for n
                        if (c == 0x4E || c == 0x6E)
                            state = 15;
                        else state = 11;
                        break;
                    case 15: // looking for t
                        if (c == 0x54 || c == 0x74)
                            state = 16;
                        else state = 11;
                        break;
                    case 16: // looking for e
                        if (c == 0x45 || c == 0x65)
                            state = 17;
                        else state = 11;
                        break;
                    case 17: // looking for n
                        if (c == 0x4E || c == 0x6E)
                            state = 18;
                        else state = 11;
                        break;
                    case 18: // looking for t
                        if (c == 0x54 || c == 0x74)
                            state = 19;
                        else state = 11;
                        break;
                    case 19: // -
                        if (c == 0x2D || c == 0x2D)
                            state = 20;
                        else state = 11;
                        break;
                    case 20: // l
                        if (c == 0x4C || c == 0x6C)
                            state = 21;
                        else state = 11;
                        break;
                    case 21: // e
                        if (c == 0x45 || c == 0x65)
                            state = 22;
                        else state = 11;
                        break;
                    case 22: // n
                        if (c == 0x4E || c == 0x6E)
                            state = 23;
                        else state = 11;
                        break;
                    case 23: // g
                        if (c == 0x47 || c == 0x67)
                            state = 24;
                        else state = 11;
                        break;
                    case 24: // t
                        if (c == 0x54 || c == 0x74)
                            state = 25;
                        else state = 11;
                        break;
                    case 25: // h
                        if (c == 0x48 || c == 0x68)
                            state = 26;
                        else state = 11;
                        break;
                    case 26: // :
                        if (c == 0x3a)
                            state = 27;
                        else state = 11;
                        break;
                    case 27: // read length until \r
                        while (c < 0x30 || c > 0x39) {
                            // Read past the whitespace between the : 
                            // and the length
                            c = byteBuffer.get();
                        }
                        StringBuilder sb = new StringBuilder();
                        while (c >= 0x30 && c <= 0x39) {
                            sb.append((char) c);
                            c = byteBuffer.get();
                        }
                        contentLength = Integer.parseInt(sb.toString());
                        // XXX: We've already read the character past the length.
                        // Can it ever not be 0x0d? Would that be an error
                        // otherwise?                        
                        if (c == 0x0d)
                            state = 29;
                        else state = 28;
                        break;
                    case 28: // looking for \r
                        if (c == 0x0d)
                            state = 29;
                        break;
                    case 29: // looking for \n
                        if (c == 0x0a)
                            state = 30;
                        else state = 28;
                        break;
                    case 30: // looking for \r
                        if (c == 0x0d)
                            state = 31;
                        else state = 28;
                        break;
                    case 31: // looking for \n
                        if (c == 0x0a){
                            headerLength = byteBuffer.position();
                            // wait until we have fully read the request body. 
                            isFound = ((contentLength + headerLength) 
                                                         <= byteBuffer.limit());
                            return isFound;
                        }
                        else state = 28;
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected state");
                    }
                    prev_c = c;
                }
                
                // NEITHER A GET OR A POST
                if ( state == 0 ) {
                    isFound = true;
                }
            
                return isFound;
        } catch (BufferUnderflowException bue) {
            SelectorThread.logger().log(Level.SEVERE,"readTask.bufferunderflow", bue);
            return false;
        } finally {       
            if ( headerLength == -1 && (state > 0)){
                // This means we weren't able to able to find the content-length
                // or the end of the stream.
                lastStatePosition = byteBuffer.limit();
            }
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);
            
            if (isFound){
                byteBuffer.flip();
            }
        }        
    }

    
    /**
     * Return the <code>Handler</code> used by this algorithm.
     */
    public Handler getHandler(){
        return handler;
    }
     
    
    /***
     * Recycle this object.
     */
    public void recycle(){
        super.recycle();
        
        socketChannel = null;
        if ( handler != null){
            handler.attachChannel(null);
        }
    }   
      
        
    /**
     * Return the full name of the class responsible for handling OP_READ.
     */
    public Class getReadTask(SelectorThread selectorThread){
        return com.sun.enterprise.web.connector.grizzly.XAReadTask.class;
    }
}
