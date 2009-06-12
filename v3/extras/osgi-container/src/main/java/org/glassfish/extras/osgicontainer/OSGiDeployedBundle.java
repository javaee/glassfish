package org.glassfish.extras.osgicontainer;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import com.sun.enterprise.module.Module;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jun 11, 2009
 * Time: 3:58:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class OSGiDeployedBundle implements ApplicationContainer<OSGiContainer> {

    final RefCountingClassLoader cl;
    final Module m;

    public OSGiDeployedBundle(Module m,RefCountingClassLoader cl) {
        this.cl = cl;
        this.m = m;
    }

    public OSGiContainer getDescriptor() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        m.start();
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        m.stop();
        return true;
    }

    public boolean suspend() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean resume() throws Exception {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ClassLoader getClassLoader() {
        return cl;
    }
}
