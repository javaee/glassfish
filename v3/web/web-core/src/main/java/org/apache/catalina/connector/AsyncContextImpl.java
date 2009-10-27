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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.Globals;
import org.apache.catalina.connector.*;
import org.apache.catalina.core.*;
import org.apache.catalina.util.StringManager;

public class AsyncContextImpl implements AsyncContext {

    /* 
     * Event notification types for async mode
     */
    static enum AsyncEventType { COMPLETE, TIMEOUT, ERROR, START_ASYNC }

    private static final Logger log =
        Logger.getLogger(AsyncContextImpl.class.getName());

    // Default timeout for async operations
    private static final long DEFAULT_ASYNC_TIMEOUT_MILLIS = 30000L;

    // Thread pool for async dispatches
    private static final ExecutorService pool =
        Executors.newCachedThreadPool();

    private static final StringManager STRING_MANAGER =
        StringManager.getManager(Constants.Package);

    // The original (unwrapped) request
    private Request origRequest;

    // The possibly wrapped request passed to ServletRequest.startAsync
    private ServletRequest servletRequest;

    // The possibly wrapped response passed to ServletRequest.startAsync    
    private ServletResponse servletResponse;

    private boolean isOriginalRequestAndResponse = false;

    // The target of zero-argument async dispatches
    private String zeroArgDispatchTarget = null;

    // defaults to false
    private AtomicBoolean isDispatchInProgress = new AtomicBoolean(); 

    private AtomicBoolean isOkToConfigure = new AtomicBoolean(true); 

    private long asyncTimeoutMillis = DEFAULT_ASYNC_TIMEOUT_MILLIS;

    private LinkedList<AsyncListenerContext> asyncListenerContexts =
        new LinkedList<AsyncListenerContext>();

    private AtomicInteger startAsyncCounter = new AtomicInteger(0);

    private ThreadLocal<Boolean> isStartAsyncInScope =
    new ThreadLocal<Boolean>() {
        @Override  
        protected Boolean initialValue() {  
            return Boolean.FALSE;  
        }  
    };  

    /**
     * Constructor
     *
     * @param origRequest the original (unwrapped) request
     * @param servletRequest the possibly wrapped request passed to
     * ServletRequest.startAsync
     * @param servletResponse the possibly wrapped response passed to
     * ServletRequest.startAsync
     * @param isOriginalRequestAndResponse true if the zero-arg version of
     * startAsync was called, false otherwise
     */
    public AsyncContextImpl(Request origRequest,
                            ServletRequest servletRequest,
                            Response origResponse,
                            ServletResponse servletResponse,
                            boolean isOriginalRequestAndResponse) {
        this.origRequest = origRequest;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.isOriginalRequestAndResponse = isOriginalRequestAndResponse;
        if (!isOriginalRequestAndResponse &&
                (servletRequest instanceof HttpServletRequest)) {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(
                (HttpServletRequest)servletRequest);
        } else {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(origRequest);
        }
    }

    @Override
    public ServletRequest getRequest() {
        return servletRequest;
    }

    Request getOriginalRequest() {
        return origRequest;
    }

    @Override
    public ServletResponse getResponse() {
        return servletResponse;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return isOriginalRequestAndResponse;
    }

    @Override
    public void dispatch() {
        if (zeroArgDispatchTarget == null) {
            log.severe("Unable to determine target of zero-arg dispatch");
            return;
        }
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            servletRequest.getRequestDispatcher(zeroArgDispatchTarget);
        if (dispatcher != null) {
            if (isDispatchInProgress.compareAndSet(false, true)) {
                pool.execute(new Handler(this, dispatcher, origRequest));
            } else {
                throw new IllegalStateException(
                    STRING_MANAGER.getString("async.dispatchInProgress"));
            }
        } else {
            // Should never happen, because any unmapped paths will be 
            // mapped to the DefaultServlet
            log.warning("Unable to acquire RequestDispatcher for " +
                        zeroArgDispatchTarget);
        }
    } 

