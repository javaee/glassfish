/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.s1asdev.security.timerStandalone.client;

import javax.ejb.EJB;
import com.sun.s1asdev.security.timerStandalone.*;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

public class Client {

    private static SimpleReporterAdapter stat = 
        new SimpleReporterAdapter("appserv-tests");

    public static void main (String[] args) {

        stat.addDescription("security-timerStandalone");
        Client client = new Client(args);
        client.doTest();
        stat.printSummary("security-timerStandalone");
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
