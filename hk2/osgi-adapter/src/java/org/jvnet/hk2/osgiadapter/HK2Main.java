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

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.common_impl.TracingUtilities;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static org.jvnet.hk2.osgiadapter.BundleEventType.valueOf;
import static org.jvnet.hk2.osgiadapter.Logger.logger;

/**
 * {@link org.osgi.framework.BundleActivator} that launches a Habitat.
 * A habitat is a collection of inhabitants, which are configured in a certain way.
 * So, there is a one-to-one mapping between habitat and configuration file used to configure the inhabitants.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class HK2Main extends Main implements
        BundleActivator,
        SynchronousBundleListener {

    // TODO(Sahoo): Change to use ServiceTracker for all ServiceRegistrations

    private BundleContext ctx;

    private ServiceRegistration mrReg;
    private Map<Habitat, HabitatInfo> habitatInfos = new HashMap<Habitat, HabitatInfo>();

    /**
     * Stores additional artifacts corresponding to each Habitat created by us.
     */
    private class HabitatInfo {
        private Habitat habitat;
        private ServiceRegistration habitatRegistration;
        private ServiceTracker osgiServiceTracker;
        private ServiceRegistration moduleStartupRegistration;
    }

    public Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
        HabitatInfo habitatInfo = new HabitatInfo();
        habitatInfo.habitat = super.createHabitat(registry, context);
        createHK2ServiceTracker(habitatInfo);
        // register Habitat as an OSGi service
        habitatInfo.habitatRegistration = ctx.registerService(Habitat.class.getName(), habitatInfo.habitat, context.getArguments());
        habitatInfos.put(habitatInfo.habitat, habitatInfo);
        return habitatInfo.habitat;
    }

    private void destroyHabitat(Habitat habitat) {
        HabitatInfo habitatInfo = habitatInfos.get(habitat);
        if (habitatInfo == null) {
            return;
        }

        // run code in the reverse order
        habitatInfo.habitatRegistration.unregister();
        stopHK2ServiceTracker(habitatInfo);
        // AMX i shaving trouble if we release inhabitants. So temporarily disable this.
        // habitat.release();
        habitatInfo = null;
        habitatInfos.remove(habitat);
    }

    private void createHK2ServiceTracker(HabitatInfo habitatInfo) {
        habitatInfo.osgiServiceTracker = new ServiceTracker(
                ctx, new NonHK2ServiceFilter(), new HK2ServiceTrackerCustomizer(habitatInfo.habitat));
        habitatInfo.osgiServiceTracker.open(true);
    }

    /**
     * Stop service tracker associated with the given habitat
     *
     * @param habitatInfo
     */
    private void stopHK2ServiceTracker(HabitatInfo habitatInfo) {
        if (habitatInfo.osgiServiceTracker != null) {
            habitatInfo.osgiServiceTracker.close();
            habitatInfo.osgiServiceTracker = null;
        }
    }

    public void start(BundleContext context) throws Exception {
        this.ctx = context;
        logger.entering("HK2Main", "start", new Object[]{context});

//        ctx.addBundleListener(this); // used for debugging purpose

        OSGiFactoryImpl.initialize(ctx);

        ModulesRegistry mr = createModulesRegistry();
        if (TracingUtilities.isEnabled())
            registerBundleDumper(mr);

        ctx.registerService(Main.class.getName(), this, null);
    }

    private void registerBundleDumper(final ModulesRegistry mr) {

        ctx.addBundleListener(new SynchronousBundleListener() {
            public void bundleChanged(final BundleEvent event) {
                switch (event.getType()) {
                    case BundleEvent.RESOLVED:
                        TracingUtilities.traceResolution(mr,
                                event.getBundle().getBundleId(),
                                event.getBundle().getSymbolicName(),
                                new TracingUtilities.Loader() {
                                    @Override
                                    public Class loadClass(String type) throws ClassNotFoundException {
                                        return event.getBundle().loadClass(type);
                                    }
                                });
                        break;

                    case BundleEvent.STARTED:
                        TracingUtilities.traceStarted(mr,
                                event.getBundle().getBundleId(),
                                event.getBundle().getSymbolicName(),
                                new TracingUtilities.Loader() {
                                    @Override
                                    public Class loadClass(String type) throws ClassNotFoundException {
                                        return event.getBundle().loadClass(type);
                                    }
                                });

                        break;
                }
            }
        });
    }

    protected ModulesRegistry createModulesRegistry() {
        assert (mrReg == null);
        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();
        mrReg = ctx.registerService(ModulesRegistry.class.getName(), mr, null);
        return mr;
    }

    @Override
    protected void setParentClassLoader(StartupContext context, ModulesRegistry mr) throws BootException {
        // OSGi doesn't have this feature, so ignore it for now.
    }

    public void stop(BundleContext context) throws Exception {
        // When OSGi framework shuts down, it shuts down all started bundles, but the order is unspecified.
        // So, since we are going to shutdown the registry, it's better that we stop startup service just incase it is still running.
        // Similarly, we can release the habitat.

        // Execute code in reverse order w.r.t. start()

        for (HabitatInfo habitatInfo : habitatInfos.values()) {
            ModuleStartup startupService =
                    habitatInfo.habitat.getComponent(ModuleStartup.class, habitatInfo.habitat.DEFAULT_NAME);
            if (startupService != null) {
                try {
                    logger.info("Stopping " + startupService);
                    startupService.stop();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "HK2Main:stop():Exception while stopping ModuleStartup service.", e);
                }
            }
            destroyHabitat(habitatInfo.habitat);
        }

        ModulesRegistry mr = (ModulesRegistry) ctx.getService(mrReg.getReference());
        if (mr != null) {
            mr.shutdown();
            mr = null;
        }
