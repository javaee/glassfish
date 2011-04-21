package org.glassfish.deployment.cloud;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Defines a Java EE service in the cloud
 * @author Jerome Dochez
 */
@Configured(name="java-ee-service")
public interface JavaEEService extends CloudService, ConfigBeanProxy {

    @Attribute(defaultValue = "2")
    String getMinInstances();
    void setMinInstances(String minInstances);

    @Attribute
    String getMaxInstances();
    void setMaxInstances(String maxInstances);

}
