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
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.internal.identitymaps.CacheKey;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.indirection.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.mappings.converters.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * Mapping for a collection of key-value pairs.
 * The key and value must be simple types (String, Number, Date, etc.)
 * and stored in a single table along with a foreign key to the source object.
 * A converter can be used on the key and value if the desired object types
 * do not match the data types.
 *
 * @see Converter
 * @see ObjectTypeConverter
 * @see TypeConversionConverter
 * @see SerializedObjectConverter
 *
 * @author: Steven Vo
 * @since TopLink 3.5
 */
public class DirectMapMapping extends DirectCollectionMapping {

    /** The direct key field name is converted and stored */
    protected DatabaseField directKeyField;

    /** Allows user defined conversion between the object attribute value and the database value. */
    protected Converter keyConverter;

    /**
     * DirectMapCollectionMapping constructor
     */
    public DirectMapMapping() {
        super();
        this.selectionQuery = new DataReadQuery();
        this.containerPolicy = new DirectMapContainerPolicy(ClassConstants.Hashtable_Class);
    }

    /**
     * PUBLIC:
     * Return the converter on the mapping.
     * A converter can be used to convert between the key's object value and database value.
     */
    public Converter getKeyConverter() {
        return keyConverter;
    }

    /**
     * PUBLIC:
     * Set the converter on the mapping.
     * A converter can be used to convert between the key's object value and database value.
     */
    public void setKeyConverter(Converter keyConverter) {
        this.keyConverter = keyConverter;
    }
    
    /**
     * INTERNAL:
     * Add a new value and its change set to the collection change record.  This is used by
     * attribute change tracking.  If a value has changed then issue a remove first with the key
     * then an add.
     */
    public void addToCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        DirectMapChangeRecord collectionChangeRecord = (DirectMapChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectMapChangeRecord(objectChangeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            objectChangeSet.addChange(collectionChangeRecord);
        }
        collectionChangeRecord.addAdditionChange(newKey, newValue);
    }

    /**
     * INTERNAL:
     * Require for cloning, the part must be cloned.
     * Ignore the objects, use the attribute value.
     */
    public Object buildCloneForPartObject(Object attributeValue, Object original, Object clone, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        DirectMapContainerPolicy containerPolicy = (DirectMapContainerPolicy)getContainerPolicy();
        if (attributeValue == null) {
            return containerPolicy.containerInstance(1);
        }
        Object clonedAttributeValue = containerPolicy.containerInstance(containerPolicy.sizeFor(attributeValue));

        // I need to synchronize here to prevent the collection from changing while I am cloning it.
        // This will occur when I am merging into the cache and I am instantiating a UOW valueHolder at the same time
        // I can not synchronize around the clone, as this will cause deadlocks, so I will need to copy the collection then create the clones
        // I will use a temporary collection to help speed up the process
        Object temporaryCollection = null;
        synchronized (attributeValue) {
            temporaryCollection = containerPolicy.cloneFor(attributeValue);
        }

        for (Object keysIterator = containerPolicy.iteratorFor(temporaryCollection);
                 containerPolicy.hasNext(keysIterator);) {
            Object key = containerPolicy.next(keysIterator, unitOfWork);
            Object cloneKey = buildKeyClone(key, unitOfWork, isExisting);
            Object cloneValue = buildElementClone(containerPolicy.valueFromKey(key, temporaryCollection), unitOfWork, isExisting);
            containerPolicy.addInto(cloneKey, cloneValue, clonedAttributeValue, unitOfWork);
        }
        return clonedAttributeValue;
    }

