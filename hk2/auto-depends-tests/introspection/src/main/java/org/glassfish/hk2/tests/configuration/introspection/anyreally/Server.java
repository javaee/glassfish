package org.glassfish.hk2.tests.configuration.introspection.anyreally;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 18, 2010
 * Time: 9:32:28 PM
 * To change this template use File | Settings | File Templates.
 */
@Configured
public interface Server extends ConfigBeanProxy {

    @Attribute(key=true)
    String getName();
    void setName(String name);

    @Attribute(reference = true)
    Config getConfig();


}
