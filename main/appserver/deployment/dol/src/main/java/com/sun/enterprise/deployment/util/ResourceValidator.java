package com.sun.enterprise.deployment.util;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.Application;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.internal.deployment.Deployment;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Created by mskd on 6/9/17.
 */
@Service
public class ResourceValidator implements EventListener, ResourceValidatorVisitor {
    @Inject
    private Events events;

    @Inject
    Domain domain;

    public void postConstruct() {
        events.register(this);
    }

    @Override
    public void event(Event event) {
        if (event.is(Deployment.AFTER_APPLICATION_CLASSLOADER_CREATION)) {
            DeploymentContext dc = (DeploymentContext) event.hook();
            processResources(dc);
        }
    }

    //TODO: Extract resources out of dc
    private void processResources(DeploymentContext dc) {
        DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
        Application application = dc.getModuleMetaData(Application.class);
        validateJNDIRefs("", commandParams.target, dc);
    }

    private void validateJNDIRefs(String jndiName, String target, DeploymentContext dc) {
        if(!validateResource(target, jndiName)) {
            if (!validateAppScopedResource(jndiName, dc)) {
                //throw new IllegalStateException(String.format("JNDI resource not present: %s", jndiName));
            }
        }
    }

    private boolean validateResource(String target, String jndiName) {
        Server svr = domain.getServerNamed(target);
        if (svr != null) {
            return svr.isResourceRefExists(jndiName);
        }
        Cluster cluster = domain.getClusterNamed(target);
        if (cluster != null) {
            return cluster.isResourceRefExists(jndiName);
        }
        return false;
        // throw new IllegalStateException(String.format("Where am I? %s", target));
    }

    // TODO: Understand application scoped resources and validate using resourcesList
    private boolean validateAppScopedResource(String jndiName, DeploymentContext dc) {
        com.sun.enterprise.config.serverbeans.Application app = dc.getTransientAppMetaData(ServerTags.APPLICATION, com.sun.enterprise.config.serverbeans.Application.class);
        Map<String, Map<String, List>> resourcesList =
                (Map<String, Map<String, List>>) dc.getTransientAppMetadata().get("app-scoped-resources-map");

        List<Resource> resources = app.getResources().getResources();

        for (Resource resource: resources) {
            if (resource.getIdentity() == jndiName) return true;
        }
        return false;
    }


}
