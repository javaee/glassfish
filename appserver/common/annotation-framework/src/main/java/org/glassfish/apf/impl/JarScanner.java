/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.apf.impl;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashSet;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import org.glassfish.apf.Scanner;

/**
 * Implements the scanner interface on a jar file.
 *
 * @author Jerome Dochez
 */
public class JarScanner extends JavaEEScanner implements Scanner<Object> {
    
    File jarFile;
    Set<JarEntry> entries = new HashSet<JarEntry>();
    ClassLoader classLoader = null;
    
    
    public  void process(File jarFile, Object bundleDesc, ClassLoader loader) throws java.io.IOException {
        this.jarFile = jarFile;
        JarFile jf = new JarFile(jarFile);
        
        try {
            Enumeration<JarEntry> entriesEnum = jf.entries();
            while(entriesEnum.hasMoreElements()) {
                JarEntry je = entriesEnum.nextElement();
                if (je.getName().endsWith(".class")) {
                    entries.add(je);
                }
            }        
        } finally {
            jf.close();
        }
        initTypes(jarFile);
    }    
    
    public ClassLoader getClassLoader() {
        if (classLoader==null) {
            final URL[] urls = new URL[1];
            try {
                if (jarFile == null) throw new IllegalStateException("jarFile must first be set with the process method.");
                urls[0] = jarFile.getAbsoluteFile().toURL();
                classLoader = new PrivilegedAction<URLClassLoader>() {
                  @Override
                  public URLClassLoader run() {
                    return new URLClassLoader(urls);
                  }
                }.run();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return classLoader;
    }
    
    public Set<Class> getElements() {
        
        
        Set<Class> elements = new HashSet<Class>();
        if (getClassLoader()==null) {
            AnnotationUtils.getLogger().severe("Class loader null");
            return elements;
        }        
        for (JarEntry je : entries) {
            String fileName = je.getName();
            // convert to a class name...
            String className = fileName.replace(File.separatorChar, '.');
            className = className.substring(0, className.length()-6);
            try {                
                elements.add(classLoader.loadClass(className));
                
            } catch(ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        return elements;
    }
    


}
