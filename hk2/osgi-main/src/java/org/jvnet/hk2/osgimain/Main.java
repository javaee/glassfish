/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.osgimain;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * Goes through a directory structure recurssively and installs all the
 * jar files as OSGi bundles. This bundle can be passed a list of bundle paths
 * to be started automatically. The bundle path is treated relative to the
 * directory from where it installs all the bundles. It does not stop those bundle
 * when this bundle is stopped. It stops them in reverse order.
 *
 * This bundle is also responsible for updating or uninstalling bundle during
 * subsequent restart if jars have been updated or deleted.
 *
 * It does not manage itself. So, we use reference: scheme to install this bundle
 * which allows us to see changes to this jar file automatically.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Main implements BundleActivator
{
    /*
     * The reason for this bundle not to use config admin service is that config admin is not part of core framework.
     * This being a provisioning service itself can't expect too many other services to be available. So, it relies on
     * core framework services only.
     */
    
    public static final String BUNDLES_DIR =
            "org.jvnet.hk2.osgimain.bundlesDir";

    // a comma separated list of dir names relative to bundles dir
    // that need to be excluded. e.g., autostart
    public static final String EXCLUDED_SUBDIRS = "org.jvnet.hk2.osgimain.excludedSubDirs";
    public final static String HK2_CACHE_DIR = "com.sun.enterprise.hk2.cacheDir";
    public final static String INHABITANTS_CACHE = "inhabitants";
    public static final String AUTO_START_BUNDLES_PROP =
            "org.jvnet.hk2.osgimain.autostartBundles";

    private static final Logger logger =
            Logger.getLogger(Main.class.getPackage().getName());

    private BundleContext context;

    private File bundlesDir;

    // files under bundles dir structure that are excluded from processing
    // if any of the excluded file is a directory, then entire content
    // of the directiry is excluded.
    private Collection<File> excludedSubDirs = new HashSet<File>();

    private List<URI> autoStartBundleLocations = new ArrayList<URI>();
    private Map<URI, Jar> currentManagedBundles = new HashMap<URI, Jar>();
    private static final String THIS_JAR_NAME = "osgi-main.jar";

    public void start(BundleContext context) throws Exception
    {
        this.context = context;
        final String bundlesDirPath = getProperty(BUNDLES_DIR);
        if (bundlesDirPath == null) {
            // nothing to do, let's return
            return;
        }
        bundlesDir = new File(bundlesDirPath);
        String autostartBundlesProp = getProperty(AUTO_START_BUNDLES_PROP);
        if (autostartBundlesProp != null) {
            StringTokenizer st = new StringTokenizer(autostartBundlesProp, ",");
            while (st.hasMoreTokens()) {
                String bundleRelPath = st.nextToken().trim();
                if (bundleRelPath.isEmpty()) break;
                URI bundleURI = new File(bundlesDir, bundleRelPath).toURI().normalize();
                autoStartBundleLocations.add(bundleURI);
            }
        }
        String excludedFilesProp = getProperty(EXCLUDED_SUBDIRS);
        if (excludedFilesProp != null) {
            for (String s : excludedFilesProp.split(",")) {
                excludedSubDirs.add(new File(bundlesDir, s.trim()));
            }
        }

        traverse();

        for (URI location : autoStartBundleLocations) {
            long id = currentManagedBundles.get(location).getBundleId();
            if (id < 0) {
                logger.logp(Level.WARNING, "Main", "start", "Not able to locate autostart bundle for location = {0}", new Object[]{location});
                continue;
            }
            final Bundle bundle = context.getBundle(id);
            // check is necessary as bundle could have been uninstalled
            if (bundle != null) {
                try
                {
                    bundle.start(Bundle.START_TRANSIENT);
                }
                catch (BundleException e)
                {
                    logger.logp(Level.WARNING, "Main", "start", "Exception while starting bundle " + bundle, e);
                }
            }
        }
    }

    private String getProperty(String property) {
        String value = context.getProperty(property);
        // Check System properties to work around Equinox Bug:
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=320459
        if (value == null) value = System.getProperty(property);
        return value;
    }

    /**
     * Stops all the autostart bundles in reverse order
     * @param context
     * @throws Exception
     */
    public void stop(BundleContext context) throws Exception
    {
        List<URI> bundlesToStop = new ArrayList<URI>(autoStartBundleLocations);
        Collections.reverse(bundlesToStop);
        for (URI location : bundlesToStop) {
            long id = currentManagedBundles.get(location).getBundleId();
            if (id < 0) {
                logger.logp(Level.WARNING, "Main", "stop",
                        "Not able to locate autostart bundle for location = {0}",
                        new Object[]{location});
                continue;
            }
            final Bundle bundle = context.getBundle(id);
            // check is necessary as bundle could have been uninstalled
            if (bundle != null) bundle.stop();
        }
    }

    private Set<Jar> discoverJars() {
        final Set<Jar> jars = new HashSet<Jar>();
        bundlesDir.listFiles(new FileFilter(){
            final String JAR_EXT = ".jar";
            public boolean accept(File file)
            {
                if (file.isDirectory() && !excludedSubDirs.contains(file)) {
                    file.listFiles(this);
                } else if (file.isFile()
                        && file.getName().endsWith(JAR_EXT)
                        && !file.getName().equals(THIS_JAR_NAME)) {
                    jars.add(new Jar(file));
                    return true;
                }
                return false;
            }
        });
        return jars;
    }

    /**
     * This method goes through all the currently installed bundles
     * and returns information about those bundles whose location
     * refers to a file in our {@link #bundlesDir}.
     */
    private void initCurrentManagedBundles()
    {
        Bundle[] bundles = this.context.getBundles();
        String watchedDirPath = bundlesDir.toURI().normalize().getPath();
        final long thisBundleId = context.getBundle().getBundleId();
        for (Bundle bundle : bundles)
        {
            try
            {
                final long id = bundle.getBundleId();
                if (id == 0 || id == thisBundleId) {
                    // We can't manage system bundle or this bundle
                    continue;
                }
                Jar jar = new Jar(bundle);
                String path = jar.getPath();
                if (path == null)
                {
                    // jar.getPath is null means we could not parse the location
                    // as a meaningful URI or file path. e.g., location
                    // represented an Opaque URI.
                    // We can't do any meaningful processing for this bundle.
                    continue;
                }
                if (path.regionMatches(0, watchedDirPath, 0, watchedDirPath.length()))
                {
                    currentManagedBundles.put(jar.getURI(), jar);
                }
            }
            catch (URISyntaxException e)
            {
                // Ignore and continue.
                // This can never happen for bundles that have been installed
                // by FileInstall, as we always use proper filepath as location.
            }
        }
    }

    /**
     * This method goes collects list of bundles that have been installed
     * from the watched directory in previous run of the program,
     * compares them with the current set of jar files,
     * uninstalls old bundles, updates modified bundles, installs new bundles
     * and refreshes the framework for the changes to take effect.
     */
    private void traverse() {
        initCurrentManagedBundles();
        final Collection<Jar> current = currentManagedBundles.values();
        Set<Jar> discovered = discoverJars();

        // Find out all the new, deleted and common bundles.
        // new = discovered - current
        Set<Jar> newBundles = new HashSet<Jar>(discovered);
        newBundles.removeAll(current);

        // deleted = current - discovered
        Set<Jar> deletedBundles = new HashSet<Jar>(current);
        deletedBundles.removeAll(discovered);

        // existing = intersection of current & discovered
        Set<Jar> existingBundles = new HashSet<Jar>(discovered);
        // We remove discovered ones from current, so that we are left
        // with a collection of Jars made from files so that we can compare
        // them with bundles.
        existingBundles.retainAll(current);

        // We do the operations in the following order:
        // uninstall, update, install, refresh & start.
        int uninstalled = uninstall(deletedBundles);
        int updated = update(existingBundles);
        int installed = install(newBundles);
        if ((installed + uninstalled + updated) > 0) {
            refresh();
        }
    }

    private int uninstall(Collection<Jar> bundles) {
        int noOfBundlesUninstalled = 0;
        for (Jar jar : bundles) {
            Bundle bundle = context.getBundle(jar.getBundleId());
            if (bundle == null) {
                // this is highly unlikely, but can't be ruled out.
                continue;
            }
            if (isExcludedFile(new File(jar.getPath()))) {
                // This is a bundle which is excluded from our processing.
                // The reason we have not discovered this jar is because
                // we have exclueded them in discoverJars(). So, don't uninstall
                // them.
                continue;
            }
            try {
                bundle.uninstall();
                noOfBundlesUninstalled++;
                logger.logp(Level.INFO, "Main", "uninstall",
                        "Uninstalled bundle {0} installed from {1} ",
                        new Object[]{bundle.getBundleId(), jar.getPath()});
            } catch (Exception e) {
                logger.logp(Level.WARNING, "Main", "uninstall",
                        "Failed to uninstall bundle " + jar.getPath(),
                        e);
            }
        }
        return noOfBundlesUninstalled;
    }

    private int update(Collection<Jar> jars) {
        int updated = 0;
        for (Jar jar : jars) {
            final Jar existingJar= currentManagedBundles.get(jar.getURI());
            if (jar.isNewer(existingJar)) {
                Bundle bundle = context.getBundle(existingJar.getBundleId());
                if (bundle == null) {
                    // this is highly unlikely, but can't be ruled out.
                    continue;
                }
                try {
                    bundle.update();
                    updated++;
                    logger.logp(Level.INFO, "Main", "update",
                            "Updated bundle {0} from {1} ",
                            new Object[]{bundle.getBundleId(), jar.getPath()});
                } catch (Exception e) {
                    logger.logp(Level.WARNING, "Main", "update",
                            "Failed to update " + jar.getPath(),
                            e);
                }
            }
        }
        return updated;
    }

    private int install(Collection<Jar> jars) {
        int installed = 0;
        for (Jar jar : jars) {
            try {
                final String path = jar.getPath();
                File file = new File(path);
                final FileInputStream is = new FileInputStream(file);
                try {
                    Bundle b = context.installBundle(jar.getURI().toString(), is);
                    installed++;
                    currentManagedBundles.put(jar.getURI(), new Jar(b));
                    logger.logp(Level.FINE, "Main", "install",
                            "Installed bundle {0} from {1} ",
                            new Object[]{b.getBundleId(), jar.getURI()});
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            } catch (Exception e) {
                logger.logp(Level.WARNING, "Main", "install",
                        "Failed to install " + jar.getURI(),
                        e);
            }
        }
        return installed;
    }

    private void refresh() {
        final ServiceReference reference =
                context.getServiceReference(PackageAdmin.class.getName());
        PackageAdmin pa = PackageAdmin.class.cast(
                context.getService(reference));
        pa.refreshPackages(null); // null to refresh any bundle that's obsolete
        context.ungetService(reference);

        // This is a HACK - thanks to some weired optimization trick
        // done for GlassFish. HK2 maintains a cache of inhabitants and
        // that needs  to be recreated when there is a change in modules dir.
        final String cacheDir = getProperty(HK2_CACHE_DIR);
        if (cacheDir != null) {
            File inhabitantsCache = new File(cacheDir, INHABITANTS_CACHE);
            if (inhabitantsCache.exists()) inhabitantsCache.delete();
        }
    }

    private boolean isExcludedFile(File f) {
        String path = f.getPath();
        for (File excludedSubDir : excludedSubDirs) {
            String excludedSubDirPath = excludedSubDir.getPath();
            if (path.regionMatches(0, excludedSubDirPath, 0, excludedSubDirPath.length())) {
                return true;
            }
        }
        return false;
    }
}
