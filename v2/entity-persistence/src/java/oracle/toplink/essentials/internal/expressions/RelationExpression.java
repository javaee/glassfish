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

import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <p><b>Purpose</b>:Used for all relation operators except for between.
 */
public class RelationExpression extends CompoundExpression {

    /**
     * RelationExpression constructor comment.
     */
    public RelationExpression() {
        super();
    }

    /**
     * Test that both of our children are field nodes
     */
    protected boolean allChildrenAreFields() {
        return (getFirstChild().getFields().size() == 1) && (getSecondChild().getFields().size() == 1);

    }

    /**
     * INTERNAL:
     * Modify this individual expression node to use outer joins wherever there are
     * equality operations between two field nodes.
     */
    protected void convertNodeToUseOuterJoin() {
        if ((getOperator().getSelector() == ExpressionOperator.Equal) && allChildrenAreFields()) {
            setOperator(getOperator(ExpressionOperator.EqualOuterJoin));
        }
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Relation";
    }

    /**
     * INTERNAL:
     * Check if the object conforms to the expression in memory.
     * This is used for in-memory querying.
     * If the expression in not able to determine if the object conform throw a not supported exception.
     */
    public boolean doesConform(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        // Extract the value from the right side.
        //CR 3677 integration of valueHolderPolicy
        Object rightValue = getSecondChild().valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);

