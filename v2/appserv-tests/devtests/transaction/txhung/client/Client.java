package com.sun.s1peqe.transaction.txhung.client;
/*
 * Client.java
 *
 */

import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txhung.ejb.test.*;

public class Client {
    public SimpleReporterAdapter status;

    public Client() {
    status = new SimpleReporterAdapter("appserv-tests");
    }

    
    public static void main(String[] args) {
	System.out.println("\nStarting Txglobal Test Suite");
        Client client = new Client();

        // run the tests
        client.runTestClient();

    }
    public void runTestClient() {
	status.addDescription("This is to test the hung transaction!");
        try {
            System.out.println("START");

            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/TestHung");
            com.sun.s1peqe.transaction.txhung.ejb.test.TestHome thome = (com.sun.s1peqe.transaction.txhung.ejb.test.TestHome)PortableRemoteObject.narrow(objref, TestHome.class);

            com.sun.s1peqe.transaction.txhung.ejb.test.TestRemote t = thome.create();
            boolean result=false;
            boolean xa = true;
            boolean nonxa = false;

            try {
                result = t.testA1(xa);
		System.out.println("TEST FAILED");
            } catch (javax.ejb.CreateException e) {
                System.out.println("CreateException");
		System.out.println("TEST FAILED");
            } catch (Exception e) {
		System.out.println("TEST PASSED");
               // System.out.println(""+e.getMessage());
            }
            if (!result) {
                status.addStatus("txhung testA1 ", status.PASS);
            } else {
                status.addStatus("txhung testA1 ", status.FAIL);
            }
	    result = false;

            try {
                result = t.testA1(nonxa);
		System.out.println("TEST FAILED");
            } catch (javax.ejb.CreateException e) {
                System.out.println("CreateException");
		System.out.println("TEST FAILED");
            } catch (Exception e) {
		System.out.println("TEST PASSED");
               // System.out.println(""+e.getMessage());
            }
            if (!result) {
                status.addStatus("txhung testA2 ", status.PASS);
            } else {
                status.addStatus("txhung testA2 ", status.FAIL);
            }


            System.out.println("FINISH");
            status.printSummary("txglobalID");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            status.addStatus("txhung testA1 ", status.FAIL);
        }
	}
    
}
