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
package oracle.toplink.essentials.expressions;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <P>
 * <B>Purpose</B>: Allow for instances of expression to be created. Expressions are Java object-level representations of SQL "where" clauses.
 * The expressions attempt to mirror Java code as closely as possible.</p>
 *
 * <P>
 *
 * <B>Example</B>:
 * <PRE><BLOCKQUOTE>
 *        ExpressionBuilder employee = new ExpressionBuilder();
 *        employee.get("firstName").equal("Bob").and(employee.get("lastName").equal("Smith"))
 *
 *        >> equivalent Java code: (employee.getFirstName().equals("Bob")) && (employee.getLastName().equals("Smith"))
 *
 *        >> equivalent SQL: (F_NAME = 'Bob') AND (L_NAME = 'Smith')
 * </BLOCKQUOTE></PRE>
 *
 * @see Expression
 */
public class ExpressionBuilder extends ObjectExpression {
    protected transient AbstractSession session;
    protected Class queryClass;
    protected SQLSelectStatement statement;
    protected DatabaseTable viewTable;
    protected DatabaseTable aliasedViewTable;
    
    protected boolean wasQueryClassSetInternally = true;
    
    protected boolean wasAdditionJoinCriteriaUsed = false;

    /**
     * PUBLIC:
     * Create a new ExpressionBuilder.
     */
    public ExpressionBuilder() {
        super();
    }

    /**
     * ADVANCED:
     * Create a new ExpressionBuilder representing instances of the argument class.
     * This can be used for the purpose of parallel expressions.
     * This is a type of query that searches on the relationship between to un-related objects.
     */
    public ExpressionBuilder(Class queryClass) {
        super();
        this.queryClass = queryClass;
        this.wasQueryClassSetInternally = false;
    }

    /**
     * INTERNAL: Find the alias for a given table. Handle the special case where we are bogus
     * and it should be aliased against our derived tables instead.
     */
    public DatabaseTable aliasForTable(DatabaseTable table) {
        if (hasViewTable()) {
            return getAliasedViewTable();
        }

        if (doesNotRepresentAnObjectInTheQuery()) {
            for (Enumeration e = derivedTables.elements(); e.hasMoreElements();) {
                TableExpression t = (TableExpression)e.nextElement();
                DatabaseTable result = t.aliasForTable(table);
                if (result != null) {
                    return result;
                }
            }
        } else {
            return super.aliasForTable(table);
        }
        return null;// No alias found in the derived tables
    }

    /**
     * INTERNAL:
     * Assign aliases to any tables which I own. Start with t<initialValue>,
     * and return the new value of  the counter , i.e. if initialValue is one
     * and I have tables ADDRESS and EMPLOYEE I will assign them t1 and t2 respectively, and return 3.
     */
    public int assignTableAliasesStartingAt(int initialValue) {
        if (hasBeenAliased()) {
            return initialValue;
        }

        if (doesNotRepresentAnObjectInTheQuery()) {
            return initialValue;
        }

        // This block should be removed I think.
        // The only reason to clone might be to
        // preserve the qualifier, but aliases need
        // qualifiers?  That seems strange.
        // Also this will break AsOf queries.  By
        // inference if has view table the AliasTableLookup
        // will contain one table, and that will be the
        // table of the view...
        if (hasViewTable()) {
            DatabaseTable aliased = (DatabaseTable)viewTable.clone();
            String alias = "t" + initialValue;
            aliased.setName(alias);
            assignAlias(alias, viewTable);
            aliasedViewTable = aliased;
            return initialValue + 1;
        }
        return super.assignTableAliasesStartingAt(initialValue);
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Base";
    }

    /**
     * INTERNAL:
     * There are cases (which we might want to eliminate?) where the expression builder
     * doesn't actually correspond to an object to be read. Mostly this is the case where
     * it's a data query in terms of tables, and the builder is only there to provide a base.
     * It might be better to make tables able to serve as their own base, but it's very nice
     * to have a known unique, shared base. In the meantime, this
     * is a special case to make sure the builder doesn't get tables assigned.
     */
    public boolean doesNotRepresentAnObjectInTheQuery() {
        return (hasDerivedTables() && !hasDerivedFields() && !hasDerivedExpressions());
    }

