/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import org.kohsuke.MetaInfServices;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.enterprise.module.bootstrap.PlatformMain;

/**
 * Main class to launch GlassFish on Equinox (non-embedded)
 * use GlassFish on Equinox at your risk.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@MetaInfServices(PlatformMain.class)
public class ASMainEquinox extends ASMainOSGi {
    /* if equinox is installed under glassfish/eclipse this would be the
     *  glassfish/eclipse/plugins dir that contains the equinox jars
     *  can be null
     * */
    private static File pluginsDir=null;

    protected String getPreferedCacheDir() {
        return "osgi-cache/equinox/";
    }

    public String getName() {
        return Constants.Platform.Equinox.toString();
    }

    protected void setFwDir() {
        String fwPath = System.getenv("EQUINOX_HOME");
        if (fwPath == null) {
            fwPath = new File(glassfishDir, "osgi/equinox").getAbsolutePath();
        }
        fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            fwDir = new File(glassfishDir, "osgi/eclipse");
        }
        if (fwDir.exists()){//default Eclipse equinox structure from a equinoz zip distro
            pluginsDir = new File(fwDir,"plugins");
            if (!pluginsDir.exists()){
                pluginsDir =null;//no luck
            }
        }

        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate Equinox at " + fwPath);
        }
    }

    protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        // Add all the jars to classpath for the moment, since the jar name
        // is not a constant.
       if (pluginsDir!=null) {
            cpb.addGlob(pluginsDir, "org.eclipse.osgi_*.jar");

        } else {
            cpb.addJarFolder(fwDir);
        }
    }

    private File getSettingsFile() {
        File settings = new File(fwDir, "configuration");
        return new File(settings, "config.ini");
    }

    private void setUpCache() throws IOException {
        File cacheDir = getCacheDir();

        // Refer to http://help.eclipse.org/help32/index.jsp?topic=/org.eclipse.platform.doc.isv/reference/misc/index.html
        // for details about the configuration options used here.
        // Starting with version 3.5.1, Equinox understands standard OSGi property
        // org.osgi.framework.storage to configure cache dir, but it is not used by the
        // launcher that we use. SO, we still use the old property.
        System.setProperty("osgi.configuration.area", cacheDir.getCanonicalPath());

        // I need to copy the configuration from our eclipse directory into
        // the cache so equinox use that as its caching directory. Otherwise, it uses
        // configuration dir as the cache dir.
        File settings = getSettingsFile();
        if (!cacheDir.exists() && !cacheDir.mkdirs()) {
            throw new RuntimeException("Not able to create " + cacheDir.getAbsolutePath());
        }
        ASMainHelper.copyFile(settings, new File(cacheDir, "config.ini"));
    }

    protected void launchOSGiFW() throws Exception {
        setUpCache();
        Class mc = launcherCL.loadClass(getFWMainClassName());
        final String[] args = new String[0];
        final Method m = mc.getMethod("main", new Class[]{args.getClass()});
        Thread launcherThread = new Thread(new Runnable() {
            public void run() {
                try {
                    m.invoke(null, new Object[]{args});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "OSGi Framework Launcher");
        launcherThread.setDaemon(false);
        launcherThread.start();
    }

    private String getFWMainClassName() {
        return "org.eclipse.core.runtime.adaptor.EclipseStarter";
    }

}
