package org.glassfish.flashlight.provider;

import org.jvnet.hk2.annotations.Contract;

/**
 * Created by IntelliJ IDEA.
 * User: mk
 * Date: Nov 10, 2009
 * Time: 9:12:22 PM
 * To change this template use File | Settings | File Templates.
 */
@Contract
public interface ProbeProviderEventListener {

    public <T> void probeProviderAdded(String moduleProviderName, String moduleName,
    		String probeProviderName, String invokerId,
    		Class<T> providerClazz, T provider);

}
