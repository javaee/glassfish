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

package oracle.toplink.essentials.internal.expressions;

import java.io.*;
import java.util.*;
import oracle.toplink.essentials.descriptors.FetchGroupManager;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.mappings.foundation.AbstractDirectMapping;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.querykeys.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * Represents expression on query keys or mappings.
 * This includes direct, relationships query keys and mappings.
 */
public class QueryKeyExpression extends ObjectExpression {

    /** The name of the query key. */
    protected String name;

    /** Cache the aliased field. Only applies to attributes. */
    protected DatabaseField aliasedField;

    /** Is this a query across a 1:many or many:many relationship. Does not apply to attributes. */
    protected boolean shouldQueryToManyRelationship;
    
    /** Cache the query key for performance. Store a boolean so we don't repeat the search if there isn't one. */
    transient protected QueryKey queryKey;
    protected boolean hasQueryKey;

    /** Same for mappings. */
    transient protected DatabaseMapping mapping;
    protected boolean hasMapping;

    public QueryKeyExpression() {
        this.shouldQueryToManyRelationship = false;
        this.hasQueryKey = true;
        this.hasMapping = true;
    }

    public QueryKeyExpression(String aName, Expression base) {
        super();
        name = aName;
        baseExpression = base;
        shouldUseOuterJoin = false;
        shouldQueryToManyRelationship = false;
        hasQueryKey = true;
        hasMapping = true;
    }

    /**
     * INTERNAL:
     * Return the expression to join the main table of this node to any auxiliary tables.
     */
    public Expression additionalExpressionCriteria() {
        if (getDescriptor() == null) {
            return null;
        }

        Expression criteria = getDescriptor().getQueryManager().getAdditionalJoinExpression();
        if (criteria != null) {
            criteria = getBaseExpression().twist(criteria, this);
            if (shouldUseOuterJoin() && getSession().getPlatform().shouldPrintOuterJoinInWhereClause()) {
                criteria.convertToUseOuterJoin();
            }
        }

        if(getSession().getPlatform().shouldPrintOuterJoinInWhereClause()) {
            if(isUsingOuterJoinForMultitableInheritance()) {
                Expression childrenCriteria = getDescriptor().getInheritancePolicy().getChildrenJoinExpression();
                childrenCriteria = getBaseExpression().twist(childrenCriteria, this);
                childrenCriteria.convertToUseOuterJoin();
                if(criteria == null) {
                    criteria = childrenCriteria;
                } else {
                    criteria = criteria.and(childrenCriteria);
                }
            }
        }

        return criteria;
    }

    /**
     * INTERNAL:
     * Used in case outer joins should be printed in FROM clause.
     * Each of the additional tables mapped to expressions that joins it.
     */
    public Map additionalExpressionCriteriaMap() {
        if (getDescriptor() == null) {
            return null;
        }

        HashMap tablesJoinExpressions = new HashMap();
        Vector tables = getDescriptor().getTables();
        // skip the main table - start with i=1
        int tablesSize = tables.size();
        if(shouldUseOuterJoin()) {
            for( int i=1; i < tablesSize; i++) {
                DatabaseTable table = (DatabaseTable)tables.elementAt(i);
                Expression joinExpression = (Expression)getDescriptor().getQueryManager().getTablesJoinExpressions().get(table);
                joinExpression = getBaseExpression().twist(joinExpression, this);
                tablesJoinExpressions.put(table, joinExpression);
            }
        }
        if(isUsingOuterJoinForMultitableInheritance()) {
            List childrenTables = getDescriptor().getInheritancePolicy().getChildrenTables();
            tablesSize = childrenTables.size();
            for( int i=0; i < tablesSize; i++) {
                DatabaseTable table = (DatabaseTable)childrenTables.get(i);
                Expression joinExpression = (Expression)getDescriptor().getInheritancePolicy().getChildrenTablesJoinExpressions().get(table);
                joinExpression = getBaseExpression().twist(joinExpression, this);
                tablesJoinExpressions.put(table, joinExpression);
            }
        }
        
        return tablesJoinExpressions;
    }

