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
package oracle.toplink.essentials.internal.queryframework;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.internal.descriptors.OptimisticLockingPolicy;
import oracle.toplink.essentials.descriptors.VersionLockingPolicy;
import oracle.toplink.essentials.descriptors.DescriptorEvent;
import oracle.toplink.essentials.descriptors.DescriptorEventManager;
import oracle.toplink.essentials.descriptors.DescriptorQueryManager;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.internal.identitymaps.CacheKey;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>:
 * Abstract class for all database query mechanism objects.
 * DatabaseQueryMechanism is actually a helper class and currently is required
 * for all types of queries.  Most of the work performed by the query framework is
 * performed in the query mechanism.  The query mechanism contains the internal
 * knowledge necessary to perform the specific database operation.
 * <p>
 * <p><b>Responsibilities</b>:
 * Provide a common protocol for query mechanism objects.
 * Provides all of the database specific work for the assigned query.
 *
 * @author Yvon Lavoie
 * @since TOPLink/Java 1.0
 */
public abstract class DatabaseQueryMechanism implements Cloneable, Serializable {

    /** The database query that uses this mechanism. */
    protected DatabaseQuery query;

    /**
     * Initialize the state of the query.
     */
    public DatabaseQueryMechanism() {
    }

    /**
     * Initialize the state of the query
     * @param query - owner of mechanism
     */
    public DatabaseQueryMechanism(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * Remove the query object from the identity map.
     */
    protected void addObjectDeletedDuringCommit() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();

        // CR3510313, avoid removing aggregate collections from cache (maintain cache is false).
        if (writeQuery.shouldMaintainCache()) {
            if (getSession().isUnitOfWork()) {
                ((UnitOfWorkImpl)getSession()).addObjectDeletedDuringCommit(object, getDescriptor());
            } else {
                getSession().getIdentityMapAccessorInstance().removeFromIdentityMap(writeQuery.getPrimaryKey(), getDescriptor().getJavaClass(), getDescriptor());
            }
        }
    }

    /**
     * Add the initial write lock value to the row for insert.
     */
    protected void addWriteLockFieldForInsert() {
        if (getDescriptor().usesOptimisticLocking()) {
            getDescriptor().getOptimisticLockingPolicy().setupWriteFieldsForInsert(getWriteObjectQuery());
        }
    }

    /**
     * Internal:
     * In the case of EJBQL, an expression needs to be generated. Build the required expression.
     */
    public void buildSelectionCriteria(AbstractSession session) {
        // Default is do nothing
    }

    /**
     * Perform a cache lookup for the query.
     * If the translation row contains all the primary key fields,
     * then a cache check will be performed.
     * If the object is found in the cache, return it;
     * otherwise return null.
     */
    public Object checkCacheForObject(AbstractRecord translationRow, AbstractSession session) {
        // Null check added for CR#4295 - TW
        if ((translationRow == null) || (translationRow.isEmpty())) {
            return null;
        }

        List keyFields = getDescriptor().getPrimaryKeyFields();
        Vector primaryKey = new Vector(keyFields.size());

        for (int index = 0; index < keyFields.size(); index++) {
            Object value = translationRow.get((DatabaseField)keyFields.get(index));
            if (value == null) {
                return null;
            } else {
                primaryKey.add(value);
            }
        }
        return session.getIdentityMapAccessorInstance().getFromIdentityMapWithDeferredLock(primaryKey, getReadObjectQuery().getReferenceClass(), false, getDescriptor());
    }

