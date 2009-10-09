package com.sun.s1asdev.jdbc.lazyassoc.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.lazyassoc.ejb.SimpleSessionHome;
import com.sun.s1asdev.jdbc.lazyassoc.ejb.SimpleSession;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {
    
    public static void main(String[] args)
        throws Exception {
        
	SimpleReporterAdapter stat = new SimpleReporterAdapter();
	String testSuite = "LazyAssoc ";

        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleSessionHome");
	SimpleSessionHome simpleSessionHome = (SimpleSessionHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleSessionHome.class);
	stat.addDescription("Running lazyassoc testSuite1 ");
        SimpleSession simpleSession = simpleSessionHome.create();
        
        boolean passed = true;
        for( int i = 0; i < 50;i++ ) {
            try {
                if ( simpleSession.test1() == false ) {
                    passed = false;
                    break;
                } 
            } catch( Exception e ) {
                passed = false;
                break;
            }
        }
        if ( passed ) {
            stat.addStatus( testSuite + " test1 : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test1 : ", stat.FAIL );
        }

        passed = true;
        for( int i = 0; i < 50;i++ ) {
            try {
                if ( simpleSession.test2() == false ) {
                    passed = false;
                    break;
                } 
            } catch( Exception e ) {
                passed = false;
                break;
            }
        }
        if ( passed ) {
            stat.addStatus( testSuite + " test2 : ", stat.PASS );
        } else {
            stat.addStatus( testSuite + " test2 : ", stat.FAIL );
        }
	stat.printSummary();
    }
}
