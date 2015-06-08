/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.testing.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.general.GeneralUtilities;
import org.junit.Before;
import org.jvnet.hk2.testing.junit.annotations.Classes;
import org.jvnet.hk2.testing.junit.annotations.Excludes;
import org.jvnet.hk2.testing.junit.annotations.InhabitantFiles;
import org.jvnet.hk2.testing.junit.annotations.Packages;
import org.jvnet.hk2.testing.junit.internal.ClassVisitorImpl;
import org.jvnet.hk2.testing.junit.internal.ErrorServiceImpl;
import org.jvnet.hk2.testing.junit.internal.JustInTimeInjectionResolverImpl;
import org.glassfish.hk2.external.org.objectweb.asm.ClassReader;

/**
 * This class should be extended by test classes in order to get an automatically
 * filled in ServiceLocator.  By default the testLocator will inspect the package
 * of the test to find any classes annotated with &#64;Service.  The locator will
 * also be able to do second-chance advertisement of services that were injected.
 * The default ServiceLocator will also have an error handler that causes any classloading
 * failure to get rethrown up to the lookup call, since this can sometimes cause
 * confusion.
 * <p>
 * The behavior of HK2Runner can be customized by annotating the class extending
 * HK2Runner with {@link org.jvnet.hk2.testing.junit.annotations.Packages},
 * {@link org.jvnet.hk2.testing.junit.annotations.Classes}, {@link org.jvnet.hk2.testing.junit.annotations.Excludes}
 * or {@link org.jvnet.hk2.testing.junit.annotations.InhabitantFiles}.
 * <p>
 * {@link org.jvnet.hk2.testing.junit.annotations.Packages} gives the names of packages
 * that will automatically be scanned for classes that should be added to testLocator
 * as services.  {@link org.jvnet.hk2.testing.junit.annotations.Classes} gives an
 * explicit set of classes that should be added to testLocator as services.
 * {@link org.jvnet.hk2.testing.junit.annotations.Excludes} gives a set of services
 * that should not be automatically added to the testLocator.
 * {@link org.jvnet.hk2.testing.junit.annotations.InhabitantFiles} gives a set of
 * inhabitant files to load in the classpath of the test.
 * <p>
 * This behavior can be customized by overriding the before method of the test and calling one
 * of the {@link #initialize(String, List, List)} methods.  The annotations listed above
 * are overridden by any values passed to the initialize methods
 * 
 * @author jwells
 */
public class HK2Runner {
    private final static String CLASS_PATH_PROP = "java.class.path";
    private final static String DOT_CLASS = ".class";
    
    /**
     * Test classes can use this service locator as their private test locator
     */
    protected ServiceLocator testLocator;
    
    /**
     * The verbosity of this runner
     */
    private boolean verbose = false;
    
    /**
     * This will generate the default testLocator for this test
     * class, which will search the package of the test itself for
     * classes annotated with &#64;Service.
     */
    @Before
    public void before() {
        Packages packages = getClass().getAnnotation(Packages.class);
        if (packages == null) {
            initialize(getClass().getName(), 
                Collections.singletonList(getClass().getPackage().getName()),
                null, null, null);
        }
        else {
            initialize(null, null, null, null, null);
        }
    }
    
    /**
     * This method initializes the service locator with services.  The name
     * of the locator will be the fully qualified name of the class.  All
     * other values will either be empty or will come from the annotations
     * {@link Packages}, {@link Classes}, {@link Excludes}, @{link InhabitantFiles}
     */
    public void initialize() {
        initialize(null, null, null, null, null);
    }

    /**
     * This method initializes the service locator with services from the given list
     * of packages (in "." format) and with the set of classes given.
     * 
     * @param name The name of the service locator to create.  If there is already a
     * service locator of this name then the remaining fields will be ignored and the existing
     * locator with this name will be returned.  May not be null
     * @param packages The list of packages (in "." format, i.e. "com.acme.test.services") that
     * we should hunt through the classpath for in order to find services.  If null this is considered
     * to be the empty set
     * @param clazzes A set of classes that should be analyzed as services, whether they declare
     * &#64;Service or not.  If null this is considered to be the empty set
     */
    protected void initialize(String name, List<String> packages, List<Class<?>> clazzes) {
        initialize(name, packages, clazzes, null, null);
    }
    
