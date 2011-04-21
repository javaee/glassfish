package org.glassfish.vmcluster.config;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * Defines a notion of groups that can be used to deploy applications to.
 */
@Configured
public interface Groups extends ConfigBeanProxy {

    @Element
    List<String> getGroupNames();
}