    @Override
    public void dispatch(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Null path");
        }
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            servletRequest.getRequestDispatcher(path);
        if (dispatcher != null) {
            if (isDispatchInProgress.compareAndSet(false, true)) {
                pool.execute(new Handler(this, dispatcher, origRequest));
            } else {
                throw new IllegalStateException(
                    STRING_MANAGER.getString("async.dispatchInProgress"));
            }
        } else {
            // Should never happen, because any unmapped paths will be 
            // mapped to the DefaultServlet
            log.warning("Unable to acquire RequestDispatcher for " +
                        path);
        }
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        if (path == null || context == null) {
            throw new IllegalArgumentException("Null context or path");
        }
        ApplicationDispatcher dispatcher = (ApplicationDispatcher)
            context.getRequestDispatcher(path);
        if (dispatcher != null) {
            if (isDispatchInProgress.compareAndSet(false, true)) {
                pool.execute(new Handler(this, dispatcher, origRequest));
            } else {
                throw new IllegalStateException(
                    STRING_MANAGER.getString("async.dispatchInProgress"));
            }
        } else {
            // Should never happen, because any unmapped paths will be 
            // mapped to the DefaultServlet
            log.warning("Unable to acquire RequestDispatcher for " + path +
                        "in servlet context " + context.getContextPath());
        }
    }

    @Override
    public void complete() {
        origRequest.asyncComplete();
    }

    @Override
    public void start(Runnable run) {
        pool.execute(run);
    }

    @Override
    public void addListener(AsyncListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null listener");
        }

        if (!isOkToConfigure.get()) {
            throw new IllegalStateException(
                STRING_MANAGER.getString("async.addListenerIllegalState"));
        }

        synchronized(asyncListenerContexts) {
            asyncListenerContexts.add(new AsyncListenerContext(listener));
        }
    }

    @Override
    public void addListener(AsyncListener listener,
                            ServletRequest servletRequest,
                            ServletResponse servletResponse) {
        if (listener == null || servletRequest == null ||
                servletResponse == null) {
            throw new IllegalArgumentException(
                "Null listener, request, or response");
        }

        if (!isOkToConfigure.get()) {
            throw new IllegalStateException(
                STRING_MANAGER.getString("async.addListenerIllegalState"));
        }

        synchronized(asyncListenerContexts) {
            asyncListenerContexts.add(new AsyncListenerContext(
                listener, servletRequest, servletResponse));
        }
    }

    public <T extends AsyncListener> T createListener(Class<T> clazz)
            throws ServletException {
        T listener = null;
        StandardContext ctx = (StandardContext) origRequest.getContext();
        if (ctx != null) {
            try {
                listener = ctx.createListenerInstance(clazz);
            } catch (Throwable t) {
                throw new ServletException(t);
            }
        }
        return listener;
    }

    @Override
    public void setTimeout(long timeout) {
        if (!isOkToConfigure.get()) {
            throw new IllegalStateException(
                STRING_MANAGER.getString("async.setTimeoutIllegalState"));
        }
        asyncTimeoutMillis = timeout;
        origRequest.setAsyncTimeout(timeout);
    }

    @Override
    public long getTimeout() {
        return asyncTimeoutMillis;
    }

    /*
     * Reinitializes this AsyncContext with the given request and response.
     *
     * @param servletRequest the ServletRequest with which to initialize
     * the AsyncContext
     * @param servletResponse the ServletResponse with which to initialize
     * the AsyncContext
     * @param isOriginalRequestAndResponse true if the zero-arg version of
     * startAsync was called, false otherwise
     */
    void reinitialize(ServletRequest servletRequest,
                      ServletResponse servletResponse,
                      boolean isOriginalRequestAndResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.isOriginalRequestAndResponse = isOriginalRequestAndResponse;
        isDispatchInProgress.set(false);
        setOkToConfigure(true);
        startAsyncCounter.incrementAndGet();
        notifyAsyncListeners(AsyncEventType.START_ASYNC, null);
        if (isOriginalRequestAndResponse) {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(origRequest);
        } else if (servletRequest instanceof HttpServletRequest) {
            zeroArgDispatchTarget = getZeroArgDispatchTarget(
                (HttpServletRequest)servletRequest);
        } else {
            log.warning("Unable to determine target of " +
                        "zero-argument dispatch");
        }
    }

    /**
     * @param value true if calls to AsyncContext#addListener and
     * AsyncContext#setTimeout will be accepted, and false if these
     * calls will result in an IllegalStateException
     */
    void setOkToConfigure(boolean value) {
        isOkToConfigure.set(value);
    }

    /**
     * Determines the target of a zero-argument async dispatch for the
     * given request.
     *
     * @return the target of the zero-argument async dispatch
     */
    private String getZeroArgDispatchTarget(HttpServletRequest req) {
        StringBuilder sb = new StringBuilder();
        if (req.getServletPath() != null) {
            sb.append(req.getServletPath());
        }
        if (req.getPathInfo() != null) {
            sb.append(req.getPathInfo());
        }
        return sb.toString();
    }

    static class Handler implements Runnable {

        private final AsyncContextImpl asyncContext;
        private final ApplicationDispatcher dispatcher;
        private final Request origRequest;

        Handler(AsyncContextImpl asyncContext,
                ApplicationDispatcher dispatcher,
                Request origRequest) {
            this.asyncContext = asyncContext;
            this.dispatcher = dispatcher;
            this.origRequest = origRequest;
        }
       
        public void run() {
            asyncContext.isStartAsyncInScope.set(Boolean.TRUE);
            origRequest.setAttribute(Globals.DISPATCHER_TYPE_ATTR,
                                     DispatcherType.ASYNC);
            origRequest.setAsyncStarted(false);
            int startAsyncCurrent = asyncContext.startAsyncCounter.get();
            try {
                dispatcher.dispatch(asyncContext.getRequest(),
                    asyncContext.getResponse(), DispatcherType.ASYNC);
                /* 
                 * Close the response after the dispatch target has
                 * completed execution, unless startAsync was called.
                 */
                if (asyncContext.startAsyncCounter.compareAndSet(
                        startAsyncCurrent, startAsyncCurrent)) {
                    asyncContext.complete();
                }
            } catch (Throwable t) {
                asyncContext.notifyAsyncListeners(AsyncEventType.ERROR, t);
                asyncContext.getOriginalRequest().errorDispatchAndComplete(t);
            } finally {
                asyncContext.isStartAsyncInScope.set(Boolean.FALSE);
            }
        }
    }

    boolean isStartAsyncInScope() {
        return isStartAsyncInScope.get().booleanValue();
    }

    /*
     * Notifies all AsyncListeners of the given async event type
     */
    void notifyAsyncListeners(AsyncEventType asyncEventType, Throwable t) {
        synchronized(asyncListenerContexts) {
            if (asyncListenerContexts.isEmpty()) {
                return;
            }
            LinkedList<AsyncListenerContext> clone =
                (LinkedList<AsyncListenerContext>)
                    asyncListenerContexts.clone();
            if (asyncEventType.equals(AsyncEventType.START_ASYNC)) {
                asyncListenerContexts.clear();
            }
            for (AsyncListenerContext asyncListenerContext : clone) {
                AsyncListener asyncListener =
                    asyncListenerContext.getAsyncListener();
                AsyncEvent asyncEvent = new AsyncEvent(
                    this, asyncListenerContext.getRequest(),
                    asyncListenerContext.getResponse(), t);
                try {
                    switch (asyncEventType) {
                    case COMPLETE:
                        asyncListener.onComplete(asyncEvent);
                        break;
                    case TIMEOUT:
                        asyncListener.onTimeout(asyncEvent);
                        break;
                    case ERROR:
                        asyncListener.onError(asyncEvent);
                        break;
                    case START_ASYNC:
                        asyncListener.onStartAsync(asyncEvent);
                        break;
                    }
                } catch (IOException ioe) {
                    log.log(Level.WARNING, "Error invoking AsyncListener",
                            ioe);
                }
            }
        }
    }

    void clear() {
        synchronized(asyncListenerContexts) {
            asyncListenerContexts.clear();
        }
    }

    /**
     * Class holding all the information required for invoking an
     * AsyncListener (including the AsyncListener itself).
     */
    private static class AsyncListenerContext {

        private AsyncListener listener;
        private ServletRequest request;
        private ServletResponse response;

        public AsyncListenerContext(AsyncListener listener) {
            this(listener, null, null);
        }

        public AsyncListenerContext(AsyncListener listener,
                                    ServletRequest request,
                                    ServletResponse response) {
            this.listener = listener;
            this.request = request;
            this.response = response;
        }

        public AsyncListener getAsyncListener() {
            return listener;
        }

        public ServletRequest getRequest() {
            return request;
        }

        public ServletResponse getResponse() {
            return response;
        }
    }

}
