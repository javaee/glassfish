package org.glassfish.extras.grizzly;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.container.Adapter;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.tcp.Response;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Dec 2, 2008
 * Time: 4:20:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrizzlyAdapterApplication implements Adapter, ApplicationContainer {

    final com.sun.grizzly.tcp.Adapter adapter;
    final String contextRoot;
    final ClassLoader cl;

    public GrizzlyAdapterApplication(com.sun.grizzly.tcp.Adapter adapter, String contextRoot, ClassLoader cl) {
        this.adapter = adapter;
        this.contextRoot = contextRoot;
        this.cl = cl;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void service(Request req, Response res) throws Exception {
        adapter.service(req,res);
    }

    public void afterService(Request req, Response res) throws Exception {
        adapter.afterService(req, res);
    }

    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        return true;
    }

    public boolean suspend() {
        return false;
    }

    public boolean resume() throws Exception {
        return false;
    }

    public ClassLoader getClassLoader() {
        return cl;
    }
}
