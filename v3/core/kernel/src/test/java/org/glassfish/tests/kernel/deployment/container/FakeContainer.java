package org.glassfish.tests.kernel.deployment.container;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.jvnet.hk2.annotations.Service;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 12, 2009
 * Time: 9:22:42 AM
 * To change this template use File | Settings | File Templates.
 */
@Service(name="FakeContainer")
public class FakeContainer implements Container {

    public Class<? extends Deployer> getDeployer() {
        return FakeDeployer.class;
    }

    public String getName() {
        return "Fake";
    }
}