    /**
     * INTERNAL:
     * Find the alias for a given table
     */
    public DatabaseTable aliasForTable(DatabaseTable table) {
        if (isAttribute() || ((getMapping() != null) && (getMapping().isAggregateObjectMapping() || getMapping().isTransformationMapping()))) {
            return ((DataExpression)getBaseExpression()).aliasForTable(table);
        }

        //"ref" and "structure" mappings, no table printed in the FROM clause, need to get the table alias form the parent table
        if ((getMapping() != null) && (getMapping().isReferenceMapping() || getMapping().isStructureMapping())) {
            DatabaseTable alias = getBaseExpression().aliasForTable((DatabaseTable)getMapping().getDescriptor().getTables().firstElement());
            alias.setName(alias.getName() + "." + getMapping().getField().getName());
            return alias;
        }

        return super.aliasForTable(table);
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Query Key";
    }

    /**
     * INTERNAL:
     */
    public void doQueryToManyRelationship() {
        shouldQueryToManyRelationship = true;
    }

    /**
     * INTERNAL:
     * Return the field appropriately aliased
     */
    public DatabaseField getAliasedField() {
        if (aliasedField == null) {
            initializeAliasedField();
        }
        return aliasedField;

    }

    /**
     * Return the alias for our table
     */
    protected DatabaseTable getAliasedTable() {
        DataExpression base = (DataExpression)getBaseExpression();

        DatabaseTable alias = base.aliasForTable(getField().getTable());
        if (alias == null) {
            return getField().getTable();
        } else {
            return alias;
        }
    }

    /**
     * INTERNAL:
     * Return the descriptor which contains this query key.
     */
    public ClassDescriptor getContainingDescriptor() {
        return ((DataExpression)getBaseExpression()).getDescriptor();
    }

    /**
     * INTERNAL:
     */
    public DatabaseField getField() {
        if (!isAttribute()) {
            return null;
        }

        return getContainingDescriptor().getObjectBuilder().getFieldForQueryKeyName(getName());
    }

    /**
     * INTERNAL:
     * Return all the fields
     */
    public Vector getFields() {
        if (isAttribute()) {
            Vector result = new Vector(1);
            DatabaseField field = getField();
            if (field != null) {
                result.addElement(field);
            }
            return result;
        } else if ((getMapping() != null) && getMapping().isTransformationMapping()) {
            return getMapping().getFields();
        } else {
            if(isUsingOuterJoinForMultitableInheritance()) {
                return getDescriptor().getAllFields();
            } else {
                return super.getFields();
            }
        }
    }

    /**
     * INTERNAL:
     * Transform the object-level value into a database-level value
     */
    public Object getFieldValue(Object objectValue) {
        DatabaseMapping mapping = getMapping();
        Object fieldValue = objectValue;
        if ((mapping != null) && (mapping.isDirectToFieldMapping())) {
            // CR#3623207, check for IN Vector here not in mapping.
            if (objectValue instanceof Vector) {
                // This can actually be a vector for IN within expressions... however it would be better for expressions to handle this.
                Vector values = (Vector)objectValue;
                Vector fieldValues = new Vector(values.size());
                for (int index = 0; index < values.size(); index++) {
                    fieldValues.addElement(getFieldValue(values.get(index)));
                }
                fieldValue = fieldValues;
            } else {
                fieldValue = ((AbstractDirectMapping)mapping).getFieldValue(objectValue, getSession());
            }
        }

        return fieldValue;
    }

    public DatabaseMapping getMapping() {
        if (!hasMapping) {
            return null;
        }

        if (mapping == null) {
            mapping = super.getMapping();
            if (mapping == null) {
                hasMapping = false;
            }
        }
        return mapping;
    }

    public DatabaseMapping getMappingFromQueryKey() {
        QueryKey queryKey = getQueryKeyOrNull();
        if ((queryKey == null) || (!(queryKey instanceof DirectQueryKey))) {
            throw QueryException.cannotConformExpression();
        }
        mapping = queryKey.getDescriptor().getObjectBuilder().getMappingForField(((DirectQueryKey)queryKey).getField());
        if (mapping == null) {
            throw QueryException.cannotConformExpression();
        }
        return mapping;
    }

    public String getName() {
        return name;
    }

    /**
     * INTERNAL:
     */
    public Vector getOwnedTables() {
        if ((getMapping() != null) && (getMapping().isReferenceMapping() || getMapping().isStructureMapping())) {
            return null;
        }

        return super.getOwnedTables();
    }

