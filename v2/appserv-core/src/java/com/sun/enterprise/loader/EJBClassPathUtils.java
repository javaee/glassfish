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

package com.sun.enterprise.loader;

import com.sun.appserv.server.util.ASClassLoaderUtil;
import com.sun.appserv.server.util.ASURLClassLoader;
import com.sun.appserv.server.util.ClassLoaderChain;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.enterprise.deployment.backend.DeploymentUtils;
import com.sun.enterprise.instance.AppsManager;
import com.sun.enterprise.instance.BaseManager;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.PELaunch;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.StringTokenizer;

import javax.enterprise.deploy.shared.ModuleType;

import org.xml.sax.SAXParseException;

/**
 * This utility is used by EJB container during server start up to create the 
 * class loaders. It is called from the application and ejb module loaders. 
 * Deployment also calls this during ejbc.
 *
 * @author  Jerome Dochez
 */
public class EJBClassPathUtils {

    static Logger _logger = LogDomains.getLogger(LogDomains.LOADER_LOGGER);
    /**
     * Maps a application library URL to a ClassLoader instance.
     * This map is used to lookup existing classloader instances when an 
     * application wants to share a library with another application.
     */
    private static Map<URI,ClassLoader> classLoaderRegistry = 
                                             new HashMap<URI,ClassLoader>();

