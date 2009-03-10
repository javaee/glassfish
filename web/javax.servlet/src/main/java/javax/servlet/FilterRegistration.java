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

import java.util.EnumSet;
import java.util.Map;

/**
 * Class through which a {@link Filter} (either annotated or declared
 * in the deployment descriptor or added via
 * {@link ServletContext#addFilter(String, String)}) may be further 
 * configured.
 *
 * <p>While all aspects of a Filter added via
 * {@link ServletContext#addFilter(String, String)}) are configurable,
 * the only configurable aspects of an annotated or declared Filter are
 * its initialization parameters and mappings. Initialization parameters
 * may only be added, but not overridden.
 *
 * @since 3.0
 */
public interface FilterRegistration {

    /**
     * Sets the description on the filter for which this 
     * FilterRegistration was created.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param description the description of the filter
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */
    public boolean setDescription(String description);


    /**
     * Sets the initialization parameter with the given name and value
     * on the filter for which this FilterRegistration was created.
     *
     * @param name the initialization parameter name
     * @param value the initialization parameter value
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given name or value is
     * <tt>null</tt>
     */ 
    public boolean setInitParameter(String name, String value);


    /**
     * Sets the given initialization parameters on the filter for which
     * this FilterRegistration was created.
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
     * may be updated only up to the point where the filter is being
     * initialized (see {@link Filter#init}).
     *
     * @param initParameters the initialization parameters
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given map contains an
     * initialization parameter with a <tt>null</tt> name or value
     */ 
    public boolean setInitParameters(Map<String, String> initParameters);


    /**
     * Sets the initialization attribute with the given name and value
     * on the filter for which this FilterRegistration was created.
     *
     * @param name the initialization attribute name
     * @param value the initialization attribute value
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given name or value is
     * <tt>null</tt>
     */ 
    public boolean setInitAttribute(String name, Object value);


    /**
     * Sets the given initialization attributes on the filter for which
     * this FilterRegistration was created.
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
     * may be updated beyond the point where the filter has been
     * initialized (see {@link Filter#init}).
     *
     * @param initAttributes the initialization attributes
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     * @throws IllegalArgumentException if the given map contains an
     * initialization attribute with a <tt>null</tt> name or value
     */ 
    public boolean setInitAttributes(Map<String, Object> initAttributes);


    /**
     * Configures the filter for which this FilterRegistration was created
     * as supporting asynchronous operations or not.
     *
     * <p>By default, a filter does not support asynchronous operations.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param isAsyncSupported true if the filter supports asynchronous
     * operations, false otherwise
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */
    public boolean setAsyncSupported(boolean isAsyncSupported);


    /**
     * Adds a filter mapping with the given servlet names and dispatcher
     * types for the filter for which this FilterRegistration was created.
     *
     * <p>Filter mappings are matched in the order in which they were
     * added.
     * 
     * <p>Depending on the value of the <tt>isMatchAfter</tt> parameter, the
     * given filter mapping will be considered after or before any
     * <i>declared</i> filter mappings of the ServletContext from which this
     * FilterRegistration was obtained.
     *
     * @param dispatcherTypes the dispatcher types of the filter mapping,
     * or null if the default <tt>DispatcherType.REQUEST</tt> is to be used
     * @param isMatchAfter true if the given filter mapping should be matched
     * after any declared filter mappings, and false if it is supposed to
     * be matched before any declared filter mappings of the ServletContext
     * from which this FilterRegistration was obtained
     * @param servletNames the servlet names of the filter mapping
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalArgumentException if <tt>servletNames</tt> is null or
     * empty
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */
    public boolean addMappingForServletNames(
        EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... servletNames);


    /**
     * Adds a filter mapping with the given url patterns and dispatcher
     * types for the filter for which this FilterRegistration was created.
     *
     * <p>Filter mappings are matched in the order in which they were
     * added.
     * 
     * <p>Depending on the value of the <tt>isMatchAfter</tt> parameter, the
     * given filter mapping will be considered after or before any
     * <i>declared</i> filter mappings of the ServletContext from which
     * this FilterRegistration was obtained.
     *
     * @param dispatcherTypes the dispatcher types of the filter mapping,
     * or null if the default <tt>DispatcherType.REQUEST</tt> is to be used
     * @param isMatchAfter true if the given filter mapping should be matched
     * after any declared filter mappings, and false if it is supposed to
     * be matched before any declared filter mappings of the ServletContext
     * from which this FilterRegistration was obtained
     * @param urlPatterns the url patterns of the filter mapping
     *
     * @return true if the update was successful, false otherwise
     *
     * @throws IllegalArgumentException if <tt>urlPatterns</tt> is null or
     * empty
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */
    public boolean addMappingForUrlPatterns(
        EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... urlPatterns);
}

