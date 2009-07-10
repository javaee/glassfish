package org.glassfish.api.monitoring;

import org.jvnet.hk2.annotations.Contract;

/**
 *
 * @author bnevins
 */

@Contract
public interface DTraceContract {
    <T> T getProvider(Class<T> t);
}
