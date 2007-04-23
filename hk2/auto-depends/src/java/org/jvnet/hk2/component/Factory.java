package org.jvnet.hk2.component;

import org.jvnet.hk2.annotations.Service;

/**
 * Hook to create/obtain an instance of a component from
 * somewhere, instead of letting the auto-depends create it
 * via the constructor. 
 *
 * <p>
 * {@link Factory} is by itself a component, so the factory
 * implementation could have everything it needs injected.
 *
 * @author Kohsuke Kawaguchi
 * @see Service
 */
public interface Factory {
    /**
     * The system calls this method to obtain a reference
     * to the component.
     *
     * @return
     *      null is a valid return value. This is useful
     *      when a factory primarily does a look-up and it fails
     *      to find the specified component, yet you don't want that
     *      by itself to be an error. If the injection wants
     *      a non-null value (i.e., <tt>@Inject(optional=false)</tt>).
     * @throws ComponentException
     *      If the factory failed to get/create an instance
     *      and would like to propagate the error to the caller.
     */
    Object getObject() throws ComponentException;
}
