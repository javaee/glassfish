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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.*;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.annotation.HandlesTypes;
import com.sun.logging.LogDomains;
import org.apache.naming.Util;

/**
 * Utility class - contains util methods used for implementation of
 * pluggable Shared Library features
 *
 *  @author Vijay Ramachandran
 */
public class ServletContainerInitializerUtil {

    private static final Logger log = LogDomains.getLogger(
        ServletContainerInitializerUtil.class, LogDomains.WEB_LOGGER);

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
            URL[] currentUrlsInClassLoader = webAppCl.getURLs();
            ArrayList<URL> newClassLoaderUrlList = new ArrayList<URL>();
            for (int i=0; i<currentUrlsInClassLoader.length; i++) {
                // Check that the URL is using file protocol, else ignore it
                if (!"file".equals(currentUrlsInClassLoader[i].getProtocol())) {
                    continue;
                }
                File file = new File(Util.URLDecode(
                    currentUrlsInClassLoader[i].getFile()));
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
                    newClassLoaderUrlList.add(currentUrlsInClassLoader[i]);
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
            HandlesTypes ann = sc.getClass().getAnnotation(HandlesTypes.class);
            if(ann == null) {
                // This initializer does not contain @HandlesTypes
                // This means it should always be called for all web apps
                // So map it with a special token
                ArrayList<Class<? extends ServletContainerInitializer>> currentInitializerList =
                        interestList.get(ServletContainerInitializerUtil.class);
                if(currentInitializerList == null) {
                    ArrayList<Class<? extends ServletContainerInitializer>> arr =
                            new ArrayList<Class<? extends ServletContainerInitializer>>();
                    arr.add(sc.getClass());
                    interestList.put(ServletContainerInitializerUtil.class, arr);
                } else {
                    currentInitializerList.add(sc.getClass());
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
                            arr.add(sc.getClass());
                            interestList.put(c, arr);
                        } else {
                            currentInitializerList.add(sc.getClass());
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
            for(URL u : ((URLClassLoader)cl).getURLs()) {
                String path = u.getPath();
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
                                try {
                                    String className = anEntry.getName().replace('/', '.');
                                    className = className.substring(0, className.length()-6);
                                    Class aClass = cl.loadClass(className);
                                    initializerList = checkAgainstInterestList(aClass, interestList, initializerList);
                                } catch (ClassNotFoundException e) {
                                    log.log(Level.WARNING,
                                        "servletContainerInitializerUtil.cnfWarning",
                                        anEntry.getName());
                                    continue;
                                }
                            }
                        } finally {
                            jf.close();
                        }
                    } else {
                        File file = new File(path);
                        if(file.isDirectory()) {
                            initializerList = scanDirectory(file, path, cl,
                                interestList, initializerList);
                        } else {
                            log.log(Level.WARNING,
                                "servletContainerInitializerUtil.unkownFileWarning",
                                path);
                        }
                    }
                } catch(IOException ioex) {
                    log.log(Level.SEVERE,
                        "servletContainerInitializerUtil.ioError",
                        ioex);
                    return null;
                }
            }
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
     * Given a path (say /Users/user/glassfish/domains/.../WEB-INF/classes/com/sun/x.class) and the topmost directory
     * (/Users/user/glassfish/domains/.../WEB-INF/classes/) returns the class name (com.sun.x)
     *
     * @param fullPath String representing complete path of the class
     * @param top String representing the complete path of topmost directory
     *
     * @return the class name as mentioned in the example above
     */
    private static String getClassNameFromPath(String fullPath, String top) {
        //We got the  path from Class Loader
        //which has path in the form /x/y/z. On Solaris/Linux/Mac, the path
        //obtained File is also /x/y/z but on windows it D:\\x\\y\\z
        //To ensure this code works on all platforms in the same way,
        //We get a File representation of path and then do the calculation
        String className = fullPath.substring((new File(top)).getPath().length()+File.separator.length());
        className = className.replace(File.separatorChar, '.');
        className = className.substring(0, className.length()-6);
        return className;
    }

    /**
     * Given a directory, scan all sub directories looking for classes and
     * build the interest list
     *
     * @param dir the directory to be scanned
     * @param path topmost directory from which scanning started
     * @param cl the classloader to be used
     * @param interestList The interestList built earlier
     * @param initializerList The initializerList built so far
     * @return the updated initialiserList
     */
    private static Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> scanDirectory(
                                 File dir, String path, ClassLoader cl,
                                 Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList,
                                 Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> initializerList) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getPath();
                if (fileName.endsWith(".class")) {
                    try {
                        Class aClass = cl.loadClass(getClassNameFromPath(fileName, path));
                        initializerList = checkAgainstInterestList(aClass, interestList, initializerList);
                    } catch (ClassNotFoundException e) {
                        log.log(Level.WARNING,
                            "servletContainerInitializerUtil.cnfWarning",
                            fileName);
                        continue;
                    }
                }
            } else {
                initializerList = scanDirectory(file, path, cl, interestList, initializerList);
            }
        }
        return initializerList;
    }

    /**
     * Given the interestList, checks if a given class uses any of the
     * annotations; If so, builds the initializer list
     *
     * @param aClass the class to be examined
     * @param interestList the interestList built earlier
     * @param initializerList the initializerList built so far
     * @return the updated initializer list
     */
    private static Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> checkAgainstInterestList(
                                Class aClass,
                                Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList,
                                Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> initializerList) {
        for(Class c : interestList.keySet()) {
            if((aClass.getAnnotation(c) != null) || (c.isAssignableFrom(aClass)) ) {
                if(initializerList == null) {
                    initializerList = new HashMap<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>>();
                }
                ArrayList<Class<? extends ServletContainerInitializer>> containerInitializers = interestList.get(c);
                for(Class<? extends ServletContainerInitializer> initializer : containerInitializers) {
                    HashSet<Class<?>> classSet = initializerList.get(initializer);
                    if(classSet == null) {
                        classSet = new HashSet<Class<?>>();
                    }
                    classSet.add(aClass);
                    initializerList.put(initializer, classSet);
                }
            }
        }
        return initializerList;
    }
}
