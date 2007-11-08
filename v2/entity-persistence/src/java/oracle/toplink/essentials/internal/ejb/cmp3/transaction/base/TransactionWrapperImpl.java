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
package oracle.toplink.essentials.internal.ejb.cmp3.transaction.base;

import oracle.toplink.essentials.exceptions.TransactionException;
import oracle.toplink.essentials.internal.ejb.cmp3.base.*;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;

public abstract class TransactionWrapperImpl  {

    protected EntityManagerImpl entityManager = null;
        
    //This attribute will store a reference to the non transactional UnitOfWork used
    // for queries outside of a transaction
    protected RepeatableWriteUnitOfWork localUOW;
        
    //used to cache the transactional UnitOfWork so that we do not need to look it up each time.
    protected Object txnKey;
    
    
    public TransactionWrapperImpl(EntityManagerImpl entityManager){
        this.entityManager = entityManager;
    }
        
    /**
     * INTERNAL:
     * This method will be used to check for a transaction and throws exception if none exists.
     * If this methiod returns without exception then a transaction exists.
     * This method must be called before accessing the localUOW.
     */
    public abstract Object checkForTransaction(boolean validateExistence);
 
    /**
     * INTERNAL:
     * Clears the transactional UnitOfWork
     */
    public void clear(){
        if (this.localUOW != null){
            // all change sets and cache are cleared
            this.localUOW.clear(true);
        }
    }
    
    /**
     * INTERNAL:
     * THis method is used to get the active UnitOfWork.  It is special in that it will
     * return the required RepeatableWriteUnitOfWork required by the EntityManager.  Once
     * RepeatableWrite is merged into existing UnitOfWork this code can go away.
     * @param transaction
     * @return
     */
    public abstract RepeatableWriteUnitOfWork getTransactionalUnitOfWork(Object transaction);

    public abstract void registerUnitOfWorkWithTxn(UnitOfWorkImpl uow);
    
    public UnitOfWorkImpl getLocalUnitOfWork(){
        return localUOW;
    }

    public void setLocalUnitOfWork(RepeatableWriteUnitOfWork uow){
        this.localUOW = uow;
    }

    /**
    * Mark the current transaction so that the only possible
    * outcome of the transaction is for the transaction to be
    * rolled back.
    * This is an internal method and if the txn is not active will do nothing
    */
    public abstract void setRollbackOnlyInternal();
    
    /**
     * This method will be called when a query is executed.  If changes in the entity manager
     * should be flushed this method should return true
     */
    public abstract boolean shouldFlushBeforeQuery(UnitOfWorkImpl uow);
    
}
