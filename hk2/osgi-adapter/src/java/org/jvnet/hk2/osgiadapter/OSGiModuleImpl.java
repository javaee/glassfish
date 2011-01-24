/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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


package org.jvnet.hk2.osgiadapter;

import static org.jvnet.hk2.osgiadapter.Logger.logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.RequiredBundle;
import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.enterprise.module.*;
import com.sun.enterprise.module.common_impl.CompositeEnumeration;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.util.logging.Level;
import java.util.*;
import java.net.URL;
import java.net.URI;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class OSGiModuleImpl implements Module {
    private Bundle bundle;

    private ModuleDefinition md;

    private OSGiModulesRegistryImpl registry;

    private boolean isTransientlyActive = false;

    /* TODO (Sahoo): Change hk2-apt to generate an equivalent BundleActivator
       corresponding to LifecyclerPolicy class. That way, LifecyclePolicy class
       will be invoked even when underlying OSGi bundle is stopped or started
       using any OSGi bundle management tool.
     */
    private LifecyclePolicy lifecyclePolicy;

    public OSGiModuleImpl(OSGiModulesRegistryImpl registry, Bundle bundle, ModuleDefinition md) {
        this.registry = registry;
        this.bundle = bundle;
        this.md = md;
    }

    public ModuleDefinition getModuleDefinition() {
        return md;
    }

    public String getName() {
        return md.getName();
    }

    public ModulesRegistry getRegistry() {
        return registry;
    }

    public ModuleState getState() {
        // We don't cache the module state locally. Instead we always map
        // the underlying bundle's state to HK2 state. This avoids us
        // from having to register a listener with OSGi to be updated with
        // bundle state transitions.
        return mapBundleStateToModuleState(bundle);
    }

    /* package */ static ModuleState mapBundleStateToModuleState(Bundle bundle)
    {
        ModuleState state;
        switch (bundle.getState())
        {
            case Bundle.INSTALLED:
            case Bundle.UNINSTALLED:
                state = ModuleState.NEW;
                break;
            case Bundle.RESOLVED:
                state = ModuleState.RESOLVED;
                break;
            case Bundle.STARTING:
                state = ModuleState.PREPARING;
                break;
            case Bundle.ACTIVE:
                state = ModuleState.READY;
                break;
            default:
                throw new RuntimeException(
                        "Does not know how to handle bundle with state [" +
                                bundle.getState() + "]");
        }

        return state;
    }

    public synchronized void resolve() throws ResolveError {
        // Since OSGi bundle does not have a separate resolve method,
        // we use the same implementation as start();
        start();
    }

    public synchronized void start() throws ResolveError {
        int state = bundle.getState();
        if (((Bundle.STARTING | Bundle.ACTIVE | Bundle.STOPPING) & state) != 0) {
            if (logger.isLoggable(Level.FINER)) {
                logger.logp(Level.FINER, "OSGiModuleImpl", "start",
                        "Ignoring start of bundle {0} as it is in {1} state",
                        new Object[]{bundle, toString(bundle.getState())} );
            }
            return;
        }
        try {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction(){
                        public Object run() throws BundleException
                        {
                            bundle.start(Bundle.START_TRANSIENT);
                            return null;
                        }
                    });
                } catch (PrivilegedActionException e) {
                    throw (BundleException)e.getException();
                }
            } else {
                bundle.start(Bundle.START_TRANSIENT);
            }
            isTransientlyActive = true;
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, "OSGiModuleImpl",
                        "start", "Started bundle {0}", bundle);
            }
        } catch (BundleException e) {
            throw new ResolveError("Failed to start "+this,e);
        }

        // TODO(Sahoo): Remove this when hk2-apt generates equivalent BundleActivator
        // if there is a LifecyclePolicy, then instantiate and invoke.
        if (md.getLifecyclePolicyClassName()!=null) {
            try {
                Class<LifecyclePolicy> lifecyclePolicyClass =
                        (Class<LifecyclePolicy>) bundle.loadClass(md.getLifecyclePolicyClassName());
                lifecyclePolicy = lifecyclePolicyClass.newInstance();
            } catch(ClassNotFoundException e) {
                throw new ResolveError("ClassNotFound : " + e.getMessage(), e);
            } catch(java.lang.InstantiationException e) {
                throw new ResolveError(e);
            } catch(IllegalAccessException e) {
                throw new ResolveError(e);
            }
        }
        if (lifecyclePolicy!=null) {
            lifecyclePolicy.start(this);
        }
    }

    private String toString(int state)
    {
        String value;
        switch (state) {
            case Bundle.STARTING:
                value = "STARTING";
                break;
            case Bundle.STOPPING:
                value = "STOPPING";
                break;
            case Bundle.INSTALLED:
                value = "INSTALLED";
                break;
            case Bundle.UNINSTALLED:
                value = "UNINSTALLED";
                break;
            case Bundle.RESOLVED:
                value = "RESOLVED";
                break;
            case Bundle.ACTIVE:
                value = "ACTIVE";
                break;
            default:
                value = "UNKNOWN STATE [" + state + "]";
                logger.warning("No mapping exist for bundle state " + state);
        }
        return value;
    }

    public synchronized boolean stop() {
        detach();
        // Don't refresh packages, as we are not uninstalling the bundle.
//        registry.getPackageAdmin().refreshPackages(new Bundle[]{bundle});
        return true;
    }

    public void detach() {
        if (bundle.getState() != Bundle.ACTIVE) {
            if (logger.isLoggable(Level.FINER)) {
                logger.logp(Level.FINER, "OSGiModuleImpl", "detach",
                        "Ignoring stop of bundle {0} as it is in {1} state",
                        new Object[]{bundle, toString(bundle.getState())} );
            }
            return;
        }

        if (lifecyclePolicy!=null) {
            lifecyclePolicy.stop(this);
            lifecyclePolicy=null;
        }

        try {
            bundle.stop();
            if (logger.isLoggable(Level.FINE))
            {
                logger.logp(Level.FINE, "OSGiModuleImpl", "detach", "Stopped bundle = {0}", new Object[]{bundle});
            }
//            bundle.uninstall();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
    }

    public void uninstall() {
        // This method is called when the hk2-osgi-adapter module is stopped.
        // During that time, we need to stop all the modules, hence no sticky check is
        // performed in this method.
        try {
            bundle.uninstall();
        } catch (BundleException e) {
            throw new RuntimeException(e);
        }
        registry.remove(this);
        this.registry = null;
    }

    public void refresh() {
        URI location = md.getLocations()[0];
        File f = new File(location);
        if (f.lastModified() > bundle.getLastModified()) {
            try {
                bundle.update();
                registry.getPackageAdmin().refreshPackages(new Bundle[]{bundle});
            } catch (BundleException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ModuleMetadata getMetadata() {
        return md.getMetadata();
    }

    public <T> Iterable<Class<? extends T>> getProvidersClass(
            Class<T> serviceClass) {
        return (Iterable)getProvidersClass(serviceClass.getName());
    }

    public Iterable<Class> getProvidersClass(String name) {
        List<Class> r = new ArrayList<Class>();
        for( String provider : getMetadata().getEntry(name).providerNames) {
            try {
                r.add(getClassLoader().loadClass(provider));
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Failed to load "+provider+" from "+getName(),e);
            }
        }
        return r;
    }

    public boolean hasProvider(Class serviceClass) {
        String name = serviceClass.getName();
        return getMetadata().getEntry(name).hasProvider();
    }

    public void addListener(ModuleChangeListener listener) {
        registry.addModuleChangeListener(listener, this);
    }

    public void removeListener(ModuleChangeListener listener) {
        registry.removeModuleChangeListener(listener);
    }

    public void dumpState(PrintStream writer) {
        writer.print(bundle.toString());
    }

    /**
     * Parses all the inhabitants descriptors of the given name in this module.
     */
    /* package */ void parseInhabitants(String name, InhabitantsParser parser) throws IOException {
        Holder<ClassLoader> holder = new Holder<ClassLoader>() {
            public ClassLoader get() {
                //doprivileged needed for running with SecurityManager
                ClassLoader ret = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public java.lang.Object run() {
                        return new ClassLoader() {
                            @Override
                            public synchronized Class<?> loadClass(
                                    final String name) throws ClassNotFoundException {
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.logp(Level.FINE, "OSGiModuleImpl", "loadClass", "Loading {0} from bundle: {1}",
                                            new Object[]{name, bundle});
                                }
                                start();
                                try {
                                    final Class aClass = (Class)
                                           AccessController.doPrivileged(new PrivilegedAction() {
                                        public java.lang.Object run() {
                                            try {
                                                return bundle.loadClass(name);
                                            } catch (Throwable e) {
                                                logger.logp(Level.SEVERE, "OSGiModuleImpl", "loadClass", "Exception in module " + bundle.toString() + " : " + e.toString());
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    });
                                    if (logger.isLoggable(Level.FINE)) {
                                        logger.logp(Level.FINE, "OSGiModuleImpl", "loadClass",
                                                name + ".class.getClassLoader() = {0}",
                                                aClass.getClassLoader());
                                    }
                                    return aClass;
                                } catch (RuntimeException e) {
                                    logger.logp(Level.SEVERE, "OSGiModuleImpl", "loadClass", "Exception in module " + bundle.toString() + " : " + e.toString());
                                    throw new ClassNotFoundException(e.getCause().getMessage(), e.getCause());
                                }
                            }
                        };
                    }
                });
                return ret;
            }
        };

        for (InhabitantsDescriptor d : md.getMetadata().getHabitats(name))
            parser.parse(d.createScanner(),holder);
    }

    public ClassLoader getClassLoader() {
        /*
         * This is a delegating class loader.
         * It always delegates to OSGi's bundle's class loader.
         * ClassLoader.defineClass() is never called in the context of this class.
         * There will never be a class for which getClassLoader()
         * would return this class loader.
         * It overrides loadClass(), getResource() and getResources() as opposed to
         * their findXYZ() equivalents so that the OSGi export control mechanism
         * is enforced even for classes and resources available in the system/boot
         * class loader.
         */
        return new ClassLoader(Bundle.class.getClassLoader()) {
            public static final String META_INF_SERVICES = "META-INF/services/";

            @Override
            protected synchronized Class<?> loadClass(final String name, boolean resolve) throws ClassNotFoundException {
                try {
                    //doprivileged needed for running with SecurityManager
                    Class<?> ret =(Class<?>)
                    AccessController.doPrivileged(new PrivilegedAction() {
                        public java.lang.Object run() {
                            try {
                                return bundle.loadClass(name);
                            } catch (ClassNotFoundException e) {
                                // punch in. find the provider class, no matter where we are.
                                OSGiModuleImpl m =
                                        (OSGiModuleImpl) registry.getProvidingModule(name);
                                if (m != null) {
                                    try {
                                        return m.bundle.loadClass(name);
                                    } catch (ClassNotFoundException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    return ret;
                    
                } catch (RuntimeException e) {
                    if (e.getCause() instanceof ClassNotFoundException) {
                        throw (ClassNotFoundException)e.getCause();
                    }
                    throw e;
                }

            }

            @Override
            public URL getResource(String name) {
                URL result = bundle.getResource(name);
                if (result != null) return result;

                // If this is a META-INF/services lookup, search in every
                // modules that we know of.
                if(name.startsWith(META_INF_SERVICES)) {
                    // punch in. find the service loader from any module
                    String serviceName = name.substring(
                            META_INF_SERVICES.length());

                    for( Module m : registry.getModules() ) {
                        List<URL> list = m.getMetadata().getDescriptors(
                                serviceName);
                        if(!list.isEmpty())     return list.get(0);
                    }
                }
                return null;
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                if(name.startsWith(META_INF_SERVICES)) {
                    // search in the parent classloader
                    Enumeration<URL> parentResources =
                            Bundle.class.getClassLoader().getResources(name);

                    // punch in. find the service loader from any module
                    String serviceName = name.substring(
                            META_INF_SERVICES.length());

                    List<URL> punchedInURLs = new ArrayList<URL>();

                    for( Module m : registry.getModules() )
                        punchedInURLs.addAll(m.getMetadata().getDescriptors(
                                serviceName));

                    List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
                    enumerators.add(parentResources);
                    enumerators.add(Collections.enumeration(punchedInURLs));
                    Enumeration<URL> result = new CompositeEnumeration(enumerators);
                    return result;
                }
                Enumeration<URL> resources = bundle.getResources(name);
                if (resources==null) {
                    // This check is needed, because ClassLoader.getResources()
                    // expects us to return an empty enumeration.
                    resources = new Enumeration<URL>(){

                        public boolean hasMoreElements() {
                            return false;
                        }

                        public URL nextElement() {
                            throw new NoSuchElementException();
                        }
                    };
                }
                return resources;
            }

            @Override
            public String toString() {
                return "Class Loader for Bundle [" + bundle.toString() + " ]";
            }
        };
    }

    public void addImport(Module module) {
        throw new UnsupportedOperationException("This method can't be implemented in OSGi environment");
    }

    public Module addImport(ModuleDependency dependency) {
        throw new UnsupportedOperationException("This method can't be implemented in OSGi environment");
    }

    public boolean isSticky() {
        return true; // all modules are always sticky
    }

    public void setSticky(boolean sticky) {
        // NOOP: It's not required in OSGi.
    }

    public List<Module> getImports() {
        List<Module> result = new ArrayList<Module>();
        RequiredBundle[] requiredBundles =
                registry.getPackageAdmin().getRequiredBundles(bundle.getSymbolicName());
        if (requiredBundles!=null) {
            for(RequiredBundle rb : requiredBundles) {
                Module m = registry.getModule(rb.getBundle());
                if (m!=null) {
                    // module is known to the module system
                    result.add(m);
                } else {
                    // module is not known to us - may be the OSgi bundle depends on a native
                    // OSGi bundle
                }
            }

        }
        return result;
    }

    public boolean isShared() {
        return true; // all OSGi bundles are always shared.
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean isTransientlyActive() {
        return isTransientlyActive;
    }

    public String toString() {
        return "Bundle Id [" + bundle.getBundleId()
                + "]\t State [" + toString(bundle.getState())
                + "]\t [" + md.toString() + "]";
    }

    @Override
    public int hashCode() {
        return bundle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OSGiModuleImpl) {
            return bundle.equals(OSGiModuleImpl.class.cast(obj).bundle);
        }
        return false;
    }
}

