/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2011 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.osgiresourcelocator;

import org.osgi.framework.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public final class ServiceLoaderImpl extends org.glassfish.hk2.osgiresourcelocator.ServiceLoader {

    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private BundleListener bundleTracker;
    private BundleContext bundleContext;
    private ProvidersList providersList = new ProvidersList();

//    /**
//     * Map of service type to bundles providing the service
//     */
//    private Map<String, List<ServiceProviders>> serviceToProvidersMap = new HashMap<String, List<ServiceProviders>>();

    public ServiceLoaderImpl() {
        ClassLoader cl = getClass().getClassLoader();
        if (cl instanceof BundleReference) {
            bundleContext = getBundleContextSecured(BundleReference.class.cast(cl).getBundle());
        }
        if (bundleContext == null) {
            throw new RuntimeException("There is no bundle context available yet. " +
                    "Instatiate this class in STARTING or ACTIVE state only");
        }
    }

    private BundleContext getBundleContextSecured(final Bundle bundle) {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new PrivilegedAction<BundleContext>() {
                public BundleContext run() {
                    return bundle.getBundleContext();
                }
            });
        } else {
            return bundle.getBundleContext();
        }
    }

    public void trackBundles() {
        assert (bundleTracker == null);
        /*
         * The reason for separating this code from constructor is that we don't want to
         * leak a partially constructed this reference via inner class called BundleTracker.
         * That would be a problem since BundleTracker receives callbacks from other threads.
         */

        // First register a listener and then iterate over existing bundles
        bundleTracker = new BundleTracker();
        bundleContext.addBundleListener(bundleTracker);
        for (Bundle bundle : bundleContext.getBundles()) {
            addProviders(bundle);
        }
    }

    /*package*/ <T> Iterable<? extends T> lookupProviderInstances1(Class<T> serviceClass, ProviderFactory<T> factory) {
        if (factory == null) {
            factory = new DefaultFactory<T>();
        }
        List<T> providers = new ArrayList<T>();
        for (Class c : lookupProviderClasses1(serviceClass)) {
            try {
                final T providerInstance = factory.make(c, serviceClass);
                if (providerInstance != null) {
                    providers.add(providerInstance);
                } else {
                    debug(factory + " returned null provider instance!!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return providers;
    }

    /*package*/ <T> Iterable<Class> lookupProviderClasses1(Class<T> serviceClass) {
        List<Class> providerClasses = new ArrayList<Class>();
        rwLock.readLock().lock();
        try {
            final String serviceName = serviceClass.getName();
            for (ProvidersPerBundle providersPerBundle : providersList.getAllProviders()) {
                final Bundle bundle = bundleContext.getBundle(providersPerBundle.getBundleId());
                if (bundle == null) {
                    // bundle may have been uninstalled
                    continue;
                }
                final List<String> providerNames = providersPerBundle.getServiceToProvidersMap().get(serviceName);
                if (providerNames == null) {
                    continue;
                }
                for (String providerName : providerNames) {
                    try {
                        final Class providerClass = loadClassSecured(bundle, providerName);
                        if (isCompatible(providerClass, serviceClass)) {
                            providerClasses.add(providerClass);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return providerClasses;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private Class loadClassSecured(final Bundle bundle, final String name)
            throws ClassNotFoundException {
        if (System.getSecurityManager()!=null) {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction<Class>(){
                    public Class run() throws ClassNotFoundException {
                        return bundle.loadClass(name);
                    }
                });
            } catch (PrivilegedActionException e) {
                throw ClassNotFoundException.class.cast(e.getException());
            }
        } else {
            return bundle.loadClass(name);
        }
    }

    private boolean isCompatible(Class providerClass, Class serviceClass) {
        try {
            // We can't do an isAssignable check, because the provider class may not be assignable to service class
            // in cases like JAXBContextFactory. So, we try to see if provider sees the same service class or not.
            // Apparently, they need not. e.g., consider the following situation:
            // Bundle B contains interface p.I.class.
            // Export-Package: p
            //
            // Bundle B1 contains class p1.C1.class, which implements p.I.class
            // Export-Package: p1; uses p
            // Import-Package: p
            //
            // Bundle B2 contains class p2.C2.class, which extends p1.C1.class
            // Import-Package: p1 (Note, it does not import p)
            //
            // Now, p2.C2.class.getClassLoader().loadClass("p.I") will fail.
            // In such a case, we shall return TRUE and catch any bad provider in lookupProviderInstances() method
            // which will again do an isAssignable test. See DefaultFactory.make() for example.
            final Class<?> serviceClassSeenByProviderClass = Class.forName(serviceClass.getName(), false, providerClass.getClassLoader());
            final boolean isCompatible = serviceClassSeenByProviderClass == serviceClass;
            if (!isCompatible) {
                debug(providerClass + " loaded by " + providerClass.getClassLoader()
                        + " sees " + serviceClass + " from " + serviceClassSeenByProviderClass.getClassLoader()
                        + ", where as caller uses " + serviceClass + " loaded by " + serviceClass.getClassLoader());
            }
            return isCompatible;
        } catch (ClassNotFoundException e) {
            debug("Unable to reach " + serviceClass + " from " + providerClass + ", which is loaded by " + providerClass.getClassLoader(), e);
            return true;
        }
    }

    /**
     * Loads a single service file and returns the names of the providers.
     * If the same provider appears multiple times, the list contains only one entry for all of the duplicates.
     *
     * @return names of providers, empty list if none is found
     */
    private List<String> load(InputStream is) throws IOException {
        List<String> providerNames = new ArrayList<String>();
        try {
            /*
             * The format of service file is specified at
             * http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider
             * According to the above spec,
             * The file contains a list of fully-qualified binary names of
             * concrete provider classes, one per line.
             * Space and tab characters surrounding each name,
             * as well as blank lines, are ignored.
             * The comment character is '#' ('\u0023', NUMBER SIGN);
             * on each line all characters following the first comment
             * character are ignored. The file must be encoded in UTF-8.
             */
            Scanner scanner = new Scanner(is);
            final String commentPattern = "#"; // NOI18N
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.startsWith(commentPattern)) {
                    StringTokenizer st = new StringTokenizer(line);
                    while (st.hasMoreTokens()) {
                        providerNames.add(st.nextToken());
                        break; // Only one entry per line
                    }
                }
            }
        } finally {
            is.close();
        }
        return providerNames;
    }

    private class BundleTracker implements BundleListener {
        public void bundleChanged(BundleEvent event) {
            Bundle bundle = event.getBundle();
            switch (event.getType()) {
                case BundleEvent.INSTALLED:
                    addProviders(bundle);
                    break;
                case BundleEvent.UNINSTALLED:
                    removeProviders(bundle);
                    break;
                case BundleEvent.UPDATED:
                    removeProviders(bundle);
                    addProviders(bundle);
                    break;
            }
        }
    }

    private void addProviders(Bundle bundle) {
        rwLock.writeLock().lock();
        try {
            final String SERVICE_LOCATION = "META-INF/services";
            if (bundle.getEntry(SERVICE_LOCATION) == null) return;
            Enumeration<String> entries;
            entries = bundle.getEntryPaths(SERVICE_LOCATION);
            if (entries != null) {
                ProvidersPerBundle providers = new ProvidersPerBundle(bundle.getBundleId());
                while (entries.hasMoreElements()) {
                    String entry = entries.nextElement();
                    String serviceName = entry.substring(SERVICE_LOCATION.length() + 1);
                    InputStream is;
                    final URL url = bundle.getEntry(entry);
                    try {
                        is = url.openStream();
                        List<String> providerNames = load(is);
                        debug("Bundle = " + bundle + ", serviceName = " + serviceName + ", providerNames = " + providerNames);
                        providers.put(serviceName, providerNames);
                    } catch (IOException e) {
                    }
                }
                providersList.addProviders(providers);
            }
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    private synchronized void removeProviders(Bundle bundle) {
        rwLock.writeLock().lock();
        try {
            providersList.removeProviders(bundle.getBundleId());
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Map of service name to provider names for a particular bundle
     */
    private static class ProvidersPerBundle {
        private long bundleId;
        Map<String, List<String>> serviceToProvidersMap = new HashMap<String, List<String>>();

        private ProvidersPerBundle(long bundleId) {
            this.bundleId = bundleId;
        }

        public long getBundleId() {
            return bundleId;
        }

        public void put(String serviceName, List<String> providerNames) {
            serviceToProvidersMap.put(serviceName, providerNames);
        }

        public Map<String, List<String>> getServiceToProvidersMap() {
            return serviceToProvidersMap;
        }
    }

    /**
     * List of {@link ServiceLoaderImpl.ProvidersPerBundle}
     * Synchronisation is handled by outer class.
     */
    private static class ProvidersList {
        // This list is sorted in ascending order of bundle id
        private List<ProvidersPerBundle> allProviders = new LinkedList<ProvidersPerBundle>();

        void addProviders(ProvidersPerBundle providers) {
            long bundleId = providers.getBundleId();
            int idx = 0;
            Iterator<ProvidersPerBundle> iterator = getAllProviders().iterator();
            while (iterator.hasNext()) {
                ProvidersPerBundle providersPerBundle = iterator.next();
                if (providersPerBundle.getBundleId() > bundleId) {
                    getAllProviders().add(idx, providers);
                    return;
                }
            }
            getAllProviders().add(providers);
        }

        void removeProviders(long bundleId) {
            Iterator<ProvidersPerBundle> iterator = getAllProviders().iterator();
            while (iterator.hasNext()) {
                ProvidersPerBundle providersPerBundle = iterator.next();
                if (providersPerBundle.getBundleId() == bundleId) {
                    iterator.remove();
                    return;
                }
            }
        }

        /**
         * @return a list of {@link ProvidersPerBundle} sorted in ascending order of bundle id.
         */
        public List<ProvidersPerBundle> getAllProviders() {
            return allProviders;
        }
    }

    private static class DefaultFactory<T> implements ProviderFactory<T> {
        public T make(Class providerClass, Class<T> serviceClass) throws Exception {
            if (serviceClass.isAssignableFrom(providerClass)) {
                return (T) providerClass.newInstance();
            }
            return null;
        }
    }

    private void debug(String s) {
        if (Boolean.valueOf(bundleContext.getProperty("org.glassfish.hk2.osgiresourcelocator.debug"))) {
            System.out.println("org.glassfish.hk2.osgiresourcelocator:DEBUG: " + s);
        }
    }

    private void debug(String s, Throwable t) {
        if (Boolean.valueOf(bundleContext.getProperty("org.glassfish.hk2.osgiresourcelocator.debug"))) {
            System.out.println("org.glassfish.hk2.osgiresourcelocator:DEBUG: " + s);
            t.printStackTrace(System.out);
        }
    }
}
