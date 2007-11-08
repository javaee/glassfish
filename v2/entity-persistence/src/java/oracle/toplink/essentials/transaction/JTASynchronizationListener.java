/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.transaction;

import javax.transaction.Synchronization;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p>
 * <b>Purpose</b>: Synchronization object implementation for JTA 1.0
 * <p>
 * <b>Description</b>:  Instances of this class are registered against JTA 1.0
 * transactions. This class may be subclassed to provide specialized behavior
 * for specific transaction implementations. Subclasses must implement the
 * newListener() method to return an instances of the listener subclass.
 *
 * @see JTATransactionController
 */
public class JTASynchronizationListener extends AbstractSynchronizationListener implements Synchronization, SynchronizationListenerFactory {

    /**
     * PUBLIC:
     * Used to create factory instances only. Use the "full-bodied" constructor
     * for creating proper listener instances.
     */
    public JTASynchronizationListener() {
        super();
    }

    /**
     * INTERNAL:
     * Constructor for creating listener instances (expects all required state info)
     */
    public JTASynchronizationListener(UnitOfWorkImpl unitOfWork, AbstractSession session, Object transaction, AbstractTransactionController controller) {
        super(unitOfWork, session, transaction, controller);
    }

    /**
     * INTERNAL:
     * Create and return the Synchronization listener object that can be registered
     * to receive JTA transaction notification callbacks.
     */
    public AbstractSynchronizationListener newSynchronizationListener(UnitOfWorkImpl unitOfWork, AbstractSession session, Object transaction, AbstractTransactionController controller) {
        return new JTASynchronizationListener(unitOfWork, session, transaction, controller);
    }

    /**
     * INTERNAL:
     * Called by the JTA transaction manager prior to the start of the
     * transaction completion process.
     * This call is executed in the same transaction context of the caller
     * who initiates the TransactionManager.commit, or the call is executed
     * with no transaction context if Transaction.commit is used.
     */
    public void beforeCompletion() {
        super.beforeCompletion();
    }

    /**
     * INTERNAL:
     * Called by the JTA transaction manager after the transaction is committed
     * or rolled back. This method executes without a transaction context.
     *
     * @param stat The status of the transaction completion.
     */
    public void afterCompletion(int stat) {
        super.afterCompletion(new Integer(stat));
    }
}
