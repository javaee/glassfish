package com.acme;


import javax.ejb.*;
import javax.annotation.*;

import javax.naming.InitialContext;

import java.util.concurrent.*;


import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    private static String appName;

    private RemoteAsync remoteAsync;

    private int num;

    public static void main(String args[]) {

	appName = args[0]; 
	stat.addDescription(appName);
	Client client = new Client(args);       
        client.doTest();	
        stat.printSummary(appName + "ID");
    }

    public Client(String[] args) {
	num = Integer.valueOf(args[1]);
    }

    public void doTest() {

	try {

	  
	    remoteAsync = (RemoteAsync) new InitialContext().lookup("java:global/" + appName + "/SingletonBean!com.acme.RemoteAsync");
	    Future<String> future = remoteAsync.hello("Bob");
	    
	    boolean isDone = future.isDone();
	    System.out.println("isDone = " + isDone);

	    String result = future.get();

	    if( !future.isDone() ) {
		throw new RuntimeException("isDone should have been true");
	    }
	    
	    System.out.println("Remote bean says " + result);
		
	    stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }



}
