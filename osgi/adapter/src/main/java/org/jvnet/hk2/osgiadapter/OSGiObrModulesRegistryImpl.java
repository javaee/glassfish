/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.module.Repository;
import org.osgi.framework.*;

import java.net.URI;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class OSGiObrModulesRegistryImpl extends AbstractOSGiModulesRegistryImpl implements SynchronousBundleListener {
    private final Logger logger = Logger.getLogger(getClass().getPackage().getName());
    private final ObrHandler obrHandler;

    OSGiObrModulesRegistryImpl(BundleContext bctx) {
        super(bctx);
        obrHandler = new ObrHandler(bctx);
    }

    public void addObr(URI obrUri) throws Exception {
        obrHandler.addRepository(obrUri);
    }

    @Override
    public void addRepository(Repository repository, int weight) {
        if (repository instanceof OSGiDirectoryBasedRepository) {
            try {
                obrHandler.addRepository(repository.getLocation());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        super.addRepository(repository, weight);
    }

    @Override
    public void addRepository(Repository repository) {
        addRepository(repository, 1);
    }

    @Override
    protected Module newModule(ModuleDefinition moduleDef) {
        Bundle alreadyDeployedBundle = getExistingBundle(moduleDef);
        return new OSGiObrModuleImpl(this, alreadyDeployedBundle, moduleDef);
    }

    @Override
    protected Module loadFromRepository(String name, String version) {
        final Bundle bundle = getObrHandler().deploy(name, version);
        return bundle!=null ? getModule(bundle) : null;
    }

    @Override
    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.INSTALLED:
                Module module = getModule(event.getBundle());
                if (module instanceof OSGiObrModuleImpl) {
                    OSGiObrModuleImpl.class.cast(module).setBundle(event.getBundle());
                    return;
                }
        }
    }

    @Override
    public void shutdown() {
        getObrHandler().close();
        List<Bundle> bundlesToUninstall = Collections.emptyList();// getBundlesToUninstall();
               
        logger.logp(Level.INFO, "OSGiObrModulesRegistryImpl", "shutdown", "bundlesToUninstall = {0}", new Object[]{
                Arrays.toString(getBundleIds(bundlesToUninstall).toArray())});
        for (Bundle bundle : bundlesToUninstall) {
            try {
                bundle.uninstall();
            } catch (BundleException e) {
                logger.logp(Level.WARNING, "OSGiObrModulesRegistryImpl", "shutdown",
                        "Exception while uninstalling bundle " + bundle.getBundleId(), e);
            }
        }
        super.shutdown();
    }

    /**
     * This method returns a list of bundles to be uninstalled when the modules registry is shutdown.
     * Since bundles are loaded on demand and we don't have a notion of garbage collection of bundles (it's
     * pretty difficult to unload things at runtime, but not that difficult to add new bundles), when
     * registry is shutdown, we unload any bundle that has been added on demand.
     *
     * @return a list of bundles to be uninstalled. Bundles should be uninstalled as they appear in the returned list.
     */
    private List<Bundle> getBundlesToUninstall() {
        List<Bundle> bundlesToUninstall = new ArrayList<Bundle>();
        for (Bundle bundle : bctx.getBundles()) {
            // TODO(Sahoo): When we implement SCOPE in GlassFish, we don't have to rely on this technique to
            // identify bundles that are loaded on demand.
            // Otherwise, use a bundle listener to collect bundle ids in loadFromRepository() and use the same.
            if (bundle.getLocation().startsWith(Constants.OBR_SCHEME)) {
                bundlesToUninstall.add(bundle);
            }
        }
        Collections.sort(bundlesToUninstall, new Comparator<Bundle>() {
            @Override
            public int compare(Bundle o1, Bundle o2) {
                return (int) (o2.getBundleId() - o1.getBundleId()); // reverse order
            }
        });
        return bundlesToUninstall;
    }

    private List<Long> getBundleIds(List<Bundle> bundlesToUninstall) {
        List<Long> ids = new ArrayList<Long>(bundlesToUninstall.size());
        for (Bundle bundle : bundlesToUninstall) {
            ids.add(bundle.getBundleId());
        }
        return ids;
    }

    ObrHandler getObrHandler() {
        return obrHandler;
    }

    /**
     * Return bundle corresponding to a given ModuleDefinition if such a bundle is already installed in
     * current OSGi framework. It does not install the bundle itself - it simply returns null if it does find it.
     * @param md
     * @return
     */
    private Bundle getExistingBundle(ModuleDefinition md) {
        final String mn = md.getName();
        for (Bundle b : bctx.getBundles()) {
            final String bsn = b.getSymbolicName();
            boolean nameMatching = (bsn == mn) || (bsn != null && bsn.equals(mn));
            if (nameMatching) {
                Version mv = Version.parseVersion(md.getVersion());
                final Version bv = b.getVersion();
                boolean versionMatching = bv.equals(mv);
                if (versionMatching) return b;
            }
        }
        return null;
    }
}
