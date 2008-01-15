package org.glassfish.api.container;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.deployment.Deployer;

/**
 * Temporary @Contract for config integration.
 */
@Contract
public interface ContainerProvider {

    public Class<? extends Deployer> getDeployer();

    public String getName();
}
