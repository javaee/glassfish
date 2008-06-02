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

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;
//import com.sun.enterprise.config.ConfigContext;
//import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.deployment.util.FileUtil;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
//import com.sun.enterprise.server.ApplicationServer;
//import com.sun.enterprise.server.PELaunch;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.web.WebDeployer;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

public class ASClassLoaderUtil {

    private static final Logger _logger = Logger.getAnonymousLogger();

    private static String sharedClasspathForWebModule = null;
    
    //The new ClassLoader Hierarchy would be enabled only when this system 
    //property is set. 
    private static final String USE_NEW_CLASSLOADER_PROPERTY 
                                    = "com.sun.aas.useNewClassLoader";
    
    /**
     * Gets the classpath associated with a web module, suffixing libraries defined 
     * [if any] for the application
     * @param habitat
     * @param moduleId Module id of the web module
     * @param delegate
     * @return A <code>File.pathSeparator</code> separated list of classpaths
     * for the passed in web module, including the module specified "libraries"
     * defined for the web module.
     */
    public static String getWebModuleClassPath(Habitat habitat,
            String moduleId, boolean delegate) {
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ASClassLoaderUtil.getWebModuleClassPath " +
            		"for module Id : " + moduleId);
        }

        synchronized(ASClassLoaderUtil.class) {
            if (sharedClasspathForWebModule == null) {
            	final StringBuilder tmpString = new StringBuilder();
                
                if (Boolean.getBoolean(USE_NEW_CLASSLOADER_PROPERTY)) {
                    final List<String> tmpList = new ArrayList<String>();
    	            tmpList.addAll(getSharedClasspath());
                    //include addon jars as well now that they are not part of shared classpath. 
    	            tmpList.addAll(getAddOnsClasspath());
                
    	            for(final String s:tmpList){
    	                tmpString.append(s);
    	                tmpString.append(File.pathSeparatorChar);
                    }
                } else {
    	            tmpString.append(FileUtil.getAbsolutePath(System.getProperty("java.class.path")));
                    tmpString.append(File.pathSeparatorChar);

    	        }

                WebDeployer webDeployer = habitat.getComponent(WebDeployer.class);
                ModuleDefinition[] moduleDefs = webDeployer.getMetaData().getPublicAPIs();
                if (moduleDefs != null) {
                    for (ModuleDefinition moduleDef : moduleDefs) {
                        URI[] uris = moduleDef.getLocations();
                        for (URI uri : uris) {
                            tmpString.append(uri.getPath());
                            tmpString.append(File.pathSeparator);
                        }
                    }
                }     
    	        //set sharedClasspathForWebModule so that it doesn't need to be recomputed
    	        //for every other invocation
    	        sharedClasspathForWebModule = tmpString.toString();
            }
        }

        StringBuilder classpath = new StringBuilder(sharedClasspathForWebModule);
            
        if (delegate) {
            addLibrariesFromLibs(classpath, habitat);
            addLibrariesForWebModule(classpath, habitat, moduleId);
        } else {
            addLibrariesForWebModule(classpath, habitat, moduleId);
            addLibrariesFromLibs(classpath, habitat);
        }
              
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Final classpath: " + classpath.toString());    
        }
        
        return classpath.toString();
        
    }

    private static void addLibrariesForWebModule(StringBuilder sb,
            Habitat habitat, String moduleId) {
       if (moduleId != null) {
            final String specifiedLibraries = getLibrariesForWebModule(moduleId);
            final URL[] libs = getLibraries(specifiedLibraries);
            if (libs != null)  {
                for (final URL u : libs) {
                    sb.append(u);
                    sb.append(File.pathSeparator);
                }
            }
        }
    }

    /**
     * Add Libraries from lib and domain_root lib.
     */
    private static void addLibrariesFromLibs(StringBuilder sb, Habitat habitat) {
        ModulesRegistry mreg = habitat.getComponent(ModulesRegistry.class);
        if (mreg != null) {
            ClassLoader cl = mreg.getParentClassLoader();
            if (cl instanceof URLClassLoader) {
                URLClassLoader urlCl = (URLClassLoader)cl;
                URL[] urls = urlCl.getURLs();
                if (urls != null) {
                    for (URL u : urls) {
                        sb.append(u);
                        sb.append(File.pathSeparator);
                    }
                }
            }
        }
    }
 
    /**
     * Gets the deploy-time "libraries" attribute specified for module
     * @param type the module type
     * @param moduleId The module id of the web module
     * @return A comma separated list representing the libraries
     * specified by the deployer.
     */    
    public static <T> String getLibrariesForModule(Class<T> type, String moduleId) {
        T app =  Globals.get(Applications.class).getModule(type, moduleId);
        if (app==null) return null;
        
        String librariesStr=null;
        try {
            Method m = type.getMethod("getLibraries");
            if (m!=null) {
                librariesStr = (String) m.invoke(app);
            }
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "app = " +  app + " library = " + librariesStr);
            }
            
        } catch(Exception e) {
            _logger.log(Level.SEVERE, "Cannot get libraries for module " + moduleId, e);
        }
        return librariesStr;
        
    }
    
    /**
     * Gets the deploy-time "libraries" attribute specified for a web module (.war file)
     * @param moduleId The module id of the web module
     * @return A comma separated list representing the libraries
     * specified by the deployer.
     */
    public static String getLibrariesForWebModule(String moduleId) {
            
        String librariesStr = ConfigBeansUtilities.getLibraries(moduleId);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "moduleId = " +  moduleId + " library = " + librariesStr);
        }
        
        return librariesStr;
        
    }
    
    /**
     * Utility method to obtain a resolved list of URLs representing the 
     * libraries specified for an application using the libraries 
     * application deploy-time attribute 
     * @param librariesStr The deploy-time libraries attribute as specified by 
     * the deployer for an application
     * @return A list of URLs representing the libraries specified for 
     * the application
     */
    public static URL[] getLibraries(String librariesStr) {
        if(librariesStr == null)
            return null;
        
        String [] librariesStrArray = librariesStr.split(",");
        if(librariesStrArray == null)
            return null;
        
        final URL [] urls = new URL[librariesStrArray.length];
        //Using the string from lib and applibs requires admin which is 
        //built after appserv-core.
        final String appLibsDir = System.getProperty(
                        SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) 
                        + File.separator + "lib" 
                        + File.separator  + "applibs";
        
        int i=0;
        for(final String libraryStr:librariesStrArray){
            try {
                File f = new File(libraryStr);
                if(!f.isAbsolute())
                    f = new File(appLibsDir, libraryStr);
                URL url = f.toURL();
                urls[i++] = url;
            } catch (MalformedURLException malEx) {
                _logger.log(Level.WARNING,
                        "loader.cannot_convert_classpath_into_url",
                        libraryStr);
                _logger.log(Level.WARNING,"loader.exception", malEx);
            }
        }
        return urls;
    }
    
    /**
     * Returns the shared class loader
     * @return ClassLoader
     */
    public static synchronized ClassLoader getSharedClassLoader() {
        //XXX return ApplicationServer.getServerContext().getSharedClassLoader();
        return null;
    }
    
    public static ClassLoader getSharedChain(){
        // not yet implemented
        return null;
    }    
    
    public static synchronized List<String> getSharedClasspath() {
        // not yet implemented
        return null;
    }
    
    public static synchronized List<String> getAddOnsClasspath() {
        // not yet implemented
    	return null;
    }
    
}
