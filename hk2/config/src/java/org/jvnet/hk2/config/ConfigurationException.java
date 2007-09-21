package org.jvnet.hk2.config;

import org.jvnet.hk2.component.ComponentException;

import javax.xml.stream.Location;

/**
 * Indicates a problem in the configuration value.
 *
 * @author Kohsuke Kawaguchi
 */
public class ConfigurationException extends ComponentException {
    private Location location;

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable origin) {
        super(message, origin);
    }

    /**
     * Sets the location.
     *
     * This value is not set in the constructor so that we don't need to
     * carry around {@link Dom} to everywhere.
     */
    void setLocation(Location location) {
        assert this.location==null;
        this.location = location;
    }

    /**
     * Indicates the source position of the configuration file
     * where the problem happened.
     */
    public Location getLocation() {
        return location;
    }

    public String getMessage() {
        if(location==null)
            return super.getMessage();
        return String.format("%s at %s line %d",
            super.getMessage(), location.getSystemId(), location.getLineNumber());
    }
}
