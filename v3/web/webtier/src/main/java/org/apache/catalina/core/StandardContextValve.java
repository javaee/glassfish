/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */




package org.apache.catalina.core;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.*;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.naming.NamingException;

import com.sun.grizzly.util.buf.CharChunk;
import com.sun.grizzly.util.buf.MessageBytes;

import org.apache.naming.ContextBindings;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
// START GlassFish 1343
import org.apache.catalina.Valve;
// END GlassFish 1343
import org.apache.catalina.Wrapper;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;
import org.apache.catalina.valves.ValveBase;

import org.apache.tomcat.util.log.SystemLogHandler;

/**
 * Valve that implements the default basic behavior for the
 * <code>StandardContext</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.19 $ $Date: 2007/05/05 05:31:54 $
 */

final class StandardContextValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardContextValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    private static Logger log = Logger.getLogger(
        StandardContextValve.class.getName());


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Select the appropriate child Wrapper to process this request,
     * based on the specified request URI.  If no matching Wrapper can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    /** IASRI 4665318
     public void invoke(Request request, Response response,
                        ValveContext context)
         throws IOException, ServletException {
    */
    // START OF IASRI 4665318
    public int invoke(Request request, Response response)
        throws IOException, ServletException {
    // END OF IASRI 4665318

        // Disallow any direct access to resources under WEB-INF or META-INF
        HttpRequest hreq = (HttpRequest) request;
        // START CR 6415120
        if (request.getCheckRestrictedResources()) {
        // END CR 6415120
        MessageBytes requestPathMB = hreq.getRequestPathMB();
        if ((requestPathMB.startsWithIgnoreCase("/META-INF/", 0))
            || (requestPathMB.equalsIgnoreCase("/META-INF"))
            || (requestPathMB.startsWithIgnoreCase("/WEB-INF/", 0))
            || (requestPathMB.equalsIgnoreCase("/WEB-INF"))) {
            String requestURI = hreq.getDecodedRequestURI();
            notFound(requestURI, (HttpServletResponse) response.getResponse());
            return END_PIPELINE;
        }
        // START CR 6415120
        }
        // END CR 6415120

        // Wait if we are reloading
        while (((StandardContext) container).getPaused()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ;
            }
        }

        // Select the Wrapper to be used for this Request
        Wrapper wrapper = request.getWrapper();
        if (wrapper == null) {
            String requestURI = hreq.getDecodedRequestURI();
            notFound(requestURI, (HttpServletResponse) response.getResponse());
            return END_PIPELINE;
        }
        
        // Normal request processing
        if (((StandardContext) container).getSwallowOutput()) {
            try {
                SystemLogHandler.startCapture();
                return invokeInternal(wrapper, request, response);
            } finally {
                String log = SystemLogHandler.stopCapture();
                if (log != null && log.length() > 0) {
                    container.getLogger().log(log);
                }
            }
        } else {
            return invokeInternal(wrapper, request, response);
        }

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Call invoke.
     */
    private int invokeInternal(Wrapper wrapper, Request request, 
                                Response response)
        throws IOException, ServletException {

        Object instances[] = 
            ((Context) container).getApplicationEventListeners();

        ServletRequestEvent event = null;

        if ((instances != null) 
                && (instances.length > 0)) {
            event = new ServletRequestEvent
                (((StandardContext) container).getServletContext(), 
                 request.getRequest());
            // create pre-service event
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener =
                    (ServletRequestListener) instances[i];
                // START SJSAS 6329662
                container.fireContainerEvent(
                    ContainerEvent.BEFORE_REQUEST_INITIALIZED,
                    listener);
                // END SJSAS 6329662
                try {
                    listener.requestInitialized(event);
                } catch (Throwable t) {
                    log(sm.getString(
                        "standardContextValve.requestListener.requestInit",
                        instances[i].getClass().getName()),
                        t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(Globals.EXCEPTION_ATTR,t);
                    // START OF IASRI 4665318
                    // return;
                    return END_PIPELINE;
                    // END OF IASRI 4665318
                // START SJSAS 6329662
                } finally {
                    container.fireContainerEvent(
                        ContainerEvent.AFTER_REQUEST_INITIALIZED,
                        listener);
                // END SJSAS 6329662
                }
            }
        }

        // START OF IASRI 4665318
        /* GlassFish 1343
        wrapper.getPipeline().invoke(request, response);
        */
        // START GlassFish 1343
        Valve basic = wrapper.getPipeline().getBasic();
        if (basic != null) {
            basic.invoke(request, response);
            basic.postInvoke(request, response);
        }
        // END GlassFish 1343
        return END_PIPELINE;
        // END OF IASRI 4665318

   } 

    public void postInvoke(Request request, Response response)
        throws IOException, ServletException {
    // END OF IASRI 4665318
        Object instances[] = 
            ((Context) container).getApplicationEventListeners();

        if ((instances !=null ) &&
                (instances.length > 0)) {
            // create post-service event
            ServletRequestEvent event = new ServletRequestEvent
                (((StandardContext) container).getServletContext(), 
                request.getRequest());

            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener =
                    (ServletRequestListener) instances[i];
                // START SJSAS 6329662
                container.fireContainerEvent(
                    ContainerEvent.BEFORE_REQUEST_DESTROYED,
                    listener);
                // END SJSAS 6329662
                try {
                    listener.requestDestroyed(event);
                } catch (Throwable t) {
                    log(sm.getString(
                        "standardContextValve.requestListener.requestDestroyed",
                        instances[i].getClass().getName()),
                        t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(Globals.EXCEPTION_ATTR,t);
                // START SJSAS 6329662
                } finally {
                    container.fireContainerEvent(
                        ContainerEvent.AFTER_REQUEST_DESTROYED,
                        listener);
                // END SJSAS 6329662
                }
            }
        }

    }


    /**
     * Report a "not found" error for the specified resource.  FIXME:  We
     * should really be using the error reporting settings for this web
     * application, but currently that code runs at the wrapper level rather
     * than the context level.
     *
     * @param requestURI The request URI for the requested resource
     * @param response The response we are creating
     */
    private void notFound(String requestURI, HttpServletResponse response) {

        try {
            /* IASRI 4878272
            response.sendError(HttpServletResponse.SC_NOT_FOUND, requestURI);
            */
            // BEGIN IASRI 4878272
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            // END IASRI 4878272
        } catch (IllegalStateException e) {
            ;
        } catch (IOException e) {
            ;
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {

        org.apache.catalina.Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardContextValve[" + container.getName() + "]: "
                       + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardContextValve[" + containerName
                               + "]: " + message);
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        org.apache.catalina.Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("StandardContextValve[" + container.getName() + "]: "
                       + message, throwable);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("StandardContextValve[" + containerName
                               + "]: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }

    }


}
