/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.jvnet.hk2.osgimain;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.net.URI;

/**
 * Goes through a directory structure recurssively and installs all the
 * jar files as OSGi bundles. This bundle can be passed a list of bundle paths
 * to be started automatically. The bundle path is treated relative to the
 * directory from where it installs all the bundles. It does not stop those bundle
 * when this bundle is stopped. It stops them in reverse order.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class Main implements BundleActivator
{
    private static final String DIR_NAME_PROP =
            Main.class.getPackage().getName()+".bundlesDir";
    private static final Logger logger =
            Logger.getLogger(Main.class.getPackage().getName());

    private BundleContext context;

    private File bundlesDir;

    private Bundle primordialBundle;

    private static final String AUTO_START_BUNDLES_PROP =
            Main.class.getPackage().getName()+".autostartBundles";

    private Map<String, Long> autoStartBundleIds = new HashMap<String, Long>();
    private List<String> autoStartBundleLocations = new ArrayList<String>();

    public void start(BundleContext context) throws Exception
    {
        this.context = context;
        bundlesDir = new File(context.getProperty(DIR_NAME_PROP));
        StringTokenizer st = new StringTokenizer(context.getProperty(AUTO_START_BUNDLES_PROP), ",");
        while (st.hasMoreTokens()) {
            String bundleRelPath = st.nextToken().trim();
            String bundleURI = new File(bundlesDir, bundleRelPath).toURI().normalize().toString();
            autoStartBundleLocations.add(bundleURI);
        }
        installBundles();
        for (String location : autoStartBundleLocations) {
            Long id = autoStartBundleIds.get(location);
            if (id == null) {
                logger.logp(Level.WARNING, "Main", "start", "Not able to locate autostart bundle for location = {0}", new Object[]{location});
                continue;
            }
            final Bundle bundle = context.getBundle(id);
            // check is necessary as bundle could have been uninstalled
            if (bundle != null) bundle.start();
        }
    }

    /**
     * Stops all the autostart bundles in reverse order
     * @param context
     * @throws Exception
     */
    public void stop(BundleContext context) throws Exception
    {
        List<String> bundlesToStop = new ArrayList<String>(autoStartBundleLocations);
        Collections.reverse(bundlesToStop);
        for (String location : bundlesToStop) {
            Long id = autoStartBundleIds.get(location);
            if (id == null) {
                logger.logp(Level.WARNING, "Main", "stop", "Not able to locate autostart bundle for location = {0}", new Object[]{location});
                continue;
            }
            final Bundle bundle = context.getBundle(id);
            // check is necessary as bundle could have been uninstalled
            if (bundle != null) bundle.stop();
        }
    }

    private void installBundles()
    {
        bundlesDir.listFiles(new FileFilter(){
            final String JAR_EXT = ".jar";
            public boolean accept(File pathname)
            {
                if (pathname.isDirectory()) {
                    pathname.listFiles(this);
                } else if (pathname.isFile()
                        && pathname.getName().endsWith(JAR_EXT)) {
                    installBundle(pathname);
                    return true;
                }
                return false;
            }
        });
    }

    private void installBundle(File jar) {
        try
        {
            // We use URI as the location, because Felix uses it
            // in autostart properties.
            final String location = jar.toURI().normalize().toString();
            Bundle b = context.installBundle(location, new FileInputStream(jar));
            if (autoStartBundleLocations.contains(location)) {
                autoStartBundleIds.put(location, b.getBundleId());
            }
        }
        catch (Exception e)
        {
            logger.logp(Level.WARNING, "Installer", "install",
                    "Failed to install {0} because of {1}",
                    new Object[]{jar, e});
        }
    }

}
