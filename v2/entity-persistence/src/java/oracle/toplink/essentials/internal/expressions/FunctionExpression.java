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
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * Used for expressions that have 0 to n children.
 * These include not, between and all functions.
 */
public class FunctionExpression extends Expression {
    protected Expression baseExpression;
    protected Vector children;
    protected ExpressionOperator operator;
    protected transient ExpressionOperator platformOperator;
    protected Class resultType;

    public FunctionExpression() {
        this.children = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(2);
        this.resultType = null;
    }

    public void addChild(Expression child) {
        getChildren().addElement(child);
    }

    /**
     * INTERNAL:
     * Find the alias for a given table
     */
    public DatabaseTable aliasForTable(DatabaseTable table) {
        return getBaseExpression().aliasForTable(table);
    }

    /**
     * INTERNAL:
     */
    public Expression create(Expression base, Object singleArgument, ExpressionOperator operator) {
        baseExpression = base;
        addChild(base);
        Expression arg = Expression.from(singleArgument, base);
        addChild(arg);
        setOperator(operator);
        return this;
    }
    
    /**
     * INTERNAL: 
     * added for Trim support.  TRIM([trim_character FROM] string_primary)
     */
    public Expression createWithBaseLast(Expression base, Object singleArgument, ExpressionOperator anOperator) {
        baseExpression = base;
        Expression arg = Expression.from(singleArgument, base);
        addChild(arg);
        addChild(base);
        setOperator(anOperator);
        return this;
    }

    /**
     * INTERNAL:
     */
    public Expression create(Expression base, Vector arguments, ExpressionOperator operator) {
        baseExpression = base;
        addChild(base);
        for (Enumeration e = arguments.elements(); e.hasMoreElements();) {
            Expression arg = Expression.from(e.nextElement(), base);
            addChild(arg);
        }
        setOperator(operator);
        return this;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Function";
    }

    /**
     * INTERNAL:
     * Check if the object conforms to the expression in memory.
     * This is used for in-memory querying.
     * If the expression in not able to determine if the object conform throw a not supported exception.
     */
    public boolean doesConform(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        // Must check for NOT and negate entire base expression.
        if (getOperator().getSelector() == ExpressionOperator.Not) {
            return !getBaseExpression().doesConform(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);
        }

        // Conform between or in function.
        if ((getOperator().getSelector() == ExpressionOperator.Between) || (getOperator().getSelector() == ExpressionOperator.NotBetween)
                ||(getOperator().getSelector() == ExpressionOperator.In) || (getOperator().getSelector() == ExpressionOperator.NotIn)) {
            // Extract the value from the left side.
            Object leftValue = getBaseExpression().valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);

            // Extract the value from the arguments, skip the first child which is the base.
            Vector rightValue = new Vector(getChildren().size());
            for (int index = 1; index < getChildren().size(); index++) {
                Object valueFromRight;
                if (getChildren().elementAt(index) instanceof Expression) {
                    valueFromRight = ((Expression)getChildren().elementAt(index)).valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);
                } else {      
                    valueFromRight = getChildren().elementAt(index);
                }
                //If valueFromRight is a Vector, then there is only one child other than the base, e.g. valueFromRight is a collection of constants.  
                //Then it should be the vector to be compared with.  Don't add it to another collection.
                if (valueFromRight instanceof Vector) {
                    rightValue = (Vector)valueFromRight;
                //Single values should be added to the rightValue, which will be compared with leftValue.
                } else {
                    rightValue.addElement(valueFromRight);
                }

            }

