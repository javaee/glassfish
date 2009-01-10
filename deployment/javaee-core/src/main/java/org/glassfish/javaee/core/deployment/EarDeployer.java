package org.glassfish.javaee.core.deployment;

import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.container.Container;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.PerLookup;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.util.ModuleDescriptor;
import com.sun.logging.LogDomains;

import java.util.*;
import java.util.logging.Logger;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 8, 2009
 * Time: 11:01:25 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
@Scoped(PerLookup.class)
public class EarDeployer implements Deployer {

    @Inject
    Habitat habitat;

    @Inject
    Deployment deployment;

    @Inject
    ServerEnvironment env;

    Collection<Sniffer> sniffers;

    final static Logger logger = LogDomains.getLogger(EarDeployer.class, LogDomains.DPL_LOGGER);
    
    public MetaData getMetaData() {
        return new MetaData(false, null, new Class[] { Application.class});
    }

    public Object loadMetaData(Class type, DeploymentContext context) {
        return null;
    }

    public boolean prepare(final DeploymentContext context) {

        Application application = context.getModuleMetaData(Application.class);

        final Map<ModuleDescriptor, DeploymentContext> contextPerModules =
                this.initSubContext(application, context);

        final Map<BundleDescriptor, Collection<EngineInfo>> containersPerBundle =
                new HashMap<BundleDescriptor, Collection<EngineInfo>>();

        context.getProps().put("SUB_CONTEXTS", contextPerModules);
        
        doOnAllBundles(application, new BundleBlock<Collection<EngineInfo>>() {
            public Collection<EngineInfo> doBundle(BundleDescriptor bundle) {
                containersPerBundle.put(bundle,
                        prepareBundle(bundle, contextPerModules.get(bundle.getModuleDescriptor())));
                return null;
            }
        });

        context.getProps().put("CONTAINERS_PER_BUNDLE", containersPerBundle);

        return true;
    }

    private void doOnAllBundles(Application application, BundleBlock runnable) {

        Collection<BundleDescriptor> bundles = new HashSet<BundleDescriptor>();
        bundles.addAll(application.getBundleDescriptors());
        
        // first we take care of the connectors
        for (ConnectorDescriptor connector : application.getBundleDescriptors(ConnectorDescriptor.class)) {
            bundles.remove(connector);
            runnable.doBundle(connector);
        }

        // now the EJBs
        for (EjbBundleDescriptor ejbBundle : application.getBundleDescriptors(EjbBundleDescriptor.class)) {
            bundles.remove(ejbBundle);
            runnable.doBundle(ejbBundle);
        }

        // finally the war files.
        for (final WebBundleDescriptor webBundle : application.getBundleDescriptors(WebBundleDescriptor.class)) {
            bundles.remove(webBundle);
            runnable.doBundle(webBundle);
        }

        // now ther remaining bundles
        for (final BundleDescriptor bundle : bundles) {
            runnable.doBundle(bundle);
        }
        
    }

    private Collection<EngineInfo> prepareBundle(final BundleDescriptor bundle, final DeploymentContext bundleContext) {

        HashSet<EngineInfo> orderedContainers = new HashSet<EngineInfo>();

        ActionReport report = habitat.getComponent(ActionReport.class, "hk2-agent");

        try {
            // let's get the list of containers interested in this module
            orderedContainers.addAll(deployment.setupContainerInfos(sniffers, bundleContext, report));
        } catch(Exception e) {
            e.printStackTrace();
        }

        for (EngineInfo engineInfo : orderedContainers) {
            Deployer deployer = engineInfo.getDeployer();
            deployer.prepare(bundleContext);
        }
        return orderedContainers;
    }

    public ApplicationContainer load(Container container, DeploymentContext context) {
        return null;
    }

    public void unload(ApplicationContainer appContainer, DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clean(DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private interface BundleBlock<T> {

        public T doBundle(BundleDescriptor bundle);
    }

    public Map<ModuleDescriptor, DeploymentContext> initSubContext(final Application application, final DeploymentContext context) {

        Map<ModuleDescriptor, DeploymentContext> results = new HashMap<ModuleDescriptor, DeploymentContext>();
        for (BundleDescriptor bd : application.getBundleDescriptors()) {
            if (!results.containsKey(bd.getModuleDescriptor())) {
                final ReadableArchive subArchive;
                try {
                    subArchive = context.getSource().getSubArchive(bd.getModuleDescriptor().getArchiveUri());
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    return null;
                }
                DeploymentContext subContext = new DeploymentContextImpl(logger, context.getSource(), context.getCommandParameters(), env) {

                    @Override
                    public ClassLoader getClassLoader() {
                        return context.getClassLoader();
                    }

                    @Override
                    public ClassLoader getFinalClassLoader() {
                        return context.getFinalClassLoader();
                    }

                    @Override
                    public ReadableArchive getSource() {
                        return subArchive;
                    }
                };
                results.put(bd.getModuleDescriptor(), subContext);
            }
        }
        return results;
    }
}
