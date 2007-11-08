/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

package com.sun.persistence.runtime.em.impl;

import com.sun.org.apache.jdo.pm.PersistenceManagerInternal;
import com.sun.org.apache.jdo.util.I18NHelper;
import com.sun.persistence.runtime.em.EntityManagerFactoryInternal;
import com.sun.persistence.runtime.em.EntityManagerInternal;
import com.sun.persistence.runtime.query.impl.QueryFactory;
import com.sun.persistence.support.JDOObjectNotFoundException;
import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.support.spi.PersistenceCapable;

import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;


/**
 * The <code>EntityManager</code> implementation.
 * 
 * @see javax.persistence.EntityManager
 * @author Martin Zaun
 */
public class EntityManagerImpl implements EntityManagerInternal {

    /**
     * I18N message handler
     */
    static protected final I18NHelper msg
        = I18NHelper.getInstance("com.sun.persistence.runtime.Bundle"); //NOI18

    /** Means by which query instances are created. */
    private final QueryFactory queryFactory = QueryFactory.getInstance();

    /** The EntityManagerFactory who created this EntityManager. */
    protected final EntityManagerFactoryInternal emf;

    /** The PersistenceManager delegate. */
    protected final PersistenceManagerInternal pm;

    /** The entity transaction if this is a resource-local entity manager. */
    protected final EntityTransaction etx;

    /**
     * Creates a new <code>EntityManagerImpl</code>.
     * This constructor is for use by an <code>EntityManagerFactory</code>
     * implementation, not by application code.
     * @param em the entity manager factory creating this instance
     * @param pm the persistence manager delegate
     */
    protected EntityManagerImpl(EntityManagerFactoryInternal emf,
                                PersistenceManagerInternal pm) {
        assert (emf != null);
        assert (pm != null);
        this.emf = emf;
        this.pm = pm;
        this.etx = (emf.isJtaAware() ? null
                    : new EntityTransactionImpl(this, pm.currentTransaction()));
    }

    /**
     * Returns a hash code value for the object.
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return pm.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * @param   obj   the reference object with which to compare.
     * @return  <code>true</code> if this object is the same as the obj
     *          argument; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof EntityManagerImpl) {
            return pm.equals(((EntityManagerImpl)obj).pm);
        }
        return false;
    }

    // ----------------------------------------------------------------------
    // EM Lifecycle Methods
    // ----------------------------------------------------------------------

    /**
     * Indicates whether the EntityManager is open.
     * @see javax.persistence.EntityManager#isOpen
     */
    public boolean isOpen() {
        return !pm.isClosed();
    }

    /**
     * Closes an application-managed EntityManager.
     * @see javax.persistence.EntityManager#close
     */
    public void close() {
        // XXX : remove comments when finally implemented
        // This method can only be called when the EntityManager
        // is not associated with an active transaction.
        // @throws IllegalStateException if the EntityManager is
        // associated with an active transaction or if the
        // EntityManager is container-managed.
        ensureIsOpen();
        pm.close();
    }

    /**
     * Returns the resource-level transaction object.
     * @see javax.persistence.EntityManager#getTransaction
     */
    public EntityTransaction getTransaction() {
        ensureIsOpen();
        ensureIsResourceLocalEM();        
        return etx;
    }

    /**
     * Returns the internally used <code>PersistenceManager</code>
     * associated with this entity manager.
     * @see EntityManagerInternal#getPersistenceManagerInternal
     */
    public PersistenceManagerInternal getPersistenceManager() {
        ensureIsOpen();
        return pm;
    }

    // ----------------------------------------------------------------------
    // Entity Operations
    // ----------------------------------------------------------------------

