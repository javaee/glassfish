package com.sun.s1asdev.jdbc.nopasswdfordb.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.nopasswdfordb.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.nopasswdfordb.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args)
        throws Exception {
        
	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "nopasswdfordb ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        System.out.println(" Will fail till Derby gets a better XA driver");
	stat.addDescription("Running nopasswdfordb testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();
        	
	if (simpleSession.test1() ) {
	    stat.addStatus( testSuite + " test1 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test1 : " , stat.FAIL );
	}

        if (simpleSession.test2() ) {
	    stat.addStatus( testSuite + " test2 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test2 : " , stat.FAIL );
	}
        
        if (simpleSession.test3() ) {
	    stat.addStatus( testSuite + " test3 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test3 : " , stat.FAIL );
	}

  	if (simpleSession.test4() ) {
	    stat.addStatus( testSuite + " test4 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test4 : " , stat.FAIL );
	}

        if (simpleSession.test5() ) {
	    stat.addStatus( testSuite + " test5 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test5 : " , stat.FAIL );
	}
        
        if (simpleSession.test6() ) {
	    stat.addStatus( testSuite + " test6 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test6 : " , stat.FAIL );
	}

	
	stat.printSummary();
    }
}
