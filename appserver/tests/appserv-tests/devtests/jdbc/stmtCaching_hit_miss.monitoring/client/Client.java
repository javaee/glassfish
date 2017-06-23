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

package com.sun.s1asdev.jdbc.stmtcaching.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.stmtcaching.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.stmtcaching.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;

import java.rmi.RemoteException;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final String NUM_HITS = "numstatementcachehit";
    public static final String NUM_MISSES = "numstatementcachemiss";
    public static final int JMX_PORT = 8686;
    public static final String HOST_NAME = "localhost";

    public static void main(String[] args)
            throws Exception {

        Client client = new Client();
        client.runTest();
    }

    public void runTest() throws Exception {
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("Statement Caching Hit miss tests");

        if (simpleBMP.testHit() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_HITS) == 1 && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_MISSES) == 5) {
	    System.out.println("HIT pass");
            stat.addStatus(" Statement Caching  -  (Hit): ", stat.PASS);
        } else {
	    System.out.println("HIT fail");
            stat.addStatus(" Statement Caching  -  (Hit): ", stat.FAIL);
        }

        if (simpleBMP.testMiss() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_HITS) == 6 && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_MISSES) == 7) {
	    System.out.println("MISS pass");
            stat.addStatus(" Statement Caching  -  (Miss): ", stat.PASS);
        } else {
	    System.out.println("MISS fail");
            stat.addStatus(" Statement Caching  -  (Miss): ", stat.FAIL);
        }

        if (simpleBMP.testHitColumnIndexes() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_HITS) == 7 && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_MISSES) == 12) {
	    System.out.println("columnIndexes pass");
            stat.addStatus(" Statement Caching  -  (hit columnIndexes) : ", stat.PASS);
        } else {
	    System.out.println("columnIndexes fail");
            stat.addStatus(" Statement Caching  -  (hit columnIndexes) : ", stat.FAIL);
        }

        if (simpleBMP.testHitColumnNames() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_HITS) == 8 && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-stmtcaching_hit_miss-pool",NUM_MISSES) == 17) {
	    System.out.println("columnNames pass");
            stat.addStatus(" Statement Caching  -  (hit columnNames) : ", stat.PASS);
        } else {
	    System.out.println("columnNames fail");
            stat.addStatus(" Statement Caching  -  (hit columnNames) : ", stat.FAIL);
        }
        stat.printSummary();
    }

    public int getMonitorablePropertyOfConnectionPool(String poolName, String monitoringStat) throws Exception {

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
