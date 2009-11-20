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
import com.sun.enterprise.module.ModuleLifecycleListener;
import com.sun.enterprise.module.common_impl.CompositeEnumeration;
import com.sun.logging.LogDomains;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
import java.net.URL;

/**
 * This class is responsible for creating a ClassLoader that can
 * load classes exported by any OSGi bundle in the system for public use.
 * Such classes include Java EE API, AMX API, appserv-ext API, etc.
 * CommonClassLoader delegates to this class loader..
 * It does special treatment of META-INF/mailcap file. For such resources,
 * it searches all available bundles.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class APIClassLoaderServiceImpl implements PostConstruct {

    /*
     * Implementation Note:
     * 1. This class depends on OSGi runtime, so not portable on HK2.
     * 2. APIClassLoader maintains a blacklist, i.e., classes and resources that could not be loaded to avoid
     * unnecessary delegation. It flushes that list everytime a new bundle is installed in the system.
     * This takes care of performance problem in typical production use of GlassFish.
     */

    private ClassLoader theAPIClassLoader;
    @Inject
    ModulesRegistry mr;
    private static final String APIExporterModuleName =
            "GlassFish-Application-Common-Module"; // NOI18N
    private static final String MAILCAP = "META-INF/mailcap";
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
        assert (APIModule != null);
        final ClassLoader apiModuleLoader = APIModule.getClassLoader();
        /*
         * We don't directly retrun APIModule's class loader, because
         * that class loader does not delegate to the parent. Instead, it
         * relies on OSGi bundle to load the classes. That behavior is
         * fine if we want to honor OSGi classloading semantics. APIClassLoader
         * wants to use delegation model so that we don't have to set
         * bootdelegation=* for OSGi bundles.
         */
        theAPIClassLoader = new APIClassLoader(apiModuleLoader);
        logger.logp(Level.FINE, "APIClassLoaderService", "createAPIClassLoader",
                "APIClassLoader = {0}", new Object[]{theAPIClassLoader});
    }

    public ClassLoader getAPIClassLoader() {
        return theAPIClassLoader;
    }

    private class APIClassLoader extends ClassLoader {

        // list of not found classes and resources.
        // the string represents resource name, so foo/Bar.class for foo.Bar
        private Set<String> blacklist;
        private final ClassLoader apiModuleLoader;

        public APIClassLoader(ClassLoader apiModuleLoader) {
            super(apiModuleLoader.getParent());
            this.apiModuleLoader = apiModuleLoader;
            blacklist = new HashSet<String>();

            // add a listener to manage blacklist in APIClassLoader
            mr.register(new ModuleLifecycleListener() {
                public void moduleInstalled(Module module) {
                    clearBlackList();
                }

                public void moduleResolved(Module module) {
                }

                public void moduleStarted(Module module) {
                }

                public void moduleStopped(Module module) {
                }

                public void moduleUpdated(Module module) {
                    clearBlackList();
                }
            });

        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // First check if we know this can't be loaded
            final String resourceName = convertToResourceName(name);
            if (isBlackListed(resourceName)) {
                throw new ClassNotFoundException(name);
            }

            // Then check if the class has already been loaded
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
                    try {
                        c = super.loadClass(name, resolve);
                    } catch (ClassNotFoundException e) {
                        addToBlackList(resourceName);
                        throw e;
                    }
                }
            }
            return c;
        }

        @Override
        public URL getResource(String name) {
            if (isBlackListed(name)) return null;
            URL url = null;
            if (!name.startsWith("java/")) {
                if (name.equals(MAILCAP)) {
                    // punch in for META-INF/mailcap files.
                    // see issue #8426
                    for (Module m : mr.getModules()) {
                        if ((url = m.getClassLoader().getResource(name)) != null) {
                            break;
                        }
                    }
                } else {
                    url = apiModuleLoader.getResource(name);
                }
            }
            if (url == null) {
                // Either requested resource belongs to java/ namespace or
                // it was not found in any of the bundles, so call
                // super class implementation which will delegate to parent.
                url = super.getResource(name);
            }
            if (url == null) {
                addToBlackList(name);
            }
            return url;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException {
            List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
            if (!name.startsWith("java/")) {
                if (name.equals(MAILCAP)) {
                    // punch in for META-INF/mailcap files.
                    // see issue #8426
                    for (Module m : mr.getModules()) {
                        enumerators.add(m.getClassLoader().getResources(name));
                    }
                } else {
                    enumerators.add(apiModuleLoader.getResources(name));
                }
            }
            // Either requested resource belongs to java/ namespace or
            // it was not found in any of the bundles, so call
            // super class implementation which will delegate to parent.
            enumerators.add(super.getResources(name));
            return new CompositeEnumeration(enumerators);
        }

        @Override
        public String toString() {
            return "APIClassLoader";
        }

        /**
         * Takes a class name as used in Class.forName and converts it to a resource name as used in
         * ClassLoader.getResource
         *
         * @param className className to be converted
         * @return equivalent resource name
         */
        private String convertToResourceName(String className) {
            return className.replace('.', '/').concat(".class");
        }

        private synchronized boolean isBlackListed(String name) {
            return blacklist.contains(name);
        }

        private synchronized void addToBlackList(String name) {
            blacklist.add(name);
        }

        private synchronized void clearBlackList() {
            blacklist.clear();
        }

    }
}
