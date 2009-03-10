package com.sun.enterprise.configapi.tests.extensibility;

import org.glassfish.api.admin.config.Container;
import org.glassfish.api.admin.config.Named;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;

/**
 * @author Jerome Dochez
 */
@Configured
public interface RandomContainer extends Container, Named {

    @Attribute
    public String getNumberOfRuntime();

    public void setNumberOfRuntime();

    @Element
    public RandomElement getRandomElement();

    public void setRandomElement(RandomElement randomElement);
}
