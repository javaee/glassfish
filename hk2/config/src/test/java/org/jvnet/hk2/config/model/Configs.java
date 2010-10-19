package org.jvnet.hk2.config.model;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Oct 19, 2010
 * Time: 11:15:45 AM
 * To change this template use File | Settings | File Templates.
 */
@Configured
public interface Configs extends ConfigBeanProxy {

    @Element
    List<Config> getConfig();
}
