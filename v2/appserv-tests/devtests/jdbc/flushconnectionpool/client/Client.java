package com.sun.s1asdev.jdbc.flushconnectionpool.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.flushconnectionpool.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.flushconnectionpool.ejb.SimpleBMP;
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
        stat.addDescription("Flush Connection Pool");

        if (simpleBMP.test1()) {
            stat.addStatus(" Flush Connection Pool -  (test1): ", stat.PASS);
        } else {
            stat.addStatus(" Flush Connection Pool -  (test1): ", stat.FAIL);
        }

        if (simpleBMP.test2()) {
            stat.addStatus(" Flush Connection Pool -  (test2): ", stat.PASS);
        } else {
            stat.addStatus(" Flush Connection Pool -  (test2): ", stat.FAIL);
        }

        stat.printSummary();
    }

}
