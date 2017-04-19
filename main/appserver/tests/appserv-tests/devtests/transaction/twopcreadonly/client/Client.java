/**
 * Copyright š 2002 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.s1peqe.transaction.txglobal.client;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import com.sun.s1peqe.transaction.txglobal.ejb.beanB.*;


public class Client {

    private TxRemoteHomeB home = null;
    private SimpleReporterAdapter status =
        new SimpleReporterAdapter("appserv-tests");

    public Client() {
    }

    public static void main(String[] args) { 
        System.out.println("\nStarting Txglobal Test Suite");  
        Client client = new Client(); 

        // initialize the context and home object
        client.setup();

        // run the tests
        client.runTestClient();   
    }

    public void setup() {
        Class homeClass = TxRemoteHomeB.class;
        try {
            // Initialize the Context
            Context context = new InitialContext();
            System.out.println("Context Initialized...");

            // Create Home object
            java.lang.Object obj = context.lookup("java:comp/env/ejb/TxBeanB");
            home = (TxRemoteHomeB) PortableRemoteObject.narrow(obj, homeClass);
            System.out.println("Home Object Initialized...");
        } catch (Throwable ex) {
            System.out.println("Exception in setup: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void runTestClient() {
        try{
            status.addDescription("This tests the commitStatus of two read only resources");
            test1();
            test2();
            status.printSummary("txglobalID");
        } catch (Exception ex) {
            System.out.println("Exception in runTestClient: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void test1() {
        try {  
            System.out.println("Execute BeanB::test1");

            TxRemoteB beanB = home.create();
            beanB.test1();
            int result = beanB.getCommitStatus();

            if (result == 0) {
                status.addStatus("txglobal test1: ", status.PASS);
            } else {	 
                status.addStatus("txglobal test1: ", status.FAIL);
            }

            beanB.remove();
        } catch (Exception ex) {
            status.addStatus("txglobal test1: ", status.FAIL);
            System.out.println("Exception in test1: " + ex.toString());
            ex.printStackTrace();
        }
    }

    public void test2() {
        TxRemoteB beanB = null;
        try {  
            System.out.println("Execute BeanB::test2");

            beanB = home.create();
            beanB.test2();
            int result = beanB.getCommitStatus();

            if (result == 1) {
                status.addStatus("txglobal test2: ", status.PASS);
            } else {	 
                status.addStatus("txglobal test2: ", status.FAIL);
            }
            beanB.remove();
        } catch (Throwable ex) {
            status.addStatus("txglobal test2: ", status.FAIL);
            System.out.println("Exception in test2: " + ex.toString());
            ex.printStackTrace();
        }
    }

}
