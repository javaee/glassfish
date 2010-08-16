package com.sun.s1asdev.jdbc.statementwrapper.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementwrapper.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

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
            Thread.sleep(20000);
        }
        SimpleBMPHome simpleBMPHome1 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP1 = simpleBMPHome1.create();
        if (result && simpleBMP1.compareRecords("S")) {
            stat.addStatus(testSuite + " statementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " statementTest : ", stat.FAIL);
        }

        //Testing PreparedStatement object
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.preparedStatementTest()) {
                result = false;
                break;
            }
            Thread.sleep(20000);
        }
        SimpleBMPHome simpleBMPHome2 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP2 = simpleBMPHome2.create();
        if (result && simpleBMP2.compareRecords("PS")) {
            stat.addStatus(testSuite + " preparedStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " preparedStatementTest : ", stat.FAIL);
        }

        //Testing CallableStatement objects
        for (int i = 0; i < 2; i++) {
            SimpleBMPHome simpleBMPHome = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

            SimpleBMP simpleBMP = simpleBMPHome.create();

            if (!simpleBMP.callableStatementTest()) {
                result = false;
                break;
            }
            Thread.sleep(20000);
        }
        SimpleBMPHome simpleBMPHome3 = (SimpleBMPHome) javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP3 = simpleBMPHome3.create();
        if (result && simpleBMP3.compareRecords("CS")) {
            stat.addStatus(testSuite + " callableStatementTest : ", stat.PASS);
        } else {
            stat.addStatus(testSuite + " callableStatementTest : ", stat.FAIL);
        }

        /*    if ( simpleBMP.metaDataTest() ) {
        stat.addStatus(testSuite+" metaDataTest : ", stat.PASS);
        } else {
        stat.addStatus(testSuite+" metaDataTest : ", stat.FAIL);
        }

        if ( simpleBMP.resultSetTest() ) {
        stat.addStatus(testSuite+" resultSetTest : ", stat.PASS);
        } else {
        stat.addStatus(testSuite+" resultSetTest : ", stat.FAIL);
        }
         */
        stat.printSummary();
    }
}
