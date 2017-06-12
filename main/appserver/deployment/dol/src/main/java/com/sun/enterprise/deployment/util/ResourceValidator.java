package com.sun.enterprise.deployment.util;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.deployment.Application;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.deployment.common.DeploymentException;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.logging.annotation.LogMessageInfo;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by mskd on 6/9/17.
 */
@Service
public class ResourceValidator implements EventListener, ResourceValidatorVisitor {

    public static final Logger deplLogger = com.sun.enterprise.deployment.util.DOLUtils.deplLogger;

    @LogMessageInfo(
            message = "JNDI lookup failed for the resource with jndi name: {0}",
            level = "SEVERE",
            cause = "JNDI lookup for the specified resource failed.",
            action = "Create the necessary object before deploying the application.",
            comment = "For the method validateJNDIRefs of com.sun.enterprise.deployment.util.ResourceValidator."
    )
    private static final String RESOURCE_REF_JNDI_LOOKUP_FAILED = "AS-DEPLOYMENT-00026";

    String target;

    DeploymentContext dc;

    Application application;

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
            dc = (DeploymentContext) event.hook();
            application = dc.getModuleMetaData(Application.class);
            DeployCommandParameters commandParams = dc.getCommandParameters(DeployCommandParameters.class);
            target = commandParams.target;
            processResources();
        }
    }

    //TODO: Extract resources completely out of dc
    private void processResources() {
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.warType())) {
            accept(bd);
        }
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.carType())) {
            accept(bd);
        }
        for (BundleDescriptor bd : application.getBundleDescriptorsOfType(DOLUtils.ejbType())) {
            accept(bd);
        }
    }

    private void accept(BundleDescriptor bundleDescriptor) {
        if (bundleDescriptor instanceof JndiNameEnvironment) {
            JndiNameEnvironment nameEnvironment = (JndiNameEnvironment) bundleDescriptor;
            for (Iterator<ResourceReferenceDescriptor> itr = nameEnvironment.getResourceReferenceDescriptors().iterator(); itr.hasNext();) {
                accept(itr.next());
            }
            for (Iterator<MessageDestinationDescriptor> itr = bundleDescriptor.getMessageDestinations().iterator(); itr.hasNext();) {
                accept(itr.next());
            }
        }
    }

    private void accept(NamedDescriptor resRef) {
        validateJNDIRefs(resRef.getJndiName());
    }

    // check in the domain.xml and in the app scoped resources
    private void validateJNDIRefs(String jndiName) {
        if(!validateResource(target, jndiName)) {
            if (!validateAppScopedResource(jndiName, dc)) {
                deplLogger.log(Level.SEVERE, RESOURCE_REF_JNDI_LOOKUP_FAILED,
                        new Object[] {jndiName});
                throw new DeploymentException(String.format("JNDI resource not present: %s", jndiName));
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
    }

    // TODO: Understand application scoped resources and validate using resourcesList
    private boolean validateAppScopedResource(String jndiName, DeploymentContext dc) {
        Map<String, Map<String, List>> resourcesList =
                (Map<String, Map<String, List>>) dc.getTransientAppMetadata().get("app-scoped-resources-map");
        return false;
    }
}
