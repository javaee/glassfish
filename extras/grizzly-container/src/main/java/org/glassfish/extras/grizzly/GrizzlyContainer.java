package org.glassfish.extras.grizzly;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 2, 2008
 * Time: 4:17:31 PM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="grizzly")
public class GrizzlyContainer implements Container {

    public Class<? extends Deployer> getDeployer() {
        return GrizzlyDeployer.class;
    }

    public String getName() {
        return "grizzly";
    }
}
