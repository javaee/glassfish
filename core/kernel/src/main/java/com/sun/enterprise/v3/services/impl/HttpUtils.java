/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.util.buf.Ascii;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Utility class for parsing ByteBuffer
 * @author Jeanfrancois
 */
public class HttpUtils {

    /** 
     * Return the Context root of the request.
     */
    public static String findContextRoot(ByteBuffer byteBuffer) throws IOException{                          
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();
      
        if (byteBuffer.position() == 0){
            throw new IllegalStateException("Invalid state");
        }
       
        byteBuffer.position(0);
        byteBuffer.limit(curPosition);
        int state =0;
        int start =0;
        int end = 0;  
        int separatorPos = -1;
        int lastPos = -1;
        try {                         
            byte c;            
            
            // Rule b - try to determine the context-root
            while(byteBuffer.hasRemaining()) {
                c = byteBuffer.get();
                // State Machine
                // 0 - Search for the first SPACE ' ' between the method and the
                //     the request URI
                // 1 - Search for the second SPACE ' ' between the request URI
                //     and the method
                switch(state) {
                    case 0: // Search for first ' '
                        if (c == 0x20){
                            state = 1;
                            start = byteBuffer.position();
                            lastPos = start;
                        }
                        break;
                    case 1: // Search for next valid '/', and then ' '
                        if (c == 0x2f && separatorPos == -1){
                            if (byteBuffer.position() != lastPos + 1){
                                separatorPos = byteBuffer.position() - 1;
                            } else if (byteBuffer.position() - 1 == start){
                                start = byteBuffer.position();
                                lastPos = start;
                            } else {
                                lastPos = byteBuffer.position();
                            }                            
                         } else if (c == 0x20){
                            if (separatorPos != -1){
                                end = separatorPos;
                            } else {
                                end = byteBuffer.position() - 1;
                            }
                            
                            if (byteBuffer.hasArray()) { // Optimization, if ByteBuffer has underlying array
                                return new String(byteBuffer.array(), 
                                        byteBuffer.arrayOffset() + start, 
                                        end - start);
                            } else {
                                byte[] contextRoot = new byte[end - start];
                                byteBuffer.position(start);
                                byteBuffer.limit(end);
                                byteBuffer.get(contextRoot);
                                return new String(contextRoot);
                            }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected state");
                }      
            }
            throw new IllegalStateException("Invalid request");
        } finally {     
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);                               
        }       
    }        
    
    
    /**
     * Specialized utility method: find a sequence of lower case bytes inside
     * a ByteChunk.
     */
    public static int findBytes(ByteBuffer byteBuffer, byte[] b) {
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();
      
        if (byteBuffer.position() == 0){
            throw new IllegalStateException("Invalid state");
        }
       
        byteBuffer.position(0);
        byteBuffer.limit(curPosition);
        try {                         
            byte first = b[0];
            int start = 0;
            int end = curPosition;

            // Look for first char 
            int srcEnd = b.length;

            for (int i = start; i <= (end - srcEnd); i++) {
                if (Ascii.toLower(byteBuffer.get(i)) != first) continue;
                // found first char, now look for a match
                int myPos = i+1;
                for (int srcPos = 1; srcPos < srcEnd; ) {
                        if (Ascii.toLower(byteBuffer.get(myPos++)) != b[srcPos++])
                    break;
                        if (srcPos == srcEnd) return i - start; // found it
                }
            }
            return -1;
        } finally {
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);                
        }
    }
    
    
    /**
     * Return the host value, or null if not found.
     * @param byteBuffer the request bytes.
     */
    public static String findHost(ByteBuffer byteBuffer){
         int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();

        // Rule a - If nothing, return to the Selector.
        if (byteBuffer.position() == 0)
            return null;
       
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
                        String result;
                        if (byteBuffer.hasArray()) {// Optimization, if ByteBuffer has underlying array
                            int startPos = byteBuffer.position();
                            int endPos = startPos;
                            while (c != 0x0d && c != 0x0a) {
                                endPos++;
                                c = byteBuffer.get();
                            }
                            result = new String(byteBuffer.array(), 
                                    byteBuffer.arrayOffset() + startPos, 
                                    endPos - startPos);
                        } else {
                            StringBuilder sb = new StringBuilder();
                            while (c != 0x0d && c != 0x0a) {
                                sb.append((char) c);
                                c = byteBuffer.get();
                            }
                            result = sb.toString();
                        }
                        return result.trim();                        
                    default:
                        throw new IllegalArgumentException("Unexpected state");
                }      
            }
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
}
