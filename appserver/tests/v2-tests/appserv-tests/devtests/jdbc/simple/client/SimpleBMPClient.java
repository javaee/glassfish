package com.sun.s1asdev.jdbc.simple.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.simple.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.simple.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbcsimple ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
        
        if ( simpleBMP.test1(10) ) {
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
	    stat.addStatus(testSuite+"test3 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test3 : ", stat.FAIL);
	}

	if ( simpleBMP.test4() ) {
	    stat.addStatus(testSuite+"test4 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test4 : ", stat.FAIL);
	}

	if ( simpleBMP.test5() ) {
	    stat.addStatus(testSuite+"test5 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test5 : ", stat.FAIL);
	}

	if ( simpleBMP.test6() ) {
	    stat.addStatus(testSuite+"test6 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test6 : ", stat.FAIL);
	}

	if ( simpleBMP.test7() ) {
	    stat.addStatus(testSuite+"test7 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test7 : ", stat.FAIL);
	}

	if ( simpleBMP.test8() ) {
	    stat.addStatus(testSuite+"test8 : ", stat.PASS);
	} else {
	    stat.addStatus(testSuite+"test8 : ", stat.FAIL);
	}

	stat.printSummary();
    
        
    }
}
