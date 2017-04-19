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

	    CacheRemote cache = (CacheRemote) new InitialContext().lookup("java:global/" + appName + "/CacheBean!com.acme.CacheRemote");

	    // sleep for a few second
	    int sleep = 11;
	    System.out.println("Waiting " + sleep + " seconds for cache refresh...");
	    System.out.println("(Lower this value once minimum timeout is 1 sec)");
	    Thread.sleep(sleep * 1000);
	    System.out.println("Waking up from sleep. Now check cache");
	    
	    int numRefreshes = cache.checkCache();
	    
	    System.out.println("Num refreshes = " + numRefreshes);

	     stat.addStatus("local main", stat.PASS);

	} catch(Exception e) {
	    stat.addStatus("local main", stat.FAIL);
	    e.printStackTrace();
	}
    }


}
