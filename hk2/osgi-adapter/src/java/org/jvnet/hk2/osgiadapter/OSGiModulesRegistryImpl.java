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

import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import static org.jvnet.hk2.osgiadapter.Logger.logger;
import com.sun.enterprise.module.*;
import com.sun.enterprise.module.common_impl.CompositeEnumeration;
import com.sun.enterprise.module.common_impl.ModuleId;
import com.sun.hk2.component.InhabitantsParser;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.net.URISyntaxException;

/**
 * This is an implementation of {@link com.sun.enterprise.module.ModulesRegistry}.
 * It uses OSGi extender pattern to do necessary parsing of OSGi bundles.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiModulesRegistryImpl
        extends com.sun.enterprise.module.common_impl.AbstractModulesRegistryImpl
        implements SynchronousBundleListener {

    /**
     * OSGi BundleContext - used to install/uninstall, start/stop bundles
     */
    BundleContext bctx;
    private PackageAdmin pa;
    private Map<ModuleChangeListener, BundleListener> moduleChangeListeners =
            new HashMap<ModuleChangeListener, BundleListener>();
    private Map<ModuleLifecycleListener, BundleListener> moduleLifecycleListeners =
            new HashMap<ModuleLifecycleListener, BundleListener>();

    // cache related attributes
    private Map<URI, ModuleDefinition> cachedData = new HashMap<URI, ModuleDefinition>();
    private boolean cacheInvalidated = false;
    private static final String HK2_CACHE_DIR = "com.sun.enterprise.hk2.cacheDir";
    private static final String INHABITANTS_CACHE = "inhabitants";
    private static final String HK2_CACHE_IO_BUFFER_SIZE = "com.sun.enterprise.hk2.cacheIoBufferSize";
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    /*package*/ OSGiModulesRegistryImpl(BundleContext bctx) {
        super(null);
        this.bctx = bctx;

        // Need to add a listener so that we get notification about
        // bundles that get installed/uninstalled from now on...
        // This must happen before we start iterating the existing bundles.
        bctx.addBundleListener(this);

        try {
            loadCachedData();
        } catch (Exception e) {
            Logger.logger.log(Level.WARNING, "Cannot load cached metadata, will recreate the cache", e);
            cachedData.clear();
        }

        // Populate registry with pre-installed bundles
        for (final Bundle b : bctx.getBundles()) {
            if (b.getLocation().equals (Constants.SYSTEM_BUNDLE_LOCATION)) {
                continue;
            }
            try {
                add(makeModule(b)); // call add as it processes provider names
            } catch (Exception e) {
                logger.logp(Level.WARNING, "OSGiModulesRegistryImpl",
                        "OSGiModulesRegistryImpl",
                        "Not able convert bundle [{0}] having location [{1}] " +
                                "to module because of exception: {2}",
                        new Object[]{b, b.getLocation(), e});
                continue;
            }
        }
        ServiceReference ref = bctx.getServiceReference(PackageAdmin.class.getName());
        pa = PackageAdmin.class.cast(bctx.getService(ref));
    }

    public void bundleChanged(BundleEvent event) {
        // Extender implementation.
        try {
            final Bundle bundle = event.getBundle();
            switch (event.getType()) {
                case BundleEvent.RESOLVED : {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("[" + bundle.getBundleId() + "] " + bundle.getSymbolicName() +  " resolved");
                    }
                    break;
                } 

                case BundleEvent.INSTALLED :
                {
                    // call add as it processes provider names
                    OSGiModuleImpl m = makeModule(bundle);
                    add(m);
                    break;
                }
                case BundleEvent.UNINSTALLED :
                {
                    final Module m = getModule(bundle);
                    if (m!=null) {
                        // getModule can return null if some bundle got uninstalled
                        // before we have finished initialization. This can
                        // happen if framework APIs are called in parallel
                        // by some third party bundles.
                        // We need to call remove as it processes provider names
                        // and updates the cache.
                        remove(m);
                    }
                    break;
                }
                case BundleEvent.UPDATED :
                    final Module m = getModule(bundle);
                    if (m!=null) {
                        // getModule can return null if some bundle got uninstalled
                        // before we have finished initialization. This can
                        // happen if framework APIs are called in parallel
                        // by some third party bundles.
                        // We need to call remove as it processes provider names
                        // and updates the cache.
                        remove(m);
                    }

                    // make a new module from the updated bundle data and add it
                    add(makeModule(bundle));
                    break;
            }
        } catch (Exception e) {
            logger.logp(Level.WARNING, "OSGiModulesRegistryImpl", "bundleChanged",
                    "e = {0}", new Object[]{e});
        }
    }

    // Factory method
    private OSGiModuleImpl makeModule(Bundle bundle) throws IOException, URISyntaxException {
        final OSGiModuleDefinition md = makeModuleDef(bundle);
        OSGiModuleImpl m = new OSGiModuleImpl(this, bundle, md);
        return m;
    }

    /**
     * Loads the inhabitants metadata from the cache. metadata is saved in a file
     * called inhabitants
     *
     * @throws Exception if the file cannot be read correctly
     */
    private void loadCachedData() throws Exception {
        String cacheLocation = getProperty(HK2_CACHE_DIR);
        if (cacheLocation == null) {
            return;
        }
        File io = new File(cacheLocation, INHABITANTS_CACHE);
        if (!io.exists()) return;
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.INFO, "OSGiModulesRegistryImpl", "loadCachedData", "HK2 cache file = {0}", new Object[]{io});
        }
        ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(io),
                getBufferSize()));
        cachedData = (Map<URI, ModuleDefinition>) stream.readObject();
        stream.close();
    }

    /**
     * Saves the inhabitants metadata to the cache in a file called inhabitants
     * @throws IOException if the file cannot be saved successfully
     */
    private void saveCache() throws IOException {
        String cacheLocation = getProperty(HK2_CACHE_DIR);
        if (cacheLocation == null) {
            return;
        }
        File io = new File(cacheLocation, INHABITANTS_CACHE);
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.INFO, "OSGiModulesRegistryImpl", "saveCache", "HK2 cache file = {0}", new Object[]{io});
        }
        if (io.exists()) io.delete();
        io.createNewFile();
        Map<URI, ModuleDefinition> data = new HashMap<URI, ModuleDefinition>();
        for (Module m : modules.values()) {
            data.put(m.getModuleDefinition().getLocations()[0], m.getModuleDefinition());
        }
        ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(io), getBufferSize()));
        os.writeObject(data);
        os.close();
    }

    private void deleteCache() {
        String cacheLocation = getProperty(HK2_CACHE_DIR);
        if (cacheLocation == null) {
            return;
        }
        File io = new File(cacheLocation, INHABITANTS_CACHE);
        if (io.exists()) {
            if (io.delete()) {
                logger.logp(Level.FINE, "OSGiModulesRegistryImpl",
                        "deleteCache", "deleted = {0}", new Object[]{io});
            } else {
                logger.logp(Level.WARNING, "OSGiModulesRegistryImpl",
                        "deleteCache", "failed to delete = {0}", new Object[]{io});
            }
        }
    }

    private int getBufferSize() {
        int bufsize = DEFAULT_BUFFER_SIZE;
        try {
            bufsize = Integer.valueOf(bctx.getProperty(HK2_CACHE_IO_BUFFER_SIZE));
        } catch (Exception e) {
        }
        if(logger.isLoggable(Level.FINE)) {
            logger.logp(Level.FINE, "OSGiModulesRegistryImpl", "getBufferSize", "bufsize = {0}", new Object[]{bufsize});
        }
        return bufsize;
    }

    // Factory method
    private OSGiModuleDefinition makeModuleDef(Bundle bundle)
            throws IOException, URISyntaxException {
        URI key = OSGiModuleDefinition.toURI(bundle);
        if (cachedData.containsKey(key)) {
            return OSGiModuleDefinition.class.cast(cachedData.get(key));
        } else {
            this.cacheInvalidated = true;
            return new OSGiModuleDefinition(bundle);
        }
    }

    @Override
    protected synchronized void add(Module newModule) {
        // It is overridden to make it synchronized as it is called from
        // BundleListener.
        super.add(newModule);
        // don't set cacheInvalidated = true here, as this method is called while iterating initial
        // set of bundles when this module is started. Instead, we invalidate the cache makeModuleDef().
    }

    @Override
    public synchronized void remove(Module module) {
        // It is overridden to make it synchronized as it is called from
        // BundleListener.
        super.remove(module);

        // Update cache. 
        final URI location = module.getModuleDefinition().getLocations()[0];
        cachedData.remove(location);
        cacheInvalidated = true;
    }

    // factory method
    protected Module newModule(ModuleDefinition moduleDef) {
        String location = moduleDef.getLocations()[0].toString();
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.logp(Level.FINE, "OSGiModulesRegistryImpl", "newModule",
                    "location = {0}", location);
            }
            File l = new File(moduleDef.getLocations()[0]);
            if (l.isDirectory()) {
                location = "reference:" + location;
            }
            Bundle bundle = bctx.installBundle(location);
            // wrap Bundle by a Module object
            return new OSGiModuleImpl(this, bundle, moduleDef);
        } catch (BundleException e) {
            logger.logp(Level.WARNING, "OSGiModulesRegistryImpl", "newModule",
                    "Exception {0} while adding location = {1}", new Object[]{e, location});
//            throw new RuntimeException(e); // continue
        }
        return null;
    }

    public void parseInhabitants(
            Module module, String name, InhabitantsParser inhabitantsParser)
            throws IOException {
        OSGiModuleImpl.class.cast(module).parseInhabitants(name, inhabitantsParser);
    }

    public ModulesRegistry createChild() {
        throw new UnsupportedOperationException("Not Yet Implemented"); // TODO(Sahoo)
    }

    public synchronized void detachAll() {
        for (Module m : modules.values()) {
            m.detach();
        }
    }

    public synchronized void shutdown() {
        for (Module m : modules.values()) {
            // Only stop modules that were started after ModulesRegistry
            // came into existence.
            if (OSGiModuleImpl.class.cast(m).isTransientlyActive()) {
                 m.stop();
            }
        }
        // Save the cache before clearing modules
        try {
            if (cacheInvalidated) {
                saveCache();
            }
        } catch (IOException e) {
            Logger.logger.log(Level.WARNING, "Cannot save metadata to cache", e);
        }
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
     * @throws com.sun.enterprise.module.ResolveError if one of the provided module
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

    /**
     * This method is needed because {@link super#getModules()} always goes thru'
     * all the repositories and installs new modules. When such a side effect is not
     * necessary, this method can be called. The returned collection does not include
     * modules known to ancestors of this registry.
     * @return a readonly map of moduleId to modules.
     */
    /*package*/Map<ModuleId, Module> getExistingModules() {
        return Collections.unmodifiableMap(modules);
    }

    /*package*/ Module getModule(Bundle bundle) {
        return modules.get(new OSGiModuleId(bundle));
    }

    private String getProperty(String property) {
        String value = bctx.getProperty(property);
        // Check System properties to work around Equinox Bug:
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=320459
        if (value == null) value = System.getProperty(property);
        return value;
    }

}
