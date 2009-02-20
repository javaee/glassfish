/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limisubtations under the License.
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

package org.glassfish.loader.util;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.api.admin.ServerEnvironment;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.Collection;

public class ASClassLoaderUtil {

    private static final Logger _logger = Logger.getAnonymousLogger();

    private static String modulesClassPath = null;

    /**
     * Gets the classpath associated with a module, suffixing libraries
     * defined [if any] for the application
     *
     * @param habitat the habitat the application resides in.
     * @param moduleId Module id of the module
     * @param deploymentLibs libraries option passed through deployment
     * @return A <code>File.pathSeparator</code> separated list of classpaths
     *         for the passed in module, including the module specified
     *         "libraries" defined for the module.
     */
    public static String getModuleClassPath
        (Habitat habitat, String moduleId, String deploymentLibs) {

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ASClassLoaderUtil.getModuleClassPath " +
                    "for module Id : " + moduleId);
        }

        StringBuilder classpath = new StringBuilder(getModulesClasspath(habitat));
        ClassLoaderHierarchy clh =
                habitat.getByContract(ClassLoaderHierarchy.class);
        final String commonClassPath = clh.getCommonClassPath();
        if (commonClassPath != null && commonClassPath.length() > 0) {
            classpath.append(commonClassPath).append(File.pathSeparator);
        }
        addLibrariesForModule(classpath, moduleId, deploymentLibs, habitat);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Final classpath: " + classpath.toString());
        }
        return classpath.toString();

    }

    public static String getModuleClassPath (Habitat habitat, 
        DeploymentContext context) {
        DeployCommandParameters params = 
            context.getCommandParameters(DeployCommandParameters.class);
        return getModuleClassPath(habitat, params.name(), params.libraries());
    }


    private static void addLibrariesForModule(StringBuilder sb, 
        String moduleId, String deploymentLibs, Habitat habitat) {
        if (deploymentLibs == null) {
            ApplicationInfo appInfo = 
                habitat.getComponent(ApplicationRegistry.class).get(moduleId);
            if (appInfo == null) {
                // this might be an internal container app, 
                // like _default_web_app, ignore.
                return;
            }
            deploymentLibs = appInfo.getLibraries();
        }
        final URL[] libs = getLibrariesAsURLs(deploymentLibs, habitat);
        if (libs != null) {
            for (final URL u : libs) {
                sb.append(u.getPath());
                sb.append(File.pathSeparator);                    
            }
        }
    }

    private static URL[] getLibrariesAsURLs(String librariesStr, 
        Habitat habitat) {
            return getLibrariesAsURLs(librariesStr, 
                habitat.getComponent(ServerEnvironment.class));
    }

    /**
     * converts libraries specified via the --libraries deployment option to
     * URL[].  The library JAR files are specified by either relative or
     * absolute paths.  The relative path is relative to 
     * instance-root/lib/applibs. The libraries  are made available to 
     * the application in the order specified.
     *
     * @param librariesStr is a comma-separated list of library JAR files
     * @param env the server environment
     * @return array of URL
     */
    public static URL[] getLibrariesAsURLs(String librariesStr,
        ServerEnvironment env) {
        if(librariesStr == null)
            return null;
        String [] librariesStrArray = librariesStr.split(",");
        if(librariesStrArray == null)
            return null;
        final URL [] urls = new URL[librariesStrArray.length];
        final String appLibsDir = env.getLibPath()
                + File.separator + "applibs";

        int i=0;
        for(final String libraryStr:librariesStrArray){
            try {
                File f = new File(libraryStr);
                if(!f.isAbsolute())
                    f = new File(appLibsDir, libraryStr);
                URL url =f.toURI().toURL();
                urls[i++] = url;
            } catch (MalformedURLException malEx) {
                _logger.log(Level.WARNING, "Cannot convert classpath to URL",
                        libraryStr);
                _logger.log(Level.WARNING, malEx.getMessage(), malEx);
            }
        }
        return urls;
    }

    private static synchronized String getModulesClasspath(Habitat habitat) {
        synchronized (ASClassLoaderUtil.class) {
            if (modulesClassPath == null) {
                final StringBuilder tmpString = new StringBuilder();
                ModulesRegistry mr = habitat.getComponent(ModulesRegistry.class);
                if (mr != null) {
                    for (Module module : mr.getModules()) {
                        for (URI uri : module.getModuleDefinition().getLocations()) {
                            tmpString.append(uri.getPath());
                            tmpString.append(File.pathSeparator);
                        }
                    }
                }

                //set shared classpath for module so that it doesn't need to be 
                //recomputed for every other invocation
                modulesClassPath = tmpString.toString();
            }
        }
        return modulesClassPath;
    }

}
