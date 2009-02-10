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

import java.util.Map;

/**
 * Class representing a handle to a {@link Servlet} registered via
 * {@link ServletContext#addServlet(String, String)}, which may be used to
 * configure the servlet.
 *
 * @since 3.0
 */
public interface ServletRegistration {

    /**
     * Sets the description on the servlet for which this ServletRegistration
     * was created.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param description the description of the servlet
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public void setDescription(String description);


    /*
     * Sets the initialization parameter with the given name and value
     * on the servlet for which this ServletRegistration was created.
     *
     * <p>A call to this method overrides any existing initialization
     * parameter of the same name. Passing in a value of <code>null</code>
     * will remove any existing initialization parameter of the given name.
     *
     * @param name the initialization parameter name
     * @param value the initialization parameter value
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */ 
    public void setInitParameter(String name, String value);


    /*
     * Sets the given initialization parameters on the servlet for which
     * this ServletRegistration was created.
     *
     * <p>The given map of initialization parameters is processed
     * <i>by-value</i>, i.e., for each initialization parameter contained
     * in the map, this method calls {@link setInitParameter(String,String)}.
     *
     * @param initParameters the initialization parameters
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */ 
    public void setInitParameters(Map<String, String> initParameters);


    /*
     * Sets the <code>loadOnStartup</code> priority on the servlet for which
     * this ServletRegistration was created.
     *
     * <p>A <tt>loadOnStartup</tt> value of greater than or equal to zero
     * indicates to the container the initialization priority of the
     * servlet. In this case, the container must instantiate and initialize
     * the servlet during the initialization phase of this servlet context,
     * that is, after it has invoked all of the ServletContextListeners
     * configured for this servlet context at their
     * {@link ServletContextListener#contextInitialized} method.
     *
     * <p>If <tt>loadOnStartup</tt> is a negative integer, the container
     * is free to instantiate and initialize the servlet lazily.
     *
     * <p>The default value for <tt>loadOnStartup</tt> is <code>-1</code>.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param loadOnStartup the initialization priority of the servlet
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public void setLoadOnStartup(int loadOnStartup);


    /*
     * Configures the servlet for which this ServletRegistration was
     * created as supporting asynchronous operations or not.
     *
     * <p>By default, a servlet does not support asynchronous operations.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param isAsyncSupported true if the servlet supports asynchronous
     * operations, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public void setAsyncSupported(boolean isAsyncSupported);


    /**
     * Adds a servlet mapping with the given URL patterns for the servlet
     * for which this ServletRegistration was created.
     *
     * @param urlPatterns the URL patterns of the servlet mapping
     *
     * @throws IllegalArgumentException if <tt>urlPatterns</tt> is null
     * or empty
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public void addMapping(String... urlPatterns);
}

