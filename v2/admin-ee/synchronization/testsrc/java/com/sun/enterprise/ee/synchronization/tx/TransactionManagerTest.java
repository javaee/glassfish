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

public class TransactionManagerTest extends TestCase {

    public TransactionManagerTest(String name) {
        super(name);
    }

    protected void setUp() {
    }

    protected void tearDown() {
    }

    public void testTxnMgr() {         
        Transaction txn = null;
        try {
            TransactionManager mgr = TransactionManager.getTransactionManager();

            int activeTx = mgr.numActiveTransactions();

            if ( activeTx == 0 ) {
                System.out.println(
                        "No active transactions, starting a new transaction");
            } else {
                fail(
                "No active transactions. Expected 0 for numActiveTransactions() but got " + activeTx);
            }

            long defTimeout = mgr.getDefaultTransactionTimeout();
            if ( defTimeout == 0 ) {
                System.out.println(
                        "Transaction manager default timeout is 0");
            } else {
                fail(
                 "Transaction manager default timeout is not 0. Expected 0.");
            }

            mgr.setDefaultTransactionTimeout(100);
            
            defTimeout = mgr.getDefaultTransactionTimeout();
            if ( defTimeout == 100 ) {
                System.out.println(
                        "Transaction manager timeout is set to 100");
            } else {
                fail(
                 "Transaction manager default timeout is " + defTimeout + 
                 " Expected 100.");
            }

            txn = mgr.begin(10, 0);

            activeTx = mgr.numActiveTransactions();

            if ( activeTx == 1 ) {
                System.out.println(
                        "1 active transaction");
            } else {
                fail(
                " Expected 1 for numActiveTransactions() but got " + activeTx);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.toString());
        }
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(TransactionManagerTest.class);
    }

}
