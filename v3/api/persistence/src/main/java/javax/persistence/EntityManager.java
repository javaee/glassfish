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
package javax.persistence;

/**
 * Interface used to interact with the persistence context.
 *
 * <p> An <code>EntityManager</code> instance is associated with 
 * a persistence context. A persistence context is a set of entity 
 * instances in which for any persistent entity identity there is 
 * a unique entity instance. Within the persistence context, the 
 * entity instances and their lifecycle are managed. This interface 
 * defines the methods that are used to interact with the 
 * persistence context. The <code>EntityManager</code> API is used 
 * to create and remove persistent entity instances, to find entities 
 * by their primary key, and to query over entities.
 *
 * <p> The set of entities that can be managed by a given 
 * <code>EntityManager</code> instance is defined by a persistence 
 * unit. A persistence unit defines the set of all classes that are 
 * related or grouped by the application, and which must be 
 * colocated in their mapping to a single database.
 *
 * @since Java Persistence 1.0
 */
public interface EntityManager {

    /**
     * Make an entity instance managed and persistent.
     * @param entity
     * @throws EntityExistsException if the entity already exists.
     * (The EntityExistsException may be thrown when the persist
     * operation is invoked, or the EntityExistsException or
     * another PersistenceException may be thrown at flush or commit
     * time.)
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if not an entity
     * @throws TransactionRequiredException if invoked on a
     * container-managed entity manager of type
     * PersistenceContextType.TRANSACTION and there is
     * no transaction.
     */
    public void persist(Object entity);
    
    /**
     * Merge the state of the given entity into the
     * current persistence context.
     * @param entity
     * @return the instance that the state was merged to
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if instance is not an
     * entity or is a removed entity
     * @throws TransactionRequiredException if invoked on a
     * container-managed entity manager of type
     * PersistenceContextType.TRANSACTION and there is
     * no transaction.
     */
    public <T> T merge(T entity);
    
    /**
     * Remove the entity instance.
     * @param entity
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if not an entity
     * or if a detached entity
     * @throws TransactionRequiredException if invoked on a
     * container-managed entity manager of type
     * PersistenceContextType.TRANSACTION and there is
     * no transaction.
     */
    public void remove(Object entity);
    
    /**
     * Find by primary key.
     * @param entityClass
     * @param primaryKey
     * @return the found entity instance or null
     *    if the entity does not exist
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if the first argument does
     *    not denote an entity type or the second
     *    argument is not a valid type for that
     *    entity's primary key
     */
    public <T> T find(Class<T> entityClass, Object primaryKey);

    /**
     * Get an instance, whose state may be lazily fetched.
     * If the requested instance does not exist in the database,
     * throws {@link EntityNotFoundException} when the instance state is
     * first accessed. (The persistence provider runtime is permitted to throw
     * {@link EntityNotFoundException} when {@link #getReference} is called.)
     *
     * The application should not expect that the instance state will
     * be available upon detachment, unless it was accessed by the
     * application while the entity manager was open.
     * @param entityClass
     * @param primaryKey
     * @return the found entity instance
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if the first argument does
     *    not denote an entity type or the second
     *    argument is not a valid type for that
     *    entity's primary key
     * @throws EntityNotFoundException if the entity state
     *    cannot be accessed
     */
    public <T> T getReference(Class<T> entityClass, Object primaryKey);
    
    /**
     * Synchronize the persistence context to the
     * underlying database.
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws TransactionRequiredException if there is
     *    no transaction
     * @throws PersistenceException if the flush fails
     */
    public void flush();
  
    /**
    * Set the flush mode that applies to all objects contained
    * in the persistence context.
    * @param flushMode
     * @throws IllegalStateException if this EntityManager has been closed.
    */
    public void setFlushMode(FlushModeType flushMode);    

    /**
    * Get the flush mode that applies to all objects contained
    * in the persistence context.
    * @return flush mode
     * @throws IllegalStateException if this EntityManager has been closed.
    */
    public FlushModeType getFlushMode();