    /**
     * INTERNAL:
     * Clone the key, if necessary.
     * DirectCollections hold on to objects that do not have Descriptors
     * (e.g. int, String). These objects do not need to be cloned, unless they use a converter - they
     * are immutable.
     */
    protected Object buildKeyClone(Object element, UnitOfWorkImpl unitOfWork, boolean isExisting) {
        Object cloneValue = element;
        if ((getKeyConverter() != null) && getKeyConverter().isMutable()) {
            cloneValue = getKeyConverter().convertDataValueToObjectValue(getKeyConverter().convertObjectValueToDataValue(cloneValue, unitOfWork), unitOfWork);
        }
        return cloneValue;
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

        DirectMapContainerPolicy cp = (DirectMapContainerPolicy)getContainerPolicy();

        cloneAttribute = getAttributeValueFromObject(clone);
        if ((cloneAttribute != null) && (!getIndirectionPolicy().objectIsInstantiated(cloneAttribute))) {
            return null;
        }

        Map cloneObjectCollection = (Map)getRealCollectionAttributeValueFromObject(clone, session);
        HashMap originalKeyValues = new HashMap(10);
        HashMap cloneKeyValues = new HashMap(10);

        if (!owner.isNew()) {
            backUpAttribute = getAttributeValueFromObject(backUp);
            if ((backUpAttribute == null) && (cloneAttribute == null)) {
                return null;
            }
            Map backUpCollection = (Map)getRealCollectionAttributeValueFromObject(backUp, session);
            Object backUpIter = cp.iteratorFor(backUpCollection);
            while (cp.hasNext(backUpIter)) {// Make a lookup of the objects
                Object key = cp.next(backUpIter, session);
                originalKeyValues.put(key, backUpCollection.get(key));
            }
        }
        Object cloneIter = cp.iteratorFor(cloneObjectCollection);
        while (cp.hasNext(cloneIter)) {//Compare them with the objects from the clone
            Object firstObject = cp.next(cloneIter, session);
            Object firstValue = cloneObjectCollection.get(firstObject);
            Object backupValue = originalKeyValues.get(firstObject);
            if ( ! originalKeyValues.containsKey(firstObject) ){
                cloneKeyValues.put(firstObject, cloneObjectCollection.get(firstObject));
            }else if ( (backupValue == null && firstValue != null) || (!backupValue.equals(firstValue)) ) {//the object was not in the backup
                cloneKeyValues.put(firstObject, cloneObjectCollection.get(firstObject));
            }else{
                originalKeyValues.remove(firstObject);
            }
        }
        if (cloneKeyValues.isEmpty() && originalKeyValues.isEmpty() && (!owner.isNew())) {
            return null;
        }
        DirectMapChangeRecord changeRecord = new DirectMapChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        changeRecord.addAdditionChange(cloneKeyValues);
        changeRecord.addRemoveChange(originalKeyValues);
        return changeRecord;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        Object firstObjectMap = getRealCollectionAttributeValueFromObject(firstObject, session);
        Object secondObjectMap = getRealCollectionAttributeValueFromObject(secondObject, session);
        DirectMapContainerPolicy mapContainerPolicy = (DirectMapContainerPolicy)getContainerPolicy();

        return mapContainerPolicy.compareContainers(firstObjectMap, secondObjectMap);
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
        
        if (keyConverter != null) {
            if (keyConverter instanceof TypeConversionConverter){
                ((TypeConversionConverter)keyConverter).convertClassNamesToClasses(classLoader);
            } else if (keyConverter instanceof ObjectTypeConverter) {
                // To avoid 1.5 dependencies with the EnumTypeConverter check
                // against ObjectTypeConverter.
                ((ObjectTypeConverter) keyConverter).convertClassNamesToClasses(classLoader);
            }
        }
    };

    /**
     * INTERNAL:
     */
    public DatabaseField getDirectKeyField() {
        return directKeyField;
    }

    /**
     * INTERNAL:
     * Initialize and validate the mapping properties.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        initializeDirectKeyField(session);
        initializeContainerPolicy(session);
        if (getKeyConverter() != null) {
            getKeyConverter().initialize(this, session);
        }
        if (getValueConverter() != null) {
            getValueConverter().initialize(this, session);
        }
    }

    /**
     * set the key and value fields that are used to build the container from database rows
     */
    protected void initializeContainerPolicy(AbstractSession session) {
        ((DirectMapContainerPolicy)getContainerPolicy()).setKeyField(getDirectKeyField());
        ((DirectMapContainerPolicy)getContainerPolicy()).setValueField(getDirectField());
        ((DirectMapContainerPolicy)getContainerPolicy()).setKeyConverter(getKeyConverter());
        ((DirectMapContainerPolicy)getContainerPolicy()).setValueConverter(getValueConverter());
    }

