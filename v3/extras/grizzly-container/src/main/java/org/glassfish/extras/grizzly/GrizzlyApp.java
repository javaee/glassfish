package org.glassfish.extras.grizzly;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.container.EndpointRegistrationException;

import com.sun.logging.LogDomains;

import java.util.Collection;
import java.util.logging.Level;

/**
 * Deployed grizzly application.
 *
 * @author Jerome Dochez
 */
public class GrizzlyApp implements ApplicationContainer {

    final ClassLoader cl;
    final Collection<Adapter> modules;
    final RequestDispatcher dispatcher;

    public static final class Adapter {
        final com.sun.grizzly.tcp.Adapter adapter;
        final String contextRoot;        
        public Adapter(String contextRoot, com.sun.grizzly.tcp.Adapter adapter) {
            this.adapter = adapter;
            this.contextRoot = contextRoot;
        }
    }

    public GrizzlyApp(Collection<Adapter> adapters, RequestDispatcher dispatcher, ClassLoader cl) {
        this.modules = adapters;
        this.dispatcher = dispatcher;
        this.cl = cl;
    }

    public Object getDescriptor() {
        return null;
    }

    public boolean start(ApplicationContext startupContext) throws Exception {
        for (Adapter module : modules) {
            dispatcher.registerEndpoint(module.contextRoot, module.adapter, this);
        }
        return true;
    }

    public boolean stop(ApplicationContext stopContext) {
        boolean success = true;
        for (Adapter module : modules) {
            try {
                dispatcher.unregisterEndpoint(module.contextRoot);
            } catch (EndpointRegistrationException e) {
                LogDomains.getLogger(getClass(), LogDomains.DPL_LOGGER).log(
                        Level.SEVERE, "Exception while unregistering adapter at " + module.contextRoot, e);
                success = false;
            }
        }
        return success;
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
