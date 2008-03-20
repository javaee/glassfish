/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.module.impl;

import java.net.URLClassLoader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.io.IOException;

/**
 * ClassLoaderProxy capable of loading classes from itself but also from other class loaders
 *
 * @author Jerome Dochez
 */
public class ClassLoaderProxy extends URLClassLoader {

    private final List<ClassLoader> surrogates = new CopyOnWriteArrayList<ClassLoader>();
    private final List<ClassLoaderFacade> facadeSurrogates = new CopyOnWriteArrayList<ClassLoaderFacade>();

    /** Creates a new instance of ClassLoader */
    public ClassLoaderProxy(URL[] shared, ClassLoader parent) {
        super(shared, parent);
    }

    protected void finalize() throws Throwable {
        super.finalize();
        stop();
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve, boolean followImports)
            throws ClassNotFoundException {
        // First, check if the class has already been loaded
        Class c = findLoadedClass(name);
        if (c == null) {
            try {
                if (getParent() != null) {
                    c = getParent().loadClass(name);
                }
            } catch (ClassNotFoundException e) {

            }
            if (c == null) {
                c = findClass(name, followImports);
            }
            if (resolve) {
                resolveClass(c);
            }
        } else {
            if (c.getClassLoader() == this) {
                return c;
            } else throw new ClassNotFoundException(name);
        }

        return c;
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }

    protected Class<?> findClass(String name, boolean followImports) throws ClassNotFoundException {
        try {
            // try to find it within this module first.
            // this potentially causes a problem when two modules have the same jar in the classpath,
            // but because the classes are most often found locally, this has a tremendous performance boost.
            // so we knowingly make this decision to do child-first loading.

            // the pain of duplicate jars are somewhat mitigated by the fact that dependencies tend to be
            // defined between HK2 modules, and those will not show up in the classpath of this module.
            return findClassDirect(name);
        } catch(ClassNotFoundException cfne) {
            if (followImports) {
                Class c=null;
                for (ClassLoaderFacade classLoader : facadeSurrogates) {
                    try {
                        c = classLoader.getClass(name);
                    } catch(ClassNotFoundException e) {
                        // ignored.
                    }
                    if (c!=null) {
                        return c;
                    }
                }
                for (ClassLoader classLoader : surrogates) {
                    try {
                        c = classLoader.loadClass(name);
                    } catch(ClassNotFoundException e) {
                        // ignored.
                    }
                    if (c!=null) {
                        return c;
                    }
                }
            }
            throw cfne;
        }
    }

    /**
     * {@link #findClass(String)} except the classloader punch-in hack.
     */
    /*package*/ Class findClassDirect(String name) throws ClassNotFoundException {
        Class c = findLoadedClass(name);
        if(c!=null) return c;
        try {
            return super.findClass(name);
        } catch (NoClassDefFoundError e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

    public URL findResource(String name) {
        URL url = super.findResource(name);
        if (url!=null)  return url;

        for (ClassLoaderFacade classLoader : facadeSurrogates) {
            url = classLoader.findResourceDirect(name);
            if (url!=null) {
                return url;
            }
        }
        for (ClassLoader classLoader : surrogates) {
            url = classLoader.getResource(name);
            if (url!=null) {
                return url;
            }
        }
        return null;
    }

    /**
     * Works like {@link #findResource(String)} but only looks at
     * this module, without delegating to ancestors.
     */
    public URL findResourceDirect(String name) {
        return super.findResource(name);
    }

    public Enumeration<URL> findResources(String name) throws IOException {
        // TODO: this is broken. We need to enumerate all of them, not just the first one discovered.
        Enumeration<URL> enumerat = super.findResources(name);
        if (enumerat!=null && enumerat.hasMoreElements()) {
             return enumerat;
        }
        for (ClassLoaderFacade classLoader : facadeSurrogates) {
            enumerat = classLoader.getResources(name);
            if (enumerat!=null && enumerat.hasMoreElements()) {
                return enumerat;
            }
        }
        for (ClassLoader classLoader : surrogates) {
            enumerat = classLoader.getResources(name);
            if (enumerat!=null && enumerat.hasMoreElements()) {
                return enumerat;
            }

        }
        return enumerat;
    }

    public void addDelegate(ClassLoader cl) {
        if (cl instanceof ClassLoaderFacade) {
            facadeSurrogates.add((ClassLoaderFacade) cl);
        } else {
            surrogates.add(cl);
        }
    }

    public void removeDelegate(ClassLoader cl) {
        if (cl instanceof ClassLoaderFacade) {
            facadeSurrogates.remove(cl);
        } else {
            surrogates.remove(cl);
        }
    }

    public Collection<ClassLoader> getDelegates() {
        return new ArrayList<ClassLoader>(surrogates);
    }


    /**
     * called by the facade class loader when it is garbage collected.
     * this is a good time to see if this module should be unloaded.
     */
    public void stop() {
       surrogates.clear();
       facadeSurrogates.clear();
    }

    public String toString() {
        StringBuffer s= new StringBuffer();
        s.append(",URls[]=");
        for (URL url : getURLs()) {
            s.append(url).append(",");
        }
        s.append(")");

        for (ClassLoader surrogate : surrogates) {
            s.append("\n ref : ").append(surrogate.toString());
        }
        return s.toString();
    }              
}
