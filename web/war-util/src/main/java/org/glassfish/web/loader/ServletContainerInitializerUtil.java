/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.web.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.*;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.annotation.HandlesTypes;
import com.sun.logging.LogDomains;
import org.apache.naming.Util;
import org.glassfish.deployment.common.ClassDependencyBuilder;

/**
 * Utility class - contains util methods used for implementation of
 * pluggable Shared Library features
 *
 *  @author Vijay Ramachandran
 */
public class ServletContainerInitializerUtil {

    private static final Logger log = LogDomains.getLogger(
        ServletContainerInitializerUtil.class, LogDomains.WEB_LOGGER);

    private static final ResourceBundle rb = log.getResourceBundle();

    /**
     * Given a class loader, check for ServletContainerInitializer
     * implementations in any JAR file in the classpath
     *
     * @param cl The ClassLoader to be used to find JAR files
     *
     * @return Iterable over all ServletContainerInitializers that were found
     */
    public static Iterable<ServletContainerInitializer> getServletContainerInitializers(
            Map<String, String> webFragmentMap, List<String> absoluteOrderingList,
            boolean hasOthers, ClassLoader cl) {
        /*
         * If there is an absoluteOrderingList specified, then make sure that
         * any ServletContainerInitializers included in fragment JARs 
         * NOT listed in the absoluteOrderingList will be ignored.
         * For this, we remove any unwanted fragment JARs from the class
         * loader's URL
         */
        if((absoluteOrderingList != null) && !hasOthers) {
            if(!(cl instanceof URLClassLoader)) {
                log.log(Level.WARNING,
                    "servletContainerInitializerUtil.wrongClassLoaderType",
                    cl.getClass().getCanonicalName());
                return null;
            }
            URLClassLoader webAppCl = (URLClassLoader) cl;

            // Create a new List of URLs with missing fragments removed from
            // the currentUrls
            ArrayList<URL> newClassLoaderUrlList = new ArrayList<URL>();
            for (URL classLoaderUrl : webAppCl.getURLs()) {
                // Check that the URL is using file protocol, else ignore it
                if (!"file".equals(classLoaderUrl.getProtocol())) {
                    continue;
                }
                File file = new File(Util.URLDecode(classLoaderUrl.getFile()));
                try {
                    file = file.getCanonicalFile();
                } catch (IOException e) {
                    // Ignore
                }
                if (!file.exists()) {
                    continue;
                }
                String path = file.getAbsolutePath();
                if (!path.endsWith(".jar")) {
                    continue;
                }
                if (!isFragmentMissingFromAbsoluteOrdering(file.getName(),
                        webFragmentMap, absoluteOrderingList)) {
                    newClassLoaderUrlList.add(classLoaderUrl);
                }
            }

            // Create temporary classloader for ServiceLoader#load
            // TODO: Have temporary classloader honor delegate flag from
            // sun-web.xml
            URL[] urlsForNewClassLoader =
                new URL[newClassLoaderUrlList.size()];
            cl = new URLClassLoader(newClassLoaderUrlList.toArray(
                                        urlsForNewClassLoader),
                                    webAppCl.getParent());
        }

        return ServiceLoader.load(ServletContainerInitializer.class, cl);
    }


    /**
     * Builds a mapping of classes to the list of ServletContainerInitializers
     * interested in them
     *
     * @param initializers an Iterable over all ServletContainerInitializers
     * that need to be considered
     *
     * @return Mapping of classes to list of ServletContainerInitializers
     * interested in them
     */
    public static Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> getInterestList(Iterable<ServletContainerInitializer> initializers) {

        if (null == initializers) {
            return null;
        }

        Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList = null;

        // Build a list of the classes / annotations in which the
        // initializers are interested
        for (ServletContainerInitializer sc : initializers) {
            if(interestList == null) {
                interestList = new HashMap<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>>();
            }
            Class sciClass = sc.getClass();
            HandlesTypes ann = (HandlesTypes) sciClass.getAnnotation(HandlesTypes.class);
            if(ann == null) {
                // This initializer does not contain @HandlesTypes
                // This means it should always be called for all web apps
                // So map it with a special token
                ArrayList<Class<? extends ServletContainerInitializer>> currentInitializerList =
                        interestList.get(ServletContainerInitializerUtil.class);
                if(currentInitializerList == null) {
                    ArrayList<Class<? extends ServletContainerInitializer>> arr =
                            new ArrayList<Class<? extends ServletContainerInitializer>>();
                    arr.add(sciClass);
                    interestList.put(ServletContainerInitializerUtil.class, arr);
                } else {
                    currentInitializerList.add(sciClass);
                }
            } else {
                Class[] interestedClasses = ann.value();
                if( (interestedClasses != null) && (interestedClasses.length != 0) ) {
                    for(Class c : interestedClasses) {
                        ArrayList<Class<? extends ServletContainerInitializer>> currentInitializerList =
                                interestList.get(c);
                        if(currentInitializerList == null) {
                            ArrayList<Class<? extends ServletContainerInitializer>> arr =
                                    new ArrayList<Class<? extends ServletContainerInitializer>>();
                            arr.add(sciClass);
                            interestList.put(c, arr);
                        } else {
                            currentInitializerList.add(sciClass);
                        }
                    }
                }
            }
        }

        return interestList;
    }

