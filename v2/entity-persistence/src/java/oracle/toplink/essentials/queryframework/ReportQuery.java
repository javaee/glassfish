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

import java.util.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.internal.queryframework.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.mappings.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.UnitOfWorkImpl;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * <b>Purpose</b>: Query for information about a set of objects instead of the objects themselves.
 * This supports select single attributes, nested attributes, aggregation functions and group bys.<p>
 *
 * <b>Attribute Types</b>:<ol>
 * <li>addAttribute("directQueryKey") is a short cut method to add an attribute with the same name as its corresponding direct query key.
 * <li>addAttribute("attributeName", expBuilder.get("oneToOneMapping").get("directQueryKey")) is the full approach for get values through joined 1:1 relationships.
 * <li>addAttribute("attributeName", expBuilder.getField("TABLE.FIELD")) allows the addition of raw values or values which were not mapped in the object model directly (i.e. FK attributes).
 * <li>addAttribute("attributeName", null) Leave a place holder (NULL) value in the result (used for included values from other systems or calculated values).
 * </ol>
 * <b>Retrieving Primary Keys</b>:     It is possble to retrieve the primary key raw values within each result, but stored in a separate (internal) vector. This
 *                                            primary key vector can later be used to retrieve the real object.
 *                                            @see #retrievePrimaryKeys()
 *                                            @see ReportQueryResult#readObject(Class, Session)
 *                                            If the values are wanted in the result array then they must be added as attributes. For primary keys which are not mapped directly
 *                                            you can add them as DatabaseFields (see above).
 *
 * @author Doug Clarke
 * @since TOPLink/Java 2.0
 */
public class ReportQuery extends ReadAllQuery {

    /** Simplifies the result by only returning the first result. */
    public static final int ShouldReturnSingleResult = 1;

    /** Simplifies the result by only returning one value. */
    public static final int ShouldReturnSingleValue = 2;

    /** Simplifies the result by only returning the single attribute(as opposed to wrapping in a
    ReportQueryResult). */
    public static final int ShouldReturnSingleAttribute = 3;
    
    /** For EJB 3 support returns results without using the ReportQueryResult */
    public static final int ShouldReturnWithoutReportQueryResult = 4;

    /** Specifies whether to retreive primary keys, first primary key, or no primary key.*/
    public static final int FULL_PRIMARY_KEY = 2;
    public static final int FIRST_PRIMARY_KEY = 1;
    public static final int NO_PRIMARY_KEY = 0;
    
    //GF_ISSUE_395
    protected static final Boolean RESULT_IGNORED = Boolean.valueOf(true);
    //end GF_ISSUE
    
    /** Flag indicating wether the primary key values should also be retrieved for the reference class. */
    protected int shouldRetrievePrimaryKeys;

    /** Collection of names for use by results. */
    protected Vector names;

    /** Items to be selected, these could be attributes or aggregate functions. */
    protected Vector items;

    /** Expressions representing fields to be used in the GROUP BY clause. */
    protected Vector groupByExpressions;
    
    /** Expression representing the HAVING clause. */
    protected Expression havingExpression;

    /** Can be one of (ShouldReturnSingleResult, ShouldReturnSingleValue, ShouldReturnSingleAttribute)
     ** Simplifies the result by only returning the first result, first value, or all attribute values
     */
    protected int returnChoice;
    
    /** flag to allow items to be added to the last ConstructorReportItem **/
    protected boolean addToConstructorItem;
    protected Class resultConstructorClass;
    protected Class[] constructorArgTypes;
    protected List constructorMappings;
    
    /* GF_ISSUE_395 this attribute stores a set of unique keys that identity results.
     * Used when distinct has been set on the query.  For use in TCK
     */
    protected HashSet returnedKeys;

    /**
     * INTERNAL:
     * The builder should be provided.
     */
    public ReportQuery() {
        this.queryMechanism = new ExpressionQueryMechanism(this);
        this.items = new Vector();
        this.shouldRetrievePrimaryKeys = NO_PRIMARY_KEY;
        this.groupByExpressions = new Vector(3);
        this.havingExpression=null;
        this.addToConstructorItem = false;

        // overwrite the lock mode to NO_LOCK, this prevents the report query to lock
        // when DEFAULT_LOCK_MODE and a pessimistic locking policy are used.
        this.setLockMode(ObjectBuildingQuery.NO_LOCK);
    }

    public ReportQuery(Class javaClass, Expression expression) {
        this();
        this.defaultBuilder = expression.getBuilder();
        setReferenceClass(javaClass);
        setSelectionCriteria(expression);
    }

    /**
     * PUBLIC:
     * The report query is require to be constructor with an expression builder.
     * This build must be used for the selection critiera, any item expressions, group bys and order bys.
     */
    public ReportQuery(Class javaClass, ExpressionBuilder builder) {
        this();
        this.defaultBuilder = builder;
        setReferenceClass(javaClass);
    }

    /**
     * PUBLIC:
     * The report query is require to be constructor with an expression builder.
     * This build must be used for the selection critiera, any item expressions, group bys and order bys.
     */
    public ReportQuery(ExpressionBuilder builder) {
        this();
        this.defaultBuilder = builder;
    }

