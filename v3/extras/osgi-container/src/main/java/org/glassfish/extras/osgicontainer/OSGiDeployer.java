package org.glassfish.extras.osgicontainer;

import org.glassfish.internal.deployment.GenericDeployer;
import org.glassfish.internal.deployment.GenericApplicationContainer;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.*;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.PreDestroy;
import com.sun.enterprise.module.*;

import java.util.Collection;

/**
 * OSGi deployer, takes care of loading and cleaning modules from the OSGi runtime.
 *
 * @author Jerome Dochez
 */
@Service
public class OSGiDeployer implements Deployer<OSGiContainer, OSGiDeployedBundle> {

    @Inject
    ModulesRegistry registry;

    @Inject
    OSGiArchiveHandler archiveHandler;

    public OSGiDeployedBundle load(OSGiContainer container, DeploymentContext context) {

        Collection<Module> modules = registry.getModules(context.getAppProps().getProperty("module-name"));
        final Module module = (modules.size()>0?modules.iterator().next():null);
        if (module==null) {
            throw new RuntimeException("Cannot install OSGi bundle in repository, is this an OSGi bundle ?"); 
        }
        // I am ensure I have a RefCountingClassLoader...
        final RefCountingClassLoader loader = archiveHandler.getClassLoader(null, module);
        // and release the one I got from the context.
        if (context.getFinalClassLoader() instanceof PreDestroy) {
            ((PreDestroy) context.getFinalClassLoader()).preDestroy();
        }

        return new OSGiDeployedBundle(module, loader);
    }

    public void unload(OSGiDeployedBundle appContainer, DeploymentContext context) {
        appContainer.cl.preDestroy();
        context.addModuleMetaData(appContainer.m);
    }



    public void clean(DeploymentContext context) {
        OpsParams params = context.getCommandParameters(OpsParams.class);
        if (params!=null) {
            if (params.origin== OpsParams.Origin.undeploy) {
                Module m = context.getModuleMetaData(Module.class);
                if (m!=null && m.getState()!= ModuleState.NEW) {
                    m.uninstall();
                }
            }
        }

    }

    public MetaData getMetaData() {
        return null;
    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    public boolean prepare(DeploymentContext context) {
        return true; 
    }
}
