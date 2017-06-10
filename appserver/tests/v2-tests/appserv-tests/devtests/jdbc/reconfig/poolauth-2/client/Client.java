package com.sun.s1asdev.jdbc.reconfig.poolauth.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.reconfig.poolauth.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.reconfig.poolauth.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args)
        throws Exception {
        
	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "reconfig.poolauth-test1 ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        
	stat.addDescription("Running reconfig.poolauth testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();

        if ( "1".equals(args[0]) )  {
	    if (simpleSession.test1() ) {
	        stat.addStatus( testSuite + " test1 : " , stat.PASS );
	    } else {
	        stat.addStatus( testSuite + " test1 : " , stat.FAIL );
	    }
        } else {
            if (simpleSession.test2() ) {
	        stat.addStatus( testSuite + " test2 : " , stat.PASS );
	    } else {
	        stat.addStatus( testSuite + " test2 : " , stat.FAIL );
	    }
        } 
        	
	stat.printSummary();
    }
}
