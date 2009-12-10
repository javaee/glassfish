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
package com.sun.enterprise.deployment.annotation.impl;

import com.sun.enterprise.deployment.annotation.introspection.ClassFile;
import com.sun.enterprise.deployment.annotation.introspection.ConstantPoolInfo;
import com.sun.enterprise.deployment.annotation.introspection.DefaultAnnotationScanner;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.logging.LogDomains;
import org.glassfish.apf.Scanner;
import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.apf.impl.JavaEEScanner;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PostConstruct;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.loader.util.ASClassLoaderUtil;
import org.glassfish.api.deployment.archive.ReadableArchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.net.URISyntaxException;


/**
 * This is an abstract class of the Scanner interface for J2EE module.
 *
 * @author Shing Wai Chan
 */
public abstract class ModuleScanner<T> extends JavaEEScanner implements Scanner<T>, PostConstruct {

    @Inject
    DefaultAnnotationScanner defaultScanner;
    
    protected File archiveFile = null;
    protected ClassLoader classLoader = null;
    protected ClassFile classFile = null;
    private boolean processAllClasses = Boolean.getBoolean("com.sun.enterprise.deployment.annotation.processAllClasses");

    
    private Set<String> entries = new HashSet<String>();

    protected Logger logger = LogDomains.getLogger(DeploymentUtils.class, 
        LogDomains.DPL_LOGGER);

    public void process(ReadableArchive archiveFile, 
            T bundleDesc, ClassLoader classLoader) throws IOException {
        File file = new File(archiveFile.getURI()); 
        process(file, bundleDesc, classLoader);
        completeProcess(bundleDesc, archiveFile);
    }

    /**
     * Performs all additional work after the "process" method has finished.
     * <p>
     * This is a separate method from "process" so that the app client scanner can invoke
     * it from its overriding process method.  All post-processing logic needs to be
     * collected in this one place.
     *
     * @param bundleDescr
     * @param archive
     * @throws IOException
     */
    protected void completeProcess(T bundleDescr, ReadableArchive archive) throws IOException {
        addLibraryJars(bundleDescr, archive);
    }

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
        classFile = new ClassFile(new ConstantPoolInfo(defaultScanner));
    }

    /**
     * This add extra className to be scanned.
     * @param className
     */
    protected void addScanClassName(String className) {
        if (className!=null && className.length()!=0)
            entries.add(className);
    }

    /**
     * This add all classes in given jarFile to be scanned.
     * @param jarFile
     */
    protected void addScanJar(File jarFile) throws IOException {
        JarFile jf = null;
        

        try {
            /*
             * An app might refer to a non-existent JAR in its Class-Path.  Java
             * SE accepts that silently, and so will GlassFish.
             */
            if ( ! jarFile.exists()) {
                return;
            }
            jf = new JarFile(jarFile);
            Enumeration<JarEntry> entriesEnum = jf.entries();
            while(entriesEnum.hasMoreElements()) {
                JarEntry je = entriesEnum.nextElement();
                if (je.getName().endsWith(".class")) {
                    if (processAllClasses) {
                        addEntry(je);
                    } else {
                        // check if it contains top level annotations...
                        ReadableByteChannel channel = Channels.newChannel(jf.getInputStream(je));
                        if (channel!=null) {
                            if (classFile.containsAnnotation(channel, je.getSize())) {
                                addEntry(je);                     
                            }

                            channel.close();
                        }
                    }
                }
            }
        } catch (ZipException ze) {
            logger.log(Level.WARNING, ze.getMessage() +  ": file path: " + jarFile.getPath());
        } 
        finally {
            if (jf != null) {
                jf.close();
            }
        }
    }
    
    private void addEntry(JarEntry je) {
        String className = je.getName().replace('/', '.');
        className = className.substring(0, className.length()-6);
        entries.add(className);                                
    }
    
    private void addEntry(File top, File f) {
        String fileName = f.getPath();
        fileName = fileName.substring(top.getPath().length()+1);
        String className = fileName.replace(File.separatorChar, '.');
        className = className.substring(0, className.length()-6);
        entries.add(className);        
    }

    /**
     * This will include all class in directory to be scanned.
     * param directory
     */
    protected void addScanDirectory(File directory) throws IOException {
        initScanDirectory(directory, directory);
    } 
    
    private void initScanDirectory(File top, File directory) throws IOException {
   
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getPath();
                if (fileName.endsWith(".class")) {
                    if (processAllClasses) {
                        addEntry(top, file);
                    } else {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(file);
                            if (classFile.containsAnnotation(fis.getChannel(), file.length())) {
                                addEntry(top, file);
                            }
                        } finally {
                            if (fis != null) {
                                fis.close();
                            }
                        }
                    }
                }
            } else {
                initScanDirectory(top, file);
            }
        }
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Set<Class> getElements() {
        Set<Class> elements = new HashSet<Class>();
        if (getClassLoader() == null) {
            AnnotationUtils.getLogger().severe("Class loader null");
            return elements;
        }        

        for (String className : entries) {
            if (AnnotationUtils.getLogger().isLoggable(Level.FINE)) {
                AnnotationUtils.getLogger().fine("Getting " + className);
            }
            try {                
                elements.add(classLoader.loadClass(className));
            } catch(ClassNotFoundException cnfe) {
                AnnotationUtils.getLogger().log(Level.WARNING, "Cannot load " + className + " reason : " + cnfe.getMessage(), cnfe);
            }
        }
        return elements;
    }

    protected void addLibraryJars(T bundleDesc, 
        ReadableArchive moduleArchive) throws IOException {
        List<URL> libraryURLs = new ArrayList<URL>();
        ModuleDescriptor moduleDesc = ((BundleDescriptor)bundleDesc).getModuleDescriptor();
        Application app = ((BundleDescriptor)moduleDesc.getDescriptor()).getApplication();
        ReadableArchive appArchive = moduleArchive.getParentArchive();
        if (app != null && appArchive != null) {
            // ear case
            File appRoot = new File(appArchive.getURI());

            // add libraries jars inside application lib directory
            libraryURLs.addAll(ASClassLoaderUtil.getAppLibDirLibrariesAsList(
                appRoot, app.getLibraryDirectory(), null));
            
            // add libraries referenced through manifest
            Manifest manifest = ASClassLoaderUtil.getManifest(moduleArchive.getURI().getPath());
            libraryURLs.addAll(ASClassLoaderUtil.getManifestClassPathAsURLs(
                manifest, appRoot.getPath()));
        }

        for (URL url : libraryURLs) {
            try {
                File libFile = new File(url.toURI());;
                if (libFile.isFile()) {
                    addScanJar(libFile);
                } else if (libFile.isDirectory()) {
                    addScanDirectory(libFile);
                }
            } catch (Exception ex) {
                // we log a warning and proceed for any problems in 
                // adding library jars to the scan list
                logger.log(Level.WARNING, ex.getMessage());
            }
        }       
    }
}
