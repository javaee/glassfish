/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2015 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.module.impl;

import com.sun.enterprise.module.common_impl.LogHelper;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.ArrayList;

/**
 * Facade for {@link ModuleClassLoader} to only expose public classes.
 *
 * @author dochez
 */
final class ClassLoaderFacade extends URLClassLoader {
 
    private final static URL[] EMPTY_URLS = new URL[0];
    private HashSet<String> publicPkgs = null;
    private ArrayList<String> publicSet = null;
    private ModuleClassLoader privateLoader;
    private int classesLoaded = 0;

    /** Creates a new instance of ClassLoaderFacade */
    public ClassLoaderFacade(ModuleClassLoader privateLoader) {
        super(EMPTY_URLS, privateLoader.getParent());
        this.privateLoader = privateLoader;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        LogHelper.getDefaultLogger().fine("Facade ClassLoader killed " + privateLoader.getOwner().getModuleDefinition().getName());
        privateLoader.stop();
    }

    public void setPublicPkgs(String[] publicPkgs) {
        
        if (publicPkgs==null || publicPkgs.length==0)
            return;
        
        for (String publicPkg : publicPkgs) {
            if (publicPkg.endsWith(".")) {
                if (publicSet==null) {
                    publicSet = new ArrayList<String>();
                }
                publicSet.add(publicPkg);
            } else {
                if (this.publicPkgs==null) {
                    this.publicPkgs = new HashSet<String>();
                }
                this.publicPkgs.add(publicPkg);
            }
        }
    }
    
    public String[] getPublicPkgs() {
        return publicPkgs.toArray(new String[publicPkgs.size()]);
    }
    
    boolean matchExportedPackage(String name) {
        if (publicPkgs==null && publicSet==null) {
            return true;
        }
        if (publicSet!=null) {
            for (String aPublicSet : publicSet) {
                if (name.startsWith(aPublicSet)) {
                    return true;
                 }
            }
        }
        if (publicPkgs==null) {
            return false;
        }
        int index = name.lastIndexOf('.');
        if (index==-1) {
            return false;
        }
        String packageName = name.substring(0, index);
        return publicPkgs.contains(packageName);
    }
              
    protected Class findClass(String name) throws ClassNotFoundException {
        if (matchExportedPackage(name)) {
            return privateLoader.loadClass(name);
        } 
        throw new ClassNotFoundException(name);
    }
    
    public Enumeration<URL> findResources(String name) throws IOException {
        return privateLoader.findResources(name);
    }
    
    public URL findResource(String name) {
        return privateLoader.findResource(name);
    }  
    
/*    public Enumeration<URL> getResources(String name) throws IOException {
        
        return privateLoader.getResources(name);    
    }
    
    public URL getResource(String name) {
        
        return privateLoader.getResource(name);    
    }    
    
*/                
/*    public Class loadClass(String name) throws ClassNotFoundException {
        if (matchExportedPackage(name)) {
            if (!initialized) {
                initialize(name);
            }
            Class c = privateLoader.loadClass(name);
            if (c!=null) {
                classesLoaded++;
            } 
            return c;
                
        } 
        throw new ClassNotFoundException(name);        
    }
*/

    /**
     * Tries to find a class from the {@link ModuleClassLoader} that this facade is wrapping,
     * without doing further delegation to ancestors.
     */
    Class getClass(String name) throws ClassNotFoundException {
        if (matchExportedPackage(name)) {
            Class c = privateLoader.findClassDirect(name);
            classesLoaded++;
            return c;
        }
        return null;
        
    }

    /**
     * Works like {@link #findResource(String)} but only looks at
     * this module, without delegating to ancestors.
     */
    URL findResourceDirect(String name) {
        return privateLoader.findResourceDirect(name);
    }

    public void dumpState(PrintStream writer) {
        privateLoader.dumpState(writer);
        writer.println("Nb of classes loaded " + classesLoaded);
    }
    
    public String toString() {
        return super.toString() + " Facade for " + privateLoader.toString();
    }

}
