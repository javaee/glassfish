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

/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.sun.enterprise.web.connector.grizzly.algorithms;

import com.sun.enterprise.web.connector.grizzly.Constants;
import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.handlers.ContentLengthHandler;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.util.logging.Level;

import org.apache.tomcat.util.buf.Ascii;

/**
 * Predict if the NIO channel has been fully read or not. This lagorithm will 
 * first search for the content-length header, and use that value to determine if
 * the bytes has been fully read or not. If the content-length isn't included,
 * it will search for the end of the HTTP stream, which is a '\r\n'
 *
 * Note: the parsing algorithm is an adaptation of:
 * org.apache.coyote.http11.InternalInputBuffer
 * written by Remy Maucherat 
 *
 * @author Jean-Francois Arcand
 */
public class ContentLengthAlgorithm extends StreamAlgorithmBase{
         
    protected final static byte[] POST_METHOD = "post".getBytes();
    protected final static byte[] PUT_METHOD = "put".getBytes();
    
    protected final static byte[] CL_HEADER = "content-length".getBytes(); 
    
    
    /**
     * Pointer to the US-ASCII header buffer.
     */
    public byte[] ascbuf = new byte[Constants.DEFAULT_REQUEST_BUFFER_SIZE];    

    
    /**
     * Last valid byte.
     */
    protected int lastValid;


    /**
     * Position in the buffer.
     */
    protected int pos;    

    
    /**
     * Is the content-length fully read.
     */
    protected boolean isFound = false;   
    
    
    /**
     * Is the request line parsed?
     */
    protected boolean requestLineParsed = false;   
    
    
    /**
     * The request bytes position.
     */
    public int startReq = -1;
          
    
    /**
     * The request bytes length
     */
    public int lengthReq = -1;

    
    // ---------------------------------------------- Constructor ----------/
    
    
    public ContentLengthAlgorithm() {
        if (embeddedInGlassFish){
            handler = new ContentLengthHandler(this);
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
        isFound = false;

        curLimit = byteBuffer.limit();
        curPosition = byteBuffer.position();

        // Rule a - if we know the content length, verify all bytes are read
        //          Return true only if all bytes has been read.
        if ( contentLength != -1 ){
            isFound = 
                 ((contentLength + headerLength) <= byteBuffer.position());

            if (isFound)
               byteBuffer.flip();

            return isFound;
        }
       
        try{
            // Rule b - If nothing, return to the Selector.
            if (byteBuffer.position() == 0)
                return false;
                    
            byteBuffer.flip();
            lastValid = byteBuffer.limit();

            // Rule c - Parse the request line
            if ( !requestLineParsed ) {
                requestLineParsed = parseRequestLine(byteBuffer);
                if ( !requestLineParsed ) {
                    return false;
                }
            }

            // Rule d -- Parse the headers, looking for content-length
            while (parseHeader(byteBuffer));
            
            // Rule e - for POST/PUT, make sure all the body is loaded from
            //          the socket channel.
            if ( headerLength != -1 && isFound ){
                isFound = ((contentLength + headerLength) 
                                <= byteBuffer.limit());
            } else {
                // The content-length was found, but the headers
                // aren't yet bufferred entirely.
                isFound = false;
            }
           
            return isFound;
        } catch (BufferUnderflowException bue) {
            SelectorThread.logger().log(Level.SEVERE,
                    "readTask.bufferunderflow", bue);
            return false;
        } finally {       
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);
            
            if (isFound){
                byteBuffer.flip();
            }
        }         
    }
    
    
    /**
     * Parse the request line, looking for a POST method.
     */
    protected boolean parseRequestLine(ByteBuffer byteBuffer){           
        int start = 0;
        byte chr = 0;
        
        if ( state == 0 ){
            do {
                if (pos >= lastValid)
                    return false;

                chr = byteBuffer.get(pos++);

            } while ((chr == Constants.CR) || (chr == Constants.LF));
            
            state = 1;

            pos--;
            byteBuffer.position(pos);
        }
            
        // Mark the current buffer position
        start = pos;
        boolean space = false;
        if ( state == 1 ) {
            while (!space) {
                if (pos >= lastValid)
                    return false;

                byte c = byteBuffer.get(pos);
                ascbuf[pos] = c;

                if (c == Constants.SP) {
                    space = true;
                    if ( !isFound && 
                            (findBytes(ascbuf,start, pos, POST_METHOD) == -1 
                             || findBytes(ascbuf,start, pos, PUT_METHOD) == -1)){
                        isFound = true;
                    }
                }

                pos++;
            } 
            state = 2;
        }

        // Mark the current buffer position
        start = pos;
        int end = 0;
        space = false;
        boolean eol = false;    
        int questionPos = -1;
        if ( state == 2 ){
            while (!space) {
                // Read new bytes if needed
                if (pos >= lastValid)
                    return false;

                byte b = byteBuffer.get(pos);
                ascbuf[pos] = b;
                if ( b == Constants.SP) {
                    space = true;
                    end = pos;
                } else if ((b == Constants.CR) 
                           || (b == Constants.LF)) {
                    // HTTP/0.9 style request
                    eol = true;
                    space = true;
                    end = pos;
                } else if ((b == Constants.QUESTION) && (questionPos == -1)){
                    questionPos = pos;
                }
                pos++;

            }
            state = 3;
            
            startReq = start;
            if (questionPos >= 0) {
                lengthReq = questionPos - start;
            } else {
                lengthReq = end - start;
            }
        }

        if ( state == 3 ) {
            // Mark the current buffer position
            start = pos;
            end = 0;
            while (!eol) {
                // Read new bytes if needed
                if (pos >= lastValid)
                    return false;

                byte b = byteBuffer.get(pos);

                if (b == Constants.CR) {
                    end = pos;
                } else if (b == Constants.LF) {
                    if (end == 0)
                        end = pos;
                    eol = true;
                }
                pos++;
            }
            state = 4;
        }
        return eol;
    }
    
    
    /**
     * Parse the headers, looking for content-length header and value.
     */
    protected boolean parseHeader(ByteBuffer byteBuffer){

        boolean headerFound = false;
        byte chr = 0;
        
        if ( state == 4 ){
            while (true) {
                // Read new bytes if needed
                if (pos >= lastValid)
                    return false;

                chr = byteBuffer.get(pos);

                if ((chr == Constants.CR) || (chr == Constants.LF)) {
                    if (chr == Constants.LF) {
                        pos++;   
                        headerLength = pos;
                        isFound = true;  
                        state = 4;
                        return false;
                    }
                } else {
                    break;
                }

                pos++;
            }
            state = 5;
        }
        
        // Mark the current buffer position
        int start = pos;

        if ( state == 5 ){
            boolean colon = false;        
            while (!colon) {
                // Read new bytes if needed
                if (pos >= lastValid)
                    return false;

                chr = byteBuffer.get(pos);
                if (chr == Constants.COLON) {
                    colon = true;

                    if ( contentLength == -1
                         && findBytes(ascbuf, start, start + (pos - start), 
                            CL_HEADER ) != -1){
                        headerFound = true;
                    }
                }

                if ((chr >= Constants.A) && (chr <= Constants.Z)) {
                    chr = (byte) (chr - Constants.LC_OFFSET);
                }

                ascbuf[pos] = chr;

                pos++;
            }
            state = 6;
        }
        
        // Mark the current buffer position
        start = pos;
        int realPos = pos;

        boolean eol = false;
        boolean validLine = true;

        while (validLine) {

            boolean space = true;

            if ( state == 6 ){
                // Skipping spaces
                while (space) {
                    // Read new bytes if needed
                    if (pos >= lastValid)
                        return false;

                    chr = byteBuffer.get(pos);
                    if (( chr == Constants.SP) || (chr == Constants.HT)) {
                        pos++;
                    } else {
                        space = false;
                    }                   
                } 
                state = 7;
            }
            
            int lastSignificantChar = realPos;
            // Reading bytes until the end of the line
            if ( state == 7){
                while (!eol) {
                    // Read new bytes if needed
                    if (pos >= lastValid)
                        return false;

                    chr = byteBuffer.get(pos);
                    if (chr == Constants.CR) {
                    } else if (chr == Constants.LF) {
                        eol = true;
                    } else if (chr == Constants.SP) {
                        realPos++;
                    } else {
                        realPos++;
                        lastSignificantChar = realPos;
                    }
                    pos++;
                }
                state = 8;
                realPos = lastSignificantChar;
            }

            if ( state == 8){
                // Read new bytes if needed
                if (pos >= lastValid)
                    return false;

                chr = byteBuffer.get(pos);
                if ((chr != Constants.SP) && (chr != Constants.HT)) {
                    validLine = false;
                } else {
                    eol = false;
                    realPos++;
                }  
                 state = 4;
            }
        }
 
        if ( headerFound ){
            byteBuffer.position(start+1);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i < realPos - start; i++) {
                sb.append((char) byteBuffer.get());
            }
            contentLength = Integer.parseInt(sb.toString());
        }

