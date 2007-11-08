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
package oracle.toplink.essentials.mappings.foundation;

import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.descriptors.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.converters.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.ObjectCopyingPolicy;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;

/**
 * <b>Purpose</b>: Maps an attribute to the corresponding database field type.
 * The list of field types that are supported by TopLink's direct to field mapping
 * is dependent on the relational database being used.
 * A converter can be used to convert between the object and data type if they do not match.
 *
 * @see Converter
 * @see ObjectTypeConverter
 * @see TypeConversionConverter
 * @see SerializedObjectConverter
 *
 * @author Sati
 * @since TopLink/Java 1.0
 */
public abstract class AbstractDirectMapping extends DatabaseMapping {

    /** DatabaseField which this mapping represents. */
    protected DatabaseField field;

    /** To specify the conversion type */
    protected transient Class attributeClassification;
    protected transient String attributeClassificationName;
    
    /** PERF: Also store object class of attribute in case of primitive. */
    protected transient Class attributeObjectClassification;

    /** Allows user defined conversion between the object attribute value and the database value. */
    protected Converter converter;

    /** Support specification of the value to use for null. */
    protected transient Object nullValue;

    /**
     * PERF: Indicates if this mapping's attribute is a simple atomic value and cannot be modified, only replaced.
     * This is a tri-state to allow user to set to true or false, as default is false but
     * some data-types such as Calendar or byte[] or converter types may be desired to be used as mutable.
     */
    protected Boolean isMutable;

    /**
     * Default constructor.
     */
    public AbstractDirectMapping() {
        super();
        this.setWeight(WEIGHT_1);
    }

    /**
     * PUBLIC:
     * Return the converter on the mapping.
     * A converter can be used to convert between the object's value and database value of the attribute.
     */
    public Converter getConverter() {
        return converter;
    }

    /**
     * PUBLIC:
     * Set the converter on the mapping.
     * A converter can be used to convert between the object's value and database value of the attribute.
     */
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    /**
     * PUBLIC:
     * Return true if the attribute for this mapping is a simple atomic value that cannot be modified,
     * only replaced.
     * This is false by default unless a mutable converter is used such as the SerializedObjectConverter.
     * This can be set to false in this case, or if a Calendar or byte[] is desired to be used as a mutable value it can be set to true.
     */
    public boolean isMutable() {
        if (isMutable == null) {
            return false;
        }
        return isMutable.booleanValue();
    }

    /**
     * PUBLIC:
     * Return true if the attribute for this mapping is a simple atomic value that cannot be modified,
     * only replaced.
     * This is false by default unless a mutable converter is used such as the SerializedObjectConverter.
     * This can be set to false in this case, or if a Calendar or byte[] is desired to be used as a mutable value it can be set to true.
     */
    public void setIsMutable(boolean isMutable) {
        if (isMutable == true) {
            this.isMutable = Boolean.TRUE;
        } else {
            this.isMutable = Boolean.FALSE;
        }
    }

    /**
     * INTERNAL:
     * Clone the attribute from the clone and assign it to the backup.
     */
    public void buildBackupClone(Object clone, Object backup, UnitOfWorkImpl unitOfWork) {
        buildClone(clone, backup, unitOfWork);
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildClone(Object original, Object clone, UnitOfWorkImpl unitOfWork) {
        buildCloneValue(original, clone, unitOfWork);
    }

    /**
     * INTERNAL:
     * Clone the attribute from the original and assign it to the clone.
     */
    public void buildCloneValue(Object original, Object clone, AbstractSession session) {
        // Optimized for clone copy policy, setting of the value is not required.
        if (isCloningRequired()) {
            Object attributeValue = getAttributeValueFromObject(original);
            if (isMutable()) {
                attributeValue = getAttributeValue(getFieldValue(attributeValue, session), session);
            }
            setAttributeValueInObject(clone, attributeValue);
        }
    }

    /**
     * INTERNAL:
     * Copy of the attribute of the object.
     * This is NOT used for unit of work but for templatizing an object.
     */
    public void buildCopy(Object copy, Object original, ObjectCopyingPolicy policy) {
        buildCloneValue(original, copy, policy.getSession());
    }

    /**
     * INTERNAL:
     * Cascade perform delete through mappings that require the cascade
     */
    public void cascadePerformRemoveIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }

