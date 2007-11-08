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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServerInvocationHandler;

import com.sun.enterprise.admin.common.MBeanServerFactory;
import com.sun.enterprise.server.ApplicationServer;

import com.sun.appserv.management.client.ProxyFactory;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 * Helper class for setting server status in DomianStatusMBean
 *
 * @author Sreenivas Munnangi
 */

public class DomainStatusHelper {

    // vars

    private static final Logger sLogger =
        Logger.getLogger(AdminConstants.kLoggerName);

    private static final StringManager localStrings =
        StringManager.getManager( DomainStatusHelper.class );

    private static final Class[] DOMAIN_STATUS_INTERFACES =
	new Class[] { DomainStatusMBean.class };


    // default constructor
    public DomainStatusHelper() {
    }

    public DomainStatusHelper(String serverName) {
    }

    public void setstate (String serverName, int state) {
	sLogger.log(Level.FINE, "DomainStatusHelper setstate for " + serverName);
	try {
		getDomainStatus().setstate(serverName, Integer.valueOf(state));
	} catch (Exception e) {
		sLogger.log(Level.WARNING, 
			"DomainStatusHelper setstate exception for server " +
			serverName, e);
	}
    }

    public int getstate (String serverName) throws Exception {
	sLogger.log(Level.FINE, "DomainStatusHelper getstate for " + serverName);
	return (getDomainStatus().getstate(serverName));
    }

    protected DomainStatusMBean getDomainStatus() {
	MBeanServer mbs = MBeanServerFactory.getMBeanServer();
	ObjectName  on  = null;
	try {
		on  = getDomainStatusObjectName();
	} catch (MalformedObjectNameException mone) {
		sLogger.log(Level.WARNING, 
			"DomainStatusHelper getDomainStatus ObjectName exception", mone);
	}
	DomainStatusMBean domainStatus = null;
	try {
		domainStatus = 
			(DomainStatusMBean)MBeanServerInvocationHandler.newProxyInstance(
				mbs, on, DomainStatusMBean.class, false );
	} catch (Exception e) {
		sLogger.log(Level.WARNING, 
			"DomainStatusHelper getDomainStatus io exception", e);
	}
	return(domainStatus);
    }

    public static ObjectName getDomainStatusObjectName() 
	throws MalformedObjectNameException {

	ObjectName on = new ObjectName(
		ApplicationServer.getServerContext().getDefaultDomainName() + ":" +
		DomainStatusMBean.DOMAIN_STATUS_PROPS);
	return on;
    }

    public static ObjectName getServersConfigObjectName() 
	throws MalformedObjectNameException {

	ObjectName on = new ObjectName(
		ApplicationServer.getServerContext().getDefaultDomainName() + ":" +
		"type=servers,category=config");
	return on;
    }


}
