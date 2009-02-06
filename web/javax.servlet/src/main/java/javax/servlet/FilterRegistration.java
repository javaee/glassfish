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
 * Class representing a handle to a {@link Filter} registered via
 * {@link ServletContext#addFilter(String, String)}, which may be used to
 * configure the registered filter.
 *
 * @since 3.0
 */
public abstract class FilterRegistration {

    protected String description;
    protected boolean isAsyncSupported;


    /**
     * Sets the descriptions of the filter.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param description the description of the filter
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /*
     * Sets the initialization parameter with the given name and value
     * on the filter.
     *
     * <p>A call to this method overrides any existing initialization
     * parameter of the same name. Passing in a value of <code>null</code>
     * will remove any existing initialization parameter of the given name.
     *
     * @param name the initialization parameter name
     * @param value the initialization parameter value
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */ 
    public abstract void setInitParameter(String name, String value);


    /*
     * Sets the given initialization parameters on the filter.
     *
     * <p>The given map of initialization parameters is processed
     * <i>by-value</i>, i.e., for each initialization parameter contained
     * in the map, this method calls {@link setInitParameter(String,String)}.
     *
     * @param initParameters the initialization parameters
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */ 
    public void setInitParameters(Map<String, String> initParameters) {
        if (null == initParameters) {
            throw new IllegalArgumentException("Null init parameters");
        }
        for (Map.Entry<String, String> e : initParameters.entrySet()) {
            setInitParameter(e.getKey(), e.getValue());
        }
    }


    /*
     * Configures the filter as supporting asynchronous operations or not.
     *
     * <p>By default, a servlet does not support asynchronous operations.
     *
     * <p>A call to this method overrides any previous setting.
     *
     * @param isAsyncSupported true if the filter supports asynchronous
     * operations, false otherwise
     *
     * @throws IllegalStateException if the ServletContext from which this
     * FilterRegistration was obtained has already been initialized
     */
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.isAsyncSupported = isAsyncSupported;
    }
}

