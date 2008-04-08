

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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


package org.apache.catalina.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.InstanceEvent;
import org.apache.catalina.Logger;
//START OF 6364900
import org.apache.catalina.Manager;
//END OF 6364900
import org.apache.catalina.Request;
import org.apache.catalina.Response;
//START OF 6364900
import org.apache.catalina.Session;
//END OF 6364900
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.core.StandardWrapper;
//START OF 6364900
import org.apache.catalina.session.StandardSession;
//END OF 6364900
import org.apache.catalina.util.InstanceSupport;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.security.SecurityUtil;
import com.sun.org.apache.commons.beanutils.PropertyUtils;
import com.sun.org.apache.commons.logging.Log;
import com.sun.org.apache.commons.logging.LogFactory;
import org.apache.coyote.tomcat5.CoyoteRequest;
import org.apache.coyote.tomcat5.CoyoteRequestFacade;

/**
 * Standard implementation of <code>RequestDispatcher</code> that allows a
 * request to be forwarded to a different resource to create the ultimate
 * response, or to include the output of another resource in the response
 * from this resource.  This implementation allows application level servlets
 * to wrap the request and/or response objects that are passed on to the
 * called resource, as long as the wrapping classes extend
 * <code>javax.servlet.ServletRequestWrapper</code> and
 * <code>javax.servlet.ServletResponseWrapper</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.16 $ $Date: 2007/02/26 22:57:08 $
 */

