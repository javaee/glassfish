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
public class ASMainKnopflerFish extends ASMainOSGi {
    protected String getPreferedCacheDir() {
        return "osgi-cache/knopflerfish/";
    }

    public String getName() {
        return ASMain.Platform.Knopflerfish.toString();
    }

    protected void setFwDir() {
        String fwPath = System.getenv("KNOPFLERFISH_HOME");
        if (fwPath == null) {
            fwPath = new File(glassfishDir, "osgi/knopflerfish.org/osgi/").getAbsolutePath();
        }
        fwDir = new File(fwPath);
        if (!fwDir.exists()) {
            throw new RuntimeException("Can't locate KnopflerFish at " + fwPath);
        }
    }

    protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
        cpb.addJar(new File(fwDir, "framework.jar"));
    }

    protected void launchOSGiFW() throws Exception {
        // Refere to http://www.knopflerfish.org/running.html for more details about
        // options and properties used here
        File cacheProfileDir = new File(domainDir, "kf-cache/gf/");
        setUpCache(bootstrapFile.getParentFile(), cacheProfileDir);
        String jars = new File(fwDir, "jars/").toURI().toString();
        System.setProperty("org.knopflerfish.gosg.jars", jars);
        String pkgFilePath = new File(fwDir, "gfpackages.txt").getAbsolutePath();
        System.setProperty("org.osgi.framework.system.packages.file", pkgFilePath);   
        Class mc = launcherCL.loadClass(getFWMainClassName());
        String xargsURL = new File(fwDir, "gf.xargs").toURI().toURL().toString();
        final String[] args = {"-xargs", xargsURL};
        final Method m = mc.getMethod("main", new Class[]{args.getClass()});
        Thread launcherThread = new Thread(new Runnable(){
            public void run() {
                try {
                    m.invoke(null, new Object[]{args});
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        },"OSGi Framework Launcher");
        launcherThread.setDaemon(false);
        launcherThread.start();
    }

    private String getFWMainClassName() {
        return "org.knopflerfish.framework.Main";
    }

    protected void setUpCache(File sourceDir, File cacheDir) throws IOException
    {
        System.setProperty("org.osgi.framework.dir", cacheDir.getCanonicalPath());
    }
}
