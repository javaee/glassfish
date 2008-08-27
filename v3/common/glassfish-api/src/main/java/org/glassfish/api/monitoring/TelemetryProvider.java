package org.glassfish.api.monitoring;

/**
 * A TelemetryProvider provides monitoring data using the flashlight
 * framework.
 *
 * @author Jerome Dochez
 */
public interface TelemetryProvider {

    /**
     * The requested monitoring level has changed.
     * @param newLevel the new monitoring level
     */
    public void onLevelChange(String newLevel);

}