    /**
     * Clone the mechanism
     */
    protected Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }

    /**
     * Clone the mechanism for the specified query clone.
     */
    public DatabaseQueryMechanism clone(DatabaseQuery queryClone) {
        DatabaseQueryMechanism clone = (DatabaseQueryMechanism)clone();
        clone.setQuery(queryClone);
        return clone;
    }

    /**
     * Read all rows from the database using a cursored stream.
     * @exception  DatabaseException - an error has occurred on the database
     */
    public abstract DatabaseCall cursorSelectAllRows() throws DatabaseException;

    /**
     * Delete a collection of objects
     * This should be overriden by subclasses.
     * @exception  DatabaseException - an error has occurred on the database
     */
    public boolean isEJBQLCallQueryMechanism() {
        return false;
    }

    /**
     * Build the objects for the rows, and answer them
     * @exception  DatabaseException - an error has occurred on the database
     */
    public Object buildObjectsFromRows(Vector rows) throws DatabaseException {
        Object result = ((ReadAllQuery)getQuery()).getContainerPolicy().containerInstance(rows.size());
        return getDescriptor().getObjectBuilder().buildObjectsInto((ReadAllQuery)getQuery(), rows, result);
    }
    ;
    public abstract Integer deleteAll() throws DatabaseException;

    /**
     * Delete an object
     * This should be overriden by subclasses.
     * @exception DatabaseException
     * @return the row count.
     */
    public abstract Integer deleteObject() throws DatabaseException;

    /**
     * Delete an object from the database.
     */
    public void deleteObjectForWrite() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();

        // check for user-defined query
        if ((!writeQuery.isUserDefined())// this is not a user-defined query
                 &&queryManager.hasDeleteQuery()// there is a user-defined query
                 &&isExpressionQueryMechanism()) {// this is not a hand-coded call (custom SQL etc.)
            performUserDefinedDelete();
            return;
        }

        CommitManager commitManager = getSession().getCommitManager();

        // This must be done after the custom query check, otherwise it will be done twice.
        commitManager.markPreModifyCommitInProgress(object);

        if (writeQuery.getObjectChangeSet() == null) {
            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                // only throw the events if there is no changeset otherwise the event will be thrown twice
                // once by the calculate changes code and here
                getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreDeleteEvent, writeQuery));
            }
        }

        // check whether deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.preDelete(writeQuery);
        }

        // In a unit of work/writeObjects the preDelete may cause a shallow update of this object,
        // in this case the following second write must do the real delete.
        if (!commitManager.isShallowCommitted(object) && !writeQuery.shouldCascadeParts()) {
            updateForeignKeyFieldBeforeDelete();
        } else {
            // CR#2660080 missing aboutToDelete event.		
            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                DescriptorEvent event = new DescriptorEvent(DescriptorEventManager.AboutToDeleteEvent, writeQuery);
                event.setRecord(getModifyRow());
                getDescriptor().getEventManager().executeEvent(event);
            }

            int rowCount = deleteObject().intValue();
            
            if (rowCount < 1) {
                getSession().getEventManager().noRowsModified(writeQuery, object);
            }

            if (getDescriptor().usesOptimisticLocking()) {
                getDescriptor().getOptimisticLockingPolicy().validateDelete(rowCount, object, writeQuery);
            }
                
            // remember that the object was deleted
            addObjectDeletedDuringCommit();
        }

        commitManager.markPostModifyCommitInProgress(object);
        
        // Verify if deep shallow modify is turned on.
        if (writeQuery.shouldCascadeParts()) {
            queryManager.postDelete(writeQuery);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostDeleteEvent, writeQuery));
        }
    }

    /**
     * Execute a non selecting SQL call
     * This should be overriden by subclasses.
     * @exception DatabaseException
     * @return the row count.
     */
    public abstract Integer executeNoSelect() throws DatabaseException;

    /**
     * Execute a select SQL call and return the rows.
     * This should be overriden by subclasses.
     * @exception DatabaseException
     */
    public abstract Vector executeSelect() throws DatabaseException;

    /**
     * Check whether the object already exists on the database; then
     * perform an insert, update or delete, as appropriate.
     * This method was moved here, from WriteObjectQuery.execute(),
     * so we can hide the source.
     * Return the object being written.
     */
    public Object executeWrite() throws DatabaseException, OptimisticLockException {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();
        CommitManager commitManager = getSession().getCommitManager();

        // if the object has already been committed, no work is required
        if (commitManager.isCommitCompleted(object) || commitManager.isCommitInPostModify(object)) {
            return object;
        }

        // check whether the object is already being committed -
        // if it is, then a shallow write must be executed
        if (commitManager.isCommitInPreModify(object)) {
            writeQuery.executeShallowWrite();
            return object;
        }

        try {
            getSession().beginTransaction();

            if (writeQuery.getObjectChangeSet() == null) {
                // PERF: Avoid events if no listeners.
                if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                    // only throw the events if there is no changeset otherwise the event will be thrown twice
                    // once by the calculate changes code and here
                    getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreWriteEvent, writeQuery));
                }
            }
            writeQuery.executeCommit();

            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostWriteEvent, writeQuery));
            }

            getSession().commitTransaction();

            // notify the commit manager of the completion to the commit
            commitManager.markCommitCompleted(object);

            return object;

        } catch (RuntimeException exception) {
            getSession().rollbackTransaction();
            commitManager.markCommitCompleted(object);
            throw exception;
        }
    }

    /**
     * Check whether the object already exists on the database; then
     * perform an insert or update, as appropriate.
     * This method was moved here, from WriteObjectQuery.execute(),
     * so we can hide the source.
     * Return the object being written.
     */
    public Object executeWriteWithChangeSet() throws DatabaseException, OptimisticLockException {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        ObjectChangeSet objectChangeSet = writeQuery.getObjectChangeSet();
        CommitManager commitManager = getSession().getCommitManager();

        //if there are no changes then there is no work required
        // Check for forcedUpdate Version and Optimistic read lock (hasForcedChanges() set in ObjectChangePolicy)
        if (!objectChangeSet.hasChanges() && !objectChangeSet.hasForcedChanges()) {
            commitManager.markCommitCompleted(objectChangeSet);
            commitManager.markCommitCompleted(writeQuery.getObject());
            return writeQuery.getObject();
        }
	// if the object has already been committed, no work is required
	if (commitManager.isCommitCompleted(objectChangeSet)
			|| commitManager.isCommitInPostModify(objectChangeSet)) {
		return writeQuery.getObject();
	}

	// if the object has already been committed, no work is required
    // need to check for the object to ensure insert wasn't completed already.
	if (commitManager.isCommitCompleted(writeQuery.getObject())
			|| commitManager.isCommitInPostModify(writeQuery.getObject())) {
            return writeQuery.getObject();
        }
        try {
            getSession().beginTransaction();

            writeQuery.executeCommitWithChangeSet();

            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostWriteEvent, writeQuery));
            }

            getSession().commitTransaction();

            // notify the commit manager of the completion to the commit
            commitManager.markCommitCompleted(objectChangeSet);
            commitManager.markCommitCompleted(writeQuery.getObject());

            return writeQuery.getObject();

        } catch (RuntimeException exception) {
            getSession().rollbackTransaction();
            commitManager.markCommitCompleted(objectChangeSet);
            commitManager.markCommitCompleted(writeQuery.getObject());
            throw exception;
        }
    }

    /**
     * Convenience method
     */
    protected ClassDescriptor getDescriptor() {
        return getQuery().getDescriptor();
    }

    /**
     * Convenience method
     */
    public AbstractRecord getModifyRow() {
        if (getQuery().isModifyQuery()) {
            return ((ModifyQuery)getQuery()).getModifyRow();
        } else {
            return null;
        }
    }

    /**
     * Return the query that uses the mechanism.
     */
    public DatabaseQuery getQuery() {
        return query;
    }

    /**
     * Convenience method
     */
    protected ReadObjectQuery getReadObjectQuery() {
        return (ReadObjectQuery)getQuery();
    }

    /**
     * Return the selection criteria for the mechanism.
     * By default this is null. This method exists because both statement and expression
     * mechanisms use an expression and some code in the mappings depends on returning this.
     */
    public Expression getSelectionCriteria() {
        return null;
    }

    /**
     * Convenience method
     */
    protected AbstractSession getSession() {
        return getQuery().getSession();
    }

    /**
     * Convenience method
     */
    protected AbstractRecord getTranslationRow() {
        return getQuery().getTranslationRow();
    }

    /**
     * Convenience method
     */
    protected WriteObjectQuery getWriteObjectQuery() {
        return (WriteObjectQuery)getQuery();
    }

    /**
     * Insert an object.
     */
    public abstract void insertObject() throws DatabaseException;

    /**
     *  Insert an object and provide the opportunity to reprepare prior to the insert.
     *  This will be overridden
     *  CR#3237
     */
    public void insertObject(boolean reprepare) {
        insertObject();
    }

    /**
     * Insert an object in the database.
     */
    public void insertObjectForWrite() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();

        // check for user-defined query
        if ((!writeQuery.isUserDefined())// this is not a user-defined query
                 &&queryManager.hasInsertQuery()// there is a user-defined query
                 &&isExpressionQueryMechanism()) {// this is not a hand-coded call (custom SQL etc.)
            performUserDefinedInsert();
            return;
        }

        CommitManager commitManager = getSession().getCommitManager();

        // This must be done after the custom query check, otherwise it will be done twice.
        commitManager.markPreModifyCommitInProgress(object);

        if (writeQuery.getObjectChangeSet() == null) {
            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                // only throw the events if there is no changeset otherwise the event will be thrown twice
                // once by the calculate changes code and here
                getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreInsertEvent, writeQuery));
            }
        }

        // check whether deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.preInsert(writeQuery);
        }

        // In a unit of work/writeObjects the preInsert may have caused a shallow insert of this object,
        // in this case this second write must do an update.
        if (commitManager.isShallowCommitted(object)) {
            updateForeignKeyFieldAfterInsert();
        } else {
            AbstractRecord modifyRow = writeQuery.getModifyRow();
            if (modifyRow == null) {// Maybe have been passed in as in aggregate collection.
                if (writeQuery.shouldCascadeParts()) {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRow(object, getSession()));
                } else {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForShallowInsert(object, getSession()));
                }
            } else {
                if (writeQuery.shouldCascadeParts()) {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRow(modifyRow, object, getSession()));
                } else {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForShallowInsert(modifyRow, object, getSession()));
                }
            }

            // the modify row and the translation row are the same for insert
            writeQuery.setTranslationRow(getModifyRow());
            if (!writeQuery.getDescriptor().isAggregateCollectionDescriptor()) {// Should/cannot be recomputed in aggregate collection.
                writeQuery.setPrimaryKey(getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, getSession()));
            }
            addWriteLockFieldForInsert();

            // CR#3237
            // Store the size of the modify row so we can determine if the user has added to the row in the insert.
            int modifyRowSize = getModifyRow().size();

            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                DescriptorEvent event = new DescriptorEvent(DescriptorEventManager.AboutToInsertEvent, writeQuery);
                event.setRecord(getModifyRow());
                getDescriptor().getEventManager().executeEvent(event);
            }

            // CR#3237
            // Call insert with a boolean that tells it to reprepare if the user has altered the modify row.
            insertObject(modifyRowSize != getModifyRow().size());

            // register the object before post insert to resolve possible cycles
            registerObjectInIdentityMap();
            if (writeQuery.getObjectChangeSet() != null) {
                //make sure that we put this new changeset in the changes list of the 
                //uow changeset for serialization, or customer usage.
                ((UnitOfWorkChangeSet)writeQuery.getObjectChangeSet().getUOWChangeSet()).putNewObjectInChangesList(writeQuery.getObjectChangeSet(), getSession());
            }
        }

        commitManager.markPostModifyCommitInProgress(object);
        // Verify if deep shallow modify is turned on.
        if (writeQuery.shouldCascadeParts()) {
            queryManager.postInsert(writeQuery);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostInsertEvent, writeQuery));
        }
    }

    /**
     * Insert an object in the database.
     */
    public void insertObjectForWriteWithChangeSet() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        ObjectChangeSet objectChangeSet = writeQuery.getObjectChangeSet();
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();
        CommitManager commitManager = getSession().getCommitManager();

        // check for user-defined query
        if ((!writeQuery.isUserDefined())// this is not a user-defined query
                 &&queryManager.hasInsertQuery()// there is a user-defined query
                 &&isExpressionQueryMechanism()) {// this is not a hand-coded call (custom SQL etc.)
            //must mark the changeSet here because the userDefined Insert will not use the changesets
            commitManager.markPreModifyCommitInProgress(objectChangeSet);
            performUserDefinedInsert();
            return;
        }

        // This must be done after the custom query check, otherwise it will be done twice.
        commitManager.markPreModifyCommitInProgress(objectChangeSet);
        commitManager.markPreModifyCommitInProgress(writeQuery.getObject());

        // check whether deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.preInsert(writeQuery);
        }

        // In a unit of work/writeObjects the preInsert may have caused a shallow insert of this object,
        // in this case this second write must do an update.
        if (commitManager.isShallowCommitted(objectChangeSet)) {
            updateForeignKeyFieldAfterInsert();
        } else {
            AbstractRecord modifyRow = writeQuery.getModifyRow();
            if (modifyRow == null) {// Maybe have been passed in as in aggregate collection.
                if (writeQuery.shouldCascadeParts()) {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowWithChangeSet(objectChangeSet, getSession()));
                } else {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForShallowInsertWithChangeSet(objectChangeSet, getSession()));
                }
            } else {
                if (writeQuery.shouldCascadeParts()) {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowWithChangeSet(modifyRow, objectChangeSet, getSession()));
                } else {
                    writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForShallowInsertWithChangeSet(modifyRow, objectChangeSet, getSession()));
                }
            }

            // the modify row and the translation row are the same for insert
            writeQuery.setTranslationRow(getModifyRow());
            if (!writeQuery.getDescriptor().isAggregateCollectionDescriptor()) {// Should/cannot be recomputed in aggregate collection.
                writeQuery.setPrimaryKey(objectChangeSet.getPrimaryKeys());
            }
            addWriteLockFieldForInsert();

            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                DescriptorEvent event = new DescriptorEvent(DescriptorEventManager.AboutToInsertEvent, writeQuery);
                event.setRecord(getModifyRow());
                getDescriptor().getEventManager().executeEvent(event);
            }

            insertObject();

            // register the object before post insert to resolve possible cycles
            registerObjectInIdentityMap();
            if (objectChangeSet != null) {
                //make sure that we put this new changeset in the changes list of the 
                //uow changeset for serialization, or customer usage.
                ((UnitOfWorkChangeSet)objectChangeSet.getUOWChangeSet()).putNewObjectInChangesList(objectChangeSet, getSession());
            }
        }

        commitManager.markPostModifyCommitInProgress(objectChangeSet);
        commitManager.markPostModifyCommitInProgress(writeQuery.getObject());
        // Verify if deep shallow modify is turned on.
        if (writeQuery.shouldCascadeParts()) {
            queryManager.postInsert(writeQuery);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostInsertEvent, writeQuery));
        }
    }

    /**
     * Return true if this is a call query mechanism
     */
    public boolean isCallQueryMechanism() {
        return false;
    }

    /**
     * Return true if this is an expression query mechanism
     */
    public boolean isExpressionQueryMechanism() {
        return false;
    }

    /**
     * Return true if this is a query by example mechanism
     */
    public boolean isQueryByExampleMechanism() {
        return false;
    }

    /**
     * Return true if this is a statement query mechanism
     */
    public boolean isStatementQueryMechanism() {
        return false;
    }

    /**
     * Delete the object using the user defined query.
     * This ensures that the query is cloned and prepared correctly.
     */
    protected void performUserDefinedDelete() {
        performUserDefinedWrite(getDescriptor().getQueryManager().getDeleteQuery());
    }

    /**
     * Insert the object using the user defined query.
     * This ensures that the query is cloned and prepared correctly.
     */
    protected void performUserDefinedInsert() {
        performUserDefinedWrite(getDescriptor().getQueryManager().getInsertQuery());
    }

    /**
     * Update the object using the user defined query.
     * This ensures that the query is cloned and prepared correctly.
     */
    protected void performUserDefinedUpdate() {
        performUserDefinedWrite(getDescriptor().getQueryManager().getUpdateQuery());
    }

    /**
     * Write the object using the specified user-defined query.
     * This ensures that the query is cloned and prepared correctly.
     */
    protected void performUserDefinedWrite(WriteObjectQuery userDefinedWriteQuery) {
        userDefinedWriteQuery.checkPrepare(getSession(), getTranslationRow());

        Object object = getWriteObjectQuery().getObject();
        WriteObjectQuery writeQuery = (WriteObjectQuery)userDefinedWriteQuery.clone();
        writeQuery.setObject(object);
        writeQuery.setObjectChangeSet(getWriteObjectQuery().getObjectChangeSet());
        writeQuery.setCascadePolicy(getQuery().getCascadePolicy());
        writeQuery.setShouldMaintainCache(getQuery().shouldMaintainCache());
        writeQuery.setTranslationRow(getTranslationRow());
        writeQuery.setModifyRow(getModifyRow());
        writeQuery.setPrimaryKey(getWriteObjectQuery().getPrimaryKey());
        writeQuery.setSession(getSession());
        writeQuery.prepareForExecution();

        // HACK: If there is a changeset, the change set method must be used,
        // however it is currently broken for inserts, so until this is fixed,
        // only using correct commit for updates.
        if (getWriteObjectQuery().isUpdateObjectQuery() && (getWriteObjectQuery().getObjectChangeSet() != null)) {
            writeQuery.executeCommitWithChangeSet();
        } else {
            writeQuery.executeCommit();
        }
    }

    /**
     * This is different from 'prepareForExecution()'
     * in that this is called on the original query,
     * and the other is called on the clone of the query.
     * This query is copied for concurrency so this prepare can only setup things that
     * will apply to any future execution of this query.
     */
    public void prepare() throws QueryException {
        // the default is to do nothing
    }

    /**
     * Pre-pare for a cursored execute.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareCursorSelectAllRows() throws QueryException;

    /**
     * Prepare for a delete all.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareDeleteAll() throws QueryException;

    /**
     * Prepare for a delete.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareDeleteObject() throws QueryException;

    /**
     * Pre-pare for a select execute.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareDoesExist(DatabaseField field) throws QueryException;

    /**
     * Prepare for a raw (non-object), non-selecting call.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareExecuteNoSelect() throws QueryException;

    /**
     * Prepare for a raw (non-object) select call.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareExecuteSelect() throws QueryException;

    /**
     * All the query mechanism related things are initialized here.
     * This method is called on the *clone* of the query with
     * every execution.
     */
    public void prepareForExecution() throws QueryException {
        // the default is to do nothing
    }

    /**
     * Prepare for an insert.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareInsertObject() throws QueryException;

    /**
     * Pre-pare for a select execute.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareReportQuerySelectAllRows() throws QueryException;

    /**
     * Pre-pare a report query for a sub-select.
     */
    public abstract void prepareReportQuerySubSelect() throws QueryException;

    /**
     * Prepare for a select returning (possibly) multiple rows.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareSelectAllRows() throws QueryException;

    /**
     * Prepare for a select returning a single row.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareSelectOneRow() throws QueryException;

    /**
     * Prepare for an update.
     * This is sent to the original query before cloning.
     */
    public abstract void prepareUpdateObject() throws QueryException;

    /**
       * Prepare for an update all.
       * This is sent to the original query before cloning.
       */
    public abstract void prepareUpdateAll() throws QueryException;

    /**
     * Store the query object in the identity map.
     */
    protected void registerObjectInIdentityMap() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();

        if (writeQuery.shouldMaintainCache()) {
            if (getDescriptor().usesOptimisticLocking()) {
                Object optimisticLockValue = getDescriptor().getOptimisticLockingPolicy().getValueToPutInCache(writeQuery.getModifyRow(), getSession());
                getSession().getIdentityMapAccessorInstance().putInIdentityMap(object, writeQuery.getPrimaryKey(), optimisticLockValue, System.currentTimeMillis(), getDescriptor());
            } else {
                getSession().getIdentityMapAccessorInstance().putInIdentityMap(object, writeQuery.getPrimaryKey(), null, System.currentTimeMillis(), getDescriptor());
            }
        }
    }

    /**
     * INTERNAL:
     * Read all rows from the database.
     */
    public abstract Vector selectAllReportQueryRows() throws DatabaseException;

    /**
     * Read and return rows from the database.
     */
    public abstract Vector selectAllRows() throws DatabaseException;

    /**
     * Read and return a row from the database.
     */
    public abstract AbstractRecord selectOneRow() throws DatabaseException;

    /**
     * Read and return a row from the database for an existence check.
     */
    public abstract AbstractRecord selectRowForDoesExist(DatabaseField field) throws DatabaseException;

    /**
     * Set the query that uses this mechanism.
     */
    public void setQuery(DatabaseQuery query) {
        this.query = query;
    }

    /**
     * INTERNAL:
     * Shallow delete the specified object.
     */
    public void shallowDeleteObjectForWrite(Object object, WriteObjectQuery writeQuery, CommitManager commitManager) throws DatabaseException, OptimisticLockException {
        // a shallow delete must be performed
        writeQuery.dontCascadeParts();
        deleteObjectForWrite();
        // mark this object as shallow committed so that the delete will be executed
        commitManager.markShallowCommit(object);
    }

    /**
     * INTERNAL:
     * Shallow insert the specified object.
     */
    public void shallowInsertObjectForWrite(Object object, WriteObjectQuery writeQuery, CommitManager commitManager) throws DatabaseException, OptimisticLockException {
        // a shallow insert must be performed
        writeQuery.dontCascadeParts();
        insertObjectForWrite();
        // mark this object as shallow committed so that the insert will do an update
        commitManager.markShallowCommit(object);
    }

    /**
     * Update the foreign key fields when resolving a bi-directonal reference in a UOW.
     * This must always be dynamic as it is called within an insert query and is really part of the insert
     * and does not fire update events or worry about locking.
     */
    protected void updateForeignKeyFieldAfterInsert() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();

        writeQuery.setPrimaryKey(getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, getSession()));
        // reset the translation row because the insert has occurred and the id has
        // been assigned to the object, but not the row
        writeQuery.setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(object, getSession()));

        writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForUpdate(writeQuery));
        
        updateForeignKeyFieldShallow(writeQuery);
    }

    /**
     * Null out the foreign key fields when resolving a bi-directonal reference in a UOW.
     * This must always be dynamic as it is called within a delete query and is really part of the delete
     * and does not fire update events or worry about locking.
     */
    protected void updateForeignKeyFieldBeforeDelete() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();

        writeQuery.setPrimaryKey(getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, getSession()));
        // set the translation row
        writeQuery.setTranslationRow(getDescriptor().getObjectBuilder().buildRowForTranslation(object, getSession()));
        // build a query to null out the foreign keys
        writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForShallowDelete(object, getSession()));

        updateForeignKeyFieldShallow(writeQuery);
    }

    /**
       * Issue update SQL statement
       */
    public abstract Integer updateAll() throws DatabaseException;

    /**
     * Update an object.
     * Return the row count.
     */
    public abstract Integer updateObject() throws DatabaseException;

    /**
     * Update the foreign key fields when resolving a bi-directonal reference in a UOW.
     * This must always be dynamic as it is called within an insert query and is really part of the insert
     * and does not fire update events or worry about locking.
     */
    protected abstract void updateForeignKeyFieldShallow(WriteObjectQuery writeQuery);

    protected void updateObjectAndRowWithReturnRow(Collection returnFields, boolean isFirstCallForInsert) {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        AbstractRecord outputRow = (AbstractRecord)writeQuery.getProperties().get("output");
        if ((outputRow == null) || outputRow.isEmpty()) {
            return;
        }
        AbstractRecord row = new DatabaseRecord();
        for (Iterator iterator = returnFields.iterator(); iterator.hasNext();) {
            DatabaseField field = (DatabaseField)iterator.next();
            if (outputRow.containsKey(field)) {
                row.put(field, outputRow.get(field));
            }
        }
        if (row.isEmpty()) {
            return;
        }

        Object object = writeQuery.getObject();

        getDescriptor().getObjectBuilder().assignReturnRow(object, getSession(), row);

        Vector primaryKeys = null;
        if (isFirstCallForInsert) {
            AbstractRecord pkToModify = new DatabaseRecord();
            List primaryKeyFields = getDescriptor().getPrimaryKeyFields();
            for (int i = 0; i < primaryKeyFields.size(); i++) {
                DatabaseField field = (DatabaseField)primaryKeyFields.get(i);
                if (row.containsKey(field)) {
                    pkToModify.put(field, row.get(field));
                }
            }
            if (!pkToModify.isEmpty()) {
                primaryKeys = getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, getSession());
                writeQuery.setPrimaryKey(primaryKeys);
                // Now I need to update the row
                getModifyRow().putAll(pkToModify);
                getDescriptor().getObjectBuilder().addPrimaryKeyForNonDefaultTable(getModifyRow());
            }
        }

        // update the changeSet if there is one
        if (getSession().isUnitOfWork()) {
            ObjectChangeSet objectChangeSet = writeQuery.getObjectChangeSet();
            if ((objectChangeSet == null) && (((UnitOfWorkImpl)getSession()).getUnitOfWorkChangeSet() != null)) {
                objectChangeSet = (ObjectChangeSet)((UnitOfWorkImpl)getSession()).getUnitOfWorkChangeSet().getObjectChangeSetForClone(object);
            }
            if (objectChangeSet != null) {
                updateChangeSet(getDescriptor(), objectChangeSet, row, object);
                if (primaryKeys != null) {
                    objectChangeSet.setCacheKey(new CacheKey(primaryKeys));
                }
            }
        }
    }

    /**
     * Update the change set with all of the field values in the row.
     * This handle writable and read-only mappings, direct and nested aggregates.
     * It is used from ReturningPolicy and VersionLockingPolicy.
     */
    public void updateChangeSet(ClassDescriptor desc, ObjectChangeSet objectChangeSet, AbstractRecord row, Object object) {
        HashSet handledMappings = new HashSet(row.size());
        for (int i = 0; i < row.size(); i++) {
            DatabaseField field = (DatabaseField)row.getFields().elementAt(i);
            Object value = row.getValues().elementAt(i);
            updateChangeSet(desc, objectChangeSet, field, object, handledMappings);
        }
    }

    protected void updateChangeSet(ClassDescriptor desc, ObjectChangeSet objectChangeSet, DatabaseField field, Object object) {
        updateChangeSet(desc, objectChangeSet, field, object, null);
    }

    protected void updateChangeSet(ClassDescriptor desc, ObjectChangeSet objectChangeSet, DatabaseField field, Object object, Collection handledMappings) {
        DatabaseMapping mapping;
        Vector mappingVector = desc.getObjectBuilder().getReadOnlyMappingsForField(field);
        if (mappingVector != null) {
            for (int j = 0; j < mappingVector.size(); j++) {
                mapping = (DatabaseMapping)mappingVector.elementAt(j);
                updateChangeSet(mapping, objectChangeSet, field, object, handledMappings);
            }
        }
        mapping = desc.getObjectBuilder().getMappingForField(field);
        if (mapping != null) {
            updateChangeSet(mapping, objectChangeSet, field, object, handledMappings);
        }
    }

    protected void updateChangeSet(DatabaseMapping mapping, ObjectChangeSet objectChangeSet, DatabaseField field, Object object, Collection handledMappings) {
        if ((handledMappings != null) && handledMappings.contains(mapping)) {
            return;
        }
        if (mapping.isAggregateObjectMapping()) {
            Object aggregate = mapping.getAttributeValueFromObject(object);
            AggregateChangeRecord record = (AggregateChangeRecord)objectChangeSet.getChangesForAttributeNamed(mapping.getAttributeName());
            if (aggregate != null) {
                if (record == null) {
                    record = new AggregateChangeRecord(objectChangeSet);
                    record.setAttribute(mapping.getAttributeName());
                    record.setMapping(mapping);
                    objectChangeSet.addChange(record);
                }
                ObjectChangeSet aggregateChangeSet = (oracle.toplink.essentials.internal.sessions.ObjectChangeSet)record.getChangedObject();
                ClassDescriptor aggregateDescriptor = ((AggregateObjectMapping)mapping).getReferenceDescriptor();
                if (aggregateChangeSet == null) {
                    aggregateChangeSet = aggregateDescriptor.getObjectBuilder().createObjectChangeSet(aggregate, (oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet)((UnitOfWorkImpl)getSession()).getUnitOfWorkChangeSet(), getSession());
                    record.setChangedObject(aggregateChangeSet);
                }
                updateChangeSet(aggregateDescriptor, aggregateChangeSet, field, aggregate, handledMappings);
            } else {
                if (record != null) {
                    record.setChangedObject(null);
                }
            }
        } else if (mapping.isDirectToFieldMapping()) {
            Object attributeValue = mapping.getAttributeValueFromObject(object);
            objectChangeSet.updateChangeRecordForAttribute(mapping, attributeValue);
        } else {
            getSession().log(SessionLog.FINEST, SessionLog.QUERY, "field_for_unsupported_mapping_returned", field, getDescriptor());
        }
    }

    /**
     * Update the object's primary key by fetching a new sequence number from the accessor.
     */
    protected void updateObjectAndRowWithSequenceNumber() throws DatabaseException {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();

        Object sequenceValue = getDescriptor().getObjectBuilder().assignSequenceNumber(object, getSession());
        if (sequenceValue == null) {
            return;
        }
        Vector primaryKeys = getDescriptor().getObjectBuilder().extractPrimaryKeyFromObject(object, getSession());
        writeQuery.setPrimaryKey(primaryKeys);
        DatabaseField sequenceNumberField = getDescriptor().getSequenceNumberField();

        // Now I need to update the row
        getModifyRow().put(sequenceNumberField, sequenceValue);
        getDescriptor().getObjectBuilder().addPrimaryKeyForNonDefaultTable(getModifyRow());
        // update the changeSet if there is one
        if (getSession().isUnitOfWork()) {
            ObjectChangeSet objectChangeSet = writeQuery.getObjectChangeSet();
            if ((objectChangeSet == null) && (((UnitOfWorkImpl)getSession()).getUnitOfWorkChangeSet() != null)) {
                objectChangeSet = (ObjectChangeSet)((UnitOfWorkImpl)getSession()).getUnitOfWorkChangeSet().getObjectChangeSetForClone(object);
            }
            if (objectChangeSet != null) {
                updateChangeSet(getDescriptor(), objectChangeSet, sequenceNumberField, object);
                objectChangeSet.setCacheKey(new CacheKey(primaryKeys));
            }
        }
    }

    /**
     * Update the object
     */
    public void updateObjectForWrite() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();

        // check for user-defined query
        if ((!writeQuery.isUserDefined())// this is not a user-defined query
                 &&queryManager.hasUpdateQuery()// there is a user-defined query
                 &&isExpressionQueryMechanism()) {// this is not a hand-coded call (custom SQL etc.)
            performUserDefinedUpdate();
            return;
        }

        // This must be done after the custom query check, otherwise it will be done twice.
        getSession().getCommitManager().markPreModifyCommitInProgress(object);

        if (writeQuery.getObjectChangeSet() == null) {
            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                // only throw the events if there is no changeset otherwise the event will be thrown twice
                // once by the calculate changes code and here
                getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreUpdateEvent, writeQuery));
            }
        }

        // Verify if deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.preUpdate(writeQuery);
        }

        // The row must not be built until after preUpdate in case the object reference has changed.
        // For a user defined update in the uow to row must be built twice to check if any update is required.
        if ((writeQuery.isUserDefined() || writeQuery.isCallQuery()) && (!getSession().isUnitOfWork())) {
            writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRow(object, getSession()));
        } else {
            writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForUpdate(writeQuery));
        }

        if (!getModifyRow().isEmpty()) {
            // If user defined the entire row is required. Must not be built until change is known.
            if ((writeQuery.isUserDefined() || writeQuery.isCallQuery()) && getSession().isUnitOfWork()) {
                writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRow(object, getSession()));
            }

            // Update the write lock field if required
            if (getDescriptor().usesOptimisticLocking()) {
                OptimisticLockingPolicy policy = getDescriptor().getOptimisticLockingPolicy();
                policy.addLockValuesToTranslationRow(writeQuery);

                // update the row with newer lock value		
                policy.updateRowAndObjectForUpdate(writeQuery, object);               
            }

            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                DescriptorEvent event = new DescriptorEvent(DescriptorEventManager.AboutToUpdateEvent, writeQuery);
                event.setRecord(getModifyRow());
                getDescriptor().getEventManager().executeEvent(event);
            }

            int rowCount = updateObject().intValue();

            if (rowCount < 1) {
                getSession().getEventManager().noRowsModified(writeQuery, object);
            }
            if (getDescriptor().usesOptimisticLocking()) {
                getDescriptor().getOptimisticLockingPolicy().validateUpdate(rowCount, object, writeQuery);
            }
        }

        getSession().getCommitManager().markPostModifyCommitInProgress(object);

        // Verify if deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.postUpdate(writeQuery);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostUpdateEvent, writeQuery));
        }
    }

    /**
     * Update the object
     */
    public void updateObjectForWriteWithChangeSet() {
        WriteObjectQuery writeQuery = getWriteObjectQuery();
        Object object = writeQuery.getObject();
        DescriptorQueryManager queryManager = getDescriptor().getQueryManager();

        // check for user-defined query
        if ((!writeQuery.isUserDefined())// this is not a user-defined query
                 &&queryManager.hasUpdateQuery()// there is a user-defined query
                 &&isExpressionQueryMechanism()) {// this is not a hand-coded call (custom SQL etc.)
            // THis must be done here because the userdefined updatedoes not use a changeset so it will noe be set otherwise
            getSession().getCommitManager().markPreModifyCommitInProgress(writeQuery.getObjectChangeSet());
            performUserDefinedUpdate();
            return;
        }

        // This must be done after the custom query check, otherwise it will be done twice.
        getSession().getCommitManager().markPreModifyCommitInProgress(object);
        // This must be done after the custom query check, otherwise it will be done twice.
        getSession().getCommitManager().markPreModifyCommitInProgress(writeQuery.getObjectChangeSet());

        if (writeQuery.getObjectChangeSet().hasChanges()) {
            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                DescriptorEvent event = new DescriptorEvent(DescriptorEventManager.PreUpdateWithChangesEvent, writeQuery);
                getDescriptor().getEventManager().executeEvent(event);

                // PreUpdateWithChangesEvent listeners may have altered the object - should recalculate the change set.
                UnitOfWorkChangeSet uowChangeSet = (oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet)((UnitOfWorkImpl)writeQuery.getSession()).getUnitOfWorkChangeSet();
                // writeQuery.getObjectChangeSet() is mapped to object in uowChangeSet.
                // It is first cleared then re-populated by calculateChanges method.
                writeQuery.getObjectChangeSet().clear();
                if(writeQuery.getDescriptor().getObjectChangePolicy().calculateChanges(object, ((UnitOfWorkImpl)event.getSession()).getBackupClone(object), uowChangeSet, writeQuery.getSession(), writeQuery.getDescriptor(), false) == null) {
                    // calculateChanges returns null in case the changeSet doesn't have changes.
                    // It should be removed from the list of ObjectChangeSets that have changes in uowChangeSet.
                    uowChangeSet.getAllChangeSets().remove(writeQuery.getObjectChangeSet());
                }
            }
        }
         
        // Verify if deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.preUpdate(writeQuery);
        }

        // The row must not be built until after preUpdate in case the object reference has changed.
        // For a user defined update in the uow to row must be built twice to check if any update is required.
        writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRowForUpdateWithChangeSet(writeQuery));
            
    	Boolean shouldModifyVersionField = writeQuery.getObjectChangeSet().shouldModifyVersionField();

        if (!getModifyRow().isEmpty() || (shouldModifyVersionField != null) || writeQuery.getObjectChangeSet().hasCmpPolicyForcedUpdate()) {
            // If user defined the entire row is required. Must not be built until change is known.
            if (writeQuery.isUserDefined() || writeQuery.isCallQuery()) {
                writeQuery.setModifyRow(getDescriptor().getObjectBuilder().buildRow(object, getSession()));
            }

            // Update the write lock field if required
            if (getDescriptor().usesOptimisticLocking()) {
                OptimisticLockingPolicy policy = getDescriptor().getOptimisticLockingPolicy();
                policy.addLockValuesToTranslationRow(writeQuery);

                if (!getModifyRow().isEmpty() || (shouldModifyVersionField.booleanValue() && policy instanceof VersionLockingPolicy)) {
                    // update the row with newer lock value		
                    policy.updateRowAndObjectForUpdate(writeQuery, object);
                } else if (!shouldModifyVersionField.booleanValue() && policy instanceof VersionLockingPolicy) {
                    ((VersionLockingPolicy)policy).writeLockValueIntoRow(writeQuery, object);
                }
            }

            // PERF: Avoid events if no listeners.
            if (getDescriptor().getEventManager().hasAnyEventListeners()) {
                DescriptorEvent event = new DescriptorEvent(DescriptorEventManager.AboutToUpdateEvent, writeQuery);
                event.setRecord(getModifyRow());
                getDescriptor().getEventManager().executeEvent(event);
            }

            int rowCount = updateObject().intValue();

            if (rowCount < 1) {
                getSession().getEventManager().noRowsModified(writeQuery, object);
            }
            if (getDescriptor().usesOptimisticLocking()) {
                getDescriptor().getOptimisticLockingPolicy().validateUpdate(rowCount, object, writeQuery);
            }
        }

        getSession().getCommitManager().markPostModifyCommitInProgress(object);
        getSession().getCommitManager().markPostModifyCommitInProgress(writeQuery.getObjectChangeSet());

        // Verify if deep shallow modify is turned on
        if (writeQuery.shouldCascadeParts()) {
            queryManager.postUpdate(writeQuery);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            getDescriptor().getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PostUpdateEvent, writeQuery));
        }
    }
}
