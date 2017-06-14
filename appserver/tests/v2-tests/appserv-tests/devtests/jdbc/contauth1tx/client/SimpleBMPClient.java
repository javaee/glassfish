package com.sun.s1asdev.jdbc.contauth1tx.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.contauth1tx.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.contauth1tx.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "ContAuth1TX ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("JDBC ContAuth1TX tests");
        System.out.println(" All tests shall fail till a stable XA driver is integrated into Derby");
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

	System.out.println("jdbc contauth1tx status: ");
	stat.printSummary();
    }
}
