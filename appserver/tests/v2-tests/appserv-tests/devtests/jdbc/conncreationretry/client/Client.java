package com.sun.s1asdev.jdbc.connectioncreationretry.client;

import javax.naming.*;

import com.sun.s1asdev.jdbc.connectioncreationretry.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.connectioncreationretry.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    public static void main(String[] args)
            throws Exception {

        SimpleReporterAdapter stat = new
                SimpleReporterAdapter();
        String testSuite = "ConnectionCreationRetry ";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
        SimpleBMPHome convalBMPHome = (SimpleBMPHome)
                javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP convalBMP = convalBMPHome.create();
	stat.addDescription("Connection Creation Retry Tests");

        if (convalBMP.test1()) {
        	stat.addStatus(testSuite + "test ", stat.PASS);
        } else {
        	stat.addStatus(testSuite + "test ", stat.FAIL);
        }
        stat.printSummary();
    }
}
