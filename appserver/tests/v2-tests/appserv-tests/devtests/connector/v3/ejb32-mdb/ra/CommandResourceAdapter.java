package com.sun.s1asdev.ejb.ejb32.mdb.ra;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Blevins
 */
public class CommandResourceAdapter implements ResourceAdapter {

    private Map<CommandActivationSpec, ActivatedEndpoint> endpoints = new HashMap<CommandActivationSpec, ActivatedEndpoint>();

    public void start(BootstrapContext bootstrapContext) throws ResourceAdapterInternalException {
    }

    public void stop() {
    }

    public void endpointActivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) throws ResourceException {
        final CommandActivationSpec commandActivationSpec = (CommandActivationSpec) activationSpec;
        final ActivatedEndpoint activatedEndpoint = new ActivatedEndpoint(messageEndpointFactory, commandActivationSpec);
        endpoints.put(commandActivationSpec, activatedEndpoint);
        final Thread thread = new Thread(activatedEndpoint);
        thread.setDaemon(true);
        thread.start();
    }

    public void endpointDeactivation(MessageEndpointFactory messageEndpointFactory, ActivationSpec activationSpec) {
        endpoints.remove((CommandActivationSpec) activationSpec);
    }

    public XAResource[] getXAResources(ActivationSpec[] activationSpecs) throws ResourceException {
        return new XAResource[0];
    }

    private static class ActivatedEndpoint implements Runnable {

        private final MessageEndpointFactory factory;
        private final List<Method> commands = new ArrayList<Method>();

        private ActivatedEndpoint(MessageEndpointFactory factory, CommandActivationSpec spec) {
            this.factory = factory;

            final Method[] methods = factory.getEndpointClass().getMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Command.class)) {
                    commands.add(method);
                }
            }

        }

        public void run() {
            pause();
            try {
                final MessageEndpoint endpoint = factory.createEndpoint(null);
                try {
                    for (Method method : commands) {
                        endpoint.beforeDelivery(method);
                        try {
                            method.invoke(endpoint);
                        } finally {
                            endpoint.afterDelivery();
                        }
                    }
                } finally {
                    endpoint.release();
                }
            } catch (Throwable e) {
                e.printStackTrace();
                // fail
            }
        }

        /**
         *  Have to wait till the application is fully started
         */
        private static void pause() {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }
}
