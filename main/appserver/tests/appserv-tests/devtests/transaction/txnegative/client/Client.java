package com.sun.s1peqe.transaction.txnegative.client;
/*
 * Client.java
 *
 */

import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import javax.transaction.*;

public class Client {
    public SimpleReporterAdapter status;
    private UserTransaction             userTransaction;


    public Client() {
    status = new SimpleReporterAdapter("appserv-tests");
    try{
    userTransaction = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
    }catch(Exception ex) {
    	ex.printStackTrace();
    }
    }

    
    public static void main(String[] args) {
	System.out.println("\nStarting Txglobal Test Suite");
        Client client = new Client();

        // run the tests
        client.runTestClient();

    }
    public void runTestClient() {
	status.addDescription("This is to test the valid transaction exception!");
        try {
            System.out.println("START");

            /*Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/TestHung");
            com.sun.s1peqe.transaction.txnegative.ejb.test.TestHome thome = (com.sun.s1peqe.transaction.txhung.ejb.test.TestHome)PortableRemoteObject.narrow(objref, TestHome.class);

            com.sun.s1peqe.transaction.txnegative.ejb.test.TestRemote t = thome.create();
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
            }
            if (!result) {
                status.addStatus("txnegative testA1 ", status.PASS);
            } else {
                status.addStatus("txnegative testA1 ", status.FAIL);
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
            }
            if (!result) {
                status.addStatus("txnegative testA2 ", status.PASS);
            } else {
                status.addStatus("txnegative testA2 ", status.FAIL);
            }*/
            boolean result=false;

            try {
		userTransaction.begin();
		userTransaction.begin();
		System.out.println("TEST FAILED");
            } catch (NotSupportedException ne) {
                System.out.println("NotSupportedException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (SystemException ne) {
                System.out.println("SystemException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (Exception e) {
		e.printStackTrace();
		System.out.println("TEST FAILED");
            }
            if (result) {
                status.addStatus("txnegative t1 ", status.PASS);
            } else {
                status.addStatus("txnegative t1 ", status.FAIL);
            }
	    result = false;
            try {
		userTransaction.commit();
		System.out.println("TEST FAILED");
            } catch (IllegalStateException ne) {
                System.out.println("IllegalStateException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (SystemException ne) {
                System.out.println("SystemException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (Exception e) {
		e.printStackTrace();
		System.out.println("TEST FAILED");
            }
            if (result) {
                status.addStatus("txnegative t2 ", status.PASS);
            } else {
                status.addStatus("txnegative t2 ", status.FAIL);
            }
	    result = false;
            try {
		userTransaction.rollback();
		System.out.println("TEST FAILED");
            } catch (IllegalStateException ne) {
                System.out.println("IllegalStateException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (SystemException ne) {
                System.out.println("SystemException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (Exception e) {
		e.printStackTrace();
		System.out.println("TEST FAILED");
            }
            if (result) {
                status.addStatus("txnegative t3 ", status.PASS);
            } else {
                status.addStatus("txnegative t3 ", status.FAIL);
            }
	    result = false;
            try {
		userTransaction.setRollbackOnly();
		System.out.println("TEST FAILED");
            } catch (IllegalStateException ne) {
                System.out.println("IllegalStateException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (SystemException ne) {
                System.out.println("SystemException");
		System.out.println("TEST PASSED");
		result = true;
            } catch (Exception e) {
		e.printStackTrace();
		System.out.println("TEST FAILED");
            }
            if (result) {
                status.addStatus("txnegative t4 ", status.PASS);
            } else {
                status.addStatus("txnegative t4 ", status.FAIL);
            }



            System.out.println("FINISH");
            status.printSummary("txglobalID");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
            status.addStatus("txnegative testA1 ", status.FAIL);
        }
	}
    
}
