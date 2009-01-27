package org.glassfish.api.admin.config;

import org.jvnet.hk2.annotations.Contract;

/**
 * Contract for services which want to perform some upgrade on the application server configuration
 *
 * @author Jerome Dochez
 */
@Contract
public interface ConfigurationUpgrade {
    // this is a tag contract, implementation should just implement a postConstruct method
    // to perform upgrade on injected configuration data
}
