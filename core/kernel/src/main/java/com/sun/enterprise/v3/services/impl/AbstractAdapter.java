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

import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.util.buf.ByteChunk;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * @author Jerome Dochez
 */
public abstract class AbstractAdapter {

    protected static void sendError(Response res, String message) throws IOException {
        res.setStatus(404);
        res.setContentType("text/html");
        res.addHeader("Server", "GlassFish/v3");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(bos);
        printWriter.println("<html><head>Error</head><body><h1>");
        printWriter.print(message);
        printWriter.print("</h1></body></html>");
        printWriter.flush();
        res.setContentLength(bos.size());
        res.sendHeaders();

        ByteChunk chunk = new ByteChunk();
        chunk.setBytes(bos.toByteArray(), 0, bos.size());
        res.doWrite(chunk);
    }


    /**
     * Finish the response and recycle the request/response tokens. Base on
     * the connection header, the underlying socket transport will be closed
     */
    public void afterService(Request req, Response res) throws Exception {
        // Event if the sub Adapter might have called that method, it is
        // safer to recycle again in case they failed to do it.
        req.recycle();
        res.recycle();
    }


    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Adapter.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {
    }


}
