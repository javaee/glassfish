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

import com.sun.enterprise.ee.synchronization.util.concurrent.*;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.ee.EELogDomains;

/**
 * Transaction represents complete config/application sync operation
 *
 * @author Satish Viswanatham
 * @since  JDK1.4
 */
public class Transaction
{
    private static Logger _logger = Logger.getLogger(
            EELogDomains.SYNCHRONIZATION_LOGGER);

    /**
     * Starts a new transaction
     *
     * @return  returns the new created transaction object
     * @throws  SynchronizationException    if an error occurred during
     *                                      the command execution
     */
    public Transaction( int id, int np, long msecs) {
        bar = new CyclicBarrier(np, null);
        txId = id;
        numParties = np;
        timeout = msecs;
    }

    /**
     * Common preVote activity, this function is used by voteCommit and
     * VoteRollback
     *
     * @return boolean returns whether preVote phase successfully completed 
     *         or not. In case of exception, transaction is rollbacked, even 
     *         it is voted to be commited in the current case.
     */
    private boolean preVote() {
        try {
                if (timeout > 0 ) {
                    bar.attemptBarrier(timeout);
                } else {
                    bar.barrier();
                }
        } catch ( Exception e ) {
            _logger.log(Level.FINE, "synchronization.prevote.failed", e);
            return false;
        }
        return true;
    }

    /**
     * Post vote activity, typically bookkeeping with transaction manager
     * about number of active transactions.
     */
    private void postVote() {
        if (  unvotedParties() == 0 )
            TransactionManager.getTransactionManager().resolveTransaction(txId);
    }

    /**
     * Transaction paritcipant ( typically a thread) calls this function to vote
     * rollback to abort the transaction. This is a blocking call.
     */
    public void voteRollback() {
        synchronized ( this ) {
            rbVotes++; 
            votedCommit = false;
        }
        preVote();
        postVote();
    }

    /**
     * Transaction paritcipant ( typically a thread) calls this function to vote
     * commit. This is a blocking call.
     */
    public  void voteCommit() {
        boolean b = preVote();
        synchronized ( this ) {
            if ( b == true ) 
                commitVotes++; 
            else {
                rbVotes++;
                votedCommit = false;
            }
        }
        postVote();
    }

    /**
     * Returns the number of parties voted for rollback
     *
     * @return int number rollback voted parties at the moment.
     */
    public int numRollbackVotes() {
        return rbVotes;
    }

    /**
     * Returns the number of parties voted for commit
     *
     * @return int number commit voted parties at the moment.
     */
    public int numCommitVotes() {
        return commitVotes;
    }

    /**
     * Returns the number of parties not voted in the transaction
     * this could be because they are still working on the transaction
     *
     * @return int number commit voted parties at the moment.
     */
    public int unvotedParties() {
        int ucV = (numParties - (rbVotes + commitVotes));
        if ( ucV < 0 )
            ucV = 0;
        return ucV;
    }

    /**
     * Returns the number of parties paticipating in the transaction
     *
     * @return int              Total number parties.
     */
    public int totalParties() {
        return numParties;
    }

    /**
     * Returns the transaction id
     *
     * @return int              transaction id.
     */
    public int getId() {
        return txId;
    }

    /**
     * Returns the transaction timeout (msecs)
     *
     * @return int              transaction timeout.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Returns the status of the transaction, whether it is commited or not
     * this function can only be called by the thread which is participating
     * in the transaction. If a different thread calls this function, false may
     * also indicate the transaction is in progress.
     *
     * @return boolean true in case transaction commited, false in case of 
     *         rollback.
     */
    public boolean isCommited() {
        return votedCommit;
    }

    // ---- VARIABLE(S) - PRIVATE ----------------------------
    private  int txId;
    private  int numParties;
    private  int commitVotes =0;
    private  int rbVotes =0 ;
    private  long timeout;  // transaction timeout in msecs
    private boolean votedCommit = true; // presumed commit transaction model
    private CyclicBarrier bar;
}
