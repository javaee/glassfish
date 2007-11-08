
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

package com.sun.grizzly.cometd.standalone;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.comet.CometContext;
import com.sun.enterprise.web.connector.grizzly.comet.CometEngine;
import com.sun.enterprise.web.connector.grizzly.standalone.StaticResourcesAdapter;
import com.sun.grizzly.cometd.BayeuxCometHandler;
import com.sun.grizzly.cometd.CometdNotificationHandler;
import com.sun.grizzly.cometd.CometdRequest;
import com.sun.grizzly.cometd.CometdResponse;
import com.sun.grizzly.cometd.EventRouter;
import com.sun.grizzly.cometd.EventRouterImpl;
import java.io.File;
import java.io.IOException;

import org.apache.coyote.Adapter;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.Parameters;

/**
 * Standalone Cometd implementation. This class is used when Cometd is enabled
 * from the Grizzly standalone WebServer. To enable it, just add:
 *
 * -Dcom.sun.grizzly.adapterClass=com.sun.grizzly.cometd.standalone.CometdAdapter
 *
 * @author Jeanfrancois Arcand
 */
public class CometdAdapter extends StaticResourcesAdapter implements Adapter {
    
    /**
     * All request to that context-path will be considered as cometd enabled.
     */
    private String contextPath = "/cometd/cometd";
    
    /**
     * The Bayeux <code>CometHandler</code> implementation.
     */
    private BayeuxCometHandler bayeuxCometHandler;
    
    
    /**
     * The EventRouter used to route JSON message.
     */
    private EventRouter eventRouter;
    
    
    /**
     * Is the BayeuxCometHandler initialized and added to the Grizzly
     * CometEngine.
     */
    private boolean initialized = false;
    
    
    public static final int ADAPTER_NOTES = 1;
    
    
    public CometdAdapter() {
        super();
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext cometContext = cometEngine.register(contextPath);
        cometContext.setExpirationDelay(60 * 60 * 1000);    
        cometContext.setBlockingNotification(true);
        cometContext.setNotificationHandler(new CometdNotificationHandler());
    }
    
    /**
     * Route the request to the cometd implementation. If the request point to
     * a static file, delegate the call to the Grizzly WebServer implementation.
     */
    @Override
    public void service(Request req, final Response res) throws Exception {
        MessageBytes mb = req.requestURI();
        ByteChunk requestURI = mb.getByteChunk();
        String uri = req.requestURI().toString();
        File file = new File(getRootFolder(),uri);
        if (file.isDirectory()) {
            uri += "index.html";
            file = new File(file,uri);
        }

        if (file.canRead()) {
            super.service(req,res);
            return;
        }
             
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext cometContext = cometEngine.getCometContext(contextPath);        
        if (!initialized){
            synchronized(cometContext){
                if (!initialized){                            
                    bayeuxCometHandler = new BayeuxCometHandler();
                    eventRouter = new EventRouterImpl(cometContext);    
                    int mainHandlerHash = 
                            cometContext.addCometHandler(bayeuxCometHandler,true);
                    cometContext.addAttribute(BayeuxCometHandler.BAYEUX_COMET_HANDLER,
                                              mainHandlerHash);
                    initialized = true;
                }
            }
        }
        
        CometdRequest cometdReq = (CometdRequest) req.getNote(ADAPTER_NOTES);
        CometdResponse cometdRes = (CometdResponse) res.getNote(ADAPTER_NOTES);
        if (cometdReq == null) {
            cometdReq = new CometdRequest<Request>(req) {
                
                private boolean requestParametersParsed = false;
                protected byte[] postData = null;
                private ByteChunk chunk = new ByteChunk();
                private byte[] body = new byte[8192];
                
                public String[] getParameterValues(String s) {
                    
                    Parameters parameters = request.getParameters();
                    requestParametersParsed = true;
                    
                    parameters.setEncoding
                            (org.apache.coyote.Constants.DEFAULT_CHARACTER_ENCODING);
                    parameters.setQueryStringEncoding
                            (org.apache.coyote.Constants.DEFAULT_CHARACTER_ENCODING);
                    parameters.handleQueryParameters();
                    int len = request.getContentLength();
                     
                    if (len > 0) {
                        try {
                            
                            byte[] formData = getPostBody();
                            if (formData != null) {
                                parameters.processParameters(formData, 0, len);
                            }
                        } catch (Throwable t) {
                            ; // Ignore
                        }
                    }
                    
                    return parameters.getParameterValues(s);
                }

                protected byte[] getPostBody() throws IOException {                   
                    int len = request.getContentLength();                    
                    int actualLen = readPostBody(chunk, len);
                    if (actualLen == len) {                       
                        chunk.substract(body,0,chunk.getLength());
                        chunk.recycle();
                        return body;
                    }                    
                    return null;
                }
                               
                /**
                 * Read post body in an array.
                 */
                protected int readPostBody(ByteChunk chunk,int len) throws IOException {
                    
                    int offset = 0;
                    do {
                        int inputLen = request.doRead(chunk);
                        if (inputLen <= 0) {
                            return offset;
                        }
                        offset += inputLen;
                    } while ((len - offset) > 0);
                    return len;                   
                }                
            };
            
            cometdRes = new CometdResponse<Response>(res) {
                
                private StringBuffer buf = new StringBuffer();              
                private ByteChunk chunk = new ByteChunk();
                
                public void write(String s) throws IOException {
                    buf.append(s);
                }
                
                public void flush() throws IOException {
                    int length = buf.length();
                    response.addHeader("Server", 
                                       SelectorThread.SERVER_NAME); 
                    response.sendHeaders();
                    chunk.setBytes(buf.toString().getBytes(),0,length);
                    res.doWrite(chunk);   
                    chunk.recycle();
                    buf.delete(0,length);
                }
                
                public void setContentType(String s) {
                    response.setContentType(s);
                }
            };
            
            // Set as notes so we don't create them on every request.'
            req.setNote(ADAPTER_NOTES, cometdReq);
            res.setNote(ADAPTER_NOTES, cometdRes);
        }  else {
            cometdReq.setRequest(req);
            cometdRes.setResponse(res);
        }

        eventRouter.route(cometdReq,cometdRes);        
    }   
}