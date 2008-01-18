package com.sun.enterprise.v3.services.impl;

import com.sun.grizzly.tcp.Response;
import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.util.buf.ByteChunk;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 16, 2008
 * Time: 3:30:51 PM
 * To change this template use File | Settings | File Templates.
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
