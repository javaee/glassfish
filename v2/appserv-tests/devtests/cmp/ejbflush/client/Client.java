/*
 * Client.java
 *
 * Created on February 21, 2003, 3:20 PM
 */

import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;


/**
 * This class is used to test setting CMP field 'name' to a value
 * that is too large for the column size that it is mapped to.
 * The test is executed with flush after business method (set to true
 * for setNameWithFlush()), and without flush (is not set, i.e. false
 * for seName()).
 * The test is executed for CMP1.1 bean (A1) and CMP2.x bean (A2).
 *
 * @author  mvatkina
 */
public class Client {
    
    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
       
        try {
            System.out.println("START");
	    stat.addDescription("ejbflush");

            Context initial = new InitialContext();

            Object objref = initial.lookup("java:comp/env/ejb/TestFlush");
            test.TestHome thome = (test.TestHome)PortableRemoteObject.narrow(objref, test.TestHome.class);

            test.Test t = thome.create();

            // Run CMP1.1 test without flush. This test will fail
            // at transaction commit.
            try {
                t.testA1();
                System.out.println("A1 FAILED");
            } catch (javax.ejb.CreateException e) {
                System.out.println("A1 FAILED");
            } catch (Exception e) {
                System.out.println("A1 OK");
            }

            // Run CMP2.x test without flush. This test will fail
            // at transaction commit.
            try {
                t.testA2();
                System.out.println("A2 FAILED");
            } catch (javax.ejb.CreateException e) {
                System.out.println("A1 FAILED");
            } catch (Exception e) {
                System.out.println("A2 OK");
            }

            // Run CMP1.1 test with flush. This test should throw 
            // a FlushException.
            try {
                t.testA1WithFlush();
                System.out.println("A1 FAILED");
            } catch (test.FlushException e) {
                System.out.println("A1 OK");
            } catch (Exception e) {  
                System.out.println("A1 FAILED " + e.getMessage());
            }

            // Run CMP1.1 test with flush. This test should throw 
            // a FlushException.
            try {
                t.testA2WithFlush();
                System.out.println("A2 FAILED");
            } catch (test.FlushException e) {
                System.out.println("A2 OK");
            } catch (Exception e) {  
                System.out.println("A2 FAILED " + e.getMessage());
            }

	    stat.addStatus("ejbclient ejbflush", stat.PASS);
            System.out.println("FINISH");

        } catch (Exception ex) {
            System.err.println("Caught an exception:");
            ex.printStackTrace();
	    stat.addStatus("ejbclient ejbflush", stat.PASS);
        }

	  stat.printSummary("ejbflush");
    }
    
}
