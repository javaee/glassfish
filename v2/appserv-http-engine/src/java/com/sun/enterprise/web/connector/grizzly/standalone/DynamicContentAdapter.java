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

package com.sun.enterprise.web.connector.grizzly.standalone;

import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.ByteChunk;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.Date;
import java.util.Locale;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.SocketChannelOutputBuffer;


/**
 * Abstract Adapter that contains all the common behaviour of the Adapter implmentation
 * for standalone usage as well as embedded use.
 *
 * @author Jerome Dochez
 */
abstract public class DynamicContentAdapter extends StaticResourcesAdapter {
    
    protected static final String RFC_2616_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    
    protected String contextRoot=null;
    
    public DynamicContentAdapter(String publicDirectory) {
        super(publicDirectory);
    }
    
    abstract protected int getTokenID();
    
    abstract protected void serviceDynamicContent(Request req, Response res) throws IOException;
    
    public void afterService(Request req, Response res) throws Exception {
        ; // Let the GrizzlyAdapter handle the life cycle of the request/response.
    }
    
    
    public void fireAdapterEvent(String type, Object data) {
         ; // Let the GrizzlyAdapter handle the life cycle of the request/response.
    }

    
    private boolean modifiedSince(Request req, File file) {
        try {
            String since = req.getMimeHeaders().getHeader("If-Modified-Since");
            if (since == null) {
                return false;
            }
            
            Date date = new SimpleDateFormat(RFC_2616_FORMAT, Locale.US).parse(since);
            if (date.getTime() > file.lastModified()) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            return false;
        }
    }
    
    public void service(Request req, Response res) throws Exception {
        MessageBytes mb = req.requestURI();
        ByteChunk requestURI = mb.getByteChunk();
        
        try{
            String uri = requestURI.toString();
            if (contextRoot!=null && requestURI.startsWith(contextRoot)) {
                uri = uri.substring(contextRoot.length());
            }
            File file = new File(getRootFolder(),uri);
            if (file.isDirectory()) {
                uri += "index.html";
                file = new File(file,uri);
            }
            
            if (file.canRead()) {
                super.service(uri, req, res);
                return;
            } else {
                serviceDynamicContent(req, res);
            }
        } catch (Exception e) {
            if (SelectorThread.logger().isLoggable(Level.SEVERE)) {
                SelectorThread.logger().log(Level.SEVERE, e.getMessage());
            }
            
            throw e;
        }
    }
   
    
    /**
     * Simple InputStream that wrap the Grizzly internal object.
     */
    private class GrizzlyInputStream extends InputStream {
        
        public RequestTupple rt;
        
        public int read() throws IOException {
            return rt.readChunk.substract();
        }
        
        public int read(byte[] b) throws IOException {
            return read(b,0,b.length);
        }
        
        public int read(byte[] b, int off, int len) throws IOException {
            return rt.readChunk.substract(b,off,len);
        }
    }
    
    /**
     * Statefull token used to share information with the Containers.
     */
    public class RequestTupple implements ByteChunk.ByteInputChannel{
        
        public ByteChunk readChunk;
        
        public Request req;
        
        public GrizzlyInputStream inputStream;
        
        public RequestTupple(){
            readChunk = new ByteChunk();
            readChunk.setByteInputChannel(this);
            readChunk.setBytes(new byte[8192],0,8192);
            
            inputStream = new GrizzlyInputStream();
            inputStream.rt = this;
        }
        
        public int realReadBytes(byte[] b, int off, int len) throws IOException {
            req.doRead(readChunk);
            return readChunk.substract(b,off,len);
        }
        
        public void recycle(){
            req = null;
            readChunk.recycle();
        }
        
    }
    
    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
    
    public String getContextRoot() {
        return contextRoot;
    }
    
}