    /**
     * This method initializes the service locator with services from the given list
     * of packages (in "." format) and with the set of classes given.
     * 
     * @param name The name of the service locator to create.  If there is already a
     * service locator of this name then the remaining fields will be ignored and the existing
     * locator with this name will be returned.  May not be null
     * @param packages The list of packages (in "." format, i.e. "com.acme.test.services") that
     * we should hunt through the classpath for in order to find services.  If null this is considered
     * to be the empty set
     * @param clazzes A set of classes that should be analyzed as services, whether they declare
     * &#64;Service or not.  If null this is considered to be the empty set
     * @param excludes A set of implementations that should be excluded from being added.  This list is
     * NOT checked against the clazzes list (the explicit include wins), but instead against the set of
     * things coming from packages or from the hk2-locator/default file
     */
    protected void initialize(String name, List<String> packages, List<Class<?>> clazzes, Set<String> excludes) {
        initialize(name, packages, clazzes, excludes, null);
    }
    
    private List<String> getDefaultPackages() {
        Packages packages = getClass().getAnnotation(Packages.class);
        if (packages == null) return Collections.emptyList();
        
        List<String> retVal = new ArrayList<String>(packages.value().length);
        for (String pack : packages.value()) {
            if (Packages.THIS_PACKAGE.equals(pack)) {
                retVal.add(getClass().getPackage().getName());
            }
            else {
                retVal.add(pack);
            }
        }
        
        return retVal;
    }
    
    private List<Class<?>> getDefaultClazzes() {
        Classes clazzes = getClass().getAnnotation(Classes.class);
        if (clazzes == null) return Collections.emptyList();
        
        List<Class<?>> retVal = new ArrayList<Class<?>>(clazzes.value().length);
        for (Class<?> clazz : clazzes.value()) {
            retVal.add(clazz);
        }
        
        return retVal;
    }
    
    private Set<String> getDefaultExcludes() {
        Excludes excludes = getClass().getAnnotation(Excludes.class);
        if (excludes == null) return Collections.emptySet();
        
        Set<String> retVal = new HashSet<String>();
        for (String exclude : excludes.value()) {
            retVal.add(exclude);
        }
        
        return retVal;
    }
    
    private Set<String> getDefaultLocatorFiles() {
        HashSet<String> retVal = new HashSet<String>();
        InhabitantFiles iFiles = getClass().getAnnotation(InhabitantFiles.class);
        if (iFiles == null) {
            retVal.add("META-INF/hk2-locator/default");
            return retVal;
        }
        
        for (String iFile : iFiles.value()) {
            retVal.add(iFile);
        }
        
        return retVal;
    }
    
    /**
     * This method initializes the service locator with services from the given list
     * of packages (in "." format) and with the set of classes given.
     * 
     * @param name The name of the service locator to create.  If there is already a
     * service locator of this name then the remaining fields will be ignored and the existing
     * locator with this name will be returned.  May not be null
     * @param packages The list of packages (in "." format, i.e. "com.acme.test.services") that
     * we should hunt through the classpath for in order to find services.  If null this is considered
     * to be the empty set
     * @param clazzes A set of classes that should be analyzed as services, whether they declare
     * &#64;Service or not.  If null this is considered to be the empty set
     * @param excludes A set of implementations that should be excluded from being added.  This list is
     * NOT checked against the clazzes list (the explicit include wins), but instead against the set of
     * things coming from packages or from the hk2-locator/default file
     * @param locatorFiles A set of locator inhabitant files to search the classpath for to load.  If
     * this value is null then only META-INF/hk2-locator/default files on the classpath will be searched.
     * If this value is an empty set then no inhabitant files will be loaded.  If this value contains
     * values those will be searched as resources from the jars in the classpath to load the registry with
     */
    protected void initialize(String name, List<String> packages, List<Class<?>> clazzes, Set<String> excludes, Set<String> locatorFiles) {
        if (name == null) name = getClass().getName();
        if (packages == null) packages = getDefaultPackages();
        if (clazzes == null) clazzes = getDefaultClazzes();
        if (excludes == null) excludes = getDefaultExcludes();
        if (locatorFiles == null) locatorFiles = getDefaultLocatorFiles();
        
        ServiceLocator found = ServiceLocatorFactory.getInstance().find(name);
        if (found != null) {
            testLocator = found;
            
            testLocator.inject(this);
            return;
        }
        
        testLocator = ServiceLocatorFactory.getInstance().create(name);
        
        ServiceLocatorUtilities.addClasses(testLocator, ErrorServiceImpl.class, JustInTimeInjectionResolverImpl.class);
        
        DynamicConfigurationService dcs = testLocator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        addServicesFromDefault(config, excludes, locatorFiles);
        
        addServicesFromPackage(config, packages, excludes);
        
        for (Class<?> clazz : clazzes) {
            config.addActiveDescriptor(clazz);
        }
        
        config.commit();
        
        testLocator.inject(this);
    }
    