   /**
     * Returns the Application class paths for the given application name.
     *
     * @param    appName    application name used to register with the config
     * @param    apps       application config obj
     *
     * @return   the ejb class paths for the given application name
     *           or empty list
     */
    public static List getAppClasspath(Application application, BaseManager apps) {

        String appName = application.getRegistrationName();

        try {

          String appRoot = apps.getLocation(appName);
          return getAppClassPath(application, appRoot, apps);
          
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"ejb.classpath",e);
            return new ArrayList();
        }
    }
    
    public static List getAppClassPath(Application application, String appRoot, BaseManager apps) {
        
        List classpath        = new ArrayList();
        String appName = application.getRegistrationName();

        try {

          List appPath = getApplicationClassPath(application, appRoot);
          
          if (appPath.size() > 0) {
              classpath.addAll(appPath);
          }

          // adds stubs dir for this application
          classpath.add(apps.getStubLocation(appName));

        } catch (Exception e) {
            _logger.log(Level.SEVERE,"ejb.classpath",e);
        }

        // log the class path
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "[EJBClassPathUtils] EJB Class Path for [" 
                        + appName + "] is ...\n" + classpath.toString());
        }

        return classpath;
    }
    
    public static List getModuleClasspath(String moduleName, String moduleRoot, BaseManager mgr) {
                
        List classpath = new ArrayList();
        
        try {
            
            // adds the location where this module was installed
            if (moduleRoot==null) {
                moduleRoot = mgr.getLocation(moduleName);
            }
            classpath.add(moduleRoot);
            
            // adds stubs dir for this stand alone ejb module
            classpath.add( mgr.getStubLocation(moduleName) );
            
            classpath.addAll(getModuleClassPath(mgr.getModuleType(),  moduleRoot, moduleRoot));     
        
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"ejb.classpath",e);
        }        
        
        return classpath;
    }     
    
    public static List getModuleClassPath(ModuleType type, String moduleRoot, String appRoot) 
        throws IOException
    {
        
        List classpath = new ArrayList();
        
        // additional class path from the manifest
        Manifest mf = getManifest(moduleRoot);
        
        List manifestClassPath = getManifestClassPath(mf, appRoot);
        classpath.addAll(manifestClassPath);
        
        
        if (ModuleType.WAR.equals(type)) {
            
            // classes dir under WEB-INF
            String classesDir = moduleRoot + File.separator + WAR_CLASSES_DIR;
            
            // lib dir under WEB-INF
            String libDir     = moduleRoot + File.separator + WAR_LIB_DIR;
            
            // adds the class path from the WAR module
            //    i. <war-module>/WEB-INF/classes
            //   ii. <war-module>/WEB-INF/lib/*.jar
            List warClassPath = ClassLoaderUtils.getUrlList
                    (new File[] {new File(classesDir)},
                    new File[] {new File(libDir)} );
                    
             // add to the application class path
             classpath.addAll(warClassPath);
                    
        } else {
            classpath.add(moduleRoot);
            
            // adds the class path from the module
            //   <module>/*.jar
            List moduleClassPath = ClassLoaderUtils.getUrlList
                    (null,
                    new File[] {new File(moduleRoot)} );
                    classpath.addAll(moduleClassPath);
        }
        
        return classpath;            
    }

    /**
     * Returns the class path for the given application with all sub-modules
     * This is called from deployment backend.
     *
     * <p> WARNING: This list does not contain the stubs directory.
     *
     * @param    appRoot    application location 
     * @param    appName    application name used to register with the config
     *
     * @return   class path for the given application
     * 
     * @exception  AppConfigException  if an error while retrieving the 
     *                                 application's deployment descriptor
     */    
    public static List getApplicationClassPath(Application app, String appRoot)
        throws IOException {
        
        List classpath     = new ArrayList();        

        if (!app.isVirtual()) { //ear file

            // first add the libraries in the library directory if it exists
            if (app.getLibraryDirectory() != null) {
                String libPath = 
                    app.getLibraryDirectory().replace('/', File.separatorChar);
                List dirLibraries = ClassLoaderUtils.getUrlList(
                    null, new File[] {new File(appRoot, libPath)}, true);
                if (dirLibraries != null && !dirLibraries.isEmpty()) {
                   classpath.addAll(dirLibraries);
               }
            }

            // then add the top level libraries at the app root
            List rootLibraries = ClassLoaderUtils.getUrlList(
                                    null, new File[] {new File(appRoot)});
                                    
            if (rootLibraries != null && !rootLibraries.isEmpty()) {
                classpath.addAll(rootLibraries);
            }
        } 

        for (Iterator modules = app.getModules(); modules.hasNext();) {
            
            ModuleDescriptor md = (ModuleDescriptor) modules.next();

            String moduleUri = md.getArchiveUri();
            String moduleRoot;
            if (app.isVirtual()) {
                moduleRoot = appRoot;
            } else {
                moduleRoot = DeploymentUtils.getEmbeddedModulePath(
                    appRoot, moduleUri);
            }

            classpath.addAll(getModuleClassPath(md.getModuleType(),  moduleRoot, appRoot));             
        }
        return classpath;        
    }

    // ---- METHOD(S) - PRIVATE ----------------------------------------------

    /**
     * Returns the manifest file for the given root path.
     *
     * <xmp>
     *    Example: 
     *     |--repository/
     *     |   |--applications/
     *     |        |--converter/
     *     |            |--ejb-jar-ic_jar/        <---- rootPath
     *     |                 |--META-INF/
     *     |                     |--MANIFEST.MF
     * </xmp>
     *
     * @param    rootPath    absolute path to the module
     *
     * @return   the manifest file for the given module
     */
    public static Manifest getManifest(String rootPath) {

        InputStream in  = null;
        Manifest mf     = null;

        // gets the input stream to the MANIFEST.MF file
        try {
            in = new FileInputStream(rootPath+File.separator+MANIFEST_ENTRY);

            if (in != null) {
                mf = new Manifest(in);
            }
        } catch (IOException ioe) { 
            // ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }
        return mf;
    }
    
    /**
     * Returns the class path (if any) from the given manifest file.
     *
     * @param    manifest    manifest file of an archive
     * @param    rootPath    root path to the module
     *
     * @return   a list of class paths of type java.lang.String or 
     *           an empty list if given manifest is null
     */
    private static List getManifestClassPath(Manifest manifest, 
            String rootPath) {

        List classPaths            = new ArrayList();

        if (manifest != null) {
            Attributes mainAttributes  = manifest.getMainAttributes();

            for (Iterator itr=mainAttributes.keySet().iterator();
                    itr.hasNext();) {

                Attributes.Name next = (Attributes.Name) itr.next();

                if (next.equals(Attributes.Name.CLASS_PATH)) {

                    String classpathString = (String) mainAttributes.get(next);
                    StringTokenizer st = 
                        new StringTokenizer(classpathString, " ");

                    while(st.hasMoreTokens()) {
                        String mc = st.nextToken();
                        classPaths.add(rootPath+File.separator+mc);
                    }
                }
            }
        }

        return classPaths;
    }
 
    public static EJBClassLoader createEJBClassLoader(String[] classPaths, 
                         String moduleRoot, String id, ClassLoader parentClassLoader, 
                         ModuleType moduleType) {        
        URL[] classPathURLs = new URL[0];
        if (classPaths != null) {
            
            int classPathSize    = classPaths.length;
            classPathURLs   = new URL[classPathSize];

            for (int i=0; i<classPathSize; i++) {
                try {
                    classPathURLs[i] = (new File(classPaths[i])).toURI().toURL(); 
                } catch (MalformedURLException malEx) {
                    _logger.log(Level.WARNING,
                                "loader.cannot_convert_classpath_into_url",
                                classPaths[i]);
                    _logger.log(Level.WARNING,"loader.exception", malEx);
                }
            }
        }
        
        String libs = null;
        //WARs do not go via AbstractLoader and hence use ASClassLoaderUtil.getWebModuleClassPath()
        if (moduleType.equals(ModuleType.EAR)) {
            libs = ASClassLoaderUtil.getLibrariesForJ2EEApplication(id);   
        } else if (moduleType.equals(ModuleType.EJB)) {
            libs = ASClassLoaderUtil.getLibrariesForEJBJars(id);
        }
            
        URL[] deployTimeLibraries = ASClassLoaderUtil.getLibraries(libs);
        URL[] resolvedLibrariesList = null;
        
        if (deployTimeLibraries != null) {
            if (deployTimeLibraries.length > 0) {
                     resolvedLibrariesList = resolveVersionConflicts(
                                                 EJBClassPathUtils.getManifest(moduleRoot),
                                                 deployTimeLibraries,classPaths);
            }
        }
        
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "createEJBClassLoader :: Resolved libraries " + resolvedLibrariesList);
        }
        ClassLoader applicationLibrariesCL  = createApplicationLibrariesClassLoader(
                                                         parentClassLoader,  resolvedLibrariesList, id);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "- applibsCL: " + applicationLibrariesCL);
        }
        return createEJBClassLoader(parentClassLoader, applicationLibrariesCL, classPathURLs);
    }
    
    
    /**
     * Checks for conflicts between application's bundled libraries and 
     * libraries specified via the deploy-time libraries attribute and returns a resolved
     * list that does not contain any such conflicts. 
     * 
     * As per Section J2EE.8.2.3  Library conflicts section of the Java EE 5.0 specification
     * if an application includes a bundled version of a library and the same libraries 
     * exists as an installed library , the instance of the library bundled with the  application
     * should be used in preference to any installed library. 
     * 
     * To satisfy this we remove all conflicting library from the application libraries chain.
     * 
     * @param deployTimeLibrariesList a list of libraries specified via the 
     * deploy-time libraries attribute of the application
     * @param mf The manifest files associated with the module.
     * @param applicationClasspath list of libraries bundled with the application
     * @return a resolved list of URLs that needs to be loaded by the application
     * libraries classloader.
     */
    private static URL[] resolveVersionConflicts(Manifest mf, URL[] deployTimeLibraries, 
                                                        String[] applicationClasspath) {
        try {
            String appList = mf.getMainAttributes().getValue(
                                                Attributes.Name.EXTENSION_LIST);
            if (appList == null) return deployTimeLibraries;
            String[] appExtensions = appList.split(" " );
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "Application Extension List" + appExtensions);
            }
            
            List<URL> conflictingLibraries = new ArrayList<URL>(); 
            for (int i = 0; i < appExtensions.length; i++) {
                String extensionName = mf.getMainAttributes().
                                           getValue(appExtensions[i] + "-Extension-Name");
                String extensionSpecVersion = mf.getMainAttributes().
                                           getValue(appExtensions[i] + "-Extension-Specification-Version");
                String extensionImplVersion = mf.getMainAttributes().
                                           getValue(appExtensions[i] + "-Extension-Implementation-Version");
                if(bundledExtensionMatches(applicationClasspath, extensionName, 
                                                                      extensionSpecVersion, 
                                                                      extensionImplVersion )){
                    URL url = isExtensionInLibraries(extensionName, deployTimeLibraries);
                    if(url != null){
                        conflictingLibraries.add(url);
                    }
                }
            }
            
            //Filter out  conflicting libraries from original deployTimeLibrariesList
            List<URL> resolvedList = new ArrayList<URL>();
            for (int i = 0; i < deployTimeLibraries.length; i++) {
                if (!conflictingLibraries.contains(deployTimeLibraries[i])) {
                    resolvedList.add(deployTimeLibraries[i]);
                } else {
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, " conflict  " + deployTimeLibraries[i] +
                                        "being ignored");
                    }
                }
            }
            
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, " Final resolved list after conflict"
                                                  + "checking " + resolvedList);
            }
            return resolvedList.toArray(new URL[]{});
        } catch (IOException ioe) {
            _logger.log(Level.WARNING, ioe.getMessage());
            _logger.log(Level.FINE, "Exception while checking for version " +
                              "conflict in bundled vs provided libraries", ioe);
        }
        return deployTimeLibraries;
    }
    
    /*
     * Checks if the extension referred by the application is also provided in the --libraries 
     * list
     */
    private static URL isExtensionInLibraries(String extensionName, 
                            URL[] deployTimeLibrariesList) throws IOException {
        for (int i = 0; i < deployTimeLibrariesList.length; i++) {
            JarFile jf = new JarFile(deployTimeLibrariesList[i].getFile());
            String extnName = jf.getManifest().getMainAttributes().getValue(
                                               Attributes.Name.EXTENSION_NAME);
            if (extnName.equals(extensionName)) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "extensionName" + extensionName  +
                   "matched by " + deployTimeLibrariesList[i] + " = CONFLICT");
                }
                return deployTimeLibrariesList[i];
            }
        }
        return null;
    }
    
    /**
     *  Checks if a referred extension is bundled with the application already.
     */
    private static boolean bundledExtensionMatches(String[] applicationClasspath,
                      String extnName, String extnSpecVersion, String extnImplVersion) throws IOException {
        for (int i = 0; i < applicationClasspath.length; i++) {
            JarFile jf = new JarFile(applicationClasspath[i]);
            String bundledExtnName = jf.getManifest().getMainAttributes().
                                             getValue(Attributes.Name.EXTENSION_NAME);
            String bundledExtnImplVersion = jf.getManifest().getMainAttributes().
                                 getValue(Attributes.Name.IMPLEMENTATION_VERSION);
            String bundledExtnSpecVersion = jf.getManifest().getMainAttributes().
                                    getValue(Attributes.Name.SPECIFICATION_VERSION);
            if (   
                  (extnName.equals(bundledExtnName)) && 
                  ((extnSpecVersion != null) && (bundledExtnSpecVersion.compareTo(extnSpecVersion)  >=0)) 
                  && ((extnImplVersion != null) && (bundledExtnImplVersion.compareTo(extnImplVersion)  >=0))
               ) {
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "extensionName" + bundledExtnName  + 
                                    "spec version: " + bundledExtnSpecVersion + 
                                    "impl version: " + bundledExtnImplVersion + 
                                    "matches within the application");
                }
                return true;
            } 
        }
        return false;
    }
    
   
    /**
     *  Creates the application librararies classloader, derived from the 
     *  libraries attribute of the application. 
     * @return 
     */
    private static ClassLoader createApplicationLibrariesClassLoader(
                                           ClassLoader parentClassLoader, URL[] urlList, String moduleId) {
        if( urlList != null ) {
            ClassLoaderChain appChain = new ClassLoaderChain(parentClassLoader);
            appChain.setName("Application library chain for " + moduleId);
            for(URL url:urlList){
                try {
					ClassLoader urlLoader = classLoaderRegistry.get(url.toURI());
                //if this library has already been referred in a different application and been 
                //loaded, share this library by reusing the same classloader.
                if(urlLoader == null) {
                    urlLoader = new ASURLClassLoader(new URL[]{url}, parentClassLoader);
					    classLoaderRegistry.put(url.toURI(),urlLoader);
                }
                appChain.addToList(urlLoader);
				} catch (URISyntaxException e) {
					_logger.log(Level.FINE, "Error while resolving " + url + " to URI");
					_logger.log(Level.WARNING, e.getMessage());

				}
            }
            
            //Finally suffix the optional chain. The optional chain is suffixed to the appchain
            //to enable an administrator to override libraries in the optional chain via
            //the libraris deploy-time attribute.
            ClassLoader optionalChain = PELaunch.getOptionalChain();
            appChain.addToList(optionalChain);
            return appChain;
        }
        return null;
    }

    private static EJBClassLoader createEJBClassLoader( ClassLoader parentClassLoader,
                                                         ClassLoader appLibLoader,URL[] URLs ) {
        EJBClassLoader loader = null;
        if (appLibLoader != null) {
            loader = new EJBClassLoader(appLibLoader);
        } else { 
            loader = new EJBClassLoader(parentClassLoader);
        }
        
        if (URLs != null) {            
            for(int i=0; i<URLs.length; i++) {
                loader.appendURL(URLs[i]);
            }
        }                         
        return loader;
    }

    
    
    /** The manifest file name from an archive. */
    private static final String MANIFEST_ENTRY  = 
                    "META-INF" + File.separator + "MANIFEST.MF";

    private static final String WAR_CLASSES_DIR = 
                    "WEB-INF"+ File.separator + "classes";

    private static final String WAR_LIB_DIR     = 
                    "WEB-INF"+ File.separator + "lib";
}
