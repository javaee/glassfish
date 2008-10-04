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
     FIXME: should set 'key=true'.  See bugs 6039, 6040
     */
    @Attribute(required=true)
    String getName();

    public void setName(String value) throws PropertyVetoException;
    
}
