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
        stat.addDescription(" Statement CloseOnCompletion with Caching test");

        if (simpleBMP.testCloseOnCompletion()) {
            stat.addStatus(" Statement CloseOnCompletion Test: ", stat.PASS);
        } else {
            stat.addStatus(" Statement CloseOnCompletion Test: ", stat.FAIL);
        }
        stat.printSummary();
    }

}
