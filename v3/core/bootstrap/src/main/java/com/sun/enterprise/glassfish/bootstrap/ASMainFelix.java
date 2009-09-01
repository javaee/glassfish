/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@MetaInfServices(PlatformMain.class)
public class ASMainFelix extends ASMainOSGi {
    private static final String FELIX_HOME = "FELIX_HOME";
    protected final static String CACHE_DIR = "org.osgi.framework.storage";

    /**
     * Home of Felix installation relative to Glassfish root installation.
     */
    public static final String GF_FELIX_HOME="osgi/felix";

    /**
     * Location of the config properties file relative to the felix installation 
     */
    public static final String CONFIG_PROPERTIES = "conf/config.properties";

    /**
     * Location of the config properties file relative to the felix installation
     */
    public static final String SYSTEM_PROPERTIES = "conf/system.properties";

    protected String getPreferedCacheDir() {
        return "osgi-cache/felix/";
    }

    public String getName() {
        return ASMain.Platform.Felix.toString();
    }

    protected void setFwDir() {
        String fwPath = System.getenv(FELIX_HOME);
        if (fwPath == null) {
            // try system property, which comes from asenv.conf
            fwPath = System.getProperty(FELIX_HOME,
                    new File(glassfishDir, GF_FELIX_HOME).getAbsolutePath());
        }
        fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate Felix at " + fwPath);
        }
    }

    @Override
    protected void setUpCache(File sourceDir, File cacheDir) throws IOException {
        // Starting with Felix 1.4.0, the cache dir is identified by
        // property called org.osgi.framework.storage.
        System.setProperty(CACHE_DIR, cacheDir.getCanonicalPath());
    }

    protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        cpb.addJar(new File(fwDir, "bin/felix.jar"));
    }

    protected void launchOSGiFW() throws Exception {
        String sysFileURL = new File(fwDir, SYSTEM_PROPERTIES).toURI().toURL().toString();
        System.setProperty("felix.system.properties", sysFileURL);
        String confFileURL = new File(fwDir, CONFIG_PROPERTIES).toURI().toURL().toString();
        System.setProperty("felix.config.properties", confFileURL);
        Class mc = launcherCL.loadClass(getFWMainClassName());
        final String[] args = new String[0];
        final Method m = mc.getMethod("main", new Class[]{args.getClass()});
        // Call Felix on a separate Thread as the main class in Felix
        // does not return until the framework is stopped.
        Thread launcherThread = new Thread(new Runnable(){
            public void run() {
                try {
                    m.invoke(null, new Object[]{args});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e); // TODO: Proper Exception Handling
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e); // TODO: Proper Exception Handling
                }
            }
        },"OSGi Framework Launcher");

        // The EventDispatcher thread in Felix inherits the daemon status of the thread
        // that starts Felix. As the code below does, we start felix in a non-daemon
        // thread, which means for VM to exit, we need to shutdown the framework
        // in addition to stopping all the server specific non-daemon threads.
        launcherThread.setDaemon(false);
        launcherThread.start();
    }

    private String getFWMainClassName() {
        return "org.apache.felix.main.Main";
    }

}
