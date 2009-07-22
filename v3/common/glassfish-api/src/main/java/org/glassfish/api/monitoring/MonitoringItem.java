package org.glassfish.api.monitoring;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import javax.validation.constraints.NotNull;
import java.beans.PropertyVetoException;

/**
 * Marker Interface for modules that intend to provide some monitoring 
 * information.
 *
 * @author Nandini Ektare
 */
public interface MonitoringItem extends ConfigBeanProxy {

    /**
     * The monitoring level of this monitoring item 
     * @return String with values HIGH/LOW/OFF
     */
    @Attribute
    @NotNull
    public String getLevel();

    /**
     * The requested monitoring level has changed.
     * @param the new monitoring level
     */

    public void setLevel(String level) throws PropertyVetoException;
}
