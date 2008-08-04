/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.internal.api;

import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Contract;

import java.net.URI;
import java.net.MalformedURLException;
import java.util.List;

/**
 * This class is responsible foe creation of class loader hierarchy
 * of an application.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Contract
public interface ClassLoaderHierarchy {
    /**
     * Returns a ClassLoader that can load classes exported by any OSGi bundle
     * in the system for public use. Such classes include Java EE API, AMX API,
     * appserv-ext API, etc. CommonClassLoader delegates to this class loader.
     * @return a ClassLoader that can load classes exported by any bundles
     */
    ClassLoader getAPIClassLoader();

    /**
     * Returns a class loader that is common to all deployed applications.
     * Common Class Loader is responsible for loading classes from
     * following URLs (the order is strictly maintained):
     * lib/*.jar:domain_dir/classes:domain_dir/lib/*.jar.
     * Please note that domain_dir/classes comes before domain_dir/lib/*.jar,
     * just like WEB-INF/classes is searched first before WEB-INF/lib/*.jar.
     * It delegates to APIClassLoader.
     * @see #getAPIClassLoader()
     * @return ClassLoader common to all deployed applications.
     */
    ClassLoader getCommonClassLoader();

    /**
     * Returns the class loader which has visibility to appropriate list of
     * standalone RARs deployed in the server. Depending on a policy,
     * this can either return a singleton classloader for all applications or
     * a class loader specific to an application. When a singleton class loader
     * is returned, such a class loader will have visibility to all the
     * standalone RARs deployed in the system. When a class loader specific
     * to an application is returned, such a class loader will have visibility
     * to only standalone RARs that the application depends on.
     *
     * @param application Application whose class loader hierarchy is being set
     * @return class loader which has visibility to appropriate list of
     *         standalone RARs.
     */
    DelegatingClassLoader getConnectorClassLoader(String application);

    /**
     * Returns AppLibClassLoader. As the name suggests, this class loader
     * has visibility to deploy time libraries (--libraries) for an application.
     * It is different from CommonClassLoader in a sense that the libraries that
     * are part of common class loader are shared by all applications,
     * where as this class loader adds a scope to a library.
     * @param application Application for which this class loader is created
     * @param libURIs list of URIs, where each URI represent a library
     * @return class loader that has visibility to appropriate
     * application specific libraries.
     * @throws MalformedURLException
     */
    ClassLoader getAppLibClassLoader(String application, List<URI> libURIs)
            throws MalformedURLException;
}