    /**
     * INTERNAL:
     */
    public DatabaseTable getAliasedViewTable() {
        return aliasedViewTable;

    }

    /**
     * INTERNAL:
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    public ExpressionBuilder getBuilder() {
        return this;
    }

    /**
     * INTERNAL:
     * Only usable after the session and class have been set. Return the
     * descriptor for the class this node represents.
     */
    public ClassDescriptor getDescriptor() {
        if (descriptor == null) {
            if (getQueryClass() == null) {
                return null;
            } else {
                if (getSession() == null) {
                    throw QueryException.noExpressionBuilderFound(this);
                }
                descriptor = getSession().getDescriptor(getQueryClass());
            }
        }
        return descriptor;

    }

    /**
     * INTERNAL:
     */
    public Class getQueryClass() {
        return queryClass;
    }

    /**
     * INTERNAL:
     */
    public AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL:
     * Return the statement that expression is for.
     * This is used for the context in subselects.
     */
    public SQLSelectStatement getStatement() {
        return statement;
    }

    /**
     * INTERNAL:
     */
    public DatabaseTable getViewTable() {
        return viewTable;
    }

    /**
     * INTERNAL:
     */
    public boolean hasViewTable() {
        return viewTable != null;
    }

    /**
     * INTERNAL:
     */
    public boolean isExpressionBuilder() {
        return true;
    }

    /**
     * INTERNAL:
     * Normalize the expression into a printable structure.
     * Any joins must be added to form a new root.
     */
    public Expression normalize(ExpressionNormalizer normalizer) {
        if (hasBeenNormalized()) {
            return this;
        } else {
            setHasBeenNormalized(true);
        }

        // This is required for parralel selects,
        // the session must be set and the addtional join expression added.
        if (this.queryClass != null) {
            Expression criteria = null;

            setSession(normalizer.getSession().getRootSession(null));
            // The descriptor must be defined at this point.
            if (getDescriptor() == null) {
                throw QueryException.noExpressionBuilderFound(this);
            }
            if (!this.wasAdditionJoinCriteriaUsed) {
                criteria = getDescriptor().getQueryManager().getAdditionalJoinExpression();
                if (criteria != null) {
                    criteria = twist(criteria, this);
                }
            }

            if (isUsingOuterJoinForMultitableInheritance() && getSession().getPlatform().shouldPrintOuterJoinInWhereClause()) {
                Expression childrenCriteria = getDescriptor().getInheritancePolicy().getChildrenJoinExpression();
                childrenCriteria = this.twist(childrenCriteria, this);
                childrenCriteria.convertToUseOuterJoin();
                if(criteria == null) {
                    criteria = childrenCriteria;
                } else {
                    criteria = criteria.and(childrenCriteria);
                }
            }
            if (isUsingOuterJoinForMultitableInheritance() && (!getSession().getPlatform().shouldPrintOuterJoinInWhereClause())) {
                normalizer.getStatement().getOuterJoinExpressions().addElement(null);
                normalizer.getStatement().getOuterJoinedMappingCriteria().addElement(null);
                normalizer.getStatement().getOuterJoinedAdditionalJoinCriteria().addElement(additionalExpressionCriteriaMap());
                normalizer.getStatement().getDescriptorsForMultitableInheritanceOnly().add(this.getDescriptor());
                // fall through to the main case
            }
            normalizer.addAdditionalExpression(criteria);


        }
        setStatement(normalizer.getStatement());
        

        return super.normalize(normalizer);
    }

