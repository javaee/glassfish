package com.sun.s1asdev.jdbc.stmtcaching.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.stmtcaching.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.stmtcaching.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.rmi.RemoteException;

public class Client {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();

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

        if (simpleBMP.testHit()) {
            stat.addStatus(" Statement Caching  -  (Hit): ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (Hit): ", stat.FAIL);
        }

        if (simpleBMP.testMiss()) {
            stat.addStatus(" Statement Caching  -  (Miss): ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (Miss): ", stat.FAIL);
        }

        if (simpleBMP.testHitColumnIndexes()) {
            stat.addStatus(" Statement Caching  -  (hit columnIndexes) : ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (hit columnIndexes) : ", stat.FAIL);
        }

        if (simpleBMP.testHitColumnNames()) {
            stat.addStatus(" Statement Caching  -  (hit columnNames) : ", stat.PASS);
        } else {
            stat.addStatus(" Statement Caching  -  (hit columnNames) : ", stat.FAIL);
        }
        stat.printSummary();
    }

}
