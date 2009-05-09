package org.glassfish.api.deployment;

import org.glassfish.api.Param;

/**
 * parameters passed to commands changing the state of a deployed application
 */
public class StateCommandParameters extends OpsParams {

    @Param(primary=true)
    String component = null;

    @Param(optional=true)
    public String target = "server";

    public String name() {
        return component;
    }

    public String libraries() {
        throw new IllegalStateException("We need to be able to get access to libraries when enabling/disabling");
    }
}
