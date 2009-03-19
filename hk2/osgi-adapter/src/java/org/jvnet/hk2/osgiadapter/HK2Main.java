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
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import static org.jvnet.hk2.osgiadapter.BundleEventType.valueOf;
import static org.jvnet.hk2.osgiadapter.Logger.logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Dictionary;
import java.util.Properties;
import java.util.logging.Level;

/**
 * {@link org.osgi.framework.BundleActivator} that launches a Habitat and
 * hands the execution to {@link com.sun.enterprise.module.bootstrap.ModuleStartup}.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class HK2Main extends Main implements
        BundleActivator,
        SynchronousBundleListener {

    private BundleContext ctx;

    private ModulesRegistry mr;
    private Habitat habitat;

    /**
     * The startup module service.
     * e.g., GlassFish Kernel's AppServerStartup instance
     */
    private ModuleStartup moduleStartup;

    public void start(BundleContext context) throws Exception {
        this.ctx = context;
        logger.logp(Level.FINE, "HK2Main", "run",
                "Thread.currentThread().getContextClassLoader() = {0}",
                Thread.currentThread().getContextClassLoader());
        logger.logp(Level.FINE, "HK2Main", "run", "this.getClass().getClassLoader() = {0}", this.getClass().getClassLoader());
        ctx.addBundleListener(this);

        // get the startup context from the System properties
        String lineformat = System.getProperty("hk2.startup.context.args");
        String contextRoot = System.getProperty("hk2.startup.context.root", "user.dir");
        File contextRootDir = new File(contextRoot);
        StartupContext startupContext;
        if (lineformat != null) {
            Properties arguments = new Properties();
            /**
             * for jdk6 switch to this
             */
            //StringReader reader = new StringReader(lineformat);
            //arguments.load(reader);
            ByteArrayInputStream is = new ByteArrayInputStream(lineformat.getBytes());
            arguments.load(is);
            startupContext = new StartupContext(contextRootDir, arguments);
        } else {
            startupContext = new StartupContext(contextRootDir, new String[0]);
        }

        OSGiFactoryImpl.initialize(ctx);

        mr = createModulesRegistry();
        habitat = createHabitat(mr, startupContext);
        createServiceTracker(habitat);
        moduleStartup = launch(mr, habitat, null, startupContext);
    }

    protected ModulesRegistry createModulesRegistry() {
        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();
        return mr;
    }

    @Override
    protected void setParentClassLoader(StartupContext context, ModulesRegistry mr) throws BootException {
        // OSGi doesn't have this feature, so ignore it for now.
    }

    private void createServiceTracker(Habitat habitat) {
        ServiceTracker st = new ServiceTracker(ctx, new NonHK2ServiceFilter(), new HK2ServiceTrackerCustomizer(habitat));
        st.open(true);
    }

    public void stop(BundleContext context) throws Exception {
        // Execute code in reverse order w.r.t. start()
        moduleStartup.stop();
        if (mr != null) {
            mr.shutdown();
        }
    }

    public void bundleChanged(BundleEvent event) {
        logger.logp(Level.FINE, "BundleListenerImpl", "bundleChanged",
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
                    // let's get a name if possible, that will only work with Spring OSGi services
                    // we may need to find a better way to get a potential name.
                    String name = (String) reference.getProperty("org.springframework.osgi.bean.name");
                    habitat.addIndex(new ExistingSingletonInhabitant(object), contractName, name);
                    logger.logp(Level.INFO, "HK2Main$HK2ServiceTrackerCustomizer",
                            "addingService", "registering service = {0}, contract = {1}, name = {2}", new Object[]{
                            object, contractName, name});
                }
            } else {
                // this service does not implement a specific contract, let's register it by its type.
                habitat.add(new ExistingSingletonInhabitant(object));
                logger.logp(Level.INFO, "HK2Main$HK2ServiceTrackerCustomizer",
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
                    logger.logp(Level.INFO, "HK2Main$HK2ServiceTrackerCustomizer",
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