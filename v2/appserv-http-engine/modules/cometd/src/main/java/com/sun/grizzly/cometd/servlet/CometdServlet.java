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
package com.sun.grizzly.cometd.servlet;

import com.sun.enterprise.web.connector.grizzly.comet.CometContext;
import com.sun.enterprise.web.connector.grizzly.comet.CometEngine;
import com.sun.grizzly.cometd.BayeuxCometHandler;
import com.sun.grizzly.cometd.CometdNotificationHandler;
import com.sun.grizzly.cometd.CometdRequest;
import com.sun.grizzly.cometd.CometdResponse;
import com.sun.grizzly.cometd.EventRouter;
import com.sun.grizzly.cometd.EventRouterImpl;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Simple CometdChat that route Cometd Request to the EventRouter.
 *
 * @author Jeanfrancois Arcand
 * @author TAKAI, Naoto
 */
public class CometdServlet extends HttpServlet{
    
    private static final long DEFAULT_EXPIRATION_DELAY = 60 * 5 * 1000;
    
    /**
     * All request to that context-path will be considered as cometd enabled.
     */
    private String contextPath;
    
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
    
    
    public CometdServlet() {
    }
    
    
    /**
     * Initialize the Servlet by creating the CometContext.
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        contextPath = config.getInitParameter("contextPath");
        if (contextPath == null) {
            contextPath = config.getServletContext().getContextPath() + "/cometd";
        } 
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext cometContext = cometEngine.register(contextPath);
        
        String expire = config.getInitParameter("expirationDelay");
        if (expire == null) {
            cometContext.setExpirationDelay(DEFAULT_EXPIRATION_DELAY);
        } else {
            cometContext.setExpirationDelay(Long.parseLong(expire));
        }
        cometContext.setBlockingNotification(true);
        cometContext.setNotificationHandler(new CometdNotificationHandler());
    }
    
    
    @Override    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ; // Nothing
    }
    
    
    @Override    
    public void doPost(HttpServletRequest hreq, HttpServletResponse hres)
        throws ServletException, IOException {
        
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext cometContext = cometEngine.getCometContext(contextPath);
        
        // XXX Pool instances
        CometdRequest cometdReq = new CometdRequest<HttpServletRequest>(hreq){            
            public String[] getParameterValues(String s) {
                return request.getParameterValues(s);
            }
        };
        
        CometdResponse cometdRes = new CometdResponse<HttpServletResponse>(hres){            
            public void write(String s) throws IOException{
                response.getWriter().write(s);
            }

            public void flush() throws IOException{
                response.getWriter().flush();
            }

            public void setContentType(String s) {
                response.setContentType(s);
            }
        };        
        
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
        
        eventRouter.route(cometdReq,cometdRes);
    }

}