    /**
     * INTERNAL:
     * Cascade registerNew for Create through mappings that require the cascade
     */
    public void cascadeRegisterNewIfRequired(Object object, UnitOfWorkImpl uow, IdentityHashtable visitedObjects) {
        //objects referenced by this mapping are not registered as they have
        // no identity, this is a no-op.
    }

    /**
     * INTERNAL:
     * The mapping clones itself to create deep copy.
     */
    public Object clone() {
        AbstractDirectMapping clone = (AbstractDirectMapping)super.clone();

        // Field must be cloned so aggregates do not share fields.
        clone.setField((DatabaseField)getField().clone());

        return clone;
    }

    /**
     * Returns the field this mapping represents.
     */
    protected Vector<DatabaseField> collectFields() {
        Vector databaseField = new Vector(1);

        databaseField.addElement(getField());
        return databaseField;
    }

    /**
     * INTERNAL:
     * Compare the clone and backup clone values and return a change record if the value changed.
     */
    public ChangeRecord compareForChange(Object clone, Object backUp, ObjectChangeSet owner, AbstractSession session) {
        // same code as write from object into row for update
        if ((owner.isNew()) || (!compareObjects(backUp, clone, session))) {
            return buildChangeRecord(clone, owner, session);
        }
        return null;
    }

    /**
     * INTERNAL:
     * Directly build a change record without comparison
     */
    public ChangeRecord buildChangeRecord(Object clone, ObjectChangeSet owner, AbstractSession session) {
        return internalBuildChangeRecord(getAttributeValueFromObject(clone), owner);
    }

    /**
     * INTERNAL:
     * Build a change record
     */
    public ChangeRecord internalBuildChangeRecord(Object newValue, ObjectChangeSet owner) {
        DirectToFieldChangeRecord changeRecord = new DirectToFieldChangeRecord(owner);
        changeRecord.setAttribute(getAttributeName());
        changeRecord.setMapping(this);
        changeRecord.setNewValue(newValue);
        return changeRecord;
    }

    /**
     * INTERNAL:
     * Compare the attributes belonging to this mapping for the objects.
     */
    public boolean compareObjects(Object firstObject, Object secondObject, AbstractSession session) {
        Object one = getAttributeValueFromObject(firstObject);
        Object two = getAttributeValueFromObject(secondObject);

        // PERF: Check identity before conversion.
        if (one == two) {
            return true;
        }

        // CR2114 - following two lines modified; getFieldValue() needs class as an argument
        one = getFieldValue(one, session);
        two = getFieldValue(two, session);
        // PERF:  Check identity/nulls before special type comparison.
        if (one == two) {
            return true;
        }

        if ((one == null) || (two == null)) {
            return false;
        }

        // Arrays must be checked for equality because default does identity
        if ((one.getClass() == ClassConstants.APBYTE) && (two.getClass() == ClassConstants.APBYTE)) {
            return Helper.compareByteArrays((byte[])one, (byte[])two);
        }
        if ((one.getClass() == ClassConstants.APCHAR) && (two.getClass() == ClassConstants.APCHAR)) {
            return Helper.compareCharArrays((char[])one, (char[])two);
        }
        if ((one.getClass().isArray()) && (two.getClass().isArray())) {
            return Helper.compareArrays((Object[])one, (Object[])two);
        }

        // BigDecimals equals does not consider the precision correctly
        if (one instanceof java.math.BigDecimal && two instanceof java.math.BigDecimal) {
            return Helper.compareBigDecimals((java.math.BigDecimal)one, (java.math.BigDecimal)two);
        }

        return one.equals(two);
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
        
        if (converter != null) {
            if (converter instanceof TypeConversionConverter) {
                ((TypeConversionConverter)converter).convertClassNamesToClasses(classLoader);
            } else if (converter instanceof ObjectTypeConverter) {
                // To avoid 1.5 dependencies with the EnumTypeConverter check
                // against ObjectTypeConverter.
                ((ObjectTypeConverter) converter).convertClassNamesToClasses(classLoader);
            }
        } 
    };

    /**
     * PUBLIC:
     * Some databases do not properly support all of the base data types. For these databases,
     * the base data type must be explicitly specified in the mapping to tell TopLink to force
     * the instance variable value to that data type
     */
    public Class getAttributeClassification() {
        return attributeClassification;
    }

    public String getAttributeClassificationName() {
        return attributeClassificationName;
    }

