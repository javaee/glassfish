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
import oracle.toplink.essentials.internal.expressions.ObjectExpression;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.sessions.DatabaseRecord;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.internal.queryframework.JoinedAttributeManager;

/**
 * <p><b>Purpose</b>: One to one mappings are used to represent a pointer references
 * between two java objects. This mappings is usually represented by a single pointer
 * (stored in an instance variable) between the source and target objects. In the relational
 * database tables, these mappings are normally implemented using foreign keys.
 *
 * @author Sati
 * @since TOPLink/Java 1.0
 */
public class OneToOneMapping extends ObjectReferenceMapping implements RelationalMapping {

    /** Maps the source foreign/primary key fields to the target primary/foreign key fields. */
    protected Map<DatabaseField, DatabaseField> sourceToTargetKeyFields;

    /** Maps the target primary/foreign key fields to the source foreign/primary key fields. */
    protected Map<DatabaseField, DatabaseField> targetToSourceKeyFields;

    /** Keeps track of which fields are foreign keys on a per field basis (can have mixed foreign key relationships). */
    /** These are used for non-unit of work modification to check if the value of the 1-1 was changed and a deletion is required. */
    protected boolean shouldVerifyDelete;
    protected transient Expression privateOwnedCriteria;

    /** Indicates whether the referenced object should always be joined on read queries. */
    protected boolean usesJoining;
    
