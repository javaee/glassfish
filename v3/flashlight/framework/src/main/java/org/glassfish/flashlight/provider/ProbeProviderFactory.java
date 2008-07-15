package org.glassfish.flashlight.provider;

import org.jvnet.hk2.annotations.Contract;

/**
 * @author Mahesh Kannan
 *         Date: May 22, 2008
 */
@Contract
public interface ProbeProviderFactory {

    public <T> T getProbeProvider(String moduleName, String providerName, String appName, Class<T> clazz)
            throws InstantiationException, IllegalAccessException;

}
