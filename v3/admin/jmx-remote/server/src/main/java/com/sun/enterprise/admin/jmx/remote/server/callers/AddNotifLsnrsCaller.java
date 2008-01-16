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
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/server/callers/AddNotifLsnrsCaller.java,v 1.4 2005/12/25 04:26:37 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2005/12/25 04:26:37 $
*/


package com.sun.enterprise.admin.jmx.remote.server.callers;

import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.message.MBeanServerRequestMessage;
import javax.management.remote.message.MBeanServerResponseMessage;

import com.sun.enterprise.admin.jmx.remote.server.notification.NotificationListenerProxy;
import com.sun.enterprise.admin.jmx.remote.server.notification.ServerNotificationManager;
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;

/** Invokes the method addNotificationListener of the MBeanServerConnection.
 * @see MBeanServerRequestMessage#ADD_NOTIFICATION_LISTENERS
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public class AddNotifLsnrsCaller extends AbstractMethodCaller {

    private ServerNotificationManager notifyMgr = null;

    private final Logger logger = Logger.getLogger(
    DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
    DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/
    
    public AddNotifLsnrsCaller(MBeanServerConnection mbsc, ServerNotificationManager mgr) {
        super(mbsc);
        METHOD_ID = MBeanServerRequestMessage.ADD_NOTIFICATION_LISTENERS;
        this.notifyMgr = mgr;
    }
    
    public MBeanServerResponseMessage call(MBeanServerRequestMessage request) {
//        final Object result         = new UnsupportedOperationException("" + METHOD_ID);
        Object result         = null;
        boolean isException   = false;
        ObjectName objname = (ObjectName)request.getParams()[0];
        String cid = (String) request.getParams()[1];
        String id = (String) request.getParams()[2];
        NotificationListenerProxy proxy =
                new NotificationListenerProxy(  objname,
                                                notifyMgr,
                                                cid);
        notifyMgr.addNotificationListener(objname, id, proxy);

        try {
            mbsc.addNotificationListener(objname, proxy, null, null);
        } catch (Exception e) {
            isException = true;
            result = e;
        }

        return ( new MBeanServerResponseMessage(METHOD_ID, result, isException) );
    }
}
