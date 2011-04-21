package org.glassfish.deployment.cloud;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 3/2/11
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
@Configured
public interface CloudServices extends ConfigBeanProxy {

    /**
     * services requested by this application
     *
     * @return list of cloud services
     */
    @Element("*")
    List<CloudService> getServices();
}
