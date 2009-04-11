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
 *
 *
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.glassfish.web.loader;

import com.sun.logging.LogDomains;

import java.util.*;
import java.util.logging.Logger;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.annotation.HandlesTypes;

/**
 *  Utility class - contains util methods used for implementation of pluggable Shared Library features
 */
public class ServletContainerInitializerUtil {

    private static Logger log = LogDomains.getLogger(ServletContainerInitializerUtil.class, LogDomains.WEB_LOGGER);
    private static final StringManager sm =
        StringManager.getManager(ServletContainerInitializerUtil.class.getPackage().getName());

    public ServletContainerInitializerUtil() {}

    // Given a class loader, check for ServletContainerInitializer implementations in any JAR file in this classpath,
    // and build an interest list of which initializer is interested in what class implementations / annotations
    public static Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> getInterestList(ClassLoader cl) {
        ServiceLoader<ServletContainerInitializer> frameworks =
                ServiceLoader.load(ServletContainerInitializer.class, cl);
        Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList = null;
        // Build a list of the classes / annotations in which the initializers are interested in
        for(ServletContainerInitializer sc : frameworks) {
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

    // Given an interestlist that was built above, and a class loader, scan the entire web app's classes and libraries
    // looking for classes that extend/implement/use the annotations of a class present in the interest list
    public  static Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> getInitializerList(
            Map<Class<?>, ArrayList<Class<? extends ServletContainerInitializer>>> interestList,
            ClassLoader cl) {
        if(interestList == null)
            return null;
        
        // This contains the final list of initializers and the set of classes to be passed to them as arg
        Map<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>> initializerList = null;

        //If an initializer was present without @HandleTypes, that initializer should always be called
        if(interestList.containsKey(ServletContainerInitializerUtil.class)) {
            initializerList = new HashMap<Class<? extends ServletContainerInitializer>, HashSet<Class<?>>>();
            ArrayList<Class<? extends ServletContainerInitializer>> initializersWithoutHandleTypes =
                    interestList.get(ServletContainerInitializerUtil.class);
            for(Class c : initializersWithoutHandleTypes) {
                initializerList.put(c, null);
            }
        }

        //Now scan every class in this app's WEB-INF/classes and WEB-INF/lib to see if any class
        //uses the annotation or extends/implements a class in our interest list
        //Do this scanning only if we have ServletContainerinitializers that have expressed specific interest
        if( (interestList.keySet().size() > 1) ||
            ((interestList.keySet().size() == 1) && (!interestList.containsKey(ServletContainerInitializerUtil.class)))) {
            for(URL u : ((URLClassLoader)cl).getURLs()) {
                String path = u.getPath();
                try {
                    if(path.endsWith(".jar")) {
                        JarFile jf = new JarFile(path);
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
                                log.warning(sm.getString("ServletContainerInitializerUtil.CNFWarning", anEntry.getName()));
                                continue;
                            }
                        }
                    } else {
                        File file = new File(path);
                        if(file.isDirectory()) {
                            initializerList = scanDirectory(file, path, cl, interestList, initializerList);
                        } else {
                            log.warning(sm.getString("ServletContainerInitializerUtil.UnkownFileWarning", path));
                        }
                    }
                } catch(IOException ioex) {
                    log.severe(sm.getString("ServletContainerInitializerUtil.IOerror", ioex.getLocalizedMessage()));
                    return null;
                }
            }
        }
        return initializerList;
    }

    private static String getClassNameFromPath(String fullPath, String path) {
        String className = fullPath.substring(path.length());
        className = className.replace(File.separatorChar, '.');
        className = className.substring(0, className.length()-6);
        return className;
    }

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
                        log.warning(sm.getString("ServletContainerInitializerUtil.CNFWarning", fileName));
                        continue;
                    }
                }
            } else {
                initializerList = scanDirectory(file, path, cl, interestList, initializerList);
            }
        }
        return initializerList;
    }

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
