/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.File;

import com.sun.enterprise.module.common_impl.CompositeEnumeration;

import javax.naming.directory.DirContext;

/**
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
        final BundleClassLoader delegate1 = new BundleClassLoader(bundle);
        final ClassLoader delegate2 =
                Globals.get(ClassLoaderHierarchy.class).getAPIClassLoader();

//        This does not work because of lack of pernmission to call
//        protected methods.
//        DelegatingClassLoader parent =
//                new DelegatingClassLoader(delegate1.getParent());
//        parent.addDelegate(new ReflectiveClassFinder(delegate1));
//        parent.addDelegate(new ReflectiveClassFinder(delegate2));

        ClassLoader parent = new ClassLoader() {
            @Override
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
            {
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
                enumerators.add(delegate1.getResources(name));
                enumerators.add(delegate2.getResources(name));
                return new CompositeEnumeration(enumerators);
            }
        };

        finalClassLoader = new WABClassLoader(parent);
        // This does not work. Find out why TempBundleClassLoader loads a
        // separate copy of Globals.class.
//        shareableTempClassLoader = new WebappClassLoader(
// new TempBundleClassLoader(parent));
//        shareableTempClassLoader.start();
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
         */
        public WABClassLoader(ClassLoader parent) {
            super(parent);
            setDelegate(true); // we always delegate. The default is false in WebappClassLoader!!!
            FileDirContext r = new FileDirContext();
            File base = getSourceDir();
            r.setDocBase(base.getAbsolutePath());

            setResources(r);
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

        // Since we don't set URLs, we need to return appropriate URLs
        // for JSPC to work. See WebappLoader.setClassPath().
        @Override
        public URL[] getURLs()
        {
            return convert((String)bundle.getHeaders().
                    get(org.osgi.framework.Constants.BUNDLE_CLASSPATH));
        }

        private URL[] convert(String bcp) {
            if (bcp == null || bcp.isEmpty()) bcp=".";
            List<URL> urls = new ArrayList<URL>();
            //Bundle-ClassPath entries are separated by ; or ,
            StringTokenizer entries = new StringTokenizer(bcp, ",;");
            String entry;
            while (entries.hasMoreTokens()) {
                entry = entries.nextToken().trim();
                if (entry.startsWith("/")) entry = entry.substring(1);
                try
                {
                    URL url = new File(getSourceDir(), entry).toURI().toURL();
                    urls.add(url);
                }
                catch (MalformedURLException e)
                {
                    logger.logp(Level.WARNING, "OSGiDeploymentContext", "convert", "Failed to add {0} as classpath because of", new Object[]{entry, e.getMessage()});
                }
            }
            return urls.toArray(new URL[0]);
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
                            url = resourceFile.toURL();
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
}
