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
package oracle.toplink.essentials.queryframework;

import java.util.HashMap;
import java.util.Iterator;

import oracle.toplink.essentials.exceptions.DatabaseException;
import oracle.toplink.essentials.exceptions.QueryException;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.expressions.ExpressionBuilder;

/**
 * PUBLIC:
 * A Query Class used to perform a bulk update using TopLink's expression framework.
 * This class is provided to help optimize performance. It can be used in place 
 * of reading in all the objects to be changed and issuing single updates per 
 * instance. With this approach a single SQL UPDATE statement can be issued and 
 * then, based on the Expression provided, any objects in the cache that are 
 * effected by the update can be invalidated. 
 * <p>
 * Notes: <ul>
 * <li>By default, if a UOW is being used, this query will be deferred until the UOW commits.
 * <li>UpdateAllQuery does not support foreign key updates 
 * unless the relationship is 1-1 (without back pointers.)</ul>
 * <p> 
 * <b>Example of Usage:</b> Adding an area code. <br>
 * <code> 
 * UpdateAllQuery updateQuery = new UpdateAllQuery(Employee.class);   <br>
 * updateQuery.setSelectionCriteria(eb.get("areaCode").isNull());     <br>
 * updateQuery.addUpdate(eb.get("areaCode"), "613");                  <br>
 * </code> 
 * 
 * @author Guy Pelletier
 * @date March 1, 2004
 */
public class UpdateAllQuery extends ModifyAllQuery {

    protected HashMap m_updateClauses;
    
    /**
     * PUBLIC:
     * Constructs a default update all query.
     */
    public UpdateAllQuery() {
        super();
    }

    /**
     * PUBLIC:
     * Constructs an update all query for the Class type specified.
     * @param referenceClass Class
     */
    public UpdateAllQuery(Class referenceClass) {
        super(referenceClass);
    }

    /**
     * PUBLIC:
     * Constructs an update all query for the specified Class type and selection criteria.
     * @param referenceClass Class type to be considered
     * @param selectionCriteria Expression
     */
    public UpdateAllQuery(Class referenceClass, Expression selectionCriteria) {
        super(referenceClass, selectionCriteria);
    }

    /**
     * PUBLIC:
     * Constructs an update all query for the Class type specified and the given 
     * ExpressionBuilder. This sets the default builder which is used for all associated 
	 * expressions in the query.<br>
     * @param referenceClass Class type to be considered
     * @param builder ExpressionBuilder
     */
    public UpdateAllQuery(Class referenceClass, ExpressionBuilder expressionBuilder) {
        super(referenceClass);
        this.defaultBuilder = expressionBuilder;
    }

    /**
     * PUBLIC:
     * Adds the update (SET) clause to the query. Uses default ExpressionBuilder.
     * @param field Expression Object level representation of a database query 'where' clause
     * @param value Object, the new value
     */
    public void addUpdate(Expression field, Object value) {
        addUpdateInternal(field, value);
    }

    /**
     * PUBLIC:
     * Adds the update (SET) clause to the query. Uses default ExpressionBuilder. 
     * @param attributeName String, the name of the attribute
     * @param value Object, the new value
     */
    public void addUpdate(String attributeName, Object value) {
        addUpdateInternal(attributeName, value);
    }

    /**
     * PUBLIC:
     * Adds the update (SET) clause to the query. This method ensures that
     * the builder has the session and reference class set for both given Expressions. 
	 * Uses default ExpressionBuilder.
     * @param field Expression, representation of a database query 'where' clause that describes the field
     * @param value Expression, representation of a database query 'where' clause that describes the new value
     */
    public void addUpdate(Expression field, Expression value) {
        addUpdateInternal(field, value);
    }

    /**
     * PUBLIC:
     * Adds the update (SET) clause to the query. Uses default ExpressionBuilder.
     * @param attributeName String, the name of the attribute
     * @param value Expression, the new value
     */
    public void addUpdate(String attributeName, Expression value) {
        addUpdateInternal(attributeName, value);
    }
	
	/**
	 * INTERNAL:
	 */
    protected void addUpdateInternal(Object fieldObject, Object valueObject) {
        if(fieldObject == null) {
            throw QueryException.updateAllQueryAddUpdateFieldIsNull(this);
        }
        if(m_updateClauses == null) {
            m_updateClauses = new HashMap();
        }
        m_updateClauses.put(fieldObject, valueObject);
    }

    /**
     * INTERNAL:
     * Issue the SQL to the database and then merge into the cache.
     * If we are within a UoW, the merge to the cache must not be done until
     * the UoW merges into the parent. The UoW will trigger the merge to occur
     * at the correct time and will ensure the cache setting is set to none at
     * that time.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        result = getQueryMechanism().updateAll();// fire the SQL to the database
        mergeChangesIntoSharedCache();
        return result;
    }

    /**
     * INTERNAL:
     * Return the updates stored for an update all query
     */
    public HashMap getUpdateClauses() {
        return m_updateClauses;
    }

    /**
     * INTERNAL:
     * Return true if this is an update all query.
     */
    public boolean isUpdateAllQuery() {
        return true;
    }

    /**
     * INTERNAL:
     */
    protected void prepare() throws QueryException {
        super.prepare();// will tell the query mechanism to prepare itself as well.

        Class referenceClass = getReferenceClass();

        // Check the reference class, must be set
        if (referenceClass == null) {
            throw QueryException.referenceClassMissing(this);
        }

        // Check the descriptor is set, if not try to get it from the session
        if (getDescriptor() == null) {
            ClassDescriptor desc = getSession().getDescriptor(referenceClass);

            if (desc == null) {
                throw QueryException.descriptorIsMissing(referenceClass, this);
            }

            setDescriptor(desc);
        }

        ClassDescriptor descriptor = getDescriptor();

        // Check the descriptor for an aggregate
        if (descriptor.isAggregateDescriptor()) {
            throw QueryException.aggregateObjectCannotBeDeletedOrWritten(descriptor, this);
        }

        // Check that the update statement is set. That is the bare minimum,
        // the user can execute this query without a selection criteria though.
        if ((getUpdateClauses() == null || getUpdateClauses().isEmpty()) && isExpressionQuery()) {
            throw QueryException.updateStatementsNotSpecified();
        }
        
        getQueryMechanism().prepareUpdateAll();
    }

    /**
     * INTERNAL:
     * Initialize the expression builder which should be used for this query. If
     * there is a where clause, use its expression builder.
     * If after this method defaultBuilder is still null,
     * then initializeDefaultBuilder method will generate and cache it.
     */
    protected void initializeQuerySpecificDefaultBuilder() {
        super.initializeQuerySpecificDefaultBuilder();
        if(this.defaultBuilder == null && m_updateClauses != null) {
            Iterator it = m_updateClauses.values().iterator();
            while(it.hasNext() ) {
                Object value = it.next();
                if(value != null) {
                    if(value instanceof Expression) {
                        Expression valueExpression = (Expression)value;
                        this.defaultBuilder = valueExpression.getBuilder();
                        if(this.defaultBuilder != null) {
                            return;
                        }
                    }
                }
            }
            it = m_updateClauses.keySet().iterator();
            while(it.hasNext() ) {
                Object field = it.next();
                if(field instanceof Expression) {
                    Expression fieldExpression = (Expression)field;
                    this.defaultBuilder = fieldExpression.getBuilder();
                    if(this.defaultBuilder != null) {
                        return;
                    }
                }
            }
        }
    }
    
}
