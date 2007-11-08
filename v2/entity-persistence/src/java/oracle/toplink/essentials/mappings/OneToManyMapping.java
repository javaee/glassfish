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
package oracle.toplink.essentials.mappings;

import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.indirection.IndirectContainer;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkChangeSet;
import oracle.toplink.essentials.internal.sessions.ObjectChangeSet;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: This mapping is used to represent the
 * typical RDBMS relationship between a single
 * source object and collection of target objects; where,
 * on the database, the target objects have references
 * (foreign keys) to the source object.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class OneToManyMapping extends CollectionMapping implements RelationalMapping {

    /** The target foreign key fields that reference the sourceKeyFields. */
    protected transient Vector<DatabaseField> targetForeignKeyFields;

    /** The (typically primary) source key fields that are referenced by the targetForeignKeyFields. */
    protected transient Vector<DatabaseField> sourceKeyFields;

    /** This maps the target foreign key fields to the corresponding (primary) source key fields. */
    protected transient Map<DatabaseField, DatabaseField> targetForeignKeysToSourceKeys;
    
    /** This maps the (primary) source key fields to the corresponding target foreign key fields. */
    protected transient Map<DatabaseField, DatabaseField> sourceKeysToTargetForeignKeys;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public OneToManyMapping() {
        super();

        this.targetForeignKeysToSourceKeys = new HashMap(2);
        this.sourceKeysToTargetForeignKeys = new HashMap(2);
        
        this.sourceKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.targetForeignKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);

        this.deleteAllQuery = new DeleteAllQuery();
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Add the associated fields to the appropriate collections.
     */
    public void addTargetForeignKeyField(DatabaseField targetForeignKeyField, DatabaseField sourceKeyField) {
        this.getTargetForeignKeyFields().addElement(targetForeignKeyField);
        this.getSourceKeyFields().addElement(sourceKeyField);
    }

    /**
     * PUBLIC:
     * Define the target foreign key relationship in the one-to-many mapping.
     * This method is used for composite target foreign key relationships.
     * That is, the target object's table has multiple foreign key fields
     * that are references to
     * the source object's (typically primary) key fields.
     * Both the target foreign key field name and the corresponding
     * source primary key field name must be specified.
     * Because the target object's table must store a foreign key to the source table,
     * the target object must map that foreign key, this is normally done through a
     * one-to-one mapping back-reference. Other options include:
     * <ul>
     * <li> use a DirectToFieldMapping and maintain the
     * foreign key fields directly in the target
     * <li> use a ManyToManyMapping
     * <li> use an AggregateCollectionMapping
     * </ul>
     * @see DirectToFieldMapping
     * @see ManyToManyMapping
     * @see AggregateCollectionMapping
     */
    public void addTargetForeignKeyFieldName(String targetForeignKeyFieldName, String sourceKeyFieldName) {
        this.addTargetForeignKeyField(new DatabaseField(targetForeignKeyFieldName), new DatabaseField(sourceKeyFieldName));
    }

    /**
     * The selection criteria are created with target foreign keys and source "primary" keys.
     * These criteria are then used to read the target records from the table.
     * These criteria are also used as the default "delete all" criteria.
     *
     * CR#3922 - This method is almost the same as buildSelectionCriteria() the difference
     * is that TargetForeignKeysToSourceKeys contains more information after login then SourceKeyFields
     * contains before login.
     */
    protected Expression buildDefaultSelectionCriteria() {
        Expression selectionCriteria = null;
        Expression builder = new ExpressionBuilder();

        for (Iterator keys = this.getTargetForeignKeysToSourceKeys().keySet().iterator();
                 keys.hasNext();) {
            DatabaseField targetForeignKey = (DatabaseField)keys.next();
            DatabaseField sourceKey = (DatabaseField)this.getTargetForeignKeysToSourceKeys().get(targetForeignKey);

            Expression partialSelectionCriteria = builder.getField(targetForeignKey).equal(builder.getParameter(sourceKey));
            selectionCriteria = partialSelectionCriteria.and(selectionCriteria);
        }
        return selectionCriteria;
    }
    
    /**
     * This method would allow customers to get the potential selection criteria for a mapping
     * prior to initialization.  This would allow them to more easily create an ammendment method
     * that would ammend the SQL for the join.
     *
     * CR#3922 - This method is almost the same as buildDefaultSelectionCriteria() the difference
     * is that TargetForeignKeysToSourceKeys contains more information after login then SourceKeyFields
     * contains before login.
     */
    public Expression buildSelectionCriteria() {
        //CR3922	
        Expression selectionCriteria = null;
        Expression builder = new ExpressionBuilder();

        Enumeration sourceKeys = this.getSourceKeyFields().elements();
        for (Enumeration targetForeignKeys = this.getTargetForeignKeyFields().elements();
                 targetForeignKeys.hasMoreElements();) {
            DatabaseField targetForeignKey = (DatabaseField)targetForeignKeys.nextElement();
            DatabaseField sourceKey = (DatabaseField)sourceKeys.nextElement();
            Expression partialSelectionCriteria = builder.getField(targetForeignKey).equal(builder.getParameter(sourceKey));
            selectionCriteria = partialSelectionCriteria.and(selectionCriteria);
        }
        return selectionCriteria;
    }

    /**
     * INTERNAL:
     * Clone the appropriate attributes.
     */
    public Object clone() {
        OneToManyMapping clone = (OneToManyMapping)super.clone();
        clone.setTargetForeignKeysToSourceKeys(new HashMap(this.getTargetForeignKeysToSourceKeys()));
        return clone;
    }

    /**
     * Delete all the reference objects with a single query.
     */
    protected void deleteAll(WriteObjectQuery query) throws DatabaseException {
        Object referenceObjects = null;
        if(usesIndirection()) {
           Object attribute = getAttributeAccessor().getAttributeValueFromObject(query.getObject());
           if(attribute == null || (ClassConstants.IndirectContainer_Class.isAssignableFrom(attribute.getClass()) && !((IndirectContainer)attribute).isInstantiated())) {
               // An empty Vector indicates to DeleteAllQuery that no objects should be removed from cache
               referenceObjects = new Vector(0);
           }
        }
        if(referenceObjects == null) {
            referenceObjects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        }

        ((DeleteAllQuery)this.getDeleteAllQuery()).executeDeleteAll(query.getSession().getSessionForClass(this.getReferenceClass()), query.getTranslationRow(), this.getContainerPolicy().vectorFor(referenceObjects, query.getSession()));
    }

    /**
     *    This method will make sure that all the records privately owned by this mapping are
     * actually removed. If such records are found then those are all read and removed one
     * by one along with their privately owned parts.
     */
    protected void deleteReferenceObjectsLeftOnDatabase(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        Object objects = this.readPrivateOwnedForObject(query);

        // Delete all these object one by one.
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
            query.getSession().deleteObject(cp.next(iter, query.getSession()));
        }
    }

    /**
     * Extract the foreign key value from the reference object.
     * Used for batch reading. Keep the fields in the same order
     * as in the targetForeignKeysToSourceKeys hashtable.
     */
    protected Vector extractForeignKeyFromReferenceObject(Object object, AbstractSession session) {
        Vector foreignKey = new Vector(this.getTargetForeignKeysToSourceKeys().size());

        for (Iterator stream = this.getTargetForeignKeysToSourceKeys().entrySet().iterator();
                 stream.hasNext();) {
            Map.Entry entry = (Map.Entry)stream.next();
            DatabaseField targetField = (DatabaseField)entry.getKey();
            DatabaseField sourceField = (DatabaseField)entry.getValue();
            if (object == null) {
                foreignKey.addElement(null);
            } else {
                Object value = this.getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(object, targetField, session);

                //CR:somenewsgroupbug need to ensure source and target types match.
                try {
                    value = session.getDatasourcePlatform().convertObject(value, getDescriptor().getObjectBuilder().getFieldClassification(sourceField));
                } catch (ConversionException e) {
                    throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
                }

                foreignKey.addElement(value);
            }
        }
        return foreignKey;
    }

    /**
     * Extract the key field values from the specified row.
     * Used for batch reading. Keep the fields in the same order
     * as in the targetForeignKeysToSourceKeys hashtable.
     */
    protected Vector extractKeyFromRow(AbstractRecord row, AbstractSession session) {
        Vector key = new Vector(this.getTargetForeignKeysToSourceKeys().size());

        for (Iterator stream = this.getTargetForeignKeysToSourceKeys().values().iterator();
                 stream.hasNext();) {
            DatabaseField field = (DatabaseField)stream.next();
            Object value = row.get(field);

            // Must ensure the classification to get a cache hit.
            try {
                value = session.getDatasourcePlatform().convertObject(value, getDescriptor().getObjectBuilder().getFieldClassification(field));
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
            }

            key.addElement(value);
        }
        return key;
    }

    /**
     * PUBLIC:
     * Return the source key field names associated with the mapping.
     * These are in-order with the targetForeignKeyFieldNames.
     */
    public Vector getSourceKeyFieldNames() {
        Vector fieldNames = new Vector(getSourceKeyFields().size());
        for (Enumeration fieldsEnum = getSourceKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * INTERNAL:
     * Return the source key fields.
     */
    public Vector<DatabaseField> getSourceKeyFields() {
        return sourceKeyFields;
    }
    
    /**
     * INTERNAL:
     * Return the source/target key fields.
     */
    public Map<DatabaseField, DatabaseField> getSourceKeysToTargetForeignKeys() {
        return sourceKeysToTargetForeignKeys;
    }

    /**
     * INTERNAL:
     * Return the target foreign key field names associated with the mapping.
     * These are in-order with the targetForeignKeyFieldNames.
     */
    public Vector getTargetForeignKeyFieldNames() {
        Vector fieldNames = new Vector(getTargetForeignKeyFields().size());
        for (Enumeration fieldsEnum = getTargetForeignKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * INTERNAL:
     * Return the target foreign key fields.
     */
    public Vector<DatabaseField> getTargetForeignKeyFields() {
        return targetForeignKeyFields;
    }

    /**
     * INTERNAL:
     * Return the target/source key fields.
     */
    public Map<DatabaseField, DatabaseField> getTargetForeignKeysToSourceKeys() {
        return targetForeignKeysToSourceKeys;
    }

    /**
     * INTERNAL:
     * Maintain for backward compatibility.
     * This is 'public' so StoredProcedureGenerator
     * does not have to use the custom query expressions.
     */
    public Map getTargetForeignKeyToSourceKeys() {
        return this.getTargetForeignKeysToSourceKeys();
    }

    /**
     * INTERNAL:
     * Return whether the mapping has any inverse constraint dependencies,
     * such as foreign keys and join tables.
     */
    public boolean hasInverseConstraintDependency() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        if (!this.isSourceKeySpecified()) {
            // sourceKeyFields will be empty when #setTargetForeignKeyFieldName() is used
            this.setSourceKeyFields(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(getDescriptor().getPrimaryKeyFields()));
        }
        this.initializeTargetForeignKeysToSourceKeys();

        if (this.shouldInitializeSelectionCriteria()) {
            this.setSelectionCriteria(this.buildDefaultSelectionCriteria());
        }

        this.initializeDeleteAllQuery();
    }

    /**
     * Initialize the delete all query.
     * This query is used to delete the collection of objects from the
     * database.
     */
    protected void initializeDeleteAllQuery() {
        ((DeleteAllQuery)this.getDeleteAllQuery()).setReferenceClass(this.getReferenceClass());
        if (!this.hasCustomDeleteAllQuery()) {
            // the selection criteria are re-used by the delete all query
            if (this.getSelectionCriteria() == null) {
                this.getDeleteAllQuery().setSelectionCriteria(this.buildDefaultSelectionCriteria());
            } else {
                this.getDeleteAllQuery().setSelectionCriteria(this.getSelectionCriteria());
            }
        }
    }

    /**
     * Verify, munge, and hash the target foreign keys and source keys.
     */
    protected void initializeTargetForeignKeysToSourceKeys() throws DescriptorException {
        if (this.getTargetForeignKeyFields().isEmpty()) {
            if (this.shouldInitializeSelectionCriteria()) {
                throw DescriptorException.noTargetForeignKeysSpecified(this);
            } else {
                // if they have specified selection criteria, the keys do not need to be specified
                return;
            }
        }

        if (this.getTargetForeignKeyFields().size() != this.getSourceKeyFields().size()) {
            throw DescriptorException.targetForeignKeysSizeMismatch(this);
        }

        for (Enumeration stream = this.getTargetForeignKeyFields().elements();
                 stream.hasMoreElements();) {
            this.getReferenceDescriptor().buildField((DatabaseField)stream.nextElement());
        }

        for (Enumeration keys = this.getSourceKeyFields().elements(); keys.hasMoreElements();) {
            this.getDescriptor().buildField((DatabaseField)keys.nextElement());
        }

        Enumeration<DatabaseField> targetForeignKeys = this.getTargetForeignKeyFields().elements();
        Enumeration<DatabaseField> sourceKeys = this.getSourceKeyFields().elements();
        while (targetForeignKeys.hasMoreElements()) {
            DatabaseField targetForeignKey = targetForeignKeys.nextElement();
            DatabaseField sourcePrimaryKey = sourceKeys.nextElement();
            this.getTargetForeignKeysToSourceKeys().put(targetForeignKey, sourcePrimaryKey);
            this.getSourceKeysToTargetForeignKeys().put(sourcePrimaryKey, targetForeignKey);
            
            //this.getTargetForeignKeysToSourceKeys().put(targetForeignKeys.nextElement(), sourceKeys.nextElement());
        }
    }

    /**
     * INTERNAL:
     */
    public boolean isOneToManyMapping() {
        return true;
    }

    /**
     * Return whether the source key is specified.
     * It will be empty when #setTargetForeignKeyFieldName(String) is used.
     */
    protected boolean isSourceKeySpecified() {
        return !this.getSourceKeyFields().isEmpty();
    }

    /**
     * Return whether the reference objects must be deleted
     * one by one, as opposed to with a single DELETE statement.
     */
    protected boolean mustDeleteReferenceObjectsOneByOne() {
        ClassDescriptor referenceDescriptor = this.getReferenceDescriptor();
        return referenceDescriptor.hasDependencyOnParts() || referenceDescriptor.usesOptimisticLocking() || (referenceDescriptor.hasInheritance() && referenceDescriptor.getInheritancePolicy().shouldReadSubclasses()) || referenceDescriptor.hasMultipleTables();
    }

    /**
     * INTERNAL:
     * Insert the referenced objects.
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        // Insertion takes place according the the cascading policy
        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        Object objects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        // insert each object one by one
        ContainerPolicy cp = this.getContainerPolicy();
        for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
            Object object = cp.next(iter, query.getSession());
            if (this.isPrivateOwned()) {
                // no need to set changeSet as insert is a straight copy
                InsertObjectQuery insertQuery = new InsertObjectQuery();
                insertQuery.setObject(object);
                insertQuery.setCascadePolicy(query.getCascadePolicy());
                query.getSession().executeQuery(insertQuery);
            } else {
                // This will happen in a unit of work or cascaded query.
                // This is done only for persistence by reachability and is not required if the targets are in the queue anyway
                // Avoid cycles by checking commit manager, this is allowed because there is no dependency.
                if (!query.getSession().getCommitManager().isCommitInPreModify(object)) {
                    ObjectChangeSet changeSet = null;
                    UnitOfWorkChangeSet uowChangeSet = null;
                    if (query.getSession().isUnitOfWork() && (((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet() != null)) {
                        uowChangeSet = (UnitOfWorkChangeSet)((UnitOfWorkImpl)query.getSession()).getUnitOfWorkChangeSet();
                        changeSet = (ObjectChangeSet)uowChangeSet.getObjectChangeSetForClone(object);
                    }
                    WriteObjectQuery writeQuery = new WriteObjectQuery();
                    writeQuery.setObject(object);
                    writeQuery.setObjectChangeSet(changeSet);
                    writeQuery.setCascadePolicy(query.getCascadePolicy());
                    query.getSession().executeQuery(writeQuery);
                }
            }
        }
    }

    /**
     * INTERNAL:
     * Update the reference objects.
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!this.shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // if the target objects are not instantiated, they could not have been changed....
        if (!this.isAttributeValueInstantiated(query.getObject())) {
            return;
        }

        // manage objects added and removed from the collection
        Object objectsInMemory = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        Object objectsInDB = this.readPrivateOwnedForObject(query);

        this.compareObjectsAndWrite(objectsInDB, objectsInMemory, query);
    }

    /**
     * INTERNAL:
     * Delete the referenced objects.
     */
    public void preDelete(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        // Deletion takes place according the the cascading policy
        if (!this.shouldObjectModifyCascadeToPartsForPreDelete(query)) {
            return;
        }

        // if referenced parts have their privately-owned sub-parts, delete them one by one;
        // else delete everything in one shot
        if (this.mustDeleteReferenceObjectsOneByOne() || query.shouldCascadeOnlyDependentParts()) {
            Object objects = this.getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
            ContainerPolicy cp = this.getContainerPolicy();
            for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
                Object object = cp.next(iter, query.getSession());
                // Make sure only objects sheduled for deletion are deleted
                if (shouldObjectDeleteCascadeToPart(query, object)) {
                    DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                    deleteQuery.setObject(object);
                    deleteQuery.setCascadePolicy(query.getCascadePolicy());
                    query.getSession().executeQuery(deleteQuery);
                }
            }
            if (!query.getSession().isUnitOfWork()) {
                // This deletes any objects on the database, as the collection in memory may have been changed.
                // This is not required for unit of work, as the update would have already deleted these objects,
                // and the backup copy will include the same objects causing double deletes.
                this.deleteReferenceObjectsLeftOnDatabase(query);
            }
        } else {
            this.deleteAll(query);
        }
    }

    /**
     * PUBLIC:
     * Set the SQL string used by the mapping to delete the target objects.
     * This allows the developer to override the SQL
     * generated by TopLink with a custom SQL statement or procedure call.
     * The arguments are
     * translated from the fields of the source row, by replacing the field names
     * marked by '#' with the values for those fields at execution time.
     * A one-to-many mapping will only use this delete all optimization if the target objects
     * can be deleted in a single SQL call. This is possible when the target objects
     * are in a single table, do not using locking, do not contain other privately-owned
     * parts, do not read subclasses, etc.
     * <p>
     * Example: "delete from PHONE where OWNER_ID = #EMPLOYEE_ID"
     */
    public void setDeleteAllSQLString(String sqlString) {
        DeleteAllQuery query = new DeleteAllQuery();
        query.setSQLString(sqlString);
        setCustomDeleteAllQuery(query);
    }

    /**
     * INTERNAL:
     * Set the source key field names associated with the mapping.
     * These must be in-order with the targetForeignKeyFieldNames.
     */
    public void setSourceKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setSourceKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Set the source key fields.
     */
    public void setSourceKeyFields(Vector<DatabaseField> sourceKeyFields) {
        this.sourceKeyFields = sourceKeyFields;
    }

    /**
     * PUBLIC:
     * Define the target foreign key relationship in the one-to-many mapping.
     * This method can be used when the foreign and primary keys
     * have only a single field each.
     * (Use #addTargetForeignKeyFieldName(String, String)
     * for "composite" keys.)
     * Only the target foreign key field name is specified and the source
     * (primary) key field is
     * assumed to be the primary key of the source object.
     * Because the target object's table must store a foreign key to the source table,
     * the target object must map that foreign key, this is normally done through a
     * one-to-one mapping back-reference. Other options include:
     * <ul>
     * <li> use a DirectToFieldMapping and maintain the
     * foreign key fields directly in the target
     * <li> use a ManyToManyMapping
     * <li> use an AggregateCollectionMapping
     * </ul>
     * @see DirectToFieldMapping
     * @see ManyToManyMapping
     * @see AggregateCollectionMapping
     */
    public void setTargetForeignKeyFieldName(String targetForeignKeyFieldName) {
        this.getTargetForeignKeyFields().addElement(new DatabaseField(targetForeignKeyFieldName));
    }

    /**
     * PUBLIC:
     * Define the target foreign key relationship in the one-to-many mapping.
     * This method is used for composite target foreign key relationships.
     * That is, the target object's table has multiple foreign key fields to
     * the source object's (typically primary) key fields.
     * Both the target foreign key field names and the corresponding source primary
     * key field names must be specified.
     */
    public void setTargetForeignKeyFieldNames(String[] targetForeignKeyFieldNames, String[] sourceKeyFieldNames) {
        if (targetForeignKeyFieldNames.length != sourceKeyFieldNames.length) {
            throw DescriptorException.targetForeignKeysSizeMismatch(this);
        }
        for (int i = 0; i < targetForeignKeyFieldNames.length; i++) {
            this.addTargetForeignKeyFieldName(targetForeignKeyFieldNames[i], sourceKeyFieldNames[i]);
        }
    }

    /**
     * INTERNAL:
     * Set the target key field names associated with the mapping.
     * These must be in-order with the sourceKeyFieldNames.
     */
    public void setTargetForeignKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setTargetForeignKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Set the target fields.
     */
    public void setTargetForeignKeyFields(Vector<DatabaseField> targetForeignKeyFields) {
        this.targetForeignKeyFields = targetForeignKeyFields;
    }

    /**
     * INTERNAL:
     * Set the target fields.
     */
    protected void setTargetForeignKeysToSourceKeys(Map<DatabaseField, DatabaseField> targetForeignKeysToSourceKeys) {
        this.targetForeignKeysToSourceKeys = targetForeignKeysToSourceKeys;
    }

    /**
     * Return whether any process leading to object modification
     * should also affect its parts.
     * Used by write, insert, update, and delete.
     */
    protected boolean shouldObjectModifyCascadeToParts(ObjectLevelModifyQuery query) {
        if (this.isReadOnly()) {
            return false;
        }

        if (this.isPrivateOwned()) {
            return true;
        }

        return query.shouldCascadeAllParts();
    }    
    
    /**
     * INTERNAL
     * Return true if this mapping supports cascaded version optimistic locking.
     */
    public boolean isCascadedLockingSupported() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Return if this mapping support joining.
     */
    public boolean isJoiningSupported() {
        return true;
    }

    /**
     * INTERNAL:
     * Used to verify whether the specified object is deleted or not.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        if (this.isPrivateOwned()) {
            Object objects = this.getRealCollectionAttributeValueFromObject(object, session);

            ContainerPolicy containerPolicy = getContainerPolicy();
            for (Object iter = containerPolicy.iteratorFor(objects); containerPolicy.hasNext(iter);) {
                if (!session.verifyDelete(containerPolicy.next(iter, session))) {
                    return false;
                }
            }
        }
        return true;
    }
}
