/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.enterprise.transaction;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.logging.*;
import javax.transaction.*;

import java.beans.PropertyChangeEvent;
import com.sun.enterprise.config.serverbeans.ServerTags;

import org.glassfish.api.invocation.InvocationManager;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {

    JavaEETransactionManager t; 

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

    public void setUp() {
        try {
            t = new JavaEETransactionManagerSimplified();
            JavaEETransactionManagerDelegate d = new JavaEETransactionManagerSimplifiedDelegate();
            t.setDelegate(d);
            d.setTransactionManager(t);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }

    }

    /**
     * Can't test more than null (but no NPE)
     */
    public void testXAResourceWrapper() {
        assertNull(t.getXAResourceWrapper("xxx"));
        assertNull(t.getXAResourceWrapper("oracle.jdbc.xa.client.OracleXADataSource"));
    }

    /**
     * Test ConfigListener call
     */
    public void testTransactionServiceConfigListener() {
        PropertyChangeEvent e1 = new PropertyChangeEvent("", ServerTags.KEYPOINT_INTERVAL, "1", "10");
        PropertyChangeEvent e2 = new PropertyChangeEvent("", ServerTags.RETRY_TIMEOUT_IN_SECONDS, "1", "10");
        try {
            TransactionServiceConfigListener l = new TransactionServiceConfigListener();
            l.setTM(t);
            l.changed(new PropertyChangeEvent[] {e1, e2});
            assert(true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testWrongTMCommit() {
        System.out.println("**Testing Wrong TM commit ===>");
        try {
            System.out.println("**Calling TM commit ===>");
            t.commit();
            System.out.println("**WRONG: TM commit successful <===");
            assert (false);
        } catch (IllegalStateException ex) {
            System.out.println("**Caught IllegalStateException <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testWrongTMRollback() {
        System.out.println("**Testing Wrong TM Rollback ===>");
        try {
            System.out.println("**Calling TM rollback ===>");
            t.rollback();
            System.out.println("**WRONG: TM rollback successful <===");
            assert (false);
        } catch (IllegalStateException ex) {
            System.out.println("**Caught IllegalStateException <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testWrongUTXCommit() {
        System.out.println("**Testing Wrong UTX commit ===>");
        try {
            UserTransaction utx = createUtx();
            System.out.println("**Calling UTX commit ===>");
            utx.commit();
            System.out.println("**WRONG: UTX commit successful <===");
            assert (false);
        } catch (IllegalStateException ex) {
            System.out.println("**Caught IllegalStateException <===");
            assert (true);
        } catch (Exception ex) {
            ex.printStackTrace();
            assert (false);
        }
    }

    public void testWrongUTXBegin() {
        System.out.println("**Testing Wrong UTX begin ===>");
        try {
            UserTransaction utx = createUtx();
            System.out.println("**Calling TWICE UTX begin ===>");
            utx.begin();
            utx.begin();
            System.out.println("**WRONG: TWICE UTX begin successful <===");
            assert (false);
        } catch (NotSupportedException ne) {
            System.out.println("**Caught NotSupportedException <===");
            assert (true);
        } catch (SystemException ne) {
            System.out.println("**Caught SystemException <===");
            assert (true);
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
            TestSync s = new TestSync(false);
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

    public void testTxSuspendResume() {
        System.out.println("**Testing TM suspend ===>");
        try {
            System.out.println("**No-tx suspend ....");
            assertNull(t.suspend());

            System.out.println("**Starting transaction ....");
            t.begin();

            Transaction tx = t.suspend();
            assertNotNull(tx);

            System.out.println("**TX suspended ....");

            System.out.println("**No-tx suspend ....");
            assertNull(t.suspend());

            System.out.println("**Calling TM resume ===>");
            t.resume(tx);

            assertEquals (JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()), 
                "Active");

            System.out.println("**Calling TX commit ===>");
            tx.commit();
            String status = JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus());
            System.out.println("**Status after commit: " + status + " <===");
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
            TestSync s = new TestSync(false);
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

    public void testTxCommitFailBC() {
        System.out.println("**Testing TX commit with exception in beforeCompletion ===>");
        try {
            // Suppress warnings from beforeCompletion() logging
            ((JavaEETransactionManagerSimplified)t).getLogger().setLevel(Level.SEVERE);

            System.out.println("**Starting transaction ....");
            t.begin();
            Transaction tx = t.getTransaction();

            System.out.println("**Registering Synchronization ....");
            TestSync s = new TestSync(true);
            tx.registerSynchronization(s);

            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**TX Status after begin: " + status);

            assertEquals (status, "Active");

            System.out.println("**Calling TX commit ===>");
            try {
                tx.commit();
                assert (false);
            } catch (RollbackException ex) {
                System.out.println("**Caught expected exception...");
            }
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

    public void testTxCommitRollbackBC() {
        System.out.println("**Testing TX commit with rollback in beforeCompletion ===>");
        try {
            // Suppress warnings from beforeCompletion() logging
            ((JavaEETransactionManagerSimplified)t).getLogger().setLevel(Level.SEVERE);

            System.out.println("**Starting transaction ....");
            t.begin();
            Transaction tx = t.getTransaction();

            System.out.println("**Registering Synchronization ....");
            TestSync s = new TestSync(t);
            tx.registerSynchronization(s);

            String status = JavaEETransactionManagerSimplified.getStatusAsString(t.getStatus());
            System.out.println("**TX Status after begin: " + status);

            assertEquals (status, "Active");

            System.out.println("**Calling TX commit ===>");
            try {
                tx.commit();
                assert (false);
            } catch (RollbackException ex) {
                System.out.println("**Caught expected exception...");
            }
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

    private UserTransaction createUtx() throws javax.naming.NamingException {
        UserTransaction utx = new UserTransactionImpl();
        InvocationManager im = new org.glassfish.api.invocation.InvocationManagerImpl();
        ((JavaEETransactionManagerSimplified)t).invMgr = im;
        ((UserTransactionImpl)utx).setForTesting(t, im);
        return utx;
    }

    static class TestSync implements Synchronization {

        // Used to validate the calls
        private boolean fail = false;
        private TransactionManager t;

        protected boolean called_beforeCompletion = false;
        protected boolean called_afterCompletion = false;

        public TestSync(boolean fail) {
            this.fail = fail;
        }

        public TestSync(TransactionManager t) {
            fail = true;
            this.t = t;
        }

        public void beforeCompletion() {
            System.out.println("**Called beforeCompletion  **");
            called_beforeCompletion = true;
            if (fail) {
                System.out.println("**Failing in beforeCompletion  **");
                if (t != null) {
                    try {
                        System.out.println("**Calling setRollbackOnly **");
                        t.setRollbackOnly();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException("");
                }
            }
        }

        public void afterCompletion(int status) {
            System.out.println("**Called afterCompletion with status:  "
                    + JavaEETransactionManagerSimplified.getStatusAsString(status));
            called_afterCompletion = true;
        }
    }
}
