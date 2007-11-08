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

package com.sun.enterprise.appclient.jws;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 *Ad hoc servlet registered to receive requests for the user-specified (or
 *defaulted) context-root.
 *<p>
 *The user can (but is not required to) specify a context root in the runtime
 *deployment descriptor for the app client.  That context root - of, if omitted,
 *a default value computed by the app server - is registered dynamically so
 *requests to that context root are routed to an instance of this servlet.  In
 *turn, this servlet forwards such requests to the system web application that
 *actually processes all Java Web Start requests for app clients.
 *
 * @author tjquinn
 */
public class JWSAdHocServlet extends HttpServlet {

    /** servlet init parameter name for specifying the context root value */
    public static final String CONTEXT_ROOT_PARAMETER_NAME = "context-root";
    
    /** servlet init parameter name for specifying the category value (appclient or application) */
    public static final String CATEGORY_PARAMETER_NAME = "category";
    
    /** the context for the system web app that handles all Java Web Start requests */
    private ServletContext jwsAppContext = null;
    
    /** a dispatcher used in forwarding requests to the system web app */
    private RequestDispatcher systemWebAppDispatcher = null;
    
    /** the actual context root, obtained from the init parameter, to which reqs should be dispatched */
    private String targetContextRoot = null;
    
    /** the category from the init parameter */
    private String category = null;
    
    /** Creates a new instance of JWSAdHocServlet */
    public JWSAdHocServlet() {
    }
    
    /**
     *Responds to any method.
     *@param the incoming request
     *@param the outgoing response
     *@throws ServletException in case of any errors
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        /*
         *Use the context for the Java Web Start system web app and forward this request
         *to that app.
         */
        JWSAdHocServletRequestWrapper wrappedRequest = new JWSAdHocServletRequestWrapper(request, targetContextRoot, category);
        try {
            getJWSRequestDispatcher().forward(wrappedRequest, response);
        } catch (Throwable thr) {
            throw new ServletException("Error dispatching request to Java Web Start app client application", thr);
        }
    }
    
    /**
     *Init method responsible for retrieiving init params.
     */
    public void init() {
        ServletConfig config = getServletConfig();
        
        targetContextRoot = config.getInitParameter(CONTEXT_ROOT_PARAMETER_NAME);
        category = config.getInitParameter(CATEGORY_PARAMETER_NAME);
    }
    
    /**
     *Returns the context for the Java Web Start app client system web app.
     *@return the servlet context for the system web app
     */
    private ServletContext getJWSAppContext() {
        if (jwsAppContext == null) {
            String uri = NamingConventions.webAppURI();
            jwsAppContext = getServletContext().getContext(uri);
        }
        return jwsAppContext;
    }
    
    /**
     *Returns the dispatcher to the system web app.
     *@return the request dispatcher
     */
    private RequestDispatcher getJWSRequestDispatcher() {
        if (systemWebAppDispatcher == null) {
            String uri = NamingConventions.webAppURI();
            WebPath webPath = new WebPath(uri);
            
            ServletContext sc = getServletContext().getContext(webPath.contextRoot());
            String servletContextName = sc.getServletContextName();
            systemWebAppDispatcher = sc.getRequestDispatcher(webPath.path() + "/" + category + targetContextRoot);
        }
        return systemWebAppDispatcher;
    }
}
