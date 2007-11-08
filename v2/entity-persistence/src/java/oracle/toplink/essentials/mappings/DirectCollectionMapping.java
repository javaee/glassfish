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
import oracle.toplink.essentials.internal.databaseaccess.Platform;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.mappings.converters.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.ObjectCopyingPolicy;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>: This mapping is used to store a collection of simple types (String, Number, Date, etc.)
 * into a single table.  The table must store the value and a foreign key to the source object.
 * A converter can be used if the desired object type and the data type do not match.
 *
 * @see Converter
 * @see ObjectTypeConverter
 * @see TypeConversionConverter
 * @see SerializedObjectConverter
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class DirectCollectionMapping extends CollectionMapping implements RelationalMapping {

    /** Used for data modification events. */
    protected static final String Delete = "delete";
    protected static final String Insert = "insert";
    protected static final String DeleteAll = "deleteAll";

    /** Allows user defined conversion between the object value and the database value. */
    protected Converter valueConverter;

    /** Stores the reference table*/
    protected transient DatabaseTable referenceTable;

    /** The direct field name is converted and stored */
    protected transient DatabaseField directField;
    protected transient Vector<DatabaseField> sourceKeyFields;
    protected transient Vector<DatabaseField> referenceKeyFields;

    /** Used for insertion for m-m and dc, not used in 1-m. */
    protected transient DataModifyQuery insertQuery;
    /** Used for deletion when ChangeSets are used */
    protected transient ModifyQuery changeSetDeleteQuery;
    
    protected transient boolean hasCustomDeleteQuery;
    protected transient boolean hasCustomInsertQuery;

    /**
     * PUBLIC:
     * Default constructor.
     */
    public DirectCollectionMapping() {
        this.insertQuery = new DataModifyQuery();
        this.sourceKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.referenceKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.selectionQuery = new DirectReadQuery();
        this.hasCustomInsertQuery = false;
        this.isPrivateOwned = true;
    }

    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * Return the converter on the mapping.
     * A converter can be used to convert between the direct collection's object value and database value.
     */
    public Converter getValueConverter() {
        return valueConverter;
    }

    /**
     * PUBLIC:
     * Set the converter on the mapping.
     * A converter can be used to convert between the direct collection's object value and database value.
     */
    public void setValueConverter(Converter valueConverter) {
        this.valueConverter = valueConverter;
    }

    /**
     * PUBLIC:
     * Add the reference key field.
     * This is used for composite reference keys.
     * This is the foreign key field in the direct table referencing the primary key of the source object.
     * Both the reference field and the source field that it references must be provided.
     */
    public void addReferenceKeyField(DatabaseField referenceForeignKeyField, DatabaseField sourcePrimaryKeyField) {
        getSourceKeyFields().addElement(sourcePrimaryKeyField);
        getReferenceKeyFields().addElement(referenceForeignKeyField);
    }
    
    /**
     * PUBLIC:
     * Add the name of the reference key field.
     * This is used for composite reference keys.
     * This is the foreign key field in the direct table referencing the primary key of the source object.
     * Both the reference field name and the name of the source field that it references must be provided.
     */
    public void addReferenceKeyFieldName(String referenceForeignKeyFieldName, String sourcePrimaryKeyFieldName) {
        addReferenceKeyField(new DatabaseField(referenceForeignKeyFieldName), new DatabaseField(sourcePrimaryKeyFieldName));
    }

    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
        Object attributeValue = getRealCollectionAttributeValueFromObject(original, policy.getSession());
        attributeValue = getContainerPolicy().cloneFor(attributeValue);
        setRealAttributeValueInObject(copy, attributeValue);
    }

    /**
     * INTERNAL:
     * Clone the element, if necessary.
     * DirectCollections hold on to objects that do not have Descriptors
     * (e.g. int, String). These objects do not need to be cloned, unless they use a converter - they
     * are immutable.
     */
    protected Object buildElementClone(Object element, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        Object cloneValue = element;
        if ((getValueConverter() != null) && getValueConverter().isMutable()) {
            cloneValue = getValueConverter().convertDataValueToObjectValue(getValueConverter().convertObjectValueToDataValue(cloneValue, unitOfWork), unitOfWork);
        }
        return cloneValue;
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        //as this mapping type references primitive objects this method does not apply
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        //as this mapping type references primitive objects this method does not apply
    }

    /**
     * INTERNAL:
     * The mapping clones itself to create deep copy.
     */
    public Object clone() {
        DirectCollectionMapping clone = (DirectCollectionMapping)super.clone();

        clone.setSourceKeyFields(cloneFields(getSourceKeyFields()));
        clone.setReferenceKeyFields(cloneFields(getReferenceKeyFields()));

        return clone;
    }

    /**
     * INTERNAL:
     * This method is used to calculate the differences between two collections.
     */
    public void compareCollectionsForChange(Object oldCollection, Object newCollection, ChangeRecord changeRecord, AbstractSession session){
        ContainerPolicy cp = getContainerPolicy();
        int numberOfNewNulls = 0;

        HashMap originalKeyValues = new HashMap(10);
        HashMap cloneKeyValues = new HashMap(10);
        
        if (oldCollection != null){
            Object backUpIter = cp.iteratorFor(oldCollection);

            while (cp.hasNext(backUpIter)) {// Make a lookup of the objects
                Object secondObject = cp.next(backUpIter, session);

                // For CR#2258/CR#2378 handle null values inserted in a collection.
                if (secondObject == null) {
                    numberOfNewNulls--;
                } else {
                    Integer count = (Integer)originalKeyValues.get(secondObject);
                    if (count == null) {
                        originalKeyValues.put(secondObject, new Integer(1));
                    } else {
                        originalKeyValues.put(secondObject, new Integer(count.intValue() + 1));
                    }
                }
            }
        }
        // should a removal occur this is the original count of objects on the database.
        // this value is used to determine how many objects to re-insert after the delete as a
        // delete will delete all of the objects not just one.
        HashMap databaseCount = (HashMap)originalKeyValues.clone();
        int databaseNullCount = Math.abs(numberOfNewNulls);

        if (newCollection != null){
            Object cloneIter = cp.iteratorFor(newCollection);

            /* The following code is used to compare objects in a direct collection.
               Because objects in a direct collection are primitives and may be the same object
               the following code must count the number of instances in the collection not just the
               existence of an object.
            */
            while (cp.hasNext(cloneIter)) {//Compare them with the objects from the clone
                Object firstObject = cp.next(cloneIter, session);
    
                // For CR#2258/CR#2378 handle null values inserted in a collection.
                if (firstObject == null) {
                    numberOfNewNulls++;
                } else {
                    Integer count = (Integer)originalKeyValues.get(firstObject);
                    if (count == null) {//the object was not in the backup
                        Integer cloneCount = (Integer)cloneKeyValues.get(firstObject);
    
                        //Add it to the additions hashtable
                        if (cloneCount == null) {
                            cloneKeyValues.put(firstObject, new Integer(1));
                        } else {
                            cloneKeyValues.put(firstObject, new Integer(cloneCount.intValue() + 1));
                        }
                    } else if (count.intValue() == 1) {
                        //There is only one object so remove the whole reference
                        originalKeyValues.remove(firstObject);
                    } else {
                        originalKeyValues.put(firstObject, new Integer(count.intValue() - 1));
                    }
                }
            }
        }        
        if (cloneKeyValues.isEmpty() && originalKeyValues.isEmpty() && (numberOfNewNulls == 0) && (!changeRecord.getOwner().isNew())) {
            return;
        }
        ((DirectCollectionChangeRecord)changeRecord).addAdditionChange(cloneKeyValues, databaseCount);
        ((DirectCollectionChangeRecord)changeRecord).addRemoveChange(originalKeyValues, databaseCount);
        //For CR#2258, produce a changeRecord which reflects the addition and removal of null values.
        if (numberOfNewNulls != 0) {
            Vector changeList = null;
            ((DirectCollectionChangeRecord)changeRecord).getCommitAddMap().put(DirectCollectionChangeRecord.Null, new Integer(databaseNullCount));
            if (numberOfNewNulls > 0) {
                ((DirectCollectionChangeRecord)changeRecord).addAdditionChange(DirectCollectionChangeRecord.Null, new Integer(numberOfNewNulls));
            } else {
                numberOfNewNulls *= -1;
                ((DirectCollectionChangeRecord)changeRecord).addRemoveChange(DirectCollectionChangeRecord.Null, new Integer(numberOfNewNulls));
            }
        }
    }
    
    /**
     * INTERNAL:
     * This method compares the changes between two direct collections.  Comparisons are made on equality
     * not identity.
     * @return prototype.changeset.ChangeRecord
     */
    public ChangeRecord compareForChange(Object clone, Object backUp, ObjectChangeSet owner, AbstractSession session) {
        Object cloneAttribute = null;
        Object backUpAttribute = null;
        int numberOfNewNulls = 0;

        ContainerPolicy cp = getContainerPolicy();

        cloneAttribute = getAttributeValueFromObject(clone);

        if ((cloneAttribute != null) && (!getIndirectionPolicy().objectIsInstantiated(cloneAttribute))) {
            return null;
        }

        Object cloneObjectCollection = getRealCollectionAttributeValueFromObject(clone, session);

        Object backUpCollection = null;

        if (!owner.isNew()) {
            backUpAttribute = getAttributeValueFromObject(backUp);

            if ((backUpAttribute == null) && (cloneAttribute == null)) {
                return null;
            }

            backUpCollection = getRealCollectionAttributeValueFromObject(backUp, session);
        }
        DirectCollectionChangeRecord changeRecord = new DirectCollectionChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        compareCollectionsForChange(backUpCollection, cloneObjectCollection, changeRecord, session);
        if (changeRecord.hasChanges()){
            return changeRecord;
        }
        return null;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        Object firstCollection = getRealCollectionAttributeValueFromObject(firstObject, session);
        Object secondCollection = getRealCollectionAttributeValueFromObject(secondObject, session);
        ContainerPolicy containerPolicy = getContainerPolicy();

        if (containerPolicy.sizeFor(firstCollection) != containerPolicy.sizeFor(secondCollection)) {
            return false;
        }

        HashMap firstCounter = new HashMap();
        HashMap secondCounter = new HashMap();
        for (Object iter = containerPolicy.iteratorFor(firstCollection);containerPolicy.hasNext(iter);) {
            Object object = containerPolicy.next(iter, session);
            if (firstCounter.containsKey(object)){
                int count = ((Integer)firstCounter.get(object)).intValue();
                firstCounter.put(object, new Integer(++count));
            }else{
                firstCounter.put(object, new Integer(1));
            }
        }
        for (Object iter = containerPolicy.iteratorFor(secondCollection);containerPolicy.hasNext(iter);) {
            Object object = containerPolicy.next(iter, session);
            if (secondCounter.containsKey(object)){
                int count = ((Integer)secondCounter.get(object)).intValue();
                secondCounter.put(object, new Integer(++count));
            }else{
                secondCounter.put(object, new Integer(1));
            }
        }
        for (Iterator iterator = firstCounter.keySet().iterator(); iterator.hasNext();){
            Object object = iterator.next();
            
            if (!secondCounter.containsKey(object) || ( ((Integer)secondCounter.get(object)).intValue() != ((Integer)firstCounter.get(object)).intValue()) ) {
                return false;
            }else{
                iterator.remove();
                secondCounter.remove(object);
            }
        }
        if ( !firstCounter.isEmpty()  || !secondCounter.isEmpty() ) {
            return false;
        }
        return true;
    }

    /**
     * INTERNAL:
     * Convert all the class-name-based settings in this mapping to actual class-based
     * settings
     * This method is implemented by subclasses as necessary.
     * @param classLoader 
     */
    public void convertClassNamesToClasses(ClassLoader classLoader){
        super.convertClassNamesToClasses(classLoader);
        
        if (valueConverter != null) {
            if (valueConverter instanceof TypeConversionConverter){
                ((TypeConversionConverter)valueConverter).convertClassNamesToClasses(classLoader);
            } else if (valueConverter instanceof ObjectTypeConverter) {
                // To avoid 1.5 dependencies with the EnumTypeConverter check
                // against ObjectTypeConverter.
                ((ObjectTypeConverter) valueConverter).convertClassNamesToClasses(classLoader);
            }
        }
    };

    /**
     * INTERNAL:
     * Extract the source primary key value from the reference direct row.
     * Used for batch reading, most following same order and fields as in the mapping.
     */
    protected Vector extractKeyFromReferenceRow(AbstractRecord row, AbstractSession session) {
        Vector key = new Vector(getReferenceKeyFields().size());

        for (int index = 0; index < getReferenceKeyFields().size(); index++) {
            DatabaseField relationField = (DatabaseField)getReferenceKeyFields().elementAt(index);
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

    protected ModifyQuery getDeleteQuery() {
        if (changeSetDeleteQuery == null) {
            changeSetDeleteQuery = new DataModifyQuery();
        }
        return changeSetDeleteQuery;
    }

    /**
     * INTERNAL:
     * Return the direct field.
     * This is the field in the direct table to store the values.
     */
    public DatabaseField getDirectField() {
        return directField;
    }

    /**
     * PUBLIC:
     * Returns the name of the field name in the reference table.
     */
    public String getDirectFieldName() {
        if (getDirectField() == null) {
            return null;
        }
        return getDirectField().getQualifiedName();
    }

    protected DataModifyQuery getInsertQuery() {
        return insertQuery;
    }

    /**
     * INTERNAL:
     * This cannot be used with direct collection mappings.
     */
    public Class getReferenceClass() {
        return null;
    }

    public String getReferenceClassName() {
        return null;
    }

    /**
     * INTERNAL:
     * There is none on direct collection.
     */
    public ClassDescriptor getReferenceDescriptor() {
        return null;
    }

    /**
     * INTERNAL:
     * Return the reference key field names associated with the mapping.
     * These are in-order with the sourceKeyFieldNames.
     */
    public Vector getReferenceKeyFieldNames() {
        Vector fieldNames = new Vector(getReferenceKeyFields().size());
        for (Enumeration fieldsEnum = getReferenceKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * INTERNAL:
     * Return the reference key fields.
     */
    public Vector<DatabaseField> getReferenceKeyFields() {
        return referenceKeyFields;
    }

    /**
     * INTERNAL:
     * Return the direct table.
     * This is the table to store the values.
     */
    public DatabaseTable getReferenceTable() {
        return referenceTable;
    }

    /**
     * PUBLIC:
     * Returns the name of the reference table
     */
    public String getReferenceTableName() {
        if (getReferenceTable() == null) {
            return null;
        }
        return getReferenceTable().getName();
    }

    //This method is added to include table qualifier.

    /**
     * PUBLIC:
     * Returns the qualified name of the reference table
     */
    public String getReferenceTableQualifiedName() {//CR#2407  
        if (getReferenceTable() == null) {
            return null;
        }
        return getReferenceTable().getQualifiedName();
    }

    /**
     * INTERNAL:
     * Return the relationshipPartner mapping for this bi-directional mapping. If the relationshipPartner is null then
     * this is a uni-directional mapping.
     * DirectCollectionMapping can not be part of a bi-directional mapping
     */
    public DatabaseMapping getRelationshipPartner() {
        return null;
    }

    /**
     * PUBLIC:
     * Return the source key field names associated with the mapping.
     * These are in-order with the referenceKeyFieldNames.
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

    protected boolean hasCustomDeleteQuery() {
        return hasCustomDeleteQuery;
    }

    protected boolean hasCustomInsertQuery() {
        return hasCustomInsertQuery;
    }

    /**
     * INTERNAL:
     * Initialize and validate the mapping properties.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        if (isKeyForSourceSpecified()) {
            initializeSourceKeys(session);
        } else {
            initializeSourceKeysWithDefaults(session);
        }

        initializeReferenceTable(session);
        initializeReferenceKeys(session);
        initializeDirectField(session);
        if (shouldInitializeSelectionCriteria()) {
            initializeSelectionCriteria(session);
            initializeSelectionStatement(session);
        }
        if (!getSelectionQuery().hasSessionName()) {
            getSelectionQuery().setSessionName(session.getName());
        }
        if ((getValueConverter() != null) && (getSelectionQuery() instanceof DirectReadQuery)) {
            ((DirectReadQuery)getSelectionQuery()).setValueConverter(getValueConverter());
        }
        initializeDeleteAllQuery(session);
        initializeDeleteQuery(session);
        initializeInsertQuery(session);
        if (getValueConverter() != null) {
            getValueConverter().initialize(this, session);
        }
        super.initialize(session);
    }

    /**
     * Initialize delete all query. This query is used to delete the collection of objects from the
     * reference table.
     */
    protected void initializeDeleteAllQuery(AbstractSession session) {
        if (!getDeleteAllQuery().hasSessionName()) {
            getDeleteAllQuery().setSessionName(session.getName());
        }

        if (hasCustomDeleteAllQuery()) {
            return;
        }

        Expression expression = null;
        Expression subExp1;
        Expression subExp2;
        Expression subExpression;
        Expression builder = new ExpressionBuilder();
        SQLDeleteStatement statement = new SQLDeleteStatement();

        // Construct an expression to delete from the relation table.
        for (int index = 0; index < getReferenceKeyFields().size(); index++) {
            DatabaseField referenceKey = (DatabaseField)getReferenceKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);

            subExp1 = builder.getField(referenceKey);
            subExp2 = builder.getParameter(sourceKey);
            subExpression = subExp1.equal(subExp2);

            if (expression == null) {
                expression = subExpression;
            } else {
                expression = expression.and(subExpression);
            }
        }

        statement.setWhereClause(expression);
        statement.setTable(getReferenceTable());
        getDeleteAllQuery().setSQLStatement(statement);
    }

    protected void initializeDeleteQuery(AbstractSession session) {
        if (!getDeleteQuery().hasSessionName()) {
            getDeleteQuery().setSessionName(session.getName());
        }

        if (hasCustomDeleteQuery()) {
            return;
        }

        Expression builder = new ExpressionBuilder();
        Expression directExp = builder.getField(getDirectField()).equal(builder.getParameter(getDirectField()));
        Expression expression = null;
        SQLDeleteStatement statement = new SQLDeleteStatement();

        // Construct an expression to delete from the relation table.
        for (int index = 0; index < getReferenceKeyFields().size(); index++) {
            DatabaseField referenceKey = (DatabaseField)getReferenceKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);

            Expression subExp1 = builder.getField(referenceKey);
            Expression subExp2 = builder.getParameter(sourceKey);
            Expression subExpression = subExp1.equal(subExp2);

            expression = subExpression.and(expression);
        }
        expression = expression.and(directExp);
        statement.setWhereClause(expression);
        statement.setTable(getReferenceTable());
        getDeleteQuery().setSQLStatement(statement);
    }

    /**
     * The field name on the reference table is initialized and cached.
     */
    protected void initializeDirectField(AbstractSession session) throws DescriptorException {
        if (getDirectField() == null) {
            throw DescriptorException.directFieldNameNotSet(this);
        }

        getDirectField().setTable(getReferenceTable());
        getDirectField().setIndex(0);
    }

    /**
     * Initialize insert query. This query is used to insert the collection of objects into the
     * reference table.
     */
    protected void initializeInsertQuery(AbstractSession session) {
        if (!getInsertQuery().hasSessionName()) {
            getInsertQuery().setSessionName(session.getName());
        }

        if (hasCustomInsertQuery()) {
            return;
        }

        SQLInsertStatement statement = new SQLInsertStatement();
        statement.setTable(getReferenceTable());
        AbstractRecord directRow = new DatabaseRecord();
        for (Enumeration referenceEnum = getReferenceKeyFields().elements();
                 referenceEnum.hasMoreElements();) {
            directRow.put((DatabaseField)referenceEnum.nextElement(), null);
        }
        directRow.put(getDirectField(), null);
        statement.setModifyRow(directRow);
        getInsertQuery().setSQLStatement(statement);
        getInsertQuery().setModifyRow(directRow);
    }

    /**
     * There is no reference descriptor
     */
    protected void initializeReferenceDescriptor(AbstractSession session) {
        ;
    }

    /**
     * The reference keys on the reference table are initalized
     */
    protected void initializeReferenceKeys(AbstractSession session) throws DescriptorException {
        if (getReferenceKeyFields().size() == 0) {
            throw DescriptorException.noReferenceKeyIsSpecified(this);
        }

        for (Enumeration referenceEnum = getReferenceKeyFields().elements();
                 referenceEnum.hasMoreElements();) {
            DatabaseField field = (DatabaseField)referenceEnum.nextElement();
            if (field.hasTableName() && (!(field.getTableName().equals(getReferenceTable().getName())))) {
                throw DescriptorException.referenceKeyFieldNotProperlySpecified(field, this);
            }
            field.setTable(getReferenceTable());
        }
    }

    /*
     * Set the table qualifier on the reference table if required
     */
    protected void initializeReferenceTable(AbstractSession session) throws DescriptorException {
        Platform platform = session.getDatasourcePlatform();

        if (getReferenceTable() == null) {
            throw DescriptorException.referenceTableNotSpecified(this);
        }

        if (platform.getTableQualifier().length() == 0) {
            return;
        }

        if (getReferenceTable().getTableQualifier().length() == 0) {
            getReferenceTable().setTableQualifier(platform.getTableQualifier());
        }
    }

    protected void initializeSelectionCriteria(AbstractSession session) {
        Expression exp1;
        Expression exp2;
        Expression expression;
        Expression criteria = null;
        Enumeration referenceKeysEnum;
        Enumeration sourceKeysEnum;
        ExpressionBuilder base = new ExpressionBuilder();
        TableExpression table = (TableExpression)base.getTable(getReferenceTable());

        referenceKeysEnum = getReferenceKeyFields().elements();
        sourceKeysEnum = getSourceKeyFields().elements();

        for (; referenceKeysEnum.hasMoreElements();) {
            DatabaseField referenceKey = (DatabaseField)referenceKeysEnum.nextElement();
            DatabaseField sourceKey = (DatabaseField)sourceKeysEnum.nextElement();

            exp1 = table.getField(referenceKey);
            exp2 = base.getParameter(sourceKey);
            expression = exp1.equal(exp2);

            if (criteria == null) {
                criteria = expression;
            } else {
                criteria = expression.and(criteria);
            }
        }

        setSelectionCriteria(criteria);
    }

    /**
     * The selection query is initialized
     */
    protected void initializeSelectionQuery(AbstractSession session) {
        // Nothing required.
    }

    protected void initializeSelectionStatement(AbstractSession session) {
        SQLSelectStatement statement = new SQLSelectStatement();
        statement.addTable(getReferenceTable());
        statement.addField((DatabaseField)getDirectField().clone());
        statement.setWhereClause(getSelectionCriteria());
        statement.normalize(session, null);
        getSelectionQuery().setSQLStatement(statement);
    }

    /**
     * The source keys are initalized
     */
    protected void initializeSourceKeys(AbstractSession session) {
        for (Enumeration sourceKeyEnum = getSourceKeyFields().elements();
                 sourceKeyEnum.hasMoreElements();) {
            getDescriptor().buildField((DatabaseField)sourceKeyEnum.nextElement());
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
     */
    public boolean isDirectCollectionMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Checks if source and target keys are mentioned by the user or not.
     */
    protected boolean isKeyForSourceSpecified() {
        return !getSourceKeyFields().isEmpty();
    }

    /**
     * INTERNAL:
     * Return true if referenced objects are provately owned else false.
     */
    public boolean isPrivateOwned() {
        return true;
    }

    /**
     * INTERNAL:
     * Iterate on the attribute value.
     * The value holder has already been processed.
     * PERF: Avoid iteration if not required.
     */
    public void iterateOnRealAttributeValue(DescriptorIterator iterator, Object realAttributeValue) {
        if (iterator.shouldIterateOnPrimitives()) {
            super.iterateOnRealAttributeValue(iterator, realAttributeValue);
        }
    }

    /**
     * INTERNAL:
     * Iterate on the specified element.
     */
    public void iterateOnElement(DescriptorIterator iterator, Object element) {
        iterator.iteratePrimitiveForMapping(element, this);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     * Because this is a collection mapping, values are added to or removed from the
     * collection based on the changeset
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        ContainerPolicy containerPolicy = getContainerPolicy();
        Object valueOfTarget = null;
        AbstractSession session = mergeManager.getSession();

        //collect the changes into a vector
        HashMap addObjects = ((DirectCollectionChangeRecord)changeRecord).getAddObjectMap();
        HashMap removeObjects = ((DirectCollectionChangeRecord)changeRecord).getRemoveObjectMap();

        //Check to see if the target has an instantiated collection
        if ((isAttributeValueInstantiated(target)) && (!changeRecord.getOwner().isNew())) {
            valueOfTarget = getRealCollectionAttributeValueFromObject(target, session);
        } else {
            //if not create an instance of the collection
            valueOfTarget = containerPolicy.containerInstance(addObjects.size());
        }
        if (!isAttributeValueInstantiated(target)) {
            if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                return;
            }
            for (Object iterator = containerPolicy.iteratorFor(getRealCollectionAttributeValueFromObject(source, session));
                     containerPolicy.hasNext(iterator);) {
                containerPolicy.addInto(containerPolicy.next(iterator, session), valueOfTarget, session);
            }
        } else {
            synchronized (valueOfTarget) {
                // Next iterate over the changes and add them to the container
                for (Iterator iterator = addObjects.keySet().iterator(); iterator.hasNext(); ){
                    Object object = iterator.next();
                    int objectCount = ((Integer)addObjects.get(object)).intValue();
                    for (int i = 0; i < objectCount; ++i) {
                        if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                            //bug#4458089 and 4454532- check if collection contains new item before adding during merge into distributed cache					
                            if (!containerPolicy.contains(object, valueOfTarget, session)) {
                                containerPolicy.addInto(object, valueOfTarget, session);
                            }
                        } else {
                            containerPolicy.addInto(object, valueOfTarget, session);
                        }
                    }
                }
                for (Iterator iterator = removeObjects.keySet().iterator(); iterator.hasNext(); ){
                    Object object = iterator.next();
                    int objectCount = ((Integer)removeObjects.get(object)).intValue();
                    for (int i = 0; i < objectCount; ++i) {
                        containerPolicy.removeFrom(object, valueOfTarget, session);
                    }
                }
            }
        }
        setRealAttributeValueInObject(target, valueOfTarget);
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager) {
        if (isTargetUnInitialized) {
            // This will happen if the target object was removed from the cache before the commit was attempted
            if (mergeManager.shouldMergeWorkingCopyIntoOriginal() && (!isAttributeValueInstantiated(source))) {
                setAttributeValueInObject(target, getIndirectionPolicy().getOriginalIndirectionObject(getAttributeValueFromObject(source), mergeManager.getSession()));
                return;
            }
        }
        if (!shouldMergeCascadeReference(mergeManager)) {
            // This is only going to happen on mergeClone, and we should not attempt to merge the reference
            return;
        }
        if (mergeManager.shouldMergeOriginalIntoWorkingCopy()) {
            if (!isAttributeValueInstantiated(target)) {
                // This will occur when the clone's value has not been instantiated yet and we do not need
                // the refresh that attribute
                return;
            }
        } else if (!isAttributeValueInstantiated(source)) {
            // I am merging from a clone into an original.  No need to do merge if the attribute was never
            // modified
            return;
        }

        ContainerPolicy containerPolicy = getContainerPolicy();
        Object valueOfSource = getRealCollectionAttributeValueFromObject(source, mergeManager.getSession());

        // trigger instantiation of target attribute
        Object valueOfTarget = getRealCollectionAttributeValueFromObject(target, mergeManager.getSession());
        Object newContainer = containerPolicy.containerInstance(containerPolicy.sizeFor(valueOfSource));
        boolean fireChangeEvents = false;
        valueOfTarget = newContainer;
        for (Object sourceValuesIterator = containerPolicy.iteratorFor(valueOfSource);
                 containerPolicy.hasNext(sourceValuesIterator);) {
            Object sourceValue = containerPolicy.next(sourceValuesIterator, mergeManager.getSession());
            containerPolicy.addInto(sourceValue, valueOfTarget, mergeManager.getSession());
        }

        // Must re-set variable to allow for set method to re-morph changes if the collection is not being stored directly.
        setRealAttributeValueInObject(target, valueOfTarget);
    }

    /**
     * INTERNAL:
     * Perform the commit event.
     * This is used in the uow to delay data modifications.
     */
    public void performDataModificationEvent(Object[] event, AbstractSession session) throws DatabaseException, DescriptorException {
        // Hey I might actually want to use an inner class here... ok array for now.
        if (event[0] == Delete){
            session.executeQuery((DataModifyQuery)event[1], (AbstractRecord)event[(2)]);
        } else if (event[0] == Insert) {
            session.executeQuery((DataModifyQuery)event[1], (AbstractRecord)event[(2)]);
        } else if (event[0] == DeleteAll) {
            preDelete((DeleteObjectQuery)event[1]);
        } else {
            throw DescriptorException.invalidDataModificationEventCode(event[0], this);
        }
    }

    /**
     * INTERNAL:
     * Insert the private owned object.
     */
    public void postInsert(WriteObjectQuery query) throws DatabaseException {
        Object objects;
        AbstractRecord databaseRow = new DatabaseRecord();

        if (isReadOnly()) {
            return;
        }

        objects = getRealCollectionAttributeValueFromObject(query.getObject(), query.getSession());
        ContainerPolicy containerPolicy = getContainerPolicy();
        if (containerPolicy.isEmpty(objects)) {
            return;
        }

        prepareTranslationRow(query.getTranslationRow(), query.getObject(), query.getSession());
        // Extract primary key and value from the source.
        for (int index = 0; index < getReferenceKeyFields().size(); index++) {
            DatabaseField referenceKey = (DatabaseField)getReferenceKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object sourceKeyValue = query.getTranslationRow().get(sourceKey);
            databaseRow.put(referenceKey, sourceKeyValue);
        }

        // Extract target field and its value. Construct insert statement and execute it
        for (Object iter = containerPolicy.iteratorFor(objects); containerPolicy.hasNext(iter);) {
            Object object = containerPolicy.next(iter, query.getSession());
            if (getValueConverter() != null) {
                object = getValueConverter().convertObjectValueToDataValue(object, query.getSession());
            }
            databaseRow.put(getDirectField(), object);

            // In the uow data queries are cached until the end of the commit.
            if (query.shouldCascadeOnlyDependentParts()) {
                // Hey I might actually want to use an inner class here... ok array for now.
                Object[] event = new Object[3];
                event[0] = Insert;
                event[1] = getInsertQuery();
                event[2] = databaseRow.clone();
                query.getSession().getCommitManager().addDataModificationEvent(this, event);
            } else {
                query.getSession().executeQuery(getInsertQuery(), databaseRow);
            }
        }
    }

    /**
     * INTERNAL:
     * Update private owned part.
     */
    public void postUpdate(WriteObjectQuery writeQuery) throws DatabaseException {
        if (isReadOnly()) {
            return;
        }

        if (writeQuery.getObjectChangeSet() != null){
            postUpdateWithChangeSet(writeQuery);
            return;
        }
        // If objects are not instantiated that means they are not changed.
        if (!isAttributeValueInstantiated(writeQuery.getObject())) {
            return;
        }

        if (writeQuery.getSession().isUnitOfWork()) {
            if (compareObjects(writeQuery.getObject(), writeQuery.getBackupClone(), writeQuery.getSession())) {
                return;// Nothing has changed, no work required
            }
        }

        DeleteObjectQuery deleteQuery = new DeleteObjectQuery();
        deleteQuery.setObject(writeQuery.getObject());
        deleteQuery.setSession(writeQuery.getSession());
        deleteQuery.setTranslationRow(writeQuery.getTranslationRow());

        if (writeQuery.shouldCascadeOnlyDependentParts()) {
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[3];
            event[0] = DeleteAll;
            event[1] = deleteQuery;
            writeQuery.getSession().getCommitManager().addDataModificationEvent(this, event);
        } else {
            preDelete(deleteQuery);
        }
        postInsert(writeQuery);
    }

    /**
     * INTERNAL:
     * Update private owned part.
     */
    protected void postUpdateWithChangeSet(WriteObjectQuery writeQuery) throws DatabaseException {

        ObjectChangeSet changeSet = writeQuery.getObjectChangeSet();
        DirectCollectionChangeRecord changeRecord = (DirectCollectionChangeRecord)changeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (changeRecord == null){
            return;
        }
        for (int index = 0; index < getReferenceKeyFields().size(); index++) {
            DatabaseField referenceKey = (DatabaseField)getReferenceKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object sourceKeyValue = writeQuery.getTranslationRow().get(sourceKey);
            writeQuery.getTranslationRow().put(referenceKey, sourceKeyValue);
        }
        for (Iterator iterator = changeRecord.getRemoveObjectMap().keySet().iterator(); iterator.hasNext();){
            Object object = iterator.next();
            AbstractRecord thisRow = (AbstractRecord)writeQuery.getTranslationRow().clone();
            Object value = object;
            if (getValueConverter() != null){
                value = getValueConverter().convertObjectValueToDataValue(value, writeQuery.getSession());
            }
            if (value == DirectCollectionChangeRecord.Null){
                thisRow.add(getDirectField(), null);
            }else{
                thisRow.add(getDirectField(), value);
            }
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[3];
            event[0] = Delete;
            event[1] = getDeleteQuery();
            event[2] = thisRow;
            writeQuery.getSession().getCommitManager().addDataModificationEvent(this, event);
            Integer count = (Integer)changeRecord.getCommitAddMap().get(object);
            if (count != null){
                for (int counter = count.intValue(); counter > 0; --counter){
                    thisRow = (AbstractRecord)writeQuery.getTranslationRow().clone();
                    thisRow.add(getDirectField(), value);
                    // Hey I might actually want to use an inner class here... ok array for now.
                    event = new Object[3];
                    event[0] = Insert;
                    event[1] = getInsertQuery();
                    event[2] = thisRow;
                    writeQuery.getSession().getCommitManager().addDataModificationEvent(this, event);
                }
            }
        }
        for (Iterator iterator = changeRecord.getAddObjectMap().keySet().iterator(); iterator.hasNext();){
            Object object = iterator.next();
            Integer count = (Integer)changeRecord.getAddObjectMap().get(object);
            for (int counter = count.intValue(); counter > 0; --counter){
                AbstractRecord thisRow = (AbstractRecord)writeQuery.getTranslationRow().clone();
                Object value = object;
                if (getValueConverter() != null){
                    value = getValueConverter().convertObjectValueToDataValue(value, writeQuery.getSession());
                }
                if (value == DirectCollectionChangeRecord.Null){  //special placeholder for nulls
                    thisRow.add(getDirectField(), null);
                }else{
                    thisRow.add(getDirectField(), value);
                }
                // Hey I might actually want to use an inner class here... ok array for now.
                Object[] event = new Object[3];
                event[0] = Insert;
                event[1] = getInsertQuery();
                event[2] = thisRow;
                writeQuery.getSession().getCommitManager().addDataModificationEvent(this, event);
            }
        }
    }

    /**
     * INTERNAL:
     * Delete private owned part. Which is a collection of objects from the reference table.
     */
    public void preDelete(WriteObjectQuery query) throws DatabaseException {
        if (isReadOnly()) {
            return;
        }

        prepareTranslationRow(query.getTranslationRow(), query.getObject(), query.getSession());
        query.getSession().executeQuery(getDeleteAllQuery(), query.getTranslationRow());
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

    protected void setDeleteQuery(ModifyQuery query) {
        this.changeSetDeleteQuery = query;
    }

    /**
     * PUBLIC:
     * Set the receiver's delete SQL string. This allows the user to override the SQL
     * generated by TopLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row, through replacing the field names
     * marked by '#' with the values for those fields.
     * This SQL is responsible for doing the deletion required by the mapping,
     * such as deletion from join table for M-M.
     * Example, 'delete from RESPONS where EMP_ID = #EMP_ID and DESCRIP = #DESCRIP'.
     */
    public void setDeleteSQLString(String sqlString) {
        DataModifyQuery query = new DataModifyQuery();
        query.setSQLString(sqlString);
        setCustomDeleteQuery(query);
    }

    /**
     * ADVANCED:
     * Configure the mapping to use a container policy.
     * The policy manages the access to the collection.
     */
    public void setContainerPolicy(ContainerPolicy containerPolicy) {
        this.containerPolicy = containerPolicy;
        ((DataReadQuery)getSelectionQuery()).setContainerPolicy(containerPolicy);
    }

    /**
     * PUBLIC:
     * The default delete query for this mapping can be overridden by specifying the new query.
     * This query is responsible for doing the deletion required by the mapping,
     * such as deletion from join table for M-M.  The query should delete a specific row from the
     * DirectCollectionTable bases on the DirectField.
     */
    public void setCustomDeleteQuery(ModifyQuery query) {
        setDeleteQuery(query);
        setHasCustomDeleteQuery(true);
    }

    /**
     * PUBLIC:
     * The default insert query for mapping can be overridden by specifying the new query.
     * This query inserts the row into the direct table.
     */
    public void setCustomInsertQuery(DataModifyQuery query) {
        setInsertQuery(query);
        setHasCustomInsertQuery(true);
    }

    /**
     * PUBLIC:
     * Set the direct field in the reference table.
     * This is the field that the primitive data value is stored in.
     */
    public void setDirectField(DatabaseField field) {
        directField = field;
    }
    
    /**
     * ADVANCED:
     * Set the class type of the field value.
     * This can be used if field value differs from the object value,
     * has specific typing requirements such as usage of java.sql.Blob or NChar.
     * This must be called after the field name has been set.
     */
    public void setDirectFieldClassification(Class fieldType) {
        getDirectField().setType(fieldType);
    }

    /**
     * PUBLIC:
     * Set the direct field name in the reference table.
     * This is the field that the primitive data value is stored in.
     */
    public void setDirectFieldName(String fieldName) {
        setDirectField(new DatabaseField(fieldName));
    }

    protected void setHasCustomDeleteQuery(boolean bool) {
        hasCustomDeleteQuery = bool;
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
     * generated by TopLink, with there own SQL or procedure call. The arguments are
     * translated from the fields of the source row, through replacing the field names
     * marked by '#' with the values for those fields.
     * This is used to insert an entry into the direct table.
     * Example, 'insert into RESPONS (EMP_ID, RES_DESC) values (#EMP_ID, #RES_DESC)'.
     */
    public void setInsertSQLString(String sqlString) {
        DataModifyQuery query = new DataModifyQuery();
        query.setSQLString(sqlString);
        setCustomInsertQuery(query);
    }

    /**
     * INTERNAL:
     * This cannot be used with direct collection mappings.
     */
    public void setReferenceClass(Class referenceClass) {
        return;
    }

    public void setReferenceClassName(String referenceClassName) {
        return;
    }

    /**
     * PUBLIC:
     * Set the name of the reference key field.
     * This is the foreign key field in the direct table referencing the primary key of the source object.
     * This method is used if the reference key consists of only a single field.
     */
    public void setReferenceKeyFieldName(String fieldName) {
        getReferenceKeyFields().addElement(new DatabaseField(fieldName));
    }

    /**
     * INTERNAL:
     * Set the reference key field names associated with the mapping.
     * These must be in-order with the sourceKeyFieldNames.
     */
    public void setReferenceKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setReferenceKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Set the reference fields.
     */
    public void setReferenceKeyFields(Vector<DatabaseField> aVector) {
        this.referenceKeyFields = aVector;
    }

    /**
     * INTERNAL:
     * Set the reference table.
     */
    public void setReferenceTable(DatabaseTable table) {
        referenceTable = table;
    }

    /**
     * PUBLIC:
     * Set the reference table name.
     */
    public void setReferenceTableName(String tableName) {
        if (tableName == null) {
            setReferenceTable(null);
        } else {
            setReferenceTable(new DatabaseTable(tableName));
        }
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
    }

    /**
     * INTERNAL:
     * Set the source key field names associated with the mapping.
     * These must be in-order with the referenceKeyFieldNames.
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
     * INTERNAL:
     * Used by AttributeLevelChangeTracking to update a changeRecord with calculated changes
     * as apposed to detected changes.  If an attribute can not be change tracked it's
     * changes can be detected through this process.
     */
    public void calculateDeferredChanges(ChangeRecord changeRecord, AbstractSession session){
        DirectCollectionChangeRecord collectionRecord = (DirectCollectionChangeRecord) changeRecord;
        compareCollectionsForChange(collectionRecord.getOriginalCollection(), collectionRecord.getLatestCollection(), collectionRecord, session);
    }

    /**
     * ADVANCED:
     * This method is used to have an object add to a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.

     */
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object objectToAdd, ObjectChangeSet changeSet, AbstractSession session) {
        DirectCollectionChangeRecord collectionChangeRecord = (DirectCollectionChangeRecord)changeSet.getChangesForAttributeNamed(getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectCollectionChangeRecord(changeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            changeSet.addChange(collectionChangeRecord);
            Object collection = getRealAttributeValueFromObject(changeSet.getUnitOfWorkClone(), session);
            collectionChangeRecord.storeDatabaseCounts(collection, getContainerPolicy(), session);
        }
        collectionChangeRecord.addAdditionChange(objectToAdd, new Integer(1));
    }

   /**
     * ADVANCED:
     * This method is used to have an object removed from a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object objectToRemove, ObjectChangeSet changeSet, AbstractSession session) {
        DirectCollectionChangeRecord collectionChangeRecord = (DirectCollectionChangeRecord)changeSet.getChangesForAttributeNamed(getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectCollectionChangeRecord(changeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            changeSet.addChange(collectionChangeRecord);
            Object collection = getRealAttributeValueFromObject(changeSet.getUnitOfWorkClone(), session);
            collectionChangeRecord.storeDatabaseCounts(collection, getContainerPolicy(), session);
        }
        collectionChangeRecord.addRemoveChange(objectToRemove, new Integer(1));
    }

    /**
     * INTERNAL:
     * Either create a new change record or update with the new value.  This is used
     * by attribute change tracking.
     * Specifically in a collection mapping this will be called when the customer
     * Set a new collection.  In this case we will need to mark the change record
     * with the new and the old versions of the collection.
     * And mark the ObjectChangeSet with the attribute name then when the changes are calculated
     * force a compare on the collections to determine changes.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        DirectCollectionChangeRecord collectionChangeRecord = (DirectCollectionChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectCollectionChangeRecord(objectChangeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            objectChangeSet.addChange(collectionChangeRecord);
        }
        if (collectionChangeRecord.getOriginalCollection() == null){
            collectionChangeRecord.setOriginalCollection(oldValue);
        }
        collectionChangeRecord.setLatestCollection(newValue);
        
        objectChangeSet.deferredDetectionRequiredOn(getAttributeName());
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>jdk1.2.x: The container class must implement (directly or indirectly) the Collection interface.
     * <p>jdk1.1.x: The container class must be a subclass of Vector.
     */
    public void useCollectionClass(Class concreteClass) {
        ContainerPolicy policy = ContainerPolicy.buildPolicyFor(concreteClass);
        setContainerPolicy(policy);
    }

    /**
     * PUBLIC:
     * It is illegal to use a Map as the container of a DirectCollectionMapping. Only
     * Collection containers are supported for DirectCollectionMappings.
     * @see oracle.toplink.essentials.mappings.DirectMapMapping
     */
    public void useMapClass(Class concreteClass, String methodName) {
        throw ValidationException.illegalUseOfMapInDirectCollection(this, concreteClass, methodName);
    }

    /**
     * INTERNAL:
     * Return the value of the reference attribute or a value holder.
     * Check whether the mapping's attribute should be optimized through batch and joining.
     * Overridden to support flasback/historical queries.
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession session) throws DatabaseException {
        ReadQuery targetQuery = getSelectionQuery();
        return getIndirectionPolicy().valueFromQuery(targetQuery, row, query.getSession());
    }

    /**
     * INTERNAL:
     * Checks if object is deleted from the database or not.
     */
    public boolean verifyDelete(Object object, AbstractSession session) throws DatabaseException {
        // Row is built for translation
        if (isReadOnly()) {
            return true;
        }

        AbstractRecord row = getDescriptor().getObjectBuilder().buildRowForTranslation(object, session);
        Object value = session.executeQuery(getSelectionQuery(), row);

        return getContainerPolicy().isEmpty(value);
    }

    /**
     * INTERNAL:
     * Add a new value and its change set to the collection change record.  This is used by
     * attribute change tracking.
     */
    public void addToCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        if (newValue == null) {
            newValue = DirectCollectionChangeRecord.Null;
        }
        ClassDescriptor descriptor;
        DirectCollectionChangeRecord collectionChangeRecord = (DirectCollectionChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectCollectionChangeRecord(objectChangeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            objectChangeSet.addChange(collectionChangeRecord);
            Object collection = getRealAttributeValueFromObject(objectChangeSet.getUnitOfWorkClone(), uow);
            collectionChangeRecord.storeDatabaseCounts(collection, getContainerPolicy(), uow);
        }
        collectionChangeRecord.addAdditionChange(newValue, new Integer(1));
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
     * Return if this mapping supports change tracking.
     */
    public boolean isChangeTrackingSupported() {
        return true;
    }

    /**
     * INTERNAL:
     * Remove a value and its change set from the collection change record.  This is used by
     * attribute change tracking.
     */
    public void removeFromCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        if (newValue == null) {
            newValue = DirectCollectionChangeRecord.Null;
        }
        ClassDescriptor descriptor;
        DirectCollectionChangeRecord collectionChangeRecord = (DirectCollectionChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectCollectionChangeRecord(objectChangeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            objectChangeSet.addChange(collectionChangeRecord);
            Object collection = getRealAttributeValueFromObject(objectChangeSet.getUnitOfWorkClone(), uow);
            collectionChangeRecord.storeDatabaseCounts(collection, getContainerPolicy(), uow);
        }
        collectionChangeRecord.addRemoveChange(newValue, new Integer(1));
    }

}
