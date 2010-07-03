/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import static org.jvnet.hk2.osgiadapter.BundleEventType.valueOf;
import static org.jvnet.hk2.osgiadapter.Logger.logger;
import org.osgi.framework.*;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.cm.ConfigurationException;

import java.util.*;
import java.util.logging.Level;
import java.io.ByteArrayInputStream;
import java.io.File;

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

    private BundleContext ctx;

    private ModulesRegistry mr;

    private static final String pid = "org.jvnet.hk2.osgiadapter.StartupContextService";

    private HK2Main.StartupContextService startupContextService;

    // We can easily convert it into a ManagedServiceFactory to support
    // creation of multiple StartupContexts and Habitats - each habitat containing inhabitants
    // configured differently. Very useful to have multiple domains running in same JVM.
    private class StartupContextService implements ManagedService {
        private Habitat habitat;
        private ServiceRegistration habitatRegistration;
        private ServiceTracker osgiServiceTracker;
        private ServiceRegistration moduleStartupRegistration;
        private ModuleStartup startupService;

        // It is synchronized since it can be called from HK2Main.stop as well as config admin.
        public synchronized void updated(Dictionary props) throws ConfigurationException {
            if (props == null) {
                if (habitat == null) {
                    return; // initial event - ignore
                }
                destroyHabitat(habitat);
                habitat = null; // GC
                startupService = null; // GC
                if (moduleStartupRegistration != null) {
                    moduleStartupRegistration.unregister();
                    moduleStartupRegistration = null;
                    habitatRegistration.unregister();
                    habitatRegistration = null;
                }
            } else {
                StartupContext startupContext = new StartupContext(dict2Props(props));
                try {
                    habitat = createHabitat(mr, startupContext);
                    startupService = findStartupService(mr, habitat, startupContext.getStartupModuleName(), startupContext);
                    moduleStartupRegistration = ctx.registerService(ModuleStartup.class.getName(), startupService, props);
                } catch (BootException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private Habitat createHabitat(ModulesRegistry registry, StartupContext context) throws BootException {
            Habitat habitat = HK2Main.this.createHabitat(registry, context);
            createServiceTracker(habitat);
            // register Habitat as an OSGi service
            habitatRegistration = ctx.registerService(Habitat.class.getName(), habitat, context.getArguments());
            return habitat;
        }

        private void destroyHabitat(Habitat habitat) {
            stopServiceTracker(habitat);
            habitat.release();
        }

        private void createServiceTracker(Habitat habitat) {
            osgiServiceTracker = new ServiceTracker(ctx, new NonHK2ServiceFilter(), new HK2ServiceTrackerCustomizer(habitat));
            osgiServiceTracker.open(true);
        }

        /**
         * Stop service tracker associated with the given habitat
         * @param habitat
         */
        private void stopServiceTracker(Habitat habitat) {
            if (osgiServiceTracker != null) {
                osgiServiceTracker.close();
                osgiServiceTracker = null;
            }
        }

        private Properties dict2Props(Dictionary dict) {
            Properties props = new Properties();
            Enumeration e = dict.keys();
            while (e.hasMoreElements()) {
                String k = e.nextElement().toString();
                props.put(k, dict.get(k).toString());
            }
            return props;
        }

    }

    public void start(BundleContext context) throws Exception {
        this.ctx = context;
        logger.entering("HK2Main", "start", new Object[]{context});

//        ctx.addBundleListener(this); // used for debugging purpose

        OSGiFactoryImpl.initialize(ctx);

        mr = createModulesRegistry();

        Properties p = new Properties();
        p.setProperty(Constants.SERVICE_PID, pid);
        startupContextService = new StartupContextService();
        context.registerService(ManagedService.class.getName(), startupContextService, p);
    }

    protected ModulesRegistry createModulesRegistry() {
        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();
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

        if(startupContextService.startupService != null) {
            try {
                logger.info("Stopping " + startupContextService.startupService);
                startupContextService.startupService.stop();
            } catch (Exception e) {
                logger.log(Level.WARNING, "HK2Main:stop():Exception while stopping ModuleStartup service.", e);
            }
        }
        startupContextService.updated(null);

        // framework will automatically unregister when the bundle is stopped.
        // so. just make it null to assist GC.
        startupContextService = null;

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

            // let's get the list of implemented contracts
            String[] contractNames = (String[]) reference.getProperty("objectclass");
            if (contractNames != null && contractNames.length > 0) {
                // we will register this service under each contract it implements
                for (String contractName : contractNames) {
                    String name = (String) reference.getProperty("component.name");
                    if (name==null) {
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