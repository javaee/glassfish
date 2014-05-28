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

import static org.jvnet.hk2.osgiadapter.BundleEventType.valueOf;
import static org.jvnet.hk2.osgiadapter.Logger.logger;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.logging.Level;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorFileFinder;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.common_impl.TracingUtilities;

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

	// Not sure where this belongs now:
	public final String DEFAULT_NAME = "default";
	 
    // TODO(Sahoo): Change to use ServiceTracker for all ServiceRegistrations

    private BundleContext ctx;

    private ServiceRegistration mrReg;
    private Map<ServiceLocator, HabitatInfo> habitatInfos = new HashMap<ServiceLocator, HabitatInfo>();

    /**
     * Stores additional artifacts corresponding to each Habitat created by us.
     */
    private class HabitatInfo {
        private ServiceLocator serviceLocator;
        private ServiceRegistration habitatRegistration;
        private ServiceTracker osgiServiceTracker;
        private ServiceRegistration moduleStartupRegistration;
    }

    
    @Override
    public ServiceLocator createServiceLocator(ModulesRegistry mr,
                                               StartupContext context,
                                               List<PopulatorPostProcessor> postProcessors,
                                               DescriptorFileFinder descriptorFileFinder)
            throws BootException {

        HabitatInfo habitatInfo = new HabitatInfo();
        
        habitatInfo.serviceLocator = super.createServiceLocator(mr, context, postProcessors, descriptorFileFinder);
        createHK2ServiceTracker(habitatInfo);
        // register ServiceLocator as an OSGi service
        habitatInfo.habitatRegistration = ctx.registerService(ServiceLocator.class.getName(), habitatInfo.serviceLocator, context.getArguments());
        habitatInfos.put(habitatInfo.serviceLocator, habitatInfo);
        return habitatInfo.serviceLocator;
    }

    private void destroyHabitat(ServiceLocator serviceLocator) {
        HabitatInfo habitatInfo = habitatInfos.get(serviceLocator);
        if (habitatInfo == null) {
            return;
        }

        // run code in the reverse order
        habitatInfo.habitatRegistration.unregister();
        stopHK2ServiceTracker(habitatInfo);
        // AMX i shaving trouble if we release inhabitants. So temporarily disable this.
        // habitat.release();
        habitatInfo = null;
        habitatInfos.remove(serviceLocator);
    }

    private void createHK2ServiceTracker(HabitatInfo habitatInfo) {
        habitatInfo.osgiServiceTracker = new ServiceTracker(
                ctx, new NonHK2ServiceFilter(), new HK2ServiceTrackerCustomizer(habitatInfo.serviceLocator));
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

    protected ModulesRegistry createModulesRegistry() throws Exception {
        assert (mrReg == null);
        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();

        String hk2RepositoryUris = ctx.getProperty(Constants.HK2_REPOSITORIES);

        if (hk2RepositoryUris != null) {
            for (String s : hk2RepositoryUris.split("\\s")) {
                URI repoURI = URI.create(s);
                File repoDir = new File(repoURI);
                OSGiDirectoryBasedRepository repo = new OSGiDirectoryBasedRepository(repoDir.getAbsolutePath(), repoDir);
                repo.initialize();
                mr.addRepository(repo);
            }
        }
        String osgiRepositoryUris = ctx.getProperty(Constants.OBR_REPOSITORIES);
        if (osgiRepositoryUris != null && mr instanceof OSGiObrModulesRegistryImpl) {
            OSGiObrModulesRegistryImpl mr1 = (OSGiObrModulesRegistryImpl) mr;
            for (String s : osgiRepositoryUris.split("\\s")) {
                mr1.addObr(URI.create(s));
            }
        }
        
        mrReg = ctx.registerService(ModulesRegistry.class.getName(), mr, null);
        
        return mr;
    }

    @Override
    protected void defineParentClassLoader() throws BootException {
        // OSGi doesn't have this feature, so ignore it for now.
    }

    public void stop(BundleContext context) throws Exception {
        // When OSGi framework shuts down, it shuts down all started bundles, but the order is unspecified.
        // So, since we are going to shutdown the registry, it's better that we stop startup service just incase it is still running.
        // Similarly, we can release the habitat.

        // Execute code in reverse order w.r.t. start()

        // Take a copy to avoid ConcurrentModificationException. This will happen as destroHabitat removes the entry.
        for (HabitatInfo habitatInfo : new ArrayList<HabitatInfo>(habitatInfos.values())) {
            ModuleStartup startupService =
                    habitatInfo.serviceLocator.getService(ModuleStartup.class, DEFAULT_NAME);
            if (startupService != null) {
                try {
                    logger.info("Stopping " + startupService);
                    startupService.stop();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "HK2Main:stop():Exception while stopping ModuleStartup service.", e);
                }
            }
            destroyHabitat(habitatInfo.serviceLocator);
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
        /* (non-Javadoc)
         * @see org.osgi.framework.Filter#match(org.osgi.framework.ServiceReference)
         */
        @Override
        public boolean match(ServiceReference reference) {
            return (!ctx.getBundle().equals(reference.getBundle()));
        }

        /* (non-Javadoc)
         * @see org.osgi.framework.Filter#match(java.util.Dictionary)
         */
        public boolean match(Dictionary dictionary) {
            throw new RuntimeException("Unexpected method called");
        }

        /* (non-Javadoc)
         * @see org.osgi.framework.Filter#matches(java.util.Map)
         */
        public boolean matches(Map<String, ?> map) {
            throw new RuntimeException("Unexpected method called");
        }
        
        /* (non-Javadoc)
         * @see org.osgi.framework.Filter#matchCase(java.util.Dictionary)
         */
        public boolean matchCase(Dictionary dictionary) {
            throw new RuntimeException("Unexpected method called");
        }
        
        public String toString() {
            return "(objectClass=*)";
        }
    }

    private class HK2ServiceTrackerCustomizer implements ServiceTrackerCustomizer {
        private final ServiceLocator serviceLocator;

        private HK2ServiceTrackerCustomizer(ServiceLocator serviceLocator) {
            this.serviceLocator = serviceLocator;
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
            
            DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
            DynamicConfiguration config = dcs.createDynamicConfiguration();

            AbstractActiveDescriptor<Object> descriptor = BuilderHelper.createConstantDescriptor(object);
            
            
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
                      
                    try {
                        final Class<?> contractType =
                                Class.forName(contractName, false, object.getClass().getClassLoader());
                        descriptor.addContractType(contractType);
						
						if (name != null)
							descriptor.setName(name);

	                    logger.logp(Level.FINE, "HK2Main$HK2ServiceTrackerCustomizer",
	                            "addingService", "registering service = {0}, contract = {1}, name = {2}", new Object[]{
	                                    object, contractName, name});
					} catch (ClassNotFoundException e) {
						logger.log(Level.SEVERE, "Cannot resolve contract " + contractName, e);
					}
                    
                }
                
            } else {
                // this service does not implement a specific contract, let's register it by its type.
                logger.logp(Level.FINE, "HK2Main$HK2ServiceTrackerCustomizer",
                        "addingService", "registering service = {0}", object);
            }
            
            config.addActiveDescriptor(descriptor);

            config.commit();

            return descriptor;
        }

        public void modifiedService(ServiceReference reference, Object service) {
        }

        public void removedService(ServiceReference reference, final Object service) {

            DynamicConfigurationService dcs = serviceLocator.getService(DynamicConfigurationService.class);
            DynamicConfiguration config = dcs.createDynamicConfiguration();
            
            org.glassfish.hk2.api.Filter filter = new org.glassfish.hk2.api.Filter() {

                @Override
                public boolean matches(Descriptor d) {
                    return d.equals(service); // addingServices() returns the descriptor
                }

            };

            config.addUnbindFilter(filter);

            config.commit();
        }
    }
    
	public HK2Main() {
		super();
	}

}