    protected void initializeDeleteQuery(AbstractSession session) {
        if (!getDeleteQuery().hasSessionName()) {
            getDeleteQuery().setSessionName(session.getName());
        }

        if (hasCustomDeleteQuery()) {
            return;
        }

        Expression builder = new ExpressionBuilder();
        Expression directKeyExp = builder.getField(getDirectKeyField()).equal(builder.getParameter(getDirectKeyField()));
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
        expression = expression.and(directKeyExp);
        statement.setWhereClause(expression);
        statement.setTable(getReferenceTable());
        getDeleteQuery().setSQLStatement(statement);
    }

    /**
     * The field name on the reference table is initialized and cached.
     */
    protected void initializeDirectKeyField(AbstractSession session) throws DescriptorException {
        if (getDirectKeyField() == null) {
            throw DescriptorException.directFieldNameNotSet(this);
        }

        getDirectKeyField().setTable(getReferenceTable());
        getDirectKeyField().setIndex(1);
    }

    /**
     * Initialize insert query. This query is used to insert the collection of objects into the
     * reference table.
     */
    protected void initializeInsertQuery(AbstractSession session) {
        super.initializeInsertQuery(session);
        getInsertQuery().getModifyRow().put(getDirectKeyField(), null);
    }

    protected void initializeSelectionStatement(AbstractSession session) {
        SQLSelectStatement statement = new SQLSelectStatement();
        statement.addTable(getReferenceTable());
        statement.addField((DatabaseField)getDirectField().clone());
        statement.addField((DatabaseField)getDirectKeyField().clone());
        statement.setWhereClause(getSelectionCriteria());
        statement.normalize(session, null);
        getSelectionQuery().setSQLStatement(statement);
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isDirectMapMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     * Because this is a collection mapping, values are added to or removed from the
     * collection based on the changeset
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        DirectMapContainerPolicy containerPolicy = (DirectMapContainerPolicy)getContainerPolicy();
        Map valueOfTarget = null;
        AbstractSession session = mergeManager.getSession();

        //collect the changes into a vector
        HashMap addObjects = ((DirectMapChangeRecord)changeRecord).getAddObjects();
        HashMap removeObjects = ((DirectMapChangeRecord)changeRecord).getRemoveObjects();

        //Check to see if the target has an instantiated collection
        if ((isAttributeValueInstantiated(target)) && (!changeRecord.getOwner().isNew())) {
            valueOfTarget = (Map)getRealCollectionAttributeValueFromObject(target, session);
        } else {
            //if not create an instance of the map
            valueOfTarget = (Map)containerPolicy.containerInstance(addObjects.size());
        }

        if (!isAttributeValueInstantiated(target)) {
            if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                return;
            }

            Object valueOfSource = getRealCollectionAttributeValueFromObject(source, session);
            for (Object iterator = containerPolicy.iteratorFor(valueOfSource);
                     containerPolicy.hasNext(iterator);) {
                Object key = containerPolicy.next(iterator, session);
                containerPolicy.addInto(key, ((Map)valueOfSource).get(key), valueOfTarget, session);
            }

        } else {
            synchronized (valueOfTarget) {
                // Next iterate over the changes and add them to the container
                for (Iterator i = removeObjects.keySet().iterator(); i.hasNext(); ) {
                    Object keyToRemove = i.next();
                    containerPolicy.removeFrom(keyToRemove, (Object)null, valueOfTarget, session);
                }

                for (Iterator i = addObjects.keySet().iterator(); i.hasNext(); ) {
                    Object keyToAdd = i.next();
                    Object nextItem = addObjects.get(keyToAdd);
                    if (mergeManager.shouldMergeChangesIntoDistributedCache()) {
                        //bug#4458089 and 4454532- check if collection contains new item before adding during merge into distributed cache															
                        if (!containerPolicy.contains(nextItem, valueOfTarget, session)) {
                            containerPolicy.addInto(keyToAdd, nextItem, valueOfTarget, session);
                        }
                    } else {
                        containerPolicy.addInto(keyToAdd, nextItem, valueOfTarget, session);
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

        Map valueOfSource = (Map)getRealCollectionAttributeValueFromObject(source, mergeManager.getSession());

        DirectMapContainerPolicy containerPolicy = (DirectMapContainerPolicy)getContainerPolicy();

        // trigger instantiation of target attribute
        Object valueOfTarget = getRealCollectionAttributeValueFromObject(target, mergeManager.getSession());
        Object newContainer = containerPolicy.containerInstance(containerPolicy.sizeFor(valueOfSource));

        boolean fireChangeEvents = false;
        valueOfTarget = newContainer;
        
        for (Object sourceValuesIterator = containerPolicy.iteratorFor(valueOfSource);
                 containerPolicy.hasNext(sourceValuesIterator);) {
            Object sourceKey = containerPolicy.next(sourceValuesIterator, mergeManager.getSession());
            containerPolicy.addInto(sourceKey, valueOfSource.get(sourceKey), valueOfTarget, mergeManager.getSession());
        }

        // Must re-set variable to allow for set method to re-morph changes if the collection is not being stored directly.
        setRealAttributeValueInObject(target, valueOfTarget);
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
        DirectMapContainerPolicy containerPolicy = (DirectMapContainerPolicy)getContainerPolicy();
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
        Object keyIter = containerPolicy.iteratorFor(objects);
        while (containerPolicy.hasNext(keyIter)) {
            Object key = containerPolicy.next(keyIter, query.getSession());
            Object value = containerPolicy.valueFromKey(key, objects);
            if (getKeyConverter() != null) {
                key = getKeyConverter().convertObjectValueToDataValue(key, query.getSession());
            }
            if (getValueConverter() != null) {
                value = getValueConverter().convertObjectValueToDataValue(value, query.getSession());
            }
            databaseRow.put(getDirectKeyField(), key);
            databaseRow.put(getDirectField(), value);
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
    protected void postUpdateWithChangeSet(WriteObjectQuery writeQuery) throws DatabaseException {

        ObjectChangeSet changeSet = writeQuery.getObjectChangeSet();
        DirectMapChangeRecord changeRecord = (DirectMapChangeRecord)changeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (changeRecord == null){
            return;
        }
        for (int index = 0; index < getReferenceKeyFields().size(); index++) {
            DatabaseField referenceKey = (DatabaseField)getReferenceKeyFields().elementAt(index);
            DatabaseField sourceKey = (DatabaseField)getSourceKeyFields().elementAt(index);
            Object sourceKeyValue = writeQuery.getTranslationRow().get(sourceKey);
            writeQuery.getTranslationRow().put(referenceKey, sourceKeyValue);
        }
        for (Iterator iterator = changeRecord.getRemoveObjects().keySet().iterator(); iterator.hasNext();){
            Object key = iterator.next();
            AbstractRecord thisRow = (AbstractRecord)writeQuery.getTranslationRow().clone();
            if (getKeyConverter() != null){
                key = getKeyConverter().convertObjectValueToDataValue(key, writeQuery.getSession());
            }
            thisRow.add(getDirectKeyField(), key);
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[3];
            event[0] = Delete;
            event[1] = getDeleteQuery();
            event[2] = thisRow;
            writeQuery.getSession().getCommitManager().addDataModificationEvent(this, event);
        }
        for (Iterator iterator = changeRecord.getAddObjects().keySet().iterator(); iterator.hasNext();){
            Object key = iterator.next();
            AbstractRecord thisRow = (AbstractRecord)writeQuery.getTranslationRow().clone();
            Object value = changeRecord.getAddObjects().get(key);
            if (getKeyConverter() != null){
                key = getKeyConverter().convertObjectValueToDataValue(key, writeQuery.getSession());
            }
            if (getValueConverter() != null){
                value = getValueConverter().convertObjectValueToDataValue(value, writeQuery.getSession());
            }
            thisRow.add(getDirectKeyField(), key);
            thisRow.add(getDirectField(), value);
            // Hey I might actually want to use an inner class here... ok array for now.
            Object[] event = new Object[3];
            event[0] = Insert;
            event[1] = getInsertQuery();
            event[2] = thisRow;
            writeQuery.getSession().getCommitManager().addDataModificationEvent(this, event);
        }
    }

    /**
     * INTERNAL:
     * Remove a value and its change set from the collection change record.  This is used by
     * attribute change tracking.
     */
    public void removeFromCollectionChangeRecord(Object newKey, Object newValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        DirectMapChangeRecord collectionChangeRecord = (DirectMapChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectMapChangeRecord(objectChangeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            objectChangeSet.addChange(collectionChangeRecord);
        }
        collectionChangeRecord.addRemoveChange(newKey, newValue);
    }

    /**
     * INTERNAL:
     */
    public void setDirectKeyField(DatabaseField keyField) {
        directKeyField = keyField;
    }

    /**
     * PUBLIC:
     * Set the direct key field name in the reference table.
     * This is the field that the primitive data value of the Map key is stored in.
     */
    public void setDirectKeyFieldName(String fieldName) {
        setDirectKeyField(new DatabaseField(fieldName));
    }

    /**
     * INTERNAL:
     * Either create a new change record or update the change record with the new value.
     * This is used by attribute change tracking.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) throws DescriptorException {
        DirectMapChangeRecord collectionChangeRecord = (DirectMapChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectMapChangeRecord(objectChangeSet);
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
     * <p>The default container class is java.util.Hashtable.
     * <p>The container class must implements (directly or indirectly) the Map interface.
     * <p>Note: Do not use both useMapClass(Class concreteClass), useTransparentMap().  The last use of one of the two methods will overide the previous one.
     */
    public void useMapClass(Class concreteClass) {
        if (!Helper.classImplementsInterface(concreteClass, ClassConstants.Map_Class)) {
            throw DescriptorException.illegalContainerClass(concreteClass);
        }
        DirectMapContainerPolicy policy = new DirectMapContainerPolicy(concreteClass);
        setContainerPolicy(policy);
    }

    /**
     * PUBLIC:
     * Configure the mapping to use an instance of the specified container class
     * to hold the target objects.
     * <p>jdk1.2.x: The container class must implement (directly or indirectly) the Map interface.
     * <p>jdk1.1.x: The container class must be a subclass of Hashtable.
     * <p>Note: Do not use both useMapClass(Class concreteClass), useTransparentMap().  The last use of one of the two methods will overide the previous one.
     */
    public void useTransparentMap() {
        setIndirectionPolicy(new TransparentIndirectionPolicy());
        useMapClass(ClassConstants.IndirectMap_Class);
    }

    /**
     * PUBLIC:
     * This is a helper method to set the key converter to a TypeConversionConverter.
     * This ensures that the key value from the database is converted to the correct
     * Java type.  The converter can also be set directly.
     * Note that setting the converter to another converter will overwrite this setting.
     */
    public void setKeyClass(Class keyClass) {
        TypeConversionConverter converter = new TypeConversionConverter(this);
        converter.setObjectClass(keyClass);
        setKeyConverter(converter);
    }

    /**
     * PUBLIC:
     * This is a helper method to get the object class from the key converter
     * if it is a TypeConversionConverter.
     * This returns null if not using a TypeConversionConverter key converter.
     */
    public Class getKeyClass() {
        if (!(getKeyConverter() instanceof TypeConversionConverter)) {
            return null;
        }
        return ((TypeConversionConverter)getKeyConverter()).getObjectClass();
    }

    /**
     * PUBLIC:
     * This is a helper method to set the value converter to a TypeConversionConverter.
     * This ensures that the value from the database is converted to the correct
     * Java type.  The converter can also be set directly.
     * Note that setting the converter to another converter will overwrite this setting.
     */
    public void setValueClass(Class valueClass) {
        TypeConversionConverter converter = new TypeConversionConverter(this);
        converter.setObjectClass(valueClass);
        setValueConverter(converter);
    }

    /**
     * ADVANCED:
     * This method is used to have an object add to a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleAddToCollectionChangeRecord(Object referenceKey, Object objectToAdd, ObjectChangeSet changeSet, AbstractSession session) {
        DirectMapChangeRecord collectionChangeRecord = (DirectMapChangeRecord)changeSet.getChangesForAttributeNamed(getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectMapChangeRecord(changeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            collectionChangeRecord.getAddObjects().put(referenceKey, objectToAdd);
            changeSet.addChange(collectionChangeRecord);
        } else {
            if (collectionChangeRecord.getRemoveObjects().containsKey(referenceKey)) {
                collectionChangeRecord.getRemoveObjects().remove(referenceKey);
            } else {
                collectionChangeRecord.getAddObjects().put(referenceKey, objectToAdd);
            }
        }
    }

    /**
     * ADVANCED:
     * This method is used to have an object removed from a collection once the changeSet is applied
     * The referenceKey parameter should only be used for direct Maps.
     */
    public void simpleRemoveFromCollectionChangeRecord(Object referenceKey, Object objectToRemove, ObjectChangeSet changeSet, AbstractSession session) {
        DirectMapChangeRecord collectionChangeRecord = (DirectMapChangeRecord)changeSet.getChangesForAttributeNamed(getAttributeName());
        if (collectionChangeRecord == null) {
            collectionChangeRecord = new DirectMapChangeRecord(changeSet);
            collectionChangeRecord.setAttribute(getAttributeName());
            collectionChangeRecord.setMapping(this);
            collectionChangeRecord.getRemoveObjects().put(referenceKey, objectToRemove);
            changeSet.addChange(collectionChangeRecord);
        } else {
            if (collectionChangeRecord.getAddObjects().containsKey(referenceKey)) {
                collectionChangeRecord.getAddObjects().remove(referenceKey);
            } else {
                collectionChangeRecord.getRemoveObjects().put(referenceKey, objectToRemove);
            }
        }
    }

    /**
     * PUBLIC:
     * This is a helper method to get the object class from the value converter
     * if it is a TypeConversionConverter.
     * This returns null if not using a TypeConversionConverter value converter.
     */
    public Class getValueClass() {
        if (!(getValueConverter() instanceof TypeConversionConverter)) {
            return null;
        }
        return ((TypeConversionConverter)getValueConverter()).getObjectClass();
    }

    /**
     * INTERNAL:
     * Ovewrite super method
     */
    public Object extractResultFromBatchQuery(DatabaseQuery query, AbstractRecord databaseRow, AbstractSession session, AbstractRecord argumentRow) {
        //this can be null, because either one exists in the query or it will be created
        Hashtable referenceDataByKey = null;
        ContainerPolicy mappingContainerPolicy = getContainerPolicy();
        synchronized (query) {
            referenceDataByKey = (Hashtable)query.getProperty("batched objects");
            mappingContainerPolicy = getContainerPolicy();
            if (referenceDataByKey == null) {
                Vector rows = (Vector)session.executeQuery(query, argumentRow);

                referenceDataByKey = new Hashtable();

                for (Enumeration rowsEnum = rows.elements(); rowsEnum.hasMoreElements();) {
                    AbstractRecord referenceRow = (AbstractRecord)rowsEnum.nextElement();
                    Object referenceKey = referenceRow.get(getDirectKeyField());
                    Object referenceValue = referenceRow.get(getDirectField());
                    CacheKey eachCacheKey = new CacheKey(extractKeyFromReferenceRow(referenceRow, session));

                    Object container = referenceDataByKey.get(eachCacheKey);
                    if (container == null) {
                        container = mappingContainerPolicy.containerInstance();
                        referenceDataByKey.put(eachCacheKey, container);
                    }

                    // Allow for key conversion.
                    if (getKeyConverter() != null) {
                        referenceKey = getKeyConverter().convertDataValueToObjectValue(referenceKey, query.getSession());
                    }

                    // Allow for value conversion.
                    if (getValueConverter() != null) {
                        referenceValue = getValueConverter().convertDataValueToObjectValue(referenceValue, query.getSession());
                    }

                    mappingContainerPolicy.addInto(referenceKey, referenceValue, container, query.getSession());
                }

                query.setProperty("batched objects", referenceDataByKey);
            }
        }
        Object result = referenceDataByKey.get(new CacheKey(extractPrimaryKeyFromRow(databaseRow, session)));

        // The source object might not have any target objects
        if (result == null) {
            return mappingContainerPolicy.containerInstance();
        } else {
            return result;
        }
    }
}
