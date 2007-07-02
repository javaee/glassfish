package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Contract;

/**
 * Extract look up for services exposed from module(s).
 *
 * @author Kohsuke Kawaguchi
 */
public interface ServiceLookup {

    /**
     * Returns a list of class implementing a specific contract interface.
     *
     * @param contractClass
     *      {@link Contract} class/interface for which the services are looked up. 
     * @return the list of classes implementing the passed interface
     */
    public abstract <T> Iterable<Class<? extends T>> getProvidersClass(Class<T> contractClass);

}
