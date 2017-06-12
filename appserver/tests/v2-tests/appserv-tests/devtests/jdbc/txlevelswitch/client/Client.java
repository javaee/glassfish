package com.sun.s1asdev.jdbc.txlevelswitch.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.txlevelswitch.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.txlevelswitch.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args)
        throws Exception {
        
	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "txlevelswitch-test1 ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
        
	stat.addDescription("Running txlevelswitch testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();
        if (simpleSession.test1() ) {
	    stat.addStatus( testSuite + " test1 : " , stat.PASS );
	} else {
	    stat.addStatus( testSuite + " test1 : " , stat.FAIL );
	}
    
	
	stat.printSummary();
    }
}