    /**
     * Make an instance managed and persistent, using the unqualified
     * class name as the entity name.
     * @see javax.persistence.EntityManager#persist
     */
    public void persist(Object entity) {
        // XXX : remove comments when finally implemented
        // [JSR 220, Version 3.0, Early Draft 2]
        // The semantics of the persist operation, applied to an entity X
        // are as follows:
        // - If X is a new entity, it becomes managed. The entity X will be
        //   entered into the database at or before transaction commit or as
        //   a result of the flush operation.
        // - If X is a preexisting managed entity, it is ignored by the
        //   persist operation. However, the persist operation is cascaded
        //   to entities referenced by X, if the relationships from X to
        //   these other entities is annotated with the cascade=PERSIST or
        //   cascade=ALL annotation member value.
        // - If X is a removed entity, it becomes managed.
        // - If X is a detached object, an IllegalArgumentException will be
        //   thrown by the container (or the transaction commit will fail).
        // - For all entities Y referenced by a relationship from X, if the
        //   relationship to Y has been annotated with the cascade member
        //   value cascade=PERSIST or cascade=ALL, the persist operation is
        //   applied to Y.
        ensureIsOpen();
        ensureIsEntity(entity);
        ensureIsNotDetached(entity);
        ensureHasTransactionContext();

        // XXX FIXME :pm. makePersistent() does extend to the closure of this
        // argument but by slightly different rules then required by EJB spec
        pm.makePersistent(entity);
    }

    /**
     * Remove the instance.
     * @see javax.persistence.EntityManager#remove
     */
    public void remove(Object entity) {
        // XXX : remove comments when finally implemented
        // [JSR 220, Version 3.0, Early Draft 2]
        // The semantics of the remove operation, applied to an entity X
        // are as follows:
        // - If X is a new entity, it is ignored by the remove operation.
        //   However, the remove operation is cascaded to entities
        //   referenced by X, if the relationships from X to these other
        //   entities is annotated with the cascade=REMOVE or cascade=ALL
        //   annotation member value.
        // - If X is a managed entity, the remove operation causes it to
        //   transition to the removed state. The remove operation is
        //   cascaded to entities referenced by X, if the relationships
        //   from X to these other entities is annotated with the
        //   cascade=REMOVE or cascade=ALL annotation member value.
        // - If X is a detached entity, an IllegalArgumentException will
        //   be thrown by the container (or the transaction commit will
        //   fail).
        // - If X is a removed entity, an IllegalArgumentException will be
        //   thrown by the container (or the transaction commit will fail).
        //   [Note to reviewers] Alternatively: should remove on a removed
        //   entity be ignored or undefined?
        // - A removed entity X will be removed from the database at or
        //   before transaction commit or as a result of the flush
        //   operation. Accessing an entity in the removed state is
        //   undefined.
        //   [Note to readers] We are considering whether to add an option
        //   to cover the removal of orphaned entities involved in
        //   parent-child relationships.
        ensureIsOpen();
        ensureIsEntity(entity);
        ensureIsNotDetached(entity);
        ensureIsNotRemoved(entity);
        ensureHasTransactionContext();

        // XXX FIXME : pm.deletePersistent() does not extend to the closure
        pm.deletePersistent(entity);
    }

    /**
     * Merge the state of the given entity into the
     * current persistence context.
     * @see javax.persistence.EntityManager#merge
     */
    public <T> T merge(T entity) {
        throw new UnsupportedOperationException("not implemented yet");
        // XXX : remove comments when finally implemented
        // returns the instance that the state was merged to
        // throws IllegalArgumentException if not an entity
        // or entity is in the removed state
        // throws TransactionRequiredException if there is
        // no transaction
        //
        // [JSR 220, Version 3.0, Early Draft 2]
        // The semantics of the merge operation applied to an entity X are
        // as follows:
        // - If X is a detached entity, it is copied onto a pre-existing
        //   managed entity instance X' of the same identity or a new
        //   managed copy of X is created.
        // - If X is a new entity instance, a new managed entity instance X'
        //   is created and the state of X is copied into the new managed
        //   entity instance X'.
        // - If X is a removed entity instance, an IllegalArgumentException
        //   will be thrown by the container (or the transaction commit will
        //   fail).
        // - If X is a managed entity, it is ignored by the merge operation,
        //   however, the merge operation is cascaded to entities referenced
        //   by relationships from X if these relationships have been
        //   annotated with the cascade member value cascade=MERGE or
        //   cascade=ALL annotation.
        // - For all entities Y referenced by relationships from X having
        //   the cascade member value cascade=MERGE or cascade=ALL, Y is
        //   merged recursively as Y'. For all such Y referenced by X, X' is
        //   set to reference Y'. (Note that if X is managed then X is the
        //   same object as X'.)
        // - If X is an entity merged to X', with a reference to another
        //   entity Y, where cascade=MERGE or cascade=ALL is not specified,
        //   then navigation of the same association from X' yields a
        //   reference to a managed object Y' with the same persistent
        //   identity as Y.
        // - Fields or properties of type java.sql.Blob and java.sql.Clob
        //   are ignored by the merge operation.

        //ensureIsOpen();
    }

