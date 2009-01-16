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
import org.glassfish.api.admin.ParameterNames;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;
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

    @Inject
    ApplicationRegistry appRegistry;

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

        final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                this.initSubContext(application, context);

        final Map<BundleDescriptor, Collection<EngineInfo>> containersPerBundle =
                new HashMap<BundleDescriptor, Collection<EngineInfo>>();

        context.getProps().put("SUB_CONTEXTS", contextPerModules);

        final LinkedList<ModuleInfo> modules = new LinkedList<ModuleInfo>();
        try {
            doOnAllBundles(application, new BundleBlock<ModuleInfo>() {
                public ModuleInfo doBundle(BundleDescriptor bundle) throws Exception {
                    ModuleInfo info = prepareBundle(bundle, contextPerModules.get(bundle.getModuleDescriptor()));
                    modules.add(info);
                    return info;
                }

            });
        } catch(Exception e) {

        }
        final String appName = context.getCommandParameters().getProperty(
                    ParameterNames.NAME);
        final ApplicationInfo appInfo = new ApplicationInfo(context.getSource(), appName, modules);

        appRegistry.add(appName, appInfo);
        context.getProps().put("CONTAINERS_PER_BUNDLE", containersPerBundle);

        return true;
    }

    private void doOnAllBundles(Application application, BundleBlock runnable) throws Exception {

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

    private ModuleInfo prepareBundle(final BundleDescriptor bundle, final ExtendedDeploymentContext bundleContext)
        throws Exception {

        LinkedList<EngineInfo> orderedContainers = null;

        ActionReport report = habitat.getComponent(ActionReport.class, "hk2-agent");

        try {
            // let's get the list of containers interested in this module
            orderedContainers = deployment.setupContainerInfos(sniffers, bundleContext, report);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return deployment.prepareModule(orderedContainers, bundle.getName(), bundleContext, report, null);
    }

    public ApplicationContainer load(Container container, DeploymentContext context) {

        Application application = context.getModuleMetaData(Application.class);
        
        final Map<ModuleDescriptor, ExtendedDeploymentContext> contextPerModules =
                (Map<ModuleDescriptor, ExtendedDeploymentContext>)
                    context.getProps().get("SUB_CONTEXTS");

        final Map<BundleDescriptor, Collection<EngineInfo>> containersPerBundle =
                (Map<BundleDescriptor, Collection<EngineInfo>>)
                    context.getProps().get("CONTAINERS_PER_BUNDLE");

       /* doOnAllBundles(application, new BundleBlock<Collection<EngineInfo>>() {
            public Collection<EngineInfo> doBundle(BundleDescriptor bundle) {
                containersPerBundle.put(bundle,
                        loadBundle(bundle, contextPerModules.get(bundle.getModuleDescriptor())));
                return null;
            }
        });        

         */
        return null;
    }

    public void unload(ApplicationContainer appContainer, DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clean(DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private interface BundleBlock<T> {

        public T doBundle(BundleDescriptor bundle) throws Exception;
    }

    public Map<ModuleDescriptor, ExtendedDeploymentContext> initSubContext(final Application application, final DeploymentContext context) {

        Map<ModuleDescriptor, ExtendedDeploymentContext> results = new HashMap<ModuleDescriptor, ExtendedDeploymentContext>();
        for (final BundleDescriptor bd : application.getBundleDescriptors()) {
            if (!results.containsKey(bd.getModuleDescriptor())) {
                final ReadableArchive subArchive;
                try {
                    subArchive = context.getSource().getSubArchive(bd.getModuleDescriptor().getArchiveUri());
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                    return null;
                }
                ExtendedDeploymentContext subContext = new DeploymentContextImpl(logger, context.getSource(), context.getCommandParameters(), env) {

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

                    @Override
                    public <T> T getModuleMetaData(Class<T> metadataType) {
                        try {
                            return metadataType.cast(bd);
                        } catch (Exception e) {
                            return context.getModuleMetaData(metadataType);
                        }
                    }
                };
                results.put(bd.getModuleDescriptor(), subContext);
            }
        }
        return results;
    }
}