    public QueryKey getQueryKeyOrNull() {
        if (!hasQueryKey) {
            return null;
        }

        // Oct 19, 2000 JED
        // Added try/catch. This was throwing a NPE in the following case
        // expresssionBuilder.get("firstName").get("bob")
        //moved by Gordon Yorke to cover validate and normalize
        if (getContainingDescriptor() == null) {
            throw QueryException.invalidQueryKeyInExpression(getName());
        }
        if (queryKey == null) {
            queryKey = getContainingDescriptor().getQueryKeyNamed(getName());
            if (queryKey == null) {
                hasQueryKey = false;
            }
        }
        return queryKey;

    }

    /**
     * INTERNAL:
     * Alias the database field for our current environment
     */
    protected void initializeAliasedField() {
        DatabaseField tempField = (DatabaseField)getField().clone();
        DatabaseTable aliasedTable = getAliasedTable();

        //  Put in a special check here so that if the aliasing does nothing we don't cache the
        // result because it's invalid. This saves us from caching premature data if e.g. debugging
        // causes us to print too early"
        //	if (aliasedTable.equals(getField().getTable())) {
        //		return;
        //	} else {
        aliasedField = tempField;
        aliasedField.setTable(aliasedTable);
        //	}
    }

    /**
     * INTERNAL:
     */
    public boolean isAttribute() {
        if (getSession() == null) {// We can't tell, so say no
            return false;
        }

        try {
            QueryKey queryKey = getQueryKeyOrNull();
            if (queryKey != null) {
                return queryKey.isDirectQueryKey();
            }

            DatabaseMapping mapping = getMapping();
            if (mapping != null) {
                if (mapping.isVariableOneToOneMapping()) {
                    throw QueryException.cannotQueryAcrossAVariableOneToOneMapping(mapping, mapping.getDescriptor());
                } else {
                    return mapping.isDirectToFieldMapping();
                }
            }
        } catch (QueryException e) {
            throw e;//re-throw the query exception arisen from the query on varibale 1:1 mapping
        }
        return false;
    }

    public boolean isQueryKeyExpression() {
        return true;
    }

    /*
     * INTERNAL:
     * If this query key respresents a foreign reference answer the
     * base expression -> foreign reference join criteria.
     */
    public Expression mappingCriteria() {
        Expression selectionCriteria;

        // First look for a query key, then a mapping
        if (getQueryKeyOrNull() == null) {
            if ((getMapping() == null) || (!getMapping().isForeignReferenceMapping())) {
                return null;
            } else {
                // The join criteria is now twisted by the mappings.
                selectionCriteria = ((ForeignReferenceMapping)getMapping()).getJoinCriteria(this);
            }
        } else {
            if (!getQueryKeyOrNull().isForeignReferenceQueryKey()) {
                return null;
            } else {
                selectionCriteria = ((ForeignReferenceQueryKey)getQueryKeyOrNull()).getJoinCriteria();
                selectionCriteria = getBaseExpression().twist(selectionCriteria, this);
            }
        }

        if (shouldUseOuterJoin() && getSession().getPlatform().shouldPrintOuterJoinInWhereClause()) {
            selectionCriteria = selectionCriteria.convertToUseOuterJoin();
        }

        return selectionCriteria;
    }

