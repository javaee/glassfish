package com.sun.enterprise.connectors.module;

import com.sun.logging.LogDomains;
import org.glassfish.api.deployment.Deployer;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PostConstruct;
import org.jvnet.hk2.component.PreDestroy;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service(name = "com.sun.enterprise.connectors.module.ConnectorContainer")
public class ConnectorContainer implements org.glassfish.api.container.Container, PostConstruct, PreDestroy {

    private static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    public void postConstruct() {
        logFine("postConstruct of ConnectorContainer");
    }

    public void preDestroy() {
        logFine("preDestroy of ConnectorContainer");
    }

    /**
     * Returns the Deployer implementation capable of deploying applications to this
     * container.
     *
     * @return the Deployer implementation
     */
    public Class<? extends Deployer> getDeployer() {
        return ConnectorDeployer.class;
    }

    /**
     * Returns a human redeable name for this container, this name is not used for
     * identifying the container but can be used to display messages belonging to
     * the container.
     *
     * @return a human readable name for this container.
     */
    public String getName() {
        return "connectors";
    }

    public void logFine(String message) {
        _logger.log(Level.FINE, message);
    }
}