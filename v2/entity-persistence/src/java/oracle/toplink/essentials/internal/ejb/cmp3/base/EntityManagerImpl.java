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

package oracle.toplink.essentials.internal.ejb.cmp3.base;

import java.util.Vector;
import java.util.Map;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;

import java.util.HashMap;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.exceptions.TopLinkException;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.internal.ejb.cmp3.transaction.base.TransactionWrapperImpl;
import oracle.toplink.essentials.internal.localization.ExceptionLocalization;
import oracle.toplink.essentials.internal.sessions.IsolatedClientSession;
import oracle.toplink.essentials.internal.sessions.MergeManager;
import oracle.toplink.essentials.internal.helper.IdentityHashtable;
import oracle.toplink.essentials.internal.descriptors.OptimisticLockingPolicy;
import oracle.toplink.essentials.descriptors.VersionLockingPolicy;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.threetier.ServerSession;
import oracle.toplink.essentials.tools.sessionmanagement.SessionManager;
import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.essentials.config.TopLinkProperties;


/**
* <p>
* <b>Purpose</b>: Contains the implementation of the EntityManager.
* <p>
* <b>Description</b>: This class provides the implementation for the combined TopLink
* and EJB3.0 EntityManager class.  
* <p>
* <b>Responsibilities</b>:It is responcible for tracking transaction state and the
* objects within that transaction.
* @see javax.persistence.EntityManager
* @see oracle.toplink.essentials.ejb.cmp3.EntityManager
*/

/*  @author  gyorke  
 *  @since   TopLink 10.1.3 EJB 3.0 Preview
 */


public abstract class EntityManagerImpl {
   
    protected TransactionWrapperImpl transaction = null;
    protected boolean isOpen = true;
    
    protected RepeatableWriteUnitOfWork extendedPersistenceContext;
    //THis attribute references the ServerSession that this deployement is using.
    //This is a simple mechanism to reduce the number of SessionManager accesses.
    protected ServerSession serverSession;
    // References the factory that has created this entity manager
    // to make sure that the factory is not garbage collected
    protected EntityManagerFactoryImpl factory;
    
    //We have not removed these flags because we may want to use them at a later date to provide transactional EntityManagers in JAVA SE
    protected boolean extended;
    protected boolean propagatePersistenceContext;
    
    //gf3334, force begin early transaction flag.
    protected boolean beginEarlyTransaction = false;
    
    //gf3334, this is place holder for properties that passed from createEntityManager 
    protected Map properties;


    protected abstract void setJTATransactionWrapper();
    protected abstract void setEntityTransactionWrapper();
    /**
    * Internal method. Indicates whether flushMode is AUTO.
    * @return boolean
    */
    public abstract boolean isFlushModeAUTO();
    
    /**
     * Constructor returns an EntityManager assigned to the a particular ServerSession.
     * @param sessionName the ServerSession name that should be used.
     * This constructor can potentially throw TopLink exceptions regarding the existence, or
     * errors with the specified session.
     */
    public EntityManagerImpl(String sessionName, boolean propagatePersistenceContext, boolean extended){
        this((ServerSession) SessionManager.getManager().getSession(sessionName), null, propagatePersistenceContext, extended);
    }

    /**
     * Constructor called from the EntityManagerFactory to create an EntityManager
     * @param serverSession the serverSession assigned to this deployment.
     */
    public EntityManagerImpl(ServerSession serverSession, boolean propagatePersistenceContext, boolean extended){
        this(serverSession, null, propagatePersistenceContext, extended);
    }


    /**
     * Constructor called from the EntityManagerFactory to create an EntityManager
     * @param serverSession the serverSession assigned to this deployment.
     * Note: The properties argument is provided to allow properties to be passed into this EntityManager,
     * but there are currently no such properties implemented
     */
    public EntityManagerImpl(ServerSession serverSession, Map properties, boolean propagatePersistenceContext, boolean extended){
        this.serverSession = serverSession;
        detectTransactionWrapper();
        this.extended = true;
        this.propagatePersistenceContext = false;
        this.properties=properties;
        setBeginEarlyTransaction();
    }

    /**
     * Constructor called from the EntityManagerFactory to create an EntityManager
     * @param factory the EntityMangerFactoryImpl that created this entity manager.
     * Note: The properties argument is provided to allow properties to be passed into this EntityManager,
     * but there are currently no such properties implemented
     */
    public EntityManagerImpl(EntityManagerFactoryImpl factory, Map properties, boolean propagatePersistenceContext, boolean extended){
        this.factory = factory;
        this.serverSession = factory.getServerSession();
        detectTransactionWrapper();
        this.extended = true;
        this.propagatePersistenceContext = false;
        this.properties=properties;
        setBeginEarlyTransaction();
    }

