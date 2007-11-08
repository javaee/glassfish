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
package oracle.toplink.essentials.internal.descriptors;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.identitymaps.*;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.mappings.foundation.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.querykeys.*;
import oracle.toplink.essentials.descriptors.DescriptorEventManager;
import oracle.toplink.essentials.sessions.ObjectCopyingPolicy;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: Object builder is one of the behaviour class attached to descriptor.
 * It is responsible for building objects, rows, and extracting primary keys from
 * the object and the rows.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class ObjectBuilder implements Cloneable, Serializable {
    protected ClassDescriptor descriptor;
    protected Map<String, DatabaseMapping> mappingsByAttribute;
    protected Map<DatabaseField, DatabaseMapping> mappingsByField;
    protected Map<DatabaseField, Vector<DatabaseMapping>> readOnlyMappingsByField;
    protected Vector<DatabaseMapping> primaryKeyMappings;
    protected Vector<Class> primaryKeyClassifications;
    protected transient Vector<DatabaseMapping> nonPrimaryKeyMappings;
    protected transient Expression primaryKeyExpression;

    /** PERF: Cache mapping that use joining. */
    protected Vector<String> joinedAttributes = null;

    /** PERF: Cache mappings that require cloning. */
    protected List<DatabaseMapping> cloningMappings;

    public ObjectBuilder(ClassDescriptor descriptor) {
        this.mappingsByField = new HashMap(20);
        this.readOnlyMappingsByField = new HashMap(20);
        this.mappingsByAttribute = new HashMap(20);
        this.primaryKeyMappings = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(5);
        this.nonPrimaryKeyMappings = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(10);
        this.cloningMappings = new ArrayList(10);
        this.descriptor = descriptor;
    }

    /**
     * Create a new row/record for the object builder.
     * This allows subclasses to define different record types.
     */
    public AbstractRecord createRecord() {
        return new DatabaseRecord();
    }

    /**
     * Create a new row/record for the object builder.
     * This allows subclasses to define different record types.
     */
    public AbstractRecord createRecord(int size) {
        return new DatabaseRecord(size);
    }

    /**
     * Add the primary key and its value to the databaseRow for all the non default tables.
     * This method is used while writing into the multiple tables.
     */
    public void addPrimaryKeyForNonDefaultTable(AbstractRecord databaseRow) {
        // this method has been revised so it calls addPrimaryKeyForNonDefaultTable(DatabaseRow, Object, Session) is similar.
        // the session and object are null in this case.
        addPrimaryKeyForNonDefaultTable(databaseRow, null, null);
    }

    /**
     * Add the primary key and its value to the databaseRow for all the non default tables.
     * This method is used while writing into the multiple tables.
     */
    public void addPrimaryKeyForNonDefaultTable(AbstractRecord databaseRow, Object object, AbstractSession session) {
        if (!getDescriptor().hasMultipleTables()) {
            return;
        }
        Enumeration tablesEnum = getDescriptor().getTables().elements();

        // Skip first table.
        tablesEnum.nextElement();
        while (tablesEnum.hasMoreElements()) {
            DatabaseTable table = (DatabaseTable)tablesEnum.nextElement();
            Map keyMapping = (Map)getDescriptor().getAdditionalTablePrimaryKeyFields().get(table);

            // Loop over the additionalTablePK fields and add the PK info for the table. The join might
            // be between a fk in the source table and pk in secondary table.
            if (keyMapping != null) {
                Iterator primaryKeyFieldEnum = keyMapping.keySet().iterator();
                Iterator secondaryKeyFieldEnum = keyMapping.values().iterator();
                while (primaryKeyFieldEnum.hasNext()) {
                    DatabaseField primaryKeyField = (DatabaseField)primaryKeyFieldEnum.next();
                    DatabaseField secondaryKeyField = (DatabaseField)secondaryKeyFieldEnum.next();
                    Object primaryValue = databaseRow.get(primaryKeyField);

                    // normally the primary key has a value, however if the multiple tables were joined by a foreign
                    // key the foreign key has a value.
                    if ((primaryValue == null) && (!databaseRow.containsKey(primaryKeyField))) {
                        if (object != null) {
                            DatabaseMapping mapping = getMappingForField(secondaryKeyField);
                            if (mapping == null) {
                                throw DescriptorException.missingMappingForField(secondaryKeyField, getDescriptor());
                            }
                            mapping.writeFromObjectIntoRow(object, databaseRow, session);
                        }
                        databaseRow.put(primaryKeyField, databaseRow.get(secondaryKeyField));
                    } else {
                        databaseRow.put(secondaryKeyField, primaryValue);
                    }
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Assign returned row to object
     */
    public void assignReturnRow(Object object, AbstractSession writeSession, AbstractRecord row) throws DatabaseException {
        writeSession.log(SessionLog.FINEST, SessionLog.QUERY, "assign_return_row", row);

        // Require a query context to read into an object.
        ReadObjectQuery query = new ReadObjectQuery();
        query.setSession(writeSession);

        // To avoid processing the same mapping twice,
        // maintain Collection of mappings already used.
        HashSet handledMappings = new HashSet(row.size());
        for (int index = 0; index < row.size(); index++) {
            DatabaseField field = (DatabaseField)row.getFields().elementAt(index);
            assignReturnValueForField(object, query, row, field, handledMappings);
        }
    }

    /**
     * INTERNAL:
     * Assign values from objectRow to the object through all the mappings corresponding to the field.
     */
    public void assignReturnValueForField(Object object, ReadObjectQuery query, AbstractRecord row, DatabaseField field, Collection handledMappings) {
        DatabaseMapping mapping = getMappingForField(field);
        if (mapping != null) {
            assignReturnValueToMapping(object, query, row, field, mapping, handledMappings);
        }
        Vector mappingVector = getReadOnlyMappingsForField(field);
        if (mappingVector != null) {
            for (int j = 0; j < mappingVector.size(); j++) {
                mapping = (DatabaseMapping)mappingVector.elementAt(j);
                assignReturnValueToMapping(object, query, row, field, mapping, handledMappings);
            }
        }
    }

    /**
     * INTERNAL:
     * Assign values from objectRow to the object through the mapping.
     */
    protected void assignReturnValueToMapping(Object object, ReadObjectQuery query, AbstractRecord row, DatabaseField field, DatabaseMapping mapping, Collection handledMappings) {
        if (handledMappings.contains(mapping)) {
            return;
        }
        Object attributeValue;
        if (mapping.isAggregateObjectMapping()) {
            attributeValue = ((AggregateObjectMapping)mapping).readFromReturnRowIntoObject(row, object, query, handledMappings);
        } else if (mapping.isDirectToFieldMapping()) {
            attributeValue = mapping.readFromRowIntoObject(row, null, object, query);
        } else {
            query.getSession().log(SessionLog.FINEST, SessionLog.QUERY, "field_for_unsupported_mapping_returned", field, getDescriptor());
        }
    }

    /**
     * INTERNAL:
     * Update the object primary key by fetching a new sequence number from the accessor.
     * This assume the uses sequence numbers check has already been done.
     * @return the sequence value or null if not assigned.
     * @exception  DatabaseException - an error has occurred on the database.
     */
    public Object assignSequenceNumber(Object object, AbstractSession writeSession) throws DatabaseException {
        DatabaseField sequenceNumberField = getDescriptor().getSequenceNumberField();
        Object existingValue = getBaseValueForField(sequenceNumberField, object);

        //** sequencing refactoring
        if (existingValue != null) {
            if (!writeSession.getSequencing().shouldOverrideExistingValue(object.getClass(), existingValue)) {
                return null;
            }
        }
        Object sequenceValue = writeSession.getSequencing().getNextValue(object.getClass());

        //CR#2272
        writeSession.log(SessionLog.FINEST, SessionLog.SEQUENCING, "assign_sequence", sequenceValue, object);

        // Check that the value is not null, this occurs on Sybase identity only **
        if (sequenceValue == null) {
            return null;
        }

        // Now add the value to the object, this gets ugly.
        AbstractRecord tempRow = createRecord(1);
        tempRow.put(sequenceNumberField, sequenceValue);

        // Require a query context to read into an object.
        ReadObjectQuery query = new ReadObjectQuery();
        query.setSession(writeSession);
        DatabaseMapping mapping = getBaseMappingForField(sequenceNumberField);
        Object sequenceIntoObject = getParentObjectForField(sequenceNumberField, object);

        // the following method will return the converted value for the sequence
        Object convertedSequenceValue = mapping.readFromRowIntoObject(tempRow, null, sequenceIntoObject, query);

        return convertedSequenceValue;
    }

    /**
     * Each mapping is recursed to assign values from the databaseRow to the attributes in the domain object.
     */
    public void buildAttributesIntoObject(Object domainObject, AbstractRecord databaseRow, ObjectBuildingQuery query, JoinedAttributeManager joinManager, boolean forRefresh) throws DatabaseException {
        AbstractSession executionSession = query.getSession().getExecutionSession(query);

        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();

        // PERF: Cache if all mappings should be read.
        boolean readAllMappings = query.shouldReadAllMappings();
        int mappingsSize = mappings.size();
        for (int index = 0; index < mappingsSize; index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            if (readAllMappings || query.shouldReadMapping(mapping)) {
                mapping.readFromRowIntoObject(databaseRow, joinManager, domainObject, query, executionSession);
            }
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            // Need to run post build or refresh selector, currently check with the query for this,
            // I'm not sure which should be called it case of refresh building a new object, currently refresh is used...
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(domainObject);
            event.setQuery(query);
            event.setSession(query.getSession());
            event.setRecord(databaseRow);
            if (forRefresh) {
                //this method can be called from different places within TopLink.  We may be
                //executing refresh query but building the object not refreshing so we must
                //throw the appropriate event.
                //bug 3325315
                event.setEventCode(DescriptorEventManager.PostRefreshEvent);
            } else {
                event.setEventCode(DescriptorEventManager.PostBuildEvent);
            }
            getDescriptor().getEventManager().executeEvent(event);
        }
    }

    /**
     * Returns the clone of the specified object. This is called only from unit of work.
     * The clonedDomainObject sent as parameter is always a working copy from the unit of work.
     */
    public Object buildBackupClone(Object clone, UnitOfWorkImpl unitOfWork) {
        // The copy policy builds clone	
        Object backup = getDescriptor().getCopyPolicy().buildClone(clone, unitOfWork);

        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        List mappings = getCloningMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).buildBackupClone(clone, backup, unitOfWork);
        }

        return backup;
    }

    /**
     * Build and return the expression to use as the where clause to delete an object.
     * The row is passed to allow the version number to be extracted from it.
     */
    public Expression buildDeleteExpression(DatabaseTable table, AbstractRecord row) {
        if (getDescriptor().usesOptimisticLocking() && (getDescriptor().getTables().firstElement().equals(table))) {
            return getDescriptor().getOptimisticLockingPolicy().buildDeleteExpression(table, primaryKeyExpression, row);
        } else {
            return buildPrimaryKeyExpression(table);
        }
    }

    /**
     * Return a new instance of the receiver's javaClass.
     */
    public Object buildNewInstance() {
        return getDescriptor().getInstantiationPolicy().buildNewInstance();
    }

    /**
     * Return an instance of the recievers javaClass. Set the attributes of an instance
     * from the values stored in the database row.
     */
    public Object buildObject(ObjectBuildingQuery query, AbstractRecord databaseRow, JoinedAttributeManager joinManager) throws DatabaseException, QueryException {
        // Profile object building.
        AbstractSession session = query.getSession();
        session.startOperationProfile(SessionProfiler.OBJECT_BUILDING);

        Vector primaryKey = extractPrimaryKeyFromRow(databaseRow, session);

        // Check for null primary key, this is not allowed.
        if ((primaryKey == null) && (!query.hasPartialAttributeExpressions()) && (!getDescriptor().isAggregateCollectionDescriptor())) {
            // Profile object building.
            session.endOperationProfile(SessionProfiler.OBJECT_BUILDING);

            //BUG 3168689: EJBQL: "Select Distinct s.customer from SpouseBean s"
            //BUG 3168699: EJBQL: "Select s.customer from SpouseBean s where s.id = '6'"
            //If we return either a single null, or a Collection containing at least 
            //one null, then we want the nulls returned/included if the indicated 
            //property is set in the query. (As opposed to throwing an Exception).
            if (query.getProperty("return null if primary key is null") != null) {
                return null;
            } else {
                throw QueryException.nullPrimaryKeyInBuildingObject(query, databaseRow);
            }
        }
        ClassDescriptor concreteDescriptor = getDescriptor();
        if (concreteDescriptor.hasInheritance() && concreteDescriptor.getInheritancePolicy().shouldReadSubclasses()) {
            Class classValue = concreteDescriptor.getInheritancePolicy().classFromRow(databaseRow, session);
            concreteDescriptor = session.getDescriptor(classValue);
            if ((concreteDescriptor == null) && query.hasPartialAttributeExpressions()) {
                concreteDescriptor = getDescriptor();
            }
            if (concreteDescriptor == null) {
                // Profile object building.
                session.endOperationProfile(SessionProfiler.OBJECT_BUILDING);
                throw QueryException.noDescriptorForClassFromInheritancePolicy(query, classValue);
            }
        }
        Object domainObject = null;
        try {
            if (session.isUnitOfWork()) {
                // Do not wrap yet if in UnitOfWork, as there is still much more
                // processing ahead.
                domainObject = buildObjectInUnitOfWork(query, joinManager, databaseRow, (UnitOfWorkImpl)session, primaryKey, concreteDescriptor);
            } else {
                domainObject = buildObject(query, databaseRow, session, primaryKey, concreteDescriptor, joinManager);

                // wrap the object if the query requires it.
                if (query.shouldUseWrapperPolicy()) {
                    domainObject = concreteDescriptor.getObjectBuilder().wrapObject(domainObject, session);
                }
            }
        } finally {
            session.endOperationProfile(SessionProfiler.OBJECT_BUILDING);
        }
        return domainObject;
    }

    /**
     * For executing all reads on the UnitOfWork, the session when building
     * objects from rows will now be the UnitOfWork.  Usefull if the rows were
     * read via a dirty write connection and we want to avoid putting uncommitted
     * data in the global cache.
     * <p>
     * Decides whether to call either buildWorkingCopyCloneFromRow (bypassing
     * shared cache) or buildWorkingCopyCloneNormally (placing the result in the
     * shared cache).
     */
    protected Object buildObjectInUnitOfWork(ObjectBuildingQuery query, JoinedAttributeManager joinManager, AbstractRecord databaseRow, UnitOfWorkImpl unitOfWork, Vector primaryKey, ClassDescriptor concreteDescriptor) throws DatabaseException, QueryException {
        // When in transaction we are reading via the write connection
        // and so do not want to corrupt the shared cache with dirty objects.
        // Hence we build and refresh clones directly from the database row.
        if ((unitOfWork.getCommitManager().isActive() || unitOfWork.wasTransactionBegunPrematurely()) && !unitOfWork.isClassReadOnly(concreteDescriptor.getJavaClass())) {
            // It is easier to switch once to the correct builder here.
            return concreteDescriptor.getObjectBuilder().buildWorkingCopyCloneFromRow(query, joinManager, databaseRow, unitOfWork, primaryKey);
        }

        return buildWorkingCopyCloneNormally(query, databaseRow, unitOfWork, primaryKey, concreteDescriptor, joinManager);
    }

    /**
     * buildWorkingCopyCloneFromRow is an alternative to this which is the
     * normal behavior.
     * A row is read from the database, an original is built/refreshed/returned
     * from the shared cache, and the original is registered/conformed/reverted
     * in the UnitOfWork.
     * <p>
     * This default behavior is only safe when the query is executed on a read
     * connection, otherwise uncommitted data might get loaded into the shared
     * cache.
     * <p>
     * Represents the way TopLink has always worked.
     */
    protected Object buildWorkingCopyCloneNormally(ObjectBuildingQuery query, AbstractRecord databaseRow, UnitOfWorkImpl unitOfWork, Vector primaryKey, ClassDescriptor concreteDescriptor, JoinedAttributeManager joinManager) throws DatabaseException, QueryException {
        // This is normal case when we are not in transaction.
        // Pass the query off to the parent.  Let it build the object and
        // cache it normally, then register/refresh it.
        AbstractSession session = unitOfWork.getParentIdentityMapSession(query);
        Object original = null;
        Object clone = null;

        // forwarding queries to different sessions is now as simple as setting
        // the session on the query.
        query.setSession(session);
        if (session.isUnitOfWork()) {
            original = buildObjectInUnitOfWork(query, joinManager, databaseRow, (UnitOfWorkImpl)session, primaryKey, concreteDescriptor);
        } else {
            original = buildObject(query, databaseRow, session, primaryKey, concreteDescriptor, joinManager);
        }
        query.setSession(unitOfWork);
        //GFBug#404  Pass in joinManager or not based on if shouldCascadeCloneToJoinedRelationship is set to true 
        if (unitOfWork.shouldCascadeCloneToJoinedRelationship()) {
            clone = query.registerIndividualResult(original, unitOfWork, false, joinManager);// false == no longer building directly from rows.
        } else {
            clone = query.registerIndividualResult(original, unitOfWork, false, null);// false == no longer building directly from rows.            
        }
        return clone;
    }

    /**
     * Return an instance of the recievers javaClass. Set the attributes of an instance
     * from the values stored in the database row.
     */
    protected Object buildObject(ObjectBuildingQuery query, AbstractRecord databaseRow, AbstractSession session, Vector primaryKey, ClassDescriptor concreteDescriptor, JoinedAttributeManager joinManager) throws DatabaseException, QueryException {
        Object domainObject = null;

        //cache key is used for object locking
        CacheKey cacheKey = null;
        try {
            // Check if the objects exists in the identity map.
            if (query.shouldMaintainCache()) {
                //lock the object in the IM
                // PERF: Only use deferred locking if required.
                // CR#3876308 If joining is used, deferred locks are still required.
                if (DeferredLockManager.SHOULD_USE_DEFERRED_LOCKS && (concreteDescriptor.shouldAcquireCascadedLocks() || joinManager.hasJoinedAttributes())) {
                    cacheKey = session.getIdentityMapAccessorInstance().acquireDeferredLock(primaryKey, concreteDescriptor.getJavaClass(), concreteDescriptor);
                    domainObject = cacheKey.getObject();

                    int counter = 0;
                    while ((domainObject == null) && (counter < 1000)) {
                        if (cacheKey.getMutex().getActiveThread() == Thread.currentThread()) {
                            break;
                        }
                        //must release lock here to prevent acquiring multiple deferred locks but only
                        //releasing one at the end of the build object call.
			//BUG 5156075
                        cacheKey.releaseDeferredLock();

                        //sleep and try again if we arenot the owner of the lock for CR 2317
                        // prevents us from modifying a cache key that another thread has locked.												
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException exception) {
                        }
                        cacheKey = session.getIdentityMapAccessorInstance().acquireDeferredLock(primaryKey, concreteDescriptor.getJavaClass(), concreteDescriptor);
                        domainObject = cacheKey.getObject();
                        counter++;
                    }
                    if (counter == 1000) {
                        throw ConcurrencyException.maxTriesLockOnBuildObjectExceded(cacheKey.getMutex().getActiveThread(), Thread.currentThread());
                    }
                } else {
                    cacheKey = session.getIdentityMapAccessorInstance().acquireLock(primaryKey, concreteDescriptor.getJavaClass(), concreteDescriptor);
                    domainObject = cacheKey.getObject();
                }
            }

            if (domainObject == null) {
                if (query.isReadObjectQuery() && ((ReadObjectQuery)query).shouldLoadResultIntoSelectionObject()) {
                    domainObject = ((ReadObjectQuery)query).getSelectionObject();
                } else {
                    domainObject = concreteDescriptor.getObjectBuilder().buildNewInstance();
                }

                // The object must be registered before building its attributes to resolve circular dependancies. 
                if (query.shouldMaintainCache()) {
                    cacheKey.setObject(domainObject);

                    copyQueryInfoToCacheKey(cacheKey, query, databaseRow, session, concreteDescriptor);

                    //set the fetch group to the domain object
                    if (concreteDescriptor.hasFetchGroupManager()) {
                        concreteDescriptor.getFetchGroupManager().setObjectFetchGroup(domainObject, query.getFetchGroup());
                    }
                }

                concreteDescriptor.getObjectBuilder().buildAttributesIntoObject(domainObject, databaseRow, query, joinManager, false);
            } else {
                if (query.isReadObjectQuery() && ((ReadObjectQuery)query).shouldLoadResultIntoSelectionObject()) {
                    copyInto(domainObject, ((ReadObjectQuery)query).getSelectionObject());
                    domainObject = ((ReadObjectQuery)query).getSelectionObject();
                }

                //check if the cached object has been invalidated
                boolean isInvalidated = concreteDescriptor.getCacheInvalidationPolicy().isInvalidated(cacheKey, query.getExecutionTime());

                //CR #4365 - Queryid comparison used to prevent infinit recursion on refresh object cascade all
                //if the concurrency manager is locked by the merge process then no refresh is required.
                // bug # 3388383 If this thread does not have the active lock then someone is building the object so in order to maintain data integrity this thread will not
                // fight to overwrite the object ( this also will avoid potential deadlock situations
                if ((cacheKey.getMutex().getActiveThread() == Thread.currentThread()) && ((query.shouldRefreshIdentityMapResult() || concreteDescriptor.shouldAlwaysRefreshCache() || isInvalidated) && ((cacheKey.getLastUpdatedQueryId() != query.getQueryId()) && !cacheKey.getMutex().isLockedByMergeManager()))) {
                    //cached object might be partially fetched, only refresh the fetch group attributes of the query if
                    //the cached partial object is not invalidated and does not contain all data for the fetch group.	
                    if (concreteDescriptor.hasFetchGroupManager() && concreteDescriptor.getFetchGroupManager().isPartialObject(domainObject)) {
                        //only ObjectLevelReadQuery and above support partial objects
                        revertFetchGroupData(domainObject, concreteDescriptor, cacheKey, ((ObjectLevelReadQuery)query), joinManager, databaseRow, session);
                    } else {
                        boolean refreshRequired = true;
                        if (concreteDescriptor.usesOptimisticLocking()) {
                            OptimisticLockingPolicy policy = concreteDescriptor.getOptimisticLockingPolicy();
                            Object cacheValue = policy.getValueToPutInCache(databaseRow, session);
                            if (concreteDescriptor.shouldOnlyRefreshCacheIfNewerVersion()) {
                                refreshRequired = policy.isNewerVersion(databaseRow, domainObject, primaryKey, session);
                                if (!refreshRequired) {
                                    cacheKey.setReadTime(query.getExecutionTime());
                                }
                            }
                            if (refreshRequired) {
                                //update the wriet lock value
                                cacheKey.setWriteLockValue(cacheValue);
                            }
                        }
                        if (refreshRequired) {
                            //CR #4365 - used to prevent infinit recursion on refresh object cascade all
                            cacheKey.setLastUpdatedQueryId(query.getQueryId());
                            concreteDescriptor.getObjectBuilder().buildAttributesIntoObject(domainObject, databaseRow, query, joinManager, true);
                            cacheKey.setReadTime(query.getExecutionTime());
                        }
                    }
                } else if (concreteDescriptor.hasFetchGroupManager() && (concreteDescriptor.getFetchGroupManager().isPartialObject(domainObject) && (!concreteDescriptor.getFetchGroupManager().isObjectValidForFetchGroup(domainObject, query.getFetchGroup())))) {
                    //the fetched object is not sufficient for the fetch group of the query 
                    //refresh attributes of the query's fetch group
                    concreteDescriptor.getObjectBuilder().buildAttributesIntoObject(domainObject, databaseRow, query, joinManager, false);
                    concreteDescriptor.getFetchGroupManager().unionFetchGroupIntoObject(domainObject, query.getFetchGroup());
                }
                // 3655915: a query with join/batch'ing that gets a cache hit
                // may require some attributes' valueholders to be re-built.
                else if (joinManager.hasJoinedAttributeExpressions()) {
                    for (Iterator e = joinManager.getJoinedAttributeExpressions().iterator();
                             e.hasNext();) {
                        QueryKeyExpression qke = (QueryKeyExpression)e.next();

                        // only worry about immediate attributes
                        if (qke.getBaseExpression().isExpressionBuilder()) {
                            DatabaseMapping dm = getMappingForAttributeName(qke.getName());

                            if (dm == null) {
                                throw ValidationException.missingMappingForAttribute(concreteDescriptor, qke.getName(), this.toString());
                            } else {
                                // Bug 4230655 - do not replace instantiated valueholders
                                Object attributeValue = dm.getAttributeValueFromObject(domainObject);
                                if (!((attributeValue != null) && dm.isForeignReferenceMapping() && ((ForeignReferenceMapping)dm).usesIndirection() && ((ForeignReferenceMapping)dm).getIndirectionPolicy().objectIsInstantiated(attributeValue))) {
                                    dm.readFromRowIntoObject(databaseRow, joinManager, domainObject, query, query.getSession().getExecutionSession(query));
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (query.shouldMaintainCache() && (cacheKey != null)) {
                // bug 2681401:
                // in case of exception (for instance, thrown by buildNewInstance())
                // cacheKey.getObject() may be null.
                if (cacheKey.getObject() != null) {
                    cacheKey.updateAccess();
                }

                // PERF: Only use deferred locking if required.
                if (DeferredLockManager.SHOULD_USE_DEFERRED_LOCKS && (concreteDescriptor.shouldAcquireCascadedLocks() || joinManager.hasJoinedAttributes())) {
                    cacheKey.releaseDeferredLock();
                } else {
                    cacheKey.release();
                }
            }
        }

        return domainObject;
    }

    /**
     * Clean up the cached object data and only revert the fetch group data back to the cached object.
     */
    private void revertFetchGroupData(Object domainObject, ClassDescriptor concreteDescriptor, CacheKey cacheKey, ObjectBuildingQuery query, JoinedAttributeManager joinManager, AbstractRecord databaseRow, AbstractSession session) {
        //the cached object is either invalidated, or staled as the version is newer, or a refresh is explicitly set on the query.
        //clean all data of the cache object.
        concreteDescriptor.getFetchGroupManager().reset(domainObject);
        //read in the fetch group data only
        concreteDescriptor.getObjectBuilder().buildAttributesIntoObject(domainObject, databaseRow, query, joinManager, false);
        //set fetch group refrence to the cached object
        concreteDescriptor.getFetchGroupManager().setObjectFetchGroup(domainObject, query.getFetchGroup());
        //set refresh on fetch group
        concreteDescriptor.getFetchGroupManager().setRefreshOnFetchGroupToObject(domainObject, (query.shouldRefreshIdentityMapResult() || concreteDescriptor.shouldAlwaysRefreshCache()));
        //set query id to prevent infinite recursion on refresh object cascade all
        cacheKey.setLastUpdatedQueryId(query.getQueryId());
        //register the object into the IM and set the write lock object if applied.
        if (concreteDescriptor.usesOptimisticLocking()) {
            OptimisticLockingPolicy policy = concreteDescriptor.getOptimisticLockingPolicy();
            cacheKey.setWriteLockValue(policy.getValueToPutInCache(databaseRow, session));
        }
        cacheKey.setReadTime(query.getExecutionTime());
        //validate the cached object
        cacheKey.setInvalidationState(CacheKey.CHECK_INVALIDATION_POLICY);
    }

    /**
     * Return a container which contains the instances of the receivers javaClass.
     * Set the fields of the instance to the values stored in the database rows.
     */
    public Object buildObjectsInto(ReadAllQuery query, Vector databaseRows, Object domainObjects) throws DatabaseException {
        Set identitySet = null;
        for (Enumeration rowsEnum = databaseRows.elements(); rowsEnum.hasMoreElements();) {
            AbstractRecord databaseRow = (AbstractRecord)rowsEnum.nextElement();

            // Skip null rows from 1-m joining duplicate row filtering.
            if (databaseRow != null) {
                Object domainObject = buildObject(query, databaseRow, query.getJoinedAttributeManager());

                // Avoid duplicates if -m joining was used and a cache hit occured.
                if (query.getJoinedAttributeManager().isToManyJoin()) {
                    if (identitySet == null) {
                        identitySet = new TopLinkIdentityHashSet(databaseRows.size());
                    }
                    if (!identitySet.contains(domainObject)) {
                        identitySet.add(domainObject);
                        query.getContainerPolicy().addInto(domainObject, domainObjects, query.getSession());
                    }
                } else {
                    query.getContainerPolicy().addInto(domainObject, domainObjects, query.getSession());
                }
            }
        }

        return domainObjects;
    }

    /**
     * Build the primary key expression for the secondary table.
     */
    public Expression buildPrimaryKeyExpression(DatabaseTable table) throws DescriptorException {
        if (getDescriptor().getTables().firstElement().equals(table)) {
            return getPrimaryKeyExpression();
        }

        Map keyMapping = (Map)getDescriptor().getAdditionalTablePrimaryKeyFields().get(table);
        if (keyMapping == null) {
            throw DescriptorException.multipleTablePrimaryKeyNotSpecified(getDescriptor());
        }

        ExpressionBuilder builder = new ExpressionBuilder();
        Expression expression = null;
        for (Iterator primaryKeyEnum = keyMapping.values().iterator(); primaryKeyEnum.hasNext();) {
            DatabaseField field = (DatabaseField)primaryKeyEnum.next();
            expression = (builder.getField(field).equal(builder.getParameter(field))).and(expression);
        }

        return expression;
    }

    /**
     * Build the primary key expression from the specified primary key values.
     */
    public Expression buildPrimaryKeyExpressionFromKeys(Vector primaryKeyValues, AbstractSession session) {
        Expression expression = null;
        Expression subExpression;
        Expression builder = new ExpressionBuilder();
        List primaryKeyFields = getDescriptor().getPrimaryKeyFields();

        for (int index = 0; index < primaryKeyFields.size(); index++) {
            Object value = primaryKeyValues.get(index);
            DatabaseField field = (DatabaseField)primaryKeyFields.get(index);
            if (value != null) {
                subExpression = builder.getField(field).equal(value);
                expression = subExpression.and(expression);
            }
        }

        return expression;
    }

    /**
     * Build the primary key expression from the specified domain object.
     */
    public Expression buildPrimaryKeyExpressionFromObject(Object domainObject, AbstractSession session) {
        return buildPrimaryKeyExpressionFromKeys(extractPrimaryKeyFromObject(domainObject, session), session);
    }

    /**
     * Build the row representation of an object.
     */
    public AbstractRecord buildRow(Object object, AbstractSession session) {
        return buildRow(createRecord(), object, session);
    }

    /**
     * Build the row representation of an object.
     */
    public AbstractRecord buildRow(AbstractRecord databaseRow, Object object, AbstractSession session) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        int mappingsSize = mappings.size();
        for (int index = 0; index < mappingsSize; index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            mapping.writeFromObjectIntoRow(object, databaseRow, session);
        }

        // If this descriptor is involved in inheritence add the class type.
        if (getDescriptor().hasInheritance()) {
            getDescriptor().getInheritancePolicy().addClassIndicatorFieldToRow(databaseRow);
        }

        // If this descriptor has multiple tables then we need to append the primary keys for 
        // the non default tables.
        if (!getDescriptor().isAggregateDescriptor()) {
            addPrimaryKeyForNonDefaultTable(databaseRow);
        }

        return databaseRow;
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for non-relationship fields.
     */
    public AbstractRecord buildRowForShallowDelete(Object object, AbstractSession session) {
        return buildRowForShallowDelete(createRecord(), object, session);
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for non-relationship fields.
     */
    public AbstractRecord buildRowForShallowDelete(AbstractRecord databaseRow, Object object, AbstractSession session) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        int mappingsSize = mappings.size();
        for (int index = 0; index < mappingsSize; index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            mapping.writeFromObjectIntoRowForShallowDelete(object, databaseRow, session);
        }
        return databaseRow;
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildRowForShallowInsert(Object object, AbstractSession session) {
        return buildRowForShallowInsert(createRecord(), object, session);
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildRowForShallowInsert(AbstractRecord databaseRow, Object object, AbstractSession session) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        int mappingsSize = mappings.size();
        for (int index = 0; index < mappingsSize; index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            mapping.writeFromObjectIntoRowForShallowInsert(object, databaseRow, session);
        }

        // If this descriptor is involved in inheritence add the class type.
        if (getDescriptor().hasInheritance()) {
            getDescriptor().getInheritancePolicy().addClassIndicatorFieldToRow(databaseRow);
        }

        // If this descriptor has multiple tables then we need to append the primary keys for 
        // the non default tables.
        if (!getDescriptor().isAggregateDescriptor()) {
            addPrimaryKeyForNonDefaultTable(databaseRow);
        }

        return databaseRow;
    }

    /**
     * Build the row representation of an object.
     */
    public AbstractRecord buildRowWithChangeSet(ObjectChangeSet objectChangeSet, AbstractSession session) {
        return buildRowWithChangeSet(createRecord(), objectChangeSet, session);
    }

    /**
     * Build the row representation of an object.
     */
    public AbstractRecord buildRowWithChangeSet(AbstractRecord databaseRow, ObjectChangeSet objectChangeSet, AbstractSession session) {
        for (Enumeration changeRecords = objectChangeSet.getChanges().elements();
                 changeRecords.hasMoreElements();) {
            ChangeRecord changeRecord = (ChangeRecord)changeRecords.nextElement();
            DatabaseMapping mapping = changeRecord.getMapping();
            mapping.writeFromObjectIntoRowWithChangeRecord(changeRecord, databaseRow, session);
        }

        // If this descriptor is involved in inheritence add the class type.
        if (getDescriptor().hasInheritance()) {
            getDescriptor().getInheritancePolicy().addClassIndicatorFieldToRow(databaseRow);
        }

        return databaseRow;
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildRowForShallowInsertWithChangeSet(ObjectChangeSet objectChangeSet, AbstractSession session) {
        return buildRowForShallowInsertWithChangeSet(createRecord(), objectChangeSet, session);
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildRowForShallowInsertWithChangeSet(AbstractRecord databaseRow, ObjectChangeSet objectChangeSet, AbstractSession session) {
        for (Iterator changeRecords = objectChangeSet.getChanges().iterator();
                 changeRecords.hasNext();) {
            ChangeRecord changeRecord = (ChangeRecord)changeRecords.next();
            DatabaseMapping mapping = changeRecord.getMapping();
            mapping.writeFromObjectIntoRowForShallowInsertWithChangeRecord(changeRecord, databaseRow, session);
        }

        // If this descriptor is involved in inheritence add the class type.
        if (getDescriptor().hasInheritance()) {
            getDescriptor().getInheritancePolicy().addClassIndicatorFieldToRow(databaseRow);
        }

        // If this descriptor has multiple tables then we need to append the primary keys for 
        // the non default tables.
        if (!getDescriptor().isAggregateDescriptor()) {
            addPrimaryKeyForNonDefaultTable(databaseRow);
        }

        return databaseRow;
    }

    /**
     * Build the row representation of an object. The row built is used only for translations
     * for the expressions in the expresion framework.
     */
    public AbstractRecord buildRowForTranslation(Object object, AbstractSession session) {
        AbstractRecord databaseRow = createRecord();

        for (Iterator mappings = getPrimaryKeyMappings().iterator(); mappings.hasNext();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            if (mapping != null) {
                mapping.writeFromObjectIntoRow(object, databaseRow, session);
            }
        }

        // If this descriptor has multiple tables then we need to append the primary keys for 
        // the non default tables, this is require for m-m, dc defined in the Builder that prefixes the wrong table name.
        // Ideally the mappings should take part in building the translation row so they can add required values.
        addPrimaryKeyForNonDefaultTable(databaseRow, object, session);

        return databaseRow;
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildRowForUpdate(WriteObjectQuery query) {
        AbstractRecord databaseRow = createRecord();

        for (Iterator mappings = getNonPrimaryKeyMappings().iterator();
                 mappings.hasNext();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            mapping.writeFromObjectIntoRowForUpdate(query, databaseRow);
        }

        // If this descriptor is involved in inheritence and is an Aggregate, add the class type.
        // Added Nov 8, 2000 Mostly by PWK but also JED
        // Prs 24801
        // Modified  Dec 11, 2000 TGW with assitance from PWK
        // Prs 27554
        if (getDescriptor().hasInheritance() && getDescriptor().isAggregateDescriptor()) {
            if (query.getObject() != null) {
                if (query.getBackupClone() == null) {
                    getDescriptor().getInheritancePolicy().addClassIndicatorFieldToRow(databaseRow);
                } else {
                    if (!query.getObject().getClass().equals(query.getBackupClone().getClass())) {
                        getDescriptor().getInheritancePolicy().addClassIndicatorFieldToRow(databaseRow);
                    }
                }
            }
        }

        return databaseRow;
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildRowForUpdateWithChangeSet(WriteObjectQuery query) {
        AbstractRecord databaseRow = createRecord();

        for (Iterator changeRecords = query.getObjectChangeSet().getChanges().iterator();
                 changeRecords.hasNext();) {
            ChangeRecord changeRecord = (ChangeRecord)changeRecords.next();
            DatabaseMapping mapping = changeRecord.getMapping();
            mapping.writeFromObjectIntoRowWithChangeRecord(changeRecord, databaseRow, query.getSession());
        }

        return databaseRow;
    }

    /**
     * Build the row representation of an object.
     */
    public AbstractRecord buildRowForWhereClause(ObjectLevelModifyQuery query) {
        AbstractRecord databaseRow = createRecord();

        for (Iterator mappings = getDescriptor().getMappings().iterator();
                 mappings.hasNext();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            mapping.writeFromObjectIntoRowForWhereClause(query, databaseRow);
        }

        // If this descriptor has multiple tables then we need to append the primary keys for 
        // the non default tables.
        if (!getDescriptor().isAggregateDescriptor()) {
            addPrimaryKeyForNonDefaultTable(databaseRow);
        }

        return databaseRow;
    }

    /**
     * Build the row from the primary key values.
     */
    public AbstractRecord buildRowFromPrimaryKeyValues(Vector key, AbstractSession session) {
        AbstractRecord databaseRow = createRecord(key.size());
        int keySize = key.size();
        for (int index = 0; index < keySize; index++) {
            DatabaseField field = (DatabaseField)getDescriptor().getPrimaryKeyFields().get(index);
            Object value = key.elementAt(index);
            value = session.getPlatform(getDescriptor().getJavaClass()).getConversionManager().convertObject(value, field.getType());
            databaseRow.put(field, value);
        }

        return databaseRow;
    }

    /**
     * Build the row of all of the fields used for insertion.
     */
    public AbstractRecord buildTemplateInsertRow(AbstractSession session) {
        AbstractRecord databaseRow = createRecord();
        buildTemplateInsertRow(session, databaseRow);
        return databaseRow;
    }

    public void buildTemplateInsertRow(AbstractSession session, AbstractRecord databaseRow) {
        for (Iterator mappings = getDescriptor().getMappings().iterator();
                 mappings.hasNext();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            mapping.writeInsertFieldsIntoRow(databaseRow, session);
        }

        // If this descriptor is involved in inheritence add the class type.
        if (getDescriptor().hasInheritance()) {
            getDescriptor().getInheritancePolicy().addClassIndicatorFieldToInsertRow(databaseRow);
        }

        // If this descriptor has multiple tables then we need to append the primary keys for 
        // the non default tables.
        if (!getDescriptor().isAggregateDescriptor()) {
            addPrimaryKeyForNonDefaultTable(databaseRow);
        }

        if (getDescriptor().usesOptimisticLocking()) {
            getDescriptor().getOptimisticLockingPolicy().addLockFieldsToUpdateRow(databaseRow, session);
        }

        //** sequencing refactoring
        if (getDescriptor().usesSequenceNumbers() && session.getSequencing().shouldAcquireValueAfterInsert(getDescriptor().getJavaClass())) {
            databaseRow.remove(getDescriptor().getSequenceNumberField());
        }
    }

    /**
     * Build the row representation of the object for update. The row built does not
     * contain entries for uninstantiated attributes.
     */
    public AbstractRecord buildTemplateUpdateRow(AbstractSession session) {
        AbstractRecord databaseRow = createRecord();

        for (Iterator mappings = getNonPrimaryKeyMappings().iterator();
                 mappings.hasNext();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.next();
            mapping.writeUpdateFieldsIntoRow(databaseRow, session);
        }

        if (getDescriptor().usesOptimisticLocking()) {
            getDescriptor().getOptimisticLockingPolicy().addLockFieldsToUpdateRow(databaseRow, session);
        }

        return databaseRow;
    }

    /**
     * Build and return the expression to use as the where clause to an update object.
     * The row is passed to allow the version number to be extracted from it.
     */
    public Expression buildUpdateExpression(DatabaseTable table, AbstractRecord transactionRow, AbstractRecord modifyRow) {
        // Only the first table must use the lock check.
        Expression primaryKeyExpression = buildPrimaryKeyExpression(table);
        if (getDescriptor().usesOptimisticLocking()) {
            return getDescriptor().getOptimisticLockingPolicy().buildUpdateExpression(table, primaryKeyExpression, transactionRow, modifyRow);
        } else {
            return primaryKeyExpression;
        }
    }

    /**
     * INTERNAL:
     * Build just the primary key mappings into the object.
     */
    public void buildPrimaryKeyAttributesIntoObject(Object original, AbstractRecord databaseRow, ObjectBuildingQuery query) throws DatabaseException, QueryException {
        AbstractSession executionSession = query.getSession().getExecutionSession(query);

        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getPrimaryKeyMappings();
        int mappingsSize = mappings.size();
        for (int i = 0; i < mappingsSize; i++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(i);
            mapping.buildShallowOriginalFromRow(databaseRow, original, query, executionSession);
        }
    }

    /**
     * INTERNAL:
     * For reading through the write connection when in transaction,
     * We need a partially populated original, so that we
     * can build a clone using the copy policy, even though we can't
     * put this original in the shared cache yet; just build a
     * shallow original (i.e. just enough to copy over the primary
     * key and some direct attributes) and keep it on the UOW.
     */
    public void buildAttributesIntoShallowObject(Object original, AbstractRecord databaseRow, ObjectBuildingQuery query) throws DatabaseException, QueryException {
        AbstractSession executionSession = query.getSession().getExecutionSession(query);

        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector pkMappings = getPrimaryKeyMappings();
        int mappingsSize = pkMappings.size();
        for (int i = 0; i < mappingsSize; i++) {
            DatabaseMapping mapping = (DatabaseMapping)pkMappings.get(i);

            //if (query.shouldReadMapping(mapping)) {
            if (!mapping.isDirectToFieldMapping()) {
                mapping.buildShallowOriginalFromRow(databaseRow, original, query, executionSession);
            }
        }
        Vector mappings = getDescriptor().getMappings();
        mappingsSize = mappings.size();
        for (int i = 0; i < mappingsSize; i++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(i);

            //if (query.shouldReadMapping(mapping)) {
            if (mapping.isDirectToFieldMapping()) {
                mapping.buildShallowOriginalFromRow(databaseRow, original, query, executionSession);
            }
        }
    }

    /**
     * INTERNAL:
     * For reading through the write connection when in transaction,
     * populate the clone directly from the database row.
     */
    public void buildAttributesIntoWorkingCopyClone(Object clone, ObjectBuildingQuery query, JoinedAttributeManager joinManager, AbstractRecord databaseRow, UnitOfWorkImpl unitOfWork, boolean forRefresh) throws DatabaseException, QueryException {
        AbstractSession executionSession = unitOfWork;
        //execution session is UnitOfWork as these objects are being built within
        //the unit of work
        Vector mappings = getDescriptor().getMappings();
        int mappingsSize = mappings.size();
        for (int i = 0; i < mappingsSize; i++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(i);
            if (query.shouldReadMapping(mapping)) {
                mapping.buildCloneFromRow(databaseRow, joinManager, clone, query, unitOfWork, executionSession);
            }
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            // Need to run post build or refresh selector, currently check with the query for this,
            // I'm not sure which should be called it case of refresh building a new object, currently refresh is used...
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(clone);
            event.setQuery(query);
            event.setSession(query.getSession());
            event.setRecord(databaseRow);
            if (forRefresh) {
                event.setEventCode(DescriptorEventManager.PostRefreshEvent);
            } else {
                event.setEventCode(DescriptorEventManager.PostBuildEvent);
            }
            getDescriptor().getEventManager().executeEvent(event);
        }
    }

    /**
     * INTERNAL:
     * Builds a working copy clone directly from the database row.
     * This is the key method that allows us to execute queries against a
     * UnitOfWork while in transaction and not cache the results in the shared
     * cache.  This is because we might violate transaction isolation by
     * putting uncommitted versions of objects in the shared cache.
     */
    protected Object buildWorkingCopyCloneFromRow(ObjectBuildingQuery query, JoinedAttributeManager joinManager, AbstractRecord databaseRow, UnitOfWorkImpl unitOfWork, Vector primaryKey) throws DatabaseException, QueryException {
        // If the clone already exists then it may only need to be refreshed or returned.
        // We call directly on the identity map to avoid going to the parent,
        // registering if found, and wrapping the result.
        Object workingClone = unitOfWork.getIdentityMapAccessorInstance().getIdentityMapManager().getFromIdentityMap(primaryKey, getDescriptor().getJavaClass(), getDescriptor());

        // If there is a clone, and it is not a refresh then just return it.
        boolean wasAClone = workingClone != null;
        boolean isARefresh = query.shouldRefreshIdentityMapResult() || (query.isLockQuery() && (!wasAClone || !query.isClonePessimisticLocked(workingClone, unitOfWork)));
        if (wasAClone && (!isARefresh)) {
            return workingClone;
        }

        boolean wasAnOriginal = false;
        Object original = null;

        // If not refreshing can get the object from the cache.
        if (!isARefresh && !unitOfWork.shouldReadFromDB()) {
            CacheKey originalCacheKey = unitOfWork.getParentIdentityMapSession(query).getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, getDescriptor().getJavaClass(), getDescriptor());
            if (originalCacheKey != null) {
                //bug 4772232- acquire readlock on cachekey then release to ensure object is fully built before being returned
                try {
                    originalCacheKey.acquireReadLock();
                    original = originalCacheKey.getObject();
                } finally {
                    originalCacheKey.releaseReadLock();
                }
                wasAnOriginal = original != null;
                // If the original is invalid or always refresh then need to refresh.
                isARefresh = wasAnOriginal && (getDescriptor().shouldAlwaysRefreshCache() || getDescriptor().getCacheInvalidationPolicy().isInvalidated(originalCacheKey, query.getExecutionTime()));
                // Otherwise can just register the cached original object and return it.
                if (wasAnOriginal && (!isARefresh)) {
                    return unitOfWork.cloneAndRegisterObject(original, originalCacheKey);
                }
            }
        }

        CacheKey unitOfWorkCacheKey = null;
        if (!wasAClone) {
            // This code is copied from UnitOfWork.cloneAndRegisterObject.  Unlike
            // that method we don't need to lock the shared cache, because
            // are not building off of an original in the shared cache.
            // The copy policy is easier to invoke if we have an original.
            if (wasAnOriginal) {
                workingClone = instantiateWorkingCopyClone(original, unitOfWork);
                // intentionally put nothing in clones to originals, unless really was one.
                unitOfWork.getCloneToOriginals().put(workingClone, original);
            } else {
                // What happens if a copy policy is defined is not pleasant.
                workingClone = instantiateWorkingCopyCloneFromRow(databaseRow, query);
            }

            // This must be registered before it is built to avoid cycles.
            // The version and read is set below in copyQueryInfoToCacheKey.
            unitOfWorkCacheKey = unitOfWork.getIdentityMapAccessorInstance().internalPutInIdentityMap(workingClone, primaryKey, null, 0, getDescriptor());

            // This must be registered before it is built to avoid cycles.
            unitOfWork.getCloneMapping().put(workingClone, workingClone);
        }

        // Since there is no original cache key, we may need all the
        // info for the shared cache key in the UnitOfWork cache key.
        // PERF: Only lookup cache-key if did not just put it there.
        if (unitOfWorkCacheKey == null) {
            unitOfWorkCacheKey = unitOfWork.getIdentityMapAccessorInstance().getCacheKeyForObject(primaryKey, getDescriptor().getJavaClass(), getDescriptor());
        }

        // Must avoid infinite loops while refreshing.
        if (wasAClone && (unitOfWorkCacheKey.getLastUpdatedQueryId() >= query.getQueryId())) {
            return workingClone;
        }
        copyQueryInfoToCacheKey(unitOfWorkCacheKey, query, databaseRow, unitOfWork, getDescriptor());

        // If it was a clone the change listener must be cleared after.
        if (!wasAClone) {
            // The change listener must be set before building the clone as aggregate/collections need the listener.
            descriptor.getObjectChangePolicy().setChangeListener(workingClone, unitOfWork, getDescriptor());
        }

        // Turn it 'off' to prevent unwanted events.
        descriptor.getObjectChangePolicy().dissableEventProcessing(workingClone);
        // Build/refresh the clone from the row.
        buildAttributesIntoWorkingCopyClone(workingClone, query, joinManager, databaseRow, unitOfWork, wasAClone);
        Object backupClone = getDescriptor().getObjectChangePolicy().buildBackupClone(workingClone, this, unitOfWork);

        // If it was a clone the change listener must be cleared.
        if (wasAClone) {
            descriptor.getObjectChangePolicy().clearChanges(workingClone, unitOfWork, getDescriptor());
        }
        descriptor.getObjectChangePolicy().enableEventProcessing(workingClone);
        unitOfWork.getCloneMapping().put(workingClone, backupClone);
        query.recordCloneForPessimisticLocking(workingClone, unitOfWork);

        return workingClone;
    }

    /**
     * Returns clone of itself
     */
    public Object clone() {
        Object object = null;

        try {
            object = super.clone();
        } catch (Exception exception) {
            ;
        }

        // Only the shallow copy is created. The entries never change in these data structures
        ((ObjectBuilder)object).setMappingsByAttribute(new HashMap(getMappingsByAttribute()));
        ((ObjectBuilder)object).setMappingsByField(new HashMap(getMappingsByField()));
        ((ObjectBuilder)object).setReadOnlyMappingsByField(new HashMap(getReadOnlyMappingsByField()));
        ((ObjectBuilder)object).setPrimaryKeyMappings((Vector)getPrimaryKeyMappings().clone());
        ((ObjectBuilder)object).setNonPrimaryKeyMappings((Vector)getNonPrimaryKeyMappings().clone());

        return object;
    }

    /**
     * INTERNAL:
     * THis method is used by the UnitOfWork to cascade registration of new objects.
     * It may rais exceptions as described inthe EJB 3.x specification
     */
    public void cascadePerformRemove(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        Iterator mappings = getDescriptor().getMappings().iterator();
        while (mappings.hasNext()) {
            ((DatabaseMapping)mappings.next()).cascadePerformRemoveIfRequired(object, uow, visitedObjects);
        }
    }

    /**
     * INTERNAL:
     * THis method is used by the UnitOfWork to cascade registration of new objects.
     * It may rais exceptions as described inthe EJB 3.x specification
     */
    public void cascadeRegisterNewForCreate(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        Iterator mappings = getDescriptor().getMappings().iterator();
        while (mappings.hasNext()) {
            ((DatabaseMapping)mappings.next()).cascadeRegisterNewIfRequired(object, uow, visitedObjects);
        }
    }

    /**
     * INTERNAL:
     * This method creates an records changes for a particular object
     * @return ChangeRecord
     */
    public ObjectChangeSet compareForChange(Object clone, Object backUp, UnitOfWorkChangeSet changeSet, AbstractSession session) {
        // delegate the change comparision to this objects ObjectChangePolicy - TGW
        return descriptor.getObjectChangePolicy().calculateChanges(clone, backUp, changeSet, session, getDescriptor(), true);
    }

    /**
     * Compares the two specified objects
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);

            if (!mapping.compareObjects(firstObject, secondObject, session)) {
                Object firstValue = mapping.getAttributeValueFromObject(firstObject);
                Object secondValue = mapping.getAttributeValueFromObject(secondObject);
                session.log(SessionLog.FINEST, SessionLog.QUERY, "compare_failed", mapping, firstValue, secondValue);
                return false;
            }
        }

        return true;
    }

    /**
     * Copy each attribute from one object into the other.
     */
    public void copyInto(Object source, Object target, boolean cloneOneToOneValueHolders) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            Object value = null;
            if (cloneOneToOneValueHolders && mapping.isForeignReferenceMapping()){
                value = ((ForeignReferenceMapping)mapping).getAttributeValueWithClonedValueHolders(source);
            } else {
                value = mapping.getAttributeValueFromObject(source);
            }
            mapping.setAttributeValueInObject(target, value);
        }
    }
    
    /**
     * Copy each attribute from one object into the other.
     */
    public void copyInto(Object source, Object target) {
        copyInto(source, target, false);
    }
    

    /**
     * Return a copy of the object.
     * This is NOT used for unit of work but for templatizing an object.
     * The depth and primary key reseting are passed in.
     */
    public Object copyObject(Object original, ObjectCopyingPolicy policy) {
        Object copy = policy.getCopies().get(original);
        if (copy != null) {
            return copy;
        }

        copy = instantiateClone(original, policy.getSession());
        policy.getCopies().put(original, copy);

        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        List mappings = getCloningMappings();
        for (int index = 0; index < mappings.size(); index++) {
            ((DatabaseMapping)mappings.get(index)).buildCopy(copy, original, policy);
        }

        if (policy.shouldResetPrimaryKey() && (!(getDescriptor().isAggregateDescriptor() || getDescriptor().isAggregateCollectionDescriptor()))) {
            // Do not reset if any of the keys is mapped through a 1-1, i.e. back reference id has already changed.
            boolean hasOneToOne = false;
            for (Enumeration keyMappingsEnum = getPrimaryKeyMappings().elements();
                     keyMappingsEnum.hasMoreElements();) {
                if (((DatabaseMapping)keyMappingsEnum.nextElement()).isOneToOneMapping()) {
                    hasOneToOne = true;
                }
            }
            if (!hasOneToOne) {
                for (Enumeration keyMappingsEnum = getPrimaryKeyMappings().elements();
                         keyMappingsEnum.hasMoreElements();) {
                    DatabaseMapping mapping = (DatabaseMapping)keyMappingsEnum.nextElement();

                    // Only null out direct mappings, as others will be nulled in the respective objects.
                    if (mapping.isDirectToFieldMapping()) {
                        Object nullValue = ((AbstractDirectMapping)mapping).getAttributeValue(null, policy.getSession());
                        mapping.setAttributeValueInObject(copy, nullValue);
                    } else if (mapping.isTransformationMapping()) {
                        mapping.setAttributeValueInObject(copy, null);
                    }
                }
            }
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(copy);
            event.setSession(policy.getSession());
            event.setOriginalObject(original);
            event.setEventCode(DescriptorEventManager.PostCloneEvent);
            getDescriptor().getEventManager().executeEvent(event);
        }

        return copy;
    }

    /**
     * INTERNAL:
     * Used by the ObjectBuilder to create an ObjectChangeSet for the specified clone object
     * @return oracle.toplink.essentials.internal.sessions.ObjectChangeSet the newly created changeSet representing the clone object
     * @param clone java.lang.Object the object to convert to a changeSet
     * @param uowChangeSet oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet the owner of this changeSet
     */
    public ObjectChangeSet createObjectChangeSet(Object clone, UnitOfWorkChangeSet uowChangeSet, AbstractSession session) {
        boolean isNew = ((UnitOfWorkImpl)session).isObjectNew(clone);
        return createObjectChangeSet(clone, uowChangeSet, isNew, session);
    }

    /**
     * INTERNAL:
     * Used by the ObjectBuilder to create an ObjectChangeSet for the specified clone object
     * @return oracle.toplink.essentials.internal.sessions.ObjectChangeSet the newly created changeSet representing the clone object
     * @param clone java.lang.Object the object to convert to a changeSet
     * @param uowChangeSet oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet the owner of this changeSet
     * @param isNew boolean signifies if the clone object is a new object.
     */
    public ObjectChangeSet createObjectChangeSet(Object clone, UnitOfWorkChangeSet uowChangeSet, boolean isNew, AbstractSession session) {
        ObjectChangeSet changes = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(clone);
        if (changes == null) {
            if (getDescriptor().isAggregateDescriptor()) {
                changes = new AggregateObjectChangeSet(new Vector(0), getDescriptor().getJavaClass(), clone, uowChangeSet, isNew);
            } else {
                changes = new ObjectChangeSet(extractPrimaryKeyFromObject(clone, session), getDescriptor().getJavaClass(), clone, uowChangeSet, isNew);
            }
            changes.setIsAggregate(getDescriptor().isAggregateDescriptor() || getDescriptor().isAggregateCollectionDescriptor());
            uowChangeSet.addObjectChangeSetForIdentity(changes, clone);
        }
        return changes;
    }

    /**
     * Creates and stores primary key expression.
     */
    public void createPrimaryKeyExpression(AbstractSession session) {
        Expression expression = null;
        Expression builder = new ExpressionBuilder();
        Expression subExp1;
        Expression subExp2;
        Expression subExpression;
        List primaryKeyFields = getDescriptor().getPrimaryKeyFields();

        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField primaryKeyField = (DatabaseField)primaryKeyFields.get(index);
            subExp1 = builder.getField(primaryKeyField);
            subExp2 = builder.getParameter(primaryKeyField);
            subExpression = subExp1.equal(subExp2);

            if (expression == null) {
                expression = subExpression;
            } else {
                expression = expression.and(subExpression);
            }
        }

        setPrimaryKeyExpression(expression);
    }

    /**
     * Return the row with primary keys and their values from the given expression.
     */
    public Vector extractPrimaryKeyFromExpression(boolean requiresExactMatch, Expression expression, AbstractRecord translationRow, AbstractSession session) {
        AbstractRecord primaryKeyRow = createRecord(getPrimaryKeyMappings().size());

        //put the curent session onthe expression builder for use later store current session incase
        //it is required at a later stage
        AbstractSession oldSession = expression.getBuilder().getSession();
        expression.getBuilder().setSession(session.getRootSession(null));
        // Get all the field & values from expression.
        boolean isValid = expression.extractPrimaryKeyValues(requiresExactMatch, getDescriptor(), primaryKeyRow, translationRow);
        if (requiresExactMatch && (!isValid)) {
            return null;
        }

        // Check that the sizes match.
        if (primaryKeyRow.size() != getDescriptor().getPrimaryKeyFields().size()) {
            return null;
        }

        return extractPrimaryKeyFromRow(primaryKeyRow, session);
    }

    /**
     * Extract primary key attribute values from the domainObject.
     */
    public Vector extractPrimaryKeyFromObject(Object domainObject, AbstractSession session) {
        // Allow for inheritance, the concrete descriptor must always be used.
        if (getDescriptor().hasInheritance() && (domainObject.getClass() != getDescriptor().getJavaClass()) && (!domainObject.getClass().getSuperclass().equals(getDescriptor().getJavaClass()))) {
            return session.getDescriptor(domainObject).getObjectBuilder().extractPrimaryKeyFromObject(domainObject, session);
        } else {
            List primaryKeyFields = getDescriptor().getPrimaryKeyFields();
            Vector primaryKeyValues = new Vector(primaryKeyFields.size());

            // PERF: optimize simple case of direct mapped singleton primary key.
            if (getDescriptor().hasSimplePrimaryKey()) {
                // PERF: use index not enumeration			
                for (int index = 0; index < getPrimaryKeyMappings().size(); index++) {
                    AbstractDirectMapping mapping = (AbstractDirectMapping)getPrimaryKeyMappings().get(index);
                    Object keyValue = mapping.valueFromObject(domainObject, (DatabaseField)primaryKeyFields.get(index), session);
                    primaryKeyValues.add(keyValue);
                }
            } else {
                AbstractRecord databaseRow = createRecord(getPrimaryKeyMappings().size());

                // PERF: use index not enumeration			
                for (int index = 0; index < getPrimaryKeyMappings().size(); index++) {
                    DatabaseMapping mapping = (DatabaseMapping)getPrimaryKeyMappings().get(index);

                    // Primary key mapping may be null for aggregate collection.
                    if (mapping != null) {
                        mapping.writeFromObjectIntoRow(domainObject, databaseRow, session);
                    }
                }

                // PERF: use index not enumeration			
                for (int index = 0; index < primaryKeyFields.size(); index++) {
                    // Ensure that the type extracted from the object is the same type as in the descriptor,
                    // the main reason for this is that 1-1 can optimize on vh by getting from the row as the row-type.
                    Class classification = (Class)getPrimaryKeyClassifications().get(index);
                    Object value = databaseRow.get((DatabaseField)primaryKeyFields.get(index));

                    // CR2114 following line modified; domainObject.getClass() passed as an argument
                    primaryKeyValues.addElement(session.getPlatform(domainObject.getClass()).convertObject(value, classification));
                }
            }

            return primaryKeyValues;
        }
    }

    /**
     * Extract primary key values from the specified row.
     * null is returned if the row does not contain the key.
     */
    public Vector extractPrimaryKeyFromRow(AbstractRecord databaseRow, AbstractSession session) {
        List primaryKeyFields = getDescriptor().getPrimaryKeyFields();
        Vector primaryKeyValues = new Vector(primaryKeyFields.size());

        // PERF: use index not enumeration
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField field = (DatabaseField)primaryKeyFields.get(index);

            // Ensure that the type extracted from the row is the same type as in the object.
            Class classification = (Class)getPrimaryKeyClassifications().get(index);
            Object value = databaseRow.get(field);
            if (value != null) {
                primaryKeyValues.addElement(session.getPlatform(getDescriptor().getJavaClass()).convertObject(value, classification));
            } else {
                return null;
            }
        }

        return primaryKeyValues;
    }

    /**
     * Return the row with primary keys and their values from the given expression.
     */
    public AbstractRecord extractPrimaryKeyRowFromExpression(Expression expression, AbstractRecord translationRow, AbstractSession session) {
        AbstractRecord primaryKeyRow = createRecord(getPrimaryKeyMappings().size());

        //put the curent session onthe expression builder for use later store current session incase
        //it is required at a later stage
        AbstractSession oldSession = expression.getBuilder().getSession();
        expression.getBuilder().setSession(session.getRootSession(null));
        // Get all the field & values from expression	
        boolean isValid = expression.extractPrimaryKeyValues(true, getDescriptor(), primaryKeyRow, translationRow);
        expression.getBuilder().setSession(session.getRootSession(null));
        if (!isValid) {
            return null;
        }

        // Check that the sizes match up 
        if (primaryKeyRow.size() != getDescriptor().getPrimaryKeyFields().size()) {
            return null;
        }

        return primaryKeyRow;
    }

    /**
     * Extract primary key attribute values from the domainObject.
     */
    public AbstractRecord extractPrimaryKeyRowFromObject(Object domainObject, AbstractSession session) {
        AbstractRecord databaseRow = createRecord(getPrimaryKeyMappings().size());

        // PERF: use index not enumeration.
        for (int index = 0; index < getPrimaryKeyMappings().size(); index++) {
            ((DatabaseMapping)getPrimaryKeyMappings().get(index)).writeFromObjectIntoRow(domainObject, databaseRow, session);
        }

        // PERF: optimize simple primary key case, no need to remap.
        if (getDescriptor().hasSimplePrimaryKey()) {
            return databaseRow;
        }
        AbstractRecord primaryKeyRow = createRecord(getPrimaryKeyMappings().size());
        List primaryKeyFields = getDescriptor().getPrimaryKeyFields();
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            // Ensure that the type extracted from the object is the same type as in the descriptor,
            // the main reason for this is that 1-1 can optimize on vh by getting from the row as the row-type.
            Class classification = (Class)getPrimaryKeyClassifications().get(index);
            DatabaseField field = (DatabaseField)primaryKeyFields.get(index);
            Object value = databaseRow.get(field);
            primaryKeyRow.put(field, session.getPlatform(domainObject.getClass()).convertObject(value, classification));
        }

        return primaryKeyRow;
    }

    /**
     * Extract the value of the primary key attribute from the specified object.
     */
    public Object extractValueFromObjectForField(Object domainObject, DatabaseField field, AbstractSession session) throws DescriptorException {
        // Allow for inheritance, the concrete descriptor must always be used.
        ClassDescriptor descriptor = null;//this variable will be assigned in the final

        if (getDescriptor().hasInheritance() && (domainObject.getClass() != getDescriptor().getJavaClass()) && ((descriptor = session.getDescriptor(domainObject)).getJavaClass() != getDescriptor().getJavaClass())) {
            return descriptor.getObjectBuilder().extractValueFromObjectForField(domainObject, field, session);
        } else {
            DatabaseMapping mapping = getMappingForField(field);
            if (mapping == null) {
                throw DescriptorException.missingMappingForField(field, getDescriptor());
            }

            return mapping.valueFromObject(domainObject, field, session);
        }
    }

    /**
     * Return the base mapping for the given DatabaseField.
     */
    public DatabaseMapping getBaseMappingForField(DatabaseField databaseField) {
        DatabaseMapping mapping = getMappingForField(databaseField);

        // Drill down through the mappings until we get the direct mapping to the databaseField.	
        while (mapping.isAggregateObjectMapping()) {
            mapping = ((AggregateObjectMapping)mapping).getReferenceDescriptor().getObjectBuilder().getMappingForField(databaseField);
        }
        return mapping;
    }

    /**
     * Return the base value that is mapped to for given field.
     */
    public Object getBaseValueForField(DatabaseField databaseField, Object domainObject) {
        Object valueIntoObject = domainObject;
        DatabaseMapping mapping = getMappingForField(databaseField);

        // Drill down through the aggregate mappings to get to the direct to field mapping.
        while (mapping.isAggregateObjectMapping()) {
            valueIntoObject = mapping.getAttributeValueFromObject(valueIntoObject);
            mapping = ((AggregateMapping)mapping).getReferenceDescriptor().getObjectBuilder().getMappingForField(databaseField);
        }
        return mapping.getAttributeValueFromObject(valueIntoObject);
    }

    /**
     * Return the descriptor
     */
    public ClassDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * INTERNAL:
     * Return the classifiction for the field contained in the mapping.
     * This is used to convert the row value to a consistent java value.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) throws DescriptorException {
        DatabaseMapping mapping = getMappingForField(fieldToClassify);
        if (mapping == null) {
            // Means that the mapping is read-only or the classification is unknown,
            // this is normally not an issue as the classification is only really used for primary keys
            // and only when the database type can be different and not polymorphic than the object type.
            return null;
        }

        return mapping.getFieldClassification(fieldToClassify);
    }

    /**
     * Return the field used for the query key name.
     */
    public DatabaseField getFieldForQueryKeyName(String name) {
        QueryKey key = getDescriptor().getQueryKeyNamed(name);
        if (key == null) {
            DatabaseMapping mapping = getMappingForAttributeName(name);
            if (mapping == null) {
                return null;
            }
            if (mapping.getFields().isEmpty()) {
                return null;
            }
            return (DatabaseField)mapping.getFields().firstElement();
        }
        if (key.isDirectQueryKey()) {
            return ((DirectQueryKey)key).getField();
        }
        return null;
    }

    /**
     * PERF:
     * Return all mappings that require cloning.
     * This allows for simple directs to be avoided when using clone copying.
     */
    public List<DatabaseMapping> getCloningMappings() {
        return cloningMappings;
    }

    /**
     * INTERNAL:
     * Answers the attributes which are always joined to the original query on reads.
     */
    public Vector<String> getJoinedAttributes() {
        return joinedAttributes;
    }

    /**
     * INTERNAL:
     * Answers if any attributes are to be joined / returned in the same select
     * statement.
     */
    public boolean hasJoinedAttributes() {
        return (joinedAttributes != null);
    }

    /**
     * Return the mapping for the specified attribute name.
     */
    public DatabaseMapping getMappingForAttributeName(String name) {
        return getMappingsByAttribute().get(name);
    }

    /**
     * Return al the mapping for the specified field.
     */
    public DatabaseMapping getMappingForField(DatabaseField field) {
        return getMappingsByField().get(field);
    }

    /**
     * Return all the read-only mapping for the specified field.
     */
    public Vector<DatabaseMapping> getReadOnlyMappingsForField(DatabaseField field) {
        return getReadOnlyMappingsByField().get(field);
    }

    /**
     * Return all the mapping to attribute associations
     */
    protected Map<String, DatabaseMapping> getMappingsByAttribute() {
        return mappingsByAttribute;
    }

    /**
     * INTERNAL:
     * Return all the mapping to field associations
     */
    public Map<DatabaseField, DatabaseMapping> getMappingsByField() {
        return mappingsByField;
    }

    /**
     * INTERNAL:
     * Return all the read-only mapping to field associations
     */
    public Map<DatabaseField, Vector<DatabaseMapping>> getReadOnlyMappingsByField() {
        return readOnlyMappingsByField;
    }

    /**
     * Return the non primary key mappings.
     */
    protected Vector<DatabaseMapping> getNonPrimaryKeyMappings() {
        return nonPrimaryKeyMappings;
    }

    /**
     * Return the base value that is mapped to for given field.
     */
    public Object getParentObjectForField(DatabaseField databaseField, Object domainObject) {
        Object valueIntoObject = domainObject;
        DatabaseMapping mapping = getMappingForField(databaseField);

        // Drill down through the aggregate mappings to get to the direct to field mapping.
        while (mapping.isAggregateObjectMapping()) {
            valueIntoObject = mapping.getAttributeValueFromObject(valueIntoObject);
            mapping = ((AggregateMapping)mapping).getReferenceDescriptor().getObjectBuilder().getMappingForField(databaseField);
        }
        return valueIntoObject;
    }

    /**
     * Return primary key classifications.
     * These are used to ensure a consistent type for the pk values.
     */
    public Vector<Class> getPrimaryKeyClassifications() {
        if (primaryKeyClassifications == null) {
            List primaryKeyFields = getDescriptor().getPrimaryKeyFields();
            Vector<Class> classifications = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(primaryKeyFields.size());

            for (int index = 0; index < primaryKeyFields.size(); index++) {
                DatabaseMapping mapping = (DatabaseMapping)getPrimaryKeyMappings().get(index);
                DatabaseField field = (DatabaseField)primaryKeyFields.get(index);
                if (mapping != null) {
                    classifications.add(Helper.getObjectClass(mapping.getFieldClassification(field)));
                } else {
                    classifications.add(null);
                }
                primaryKeyClassifications = classifications;
            }
        }
        return primaryKeyClassifications;
    }

    /**
     * Return the primary key expression
     */
    public Expression getPrimaryKeyExpression() {
        return primaryKeyExpression;
    }

    /**
     * Return primary key mappings.
     */
    public Vector<DatabaseMapping> getPrimaryKeyMappings() {
        return primaryKeyMappings;
    }

    /**
     * INTERNAL: return a database field based on a query key name
     */
    public DatabaseField getTargetFieldForQueryKeyName(String queryKeyName) {
        DatabaseMapping mapping = (DatabaseMapping)getMappingForAttributeName(queryKeyName);
        if ((mapping != null) && mapping.isDirectToFieldMapping()) {
            return ((AbstractDirectMapping)mapping).getField();
        }

        //mapping is either null or not direct to field.
        //check query keys
        QueryKey queryKey = getDescriptor().getQueryKeyNamed(queryKeyName);
        if ((queryKey != null) && queryKey.isDirectQueryKey()) {
            return ((DirectQueryKey)queryKey).getField();
        }

        //nothing found
        return null;
    }
    
    /**
     * Cache all the mappings by their attribute and fields.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        this.getMappingsByField().clear();
        this.getReadOnlyMappingsByField().clear();
        this.getMappingsByAttribute().clear();
        this.getCloningMappings().clear();

        for (Enumeration mappings = getDescriptor().getMappings().elements(); mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();

            // Add attribute to mapping association
            if (!mapping.isWriteOnly()) {
                getMappingsByAttribute().put(mapping.getAttributeName(), mapping);
            }
            
            if (mapping.isCloningRequired()) {
                getCloningMappings().add(mapping);
            }

            // Add field to mapping association
            for (Enumeration fields = mapping.getFields().elements(); fields.hasMoreElements(); ) {
                DatabaseField field = DatabaseField.class.cast(fields.nextElement());

                if (mapping.isReadOnly()) {
                    Vector mappingVector = (Vector) getReadOnlyMappingsByField().get(field);
    
                    if (mappingVector == null) {
                        mappingVector = NonSynchronizedVector.newInstance();
                        getReadOnlyMappingsByField().put(field, mappingVector);
                    }
    
                    mappingVector.add(mapping);
                } else {
                    if (mapping.isAggregateObjectMapping()) {
                        // For Embeddable class, we need to test read-only 
                        // status of individual fields in the embeddable.
                        ObjectBuilder aggregateObjectBuilder = AggregateObjectMapping.class.cast(mapping).getReferenceDescriptor().getObjectBuilder();
                        
                        // Look in the non-read-only fields mapping
                        DatabaseMapping aggregatedFieldMapping = aggregateObjectBuilder.getMappingForField(field);
    
                        if (aggregatedFieldMapping == null) { // mapping must be read-only
                            Vector mappingVector = (Vector) getReadOnlyMappingsByField().get(field);
                            
                            if (mappingVector == null) {
                                mappingVector = NonSynchronizedVector.newInstance();
                                getReadOnlyMappingsByField().put(field, mappingVector);
                            }
    
                            mappingVector.add(mapping);
                        } else {
                            getMappingsByField().put(field, mapping);
                        }
                    } else { // Not an embeddable mapping
                        if (getMappingsByField().containsKey(field)) {  
                            session.getIntegrityChecker().handleError(DescriptorException.multipleWriteMappingsForField(field.toString(), mapping));
                        } else {
                            getMappingsByField().put(field, mapping);
                        }
                    }
                }
            }
        }

        initializePrimaryKey(session);

        initializeJoinedAttributes();
    }
    
    /**
     * INTERNAL:
     * Iterates through all one to one mappings and checks if any of them use joining.
     * <p>
     * By caching the result query execution in the case where there are no joined
     * attributes can be improved.
     */
    public void initializeJoinedAttributes() {
        // For concurrency don't worry about doing this work twice, just make sure
        // if it happens don't add the same joined attributes twice.
        Vector<String> joinedAttributes = null;
        Vector mappings = getDescriptor().getMappings();
        for (int i = 0; i < mappings.size(); i++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(i);
            if (mapping.isOneToOneMapping() && (mapping.isRelationalMapping()) && ((OneToOneMapping)mapping).shouldUseJoining()) {
                if (joinedAttributes == null) {
                    joinedAttributes = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
                }
                joinedAttributes.add(mapping.getAttributeName());
            }
        }
        this.joinedAttributes = joinedAttributes;
    }

    /**
     * Initialize a cache key.  Called by buildObject and now also by
     * buildWorkingCopyCloneFromRow.
     */
    protected void copyQueryInfoToCacheKey(CacheKey cacheKey, ObjectBuildingQuery query, AbstractRecord databaseRow, AbstractSession session, ClassDescriptor concreteDescriptor) {
        //CR #4365 - used to prevent infinit recursion on refresh object cascade all
        cacheKey.setLastUpdatedQueryId(query.getQueryId());

        if (concreteDescriptor.usesOptimisticLocking()) {
            OptimisticLockingPolicy policy = concreteDescriptor.getOptimisticLockingPolicy();
            Object cacheValue = policy.getValueToPutInCache(databaseRow, session);

            //register the object into the IM and set the write lock object
            cacheKey.setWriteLockValue(cacheValue);
        }
        cacheKey.setReadTime(query.getExecutionTime());
    }

    /**
     * Cache primary key and non primary key mappings.
     */
    public void initializePrimaryKey(AbstractSession session) throws DescriptorException {
        createPrimaryKeyExpression(session);

        List primaryKeyfields = getDescriptor().getPrimaryKeyFields();

        this.getPrimaryKeyMappings().clear();
        this.getNonPrimaryKeyMappings().clear();

        // This must be before because the scondary table primary key fields are registered after
        for (Iterator fields = getMappingsByField().keySet().iterator(); fields.hasNext();) {
            DatabaseField field = (DatabaseField)fields.next();
            if (!primaryKeyfields.contains(field)) {
                DatabaseMapping mapping = getMappingForField(field);
                if (!getNonPrimaryKeyMappings().contains(mapping)) {
                    getNonPrimaryKeyMappings().addElement(mapping);
                }
            }
        }
        List primaryKeyFields = getDescriptor().getPrimaryKeyFields();
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField primaryKeyField = (DatabaseField)primaryKeyFields.get(index);
            DatabaseMapping mapping = getMappingForField(primaryKeyField);

            if ((mapping == null) && (!getDescriptor().isAggregateDescriptor()) && (!getDescriptor().isAggregateCollectionDescriptor())) {
                throw DescriptorException.noMappingForPrimaryKey(primaryKeyField, getDescriptor());
            }

            getPrimaryKeyMappings().addElement(mapping);
            if (mapping != null) {
                mapping.setIsPrimaryKeyMapping(true);
            }

            // Use the same mapping to map the additional table primary key fields.
            // This is required if someone trys to map to one of these fields.
            if (getDescriptor().hasMultipleTables() && (mapping != null)) {
                for (Map keyMapping : getDescriptor().getAdditionalTablePrimaryKeyFields().values()) {
                    DatabaseField secondaryField = (DatabaseField) keyMapping.get(primaryKeyField);

                    // This can be null in the custom multiple join case
                    if (secondaryField != null) {
                        getMappingsByField().put(secondaryField, mapping);

                        if (mapping.isAggregateObjectMapping()) {
                            // GF#1153,1391
                            // If AggregateObjectMapping contain primary keys and the descriptor has multiple tables
                            // AggregateObjectMapping should know the the primary key join columns (secondaryField here)
                            // to handle some cases properly
                            ((AggregateObjectMapping) mapping).addPrimaryKeyJoinField(primaryKeyField, secondaryField);
                        }
                    }
                }
            }
        }

        // PERF: compute if primary key is mapped through direct mappings,
        // to allow fast extraction.
        boolean hasSimplePrimaryKey = true;
        for (int index = 0; index < getPrimaryKeyMappings().size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping)getPrimaryKeyMappings().get(index);

            // Primary key mapping may be null for aggregate collection.
            if ((mapping == null) || (!mapping.isDirectToFieldMapping())) {
                hasSimplePrimaryKey = false;
                break;
            }
        }
        getDescriptor().setHasSimplePrimaryKey(hasSimplePrimaryKey);
    }

    /**
     * Returns the clone of the specified object. This is called only from unit of work.
     * This only instatiates the clone instance, it does not clone the attributes,
     * this allows the stub of the clone to be registered before cloning its parts.
     */
    public Object instantiateClone(Object domainObject, AbstractSession session) {
        return getDescriptor().getCopyPolicy().buildClone(domainObject, session);
    }

    /**
     * Returns the clone of the specified object. This is called only from unit of work.
     * The domainObject sent as parameter is always a copy from the parent of unit of work.
     * bug 2612602 make a call to build a working clone.  This will in turn call the copy policy
     * to make a working clone.  This allows for lighter and heavier clones to
     * be created based on their use.
     * this allows the stub of the clone to be registered before cloning its parts.
     */
    public Object instantiateWorkingCopyClone(Object domainObject, AbstractSession session) {
        return getDescriptor().getCopyPolicy().buildWorkingCopyClone(domainObject, session);
    }

    /**
     * It is now possible to build working copy clones directly from rows.
     * <p>An intermediary original is no longer needed.
     * <p>This has ramifications to the copy policy and cmp, for clones are
     * no longer built via cloning.
     * <p>Instead the copy policy must in some cases not copy at all.
     * this allows the stub of the clone to be registered before cloning its parts.
     */
    public Object instantiateWorkingCopyCloneFromRow(AbstractRecord row, ObjectBuildingQuery query) {
        if (query.isObjectLevelReadQuery()){ //for backward compat reasons cast this
            return getDescriptor().getCopyPolicy().buildWorkingCopyCloneFromRow(row, ((ObjectLevelReadQuery)query));
        }else{
            return getDescriptor().getCopyPolicy().buildWorkingCopyCloneFromRow(row, query);
        }
    }

    public boolean isPrimaryKeyMapping(DatabaseMapping mapping) {
        return getPrimaryKeyMappings().contains(mapping);
    }

    /**
     * INTERNAL:
     * Perform the itteration opperation on the objects attributes through the mappings.
     */
    public void iterate(DescriptorIterator iterator) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        int mappingsSize = mappings.size();
        for (int index = 0; index < mappingsSize; index++) {
            ((DatabaseMapping)mappings.get(index)).iterate(iterator);
        }
    }

    /**
     * INTERNAL:
     * Merge changes between the objects, this merge algorthim is dependent on the merge manager.
     */
    public void mergeChangesIntoObject(Object target, ObjectChangeSet changeSet, Object source, MergeManager mergeManager) {
        for (Enumeration changes = changeSet.getChanges().elements(); changes.hasMoreElements();) {
            ChangeRecord record = (ChangeRecord)changes.nextElement();

            //cr 4236, use ObjectBuilder getMappingForAttributeName not the Descriptor one because the
            // ObjectBuilder method is much more efficient.
            DatabaseMapping mapping = getMappingForAttributeName(record.getAttribute());
            mapping.mergeChangesIntoObject(target, record, source, mergeManager);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(target);
            event.setSession(mergeManager.getSession());
            event.setOriginalObject(source);
            event.setChangeSet(changeSet);
            event.setEventCode(DescriptorEventManager.PostMergeEvent);
            getDescriptor().getEventManager().executeEvent(event);
        }
    }

    /**
     * INTERNAL:
     * Merge the contents of one object into another, this merge algorthim is dependent on the merge manager.
     * This merge also prevents the extra step of calculating the changes when it is not required.
     */
    public void mergeIntoObject(Object target, boolean isUnInitialized, Object source, MergeManager mergeManager) {
        mergeIntoObject(target, isUnInitialized, source, mergeManager, false);
    }
    
    /**
     * INTERNAL:
     * Merge the contents of one object into another, this merge algorthim is dependent on the merge manager.
     * This merge also prevents the extra step of calculating the changes when it is not required.
     * If 'cascadeOnly' is true, only foreign reference mappings are merged.
     */
    public void mergeIntoObject(Object target, boolean isUnInitialized, Object source, MergeManager mergeManager, boolean cascadeOnly) {
        // cascadeOnly is introduced to optimize merge 
        // for GF#1139 Cascade merge operations to relationship mappings even if already registered
        
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        Vector mappings = getDescriptor().getMappings();
        for (int index = 0; index < mappings.size(); index++) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.get(index);
            if(!cascadeOnly || mapping.isForeignReferenceMapping()){
                mapping.mergeIntoObject(target, isUnInitialized, source, mergeManager);
            }
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(target);
            event.setSession(mergeManager.getSession());
            event.setOriginalObject(source);
            event.setEventCode(DescriptorEventManager.PostMergeEvent);
            getDescriptor().getEventManager().executeEvent(event);
        }
    }

    /**
     * Clones the attributes of the specified object. This is called only from unit of work.
     * The domainObject sent as parameter is always a copy from the parent of unit of work.
     */
    public void populateAttributesForClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        // PERF: Avoid synchronized enumerator as is concurrency bottleneck.
        List mappings = getCloningMappings();
        for (int index = 0; index < mappings.size(); index++) {
                ((DatabaseMapping)mappings.get(index)).buildClone(original, clone, unitOfWork);
        }

        // PERF: Avoid events if no listeners.
        if (getDescriptor().getEventManager().hasAnyEventListeners()) {
            oracle.toplink.essentials.descriptors.DescriptorEvent event = new oracle.toplink.essentials.descriptors.DescriptorEvent(clone);
            event.setSession(unitOfWork);
            event.setOriginalObject(original);
            event.setEventCode(DescriptorEventManager.PostCloneEvent);
            getDescriptor().getEventManager().executeEvent(event);
        }
    }

    /**
     * Rehash any hashtables based on fields.
     * This is used to clone descriptors for aggregates, which hammer field names,
     * it is probably better not to hammer the field name and this should be refactored.
     */
    public void rehashFieldDependancies(AbstractSession session) {
        setMappingsByField(Helper.rehashMap(getMappingsByField()));
        setReadOnlyMappingsByField(Helper.rehashMap(getReadOnlyMappingsByField()));
        setPrimaryKeyMappings(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2));
        setNonPrimaryKeyMappings(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2));
        initializePrimaryKey(session);
    }

    /**
     * Set the descriptor.
     */
    public void setDescriptor(ClassDescriptor aDescriptor) {
        descriptor = aDescriptor;
    }

    /**
     * All the mappings and their respective attribute associations are cached for performance improvement.
     */
    protected void setMappingsByAttribute(Map<String, DatabaseMapping> theAttributeMappings) {
        mappingsByAttribute = theAttributeMappings;
    }

    /**
     * INTERNAL:
     * All the mappings and their respective field associations are cached for performance improvement.
     */
    public void setMappingsByField(Map<DatabaseField, DatabaseMapping> theFieldMappings) {
        mappingsByField = theFieldMappings;
    }

    /**
     * INTERNAL:
     * All the read-only mappings and their respective field associations are cached for performance improvement.
     */
    public void setReadOnlyMappingsByField(Map<DatabaseField, Vector<DatabaseMapping>> theReadOnlyFieldMappings) {
        readOnlyMappingsByField = theReadOnlyFieldMappings;
    }

    /**
     * The non primary key mappings are cached to improve performance.
     */
    protected void setNonPrimaryKeyMappings(Vector<DatabaseMapping> theNonPrimaryKeyMappings) {
        nonPrimaryKeyMappings = theNonPrimaryKeyMappings;
    }

    /**
     * Set primary key classifications.
     * These are used to ensure a consistent type for the pk values.
     */
    protected void setPrimaryKeyClassifications(Vector<Class> primaryKeyClassifications) {
        this.primaryKeyClassifications = primaryKeyClassifications;
    }

    /**
     * The primary key expression is cached to improve performance.
     */
    public void setPrimaryKeyExpression(Expression criteria) {
        primaryKeyExpression = criteria;
    }

    /**
     * The primary key mappings are cached to improve performance.
     */
    protected void setPrimaryKeyMappings(Vector<DatabaseMapping> thePrimaryKeyMappings) {
        primaryKeyMappings = thePrimaryKeyMappings;
    }

    public String toString() {
        return Helper.getShortClassName(getClass()) + "(" + getDescriptor().toString() + ")";
    }

    /**
     * Unwrap the object if required.
     * This is used for the wrapper policy support and EJB.
     */
    public Object unwrapObject(Object proxy, AbstractSession session) {
        if (proxy == null) {
            return null;
        }

        // Check if already unwrapped.
        if ((getDescriptor().getJavaClass() == proxy.getClass()) || !getDescriptor().hasWrapperPolicy() || !getDescriptor().getWrapperPolicy().isWrapped(proxy)) {
            return proxy;
        }

        // Allow for inheritance, the concrete wrapper must always be used.
        if (getDescriptor().hasInheritance() && (getDescriptor().getInheritancePolicy().hasChildren())) {
            ClassDescriptor descriptor = session.getDescriptor(proxy);
            if (descriptor != getDescriptor()) {
                return descriptor.getObjectBuilder().unwrapObject(proxy, session);
            }
        }

        if (getDescriptor().hasWrapperPolicy()) {
            return getDescriptor().getWrapperPolicy().unwrapObject(proxy, session);
        } else {
            return proxy;
        }
    }

    /**
     * Validates the object builder. This is done once the object builder initialized and descriptor
     * fires this validation.
     */
    public void validate(AbstractSession session) throws DescriptorException {
        if (getDescriptor().usesSequenceNumbers()) {
            if (getMappingForField(getDescriptor().getSequenceNumberField()) == null) {
                throw DescriptorException.mappingForSequenceNumberField(getDescriptor());
            }
        }
    }

    /**
     * Verify that an object has been deleted from the database.
     * An object can span multiple tables.  A query is performed on each of
     * these tables using the primary key values of the object as the selection
     * criteria.  If the query returns a result then the object has not been
     * deleted from the database.  If no result is returned then each of the
     * mappings is asked to verify that the object has been deleted. If all mappings
     * answer true then the result is true.
     */
    public boolean verifyDelete(Object object, AbstractSession session) {
        AbstractRecord translationRow = buildRowForTranslation(object, session);

        // If a call is used generated SQL cannot be executed, the call must be used.
        if ((getDescriptor().getQueryManager().getReadObjectQuery() != null) && getDescriptor().getQueryManager().getReadObjectQuery().isCallQuery()) {
            Object result = session.readObject(object);
            if (result != null) {
                return false;
            }
        } else {
            for (Enumeration tables = getDescriptor().getTables().elements();
                     tables.hasMoreElements();) {
                DatabaseTable table = (DatabaseTable)tables.nextElement();

                SQLSelectStatement sqlStatement = new SQLSelectStatement();
                sqlStatement.addTable(table);
                if (table == getDescriptor().getTables().firstElement()) {
                    sqlStatement.setWhereClause((Expression)getPrimaryKeyExpression().clone());
                } else {
                    sqlStatement.setWhereClause(buildPrimaryKeyExpression(table));
                }
                DatabaseField all = new DatabaseField("*");
                all.setTable(table);
                sqlStatement.addField(all);
                sqlStatement.normalize(session, null);

                DataReadQuery dataReadQuery = new DataReadQuery();
                dataReadQuery.setSQLStatement(sqlStatement);
                dataReadQuery.setSessionName(getDescriptor().getSessionName());

                // execute the query and check if there is a valid result
                Vector queryResults = (Vector)session.executeQuery(dataReadQuery, translationRow);
                if (!queryResults.isEmpty()) {
                    return false;
                }
            }
        }

        // now ask each of the mappings to verify that the object has been deleted.
        for (Enumeration mappings = getDescriptor().getMappings().elements();
                 mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();

            if (!mapping.verifyDelete(object, session)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Wrap the object if required.
     * This is used for the wrapper policy support and EJB.
     */
    public Object wrapObject(Object implementation, AbstractSession session) {
        if (implementation == null) {
            return null;
        }

        // Check if already wrapped.
        if (!getDescriptor().hasWrapperPolicy() || getDescriptor().getWrapperPolicy().isWrapped(implementation)) {
            return implementation;
        }

        // Allow for inheritance, the concrete wrapper must always be used.
        if (getDescriptor().hasInheritance() && getDescriptor().getInheritancePolicy().hasChildren() && (implementation.getClass() != getDescriptor().getJavaClass())) {
            ClassDescriptor descriptor = session.getDescriptor(implementation);
            if(descriptor != getDescriptor()) {
                return descriptor.getObjectBuilder().wrapObject(implementation, session);
            }
        }
        if (getDescriptor().hasWrapperPolicy()) {
            return getDescriptor().getWrapperPolicy().wrapObject(implementation, session);
        } else {
            return implementation;
        }
    }
}