    /**
     * Refresh the state of the instance from the database.
     * @see javax.persistence.EntityManager#refresh
     */
    public void refresh(Object entity) {
        ensureIsOpen();
        ensureIsEntity(entity);
        ensureIsNotDetached(entity);
        ensureHasTransactionContext();
        pm.refresh(entity);
    }

    /**
     * Check if the instance belongs to the current persistence context.
     * @see javax.persistence.EntityManager#contains
     */
    public boolean contains(Object entity) {
        // XXX : remove comments when finally implemented
        // [JSR 220, Version 3.0, Early Draft 2]
        // The contains method returns true:
        // - If the entity has been retrieved from the database, and has
        //   not been removed or detached.
        // - the entity instance is new, and the persist method has been
        //   called on the entity or the persist operation has been cascaded
        //   to it.
        // The contains method returns false:
        // - If the instance is detached.
        // - If the remove method has been called on the entity, or the
        //   remove operation has been cascaded to it.
        // - If the instance is new, and the persist method has not been
        //   called on the entity or the persist operation has not been
        //   cascaded to it.
        ensureIsOpen();
        ensureIsEntity(entity);
        ensureHasTransactionContext();
        return (((PersistenceCapable)entity).jdoIsPersistent());
    }

    /**
     * Synchronize the persistence context with the underlying database.
     * @see javax.persistence.EntityManager#flush
     */
    public void flush() {
        // XXX : remove comments when finally implemented
        // [JSR 220, Version 3.0, Early Draft 2]
        // The semantics of the flush operation, applied to an entity X
        // are as follows:
        // - If X is a managed entity, it is synchronized to the database.
        // - For all entities Y referenced by a relationship from X, if the
        //   relationship to Y has been annotated with the cascade member
        //   value cascade=PERSIST or cascade=ALL, the persist operation is
        //   applied to Y.
        // - For any new entity Y referenced by a relationship from X, where
        //   the relationship to Y has not been annotated with the cascade
        //   member value cascade=PERSIST or cascade=ALL, an exception will
        //   be thrown by the container or the transaction commit will fail.
        // - For any detached entity Y referenced by a relationship from X,
        //   where the relationship to Y has not been annotated with the
        //   cascade member value cascade=PERSIST or cascade=ALL, the
        //   semantics depend upon the ownership of the relationship. If X
        //   owns the relationship, any changes to the relationship are
        //   synchronized with the database; otherwise, if Y owns the
        //   relationships, the behavior is undefined.
        // - If X is a removed entity, it is removed from the database.
        ensureIsOpen();
        ensureHasTransactionContext();

        // XXX FIXME : flush() does extend to the closure of this
        // argument but by slightly different rules then required by EJB spec
        pm.flush();
    }

    // ----------------------------------------------------------------------
    // Finder and Query Methods
    // ----------------------------------------------------------------------

    /**
     * Find by primary key.
     * @see javax.persistence.EntityManager#find
     */
    public Object find(String entityName, Object primaryKey) {
        ensureIsOpen();

        // XXX TBD Get the Class for entityName
        // return find(clazz, primaryKey);
        return null;
    }

    /**
     * Find by primary key.
     * @see javax.persistence.EntityManager#find
     */
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        ensureIsOpen();

        T rc = null;
        try {
            rc = (T) pm.getObjectById(entityClass, primaryKey, true);
        } catch (JDOObjectNotFoundException ex) {
            throw new EntityNotFoundException(ex.getMessage(), ex);
        } catch (JDOUserException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }

