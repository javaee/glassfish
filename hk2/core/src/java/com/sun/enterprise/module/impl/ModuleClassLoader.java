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
import com.sun.enterprise.module.ModulesRegistry;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * {@link ClassLoader} that loads classes for a module.
 *
 * @author dochez
 */
final class ModuleClassLoader extends ClassLoaderProxy {
    
    private final ModuleImpl module;

    /**
     * Module will be initialized when this classloader is consulted for the first time.
     */
    private volatile boolean initialized = false;
    private StackTraceElement[] initializerThread;
    private String initializerClassName;

    /** Creates a new instance of ClassLoader */
    public ModuleClassLoader(ModuleImpl owner, URL[] shared, ClassLoader parent) {
        super(shared, parent);
        this.module = owner;
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        Utils.getDefaultLogger().info("ModuleClassLoader gc'ed " + module.getModuleDefinition().getName());
    }


    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        initialize(name);
        return super.loadClass(name, resolve);
    }

    public URL getResource(String name) {
        initialize(name);
        if(name.startsWith(META_INF_SERVICES)) {
            // punch in. find the service loader from any module
            String serviceName = name.substring(META_INF_SERVICES.length());

            ModulesRegistry reg = module.getRegistry();
            for( Module m : reg.getModules() ) {
                List<URL> list = m.getMetadata().getDescriptors(serviceName);
                if(!list.isEmpty())     return list.get(0);
            }

            // no such resource
            return super.getResource(name);
        } else {
            // normal service look up
            URL url = super.getResource(name);
            if(url!=null)
                return url;

            // commons-logging looks for a class file resource for providers,
            // so check for those
            if(name.endsWith(".class")) {
                String className = name.replace('/', '.').substring(0, name.length() - 6);
                ModuleImpl m = module.getRegistry().getProvidingModule(className);
                if(m!=null)
                    return m.getPrivateClassLoader().getResource(name);
            }

            return null;
        }
    }

    public Enumeration<URL> getResources(String name) throws IOException {
        initialize(name);
        if(name.startsWith(META_INF_SERVICES)) {
            // punch in. find the service loader from any module
            String serviceName = name.substring(META_INF_SERVICES.length());

            Vector<URL> urls = new Vector<URL>();

            ModulesRegistry reg = module.getRegistry();
            for( Module m : reg.getModules() )
                urls.addAll(m.getMetadata().getDescriptors(serviceName));

            return urls.elements();
        } else {
            // normal look up
            return super.getResources(name);
        }
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
        writer.println("Class-Path:");
        for (URL url : getURLs())
            writer.println("  "+url);
        if (initializerThread!=null) {
            writer.println("Initialized when " + initializerClassName + " was requested by :");
            for (StackTraceElement e : initializerThread) {
                writer.println("  "+e.toString());
            }
        }
    }


    /**
     * called by the facade class loader when it is garbage collected. 
     * this is a good time to see if this module should be unloaded.
     */
    @Override
    public void stop() {
        
        // we should only detach if the sticky flag is not set
        if (!module.isSticky()) {
            
            Utils.getDefaultLogger().info("ModuleClassLoader stopped " + module.getModuleDefinition().getName());
            super.stop();
            module.stop();
        }
    }
        
    public ModuleImpl getOwner() {
        return module;
    }



    public String toString() {
        StringBuffer s= new StringBuffer(); 
        s.append("ModuleClassLoader(name=").append(module.getName());
        s.append(", parent=").append(super.toString());
        s.append(",init=").append(initialized);
        s.append(",URls[]=");
        for (URL url : getURLs()) {
            s.append(url).append(",");
        }
        s.append(")");
               
        for (ClassLoader surrogate : super.getDelegates()) {
            s.append("\n ref : ").append(surrogate.toString());
        }
        return s.toString();
    }          

    private static final String META_INF_SERVICES = "META-INF/services/";
}