    /**
     * INTERNAL:
     * Print java
     */
    public void printJava(ExpressionJavaPrinter printer) {
        printer.printString(printer.getBuilderString());        
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     * This assumes that the original expression has only a single builder.
     */
    public Expression rebuildOn(Expression newBase) {
        return newBase;
    }

    /**
     * INTERNAL:
     * Override Expression.registerIn to check if the new base expression
     * has already been provided for the clone.
     * @see oracle.toplink.essentials.expressions.Expression#cloneUsing(Expression)
     * @bug  2637484 INVALID QUERY KEY EXCEPTION THROWN USING BATCH READS AND PARALLEL EXPRESSIONS
     */
    protected Expression registerIn(Dictionary alreadyDone) {
        // Here do a special check to see if this a cloneUsing(newBase) call.
        Object value = alreadyDone.get(alreadyDone);
        if ((value == null) || (value == alreadyDone)) {
            // This is a normal cloning operation.
            return super.registerIn(alreadyDone);
        }
        ObjectExpression copy = (ObjectExpression)value;

        // copy is actually the newBase of a cloneUsing.
        alreadyDone.put(alreadyDone, alreadyDone);
        alreadyDone.put(this, copy);
        // Now need to copy over the derived expressions, etc.
        if (this.derivedExpressions != null) {
            if (copy.derivedExpressions == null) {
                copy.derivedExpressions = copyCollection(this.derivedExpressions, alreadyDone);
            } else {
                copy.derivedExpressions.addAll(copyCollection(this.derivedExpressions, alreadyDone));
            }
        }

        // Do the same for these protected fields.
        copy.postCopyIn(alreadyDone, this.derivedFields, this.derivedTables);
        return copy;
    }

    /**
     * INTERNAL:
     * Set the class which this node represents.
     */
    public void setQueryClass(Class queryClass) {
        this.queryClass = queryClass;
        this.descriptor = null;
    }

    /**
     * INTERNAL:
     * Set the session in which we expect this expression to be translated.
     */
    public void setSession(AbstractSession session) {
        this.session = session;
    }

    /**
     * INTERNAL:
     * Set the statement that expression is for.
     * This is used for the context in subselects.
     */
    public void setStatement(SQLSelectStatement statement) {
        this.statement = statement;
    }

    /**
     * INTERNAL:
     * This expression represents something read through a view table.
     */
    public void setViewTable(DatabaseTable theTable) {
        viewTable = theTable;

    }

    /**
     * INTERNAL:
     * If the additional Join Criteria for the class this builder represents has
     * been added to the statement then mark this as true.  This will prevent
     * TopLink from adding it again at normalization
     */
    public void setWasAdditionJoinCriteriaUsed(boolean joinCriteriaUsed){
        this.wasAdditionJoinCriteriaUsed = joinCriteriaUsed;
    }
    
    /**
     * INTERNAL:
     * Rebuild myself against the base, with the values of parameters supplied by the context
     * expression. This is used for transforming a standalone expression (e.g. the join criteria of a mapping)
     * into part of some larger expression. You normally would not call this directly, instead calling twist
     * See the comment there for more details"
     * @param newBase
     * @param context
     * @return
     */
    public Expression twistedForBaseAndContext(Expression newBase, Expression context) {
        return newBase;
    }

    /**
     * INTERNAL:
     * The expression builder represent the entire object, just return it.
     */
    public Object valueFromObject(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean isObjectUnregistered) {
        return object;
    }

    /**
     * INTERNAL:
     * If the additional Join Criteria for the class this builder represents has
     * been added to the statement this method will return true;
     */
    public boolean wasAdditionJoinCriteriaUsed(){
        return this.wasAdditionJoinCriteriaUsed;
    }
    
    /**
     * INTERNAL:
     * Returns true if TopLink set the query class as appoased to the customer.  This
     * is important in determining if this Expression should be treated as a parallel
     * expression during normalization
     */
    public boolean wasQueryClassSetInternally(){
        return this.wasQueryClassSetInternally;
    }
    
    /**
     * INTERNAL:
     * For debug printing purposes.
     */
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        String className;
        if (getQueryClass() == null) {
            className = "QUERY OBJECT";
        } else {
            className = getQueryClass().getName();
        }
        writer.write(className + tableAliasesDescription());
    }
}
