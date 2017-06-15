/*
 * Copyright 2002 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

