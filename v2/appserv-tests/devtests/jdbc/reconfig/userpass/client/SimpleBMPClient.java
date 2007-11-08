package com.sun.s1asdev.jdbc.reconfig.userpass.client;

import javax.naming.*;
import java.rmi.*;
import java.util.*;

import com.sun.s1asdev.jdbc.reconfig.userpass.ejb.SimpleBMPHome;
import com.sun.s1asdev.jdbc.reconfig.userpass.ejb.SimpleBMP;

import java.io.*;

public class SimpleBMPClient {

    public static void main(String[] args)
        throws Exception {
     
        InitialContext ic = new InitialContext();
        Object objRef = ic.lookup("java:comp/env/ejb/SimpleBMPHome");
	SimpleBMPHome simpleBMPHome = (SimpleBMPHome)
        javax.rmi.PortableRemoteObject.narrow(objRef, SimpleBMPHome.class);

        SimpleBMP simpleBMP = simpleBMPHome.create();
	
	System.out.println("----------------------------");
	System.out.print(" test1: ");
	if ( simpleBMP.test1("scott","tiger", "A_Customer") ) {
	    System.out.println("Press a key");
	    BufferedReader key = new BufferedReader(
	            new InputStreamReader(System.in));
            //key.readLine();
	    System.out.println("Going to sleep for 60 seconds " );
	    System.out.println("Going to sleep for 60 seconds. Please reconfigure " );
	    try { Thread.sleep(50 * 1000); } catch( Exception e) {}
            System.out.println("Calling test again");
	    if ( simpleBMP.test1("system", "manager", "B_Customer") ) {
	        System.out.println("Passed"); 
	    } else {
	        System.out.println("Failed"); 
	    }
	    
	} else {
	    System.out.println("Failed"); 
	}
	System.out.println("----------------------------");
    }
}
