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

        if (simpleBMP.test1() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool") == 5) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - ReadOnly] : ", stat.FAIL);
        }


        if (simpleBMP.test2() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool") == 10) {
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

        if (simpleBMP.test5() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool") == 15) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test6() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool") == 20) {
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
