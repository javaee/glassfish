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

package org.glassfish.web.loader.util;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.web.WebApplication;
import com.sun.enterprise.web.WebContainer;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.api.container.Container;
import org.jvnet.hk2.component.Habitat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ASClassLoaderUtil {

    private static final Logger _logger = Logger.getAnonymousLogger();

    private static String modulesClassPath = null;

    /**
     * Gets the classpath associated with a web module, suffixing libraries defined
     * [if any] for the application
     *
     * @param habitat the habitat the application resides in.
     * @param moduleId Module id of the web module
     * @return A <code>File.pathSeparator</code> separated list of classpaths
     *         for the passed in web module, including the module specified "libraries"
     *         defined for the web module.
     */
    public static String getWebModuleClassPath(Habitat habitat,
                                               String moduleId) {

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ASClassLoaderUtil.getWebModuleClassPath " +
                    "for module Id : " + moduleId);
        }

        StringBuilder classpath = new StringBuilder(getModulesClasspath(habitat));
        ClassLoaderHierarchy clh =
                habitat.getByContract(ClassLoaderHierarchy.class);
        final String commonClassPath = clh.getCommonClassPath();
        if (commonClassPath != null && commonClassPath.length() > 0) {
            classpath.append(commonClassPath).append(File.pathSeparator);
        }
        addLibrariesForWebModule(classpath, moduleId);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Final classpath: " + classpath.toString());
        }
        return classpath.toString();

    }

    private static void addLibrariesForWebModule(StringBuilder sb,
                                                 String moduleId) {
        if (moduleId != null) {
            final String specifiedLibraries = getLibrariesForModule(WebContainer.class, moduleId);
            final URL[] libs = getLibraries(specifiedLibraries);
            if (libs != null) {
                for (final URL u : libs) {
                    sb.append(u.getPath());
                    sb.append(File.pathSeparator);                    
                }
            }
        }
    }

    /**
     * Gets the deploy-time "libraries" attribute specified for module
     *
     * @param type     the module type
     * @param moduleId The module id of the web module
     * @return A comma separated list representing the libraries
     *         specified by the deployer.
     */
    public static <T extends Container> String getLibrariesForModule(Class<T> type, String moduleId) {

        ApplicationInfo app = Globals.get(ApplicationRegistry.class).get(moduleId);
        if (app==null) {
            // this might be an internal web container app, like _default_web_app, ignore.
            return null;
        }
        ModuleInfo module = app.getModuleInfo(type);
        if (module!=null) {
            WebApplication webApp = (WebApplication) module.getApplicationContainer();
            return webApp.getLibraries();
        }
        _logger.log(Level.SEVERE, "No web module loaded for this application " + moduleId);
        return null;
    }

    /**
     * Utility method to obtain a resolved list of URLs representing the
     * libraries specified for an application using the libraries
     * application deploy-time attribute
     *
     * @param librariesStr The deploy-time libraries attribute as specified by
     *                     the deployer for an application
     * @return A list of URLs representing the libraries specified for
     *         the application
     */
    private static URL[] getLibraries(String librariesStr) {
        if (librariesStr == null)
            return null;

        String[] librariesStrArray = librariesStr.split(",");
        if (librariesStrArray == null)
            return null;

        final URL[] urls = new URL[librariesStrArray.length];
        //Using the string from lib and applibs requires admin which is 
        //built after appserv-core.
        final String appLibsDir = System.getProperty(
                SystemPropertyConstants.INSTANCE_ROOT_PROPERTY)
                + File.separator + "lib"
                + File.separator + "applibs";

        int i = 0;
        for (final String libraryStr : librariesStrArray) {
            try {
                File f = new File(libraryStr);
                if (!f.isAbsolute())
                    f = new File(appLibsDir, libraryStr);
                URL url = f.toURI().toURL();
                urls[i++] = url;
            } catch (MalformedURLException malEx) {
                _logger.log(Level.WARNING,
                        "loader.cannot_convert_classpath_into_url",
                        libraryStr);
                _logger.log(Level.WARNING, "loader.exception", malEx);
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

                //set sharedClasspathForWebModule so that it doesn't need to be recomputed
                //for every other invocation
                modulesClassPath = tmpString.toString();
            }
        }
        return modulesClassPath;
    }

}
