package com.sun.s1asdev.jdbc.markconnectionasbad.xa.client;

import javax.naming.*;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.markconnectionasbad.xa.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.ModuleMonitoringLevelsConfig;
import com.sun.appserv.management.config.ModuleMonitoringLevelValues;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static final String NUM_CON_DESTROYED_COUNT = "numconndestroyed-count";
    public static final String USER_PASSWORD = "adminadmin";
    public static final int JMX_PORT = 8686;
    public static final String DOTTED_NAME_GET = "dottedNameGet";
    public static final String USER = "admin";
    public static final String CONFIG = "server-config";
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

        if (simpleBMP.test1() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 5) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - ReadOnly] : ", stat.FAIL);
        }


        if (simpleBMP.test2() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 10) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - UnShareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test3() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 5) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test4() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 10) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - NoTx - Shareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test5() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 15) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test6() && getMonitorablePropertyOfConnectionPool("jdbc-unshareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 20) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - UnShareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test7() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 11) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - ReadOnly] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - ReadOnly] : ", stat.FAIL);
        }

        if (simpleBMP.test8() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 12) {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [XA - Tx - Shareable - Write] : ", stat.FAIL);
        }

        if (simpleBMP.test9() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 13 &&
                getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                        NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 1) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Write] : ", stat.FAIL);
        }

        if (simpleBMP.test10() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 14 &&
                getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                        NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG) == 2) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Write] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Write] : ", stat.FAIL);
        }

         if (simpleBMP.test11() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==15 &&
                 getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==3) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Read] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Read-Read] : ", stat.FAIL);
        }

         if (simpleBMP.test12() && getMonitorablePropertyOfConnectionPool("jdbc-shareable-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==16 &&
                 getMonitorablePropertyOfConnectionPool("jdbc-local-pool",
                NUM_CON_DESTROYED_COUNT, HOST_NAME, JMX_PORT, USER, USER_PASSWORD, CONFIG )==4) {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Read] : ", stat.PASS);
        } else {
            stat.addStatus(" Mark-Connection-As-Bad [Local-XA -Shareable-Shareable - Write-Read] : ", stat.FAIL);
        }


        System.out.println(" Mark-Connection-As-Bad ");
        stat.printSummary();
    }


    public int getMonitorablePropertyOfConnectionPool(String poolName, String property, String hostName, int JMX_PORT, String user, String password, String configName) throws Exception {
        AppserverConnectionSource appserver =
                new AppserverConnectionSource(AppserverConnectionSource.PROTOCOL_RMI, hostName, JMX_PORT, user, password, null);
        MBeanServerConnection connection = appserver.getJMXConnector(false).getMBeanServerConnection();
        //System.out.println("Connected to JMX");

        DomainRoot domainRoot = appserver.getDomainRoot();

        final ConfigConfig config =
                domainRoot.getDomainConfig().getConfigConfigMap().get(configName);

        final ModuleMonitoringLevelsConfig mon = config.getMonitoringServiceConfig().getModuleMonitoringLevelsConfig();

        mon.setJDBCConnectionPool(ModuleMonitoringLevelValues.HIGH);

        ObjectName objectName =
                new ObjectName("amx:j2eeType=X-MonitoringDottedNames,name=na");

        String params[] = new String[]{String.class.getName()};
        Object values[] = new Object[]{"server.resources." + poolName + "." + property};
        javax.management.Attribute returnValue = (javax.management.Attribute) connection.invoke(objectName, DOTTED_NAME_GET, values, params);

        System.out.println(DOTTED_NAME_GET + " : " + returnValue.getName() + " : " + returnValue.getValue());

        return new Integer(returnValue.getValue().toString());
    }
}