        return true;

    }    
 
    
    /**
     * Compare two bytes array and return > 0 if true.
     */
    protected int findBytes(byte[] buff, int start, int end, byte[] b) {

        byte first = b[0];

        // Look for first char 
        int srcEnd = b.length;

        for (int i = start; i <= (end - srcEnd); i++) {
            if (Ascii.toLower(buff[i]) != first) continue;
            
            // found first char, now look for a match
            int myPos = i+1;
            for (int srcPos = 1; srcPos < srcEnd; ) {
                if (Ascii.toLower(buff[myPos++]) != b[srcPos++])
                    break;
                if (srcPos == srcEnd) return i - start; // found it
            }
        }
        return -1;

    }    
      
    
    /***
     * Recylce this object.
     */
    public void recycle(){
        contentLength = -1;
        headerLength = -1;
        curLimit = -1;
        curPosition = -1;
        pos = 0;
        lastValid = 0;
        requestLineParsed = false;
        isFound = false;
        state = 0;
        lengthReq = -1;
        startReq = -1;
        
        socketChannel = null;
        if ( handler != null){
            handler.attachChannel(null);
        }
    }
    
    
    /**
     * Return the <code>Handler</code> used by this algorithm.
     */
    public Handler getHandler(){;
       return handler;
    }
    
        
    /**
     * Return the class responsible for handling OP_READ.
     */
    public Class getReadTask(SelectorThread selectorThread){
        return com.sun.enterprise.web.connector.grizzly.XAReadTask.class;
    }
    
}
