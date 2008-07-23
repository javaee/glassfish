package org.glassfish.api.admin.config;

import org.jvnet.hk2.config.Attribute;

import java.beans.PropertyVetoException;

/**
 * An configured element which is named.
 * 
 * @author Jerome Dochez
 */
public interface Named {

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     */
    @Attribute(required=true)
    String getName();

    public void setName(String value) throws PropertyVetoException;
    
}