    /**
     * Given an interestlist that was built above, and a class loader, scan the entire web app's classes and libraries
     * looking for classes that extend/implement/use the annotations of a class present in the interest list
     *
     * @param initializers Iterable over all ServletContainerInitializers that
     * were discovered
     * @param interestList The interestList built by the previous util method
     * @param cl The classloader to be used to load classes in WAR
     * @return Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>>
     *                          A Map of ServletContainerInitializer classes to be called and arguments to be passed
     *                          to them
     */
    public  static Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> getInitializerList(
            Iterable<ServletContainerInitializer> initializers,
            Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList,
            ClassLoader cl) {

        if (interestList == null) {
            return null;
        }

        // This contains the final list of initializers and the set of
        // classes to be passed to them as arg
        Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> initializerList = null;

        // If an initializer was present without any @HandleTypes, it 
        // must be called with a null set of classes
        if(interestList.containsKey(ServletContainerInitializerUtil.class)) {
            initializerList = new HashMap<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>>();
            ArrayList<Class<? extends ServletContainerInitializer>> initializersWithoutHandleTypes =
                    interestList.get(ServletContainerInitializerUtil.class);
            for(Class c : initializersWithoutHandleTypes) {
                initializerList.put(c, null);
            }
        }

        /*
         * Now scan every class in this app's WEB-INF/classes and WEB-INF/lib
         * to see if any class uses the annotation or extends/implements a
         * class in our interest list.
         * Do this scanning only if we have ServletContainerinitializers that
         * have expressed specific interest
         */
        if( (interestList.keySet().size() > 1) ||
            ((interestList.keySet().size() == 1) &&
                    (!interestList.containsKey(ServletContainerInitializerUtil.class)))) {
            /*
             * Create an instance of ClassDependencyBuilder that looks at the byte code and keeps
             * the information for every class in this app
             *
             */
            ClassDependencyBuilder classInfo = new ClassDependencyBuilder();
            for(URL u : ((URLClassLoader)cl).getURLs()) {
                String path = Util.URLDecode(u.getPath());
                try {
                    if(path.endsWith(".jar")) {
                        JarFile jf = new JarFile(path);
                        try {
                            Enumeration<JarEntry> entries = jf.entries();
                            while(entries.hasMoreElements()) {
                                JarEntry anEntry = entries.nextElement();
                                if(anEntry.isDirectory())
                                    continue;
                                if(!anEntry.getName().endsWith(".class"))
                                    continue;
                                InputStream jarInputStream = null;
                                try {
                                    jarInputStream = jf.getInputStream(anEntry);
                                    int size = (int) anEntry.getSize();
                                    byte[] classData = new byte[size];
                                    for(int bytesRead = 0; bytesRead < size;) {
                                        int r2 = jarInputStream.read(classData, bytesRead, size - bytesRead);
                                        bytesRead += r2;
                                    }
                                    classInfo.loadClassData(classData);
                                } catch (Throwable t) {
                                    if (log.isLoggable(Level.FINE)) {
                                        log.log(Level.FINE,
                                            "servletContainerInitializerUtil.classLoadingError",
                                            new Object[] {
                                                anEntry.getName(),
                                                t.toString()});
                                    }
                                    continue;
                                } finally {
                                    if(jarInputStream != null) {
                                        jarInputStream.close();
                                    }
                                }
                            }
                        } finally {
                            jf.close();
                        }
                    } else {
                        File file = new File(path);
                        if (file.exists()) {
                            if (file.isDirectory()) {
                                scanDirectory(file, classInfo);
                            } else {
                                log.log(Level.WARNING,
                                    "servletContainerInitializerUtil.invalidUrlClassLoaderPath",
                                    path);
                            }
                        }
                    }
                } catch(IOException ioex) {
                    String msg = rb.getString(
                        "servletContainerInitializerUtil.ioError");
                    msg = MessageFormat.format(msg,
                        new Object[] { path });
                    log.log(Level.SEVERE, msg, ioex);
                    return null;
                }
            }
            initializerList = checkAgainstInterestList(classInfo, interestList, initializerList, cl);
        }

        /*
         * If a ServletContainerInitializer was annotated with HandlesTypes,
         * but none of the application classes match, we must still invoke
         * it at its onStartup method, passing in a null Set of classes
         */ 
        for (ServletContainerInitializer initializer : initializers) {
            if (!initializerList.containsKey(initializer.getClass())) {
                initializerList.put(initializer.getClass(), null);
            }
        }

        return initializerList;
    }

