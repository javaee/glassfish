package com.sun.enterprise.module;

import com.sun.enterprise.module.impl.AdapterIterator;
import com.sun.enterprise.module.impl.Utils;

import java.util.Iterator;
import java.util.logging.Level;

/**
 * Extract look up for services exposed from module(s).
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ServiceLookup implements org.jvnet.hk2.component.ServiceLookup {
    // no subclassing is allowed outside hk2
    ServiceLookup() {
    }

    /**
     * Returns instances of all the classes implementing the
     * service interface passed as a parameter.
     * 
     * @param serviceClass the service interface
     * @return an iterator of instances of classes implementing the passed
     * interface
     */
    public <T> Iterable<T> getProviders(final Class<T> serviceClass) {
        return new Iterable<T>() {
            final Iterable<Class<? extends T>> classes = getProvidersClass(serviceClass);

            public Iterator<T> iterator() {
                return new AdapterIterator<T,Class<? extends T>>(classes.iterator()) {
                    protected T adapt(Class<? extends T> providerClass) {
                        try {
                            return providerClass.newInstance();
                        } catch(java.lang.InstantiationException e) {
                            Utils.getDefaultLogger().log(Level.SEVERE, "Cannot instantiate " +serviceClass + " service "
                                    + serviceClass.getName() + " implementation : ", e);
                        } catch(IllegalAccessException e) {
                            Utils.getDefaultLogger().log(Level.SEVERE, "Cannot instantiate " + serviceClass + " service "
                                    + serviceClass.getName() + " implementation : ", e);
                        } catch(RuntimeException e) {
                            Utils.getDefaultLogger().log(Level.SEVERE, "Runtime exception while instantiating " +serviceClass + " service "
                                    + serviceClass.getName() + " implementation : ", e);
                        }
                        return null;
                    }
                };
            }
        };
    }
}
