package com.sun.enterprise.transaction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.logging.*;
import javax.transaction.*;

import org.glassfish.api.invocation.InvocationManager;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    TransactionManager t; 
    Logger l = Logger.getAnonymousLogger();

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

    public void setUp() {
        try {
            t = new JavaEETransactionManagerSimplified();
            ((JavaEETransactionManagerSimplified)t)._logger = l;
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }

    }

    public void testBegin() {
        System.out.println("**Testing TM begin ===>");
        try {
            System.out.println("**Status before begin: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()));

            t.begin();
            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**Status after begin: "  + status + " <===");
            assertEquals (status, "Active");
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testCommit() {
        System.out.println("**Testing TM commit ===>");
        try {
            System.out.println("**Starting transaction ....");
            t.begin();
            assertEquals (JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()), 
                "Active");

            System.out.println("**Calling TM commit ===>");
            t.commit();
            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**Status after commit: " + status + " <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testRollback() {
        System.out.println("**Testing TM rollback ===>");
        try {
            System.out.println("**Starting transaction ....");
            t.begin();
            assertEquals (JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()), 
                "Active");

            System.out.println("**Calling TM rollback ===>");
            t.rollback();
            System.out.println("**Status after rollback: " 
                    + JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus()) 
                    + " <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testTxCommit() {
        System.out.println("**Testing TX commit ===>");
        try {
            System.out.println("**Starting transaction ....");
            t.begin();
            Transaction tx = t.getTransaction();

            System.out.println("**Registering Synchronization ....");
            TestSync s = new TestSync();
            tx.registerSynchronization(s);

            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**TX Status after begin: " + status);

            assertEquals (status, "Active");

            System.out.println("**Calling TX commit ===>");
            tx.commit();
            System.out.println("**Status after commit: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus())
                    + " <===");
            assertTrue ("beforeCompletion was not called", s.called_beforeCompletion);
            assertTrue ("afterCompletion was not called", s.called_afterCompletion);
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testTxRollback() {
        System.out.println("**Testing TX rollback ===>");
        try {
            System.out.println("**Starting transaction ....");
            t.begin();
            Transaction tx = t.getTransaction();

            System.out.println("**Registering Synchronization ....");
            TestSync s = new TestSync();
            tx.registerSynchronization(s);

            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**TX Status after begin: " + status);

            assertEquals (status, "Active");

            System.out.println("**Calling TX rollback ===>");
            tx.rollback();
            System.out.println("**Status after rollback: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus())
                    + " <===");
            assertFalse ("beforeCompletion was called", s.called_beforeCompletion);
            assertTrue ("afterCompletion was not called", s.called_afterCompletion);
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testUTxCommit() {
        System.out.println("**Testing UTX commit ===>");
        try {
            UserTransaction utx = createUtx();
            System.out.println("**Starting transaction ....");
            utx.begin();
            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**UTX Status after begin: " + status);

            assertEquals (status, "Active");

            System.out.println("**Calling UTX commit ===>");
            utx.commit();
            System.out.println("**Status after commit: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus())
                    + " <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testUTxRollback() {
        System.out.println("**Testing UTX rollback ===>");
        try {
            UserTransaction utx = createUtx();
            System.out.println("**Starting transaction ....");
            utx.begin();

            assertEquals (JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus()), 
                "Active");

            System.out.println("**Calling UTX rollback ===>");
            utx.rollback();
            System.out.println("**Status after rollback: "
                    + JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus())
                    + " <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    private UserTransaction createUtx() throws javax.naming.NamingException {
        UserTransaction utx = new UserTransactionImpl();
        InvocationManager im = new org.glassfish.api.invocation.InvocationManagerImpl();
        ((JavaEETransactionManagerSimplified)t).invMgr = im;
        ((UserTransactionImpl)utx).setForTesting(t, im, l);
        return utx;
    }

    static class TestSync implements Synchronization {

        // Used to validate the calls
        protected boolean called_beforeCompletion = false;
        protected boolean called_afterCompletion = false;

        public void beforeCompletion() {
            System.out.println("**Called beforeCompletion  **");
            called_beforeCompletion = true;
        }

        public void afterCompletion(int status) {
            System.out.println("**Called afterCompletion with status:  "
                    + JavaEETransactionManagerSimplified.getStatusAsString(status));
            called_afterCompletion = true;
        }
    }
}
