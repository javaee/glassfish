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

package com.sun.enterprise.module;


import com.sun.enterprise.module.impl.Utils;

import java.io.File;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A module represents a set of resources accessible to third party modules. 
 * Each module has a module definition which defines its name, its list of 
 * exported resources and its dependencies to other modules. 
 * A module instance stores references to the class loader instances giving
 * access to the module's implementation. 
 * A module instance belongs to a <code>ModuleRegistry</code> which can be used
 * to get the list of available modules and/or get particular module
 * implementation. 
 * Modules can only satisfy their dependencies within the <code>ModuleRegistry
 * </code> instance they are registered in.
 *
 * @author Jerome Dochez
 */
public final class Module extends ServiceLookup {
    
    private ModuleDefinition moduleDef;
    private WeakReference<ClassLoader> publicCL;
    private volatile ModuleClassLoader privateCL;

    /**
     * Lazily loaded provider {@link Class}es from {@link ServiceProviderInfoList}.
     * The key is the service class name. We can't use {@link Class} because that would cause leaks.
     */
    private final Map<String,List<Class>> serviceClasses = new ConcurrentHashMap<String,List<Class>>();

    /**
     * {@link ModulesRegistry} that owns this module.
     * Always non-null.
     */
    private final ModulesRegistry registry;
    private ModuleState state;
    private final List<Module> dependencies = new ArrayList<Module>();
    private final ArrayList<ModuleChangeListener> listeners = new ArrayList<ModuleChangeListener>();
    private final HashMap<String,Long> lastModifieds = new HashMap<String,Long>();
    private boolean shared=true;
    private boolean sticky=false;
    private LifecyclePolicy lifecyclePolicy = null;
    
    /** Creates a new instance of Module */
    public Module(ModulesRegistry registry, ModuleDefinition info) {
        assert registry!=null && info!=null;
        this.registry = registry;
        moduleDef = info;
        for (URI lib : info.getLocations()) {
            File f = new File(lib);
            if (f.exists()) {
                lastModifieds.put(f.getAbsolutePath(), f.lastModified());
            }

        }
        state = ModuleState.NEW;
    }
    
    /**
     * Return the <code>ClassLoader</code>  instance associated with this module.
     * Only designated public interfaces will be loaded and returned by 
     * this classloader
     * @return the public <code>ClassLoader</code>
     */
    public ClassLoader getClassLoader() {
        ClassLoader r=null;
        if (publicCL!=null)
            r = publicCL.get();
        if (r!=null)
            return r;

        ClassLoaderFacade facade = AccessController.doPrivileged(new PrivilegedAction<ClassLoaderFacade>() {
            public ClassLoaderFacade run() {
                return new ClassLoaderFacade(getPrivateClassLoader());
            }
        });
        facade.setPublicPkgs(moduleDef.getPublicInterfaces());
        publicCL = new WeakReference<ClassLoader>(facade);
        return facade;
    }
    
