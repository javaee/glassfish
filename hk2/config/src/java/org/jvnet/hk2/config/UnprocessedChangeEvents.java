package org.jvnet.hk2.config;

import java.beans.PropertyChangeEvent;

/**
 * Notification that some configuration changes could not be processed successfully.
 * This usually means that changes will only be accepted upon server restart.
 *
 * @author Jerome Dochez
 */
public class UnprocessedChangeEvents {

    final PropertyChangeEvent[] events;
    final String reason;

    public UnprocessedChangeEvents(String reason, PropertyChangeEvent[] events) {
        this.events = events;
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public PropertyChangeEvent[] getUnprocessedEvents() {
        return events;
    }
}