        assert rc != null
            : "EntityManager.find: Must throw EntityNotFoundException"; //NOI18N
        return rc;
    }

    /**
     * Create an instance of Query for executing an
     * EJBQL query.
     * @see javax.persistence.EntityManager#createQuery
     */
    public Query createQuery(String ejbqlString) {
        ensureIsOpen();
        return queryFactory.createQuery(ejbqlString, pm);
    }

    /**
     * Create an instance of Query for executing a
     * named query (in EJBQL or native SQL).
     * @see javax.persistence.EntityManager#createNamedQuery
     */
    public Query createNamedQuery(String name) {
        ensureIsOpen();

        // XXX : remove comments when finally implemented
        // returns the new query instance
        // throws IllegalArgumentException if query string is not valid
        String ejbqlString = null;
        // XXX TBD get query for name
        //ejbqlString = persistenceUnit.getNamedQuery(name);
        return queryFactory.createQuery(ejbqlString, pm);
    }

    /**
     * Create an instance of Query for executing
     * a native SQL query.
     * @see javax.persistence.EntityManager#createNativeQuery
     */
    public Query createNativeQuery(String sqlString) {
        ensureIsOpen();
        return queryFactory.createNativeQuery(sqlString, pm);
    }

    /**
     * Create an instance of Query for executing
     * a native SQL query.
     * @see javax.persistence.EntityManager#createNativeQuery
     */
    public Query createNativeQuery(String sqlString, Class resultClass) {
        ensureIsOpen();
        return queryFactory.createNativeQuery(sqlString, resultClass, pm);
    }

    /**
     * Create an instance of Query for executing
     * a native SQL query.
     * @see javax.persistence.EntityManager#createNativeQuery
     */
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        ensureIsOpen();
        return queryFactory.createNativeQuery(sqlString, resultSetMapping, pm);
    }

    // ----------------------------------------------------------------------
    // Helper Methods
    // ----------------------------------------------------------------------

    /**
     * Checks that an object is an entity.
     * @param entity the object to be checked
     * @throws IllegalArgumentException if the object is not an entity
     */
    protected final void ensureIsEntity(Object entity) {
        if (!(entity instanceof PersistenceCapable)) {
            throw new IllegalArgumentException(
                msg.msg("runtime.entitymanagerimpl.notanentity"));//NOI18N
        }
    }

    /**
     * Checks that an object is not a detached entity.
     * @param entity the object to be checked
     * @throws IllegalArgumentException if the object is a detached entity
     */
    protected final void ensureIsNotDetached(Object entity) {
        // XXX FIXME : not implemented yet
        // if (!((PersistenceCapable)entity).jdoIsTransactional()) {
        //     throw new IllegalArgumentException(
        //         msg.msg("runtime.entitymanagerimpl.entityisdetached"));//NOI18N
        // }
    }

    /**
     * Checks that an object is a removed entity.
     * @param entity the object to be checked
     * @throws IllegalArgumentException if the object is a removed entity
     */
    protected final void ensureIsNotRemoved(Object entity) {
        if (((PersistenceCapable)entity).jdoIsDeleted()) {
            throw new IllegalArgumentException(
                msg.msg("runtime.entitymanagerimpl.entityisremoved"));//NOI18N
        }
    }

    /**
     * Checks that there's an active transaction context.
     * @throws TransactionRequiredException if there's no transaction context
     */
    protected final void ensureHasTransactionContext() {
        if (pm.currentTransaction() == null) {
            throw new TransactionRequiredException(
                msg.msg("runtime.entitymanagerimpl.notransaction"));//NOI18N
        }
    }

    /**
     * Checks that this <code>EntityManager</code> is not closed.
     * @throws IllegalStateException if this <code>EntityManager</code> is closed
     */
    protected final void ensureIsOpen() {
        if (!isOpen()) {
            throw new IllegalStateException(
                msg.msg("runtime.entitymanagerimpl.isclosed"));//NOI18N
        }
    }

    /**
     * Checks that this is a Resource-Local <code>EntityManager</code>.
     * @throws IllegalStateException if this is a JTA <code>EntityManager</code>
     */
    protected final void ensureIsResourceLocalEM() {
        if (emf.isJtaAware()) {
            throw new IllegalStateException(
                msg.msg("runtime.entitymanagerimpl.noresourcelocalem"));//NOI18N
        }
    }
}
