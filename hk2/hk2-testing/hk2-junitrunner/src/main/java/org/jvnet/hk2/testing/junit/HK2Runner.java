/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.junit.Before;
import org.jvnet.hk2.testing.junit.internal.ClassVisitorImpl;
import org.jvnet.hk2.testing.junit.internal.ErrorServiceImpl;
import org.jvnet.hk2.testing.junit.internal.JustInTimeInjectionResolverImpl;
import org.objectweb.asm.ClassReader;

/**
 * This class should be extended by test classes in order to get an automatically
 * filled in ServiceLocator.  By default the testLocator will inspect the package
 * of the test to find any classes annotated with &#64;Service.  The locator will
 * also be able to do second-chance advertisement of services that were injected.
 * The default ServiceLocator will also have an error handler that causes any classloading
 * failure to get rethrown up to the lookup call, since this can sometimes cause
 * confusion.
 * <p>
 * This behavior can be customized by overriding the before method and calling the super
 * of one of the other methods available for customization
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
        initialize(this.getClass().getName(), null, null);
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
        if (name == null) throw new IllegalArgumentException();
        if (packages == null) packages = new LinkedList<String>();
        if (clazzes == null) clazzes = new LinkedList<Class<?>>();
        
        ServiceLocator found = ServiceLocatorFactory.getInstance().find(name);
        if (found != null) {
            testLocator = found;
            
            testLocator.inject(this);
            return;
        }
        
        testLocator = ServiceLocatorFactory.getInstance().create(name);
        
        DynamicConfigurationService dcs = testLocator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();
        
        config.addActiveDescriptor(ErrorServiceImpl.class);
        config.addActiveDescriptor(JustInTimeInjectionResolverImpl.class);
        
        addServicesFromDefault(config);
        
        addServicesFromPackage(config, packages);
        
        for (Class<?> clazz : clazzes) {
            config.addActiveDescriptor(clazz);
        }
        
        config.commit();
        
        testLocator.inject(this);
    }
    
    protected void setVerbosity(boolean verbose) {
        this.verbose = verbose;
    }
    
    private void addServicesFromDefault(final DynamicConfiguration config) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                internalAddServicesFromDefault(config);
                return null;
            }
            
        });
    }
    
    private void internalAddServicesFromDefault(DynamicConfiguration config) {
        ClassLoader loader = this.getClass().getClassLoader();
        
        Enumeration<URL> resources;
        try {
            resources = loader.getResources("META-INF/hk2-locator/default");
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            
            return;
        }
        
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
           
            try {
                InputStream urlStream = url.openStream();
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlStream));
                
                boolean goOn = true;
                while (goOn) {
                    DescriptorImpl bindMe = new DescriptorImpl();
                
                    goOn = bindMe.readObject(reader);
                    if (goOn == true) {
                        config.bind(bindMe);
                    }
                }
                
                reader.close();
            }
            catch (IOException ioe) {
                continue;
            }
            
            
        }
        
    }
    
    private void addServicesFromPackage(final DynamicConfiguration config, final List<String> packages) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {

            @Override
            public Object run() {
                internalAddServicesFromPackage(config, packages);
                return null;
            }
            
        });
    }
    
    private void internalAddServicesFromPackage(DynamicConfiguration config, List<String> packages) {
        if (packages.isEmpty()) return;
        
        String classPath = System.getProperty(CLASS_PATH_PROP);
        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        
        while(st.hasMoreTokens()) {
            String pathElement = st.nextToken();
            
            addServicesFromPathElement(config, packages, pathElement);
        }
        
    }
    
    private void addServicesFromPathElement(DynamicConfiguration config, List<String> packages, String element) {
        File fileElement = new File(element);
        if (!fileElement.exists()) return;
        
        if (fileElement.isDirectory()) {
            addServicesFromPathDirectory(config, packages, fileElement);
        }
        else {
            addServicesFromPathJar(config, packages, fileElement);
        }
    }
    
    private void addServicesFromPathDirectory(DynamicConfiguration config, List<String> packages, File directory) {
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
                    
                    addClassIfService(config, fis);
                }
                catch (IOException ioe) {
                    // Just don't add it
                }
            }
        }
        
    }
    
    private void addServicesFromPathJar(DynamicConfiguration config, List<String> packages, File jar) {
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
                        addClassIfService(config, jarFile.getInputStream(entry));
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
    
    private void addClassIfService(DynamicConfiguration config, InputStream is) throws IOException {
        ClassReader reader = new ClassReader(is);
        
        ClassVisitorImpl cvi = new ClassVisitorImpl(config, verbose);
        
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        
    }
    
    private static String convertToFileFormat(String clazzFormat) {
        return clazzFormat.replaceAll("\\.", "/");
    }
}
