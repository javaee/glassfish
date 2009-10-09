package com.sun.s1asdev.jdbc.notxops.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.notxops.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.notxops.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args)
        throws Exception {
        
	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "notxops ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        
        System.out.println(" Will fail till a better XA driver is integrated with Derby");
	stat.addDescription("Running notxops testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();
        if (simpleSession.test1() ) {
	    stat.addStatus( testSuite + " test1 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test1 : " , stat.FAIL );
	}
    
	
	stat.printSummary();
    }
}
