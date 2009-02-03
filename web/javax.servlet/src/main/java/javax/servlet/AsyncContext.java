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
 * @since 3.0
 */
public interface AsyncContext {

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
     * Forwards the request and response objects that were used to 
     * initialize this AsyncContext to the original URI of the request,
     * unless {@link forward(String)}, {@link forward(ServletContext, String)},
     * or {@link RequestDispatcher#forward(ServletRequest, ServletResponse)}
     * was called, in which case the request and response objects will be
     * forwarded to the target resource of the forward or the target
     * resource for which the RequestDispatcher was acquired, respectively.
     *
     * <p>This method returns immediately after dispatching a container
     * managed thread to do a {@link RequestDispatcher#forward(ServletRequest,
     * ServletResponse)} to the target resource, and setting the dispatcher
     * type of the request to <code>DispatcherType.ASYNC</code>.
     *
     * <p>Control over the request and response objects of this AsyncContext
     * is handed off to the target resource of the dispatch, and the 
     * response will be closed when the target resource of the dispatch
     * has completed execution, unless {@link ServletRequest#startAsync()}
     * or {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}
     * was called during the execution of the target resource, in which case
     * the response will not be closed.
     *
     * @exception IllegalStateException if {@link #complete} has already
     * been called
     *
     * @see ServletRequest#getDispatcherType
     */
    public void forward();


    /**
     * Forwards the request and response objects that were used to 
     * initialize this AsyncContext to the resource with the given path.
     *
     * <p>The <code>path</code> parameter is interpreted in the same way 
     * as in {@link ServletRequest#getRequestDispatcher(String)}, except that
     * it is scoped to the {@link ServletContext} that initialized this
     * AsyncContext.
     *
     * <p>This method returns immediately after dispatching a container
     * managed thread to do a {@link RequestDispatcher#forward(ServletRequest,
     * ServletResponse)} to the target resource, and setting the dispatcher
     * type of the request to <code>DispatcherType.ASYNC</code>.
     *
     * <p>Control over the request and response objects of this AsyncContext
     * is handed off to the target resource of the dispatch, and the 
     * response will be closed when the target resource of the dispatch
     * has completed execution, unless {@link ServletRequest#startAsync()}
     * or {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}
     * was called during the execution of the target resource, in which case
     * the response will not be closed.
     *
     * @param path the path of the target resource of the request dispatch
     *
     * @exception IllegalStateException if {@link #complete} has already
     * been called
     *
     * @see ServletRequest#getDispatcherType
     */
    public void forward(String path);


    /**
     * Forwards the request and response objects that were used to
     * initialize this AsyncContext to the resource with the given path
     * scoped to the given ServletContext.
     *
     * <p>This method returns immediately after dispatching a container
     * managed thread to do a {@link RequestDispatcher#forward(ServletRequest,
     * ServletResponse)} to the target resource, and setting the dispatcher
     * type of the request to <code>DispatcherType.ASYNC</code>.
     *
     * <p>Control over the request and response objects of this AsyncContext
     * is handed off to the target resource of the dispatch, and the 
     * response will be closed when the target resource of the dispatch
     * has completed execution, unless {@link ServletRequest#startAsync()}
     * or {@link ServletRequest#startAsync(ServletRequest, ServletResponse)}
     * was called during the execution of the target resource, in which case
     * the response will not be closed.
     *
     * @param context the target ServletContext of the dispatch
     * @param path the path of the target resource of the dispatch,
     * relative to the given ServletContext
     *
     * @exception IllegalStateException if {@link #complete} has already
     * been called
     *
     * @see ServletRequest#getDispatcherType
     */
    public void forward(ServletContext context, String path);


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