    /**
    * Clear the persistence context, causing all managed
    * entities to become detached. Changes made to entities that
    * have not been flushed to the database will not be
    * persisted.
    */
    public void clear(){
        try {
            verifyOpen();
            if (this.isExtended()){
                if(this.extendedPersistenceContext != null){
                    if (checkForTransaction(false) == null){
                        // clear all change sets and cache
                        this.extendedPersistenceContext.clearForClose(true);
                        this.extendedPersistenceContext = null;
                    } else {
                        // clear all change sets created after the last flush and cache
                        this.extendedPersistenceContext.clear(true);
                    }
                }

            } else {
                transaction.clear();
            }
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
    }

    /**
    * Internal method called by EntityTransactionImpl class in case of transaction rollback.
    * The caller is responsible for releasing extendedPersistenceContext and it's parent.
    */
    public void removeExtendedPersistenceContext(){
        this.extendedPersistenceContext = null;
    }

    /**
	 * If in a transaction this method will check for existence and register the object if
	 * it is new.  The instance of the entity provided will become managed.
	 * @param entity
	 * @throws IllegalArgumentException if the given Object is not an entity
	 */
	public void persist(Object entity){
        try {
            verifyOpen();
            if (entity == null){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[] {entity}));
            }
            try {
                getActivePersistenceContext(checkForTransaction(!isExtended())).registerNewObjectForPersist(entity, new IdentityHashtable());
            } catch (RuntimeException e) {
                if (ValidationException.class.isAssignableFrom(e.getClass())){
                    throw new EntityExistsException(e.getLocalizedMessage() , e);
                }
                throw e;
            }         
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
    }
	
