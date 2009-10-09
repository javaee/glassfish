package com.sun.s1asdev.jdbc.contauth.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.contauth.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.contauth.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    SimpleReporterAdapter stat = new SimpleReporterAdapter();
    public static void main(String[] args)
        throws Exception {
     
        	SimpleBMPClient client = new SimpleBMPClient();
             client.runTest();
    }

    public void runTest() throws Exception {
	String testSuite = "jdbccontauth ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        stat.addDescription("JDBC Container Authentication test");

        if ( simpleBMP.test1() ) {
	    stat.addStatus(testSuite+"test1 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test1 : ", stat.FAIL);
	}
	if ( simpleBMP.test2() ) {
	    stat.addStatus(testSuite+"test2 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test2 : ", stat.FAIL);
	}

	System.out.println("jdbc contauth status: ");
	stat.printSummary();
    }
}
