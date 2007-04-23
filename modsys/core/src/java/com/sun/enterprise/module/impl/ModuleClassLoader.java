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

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleState;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 *
 * @author dochez
 */
public class ModuleClassLoader extends URLClassLoader {
    
    private final List<ClassLoader> surrogates = Collections.synchronizedList(new ArrayList<ClassLoader>());
    private final List<ClassLoaderFacade> facadeSurrogates = Collections.synchronizedList(new ArrayList<ClassLoaderFacade>());
    private final Module module;

    /**
     * Module will be initialized when this classloader is consulted for the first time.
     */
    private volatile boolean initialized = false;
    private StackTraceElement[] initializerThread;
    private String initializerClassName;

    /** Creates a new instance of ClassLoader */
    public ModuleClassLoader(Module owner, URL[] shared, ClassLoader parent) {
        super(shared, parent);
        this.module = owner;
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        Utils.getDefaultLogger().info("ModuleClassLoader gc'ed " + module.getModuleDefinition().getName());
        stop();
    }


    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        initialize(name);
        return super.loadClass(name, resolve);
    }

    public URL getResource(String name) {
        initialize(name);
        return super.getResource(name);
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        initialize(name);
        return super.getResources(name);
    }

    /**
     * Lazily initialize this module if not done so yet.
     */
    private void initialize(String name) {
        if (initialized)    return;

        synchronized(this) {
            if(!initialized) {
                // if we are validating, we should just not initiate initialization. 
                if (module.getState().equals(ModuleState.VALIDATING)) {
                    return;
                }
                initialized = true;

                module.start();
                // remember who started us to assist debugging.
                initializerThread = Thread.currentThread().getStackTrace();
                initializerClassName = name;
            }
        }
    }

    /*package*/ void dumpState(PrintStream writer) {
        if (initializerThread!=null) {
            writer.println("Initialized when " + initializerClassName + " was requested by :");
            for (StackTraceElement e : initializerThread) {
                writer.println(e.toString());
            }
        }
    }


    /**
     * called by the facade class loader when it is garbage collected. 
     * this is a good time to see if this module should be unloaded.
     */
    public void stop() {
        
        // we should only detach if the sticky flag is not set
        if (!module.isSticky()) {
            
            Utils.getDefaultLogger().info("ModuleClassLoader stopped " + module.getModuleDefinition().getName());
            surrogates.clear();
            facadeSurrogates.clear();
            module.stop();
        }
    }
        
    public Module getOwner() {
        return module;
    }

    
    protected Class<?> findClass(String name) throws ClassNotFoundException {
       
        Class c = null;
        synchronized(facadeSurrogates) {
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
        }
        synchronized(surrogates) {
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
        return super.findClass(name);
    }
     
    public URL findResource(String name) {
        synchronized(facadeSurrogates) {
            for (ClassLoaderFacade classLoader : facadeSurrogates) {

                URL url = classLoader.findResource(name);
                if (url!=null) {
                    return url;
                }
            }
        }
        synchronized(surrogates) {
            for (ClassLoader classLoader : surrogates) {

                URL url = classLoader.getResource(name);
                if (url!=null) {
                    return url;
                }
            }
        }
        return super.findResource(name);
        
    }
    
    public Enumeration<URL> findResources(String name) throws IOException {

        for (ClassLoaderFacade classLoader : facadeSurrogates) {
            
            Enumeration<URL> enumerat = classLoader.getResources(name);
            if (enumerat!=null && enumerat.hasMoreElements()) {
                return enumerat;
            }
        }
        for (ClassLoader classLoader : surrogates) {
            Enumeration<URL> enumerat = classLoader.getResources(name);
            if (enumerat!=null && enumerat.hasMoreElements()) {
                return enumerat;
            }
            
        }
        return super.findResources(name);
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
    
    public String toString() {
        StringBuffer s= new StringBuffer(); 
        s.append(super.toString()).append("(name=").append(module.getName());
        s.append(",init=").append(initialized);
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
