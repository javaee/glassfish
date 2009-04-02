package org.glassfish.extras.osgicontainer;

import org.glassfish.internal.deployment.GenericDeployer;
import org.glassfish.internal.deployment.GenericApplicationContainer;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.Module;

import java.util.Collection;

/**
 * OSGi deployer, takes care of loading and cleaning modules from the OSGi runtime.
 *
 * @author Jerome Dochez
 */
@Service
public class OSGiDeployer extends GenericDeployer {

    @Inject
    ModulesRegistry registry;

    @Override
    public GenericApplicationContainer load(Container container, DeploymentContext context) {

        Collection<Module> modules = registry.getModules(context.getProps().getProperty("module-name"));
        final Module module = (modules.size()>0?modules.iterator().next():null);

        return new GenericApplicationContainer(context.getFinalClassLoader()) {
            @Override
            public boolean start(ApplicationContext startupContext) throws Exception {
                if (module!=null) {
                    module.start();
                }
                return true;
            }

            @Override
            public boolean stop(ApplicationContext stopContext) {
                if (module!=null) {
                    module.stop();
                }
                return true;
            }
        };
    }

    @Override
    public void clean(DeploymentContext context) {
        UndeployCommandParameters Params = context.getCommandParameters(UndeployCommandParameters.class);

        Collection<Module> modules = registry.getModules(context.getProps().getProperty("module-name"));
        final Module module = (modules.size()>0?modules.iterator().next():null);

        if (module!=null) {
            module.uninstall();
        }

    }
}