	/**
	* Merge the state of the given entity into the
	* current persistence context, using the unqualified
	* class name as the entity name.
	* @param entity
	* @return the instance that the state was merged to
	* @throws IllegalArgumentException if given Object is not an entity or is a removed entity
	*/
	protected Object mergeInternal(Object entity){
        if (entity == null){
        	throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[] {entity}));
        }
        //gf830 - merging a removed entity should throw exception
        if (getActivePersistenceContext(checkForTransaction(!isExtended())).getDeletedObjects().contains(entity)){ 
        	throw new IllegalArgumentException(ExceptionLocalization.buildMessage("cannot_merge_removed_entity", new Object[]{entity}));
        }
        try {
        	return getActivePersistenceContext(checkForTransaction(!isExtended())).mergeCloneWithReferences(entity, MergeManager.CASCADE_BY_MAPPING, true);
        } catch (oracle.toplink.essentials.exceptions.OptimisticLockException ole) {
            throw new javax.persistence.OptimisticLockException(ole);
        }
	}
	
	/**
	* Remove the instance.
	* @param entity
	* @throws IllegalArgumentException if Object passed in is not an entity
	*/
	public void remove(Object entity){
        try {
            verifyOpen();
			if (entity == null){ //gf732 - check for null
            	throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[] {entity}));
            }
            try{
                getActivePersistenceContext(checkForTransaction(!isExtended())).performRemove(entity, new IdentityHashtable());
            }catch (RuntimeException e){
                throw e;
            }
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
	}
	
	/**
	* Find by primary key.
	* @param entityName
	* @param primaryKey
	* @return the found entity instance
	* @throws IllegalArgumentException if the first argument does not indicate an entity or if the
	* second argument is not a valid type for that entity's primaryKey
	*/
	public Object find(String entityName, Object primaryKey){
        try {
            verifyOpen();
            Session session = getActiveSession();
            ClassDescriptor descriptor = session.getDescriptorForAlias(entityName);
            if (descriptor == null || descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("unknown_entitybean_name", new Object[] {entityName}));
            }
            if (primaryKey == null){ //gf721 - check for null PK
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_pk"));
            }
            if ( ((CMP3Policy)descriptor.getCMPPolicy()).getPKClass() != null && !((CMP3Policy)descriptor.getCMPPolicy()).getPKClass().isAssignableFrom(primaryKey.getClass())){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("invalid_pk_class", new Object[] {((CMP3Policy)descriptor.getCMPPolicy()).getPKClass(), primaryKey.getClass()}));
            }
            return findInternal(descriptor, session, primaryKey);
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
	}
	
	/**
	* Find by primary key.
	* @param entityClass
	* @param primaryKey
	* @return the found entity instance or null
	* if the entity does not exist
	* @throws IllegalArgumentException if the first argument does
	* not denote an entity type or the second argument is not a valid type for that
	* entity's primary key
	*/
	protected Object findInternal(Class entityClass, Object primaryKey) {
	    Session session = getActiveSession();
	    ClassDescriptor descriptor = session.getDescriptor(entityClass);
	    if (descriptor == null || descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()){
	        throw new IllegalArgumentException(ExceptionLocalization.buildMessage("unknown_bean_class", new Object[]{ entityClass}));
	    }
	    if (primaryKey == null){ //gf721 - check for null PK
	    	throw new IllegalArgumentException(ExceptionLocalization.buildMessage("null_pk"));
	    }
        if ( ((CMP3Policy)descriptor.getCMPPolicy()).getPKClass() != null && !((CMP3Policy)descriptor.getCMPPolicy()).getPKClass().isAssignableFrom(primaryKey.getClass())){
            throw new IllegalArgumentException(ExceptionLocalization.buildMessage("invalid_pk_class", new Object[] {((CMP3Policy)descriptor.getCMPPolicy()).getPKClass(), primaryKey.getClass()}));
        }
        return findInternal(descriptor, session, primaryKey);
	}
	
	/**
	* Find by primary key.
	* @param descriptor
	* @param session
	* @param primaryKey
	* @return the found entity instance or null
	* if the entity does not exist
	* @throws IllegalArgumentException if the first argument does
	* not denote an entity type or the second argument is not a valid type for that
	* entity's primary key
	*/
	protected static Object findInternal(ClassDescriptor descriptor, Session session, Object primaryKey){
        Vector pk;
        if(primaryKey instanceof Vector) {
            pk = (Vector)primaryKey;
        } else {
            pk = ((CMP3Policy) descriptor.getCMPPolicy()).createPkVectorFromKey(primaryKey, (oracle.toplink.essentials.internal.sessions.AbstractSession)session);
        }
	    ReadObjectQuery query = new ReadObjectQuery(descriptor.getJavaClass());
	    query.setSelectionKey(pk);
        query.conformResultsInUnitOfWork();
	    return session.executeQuery(query);
	}
	
	/**
	* Synchronize the persistence context with the
	* underlying database.
	*/
	public void flush(){
        try {
            verifyOpen();        
        
            try {
                getActivePersistenceContext(checkForTransaction(true)).writeChanges();
            }catch (RuntimeException e) {
                if (TopLinkException.class.isAssignableFrom(e.getClass())){
                    throw new PersistenceException(e);
                }
                throw e;
            }        
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
	}

    protected void detectTransactionWrapper(){
        if (this.serverSession.hasExternalTransactionController()){
            setJTATransactionWrapper();
        } else {
            setEntityTransactionWrapper();
        }
     }

	/**
	* Refresh the state of the instance from the
	* database.
	* @param entity
	*/
	public void refresh(Object entity){
        try {
            verifyOpen();
            UnitOfWork uow = getActivePersistenceContext(checkForTransaction(!isExtended()));
            if(!contains(entity, uow)) {
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("cant_refresh_not_managed_object", new Object[]{entity}));
            }
            ReadObjectQuery query = new ReadObjectQuery();
            query.setSelectionObject(entity);
            query.refreshIdentityMapResult();
            query.cascadeByMapping();
            query.setLockMode(ObjectBuildingQuery.NO_LOCK);
            Object refreshedEntity = null;
            refreshedEntity = uow.executeQuery(query);
            if(refreshedEntity == null) {
                //bug3323, should also invalidate the shared cached object if object not exists in DB.
                uow.getParent().getIdentityMapAccessor().invalidateObject(entity);
                throw new EntityNotFoundException(ExceptionLocalization.buildMessage("entity_no_longer_exists_in_db", new Object[]{entity}));
            }
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
	}
	
	/**
	* Check if the instance belongs to the current persistence
	* context.
	* @param entity
	* @return
	* @throws IllegalArgumentException if given Object is not an entity
	*/
	public boolean contains(Object entity){
        try {
            verifyOpen();               
            if (entity == null){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[] {entity}));
            }
            ClassDescriptor descriptor = (ClassDescriptor)getServerSession().getDescriptors().get(entity.getClass());
            if (descriptor == null  || descriptor.isAggregateDescriptor() || descriptor.isAggregateCollectionDescriptor()){
                throw new IllegalArgumentException(ExceptionLocalization.buildMessage("not_an_entity", new Object[]{entity}));
            }       	
            
            if( (!hasActivePersistenceContext())) {
                return false;
            }
            
            return contains(entity,getActivePersistenceContext(checkForTransaction(false)));
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
	}

    /**
    * Check if the instance belongs to the current persistence
    * context.
    * @param entity
    * @param uow
    * @return
    */
    protected boolean contains(Object entity, UnitOfWork uow){
        
        return ((oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl)uow).isObjectRegistered(entity) && 
                !((oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl)uow).isObjectDeleted(entity);
    }

    /**
	 * This method returns the current session to the requestor.  The current session
	 * will be a the active UnitOfWork within a transaction and will be a 'scrap'
	 * UnitOfWork outside of a transaction.  The caller is conserned about the results
	 * then the getSession() or getUnitOfWork() API should be called.
	 */
    public Session getActiveSession(){
        Object txn = checkForTransaction(false);
        if ( txn == null && ! this.isExtended() ){
            return this.serverSession.acquireNonSynchronizedUnitOfWork();
        }else{
            return getActivePersistenceContext(txn);
        }
        
    }
    
    /**
     * Return the underlying provider object for the EntityManager,
     * if available. The result of this method is implementation
     * specific.
     */
    public Object getDelegate(){
        try {
            verifyOpen();
            return this;
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
    }

    /**
     * The method search for user defined property passed in from EntityManager, if it is not found then
     * search for it from EntityManagerFactory properties.
     * @param name
     * @return
     */
    public Object getProperty(String name) {
        Object propertyValue=null;
        if(name==null){
            return null;
        }
        if(this.properties!=null){
            propertyValue=this.properties.get(name);
        }
        if(propertyValue==null){
            propertyValue=this.factory.getProperty(name);
        }
        return propertyValue;
    }

    /**
     * This method will return the active UnitOfWork 
     */
    public UnitOfWork getUnitOfWork(){
        return getActivePersistenceContext(checkForTransaction(false));
    }
    
    /**
     * This method will return a Session outside of a transaction and null within a transaction.
     */
    public Session getSession(){
        if (checkForTransaction(false) == null){
            return this.serverSession.acquireNonSynchronizedUnitOfWork();
        }
        return null;
    }
    
    /**
     * Return the underlying server session
     */
    public ServerSession getServerSession(){
        return this.serverSession;
    }
    /**
     * This method is used to create a query using SQL.  The class, must be the expected
     * return type.
     */
    protected DatabaseQuery createNativeQueryInternal(String sqlString, Class resultType){
        ReadAllQuery query = new ReadAllQuery(resultType);
        query.setSQLString(sqlString);
        query.setIsUserDefined(true);
        return query;
    }
     
    /**
     * This method is used to create a query using a Toplink Expression and the return type.
     */
    protected DatabaseQuery createQueryInternal(Expression expression, Class resultType){
        ReadAllQuery query = new ReadAllQuery(resultType);
        query.setSelectionCriteria(expression);
        return query;
    }
    

   	/**
	 * <p>Closes this EntityManager.
     * 
	 * <p>After invoking this method, all methods on the instance will throw an
	 * {@link IllegalStateException} except for {@link #isOpen}, which will return
	 * <code>false</code>   .</p>
	 *
	 * <p>This should be called when a method is finished with the EntityManager in a
	 * bean-managed transaction environment or when executed outside a container. Closing
	 * of the EntityManager is handled by the container when using container-managed
	 * transactions.</p>
	 */
	public void close(){
        try {
            verifyOpen();
            isOpen = false;
            factory = null;
            serverSession = null;
            if(extendedPersistenceContext != null) {
                if (checkForTransaction(false) == null){
                    // clear change sets but keep the cache
                    extendedPersistenceContext.clearForClose(false);
                } else {
                    // when commit will be called, all change sets will be cleared, but the cache will be kept
                    extendedPersistenceContext.setShouldClearForCloseInsteadOfResume(true);
                }
                extendedPersistenceContext = null;
            }
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
    }


    /**
     * Indicates if this EntityManager is an extended Persistence Context
     */
    public boolean isExtended(){
        return this.extended;
    }
    
    /**
	 * Indicates whether or not this entity manager is open. Returns <code>true</code> until
	 * a call to {@link #close} is made.
	 */	
	public boolean isOpen(){
        return isOpen && factory.isOpen();
    }
 
    /**
    * Set the lock mode for an entity object contained in the persistence context.
    * @param entity
    * @param lockMode
    * @throws PersistenceException if an unsupported lock call is made
    * @throws IllegalArgumentException if the instance is not an entity or is a detached entity
    * @throws TransactionRequiredException if there is no transaction
    */
    public void lock(Object entity, LockModeType lockMode){
        try {
            verifyOpen();
            RepeatableWriteUnitOfWork context = getActivePersistenceContext(checkForTransaction(!isExtended()));
            ClassDescriptor descriptor = context.getDescriptor(entity);
            OptimisticLockingPolicy lockingPolicy = descriptor.getOptimisticLockingPolicy();
            if ((lockingPolicy == null) || !(lockingPolicy instanceof VersionLockingPolicy)){
                throw new PersistenceException(ExceptionLocalization.buildMessage("ejb30-wrong-lock_called_without_version_locking-index", null));
            }
            context.forceUpdateToVersionField(entity, (lockMode == LockModeType.WRITE));
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
    }

    public void verifyOpen(){
        if (!isOpen()){
            throw new IllegalStateException(ExceptionLocalization.buildMessage("operation_on_closed_entity_manager"));
        }
    }
    
    public RepeatableWriteUnitOfWork getActivePersistenceContext(Object txn) {
        if (this.isExtended()){
            // use local uow as it will be local to this EM and not on the txn
            if (this.extendedPersistenceContext == null || !this.extendedPersistenceContext.isActive()){
                this.extendedPersistenceContext = new RepeatableWriteUnitOfWork(this.serverSession.acquireClientSession());
                this.extendedPersistenceContext.setResumeUnitOfWorkOnTransactionCompletion(true);
                this.extendedPersistenceContext.setShouldCascadeCloneToJoinedRelationship(true);
                this.extendedPersistenceContext.setProperties(properties);
                if (txn != null) {
                    // if there is an active txn we must register with it on creation of PC
                    transaction.registerUnitOfWorkWithTxn(this.extendedPersistenceContext);
                }
            }
            //gf3334, force persistencecontext begin early transaction if conditions meet.
            if (this.beginEarlyTransaction && txn != null && !this.extendedPersistenceContext.isInTransaction() ) {
                this.extendedPersistenceContext.beginEarlyTransaction();
            }
            return this.extendedPersistenceContext;
        }else{
            return getTransactionalUnitOfWork_new(txn);
        }
    }
    
    /* 
     * This method is used in contains to check if we already have a persistence context.
     * If there is no active persistence context the method returns false 
     */
    private boolean hasActivePersistenceContext() 
    {
        if (isExtended() && (this.extendedPersistenceContext == null || !this.extendedPersistenceContext.isActive())) 
            return false;
        else 
            return true;
    }
    
    protected RepeatableWriteUnitOfWork getTransactionalUnitOfWork_new(Object tnx) {
        return transaction.getTransactionalUnitOfWork(tnx);
    }
    
    protected Object checkForTransaction(boolean validateExistence) {
        return transaction.checkForTransaction(validateExistence);    
    }
    
    public boolean shouldFlushBeforeQuery(){
        Object foundTransaction = checkForTransaction(false);
        if ((foundTransaction!=null) && transaction.shouldFlushBeforeQuery(getActivePersistenceContext(foundTransaction))){
            return true;
        }
        return false;
    }
    
    //This is used to determine if the Persistence Contexts (in the form of UOWs)
    //should be propagated or not, which effectively means we will use the 
    //active unit of work
    public boolean shouldPropagatePersistenceContext(){
        return this.propagatePersistenceContext;
    }

    //Indicate the early transaction should be forced to start.
    public boolean shouldBeginEarlyTransaction(){
        return this.beginEarlyTransaction;
    }

    
    /**
    * Indicate to the EntityManager that a JTA transaction is
    * active. This method should be called on a JTA application
    * managed EntityManager that was created outside the scope
    * of the active transaction to associate it with the current
    * JTA transaction.
    * @throws TransactionRequiredException if there is
    * no transaction.
    */
    public void joinTransaction(){
        try {
            verifyOpen();               
            transaction.registerUnitOfWorkWithTxn(getActivePersistenceContext(checkForTransaction(true)));
        } catch (RuntimeException e) {
            this.setRollbackOnly();
            throw e;
        }
    }

    /**
    * Internal method. Sets transaction to rollback only.
    */
    protected void setRollbackOnly() {
        this.transaction.setRollbackOnlyInternal();        
    }

    /**
     * Internal method, set begin early transaction if property has been specified.
     */
    private void setBeginEarlyTransaction(){
        String beginEarlyTransactionProperty = (String)getProperty(TopLinkProperties.JOIN_EXISTING_TRANSACTION);
        if(beginEarlyTransactionProperty!=null){
            this.beginEarlyTransaction="true".equalsIgnoreCase(beginEarlyTransactionProperty);
        }
    }
    
}
