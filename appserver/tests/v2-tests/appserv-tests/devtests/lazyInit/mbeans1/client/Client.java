/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.management.mejb.client;


import java.util.*;
import javax.management.*;
import javax.management.remote.*;

import com.sun.appserv.management.*;
import com.sun.appserv.management.client.*;
import com.sun.appserv.management.config.*;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    // consts
    public static String kTestNotRun    = "TEST NOT RUN";
    public static String kTestPassed    = "TEST PASSED";
    public static String kTestFailed    = "TEST FAILED";
    
    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");
    
    public static void main (String[] args) {
        stat.addDescription("management-mejb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("management-mejb");
    }
    
    public Client (String[] args) {}

    public String doTest() {
        
	boolean pass = false;
        String res = kTestNotRun;
        
    	try {
            
		String host = "localhost";
		int port = 8686;
		String user = "admin";
		String password = "adminadmin";

		// Get Config info using AMX
		AppserverConnectionSource conn = new AppserverConnectionSource(
						      host, port, user, password, null);

		DomainRoot domainRoot = conn.getDomainRoot();

		DomainConfig config = domainRoot.getDomainConfig();

		Map map = config.getJDBCResourceConfigMap();
		Collection<JDBCResourceConfig> jdbcConfigs = 
					(Collection<JDBCResourceConfig>)map.values();
		for ( JDBCResourceConfig jdbcConfig : jdbcConfigs ) {
		    System.err.println("JDBC Resource: name = " + jdbcConfig.getName()
					+ ", JNDI Name = " + jdbcConfig.getJNDIName()
					+ ", Pool Name = " + jdbcConfig.getPoolName());
		    System.err.println("\t\tJ2EEType = " + jdbcConfig.getJ2EEType());
                    pass = true;
		}

                if (pass == false) {
                    throw new Exception("No JDBC resources found");
                }
                pass = false;

		// Get Config info using JMX
		//MBeanServerConnection mbConn = conn.getMBeanServerConnection( false );
		String urls = "service:jmx:rmi:///jndi/rmi://" +
			      host + ":" + port  + "/management/rmi-jmx-connector";
		JMXServiceURL url = new JMXServiceURL(urls);
		HashMap env = new HashMap();
		env.put(JMXConnector.CREDENTIALS, new String[] { user, password });
		JMXConnector jmxConnector = JMXConnectorFactory.connect(url, env);
		MBeanServerConnection mbConn = jmxConnector.getMBeanServerConnection();

		//ObjectName objectName = 
		//		new ObjectName("amx:j2eeType=X-JDBCResourceConfig,*");
		ObjectName objectName = null; // to query all MBeans
		Set mbeans = (Set) mbConn.queryNames(objectName, null);
		Iterator it = mbeans.iterator();
		while ( it.hasNext() ) {
		    ObjectName mbean = (ObjectName) it.next(); 
		    System.err.println("Object Name : " + mbean.getCanonicalName());
                    pass = true;
                }

                if (pass == false) {
                    throw new Exception("No Mbeans found");
                }
 
	} catch(Exception re) {
            re.printStackTrace();
            res = kTestFailed;
            return res;
	} 

	if ( pass ) {
	    res = kTestPassed;
	    System.out.println("Mbean Test passed");
            stat.addStatus("Mbean Test", stat.PASS);

	} else {
	    res = kTestFailed;
	    System.out.println("Mbean Test failed");
            stat.addStatus("Mbean Test", stat.FAIL);

	}

        return res;
    }
    
}

