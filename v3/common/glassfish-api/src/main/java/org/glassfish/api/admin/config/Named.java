package org.glassfish.api.admin.config;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import javax.validation.constraints.NotNull;
import java.beans.PropertyVetoException;

/**
 * An configured element which is named.
 * 
 * @author Jerome Dochez
 */
@Configured
public interface Named extends ConfigBeanProxy {

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     */
    @Attribute(required=true, key=true)
    @NotNull
    public String getName();

    public void setName(String value) throws PropertyVetoException;
    
}
