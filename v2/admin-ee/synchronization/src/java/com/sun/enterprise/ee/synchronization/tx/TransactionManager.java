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

import com.sun.enterprise.util.i18n.StringManager;

/**
 * Transaction Manager coordinates sync transaction across request threads
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class TransactionManager
{
    private static final StringManager sMgr =
                        StringManager.getManager(TransactionManager.class);

    /**
     * Returns a handle to the transaction manager
     * @return returns the transaction manager handle
     */
    public static TransactionManager getTransactionManager() {
        return tm; 
    }

	/**
     * Starts a new transaction
     *
     * @return  returns the new created transaction object
     */
	public Transaction begin(int numParties ) {
        if ( numParties < 0 )
            throw new IllegalArgumentException(
                sMgr.getString("zeroTxTimeoutError"));
		activeTx++;
        return new Transaction( activeTx, numParties, timeout);
    }
	
	/**
     * Starts a new transaction, with the specified timeout
     *
     * @return  returns the new created transaction object
     */
	public Transaction begin(int numParties , long msecs) {
        if ( msecs < 0 )
            throw new IllegalArgumentException(
                "Transaction time out should be non zero value");
		activeTx++;
        return new Transaction( activeTx, numParties, msecs);
    }

	/**
     * Returns the number of action transaction 
     *
     * @return  returns the count of active tx's since server startup
     */
    public int numActiveTransactions() {
        return activeTx;
    }

	/**
     * Resolves the transaction, this is used to book keep active tx count
     *
     * @return  returns the count of active tx's since server startup
     */
    int resolveTransaction(int tx) {
		synchronized (lock) {
   		     return activeTx--;
		}
    }
    
    /**
     *  Sets the default transaction time out for all transactions.
     *  @param msecs  Transaction time out in milli secs
     */
    public void setDefaultTransactionTimeout(long msecs) {

		synchronized (lock) {
        	timeout = msecs;
		}
    }
    
    /**
     * Gets the default transaction timeout specified for all transactions.
     * @return default transaction timeout in milli secs
     */
    public long getDefaultTransactionTimeout() {
        return timeout;
    }
    
    // ---- VARIABLE(S) - PRIVATE ----------------------------
    private int activeTx = 0;
    private long timeout = 0;
    private Object lock = new Object();
    private static TransactionManager tm = new TransactionManager();
}
