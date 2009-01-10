package org.glassfish.javaee.core.deployment;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 8, 2009
 * Time: 11:00:31 AM
 * To change this template use File | Settings | File Templates.
 */

public class EarContainer implements Container {

    public Class<? extends Deployer> getDeployer() {
        return EarDeployer.class;
    }

    public String getName() {
        return "application";
    }
}
