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

import com.sun.logging.LogDomains;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.api.admin.ServerEnvironment;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for setting up Common Class Loader. As the
 * name suggests, Common Class Loader is common to all deployed applications.
 * Common Class Loader is responsible for loading classes from
 * following URLs (the order is strictly maintained):
 * lib/*.jar:domain_dir/lib/classes:domain_dir/lib/*.jar.
 * Please note that domain_dir/lib/classes comes before domain_dir/lib/*.jar,
 * just like WEB-INF/classes is searched first before WEB-INF/lib/*.jar.
 *
 * It applies a special rule while handling jars in install_root/lib.
 * In order to maintain file layout compatibility (see  issue #9526),
 * we add jars like javaee.jar and appserv-rt.jar which need to be excluded
 * from runtime classloaders in the server side, as they are already available via
 * PublicAPIClassLoader. So, before we add any jar from install_root/lib,
 * we look at their manifest entry and skip the ones that have an entry
 * GlassFish-ServerExcluded: true
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class CommonClassLoaderServiceImpl implements PostConstruct {
    /**
     * The common classloader.
     */
    private ClassLoader commonClassLoader;

    @Inject
    APIClassLoaderServiceImpl acls;

    @Inject
    ServerEnvironment env;

    final static Logger logger = LogDomains.getLogger(CommonClassLoaderServiceImpl.class, LogDomains.LOADER_LOGGER);
    private ClassLoader APIClassLoader;
    private String commonClassPath = "";

    private static final String SERVER_EXCLUDED_ATTR_NAME = "GlassFish-ServerExcluded";

    public void postConstruct() {
        APIClassLoader = acls.getAPIClassLoader();
        assert (APIClassLoader != null);
        createCommonClassLoader();
    }

    private void createCommonClassLoader() {
        List<File> cpElements = new ArrayList<File>();
        File domainDir = env.getDomainRoot();
        // I am forced to use System.getProperty, as there is no API that makes
        // the installRoot available. Sad, but true. Check dev forum on this.
        final String installRoot = System.getProperty(
                SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

        // See https://glassfish.dev.java.net/issues/show_bug.cgi?id=5872
        // In case of embedded GF, we may not have an installRoot.
        if (installRoot!=null) {
            File installDir = new File(installRoot);
            File installLibPath = new File(installDir, "lib");
            if (installLibPath.isDirectory()) {
                Collections.addAll(cpElements,
                        installLibPath.listFiles(new CompiletimeJarFileFilter()));
            }
        } else {
            logger.logp(Level.WARNING, "CommonClassLoaderServiceImpl",
                    "createCommonClassLoader",
                    "System property called {0} is null, is this intended?",
                    new Object[]{SystemPropertyConstants.INSTALL_ROOT_PROPERTY});
        }
        File domainClassesDir = new File(domainDir, "lib/classes/"); // NOI18N
        if (domainClassesDir.exists()) {
            cpElements.add(domainClassesDir);
        }
        final File domainLib = new File(domainDir, "lib/"); // NOI18N
        if (domainLib.isDirectory()) {
            Collections.addAll(cpElements,
                    domainLib.listFiles(new JarFileFilter()));
        }
        List<URL> urls = new ArrayList<URL>();
        StringBuilder cp = new StringBuilder();
        for (File f : cpElements) {
            try {
                urls.add(f.toURI().toURL());
                if (cp.length() > 0) {
                    cp.append(File.pathSeparator);
                }
                cp.append(f.getAbsolutePath());
            } catch (MalformedURLException e) {
                logger.logp(Level.WARNING, "ClassLoaderManager",
                        "postConstruct", "e = {0}", new Object[]{e});
            }
        }
        commonClassPath = cp.toString();
        if (!urls.isEmpty()) {
            // Skip creation of an unnecessary classloader in the hierarchy,
            // when all it would have done was to delegate up.
            commonClassLoader = new URLClassLoader(
                    urls.toArray(new URL[urls.size()]), APIClassLoader);
        } else {
            logger.logp(Level.FINE, "CommonClassLoaderManager",
                    "Skipping creation of CommonClassLoader " +
                            "as there are no libraries available",
                    "urls = {0}", new Object[]{urls});
        }
    }

    public ClassLoader getCommonClassLoader() {
        return commonClassLoader != null ? commonClassLoader : APIClassLoader;
    }

    public String getCommonClassPath() {
        return commonClassPath;
    }

    private static class JarFileFilter implements FilenameFilter {
        private final String JAR_EXT = ".jar"; // NOI18N

        public boolean accept(File dir, String name) {
            return name.endsWith(JAR_EXT);
        }
    }

    private static class CompiletimeJarFileFilter extends JarFileFilter {
        /*
         * See https://glassfish.dev.java.net/issues/show_bug.cgi?id=9526
         */
        @Override
        public boolean accept(File dir, String name)
        {
            if (super.accept(dir, name)) {
                File file = new File(dir, name);
                JarFile jar = null;
                try
                {
                    jar = new JarFile(file);
                    Manifest manifest = jar.getManifest();
                    if (manifest != null) {
                        String exclude = manifest.getMainAttributes().
                                getValue(SERVER_EXCLUDED_ATTR_NAME);
                        if (exclude != null && exclude.equalsIgnoreCase("true")) {
                            return false;
                        }
                    }
                }
                catch (IOException e)
                {
                    logger.log(Level.WARNING, "CommonClassLoaderServiceImpl " +
                            "is unable to process " + file.getAbsolutePath() +
                            " because of exception.", e);
                } finally {
                    try
                    {
                        if (jar != null) jar.close();
                    }
                    catch (IOException e)
                    {
                        // ignore
                    }
                }
                return true;
            }
            return false;
        }
    }
}
