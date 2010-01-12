package com.sun.s1asdev.jdbc.reconfig.userpass.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.reconfig.userpass.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.reconfig.userpass.ejb.SimpleBMP;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

import java.io.*;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {
     
        SimpleReporterAdapter stat = new SimpleReporterAdapter();
        String testSuite = "Reconfig User/Pass";
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        stat.addDescription("Reconfig user/pass tests");
        SimpleBMP simpleBMP = simpleBMPHome.create();
	
	System.out.println("----------------------------");
	System.out.print(" test1: ");
	if ( simpleBMP.test1("scott","tiger", "A_Customer") ) {
            stat.addStatus(testSuite + " user1Test : ", stat.PASS);
            System.out.println("Calling test again");
	    if ( simpleBMP.test1("shal", "shal", "B_Customer") ) {
	        System.out.println("Passed");
                stat.addStatus(testSuite + " user2Test : ", stat.PASS); 
	    } else {
	        System.out.println("Failed"); 
                stat.addStatus(testSuite + " user2Test : ", stat.FAIL);
	    }
	    
	} else {
	    System.out.println("Failed");
            stat.addStatus(testSuite + " user1Test : ", stat.FAIL); 
	}
	System.out.println("----------------------------");
        stat.printSummary();
    }
}
