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
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Many to many mappings are used to represent the relationships
 * between a collection of source objects and a collection of target objects.
 * The mapping require the creation of an intermediate table for managing the
 * associations between the source and target records.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class ManyToManyMapping extends CollectionMapping implements RelationalMapping {

    /** Used for data modification events. */
    protected static final String PostInsert = "postInsert";
    protected static final String ObjectRemoved = "objectRemoved";
    protected static final String ObjectAdded = "objectAdded";

    /** The intermediate relation table. */
    protected transient DatabaseTable relationTable;

    /** The field in the source table that corresponds to the key in the relation table */
    protected transient Vector<DatabaseField> sourceKeyFields;

    /**  The field in the target table that corresponds to the key in the relation table */
    protected transient Vector<DatabaseField> targetKeyFields;

    /** The field in the intermediate table that corresponds to the key in the source table */
    protected transient Vector<DatabaseField> sourceRelationKeyFields;

    /** The field in the intermediate table that corresponds to the key in the target table */
    protected transient Vector<DatabaseField> targetRelationKeyFields;

    /** Query used for single row deletion. */
    protected transient DataModifyQuery deleteQuery;
    protected transient boolean hasCustomDeleteQuery;

    /** Used for insertion. */
    protected transient DataModifyQuery insertQuery;
    protected transient boolean hasCustomInsertQuery;
    
    /**
     * PUBLIC:
     * Default constructor.
     */
    public ManyToManyMapping() {
        this.insertQuery = new DataModifyQuery();
        this.deleteQuery = new DataModifyQuery();
        this.sourceRelationKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.targetRelationKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.sourceKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.targetKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.hasCustomDeleteQuery = false;
        this.hasCustomInsertQuery = false;
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * Add the fields in the intermediate table that corresponds to the primary 
     * key in the source table. This method is used if the keys are composite.
     */
    public void addSourceRelationKeyField(DatabaseField sourceRelationKeyField, DatabaseField sourcePrimaryKeyField) {
        getSourceRelationKeyFields().addElement(sourceRelationKeyField);
        getSourceKeyFields().addElement(sourcePrimaryKeyField);
    }
    
    /**
     * PUBLIC:
     * Add the fields in the intermediate table that corresponds to the primary 
     * key in the source table. This method is used if the keys are composite.
     */
    public void addSourceRelationKeyFieldName(String sourceRelationKeyFieldName, String sourcePrimaryKeyFieldName) {
        addSourceRelationKeyField(new DatabaseField(sourceRelationKeyFieldName), new DatabaseField(sourcePrimaryKeyFieldName));
    }

    /**
     * PUBLIC:
     * Add the fields in the intermediate table that corresponds to the primary 
     * key in the target table. This method is used if the keys are composite.
     */
    public void addTargetRelationKeyField(DatabaseField targetRelationKeyField, DatabaseField targetPrimaryKeyField) {
        getTargetRelationKeyFields().addElement(targetRelationKeyField);
        getTargetKeyFields().addElement(targetPrimaryKeyField);
    }
    
    /**
     * PUBLIC:
     * Add the fields in the intermediate table that corresponds to the primary 
     * key in the target table. This method is used if the keys are composite.
     */
    public void addTargetRelationKeyFieldName(String targetRelationKeyFieldName, String targetPrimaryKeyFieldName) {
        addTargetRelationKeyField(new DatabaseField(targetRelationKeyFieldName), new DatabaseField(targetPrimaryKeyFieldName));
    }

    /**
     * INTERNAL:
     * The mapping clones itself to create deep copy.
     */
    public Object clone() {
        ManyToManyMapping clone = (ManyToManyMapping)super.clone();

        clone.setTargetKeyFields(cloneFields(getTargetKeyFields()));
        clone.setSourceKeyFields(cloneFields(getSourceKeyFields()));
        clone.setTargetRelationKeyFields(cloneFields(getTargetRelationKeyFields()));
        clone.setSourceRelationKeyFields(cloneFields(getSourceRelationKeyFields()));

        return clone;
    }

    /**
     * INTERNAL:
     * Extract the source primary key value from the relation row.
     * Used for batch reading, most following same order and fields as in the mapping.
     */
    protected Vector extractKeyFromRelationRow(AbstractRecord row, AbstractSession session) {
        Vector key = new Vector(getSourceRelationKeyFields().size());

        for (int index = 0; index < getSourceRelationKeyFields().size(); index++) {
            DatabaseField relationField = (DatabaseField)getSourceRelationKeyFields().elementAt(index);
            DatabaseField sourceField = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object value = row.get(relationField);

            // Must ensure the classificatin to get a cache hit.
            try {
                value = session.getDatasourcePlatform().getConversionManager().convertObject(value, getDescriptor().getObjectBuilder().getFieldClassification(sourceField));
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
            }

            key.addElement(value);
        }

        return key;
    }

    /**
     * INTERNAL:
     * Extract the primary key value from the source row.
     * Used for batch reading, most following same order and fields as in the mapping.
     */
    protected Vector extractPrimaryKeyFromRow(AbstractRecord row, AbstractSession session) {
        Vector key = new Vector(getSourceKeyFields().size());

        for (Enumeration fieldEnum = getSourceKeyFields().elements(); fieldEnum.hasMoreElements();) {
            DatabaseField field = (DatabaseField)fieldEnum.nextElement();
            Object value = row.get(field);

            // Must ensure the classificatin to get a cache hit.
            try {
                value = session.getDatasourcePlatform().getConversionManager().convertObject(value, getDescriptor().getObjectBuilder().getFieldClassification(field));
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
            }

            key.addElement(value);
        }

        return key;
    }

    protected DataModifyQuery getDeleteQuery() {
        return deleteQuery;
    }

    protected DataModifyQuery getInsertQuery() {
        return insertQuery;
    }

    /**
     * INTERNAL:
     * Return the relation table associated with the mapping.
     */
    public DatabaseTable getRelationTable() {
        return relationTable;
    }

    /**
     * PUBLIC:
     * Return the relation table name associated with the mapping.
     */
    public String getRelationTableName() {
        if (relationTable == null) {
            return null;
        }
        return relationTable.getName();
    }

    //CR#2407  This method is added to include table qualifier.

    /**
     * PUBLIC:
     * Return the relation table qualified name associated with the mapping.
     */
    public String getRelationTableQualifiedName() {
        if (relationTable == null) {
            return null;
        }
        return relationTable.getQualifiedName();
    }

    /**
     * INTERNAL:
     * Returns the selection criteria stored in the mapping selection query. This criteria
     * is used to read reference objects from the database.
     */
    public Expression getSelectionCriteria() {
        return getSelectionQuery().getSelectionCriteria();
    }

    /**
     * INTERNAL:
     * Returns the read query assoicated with the mapping.
     */
    public ReadQuery getSelectionQuery() {
        return selectionQuery;
    }

    /**
     * PUBLIC:
     * Return the source key field names associated with the mapping.
     * These are in-order with the sourceRelationKeyFieldNames.
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
     * Return all the source key fields associated with the mapping.
     */
    public Vector<DatabaseField> getSourceKeyFields() {
        return sourceKeyFields;
    }

    /**
     * PUBLIC:
     * Return the source relation key field names associated with the mapping.
     * These are in-order with the sourceKeyFieldNames.
     */
    public Vector getSourceRelationKeyFieldNames() {
        Vector fieldNames = new Vector(getSourceRelationKeyFields().size());
        for (Enumeration fieldsEnum = getSourceRelationKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * INTERNAL:
     * Return all the source realtion key fields associated with the mapping.
     */
    public Vector<DatabaseField> getSourceRelationKeyFields() {
        return sourceRelationKeyFields;
    }

    /**
     * PUBLIC:
     * Return the target key field names associated with the mapping.
     * These are in-order with the targetRelationKeyFieldNames.
     */
    public Vector getTargetKeyFieldNames() {
        Vector fieldNames = new Vector(getTargetKeyFields().size());
        for (Enumeration fieldsEnum = getTargetKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * INTERNAL:
     * Return all the target keys associated with the mapping.
     */
    public Vector<DatabaseField> getTargetKeyFields() {
        return targetKeyFields;
    }

    /**
     * PUBLIC:
     * Return the target relation key field names associated with the mapping.
     * These are in-order with the targetKeyFieldNames.
     */
    public Vector getTargetRelationKeyFieldNames() {
        Vector fieldNames = new Vector(getTargetRelationKeyFields().size());
        for (Enumeration fieldsEnum = getTargetRelationKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * INTERNAL:
     * Return all the target relation key fields associated with the mapping.
     */
    public Vector<DatabaseField> getTargetRelationKeyFields() {
        return targetRelationKeyFields;
    }

    /**
     * INTERNAL:
     * Returns whether the mapping has any constraint dependencies, such as
     * join table entries. This dependency is important for deletes from 
     * unidirectional relationships.
     */
    public boolean hasConstraintDependency() {
        // Set a dependency on the owning side
        return !isReadOnly();
    }

    protected boolean hasCustomDeleteQuery() {
        return hasCustomDeleteQuery;
    }

    protected boolean hasCustomInsertQuery() {
        return hasCustomInsertQuery;
    }

    /**
     * INTERNAL:
     * The join table is a dependency if not read-only.
     */
    public boolean hasDependency() {
        return isPrivateOwned() || (!isReadOnly());
    }

    /**
     * INTERNAL:
     * Initialize mappings
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        
        initializeRelationTable(session);
        initializeSourceRelationKeys(session);
        initializeTargetRelationKeys(session);

        if (isSingleSourceRelationKeySpecified()) {
            initializeSourceKeysWithDefaults(session);
        } else {
            initializeSourceKeys(session);
        }

        if (isSingleTargetRelationKeySpecified()) {
            initializeTargetKeysWithDefaults(session);
        } else {
            initializeTargetKeys(session);
        }

        if (shouldInitializeSelectionCriteria()) {
            initializeSelectionCriteria(session);
        }

        if (!getSelectionQuery().hasSessionName()) {
            getSelectionQuery().setSessionName(session.getName());
        }
        initializeDeleteAllQuery(session);
        initializeInsertQuery(session);
        initializeDeleteQuery(session);
    }

    /**
     * Initialize delete all query. This query is used to all relevant rows from the
     * relation table.
     */
    protected void initializeDeleteAllQuery(AbstractSession session) {
        if (!getDeleteAllQuery().hasSessionName()) {
            getDeleteAllQuery().setSessionName(session.getName());
        }

        if (hasCustomDeleteAllQuery()) {
            return;
        }

        Expression expression = null;
        Expression subExpression;
        Expression builder = new ExpressionBuilder();
        SQLDeleteStatement statement = new SQLDeleteStatement();

        // Construct an expression to delete from the relation table.
        for (int index = 0; index < getSourceRelationKeyFields().size(); index++) {
            DatabaseField sourceRelationKey = (DatabaseField)getSourceRelationKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);

            subExpression = builder.getField(sourceRelationKey).equal(builder.getParameter(sourceKey));
            expression = subExpression.and(expression);
        }

        // All the enteries are deleted in one shot.
        statement.setWhereClause(expression);
        statement.setTable(getRelationTable());
        getDeleteAllQuery().setSQLStatement(statement);
    }

    /**
     * INTERNAL:
     * Initialize delete query. This query is used to delete a specific row from the join table in uow,
     * given the objects on both sides of the relation.
     */
    protected void initializeDeleteQuery(AbstractSession session) {
        if (!getDeleteQuery().hasSessionName()) {
            getDeleteQuery().setSessionName(session.getName());
        }
        if (hasCustomDeleteQuery()) {
            return;
        }

        // Build where clause expression.
        Expression whereClause = null;
        Expression builder = new ExpressionBuilder();

        Enumeration relationKeyEnum = getSourceRelationKeyFields().elements();
        for (; relationKeyEnum.hasMoreElements();) {
            DatabaseField relationKey = (DatabaseField)relationKeyEnum.nextElement();

            Expression expression = builder.getField(relationKey).equal(builder.getParameter(relationKey));
            whereClause = expression.and(whereClause);
        }

        relationKeyEnum = getTargetRelationKeyFields().elements();
        for (; relationKeyEnum.hasMoreElements();) {
            DatabaseField relationKey = (DatabaseField)relationKeyEnum.nextElement();

            Expression expression = builder.getField(relationKey).equal(builder.getParameter(relationKey));
            whereClause = expression.and(whereClause);
        }

        SQLDeleteStatement statement = new SQLDeleteStatement();
        statement.setTable(getRelationTable());
        statement.setWhereClause(whereClause);
        getDeleteQuery().setSQLStatement(statement);
    }

    /**
     * INTERNAL:
     * Initialize insert query. This query is used to insert the collection of objects into the
     * relation table.
     */
    protected void initializeInsertQuery(AbstractSession session) {
        if (!getInsertQuery().hasSessionName()) {
            getInsertQuery().setSessionName(session.getName());
        }
        if (hasCustomInsertQuery()) {
            return;
        }

        SQLInsertStatement statement = new SQLInsertStatement();
        statement.setTable(getRelationTable());
        AbstractRecord joinRow = new DatabaseRecord();
        for (Enumeration targetEnum = getTargetRelationKeyFields().elements();
                 targetEnum.hasMoreElements();) {
            joinRow.put((DatabaseField)targetEnum.nextElement(), null);
        }
        for (Enumeration sourceEnum = getSourceRelationKeyFields().elements();
                 sourceEnum.hasMoreElements();) {
            joinRow.put((DatabaseField)sourceEnum.nextElement(), null);
        }
        statement.setModifyRow(joinRow);
        getInsertQuery().setSQLStatement(statement);
        getInsertQuery().setModifyRow(joinRow);
    }

    /**
     * INTERNAL:
     * Set the table qualifier on the relation table if required
     */
    protected void initializeRelationTable(AbstractSession session) throws DescriptorException {
        Platform platform = session.getDatasourcePlatform();

        if ((getRelationTable() == null) || (getRelationTableName().length() == 0)) {
            throw DescriptorException.noRelationTable(this);
        }

        if (platform.getTableQualifier().length() == 0) {
            return;
        }

        if (getRelationTable().getTableQualifier().length() == 0) {
            getRelationTable().setTableQualifier(platform.getTableQualifier());
        }
    }

    /**
     * INTERNAL:
     * Selection criteria is created to read target records from the table.
     */
    protected void initializeSelectionCriteria(AbstractSession session) {
        DatabaseField relationKey;
        DatabaseField sourceKey;
        DatabaseField targetKey;
        Expression exp1;
        Expression exp2;
        Expression expression;
        Expression criteria;
        Expression builder = new ExpressionBuilder();
        Enumeration relationKeyEnum;
        Enumeration sourceKeyEnum;
        Enumeration targetKeyEnum;

        Expression linkTable = null;

        targetKeyEnum = getTargetKeyFields().elements();
        relationKeyEnum = getTargetRelationKeyFields().elements();
        for (; targetKeyEnum.hasMoreElements();) {
            relationKey = (DatabaseField)relationKeyEnum.nextElement();
            targetKey = (DatabaseField)targetKeyEnum.nextElement();
            if (linkTable == null) {// We could just call getTable repeatedly, but it's a waste
                linkTable = builder.getTable(relationKey.getTable());
            }

            exp1 = builder.getField(targetKey);
            exp2 = linkTable.getField(relationKey);
            expression = exp1.equal(exp2);

            if ((criteria = getSelectionCriteria()) == null) {
                criteria = expression;
            } else {
                criteria = expression.and(criteria);
            }

            setSelectionCriteria(criteria);
        }

        relationKeyEnum = getSourceRelationKeyFields().elements();
        sourceKeyEnum = getSourceKeyFields().elements();

        for (; relationKeyEnum.hasMoreElements();) {
            relationKey = (DatabaseField)relationKeyEnum.nextElement();
            sourceKey = (DatabaseField)sourceKeyEnum.nextElement();

            exp1 = linkTable.getField(relationKey);
            exp2 = builder.getParameter(sourceKey);
            expression = exp1.equal(exp2);

            if ((criteria = getSelectionCriteria()) == null) {
                criteria = expression;
            } else {
                criteria = expression.and(criteria);
            }

            setSelectionCriteria(criteria);
        }
    }

    /**
     * INTERNAL:
     * All the source key field names are converted to DatabaseField and stored.
     */
    protected void initializeSourceKeys(AbstractSession session) {
        for (Enumeration sourceEnum = getSourceKeyFields().elements();
                 sourceEnum.hasMoreElements();) {
            getDescriptor().buildField((DatabaseField)sourceEnum.nextElement());
        }
    }

    /**
     * INTERNAL:
     * If a user does not specify the source key then the primary keys of the source table are used.
     */
    protected void initializeSourceKeysWithDefaults(AbstractSession session) {
        List<DatabaseField> primaryKeyFields = getDescriptor().getPrimaryKeyFields();
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            getSourceKeyFields().addElement(primaryKeyFields.get(index));
        }
    }

    /**
     * INTERNAL:
     * All the source relation key field names are converted to DatabaseField and stored.
     */
    protected void initializeSourceRelationKeys(AbstractSession session) throws DescriptorException {
        if (getSourceRelationKeyFields().size() == 0) {
            throw DescriptorException.noSourceRelationKeysSpecified(this);
        }

        for (Enumeration entry = getSourceRelationKeyFields().elements(); entry.hasMoreElements();) {
            DatabaseField field = (DatabaseField)entry.nextElement();
            if (field.hasTableName() && (!(field.getTableName().equals(getRelationTable().getName())))) {
                throw DescriptorException.relationKeyFieldNotProperlySpecified(field, this);
            }
            field.setTable(getRelationTable());
        }
    }

    /**
     * INTERNAL:
     * All the target key field names are converted to DatabaseField and stored.
     */
    protected void initializeTargetKeys(AbstractSession session) {
        for (Enumeration targetEnum = getTargetKeyFields().elements();
                 targetEnum.hasMoreElements();) {
            getReferenceDescriptor().buildField((DatabaseField)targetEnum.nextElement());
        }
    }

    /**
     * INTERNAL:
     * If a user does not specify the target key then the primary keys of the target table are used.
     */
    protected void initializeTargetKeysWithDefaults(AbstractSession session) {
        List<DatabaseField> primaryKeyFields = getReferenceDescriptor().getPrimaryKeyFields();
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            getTargetKeyFields().addElement(primaryKeyFields.get(index));
        }
    }

    /**
     * INTERNAL:
     * All the target relation key field names are converted to DatabaseField and stored.
     */
    protected void initializeTargetRelationKeys(AbstractSession session) {
        if (getTargetRelationKeyFields().size() == 0) {
            throw DescriptorException.noTargetRelationKeysSpecified(this);
        }

        for (Enumeration targetEnum = getTargetRelationKeyFields().elements();
                 targetEnum.hasMoreElements();) {
            DatabaseField field = (DatabaseField)targetEnum.nextElement();
            if (field.hasTableName() && (!(field.getTableName().equals(getRelationTable().getName())))) {
                throw DescriptorException.relationKeyFieldNotProperlySpecified(field, this);
            }
            field.setTable(getRelationTable());
        }
    }

    /**
     * INTERNAL:
     * An object was added to the collection during an update, insert it.
     */
    protected void insertAddedObjectEntry(ObjectLevelModifyQuery query, Object objectAdded) throws DatabaseException, OptimisticLockException {
        //cr 3819 added the line below to fix the translationtable to ensure that it
        // contains the required values
        prepareTranslationRow(query.getTranslationRow(), query.getObject(), query.getSession());
        AbstractRecord databaseRow = new DatabaseRecord();

        // Extract primary key and value from the source.
        for (int index = 0; index < getSourceRelationKeyFields().size(); index++) {
            DatabaseField sourceRelationKey = (DatabaseField)getSourceRelationKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object sourceKeyValue = query.getTranslationRow().get(sourceKey);
            databaseRow.put(sourceRelationKey, sourceKeyValue);
        }

        // Extract target field and its value. Construct insert statement and execute it
        for (int index = 0; index < getTargetRelationKeyFields().size(); index++) {
            DatabaseField targetRelationKey = (DatabaseField)getTargetRelationKeyFields().elementAt(index);
            DatabaseField targetKey = (DatabaseField)getTargetKeyFields().elementAt(index);
            Object targetKeyValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(objectAdded, targetKey, query.getSession());
            databaseRow.put(targetRelationKey, targetKeyValue);
        }

        query.getSession().executeQuery(getInsertQuery(), databaseRow);
    }

    /**
     * INTERNAL:
     * Insert into relation table. This follows following steps.
     * <p>- Extract primary key and its value from the source object.
     * <p>- Extract target key and its value from the target object.
     * <p>- Construct a insert statement with above fields and values for relation table.
     * <p>- execute the statement.
     * <p>- Repeat above three statements until all the target objects are done.
     */
    public void insertIntoRelationTable(WriteObjectQuery query) throws DatabaseException {
        if (isReadOnly()) {
            return;
        }

        ContainerPolicy cp = getContainerPolicy();
        Object objects = getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        if (cp.isEmpty(objects)) {
            return;
        }

        prepareTranslationRow(query.getTranslationRow(), query.getObject(), query.getSession());
        AbstractRecord databaseRow = new DatabaseRecord();

        // Extract primary key and value from the source.
        for (int index = 0; index < getSourceRelationKeyFields().size(); index++) {
            DatabaseField sourceRelationKey = (DatabaseField)getSourceRelationKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object sourceKeyValue = query.getTranslationRow().get(sourceKey);
            databaseRow.put(sourceRelationKey, sourceKeyValue);
        }

        // Extract target field and its value. Construct insert statement and execute it
        for (Object iter = cp.iteratorFor(objects); cp.hasNext(iter);) {
            Object object = cp.next(iter, query.getSession());
            for (int index = 0; index < getTargetRelationKeyFields().size(); index++) {
                DatabaseField targetRelationKey = (DatabaseField)getTargetRelationKeyFields().elementAt(index);
                DatabaseField targetKey = (DatabaseField)getTargetKeyFields().elementAt(index);
                Object targetKeyValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(object, targetKey, query.getSession());
                databaseRow.put(targetRelationKey, targetKeyValue);
            }

            query.getSession().executeQuery(getInsertQuery(), databaseRow);
        }
    }

    /**
     * INTERNAL:
     * Write the target objects if the cascade policy requires them to be written first.
     * They must be written within a unit of work to ensure that they exist.
     */
    public void insertTargetObjects(WriteObjectQuery query) throws DatabaseException, OptimisticLockException {
        if (!shouldObjectModifyCascadeToParts(query)) {
            return;
        }

        // Only cascade dependents writes in uow.
        if (query.shouldCascadeOnlyDependentParts()) {
            return;
        }

        Object objects = getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        ContainerPolicy cp = getContainerPolicy();
        if (cp.isEmpty(objects)) {
            return;
        }

        // Write each of the target objects
        for (Object objectsIterator = cp.iteratorFor(objects); cp.hasNext(objectsIterator);) {
            Object object = cp.next(objectsIterator, query.getSession());
            if (isPrivateOwned()) {
                // no need to set changeset as insert is a straight copy anyway
                InsertObjectQuery insertQuery = new InsertObjectQuery();
                insertQuery.setObject(object);
                insertQuery.setCascadePolicy(query.getCascadePolicy());
                query.getSession().executeQuery(insertQuery);
            } else {
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

    /**
     * INTERNAL:
     * Return if this mapping support joining.
     */
    public boolean isJoiningSupported() {
        return true;
    }

    /**
     * INTERNAL:
     */
    public boolean isManyToManyMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Checks if a single source key was specified.
     */
    protected boolean isSingleSourceRelationKeySpecified() {
        return getSourceKeyFields().isEmpty();
    }

    /**
     * INTERNAL:
     * Checks if a single target key was specified.
     */
    protected boolean isSingleTargetRelationKeySpecified() {
        return getTargetKeyFields().isEmpty();
    }

    /**
     * INTERNAL:
     * An object was added to the collection during an update, insert it if private.
     */
    protected void objectAddedDuringUpdate(ObjectLevelModifyQuery query, Object objectAdded, ObjectChangeSet changeSet) throws DatabaseException, OptimisticLockException {
        // First insert/update object.
        super.objectAddedDuringUpdate(query, objectAdded, changeSet);

        // In the uow data queries are cached until the end of the commit.
        if (query.shouldCascadeOnlyDependentParts()) {
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[3];
            event[0] = ObjectAdded;
            event[1] = query;
            event[2] = objectAdded;
            query.getSession().getCommitManager().addDataModificationEvent(this, event);
        } else {
            insertAddedObjectEntry(query, objectAdded);
        }
    }

    /**
     * INTERNAL:
     * An object was removed to the collection during an update, delete it if private.
     */
    protected void objectRemovedDuringUpdate(ObjectLevelModifyQuery query, Object objectDeleted) throws DatabaseException, OptimisticLockException {
        AbstractRecord databaseRow = new DatabaseRecord();

        // Extract primary key and value from the source.
        for (int index = 0; index < getSourceRelationKeyFields().size(); index++) {
            DatabaseField sourceRelationKey = (DatabaseField)getSourceRelationKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object sourceKeyValue = query.getTranslationRow().get(sourceKey);
            databaseRow.put(sourceRelationKey, sourceKeyValue);
        }

        // Extract target field and its value. Construct insert statement and execute it
        for (int index = 0; index < getTargetRelationKeyFields().size(); index++) {
            DatabaseField targetRelationKey = (DatabaseField)getTargetRelationKeyFields().elementAt(index);
            DatabaseField targetKey = (DatabaseField)getTargetKeyFields().elementAt(index);
            Object targetKeyValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(objectDeleted, targetKey, query.getSession());
            databaseRow.put(targetRelationKey, targetKeyValue);
        }

        // In the uow data queries are cached until the end of the commit.
        if (query.shouldCascadeOnlyDependentParts()) {
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[3];
            event[0] = ObjectRemoved;
            event[1] = getDeleteQuery();
            event[2] = databaseRow;
            query.getSession().getCommitManager().addDataModificationEvent(this, event);
        } else {
            query.getSession().executeQuery(getDeleteQuery(), databaseRow);
        }

        // Delete object after join entry is delete if private.
        super.objectRemovedDuringUpdate(query, objectDeleted);
    }

    /**
     * INTERNAL:
     * Perform the commit event.
     * This is used in the uow to delay data modifications.
     */
    public void performDataModificationEvent(Object[] event, AbstractSession session) throws DatabaseException, DescriptorException {
        // Hey I might actually want to use an inner class here... ok array for now.
        if (event[0] == PostInsert) {
            insertIntoRelationTable((WriteObjectQuery)event[1]);
        } else if (event[0] == ObjectRemoved) {
            session.executeQuery((DataModifyQuery)event[1], (AbstractRecord)event[(2)]);
        } else if (event[0] == ObjectAdded) {
            insertAddedObjectEntry((WriteObjectQuery)event[1], event[2]);
        } else {
            throw DescriptorException.invalidDataModificationEventCode(event[0], this);
        }
    }

    /**
     * INTERNAL:
     * Insert into relation table. This follows following steps.
     * <p>- Extract primary key and its value from the source object.
     * <p>- Extract target key and its value from the target object.
     * <p>- Construct a insert statement with above fields and values for relation table.
     * <p>- execute the statement.
     * <p>- Repeat above three statements until all the target objects are done.
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException {
        insertTargetObjects(query);
        // Batch data modification in the uow
        if (query.shouldCascadeOnlyDependentParts()) {
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[2];
            event[0] = PostInsert;
            event[1] = query;
            query.getSession().getCommitManager().addDataModificationEvent(this, event);
        } else {
            insertIntoRelationTable(query);
        }
    }

    /**
     * INTERNAL:
     * Update the relation table with the entries related to this mapping.
     * Delete entries removed, insert entries added.
     * If private also insert/delete/update target objects.
     */
    public void postUpdate(WriteObjectQuery query) throws DatabaseException {
        if (isReadOnly()) {
            return;
        }

        // If objects are not instantiated that means they are not changed.
        if (!isAttributeValueInstantiated(query.getObject())) {
            return;
        }

        Object objectsInMemoryModel = getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        // This accesses the backup in uow otherwise goes to database (may be better of to delete all in non uow case).
        Object currentObjectsInDB = readPrivateOwnedForObject(query);
        if (currentObjectsInDB == null) {
            currentObjectsInDB = getContainerPolicy().containerInstance(1);
        }
        compareObjectsAndWrite(currentObjectsInDB, objectsInMemoryModel, query);
    }

    /**
     * INTERNAL:
     * Delete entries related to this mapping from the relation table.
     */
    public void preDelete(WriteObjectQuery query) throws DatabaseException {
        Object objectsIterator = null;
        ContainerPolicy containerPolicy = getContainerPolicy();

        Object objects = getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());

        if (shouldObjectModifyCascadeToPartsForPreDelete(query)) {
            //this must be done up here because the select must be done before the entry in the relation table is deleted.
            objectsIterator = containerPolicy.iteratorFor(objects);
        }
        
        if (!isReadOnly()) {
            prepareTranslationRow(query.getTranslationRow(), query.getObject(), query.getSession());
            query.getSession().executeQuery(getDeleteAllQuery(), query.getTranslationRow());
        }

        // If privately owned delete the objects, this does not handle removed objects (i.e. verify delete, not req in uow).
        // Does not try to optimize delete all like 1-m, (rarely used and hard to do).
        if (shouldObjectModifyCascadeToPartsForPreDelete(query)) {
            if (objects != null) {
                //objectsIterator will not be null because cascade check will still return true.
                while (containerPolicy.hasNext(objectsIterator)) {
                    Object object = containerPolicy.next(objectsIterator, query.getSession());
                    // Do not cascade to objects that are currently being processed
                    if (!query.getSession().getCommitManager().isCommitInPreModify(object) &&
                            shouldObjectDeleteCascadeToPart(query, object)) {
                        DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
                        deleteQuery.setObject(object);
                        deleteQuery.setCascadePolicy(query.getCascadePolicy());
                        query.getSession().executeQuery(deleteQuery);
                    }
                }
            }
        }
    }

    /**
     * INTERNAL:
     * The translation row may require additional fields than the primary key if the mapping in not on the primary key.
     */
    protected void prepareTranslationRow(AbstractRecord translationRow, Object object, AbstractSession session) {
        // Make sure that each source key field is in the translation row.
        for (Enumeration sourceFieldsEnum = getSourceKeyFields().elements();
                 sourceFieldsEnum.hasMoreElements();) {
            DatabaseField sourceKey = (DatabaseField)sourceFieldsEnum.nextElement();
            if (!translationRow.containsKey(sourceKey)) {
                Object value = getDescriptor().getObjectBuilder().extractValueFromObjectForField(object, sourceKey, session);
                translationRow.put(sourceKey, value);
            }
        }
    }

    /**
     * PUBLIC:
     * The default delete query for mapping can be overridden by specifying the new query.
     * This query must delete the row from the M-M join table.
     */
    public void setCustomDeleteQuery(DataModifyQuery query) {
        setDeleteQuery(query);
        setHasCustomDeleteQuery(true);
    }

    /**
     * PUBLIC:
     * The default insert query for mapping can be overridden by specifying the new query.
     * This query must insert the row into the M-M join table.
     */
    public void setCustomInsertQuery(DataModifyQuery query) {
        setInsertQuery(query);
        setHasCustomInsertQuery(true);
    }

    protected void setDeleteQuery(DataModifyQuery deleteQuery) {
        this.deleteQuery = deleteQuery;
    }

    /**
     * PUBLIC:
     * Set the receiver's delete SQL string. This allows the user to override the SQL
     * generated by TOPLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row, through replacing the field names
     * marked by '#' with the values for those fields.
     * This is used to delete a single entry from the M-M join table.
     * Example, 'delete from PROJ_EMP where PROJ_ID = #PROJ_ID AND EMP_ID = #EMP_ID'.
     */
    public void setDeleteSQLString(String sqlString) {
        DataModifyQuery query = new DataModifyQuery();
        query.setSQLString(sqlString);
        setCustomDeleteQuery(query);
    }
    
    /**
     * PUBLIC:
     * Set the receiver's delete Call. This allows the user to override the SQL
     * generated by TOPLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row.
     * This is used to delete a single entry from the M-M join table.
     * Example, 'new SQLCall("delete from PROJ_EMP where PROJ_ID = #PROJ_ID AND EMP_ID = #EMP_ID")'.
     */
    public void setDeleteCall(Call call) {
        DataModifyQuery query = new DataModifyQuery();
        query.setCall(call);
        setCustomDeleteQuery(query);
    }

    protected void setHasCustomDeleteQuery(boolean hasCustomDeleteQuery) {
        this.hasCustomDeleteQuery = hasCustomDeleteQuery;
    }

    protected void setHasCustomInsertQuery(boolean bool) {
        hasCustomInsertQuery = bool;
    }

    protected void setInsertQuery(DataModifyQuery insertQuery) {
        this.insertQuery = insertQuery;
    }

    /**
     * PUBLIC:
     * Set the receiver's insert SQL string. This allows the user to override the SQL
     * generated by TOPLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row, through replacing the field names
     * marked by '#' with the values for those fields.
     * This is used to insert an entry into the M-M join table.
     * Example, 'insert into PROJ_EMP (EMP_ID, PROJ_ID) values (#EMP_ID, #PROJ_ID)'.
     */
    public void setInsertSQLString(String sqlString) {
        DataModifyQuery query = new DataModifyQuery();
        query.setSQLString(sqlString);
        setCustomInsertQuery(query);
    }
    
    /**
     * PUBLIC:
     * Set the receiver's insert Call. This allows the user to override the SQL
     * generated by TOPLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row.
     * This is used to insert an entry into the M-M join table.
     * Example, 'new SQLCall("insert into PROJ_EMP (EMP_ID, PROJ_ID) values (#EMP_ID, #PROJ_ID)")'.
     */
    public void setInsertCall(Call call) {
        DataModifyQuery query = new DataModifyQuery();
        query.setCall(call);
        setCustomInsertQuery(query);
    }

    /**
     * PUBLIC:
     * Set the relational table.
     * This is the join table that store both the source and target primary keys.
     */
    public void setRelationTable(DatabaseTable relationTable) {
        this.relationTable = relationTable;
    }
    
    /**
     * PUBLIC:
     * Set the name of the relational table.
     * This is the join table that store both the source and target primary keys.
     */
    public void setRelationTableName(String tableName) {
        relationTable = new DatabaseTable(tableName);
    }

    /**
     * PUBLIC:
     * Set the name of the session to execute the mapping's queries under.
     * This can be used by the session broker to override the default session
     * to be used for the target class.
     */
    public void setSessionName(String name) {
        super.setSessionName(name);
        getInsertQuery().setSessionName(name);
        getDeleteQuery().setSessionName(name);
    }

    /**
     * PUBLIC:
     * Set the source key field names associated with the mapping.
     * These must be in-order with the sourceRelationKeyFieldNames.
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
     * Set the source fields.
     */
    public void setSourceKeyFields(Vector<DatabaseField> sourceKeyFields) {
        this.sourceKeyFields = sourceKeyFields;
    }

    /**
     * PUBLIC:
     * Set the source key field in the relation table.
     * This is the name of the foreign key in the relation table to the source's primary key field.
     * This method is used if the source primary key is a singleton only.
     */
    public void setSourceRelationKeyFieldName(String sourceRelationKeyFieldName) {
        getSourceRelationKeyFields().addElement(new DatabaseField(sourceRelationKeyFieldName));
    }

    /**
     * PUBLIC:
     * Set the source relation key field names associated with the mapping.
     * These must be in-order with the sourceKeyFieldNames.
     */
    public void setSourceRelationKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setSourceRelationKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Set the source fields.
     */
    public void setSourceRelationKeyFields(Vector<DatabaseField> sourceRelationKeyFields) {
        this.sourceRelationKeyFields = sourceRelationKeyFields;
    }

    /**
     * INTERNAL:
     * Set the target key field names associated with the mapping.
     * These must be in-order with the targetRelationKeyFieldNames.
     */
    public void setTargetKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setTargetKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Set the target fields.
     */
    public void setTargetKeyFields(Vector<DatabaseField> targetKeyFields) {
        this.targetKeyFields = targetKeyFields;
    }

    /**
     * PUBLIC:
     * Set the target key field in the relation table.
     * This is the name of the foreign key in the relation table to the target's primary key field.
     * This method is used if the target's primary key is a singleton only.
     */
    public void setTargetRelationKeyFieldName(String targetRelationKeyFieldName) {
        getTargetRelationKeyFields().addElement(new DatabaseField(targetRelationKeyFieldName));
    }

    /**
     * INTERNAL:
     * Set the target relation key field names associated with the mapping.
     * These must be in-order with the targetKeyFieldNames.
     */
    public void setTargetRelationKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setTargetRelationKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Set the target fields.
     */
    public void setTargetRelationKeyFields(Vector<DatabaseField> targetRelationKeyFields) {
        this.targetRelationKeyFields = targetRelationKeyFields;
    }

    /**
     * Return whether any process leading to object deletion should also affect its parts. 
     * Used in preDelete. Note that dependencies are reversed for deletes: 
     * Join table entries must be removed before the related objects can be deleted.
     * So the owning side must be deleted before the non-owning side is processed. 
     */
    protected boolean shouldObjectModifyCascadeToPartsForPreDelete(ObjectLevelModifyQuery query) {
        // Always cascade for privately-owned parts
        if (isPrivateOwned()) {
            return true;
        }

        // The non-owning side is read-only for join table mappings.
        if (query.shouldCascadeOnlyDependentParts()) {
            return isReadOnly();
        } 
        
        return query.shouldCascadeAllParts();
    }
}
