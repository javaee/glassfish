package org.glassfish.api.admin.config;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import javax.validation.constraints.NotNull;
import java.beans.PropertyVetoException;

/**
 * An configured element which has to have application type of name.
 * 
 * @author Nandini Ektare
 */
@Configured
public interface ApplicationName extends Named {

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     */
    @Override
    @NotNull
    @Attribute(required=true)
    public String getName();

    @Override
    public void setName(String value) throws PropertyVetoException;
    
}
