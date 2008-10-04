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
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/server/callers/AddNotifLsnrObjNameCaller.java,v 1.4 2005/12/25 04:26:37 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2005/12/25 04:26:37 $
*/

package com.sun.enterprise.admin.jmx.remote.server.callers;

/* BEGIN -- S1WS_MOD */
import java.util.logging.Logger;
/* END -- S1WS_MOD */
import com.sun.enterprise.admin.jmx.remote.DefaultConfiguration;
import javax.management.MBeanServerConnection;
/* BEGIN -- S1WS_MOD */
import javax.management.NotificationFilter;
import javax.management.ObjectName;
/* END -- S1WS_MOD */
import javax.management.remote.message.MBeanServerRequestMessage;
import javax.management.remote.message.MBeanServerResponseMessage;

/* BEGIN -- S1WS_MOD */
import com.sun.enterprise.admin.jmx.remote.notification.ListenerInfo;
import com.sun.enterprise.admin.jmx.remote.server.notification.ServerNotificationManager;

/* END -- S1WS_MOD */

/** Invokes the method addNotificationListener of the MBeanServerConnection.
 * @see MBeanServerRequestMessage#ADD_NOTIFICATION_LISTENER_OBJECTNAME
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public class AddNotifLsnrObjNameCaller extends AbstractMethodCaller {
/* BEGIN -- S1WS_MOD */
    private final Logger logger = Logger.getLogger(
        DefaultConfiguration.JMXCONNECTOR_LOGGER);/*, 
        DefaultConfiguration.LOGGER_RESOURCE_BUNDLE_NAME );*/
    
    private ServerNotificationManager notifMgr = null;

//    public AddNotifLsnrObjNameCaller(MBeanServerConnection mbsc) {
    public AddNotifLsnrObjNameCaller(MBeanServerConnection mbsc, ServerNotificationManager mgr) {
/* END -- S1WS_MOD */
        super(mbsc);
        METHOD_ID = MBeanServerRequestMessage.ADD_NOTIFICATION_LISTENER_OBJECTNAME;
/* BEGIN -- S1WS_MOD */
        this.notifMgr = mgr;
/* END -- S1WS_MOD */
    }
    
    public MBeanServerResponseMessage call(MBeanServerRequestMessage request) {
/* BEGIN -- S1WS_MOD */
//        final Object result		= new UnsupportedOperationException("" + METHOD_ID);
//        boolean isException = true;
        Object result		= null;
        boolean isException = false;

        ObjectName objname1 = (ObjectName) request.getParams()[0];
        ObjectName objname2 = (ObjectName) request.getParams()[1];
        NotificationFilter filter  = (NotificationFilter) request.getParams()[2];
        Object handback = request.getParams()[3];
        String lsnrid = (String) request.getParams()[4];

        notifMgr.addObjNameNotificationListener(objname1, filter, handback, lsnrid);

        try {
            mbsc.addNotificationListener(objname1, objname2, filter, handback);
        } catch (Exception e) {
            result = e;
            isException = true;
        }

/* END -- S1WS_MOD */
        return ( new MBeanServerResponseMessage(METHOD_ID, result, isException) );
    }
}
