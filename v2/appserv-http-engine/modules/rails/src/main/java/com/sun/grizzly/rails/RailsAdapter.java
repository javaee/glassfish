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
package com.sun.grizzly.rails;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

import org.apache.coyote.ActionCode;
import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http11.InternalOutputBuffer;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.jruby.Ruby;
import org.jruby.RubyException;
import org.jruby.RubyIO;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.SocketChannelOutputBuffer;
import com.sun.enterprise.web.connector.grizzly.standalone.StaticResourcesAdapter;

/**
 * Adapter implementation that brige JRuby on Rails with Grizzly.
 *
 * @author TAKAI Naoto
 * @author Jean-Francois Arcand
 */
public class RailsAdapter extends StaticResourcesAdapter 
        implements Adapter{

    private static final String RFC_2616_FORMAT = "EEE, d MMM yyyy HH:mm:ss z";
    
    private static final int RAILS_TOKEN = 1;

    private final String publicDirectory;

    private RubyObjectPool pool = null;
    
    private RailAsyncFilter railAsyncFilter;
    
    public RailsAdapter(RubyObjectPool pool, RailAsyncFilter railAsyncFilter) {
        super();
        this.pool = pool;
        this.publicDirectory = pool.getRailsRoot() + "/public";
        this.railAsyncFilter = railAsyncFilter;
    }

    public void afterService(Request req, Response res) throws Exception {
        try {
            req.action(ActionCode.ACTION_POST_REQUEST, null);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            // Recycle the wrapper request and response
            req.recycle();
            res.recycle();
        }
    }

    public void fireAdapterEvent(String type, Object data) {
    }

    public void service(Request req, Response res) throws Exception { 
        MessageBytes mb = req.requestURI();
        ByteChunk requestURI = mb.getByteChunk();

        try{
            String uri = requestURI.toString();
            File file = new File(publicDirectory,uri);
            if (file.isDirectory()) {
                uri += "index.html";
                file = new File(file,uri);
            }

            if (file.canRead()) {
                super.service(req, res);      
                return;
            } else {
                serviceRails(req, res);
            }

            res.finish();
        } catch (Exception e) {
            if (SelectorThread.logger().isLoggable(Level.SEVERE)) {
                SelectorThread.logger().log(Level.SEVERE, e.getMessage());
            }

            throw e;
        }
    }

    private SocketChannel getChannel(Response res) {
        SocketChannelOutputBuffer buffer = (SocketChannelOutputBuffer) res.getOutputBuffer();
        SocketChannel channel = buffer.getChannel();

        return channel;
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


    private void serviceRails(Request req, Response res) throws IOException {
        Ruby runtime = null;            
        RailsToken rt = (RailsToken)req.getNote(RAILS_TOKEN);
        if (rt == null){
            rt = new RailsToken();
        }
        rt.req = req;        
        try {
            runtime = pool.bollowRuntime();
            if (runtime == null){
                throw new IllegalStateException();
            }

            req.doRead(rt.readChunk);
            ((InternalOutputBuffer)res.getOutputBuffer()).commit();
            res.setCommitted(true);

            IRubyObject reqObj = JavaEmbedUtils.javaToRuby(runtime, req);
            IRubyObject loggerObj = JavaEmbedUtils.javaToRuby(runtime, SelectorThread.logger());

            OutputStream os = 
                ((InternalOutputBuffer)res.getOutputBuffer()).getOutputStream();
            
            RubyIO iObj = new RubyIO(runtime, rt.inputStream);
            RubyIO oObj = new RubyIO(runtime, os);

            runtime.defineReadonlyVariable("$req", reqObj);
            runtime.defineReadonlyVariable("$stdin", iObj);
            runtime.defineReadonlyVariable("$stdout", oObj);
            runtime.defineReadonlyVariable("$logger", loggerObj);

            runtime.getLoadService().load("dispatch.rb");
        } catch (RaiseException e) {
            RubyException exception = e.getException();

            System.err.println(e.getMessage());
            exception.printBacktrace(System.err);

            throw e;
        } finally {
            rt.recycle();
            req.setNote(RAILS_TOKEN,rt);
            if (runtime != null) {
                pool.returnRuntime(runtime);
                railAsyncFilter.unpark();
            }
        }
    }
    
    private class RailsInputStream extends InputStream{
        
        public RailsToken rt;
        
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
     * Statefull token used to share information with the Rails runtime
     * in a thread safe manner.
     */
    private class RailsToken implements ByteChunk.ByteInputChannel{
        
        public ByteChunk readChunk;

        public Request req;

        public RailsInputStream inputStream;      
             
        public RailsToken(){
            readChunk = new ByteChunk();
            readChunk.setByteInputChannel(this);
            readChunk.setBytes(new byte[8192],0,8192);

            inputStream = new RailsInputStream();
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

}
