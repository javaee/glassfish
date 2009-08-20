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

    public static final String LEVEL_OFF = "OFF";
    public static final String LEVEL_LOW = "LOW";
    public static final String LEVEL_HIGH = "HIGH";

    public static final String CONNECTOR_CONNECTION_POOL = "connector-connection-pool";
    public static final String CONNECTOR_SERVICE = "connector-service";
    public static final String EJB_CONTAINER = "ejb-container";
    public static final String HTTP_SERVICE = "http-service";
    public static final String JDBC_CONNECTION_POOL = "jdbc-connection-pool";
    public static final String JMS_SERVICE = "jms-service";
    public static final String JVM = "jvm";
    public static final String ORB = "orb";
    public static final String THREAD_POOL = "thread-pool";
    public static final String TRANSACTION_SERVICE = "transaction-service";
    public static final String WEB_CONTAINER = "web-container";

    /**
     * The monitoring level of this monitoring item 
     * @return String with values HIGH/LOW/OFF
     */
    @Attribute (defaultValue="OFF")
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
