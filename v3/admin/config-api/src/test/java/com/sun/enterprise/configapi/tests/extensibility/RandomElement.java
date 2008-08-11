package com.sun.enterprise.configapi.tests.extensibility;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Attribute;
import org.glassfish.api.admin.config.Named;

/**
 * @author Jerome Dochez
 */
@Configured
public interface RandomElement extends ConfigBeanProxy {

    @Attribute
    public String getAttr1();

    public void setAttr1(String string);
}
