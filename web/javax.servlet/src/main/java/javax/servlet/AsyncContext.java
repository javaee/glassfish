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

package javax.servlet;

/**
 * Class representing the execution context for an asynchronous operation
 * that was started on a ServletRequest.
 *
 * <p>An AsyncContext is created and initialized by a call to
 * {@link ServletRequest#startAsync()} or
 * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}.
 * Repeated invocations of these methods will return the same AsyncContext
 * instance, reinitialized as appropriate.
 *
 * @since Servlet 3.0
 */
public interface AsyncContext {

    /**
     * The name of the request attribute under which the original
     * request URI is made available to the target of a
     * {@link #dispatch(String)} or {@link #dispatch(ServletContext,String)} 
     */
    static final String ASYNC_REQUEST_URI = "javax.servlet.async.request_uri";

    /**
     * The name of the request attribute under which the original
     * context path is made available to the target of a
     * {@link #dispatch(String)} or {@link #dispatch(ServletContext,String)} 
     */
    static final String ASYNC_CONTEXT_PATH = "javax.servlet.async.context_path";

    /**
     * The name of the request attribute under which the original
     * path info is made available to the target of a
     * {@link #dispatch(String)} or {@link #dispatch(ServletContext,String)} 
     */
    static final String ASYNC_PATH_INFO = "javax.servlet.async.path_info";

    /**
     * The name of the request attribute under which the original
     * servlet path is made available to the target of a
     * {@link #dispatch(String)} or {@link #dispatch(ServletContext,String)}  
     */
    static final String ASYNC_SERVLET_PATH = "javax.servlet.async.servlet_path";

    /**
     * The name of the request attribute under which the original
     * query string is made available to the target of a
     * {@link #dispatch(String)} or {@link #dispatch(ServletContext,String)} 
     */
    static final String ASYNC_QUERY_STRING = "javax.servlet.async.query_string";


    /**
     * Gets the request that was used to initialize this AsyncContext
     * by calling {@link ServletRequest#startAsync()} or
     * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}.
     *
     * @return the request that was used to initialize this AsyncContext
     */
    public ServletRequest getRequest();


    /**
     * Gets the response that was used to initialize this AsyncContext
     * by calling {@link ServletRequest#startAsync()} or
     * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}.
     *
     * @return the response that was used to initialize this AsyncContext
     */
    public ServletResponse getResponse();


    /**
     * Checks if this AsyncContext was initialized with the original
     * request and response objects by calling
     * {@link ServletRequest#startAsync()}, or if it was initialized
     * with wrapped request and/or response objects using 
     * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}.
     * 
     * <p>This information may be used by filters invoked in the
     * <i>outbound</i> direction, after a request was put into
     * asynchronous mode, to determine whether any request and/or response
     * wrappers that they added during their <i>inbound</i> invocation need
     * to be preserved for the duration of the asynchronous operation, or may
     * be released.
     *
     * @return true if this AsyncContext was initialized with the original
     * request and response objects by calling
     * {@link ServletRequest#startAsync()}, and false if it was initialized
     * with wrapped request and/or response objects using 
     * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}.
     */
    public boolean hasOriginalRequestAndResponse();


