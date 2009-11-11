package org.glassfish.api.admin.config;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;

/**
 * An configured element which has to have application type of name.
 * 
 * @author Nandini Ektare
 */
@Configured
public interface ApplicationName extends ConfigBeanProxy {

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     */
    @Attribute(key=true)
    public String getName();

    public void setName(String value) throws PropertyVetoException;
    
}
