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

package com.sun.enterprise.web.connector.grizzly.standalone;

import com.sun.enterprise.web.connector.grizzly.Constants;
import com.sun.enterprise.web.connector.grizzly.FileCache;
import com.sun.enterprise.web.connector.grizzly.FileCacheFactory;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.SocketChannelOutputBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.apache.coyote.ActionCode;

import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http11.InternalOutputBuffer;
import org.apache.tomcat.util.buf.Ascii;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;

/**
 * Simple HTTP based Web Server. Part of this class is from Tomcat sandbox code
 * from Costin Manolache.
 *
 * @author Jeanfrancois Arcand
 */
public class StaticResourcesAdapter implements Adapter {

    static Properties contentTypes=new Properties();

    static {
        initContentTypes();
    }

    static void initContentTypes() {
        contentTypes.put("html", "text/html");
        contentTypes.put("txt", "text/plain");
        contentTypes.put("css", "text/css");        
    }

    private String rootFolder = ".";

    private File rootFolderF;

    private ConcurrentHashMap<String,File> cache
            = new ConcurrentHashMap<String,File>();

    /**
     * Are we running in GlassFish or Grizzly standalone.
     */
    protected static boolean embeddedInV3 = false;
    
    
    // Temporary hack to find if Grizzly is embedded in GlassFish or not.
    // TODO: Fix me by having a V3 Adapter interface.
    static{
        try{
            embeddedInV3 = 
                (Class.forName("com.sun.enterprise.web.connector.grizzly.standalone.DynamicContentAdapter") != null);
        } catch(Exception ex){
            ; //Swallow
        }
    }

    public StaticResourcesAdapter() {
        this(SelectorThread.getWebAppRootPath());
    }

    public StaticResourcesAdapter(String rootFolder) {


        this.rootFolder = rootFolder;
        rootFolderF = new File(rootFolder);
        try {
            rootFolder = rootFolderF.getCanonicalPath();
            SelectorThread.logger().log(Level.INFO, "New Servicing page from: " 
                + rootFolder);
        
        } catch (IOException e) {
        }
    }
    
    
    public void service(Request req, final Response res) throws Exception {
        MessageBytes mb = req.requestURI();
        ByteChunk requestURI = mb.getByteChunk();
        String uri = req.requestURI().toString();
        if (uri.indexOf("..") >= 0) {
            res.setStatus(404);
            return;
        }
        service(uri, req, res);
    }

    protected void service(String uri, Request req, final Response res) throws Exception {

        // local file
        File resource = cache.get(uri);
        if (resource == null){
            resource = new File(rootFolderF, uri);
            cache.put(uri,resource);
        }

        if (!resource.getCanonicalPath().startsWith(rootFolder)) {
            res.setStatus(404);
            return;
        }

        if (resource.isDirectory()) {
            resource = new File(resource, "index.html");
            cache.put(uri,resource);            
        }

        if (!resource.exists()) {
            SelectorThread.logger().log(Level.INFO,"File not found  " + resource);
            res.setStatus(404);
            return;
        }        
        res.setStatus(200);

        int dot=uri.lastIndexOf(".");
        if( dot > 0 ) {
            String ext=uri.substring(dot+1);
            String ct=getContentType(ext);
            if( ct!=null) {
                res.setContentType(ct);
            }
        }

        res.setContentLength((int)resource.length());        
        res.sendHeaders();

        /* Workaround Linux NIO bug
         * 6427312: (fc) FileChannel.transferTo() throws IOException "system call interrupted"
         * 5103988: (fc) FileChannel.transferTo should return -1 for EAGAIN instead throws IOException
         * 6253145: (fc) FileChannel.transferTo on Linux fails when going beyond 2GB boundary
         * 6470086: (fc) FileChannel.transferTo(2147483647, 1, channel) cause "Value too large" exception 
         */
        FileInputStream fis = new FileInputStream(resource);
        byte b[] = new byte[8192];
        ByteChunk chunk = new ByteChunk();
        int rd = 0;
        while ((rd = fis.read(b)) > 0) {
            chunk.setBytes(b, 0, rd);
            res.doWrite(chunk);
        }

        if (!embeddedInV3) {
            try{
                req.action( ActionCode.ACTION_POST_REQUEST , null);
            }catch (Throwable t) {
                t.printStackTrace();
            }

            res.finish();
        }
    }
    
    
    public String getContentType( String ext ) {
        return contentTypes.getProperty( ext, "text/plain" );
    }

    
    public void afterService(Request req, Response res) 
        throws Exception {
        // Recycle the wrapper request and response
        req.recycle();
        res.recycle();     
    }

    
    public void fireAdapterEvent(String string, Object object) {
    }

    
    public String getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(String newRoot) {
        this.rootFolder = newRoot;
    }

    
}
