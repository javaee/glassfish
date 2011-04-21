package org.glassfish.deployment.cloud;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * Defines application cloud decorations, will be used at deployment to shape the virtual
 * cluster
 * @author Jerome Dochez
 */
@Configured
public interface CloudApplication extends ConfigBeanProxy {

    /**
     * services requested by this application
     *
     * @return list of cloud services
     */
    @Element
    CloudServices getServices();
}
