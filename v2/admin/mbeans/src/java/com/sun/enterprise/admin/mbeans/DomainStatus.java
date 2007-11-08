/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.admin.mbeans;

import javax.management.ObjectName;
import javax.management.NotificationBroadcasterSupport;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.MBeanServerConnection;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.common.constant.AdminConstants;

import com.sun.enterprise.admin.server.core.AdminService;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.common.MBeanServerFactory;

import com.sun.appserv.management.j2ee.StateManageable;

/**
 * Provides for determining the state of servers within the
 * scope of this domain
 *
 * @author Sreenivas Munnangi
 */

public class DomainStatus extends NotificationBroadcasterSupport 
    implements DomainStatusMBean {


    /**
     * variables
     */
    private static final Logger sLogger = 
	Logger.getLogger(AdminConstants.kLoggerName);
    private static final StringManager localStrings =
	StringManager.getManager( DomainStatus.class );
    private Map m;

    /**
     * default constructor
     */
    public DomainStatus() {
	m = Collections.synchronizedMap(new HashMap());
    }

    /**
     * get the current state of the given server
     * @return int the current value of the state
     */
    public int getstate(String serverName) throws Exception {
	sLogger.log(Level.FINE, "DomainStatus.getstate for " + serverName);
	// check for server name and return the value
	if ((serverName != null) && (serverName.length() > 0)) {
		if (m.containsKey(serverName)) {
			return ((Integer) m.get(serverName)).intValue();
		} else {
			throw new Exception(
				localStrings.getString(
				"admin.mbeans.domainStatus.serverNotFound",
				serverName));
		}
	}
	return StateManageable.STATE_FAILED;
    }
    
    /**
     * set the current state of the given server
     */
    public void setstate(String serverName, Integer state) throws Exception {
	sLogger.log(Level.FINE, "DomainStatus.setstate for " + serverName);
	// check for server name and set the value
	if ((serverName != null) && (serverName.length() > 0)) {
		m.put(serverName, state);
		// send notification
		sendServerStatusChangedNotification(serverName);
	} else {
		throw new Exception(
				localStrings.getString(
				"admin.mbeans.domainStatus.serverNotFound",
				serverName));
	}
    }

    /**
     * get the mBean server connection for the given server
     */
    public MBeanServerConnection getServerMBeanServerConnection(String serverName) 
	throws Exception {

	sLogger.log(Level.FINE, 
		"DomainStatus.getServerMBeanServerConnection for " + serverName);
	// vars
	ConfigContext configContext = 
		AdminService.getAdminService().getAdminContext().getAdminConfigContext();
	MBeanServerConnection mbsc = null;

	// check if DAS
	if (ServerHelper.isDAS(configContext, serverName)) {
		mbsc = MBeanServerFactory.getMBeanServer();
	} else {
		mbsc = ServerHelper.connect(configContext, serverName);
	}

	return mbsc;
    }

    /**
     * send status change notification
     */
    private void sendServerStatusChangedNotification(final String serverName) {

	sLogger.log(Level.FINE, 
	"DomainStatus.sendServerStatusChangedNotification for " + serverName);

	Map m = Collections.synchronizedMap(new HashMap());
	m.put(DomainStatusMBean.SERVER_NAME_KEY, serverName);
	Notification notification = new Notification(
			  DomainStatusMBean.SERVER_STATUS_NOTIFICATION_TYPE,
			  this,
			  0,
			  serverName);
	notification.setUserData(m);
	sendNotification(notification);
    }

}
