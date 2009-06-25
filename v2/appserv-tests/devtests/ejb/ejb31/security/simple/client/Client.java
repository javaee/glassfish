package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    @EJB static Hello hello;

    public static void main(String args[]) {

	appName = args.length > 0 ? args[0] : null;
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {}

    public void doTest() {

	try {

	    //	    ProgrammaticLogin login = new com.sun.appserv.security.api.ProgrammaticLogin();
	    
	    if( hello == null ) {
		hello = (Hello) new InitialContext().lookup("java:global/" + appName + "/SingletonBean!com.acme.Hello");
	    }

	    System.out.println("Singleton says : " + hello.hello());

	     stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