            // If left is anyof collection of values, check each one.
            // If the right had an anyof not supported will be thrown from the operator.
            if (leftValue instanceof Vector) {
                for (Enumeration leftEnum = (Enumeration)((Vector)leftValue).elements();
                         leftEnum.hasMoreElements();) {
                    Object tempLeft = leftEnum.nextElement();
                    if (getOperator().doesRelationConform(tempLeft, rightValue)) {
                        return true;
                    }
                }

                // Only return false if none of the values match.
                return false;
            } else {
                return getOperator().doesRelationConform(leftValue, rightValue);
            }
        } else if ((getOperator().getSelector() == ExpressionOperator.IsNull) || (getOperator().getSelector() == ExpressionOperator.NotNull)) {
            // Extract the value from the left side.
            Object leftValue = getBaseExpression().valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);

            // If left is anyof collection of values, check each one.
            if (leftValue instanceof Vector) {
                for (Enumeration leftEnum = (Enumeration)((Vector)leftValue).elements();
                         leftEnum.hasMoreElements();) {
                    Object tempLeft = leftEnum.nextElement();
                    if (getOperator().doesRelationConform(tempLeft, null)) {
                        return true;
                    }
                }

                // Only return false if none of the values match.
                return false;
            } else {
                return getOperator().doesRelationConform(leftValue, null);
            }
        }

        // No other relation functions are supported.
        // Non-relation functions are supported through valueFromObject().
        throw QueryException.cannotConformExpression();
    }

    public Expression getBaseExpression() {
        return baseExpression;
    }

    /**
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    public ExpressionBuilder getBuilder() {
        if (getBaseExpression() == null) {
            return null;
        }
        return getBaseExpression().getBuilder();
    }

    public Vector getChildren() {
        return children;
    }

    /**
     * INTERNAL: Not to be confused with the public getField(String)
     * This returns a collection of all fields associated with this object. Really
     * only applies to query keys representing an object or to expression builders.
     *
     */
    public Vector getFields() {
        return getBaseExpression().getFields();
    }

    public ExpressionOperator getOperator() {
        return operator;
    }

    public ExpressionOperator getPlatformOperator(DatabasePlatform platform) {
        if (platformOperator == null) {
            initializePlatformOperator(platform);
        }
        return platformOperator;
    }
    
    public Class getResultType() {
        return resultType;
    }

    public boolean hasResultType() {
        return resultType != null;
    }
    
    /**
     * INTERNAL:
     */
    public void initializePlatformOperator(DatabasePlatform platform) {
        if (getOperator().isComplete()) {
            platformOperator = getOperator();
            return;
        }
        platformOperator = platform.getOperator(getOperator().getSelector());
        if (platformOperator == null) {
            throw QueryException.invalidOperator(getOperator().toString());
        }
    }

    public boolean isFunctionExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * Return if the represents an object comparison.
     */
    protected boolean isObjectComparison() {
        if (getChildren().size() != 1) {
            return false;
        }

        int selector = getOperator().getSelector();
        if ((selector != ExpressionOperator.IsNull) && (selector != ExpressionOperator.NotNull)) {
            return false;
        }

        Expression base = getBaseExpression();
        return (base.isObjectExpression() && (!((ObjectExpression)base).isAttribute()));
    }

    /**
     * INTERNAL:
     * For iterating using an inner class
     */
    public void iterateOn(ExpressionIterator iterator) {
        super.iterateOn(iterator);
        for (Enumeration childrenEnum = getChildren().elements(); childrenEnum.hasMoreElements();) {
            Expression child = (Expression)childrenEnum.nextElement();
            child.iterateOn(iterator);
        }
    }

    /**
     * INTERNAL:
     * Normalize into a structure that is printable.
     * Also compute printing information such as outer joins.
     * This checks for object isNull and notNull comparisons.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        //This method has no validation but we should still make the method call for consistency
        //bug # 2956674
        //validation is moved into normalize to ensure that expressions are valid before we attempt to work with them
        validateNode();
        if (getChildren().isEmpty()) {
            return this;
        }

        if (!isObjectComparison()) {
            for (int index = 0; index < getChildren().size(); index++) {
                getChildren().setElementAt(((Expression)getChildren().elementAt(index)).normalize(normalizer), index);
            }
            return this;
        } else {
            //if not normalising we must still validate the corresponding node to make sure that they are valid
            //bug # 2956674
            for (int index = 0; index < getChildren().size(); index++) {
                ((Expression)getChildren().elementAt(index)).validateNode();
            }
        }

        // This code is executed only in the case of an is[not]Null on an
        // object attribute.
        ObjectExpression base = (ObjectExpression)getBaseExpression();

        // For cr2334, fix code so that normalize is first called on base expressions.
        // I.e. if base itself had a base expression this expression would not be normalized.
        base.getBaseExpression().normalize(normalizer);

        // Switch to null foreign key comparison (i.e. get('c').isNull() to getField('C_ID').isNull()).
        // For bug 3105559 also must handle aggregates: get("period").isNull();
        Expression foreignKeyJoin = base.getMapping().buildObjectJoinExpression(base, (Object)null, getSession());

        if (getOperator().getSelector() == ExpressionOperator.NotNull) {
            foreignKeyJoin = foreignKeyJoin.not();
        }
        return foreignKeyJoin;
    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);

        baseExpression = baseExpression.copiedVersionFrom(alreadyDone);
        Vector oldChildren = children;
        children = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        for (int i = 0; i < oldChildren.size(); i++) {
            addChild((Expression)(((Expression)oldChildren.elementAt(i)).copiedVersionFrom(alreadyDone)));
        }
    }

    /**
     * INTERNAL:
     * Print SQL
     */
    public void printSQL(ExpressionSQLPrinter printer) {
        ExpressionOperator realOperator;
        realOperator = getPlatformOperator(printer.getPlatform());
        //In or NotIn operator only comes with " IN (" and ")" since the number of elements are unknown.  Need to go through 
        //the collection to print out each element.
        if (realOperator.getSelector() == ExpressionOperator.In || realOperator.getSelector() == ExpressionOperator.NotIn) {
            printInCollection(getChildren(), printer, realOperator);
        } else {
            realOperator.printCollection(getChildren(), printer);
        }
    }

    /**
     * INTERNAL: Print the collection in "IN" onto the SQL stream.  Go through the collection to print out each element.
     */
    public void printInCollection(Vector items, ExpressionSQLPrinter printer, ExpressionOperator operator) {        
        ((Expression)items.elementAt(0)).printSQL(printer);
        printer.printString(operator.getDatabaseStrings()[0]);
        
        for (int i = 1; i < items.size() - 1; i++) {
            Expression item = (Expression)items.elementAt(i);
            item.printSQL(printer);
            printer.printString(", ");
        }
        
        ((Expression)items.elementAt(items.size() - 1)).printSQL(printer);
        printer.printString(operator.getDatabaseStrings()[1]);
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    public void printJava(ExpressionJavaPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        realOperator.printJavaCollection(getChildren(), printer);
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    public Expression rebuildOn(Expression newBase) {
        Expression newLocalBase = getBaseExpression().rebuildOn(newBase);
        Vector newChildren = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(getChildren().size());
        for (int i = 1; i < getChildren().size(); i++) {// Skip the first one, since it's also the base
            newChildren.addElement(((Expression)children.elementAt(i)).rebuildOn(newBase));
        }
        newLocalBase.setSelectIfOrderedBy(getBaseExpression().selectIfOrderedBy());
        FunctionExpression rebuilt = (FunctionExpression) newLocalBase.performOperator(getOperator(), newChildren);
        rebuilt.setResultType(this.getResultType()); //copy over result type.
        return rebuilt;
    }

    /**
     * Sets the base of the expression
     * Added to allow creation of functions which take the column name
     * as something other than the first option.
     * Creation date: (9/11/00 4:04:03 PM)
     * @param expression oracle.toplink.essentials.expressions.Expression
     */
    public void setBaseExpression(Expression expression) {
        baseExpression = expression;
    }

    // Set the local base expression, ie the one on the other side of the operator
    // Most types will ignore this, since they don't need it.
    public void setLocalBase(Expression exp) {
        getBaseExpression().setLocalBase(exp);
    }

    public void setOperator(ExpressionOperator theOperator) {
        operator = theOperator;
    }
    
    public void setResultType(Class resultType) {
        this.resultType = resultType;
    }

    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        if (getChildren().isEmpty()) {
            return (Expression)clone();
        }
        Vector newChildren = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(getChildren().size());

        // For functions the base is the first child, we only want the arguments so start at the second.
        for (int index = 1; index < getChildren().size(); index++) {
            newChildren.addElement(((Expression)children.elementAt(index)).twistedForBaseAndContext(newBase, context));
        }

        // Aply the function to the twisted old base.
        Expression oldBase = (Expression)getChildren().elementAt(0);
        return oldBase.twistedForBaseAndContext(newBase, context).performOperator(getOperator(), newChildren);
    }

    /**
     * INTERNAL:
     * Return the value for in memory comparison.
     * This is only valid for valueable expressions.
     */
    public Object valueFromObject(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        Object baseValue = getBaseExpression().valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered);
        Vector arguments = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance(getChildren().size());
        for (int index = 1; index < getChildren().size(); index++) {
            if (getChildren().elementAt(index) instanceof Expression) {
                arguments.addElement(((Expression)getChildren().elementAt(index)).valueFromObject(object, session, translationRow, valueHolderPolicy, isObjectUnregistered));
            } else {
                arguments.addElement(getChildren().elementAt(index));
            }
        }
        if (baseValue instanceof Vector) {// baseValue might be a vector, so the individual values must be extracted before applying the function call to them
            Vector baseVector = new Vector();
            for (Enumeration valuesToCompare = (Enumeration)((Vector)baseValue).elements();
                     valuesToCompare.hasMoreElements();) {
                Object baseObject = (Object)valuesToCompare.nextElement();
                if (baseObject == null) {
                    baseVector.addElement(baseObject);
                } else {
                    baseVector.addElement(getOperator().applyFunction(baseObject, arguments));
                }
            }
            return baseVector;
        } else {
            // Do not apply functions to null, just leave as null.
            if (baseValue == null) {
                return null;
            } else {
                return getOperator().applyFunction(baseValue, arguments);
            }
        }
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(operator.toString());
    }

    /**
     * INTERNAL: called from SQLSelectStatement.writeFieldsFromExpression(...)
     */
    public void writeFields(ExpressionSQLPrinter printer, Vector newFields, SQLSelectStatement statement) {
        //print ", " before each selected field except the first one
        if (printer.isFirstElementPrinted()) {
            printer.printString(", ");
        } else {
            printer.setIsFirstElementPrinted(true);
        }

        if (getBaseExpression().isDataExpression()) {
            DatabaseField field = ((DataExpression)getBaseExpression()).getField();
            if (field == null) {
                // This means the select wants a *.
                field = new DatabaseField("*");
                field.setType(getResultType());
            } else {
                // Set the result type to the type specified for this function
                // expression, if one has been set. Otherwise, use the mappings
                // database field type.
                if (hasResultType()) {
                    field = (DatabaseField) field.clone();
                    field.setType(getResultType());
                }
            }
            
            newFields.addElement(field);
        } else {
            // This field is a complex function value so any name can be used.
            DatabaseField field = new DatabaseField("*");
            field.setType(getResultType());
            newFields.addElement(field);
        }

        printSQL(printer);
    }

    /**
     * INTERNAL:
     * Used in SQL printing.
     */
    public void writeSubexpressionsTo(BufferedWriter writer, int indent) throws IOException {
        if (baseExpression != null) {
            baseExpression.toString(writer, indent);
        }
    }
}
