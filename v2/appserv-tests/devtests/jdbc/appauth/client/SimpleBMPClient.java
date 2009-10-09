package com.sun.s1asdev.jdbc.appauth.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.appauth.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.appauth.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {
    
    public static void main(String[] args)
        throws Exception {
        
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbcappauth ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);
        
	stat.addDescription("JDBC Application Authentication test");
        SimpleBMP simpleBMP = simpleBMPHome.create();
  	
        System.out.println("test 3 will fail with Derby" );
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

	if ( simpleBMP.test3() ) {
	    stat.addStatus(testSuite + "test3 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite + "test3 : ", stat.FAIL);
	}
        
	if ( simpleBMP.test4() ) {
	    stat.addStatus(testSuite + "test4 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite + "test4 : ", stat.FAIL);
	}

        if ( simpleBMP.test5() ) {
	    stat.addStatus(testSuite + "test5 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite + "test5 : ", stat.FAIL);
	}

        System.out.println("jdbc appauth status: ");
	stat.printSummary();
    }
}