    /**
     * INTERNAL:
     * Normalize the expression into a printable structure.
     * Any joins must be added to form a new root.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        return normalize(normalizer, null);
    }

    /**
     * INTERNAL:
     * For CR#2456 if this is part of an objExp.equal(objExp), do not need to add
     * additional expressions to normalizer both times, and the foreign key join
     * replaces the equal expression.
     */
    public Expression normalize(ExpressionNormalizer normalizer, Vector foreignKeyJoinPointer) {
        if (hasBeenNormalized()) {
            return this;
        }
        super.normalize(normalizer);

        setHasBeenNormalized(true);
        if ((getMapping() != null) && getMapping().isDirectToXMLTypeMapping()) {
            normalizer.getStatement().setRequiresAliases(true);
        }

        // Check if any joins need to be added.
        if (isAttribute()) {
            return this;
        }

        // If the mapping is 'ref' or 'structure', no join needed.
        if ((getMapping() != null) && (getMapping().isReferenceMapping() || getMapping().isStructureMapping())) {
            normalizer.getStatement().setRequiresAliases(true);
            return this;
        }

        // Compute if a distinct is required during normalization.
        if (shouldQueryToManyRelationship() && (!normalizer.getStatement().isDistinctComputed()) && (!normalizer.getStatement().isAggregateSelect())) {
            normalizer.getStatement().useDistinct();
        }

        // Turn off DISTINCT if nestedTableMapping is used (not supported by Oracle 8.1.5).
        if ((getMapping() != null) && getMapping().isNestedTableMapping()) {
            // There are two types of nested tables, one used by clients, one used by mappings, do nothing in the mapping case.
            if (!shouldQueryToManyRelationship()) {
                return this;
            }
            normalizer.getStatement().dontUseDistinct();
        }

        Expression mappingExpression = mappingCriteria();
        if (mappingExpression != null) {
            mappingExpression = mappingExpression.normalize(normalizer);
        }
        if (mappingExpression != null) {
            // If the join was an outer join we must not add the join criteria to the where clause,
            // if the platform prints the join in the from clause.
            if (shouldUseOuterJoin() && (getSession().getPlatform().isInformixOuterJoin())) {
                normalizer.getStatement().getOuterJoinExpressions().addElement(this);
                normalizer.getStatement().getOuterJoinedMappingCriteria().addElement(mappingExpression);
                normalizer.addAdditionalExpression(mappingExpression.and(additionalExpressionCriteria()));
                return this;
            } else if ((shouldUseOuterJoin() || isUsingOuterJoinForMultitableInheritance()) && (!getSession().getPlatform().shouldPrintOuterJoinInWhereClause())) {
                if(shouldUseOuterJoin()) {
                    normalizer.getStatement().getOuterJoinExpressions().addElement(this);
                    normalizer.getStatement().getOuterJoinedMappingCriteria().addElement(mappingExpression);
                    normalizer.getStatement().getOuterJoinedAdditionalJoinCriteria().addElement(additionalExpressionCriteriaMap());
                    normalizer.getStatement().getDescriptorsForMultitableInheritanceOnly().add(null);
                    return this;
                } else {
                    if (isUsingOuterJoinForMultitableInheritance()) {
                        normalizer.getStatement().getOuterJoinExpressions().addElement(null);
                        normalizer.getStatement().getOuterJoinedMappingCriteria().addElement(null);
                        normalizer.getStatement().getOuterJoinedAdditionalJoinCriteria().addElement(additionalExpressionCriteriaMap());
                        normalizer.getStatement().getDescriptorsForMultitableInheritanceOnly().add(getMapping().getReferenceDescriptor());
                        // fall through to the main case
                    }
                }
            }
            
            // This must be added even if outer. Actually it should be converted to use a right outer join, but that gets complex
            // so we do not support this current which is a limitation in some cases.
            if (foreignKeyJoinPointer != null) {
                // If this expression is right side of an objExp.equal(objExp), one
                // need not add additionalExpressionCriteria twice.
                // Also the join will replace the original objExp.equal(objExp).
                // For CR#2456.
                foreignKeyJoinPointer.add(mappingExpression);
            } else {
                normalizer.addAdditionalExpression(mappingExpression.and(additionalExpressionCriteria()));
            }
        }

        // For bug 2900974 special code for DirectCollectionMappings moved to printSQL.
        return this;
    }

