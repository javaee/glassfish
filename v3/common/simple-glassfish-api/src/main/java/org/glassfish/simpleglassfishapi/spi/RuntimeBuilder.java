package org.glassfish.simpleglassfishapi.spi;

import org.glassfish.simpleglassfishapi.*;


/**
 * Internal interface. Not for public use.
 * This is an SPI for GlassFishRuntime. Different implementations exist to provide different runtime
 * enviornment such as Felix/Equinox based or non-OSGi based runtime.
 */
public interface RuntimeBuilder {

    /**
     * Builds a custom GlassFishRuntime with the supplied bootstrap options
     * @param options
     * @return
     * @throws GlassFishException
     */
    GlassFishRuntime build(BootstrapOptions options) throws GlassFishException;

    /**
     * Returns true if this RuntimeBuilder is capable of creating a GlassFishRuntime
     * for the supplied BootstrapOptions
     * @param options
     * @return
     */
    boolean handles(BootstrapOptions options);

    /**
     * Destroy the RuntimeBuilder and follow up with cleaing up operations
     * @throws Exception
     */
    void destroy() throws GlassFishException;
}
