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
import oracle.toplink.essentials.querykeys.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * Superclass for any object type expressions.
 */
public abstract class ObjectExpression extends DataExpression {
    transient public ClassDescriptor descriptor;
    public Vector derivedExpressions;

    /** indicates whether subclasses should be joined */
    protected boolean shouldUseOuterJoinForMultitableInheritance;

    /** Is this query key to be resolved using an outer join or not. Does not apply to attributes. */
    protected boolean shouldUseOuterJoin;

    public ObjectExpression() {
        this.shouldUseOuterJoin = false;
    }

    public void addDerivedExpression(Expression addThis) {
        if (derivedExpressions == null) {
            derivedExpressions = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        }
        derivedExpressions.addElement(addThis);
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

        HashMap tablesJoinExpressions = null;
        if(isUsingOuterJoinForMultitableInheritance()) {
            Vector tables = getDescriptor().getTables();
            tablesJoinExpressions = new HashMap();
            List childrenTables = getDescriptor().getInheritancePolicy().getChildrenTables();
            for( int i=0; i < childrenTables.size(); i++) {
                DatabaseTable table = (DatabaseTable)childrenTables.get(i);
                Expression joinExpression = (Expression)getDescriptor().getInheritancePolicy().getChildrenTablesJoinExpressions().get(table);
                if (getBaseExpression() != null){
                    joinExpression = getBaseExpression().twist(joinExpression, this);
                } else {
                    joinExpression = twist(joinExpression, this);
                }
                tablesJoinExpressions.put(table, joinExpression);
            }
        }
        
        return tablesJoinExpressions;
    }

    /**
     * PUBLIC:
     * Return an expression representing traversal of a 1:many or many:many relationship.
     * This allows you to query whether any of the "many" side of the relationship satisfies the remaining criteria.
     * <p>Example:
     * <pre><blockquote>
     *     TopLink: employee.anyOf("managedEmployees").get("firstName").equal("Bob")
     *     Java: no direct equivalent
     *     SQL: SELECT DISTINCT ... WHERE (t2.MGR_ID = t1.ID) AND (t2.F_NAME = 'Bob')
     * </pre></blockquote>
     */
    public Expression anyOf(String attributeName) {
        QueryKeyExpression queryKey = (QueryKeyExpression)newDerivedExpressionNamed(attributeName);

        queryKey.doQueryToManyRelationship();
        return queryKey;

    }
    
    /**
     * ADVANCED:
     * Return an expression representing traversal of a 1:many or many:many relationship.
     * This allows you to query whether any of the "many" side of the relationship satisfies the remaining criteria.
     * <p>Example:
     * <pre><blockquote>
     *     TopLink: employee.anyOf("managedEmployees").get("firstName").equal("Bob")
     *     Java: no direct equivalent
     *     SQL: SELECT DISTINCT ... WHERE (t2.MGR_ID (+) = t1.ID) AND (t2.F_NAME = 'Bob')
     * </pre></blockquote>
     */
    public Expression anyOfAllowingNone(String attributeName) {
        QueryKeyExpression queryKey = (QueryKeyExpression)newDerivedExpressionNamed(attributeName);
        queryKey.doUseOuterJoin();
        queryKey.doQueryToManyRelationship();
        return queryKey;

    }

    public QueryKeyExpression derivedExpressionNamed(String attributeName) {
        QueryKeyExpression existing = existingDerivedExpressionNamed(attributeName);
        if (existing != null) {
            return existing;
        }
        return newDerivedExpressionNamed(attributeName);

    }

    public Expression derivedManualExpressionNamed(String attributeName, ClassDescriptor aDescriptor) {
        Expression existing = existingDerivedExpressionNamed(attributeName);
        if (existing != null) {
            return existing;
        }
        return newManualDerivedExpressionNamed(attributeName, aDescriptor);

    }

    protected void doNotUseOuterJoin() {
        shouldUseOuterJoin = false;
    }

