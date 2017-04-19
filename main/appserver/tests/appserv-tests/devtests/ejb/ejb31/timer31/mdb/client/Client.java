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

    public Client(String[] args) {}

    public void doTest() {

	try {

	    SingletonRemote singleton = (SingletonRemote) new InitialContext().lookup("java:global/" + appName + "/SingletonBean!com.acme.SingletonRemote");

	    System.out.println("Waiting a few seconds for timer callback...");
	    try {
		Thread.sleep(4000);
	    } catch(Exception e) {
		e.printStackTrace();
	    }

	    System.out.println("Test passed = " + singleton.getTestPassed());

	    if( singleton.getTestPassed() ) {
		stat.addStatus("local main", stat.PASS);
	    } else {
		stat.addStatus("local main", stat.FAIL);
	    }

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
