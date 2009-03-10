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
 * Class through which a {@link Servlet} (either annotated or declared
 * in the deployment descriptor or added via
 * {@link ServletContext#addServlet(String, String)}) may be further 
 * configured.
 *
 * <p>While all aspects of a Servlet added via
 * {@link ServletContext#addServlet(String, String)}) are configurable,
 * the only configurable aspects of an annotated or declared Servlet are
 * its initialization parameters and mappings. Initialization parameters
 * may only be added, but not overridden.
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
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public boolean setDescription(String description);


    /**
     * Sets the initialization parameter with the given name and value
     * on the servlet for which this ServletRegistration was created.
     *
     * @param name the initialization parameter name
     * @param value the initialization parameter value
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given name or value is
     * <tt>null</tt>
     */ 
    public boolean setInitParameter(String name, String value);


    /**
     * Sets the given initialization parameters on the servlet for which
     * this ServletRegistration was created.
     *
     * <p>The given map of initialization parameters is processed
     * <i>by-value</i>, i.e., for each initialization parameter contained
     * in the map, this method calls {@link setInitParameter(String,Object)}.
     * If that method would return false for any of the
     * initialization parameters in the given map, no updates will be
     * performed, and false will be returned. Likewise, if the map contains
     * an initialization parameter with a <tt>null</tt> name or value, no
     * updates will be performed, and an IllegalArgumentException will be
     * thrown.
     *
     * <p>Unlike the initialization attribute map
     * (see {@link #setInitAttributes}), the initialization parameter map
     * may be updated only up to the point where the servlet is being
     * initialized (see {@link Servlet#init}).
     *
     * @param initParameters the initialization parameters
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given map contains an
     * initialization parameter with a <tt>null</tt> name or value
     */ 
    public boolean setInitParameters(Map<String, String> initParameters);


    /**
     * Sets the initialization attribute with the given name and value
     * on the servlet for which this ServletRegistration was created.
     *
     * @param name the initialization attribute name
     * @param value the initialization attribute value
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given name or value is
     * <tt>null</tt>
     */ 
    public boolean setInitAttribute(String name, Object value);


    /**
     * Sets the given initialization attributes on the servlet for which
     * this ServletRegistration was created.
     *
     * <p>The given map of initialization attributes is processed
     * <i>by-value</i>, i.e., for each initialization attribute contained
     * in the map, this method calls {@link setInitAttribute(String,Object)}.
     * If that method would return false for any of the
     * initialization attributes in the given map, no updates will be
     * performed, and false will be returned. Likewise, if the map contains
     * an initialization attribute with a <tt>null</tt> name or value, no
     * updates will be performed, and an IllegalArgumentException will be
     * thrown.
     *
     * <p>Unlike the initialization parameter map
     * (see {@link #setInitParameters}), the initialization attribute map
     * may be updated beyond the point where the servlet has been
     * initialized (see {@link Servlet#init}).
     *
     * @param initAttributes the initialization attributes
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given map contains an
     * initialization attribute with a <tt>null</tt> name or value
     */ 
    public boolean setInitAttributes(Map<String, Object> initAttributes);


    /**
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
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public boolean setLoadOnStartup(int loadOnStartup);


    /**
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
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public boolean setAsyncSupported(boolean isAsyncSupported);


    /**
     * Adds a servlet mapping with the given URL patterns for the servlet
     * for which this ServletRegistration was created.
     *
     * @param urlPatterns the URL patterns of the servlet mapping
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalArgumentException if <tt>urlPatterns</tt> is null
     * or empty
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public boolean addMapping(String... urlPatterns);
}

