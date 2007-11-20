package org.glassfish.api.naming;

import org.jvnet.hk2.annotations.Contract;

/**
 * A contract that describes a binding in the component namespace
 */

@Contract
public interface JNDIBinding {

    /**
     * Returns the logical name in the component namespace
     *
     * @return the logical name in the component namespace
     */
    public String getName();


    /**
     * Returns the value to be bound against the logical name
     *
     * @return the value to be bound against the logical name
     */
    public Object getValue();

}