        // Extract the value from the object.
        //CR 3677 integration of valueHolderPolicy
        Object leftValue = getFirstChild().valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);

        // The right value may be a vector of values from an anyof, or an in.
        if (rightValue instanceof Vector) {
            //right vector means an anyof on right, so must check each value.
            for (Enumeration rightEnum = (Enumeration)((Vector)rightValue).elements();
                     rightEnum.hasMoreElements();) {
                Object tempRight = rightEnum.nextElement();

                // Left may also be an anyof some must check each left with each right.
                if (leftValue instanceof Vector) {
                    // If anyof the left match return true, otherwise keep checking.
                    if (doesAnyOfLeftValuesConform((Vector)leftValue, tempRight, session)) {
                        return true;
                    }
                }
                if (doValuesConform(leftValue, tempRight, session)) {
                    return true;
                }
            }

            // None of the value conform.
            return false;
        }

        // Otherwise the left may also be a vector of values from an anyof.
        if (leftValue instanceof Vector) {
            return doesAnyOfLeftValuesConform((Vector)leftValue, rightValue, session);
        }

        // Otherwise it is a simple value to value comparison, or simple object to object comparison.
        return doValuesConform(leftValue, rightValue, session);
    }

    /**
     * Conform in-memory the collection of left values with the right value for this expression.
     * This is used for anyOf support when the left side is a collection of values.
     */
    protected boolean doesAnyOfLeftValuesConform(Vector leftValues, Object rightValue, AbstractSession session) {
        // Check each left value with the right value.
        for (int index = 0; index < leftValues.size(); index++) {
            Object leftValue = leftValues.get(index);
            if (doValuesConform(leftValue, rightValue, session)) {
                // Return true if any value matches.
                return true;
            }
        }

        // Return false only if none of the values match.
        return false;
    }

    /**
     * Conform in-memory the two values.
     */
    protected boolean doValuesConform(Object leftValue, Object rightValue, AbstractSession session) {
        // Check for object comparison.
        if (isObjectComparison()) {
            return doesObjectConform(leftValue, rightValue, session);
        } else {
            return getOperator().doesRelationConform(leftValue, rightValue);
        }
    }

    /**
     * INTERNAL:
     * Check if the object conforms to the expression in memory.
     * This is used for in-memory querying across object relationships.
     */
    public boolean doesObjectConform(Object leftValue, Object rightValue, AbstractSession session) {
        if ((leftValue == null) && (rightValue == null)) {
            return performSelector(true);
        }
        if ((leftValue == null) || (rightValue == null)) {
            //both are not null.
            return performSelector(false);
        }

        Class javaClass = leftValue.getClass();
        Vector leftPrimaryKey;
        Vector rightPrimaryKey;
        ClassDescriptor descriptor;
        oracle.toplink.essentials.internal.identitymaps.CacheKey rightCacheKey;
        oracle.toplink.essentials.internal.identitymaps.CacheKey leftCacheKey;

        if (javaClass != rightValue.getClass()) {
            return performSelector(false);
        }

        descriptor = session.getDescriptor(javaClass);
        // Currently cannot conform aggregate comparisons in-memory.
        if (descriptor.isAggregateDescriptor()) {
            throw QueryException.cannotConformExpression();
        }
        leftPrimaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(leftValue, session);
        rightPrimaryKey = descriptor.getObjectBuilder().extractPrimaryKeyFromObject(rightValue, session);

        rightCacheKey = new oracle.toplink.essentials.internal.identitymaps.CacheKey(rightPrimaryKey);
        leftCacheKey = new oracle.toplink.essentials.internal.identitymaps.CacheKey(leftPrimaryKey);

        return performSelector(rightCacheKey.equals(leftCacheKey));

    }

    /**
     * INTERNAL:
     * Extract the primary key from the expression into the row.
     * Ensure that the query is quering the exact primary key.
     * @param requireExactMatch refers to the primary key extracted gaurenteeing the result,
     * if not exact it is a hueristic and the cache hit will be conformed to the expression after the lookup
     * Return false if not on the primary key.
     */
    public boolean extractPrimaryKeyValues(boolean requireExactMatch, ClassDescriptor descriptor, AbstractRecord primaryKeyRow, AbstractRecord translationRow) {
        // If an exact match is required then the operator must be equality.
        if (requireExactMatch && (!(getOperator().getSelector() == ExpressionOperator.Equal))) {
            return false;
        }

        // If not an exact match only =, <, <=, >=, >,... are allowed but not IN which has a different type
        if ((!requireExactMatch) && (getOperator().getSelector() == ExpressionOperator.In)) {
            return false;
        }

        DatabaseField field = null;

        Object value = null;
        if (getSecondChild().isConstantExpression()) {
            value = ((ConstantExpression)getSecondChild()).getValue();
        } else if (getSecondChild().isParameterExpression()) {
            value = translationRow.get(((ParameterExpression)getSecondChild()).getField());
        } else if (getFirstChild().isConstantExpression()) {
            value = ((ConstantExpression)getFirstChild()).getValue();
        } else if (getFirstChild().isParameterExpression()) {
            value = translationRow.get(((ParameterExpression)getFirstChild()).getField());
        }
        if (value == null) {
            return false;
        }

        // Ensure that the primary key is being queried on.
        if (getFirstChild().isFieldExpression()) {
            // Only get field for the source object.
            if (getFirstChild().getBuilder() != ((FieldExpression)getFirstChild()).getBaseExpression()) {
                return false;
            }
            field = ((FieldExpression)getFirstChild()).getField();
        } else if (getFirstChild().isQueryKeyExpression()) {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(((QueryKeyExpression)getFirstChild()).getName());
            if (getFirstChild().getBuilder() != ((QueryKeyExpression) getFirstChild()).getBaseExpression()){
                return false;
             }

            // Only support referencing limited number of relationship types.
            if (mapping != null && (!mapping.isPrimaryKeyMapping()) ){
                return false;
            }

            if (mapping != null && (mapping.isObjectReferenceMapping() || mapping.isAggregateObjectMapping())){
                mapping.writeFromAttributeIntoRow(value, primaryKeyRow, getSession());
                return true;
            }

            if (mapping != null && !mapping.isDirectToFieldMapping()){
                return false;
             }
            
            // Only get field for the source object.
            field = descriptor.getObjectBuilder().getFieldForQueryKeyName(getFirstChild().getName());
        } else if (getSecondChild().isFieldExpression()) {// For parameterized queries it may also be on the right side.
            // Only get field for the source object.
            if (getFirstChild().getBuilder() != ((FieldExpression)getSecondChild()).getBaseExpression()) {
                return false;
            }
            field = ((FieldExpression)getSecondChild()).getField();
        } else if (getSecondChild().isQueryKeyExpression()) {
            DatabaseMapping mapping = descriptor.getMappingForAttributeName(((QueryKeyExpression)getSecondChild()).getName());
            if (getSecondChild().getBuilder() != ((QueryKeyExpression) getSecondChild()).getBaseExpression() ){
                return false;
             }
            
            // Only support referencing limited number of relationship types.
            if (mapping != null && (!mapping.isPrimaryKeyMapping()) ){
                return false;
            }

            if (mapping != null && (mapping.isObjectReferenceMapping() || mapping.isAggregateObjectMapping())){
                mapping.writeFromAttributeIntoRow(value, primaryKeyRow, getSession());
                return true;
            }

            if (mapping != null && !mapping.isDirectToFieldMapping()){
                return false;
             }
            
            field = descriptor.getObjectBuilder().getFieldForQueryKeyName(getSecondChild().getName());
        } else {
            return false;
        }
        if ((field == null) || (!descriptor.getPrimaryKeyFields().contains(field))) {
            return false;
        }

        primaryKeyRow.put(field, value);

        return true;
    }

    /**
     * Check if the expression is an equal null expression, these must be handle in a special way in SQL.
     */
    public boolean isEqualNull(ExpressionSQLPrinter printer) {
        if (isObjectComparison()) {
            return false;
        }

        if (getOperator().getSelector() != ExpressionOperator.Equal) {
            return false;
        }

        if (getSecondChild().isConstantExpression() && (((ConstantExpression)getSecondChild()).getValue() == null)) {
            return true;
        }

        if (getSecondChild().isParameterExpression() && (printer.getTranslationRow() != null) && (((ParameterExpression)getSecondChild()).getValue(printer.getTranslationRow(), printer.getSession()) == null)) {
            return true;
        }

        return false;
    }

    /**
     * Check if the expression is an equal null expression, these must be handle in a special way in SQL.
     */
    public boolean isNotEqualNull(ExpressionSQLPrinter printer) {
        if (isObjectComparison()) {
            return false;
        }

        if (getOperator().getSelector() != ExpressionOperator.NotEqual) {
            return false;
        }

        if (getSecondChild().isConstantExpression() && (((ConstantExpression)getSecondChild()).getValue() == null)) {
            return true;
        }

        if (getSecondChild().isParameterExpression() && (printer.getTranslationRow() != null) && (((ParameterExpression)getSecondChild()).getValue(printer.getTranslationRow(), printer.getSession()) == null)) {
            return true;
        }

        return false;
    }

    /**
     * INTERNAL:
     * Return if the represents an object comparison.
     */
    protected boolean isObjectComparison() {
        if ((!getFirstChild().isObjectExpression()) || ((ObjectExpression)getFirstChild()).isAttribute()) {
            return false;
        }

        DatabaseMapping mapping = ((ObjectExpression)getFirstChild()).getMapping();
        if ((mapping != null) && (mapping.isDirectCollectionMapping())) {
            return false;
        }

        return getSecondChild().isObjectExpression() || (getSecondChild().isValueExpression() || (getSecondChild().isFunctionExpression() && ((FunctionExpression)getSecondChild()).getOperator().isAnyOrAll()));
    }

    /**
     * INTERNAL:
     */
    public boolean isRelationExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * Check for object comparison as this requires for the expression to be replaced by the object comparison.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        if (!isObjectComparison()) {
            return super.normalize(normalizer);
        } else {
            //bug # 2956674
            //validation is moved into normalize to ensure that expressions are valid before we attempt to work with them
            // super.normalize will call validateNode as well.
            validateNode();
        }
        if ((getOperator().getSelector() != ExpressionOperator.Equal) && (getOperator().getSelector() != ExpressionOperator.NotEqual)) {
            throw QueryException.invalidOperatorForObjectComparison(this);
        }

        if(getSecondChild().isFunctionExpression()) {
            FunctionExpression funcExp = (FunctionExpression)getSecondChild();
            if(funcExp.getOperator().isAnyOrAll()) {
                SubSelectExpression subSelectExp = (SubSelectExpression)funcExp.getChildren().elementAt(1);
                ReportQuery subQuery = subSelectExp.getSubQuery();
                
                // some db (derby) require that in EXIST(SELECT...) subquery returns a single column
                subQuery.getItems().clear();
                subQuery.addItem("one", new ConstantExpression(new Integer(1), subQuery.getExpressionBuilder()));
                
                Expression subSelectCriteria = subQuery.getSelectionCriteria();
                ExpressionBuilder subBuilder = subQuery.getExpressionBuilder();

                ExpressionBuilder builder = getFirstChild().getBuilder();

                Expression newExp;
                if(funcExp.getOperator().isAny()) {
                    // Any or Some
                    if(getOperator().getSelector() == ExpressionOperator.Equal) {
                        subSelectCriteria = subBuilder.equal(getFirstChild()).and(subSelectCriteria);
                    } else {
                        subSelectCriteria = subBuilder.notEqual(getFirstChild()).and(subSelectCriteria);
                    }
                    subQuery.setSelectionCriteria(subSelectCriteria);
                    newExp = builder.exists(subQuery);
                } else {
                    // All
                    if(getOperator().getSelector() == ExpressionOperator.Equal) {
                        subSelectCriteria = subBuilder.notEqual(getFirstChild()).and(subSelectCriteria);
                    } else {
                        subSelectCriteria = subBuilder.equal(getFirstChild()).and(subSelectCriteria);
                    }
                    subQuery.setSelectionCriteria(subSelectCriteria);
                    newExp = builder.notExists(subQuery);
                }
                return newExp.normalize(normalizer);
            }
        }
        
        // This can either be comparison to another object, null or another expression reference.
        // Object comparisons can be done on other object builders, 1:1 or 1:m m:m mappings,
        // 1:m/m:m must twist the primary key expression,
        // 1:1 must not join into the target but become the foreign key expression.
        // The value may be a constant or another expression.
        Expression foreignKeyJoin = null;
        ObjectExpression first = (ObjectExpression)getFirstChild();

        // OPTIMIZATION 1: IDENTITY for CR#2456 / bug 2778339
        // Most exists subqueries have something like projBuilder.equal(empBuilder.anyOf("projects"))
        // to correlate the subquery to the enclosing query.
        // TopLink can produce SQL with one less join by not mapping projBuilder and 
        // anyOf("projects") to separate tables and equating them, but by treating
        // them as one and the same thing: as identical.
        // This trick can be pulled off by giving both the same TableAliasLookup,
        // but needs to be done very conservatively.
        // the equal() will be replaced directly with the mappingCriteria() of the anyOf("projects")
        // Example.  emp.equal(emp.get("manager")) will now produce this SQL:
        // SELECT ... FROM EMPLOYEE t0 WHERE (t0.EMP_ID = t0.MANAGER_ID) not:
        // SELECT ... FROM EMPLOYEE t0, EMPLOYEE t1 WHERE ((t0.EMP_ID = t1.EMP_ID) 
        //                                        AND (t0.MANAGER_ID = t1.EMP_ID))
        if (first.isExpressionBuilder() &&// If setting two query keys to equal the user probably intends a proper join.
                getSecondChild().isQueryKeyExpression() &&//.equal(anyOf() or get())
                !((QueryKeyExpression)getSecondChild()).hasDerivedExpressions()) {//The right side is not used for anything else.
            first = (ExpressionBuilder)first.normalize(normalizer);

            // If FK joins go in the WHERE clause, want to get hold of it and
            // not put it in normalizer.additionalExpressions.
            Vector foreignKeyJoinPointer = new Vector(1);
            QueryKeyExpression second = (QueryKeyExpression)getSecondChild();

            // If inside an OR the foreign key join must be on both sides.
            if (second.hasBeenNormalized()) {
                second.setHasBeenNormalized(false);
            }
            second = (QueryKeyExpression)second.normalize(normalizer, foreignKeyJoinPointer);
            if (!foreignKeyJoinPointer.isEmpty()) {
                foreignKeyJoin = (Expression)foreignKeyJoinPointer.firstElement();
                // Will make left and right identical in the SQL.
                if (first.getTableAliases() == null) {
                    TableAliasLookup tableAliases = new TableAliasLookup();
                    first.setTableAliases(tableAliases);
                    second.setTableAliases(tableAliases);
                } else {
                    second.setTableAliases(first.getTableAliases());
                }
            }
        }
        // OPTIMIZATION 2: for 1-1 mappings and get(...).equal(null)
        // Imagine you had addr1 = emp.get("address"); then addr1.equal(addr2);
        // One could go (addr1.ADDRESS_ID = addr2.ADDRESS_ID) and (emp.ADDR_ID = addr1.ADDRESS_ID) (foreign key join).
        // The optimization is to drop addr1 and instead have: (emp.ADDR_ID = addr2.ADDRESS_ID).
        // Since emp can have only 1 address (OneToOne) the addr1.equal(addr2) is
        // implicit.  This way if addr1 is used only for the comparison it can
        // be optimized out.
        // Also if addr2 were NULL there must be no join, just (emp.ADDR_ID = NULL)
        // For bug 3105559 handle AggregateObject case (emp.get("period").equal(period2)
        // which always falls into this case.
        else if (!first.isExpressionBuilder() && !((QueryKeyExpression)first).isNormalizationRequired()) {
            // Normalize firstChild's base only, as firstChild will be optimized out.
            if (first.getBaseExpression() != null) {
                first.setBaseExpression(first.getBaseExpression().normalize(normalizer));
            }

            if (getSecondChild().isConstantExpression()) {
                Object targetObject = ((ConstantExpression)getSecondChild()).getValue();
                foreignKeyJoin = first.getMapping().buildObjectJoinExpression(first, targetObject, getSession());
            } else if (getSecondChild().isObjectExpression() || getSecondChild().isParameterExpression()) {
                foreignKeyJoin = first.getMapping().buildObjectJoinExpression(first, getSecondChild(), getSession());
            } else {
                throw QueryException.invalidUseOfToManyQueryKeyInExpression(this);
            }
        }

        // DEFAULT:  Left and right are separate entities, and the
        // equal() will be replaced with a comparison by primary key.
        if (foreignKeyJoin == null) {
            first = (ObjectExpression)first.normalize(normalizer);

            // A ConstantExpression stores a selection object.  Compare the primary
            // keys of the first expression and the selection object.
            if (getSecondChild().isConstantExpression()) {
                Expression keyExpression = first.getDescriptor().getObjectBuilder().buildPrimaryKeyExpressionFromObject(((ConstantExpression)getSecondChild()).getValue(), getSession());
                foreignKeyJoin = first.twist(keyExpression, first);

                // Each expression will represent a separate table, so compare the primary
                // keys of the first and second expressions.
            } else if (getSecondChild().isObjectExpression() || getSecondChild().isParameterExpression()) {
                foreignKeyJoin = first.twist(first.getDescriptor().getObjectBuilder().getPrimaryKeyExpression(), getSecondChild());
            } else {
                throw QueryException.invalidUseOfToManyQueryKeyInExpression(this);
            }
        }
        if (getOperator().getSelector() == ExpressionOperator.NotEqual) {
            foreignKeyJoin = foreignKeyJoin.not();
        }

        return foreignKeyJoin.normalize(normalizer);
    }

    /**
     * INTERNAL:
     * Check if the object conforms to the expression in memory.
     * This is used for in-memory querying across object relationships.
     */
    public boolean performSelector(boolean areValuesEqual) {
        if (getOperator().getSelector() == getOperator().Equal) {
            return areValuesEqual;
        }
        if (getOperator().getSelector() == getOperator().NotEqual) {
            return !areValuesEqual;
        }

        throw QueryException.cannotConformExpression();

    }

    /**
     * INTERNAL:
     * Print SQL
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        if (isEqualNull(printer)) {
            getFirstChild().isNull().printSQL(printer);
        } else if (isNotEqualNull(printer)) {
            getFirstChild().notNull().printSQL(printer);
        } else {
            super.printSQL(printer);
        }
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    public void printJava(ExpressionJavaPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        Expression tempFirstChild = getFirstChild();
        Expression tempSecondChild = getSecondChild();
        realOperator.printJavaDuo(tempFirstChild, tempSecondChild, printer);
    }

    /**
     * INTERNAL:
     * Check if it is a FunctionExpression and its Operator is ToUpperCase.
     */
