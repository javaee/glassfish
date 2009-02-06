package com.sun.enterprise.v3.server;

import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.*;
import org.glassfish.api.Startup;
import com.sun.enterprise.v3.services.impl.GrizzlyService;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ContainerRegistry;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.hk2.component.ExistingSingletonInhabitant;

/**
 * User: Jerome Dochez
 * Date: Apr 14, 2008
 * Time: 3:49:36 PM
 */
//@Service
public class ApplicationLoaderInjector implements Startup, PostConstruct, PreDestroy {

    @Inject
    Habitat habitat;

    ApplicationLoaderService service=null;

    /**
     * Returns the life expectency of the service
     *
     * @return the life expectency.
     */
    public Lifecycle getLifecycle() {
        return Lifecycle.SERVER;
    }

    /**
     * The component has been injected with any dependency and
     * will be placed into commission by the subsystem.
     */
    public void postConstruct() {
/*        service = new ApplicationLoaderService();
        service.server = habitat.getComponent(Server.class);
        service.applications = habitat.getComponent(Applications.class);
        service.archiveFactory = habitat.getComponent(ArchiveFactory.class);
        service.habitat = habitat;
        service.appRegistry = habitat.getComponent(ApplicationRegistry.class);
        Inhabitant<GrizzlyService> grizzly = habitat.getInhabitantByType(GrizzlyService.class);
        service.adapter = grizzly.get();

        service.modulesRegistry = habitat.getComponent(ModulesRegistry.class);
        service.containerRegistry = habitat.getComponent(ContainerRegistry.class);
        service.env = habitat.getComponent(ServerEnvironmentImpl.class);
        service.snifferManager = habitat.getComponent(SnifferManager.class);
        service.clh = habitat.getComponent(ClassLoaderHierarchy.class);
        habitat.add(new ExistingSingletonInhabitant<ApplicationLoaderService>(service));
        try {
            service.postConstruct();
        } catch(RuntimeException e) {
            grizzly.release();
            throw e;
        }
                    */
    }

    public void preDestroy() {
        if (service!=null) {
            service.preDestroy();
        }
    }
}
