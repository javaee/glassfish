package com.sun.s1asdev.jdbc.simpleora.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.simpleora.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.simpleora.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {

 	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "jdbcsimpleora ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	stat.addDescription("Simple Oracle test ");

	
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

	System.out.println("jdbc simpleora status: ");
	stat.printSummary();
    
        
    }
}
