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

package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

public class SimpleBMPClient {

    public static final String poolName = "ql-jdbc-pool";
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";
    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "StatementLeakDetection ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        stat.addDescription("JDBC Statement Leak Detection & Reclaim Tests");
        boolean result = true;

        //Testing Statement objects
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.statementTest()) {
                result = false;
                break;
            }
            Thread.sleep(5000);
        }
        SimpleBMPHome simpleBMPHome1 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP1 = simpleBMPHome1.create();
        if (result && simpleBMP1.compareRecords("S")) {
            stat.addStatus(testSuite + " statementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " statementTest : ", stat.FAIL);
        }

	if(getMonitorablePropertyOfConnectionPool() == 2) {
            stat.addStatus(testSuite + " Monitoring after statementTest : ", stat.PASS);
	} else {
            stat.addStatus(testSuite + " Monitoring after statementTest : ", stat.FAIL);
	}
        //Testing PreparedStatement object
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.preparedStatementTest()) {
                result = false;
                break;
            }
            Thread.sleep(5000);
        }
        SimpleBMPHome simpleBMPHome2 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP2 = simpleBMPHome2.create();
        if (result && simpleBMP2.compareRecords("PS")) {
            stat.addStatus(testSuite + " preparedStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " preparedStatementTest : ", stat.FAIL);
        }

	if(getMonitorablePropertyOfConnectionPool() == 4) {
            stat.addStatus(testSuite + " Monitoring after preparedStatementTest : ", stat.PASS);
	} else {
            stat.addStatus(testSuite + " Monitoring after preparedStatementTest : ", stat.FAIL);
	}
        //Testing CallableStatement objects
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.callableStatementTest()) {
                result = false;
                break;
            }
            Thread.sleep(5000);
        }
        SimpleBMPHome simpleBMPHome3 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP3 = simpleBMPHome3.create();
        if (result && simpleBMP3.compareRecords("CS")) {
            stat.addStatus(testSuite + " callableStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " callableStatementTest : ", stat.FAIL);
        }

	if(getMonitorablePropertyOfConnectionPool() == 6) {
            stat.addStatus(testSuite + " Monitoring after callableStatementTest : ", stat.PASS);
	} else {
            stat.addStatus(testSuite + " Monitoring after callableStatementTest : ", stat.FAIL);
	}
        stat.printSummary();
    }

    public static int getMonitorablePropertyOfConnectionPool() throws Exception {
	final String monitoringStat = "numpotentialstatementleak";
	final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";    
        final JMXServiceURL url = new JMXServiceURL(urlStr);

	final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
	final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName =
            new ObjectName("amx:pp=/mon/server-mon[server],type=jdbcra-mon,name=resources/" + poolName);

	javax.management.openmbean.CompositeDataSupport returnValue = 
		(javax.management.openmbean.CompositeDataSupport) 
		connection.getAttribute(objectName, monitoringStat);

	System.out.println(">>>" + monitoringStat + "=" + returnValue.get("count"));
        return new Integer(returnValue.get("count").toString());
    }
}