    protected void doUseOuterJoin() {
        shouldUseOuterJoin = true;
    }

    public QueryKeyExpression existingDerivedExpressionNamed(String attributeName) {
        if (derivedExpressions == null) {
            return null;
        }
        for (Enumeration e = derivedExpressions.elements(); e.hasMoreElements();) {
            QueryKeyExpression exp = (QueryKeyExpression)e.nextElement();
            if (exp.getName().equals(attributeName)) {
                return exp;
            }
        }
        return null;

    }

    public Expression get(String attributeName, Vector arguments) {
        Expression operatorExpression = super.get(attributeName, arguments);
        if (operatorExpression != null) {
            return operatorExpression;
        }

        QueryKeyExpression result = derivedExpressionNamed(attributeName);
        result.doNotUseOuterJoin();
        return result;

    }

    public Expression getAllowingNull(String attributeName, Vector arguments) {
        ObjectExpression exp = (ObjectExpression)existingDerivedExpressionNamed(attributeName);

        // The same (aliased) table cannot participate in a normal join and an outer join.
        // To help enforce this, if the node already exists 
        if (exp != null) {
            return exp;
        }
        exp = (ObjectExpression)derivedExpressionNamed(attributeName);
        exp.doUseOuterJoin();
        return exp;

    }

    public ClassDescriptor getDescriptor() {
        if (isAttribute()) {
            return null;
        }
        if (descriptor == null) {
            // Look first for query keys, then mappings. Ultimately we should have query keys
            // for everything and can dispense with the mapping part.
            ForeignReferenceQueryKey queryKey = (ForeignReferenceQueryKey)getQueryKeyOrNull();
            if (queryKey != null) {
                descriptor = getSession().getDescriptor(queryKey.getReferenceClass());
                return descriptor;
            }
            if (getMapping() == null) {
                throw QueryException.invalidQueryKeyInExpression(this);
            }

            // We assume this is either a foreign reference or an aggregate mapping
            descriptor = getMapping().getReferenceDescriptor();
            if (getMapping().isVariableOneToOneMapping()) {
                throw QueryException.cannotQueryAcrossAVariableOneToOneMapping(getMapping(), descriptor);
            }
        }
        return descriptor;

    }

    /**
     * INTERNAL: Not to be confused with the public getField(String)
     * This returns a collection of all fields associated with this object. Really
     * only applies to query keys representing an object or to expression builders.
     */
    public Vector getFields() {
        if (getDescriptor() == null) {
            return new Vector(1);
        }
        if ((descriptor.hasInheritance() && ! descriptor.getInheritancePolicy().hasMultipleTableChild()) || (shouldUseOuterJoinForMultitableInheritance())){
            // return all fields because we can.
            return descriptor.getAllFields();
        }else{
            return descriptor.getFields();
        }
    }

    /**
     * INTERNAL:
     * Returns the first field from each of the owned tables, used for
     * fine-grained pessimistic locking.
     */
    protected Vector getForUpdateOfFields() {
        Vector allFields = getFields();
        int expected = getTableAliases().size();
        Vector firstFields = new Vector(expected);
        DatabaseTable lastTable = null;
        DatabaseField field = null;
        int i = 0;

        // The following loop takes O(n*m) time.  n=# of fields. m=#tables.
        // However, in the m=1 case this will take one pass only.
        // Also assuming that fields are generally sorted by table, this will
        // take O(n) time.
        // An even faster way may be to go getDescriptor().getAdditionalPrimaryKeyFields.
        while ((i < allFields.size()) && (firstFields.size() < expected)) {
            field = (DatabaseField)allFields.elementAt(i++);
            if ((lastTable == null) || !field.getTable().equals(lastTable)) {
                lastTable = field.getTable();
                int j = 0;
                while (j < firstFields.size()) {
                    if (lastTable.equals(((DatabaseField)firstFields.elementAt(j)).getTable())) {
                        break;
                    }
                    j++;
                }
                if (j == firstFields.size()) {
                    firstFields.addElement(field);
                }
            }
        }
        return firstFields;
    }

