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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Date;

/**
 * Utility class used to generate HTML pages.
 *
 * @author Jean-Francois Arcand
 */
public class HtmlHelper{    
    
    private final static String CSS =
        "H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
        "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
        "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
        "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
        "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
        "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
        "A {color : black;}" +
        "HR {color : #525D76;}";

    /**
     * <code>CharBuffer</code> used to store the HTML response, containing
     * the headers and the body of the response.
     */
    private static CharBuffer reponseBuffer = CharBuffer.allocate(4096);
    

    /**
     * Encoder used to encode the HTML response
     */
    private static CharsetEncoder encoder =
                                          Charset.forName("UTF-8").newEncoder();

    /**
     * HTTP end line.
     */
    private static String NEWLINE = "\r\n";


    /**
     * HTTP OK header
     */
    public final static String OK = "HTTP/1.1 200 OK" + NEWLINE;
    

    /**
     * HTTP Bas Request
     */
    public final static String BAD_REQUEST 
        = "HTTP/1.1 400 Bad Request" + NEWLINE;
 
    
    /**
     * When Grizzlu has reached its connection-queue pool limits, an HTML
     * error pages will to be returned to the clients.
     *
     * @return A <code>ByteBuffer</code> containings the HTTP response.
     */
    public synchronized static ByteBuffer 
            getErrorPage(String message, String code) throws IOException {
        String body = prepareBody(message);
        reponseBuffer.clear();
        reponseBuffer.put(code);
        appendHeaderValue("Content-Type", "text/html");
        appendHeaderValue("Content-Length", body.getBytes().length + "");
        appendHeaderValue("Date", new Date().toString());
        appendHeaderValue("Connection", "Close");
        appendHeaderValue("Server", SelectorThread.SERVER_NAME);
        reponseBuffer.put(NEWLINE);
        reponseBuffer.put(body);
        reponseBuffer.flip();
        return encoder.encode(reponseBuffer);
    }

    
    /**
     * Utility to add headers to the HTTP response.
     */
    private static void appendHeaderValue(String name, String value) {
        reponseBuffer.put(name);
        reponseBuffer.put(": ");
        reponseBuffer.put(value);
        reponseBuffer.put(NEWLINE);
    }


    /**
     * Prepare the HTTP body containing the error messages.
     */
    private static String prepareBody(String message){
        StringBuffer sb = new StringBuffer();

        sb.append("<html><head><title>");
        sb.append(SelectorThread.SERVER_NAME);
        sb.append("</title>");
        sb.append("<style><!--");
        sb.append(CSS);
        sb.append("--></style> ");
        sb.append("</head><body>");
        sb.append("<h1>");
        sb.append(message);
        sb.append("</h1>");
        sb.append("<HR size=\"1\" noshade>");
        sb.append("<h3>").append(SelectorThread.SERVER_NAME)
            .append("</h3>");
        sb.append("</body></html>");
        return sb.toString();
    }

}