    /**
     * Checks if a given JAR file is to be excluded while searching for ServletContainerInitializer implementations
     * @param jarName the JAR file
     * @param webFragmentMap fragment information from deployment desc
     * @param absoluteOrderingList give ordering list
     * @return true if the given JAR file is NOT present in the absolute ordering list
     */
    private static boolean isFragmentMissingFromAbsoluteOrdering(
           String jarName, Map<String, String> webFragmentMap,
           List<String> absoluteOrderingList) {
       return (webFragmentMap != null &&
           absoluteOrderingList != null &&
           !absoluteOrderingList.contains(
               webFragmentMap.get(jarName)));
   }

    /**
     * Given a directory, scan all sub directories looking for classes and
     * build the interest list
     *
     * @param dir the directory to be scanned
     * @param classInfo the ClassDependencyBuilder that holds info on all classes
     */
    private static void scanDirectory(File dir, ClassDependencyBuilder classInfo) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getPath();
                if (fileName.endsWith(".class")) {
                    try {
                        byte[] classData = null;
                        InputStream is = null;
                        try {
                            is = new FileInputStream(fileName);
                            int size = is.available();
                            classData = new byte[size];
                            is.read(classData);
                            classInfo.loadClassData(classData);
                        } finally {
                            if (is != null) {
                                is.close();
                            }
                        }
                    } catch (Throwable t) {
                        if (log.isLoggable(Level.WARNING)) {
                            log.log(Level.WARNING,
                                "servletContainerInitializerUtil.classLoadingError",
                                new Object[] {fileName, t.toString()});
                        }
                        continue;
                    }
                }
            } else {
                scanDirectory(file, classInfo);
            }
        }
        return;
    }

    /**
     * Given the interestList, checks if a given class uses any of the
     * annotations; If so, builds the initializer list
     *
     * @param classInfo the ClassDependencyBuilder instance that holds info on all classes
     * @param interestList the interestList built earlier
     * @param initializerList the initializerList built so far
     * @param cl the ClassLoader to be used to load the class
     * @return the updated initializer list
     */
    private static Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> checkAgainstInterestList(
                                ClassDependencyBuilder classInfo,
                                Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList,
                                Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> initializerList,
                                ClassLoader cl) {
        for(Class c : interestList.keySet()) {
            Set<String> resultFromClassInfo = classInfo.computeResult(c.getName());
            if(resultFromClassInfo.isEmpty()) {
                continue;
            }
            HashSet<Class<?>> resultSet = new HashSet<Class<?>>();
            for(Iterator<String> iter = resultFromClassInfo.iterator(); iter.hasNext();) {
                String className = iter.next().replace('/', '.');
                try {
                    Class aClass = cl.loadClass(className);
                    resultSet.add(aClass);
                } catch (Throwable t) {
                    if (log.isLoggable(Level.WARNING)) {
                        log.log(Level.WARNING,
                            "servletContainerInitializerUtil.classLoadingError",
                            new Object[] {className, t.toString()});
                    }
                }
            }
            if(initializerList == null) {
                initializerList = new HashMap<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>>();
            }
            ArrayList<Class<? extends ServletContainerInitializer>> containerInitializers = interestList.get(c);
            for(Class<? extends ServletContainerInitializer> initializer : containerInitializers) {
                HashSet<Class<?>> classSet = initializerList.get(initializer);
                if(classSet == null) {
                    classSet = new HashSet<Class<?>>();
                }
                classSet.addAll(resultSet);
                initializerList.put(initializer, classSet);
            }
        }
        return initializerList;
    }
}
