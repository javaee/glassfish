package com.sun.s1asdev.jdbc.txisolation.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.txisolation.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.txisolation.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.sql.Connection;

public class SimpleBMPClient {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "JDBCTxIsolation ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("JDBC TX Isolation tests");
            boolean result = false;
            String testName = null;

        if (args != null && args.length > 0) {
            String param = args[0];

            switch (Integer.parseInt(args[0])) {

                case 1:
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_COMMITTED);
                    testName = "read-committed, new connection";
                    break;
                case 2:
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_UNCOMMITTED);
                    testName = "read-uncommitted, new connection";
                    break;
                case 3:
                    result = simpleBMP.test1(Connection.TRANSACTION_REPEATABLE_READ);
                    testName = "repeatable-read, new connection";
                    break;
                case 4:
                    result = simpleBMP.test1(Connection.TRANSACTION_SERIALIZABLE);
                    testName = "serializable, new connection";
                    break;
                 case 5:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_SERIALIZABLE);
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_COMMITTED);
                    testName = "read-committed, guaranteed";
                    break;
                case 6:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                    result = simpleBMP.test1(Connection.TRANSACTION_READ_UNCOMMITTED);
                    testName = "read-uncommitted, guaranteed";
                    break;
                case 7:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_READ_COMMITTED);
                    result = simpleBMP.test1(Connection.TRANSACTION_REPEATABLE_READ);
                    testName = "repeatable-read, guaranteed";
                    break;
                case 8:
                    simpleBMP.modifyIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    result = simpleBMP.test1(Connection.TRANSACTION_SERIALIZABLE);
                    testName = "serializable, guaranteed";
                    break;
            }
            if (result) {
                stat.addStatus(testSuite + testName, stat.PASS);
            } else {
                stat.addStatus(testSuite + testName, stat.FAIL);
            }


        }
        stat.printSummary();
    }
}
