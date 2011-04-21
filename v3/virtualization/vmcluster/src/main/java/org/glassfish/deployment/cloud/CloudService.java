package org.glassfish.deployment.cloud;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Tag interface for cloud services configurations
 * @author Jerome Dochez
 */
@Configured
public interface CloudService extends ConfigBeanProxy {
}
