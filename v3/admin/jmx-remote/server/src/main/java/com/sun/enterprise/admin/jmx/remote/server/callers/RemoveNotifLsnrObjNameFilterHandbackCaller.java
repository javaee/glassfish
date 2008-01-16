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

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/server/callers/RemoveNotifLsnrObjNameFilterHandbackCaller.java,v 1.4 2005/12/25 04:26:40 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2005/12/25 04:26:40 $
*/


package com.sun.enterprise.admin.jmx.remote.server.callers;

import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import javax.management.remote.message.MBeanServerRequestMessage;
import javax.management.remote.message.MBeanServerResponseMessage;

import com.sun.enterprise.admin.jmx.remote.notification.ListenerInfo;
import com.sun.enterprise.admin.jmx.remote.server.notification.ServerNotificationManager;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

/** Invokes the method removeNotificationListener of the MBeanServerConnection.
 * @see MBeanServerRequestMessage.REMOVE_NOTIFICATION_LISTENER_OBJECTNAME_FILTER_HANDBACK
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public class RemoveNotifLsnrObjNameFilterHandbackCaller extends AbstractMethodCaller {

    private static final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/

    private ServerNotificationManager notifMgr = null;

    public RemoveNotifLsnrObjNameFilterHandbackCaller(MBeanServerConnection mbsc, ServerNotificationManager mgr) {
        super(mbsc);
        METHOD_ID = MBeanServerRequestMessage.REMOVE_NOTIFICATION_LISTENER_OBJECTNAME_FILTER_HANDBACK;
        this.notifMgr = mgr;
    }
    
    public MBeanServerResponseMessage call(MBeanServerRequestMessage request) {
//        final Object result		= new UnsupportedOperationException("" + METHOD_ID);
        Object result		= null;
        boolean isException = false;

        ObjectName objname1 = (ObjectName) request.getParams()[0];
        ObjectName objname2 = (ObjectName) request.getParams()[1];
        NotificationFilter filter = (NotificationFilter) request.getParams()[2];
        Object handback = request.getParams()[3];
        String lsnrid = (String) request.getParams()[4];

        ListenerInfo info = notifMgr.removeObjNameNotificationListener(objname1, lsnrid);
        if (info == null)
            info = new ListenerInfo(null, filter, handback);
        try {
            mbsc.removeNotificationListener(objname1, objname2, info.filter, info.handback);
        } catch (Exception e) {
            result = e;
            isException = true;
        }

        return ( new MBeanServerResponseMessage(METHOD_ID, result, isException) );
    }
}