    /**
     * PUBLIC:
     * Default constructor.
     */
    public OneToOneMapping() {
        this.selectionQuery = new ReadObjectQuery();
        this.sourceToTargetKeyFields = new HashMap(2);
        this.targetToSourceKeyFields = new HashMap(2);
        this.foreignKeyFields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(1);
        this.isForeignKeyRelationship = false;
        this.shouldVerifyDelete = true;
        this.usesJoining = false;
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationalMapping() {
        return true;
    }

    /**
     * PUBLIC:
     * Define the foreign key relationship in the 1-1 mapping.
     * This method is used for composite foreign key relationships,
     * that is the source object's table has multiple foreign key fields to
     * the target object's primary key fields.
     * Both the source foreign key field and the target foreign key field must 
     * be specified.
     * When a foreign key is specified TopLink will automatically populate the 
     * value for that field from the target object when the object is written to 
     * the database. If the foreign key is also mapped through a direct-to-field 
     * then the direct-to-field must be set read-only.
     */
    public void addForeignKeyField(DatabaseField sourceForeignKeyField, DatabaseField targetPrimaryKeyField) {
        setIsForeignKeyRelationship(true);
        getForeignKeyFields().addElement(sourceForeignKeyField);

        getSourceToTargetKeyFields().put(sourceForeignKeyField, targetPrimaryKeyField);
        getTargetToSourceKeyFields().put(targetPrimaryKeyField, sourceForeignKeyField);
    }
    
    /**
     * PUBLIC:
     * Define the foreign key relationship in the 1-1 mapping.
     * This method is used for composite foreign key relationships,
     * that is the source object's table has multiple foreign key fields to
     * the target object's primary key fields.
     * Both the source foreign key field name and the target foreign key field 
     * name must be specified.
     * When a foreign key is specified TopLink will automatically populate the 
     * value for that field from the target object when the object is written to 
     * the database. If the foreign key is also mapped through a direct-to-field 
     * then the direct-to-field must be set read-only.
     */
    public void addForeignKeyFieldName(String sourceForeignKeyFieldName, String targetPrimaryKeyFieldName) {
        addForeignKeyField(new DatabaseField(sourceForeignKeyFieldName), new DatabaseField(targetPrimaryKeyFieldName));
    }

    /**
     * PUBLIC:
     * Define the target foreign key relationship in the 1-1 mapping.
     * This method is used for composite target foreign key relationships,
     * that is the target object's table has multiple foreign key fields to
     * the source object's primary key fields.
     * Both the target foreign key field and the source primary key field must 
     * be specified.
     * The distinction between a foreign key and target foreign key is that the 
     * 1-1 mapping will not populate the target foreign key value when written 
     * (because it is in the target table). Normally 1-1's are through foreign 
     * keys but in bi-directional 1-1's the back reference will be a target 
     * foreign key. In obscure composite legacy data models a 1-1 may consist of 
     * a foreign key part and a target foreign key part, in this case both 
     * method will be called with the correct parts.
     */
    public void addTargetForeignKeyField(DatabaseField targetForeignKeyField, DatabaseField sourcePrimaryKeyField) {
        getSourceToTargetKeyFields().put(sourcePrimaryKeyField, targetForeignKeyField);
        getTargetToSourceKeyFields().put(targetForeignKeyField, sourcePrimaryKeyField);
    }
    
    /**
     * PUBLIC:
     * Define the target foreign key relationship in the 1-1 mapping.
     * This method is used for composite target foreign key relationships,
     * that is the target object's table has multiple foreign key fields to
     * the source object's primary key fields.
     * Both the target foreign key field name and the source primary key field 
     * name must be specified.
     * The distinction between a foreign key and target foreign key is that the 
     * 1-1 mapping will not populate the target foreign key value when written 
     * (because it is in the target table). Normally 1-1's are through foreign 
     * keys but in bi-directional 1-1's the back reference will be a target 
     * foreign key. In obscure composite legacy data models a 1-1 may consist of 
     * a foreign key part and a target foreign key part, in this case both 
     * method will be called with the correct parts.
     */
    public void addTargetForeignKeyFieldName(String targetForeignKeyFieldName, String sourcePrimaryKeyFieldName) {
        addTargetForeignKeyField(new DatabaseField(targetForeignKeyFieldName), new DatabaseField(sourcePrimaryKeyFieldName));
    }

    /**
     * INTERNAL:
     * Used to allow object level comparisons.
     */
    public Expression buildObjectJoinExpression(Expression expression, Object value, AbstractSession session) {
        Expression base = ((oracle.toplink.essentials.internal.expressions.ObjectExpression)expression).getBaseExpression();
        Expression foreignKeyJoin = null;

        // Allow for equal null.
        if (value == null) {
            // Can only perform null comparison on foreign key relationships.
            // It does not really make sense for target any way as it is the source key.
            if (!isForeignKeyRelationship()) {
                throw QueryException.cannotCompareTargetForeignKeysToNull(base, value, this);
            }
            for (Iterator sourceFieldsEnum = getSourceToTargetKeyFields().keySet().iterator();
                     sourceFieldsEnum.hasNext();) {
                DatabaseField field = (DatabaseField)sourceFieldsEnum.next();
                Expression join = null;
                if (expression.isObjectExpression() && ((ObjectExpression)expression).shouldUseOuterJoin()){
                    join = base.getField(field).equalOuterJoin(null);
                } else {
                    join = base.getField(field).equal(null);
                }
                if (foreignKeyJoin == null) {
                    foreignKeyJoin = join;
                } else {
                    foreignKeyJoin = foreignKeyJoin.and(join);
                }
            }
        } else {
            if (!getReferenceDescriptor().getJavaClass().isInstance(value)) {
                throw QueryException.incorrectClassForObjectComparison(base, value, this);
            }

            Enumeration keyEnum = extractKeyFromReferenceObject(value, session).elements();
            for (Iterator sourceFieldsEnum = getSourceToTargetKeyFields().keySet().iterator();
                     sourceFieldsEnum.hasNext();) {
                DatabaseField field = (DatabaseField)sourceFieldsEnum.next();
                Expression join = null;
                if (expression.isObjectExpression() && ((ObjectExpression)expression).shouldUseOuterJoin()){
                    join = base.getField(field).equalOuterJoin(keyEnum.nextElement());
                } else {
                    join = base.getField(field).equal(keyEnum.nextElement());
                }
                if (foreignKeyJoin == null) {
                    foreignKeyJoin = join;
                } else {
                    foreignKeyJoin = foreignKeyJoin.and(join);
                }
            }
        }
        return foreignKeyJoin;
    }

    /**
     * INTERNAL:
     * Used to allow object level comparisons.
     */
    public Expression buildObjectJoinExpression(Expression expression, Expression argument, AbstractSession session) {
        Expression base = ((oracle.toplink.essentials.internal.expressions.ObjectExpression)expression).getBaseExpression();
        Expression foreignKeyJoin = null;
        if (expression==argument){
            for (Iterator sourceFieldsEnum = getSourceToTargetKeyFields().keySet().iterator();
                     sourceFieldsEnum.hasNext();) {
                DatabaseField field = (DatabaseField)sourceFieldsEnum.next();
                Expression join = base.getField(field);
                if (expression.isObjectExpression() && ((ObjectExpression)expression).shouldUseOuterJoin()){
                    join = join.equalOuterJoin(join);
                } else {
                    join = join.equal(join);
                }
                if (foreignKeyJoin == null) {
                    foreignKeyJoin = join;
                } else {
                    foreignKeyJoin = foreignKeyJoin.and(join);
                }
            }
        }else{
            Iterator targetFieldsEnum = getSourceToTargetKeyFields().values().iterator();
            for (Iterator sourceFieldsEnum = getSourceToTargetKeyFields().keySet().iterator();
                     sourceFieldsEnum.hasNext();) {
                DatabaseField sourceField = (DatabaseField)sourceFieldsEnum.next();
                DatabaseField targetField = (DatabaseField)targetFieldsEnum.next();
                Expression join = null;
                if (expression.isObjectExpression() && ((ObjectExpression)expression).shouldUseOuterJoin()){
                    join = base.getField(sourceField).equalOuterJoin(argument.getField(targetField));
                } else {
                    join = base.getField(sourceField).equal(argument.getField(targetField));
                }
                if (foreignKeyJoin == null) {
                    foreignKeyJoin = join;
                } else {
                    foreignKeyJoin = foreignKeyJoin.and(join);
                }
            }
        }
        return foreignKeyJoin;
    }

    /**
     * INTERNAL:
     * This methods clones all the fields and ensures that each collection refers to
     * the same clones.
     */
    public Object clone() {
        OneToOneMapping clone = (OneToOneMapping)super.clone();
        clone.setForeignKeyFields(oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(getForeignKeyFields().size()));
        clone.setSourceToTargetKeyFields(new HashMap(getSourceToTargetKeyFields().size()));
        clone.setTargetToSourceKeyFields(new HashMap(getTargetToSourceKeyFields().size()));
        Hashtable setOfFields = new Hashtable(getTargetToSourceKeyFields().size());

        //clone foreign keys and save the clones in a set
        for (Enumeration enumtr = getForeignKeyFields().elements(); enumtr.hasMoreElements();) {
            DatabaseField field = (DatabaseField)enumtr.nextElement();
            DatabaseField fieldClone = (DatabaseField)field.clone();
            setOfFields.put(field, fieldClone);
            clone.getForeignKeyFields().addElement(fieldClone);
        }

        //get clones from set for source hashtable.  If they do not exist, create a new one.
        for (Iterator sourceEnum = getSourceToTargetKeyFields().keySet().iterator();
                 sourceEnum.hasNext();) {
            DatabaseField sourceField = (DatabaseField)sourceEnum.next();
            DatabaseField targetField = (DatabaseField)getSourceToTargetKeyFields().get(sourceField);

            DatabaseField targetClone;
            DatabaseField sourceClone;

            targetClone = (DatabaseField)setOfFields.get(targetField);
            if (targetClone == null) {
                targetClone = (DatabaseField)targetField.clone();
                setOfFields.put(targetField, targetClone);
            }
            sourceClone = (DatabaseField)setOfFields.get(sourceField);
            if (sourceClone == null) {
                sourceClone = (DatabaseField)sourceField.clone();
                setOfFields.put(sourceField, sourceClone);
            }
            clone.getSourceToTargetKeyFields().put(sourceClone, targetClone);
        }

        //get clones from set for target hashtable.  If they do not exist, create a new one.
        for (Iterator targetEnum = getTargetToSourceKeyFields().keySet().iterator();
                 targetEnum.hasNext();) {
            DatabaseField targetField = (DatabaseField)targetEnum.next();
            DatabaseField sourceField = (DatabaseField)getTargetToSourceKeyFields().get(targetField);

            DatabaseField targetClone;
            DatabaseField sourceClone;

            targetClone = (DatabaseField)setOfFields.get(targetField);
            if (targetClone == null) {
                targetClone = (DatabaseField)targetField.clone();
                setOfFields.put(targetField, targetClone);
            }
            sourceClone = (DatabaseField)setOfFields.get(sourceField);
            if (sourceClone == null) {
                sourceClone = (DatabaseField)sourceField.clone();
                setOfFields.put(sourceField, sourceClone);
            }
            clone.getTargetToSourceKeyFields().put(targetClone, sourceClone);
        }
        return clone;
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be joined on read queries.
     * Joining will join the two classes tables to read all of the data in a single query.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public void dontUseJoining() {
        setUsesJoining(false);
    }

    /**
     * INTERNAL:
     * Extract the foreign key value from the source row.
     */
    protected Vector extractForeignKeyFromRow(AbstractRecord row, AbstractSession session) {
        Vector key = new Vector();

        for (Iterator fieldEnum = getSourceToTargetKeyFields().keySet().iterator();
                 fieldEnum.hasNext();) {
            DatabaseField field = (DatabaseField)fieldEnum.next();
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

    /**
     * INTERNAL:
     * Extract the key value from the reference object.
     */
    protected Vector extractKeyFromReferenceObject(Object object, AbstractSession session) {
        Vector key = new Vector();

        for (Iterator fieldEnum = getSourceToTargetKeyFields().values().iterator();
                 fieldEnum.hasNext();) {
            DatabaseField field = (DatabaseField)fieldEnum.next();

            if (object == null) {
                key.addElement(null);
            } else {
                key.addElement(getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(object, field, session));
            }
        }

        return key;
    }

    /**
     * INTERNAL:
     *    Return the primary key for the reference object (i.e. the object
     * object referenced by domainObject and specified by mapping).
     * This key will be used by a RemoteValueHolder.
     */
    public Vector extractPrimaryKeysForReferenceObjectFromRow(AbstractRecord row) {
        List primaryKeyFields = getReferenceDescriptor().getPrimaryKeyFields();
        Vector result = new Vector(primaryKeyFields.size());
        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField targetKeyField = (DatabaseField)primaryKeyFields.get(index);
            DatabaseField sourceKeyField = (DatabaseField)getTargetToSourceKeyFields().get(targetKeyField);
            if (sourceKeyField == null) {
                return new Vector(1);
            }
            result.addElement(row.get(sourceKeyField));
        }
        return result;
    }

    /**
     * INTERNAL:
     * Return the classifiction for the field contained in the mapping.
     * This is used to convert the row value to a consistent java value.
     */
    public Class getFieldClassification(DatabaseField fieldToClassify) throws DescriptorException {
        DatabaseField fieldInTarget = (DatabaseField)getSourceToTargetKeyFields().get(fieldToClassify);
        if (fieldInTarget == null) {
            return null;// Can be registered as multiple table secondary field mapping
        }
        DatabaseMapping mapping = getReferenceDescriptor().getObjectBuilder().getMappingForField(fieldInTarget);
        if (mapping == null) {
            return null;// Means that the mapping is read-only
        }
        return mapping.getFieldClassification(fieldInTarget);
    }

    /**
     * PUBLIC:
     * Return the foreign key field names associated with the mapping.
     * These are only the source fields that are writable.
     */
    public Vector getForeignKeyFieldNames() {
        Vector fieldNames = new Vector(getForeignKeyFields().size());
        for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            fieldNames.addElement(((DatabaseField)fieldsEnum.nextElement()).getQualifiedName());
        }

        return fieldNames;
    }

    /**
     * Return the appropriate hashtable that maps the "foreign keys"
     * to the "primary keys".
     */
    protected Map getForeignKeysToPrimaryKeys() {
        if (this.isForeignKeyRelationship()) {
            return this.getSourceToTargetKeyFields();
        } else {
            return this.getTargetToSourceKeyFields();
        }
    }

    /**
     * INTERNAL:
     * Return a vector of the foreign key fields in the same order
     * as the corresponding primary key fields are in their descriptor.
     */
    public Vector getOrderedForeignKeyFields() {
        List primaryKeyFields = getPrimaryKeyDescriptor().getPrimaryKeyFields();
        Vector result = new Vector(primaryKeyFields.size());

        for (int index = 0; index < primaryKeyFields.size(); index++) {
            DatabaseField pkField = (DatabaseField)primaryKeyFields.get(index);
            boolean found = false;
            for (Iterator fkStream = this.getForeignKeysToPrimaryKeys().keySet().iterator();
                     fkStream.hasNext();) {
                DatabaseField fkField = (DatabaseField)fkStream.next();

                if (this.getForeignKeysToPrimaryKeys().get(fkField).equals(pkField)) {
                    found = true;
                    result.addElement(fkField);
                    break;
                }
            }
            if (!found) {
                throw DescriptorException.missingForeignKeyTranslation(this, pkField);
            }
        }
        return result;
    }

    /**
     * Return the descriptor for whichever side of the
     * relation has the "primary key".
    */
    protected ClassDescriptor getPrimaryKeyDescriptor() {
        if (this.isForeignKeyRelationship()) {
            return this.getReferenceDescriptor();
        } else {
            return this.getDescriptor();
        }
    }

    /**
     * INTERNAL:
     * The private owned criteria is only used outside of the unit of work to compare the previous value of the reference.
     */
    public Expression getPrivateOwnedCriteria() {
        if (privateOwnedCriteria == null) {
            initializePrivateOwnedCriteria();
        }
        return privateOwnedCriteria;
    }

    /**
     * INTERNAL:
     * Return a collection of the source to target field value associations.
     */
    public Vector getSourceToTargetKeyFieldAssociations() {
        Vector associations = new Vector(getSourceToTargetKeyFields().size());
        Iterator sourceFieldEnum = getSourceToTargetKeyFields().keySet().iterator();
        Iterator targetFieldEnum = getSourceToTargetKeyFields().values().iterator();
        while (sourceFieldEnum.hasNext()) {
            Object fieldValue = ((DatabaseField)sourceFieldEnum.next()).getQualifiedName();
            Object attributeValue = ((DatabaseField)targetFieldEnum.next()).getQualifiedName();
            associations.addElement(new Association(fieldValue, attributeValue));
        }

        return associations;
    }

    /**
     * INTERNAL:
     * Returns the source keys to target keys fields association.
     */
    public Map<DatabaseField, DatabaseField> getSourceToTargetKeyFields() {
        return sourceToTargetKeyFields;
    }

    /**
     * INTERNAL:
     * Returns the target keys to source keys fields association.
     */
    public Map<DatabaseField, DatabaseField> getTargetToSourceKeyFields() {
        return targetToSourceKeyFields;
    }

    /**
     * INTERNAL:
     * Initialize the mapping.
     */
    public void initialize(AbstractSession session) throws DescriptorException {
        super.initialize(session);
        
        // Must set table of foreign keys.
        for (Enumeration foreignKeysEnum = getForeignKeyFields().elements();
                 foreignKeysEnum.hasMoreElements();) {
            DatabaseField foreignKeyField = (DatabaseField)foreignKeysEnum.nextElement();
            getDescriptor().buildField(foreignKeyField);
        }

        // If only a selection criteria is specified then the foreign keys do not have to be initialized.
        if (!(getTargetToSourceKeyFields().isEmpty() && getSourceToTargetKeyFields().isEmpty())) {
            if (getTargetToSourceKeyFields().isEmpty() || getSourceToTargetKeyFields().isEmpty()) {
                initializeForeignKeysWithDefaults(session);
            } else {
                initializeForeignKeys(session);
            }
        }

        if (shouldInitializeSelectionCriteria()) {
            initializeSelectionCriteria(session);
        } else {
            setShouldVerifyDelete(false);
        }

        setFields(collectFields());
    }

    /**
     * INTERNAL:
     * The foreign keys primary keys are stored as database fields in the hashtable.
     */
    protected void initializeForeignKeys(AbstractSession session) {
        Iterator sourceEnum = getSourceToTargetKeyFields().keySet().iterator();
        Iterator targetEnum = getTargetToSourceKeyFields().keySet().iterator();
        while (sourceEnum.hasNext()) {
            DatabaseField sourceField = (DatabaseField)sourceEnum.next();
            DatabaseField targetField = (DatabaseField)targetEnum.next();

            getDescriptor().buildField(sourceField);
            getReferenceDescriptor().buildField(targetField);
        }
    }

    /**
     * INTERNAL:
     * The foreign keys primary keys are stored as database fields in the hashtable.
     */
    protected void initializeForeignKeysWithDefaults(AbstractSession session) {
        if (isForeignKeyRelationship()) {
            if (getSourceToTargetKeyFields().size() != 1) {
                throw DescriptorException.foreignKeysDefinedIncorrectly(this);
            }
            List<DatabaseField> targetKeys = getReferenceDescriptor().getPrimaryKeyFields();
            if (targetKeys.size() != 1) {
                //target and source keys are not the same size.
                throw DescriptorException.sizeMismatchOfForeignKeys(this);
            }

            //grab the only element out of the Hashtable
            DatabaseField sourceField = (DatabaseField)getSourceToTargetKeyFields().keySet().iterator().next();
            getDescriptor().buildField(sourceField);
            getSourceToTargetKeyFields().put(sourceField, targetKeys.get(0));
            getTargetToSourceKeyFields().put(targetKeys.get(0), sourceField);
        } else {
            if (getTargetToSourceKeyFields().size() != 1) {
                throw DescriptorException.foreignKeysDefinedIncorrectly(this);
            }
            List<DatabaseField> sourceKeys = getDescriptor().getPrimaryKeyFields();
            if (sourceKeys.size() != 1) {
                //target and source keys are not the same size.
                throw DescriptorException.sizeMismatchOfForeignKeys(this);
            }

            //grab the only element out of the Hashtable
            DatabaseField targetField = (DatabaseField)getTargetToSourceKeyFields().keySet().iterator().next();
            getReferenceDescriptor().buildField(targetField);
            getTargetToSourceKeyFields().put(targetField, sourceKeys.get(0));
            getSourceToTargetKeyFields().put(sourceKeys.get(0), targetField);
        }
    }

    /**
     * INTERNAL:
     * Selection criteria is created with source foreign keys and target keys.
     */
    protected void initializePrivateOwnedCriteria() {
        if (!isForeignKeyRelationship()) {
            setPrivateOwnedCriteria(getSelectionCriteria());
        } else {
            Expression pkCriteria = getDescriptor().getObjectBuilder().getPrimaryKeyExpression();
            ExpressionBuilder builder = new ExpressionBuilder();
            Expression backRef = builder.getManualQueryKey(getAttributeName() + "-back-ref", getDescriptor());
            Expression newPKCriteria = pkCriteria.rebuildOn(backRef);
            Expression twistedSelection = backRef.twist(getSelectionCriteria(), builder);
            if (getDescriptor().getQueryManager().getAdditionalJoinExpression() != null) {
                // We don't have to twist the additional join because it's all against the same node, which is our base
                // but we do have to rebuild it onto the manual query key
                Expression rebuiltAdditional = getDescriptor().getQueryManager().getAdditionalJoinExpression().rebuildOn(backRef);
                if (twistedSelection == null) {
                    twistedSelection = rebuiltAdditional;
                } else {
                    twistedSelection = twistedSelection.and(rebuiltAdditional);
                }
            }
            setPrivateOwnedCriteria(newPKCriteria.and(twistedSelection));
        }
    }

    /**
     * INTERNAL:
     * Selection criteria is created with source foreign keys and target keys.
     * This criteria is then used to read target records from the table.
     *
     * CR#3922 - This method is almost the same as buildSelectionCriteria() the difference
     * is that getSelectionCriteria() is called
     */
    protected void initializeSelectionCriteria(AbstractSession session) {
        if (getSourceToTargetKeyFields().isEmpty()) {
            throw DescriptorException.noForeignKeysAreSpecified(this);
        }

        Expression criteria;
        Expression builder = new ExpressionBuilder();
        for (Iterator entries = getSourceToTargetKeyFields().entrySet().iterator(); entries.hasNext();) {
            Map.Entry entry = (Map.Entry) entries.next();
            DatabaseField foreignKey = (DatabaseField)entry.getKey();
            DatabaseField targetKey = (DatabaseField)entry.getValue();
            Expression expression = builder.getField(targetKey).equal(builder.getParameter(foreignKey));
            criteria = expression.and(getSelectionCriteria());
            setSelectionCriteria(criteria);
        }
    }
    
    /**
     * This method would allow customers to get the potential selection criteria for a mapping
     * prior to initialization.  This would allow them to more easily create an ammendment method
     * that would ammend the SQL for the join.
     *
     * CR#3922 - This method is almost the same as initializeSelectionCriteria() the difference
     * is that getSelectionCriteria() is not called
     */
    public Expression buildSelectionCriteria() {
        // CR3922
        if (getSourceToTargetKeyFields().isEmpty()) {
            throw DescriptorException.noForeignKeysAreSpecified(this);
        }

        Expression criteria = null;
        Expression builder = new ExpressionBuilder();

        for (Iterator keys = getSourceToTargetKeyFields().keySet().iterator(); keys.hasNext();) {
            DatabaseField foreignKey = (DatabaseField)keys.next();
            DatabaseField targetKey = (DatabaseField)getSourceToTargetKeyFields().get(foreignKey);

            Expression expression = builder.getField(targetKey).equal(builder.getParameter(foreignKey));
            if (criteria == null) {
                criteria = expression;
            } else {
                criteria = expression.and(criteria);
            }
        }
        return criteria;
    }

    /**
     * INTERNAL:
     * Builds a shallow original object.  Only direct attributes and primary
     * keys are populated.  In this way the minimum original required for
     * instantiating a working copy clone can be built without placing it in
     * the shared cache (no concern over cycles).
     */
    public void buildShallowOriginalFromRow(AbstractRecord databaseRow, Object original, ObjectBuildingQuery query, AbstractSession executionSession) {
        // Now we are only building this original so we can extract the primary
        // key out of it.  If the primary key is stored accross a 1-1 a value
        // holder needs to be built/triggered to get at it.
        // In this case recursively build the shallow original accross the 1-1.
        // We only need the primary key for that object, and we know
        // what that primary key is: it is the foreign key in our row.
        ClassDescriptor descriptor = getReferenceDescriptor();
        AbstractRecord targetRow = new DatabaseRecord();

        for (Iterator keys = getSourceToTargetKeyFields().keySet().iterator(); keys.hasNext();) {
            DatabaseField foreignKey = (DatabaseField)keys.next();
            DatabaseField targetKey = (DatabaseField)getSourceToTargetKeyFields().get(foreignKey);

            targetRow.put(targetKey, databaseRow.get(foreignKey));
        }

        Object targetObject = descriptor.getObjectBuilder().buildNewInstance();
        descriptor.getObjectBuilder().buildAttributesIntoShallowObject(targetObject, databaseRow, query);
        targetObject = getIndirectionPolicy().valueFromRow(targetObject);

        setAttributeValueInObject(original, targetObject);
    }

    /**
     * INTERNAL:
     */
    public boolean isOneToOneMapping() {
        return true;
    }

    /**
     * INTERNAL:
     * Reads the private owned object.
     */
    protected Object readPrivateOwnedForObject(ObjectLevelModifyQuery modifyQuery) throws DatabaseException {
        if (modifyQuery.getSession().isUnitOfWork()) {
            return super.readPrivateOwnedForObject(modifyQuery);
        } else {
            if (!shouldVerifyDelete()) {
                return null;
            }
            ReadObjectQuery readQuery = (ReadObjectQuery)getSelectionQuery().clone();

            readQuery.setSelectionCriteria(getPrivateOwnedCriteria());
            return modifyQuery.getSession().executeQuery(readQuery, modifyQuery.getTranslationRow());
        }
    }

    /**
     * INTERNAL:
     * Rehash any hashtables based on fields.
     * This is used to clone descriptors for aggregates, which hammer field names,
     * it is probably better not to hammer the field name and this should be refactored.
     */
    public void rehashFieldDependancies(AbstractSession session) {
        setSourceToTargetKeyFields(Helper.rehashMap(getSourceToTargetKeyFields()));
    }

    /**
     * PUBLIC:
     * Define the foreign key relationship in the 1-1 mapping.
     * This method is used for singleton foreign key relationships only,
     * that is the source object's table has a foreign key field to
     * the target object's primary key field.
     * Only the source foreign key field name is specified.
     * When a foreign key is specified TopLink will automatically populate the value
     * for that field from the target object when the object is written to the database.
     * If the foreign key is also mapped through a direct-to-field then the direct-to-field must
     * be set read-only.
     */
    public void setForeignKeyFieldName(String sourceForeignKeyFieldName) {
        DatabaseField sourceField = new DatabaseField(sourceForeignKeyFieldName);

        setIsForeignKeyRelationship(true);
        getForeignKeyFields().addElement(sourceField);
        getSourceToTargetKeyFields().put(sourceField, new DatabaseField());
    }

    /**
     * PUBLIC:
     * Return the foreign key field names associated with the mapping.
     * These are only the source fields that are writable.
     */
    public void setForeignKeyFieldNames(Vector fieldNames) {
        Vector fields = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(fieldNames.size());
        for (Enumeration fieldNamesEnum = fieldNames.elements(); fieldNamesEnum.hasMoreElements();) {
            fields.addElement(new DatabaseField((String)fieldNamesEnum.nextElement()));
        }

        setForeignKeyFields(fields);
    }

    /**
     * INTERNAL:
     * Private owned criteria is used to verify the deletion of the target.
     * It joins from the source table on the foreign key to the target table,
     * with a parameterization of the primary key of the source object.
     */
    protected void setPrivateOwnedCriteria(Expression expression) {
        privateOwnedCriteria = expression;
    }

    /**
     * PUBLIC:
     * Verify delete is used during delete and update on private 1:1's outside of a unit of work only.
     * It checks for the previous value of the target object through joining the source and target tables.
     * By default it is always done, but may be disabled for performance on distributed database reasons.
     * In the unit of work the previous value is obtained from the backup-clone so it is never used.
     */
    public void setShouldVerifyDelete(boolean shouldVerifyDelete) {
        this.shouldVerifyDelete = shouldVerifyDelete;
    }

    /**
     * INTERNAL:
     * Set a collection of the source to target field associations.
     */
    public void setSourceToTargetKeyFieldAssociations(Vector sourceToTargetKeyFieldAssociations) {
        setSourceToTargetKeyFields(new HashMap(sourceToTargetKeyFieldAssociations.size() + 1));
        setTargetToSourceKeyFields(new HashMap(sourceToTargetKeyFieldAssociations.size() + 1));
        for (Enumeration associationsEnum = sourceToTargetKeyFieldAssociations.elements();
                 associationsEnum.hasMoreElements();) {
            Association association = (Association)associationsEnum.nextElement();
            DatabaseField sourceField = new DatabaseField((String)association.getKey());
            DatabaseField targetField = new DatabaseField((String)association.getValue());
            getSourceToTargetKeyFields().put(sourceField, targetField);
            getTargetToSourceKeyFields().put(targetField, sourceField);
        }
    }

    /**
     * INTERNAL:
     * Set the source keys to target keys fields association.
     */
    public void setSourceToTargetKeyFields(Map<DatabaseField, DatabaseField> sourceToTargetKeyFields) {
        this.sourceToTargetKeyFields = sourceToTargetKeyFields;
    }

    /**
     * PUBLIC:
     * Define the target foreign key relationship in the 1-1 mapping.
     * This method is used for singleton target foreign key relationships only,
     * that is the target object's table has a foreign key field to
     * the source object's primary key field.
     * The target foreign key field name is specified.
     * The distinction between a foreign key and target foreign key is that the 1-1
     * mapping will not populate the target foreign key value when written (because it is in the target table).
     * Normally 1-1's are through foreign keys but in bi-directional 1-1's
     * the back reference will be a target foreign key.
     */
    public void setTargetForeignKeyFieldName(String targetForeignKeyFieldName) {
        DatabaseField targetField = new DatabaseField(targetForeignKeyFieldName);
        getTargetToSourceKeyFields().put(targetField, new DatabaseField());
    }

    /**
     * INTERNAL:
     * Set the target keys to source keys fields association.
     */
    public void setTargetToSourceKeyFields(Map<DatabaseField, DatabaseField> targetToSourceKeyFields) {
        this.targetToSourceKeyFields = targetToSourceKeyFields;
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be joined on read queries.
     * Joining will join the two classes tables to read all of the data in a single query.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public void setUsesJoining(boolean usesJoining) {
        if (usesJoining == this.usesJoining) {
            return;
        }
        this.usesJoining = usesJoining;

        // For 3524579 now cache joined mappings on the object builder.
        // This allows a user to set joining dynamically after the
        // descriptors have been initialized.  Generally this is not
        // supported, but since we were checking this flag in prepare after
        // initialization some degree of backward compatibility should be
        // provided.
        if (getDescriptor() != null) {
            getDescriptor().reInitializeJoinedAttributes();
        }

        // Still every query which is already prepared, like all selection
        // queries, will not pick up this change.
    }

    /**
     * PUBLIC:
     * Indicates whether the referenced object should always be joined on read queries.
     * Joining will join the two classes tables to read all of the data in a single query.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public boolean shouldUseJoining() {
        return usesJoining;
    }

    /**
     * PUBLIC:
     * Verify delete is used during delete and update outside of a unit of work only.
     * It checks for the previous value of the target object through joining the source and target tables.
     */
    public boolean shouldVerifyDelete() {
        return shouldVerifyDelete;
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
     * PUBLIC:
     * Indicates whether the referenced object should always be joined on read queries.
     * Joining will join the two classes tables to read all of the data in a single query.
     * This should only be used if it is know that the related objects are always required with the source object, or indirection is not used.
     */
    public void useJoining() {
        setUsesJoining(true);
    }

    /**
     * INTERNAL:
     * A subclass should implement this method if it wants different behaviour.
     * Write the foreign key values from the attribute to the row.
     */
    public void writeFromAttributeIntoRow(Object attribute, AbstractRecord row, AbstractSession session)
    {
          for (Enumeration fieldsEnum = getForeignKeyFields().elements(); fieldsEnum.hasMoreElements();) {
                  DatabaseField sourceKey = (DatabaseField) fieldsEnum.nextElement();
                  DatabaseField targetKey = (DatabaseField) getSourceToTargetKeyFields().get(sourceKey);
                  Object referenceValue = null;
                          // If privately owned part is null then method cannot be invoked.
                  if (attribute != null) {
                          referenceValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(attribute, targetKey, session);
                  }
                  row.add(sourceKey, referenceValue);
          }
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public Object valueFromObject(Object object, DatabaseField field, AbstractSession session) {
        // First check if the value can be obtained from the value holder's row.
        AbstractRecord referenceRow = getIndirectionPolicy().extractReferenceRow(getAttributeValueFromObject(object));
        if (referenceRow != null) {
            Object value = referenceRow.get(field);

            // Must ensure the classification to get a cache hit.
            try {
                value = session.getDatasourcePlatform().convertObject(value, getFieldClassification(field));
            } catch (ConversionException e) {
                throw ConversionException.couldNotBeConverted(this, getDescriptor(), e);
            }
            return value;
        }

        Object referenceObject = getRealAttributeValueFromObject(object, session);
        if (referenceObject == null) {
            return null;
        }
        DatabaseField targetField = (DatabaseField)getSourceToTargetKeyFields().get(field);

        return getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(referenceObject, targetField, session);
    }

    /**
     * INTERNAL:
     * If the query used joining or partial attributes, build the target object directly.
     */
    protected Object valueFromRowInternalWithJoin(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) throws DatabaseException {
        // PERF: Direct variable access.
        Object referenceObject;
        // CR #... the field for many objects may be in the row,
        // so build the subpartion of the row through the computed values in the query,
        // this also helps the field indexing match.
        AbstractRecord targetRow = trimRowForJoin(row, joinManager, executionSession);
        // PERF: Only check for null row if an outer-join was used.
        if (joinManager.isAttributeJoined(getDescriptor(), getAttributeName()) && joinManager.hasOuterJoinedAttributeQuery()) {
            Vector key = this.referenceDescriptor.getObjectBuilder().extractPrimaryKeyFromRow(targetRow, executionSession);
            if (key == null) {
                return this.indirectionPolicy.nullValueFromRow();
            }
        }
        // A nested query must be built to pass to the descriptor that looks like the real query execution would,
        // these should be cached on the query during prepare.
        ObjectLevelReadQuery nestedQuery = null;
        if (joinManager.getJoinedMappingQueries_() != null) {
            nestedQuery = (ObjectLevelReadQuery) joinManager.getJoinedMappingQueries_().get(this);
        } else {
            nestedQuery = prepareNestedJoins(joinManager, executionSession);
        }
        nestedQuery = (ObjectLevelReadQuery)nestedQuery.clone();
        nestedQuery.setTranslationRow(targetRow);
        nestedQuery.setSession(executionSession);            
        //CR #4365 - used to prevent infinite recursion on refresh object cascade all
        nestedQuery.setQueryId(joinManager.getBaseQuery().getQueryId());
        referenceObject = this.referenceDescriptor.getObjectBuilder().buildObject(nestedQuery, targetRow, nestedQuery.getJoinedAttributeManager());

        // For bug 3641713 buildObject doesn't wrap if called on a UnitOfWork for performance reasons,
        // must wrap here as this is the last time we can look at the query and tell whether to wrap or not.
        if (nestedQuery.shouldUseWrapperPolicy() && nestedQuery.getSession().isUnitOfWork()) {
            referenceObject = this.referenceDescriptor.getObjectBuilder().wrapObject(referenceObject, nestedQuery.getSession());
        }
        return this.indirectionPolicy.valueFromRow(referenceObject);
    }
    
    /**
     * INTERNAL:
     * Return the value of the field from the row or a value holder on the query to obtain the object.
     * Check for batch + aggregation reading.
     */
    protected Object valueFromRowInternal(AbstractRecord row, JoinedAttributeManager joinManager, AbstractSession executionSession) throws DatabaseException {
        // If any field in the foreign key is null then it means there are no referenced objects
        // Skip for partial objects as fk may not be present.
        int size = this.fields.size();
        for (int index = 0; index < size; index++) {
            DatabaseField field = (DatabaseField)this.fields.get(index);
            if (row.get(field) == null) {
                return this.indirectionPolicy.nullValueFromRow();
            }
        }

        // Call the default which executes the selection query,
        // or wraps the query with a value holder.
        return super.valueFromRowInternal(row, joinManager, executionSession);
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     */
    public void writeFromObjectIntoRow(Object object, AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly() || (!isForeignKeyRelationship())) {
            return;
        }

        AbstractRecord referenceRow = getIndirectionPolicy().extractReferenceRow(getAttributeValueFromObject(object));
        if (referenceRow == null) {
            // Extract from object.
            Object referenceObject = getRealAttributeValueFromObject(object, session);

            for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                     fieldsEnum.hasMoreElements();) {
                DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
                DatabaseField targetKey = (DatabaseField)getSourceToTargetKeyFields().get(sourceKey);

                Object referenceValue = null;

                // If privately owned part is null then method cannot be invoked.
                if (referenceObject != null) {
                    referenceValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(referenceObject, targetKey, session);
                }
                databaseRow.add(sourceKey, referenceValue);
            }
        } else {
            for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                     fieldsEnum.hasMoreElements();) {
                DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
                Object referenceValue = referenceRow.get(sourceKey);
                databaseRow.add(sourceKey, referenceValue);
            }
        }
    }

    /**
     * INTERNAL:
     * This row is built for shallow update which happens in case of bidirectional deletes.
     */
    public void writeFromObjectIntoRowForShallowDelete(Object object, AbstractRecord databaseRow, AbstractSession session) {
        writeFromObjectIntoRowForShallowOperation(object, databaseRow, session);
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     */
    public void writeFromObjectIntoRowForShallowInsert(Object object, AbstractRecord databaseRow, AbstractSession session) {
        writeFromObjectIntoRowForShallowOperation(object, databaseRow, session);
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert or delete which happens in case of bidirectional relationships.
     * The foreign keys must be set to null to avoid foreign key constraint violations.
     */
    protected void writeFromObjectIntoRowForShallowOperation(Object object, AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly() || (!isForeignKeyRelationship())) {
            return;
        }

        for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
            databaseRow.add(sourceKey, null);
        }
    }

    /**
     * INTERNAL:
     * Get a value from the object and set that in the respective field of the row.
     * Validation preventing primary key updates is implemented here.
     */
    public void writeFromObjectIntoRowWithChangeRecord(ChangeRecord changeRecord, AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly() || (!isForeignKeyRelationship())) {
            return;
        }

        if (isPrimaryKeyMapping() && !changeRecord.getOwner().isNew()) {
           throw ValidationException.primaryKeyUpdateDisallowed(changeRecord.getOwner().getClassName(), changeRecord.getAttribute());
        }
        
        // the object must be used here as the foreign key may include more than just the
        // primary key of the referenced object and the changeSet may not have the rquired information
        Object object = ((ObjectChangeSet)changeRecord.getOwner()).getUnitOfWorkClone();
        AbstractRecord referenceRow = getIndirectionPolicy().extractReferenceRow(getAttributeValueFromObject(object));
        if (referenceRow == null) {
            // Extract from object.
            Object referenceObject = getRealAttributeValueFromObject(object, session);

            for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                     fieldsEnum.hasMoreElements();) {
                DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
                DatabaseField targetKey = (DatabaseField)getSourceToTargetKeyFields().get(sourceKey);

                Object referenceValue = null;

                // If privately owned part is null then method cannot be invoked.
                if (referenceObject != null) {
                    referenceValue = getReferenceDescriptor().getObjectBuilder().extractValueFromObjectForField(referenceObject, targetKey, session);
                }
                databaseRow.add(sourceKey, referenceValue);
            }
        } else {
            for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                     fieldsEnum.hasMoreElements();) {
                DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
                Object referenceValue = referenceRow.get(sourceKey);
                databaseRow.add(sourceKey, referenceValue);
            }
        }
    }

    /**
     * INTERNAL:
     * This row is built for shallow insert which happens in case of bidirectional inserts.
     * The foreign keys must be set to null to avoid constraints.
     */
    public void writeFromObjectIntoRowForShallowInsertWithChangeRecord(ChangeRecord ChangeRecord, AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly() || (!isForeignKeyRelationship())) {
            return;
        }

        for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
            databaseRow.add(sourceKey, null);
        }
    }

    /**
     * INTERNAL:
     * Write fields needed for insert into the template for with null values.
     */
    public void writeInsertFieldsIntoRow(AbstractRecord databaseRow, AbstractSession session) {
        if (isReadOnly() || (!isForeignKeyRelationship())) {
            return;
        }

        for (Enumeration fieldsEnum = getForeignKeyFields().elements();
                 fieldsEnum.hasMoreElements();) {
            DatabaseField sourceKey = (DatabaseField)fieldsEnum.nextElement();
            databaseRow.add(sourceKey, null);
        }
    }
}
