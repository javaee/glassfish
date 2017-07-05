/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.s1asdev.jdbc.appmonitoring.client;

import javax.naming.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import java.rmi.RemoteException;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final String NUM_ACQUIRED = "numconnacquired";
    public static final String NUM_RELEASED = "numconnreleased";
    public static final String app1 = "ejb-bmp-contauthApp";
    public static final String app2 = "ejb-bmp-statementtimeoutApp";
    public static final String poolName = "ql-jdbc-pool";
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";

    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {
        stat.addDescription("App based monitoring tests");

        if (getMonitorablePropertyOfConnectionPool(app1, NUM_ACQUIRED) == 2 && getMonitorablePropertyOfConnectionPool(app2, NUM_ACQUIRED) == 3 && getMonitorablePropertyOfConnectionPool(null, NUM_ACQUIRED) == 5) {
	    System.out.println("Monitoring : Acquired Statistic test pass");
            stat.addStatus("Monitoring : Acquired Statistic test: ", stat.PASS);
        } else {
	    System.out.println("Monitoring : Acquired Statistic test fail");
            stat.addStatus("Monitoring : Acquired Statistic test: ", stat.FAIL);
        }

        if (getMonitorablePropertyOfConnectionPool(app1, NUM_RELEASED) == 2 && getMonitorablePropertyOfConnectionPool(app2, NUM_RELEASED) == 3 && getMonitorablePropertyOfConnectionPool(null, NUM_RELEASED) == 5) {
	    System.out.println("Monitoring : Released Statistic test pass");
            stat.addStatus("Monitoring : Released Statistic test: ", stat.PASS);
        } else {
	    System.out.println("Monitoring : Released Statistic test fail");
            stat.addStatus("Monitoring : Released Statistic test: ", stat.FAIL);
        }

        stat.printSummary();
    }

    public int getMonitorablePropertyOfConnectionPool(String appName, String monitoringStat) throws Exception {

	final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";    
        final JMXServiceURL url = new JMXServiceURL(urlStr);

	final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
	final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName = null;
	if(appName == null) {
            objectName = new ObjectName("amx:pp=/mon/server-mon[server],type=jdbc-connection-pool-mon,name=resources/" + poolName);
	} else {
	    objectName = new ObjectName("amx:pp=/mon/server-mon[server],type=jdbc-connection-pool-app-mon,name=resources/" + poolName + "/" + appName);
	}

	javax.management.openmbean.CompositeDataSupport returnValue = 
		(javax.management.openmbean.CompositeDataSupport) 
		connection.getAttribute(objectName, monitoringStat);

	System.out.println(">>>" + monitoringStat + "=" + returnValue.get("count"));
        return new Integer(returnValue.get("count").toString());
    }

}
