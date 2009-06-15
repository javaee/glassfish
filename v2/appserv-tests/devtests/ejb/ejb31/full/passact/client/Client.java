package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

	try {
	    int numHellos = 10;

	    Hello[] hellos = new Hello[numHellos];
	    int passivationCount = 0;

	    for(int i = 0; i < numHellos; i++) {
		
		hellos[i] = (Hello) new InitialContext().lookup("java:global/"+appName+"/HelloBean");
		hellos[i].hello();
	    }

	    try {
		System.out.println("Waiting for passivation...");
		Thread.sleep(10000);
	    } catch(Exception e) {
		e.printStackTrace();
	    }

	    for(int i = 0; i < numHellos; i++) {
		
		hellos[i].hello();
		if( hellos[i].passivatedAndActivated() ) {
		    passivationCount++;
		}
	    }

	    if( passivationCount != numHellos ) {
		System.out.println("Passivation failed -- count = " + passivationCount);
		throw new EJBException("passivation failed");
	    }

	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}

    }


}
