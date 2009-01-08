/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.jmx.remote.server.notification;

import java.util.logging.Logger;
import javax.management.*;

import com.sun.enterprise.admin.jmx.remote.notification.NotificationWrapper;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

/**
 * A Proxy for NotificationListener.
 * An object of NotificationListenerProxy is registered to the NotificationBroadcaster
 * for every notification listener that is registered by the client.
 * Whenever the NotificationBroadcaster calls this proxy's handleNotification method,
 * this proxy object will invoke ServerNotificationManager.fireNotification(...)
 */
public class NotificationListenerProxy implements NotificationListener {
    private String id = null;
    private ServerNotificationManager mgr = null;
    private ObjectName objname = null;
    private Notification notification = null;

    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/

    public NotificationListenerProxy(ObjectName objname,
                                     ServerNotificationManager mgr,
                                     String id) {
        this.objname = objname;
        this.mgr = mgr;
        this.id = id;
    }

    /**
     * Returns the client id, which has registered the notification listener
     * represented by this proxy object.
     */
    public String getId() {
        return id;
    }


    public NotificationWrapper getNotificationWrapper() {
        return ( new NotificationWrapper(objname, notification) );
    }

    public Notification getNotification() {
        return notification;
    }

    public void handleNotification( Notification notification,
                                    Object handback) {
        this.notification = notification;
        mgr.fireNotification(this);
    }
}