/*    public boolean isUpperCaseFunctionExpression(Expression expression, ExpressionJavaPrinter printer) {
        return expression.isFunctionExpression() &&
                ((FunctionExpression)expression).getPlatformOperator(printer.getPlatform()).getSelector() == ExpressionOperator.ToUpperCase;
    }*/

    /**
     * INTERNAL:
     * Check if it is a ConstantExpression and its value(String) is all upper case.
     */
/*    public boolean isUpperCaseConstantExpression(Expression expression) {
        return expression.isConstantExpression() && ((ConstantExpression)expression).getValue() instanceof String 
                && Helper.isUpperCaseString((String)((ConstantExpression)expression).getValue());
    }*/

    /**
     * INTERNAL:
     * Print SQL without adding parentheses (for DB2 outer joins).
     */
    public void printSQLNoParens(ExpressionSQLPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        realOperator.printDuo(getFirstChild(), getSecondChild(), printer);
    }

    /**
     * Do any required validation for this node. Throw an exception if it's incorrect.
     */
    public void validateNode() {
        if (getFirstChild().isTableExpression()) {
            throw QueryException.cannotCompareTablesInExpression(((TableExpression)getFirstChild()).getTable());
        }
        if (getSecondChild().isTableExpression()) {
            throw QueryException.cannotCompareTablesInExpression(((TableExpression)getSecondChild()).getTable());
        }
    }
}
