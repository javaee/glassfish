package org.jvnet.hk2.config.model;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Example of a server definition, indexed by its name.
 */
@Configured
public interface Server extends ConfigBeanProxy {

    @Attribute(key=true)
    String getName();
    void setName();

    @Attribute(reference = true)
    Config getConfig();
    void setConfig(Config config);
}
