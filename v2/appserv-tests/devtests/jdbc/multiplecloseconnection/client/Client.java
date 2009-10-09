package com.sun.s1asdev.jdbc.multiplecloseconnection.client;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1asdev.jdbc.multiplecloseconnection.ejb.SimpleSession;
import com.sun.s1asdev.jdbc.multiplecloseconnection.ejb.SimpleSessionHome;

import javax.naming.InitialContext;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "MultipleCloseConnection ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
        SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        stat.addDescription("Multiple Close Connection Tests");
        SimpleSession simpleSession = simpleSessionHome.create();

        boolean passed = true;
        for (int i = 0; i < 50; i++) {
            try {
                if (simpleSession.test1() == false) {
                    passed = false;
                    break;
                }
            } catch (Exception e) {
                passed = false;
                break;
            }
        }
        if (passed) {
            stat.addStatus(testSuite + " test1 : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " test1 : ", SimpleReporterAdapter.FAIL);
        }

        passed = true;
        for (int i = 0; i < 50; i++) {
            try {
                if (simpleSession.test2() == false) {
                    passed = false;
                    break;
                }
            } catch (Exception e) {
                passed = false;
                break;
            }
        }
        if (passed) {
            stat.addStatus(testSuite + " test2 : ", SimpleReporterAdapter.PASS);
        } else {
            stat.addStatus(testSuite + " test2 : ", SimpleReporterAdapter.FAIL);
        }
        stat.printSummary();
    }
}
