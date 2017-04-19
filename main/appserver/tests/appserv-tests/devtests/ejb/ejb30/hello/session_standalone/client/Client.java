package com.sun.s1asdev.ejb.ejb30.hello.session_standalone.client;

import java.io.*;
import java.util.*;
import javax.ejb.EJB;
import com.sun.s1asdev.ejb.ejb30.hello.session_standalone.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("ejb-ejb30-hello-session_standalone");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("ejb-ejb30-hello-session_standaloneID");
    }  
    
    public Client (String[] args) {
    }

    @EJB
    private static Sful sful;

    @EJB
    private static Sless sless;

    public void doTest() {

        try {

            System.out.println("invoking stateful");
            sful.hello();

            System.out.println("invoking stateless");
            sless.hello();

            System.out.println("Sleeping to wait for timeout to happen...");
            // wait a bit for timeout to happen
            Thread.sleep(12000);

            System.out.println("Woke up. Now checking for timeout");

            boolean timeoutCalled = sless.timeoutCalled();

            if( timeoutCalled ) {
                System.out.println("verified that timeout was called");
            } else {
                throw new Exception("timeout not called");
            }

            System.out.println("test complete");

            stat.addStatus("local main", stat.PASS);

        } catch(Exception e) {
            e.printStackTrace();
            stat.addStatus("local main" , stat.FAIL);
        }
        
    	return;
    }

}

