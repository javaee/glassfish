package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;

/**
 * Tag Interface for any module
 *
 * @author Jerome Dochez
 */
@Configured
public interface Module extends ConfigBeanProxy {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

}
