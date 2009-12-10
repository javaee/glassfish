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

package com.sun.enterprise.admin.jmx.remote.notification;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * Class to wrap the notification being sent to the client.
 * This wrapper is used to carry extra information, such as WAIT notification
 * or an actual notification (type=NOTIF)
 */
public class NotificationWrapper implements java.io.Serializable {
    public static final int WAIT = 1;
    public static final int NOTIF= 2;

    private Notification notif  = null;
    private ObjectName   source = null;
    private int          type   = NOTIF;

    public NotificationWrapper(int type, ObjectName source, Notification notif) {
        this.notif = notif;
        this.source = source;
        this.type = type;
    }

    public NotificationWrapper(ObjectName source, Notification notif) {
        this.notif = notif;
        this.source = source;
        this.type = NOTIF;
    }

    public int getType() {
        return type;
    }

    public ObjectName getSource() {
        return source;
    }

    public Notification getNotification() {
        return notif;
    }
}
