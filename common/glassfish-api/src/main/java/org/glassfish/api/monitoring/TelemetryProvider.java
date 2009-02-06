package org.glassfish.api.monitoring;

import org.jvnet.hk2.annotations.Contract;

/**
 * A TelemetryProvider provides monitoring data using the flashlight
 * framework.
 *
 * @author Jerome Dochez
 */
@Contract
public interface TelemetryProvider {

    /**
     * The requested monitoring level has changed.
     * @param newLevel the new monitoring level
     */
    public void onLevelChange(String newLevel);

}
