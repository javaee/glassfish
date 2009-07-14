package org.glassfish.api.monitoring;

/**
 * Marker Interface for modules that intend to provide some monitoring 
 * information.
 *
 * @author Nandini Ektare
 */
public interface MonitoringItem {

    /**
     * The monitoring level of this monitoring item 
     * @return String with values HIGH/LOW/OFF
     */
    public String getLevel();

    /**
     * The requested monitoring level has changed.
     * @param the new monitoring level
     */
    public void setLevel(String level);
}
