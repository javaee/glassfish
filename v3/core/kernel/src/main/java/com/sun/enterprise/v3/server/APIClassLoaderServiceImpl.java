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


package com.sun.enterprise.v3.server;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.CompositeEnumeration;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;

/**
 * This class is responsible for creating a ClassLoader that can
 * load classes exported by any OSGi bundle in the system for public use.
 * Such classes include Java EE API, AMX API, appserv-ext API, etc.
 * CommonClassLoader delegates to this class loader..
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class APIClassLoaderServiceImpl implements PostConstruct {

    /*
     * Implementation Note: This class depends on OSGi runtime, so
     * not portable on HK2.
     */
    private ClassLoader APIClassLoader;
    @Inject
    ModulesRegistry mr;
    private static final String APIExporterModuleName =
            "GlassFish-Application-Common-Module"; // NOI18N
    final static Logger logger = LogDomains.getLogger(APIClassLoaderServiceImpl.class, LogDomains.LOADER_LOGGER);
    private Module APIModule;

    public void postConstruct() {
        try {
            createAPIClassLoader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createAPIClassLoader() throws IOException {
        APIModule = mr.getModules(APIExporterModuleName).iterator().next();
        assert(APIModule != null);
        final ClassLoader apiModuleLoader = APIModule.getClassLoader();
        /*
         * We don't directly retrun APIModule's class loader, because
         * that class loader does not delegate to the parent. Instead, it
         * relies on OSGi bundle to load the classes. That behavior is
         * fine if we want to honor OSGi classloading semantics. APIClassLoader
         * wants to use delegation model so that we don't have to set
         * bootdelegation=* for OSGi bundles.
         */
        APIClassLoader = new ClassLoader(apiModuleLoader.getParent()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException
            {
                return loadClass(name, false);
            }

            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
            {
                // First, check if the class has already been loaded
                Class c = findLoadedClass(name);
                if (c == null) {
                    if (!name.startsWith("java.")) { // java classes always come from parent
                        try {
                            c = apiModuleLoader.loadClass(name); // we ignore the resolution flag
                        } catch (ClassNotFoundException cnfe) {
                        }
                    }
                    if (c == null) {
                        // Call super class implementation which takes care of
                        // delegating to parent.
                        c = super.loadClass(name, resolve);
                    }
                }
                return c;
            }

            @Override
            public URL getResource(String name)
            {
                URL url = null;
                if (!name.startsWith("java/")) {
                    url = apiModuleLoader.getResource(name);
                }
                if (url == null) {
                    url = super.getResource(name);
                }
                return url;
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException
            {
                List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
                enumerators.add(super.getResources(name));
                if (!name.startsWith("java/")) {
                    enumerators.add(apiModuleLoader.getResources(name));
                }
                return new CompositeEnumeration(enumerators);
            }
        };
        logger.logp(Level.INFO, "APIClassLoaderService", "createAPIClassLoader",
                "APIClassLoader = {0}", new Object[]{APIClassLoader});
    }

    public ClassLoader getAPIClassLoader() {
        return APIClassLoader;
    }

}