    /**
     * Return the private class loader for this module. This class loader will
     * be loading all the classes which are not explicitely exported in the 
     * module definition
     * @return the private <code>ClassLoader</code> instance
     */
    /*package*/ ModuleClassLoader getPrivateClassLoader() {
        if (privateCL==null) {
            synchronized(this) {
                if(privateCL==null) {
                    URI[] locations = moduleDef.getLocations();
                    URL[] urlLocations = new URL[locations.length];
                    for (int i=0;i<locations.length;i++) {
                        try {
                            urlLocations[i] = locations[i].toURL();
                        } catch(MalformedURLException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                    final URL[] urls = urlLocations.clone();
                    privateCL = AccessController.doPrivileged(new PrivilegedAction<ModuleClassLoader>() {
                        public ModuleClassLoader run() {
                            return new ModuleClassLoader(
                                Module.this,
                                urls,
                                registry.getParentClassLoader());
                        }
                    });
                }
            }
        }
        return privateCL;
    }
    
    /**
     * Returns the module definition for this module instance
     * @return the module definition
     */
    public com.sun.enterprise.module.ModuleDefinition getModuleDefinition() {
        return moduleDef;
    }
    
    /**
     * Returns the registry owning this module
     * @return the registry owning the module
     */
    public ModulesRegistry getRegistry() {
        return registry;
    }
    
    /**
     * Detach this module from its registry. This does not free any of the 
     * loaded resources. Only proper release of all references to the public
     * class loader will ensure module being garbage collected. 
     * Detached modules are orphan and will be garbage collected if resources
     * are properly disposed. 
     */
    public void detach() {        
        registry.remove(this);
    }
    
    /**
     * Return a String representation 
     * @return a descriptive String about myself
     */
    public String toString() {
        return "Module :" + moduleDef.getName() + "::" + (privateCL==null?"none":privateCL.toString());
    }
    
    /**
     * Add a new module change listener
     * @param listener the listener
     */
    public void addListener(ModuleChangeListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Unregister a module change listener
     * @param listener the listener to unregister
     */
    public void removeListener(ModuleChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * fires a ModuleChange event to all listeners
     */
    protected void fireChangeEvent() {
        ArrayList<ModuleChangeListener> list = new ArrayList<ModuleChangeListener>(listeners);
        for (ModuleChangeListener listener : list) {
            listener.changed(this);
        }
        // registry listens by default
        registry.changed(this);
    }
    
    /**
     * Trigger manual refresh mechanism, the module will check all its 
     * URLs and generate change events if any of them has changed. This 
     * will allow the owning registry to force a module upgrade at next 
     * module request.
     */
    public void refresh() {
        URI[] urls = moduleDef.getLocations();
        for (URI lib : urls) {
            File f = new File(lib);
            if (f.exists() && lastModifieds.containsKey(f.getAbsolutePath())) {
                if (lastModifieds.get(f.getAbsolutePath()) !=f.lastModified()) {
                    //Utils.getDefaultLogger().info("Changed : " + this);
                    fireChangeEvent();                       
                    // detach for now...
                    detach();
                    return;
                }
            }
        }
    }   
    
    /**
     * Requests the list of Services this module provide (generally, module
     * provide one service but it's not limited). The Service is described 
     * by the module implementation using the META_INF/services format 
     * described in the JDK jar file format description.
     *
     * @return a collection of ServiceProviderInfo 
     * 
     */
    public ServiceProviderInfoList getServiceProviders() {
        return moduleDef.getServiceProviders();
    }
    
    /** 
     * Ensure that this module is {@link ModuleState#RESOLVED resolved}.
     *
     * <p>
     * If the module is already resolved, this method does nothing.
     * Otherwise, iterate over all declared ModuleDependency instances and use the
     * associated <code>ModuleRegistry</code> to resolve it. After successful
     * completion of this method, the module state is 
     * {@link ModuleState#RESOLVED}.
     *
     * @throws <code>ResolveError</code> if any of the declared dependency of this module 
     * cannot be satisfied
     */
    public synchronized void resolve() throws ResolveError {
        
        // already resolved ?
        if (state==ModuleState.ERROR)
            throw new ResolveError("Module " + getName() + " is in ERROR state");
        if (state.compareTo(ModuleState.RESOLVED)>=0)
            return;

        if (state==ModuleState.VALIDATING) {
            Utils.identifyCyclicDependency(this, Logger.getAnonymousLogger());
            throw new ResolveError("Cyclic dependency with " + getName());
        }
        state = ModuleState.VALIDATING;
        
        if (moduleDef.getImportPolicyClassName()!=null) {
            try {
                Class<ImportPolicy> importPolicyClass = (Class<ImportPolicy>) getPrivateClassLoader().loadClass(moduleDef.getImportPolicyClassName());
                ImportPolicy importPolicy = importPolicyClass.newInstance();
                importPolicy.prepare(this);
            } catch(ClassNotFoundException e) {
                throw new ResolveError(e);
            } catch(java.lang.InstantiationException e) {
                throw new ResolveError(e);
            } catch(IllegalAccessException e) {
                throw new ResolveError(e);
            }
        }
        for (ModuleDependency dependency : moduleDef.getDependencies()) {
            
            Module depModule = registry.makeModuleFor(dependency.getName(), null);
            if (depModule==null) {
                throw new ResolveError(dependency + " referenced from " 
                        + moduleDef.getName() + " is not resolved");
            }
            addImport(depModule);
        }
        state = ModuleState.RESOLVED;
        
        // time to initialize the lifecycle instance
        if (moduleDef.getLifecyclePolicyClassName()!=null) {
            try {
                Class<LifecyclePolicy> lifecyclePolicyClass = (Class<LifecyclePolicy>) getPrivateClassLoader().loadClass(moduleDef.getLifecyclePolicyClassName());
                lifecyclePolicy = lifecyclePolicyClass.newInstance();
            } catch(ClassNotFoundException e) {
                throw new ResolveError("ClassNotFound : " + e.getMessage(), e);
            } catch(java.lang.InstantiationException e) {
                throw new ResolveError(e);
            } catch(IllegalAccessException e) {
                throw new ResolveError(e);
            }
            lifecyclePolicy.load(this);
        }
        //Logger.global.info("Module " + getName() + " resolved");

        // module resolution complete. notify listeners
        for (ModuleLifecycleListener listener : registry.lifecycleListeners)
            listener.moduleStarted(this);
    }
    
    /**
     * Forces module startup. In most cases, the runtime will take care 
     * of starting modules when they are first used. There could be cases where
     * code need to manually start a sub module. Invoking this method will 
     * move the module to the {@link ModuleState#READY ModuleState.READY}, the 
     * {@link LifecyclePolicy#start Lifecycle.start} method will be invoked.
     */
    public void start() throws ResolveError {
        
        if (state==ModuleState.READY)
            return;
        
        // ensure RESOLVED state
        resolve();      
        
        state = ModuleState.READY;
        try {
            for (Module subModules : dependencies) {
                subModules.start();
            }
        } catch(ResolveError e) {
            state = ModuleState.RESOLVED;
            throw e;
        }
        
        if (lifecyclePolicy!=null) {
            lifecyclePolicy.start(this);
        }
        //Logger.global.info("Module " + getName() + " started");
    }
    
    /** 
     * Forces module stop. In most cases, the runtime will take care of stopping
     * modules when the last module user released its interest. However, in 
     * certain cases, it may be interesting to manually stop the module. 
     * Stopping the module means that the module is removed from the registry, 
     * the class loader references are released (note : the class loaders will
     * only be released if all instances of any class loaded by them are gc'ed). 
     * If a <code>LifecyclePolicy</code> for this module is defined, the 
     * {@link LifecyclePolicy#stop(Module) Lifecycle.stop(Module)}
     * method will be called and finally the module state will be 
     * returned to {@link ModuleState#NEW ModuleState.NEW}.
     *
     * @return true if unloading was successful
     */
    public boolean stop() {

        if (sticky) {
            return false;
        } 
        
        if (lifecyclePolicy!=null) {
            lifecyclePolicy.stop(this);
            lifecyclePolicy=null;
        }
        
        detach();
        
        // we do NOT stop our sub modules which are shared...
        for (Module subModule : dependencies) {
            if (!subModule.isShared()) {
                subModule.stop();
            }
        }
        // release all sub modules class loaders
        privateCL = null;
        publicCL = null;
        dependencies.clear();
        
        state = ModuleState.NEW;
        return true;
    }
    
    /**
     * Returns the list of imported modules
     * @return the list of imported modules
     */
    public List<Module> getImports() {
        return dependencies;
    }
    
    /**
     * Create and add a new module to this module's list of 
     * imports.
     * @param dependency new module's definition
     */
    public Module addImport(ModuleDependency dependency) {

        Module newModule;
        if (dependency.isShared()) {
            newModule = registry.makeModuleFor(dependency.getName(), dependency.getVersion());
        } else {
            newModule = registry.newPrivateModuleFor(dependency.getName(), dependency.getVersion());
        }
        addImport(newModule);
        return newModule;
    }
        
    /**
     * Returns the module's state
     * @return the module's state 
     */
    public ModuleState getState() {
        return state;
    }
    
    public void addImport(Module module) {
        //if (Utils.isLoggable(Level.INFO)) {
        //    Utils.getDefaultLogger().info("For module" + getName() + " adding new dependent " + module.getName());
        //}
        if (!dependencies.contains(module)) {
            dependencies.add(module);
            getPrivateClassLoader().addDelegate(module.getClassLoader());
        }
    }
    
    public void removeImport(Module module) {
        if (dependencies.contains(module)) {
            dependencies.remove(module);
            getPrivateClassLoader().removeDelegate(module.getClassLoader());
        }
    }

    /**
     * Short-cut for {@code getModuleDefinition().getName()}.
     */
    public String getName() {
        if (getModuleDefinition()!=null) {
            return getModuleDefinition().getName();
        }
        return "unknown module";                
    }
    
    /**
     * Returns true if this module is sharable. A sharable module means that 
     * onlu one instance of the module classloader will be used by all users. 
     *
     * @return true if this module is sharable.
     */
    public boolean isShared() {
        return shared;
    }
    
    /**
     * Sets the sharable flag. Setting the flag to false means that the moodule 
     * class loader should not be shared among module owners.
     * @param sharable set to true to share the module
     */
    void setShared(boolean sharable) {
        this.shared = sharable;
    }
    
    /**
     * Returns true if the module is sticky. A sticky module cannot be stopped or
     * unloaded. Once a sticky module is loaded or started, it will stay in the 
     * JVM until it exists.
     * @return true is the module is sticky
     */
    public boolean isSticky() {
        return sticky;
    }
    
    /**
     * Sets the sticky flag.
     * @param sticky true if the module should stick around
     */
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    @SuppressWarnings({"unchecked"})
    public <T> Iterable<Class<? extends T>> getProvidersClass(Class<T> serviceClass) {
        return (Iterable)getProvidersClass(serviceClass.getName());
    }

    public Iterable<Class> getProvidersClass(String name) {
        List<Class> r = serviceClasses.get(name);
        if(r!=null) return r;

        // the worst case scenario in the race situation is we end up creating the same list twice,
        // which is not a big deal.

        for( String provider : getServiceProviders().getEntry(name).providerNames) {
            if(r==null)
                r = new ArrayList<Class>();
            try {
                r.add(getPrivateClassLoader().loadClass(provider));
            } catch (ClassNotFoundException e) {
                Utils.getDefaultLogger().log(Level.SEVERE, "Failed to load "+provider+" from "+getName(),e);
            }
        }

        if(r==null)
            r = Collections.emptyList();

        serviceClasses.put(name, r);
        return r;
    }

    /**
     * Returns true if this module has any provider for the given service class.
     */
    public boolean hasProvider(Class serviceClass) {
        String name = serviceClass.getName();

        List<Class> v = serviceClasses.get(name);
        if(v!=null && !v.isEmpty())    return true;

        return getServiceProviders().getEntry(name).hasProvider();
    }

    void dumpState(PrintStream writer) {
        
        writer.println("Module " + getName() + " Dump");
        writer.println("State " + getState());
        for (Module imported : getImports()) {
            writer.println("Depends on " + imported.getName());
        }
        if (publicCL!=null) {
            ClassLoaderFacade cloader = (ClassLoaderFacade) publicCL.get();
            cloader.dumpState(writer);
        }
    }

    /**
     * Finds the {@link Module} that owns the given class.
     *
     * @return
     *      null if the class is loaded outside the module system.
     */
    public static Module find(Class clazz) {
        ClassLoader cl = clazz.getClassLoader();
        if(cl==null)    return null;
        if (cl instanceof ModuleClassLoader)
            return ((ModuleClassLoader) cl).getOwner();
        return null;
    }
}
