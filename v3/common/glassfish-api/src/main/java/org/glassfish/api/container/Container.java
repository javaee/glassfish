package org.glassfish.api.container;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.deployment.Deployer;

/**
 * Contract identifying a container implementation.
 *
 * Usually the names of the container should be specific enough to ensure uniqueness.
 * In most cases, it is recommended to use the full class name as the @Service name
 * attribute to ensure that two containers do no collide.
 *
 * @author Jerome Dochez
 */
@Contract
public interface Container {

    /**
     * Returns the Deployer implementation capable of deploying applications to this
     * container.
     *
     * @return the Deployer implementation
     */
    public Class<? extends Deployer> getDeployer();

    /**
     * Returns a human redeable name for this container, this name is not used for
     * identifying the container but can be used to display messages belonging to
     * the container.
     * 
     * @return a human readable name for this container.
     */
    public String getName();
}
