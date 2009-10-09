package com.sun.s1asdev.jdbc.statementtimeout.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.statementtimeout.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.statementtimeout.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "StatementTimeout ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("JDBC Statement Timeout Tests");

        if (simpleBMP.statementTest()) {
            stat.addStatus(testSuite + " statementTest : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " statementTest : ", SimpleReporterAdapter.FAIL);
        }

        if (simpleBMP.preparedStatementTest()) {
            stat.addStatus(testSuite + " preparedStatementTest : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " preparedStatementTest : ", SimpleReporterAdapter.FAIL);
        }

        if (simpleBMP.callableStatementTest()) {
            stat.addStatus(testSuite + " callableStatementTest : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " callableStatementTest : ", SimpleReporterAdapter.FAIL);
        }

        stat.printSummary();
    }
}
