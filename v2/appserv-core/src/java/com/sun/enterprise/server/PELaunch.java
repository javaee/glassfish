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

package com.sun.enterprise.server;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.appserv.server.util.ASURLClassLoader;
import com.sun.appserv.server.util.ClassLoaderChain;
import com.sun.enterprise.util.ASenvPropertyReader;
import com.sun.enterprise.util.SystemPropertyConstants;


/**
 * New Start up class for PE/RI. For AS9 this is set as the default 
 * <main-class> in processLauncher.xml. 
 * 
 * To disable the new classloader hierarchy the following needs to be done:
 * - Modify the system property '-Dcom.sun.aas.processName="as9-server"' to
 *   '-Dcom.sun.aas.processName="s1as8-server' in the startserv/stopserv scripts
 * - Modify system-classpath attribute in the relevant java-config to 
 *    if PE: "com.sun.aas.classloader.serverClassPath" element in processLauncher.xml
 *    if EE: "com.sun.aas.classloader.serverClassPath.ee" element in processLauncher.xml
 * 
 * @author Harsha RA, Sivakumar Thyagarajan
 */

public class PELaunch {
    private static final long START_TIME_MILLIS = System.currentTimeMillis();
    public static long getStartTimeMillis() { return START_TIME_MILLIS; }
    
    //The new ClassLoader Hierarchy would be enabled only when this system 
    //property is set. 
    public static final String USE_NEW_CLASSLOADER_PROPERTY 
                                    = "com.sun.aas.useNewClassLoader";
    
    public static final String PROPERTIES_FILES 
                                    = "processLauncher.properties";

    //These properties are set by the ProcessLauncher in the new classloader
    //scheme. The classpath prefix and server classpath are prefixed to the
    //shared classloader chain. The classpath suffix is suffixed to the shared classloader
    //chain
    private  static final String CLASSPATH_PREFIX_PROPERTY 
                                    = "com.sun.aas.ClassPathPrefix";
    private  static final String CLASSPATH_SUFFIX_PROPERTY 
                                    = "com.sun.aas.ClassPathSuffix";
    private  static final String SERVER_CLASSPATH_PROPERTY 
                                    = "com.sun.aas.ServerClassPath";

    private static final String fileSeparator = File.separator;
    private static final String pathSeparator = File.pathSeparator;
    
