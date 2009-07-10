package org.glassfish.api.admin.config;

import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * Tag interface for container related configuration. Inheriting from this
 * interface allows glassfish to replicate the container configuration in
 * a clustered environment allowing per server configuration, etc...
 *
 * @author Jerome Dochez
 */
@Configured
public interface Container extends ConfigBeanProxy {
}
