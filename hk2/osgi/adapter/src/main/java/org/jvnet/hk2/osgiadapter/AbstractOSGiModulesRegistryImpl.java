/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.module.*;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.common_impl.AbstractModulesRegistryImpl;
import com.sun.enterprise.module.common_impl.CompositeEnumeration;
import org.glassfish.hk2.api.ServiceLocator;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;

import static org.jvnet.hk2.osgiadapter.Logger.logger;

/**
 * @author sanjeeb.sahoo@oracle.com
 */
public abstract class AbstractOSGiModulesRegistryImpl extends AbstractModulesRegistryImpl {
    /**
     * OSGi BundleContext - used to install/uninstall, start/stop bundles
     */
    BundleContext bctx;
    protected PackageAdmin pa;
    private Map<ModuleChangeListener, BundleListener> moduleChangeListeners =
            new HashMap<ModuleChangeListener, BundleListener>();
    private Map<ModuleLifecycleListener, BundleListener> moduleLifecycleListeners =
            new HashMap<ModuleLifecycleListener, BundleListener>();

    protected AbstractOSGiModulesRegistryImpl(BundleContext bctx) {
        super(null);
        this.bctx = bctx;
        ServiceReference ref = bctx.getServiceReference(PackageAdmin.class.getName());
        pa = PackageAdmin.class.cast(bctx.getService(ref));
    }

    @Override
    public void shutdown() {
        modules.clear();

        for (Repository repo : repositories.values()) {
            try {
                repo.shutdown();
            } catch(Exception e) {
                java.util.logging.Logger.getAnonymousLogger().log(Level.SEVERE, "Error while closing repository " + repo, e);
                // swallows
            }
        }
        // don't try to stop the system bundle, as we may be embedded inside
        // something like Eclipse.
    }

    public void parseInhabitants(
            Module module, String name, ServiceLocator serviceLocator)
            throws IOException, BootException {
        ((OSGiModuleImpl)module).parseInhabitants(name, serviceLocator);
    }