    /**
     * INTERNAL:
     * Print SQL onto the stream, using the ExpressionPrinter for context
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        if (isAttribute()) {
            printer.printField(getAliasedField());
        }

        // If the mapping is a direct collection then this falls into a gray area.
        // It must be treated as an attribute at this moment for it has a direct field.
        // However it is not an attribute in the sense that it also represents a foreign
        // reference and a mapping criteria has been added.
        // For bug 2900974 these are now handled as non-attributes during normalize but
        // as attributes when printing SQL.
        //
        if ((getMapping() != null) && getMapping().isDirectCollectionMapping()) {
            DirectCollectionMapping directCollectionMapping = (DirectCollectionMapping)getMapping();

            // The aliased table comes for free as it was a required part of the join criteria.
            TableExpression table = (TableExpression)getTable(directCollectionMapping.getReferenceTable());
            DatabaseTable aliasedTable = table.aliasForTable(table.getTable());
            DatabaseField aliasedField = (DatabaseField)directCollectionMapping.getDirectField().clone();
            aliasedField.setTable(aliasedTable);
            printer.printField(aliasedField);
        }
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    public void printJava(ExpressionJavaPrinter printer) {
        getBaseExpression().printJava(printer);
        if (!shouldUseOuterJoin()) {
            if (!shouldQueryToManyRelationship()) {
                printer.printString(".get(");
            } else {
                printer.printString(".anyOf(");
            }
        } else {
            if (!shouldQueryToManyRelationship()) {
                printer.printString(".getAllowingNull(");
            } else {
                printer.printString(".anyOfAllowingNone(");
            }
        }
        printer.printString("\"" + getName() + "\")");
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        Expression newLocalBase = getBaseExpression().rebuildOn(newBase);
        QueryKeyExpression result = null;

        // For bug 3096634 rebuild outer joins correctly from the start.
        if (shouldUseOuterJoin) {
            result = (QueryKeyExpression)newLocalBase.getAllowingNull(getName());
        } else {
            result = (QueryKeyExpression)newLocalBase.get(getName());
        }
        if (shouldQueryToManyRelationship) {
            result.doQueryToManyRelationship();
        }
        return result;
    }

    /**
     * INTERNAL:
     * A special version of rebuildOn where the newBase need not be a new
     * ExpressionBuilder but any expression.
     * <p>
     * For nested joined attributes, the joined attribute query must have
     * its joined attributes rebuilt relative to it.
     */
    public Expression rebuildOn(Expression oldBase, Expression newBase) {
        if (this == oldBase) {
            return newBase;
        }
        Expression newLocalBase = ((QueryKeyExpression)getBaseExpression()).rebuildOn(oldBase, newBase);
        QueryKeyExpression result = null;

        // For bug 3096634 rebuild outer joins correctly from the start.
        if (shouldUseOuterJoin) {
            result = (QueryKeyExpression)newLocalBase.getAllowingNull(getName());
        } else {
            result = (QueryKeyExpression)newLocalBase.get(getName());
        }
        if (shouldQueryToManyRelationship) {
            result.doQueryToManyRelationship();
        }
        result.setSelectIfOrderedBy(selectIfOrderedBy());
        return result;
    }

    /**
     * Reset cached information here so that we can be sure we're accurate.
     */
    protected void resetCache() {
        hasMapping = true;
        mapping = null;
        hasQueryKey = true;
        queryKey = null;
    }

