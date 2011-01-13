/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.glassfish.osgiweb;

import org.apache.naming.resources.FileDirContext;
import org.glassfish.osgijavaeebase.OSGiDeploymentContext;
import org.glassfish.osgijavaeebase.BundleClassLoader;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.web.loader.ResourceEntry;
import org.glassfish.web.loader.WebappClassLoader;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.OpsParams;
import org.osgi.framework.*;

import java.io.FileFilter;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.File;

import com.sun.enterprise.module.common_impl.CompositeEnumeration;

/**
 * This is at the heart of WAB support. It is responsible for setting up
 * a class loader for the WAB. In theory, a WAB's class loader should just be a simple wrapper around
 * the Bundle object, but in truth we need to take care of all the special requirements mostly
 * around resource finding logic to ensure a WAB behaves like a WAR in our web container. So,
 * we create a special class loader called {@link org.glassfish.osgiweb.OSGiWebDeploymentContext.WABClassLoader}
 * and set that in the deployment context.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
class OSGiWebDeploymentContext extends OSGiDeploymentContext {

    private static final Logger logger =
            Logger.getLogger(OSGiWebDeploymentContext.class.getPackage().getName());

    public OSGiWebDeploymentContext(ActionReport actionReport,
                                            Logger logger,
                                            ReadableArchive source,
                                            OpsParams params,
                                            ServerEnvironment env,
                                            Bundle bundle) throws Exception {
        super(actionReport, logger, source, params, env, bundle);
    }

    protected void setupClassLoader() throws Exception     {
        finalClassLoader = new WABClassLoader(null);
        shareableTempClassLoader = finalClassLoader;
        WebappClassLoader.class.cast(finalClassLoader).start();
    }

    private class WABClassLoader extends WebappClassLoader {
        /*
         * We need this class loader for variety of reasons explained below:
         * a) GlassFish default servlet (DefaultServlet.java), the servlet responsible for serving static content
         * fails to serve any static content from META-INF/resources/ of WEB-INF/lib/*.jar, if the classloader is not
         * an instanceof WebappClassLoader.
         * b) DefaultServlet also expects WebappClassLoader's resourceEntries to be properly populated.
         * c) JSPC relies on getURLs() methods so that it can discover TLDs in the web app. Setting up
         * repositories and jar files ensures that WebappClassLoader's getURLs() method will
         * return appropriate URLs for JSPC to work.
         *
         * It overrides loadClass(), getResource() and getResources() as opposed to
         * their findXYZ() equivalents so that the OSGi export control mechanism
         * is enforced even for classes and resources available in the system/boot class loader.
         * The only time this class loader is defining class loader for some classes is when this class loader
         * is used by containers like CDI or EJB to define generated classes.
         */

        final BundleClassLoader delegate1 = new BundleClassLoader(bundle);
        final ClassLoader delegate2 =
                Globals.get(ClassLoaderHierarchy.class).getAPIClassLoader();

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            return loadClass(name, false);
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
        {
            Class c = findLoadedClass(name); // this class loader may be the defining loader for a proxy or generated class 
            if (c != null) return c;
            // mojarra uses Thread's context class loader (which is us) to look up custom annotation provider.
            // since we don't export our package and in fact hide our provider, we need to load them using
            // current loader.
            if (hiddenServices.contains(name)) {
                return Class.forName(name);
            }
            try {
                return delegate1.loadClass(name, resolve);
            } catch (ClassNotFoundException cnfe) {
                return delegate2.loadClass(name);
            }
        }

        @Override
        public URL getResource(String name)
        {
            URL url = delegate1.getResource(name);
            if (url == null) {
                url = delegate2.getResource(name);
            }
            return url;
        }

        @Override
        public Enumeration<URL> getResources(String name) throws IOException
        {
            List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
            final String mappedResourcePath = hiddenServicesMap.get(name);
            if (mappedResourcePath != null) {
                return getClass().getClassLoader().getResources(mappedResourcePath);
            }
            enumerators.add(delegate1.getResources(name));
            enumerators.add(delegate2.getResources(name));
            return new CompositeEnumeration(enumerators);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            // We need to override this method because of the stupid WebappClassLoader that for some reason
            // not only overrides getResourceAsStream, it also does not delegate to getResource method.
            URL url = getResource(name);
            try {
                return url != null ? url.openStream() : null;
            } catch (IOException e) {
                return null;
            }
        }

        public WABClassLoader(ClassLoader parent) {
            super(parent);
            setDelegate(true); // we always delegate. The default is false in WebappClassLoader!!!
            FileDirContext r = new FileDirContext();
            File base = getSourceDir();
            r.setDocBase(base.getAbsolutePath());

            setResources(r);

            // add WEB-INF/classes/ and WEB-INF/lib/*.jar to repository list, because many legacy code
            // path like DefaultServlet, JSPC, StandardContext rely on them.
            // See WebappLoader.setClassPath() for example.
            addRepository("WEB-INF/classes/", new File(base, "WEB-INF/classes/"));
            File libDir = new File(base, "WEB-INF/lib");
            if (libDir.exists()) {
                int baseFileLen = base.getPath().length();
                for (File file : libDir.listFiles(
                        new FileFilter() {
                            public boolean accept(File pathname) {
                                String fileName = pathname.getName();
                                return (fileName.endsWith(".jar") && pathname.isFile());
                            }
                        }))
                {
                    try {
                        addJar(file.getPath().substring(baseFileLen),
                                new JarFile(file), file);
                    } catch (Exception e) {
                        // Catch and ignore any exception in case the JAR file
                        // is empty.
                    }
                }
            }
            setWorkDir(getScratchDir("jsp")); // We set the same working dir as set in WarHandler
        }

        @Override
        public URL getResourceFromJars(String name) {
            // We override this method, because both DefaultServlet and StandardContext call this API to find
            // static resources in WEB-INF/lib/*.jar. If we don't override, the default implementation in
            // WebappClassLoader will find the resource via bundle class loader and that won't be acceptable to
            // DefaultServlet or StandardContext.
            assert(name.startsWith("META-INF/resources/"));
            // META-INF/resources punch-in
            if (name.startsWith("META-INF/resources/")) {
                URL url = super.findResource(name);
                if (url != null) {
                    // Locating the repository for special handling in the case
                    // of a JAR
                    ResourceEntry entry = resourceEntries.get(name);
                    try {
                        String repository = entry.codeBase.toString();
                        if ((repository.endsWith(".jar"))
                                && !(name.endsWith(".class"))
                                && !(name.endsWith(".jar"))) {
                            // Copy binary content to the work directory if not present
                            File resourceFile = new File(loaderDir, name);
                            url = resourceFile.toURI().toURL();
                        }
                    } catch (Exception e) {
                        // Ignore
                    }
                    return url;
                }
            } else {
                return super.getResourceFromJars(name);
            }
            return null;
        }
    }

    /**
     * We don't package our custom providers as a META-INF/services/, for doing so will make them
     * visible to non hybrid applications as well. So, we package it at a different location and
     * punch in our classloader appropriately. This map holds the key name that client is looking for
     * and the value is where we have placed it in our bundle.
     */
    private static Map<String, String> hiddenServicesMap;

    /**
     * Since mojarra uses thread's context class loader to look up custom providers and our custom providers
     * are not available via APIClassLoader's META-INF/service punch-in mechanism, we need to make them visible
     * specially. This field maintains a list of such service class names.
     * As much as we would like to hide {@link org.glassfish.osgiweb.OSGiWebModuleDecorator}, we can't, because
     * that's looked up via habitat, which means it has to be either present as META-INF/services in the bundle itself
     * or added as an existing inhabitant. We have gone for the latter approach for the decorator. The other providers
     * that are looked up by mojarra are hidden using the technique implemented here.
     */
    private static Collection<String> hiddenServices;
    static {
        Map<String, String> map = new HashMap<String, String>();

        // This is for the custom AnnotationProvider. Note that Mojarra surprising uses different nomenclature than
        // what is used by JDK SPI. The service type is AnnotationProvider, yet it looks for annotationprovider.
        map.put("META-INF/services/com.sun.faces.spi.annotationprovider",
                "META-INF/hiddenservices/com.sun.faces.spi.annotationprovider");

        // This is for our custom faces-config.xml discoverer
        map.put("META-INF/services/com.sun.faces.spi.FacesConfigResourceProvider",
                "META-INF/hiddenservices/com.sun.faces.spi.FacesConfigResourceProvider");

        // This is for our custom taglib.xml discoverer
        map.put("META-INF/services/com.sun.faces.spi.FaceletConfigResourceProvider",
                "META-INF/hiddenservices/com.sun.faces.spi.FaceletConfigResourceProvider");
        hiddenServicesMap = Collections.unmodifiableMap(map);

        hiddenServices = Collections.unmodifiableList(Arrays.asList(
                OSGiFacesAnnotationScanner.class.getName(),
                OSGiFaceletConfigResourceProvider.class.getName(),
                OSGiFacesConfigResourceProvider.class.getName()
        ));
    }

}