    public ModulesRegistry createChild() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO(Sahoo)
    }

    public synchronized void detachAll() {
        for (Module m : modules.values()) {
            m.detach();
        }
    }

    /**
     * Sets the classloader parenting the class loaders created by the modules
     * associated with this registry.
     * @param parent parent class loader
     */
    public void setParentClassLoader(ClassLoader parent) {
        throw new UnsupportedOperationException("This method can't be implemented in OSGi environment");
    }

    /**
     * Returns the parent class loader parenting the class loaders created
     * by modules associated with this registry.
     * @return the parent classloader
     */
    public ClassLoader getParentClassLoader() {
        return Bundle.class.getClassLoader();
    }

    /**
     * Returns a ClassLoader capable of loading classes from a set of modules identified
     * by their module definition and also load new urls.
     *
     * @param parent the parent class loader for the returned class loader instance
     * @param mds module definitions for all modules this classloader should be
     *        capable of loading
     * @param urls urls to be added to the module classloader
     * @return class loader instance
     * @throws com.sun.enterprise.module.ResolveError if one of the provided module
     *         definition cannot be resolved
     */
    public ClassLoader getModulesClassLoader(final ClassLoader parent,
                                             Collection<ModuleDefinition> mds,
                                             URL[] urls) throws ResolveError {
        final List<ClassLoader> delegateCLs = new ArrayList<ClassLoader>();
        final List<Module> delegateModules = new ArrayList<Module>();
        for (ModuleDefinition md : mds) {
            Module m = makeModuleFor(md.getName(), md.getVersion());
            delegateModules.add(m);
            delegateCLs.add(m.getClassLoader());
        }
        return new URLClassLoader(urls!=null?urls:new URL[0], parent) {
            /*
             * This is a delegating class loader.
             * This extends URLClassLoader, because web layer (Jasper to be specific)
             * expects it to be a URLClassLoader so that it can extract Classpath information
             * used for javac.
             * It always delegates to a chain of OSGi bundle's class loader.
             * ClassLoader.defineClass() is never called in the context of this class.
             * There will never be a class for which getClassLoader()
             * would return this class loader.
             */
            @Override
            public URL[] getURLs() {
                List<URL> result = new ArrayList<URL>();
                if (parent instanceof URLClassLoader) {
                    URL[] parentURLs = URLClassLoader.class.cast(parent).getURLs();
                    result.addAll(Arrays.asList(parentURLs));
                }
                for (Module m : delegateModules) {
                    ModuleDefinition md = m.getModuleDefinition();
                    URI[] uris = md.getLocations();
                    URL[] urls = new URL[uris.length];
                    for (int i = 0; i < uris.length; ++i) {
                        try {
                            urls[i] = uris[i].toURL();
                        } catch (MalformedURLException e) {
                            logger.warning("Exception " + e + " while converting " + uris[i] + " to URL");
                        }
                    }
                    result.addAll(Arrays.asList(urls));
                }
                return result.toArray(new URL[0]);
            }

            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                for (ClassLoader delegate : delegateCLs) {
                    try {
                        return delegate.loadClass(name);
                    } catch(ClassNotFoundException e) {
                        // This is expected, so ignore
                    }
                }
                throw new ClassNotFoundException(name);
            }

            @Override
            public URL findResource(String name) {
                URL resource = null;
                for (ClassLoader delegate : delegateCLs) {
                    resource = delegate.getResource(name);
                    if (resource != null) {
                        return resource;
                    }
                }
                return resource;
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                List<Enumeration<URL>> enumerators = new ArrayList<Enumeration<URL>>();
                for (ClassLoader delegate : delegateCLs) {
                    Enumeration<URL> enumerator = delegate.getResources(name);
                    enumerators.add(enumerator);
                }
                return new CompositeEnumeration(enumerators);
            }

        };
    }

    /**
     * Returns a ClassLoader capable of loading classes from a set of modules identified
     * by their module definition
     *
     * @param parent the parent class loader for the returned class loader instance
     * @param defs module definitions for all modules this classloader should be
     *        capable of loading classes from
     * @return class loader instance
     * @throws ResolveError if one of the provided module
     *         definition cannot be resolved
     */
    public ClassLoader getModulesClassLoader(ClassLoader parent,
                                             Collection<ModuleDefinition> defs)
        throws ResolveError {
        return getModulesClassLoader(parent, defs, null);
    }

    public Module find(Class clazz) {
        Bundle b = pa.getBundle(clazz);
        if (b!=null) {
            return getModule(b);
        }
        return null;
    }

    public PackageAdmin getPackageAdmin() {
        return pa;
    }

    public void addModuleChangeListener(final ModuleChangeListener listener, final OSGiModuleImpl module) {
        BundleListener bundleListener = new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                if ((event.getBundle() == module.getBundle()) &&
                        ((event.getType() & BundleEvent.UPDATED) == BundleEvent.UPDATED)) {
                    listener.changed(module);
                }
            }
        };
        bctx.addBundleListener(bundleListener);
        moduleChangeListeners.put(listener, bundleListener);
    }

    public boolean removeModuleChangeListener(ModuleChangeListener listener) {
        BundleListener bundleListener = moduleChangeListeners.remove(listener);
        if (bundleListener!= null) {
            bctx.removeBundleListener(bundleListener);
            return true;
        }
        return false;
    }

    public void register(final ModuleLifecycleListener listener) {
        // This is purposefully made an asynchronous bundle listener
        BundleListener bundleListener = new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                switch (event.getType()) {
                    case BundleEvent.INSTALLED:
                        listener.moduleInstalled(getModule(event.getBundle()));
                        break;
                    case BundleEvent.UPDATED:
                        listener.moduleUpdated(getModule(event.getBundle()));
                        break;
                    case BundleEvent.RESOLVED:
                        listener.moduleResolved(getModule(event.getBundle()));
                        break;
                    case BundleEvent.STARTED:
                        listener.moduleStarted(getModule(event.getBundle()));
                        break;
                    case BundleEvent.STOPPED:
                        listener.moduleStopped(getModule(event.getBundle()));
                        break;
                }
            }
        };
        bctx.addBundleListener(bundleListener);
        moduleLifecycleListeners.put(listener,  bundleListener);
    }

    public void unregister(ModuleLifecycleListener listener) {
        BundleListener bundleListener = moduleLifecycleListeners.remove(listener);
        if (bundleListener!=null) {
            bctx.removeBundleListener(bundleListener);
        }
    }

    /*package*/ Module getModule(Bundle bundle) {
        return modules.get(new OSGiModuleId(bundle));
    }
}
