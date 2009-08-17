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

	    Hello hello = (Hello) new InitialContext().lookup("java:global/" + appName + "/SingletonBean");

	    System.out.println("Singleton says : " + hello.hello());

	    try {
		hello.testError();
		throw new RuntimeException("Expected EJBException");
	    } catch(EJBException e) {
		System.out.println("Got expected EJBException after java.lang.Error thrown from ejb");
	    }

	     stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