    private static final String installRoot = 
    	System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY);

    private static final List<String> _appserverClasspath = new ArrayList<String>();
    private static final List<String> _sharedClasspath = new ArrayList<String>();
    private static final List<String> _optionalClasspath = new ArrayList<String>();
    
    
    //These classloaders are marked 'volatile' because it is initialised once by 
    //one thread, but accessed by multiple other threads.
    private static volatile ASURLClassLoader _sharedClassLoader = null;
    private static volatile ClassLoaderChain _optionalChain = null;
    private static volatile ClassLoaderChain _asChain = null;
    
    //Maintains a list of all addon classloaders.
    private static final Map<String, ASURLClassLoader> addonsMap = new HashMap<String, ASURLClassLoader>();
    //Maintains a map of all the jars loaded for an addon
    private static final Map<String, List<String>> addOnsManifestsMap = new HashMap<String, List<String>>();

    private static volatile ClassLoaderChain _addOnsChain = null;
    
    private static final boolean  bDebug = new Boolean(
                    System.getProperty("com.sun.aas.useNewClassLoader.debug", "false")).booleanValue();


    //By default we set the new classloader hierarchy as the default 
    static {
        if (System.getProperty(USE_NEW_CLASSLOADER_PROPERTY) == null) {
            System.setProperty(USE_NEW_CLASSLOADER_PROPERTY, "true");
        }
    }
    
    public static ClassLoader getSharedChain(){
        return _sharedClassLoader;
    }

    public static ClassLoader getOptionalChain(){
        return _optionalChain;
    }
    
    public static ClassLoader getAppServerChain(){
        return _asChain;
    }

    /**
     * Gets the paths for <em>all</em> addons.
     * Used in ASClassLoaderUtil while computing WebModuleClassPath.
     * 
     * This method is marked synchronized because there might be multiple 
     * callers to get addons classpath while we are iterating 
     * through the addons Map. 
     * [via ASClassLoaderUtil.getWebModuleClassPath]
     * 
     * @return An unmodifiable <code>List</code> containing the 
     * absolute paths of all libraries in the addons classloader
     * chain.
     */
    public static synchronized List<String> getAddOnsClasspath() {
    	final List<String> s = new ArrayList<String>();
    	for (final List<String> lst : addOnsManifestsMap.values()) {
			s.addAll(lst);
		}
    	logFine("addons classpath : " + s);
    	return Collections.unmodifiableList(s);
    }

    /*
     * This method is marked synchronized because we need to initialise
     * the shared classpath only once and there might multiple callers
     * in different threads to get the shared classpath. 
     */
    private static synchronized List<String> _getSharedClasspathInternal() {
        if(_sharedClasspath.size() == 0) {
            initialiseSharedClasspath();
            assert(_sharedClasspath.size() != 0);
        }
            return _sharedClasspath;
    }
    
    /**
     * Returns an unmodifiable list of the shared classpath components,
     * for use by external classes like ASClassLoaderUtil.
     * 
     * @return An unmodifiable <code>List</code> containing the 
     * absolute paths of all libraries in the shared classloader
     * chain.
     */
    public static synchronized List<String> getSharedClasspath() {
    	return Collections.unmodifiableList(_sharedClasspath);
    }
    
    private static void initialiseSharedClasspath(){
        //PE/EE Shared jars        
        final String asLib = installRoot + fileSeparator + "lib" + fileSeparator;        
        String sharedJarsList = System.getProperty("com.sun.aas.classloader.sharedChainJars");
        if (isEE()) {
            final String eeSharedJarsList = System.getProperty("com.sun.aas.classloader.sharedChainJars.ee");
            sharedJarsList  += ("," + eeSharedJarsList);
        }
        logFine("shared jar list " + sharedJarsList);
        
        final List<String> shr = getLibraryList(asLib, sharedJarsList);
        
        //Computing classpath prefix,suffix and server classpath
        final String prefixString = System.getProperty(CLASSPATH_PREFIX_PROPERTY);
        logFine(" prefixString " + prefixString );
        String[] classpathPrefix = null;
        if (!isEmpty(prefixString)) {
        	classpathPrefix = prefixString.split("" +File.pathSeparatorChar);
        }
        
        final String suffixString = System.getProperty(CLASSPATH_SUFFIX_PROPERTY);
        logFine(" suffixString " + suffixString);
        String[] classpathSuffix = null;
        if (!isEmpty(suffixString)) {
        	classpathSuffix = suffixString.split("" +File.pathSeparatorChar);
        }
        
        final String serverClassPathString = System.getProperty(SERVER_CLASSPATH_PROPERTY);
        logFine(" serverClassPathString " + serverClassPathString);
        String[] serverClassPath = null;
        if (!isEmpty(serverClassPathString)) {
        	serverClassPath = serverClassPathString.split("" +File.pathSeparatorChar);
        }
        
        //Creating final shared chain list.
        
        if (classpathPrefix != null) _sharedClasspath.addAll(Arrays.asList(classpathPrefix));
        _sharedClasspath.addAll(shr);
        if (serverClassPath != null) _sharedClasspath.addAll(Arrays.asList(serverClassPath));
        if (classpathSuffix != null) _sharedClasspath.addAll(Arrays.asList(classpathSuffix));
    }

    /**
     * Returns an unmodifiable list of all libraries in 
     * the appserver chain.
     * 
     * @return An unmodifiable <code>List</code> containing the 
     * absolute paths of all libraries in the appserver chain.
     */
    private static List<String> getAppServerClasspath() {
        return Collections.unmodifiableList(_appserverClasspath);
    }
    
    /**
     * Returns an unmodifiable list of all libraries in 
     * the optional chain.
     * 
     * @return An unmodifiable <code>List</code> containing the 
     * absolute paths of all libraries in the optional chain.
     */
    private static List<String> getOptionalClasspath() {
        return Collections.unmodifiableList(_optionalClasspath);
    }
      
    /**
     * ServerClassPath was earlier provided as "server-classpath" attribute
     * in the "java-config" element of domain.xml and this is used by 
     * BaseManager Only.
     * 
     * Marked synchronized as there could multiple threads trying to get 
     * server classpath.
     */
    public static synchronized List<String> getServerClasspath() {
        final String asLib = installRoot + fileSeparator + "lib" + fileSeparator;
        String serverJarsList = System.getProperty("com.sun.aas.classloader.serverClassPath");

        if (isEE()) {
            final String eeServerJarsList = System.getProperty("com.sun.aas.classloader.serverClassPath.ee");
            serverJarsList  += ("," + eeServerJarsList);
        }
        logFine("serverClassPathJarsList " + serverJarsList);
        
        final List<String> serverClasspathList = getLibraryList(asLib, serverJarsList);
        return serverClasspathList;
    }
    
    
    /**
     * Read all Non-manifest jars under lib/addons directory 
     * and compute all addons and their dependencies and 
     * update <code>manifestJars</code> and
     * <code>addOnsManifestsMap</code> accordingly
     */
    private static List<String> getManifestAddonJars() {
        final List<String> manifestJars = new ArrayList<String>();
        JarFile file = null;
        try {
        final String addonDir = installRoot + fileSeparator + "lib" + fileSeparator + "addons";
        final File libAddonDirectory = new File(addonDir);
        if(!libAddonDirectory.isDirectory())
            return manifestJars;
        
        final File[] fileArray = libAddonDirectory.listFiles();
        for(int i = 0;i<fileArray.length;i++) {
                final String addonJar = fileArray[i].getName();
                String addOnName = "";
                String jarExtension = "";
                int dotLastIndex = addonJar.lastIndexOf(".");
                if(dotLastIndex != -1) {
                    jarExtension = addonJar.substring(dotLastIndex + 1);
                    addOnName = addonJar.substring(0, dotLastIndex - 1);
                }
                
                
                if(jarExtension.equalsIgnoreCase("jar")) {
                    manifestJars.add(fileArray[i].getAbsolutePath()); 
                    updateAddOnManifests(addOnName, fileArray[i].getAbsolutePath());
                    
                    file = new JarFile(fileArray[i].getAbsolutePath());
                    Manifest mf = file.getManifest();
                    Attributes attributes = null;
                    if(mf != null) {
                        attributes = mf.getMainAttributes();
                        if(attributes != null) {
                            String classPath = attributes.getValue(Attributes.Name.CLASS_PATH);
                            if(classPath != null && !classPath.trim().equals("")) {
                                StringTokenizer stoken = new StringTokenizer(classPath);
                                while(stoken.hasMoreTokens()) {
                                    String classPathJar = addonDir + fileSeparator + stoken.nextElement();
                                    manifestJars.add(classPathJar);
                                    updateAddOnManifests(addOnName, classPathJar);
                                }
                            }
                            //Logger.getAnonymousLogger().log(Level.FINE, "Main Class "+mainClass); 
                        }
                        file.close();
                    }
                }
        }
        }catch(Exception e) {
            e.printStackTrace(System.err);
        }finally {
            try {
            if(file != null)
                file.close();
            }catch(Exception ex) {
                ex.printStackTrace(System.err);
            }
        }
        //Logger.getAnonymousLogger().log(Level.INFO, "nonManifestJars list: "+nonManifestJars);
        return manifestJars;
    }
    
    /**
     *  Used to un-install an addon during AS lifetime. [called when asadmin
     *  uninstall-addon is called]
     *  
     *  Uninstallation should result in removing the addon's classloader
     *  from the addons chain.
     *  
     *  This method is made synchronized to prevent invalid concurrent modifications 
     *  to the classloader chain.
     *   
     * @param addonName Addon to uninstall.
     */
    public static synchronized void uninstallAddon(String addonName) {
    	logFine("Uninstalling addon " + addonName);
    	_addOnsChain.removeFromList(addonsMap.get(addonName));
    	logFine("removed addonclassloader " + addonsMap.get(addonName) + 
    			" for " + addonName);
    }
    
    /**
     * Utility method to keep track of all jars [main addon jar and jars from 
     * manifest] of an addon. 
     * 
     * Used while creating the addon chain
     * @param addOnName
     * @param addonJarPath
     */
    private static void updateAddOnManifests(String addOnName,
			String addonJarPath) {
    	logFine("updateAddOnManifests: adding " + addonJarPath + " for " + addOnName);
    	List<String> al = null;
    	final List<String> tmp = addOnsManifestsMap.get(addOnName);
    	al = (tmp != null) ? tmp : new ArrayList<String>();
    	al.add(addonJarPath);
    	addOnsManifestsMap.put(addOnName, al);
	}

    /**
     * Entry point into the application server
     */
    public static void main(String[] args) {
        try{
            Class peMainClass = null;
            
            if(Boolean.getBoolean(USE_NEW_CLASSLOADER_PROPERTY)){
                ASenvPropertyReader reader = new ASenvPropertyReader(
                    System.getProperty(SystemPropertyConstants.CONFIG_ROOT_PROPERTY),
                    false);
                reader.setSystemProperties();
                
                
                setupClassloaders();
                //Use the new classloader hierarchy
                peMainClass = _asChain.loadClass(
                                "com.sun.enterprise.server.PEMain", true);
                Thread.currentThread().setContextClassLoader(_asChain);            
            } else {
                peMainClass = Class.forName("com.sun.enterprise.server.PEMain");
            }
            
            Class[] argTypes = new Class[]{String[].class};
            Method m = peMainClass.getMethod("main", argTypes);           
            Object[] argListForInvokedMain = new Object[]{args};
            m.invoke(null, argListForInvokedMain);
        } catch(Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /*
     * Sets up all internal classloader chains 
     * 
     *  This method is marked synchronized to prevent multiple threads
     *  from initialising the classloader chains concurrently.
     */
    private static synchronized void setupClassloaders(){
        prepareAppServerJars();
        initOptionalOverrideableJars();
        appendOtherJarsToSharedChain();
        setupSharedChain();
        setupAddOnChain();
        setupOptionalOverrideableChain();
        setupAppServerChain();
        
        List<String> cp = _getSharedClasspathInternal();
        //Seed the classpath string with the system classpath
        //to support users setting "env-classpath-ignored" to false.
        String oldcp = System.getProperty("java.class.path");
        StringBuilder classpath = null;
        
        if (oldcp != null) {
        	classpath = new StringBuilder(oldcp + pathSeparator);
        } else {
        	classpath = new StringBuilder();
        }
         
        for(final String s:cp){
            classpath.append(s);
            classpath.append(pathSeparator);
        }
        
        System.setProperty("java.class.path", classpath.toString());
    }

    private static void setupAddOnChain() {
    	logFine("setting up addon chain");
        _addOnsChain = new ClassLoaderChain(_sharedClassLoader);
        _addOnsChain.setName("Addons Chain");
        getManifestAddonJars();
        
        for (final String addOnName : addOnsManifestsMap.keySet()) {
        	//create a classloader for an addon
			//get all jars associated with this addon
			URL[] addOnURLs = getURLList(addOnsManifestsMap.get(addOnName));
			logFine(" addon: " + addOnName + " urls: " + addOnURLs);
			
			//create a URLClassLoader for this addon - parent set to shared classloader
			ASURLClassLoader addonClassLoader = new ASURLClassLoader(addOnURLs, _sharedClassLoader);
			//keep a map so that we can remove it during uninstall-addon
			addonsMap.put(addOnName, addonClassLoader);
			
			//add it to the AddOns chain
			_addOnsChain.addToList(addonClassLoader);
		}
	}

    /**
     * SPI for Verifier to use when it runs in appserver mode. Returns the server classpath
     * for an application. This as now includes all libraries in installRoot and 
     * server-classpath.
     */ 
    public static List<String> getServerClassPath(String configDir, 
                                                  String domainDir){
        ASenvPropertyReader reader = new ASenvPropertyReader(configDir,false);
        reader.setSystemProperties();

        //Add all libraries in as-install-dir/lib
        final String asLib = installRoot + fileSeparator ;        
        final List<String >serverClassPathList = new ArrayList<String>();
        final File[] fls= getAllLibrariesInLib(asLib); 
        
        for (File element : fls) {
            serverClassPathList.add(element.getAbsolutePath()); 
        }
        
        //add server-classpath
        final String mqlib = System.getProperty(SystemPropertyConstants.IMQ_LIB_PROPERTY);
        final String antlib = System.getProperty(SystemPropertyConstants.ANT_LIB_PROPERTY);
        final String jdmklib = System.getProperty(SystemPropertyConstants.JDMK_HOME_PROPERTY);
        final String hadbRoot = System.getProperty(SystemPropertyConstants.HADB_ROOT_PROPERTY);
        
        final String[] peServerClassPath = {installRoot + "/lib/install/applications/jmsra/imqjmsra.jar",
                        mqlib + "/jaxm-api.jar" , mqlib + "/fscontext.jar",
                        mqlib + "/imqbroker.jar", mqlib + "/imqjmx.jar",
                        mqlib + "/imqxm.jar",
                        antlib + "/ant.jar", jdmklib + "/lib/jdmkrt.jar"} ;
        final String[] eeServerClassPath = {hadbRoot + "/lib/hadbjdbc4.jar",
                        jdmklib + "/lib/jdmkrt.jar",hadbRoot + "/lib/dbstate.jar",
                        hadbRoot + "/lib/hadbm.jar", hadbRoot + "/lib/hadbmgt.jar"} ;
        for (final String element : peServerClassPath) {
            File f = new File(element);
            if(f.exists()) {
                serverClassPathList.add(f.getAbsolutePath());
            }
        }
        
        for (final String element : eeServerClassPath) {
            File f = new File(element);
            if(f.exists()) {
                serverClassPathList.add(f.getAbsolutePath());
            }
        }
        
        //add jars placed in domain-dir/lib
        final File[] domainfls= getAllLibrariesInLib(domainDir + fileSeparator);
        for (File elt : domainfls) {
            serverClassPathList.add(elt.getAbsolutePath()); 
        }

        logFine("Server Classpath for verifier " + serverClassPathList + "\n");
        Logger.getAnonymousLogger().log(Level.FINE, "Server Classpath for verifier " + serverClassPathList);
        return serverClassPathList;
    }
    
    /**
     * The Shared chain is the child of SystemClassLoader in the AS classloader 
     * hierarchy and houses all immutable, platform classes. [eg j2ee.jar] 
     *
     */
    private static void setupSharedChain(){
        final List<String> sharedChainJarList = _getSharedClasspathInternal();
        logFine("shared classpath jars : " + sharedChainJarList + "\n");
        final URL[] urls = getURLList(sharedChainJarList);
        logFine(" SharedChain URL List " + urls);
         _sharedClassLoader = new ASURLClassLoader(urls, 
                         ClassLoader.getSystemClassLoader());
        _sharedClassLoader.setName("Shared ClassLoader Chain");
    }

    
    /**
     * The optional overrideable chain is "suffixed" to the chain of 
     * dependencies of an application. It consists of libraries that are by 
     * default provided to an application when an application has not specified
     * an explicit dependency. This chain consists of all AS provided libraries 
     * that could be overridden by an application. 
     */
    private static void setupOptionalOverrideableChain(){
         
        _optionalChain = new ClassLoaderChain(_addOnsChain);
        _optionalChain.setName("optionalChain");
        
        final URL[] urls = getURLList(_optionalClasspath);
        //Parent set to Shared Chain
        final ASURLClassLoader optionalJarsLoader = new ASURLClassLoader(urls, 
                                                                _addOnsChain);
        _optionalChain.addToList(optionalJarsLoader);

    }
    
    private static void initOptionalOverrideableJars(){
        final String asLib = installRoot + fileSeparator + "lib" + fileSeparator;
        String optionalJarsString = System.getProperty("com.sun.aas.classloader.optionalOverrideableChain");
        if (isEE()) {
            final String eeOptionalJarsList = System.getProperty("com.sun.aas.classloader.optionalOverrideableChain.ee");
            optionalJarsString  += ("," + eeOptionalJarsList);
        }
        
        logFine(" optionalOverrideableChain" + optionalJarsString );
        
        updateClasspathList(asLib, optionalJarsString, _optionalClasspath);
        logFine("Optional overrideable chain classpath : " + _optionalClasspath + "\n");
    }    
    
    /**
     * The application server chain is composed of jars/libraries that are used
     * and visible to application server classes only. The optional overrideable
     * chain is also a part of this chain. The Shared ClassLoader chain is set
     * as the parent of the application server chain.
     */
    private static void setupAppServerChain(){
        final URL[] urls = getURLList(_appserverClasspath);
        
        //parent set to Shared Chain
        _asChain = new ClassLoaderChain(_addOnsChain);
        _asChain.setName("ASChain");
        
        final ASURLClassLoader asimplloader = new ASURLClassLoader(urls, _asChain);
        asimplloader.setName("asimpl");
        _asChain.addToList(asimplloader);
        _asChain.addToList(_optionalChain);        
    }

    private static void prepareAppServerJars(){
        //AS only jars
        final String asLib = installRoot + fileSeparator + "lib" + fileSeparator;
        String appserverJarsStr = System.getProperty("com.sun.aas.classloader.appserverChainJars");
        if (isEE()) {
            final String eeAppserverJarsList = System.getProperty("com.sun.aas.classloader.appserverChainJars.ee");
            appserverJarsStr  += ("," + eeAppserverJarsList);
        }
        logFine("appserverJarsString " + appserverJarsStr );
        
        updateClasspathList(asLib, appserverJarsStr, _appserverClasspath);
        logFine("Application server classpath : " + _appserverClasspath + "\n");
    }
    
    /**
     * Determines if the AS process is running in EE. 
     * XXX: to refactor this to use the common implementation.
     */
    private static boolean isEE() {
        boolean isEE = false;
        final String eepffc = "com.sun.enterprise.ee.server.pluggable.EEPluggableFeatureImpl";
        final String pn = "com.sun.appserv.pluggable.features";
        final String pv = System.getProperty(pn);
        if (eepffc.equals(pv)) {
            isEE = true;
        }
        return ( isEE );
    }
    
    //checks to see if a string is empty or null
    private static boolean isEmpty(String s){
    	return ((s == null) || (s.trim().length() == 0));
    }
    
    /**
     * Update the passed in <code>classpathAddressList</code> with the 
     * library jars referred to in <code>librariesString</code>
     * 
     * @param libraryRoot The directory root for the jars referred 
     * to in <code>librariesString</code>
     * 
     * @param librariesList A comma-separated list of all library 
     * jars under <code>libraryRoot</code> that needs to be added to
     * the list.
     * 
     * @param classpathAddressList A list of jars [absolute paths] that 
     * defines a search path for a particular classloader.
     *  
     * @return the updated list of jars.
     */
    private static List<String> updateClasspathList(String libraryRoot, 
    		String librariesList, List<String> classpathAddressList) {
        String[] libList = librariesList.split(",");
		for (String library : libList) {
            library = library.trim();
			final File file = new File(library);
            if (!file.isAbsolute()) {
				classpathAddressList.add(libraryRoot + library);
            } else {
				classpathAddressList.add(library);
			}
            }
		return classpathAddressList;
        }
    
    /**
     * Gets a List of absolute path names for the passed in
     * <code>librariesString</code> rooted at <code>libraryRoot</code>
     */
    private static List<String> getLibraryList(String libraryRoot, 
    		String librariesString) {
    	return updateClasspathList(libraryRoot, librariesString, 
    			new ArrayList<String>());
    }
    
    private static URL[] getURLList(List<String> librariesList) {
        int i=0;
        final String [] sharedJars = librariesList.toArray(new String[] {});
        final URL [] urls = new URL[sharedJars.length];
        for(final String s:sharedJars){
            try{
                URL url = (new File(s)).toURI().toURL();
                logFine(s + " exists ? "+ (new File(s)).exists());
                urls[i++] = url;
            }catch(MalformedURLException e){
                Logger.getAnonymousLogger().warning(e.getMessage());
                Logger.getAnonymousLogger().log(Level.WARNING, "Exception while" 
                		+ "setting up shared chain", e);
            }
        }
        return urls;
    }
    
    private static void logFine(String s) {
        if(bDebug) {
            System.err.println(s);
        }
    }
    
    /**
     * Adds all the remaining jars in the installRoot/lib folder
     */
    private static void appendOtherJarsToSharedChain(){
        final List<String> list = new ArrayList<String>();
        list.addAll(_getSharedClasspathInternal());
        list.addAll(getAppServerClasspath());
        list.addAll(getOptionalClasspath());
        final File[] files = getAllLibrariesInLib(installRoot);
        
        //Remove all libraries listed in the excludesList.
        final String excludesListString = System.getProperty(
        		"com.sun.aas.classloader.excludesList", "");
        final List<String> excludesList = getLibraryList(
        		installRoot + fileSeparator + "lib" + fileSeparator, 
        		excludesListString);
        
        for(final File file:files){
            try{
                if((!list.contains(file.getCanonicalPath())) 
                		&& (!(isInExcludesList(excludesList, file)))){
                    _getSharedClasspathInternal().add(file.getCanonicalPath());
                    logFine("appendOtherJarsToSharedChain - " +
                    		"added " + file.getCanonicalPath());
                } else {
                	logFine("appendOtherJarsToSharedChain - " +
                			"not adding " + file.getCanonicalPath());
                    logFine(file.getCanonicalPath() + " exists in list ? " + list.contains(file.getCanonicalPath()));
                    logFine(file.getCanonicalPath() + "in excludes list ? " + isInExcludesList(excludesList, file));
                }
            }catch(java.io.IOException ioe){
                System.err.println("Error getting " + file.getAbsolutePath() 
                		+ " " + ioe.getMessage());
            }
        }

        //Finally ensure that the libraries listed in the includes list is added
        //to the shared chain
        final String includesListString = System.getProperty(
        		"com.sun.aas.classloader.includesList", "");
        final List<String> includesList = getLibraryList(installRoot, includesListString);
        for (final String f : includesList) {
			if(!(_sharedClasspath.contains(f))){
				logFine("appendOtherJarsToSharedChain - " +
						"adding " + f + " via includes");
				_sharedClasspath.add(f);
			}
            }
        }    
    
    private static boolean isInExcludesList(List<String> excludesList, File f) throws IOException {
    	return excludesList.contains(f.getCanonicalPath());
}

    private static File[] getAllLibrariesInLib(String asLib) {
        final File installLib = new File(asLib,"lib");
        final File [] files = installLib.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if(name.endsWith(".jar") || name.endsWith(".zip")) {
                    return true;
                } else {
                return false;
            }
            }
        });
        return files;
    }
}
