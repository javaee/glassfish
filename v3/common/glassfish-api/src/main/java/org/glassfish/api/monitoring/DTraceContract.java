package org.glassfish.api.monitoring;

import org.glassfish.api.monitoring.ProbeProviderInfo;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author bnevins
 */

@Contract
public interface DTraceContract {
    boolean isSupported();
    <T> T getProvider(Class<T> t);
    <T> T getProvider(ProbeProviderInfo ppi);
    Class getInterface(ProbeProviderInfo ppi);
}
