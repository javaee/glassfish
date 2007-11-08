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

package com.sun.enterprise.web.connector.grizzly;

import org.apache.tomcat.util.buf.ByteChunk;

/**
 * Constants. Inspired from class org.apache.coyote.http11.Constants
 * 
 * @author Jean-Francois Arcand
 */
public final class Constants {


    /**
     * Package name.
     */
    public static final String Package = 
                                    "com.sun.enterprise.web.connector.grizzly";

    public static final int DEFAULT_CONNECTION_LINGER = -1;
    public static final int DEFAULT_CONNECTION_UPLOAD_TIMEOUT = 300000;
    public static final int DEFAULT_SERVER_SOCKET_TIMEOUT = 0;
    public static final boolean DEFAULT_TCP_NO_DELAY = true;

    
    /**
     * The default response-type
     */
    public final static String DEFAULT_RESPONSE_TYPE = 
            "text/plain; charset=iso-8859-1";


    /**
     * The forced request-type
     */
    public final static String FORCED_REQUEST_TYPE = 
           "text/plain; charset=iso-8859-1";
 
    
    /**
     * Default socket timeout
     */
    public final static int DEFAULT_TIMEOUT = 30;
    
    
    /**
     * Default request buffer size
     */
    public final static int DEFAULT_REQUEST_BUFFER_SIZE = 8192;
    
    
    /**
     * Default request header size
     */
    public final static int DEFAULT_HEADER_SIZE = 8 * 1024;
    
    
    /**
     * Default recycle value.
     */
    public final static boolean DEFAULT_RECYCLE = true;
    
    
    /**
     * Default max keep-alive count.
     */
    public final static int DEFAULT_MAX_KEEP_ALIVE = 256;
       
    
    /**
     * Default queue in bytes size.
     */
    public final static int DEFAULT_QUEUE_SIZE = 4096;
    

    // -------------------------------------------------------------- Constants
    
     /**
     * CRLF.
     */
    public static final String CRLF = "\r\n";

    
    /**
     * CR.
     */
    public static final byte CR = (byte) '\r';


    /**
     * LF.
     */
    public static final byte LF = (byte) '\n';


    /**
     * SP.
     */
    public static final byte SP = (byte) ' ';


    /**
     * HT.
     */
    public static final byte HT = (byte) '\t';


    /**
     * COLON.
     */
    public static final byte COLON = (byte) ':';


    /**
     * 'A'.
     */
    public static final byte A = (byte) 'A';


    /**
     * 'a'.
     */
    public static final byte a = (byte) 'a';


    /**
     * 'Z'.
     */
    public static final byte Z = (byte) 'Z';


    /**
     * '?'.
     */
    public static final byte QUESTION = (byte) '?';


    /**
     * Lower case offset.
     */
    public static final byte LC_OFFSET = A - a;


    /**
     * Default HTTP header buffer size.
     */
    public static final int DEFAULT_HTTP_HEADER_BUFFER_SIZE = 48 * 1024;


    /* Various constant "strings" */
    public static final byte[] CRLF_BYTES = ByteChunk.convertToBytes(CRLF);
    public static final byte[] COLON_BYTES = ByteChunk.convertToBytes(": ");
    public static final String CONNECTION = "Connection";
    public static final String CLOSE = "close";
    public static final byte[] CLOSE_BYTES = 
        ByteChunk.convertToBytes(CLOSE);
    public static final String KEEPALIVE = "keep-alive";
    public static final byte[] KEEPALIVE_BYTES = 
        ByteChunk.convertToBytes(KEEPALIVE);
    public static final String CHUNKED = "chunked";
    public static final byte[] ACK_BYTES = 
        ByteChunk.convertToBytes("HTTP/1.1 100 Continue" + CRLF + CRLF);
    public static final String TRANSFERENCODING = "Transfer-Encoding";
    public static final byte[] _200_BYTES = 
        ByteChunk.convertToBytes("200");
    public static final byte[] _400_BYTES = 
        ByteChunk.convertToBytes("400");
    public static final byte[] _404_BYTES = 
        ByteChunk.convertToBytes("404");
    

    /**
     * Identity filters (input and output).
     */
    public static final int IDENTITY_FILTER = 0;


    /**
     * Chunked filters (input and output).
     */
    public static final int CHUNKED_FILTER = 1;


    /**
     * Void filters (input and output).
     */
    public static final int VOID_FILTER = 2;


    /**
     * GZIP filter (output).
     */
    public static final int GZIP_FILTER = 3;


    /**
     * Buffered filter (input)
     */
    public static final int BUFFERED_FILTER = 3;


    /**
     * HTTP/1.0.
     */
    public static final String HTTP_10 = "HTTP/1.0";


    /**
     * HTTP/1.1.
     */
    public static final String HTTP_11 = "HTTP/1.1";
    public static final byte[] HTTP_11_BYTES = 
        ByteChunk.convertToBytes(HTTP_11);


    /**
     * GET.
     */
    public static final String GET = "GET";


    /**
     * HEAD.
     */
    public static final String HEAD = "HEAD";


    /**
     * POST.
     */
    public static final String POST = "POST";

}
