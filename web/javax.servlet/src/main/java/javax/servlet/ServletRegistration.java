/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

import java.util.*;

/**
 * Interface through which a {@link Servlet} may be further configured.
 *
 * @since Servlet 3.0
 */
public interface ServletRegistration extends Registration {

    /**
     * Adds a servlet mapping with the given URL patterns for the Servlet
     * represented by this ServletRegistration.
     *
     * <p>If any of the specified URL patterns are already mapped to a 
     * different Servlet, no updates will be performed.
     *
     * @param urlPatterns the URL patterns of the servlet mapping
     *
     * @return the (possibly empty) Set of URL patterns that are already
     * mapped to a different Servlet
     *
     * @throws IllegalArgumentException if <tt>urlPatterns</tt> is null
     * or empty
     * @throws IllegalStateException if the ServletContext from which this
     * ServletRegistration was obtained has already been initialized
     */
    public Set<String> addMapping(String... urlPatterns);

    /**
     * Gets an Iterable over the currently available mappings of the
     * Servlet represented by this ServletRegistration.
     *
     * @return Iterable over the currently available mappings
     * of the Servlet represented by this ServletRegistration. 
     */
    public Iterable<String> getMappings();

    /**
     * Interface through which a {@link Servlet} registered via one of the
     * <tt>addServlet</tt> methods on {@link ServletContext} may be further
     * configured.
     */
    interface Dynamic extends ServletRegistration, Registration.Dynamic {

        /**
         * Sets the <code>loadOnStartup</code> priority on the Servlet
         * represented by this dynamic ServletRegistration.
         *
         * <p>A <tt>loadOnStartup</tt> value of greater than or equal to
         * zero indicates to the container the initialization priority of
	 * the Servlet. In this case, the container must instantiate and
         * initialize the Servlet during the initialization phase of the
	 * ServletContext, that is, after it has invoked all of the
         * ServletContextListener objects configured for the ServletContext
         * at their {@link ServletContextListener#contextInitialized}
         * method.
         *
         * <p>If <tt>loadOnStartup</tt> is a negative integer, the container
         * is free to instantiate and initialize the Servlet lazily.
         *
         * <p>The default value for <tt>loadOnStartup</tt> is <code>-1</code>.
         *
         * <p>A call to this method overrides any previous setting.
         *
         * @param loadOnStartup the initialization priority of the Servlet
         *
         * @throws IllegalStateException if the ServletContext from which
         * this dynamic ServletRegistration was obtained has already been
         * initialized
         */
        public void setLoadOnStartup(int loadOnStartup);
    }

}

