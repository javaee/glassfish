/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.ee.synchronization.tx;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;


/**
 * Class CommitWorker - thread class for a transaction branch
 *
 */

class TranWorker implements Runnable {
        
    TranWorker(Transaction tx, int n, boolean failureInjection) {
        _failureInjection = failureInjection;
        _tx = tx;
        _id = n;
    }

    public void run() {
        try { 
            System.out.println("Running Thread " + _id);
            if ( _failureInjection ) {
                _tx.voteRollback();
            } else {
                _tx.voteCommit();
            }
            System.out.println("AFTER VOTE " + _id);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (_tx.isCommited()) {
                    System.out.println("FAIL: TX Commited " + _id);
            } else {
                System.out.println("TX Rollbacked " + _id);
            }
        }
    }

    boolean _failureInjection = false;
    Transaction _tx = null;
    int _id         = 0;
}


public class TransactionsTest extends TestCase {

    public TransactionsTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testCommit() {
        Transaction txn = createWorkers(false);
        printTxStatus(txn);
        if (txn.isCommited()) {
                System.out.println("TX Commited " + txn.getId());
        } else {
            fail("TX Rollbacked. Expected TX committed "  + txn.getId());
        }
    }

    public void testRollback() {
        Transaction txn = createWorkers(true);
        printTxStatus(txn);
        if (txn.isCommited()) {
            fail("TX Committed. Expected TX rollbacked "  + txn.getId());
        } else {
            System.out.println("TX Rollbacked " + txn.getId());
        }
    }

    private void printTxStatus(Transaction txn) {
        System.out.println("TX total participants " + txn.totalParties());
        System.out.println(" voted rollback  " + txn.numRollbackVotes());
        System.out.println(" voted commit  " + txn.numCommitVotes());
        System.out.println(" unvoted number   " + txn.unvotedParties());
        int calcTotal = txn.numRollbackVotes() + txn.numCommitVotes() +
                            txn.unvotedParties();
        if ( calcTotal == txn.totalParties() ) {
           System.out.println(" Checked to see if total number parties matched to total rollbacked + total committed + total unvoted"); 
        } else {
            fail(" calc total number did not match reported total parties");
        }
    }

    public Transaction createWorkers(boolean failureInjection) {         
        Transaction txn = null;
        try {
            TransactionManager mgr = TransactionManager.getTransactionManager();

            txn = mgr.begin(txN, 0);

            TranWorker[] workers = new TranWorker[txN];
            Thread[] threads = new Thread[txN];
            for (int i=0; i<workers.length; i++) {
                if (( failureIdx == i )  && failureInjection) {
                    workers[i] = new TranWorker(txn, i, true);
                } else {
                    workers[i] = new TranWorker(txn, i, false);
                }
                threads[i] = new Thread(workers[i], "TH-"+i);
                threads[i].start();
            }

            for (int i=0; i<workers.length; i++) {
                threads[i].join();
                System.out.println("Thread " + i + " is done!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        } 
        return txn;
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TransactionsTest.class);
    }

    /** Number of threads used in the test **/
    private static int txN = 10;

    /** Number of fault thread (which throws exception). This helps testing
        rollback scenario **/
    private static int failureIdx = 4;
}
