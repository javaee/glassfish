package org.glassfish.api.monitoring;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import javax.validation.constraints.NotNull;
import java.beans.PropertyVetoException;
import org.jvnet.hk2.config.Configured;

/**
 * Marker Interface for modules that intend to provide some monitoring 
 * information.
 *
 * @author Nandini Ektare
 */
@Configured
public interface MonitoringItem extends ConfigBeanProxy {

    /**
     * The monitoring level of this monitoring item 
     * @return String with values HIGH/LOW/OFF
     */
    @Attribute
    @NotNull
    public String getLevel();

    /**
     * Set the level of this monitoring module
     * @param new monitoring level
     */

    public void setLevel(String level) throws PropertyVetoException;

    /**
     * The name of the monitoring module that has this config
     * @return String name
     */
    @Attribute(key=true)
    @NotNull
    public String getName();

    /**
     * Set the name of this monitoring module
     * @param the monitoring name
     */
    public void setName(String name) throws PropertyVetoException;
}