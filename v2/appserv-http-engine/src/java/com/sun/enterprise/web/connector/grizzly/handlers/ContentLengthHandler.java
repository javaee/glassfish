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

package com.sun.enterprise.web.connector.grizzly.handlers;
import com.sun.enterprise.web.connector.grizzly.FileCacheFactory;
import com.sun.enterprise.web.connector.grizzly.Handler;
import com.sun.enterprise.web.connector.grizzly.FileCache;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.algorithms.ContentLengthAlgorithm;
import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.tomcat5.CoyoteConnector;
import org.apache.coyote.tomcat5.CoyoteRequest;
import org.apache.coyote.tomcat5.CoyoteAdapter;
import org.apache.tomcat.util.http.MimeHeaders;


/**
 * This <code>Handler</code> is invoked after the request line has been parsed. 
 *
 * @author Jeanfrancois Arcand
 */
public class ContentLengthHandler implements Handler<Request> {
    
    
    /**
     * The <code>SocketChannel</code> used to send a static resources.
     */
    private SocketChannel socketChannel;
 
    
    /**
     * The FileCache mechanism used to cache static resources.
     */
    protected FileCache fileCache;    
    
    
    /**
     * The <code>Algorithm</code> associated with this handler
     */
    private ContentLengthAlgorithm algorithm;
    
    
    // ---------------------------------------------------------------------//
    
    
    public ContentLengthHandler(ContentLengthAlgorithm algorithm){
        this.algorithm = algorithm;
    }
    
    
    /**
     * Attach a <code>SocketChannel</code> to this object.
     */
    public void attachChannel(SocketChannel socketChannel){
        this.socketChannel = socketChannel;
        if ( fileCache == null && socketChannel != null){
            fileCache = FileCacheFactory.getFactory(
                    socketChannel.socket().getLocalPort()).getFileCache();
        }        
    }
    
    
    /**
     * Add a request URI to the <code>FileCache</code> or use the cache to
     * send the static resources.
     */
    public int handle(Request request, int handlerCode) throws IOException{
        if ( socketChannel == null || !FileCacheFactory.isEnabled())
            return Handler.CONTINUE;
        
        // If not initialized, dont' continue
        if ( fileCache == null && handlerCode != Handler.RESPONSE_PROCEEDED){
             return Handler.CONTINUE;  
        }
    
        if ( handlerCode == Handler.RESPONSE_PROCEEDED ){            
            CoyoteRequest cr = 
                (CoyoteRequest)request.getNote(CoyoteAdapter.ADAPTER_NOTES);
            
            if ( cr != null && cr.getWrapper() != null){

                String mappedServlet = cr.getWrapper().getName();
                
                if ( !mappedServlet.equals(FileCache.DEFAULT_SERVLET_NAME) ) 
                    return Handler.CONTINUE;
                
                if ( cr.getContext().findConstraints().length == 0 
                    && cr.getContext().findFilterDefs().length == 0 ){
                    
                    if (!fileCache.isEnabled()) return Handler.CONTINUE;
                    
                    String docroot;
                    if ( cr.getContextPath().equals("") ){
                        docroot = cr.getContext().getDocBase();
                    } else {
                        docroot = SelectorThread.getWebAppRootPath();
                    }                
                    String requestURI = cr.getRequestURI();
                    Response response = cr.getCoyoteRequest().getResponse();  
                    MimeHeaders headers = response.getMimeHeaders();
                    boolean xPoweredBy = (
                            (CoyoteConnector)cr.getConnector()).isXpoweredBy();

                    fileCache.add(mappedServlet,docroot,requestURI,headers, 
                            xPoweredBy);
                }
            }       
        } else if ( handlerCode == Handler.REQUEST_BUFFERED ) {
            if ( algorithm.startReq != -1 ){
                if ( fileCache.sendCache(algorithm.ascbuf,
                                         algorithm.startReq, 
                                         algorithm.lengthReq, 
                                         socketChannel, true ) ){
                    return Handler.BREAK; 
                }
            }            
        }     
        return Handler.CONTINUE;
    }   
}