    /**
     * PUBLIC:
     * Add the attribute from the reference class to be included in the result.
     * EXAMPLE: reportQuery.addAttribute("firstName");
     */
    public void addAttribute(String itemName) {
        addItem(itemName, getExpressionBuilder().get(itemName));
    }

    /**
     * PUBLIC:
     * Add the attribute to be included in the result.
     * EXAMPLE: reportQuery.addAttribute("city", expBuilder.get("address").get("city"));
     */
    public void addAttribute(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression);
    }

    /**
     * PUBLIC:
     * Add the attribute to be included in the result.  Return the result as the provided class
     * EXAMPLE: reportQuery.addAttribute("city", expBuilder.get("period").get("startTime"), Time.class);
     */
    public void addAttribute(String itemName, Expression attributeExpression, Class type) {
        addItem(itemName, attributeExpression, type);
    }

    /**
     * PUBLIC:
     * Add the average value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addAverage("salary");
     */
    public void addAverage(String itemName) {
        addAverage(itemName, getExpressionBuilder().get(itemName));
    }
    
    /**
     * PUBLIC:
     * Add the average value of the attribute to be included in the result and
     * return it as the specified resultType.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addAverage("salary", Float.class);
     */
    public void addAverage(String itemName, Class resultType) {
        addAverage(itemName, getExpressionBuilder().get(itemName), resultType);
    }

    /**
     * PUBLIC:
     * Add the average value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addAverage("managerSalary", expBuilder.get("manager").get("salary"));
     */
    public void addAverage(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.average());
    }
    
    /**
     * PUBLIC:
     * Add the average value of the attribute to be included in the result and
     * return it as the specified resultType.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addAverage("managerSalary", expBuilder.get("manager").get("salary"), Double.class);
     */
    public void addAverage(String itemName, Expression attributeExpression, Class resultType) {
        addItem(itemName, attributeExpression.average(), resultType);
    }
    
    /**
     * PUBLIC: 
     * Add a ConstructorReportItem to this query's set of return values.
     * @param ConstructorReportItem - used to specify a class constructor and values to pass in from this query
     * @see ConstructorReportItem
     */
    public void addConstructorReportItem(ConstructorReportItem item){
        addItem(item);
    }

    /**
     * PUBLIC:
     * Include the number of rows returned by the query in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE:
     * Java:
     *     reportQuery.addCount();
     * SQL:
     *     SELECT COUNT (*) FROM ...
     * @see #addCount(java.lang.String)
     */
    public void addCount() {
        addCount("COUNT", getExpressionBuilder());
    }

    /**
     * PUBLIC:
     * Include the number of rows returned by the query in the result, where attributeExpression is not null.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * <p>Example:
     * <pre><blockquote>
     * TopLink:    reportQuery.addCount("id");
     * SQL: SELECT COUNT (t0.EMP_ID) FROM EMPLOYEE t0, ...
     * </blockquote></pre>
     * @param attributeName the number of rows where attributeName is not null will be returned.
     * @see #addCount(java.lang.String, oracle.toplink.essentials.expressions.Expression)
     */
    public void addCount(String attributeName) {
        addCount(attributeName, getExpressionBuilder().get(attributeName));
    }
    
    /**
     * PUBLIC:
     * Include the number of rows returned by the query in the result, where attributeExpression is not null.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * Set the count to be returned as the specified resultType.
     * <p>Example:
     * <pre><blockquote>
     * TopLink:    reportQuery.addCount("id", Long.class);
     * SQL: SELECT COUNT (t0.EMP_ID) FROM EMPLOYEE t0, ...
     * </blockquote></pre>
     * @param attributeName the number of rows where attributeName is not null will be returned.
     * @see #addCount(java.lang.String, oracle.toplink.essentials.expressions.Expression)
     */
    public void addCount(String attributeName, Class resultType) {
        addCount(attributeName, getExpressionBuilder().get(attributeName), resultType);
    }

    /**
     * PUBLIC:
     * Include the number of rows returned by the query in the result, where attributeExpression
     * is not null.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * <p>Example:
     * <pre><blockquote>
     * TopLink:    reportQuery.addCount("Count", getExpressionBuilder().get("id"));
     * SQL: SELECT COUNT (t0.EMP_ID) FROM EMPLOYEE t0, ...
     * </blockquote></pre>
     * <p>Example: counting only distinct values of an attribute.
     * <pre><blockquote>
     *  TopLink: reportQuery.addCount("Count", getExpressionBuilder().get("address").distinct());
     *  SQL: SELECT COUNT (DISTINCT t0.ADDR_ID) FROM EMPLOYEE t0, ...
     * </blockquote></pre>
     * objectAttributes can be specified also, even accross many to many
     * mappings.
     * @see #addCount()
     */
    public void addCount(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.count());
    }
    
    /**
     * PUBLIC:
     * Include the number of rows returned by the query in the result, where attributeExpression
     * is not null.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * Set the count to be returned as the specified resultType.
     * <p>Example:
     * <pre><blockquote>
     * TopLink:    reportQuery.addCount("Count", getExpressionBuilder().get("id"), Integer.class);
     * SQL: SELECT COUNT (t0.EMP_ID) FROM EMPLOYEE t0, ...
     * </blockquote></pre>
     * <p>Example: counting only distinct values of an attribute.
     * <pre><blockquote>
     *  TopLink: reportQuery.addCount("Count", getExpressionBuilder().get("address").distinct());
     *  SQL: SELECT COUNT (DISTINCT t0.ADDR_ID) FROM EMPLOYEE t0, ...
     * </blockquote></pre>
     * objectAttributes can be specified also, even accross many to many
     * mappings.
     * @see #addCount()
     */
    public void addCount(String itemName, Expression attributeExpression, Class resultType) {
        addItem(itemName, attributeExpression.count(), resultType);
    }

    /**
     * ADVANCED:
     * Add the function against the attribute expression to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * Example: reportQuery.addFunctionItem("average", expBuilder.get("salary"), "average");
     */
    public void addFunctionItem(String itemName, Expression attributeExpression, String functionName) {
        Expression functionExpression = attributeExpression;
        functionExpression = attributeExpression.getFunction(functionName);

        ReportItem item = new ReportItem(itemName, functionExpression);
        addItem(item);

    }

    /**
     * PUBLIC:
     * Add the attribute to the group by expressions.
     * This will group the result set on that attribute and is normally used in conjunction with aggregation functions.
     * Example: reportQuery.addGrouping("lastName")
     */
    public void addGrouping(String attributeName) {
        addGrouping(getExpressionBuilder().get(attributeName));
    }

    /**
     * PUBLIC:
     * Add the attribute expression to the group by expressions.
     * This will group the result set on that attribute and is normally used in conjunction with aggregation functions.
     * Example: reportQuery.addGrouping(expBuilder.get("address").get("country"))
     */
    public void addGrouping(Expression expression) {
        getGroupByExpressions().addElement(expression);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }
    
    /**
     * PUBLIC:
     * Add the expression to the query to be used in the HAVING clause.
     * This epression will be used to filter the result sets after they are grouped.  It must be used in conjunction with the GROUP BY clause.
     * Example: reportQuery.setHavingExpression(expBuilder.get("address").get("country").equal("Canada"))
     */
    public void setHavingExpression(Expression expression) {
        havingExpression = expression;
        setIsPrepared(false);
    }
    
    /**
     * INTERNAL:
     * Method used to abstract addToConstructorItem behavour from the public addItem methods
     */
    private void addItem(ReportItem item){
        if (addToConstructorItem && (getItems().size()>0) &&(((ReportItem)getItems().lastElement()).isContructorItem() )){
            ((ConstructorReportItem)getItems().lastElement()).addItem(item);
        }else{
            getItems().addElement(item);
        }
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * ADVANCED:
     * Add the expression value to be included in the result.
     * EXAMPLE: reportQuery.addItem("name", expBuilder.get("firstName").toUpperCase());
     */
    public void addItem(String itemName, Expression attributeExpression) {
        ReportItem item = new ReportItem(itemName, attributeExpression);
        addItem(item);
    }
    
    /**
     * ADVANCED:
     * Add the expression value to be included in the result.
     * EXAMPLE: reportQuery.addItem("name", expBuilder.get("firstName").toUpperCase());
     */
    public void addItem(String itemName, Expression attributeExpression, List joinedExpressions) {
        ReportItem item = new ReportItem(itemName, attributeExpression);
        item.getJoinedAttributeManager().setJoinedAttributeExpressions_(joinedExpressions);
        addItem(item);
    }
    
    /**
     * INTERNAL:
     * Add the expression value to be included in the result.
     * EXAMPLE: reportQuery.addItem("name", expBuilder.get("firstName").toUpperCase());
     * The resultType can be specified to support EJBQL that adheres to the
     * EJB 3.0 spec.
     */
    protected void addItem(String itemName, Expression attributeExpression, Class resultType) {
        ReportItem item = new ReportItem(itemName, attributeExpression);
        item.setResultType(resultType);
        addItem(item);
    }

    /**
     * PUBLIC:
     * Add the maximum value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addMaximum("salary");
     */
    public void addMaximum(String itemName) {
        addMaximum(itemName, getExpressionBuilder().get(itemName));
    }

    /**
     * PUBLIC:
     * Add the maximum value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addMaximum("managerSalary", expBuilder.get("manager").get("salary"));
     */
    public void addMaximum(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.maximum());
    }

    /**
     * PUBLIC:
     * Add the minimum value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addMinimum("salary");
     */
    public void addMinimum(String itemName) {
        addMinimum(itemName, getExpressionBuilder().get(itemName));
    }

    /**
     * PUBLIC:
     * Add the minimum value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addMinimum("managerSalary", expBuilder.get("manager").get("salary"));
     */
    public void addMinimum(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.minimum());
    }

    /**
     * PUBLIC:
     * Add the standard deviation value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addStandardDeviation("salary");
     */
    public void addStandardDeviation(String itemName) {
        addStandardDeviation(itemName, getExpressionBuilder().get(itemName));
    }

    /**
     * PUBLIC:
     * Add the standard deviation value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addStandardDeviation("managerSalary", expBuilder.get("manager").get("salary"));
     */
    public void addStandardDeviation(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.standardDeviation());
    }

    /**
     * PUBLIC:
     * Add the sum value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addSum("salary");
     */
    public void addSum(String itemName) {
        addSum(itemName, getExpressionBuilder().get(itemName));
    }

    /**
     * PUBLIC:
     * Add the sum value of the attribute to be included in the result and
     * return it as the specified resultType.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addSum("salary", Float.class);
     */
    public void addSum(String itemName, Class resultType) {
        addSum(itemName, getExpressionBuilder().get(itemName), resultType);
    }

    /**
     * PUBLIC:
     * Add the sum value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addSum("managerSalary", expBuilder.get("manager").get("salary"));
     */
    public void addSum(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.sum());
    }

    /**
     * PUBLIC:
     * Add the sum value of the attribute to be included in the result and
     * return it as the specified resultType.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addSum("managerSalary", expBuilder.get("manager").get("salary"), Float.class);
     */
    public void addSum(String itemName, Expression attributeExpression, Class resultType) {
        addItem(itemName, attributeExpression.sum(), resultType);
    }

    /**
     * PUBLIC:
     * Add the variance value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addVariance("salary");
     */
    public void addVariance(String itemName) {
        addVariance(itemName, getExpressionBuilder().get(itemName));
    }

    /**
     * PUBLIC:
     * Add the variance value of the attribute to be included in the result.
     * Aggregation functions can be used with a group by, or on the entire result set.
     * EXAMPLE: reportQuery.addVariance("managerSalary", expBuilder.get("manager").get("salary"));
     */
    public void addVariance(String itemName, Expression attributeExpression) {
        addItem(itemName, attributeExpression.variance());
    }
    
    /**
     * PUBLIC: Call a constructor for the given class with the results of this query.
     * @param constructorClass 
     */
    public ConstructorReportItem beginAddingConstructorArguments(Class constructorClass){
        ConstructorReportItem citem = new ConstructorReportItem(constructorClass.getName());
        citem.setResultType(constructorClass);
        //add directly to avoid addToConstructorItem behaviour
        getItems().add(citem);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
        this.addToConstructorItem=true;
        return citem;
    }
    /**
     * PUBLIC: Call a constructor for the given class with the results of this query.
     * @param constructorClass 
     * @param constructorArgTypes - sets the argument types to be passed to the constructor.
     */
    public ConstructorReportItem beginAddingConstructorArguments(Class constructorClass, Class[] constructorArgTypes){
        ConstructorReportItem citem =beginAddingConstructorArguments(constructorClass);
        citem.setConstructorArgTypes(constructorArgTypes);
        return citem;
    }

    /**
     * INTERNAL:
     * Construct a result from a row. Either return a ReportQueryResult or just the attribute.
     */
    public Object buildObject(AbstractRecord row, Vector toManyJoinData) {
        ReportQueryResult reportQueryResult = new ReportQueryResult(this, row, toManyJoinData);
        //GF_ISSUE_395
        if (this.returnedKeys != null){
            if (this.returnedKeys.contains(reportQueryResult.getResultKey())){
                return RESULT_IGNORED; //distinguish between null values and thrown away duplicates
            } else {
                this.returnedKeys.add(reportQueryResult.getResultKey());
            }
        }
        //end GF_ISSUE_395
        if (this.shouldReturnSingleAttribute()) {
            return reportQueryResult.getResults().firstElement();
        } 
        if (this.shouldReturnWithoutReportQueryResult()){
            if (reportQueryResult.getResults().size() == 1){
                return reportQueryResult.getResults().firstElement();
            }
            return reportQueryResult.toArray();
        }
        return reportQueryResult;
    }

    /**
     * INTERNAL:
     * Construct a container of ReportQueryResult from the rows.
     * If only one result or value was asked for only return that.
     */
    public Object buildObjects(Vector rows) {
        if (shouldReturnSingleResult() || shouldReturnSingleValue()) {
            if (rows.isEmpty()) {
                return null;
            }
            ReportQueryResult result = (ReportQueryResult)buildObject((AbstractRecord)rows.firstElement(), rows);
            if (shouldReturnSingleValue()) {
                return result.elements().nextElement();
            }
            return result;
        }

        ContainerPolicy containerPolicy = getContainerPolicy();
        Object reportResults = containerPolicy.containerInstance(rows.size());
        // GF_ISSUE_395
        if (shouldDistinctBeUsed()){
            this.returnedKeys = new HashSet();
        }
        //end GF_ISSUE
        //If only the attribute is desired, then buildObject will only get the first attribute each time
        for (Enumeration rowsEnum = rows.elements(); rowsEnum.hasMoreElements();) {
            // GF_ISSUE_395
            Object result = buildObject((AbstractRecord)rowsEnum.nextElement(), rows);
            if (result != RESULT_IGNORED){
                containerPolicy.addInto(result, reportResults, getSession());
            }
            //end GF_ISSUE
        }
        return reportResults;
    }

    /**
     * INTERNAL:
     * The cache check is done before the prepare as a hit will not require the work to be done.
     */
    protected Object checkEarlyReturnImpl(AbstractSession session, AbstractRecord translationRow) {
        // Check for in-memory only query.
        if (shouldCheckCacheOnly()) {
            throw QueryException.cannotSetShouldCheckCacheOnlyOnReportQuery();
        } else {
            return null;
        }
    }
    
    /**
     * INTERNAL: Required for a very special case of bug 2612185:
     * ReportItems from parallelExpressions, on a ReportQuery which is a subQuery,
     * which is being batch read.
     * In a batch query the selection criteria is effectively cloned twice, meaning
     * the ReportItems need to be cloned an extra time also to stay in sync.
     * Each call to copiedVersionFrom() will take O(1) time as the expression was
     * already cloned.
     */
    public void copyReportItems(Dictionary alreadyDone) {
        items = (Vector)items.clone();
        for (int i = items.size() - 1; i >= 0; i--) {
            ReportItem item = (ReportItem)items.elementAt(i);
            Expression expression = item.getAttributeExpression();
            if ((expression != null) && (alreadyDone.get(expression.getBuilder()) != null)) {
                expression = expression.copiedVersionFrom(alreadyDone);
            }
            items.set(i, new ReportItem(item.getName(), expression));
        }
        if (groupByExpressions != null) {
            groupByExpressions = (Vector)groupByExpressions.clone();
            for (int i = groupByExpressions.size() - 1; i >= 0; i--) {
                Expression item = (Expression)groupByExpressions.elementAt(i);
                if (alreadyDone.get(item.getBuilder()) != null) {
                    groupByExpressions.set(i, item.copiedVersionFrom(alreadyDone));
                }
            }
        }
        if (orderByExpressions != null) {
            for (int i = orderByExpressions.size() - 1; i >= 0; i--) {
                Expression item = (Expression)orderByExpressions.elementAt(i);
                if (alreadyDone.get(item.getBuilder()) != null) {
                    orderByExpressions.set(i, item.copiedVersionFrom(alreadyDone));
                }
            }
        }
    }

    /**
     * PUBLIC:
     * Set if the query results should contain the primary keys or each associated object.
     * This make retrieving the real object easier.
     * By default they are not retrieved.
     */
    public void dontRetrievePrimaryKeys() {
        setShouldRetrievePrimaryKeys(false);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Don't simplify the result by returning the single attribute. Wrap in a ReportQueryResult.
     */
    public void dontReturnSingleAttribute() {
        if (shouldReturnSingleAttribute()) {
            returnChoice = 0;
        }
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning the first result.
     * This can be used if it known that only one row is returned by the report query.
     */
    public void dontReturnSingleResult() {
        if (shouldReturnSingleResult()) {
            returnChoice = 0;
        }
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning a single value.
     * This can be used if it known that only one row is returned by the report query and only a single item is added
     * to the report.
     */
    public void dontReturnSingleValue() {
        if (shouldReturnSingleValue()) {
            returnChoice = 0;
        }
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning a single value.
     * This can be used if it known that only one row is returned by the report query and only a single item is added
     * to the report.
     */
    public void dontReturnWithoutReportQueryResult() {
        if (shouldReturnWithoutReportQueryResult()) {
            returnChoice = 0;
        }
    }
    
    /**
     * PUBLIC:
     * Used in conjunction with beginAddingConstructorArguments to signal that expressions should no longer be 
     * be added to the collection used in the constructor
     * 
     * Get the rows and build the object from the rows.
     * @exception  DatabaseException - an error has occurred on the database
     * @return Vector - collection of objects resulting from execution of query.
     */
    public void endAddingToConstructorItem(){
        this.addToConstructorItem=false;
    }

    /**
     * INTERNAL:
     * Execute the query.
     * Get the rows and build the object from the rows.
     * @exception  DatabaseException - an error has occurred on the database
     * @return Vector - collection of objects resulting from execution of query.
     */
    public Object executeDatabaseQuery() throws DatabaseException {
        // ensure a pessimistic locking query will go down the write connection
       if (isLockQuery() && getSession().isUnitOfWork()) {
            UnitOfWorkImpl unitOfWork = (UnitOfWorkImpl)getSession();
            // Note if a nested unit of work this will recursively start a
            // transaction early on the parent also.
            if (isLockQuery()) {
                if ((!unitOfWork.getCommitManager().isActive()) && (!unitOfWork.wasTransactionBegunPrematurely())) {
                    unitOfWork.beginTransaction();
                    unitOfWork.setWasTransactionBegunPrematurely(true);
                }
            }
        }
        
        if (getContainerPolicy().overridesRead()) {
            return getContainerPolicy().execute();
        }

        if (getQueryId() == 0) {
            setQueryId(getSession().getNextQueryId());
        }

        Vector rows = getQueryMechanism().selectAllReportQueryRows();
        // If using -m joins, must set all rows.
        return buildObjects(rows);
    }

    /**
     * INTERNAL:
     * Return the group bys.
     */
    public Vector getGroupByExpressions() {
        return groupByExpressions;
    }
    
    /**
     * INTERNAL:
     * Return the Having expression.
     */
    public Expression getHavingExpression() {
        return havingExpression;
    }

    /**
     * INTERNAL:
     * return a collection of expressions if PK's are used.
     */
    public Vector getQueryExpressions() {
        Vector fieldExpressions = new Vector(getItems().size());

        // For bug 3115576 and an EXISTS subquery only need to return a single field.
        if (shouldRetrieveFirstPrimaryKey()) {
            if (!getDescriptor().getPrimaryKeyFields().isEmpty()) {
                fieldExpressions.addElement(getDescriptor().getPrimaryKeyFields().get(0));
            }
        }
        if (shouldRetrievePrimaryKeys()) {
            fieldExpressions.addAll(getDescriptor().getPrimaryKeyFields());
        }

        return fieldExpressions;
    }

    /**
     * INTERNAL:
     * return a collection of expressions from the items. Ignore the null (place holders).
     */
    public Vector getItemExpressions() {
        Vector fieldExpressions = new Vector(getItems().size());

        // For bug 3115576 and an EXISTS subquery only need to return a single field.
        if (shouldRetrieveFirstPrimaryKey()) {
            if (!getDescriptor().getPrimaryKeyFields().isEmpty()) {
                fieldExpressions.addElement(getDescriptor().getPrimaryKeyFields().get(0));
            }
        }
        if (shouldRetrievePrimaryKeys()) {
            fieldExpressions.addAll(getDescriptor().getPrimaryKeyFields());
        }

        for (Enumeration itemsEnum = getItems().elements(); itemsEnum.hasMoreElements();) {
            ReportItem item = (ReportItem)itemsEnum.nextElement();
            Expression fieldExpression = item.getAttributeExpression();
            if (fieldExpression != null) {
                fieldExpressions.addElement(fieldExpression);
            }
        }
        return fieldExpressions;
    }

    /**
     * INTERNAL:
     * @return ReportQueryItems defining the attributes to be read
     */
    public Vector getItems() {
        return items;
    }

    /**
     * INTERNAL:
     * Clear the ReportQueryItems
     */
    public void clearItems() {
        items = new Vector();
        setIsPrepared(false);
    }

    /**
     * INTERNAL:
     * Lazily initialize and return the names of the items requested for use in each result object
     */
    public Vector getNames() {
        if (names == null) {
            names = new Vector();
            for (Enumeration e = getItems().elements(); e.hasMoreElements();) {
                names.addElement(((ReportItem)e.nextElement()).getName());
            }
        }
        return names;
    }

    /**
     * PUBLIC:
     * Return if this is a report query.
     */
    public boolean isReportQuery() {
        return true;
    }

    /**
     * INTERNAL:
     * Prepare the receiver for execution in a session.
     * Initialize each item with its DTF mapping
     */
    protected void prepare() throws QueryException {
        // Oct 19, 2000 JED
        // Added exception to be thrown if no attributes have been added to the query
        if (getItems().size() > 0) {
            try {
                for (Enumeration itemsEnum = getItems().elements(); itemsEnum.hasMoreElements();) {
                    ((ReportItem)itemsEnum.nextElement()).initialize(this);
                }
            } catch (QueryException exception) {
                exception.setQuery(this);
                throw exception;
            }
        } else {
            if ((!shouldRetrievePrimaryKeys()) && (!shouldRetrieveFirstPrimaryKey())) {
                throw QueryException.noAttributesForReportQuery(this);
            }
        }

        super.prepare();

    }

    /**
     * INTERNAL:
     * Prepare a report query with a count defined on an object attribute.
     * Added to fix bug 3268040, addCount(objectAttribute) not supported.
     */
    protected void prepareObjectAttributeCount(Dictionary clonedExpressions) {
        prepareObjectAttributeCount(getItems(), clonedExpressions);
    }
    
    private void prepareObjectAttributeCount(List items, Dictionary clonedExpressions) {
        int numOfReportItems = items.size();
        //gf675: need to loop through all items to fix all count(..) instances
        for (int i =0;i<numOfReportItems; i++){
            ReportItem item = (ReportItem)items.get(i);
            if (item == null) {
                continue;
            } else if (item instanceof ConstructorReportItem) {
                // recursive call to process child ReportItems
                prepareObjectAttributeCount(((ConstructorReportItem)item).getReportItems(), clonedExpressions);
            } else if (item.getAttributeExpression() instanceof FunctionExpression) {
                FunctionExpression count = (FunctionExpression)item.getAttributeExpression();
                if (count.getOperator().getSelector() == ExpressionOperator.Count) {
                    Expression baseExp = count.getBaseExpression();
                    boolean distinctUsed = false;
                    if (baseExp.isFunctionExpression() && (((FunctionExpression)baseExp).getOperator().getSelector() == ExpressionOperator.Distinct)) {
                        distinctUsed = true;
                        baseExp = ((FunctionExpression)baseExp).getBaseExpression();
                    }
                    boolean outerJoin = false;
                    ClassDescriptor newDescriptor = null;
                    if (baseExp.isQueryKeyExpression()) {
                        // now need to find out if it is a direct to field or something else.
                        DatabaseMapping mapping = getLeafMappingFor(baseExp, getDescriptor());
                        if ((mapping != null) && !mapping.isDirectToFieldMapping()) {
                            newDescriptor = mapping.getReferenceDescriptor();
                            outerJoin = ((QueryKeyExpression)baseExp).shouldUseOuterJoin();
                        }
                    } else if (baseExp.isExpressionBuilder()) {
                        newDescriptor = getSession().getDescriptor(((ExpressionBuilder)baseExp).getQueryClass());
                    }
                    
                    if (newDescriptor != null) {
                        // At this point we are committed to rewriting the query.
                        if (newDescriptor.hasSimplePrimaryKey()) {
                            // case 1: simple PK =>
                            // treat COUNT(entity) as COUNT(entity.pk)
                            DatabaseMapping pk = getMappingOfFirstPrimaryKey(newDescriptor);
                            Expression countArg = baseExp.get(pk.getAttributeName());
                            if (distinctUsed) {
                                countArg = countArg.distinct();
                            }
                            count.setBaseExpression(countArg);
                            count.getChildren().setElementAt(countArg, 0);
                        } else if (!distinctUsed) {
                            // case 2: composite PK, but no DISTINCT =>
                            // pick a PK column for the COUNT aggregate
                            DatabaseMapping pk = getMappingOfFirstPrimaryKey(newDescriptor);
                            Expression countArg = baseExp.get(pk.getAttributeName());
                            while (pk.isAggregateObjectMapping()) {
                                newDescriptor = ((AggregateObjectMapping)pk).getReferenceDescriptor();
                                pk = getMappingOfFirstPrimaryKey(newDescriptor);
                                countArg = countArg.get(pk.getAttributeName());
                            }
                            count.setBaseExpression(countArg);
                            count.getChildren().setElementAt(countArg, 0);
                        } else if (!outerJoin) {
                            // case 3: composite PK and DISTINCT, but no
                            // outer join => previous solution using
                            // COUNT(*) and EXISTS subquery
                            
                            // If this is a subselect baseExp is yet uncloned,
                            // and will miss out if moved now from items into a selection criteria.
                            if (clonedExpressions != null) {
                                if (clonedExpressions.get(baseExp.getBuilder()) != null) {
                                    baseExp = (QueryKeyExpression)baseExp.copiedVersionFrom(clonedExpressions);
                                } else {
                                    baseExp = (QueryKeyExpression)baseExp.rebuildOn(getExpressionBuilder());
                                }
                            }
                            
                            // Now the reference class of the query needs to be reversed.
                            // See the bug description for an explanation.
                            ExpressionBuilder countBuilder = baseExp.getBuilder();
                            ExpressionBuilder outerBuilder = new ExpressionBuilder();
                            
                            ReportQuery subSelect = new ReportQuery(getReferenceClass(), countBuilder);
                            subSelect.setShouldRetrieveFirstPrimaryKey(true);
                            
                            // Make sure the outerBuilder does not appear on the left of the subselect.
                            // Putting a builder on the left is desirable to trigger an optimization.
                            if (getSelectionCriteria() != null) {
                                outerBuilder.setQueryClass(newDescriptor.getJavaClass());
                                subSelect.setSelectionCriteria(baseExp.equal(outerBuilder).and(getSelectionCriteria()));
                            } else {
                                subSelect.setSelectionCriteria(baseExp.equal(outerBuilder));
                            }
                            setSelectionCriteria(outerBuilder.exists(subSelect));
                            count.setBaseExpression(outerBuilder);
                            count.getChildren().setElementAt( outerBuilder, 0);
                            setReferenceClass(newDescriptor.getJavaClass());
                            changeDescriptor(getSession());
                        } else {
                            // case 4: composite PK, DISTINCT, outer join => 
                            // not supported, throw exception
                            throw QueryException.distinctCountOnOuterJoinedCompositePK(
                                newDescriptor, this);
                        }
                    }
                }
            }
        }
    }

    /** */
    private DatabaseMapping getMappingOfFirstPrimaryKey(ClassDescriptor descriptor) {
        if (descriptor != null) {
            for (Iterator i = descriptor.getMappings().iterator(); i.hasNext(); ) {
                DatabaseMapping m = (DatabaseMapping)i.next();
                if (m.isPrimaryKeyMapping()) {
                    return m;
                }
            }
        }
        return null;
    }

    /**
     * INTERNAL:
     * Prepare the mechanism.
     */
    protected void prepareSelectAllRows() {
        prepareObjectAttributeCount(null);

        getQueryMechanism().prepareReportQuerySelectAllRows();
    }

    /**
     * INTERNAL:
     * Prepare the receiver for being printed inside a subselect.
     * This prepares the statement but not the call.
     */
    public synchronized void prepareSubSelect(AbstractSession session, AbstractRecord translationRow, Dictionary clonedExpressions) throws QueryException {
        if (isPrepared()) {
            return;
        }

        setIsPrepared(true);
        setSession(session);
        setTranslationRow(translationRow);

        checkDescriptor(getSession());

        if (descriptor.isAggregateDescriptor()) {
            // Not allowed
            throw QueryException.aggregateObjectCannotBeDeletedOrWritten(descriptor, this);
        }

        try {
            for (Enumeration itemsEnum = getItems().elements(); itemsEnum.hasMoreElements();) {
                ((ReportItem)itemsEnum.nextElement()).initialize(this);
            }
        } catch (QueryException exception) {
            exception.setQuery(this);
            throw exception;
        }

        prepareObjectAttributeCount(clonedExpressions);

        getQueryMechanism().prepareReportQuerySubSelect();

        setSession(null);
        setTranslationRow(null);
    }

    /**
     * PUBLIC:
     * Set if the query results should contain the primary keys or each associated object.
     * This make retrieving the real object easier.
     * By default they are not retrieved.
     */
    public void retrievePrimaryKeys() {
        setShouldRetrievePrimaryKeys(true);
        //Bug2804042 Must un-prepare if prepared as the SQL may change.
        setIsPrepared(false);
    }

    /**
     * PUBLIC:
     * Simplify the result by returning a single attribute. Don't wrap in a ReportQueryResult.
     */
    public void returnSingleAttribute() {
        returnChoice = ShouldReturnSingleAttribute;
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning the first result.
     * This can be used if it known that only one row is returned by the report query.
     */
    public void returnSingleResult() {
        returnChoice = ShouldReturnSingleResult;
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning a single value.
     * This can be used if it known that only one row is returned by the report query and only a single item is added
     * to the report.
     */
    public void returnSingleValue() {
        returnChoice = ShouldReturnSingleValue;
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning a single value.
     * This can be used if it known that only one row is returned by the report query and only a single item is added
     * to the report.
     */
    public void returnWithoutReportQueryResult() {
        this.returnChoice = ShouldReturnWithoutReportQueryResult;
    }

    /**
     * PUBLIC:
     * Set if the query results should contain the primary keys or each associated object.
     * This make retrieving the real object easier.
     * By default they are not retrieved.
     */
    public void setShouldRetrievePrimaryKeys(boolean shouldRetrievePrimaryKeys) {
        this.shouldRetrievePrimaryKeys = (shouldRetrievePrimaryKeys ? FULL_PRIMARY_KEY : NO_PRIMARY_KEY);
    }

    /**
     * ADVANCED:
     * Sets if the query results should contain the first primary key of each associated object.
     * Usefull if this is an EXISTS subquery and you don't care what fields are returned
     * so long as it is a single field.
     * The default value is false.
     * This should only be used with a subquery.
     */
    public void setShouldRetrieveFirstPrimaryKey(boolean shouldRetrieveFirstPrimaryKey) {
        this.shouldRetrievePrimaryKeys = (shouldRetrieveFirstPrimaryKey ? FIRST_PRIMARY_KEY : NO_PRIMARY_KEY);
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning the attribute (as opposed to wrapping in a ReportQueryResult).
     * This can be used if it is known that only one attribute is returned by the report query.
     */
    public void setShouldReturnSingleAttribute(boolean newChoice) {
        if (newChoice) {
            returnSingleAttribute();
        } else {
            dontReturnSingleAttribute();
        }
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning the first result.
     * This can be used if it known that only one row is returned by the report query.
     */
    public void setShouldReturnSingleResult(boolean newChoice) {
        if (newChoice) {
            returnSingleResult();
        } else {
            dontReturnSingleResult();
        }
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning a single value.
     * This can be used if it known that only one row is returned by the report query and only a single item is added
     * to the report.
     */
    public void setShouldReturnSingleValue(boolean newChoice) {
        if (newChoice) {
            returnSingleValue();
        } else {
            dontReturnSingleValue();
        }
    }

    /**
     * PUBLIC:
     * Simplifies the result by returning a nested list instead of the ReportQueryResult.
     * This is used by EJB 3.
     */
    public void setShouldReturnWithoutReportQueryResult(boolean newChoice) {
        if (newChoice) {
            returnWithoutReportQueryResult();
        } else {
            dontReturnWithoutReportQueryResult();
        }
    }

    /**
     * PUBLIC:
     * Return if the query results should contain the primary keys or each associated object.
     * This make retrieving the real object easier.
     */
    public boolean shouldRetrievePrimaryKeys() {
        return (shouldRetrievePrimaryKeys == FULL_PRIMARY_KEY);
    }

    /**
     * PUBLIC:
     * Return if the query results should contain the first primary key of each associated object.
     * Usefull if this is an EXISTS subquery and you don't care what fields are returned
     * so long as it is a single field.
     */
    public boolean shouldRetrieveFirstPrimaryKey() {
        return (shouldRetrievePrimaryKeys == FIRST_PRIMARY_KEY);
    }

    /**
     * PUBLIC:
     * Answer if we are only returning the attribute (as opposed to wrapping in a ReportQueryResult).
     * This can be used if it is known that only one attribute is returned by the report query.
     */
    public boolean shouldReturnSingleAttribute() {
        return returnChoice == ShouldReturnSingleAttribute;
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning the first result.
     * This can be used if it known that only one row is returned by the report query.
     */
    public boolean shouldReturnSingleResult() {
        return returnChoice == ShouldReturnSingleResult;
    }

    /**
     * PUBLIC:
     * Simplifies the result by only returning a single value.
     * This can be used if it known that only one row is returned by the report query and only a single item is added
     * to the report.
     */
    public boolean shouldReturnSingleValue() {
        return returnChoice == ShouldReturnSingleValue;
    }

    /**
     * PUBLIC:
     * Simplifies the result by returning a nested list instead of the ReportQueryResult.
     * This is used by EJB 3.
     */
    public boolean shouldReturnWithoutReportQueryResult() {
        return returnChoice == ShouldReturnWithoutReportQueryResult;
    }


}
