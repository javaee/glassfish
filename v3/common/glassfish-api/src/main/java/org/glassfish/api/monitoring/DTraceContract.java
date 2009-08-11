package org.glassfish.api.monitoring;

import org.glassfish.api.monitoring.ProbeProviderInfo;
import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author bnevins
 */

@Contract
public interface DTraceContract {
    <T> T getProvider(Class<T> t);
    //Class getProvider(ProbeProviderInfo ppi);
    boolean isSupported();

    Object getProvider23(ProbeProviderInfo probeInfo);
}
