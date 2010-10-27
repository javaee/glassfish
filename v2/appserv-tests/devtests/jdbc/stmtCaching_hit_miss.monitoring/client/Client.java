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
