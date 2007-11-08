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
package oracle.toplink.essentials.sessions;

import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: Interface for external transaction management.
 * <p>
 * <b>Description</b>: This interface represents a delegate to be used for external
 * transaction management. The implementing class may interface to an OMG OTS service,
 * a Java JTA service or a manufacturer's specific implementation of these services.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Define the API for UnitOfWork to add a listener to the externally controlled transaction.
 * </ul>
 */
public interface ExternalTransactionController {

    /**
     * INTERNAL:
     * Begin a transaction externally.
     * This allows for TopLink to force a JTS transaction.
     */
    void beginTransaction(AbstractSession session);

    /**
     * INTERNAL:
     * Commit a transaction externally.
     * This allows for TopLink to force a JTS transaction.
     */
    void commitTransaction(AbstractSession session);

    /**
     * INTERNAL:
     * Return the active unit of work for the current active external transaction.
     */
    UnitOfWorkImpl getActiveUnitOfWork();

    /**
     * INTERNAL:
     * Return the manager's session.
     */
    AbstractSession getSession();

    /**
     * INTERNAL:
     * Register a listener on the unit of work.
     * The listener will callback to the unit of work to tell it to commit and merge.
     */
    void registerSynchronizationListener(UnitOfWorkImpl uow, AbstractSession session) throws DatabaseException;

    /**
     * INTERNAL:
     * Rollback a transaction externally.
     * This allows for TopLink to force a JTS transaction.
     */
    void rollbackTransaction(AbstractSession session);

    /**
     * INTERNAL:
     * Marks the external transaction for rollback only.
     */
    void markTransactionForRollback();

    /**
     * INTERNAL:
     * Set the manager's session.
     */
    void setSession(AbstractSession session);
}