    public Expression getManualQueryKey(String attributeName, ClassDescriptor aDescriptor) {
        return derivedManualExpressionNamed(attributeName, aDescriptor);
    }

    /**
     * INTERNAL:
     */
    public Vector getOwnedTables() {
        if(isUsingOuterJoinForMultitableInheritance()) {
            return getDescriptor().getInheritancePolicy().getAllTables();
        } else {
            return super.getOwnedTables();
        }
    }

    protected boolean hasDerivedExpressions() {
        return derivedExpressions != null;
    }

    public boolean isObjectExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * indicates whether additional expressions for multitable inheritance should be used and are available
     */
    public boolean isUsingOuterJoinForMultitableInheritance() {
        return shouldUseOuterJoinForMultitableInheritance() && 
                getDescriptor() != null && getDescriptor().hasInheritance() &&
                getDescriptor().getInheritancePolicy().hasMultipleTableChild() &&
                getDescriptor().getInheritancePolicy().shouldReadSubclasses();
    }
    
    public QueryKeyExpression newDerivedExpressionNamed(String attributeName) {
        QueryKeyExpression result = new QueryKeyExpression(attributeName, this);
        addDerivedExpression(result);
        return result;

    }

    public Expression newManualDerivedExpressionNamed(String attributeName, ClassDescriptor aDescriptor) {
        QueryKeyExpression result = new ManualQueryKeyExpression(attributeName, this, aDescriptor);
        addDerivedExpression(result);
        return result;

    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    protected void postCopyIn(Dictionary alreadyDone) {
        super.postCopyIn(alreadyDone);
        derivedExpressions = copyCollection(derivedExpressions, alreadyDone);
    }

    /**
     * INTERNAL:
     * The method was added to circumvent derivedFields and derivedTables being
     * protected.
     * @see oracle.toplink.essentials.expressions.ExpressionBuilder#registerIn(Dictionary alreadyDone)
     * @bug  2637484 INVALID QUERY KEY EXCEPTION THROWN USING BATCH READS AND PARALLEL EXPRESSIONS
     */
    public void postCopyIn(Dictionary alreadyDone, Vector oldDerivedFields, Vector oldDerivedTables) {
        if (oldDerivedFields != null) {
            if (derivedFields == null) {
                derivedFields = copyCollection(oldDerivedFields, alreadyDone);
            } else {
                derivedFields.addAll(copyCollection(oldDerivedFields, alreadyDone));
            }
        }
        if (oldDerivedTables != null) {
            if (derivedTables == null) {
                derivedTables = copyCollection(oldDerivedTables, alreadyDone);
            } else {
                derivedTables.addAll(copyCollection(oldDerivedTables, alreadyDone));
            }
        }
    }

    /**
     * INTERNAL:
     * set the flag indicating whether subclasses should be joined
     */
    public void setShouldUseOuterJoinForMultitableInheritance(boolean shouldUseOuterJoinForMultitableInheritance) {
        this.shouldUseOuterJoinForMultitableInheritance = shouldUseOuterJoinForMultitableInheritance;
    }

    public boolean shouldUseOuterJoin() {
        return shouldUseOuterJoin;
    }

    public boolean shouldUseOuterJoinForMultitableInheritance() {
        return shouldUseOuterJoinForMultitableInheritance;
    }
    
    /**
     * INTERNAL:
     * writes the first field from each of the owned tables, used for
     * fine-grained pessimistic locking.
     */
    protected void writeForUpdateOfFields(ExpressionSQLPrinter printer, SQLSelectStatement statement) {
        for (Enumeration fieldsEnum = getForUpdateOfFields().elements();
                 fieldsEnum.hasMoreElements();) {
            DatabaseField field = (DatabaseField)fieldsEnum.nextElement();
            writeField(printer, field, statement);
        }
    }
}
