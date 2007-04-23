package org.jvnet.hk2.component;

/**
 * Extract look up for services exposed from module(s).
 *
 * @author Kohsuke Kawaguchi
 */
public interface ServiceLookup {

    /**
     * Returns a list of class implementing a specific contract interface.
     *
     * @param serviceClass the service interface
     * @return the list of classes implementing the passed interface
     */
    public abstract <T> Iterable<Class<? extends T>> getProvidersClass(Class<T> serviceClass);

}
