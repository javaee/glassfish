/**
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.s1peqe.loadbalancing.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.util.*;
import com.sun.s1peqe.ejb.bmp.enroller.ejb.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class EnrollerClientthreading extends Thread{

    private static  SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static int MAXTHREADS = 100;
    public static int count = 0;
    public static String ctxFactory;

    public static void main(String[] args) { 

        ctxFactory = args[0];
	System.out.println("Using " + ctxFactory);
	for (int i = 0; i < 300; i++) {
	    new EnrollerClientthreading().start();
	}
    }
  
    public void run() {
        try {
	    Properties env = new Properties();
	    env.put("java.naming.factory.initial", ctxFactory);	  
	    InitialContext ctx = new InitialContext(env);
		    
	    Object objref = ctx.lookup("ejb/MyStudent");
	    System.out.println("Thread #" + ++count + " looked up...ejb/MyStudent");
	    
	    StudentHome sHome = 
	      (StudentHome) PortableRemoteObject.narrow(objref, 
							StudentHome.class);
	    	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
