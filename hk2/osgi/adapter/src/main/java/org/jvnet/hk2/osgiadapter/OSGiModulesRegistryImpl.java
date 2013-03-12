/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2012 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.SynchronousBundleListener;

import com.sun.enterprise.module.Module;
import com.sun.enterprise.module.ModuleDefinition;

/**
 * This is an implementation of {@link com.sun.enterprise.module.ModulesRegistry}.
 * It uses OSGi extender pattern to do necessary parsing of OSGi bundles.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiModulesRegistryImpl
        extends AbstractOSGiModulesRegistryImpl
        implements SynchronousBundleListener {

    ModuleDefinitionCacheSingleton cache = ModuleDefinitionCacheSingleton.getInstance();

    /*package*/ OSGiModulesRegistryImpl(BundleContext bctx) {
        super(bctx);

        // Need to add a listener so that we get notification about
        // bundles that get installed/uninstalled from now on...
        // This must happen before we start iterating the existing bundles.
        bctx.addBundleListener(this);

        // Populate registry with pre-installed bundles
        for (final Bundle b : bctx.getBundles()) {
            if (b.getLocation().equals (org.osgi.framework.Constants.SYSTEM_BUNDLE_LOCATION)) {
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

        if (cache.isCacheInvalidated()) {
            try {
                cache.saveCache();
            } catch (IOException e) {
                logger.logp(Level.WARNING, "OSGiModulesRegistryImpl", "OSGiModulesRegistryImpl", "Could not save module definition cache",e);
            }
        }
    }

    public void bundleChanged(BundleEvent event) {
        // Extender implementation.
        try {
            final Bundle bundle = event.getBundle();
            switch (event.getType()) {
                case BundleEvent.INSTALLED : {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("[" + bundle.getBundleId() + "] " + bundle.getSymbolicName() +  " installed");
                    }
                    break;
                } 

                case BundleEvent.RESOLVED :
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
            logger.logp(Level.WARNING, "OSGiModulesRegistryImpl", "bundleChanged", e.getMessage(), e);
        }
    }

    // Factory method
    private OSGiModuleImpl makeModule(Bundle bundle) throws IOException, URISyntaxException {
        final OSGiModuleDefinition md = makeModuleDef(bundle);

        OSGiModuleImpl m = new OSGiModuleImpl(this, bundle, md);

        return m;
    }

    // Factory method
    private OSGiModuleDefinition makeModuleDef(Bundle bundle)
            throws IOException, URISyntaxException {
        URI key = OSGiModuleDefinition.toURI(bundle);

        ModuleDefinition md = cache.get(key);

        if (md != null) {
        	return OSGiModuleDefinition.class.cast(md);
        } else {
            cache.invalidate();
            md = new OSGiModuleDefinition(bundle);

            cache.cacheModuleDefinition(key, md);

            return (OSGiModuleDefinition) md;
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

        cache.remove(location);
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
            cache.saveCache();
        } catch (IOException e) {
            Logger.logger.log(Level.WARNING, "Cannot save metadata to cache", e);
            }

        bctx.removeBundleListener(this);

        super.shutdown();
    }

    protected String getProperty(String property) {
        String value = bctx.getProperty(property);
        // Check System properties to work around Equinox Bug:
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=320459
        if (value == null) value = System.getProperty(property);
        return value;
    }

}
