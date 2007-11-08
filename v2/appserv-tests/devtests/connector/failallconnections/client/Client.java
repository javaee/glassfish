package com.sun.s1asdev.connector.failallconnections.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;
import java.io.*;
import com.sun.s1asdev.connector.failallconnections.ejb.SimpleSessionHome;
import com.sun.s1asdev.connector.failallconnections.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.jms.*;

public class Client {
    
    public static void main(String[] args)
        throws Exception {

	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "failallconnections-test1 ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        
	stat.addDescription("Running failallconnections testSuite1 ");
        SimpleSession bean = simpleSessionHome.create();

	boolean passed = false;
	
	System.out.println(" Please undeploy and redeploy the RAR for running the test again");
	System.out.println(" Ensure validation and fail-all-connections is true");
	try {
	    if (! bean.test1() ) {
	        stat.addStatus( testSuite + " test1 : invocation 1: ", stat.FAIL );
	        stat.printSummary();
	        System.exit(1);
	    } 
	} catch( Exception e ) {
	    //e.printStackTrace();
	    passed = true;
	}

	try {
	    if ( bean.test2() ) {
	        stat.addStatus( testSuite + " test1: invocation 2 : ", stat.PASS );
	        stat.printSummary();
	        System.exit(0);
	    } else {
	        stat.addStatus( testSuite + " test1: invocation 2 : ", stat.FAIL );
	        stat.printSummary();
	        System.exit(0);
	    }
	} catch( Exception e ) {
	    System.out.println(" Caught expected exception for invocation 2");
	    stat.addStatus( testSuite + " test1 : invocation 2: ", stat.FAIL );
	    stat.printSummary();
	    System.exit(1);
	}

	stat.printSummary();
    }
}