    /**
     * Dispatches the request and response objects of this AsyncContext
     * to the servlet container.
     * 
     * <p>If the asynchronous cycle was started with
     * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}
     * then the dispatch is to the URI of the request passed to startAsync.
     * If the asynchronous cycle was started with
     * {@link ServletRequest#startAsync()}, then the dispatch is to the
     * URI of the request when it was last dispatched by the container.
     *
     * <p>The following sequence illustrates how this will work:
     * <code><pre>
     * // REQUEST dispatch to /url/A
     * AsyncContext ac = request.startAsync();
     * ...
     * ac.dispatch(); // ASYNC dispatch to /url/A
     * 
     * // FORWARD dispatch to /url/B
     * getRequestDispatcher("/url/B").forward(request,response);
     * // Start async operation from within the target of the FORWARD
     * // dispatch
     * ac = request.startAsync();
     * ...
     * ac.dispatch(); // ASYNC dispatch to /url/A
     * 
     * // FORWARD dispatch to /url/B
     * getRequestDispatcher("/url/B").forward(request,response);
     * // Start async operation from within the target of the FORWARD
     * // dispatch
     * ac = request.startAsync(request,response);
     * ...
     * ac.dispatch(); // ASYNC dispatch to /url/B
     * </pre></code>
     *
     * <p>This method returns immediately after passing the request
     * and response objects to a container managed thread, on which the
     * dispatch operation will be performed.
     *
     * <p>The dispatcher type of the request is set to
     * <tt>DispatcherType.ASYNC</tt>. Unlike
     * {@link RequestDispatcher#forward(ServletRequest, ServletResponse)
     * forward dispatches}, the response buffer and
     * headers will not be reset, and it is legal to dispatch even if the
     * response has already been committed.
     *
     * <p>Control over the request and response is delegated
     * to the dispatch target, and the response will be closed when the
     * dispatch target has completed execution, unless
     * {@link ServletRequest#startAsync()} or
     * {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}
     * are called.
     * 
     * @exception IllegalStateException if {@link #complete} has already
     * been called
     *
     * @see ServletRequest#getDispatcherType
     */
    public void dispatch();


    /**
     * Dispatches the request and response objects of this AsyncContext
     * to the given <tt>path</tt>.
     *
     * <p>The <tt>path</tt> parameter is interpreted in the same way 
     * as in {@link ServletRequest#getRequestDispatcher(String)}, within
     * the scope of the {@link ServletContext} from which this
     * AsyncContext was initialized.
     *
     * <p>All path related query methods of the request must reflect the
     * dispatch target, while the original request URI, context path,
     * path info, servlet path, and query string may be recovered from
     * the {@link #ASYNC_REQUEST_URI}, {@link #ASYNC_CONTEXT_PATH},
     * {@link #ASYNC_PATH_INFO}, {@link #ASYNC_SERVLET_PATH}, and
     * {@link #ASYNC_QUERY_STRING} attributes of the request. These
     * attributes will always reflect the original path elements, even under
     * repeated dispatches.
     *
     * <p>See {@link #dispatch()} for additional details.
     *
     * @param path the path of the dispatch target, scoped to the
     * ServletContext from which this AsyncContext was initialized
     *
     * @exception IllegalStateException if {@link #complete} has already
     * been called
     *
     * @see ServletRequest#getDispatcherType
     */
    public void dispatch(String path);


    /**
     * Dispatches the request and response objects of this AsyncContext
     * to the given <tt>path</tt> scoped to the given <tt>context</tt>.
     *
     * <p>The <tt>path</tt> parameter is interpreted in the same way 
     * as in {@link ServletRequest#getRequestDispatcher(String)}, except that
     * it is scoped to the given <tt>context</tt>.
     *
     * <p>All path related query methods of the request must reflect the
     * dispatch target, while the original request URI, context path,
     * path info, servlet path, and query string may be recovered from
     * the {@link #ASYNC_REQUEST_URI}, {@link #ASYNC_CONTEXT_PATH},
     * {@link #ASYNC_PATH_INFO}, {@link #ASYNC_SERVLET_PATH}, and
     * {@link #ASYNC_QUERY_STRING} attributes of the request. These
     * attributes will always reflect the original path elements, even under
     * repeated dispatches.
     *
     * <p>See {@link #dispatch()} for additional details.
     *
     * @param context the ServletContext of the dispatch target
     * @param path the path of the dispatch target, scoped to the given
     * ServletContext
     *
     * @exception IllegalStateException if {@link #complete} has already
     * been called
     *
     * @see ServletRequest#getDispatcherType
     */
    public void dispatch(ServletContext context, String path);


    /**
     * Completes the asynchronous operation that was started on the request
     * that was used to initialze this AsyncContext, closing the response
     * that was used to initialize this AsyncContext.
     *
     * <p>Any listeners of type {@link AsyncListener} that were added to the
     * request that was used to initialize this AsyncContext will have their
     * {@link AsyncListener#onComplete(AsyncEvent)} method invoked.
     */
    public void complete();


    /**
     * Dispatches a container thread to run the specified Runnable in the
     * {@link ServletContext} that initialized this AsyncContext.
     *
     * @param run the asynchronous handler
     */
    public void start(Runnable run);
}


