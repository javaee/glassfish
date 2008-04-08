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
package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.PerLookup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple transaction mechanism for config-api objects
 *
 * @author Jerome Dochez
 */
public class Transaction {

    final LinkedList<Transactor> participants = new LinkedList<Transactor>();

    /**
	 * Enlists a new participant in this transaction
     *
     * @param t new participant to this transaction
	 * 
	 */
    synchronized void addParticipant(Transactor t) {
        // add participants first so the lastly created elements are processed before the parent
        // is modified, this is expecially important when sub elements have key attributes the parent
        // need (or the habitat).
          participants.addFirst(t);
    }

	/**
	 * Rollbacks all participants to this transaction. 
	 */
    public synchronized void rollback() {
        for (Transactor t : participants) {
            t.abort(this);
        }
    }

	/**
	 * Commits all participants to this transaction
	 * 
	 * @return list of PropertyChangeEvent for the changes that were applied to the 
	 * participants during the transaction.
     * @throws RetryableException if the transaction cannot commit at this time but
     * could succeed later.
     * @throws TransactionFailure if the transaction commit failed.
	 */
    public synchronized List<PropertyChangeEvent> commit()
            throws RetryableException, TransactionFailure {

        for (Transactor t : participants) {
            if (!t.canCommit(this)) {
                throw new RetryableException();
            }
        }
        List<PropertyChangeEvent> transactionChanges = new ArrayList<PropertyChangeEvent>();
        for (Transactor t : participants) {
            transactionChanges.addAll(t.commit(this));            
        }
        Transactions.get().addTransaction(transactionChanges);
        return transactionChanges;
    }

}
