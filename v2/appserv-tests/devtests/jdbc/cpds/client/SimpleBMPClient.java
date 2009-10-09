package com.sun.s1asdev.jdbc.cpds.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.cpds.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.cpds.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {
     
 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "ConnectionPoolDataSource ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("Connection Pool DataSource tests");
	
	if ( simpleBMP.test1() ) {
	    stat.addStatus(testSuite + " test1 : ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test1 : ", stat.FAIL );
	}

	if ( simpleBMP.test2() ) {
	    stat.addStatus(testSuite + " test2 : ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test2 : ", stat.FAIL );
	}

	if ( simpleBMP.test3() ) {
	    stat.addStatus(testSuite + " test3 : ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test3 : ", stat.FAIL );
	}
	if ( simpleBMP.test4() ) {
	    stat.addStatus(testSuite + " test4 : ", stat.PASS );
	} else {
	    stat.addStatus(testSuite + " test4 : ", stat.FAIL );
	}
     
	stat.printSummary();
    }
}
