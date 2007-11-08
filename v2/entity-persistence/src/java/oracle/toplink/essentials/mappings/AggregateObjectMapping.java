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
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.internal.descriptors.ObjectBuilder;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>:Two objects can be considered to be related by aggregation if there is a strict
 * 1:1 relationship between the objects. This means that if the source (parent)
 * object exists, then the target (child or owned) object must exist.
 *
 * In TopLink, it also means the data for the owned object is in the same table as
 * the parent.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class AggregateObjectMapping extends AggregateMapping implements RelationalMapping {

    /**
     * If <em>all</em> the fields in the database row for the aggregate object are NULL,
     * then, by default, TopLink will place a null in the appropriate source object
     * (as opposed to an aggregate object filled with nulls).
     * To change this behavior, set the value of this variable to false. Then TopLink
     * will build a new instance of the aggregate object that is filled with nulls
     * and place it in the source object.
     */
    protected boolean isNullAllowed;

    /** Map the name of a field in the aggregate descriptor to a field in the source table. */
    protected transient Map<String, String> aggregateToSourceFieldNames;

    /**
     * Default constructor.
     */
    public AggregateObjectMapping() {
        aggregateToSourceFieldNames = new HashMap(5);
        isNullAllowed = true;
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * Add a field name translation that maps from a field name in the
     * source table to a field name in the aggregate descriptor.
     */
    public void addFieldNameTranslation(String sourceFieldName, String aggregateFieldName) {
        String unQualifiedAggregateFieldName = aggregateFieldName.substring(aggregateFieldName.lastIndexOf('.') + 1);// -1 is returned for no ".".
        getAggregateToSourceFieldNames().put(unQualifiedAggregateFieldName, sourceFieldName);
    }

    /**
     * INTERNAL:
     * Return whether all the aggregate fields in the specified
     * row are NULL.
     */
    protected boolean allAggregateFieldsAreNull(AbstractRecord databaseRow) {
        for (Enumeration fieldsEnum = getReferenceFields().elements();
                 fieldsEnum.hasMoreElements();) {
            DatabaseField field = (DatabaseField)fieldsEnum.nextElement();
            Object value = databaseRow.get(field);
            if (value != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * PUBLIC:
     * If <em>all</em> the fields in the database row for the aggregate object are NULL,
     * then, by default, TopLink will place a null in the appropriate source object
     * (as opposed to an aggregate object filled with nulls). This behavior can be
     * explicitly set by calling #allowNull().
     * To change this behavior, call #dontAllowNull(). Then TopLink
     * will build a new instance of the aggregate object that is filled with nulls
     * and place it in the source object.
     * In either situation, when writing, TopLink will place a NULL in all the
     * fields in the database row for the aggregate object.
     */
    public void allowNull() {
        setIsNullAllowed(true);
    }

    /**
     * INTERNAL:
     * Return whether the query's backup object has an attribute
     * value of null.
     */
    protected boolean backupAttributeValueIsNull(WriteObjectQuery query) {
        if (query.getSession().isUnitOfWork()) {
            Object backupAttributeValue = getAttributeValueFromObject(query.getBackupClone());
            if (backupAttributeValue == null) {
                return true;
            }
        }
        return false;
    }

    /**
     * INTERNAL:
     * Build and return an aggregate object from the specified row.
     * If a null value is allowed and
     * all the appropriate fields in the row are NULL, return a null.
     * Otherwise, simply create a new aggregate object and return it.
     */
    protected Object buildAggregateFromRow(AbstractRecord databaseRow, Object targetObject, JoinedAttributeManager joinManager, boolean buildShallowOriginal, AbstractSession session) throws DatabaseException {
        // check for all NULLs
        if (isNullAllowed() && allAggregateFieldsAreNull(databaseRow)) {
            return null;
        }

        // If refreshing, maintain object identity;
        // otherwise construct a new aggregate object.
        Object aggregate = null;
        ClassDescriptor descriptor = getReferenceDescriptor();
        boolean refreshing = true;
        if (descriptor.hasInheritance()) {
            Class newAggregateClass = descriptor.getInheritancePolicy().classFromRow(databaseRow, session);
            descriptor = getReferenceDescriptor(newAggregateClass, session);

            if (joinManager.getBaseQuery().shouldRefreshIdentityMapResult()) {
                aggregate = getMatchingAttributeValueFromObject(databaseRow, targetObject, session, descriptor);
                if ((aggregate != null) && (aggregate.getClass() != newAggregateClass)) {
                    // if the class has changed out from underneath us, we cannot preserve object identity
                    // build a new instance of the *new* class
                    aggregate = descriptor.getObjectBuilder().buildNewInstance();
                    refreshing = false;
                }
            }
        } else {
            if (joinManager.getBaseQuery().shouldRefreshIdentityMapResult()) {
                aggregate = getMatchingAttributeValueFromObject(databaseRow, targetObject, session, descriptor);
            }
        }

        if (aggregate == null) {
            aggregate = descriptor.getObjectBuilder().buildNewInstance();
            refreshing = false;
        }

        ObjectBuildingQuery nestedQuery = joinManager.getBaseQuery();
        nestedQuery.setSession(session); //ensure the correct session is set on the query.
        if (joinManager.getBaseQuery().isObjectLevelReadQuery()){
            if (joinManager.isAttributeJoined(getDescriptor(), getAttributeName()) ){
                // A nested query must be built to pass to the descriptor that looks like the real query execution would.
                nestedQuery = (ObjectLevelReadQuery)((ObjectLevelReadQuery)joinManager.getBaseQuery()).deepClone();
                // Must cascade the nested partial/join expression and filter the nested ones.
                ((ObjectLevelReadQuery)nestedQuery).getJoinedAttributeManager().setJoinedAttributeExpressions_(extractNestedExpressions(joinManager.getJoinedAttributeExpressions(), joinManager.getBaseExpressionBuilder(), false));
                nestedQuery.setDescriptor(descriptor);
            }
        }
        if (buildShallowOriginal) {
            descriptor.getObjectBuilder().buildAttributesIntoShallowObject(aggregate, databaseRow, nestedQuery);
        } else if (session.isUnitOfWork()) {
            descriptor.getObjectBuilder().buildAttributesIntoWorkingCopyClone(aggregate, nestedQuery, joinManager, databaseRow, (UnitOfWorkImpl)session, refreshing);
        } else {
            descriptor.getObjectBuilder().buildAttributesIntoObject(aggregate, databaseRow, nestedQuery, joinManager, refreshing);
        }
        return aggregate;
    }

    /**
     * INTERNAL:
     * Build and return a database row with all the reference
     * fields set to nulls.
     */
    protected AbstractRecord buildNullReferenceRow() {
        AbstractRecord result = new DatabaseRecord(getReferenceFields().size());
        for (Enumeration stream = getReferenceFields().elements(); stream.hasMoreElements();) {
            result.put((DatabaseField)stream.nextElement(), null);
        }
        return result;
    }

    /**
     * INTERNAL:
     * Used to allow object level comparisons.
     * In the case of an Aggregate which has no primary key must do an attribute
     * by attribute comparison.
     */
    public Expression buildObjectJoinExpression(Expression expression, Object value, AbstractSession session) {
        Expression attributeByAttributeComparison = null;
        Expression join = null;
        Object attributeValue = null;

        // value need not be unwrapped as it is an aggregate, nor should it
        // influence a call to getReferenceDescriptor.
        ClassDescriptor referenceDescriptor = getReferenceDescriptor();
        if ((value != null) && !referenceDescriptor.getJavaClass().isInstance(value)) {
            throw QueryException.incorrectClassForObjectComparison(expression, value, this);
        }
        Enumeration mappings = referenceDescriptor.getMappings().elements();
        for (; mappings.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();
            if (value == null) {
                attributeValue = null;
            } else {
                attributeValue = mapping.getAttributeValueFromObject(value);
            }
            join = expression.get(mapping.getAttributeName()).equal(attributeValue);
            if (attributeByAttributeComparison == null) {
                attributeByAttributeComparison = join;
            } else {
                attributeByAttributeComparison = attributeByAttributeComparison.and(join);
            }
        }
        return attributeByAttributeComparison;
    }

    /**
     * INTERNAL:
     * Used to allow object level comparisons.
     */
    public Expression buildObjectJoinExpression(Expression expression, Expression argument, AbstractSession session) {
        Expression attributeByAttributeComparison = null;

        //Enumeration mappingsEnum = getSourceToTargetKeyFields().elements();
        Enumeration mappingsEnum = getReferenceDescriptor().getMappings().elements();
        for (; mappingsEnum.hasMoreElements();) {
            DatabaseMapping mapping = (DatabaseMapping)mappingsEnum.nextElement();
            String attributeName = mapping.getAttributeName();
            Expression join = expression.get(attributeName).equal(argument.get(attributeName));
            if (attributeByAttributeComparison == null) {
                attributeByAttributeComparison = join;
            } else {
                attributeByAttributeComparison = attributeByAttributeComparison.and(join);
            }
        }
        return attributeByAttributeComparison;
    }

    /**
     * INTERNAL:
     * Build and return a database row built with the values from
     * the specified attribute value.
     */
    protected AbstractRecord buildRowFromAggregate(Object object, Object attributeValue, AbstractSession session) throws DescriptorException {
        return buildRowFromAggregate(object, attributeValue, session, false);
    }

    /**
     * INTERNAL:
     * Build and return a database row built with the values from
     * the specified attribute value.
     */
    protected AbstractRecord buildRowFromAggregate(Object object, Object attributeValue, AbstractSession session, boolean forceWriteOfReadOnlyClasses) throws DescriptorException {
        if (attributeValue == null) {
            if (isNullAllowed()) {
                return buildNullReferenceRow();
            } else {
                throw DescriptorException.nullForNonNullAggregate(object, this);
            }
        } else {
            if ((!forceWriteOfReadOnlyClasses) && (session.isClassReadOnly(attributeValue.getClass()))) {
                return new DatabaseRecord(1);
            } else {
                return getObjectBuilder(attributeValue, session).buildRow(attributeValue, session);
            }
        }
    }

    /**
     * INTERNAL:
     * Build and return a database row built with the values from
     * the specified attribute value.
     */
    protected AbstractRecord buildRowFromAggregateWithChangeRecord(ChangeRecord changeRecord, ObjectChangeSet objectChangeSet, AbstractSession session) throws DescriptorException {
        return buildRowFromAggregateWithChangeRecord(changeRecord, objectChangeSet, session, false);
    }

    /**
     * INTERNAL:
     * Build and return a database row built with the values from
     * the specified attribute value.
     */
    protected AbstractRecord buildRowFromAggregateWithChangeRecord(ChangeRecord changeRecord, ObjectChangeSet objectChangeSet, AbstractSession session, boolean forceWriteOfReadOnlyClasses) throws DescriptorException {
        if (objectChangeSet == null) {
            if (isNullAllowed()) {
                return buildNullReferenceRow();
            } else {
                Object object = ((ObjectChangeSet)changeRecord.getOwner()).getUnitOfWorkClone();
                throw DescriptorException.nullForNonNullAggregate(object, this);
            }
        } else {
            if ((!forceWriteOfReadOnlyClasses) && (session.isClassReadOnly(objectChangeSet.getClassType(session)))) {
                return new DatabaseRecord(1);
            } else {
                return getReferenceDescriptor(objectChangeSet.getClassType(session), session).getObjectBuilder().buildRowWithChangeSet(objectChangeSet, session);
            }
        }
    }

    /**
     * INTERNAL:
     * Build and return a database row built with the changed values from
     * the specified attribute value.
     */
    protected AbstractRecord buildRowFromAggregateForUpdate(WriteObjectQuery query, Object attributeValue) throws DescriptorException {
        if (attributeValue == null) {
            if (isNullAllowed()) {
                if (backupAttributeValueIsNull(query)) {
                    return new DatabaseRecord(1);// both attributes are null - no update required
                } else {
                    return buildNullReferenceRow();
                }
            } else {
                throw DescriptorException.nullForNonNullAggregate(query.getObject(), this);
            }
        } else if ((query.getBackupClone() != null) && ((getMatchingBackupAttributeValue(query, attributeValue) == null) || !(attributeValue.getClass().equals(getMatchingBackupAttributeValue(query, attributeValue).getClass())))) {
            return getObjectBuilder(attributeValue, query.getSession()).buildRow(attributeValue, query.getSession());
        } else {
            if (query.getSession().isClassReadOnly(attributeValue.getClass())) {
                return new DatabaseRecord(1);
            }
            WriteObjectQuery clonedQuery = (WriteObjectQuery)query.clone();
            clonedQuery.setObject(attributeValue);
            if (query.getSession().isUnitOfWork()) {
                Object backupAttributeValue = getMatchingBackupAttributeValue(query, attributeValue);
                if (backupAttributeValue == null) {
                    backupAttributeValue = getObjectBuilder(attributeValue, query.getSession()).buildNewInstance();
                }
                clonedQuery.setBackupClone(backupAttributeValue);
            }
            return getObjectBuilder(attributeValue, query.getSession()).buildRowForUpdate(clonedQuery);
        }
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        Object attributeValue = getAttributeValueFromObject(original);
        Object aggregateClone = buildClonePart(original, attributeValue, unitOfWork);

        if (aggregateClone != null) {
            ClassDescriptor descriptor = getReferenceDescriptor(aggregateClone, unitOfWork);
            descriptor.getObjectChangePolicy().setAggregateChangeListener(clone, aggregateClone, unitOfWork, descriptor, getAttributeName());
        }

        setAttributeValueInObject(clone, aggregateClone);
    }

    /**
     * INTERNAL:
     * A combination of readFromRowIntoObject and buildClone.
     * <p>
     * buildClone assumes the attribute value exists on the original and can
     * simply be copied.
     * <p>
     * readFromRowIntoObject assumes that one is building an original.
     * <p>
     * Both of the above assumptions are false in this method, and actually
     * attempts to do both at the same time.
     * <p>
     * Extract value from the row and set the attribute to this value in the
     * working copy clone.
     * In order to bypass the shared cache when in transaction a UnitOfWork must
     * be able to populate working copies directly from the row.
     */
    public void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        // This method is a combination of buildggregateFromRow and
        // buildClonePart on the super class.
        // none of buildClonePart used, as not an orignal new object, nor
        // do we worry about creating heavy clones for aggregate objects.
        Object clonedAttributeValue = buildAggregateFromRow(databaseRow, clone, joinManager, false, executionSession);
        ClassDescriptor descriptor = getReferenceDescriptor(clonedAttributeValue, unitOfWork);
        descriptor.getObjectChangePolicy().setAggregateChangeListener(clone, clonedAttributeValue, unitOfWork, descriptor, getAttributeName());
        setAttributeValueInObject(clone, clonedAttributeValue);
        return;
    }

    /**
     * INTERNAL:
     * Builds a shallow original object.  Only direct attributes and primary
     * keys are populated.  In this way the minimum original required for
     * instantiating a working copy clone can be built without placing it in
     * the shared cache (no concern over cycles).
     */
    public void buildShallowOriginalFromRow(AbstractRecord databaseRow, Object original, JoinedAttributeManager joinManager, AbstractSession executionSession) {
        Object aggregate = buildAggregateFromRow(databaseRow, original, joinManager, true, executionSession);// shallow only.
        setAttributeValueInObject(original, aggregate);
    }

    /**
     * INTERNAL:
     * Build and return a "template" database row with all the fields
     * set to null.
     */
    protected AbstractRecord buildTemplateInsertRow(AbstractSession session) {
        AbstractRecord result = getReferenceDescriptor().getObjectBuilder().buildTemplateInsertRow(session);
        List processedMappings = (List)getReferenceDescriptor().getMappings().clone();
        if (getReferenceDescriptor().hasInheritance()) {
            Enumeration children = getReferenceDescriptor().getInheritancePolicy().getChildDescriptors().elements();
            while (children.hasMoreElements()) {
                Enumeration mappings = ((ClassDescriptor)children.nextElement()).getMappings().elements();
                while (mappings.hasMoreElements()) {
                    DatabaseMapping mapping = (DatabaseMapping)mappings.nextElement();

                    // Only write mappings once.
                    if (!processedMappings.contains(mapping)) {
                        mapping.writeInsertFieldsIntoRow(result, session);
                        processedMappings.add(mapping);
                    }
                }
            }
        }
        return result;
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects){
        //objects referenced by this mapping are not registered as they have
        // no identity, however mappings from the referenced object may need cascading.
        Object objectReferenced = getRealAttributeValueFromObject(object, uow);
        if ((objectReferenced == null)){
            return ;
        }
        if ( ! visitedObjects.contains(objectReferenced)){
            visitedObjects.put(objectReferenced, objectReferenced);
            ObjectBuilder builder = getReferenceDescriptor(objectReferenced.getClass(), uow).getObjectBuilder();
            builder.cascadePerformRemove(objectReferenced, uow, visitedObjects);
        }
        
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects){
        //aggregate objects are not registered but their mappings should be.
        Object objectReferenced = getRealAttributeValueFromObject(object, uow);
        if ( (objectReferenced == null) ){
            return ;
        }
        if ( ! visitedObjects.contains(objectReferenced)){
            visitedObjects.put(objectReferenced, objectReferenced);
            ObjectBuilder builder = getReferenceDescriptor(objectReferenced.getClass(), uow).getObjectBuilder();
            builder.cascadeRegisterNewForCreate(objectReferenced, uow, visitedObjects);
        }
    }

    /**
     * INTERNAL:
     * Return the fields handled by the mapping.
     */
    protected Vector<DatabaseField> collectFields() {
        return getReferenceFields();
    }

    /**
     * PUBLIC:
     * If <em>all</em> the fields in the database row for the aggregate object are NULL,
     * then, by default, TopLink will place a null in the appropriate source object
     * (as opposed to an aggregate object filled with nulls). This behavior can be
     * explicitly set by calling #allowNull().
     * To change this behavior, call #dontAllowNull(). Then TopLink
     * will build a new instance of the aggregate object that is filled with nulls
     * and place it in the source object.
     * In either situation, when writing, TopLink will place a NULL in all the
     * fields in the database row for the aggregate object.
     */
    public void dontAllowNull() {
        setIsNullAllowed(false);
    }

    /**
     * INTERNAL:
     * Return a collection of the aggregate to source field name associations.
     */
    public Vector<Association> getAggregateToSourceFieldNameAssociations() {
        Vector<Association> associations = new Vector(getAggregateToSourceFieldNames().size());
        Iterator aggregateEnum = getAggregateToSourceFieldNames().keySet().iterator();
        Iterator sourceEnum = getAggregateToSourceFieldNames().values().iterator();
        while (aggregateEnum.hasNext()) {
            associations.addElement(new Association(aggregateEnum.next(), sourceEnum.next()));
        }

        return associations;
    }

    /**
     * INTERNAL:
     * Return the hashtable that stores aggregate field name to source field name.
     */
    public Map<String, String> getAggregateToSourceFieldNames() {
        return aggregateToSourceFieldNames;
    }

    /**
     * INTERNAL:
     * Return the classification for the field contained in the mapping.
     * This is used to convert the row value to a consistent Java value.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) {
        DatabaseMapping mapping = getReferenceDescriptor().getObjectBuilder().getMappingForField(fieldToClassify);
        if (mapping == null) {
            return null;// Means that the mapping is read-only
        }
        return mapping.getFieldClassification(fieldToClassify);
    }

    /**
     * INTERNAL:
     * This is used to preserve object identity during a refreshObject()
     * query. Return the object corresponding to the specified database row.
     * The default is to simply return the attribute value.
     */
    protected Object getMatchingAttributeValueFromObject(AbstractRecord row, Object targetObject, AbstractSession session, ClassDescriptor descriptor) {
        return getAttributeValueFromObject(targetObject);
    }

    /**
     * INTERNAL:
     * This is used to match up objects during an update in a UOW.
     * Return the object corresponding to the specified attribute value.
     * The default is to simply return the backup attribute value.
     */
    protected Object getMatchingBackupAttributeValue(WriteObjectQuery query, Object attributeValue) {
        return getAttributeValueFromObject(query.getBackupClone());
    }

    /**
     * INTERNAL:
     * Since aggregate object mappings clone their descriptors, for inheritance the correct child clone must be found.
     */
    protected ClassDescriptor getReferenceDescriptor(Class theClass, AbstractSession session) {
        if (getReferenceDescriptor().getJavaClass().equals(theClass)) {
            return getReferenceDescriptor();
        }

        ClassDescriptor subclassDescriptor = getReferenceDescriptor().getInheritancePolicy().getSubclassDescriptor(theClass);
        if (subclassDescriptor == null) {
            throw DescriptorException.noSubClassMatch(theClass, this);
        } else {
            return subclassDescriptor;
        }
    }

    /**
     * INTERNAL:
     * Return the fields used to build the aggregate object.
     */
    protected Vector<DatabaseField> getReferenceFields() {
        return getReferenceDescriptor().getAllFields();
    }

    /**
     * INTERNAL:
     * Return if the mapping has any ownership or other dependency over its target object(s).
     */
    public boolean hasDependency() {
        return getReferenceDescriptor().hasDependencyOnParts();
    }

    /**
     * INTERNAL:
     * For an aggregate mapping the reference descriptor is cloned. The cloned descriptor is then
     * assigned primary keys and table names before initialize. Once the cloned descriptor is initialized
     * it is assigned as reference descriptor in the aggregate mapping. This is a very specific
     * behaviour for aggregate mappings. The original descriptor is used only for creating clones and
     * after that the aggregate mapping never uses it.
     * Some initialization is done in postInitialize to ensure the target descriptor's references are initialized.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        ClassDescriptor clonedDescriptor = (ClassDescriptor)getReferenceDescriptor().clone();
        if (clonedDescriptor.isChildDescriptor()) {
            ClassDescriptor parentDescriptor = session.getDescriptor(clonedDescriptor.getInheritancePolicy().getParentClass());
            initializeParentInheritance(parentDescriptor, clonedDescriptor, session);
        }
        setReferenceDescriptor(clonedDescriptor);

        initializeReferenceDescriptor(clonedDescriptor);
        clonedDescriptor.preInitialize(session);
        clonedDescriptor.initialize(session);
        translateFields(clonedDescriptor, session);

        if (clonedDescriptor.hasInheritance() && clonedDescriptor.getInheritancePolicy().hasChildren()) {
            //clone child descriptors
            initializeChildInheritance(clonedDescriptor, session);
        }

        setFields(collectFields());
    }

    /**
     * INTERNAL:
     * For an aggregate mapping the reference descriptor is cloned.
     * If the reference descriptor is involved in an inheritance tree,
     * all the parent and child descriptors are cloned also.
     * The cloned descriptors are then assigned primary keys and
     * table names before initialize.
     * This is a very specific behaviour for aggregate mappings.
     */
    public void initializeChildInheritance(ClassDescriptor parentDescriptor, AbstractSession session) throws DescriptorException {
        //recursive call to the further childern descriptors
        if (parentDescriptor.getInheritancePolicy().hasChildren()) {
            //setFields(clonedChildDescriptor.getFields());		
            Vector childDescriptors = parentDescriptor.getInheritancePolicy().getChildDescriptors();
            Vector cloneChildDescriptors = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
            for (Enumeration enumtr = childDescriptors.elements(); enumtr.hasMoreElements();) {
                ClassDescriptor clonedChildDescriptor = (ClassDescriptor)((ClassDescriptor)enumtr.nextElement()).clone();
                clonedChildDescriptor.getInheritancePolicy().setParentDescriptor(parentDescriptor);
                initializeReferenceDescriptor(clonedChildDescriptor);
                clonedChildDescriptor.preInitialize(session);
                clonedChildDescriptor.initialize(session);
                translateFields(clonedChildDescriptor, session);
                cloneChildDescriptors.addElement(clonedChildDescriptor);
                initializeChildInheritance(clonedChildDescriptor, session);
            }
            parentDescriptor.getInheritancePolicy().setChildDescriptors(cloneChildDescriptors);
        }
    }

    /**
     * INTERNAL:
     * For an aggregate mapping the reference descriptor is cloned.
     * If the reference descriptor is involved in an inheritance tree,
     * all the parent and child descriptors are cloned also.
     * The cloned descriptors are then assigned primary keys and
     * table names before initialize.
     * This is a very specific behaviour for aggregate mappings.
     */
    public void initializeParentInheritance(ClassDescriptor parentDescriptor, ClassDescriptor childDescriptor, AbstractSession session) throws DescriptorException {
        ClassDescriptor clonedParentDescriptor = (ClassDescriptor)parentDescriptor.clone();

        //recursive call to the further parent descriptors
        if (clonedParentDescriptor.getInheritancePolicy().isChildDescriptor()) {
            ClassDescriptor parentToParentDescriptor = session.getDescriptor(clonedParentDescriptor.getJavaClass());
            initializeParentInheritance(parentToParentDescriptor, parentDescriptor, session);
        }

        initializeReferenceDescriptor(clonedParentDescriptor);
        Vector children = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        children.addElement(childDescriptor);
        clonedParentDescriptor.getInheritancePolicy().setChildDescriptors(children);
        clonedParentDescriptor.preInitialize(session);
        clonedParentDescriptor.initialize(session);
        translateFields(clonedParentDescriptor, session);
    }

    /**
     * INTERNAL:
     * Initialize the cloned reference descriptor with table names and primary keys
     */
    protected void initializeReferenceDescriptor(ClassDescriptor clonedDescriptor) {
        // Must ensure default tables remains the same.
        clonedDescriptor.setDefaultTable(getDescriptor().getDefaultTable());
        clonedDescriptor.setTables(getDescriptor().getTables());
        clonedDescriptor.setPrimaryKeyFields(getDescriptor().getPrimaryKeyFields());
    }

    /**
     * INTERNAL:
     * Related mapping should implement this method to return true.
     */
    public boolean isAggregateObjectMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Return if this mapping supports change tracking.
     */
    public boolean isChangeTrackingSupported() {
        return false;
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
     * Return setting.
     */
    public boolean isNullAllowed() {
        return isNullAllowed;
    }

    /**
     * INTERNAL:
     * For an aggregate mapping the reference descriptor is cloned. The cloned descriptor is then
     * assigned primary keys and table names before initialize. Once the cloned descriptor is initialized
     * it is assigned as reference descriptor in the aggregate mapping. This is a very specific
     * behaviour for aggregate mappings. The original descriptor is used only for creating clones and
     * after that the aggregate mapping never uses it.
     * Some initialization is done in postInitialize to ensure the target descriptor's references are initialized.
     */
    public void postInitialize(AbstractSession session) throws DescriptorException {
        super.postInitialize(session);

        if (getReferenceDescriptor() != null) {
            getReferenceDescriptor().postInitialize(session);
        }
    }

    /**
     * INTERNAL:
     * Build an aggregate object from the specified return row and put it
     * in the specified target object.
     * Return row is merged into object after execution of insert or update call
     * accordiing to ReturningPolicy.
     */
    public Object readFromReturnRowIntoObject(AbstractRecord row, Object targetObject, ReadObjectQuery query, Collection handledMappings) throws DatabaseException {
        Object aggregate = getAttributeValueFromObject(targetObject);
        if (aggregate == null) {
            aggregate = readFromRowIntoObject(row, null, targetObject, query);
            handledMappings.add(this);
            return aggregate;
        }

        for (int i = 0; i < getReferenceFields().size(); i++) {
            DatabaseField field = (DatabaseField)getReferenceFields().elementAt(i);
            if (row.containsKey(field)) {
                getObjectBuilder(aggregate, query.getSession()).assignReturnValueForField(aggregate, query, row, field, handledMappings);
            }
        }

        if (isNullAllowed()) {
            boolean allAttributesNull = true;
            for (int i = 0; (i < getReferenceFields().size()) && allAttributesNull; i++) {
                DatabaseField field = (DatabaseField)fields.elementAt(i);
                if (row.containsKey(field)) {
                    allAttributesNull = row.get(field) == null;
                } else {
                    Object fieldValue = valueFromObject(targetObject, field, query.getSession());
                    if (fieldValue == null) {
                        Object baseValue = getDescriptor().getObjectBuilder().getBaseValueForField(field, targetObject);
                        if (baseValue != null) {
                            DatabaseMapping baseMapping = getDescriptor().getObjectBuilder().getBaseMappingForField(field);
                            if (baseMapping.isForeignReferenceMapping()) {
                                ForeignReferenceMapping refMapping = (ForeignReferenceMapping)baseMapping;
                                if (refMapping.usesIndirection()) {
                                    allAttributesNull = refMapping.getIndirectionPolicy().objectIsInstantiated(baseValue);
                                }
                            }
                        }
                    } else {
                        allAttributesNull = false;
                    }
                }
            }
            if (allAttributesNull) {
                aggregate = null;
                setAttributeValueInObject(targetObject, aggregate);
            }
        }
        handledMappings.add(this);
        return aggregate;
    }

    /**
     * INTERNAL:
     * Build an aggregate object from the specified row and put it
     * in the specified target object.
     */
    public Object readFromRowIntoObject(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object targetObject, ObjectBuildingQuery sourceQuery, AbstractSession executionSession) throws DatabaseException {
        Object aggregate = buildAggregateFromRow(databaseRow, targetObject, joinManager, false, executionSession);// don't just build a shallow original
        setAttributeValueInObject(targetObject, aggregate);
        return aggregate;
    }

    /**
     * INTERNAL:
     * Rehash any hashtables based on fields.
     * This is used to clone descriptors for aggregates, which hammer field names.
     */
    public void rehashFieldDependancies(AbstractSession session) {
        getReferenceDescriptor().rehashFieldDependancies(session);
    }

    /**
     * INTERNAL:
     * Set a collection of the aggregate to source field name associations.
     */
    public void setAggregateToSourceFieldNameAssociations(Vector<Association> fieldAssociations) {
        Hashtable fieldNames = new Hashtable(fieldAssociations.size() + 1);
        for (Enumeration associationsEnum = fieldAssociations.elements();
                 associationsEnum.hasMoreElements();) {
            Association association = (Association)associationsEnum.nextElement();
            fieldNames.put(association.getKey(), association.getValue());
        }

        setAggregateToSourceFieldNames(fieldNames);
    }

    /**
     * INTERNAL:
     * Set the hashtable that stores target field name to the source field name.
     */
    protected void setAggregateToSourceFieldNames(Map<String, String> aggregateToSource) {
        aggregateToSourceFieldNames = aggregateToSource;
    }

    /**
     * INTERNAL:
     * Will be used by Gromit only.
     */
    public void setIsNullAllowed(boolean aBoolean) {
        isNullAllowed = aBoolean;
    }

    /**
     * INTERNAL:
     * If field names are different in the source and aggregate objects then the translation
     * is done here. The aggregate field name is converted to source field name from the
     * field name mappings stored.
     */
    protected void translateFields(ClassDescriptor clonedDescriptor, AbstractSession session) {
        for (Enumeration entry = clonedDescriptor.getFields().elements(); entry.hasMoreElements();) {
            DatabaseField field = (DatabaseField)entry.nextElement();
            String nameInAggregate = field.getName();
            String nameInSource = (String)getAggregateToSourceFieldNames().get(nameInAggregate);

            // Do not modify non-translated fields.
            if (nameInSource != null) {
                DatabaseField fieldInSource = new DatabaseField(nameInSource);

                // Check if the translated field specified a table qualifier.
                if (fieldInSource.getName().equals(nameInSource)) {
                    // No table so just set the field name.
                    field.setName(nameInSource);
                } else {
                    // There is a table, so set the name and table.
                    field.setName(fieldInSource.getName());
                    field.setTable(clonedDescriptor.getTable(fieldInSource.getTable().getName()));
                }
            }
        }

        clonedDescriptor.rehashFieldDependancies(session);
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Write the foreign key values from the attribute to the row.
     */
    
    public void writeFromAttributeIntoRow(Object attribute, AbstractRecord row, AbstractSession session)
    {
          AbstractRecord targetRow = buildRowFromAggregate(null, attribute, session);
          for (Enumeration stream = targetRow.keys(); stream.hasMoreElements(); ) {
                  DatabaseField field = (DatabaseField) stream.nextElement();
                  Object value = targetRow.get(field);
                  row.put(field, value);
          }
    }
    
    /**
     * INTERNAL:
     * Extract value of the field from the object
     */
    public Object valueFromObject(Object object, DatabaseField field, AbstractSession session) throws DescriptorException {
        Object attributeValue = getAttributeValueFromObject(object);
        if (attributeValue == null) {
            if (isNullAllowed()) {
                return null;
            } else {
                throw DescriptorException.nullForNonNullAggregate(object, this);
            }
        } else {
            return getObjectBuilder(attributeValue, session).extractValueFromObjectForField(attributeValue, field, session);
        }
    }

    /**
     * INTERNAL:
     * Get the attribute value from the object and add the appropriate
     * values to the specified database row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord databaseRow, AbstractSession session) throws DescriptorException {
        if (isReadOnly()) {
            return;
        }
        AbstractRecord targetRow = buildRowFromAggregate(object, getAttributeValueFromObject(object), session);
        for (Enumeration stream = targetRow.keys(); stream.hasMoreElements();) {
            DatabaseField field = (DatabaseField)stream.nextElement();
            Object value = targetRow.get(field);
            databaseRow.add(field, value);
        }
    }

    /**
     * INTERNAL:
     * Get the attribute value from the object and add the appropriate
     * values to the specified database row.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord databaseRow, AbstractSession session) throws DescriptorException {
        if (isReadOnly()) {
            return;
        }
        AbstractRecord targetRow = buildRowFromAggregateWithChangeRecord(changeRecord, (ObjectChangeSet)((AggregateChangeRecord)changeRecord).getChangedObject(), session);
        for (Enumeration stream = targetRow.keys(); stream.hasMoreElements();) {
            DatabaseField field = (DatabaseField)stream.nextElement();
            Object value = targetRow.get(field);
            databaseRow.add(field, value);
        }
    }

    /**
     * INTERNAL:
     * Get the attribute value from the object and add the changed
     * values to the specified database row.
     */
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery query, AbstractRecord databaseRow) throws DescriptorException {
        if (isReadOnly()) {
            return;
        }
        AbstractRecord targetRow = buildRowFromAggregateForUpdate(query, getAttributeValueFromObject(query.getObject()));
        for (Enumeration stream = targetRow.keys(); stream.hasMoreElements();) {
            DatabaseField field = (DatabaseField)stream.nextElement();
            Object value = targetRow.get(field);
            databaseRow.add(field, value);
        }
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        AbstractRecord targetRow = buildTemplateInsertRow(session);
        for (Enumeration keyEnum = targetRow.keys(); keyEnum.hasMoreElements();) {
            DatabaseField field = (DatabaseField)keyEnum.nextElement();
            Object value = targetRow.get(field);

            //CR-3286097 - Should use add not put, to avoid linear search.
            databaseRow.add(field, value);
        }
    }

    /**
     * INTERNAL:
     * Add a primary key join column (secondary field).
     * If this contain primary keys and the descriptor(or its subclass) has multiple tables
     * (secondary tables or joined inheritance strategy), this should also know the primary key 
     * join columns to handle some cases properly.
     */
    public void addPrimaryKeyJoinField(DatabaseField primaryKeyField, DatabaseField secondaryField) {
        // now it doesn't need to manage this as a separate table here,
        // it's enough just to add the mapping to ObjectBuilder.mappingsByField 
        ObjectBuilder builder = getReferenceDescriptor().getObjectBuilder();
        DatabaseMapping mapping = builder.getMappingForField(primaryKeyField);
        if (mapping != null) {
            builder.getMappingsByField().put(secondaryField, mapping); 
        }
    }
}
