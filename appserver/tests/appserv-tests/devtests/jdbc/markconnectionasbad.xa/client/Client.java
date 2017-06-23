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

package com.sun.s1asdev.jdbc.markconnectionasbad.xa.client;

import javax.naming.*;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.Collection;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final String NUM_CON_DESTROYED_COUNT = "numconndestroyed";
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
        stat.addDescription("Mark-Connection-As-Bad  ");

        if (simpleBMP.test1() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-unshareable-pool") == 5) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - ReadOnly] : ", stat.FAIL);
        }


        if (simpleBMP.test2() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-unshareable-pool") == 10) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test3() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 5) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test4() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 10) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test5() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-unshareable-pool") == 15) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test6() && getMonitorablePropertyOfConnectionPool("jdbc/jdbc-unshareable-pool") == 20) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test7() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 11) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test8() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 12) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test9() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 13 &&
                getMonitorablePropertyOfConnectionPool("jdbc-local-pool") == 1) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Write] : ", stat.FAIL);
        }

        if (simpleBMP.test10() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 14 &&
                getMonitorablePropertyOfConnectionPool("jdbc-local-pool") == 2) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Write] : ", stat.FAIL);
        }

         if (simpleBMP.test11() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 15 && 
                 getMonitorablePropertyOfConnectionPool("jdbc-local-pool") == 3) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Read] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Read] : ", stat.FAIL);
        }

         if (simpleBMP.test12() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool") == 16 && 
                 getMonitorablePropertyOfConnectionPool("jdbc-local-pool") == 4) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Read] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Read] : ", stat.FAIL);
        }


        System.out.println(" Mark-Connection-As-Bad ");
        stat.printSummary();
    }


    public int getMonitorablePropertyOfConnectionPool(String poolName) throws Exception {

	final String urlStr = "service:jmx:rmi:///jndi/rmi://" + HOST_NAME + ":" + JMX_PORT + "/jmxrmi";    
        final JMXServiceURL url = new JMXServiceURL(urlStr);

	final JMXConnector jmxConn = JMXConnectorFactory.connect(url);
	final MBeanServerConnection connection = jmxConn.getMBeanServerConnection();

        ObjectName objectName =
                new ObjectName("amx:pp=/mon/server-mon[server],type=jdbc-connection-pool-mon,name=resources/" + poolName);

	javax.management.openmbean.CompositeDataSupport returnValue = 
		(javax.management.openmbean.CompositeDataSupport) 
		connection.getAttribute(objectName, NUM_CON_DESTROYED_COUNT);

        return new Integer(returnValue.get("count").toString());
    }
}
