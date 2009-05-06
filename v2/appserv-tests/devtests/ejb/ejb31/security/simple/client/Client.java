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

	    //	    ProgrammaticLogin login = new com.sun.appserv.security.api.ProgrammaticLogin();
	    
	    Hello hello = (Hello) new InitialContext().lookup("java:global/" + appName + "/SingletonBean!com.acme.Hello");

	    System.out.println("Singleton says : " + hello.hello());

	     stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
