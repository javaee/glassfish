package com.sun.s1asdev.jdbc.cpdsperf.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.cpdsperf.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.cpdsperf.ejb.SimpleBMP;

public class SimpleBMPClient {
    
    public static void main(String[] args)
        throws Exception {
        int numTimes = 100; 
	if (args.length == 1) {
            numTimes = Integer.parseInt( args[0] );
	}
	
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create( numTimes );
	
	long timeTaken = 0;
	System.out.println("----------------------------");
	System.out.println(" test1: Using ConnectionPoolDataSource");
	if ( (timeTaken = simpleBMP.test1()) != -1 ) {
	    System.out.println("Passed: Num Connections: " + numTimes+
	        "  Time taken :" + timeTaken); 
	} else {
	    System.out.println("Failed"); 
	}
	System.out.println("----------------------------");
        System.out.println();
	System.out.println("----------------------------");
	System.out.println(" test2: Using DataSource");
	if ( (timeTaken = simpleBMP.test2()) != -1 ) {
	    System.out.println("Passed: Num Connections: " + numTimes+
	        "  Time taken :" + timeTaken); 
	} else {
	    System.out.println("Failed"); 
	}

	System.out.println("----------------------------");
    }	
}