    protected void setVerbosity(boolean verbose) {
        this.verbose = verbose;
    }
    
    private void addServicesFromDefault(final DynamicConfiguration config, final Set<String> excludes, final Set<String> locatorFiles) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                internalAddServicesFromDefault(config, excludes, locatorFiles);
                return null;
            }
            
        });
    }
    
    private void readResources(Enumeration<URL> resources, Set<String> excludes, DynamicConfiguration config) {
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
           
            try {
                InputStream urlStream = url.openStream();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlStream));
                
                boolean goOn = true;
                while (goOn) {
                    DescriptorImpl bindMe = new DescriptorImpl();
                
                    goOn = bindMe.readObject(reader);
                    if (goOn == true && !excludes.contains(bindMe.getImplementation())) {
                        config.bind(bindMe);
                    }
                }
                
                reader.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
                
                continue;
            }
            
            
        }
        
    }
    
    private void internalAddServicesFromDefault(DynamicConfiguration config, Set<String> excludes, Set<String> locatorFiles) {
        ClassLoader loader = this.getClass().getClassLoader();
        
        for (String locatorFile : locatorFiles) {
            Enumeration<URL> resources;
            try {
                resources = loader.getResources(locatorFile);
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            
                return;
            }
            
            readResources(resources, excludes, config);
        }
    }
    
    private void addServicesFromPackage(final DynamicConfiguration config, final List<String> packages, final Set<String> excludes) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                internalAddServicesFromPackage(config, packages, excludes);
                return null;
            }
            
        });
    }
    
    private void internalAddServicesFromPackage(DynamicConfiguration config, List<String> packages, Set<String> excludes) {
        if (packages.isEmpty()) {
            return;
        }
        
        String classPath = GeneralUtilities.getSystemProperty(CLASS_PATH_PROP, null);
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        
        while(st.hasMoreTokens()) {
            String pathElement = st.nextToken();
            
            addServicesFromPathElement(config, packages, pathElement, excludes);
        }
        
    }
    
    private void addServicesFromPathElement(DynamicConfiguration config, List<String> packages, String element, Set<String> excludes) {
        File fileElement = new File(element);
        if (!fileElement.exists()) return;
        
        if (fileElement.isDirectory()) {
            addServicesFromPathDirectory(config, packages, fileElement, excludes);
        }
        else {
            addServicesFromPathJar(config, packages, fileElement, excludes);
        }
    }
    
    private void addServicesFromPathDirectory(DynamicConfiguration config, List<String> packages, File directory, Set<String> excludes) {
        for (String pack : packages) {
            File searchDir = new File(directory, convertToFileFormat(pack));
            if (!searchDir.exists()) continue;
            if (!searchDir.isDirectory()) continue;
            
            File candidates[] = searchDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name == null) return false;
                    if (name.endsWith(DOT_CLASS)) return true;
                    return false;
                }
                
            });
            
            if (candidates == null) continue;
            
            for (File candidate : candidates) {
                try {
                    FileInputStream fis = new FileInputStream(candidate);
                    
                    addClassIfService(fis, excludes);
                }
                catch (IOException ioe) {
                    // Just don't add it
                }
            }
        }
        
    }
    
    private void addServicesFromPathJar(DynamicConfiguration config, List<String> packages, File jar, Set<String> excludes) {
        JarFile jarFile;
        try {
            jarFile = new JarFile(jar);
        }
        catch (IOException ioe) {
            // Not a jar file, forget it
            return;
        }
        
        try {
            for (String pack : packages) {
                String packAsFile = convertToFileFormat(pack);
                int packAsFileLen = packAsFile.length() + 1;
            
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                
                    String entryName = entry.getName();
                    if (!entryName.startsWith(packAsFile)) {
                        // Not in the correct directory
                        continue;
                    }
                    if (entryName.substring(packAsFileLen).contains("/")) {
                        // Next directory down
                        continue;
                    }
                    if (!entryName.endsWith(DOT_CLASS)) {
                        // Not a class
                        continue;
                    }
                
                    try {
                        addClassIfService(jarFile.getInputStream(entry), excludes);
                    }
                    catch (IOException ioe) {
                        // Simply don't add it if we can't read it
                    }
                }
            }
        }
        finally {
            try {
                jarFile.close();
            }
            catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private void addClassIfService(InputStream is, Set<String> excludes) throws IOException {
        ClassReader reader = new ClassReader(is);
        
        ClassVisitorImpl cvi = new ClassVisitorImpl(testLocator, verbose, excludes);
        
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        
    }
    
    private static String convertToFileFormat(String clazzFormat) {
        return clazzFormat.replaceAll("\\.", "/");
    }
}
