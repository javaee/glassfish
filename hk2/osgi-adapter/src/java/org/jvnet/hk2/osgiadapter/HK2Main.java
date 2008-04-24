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

import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Repository;
import com.sun.enterprise.module.RepositoryChangeListener;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.BootException;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.common_impl.DirectoryBasedRepository;
import com.sun.hk2.component.ExistingSingletonInhabitant;
import org.jvnet.hk2.component.Habitat;
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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.logging.Level;

/**
 * {@link BundleActivator} that launches a Habitat and hands the execution to {@link ModuleStartup}.
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class HK2Main extends Main implements
        BundleActivator,
        SynchronousBundleListener {

    private BundleContext ctx;

    private ModulesRegistry mr;

    private String repName = "modules";

    private static final String CONTEXT_ROOT_DIR_PROP =
            HK2Main.class.getPackage().getName()+".contextrootdir";

    /**
     * <tt>$GLASSFISH_HOME/modules</tt> directory.
     */
    private File contextRootDir;

    public void start(BundleContext context) throws Exception {
        this.ctx = context;
        logger.logp(Level.FINE, "HK2Main", "run",
                "Thread.currentThread().getContextClassLoader() = {0}",
                Thread.currentThread().getContextClassLoader());
        logger.logp(Level.FINE, "HK2Main", "run", "this.getClass().getClassLoader() = {0}", this.getClass().getClassLoader());
        ctx.addBundleListener(this);

        // Create StartupContext
        contextRootDir = getContextRootDir(context);
        StartupContext startupContext = new StartupContext(contextRootDir, new String[0]);

        OSGiFactoryImpl.initialize(ctx);

        mr = createModulesRegistry();
        Habitat habitat = createHabitat(mr, startupContext);
        // createServiceTracker(habitat); Don't track service, as there are issues with GlassFish services
        launch(mr,habitat,null,startupContext);
    }

    protected ModulesRegistry createModulesRegistry() {
        ModulesRegistry mr = AbstractFactory.getInstance().createModulesRegistry();

        Collection<? extends Repository> reps = createRepositories();

        for (Repository rep : reps)
            mr.addRepository(rep);

        return mr;
    }

    protected File getContextRootDir(BundleContext context) {
        String prop = context.getProperty(CONTEXT_ROOT_DIR_PROP);
        File f = (prop !=null) ? new File(prop) : new File(System.getProperty("user.home"));
        logger.logp(Level.INFO, "HK2Main", "start", "contextRootDir = {0}", contextRootDir);
        return f;
    }
    
    @Override
    protected void setParentClassLoader(StartupContext context, ModulesRegistry mr) throws BootException {
        // OSGi doesn't have this feature, so ignore it for now.
    }

    private void createServiceTracker(Habitat habitat) {
        ServiceTracker st = new ServiceTracker(ctx, new NonHK2ServiceFilter(), new HK2ServiceTrackerCustomizer(habitat));
        st.open(true);
    }

    private Collection<? extends Repository> createRepositories() {
        List<Repository> reps = new ArrayList<Repository>();

        Repository rep = new DirectoryBasedRepository(repName, contextRootDir);
        try {
            rep.initialize();
            reps.add(rep);
        } catch (IOException e) {
            try {
                rep.shutdown();
            } catch (IOException e1) {
                // ignore as we are shutting down
            }
            throw new RuntimeException(e);
        }
        for (File dir : contextRootDir.listFiles(
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return pathname.isDirectory();
                    }
                }))
        {
            rep = new DirectoryBasedRepository(dir.getName(), dir);
            try {
                rep.initialize();
                reps.add(rep);
            } catch(IOException e) {
                try {
                    rep.shutdown();
                } catch (IOException e1) {
                    // ignore as we are shutting down
                }
                logger.log(Level.SEVERE, "Cannot initialize repository at " + dir.getAbsolutePath(), e);
            }
        }
        // add a listener for each repository
        // There is an interesting (and necessary) side effect of adding a
        // RespositoryChangeListener when this is done in GlassFish environment.
        // It is described below:
        // These repositories are configured such that when we add a listener,
        // there is a corresponding non-daemon timer thread spawned. These non-daemon timer
        // threads are the only non-daemon threads in GlassFish. They are stopped
        // when the repositories are shutdown, which happens either
        // when this bundle is stopped or when the ModulesRegistry.shutdown is called.
        // If we don't attach these listeners, GlassFish process would just exit
        // as soon as the OSGi framework is started because of absence of any
        // non-daemon threads. There should really be a non-daemon thread in GlassFish
        // which is stopped when stop-domain is issued.
        for (Repository repo : reps) {
            repo.addListener(new RepositoryChangeListener() {

                public void jarAdded(URI location) {
                    //TODO: Not Yet Implemented
                }

                public void jarRemoved(URI location) {
                    //TODO: Not Yet Implemented
                }

                public void moduleAdded(ModuleDefinition definition) {
                    //TODO: Not Yet Implemented
                }

                public void moduleRemoved(ModuleDefinition definition) {
                    //TODO: Not Yet Implemented
                }
            });
        }
        logger.exiting("HK2Main", "createRepositories", reps);
        return reps;
    }

    public void stop(BundleContext context) throws Exception {
        if(mr!=null) {
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
            habitat.add(new ExistingSingletonInhabitant(object));
            logger.logp(Level.INFO, "HK2Main$HK2ServiceTrackerCustomizer",
                    "addingService", "object = {0}", object);
            return object;
        }

        public void modifiedService(ServiceReference reference, Object service) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void removedService(ServiceReference reference, Object service) {
            // remove from habitat (not supported yet)
        }
    }
}