    /**
    * Set the lock mode for an entity object contained
    * in the persistence context.
    * @param entity
    * @param lockMode
     * @throws IllegalStateException if this EntityManager has been closed.
    * @throws PersistenceException if an unsupported lock call
    * is made
    * @throws IllegalArgumentException if the instance is not
    * an entity or is a detached entity
    * @throws TransactionRequiredException if there is no
    * transaction
    */
    public void lock(Object entity, LockModeType lockMode);

    /** 
     * Refresh the state of the instance from the database,
     * overwriting changes made to the entity, if any.
     * @param entity
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if not an entity
     * or entity is not managed
     * @throws TransactionRequiredException if invoked on a
     * container-managed entity manager of type
     * PersistenceContextType.TRANSACTION and there is
     * no transaction.
     * @throws EntityNotFoundException if the entity no longer
     * exists in the database.
     */
    public void refresh(Object entity);
    
    /**
    * Clear the persistence context, causing all managed
    * entities to become detached. Changes made to entities that
    * have not been flushed to the database will not be
    * persisted.
     * @throws IllegalStateException if this EntityManager has been closed.
    */
    public void clear();

    /**
     * Check if the instance belongs to the current persistence
     * context.
     * @param entity
     * @return <code>true</code> if the instance belongs to 
     * the current persistence context.
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if not an entity
     */
    public boolean contains(Object entity);
    
    /**
     * Create an instance of Query for executing a
     * Java Persistence query language statement.
     * @param qlString a Java Persistence query language query string
     * @return the new query instance
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if query string is not valid
     */
    public Query createQuery(String qlString);
    
    /**
     * Create an instance of Query for executing a
     * named query (in the Java Persistence query language or in native SQL).
     * @param name the name of a query defined in metadata
     * @return the new query instance
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws IllegalArgumentException if a query has not been
     * defined with the given name
     */
    public Query createNamedQuery(String name);
    
    /**
     * Create an instance of Query for executing
     * a native SQL statement, e.g., for update or delete.
     * @param sqlString a native SQL query string
     * @return the new query instance
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    public Query createNativeQuery(String sqlString);
    
    /**
     * Create an instance of Query for executing
     * a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultClass the class of the resulting instance(s)
     * @return the new query instance
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    public Query createNativeQuery(String sqlString, Class resultClass);
    
    /**
     * Create an instance of Query for executing
     * a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultSetMapping the name of the result set mapping
     * @return the new query instance
     * @throws IllegalStateException if this EntityManager has been closed.
     */
    public Query createNativeQuery(String sqlString, String resultSetMapping);
    
    /**
     * Indicate to the EntityManager that a JTA transaction is
     * active. This method should be called on a JTA application
     * managed EntityManager that was created outside the scope
     * of the active transaction to associate it with the current
     * JTA transaction.
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws TransactionRequiredException if there is
     * no transaction.
     */
    public void joinTransaction();

    /**
    * Return the underlying provider object for the EntityManager,
    * if available. The result of this method is implementation
    * specific.
     * @throws IllegalStateException if this EntityManager has been closed.
    */
    public Object getDelegate();

    /**
     * Close an application-managed EntityManager.
     * After the close method has been invoked, all methods
     * on the EntityManager instance and any Query objects obtained
     * from it will throw the IllegalStateException except
     * for getTransaction and isOpen (which will return false).
     * If this method is called when the EntityManager is
     * associated with an active transaction, the persistence
     * context remains managed until the transaction completes.
     * @throws IllegalStateException if the EntityManager
     * is container-managed or has been already closed..
     */
    public void close();
    
    /**
     * Determine whether the EntityManager is open.
     * @return true until the EntityManager has been closed.
     */
    public boolean isOpen();
    
    /**
     * Returns the resource-level transaction object.
     * The EntityTransaction instance may be used serially to
     * begin and commit multiple transactions.
     * @return EntityTransaction instance
     * @throws IllegalStateException if invoked on a JTA
     *    EntityManager.
     */
    public EntityTransaction getTransaction();
    
}
