package com.sun.s1asdev.jdbc.connsharing.xa.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.connsharing.xa.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.connsharing.xa.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args)
        throws Exception {
        
	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "connsharing-test1 ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        
	stat.addDescription("Running notxops testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();
        if (simpleSession.test1() && simpleSession.query()) {
	    stat.addStatus( testSuite + " test1 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test1 : " , stat.FAIL );
	}

        if (simpleSession.test2() && simpleSession.query()) {
	    stat.addStatus( testSuite + " test2 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test2 : " , stat.FAIL );
	}

        if (simpleSession.test3() && simpleSession.query2()) {
	    stat.addStatus( testSuite + " test3 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test3 : " , stat.FAIL );
	}

        if (simpleSession.test4() && simpleSession.query2()) {
	    stat.addStatus( testSuite + " test4 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test4 : " , stat.FAIL );
	}
  
	
	stat.printSummary();
    }
}
