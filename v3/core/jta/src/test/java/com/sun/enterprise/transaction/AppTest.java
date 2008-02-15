package com.sun.enterprise.transaction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.logging.*;
import javax.transaction.*;

/**
 * Unit test for simple App.
 */
public class AppTest
        extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() throws Exception {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    public void testBegin() {
        System.out.println("**Testing TM begin ===>");
        try {
            TransactionManager t = new JavaEETransactionManagerSimplified();
            ((JavaEETransactionManagerSimplified)t)._logger = Logger.getAnonymousLogger();

            System.out.println("**Status before begin: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));
            t.begin();
            System.out.println("**Status after begin: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testCommit() {
        System.out.println("**Testing TM commit ===>");
        try {
            TransactionManager t = new JavaEETransactionManagerSimplified();
            ((JavaEETransactionManagerSimplified)t)._logger = Logger.getAnonymousLogger();

            t.begin();
            System.out.println("**Status after begin: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));

            System.out.println("**Calling TM commit ===>");
            t.commit();
            System.out.println("**Status after commit: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testRollback() {
        System.out.println("**Testing TM rollback ===>");
        try {
            TransactionManager t = new JavaEETransactionManagerSimplified();
            ((JavaEETransactionManagerSimplified)t)._logger = Logger.getAnonymousLogger();

            t.begin();
            System.out.println("**Status after begin: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));

            System.out.println("**Calling TM rollback ===>");
            t.rollback();
            System.out.println("**Status after rollback: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testTxCommit() {
        System.out.println("**Testing TX commit ===>");
        try {
            TransactionManager t = new JavaEETransactionManagerSimplified();
            ((JavaEETransactionManagerSimplified)t)._logger = Logger.getAnonymousLogger();

            t.begin();
            Transaction tx = t.getTransaction();
            tx.registerSynchronization(new TestSync());

            System.out.println("**Status after begin: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));

            System.out.println("**Calling TX commit ===>");
            tx.commit();
            System.out.println("**Status after commit: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testTxRollback() {
        System.out.println("**Testing TX rollback ===>");
        try {
            TransactionManager t = new JavaEETransactionManagerSimplified();
            ((JavaEETransactionManagerSimplified)t)._logger = Logger.getAnonymousLogger();

            t.begin();
            Transaction tx = t.getTransaction();
            tx.registerSynchronization(new TestSync());

            System.out.println("**Status after begin: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));

            System.out.println("**Calling TX rollback ===>");
            tx.rollback();
            System.out.println("**Status after rollback: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    static class TestSync implements Synchronization {
        public void beforeCompletion() {
            System.out.println("**In beforeCompletion  **");
        }
        public void afterCompletion(int status) {
            System.out.println("**In afterCompletion:  "
                    + JavaEETransactionManagerSimplified.getStatusAsString(status));
        }
    }
}
