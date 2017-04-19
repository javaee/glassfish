package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

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

    public Client(String[] args) {	
    }

    public void doTest() {

	try {

	    RemoteSingleton singleton = (RemoteSingleton) new InitialContext().lookup("java:global/" + appName + "/SingletonBean!com.acme.RemoteSingleton");

	    // Also make sure we can look it up via the product-specific
	    // global JNDI name specified via ejb-jar.xml
	    RemoteSingleton singleton2 = (RemoteSingleton) new InitialContext().lookup("ejb_ejb31_full_schema_SingletonRemote");

	    int sleepSeconds = 10;
	    System.out.println("Sleeping for " + sleepSeconds + " seconds before checking " +
			       "results...");
	    // wait a few seconds for timeout to happen
	    Thread.sleep(sleepSeconds * 1000);

	    System.out.println("Woke up");

	    boolean testResult = singleton.getTestResult();

	    if( !testResult ) {
		throw new EJBException("testResult = false");
	    } 

	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}

    }

}
