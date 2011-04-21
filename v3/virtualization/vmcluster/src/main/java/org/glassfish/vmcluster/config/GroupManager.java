package org.glassfish.vmcluster.config;

import org.glassfish.api.Param;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * a group master identification (URL)
 */
@Configured
public interface GroupManager extends ConfigBeanProxy {

    @Attribute(key = true)
    public String getName();
    @Param(name="name", primary = true)
    public void setName(String name);

    @Attribute
    public String getUrl();
    @Param(name="url")
    public void setUrl(String url);

}
