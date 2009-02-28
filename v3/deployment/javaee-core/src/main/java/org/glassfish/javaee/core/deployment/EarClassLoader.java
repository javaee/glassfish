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
package org.glassfish.javaee.core.deployment;

import java.net.URLClassLoader;
import java.net.URL;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;

/**
 * Simplistic class loader which will delegate to each module class loader in the order
 * they were added to the instance
 *
 * @author Jerome Dochez
 */
public class EarClassLoader extends URLClassLoader {

    private final List<ClassLoaderHolder> delegates = new LinkedList<ClassLoaderHolder>();
    private final Method findClass;
    private final Method findResource;
    private final Method findResources;
    private final Map<String, Class> classes = new HashMap<String, Class>();

    // optimization flag to not check the parent if we don't have library jars
    private final boolean checkParent;

    public EarClassLoader(URL[] urls, ClassLoader classLoader) {
        super(urls, classLoader);
        checkParent = urls!=null && urls.length>0;
        try {
            findClass = ClassLoader.class.getDeclaredMethod("findClass", new Class[] {String.class});
            findClass.setAccessible(true);

            findResource = ClassLoader.class.getDeclaredMethod("findResource", new Class[] {String.class});
            findResource.setAccessible(true);

            findResources = ClassLoader.class.getDeclaredMethod("findResources", new Class[] {String.class});
            findResources.setAccessible(true);
            
        } catch(NoSuchMethodException e) {
            // this is impossible.
            throw new RuntimeException(e);
        }
    }

    public void addModuleClassLoader(String moduleName, ClassLoader cl) {
        delegates.add(new ClassLoaderHolder(moduleName, cl));
    }

    public ClassLoader getModuleClassLoader(String moduleName) {
        for (ClassLoaderHolder clh : delegates) {
            if (moduleName.equals(clh.moduleName)) {
                return clh.loader;
            }
        }
        return null;
    }

    @Override
    protected Class<?> findClass(String s) throws ClassNotFoundException {
        
        if (classes.containsKey(s)) {
            return classes.get(s);
        }
        
        if (checkParent) {
            try {
                return super.findClass(s);
            } catch(ClassNotFoundException e) {
                // ignore
            }
        }

        for (ClassLoaderHolder clh : delegates) {
            try {
                Class<?> clazz = (Class<?>) findClass.invoke(clh.loader, s);
                if (clazz!=null) {
                    classes.put(s, clazz);
                    return clazz;
                }
            } catch(IllegalAccessException e) {
                
            } catch(InvocationTargetException e) {
                // not found most likely.   
            }
        }
        throw new ClassNotFoundException(s);
    }

    private class ClassLoaderHolder {
        final ClassLoader loader;
        final String moduleName;

        private ClassLoaderHolder(String moduleName, ClassLoader loader) {
            this.loader = loader;
            this.moduleName = moduleName;
        }
    }

    @Override
    public URL findResource(String s) {
        URL url = null;
        if (checkParent) {
            url = super.findResource(s);
            if (url!=null) {
                return url;
            }
        }
        for(ClassLoaderHolder clh : delegates) {
            try {
                url = (URL) findResource.invoke(clh.loader, s);
            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
            if (url!=null) {
                return url;
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> findResources(String s) throws IOException {
        if (checkParent) {
            Enumeration<URL> result = super.findResources(s);
            if (result!=null) {
                return result;
            }
        }
        Vector<URL> urls = new Vector<URL>();
        for(ClassLoaderHolder clh : delegates) {
            try {
                Enumeration<URL> enumeration = (Enumeration<URL>) findResources.invoke(clh.loader, s);
                while (enumeration.hasMoreElements()) {
                    urls.add(enumeration.nextElement());
                }
            } catch (IllegalAccessException e) {

            } catch (InvocationTargetException e) {

            }
        }
        return urls.elements();
    }
}
