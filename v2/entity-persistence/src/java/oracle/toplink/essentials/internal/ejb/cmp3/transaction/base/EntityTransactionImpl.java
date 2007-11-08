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

import javax.persistence.RollbackException;

import oracle.toplink.essentials.exceptions.TransactionException;
import oracle.toplink.essentials.internal.ejb.cmp3.base.RepeatableWriteUnitOfWork;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;

/**
 * INTERNAL:
 * Base EntityTransactionImpl
 * This is a thin wrapper around a UnitOfWork that implements functionality required
 * by the EntityTransaction interface.  The actual interface is implemented by two subclasses:
 *
 * @see oracle.toplink.essentials.internal.ejb.cmp3.transaction.EntityTransactionImpl
 * @see oracle.toplink.essentials.internal.ejb.cmp3.transaction.jdk14.EntityTransactionImpl
 *
 * The EntityTransaction is a thin wrapper around a UnitOFWork.
 */
public class EntityTransactionImpl {

        protected EntityTransactionWrapper wrapper;
        
        protected boolean active = false;
        
        protected boolean rollbackOnly = false;

        public EntityTransactionImpl(EntityTransactionWrapper wrapper) {
            this.wrapper = wrapper;
        }
      /**
       * Start the current transaction. This can only be invoked if {@link #isActive()} returns
       * <code>false</code>.
       * @throws IllegalStateException if isActive() is true.
       */
      public void begin(){
        if (isActive()){
          throw new IllegalStateException(TransactionException.transactionIsActive().getMessage());
        }
        if (this.wrapper.getEntityManager().isExtended()){
            // so we have a resource local extended em so get the PC from the entity manager
            this.wrapper.localUOW = this.wrapper.getEntityManager().getActivePersistenceContext(null);
            this.wrapper.localUOW.setShouldTerminateTransaction(false);
        }else{
            this.wrapper.localUOW = new RepeatableWriteUnitOfWork(this.wrapper.getEntityManager().getServerSession().acquireClientSession());
            this.wrapper.localUOW.setShouldTerminateTransaction(false);
            this.wrapper.localUOW.setShouldCascadeCloneToJoinedRelationship(true);
        }
        this.active = true;
      }
 
      /**
       * Commit the current transaction, writing any un-flushed changes to the database.
       * This can only be invoked if {@link #isActive()} returns <code>true</code>.
       * @throws IllegalStateException if isActive() is false.
       */
      public void commit(){
        if (!isActive()){
          throw new IllegalStateException(TransactionException.transactionNotActive().getMessage());
        }
        try {
            if (this.wrapper.localUOW != null){
                this.wrapper.localUOW.setShouldTerminateTransaction(true);
                if (! this.rollbackOnly){
                    if (this.wrapper.localUOW.shouldResumeUnitOfWorkOnTransactionCompletion()){
                        this.wrapper.localUOW.commitAndResume();
                        return;
                    }else{
                      this.wrapper.localUOW.commit();
                      // all change sets and are cleared, but the cache is kept
                      this.wrapper.localUOW.clearForClose(false);
                    }
                } else {
                    throw new RollbackException(ExceptionLocalization.buildMessage("rollback_because_of_rollback_only"));
                }
            }
        }catch (RuntimeException ex){
            if (this.wrapper.localUOW != null){
                wrapper.getEntityManager().removeExtendedPersistenceContext();
                this.wrapper.localUOW.release();
                this.wrapper.localUOW.getParent().release();
            }
            if(! this.rollbackOnly) {
                throw new RollbackException(ex);
            } else {
                // it's a RollbackException
                throw ex;
            }
        } finally {
            this.active = false;
            this.rollbackOnly = false;
            wrapper.setLocalUnitOfWork(null);            
        }
      }
 
      /**
       * Roll back the current transaction, discarding any changes that have happened
       * in this transaction. This can only be invoked if {@link #isActive()} returns
       * <code>true</code>.
       * @throws IllegalStateException if isActive() is false.
       */
      public void rollback(){
        if (!isActive()){
          throw new IllegalStateException(TransactionException.transactionNotActive().getMessage());
        }
        try{
            if (wrapper.getLocalUnitOfWork() != null){
                this.wrapper.localUOW.setShouldTerminateTransaction(true);
                this.wrapper.localUOW.release();
                this.wrapper.localUOW.getParent().release();
            }
        }finally{
            this.active = false;
            this.rollbackOnly = false;
            wrapper.getEntityManager().removeExtendedPersistenceContext();
            wrapper.setLocalUnitOfWork(null);            
        }
      }
 
    /**
    * Mark the current transaction so that the only possible
    * outcome of the transaction is for the transaction to be
    * rolled back.
    * @throws IllegalStateException if isActive() is false.
    */
    public void setRollbackOnly(){
        if (!isActive()){
            throw new IllegalStateException(TransactionException.transactionNotActive().getMessage());
        }
        this.rollbackOnly = true;
    }
    
    /**
     * Here incase a user does not commit or rollback an enityTransaction but just
     * throws it away.  If we do not rollback the txn the connection will go
     * back into the pool.
     */
    protected void finalize() throws Throwable{
        try{
            if (isActive())
                this.rollback();
        }finally{
            super.finalize();
        }
    }
    /**
    * Determine whether the current transaction has been marked
    * for rollback.
    * @throws IllegalStateException if isActive() is false.
    */
    public boolean getRollbackOnly(){
        if (!isActive()){
            throw new IllegalStateException(TransactionException.transactionNotActive().getMessage());
        }
        return this.rollbackOnly;
    }
    
    /**
       * Check to see if the current transaction is in progress.
       */
      public boolean isActive(){
          return this.active;
      }      
}
