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
 */

package org.apache.catalina.connector;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.connector.Request;

public class AsyncContextImpl implements AsyncContext {

    private static final Logger log = Logger.getLogger(AsyncContextImpl.class.getName());

    // Thread pool for async dispatches
    private static final ExecutorService pool =
        Executors.newCachedThreadPool();

    // The original (unwrapped) request
    private Request origRequest;

    // The possibly wrapped request passed to ServletRequest.startAsync
    private ServletRequest servletRequest;

    // The possibly wrapped response passed to ServletRequest.startAsync    
    private ServletResponse servletResponse;

    private boolean hasOriginalRequestAndResponse = false;


    /**
     * Constructor
     *
     * @param origRequest the original (unwrapped) request
     * @param servletRequest the possibly wrapped request passed to
     * ServletRequest.startAsync
     * @param servletResponse the possibly wrapped response passed to
     * ServletRequest.startAsync
     * @param hasOriginalRequestAndResponse XXX
     */
    public AsyncContextImpl(Request origRequest,
                            ServletRequest servletRequest,
                            Response origResponse,
                            ServletResponse servletResponse) {
        this.origRequest = origRequest;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        if (origRequest == servletRequest && origResponse == servletResponse) {
            hasOriginalRequestAndResponse = true;
        }
    }


    public ServletRequest getRequest() {
        return servletRequest;
    }


    public ServletResponse getResponse() {
        return servletResponse;
    }


    public boolean hasOriginalRequestAndResponse() {
        return hasOriginalRequestAndResponse;
    }


    public void forward() {
        origRequest.stopAsyncTimer();
        if (servletRequest instanceof HttpServletRequest) {
            String uri = ((HttpServletRequest)servletRequest).getRequestURI();
            RequestDispatcher rd = servletRequest.getRequestDispatcher(uri);
            if (rd != null) {
                origRequest.setOkToReinitializeAsync();
                pool.execute(new Handler(rd, servletRequest, servletResponse));
            } else {
                log.warning("Unable to acquire RequestDispatcher for " +
                            "original request URI " + uri);
            }
        } else {
            log.warning("Unable to determine original request URI");
        }
    } 


    public void forward(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Null path");
        }
        origRequest.stopAsyncTimer();
        RequestDispatcher rd = servletRequest.getRequestDispatcher(path);
        if (rd != null) {
            origRequest.setOkToReinitializeAsync();
            pool.execute(new Handler(rd, servletRequest, servletResponse));
        } else {
            log.warning("Unable to acquire RequestDispatcher for " + path);
        }
    }


    public void forward(ServletContext context, String path) {
        if (path == null || context == null) {
            throw new IllegalArgumentException("Null context or path");
        }
        origRequest.stopAsyncTimer();
        RequestDispatcher rd = context.getRequestDispatcher(path);
        if (rd != null) {
            origRequest.setOkToReinitializeAsync();
            pool.execute(new Handler(rd, servletRequest, servletResponse));
        } else {
            log.warning("Unable to acquire RequestDispatcher for " + path +
                        "in servlet context " + context.getContextPath());
        }
    }


    public void complete() {
        if (!origRequest.isAsyncStarted()) {
            throw new IllegalStateException("Request not in async mode");
        }

        origRequest.asyncComplete();
    }


    public void start(Runnable run) {
        // XXX
    }


    /*
     * Reinitializes this AsyncContext with the given request
     */
    void setServletRequest(ServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }


    /*
     * Reinitializes this AsyncContext with the given response
     */
    void setServletResponse(ServletResponse servletResponse) {
        this.servletResponse = servletResponse;
    }


    void recycle() {
        // XXX
    }


    static class Handler implements Runnable {

        private final RequestDispatcher dispatcher;
        private final ServletRequest request;
        private final ServletResponse response;

        Handler(RequestDispatcher dispatcher, ServletRequest request,
                ServletResponse response) {
            this.dispatcher = dispatcher;
            this.request = request;
            this.response = response;
        }
       
        public void run() {
            try {
                dispatcher.forward(request, response);
            } catch (Exception e) {
                // Log warning
            }
        }
    }

}