//        ctx.removeBundleListener(this); // used for debugging only. see start method

    }

    public void bundleChanged(BundleEvent event) {
        logger.logp(Level.FINE, "HK2Main", "bundleChanged",
                "source= {0}, type= {1}", new Object[]{event.getSource(),
                        valueOf(event.getType())});
    }

    private class NonHK2ServiceFilter implements Filter {
        public boolean match(ServiceReference serviceReference) {
            return (!ctx.getBundle().equals(serviceReference.getBundle()));
        }

        public boolean match(Dictionary dictionary) {
            throw new RuntimeException("Unexpected method called");
        }

        public boolean matchCase(Dictionary dictionary) {
            throw new RuntimeException("Unexpected method called");
        }

        public String toString() {
            return "(objectClass=*)";
        }
    }

    private class HK2ServiceTrackerCustomizer implements ServiceTrackerCustomizer {
        private final Habitat habitat;

        private HK2ServiceTrackerCustomizer(Habitat habitat) {
            this.habitat = habitat;
        }

        public Object addingService(final ServiceReference reference) {
            final Object object = ctx.getService(reference);

            if (object == null) {
                // service obuject can be null if the service is created using a factory and the factory fails to
                // create for whatever reason. In such a case, gracefully handle the situation instead of us failing.
                // See GLASSFISH-17398 for example.
                logger.logp(Level.INFO, "HK2Main$HK2ServiceTrackerCustomizer", "addingService",
                        "Skipping registration of inhabitant for service reference {0} " +
                                "as the service object could not be obtained.", new Object[]{reference});
                return null;
            }

            // let's get the list of implemented contracts
            String[] contractNames = (String[]) reference.getProperty("objectclass");
            if (contractNames != null && contractNames.length > 0) {
                // we will register this service under each contract it implements
                for (String contractName : contractNames) {
                    String name = (String) reference.getProperty("component.name");
                    if (name == null) {
                        // let's get a name if possible, that will only work with Spring OSGi services
                        // we may need to find a better way to get a potential name.
                        name = (String) reference.getProperty("org.springframework.osgi.bean.name");
                    }
                    habitat.addIndex(new ExistingSingletonInhabitant(object), contractName, name);
                    logger.logp(Level.FINE, "HK2Main$HK2ServiceTrackerCustomizer",
                            "addingService", "registering service = {0}, contract = {1}, name = {2}", new Object[]{
                                    object, contractName, name});
                }
            } else {
                // this service does not implement a specific contract, let's register it by its type.
                habitat.add(new ExistingSingletonInhabitant(object));
                logger.logp(Level.FINE, "HK2Main$HK2ServiceTrackerCustomizer",
                        "addingService", "registering service = {0}", object);
            }
            return object;
        }

        public void modifiedService(ServiceReference reference, Object service) {
        }

        public void removedService(ServiceReference reference, Object service) {
            // we need to unregister the service for each contract it implements.
            String[] contractNames = (String[]) reference.getProperty("objectclass");
            if (contractNames != null && contractNames.length > 0) {
                for (String contractName : contractNames) {
                    habitat.removeIndex(contractName, service);
                    logger.logp(Level.FINE, "HK2Main$HK2ServiceTrackerCustomizer",
                            "removingService", "removing service = {0}, contract = {1}",
                            new Object[]{service, contractName});

                }
            } else {
                // it was registered by type
                Inhabitant<?> inhabitant = habitat.getInhabitantByType(service.getClass());
                if (inhabitant != null) {
                    habitat.remove(inhabitant);
                } else {
                    logger.logp(Level.WARNING, "HK2Main$HK2ServiceTrackerCustomizer",
                            "removedService", "cannot removed singleton service = {0}", service);
                }
            }
        }

    }

}