    public boolean shouldQueryToManyRelationship() {
        return shouldQueryToManyRelationship;
    }

    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        Expression twistedBase = getBaseExpression().twistedForBaseAndContext(newBase, context);
        QueryKeyExpression result = (QueryKeyExpression)twistedBase.get(getName());
        if (shouldUseOuterJoin) {
            result.doUseOuterJoin();
        }
        if (shouldQueryToManyRelationship) {
            result.doQueryToManyRelationship();
        }
        return result;

    }

    /**
     * Do any required validation for this node. Throw an exception if it's incorrect.
     */
    public void validateNode() {
        if ((getQueryKeyOrNull() == null) && (getMapping() == null)) {
            throw QueryException.invalidQueryKeyInExpression(getName());
        }

        QueryKey queryKey = getQueryKeyOrNull();
        DatabaseMapping mapping = getMapping();

        Object theOneThatsNotNull = null;
        if (queryKey != null) {
            theOneThatsNotNull = queryKey;
        }
        if (mapping != null) {
            theOneThatsNotNull = mapping;
        }

        boolean qkIsToMany = false;
        if (queryKey != null) {
            qkIsToMany = queryKey.isManyToManyQueryKey() || queryKey.isOneToManyQueryKey();
        }
        if (mapping != null) {
            // Bug 2847621 - Add Aggregate Collection to the list of valid items for outer join.
            if (shouldUseOuterJoin && (!(mapping.isOneToOneMapping() || mapping.isOneToManyMapping() || mapping.isManyToManyMapping() || mapping.isAggregateCollectionMapping() || mapping.isDirectCollectionMapping()))) {
                throw QueryException.outerJoinIsOnlyValidForOneToOneMappings(getMapping());
            }
            qkIsToMany = mapping.isCollectionMapping();
        }
        if ((!shouldQueryToManyRelationship()) && qkIsToMany && (!mapping.isNestedTableMapping())) {
            throw QueryException.invalidUseOfToManyQueryKeyInExpression(theOneThatsNotNull);
        }
        if (shouldQueryToManyRelationship() && !qkIsToMany) {
            throw QueryException.invalidUseOfAnyOfInExpression(theOneThatsNotNull);
        }
    }

    /**
     * INTERNAL:
     * Return the value for in memory comparison.
     * This is only valid for valueable expressions.
     */
    public Object valueFromObject(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        // The expression may be across a relationship, in which case it must be traversed.
        if ((getBuilder() != getBaseExpression()) && getBaseExpression().isQueryKeyExpression()) {
            object = getBaseExpression().valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);

            // toDo: Null means the join filters out the row, returning null is not correct if an inner join,
            // outer/inner joins need to be fixed to filter correctly.
            if (object == null) {
                return null;
            }

            // If from an anyof the object will be a collection of values,
            // A new vector must union the object values and the values extracted from it.
            if (object instanceof Vector) {
                Vector comparisonVector = new Vector(((Vector)object).size() + 2);
                for (Enumeration valuesToIterate = (Enumeration)((Vector)object).elements();
                         valuesToIterate.hasMoreElements();) {
                    Object vectorObject = (Object)valuesToIterate.nextElement();
                    if (vectorObject == null) {
                        comparisonVector.addElement(vectorObject);
                    } else {
                        Object valueOrValues = valuesFromCollection(vectorObject, session, valueHolderPolicy, isObjectUnregistered);

                        // If a collection of values were extracted union them.
                        if (valueOrValues instanceof Vector) {
                            for (Enumeration nestedValuesToIterate = (Enumeration)((Vector)valueOrValues).elements();
                                     nestedValuesToIterate.hasMoreElements();) {
                                comparisonVector.addElement(nestedValuesToIterate.nextElement());
                            }
                        } else {
                            comparisonVector.addElement(valueOrValues);
                        }
                    }
                }
                return comparisonVector;
            }
        }
        return valuesFromCollection(object, session, valueHolderPolicy, isObjectUnregistered);
    }

    /**
     * INTERNAL
     * This method iterates through a collection and gets the values from the objects to conform in an in-memory query.
     * Creation date: (1/19/01 1:18:27 PM)
     */
    public Object valuesFromCollection(Object object, AbstractSession session, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        // in case the mapping is null - this can happen if a query key is being used
        // In this case, check for the query key and find it's mapping.
        boolean readMappingFromQueryKey = false;
        if (getMapping() == null) {
            getMappingFromQueryKey();
            readMappingFromQueryKey = true;
        }

        // For bug 2780817 get the mapping directly from the object.  In EJB 2.0 
        // inheritance, each child must override mappings defined in an abstract 
        // class with its own.
        DatabaseMapping mapping = this.mapping;
        if (mapping.getDescriptor().hasInheritance() && (mapping.getDescriptor().getJavaClass() != object.getClass())) {
            mapping = session.getDescriptor(object.getClass()).getObjectBuilder().getMappingForAttributeName(getName());
        }

        //fetch group support
        if (mapping.getDescriptor().hasFetchGroupManager()) {
            FetchGroupManager fetchGroupMgr = mapping.getDescriptor().getFetchGroupManager();
            if (fetchGroupMgr.isPartialObject(object) && (!fetchGroupMgr.isAttributeFetched(object, mapping.getAttributeName()))) {
                //the conforming attribute is not fetched, simply throw exception
                throw QueryException.cannotConformUnfetchedAttribute(mapping.getAttributeName());
            }
        }

        if (mapping.isForeignReferenceMapping()) {
            //CR 3677 integration of a ValueHolderPolicy
            Object valueFromMapping = mapping.getAttributeValueFromObject(object);
            if (!((ForeignReferenceMapping)mapping).getIndirectionPolicy().objectIsInstantiated(valueFromMapping)) {
                if (!valueHolderPolicy.shouldTriggerIndirection()) {
                    //If the client wishes us to trigger the indirection then we should do so,
                    //Other wise throw the exception
                    throw QueryException.mustInstantiateValueholders();// you should instantiate the valueholder for this to work
                }

                // maybe we should throw this exception from the start, to save time
            }
            Object valueToIterate = mapping.getRealAttributeValueFromObject(object, session);
            UnitOfWorkImpl uow = isObjectUnregistered ? (UnitOfWorkImpl)session : null;

            // First check that object in fact is unregistered.
            // toDo: ?? Why is this commented out? Why are we supporting the unregistered thing at all?
            // Does not seem to be any public API for this, nor every used internally?
            //if (isObjectUnregistered) {
            //	isObjectUnregistered = !uow.getCloneMapping().containsKey(object);
            //}
            if (mapping.isCollectionMapping() && (valueToIterate != null)) {
                // For bug 2766379 must use the correct version of vectorFor to
                // unwrap the result same time.
                valueToIterate = mapping.getContainerPolicy().vectorFor(valueToIterate, session);

                // toDo: If the value is empty, need to support correct inner/outer join filtering symantics.
                // For CR 2612601, try to partially replace the result with already
                // registered objects.
                if (isObjectUnregistered && (uow.getCloneMapping().get(object) == null)) {
                    Vector objectValues = (Vector)valueToIterate;
                    for (int i = 0; i < objectValues.size(); i++) {
                        Object original = objectValues.elementAt(i);
                        Object clone = uow.getIdentityMapAccessorInstance().getIdentityMapManager().getFromIdentityMap(original);
                        if (clone != null) {
                            objectValues.setElementAt(clone, i);
                        }
                    }
                }

                // For CR 2612601, conforming without registering, a query could be
                // bob.get("address").get("city").equal("Ottawa"); where the address
                // has been registered and modified in the UOW, but bob has not.  Thus
                // even though bob does not point to the modified address now, it will
                // as soon as it is registered, so should point to it here.
            } else if (isObjectUnregistered && (uow.getCloneMapping().get(object) == null)) {
                Object clone = uow.getIdentityMapAccessorInstance().getIdentityMapManager().getFromIdentityMap(valueToIterate);
                if (clone != null) {
                    valueToIterate = clone;
                }
            }
            return valueToIterate;
        } else if (mapping.isDirectToFieldMapping()) {
            return ((AbstractDirectMapping)mapping).valueFromObject(object, mapping.getField(), session);
        } else if (mapping.isAggregateMapping()) {
            Object aggregateValue = ((AggregateMapping)mapping).getAttributeValueFromObject(object);
            ;
            // Bug 3995468 - if this query key is to a mapping in an aggregate object, get the object from actual mapping rather than the aggregate mapping
            while (readMappingFromQueryKey && mapping.isAggregateObjectMapping() && !((AggregateObjectMapping)mapping).getReferenceClass().equals(queryKey.getDescriptor().getJavaClass())) {
                mapping = mapping.getReferenceDescriptor().getObjectBuilder().getMappingForField(((DirectQueryKey)queryKey).getField());
                aggregateValue = mapping.getRealAttributeValueFromObject(aggregateValue, session);
            }
            return aggregateValue;
        } else {
            throw QueryException.cannotConformExpression();
        }
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(getName());
        writer.write(tableAliasesDescription());
    }

    /**
     * INTERNAL:
     * Indicates that RelationExpression.normalize method shouldn't attempt
     * optimize normalization by not normalizing this.
     */
    public boolean isNormalizationRequired() {
        return shouldQueryToManyRelationship() ||
            
            // For bug 2718460, some QueryKeyExpressions have a query key but no mapping.
            // An example is the "back-ref" query key for batch reads.  Must not
            // attempt the optimization for these.
            getMapping() == null ||
            
            // For bug 5234283: WRONG =* SQL FOR LEFT JOIN ON DERBY AND DB2 PLATFORMS
            // Caused by QueryKeyExpression never been normilized.
            // The condition should be kept in sync with condtions in normalize method
            // that trigger adding to normalizer.getStatement().getOuterJoin...
            ((shouldUseOuterJoin() || isUsingOuterJoinForMultitableInheritance()) && !getSession().getPlatform().shouldPrintOuterJoinInWhereClause()) ||
            (shouldUseOuterJoin() && getSession().getPlatform().isInformixOuterJoin());
    }
}
