package com.sun.s1asdev.ejb.ejb30.ee.remote_client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import javax.naming.InitialContext;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args)
	throws Exception {

        stat.addDescription("ejb-ejb30-ee-remote_sfsb");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-ee-remote_sfsbID");
    }  
    
    public Client (String[] args) {
    }
    
    @EJB
    private static SfulProxy proxy;

    public void doTest() {


	boolean initialized = proxy.initialize();
        stat.addStatus("remote initialize",
	    initialized ? stat.PASS : stat.FAIL);
	if (initialized) {
        try {
            System.out.println("invoking stateless");
            String result = proxy.sayHello();
            stat.addStatus("remote hello",
		"Hello".equals(result) ? stat.PASS : stat.FAIL);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("remote hello" , stat.FAIL);
        }

	try {
	    for (int i=0; i<5; i++) {
                String result = proxy.sayRemoteHello();
                proxy.doCheckpoint();
	    }
	    for (int i=0; i<5; i++) {
                String result = proxy.sayRemoteHello();
                proxy.sayHello();
	    }
	    for (int i=0; i<5; i++) {
                String result = proxy.sayRemoteHello();
                proxy.doCheckpoint();
	    }
            stat.addStatus("remote remote_hello", stat.PASS);
        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("remote remote_hello" , stat.FAIL);
        }
	}

        System.out.println("test complete");
    }


    private static void sleepFor(int seconds) {
	while (seconds-- > 0) {
	    try { Thread.sleep(1000); } catch (Exception ex) {}
	    System.out.println("Sleeping for 1 second. Still " + seconds + " seconds left...");
	}
    }

}
