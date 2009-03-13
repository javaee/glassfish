package org.glassfish.tests.kernel.deployment.container;

import org.glassfish.api.deployment.*;
import org.glassfish.api.container.Container;
import org.jvnet.hk2.annotations.Service;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Mar 12, 2009
 * Time: 9:24:43 AM
 * To change this template use File | Settings | File Templates.
 */
@Service
public class FakeDeployer implements Deployer {

    public MetaData getMetaData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Object loadMetaData(Class type, DeploymentContext context) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean prepare(DeploymentContext context) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ApplicationContainer load(Container container, DeploymentContext context) {
        return new ApplicationContainer() {
            public Object getDescriptor() {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean start(ApplicationContext startupContext) throws Exception {
                return true;
            }

            public boolean stop(ApplicationContext stopContext) {
                return true;
            }

            public boolean suspend() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean resume() throws Exception {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public ClassLoader getClassLoader() {
                return FakeDeployer.class.getClassLoader();
            }
        };
    }

    public void unload(ApplicationContainer appContainer, DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void clean(DeploymentContext context) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
