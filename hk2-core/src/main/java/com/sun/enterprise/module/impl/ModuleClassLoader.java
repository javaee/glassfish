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

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.LogHelper;

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
        LogHelper.getDefaultLogger().info("ModuleClassLoader gc'ed " + module.getModuleDefinition().getName());
    }


    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        initialize(name);
        return super.loadClass(name, resolve);
    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            // punch in. find the provider class, no matter where we are.
            ModuleImpl m = module.getRegistry().getProvidingModule(name);
            if(m!=null)
                return m.getPrivateClassLoader().loadClass(name);
            throw e;
        }
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
                // if we are preparing, we should just not initiate initialization.
                if (module.getState().equals(ModuleState.PREPARING)) {
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
            
            LogHelper.getDefaultLogger().info("ModuleClassLoader stopped " + module.getModuleDefinition().getName());
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
