package org.jvnet.hk2.config;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 23, 2008
 * Time: 3:50:44 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ConfigView {

    public ConfigBean getMasterView();

    public <T extends ConfigBeanProxy> T getProxy(Class<T> proxyType);
}
