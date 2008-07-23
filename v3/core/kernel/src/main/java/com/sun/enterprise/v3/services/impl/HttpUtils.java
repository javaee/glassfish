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
import com.sun.grizzly.util.buf.ByteChunk;
import com.sun.grizzly.util.buf.HexUtils;
import com.sun.grizzly.util.buf.MessageBytes;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 * Utility class for parsing ByteBuffer
 * @author Jeanfrancois
 */
public class HttpUtils {
 
    private static final int MAX_CONTEXT_ROOT_LENGTH = 2048;
    
    private final static String CSS =
            "H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
            "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
            "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
            "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
            "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
            "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
            "A {color : black;}" +
            "HR {color : #525D76;}";

    
    public static byte[] readRequestLine(SelectionKey selectionKey,
            ByteBuffer byteBuffer, int timeout) throws IOException {

        int readBytes = -1;

        do {
            byte[] contextRoot = findContextRoot(byteBuffer);
            if (contextRoot == null) {
                if (byteBuffer.position() > MAX_CONTEXT_ROOT_LENGTH ||
                        (readBytes = GrizzlyUtils.readToWorkerThreadBuffers(selectionKey,
                        timeout)) <= 0) {
                    return null;
                }
            } else {
                return contextRoot;
            }

        } while (readBytes > 0);
        return null;
    }

    
    public static byte[] readHost(SelectionKey selectionKey,
            ByteBuffer byteBuffer, int timeout) throws IOException {

        int readBytes = -1;

        do {
            byte[] host = findHost(byteBuffer);
            if (host == null) {
                // TODO: We must parse until /r/n/r/n
                if (byteBuffer.position() > MAX_CONTEXT_ROOT_LENGTH ||
                        (readBytes = GrizzlyUtils.readToWorkerThreadBuffers(selectionKey,
                        timeout)) <= 0) {
                    return null;
                }
            } else {
                return host;
            }

        } while (readBytes > 0);
        return null;
    }

    
    /** 
     * Parse the raw request line and return the context-root, without any 
     * query parameters.
     * @param byteBuffer The raw bytes.
     */
    public static byte[] findContextRoot(ByteBuffer byteBuffer) throws IOException {
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();

        if (byteBuffer.position() == 0) {
            return null;
        }

        byteBuffer.flip();
        int state = 0;
        int start = 0;
        int end = 0;
        int separatorPos = -1;
        try {
            byte c = -1;
            byte prev = -1;
            byte prevPrev;
            // Rule b - try to determine the context-root
            while (byteBuffer.hasRemaining()) {
                prevPrev = prev;
                prev = c;
                c = byteBuffer.get();
                // State Machine
                // 0 - Search for the first SPACE ' ' between the method and the
                //     the request URI
                // 1 - Search for the second SPACE ' ' between the request URI
                //     and the method
                switch (state) {
                    case 0: // Search for first ' '
                        if (c == 0x20) {
                            state = 1;
                            start = byteBuffer.position();
                        }
                        break;
                    case 1: // Search for next valid '/', and then ' '
                        if (c == 0x2f) {
                            // check for '://', which we should skip
                            if (prev == 0x2f && prevPrev == 0x3a) {
                                start = -1;
                            } else if (start == -1) {
                                start = byteBuffer.position();
                                separatorPos = -1;
                            } else if (separatorPos == -1) {
                                if (byteBuffer.position() != start + 1) {
                                    separatorPos = byteBuffer.position() - 1;
                                } else {
                                    start = byteBuffer.position();
                                }
                            }
                        // Search for ? , space ' ' or /
                        } else if (c == 0x20 || c == 0x3b || c == 0x3f) {
                            // Grab the first '/'
                            start = start -1;
                            end = byteBuffer.position() - 1;
                            
                            byteBuffer.position(start);
                            byteBuffer.limit(end);
                                                       
                            byte[] contextRoot = new byte[end - start];
                            byteBuffer.get(contextRoot);
                            return contextRoot;
                        }
                        break;
                    default:
                        return null;
                }
            }
            return null;
        } finally {
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);
        }
    }


    /**
     * Return the host value, or null if not found.
     * @param byteBuffer the request bytes.
     */
    public static byte[] findHost(ByteBuffer byteBuffer) {
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();

        // Rule a - If nothing, return to the Selector.
        if (byteBuffer.position() == 0) {
            return null;
        }
        byteBuffer.position(0);
        byteBuffer.limit(curPosition);

        int state = 0;
        try {
            byte c;

            // Rule b - try to determine the host header
            while (byteBuffer.hasRemaining()) {
                c = (byte) Ascii.toLower(byteBuffer.get());
                switch (state) {
                    case 0: // Search for first 'h'
                        if (c == 0x68) {
                            state = 1;
                        } else {
                            state = 0;
                        }
                        break;
                    case 1: // Search for next 'o'
                        if (c == 0x6f) {
                            state = 2;
                        } else {
                            state = 0;
                        }
                        break;
                    case 2: // Search for next 's'
                        if (c == 0x73) {
                            state = 3;
                        } else {
                            state = 0;
                        }
                        break;
                    case 3: // Search for next 't'
                        if (c == 0x74) {
                            state = 4;
                        } else {
                            state = 0;
                        }
                        break;
                    case 4: // Search for next ':'
                        if (c == 0x3a) {
                            state = 5;
                        } else {
                            state = 0;
                        }
                        break;
                    case 5: // Get the Host
                        int startPos = byteBuffer.position();
                        int endPos = startPos;
                        while (c != 0x3a && c != 0x0d && c != 0x0a) {
                            endPos++;
                            c = byteBuffer.get();
                        }
                        endPos--;
                        
                        byte[] host = new byte[endPos - startPos];
                        byteBuffer.position(startPos);
                        byteBuffer.limit(endPos);
                        byteBuffer.get(host);
                        return host;

                    default:
                        throw new IllegalArgumentException("Unexpected state");
                }
            }
            return null;
        } finally {
            byteBuffer.limit(curLimit);
            byteBuffer.position(curPosition);
        }
    }
    
    
    /**
     * Specialized utility method: find a sequence of lower case bytes inside
     * a {@link ByteBuffer}.
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
     * Parse host.
     */
    public static void parseHost(MessageBytes hostMB) throws IOException{

        if (hostMB == null || hostMB.isNull()) {
            throw new IOException("Invalid Host");
        }

        ByteChunk valueBC = hostMB.getByteChunk();
        byte[] valueB = valueBC.getBytes();
        int valueL = valueBC.getLength();
        int valueS = valueBC.getStart();
        int colonPos = -1;
        
        // TODO: Cache instance?
        char[] hostNameC = new char[0];
        if (hostNameC.length < valueL) {
            hostNameC = new char[valueL];
        }

        boolean ipv6 = (valueB[valueS] == '[');
        boolean bracketClosed = false;
        for (int i = 0; i < valueL; i++) {
            char b = (char) valueB[i + valueS];
            hostNameC[i] = b;
            if (b == ']') {
                bracketClosed = true;
            } else if (b == ':') {
                if (!ipv6 || bracketClosed) {
                    colonPos = i;
                    break;
                }
            }
        }

        if (colonPos < 0) {
            hostMB.setChars(hostNameC, 0, valueL);
        } else {
            hostMB.setChars(hostNameC, 0, colonPos);

            int port = 0;
            int mult = 1;
            for (int i = valueL - 1; i > colonPos; i--) {
                int charValue = HexUtils.DEC[(int) valueB[i + valueS]];
                if (charValue == -1) {
                    throw new IOException("Invalid Host");
                }
                port = port + (charValue * mult);
                mult = 10 * mult;
            }
         }
    }  

    
    public final static byte[] getErrorPage(String serverName, String message) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><head><title>");
        sb.append(serverName);
        sb.append("</title>");
        sb.append("<style><!--");
        sb.append(CSS);
        sb.append("--></style> ");
        sb.append("</head><body>");
        sb.append("<h1>");
        sb.append(message);
        sb.append("</h1>");
        sb.append("</h3> type Status report<br>message<br>description The requested resource () is not available</h3>");
        sb.append("<HR size=\"1\" noshade>");
        sb.append("<h3>").append(serverName).append("</h3>");
        sb.append("</body></html>");
        return sb.toString().getBytes();
    }
    
}

