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

package com.sun.enterprise.container.common.impl;

import com.sun.enterprise.container.common.spi.JavaEETransaction;
import com.sun.appserv.connectors.spi.ResourceHandle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.*;
import javax.transaction.xa.XAResource;
import java.util.Set;

import org.jvnet.hk2.annotations.Service;


/**
 * Dummy implementation of JavaEETransaction
 */

@Service
public class JavaEETransactionProxy implements JavaEETransaction {
    public EntityManager getExtendedEntityManager(EntityManagerFactory factory) {
        return null;  // Do nothing
    }

    public EntityManager getTxEntityManager(EntityManagerFactory factory) {
        return null;  // Do nothing
    }

    public void addTxEntityManagerMapping(EntityManagerFactory factory, EntityManager em) {
        // Do nothing
    }

    public void addExtendedEntityManagerMapping(EntityManagerFactory factory, EntityManager em) {
        // Do nothing
    }

    public void removeExtendedEntityManagerMapping(EntityManagerFactory factory) {
        // Do nothing
    }

    public <T> void setContainerData(T data) {
        // Do nothing
    }

    public <T> T getContainerData() {
        return null;  // Do nothing
    }

    public Set getAllParticipatingPools() {
        return null;  // Do nothing
    }

    public Set getResources(String poolName) {
        return null;  // Do nothing
    }

    public ResourceHandle getNonXAResource() {
        return null;  // Do nothing
    }

    public void setResources(Set resources, String poolName) {
        // Do nothing
    }

    /**
     * Complete the transaction represented by this Transaction object.
     *
     * @throws javax.transaction.RollbackException
     *                               Thrown to indicate that
     *                               the transaction has been rolled back rather than committed.
     * @throws javax.transaction.HeuristicMixedException
     *                               Thrown to indicate that a heuristic
     *                               decision was made and that some relevant updates have been committed
     *                               while others have been rolled back.
     * @throws javax.transaction.HeuristicRollbackException
     *                               Thrown to indicate that a
     *                               heuristic decision was made and that all relevant updates have been
     *                               rolled back.
     * @throws SecurityException     Thrown to indicate that the thread is
     *                               not allowed to commit the transaction.
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
            SecurityException, IllegalStateException, SystemException {
        // Do nothing
    }

    /**
     * Disassociate the resource specified from the transaction associated
     * with the target Transaction object.
     *
     * @param xaRes The XAResource object associated with the resource
     *              (connection).
     * @param flag  One of the values of TMSUCCESS, TMSUSPEND, or TMFAIL.
     * @return <i>true</i> if the resource was delisted successfully; otherwise
     *         <i>false</i>.
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        return false;  // Do nothing
    }

    /**
     * Enlist the resource specified with the transaction associated with the
     * target Transaction object.
     *
     * @param xaRes The XAResource object associated with the resource
     *              (connection).
     * @return <i>true</i> if the resource was enlisted successfully; otherwise
     *         <i>false</i>.
     * @throws javax.transaction.RollbackException
     *                               Thrown to indicate that
     *                               the transaction has been marked for rollback only.
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is in the prepared state or the transaction is
     *                               inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
        return false;  // Do nothing
    }

    /**
     * Obtain the status of the transaction associated with the target
     * Transaction object.
     *
     * @return The transaction status. If no transaction is associated with
     *         the target object, this method returns the
     *         Status.NoTransaction value.
     * @throws javax.transaction.SystemException
     *          Thrown if the transaction manager
     *          encounters an unexpected error condition.
     */
    public int getStatus() throws SystemException {
        return 0;  // Do nothing
    }

    /**
     * Register a synchronization object for the transaction currently
     * associated with the target object. The transction manager invokes
     * the beforeCompletion method prior to starting the two-phase transaction
     * commit process. After the transaction is completed, the transaction
     * manager invokes the afterCompletion method.
     *
     * @param sync The Synchronization object for the transaction associated
     *             with the target object.
     * @throws javax.transaction.RollbackException
     *                               Thrown to indicate that
     *                               the transaction has been marked for rollback only.
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is in the prepared state or the transaction is
     *                               inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException,
            SystemException {
        // Do nothing
    }

    /**
     * Rollback the transaction represented by this Transaction object.
     *
     * @throws IllegalStateException Thrown if the transaction in the
     *                               target object is in the prepared state or the transaction is
     *                               inactive.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void rollback() throws IllegalStateException, SystemException {
        // Do nothing
    }

    /**
     * Modify the transaction associated with the target object such that
     * the only possible outcome of the transaction is to roll back the
     * transaction.
     *
     * @throws IllegalStateException Thrown if the target object is
     *                               not associated with any transaction.
     * @throws javax.transaction.SystemException
     *                               Thrown if the transaction manager
     *                               encounters an unexpected error condition.
     */
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        // Do nothing
    }
}
