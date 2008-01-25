package org.glassfish.admin.runtime.infrastructure.management;

import javax.management.Notification;

/**
 * Interface implemented by objects that can be asked to send a notification.
 */
public interface SendNotification {
    /**
     * Sends a notification.
     *
     * @param notification The notification to send.
     */
    public void sendNotification(Notification notification);
}
