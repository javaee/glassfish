package org.jvnet.hk2.config.test;

import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

@Configured
public interface GenericConfig extends ConfigBeanProxy {
    @Attribute(key = true)
    String getName();
    void setName(String name);

    @Element
    GenericConfig getGenericConfig();
    void setGenericConfig(GenericConfig genericConfig);

    @Element("*")
    List<GenericContainer> getExtensions();
}