    /**
     * INTERNAL:
     * Allows for subclasses to convert the attribute value.
     */
    public Object getAttributeValue(Object fieldValue, AbstractSession session) {
        // PERF: Direct variable access.
        Object attributeValue = fieldValue;
        if ((fieldValue == null) && (getNullValue() != null)) {// Translate default null value
            return this.nullValue;
        }

        // Allow for user defined conversion to the object value.
        if (this.converter != null) {
            attributeValue = this.converter.convertDataValueToObjectValue(attributeValue, session);
        } else {
            // PERF: Avoid conversion check when not required.
            if ((attributeValue == null) || (attributeValue.getClass() != this.attributeObjectClassification)) {
                try {
                    attributeValue = session.getDatasourcePlatform().convertObject(attributeValue, this.attributeClassification);
                } catch (ConversionException e) {
                    throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
                }
            }
        }
        if (attributeValue == null) {// Translate default null value, conversion may have produced null.
            attributeValue = this.nullValue;
        }

        return attributeValue;
    }

    /**
     * INTERNAL:
     * Returns the field which this mapping represents.
     */
    public DatabaseField getField() {
        return field;
    }

    /**
     * INTERNAL:
     */
    public boolean isAbstractDirectMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Return the classifiction for the field contained in the mapping.
     * This is used to convert the row value to a consistent Java value.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) {
        // PERF: This method is a major performance code point,
        // so has been micro optimized and uses direct variable access.
        if (fieldToClassify.type != null) {
            return fieldToClassify.type;
        } else {
            if (this.converter != null) {
                return null;
            } else {
                return this.attributeClassification;
            }
        }
    }

    /**
     * ADVANCED:
     * Return the class type of the field value.
     * This can be used if field value differs from the object value,
     * has specific typing requirements such as usage of java.sql.Blob or NChar.
     */
    public Class getFieldClassification() {
        if (getField() == null) {
            return null;
        }
        return getField().getType();
    }

    /**
     * ADVANCED:
     * Set the class type of the field value.
     * This can be used if field value differs from the object value,
     * has specific typing requirements such as usage of java.sql.Blob or NChar.
     * This must be called after the field name has been set.
     */
    public void setFieldClassification(Class fieldType) {
        getField().setType(fieldType);
    }

    /**
     * ADVANCED:
     * Set the JDBC type of the field value.
     * This can be used if field type does not corespond directly to a Java class type,
     * such as MONEY.
     * This is used for binding.
     */
    public void setFieldType(int jdbcType) {
        getField().setSqlType(jdbcType);
    }

    /**
     * PUBLIC:
     * Name of the field this mapping represents.
     */
    public String getFieldName() {
        return getField().getQualifiedName();
    }

    /**
     * INTERNAL:
     * Convert the attribute value to a field value.
     * Process any converter if defined, and check for null values.
     */
    public Object getFieldValue(Object attributeValue, AbstractSession session) {
        // PERF: This method is a major performance code point,
        // so has been micro optimized and uses direct variable access.
        Object fieldValue = attributeValue;
        if ((this.nullValue != null) && (this.nullValue.equals(fieldValue))) {
            return null;
        }

        // Allow for user defined conversion to the object value.		
        if (this.converter != null) {
            fieldValue = this.converter.convertObjectValueToDataValue(fieldValue, session);
        }
        Class fieldClassification = getFieldClassification(getField());
        // PERF: Avoid conversion if not required.
        if ((fieldValue == null) || (fieldClassification != fieldValue.getClass())) {
            try {
                fieldValue = session.getPlatform(getDescriptor().getJavaClass()).convertObject(fieldValue, fieldClassification);
            } catch (ConversionException exception) {
                throw ConversionException.couldNotBeConverted(this, getDescriptor(), exception);
            }
        }
        return fieldValue;
    }

    /**
     * PUBLIC:
     * Allow for the value used for null to be specified.
     * This can be used to convert database null values to application specific values, when null values
     * are not allowed by the application (such as in primitives).
     * Note: the default value for NULL is used on reads, writes, and query SQL generation
     */
    public Object getNullValue() {
        return nullValue;
    }

    /**
     * INTERNAL:
     * Return the weight of the mapping, used to sort mappings to ensure that
     * DirectToField Mappings get merged first
     */
    public Integer getWeight() {
        return this.weight;
    }
    
    /**
     * INTERNAL:
     * Initialize the attribute classification.
     */
    public void preInitialize(AbstractSession session) throws DescriptorException {
        super.preInitialize(session);
        this.attributeClassification = getAttributeAccessor().getAttributeClass();
        this.attributeObjectClassification = Helper.getObjectClass(this.attributeClassification);
    }
    
    /**
     * INTERNAL:
     * The mapping is initialized with the given session.
     * This mapping is fully initialized after this.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);

        // Initialize isMutable if not specified, default is false (assumes atomic).
        if (this.isMutable == null) {
            if (getConverter() != null) {
                setIsMutable(getConverter().isMutable());
            } else {
                setIsMutable(false);
            }
        }

        if (getField() == null) {
            session.getIntegrityChecker().handleError(DescriptorException.fieldNameNotSetInMapping(this));
        }

        getDescriptor().buildField(getField());
        setFields(collectFields());

        if (getConverter() != null) {
            getConverter().initialize(this, session);
        }
    }

    /**
     * INTERNAL:
     */
    public boolean isDirectToFieldMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Iterate on the appropriate attribute.
     */
    public void iterate(DescriptorIterator iterator) {
        // PERF: Only iterate when required.
        if (iterator.shouldIterateOnPrimitives()) {
            iterator.iteratePrimitiveForMapping(getAttributeValueFromObject(iterator.getVisitedParent()), this);
        }
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object.
     */
    public void mergeChangesIntoObject(Object target, ChangeRecord changeRecord, Object source, MergeManager mergeManager) {
        setAttributeValueInObject(target, ((DirectToFieldChangeRecord)changeRecord).getNewValue());
    }

    /**
     * INTERNAL:
     * Merge changes from the source to the target object. This merge is only called when a changeSet for the target
     * does not exist or the target is uninitialized
     */
    public void mergeIntoObject(Object target, boolean isTargetUnInitialized, Object source, MergeManager mergeManager) {
        Object attributeValue = getAttributeValueFromObject(source);
        if ( (this.getDescriptor().getObjectChangePolicy().isObjectChangeTrackingPolicy()) && (!compareObjects(target, source, mergeManager.getSession())) ) {
            // if it didn't change then there will be no event
            Object targetAttribute = getAttributeValueFromObject(target);
            setAttributeValueInObject(target, attributeValue);
            //set the value first, if the owner is new ( or aggregate) the change set may be created directly
            //from the target.
            this.getDescriptor().getObjectChangePolicy().raiseInternalPropertyChangeEvent(target, getAttributeName(), targetAttribute, attributeValue);
        }else{
            //just set the value and continue
            setAttributeValueInObject(target, attributeValue);
        }
    }

    /**
     * PUBLIC:
     * Some databases do not properly support all of the base data types. For these databases,
     * the base data type must be explicitly specified in the mapping to tell TopLink to force
     * the instance variable value to that data type
     */
    public void setAttributeClassification(Class attributeClassification) {
        this.attributeClassification = attributeClassification;
    }

    /**
     * INTERNAL:
     * Set the name of the class for MW usage.
     */
    public void setAttributeClassificationName(String attributeClassificationName) {
        this.attributeClassificationName = attributeClassificationName;
    }

    /**
     * ADVANCED:
     * Set the field in the mapping.
     * This can be used for advanced field types, such as XML nodes, or to set the field type.
     */
    public void setField(DatabaseField theField) {
        field = theField;
    }

    /**
     * PUBLIC:
     * Allow for the value used for null to be specified.
     * This can be used to convert database null values to application specific values, when null values
     * are not allowed by the application (such as in primitives).
     * Note: the default value for NULL is used on reads, writes, and query SQL generation
     */
    public void setNullValue(Object nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        return getClass().getName() + "[" + getAttributeName() + "-->" + getField() + "]";
    }

    /**
     * INTERNAL:
     * Either create a new change record or update with the new value.  This is used
     * by attribute change tracking.
     */
    public void updateChangeRecord(Object clone, Object newValue, Object oldValue, ObjectChangeSet objectChangeSet, UnitOfWorkImpl uow) {
        DirectToFieldChangeRecord changeRecord = (DirectToFieldChangeRecord)objectChangeSet.getChangesForAttributeNamed(this.getAttributeName());
        if (changeRecord == null) {
            objectChangeSet.addChange(internalBuildChangeRecord(newValue, objectChangeSet));
        } else {
            changeRecord.setNewValue(newValue);
        }
    }

    /**
     * INTERNAL:
     * Return if this mapping supports change tracking.
     */
    public boolean isChangeTrackingSupported() {
        return !isMutable();
    }
    
    /**
     * INTERNAL:
     * Return if this mapping requires its attribute value to be cloned.
     */
    public boolean isCloningRequired() {
        return isMutable() || getDescriptor().getCopyPolicy().buildsNewInstance();
    }

    /**
     * INTERNAL:
     * Allow for subclasses to perform validation.
     */
    public void validateBeforeInitialization(AbstractSession session) throws DescriptorException {
        if ((getFieldName() == null) || (getFieldName().length() == 0)) {
            session.getIntegrityChecker().handleError(DescriptorException.noFieldNameForMapping(this));
        }
    }

    /**
     * INTERNAL:
     * Get the value from the object for this mapping.
     */
    public Object valueFromObject(Object object, DatabaseField field, AbstractSession session) throws DescriptorException {
        return getFieldValue(getAttributeValueFromObject(object), session);
    }

    /**
     * INTERNAL:
     * Extract value from the row and set the attribute to this value in the
     * working copy clone.
     * In order to bypass the shared cache when in transaction a UnitOfWork must
     * be able to populate working copies directly from the row.
     */
    public void buildCloneFromRow(AbstractRecord databaseRow, JoinedAttributeManager joinManager, Object clone, ObjectBuildingQuery sourceQuery, UnitOfWorkImpl unitOfWork, AbstractSession executionSession) {
        // Even though the correct value may exist on the original, we can't
        // make that assumption.  It is easy to just build it again from the
        // row even if copy policy already copied it.
        // That optimization is lost.
        Object attributeValue = valueFromRow(databaseRow, joinManager, sourceQuery, executionSession);

        setAttributeValueInObject(clone, attributeValue);
    }

    /**
     * INTERNAL:
     * Builds a shallow original object.  Only direct attributes and primary
     * keys are populated.  In this way the minimum original required for
     * instantiating a working copy clone can be built without placing it in
     * the shared cache (no concern over cycles).
     * @parameter original later the input to buildCloneFromRow
     */
    public void buildShallowOriginalFromRow(AbstractRecord databaseRow, Object original, ObjectBuildingQuery query, AbstractSession executionSession) {
        readFromRowIntoObject(databaseRow, null, original, query, executionSession);
    }

    /**
     * INTERNAL:
     * In the case of building a UnitOfWork clone directly from a row, the
     * session set in the query will not know which database platform to use
     * for converting the value.  Allows the correct session to be passed in.
     * @param row
     * @param query
     * @param executionSession
     * @return
     */
    public Object valueFromRow(AbstractRecord row, JoinedAttributeManager joinManager, ObjectBuildingQuery query, AbstractSession executionSession) {
        // PERF: Direct variable access.
        Object fieldValue = row.get(this.field);
        Object attributeValue = getAttributeValue(fieldValue, executionSession);

        return attributeValue;
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord row, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        Object attributeValue = getAttributeValueFromObject(object);
        Object fieldValue = getFieldValue(attributeValue, session);

        writeValueIntoRow(row, getField(), fieldValue);

    }

    protected abstract void writeValueIntoRow(AbstractRecord row, DatabaseField field, Object value);

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     * Validation preventing primary key updates is implemented here.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord row, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        if (isPrimaryKeyMapping() && !changeRecord.getOwner().isNew()) {
           throw ValidationException.primaryKeyUpdateDisallowed(changeRecord.getOwner().getClassName(), changeRecord.getAttribute());
        }
        
        Object attributeValue = ((DirectToFieldChangeRecord)changeRecord).getNewValue();
        Object fieldValue = getFieldValue(attributeValue, session);

        row.add(getField(), fieldValue);
    }

    /**
     * INTERNAL:
     * Write the attribute value from the object to the row for update.
     */
    public void writeFromObjectIntoRowForUpdate(WriteObjectQuery query, AbstractRecord aDatabaseRow) {
        if (query.getSession().isUnitOfWork()) {
            if (compareObjects(query.getBackupClone(), query.getObject(), query.getSession())) {
                return;
            }
        }

        super.writeFromObjectIntoRowForUpdate(query, aDatabaseRow);
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly()) {
            return;
        }

        databaseRow.add(getField(), null);
    }
}