final class ApplicationDispatcher
    implements RequestDispatcher {


    protected class PrivilegedForward implements PrivilegedExceptionAction {
        private ServletRequest request;
        private ServletResponse response;

        PrivilegedForward(ServletRequest request, ServletResponse response)
        {
            this.request = request;
            this.response = response;
        }

        public Object run() throws java.lang.Exception {
            doForward(request,response);
            return null;
        }
    }

    protected class PrivilegedInclude implements PrivilegedExceptionAction {
        private ServletRequest request;
        private ServletResponse response;

        PrivilegedInclude(ServletRequest request, ServletResponse response)
        {
            this.request = request;
            this.response = response;
        }

        public Object run() throws ServletException, IOException {
            doInclude(request,response);
            return null;
        }
    }

    /**
     * Used to pass state when the request dispatcher is used. Using instance
     * variables causes threading issues and state is too complex to pass and
     * return single ServletRequest or ServletResponse objects.
     */
    private class State {
        State(ServletRequest request, ServletResponse response,
                boolean including) {
            this.outerRequest = request;
            this.outerResponse = response;
            this.including = including;
        }

        /**
         * The outermost request that will be passed on to the invoked servlet.
         */
        ServletRequest outerRequest = null;


        /**
         * The outermost response that will be passed on to the invoked servlet.
         */
        ServletResponse outerResponse = null;
        
        /**
         * The request wrapper we have created and installed (if any).
         */
        ServletRequest wrapRequest = null;


        /**
         * The response wrapper we have created and installed (if any).
         */
        ServletResponse wrapResponse = null;
        
        /**
         * Are we performing an include() instead of a forward()?
         */
        boolean including = false;
    }

    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class, configured according to the
     * specified parameters.  If both servletPath and pathInfo are
     * <code>null</code>, it will be assumed that this RequestDispatcher
     * was acquired by name, rather than by path.
     *
     * @param wrapper The Wrapper associated with the resource that will
     *  be forwarded to or included (required)
     * @param requestURI The request URI to this resource (if any)
     * @param servletPath The revised servlet path to this resource (if any)
     * @param pathInfo The revised extra path information to this resource
     *  (if any)
     * @param queryString Query string parameters included with this request
     *  (if any)
     * @param name Servlet name (if a named dispatcher was created)
     *  else <code>null</code>
     */
    public ApplicationDispatcher
        (Wrapper wrapper, String requestURI, String servletPath,
         String pathInfo, String queryString, String name) {

        super();

        // Save all of our configuration parameters
        this.wrapper = wrapper;
        this.context = (Context) wrapper.getParent();
        this.requestURI = requestURI;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.queryString = queryString;
        this.name = name;
        /* GlassFish 6386229
        if (wrapper instanceof StandardWrapper)
            this.support = ((StandardWrapper) wrapper).getInstanceSupport();
        else
            this.support = new InstanceSupport(wrapper);
        */
        // START GlassFish 6386229
        this.support = ((StandardWrapper) wrapper).getInstanceSupport();
        // END GlassFish 6386229

        if ( log.isDebugEnabled() )
            log.debug("servletPath=" + this.servletPath + ", pathInfo=" +
                this.pathInfo + ", queryString=" + queryString +
                ", name=" + this.name);

    }


    // ----------------------------------------------------- Instance Variables

    private static Log log = LogFactory.getLog(ApplicationDispatcher.class);


    //START OF 6364900
    /**
     * is this dispatch cross context
     */
    private Boolean crossContextFlag = null;
    //END OF 6364900


    /**
     * The Context this RequestDispatcher is associated with.
     */
    private Context context = null;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * Descriptive information about this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.ApplicationDispatcher/1.0";


    /**
     * The servlet name for a named dispatcher.
     */
    private String name = null;


    /**
     * The extra path information for this RequestDispatcher.
     */
    private String pathInfo = null;


    /**
     * The query string parameters for this RequestDispatcher.
     */
    private String queryString = null;


    /**
     * The request URI for this RequestDispatcher.
     */
    private String requestURI = null;


    /**
     * The servlet path for this RequestDispatcher.
     */
    private String servletPath = null;


    /**
     * The StringManager for this package.
     */
    private static final StringManager sm =
      StringManager.getManager(Constants.Package);


    /**
     * The InstanceSupport instance associated with our Wrapper (used to
     * send "before dispatch" and "after dispatch" events.
     */
    private InstanceSupport support = null;


    /**
     * The Wrapper associated with the resource that will be forwarded to
     * or included.
     */
    private Wrapper wrapper = null;


     // ------------------------------------------------------------- Properties


    /**
     * Return the descriptive information about this implementation.
     */
    public String getInfo() {

        return (this.info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Forward this request and response to another resource for processing.
     * Any runtime exception, IOException, or ServletException thrown by the
     * called servlet will be propogated to the caller.
     *
     * @param request The servlet request to be forwarded
     * @param response The servlet response to be forwarded
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void forward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        if (Globals.IS_SECURITY_ENABLED) {
            try {
                PrivilegedForward dp = new PrivilegedForward(request,response);
                AccessController.doPrivileged(dp);
                // START SJSAS 6374990
                ApplicationDispatcherForward.commit(
                    (HttpServletRequest) request,
                    (HttpServletResponse) response,
                    context, wrapper);
                // END SJSAS 6374990
            } catch (PrivilegedActionException pe) {
                Exception e = pe.getException();
                if (e instanceof ServletException)
                    throw (ServletException) e;
                throw (IOException) e;
            }
        } else {
            doForward(request,response);
            // START SJSAS 6374990
            ApplicationDispatcherForward.commit(
                (HttpServletRequest) request,
                (HttpServletResponse) response,
                context, wrapper);
            // END SJSAS 6374990
        }
    }

    private void doForward(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        
        // Reset any output that has been buffered, but keep headers/cookies
        if (response.isCommitted()) {
            if ( log.isDebugEnabled() )
                log.debug("  Forward on committed response --> ISE");
            throw new IllegalStateException
                (sm.getString("applicationDispatcher.forward.ise"));
        }
        try {
            response.resetBuffer();
        } catch (IllegalStateException e) {
            if ( log.isDebugEnabled() )
                log.debug("  Forward resetBuffer() returned ISE: " + e);
            throw e;
        }

        // Set up to handle the specified request and response
        State state = new State(request, response, false);

        // Identify the HTTP-specific request and response objects (if any)
        HttpServletRequest hrequest = null;
        /* GlassFish 6386229
        if (request instanceof HttpServletRequest)
            hrequest = (HttpServletRequest) request;
        */    
        // START GlassFish 6386229
        hrequest = (HttpServletRequest) request;
        // END GlassFish 6386229
        HttpServletResponse hresponse = null;
        /* GlassFish 6386229
        if (response instanceof HttpServletResponse)
            hresponse = (HttpServletResponse) response;
        */    
        // START GlassFish 6386229
        hresponse = (HttpServletResponse) response;
        // END GlassFish 6386229

        // Handle a non-HTTP forward by passing the existing request/response
        if ((hrequest == null) || (hresponse == null)) {

            if ( log.isDebugEnabled() )
                log.debug(" Non-HTTP Forward");
            
            processRequest(hrequest,hresponse,state);

        }

        // Handle an HTTP named dispatcher forward
        else if ((servletPath == null) && (pathInfo == null)) {

            if ( log.isDebugEnabled() )
                log.debug(" Named Dispatcher Forward");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            wrequest.setRequestURI(hrequest.getRequestURI());
            wrequest.setContextPath(hrequest.getContextPath());
            wrequest.setServletPath(hrequest.getServletPath());
            wrequest.setPathInfo(hrequest.getPathInfo());
            wrequest.setQueryString(hrequest.getQueryString());
            
            processRequest(request,response,state);

            wrequest.recycle();
            unwrapRequest(state);
        }

        // Handle an HTTP path-based forward
        else {

            if ( log.isDebugEnabled() )
                log.debug(" Path Based Forward");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            String contextPath = context.getPath();

            if (hrequest.getAttribute(Globals.FORWARD_REQUEST_URI_ATTR) == null) { 
                wrequest.initSpecialAttributes(false,
                                               hrequest.getRequestURI(),
                                               hrequest.getContextPath(),
                                               hrequest.getServletPath(),
                                               hrequest.getPathInfo(),
                                               hrequest.getQueryString());
            }
 
            wrequest.setContextPath(contextPath);
            wrequest.setRequestURI(requestURI);
            wrequest.setServletPath(servletPath);
            wrequest.setPathInfo(pathInfo);
            if (queryString != null) {
                wrequest.setQueryString(queryString);
                wrequest.setQueryParams(queryString);
            }

            processRequest(request,response,state);

            wrequest.recycle();
            unwrapRequest(state);

        }

        /* SJSAS 6374990
        // This is not a real close in order to support error processing
        if ( log.isDebugEnabled() )
            log.debug(" Disabling the response for futher output");

        if  (response instanceof ResponseFacade) {
            ((ResponseFacade) response).finish();
        } else {
            // Servlet SRV.6.2.2. The Resquest/Response may have been wrapped
            // and may no longer be instance of RequestFacade 
            if (log.isDebugEnabled()){
                log.debug( " The Response is vehiculed using a wrapper: " 
                           + response.getClass().getName() );
            }

            // Close anyway
            try {
                PrintWriter writer = response.getWriter();
                writer.close();
            } catch (IllegalStateException e) {
                try {
                    ServletOutputStream stream = response.getOutputStream();
                    stream.close();
                } catch (IllegalStateException f) {
                    ;
                } catch (IOException f) {
                    ;
                }
            } catch (IOException e) {
                ;
            }
        }
        */

    }

    

    /**
     * Prepare the request based on the filter configuration.
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void processRequest(ServletRequest request, 
                                ServletResponse response,
                                State state)
        throws IOException, ServletException {
                
        Integer disInt = (Integer) request.getAttribute
            (ApplicationFilterFactory.DISPATCHER_TYPE_ATTR);
        if (disInt != null) {
            if (disInt.intValue() != ApplicationFilterFactory.ERROR) {
                state.outerRequest.setAttribute
                    (ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                     servletPath);
                state.outerRequest.setAttribute
                    (ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                     Integer.valueOf(ApplicationFilterFactory.FORWARD));
                invoke(state.outerRequest, response, state);
            } else {
                invoke(state.outerRequest, response, state);
            }
        }

    }
    
    
    
    /**
     * Include the response from another resource in the current response.
     * Any runtime exception, IOException, or ServletException thrown by the
     * called servlet will be propogated to the caller.
     *
     * @param request The servlet request that is including this one
     * @param response The servlet response to be appended to
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public void include(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {
        if (Globals.IS_SECURITY_ENABLED) {
            try {
                PrivilegedInclude dp = new PrivilegedInclude(request,response);
                AccessController.doPrivileged(dp);
            } catch (PrivilegedActionException pe) {
                Exception e = pe.getException();

                if (e instanceof ServletException)
                    throw (ServletException) e;
                throw (IOException) e;
            }
        } else {
            doInclude(request,response);
        }
    }

    private void doInclude(ServletRequest request, ServletResponse response)
        throws ServletException, IOException
    {

        // Set up to handle the specified request and response
        State state = new State(request, response, true);

        // Create a wrapped response to use for this request
        wrapResponse(state);

        // Handle a non-HTTP include
        /* GlassFish 6386229
        if (!(request instanceof HttpServletRequest) ||
            !(response instanceof HttpServletResponse)) {

            if ( log.isDebugEnabled() )
                log.debug(" Non-HTTP Include");
            request.setAttribute(ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                                             Integer.valueOf(ApplicationFilterFactory.INCLUDE));
            request.setAttribute(ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR, 
                                             //origServletPath);
                                             servletPath);
            invoke(request, state.outerResponse, state);
            unwrapResponse(state);

        }

        // Handle an HTTP named dispatcher include
        else if (name != null) {
        */
        // START GlassFish 6386229
        // Handle an HTTP named dispatcher include
        if (name != null) {
        // END GlassFish 6386229
            if ( log.isDebugEnabled() )
                log.debug(" Named Dispatcher Include");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            wrequest.setAttribute(Globals.NAMED_DISPATCHER_ATTR, name);
            if (servletPath != null)
                wrequest.setServletPath(servletPath);
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                Integer.valueOf(ApplicationFilterFactory.INCLUDE));
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                servletPath);
            invoke(state.outerRequest, state.outerResponse, state);

            wrequest.recycle();
            unwrapRequest(state);
            unwrapResponse(state);

        }

        // Handle an HTTP path based include
        else {

            if ( log.isDebugEnabled() )
                log.debug(" Path Based Include");

            ApplicationHttpRequest wrequest =
                (ApplicationHttpRequest) wrapRequest(state);
            wrequest.initSpecialAttributes(true, 
                                           requestURI,
                                           context.getPath(),
                                           servletPath,
                                           pathInfo,
                                           queryString);
            wrequest.setQueryParams(queryString);
            
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_TYPE_ATTR,
                Integer.valueOf(ApplicationFilterFactory.INCLUDE));
            wrequest.setAttribute(
                ApplicationFilterFactory.DISPATCHER_REQUEST_PATH_ATTR,
                servletPath);
            invoke(state.outerRequest, state.outerResponse, state);

            wrequest.recycle();
            unwrapRequest(state);
            unwrapResponse(state);

        }

    }


    // -------------------------------------------------------- Private Methods
    
    
    /**
     * Ask the resource represented by this RequestDispatcher to process
     * the associated request, and create (or append to) the associated
     * response.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: This implementation assumes
     * that no filters are applied to a forwarded or included resource,
     * because they were already done for the original request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void invoke(ServletRequest request, ServletResponse response,
        State state) throws IOException, ServletException {
        //START OF 6364900 original invoke has been renamed to doInvoke
        boolean crossContext = false;
        if(crossContextFlag != null && crossContextFlag.booleanValue()) {
            crossContext = true;
        }
        if(crossContext) {
            context.getManager().lockSession(request); 
        }       
        try {
            if(crossContext) {
                context.getManager().preRequestDispatcherProcess(request, response);
            }            
            doInvoke(request,response);
            if(crossContext) {
                context.getManager().postRequestDispatcherProcess(request, response);
            }
        } finally {
            if(crossContext) {
                context.getManager().unlockSession(request);
            }
            crossContextFlag = null;
        }
        //END OF 6364900
    }
    
    
    /**
     * Ask the resource represented by this RequestDispatcher to process
     * the associated request, and create (or append to) the associated
     * response.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: This implementation assumes
     * that no filters are applied to a forwarded or included resource,
     * because they were already done for the original request.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    private void doInvoke(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {

        // Checking to see if the context classloader is the current context
        // classloader. If it's not, we're saving it, and setting the context
        // classloader to the Context classloader
        ClassLoader oldCCL = Thread.currentThread().getContextClassLoader();
        ClassLoader contextClassLoader = context.getLoader().getClassLoader();

        if (oldCCL != contextClassLoader) {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        } else {
            oldCCL = null;
        }

        // Initialize local variables we may need
        HttpServletRequest hrequest = null;
        /* GlassFish 6386229
        if (request instanceof HttpServletRequest)
            hrequest = (HttpServletRequest) request;
        */
        // START GlassFish 6386229
        hrequest = (HttpServletRequest) request;
        // END GlassFish 6386229
        HttpServletResponse hresponse = null;
        /* GlassFish 6386229
        if (response instanceof HttpServletResponse)
            hresponse = (HttpServletResponse) response;
        */
        // START GlassFish 6386229
        hresponse = (HttpServletResponse) response;
        // END GlassFish 6386229
        Servlet servlet = null;
        IOException ioException = null;
        ServletException servletException = null;
        RuntimeException runtimeException = null;
        boolean unavailable = false;
              

        // Check for the servlet being marked unavailable
        if (wrapper.isUnavailable()) {
            log(sm.getString("applicationDispatcher.isUnavailable",
                             wrapper.getName()));
            if (hresponse == null) {
                ;       // NOTE - Not much we can do generically
            } else {
                long available = wrapper.getAvailable();
                if ((available > 0L) && (available < Long.MAX_VALUE))
                    hresponse.setDateHeader("Retry-After", available);
                hresponse.sendError
                    (HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                     sm.getString("applicationDispatcher.isUnavailable",
                                  wrapper.getName()));
            }
            unavailable = true;
        }

        // Allocate a servlet instance to process this request
        try {
            if (!unavailable) {
                //                if (debug >= 2)
                //                    log("  Allocating servlet instance");
                servlet = wrapper.allocate();
                //                if ((debug >= 2) && (servlet == null))
                //                    log("    No servlet instance returned!");
            }
        } catch (ServletException e) {
            log(sm.getString("applicationDispatcher.allocateException",
                             wrapper.getName()),
                             StandardWrapper.getRootCause(e));
            servletException = e;
            servlet = null;
        } catch (Throwable e) {
            log(sm.getString("applicationDispatcher.allocateException",
                             wrapper.getName()), e);
            servletException = new ServletException
                (sm.getString("applicationDispatcher.allocateException",
                              wrapper.getName()), e);
            servlet = null;
        }
                
        // Get the FilterChain Here
        ApplicationFilterFactory factory = ApplicationFilterFactory.getInstance();
        ApplicationFilterChain filterChain = factory.createFilterChain(request,
                                                                wrapper,servlet);

        // START OF S1AS 4703023
        CoyoteRequest origRequest = null;
        // END OF S1AS 4703023

        // Call the service() method for the allocated servlet instance
        try {
            String jspFile = wrapper.getJspFile();
            if (jspFile != null) {
                request.setAttribute(Globals.JSP_FILE_ATTR, jspFile);
            } 
            support.fireInstanceEvent(InstanceEvent.BEFORE_DISPATCH_EVENT,
                                      servlet, request, response);
            // for includes/forwards
            /* IASRI 4665318
            if ((servlet != null) && (filterChain != null)) {
            */
            // START IASRI 4665318
            if (servlet != null) {
            // END IASRI 4665318
                // START OF S1AS 4703023
                origRequest = getCoyoteRequest(request);
                if (origRequest != null) {
                    origRequest.incrementDispatchDepth();
                    if (origRequest.isMaxDispatchDepthReached())
                        throw new ServletException(sm.getString(
                            "applicationDispatcher.maxDispatchDepthReached",
                            new Object[] {
                                Integer.valueOf(origRequest.getMaxDispatchDepth())}));
                }
                // END OF S1AS 4703023 
                /* IASRI 4665318
                filterChain.doFilter(request, response);
                */
                // START IASRI 4665318
                if (filterChain != null)
                    filterChain.doFilter(request, response);
                else {
                    ApplicationFilterChain.servletService(request, response,
                                                          servlet, support);
                }
                // END IASRI 4665318
            }
            // Servlet Service Method is called by the FilterChain
            support.fireInstanceEvent(InstanceEvent.AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
        } catch (ClientAbortException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            ioException = e;
        } catch (IOException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            log(sm.getString("applicationDispatcher.serviceException",
                             wrapper.getName()), e);
            ioException = e;
        } catch (UnavailableException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            log(sm.getString("applicationDispatcher.serviceException",
                             wrapper.getName()), e);
            servletException = e;
            wrapper.unavailable(e);
        } catch (ServletException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            Throwable rootCause = StandardWrapper.getRootCause(e);
            if (!(rootCause instanceof ClientAbortException)) {
                log(sm.getString("applicationDispatcher.serviceException",
                    wrapper.getName()), rootCause);
            }
            servletException = e;
        } catch (RuntimeException e) {
            support.fireInstanceEvent(InstanceEvent.AFTER_DISPATCH_EVENT,
                                      servlet, request, response);
            log(sm.getString("applicationDispatcher.serviceException",
                             wrapper.getName()), e);
            runtimeException = e;
        // START OF S1AS 4703023
        } finally {
            if (origRequest != null)
                origRequest.decrementDispatchDepth();
        // END OF S1AS 4703023
        }

        // Release the filter chain (if any) for this request
        try {
            if (filterChain != null)
                filterChain.release();
        } catch (Throwable e) {
            log.error(sm.getString("standardWrapper.releaseFilters",
                             wrapper.getName()), e);
          //FIXME Exception handling needs to be simpiler to what is in the StandardWrapperValue
        }

        // Deallocate the allocated servlet instance
        try {
            if (servlet != null) {
                wrapper.deallocate(servlet);
            }
        } catch (ServletException e) {
            log(sm.getString("applicationDispatcher.deallocateException",
                             wrapper.getName()), e);
            servletException = e;
        } catch (Throwable e) {
            log(sm.getString("applicationDispatcher.deallocateException",
                             wrapper.getName()), e);
            servletException = new ServletException
                (sm.getString("applicationDispatcher.deallocateException",
                              wrapper.getName()), e);
        }

        // Reset the old context class loader
        if (oldCCL != null)
            Thread.currentThread().setContextClassLoader(oldCCL);

        // Rethrow an exception if one was thrown by the invoked servlet
        if (ioException != null)
            throw ioException;
        if (servletException != null)
            throw servletException;
        if (runtimeException != null)
            throw runtimeException;

    }


    /**
     * Log a message on the Logger associated with our Context (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {
        Logger logger = context.getLogger();
        if (logger != null)
            logger.log("ApplicationDispatcher[" + context.getPath() +
                       "]: " + message);
        else
            System.out.println("ApplicationDispatcher[" +
                               context.getPath() + "]: " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {
        Logger logger = context.getLogger();
        if (logger != null)
            logger.log("ApplicationDispatcher[" + context.getPath() +
                       "] " + message, throwable);
        else {
            System.out.println("ApplicationDispatcher[" +
                               context.getPath() + "]: " + message);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Unwrap the request if we have wrapped it.
     */
    private void unwrapRequest(State state) {

        if (state.wrapRequest == null)
            return;

        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while (current != null) {

            // If we run into the container request we are done
            if ((current instanceof Request)
                || (current instanceof RequestFacade))
                break;

            // Remove the current request if it is our wrapper
            if (current == state.wrapRequest) {
                ServletRequest next =
                  ((ServletRequestWrapper) current).getRequest();
                if (previous == null)
                    state.outerRequest = next;
                else
                    ((ServletRequestWrapper) previous).setRequest(next);
                break;
            }

            // Advance to the next request in the chain
            previous = current;
            current = ((ServletRequestWrapper) current).getRequest();

        }

    }


    /**
     * Unwrap the response if we have wrapped it.
     */
    private void unwrapResponse(State state) {

        if (state.wrapResponse == null)
            return;

        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while (current != null) {

            // If we run into the container response we are done
            if ((current instanceof Response)
                || (current instanceof ResponseFacade))
                break;

            // Remove the current response if it is our wrapper
            if (current == state.wrapResponse) {
                ServletResponse next =
                  ((ServletResponseWrapper) current).getResponse();
                if (previous == null)
                    state.outerResponse = next;
                else
                    ((ServletResponseWrapper) previous).setResponse(next);
                break;
            }

            // Advance to the next response in the chain
            previous = current;
            current = ((ServletResponseWrapper) current).getResponse();

        }

    }


    /**
     * Create and return a request wrapper that has been inserted in the
     * appropriate spot in the request chain.
     */
    private ServletRequest wrapRequest(State state) {

        // Locate the request we should insert in front of
        ServletRequest previous = null;
        ServletRequest current = state.outerRequest;
        while (current != null) {
            if ("org.apache.catalina.servlets.InvokerHttpRequest".
                equals(current.getClass().getName()))
                break; // KLUDGE - Make nested RD.forward() using invoker work
            if (!(current instanceof ServletRequestWrapper))
                break;
            if (current instanceof ApplicationHttpRequest)
                break;
            if (current instanceof ApplicationRequest)
                break;
            if (current instanceof Request)
                break;
            previous = current;
            current = ((ServletRequestWrapper) current).getRequest();
        }

        // Instantiate a new wrapper at this point and insert it in the chain
        ServletRequest wrapper = null;
        if ((current instanceof ApplicationHttpRequest) ||
            (current instanceof HttpRequest) ||
            (current instanceof HttpServletRequest)) {
            // Compute a crossContext flag
            HttpServletRequest hcurrent = (HttpServletRequest) current;
            boolean crossContext = 
                !(context.getPath().equals(hcurrent.getContextPath()));
            //START OF 6364900
            crossContextFlag = Boolean.valueOf(crossContext);
            //END OF 6364900
            wrapper = new ApplicationHttpRequest
                (hcurrent, context, crossContext);
        } else {
            wrapper = new ApplicationRequest(current);
        }
        if (previous == null)
            state.outerRequest = wrapper;
        else
            ((ServletRequestWrapper) previous).setRequest(wrapper);
        state.wrapRequest = wrapper;
        return (wrapper);

    }


    /**
     * Create and return a response wrapper that has been inserted in the
     * appropriate spot in the response chain.
     */
    private ServletResponse wrapResponse(State state) {

        // Locate the response we should insert in front of
        ServletResponse previous = null;
        ServletResponse current = state.outerResponse;
        while (current != null) {
            if (!(current instanceof ServletResponseWrapper))
                break;
            if (current instanceof ApplicationHttpResponse)
                break;
            if (current instanceof ApplicationResponse)
                break;
            if (current instanceof Response)
                break;
            previous = current;
            current = ((ServletResponseWrapper) current).getResponse();
        }

        // Instantiate a new wrapper at this point and insert it in the chain
        ServletResponse wrapper = null;
        if ((current instanceof ApplicationHttpResponse) ||
            (current instanceof HttpResponse) ||
            (current instanceof HttpServletResponse))
            wrapper =
                new ApplicationHttpResponse((HttpServletResponse) current,
                                            state.including);
        else
            wrapper = new ApplicationResponse(current, state.including);
        if (previous == null)
            state.outerResponse = wrapper;
        else
            ((ServletResponseWrapper) previous).setResponse(wrapper);
        state.wrapResponse = wrapper;
        return (wrapper);

    }


    // START OF S1AS 4703023
    /**
     * Finds and returns the underlying/original request object.
     *
     * (Doing instanceof in a loop will impact performance)
     */
    private CoyoteRequest getCoyoteRequest(ServletRequest request) {

        CoyoteRequest coyoteRequest = null;
        Object current = request;
        while (current != null) {
            // When we run into the original request object, return it
            if (current instanceof CoyoteRequestFacade) {
                coyoteRequest = ((CoyoteRequestFacade)current).getUnwrappedCoyoteRequest();
                break;
            } else if (current instanceof ServletRequestWrapper) {
                current = ((ServletRequestWrapper) current).getRequest();
            } else
                break;
        }
        return coyoteRequest;

    }
    // END OF S1AS 4703023


}
