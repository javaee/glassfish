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
package oracle.toplink.essentials.internal.parsing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a SELECT
 * <p><b>Responsibilities</b>:<ul>
 * <li> Hold the distinct status
 * <li> Modify a query based on the contents
 *
 * The SELECT statement determines the return type of an EJBQL query.
 * The SELECT may also determine the distinct state of a query
 *
 * A SELECT can be one of the following:
 *  1. SELECT OBJECT(someObject)... This query will return a collection of objects
 *  2. SELECT anObject.anAttribute ... This will return a collection of anAttribute
 *  3. SELECT &lt;aggregateFunction&gt; ... This will return a single value
 *      The allowable aggregateFunctions are: AVG, COUNT, MAX, MIN, SUM
 *          SELECT AVG(emp.salary)... Returns the average of all the employees salaries
 *          SELECT COUNT(emp)... Returns a count of the employees
 *          SELECT COUNT(emp.firstName)... Returns a count of the employee's firstNames
 *          SELECT MAX(emp.salary)... Returns the maximum employee salary
 *          SELECT MIN(emp.salary)... Returns the minimum employee salary
 *          SELECT SUM(emp.salary)... Returns the sum of all the employees salaries
 *
 * </ul>
 *    @author Jon Driscoll
 *    @since TopLink 5.0
 */
public class SelectNode extends QueryNode {

    private List selectExpressions = new ArrayList();
    private boolean distinct =false;
    
    public SelectNode() {
    }

    /**
     * INTERNAL
     * Add an Order By Item to this node
     */
    private void addSelectExpression(Object theNode) {
        selectExpressions.add(theNode);
    }

    /** */
    public List getSelectExpressions() {
        return selectExpressions;
    }

    /** */
    public void setSelectExpressions(List exprs) {
        selectExpressions = exprs;
    }

    /** */
    public boolean usesDistinct() {
        return distinct;
    }
    
    /** */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * INTERNAL
     * Returns a DatabaseQuery instance representing the owning
     * ParseTree. This implementation returns a ReadAllQuery for simple SELECT
     * queries and a ReportQuery otherwise.
     */ 
    public DatabaseQuery createDatabaseQuery(ParseTreeContext context) {
        ObjectLevelReadQuery query;
        /* Disable the optimization for the time being. 
           The optimization exposes a problem with the ORDER BY clause in the
           generated SQL (see glassfish issue 2084).
           There is also a performance regression that needs to be
           investigated (see glassfish issue 2171).
        query = isReadAllQuery(context) ? 
            new ReadAllQuery() : new ReportQuery();
        */
        query = new ReportQuery();
        query.dontUseDistinct();//gf bug 1395- prevents using distinct unless user specified
        return query;
    }

    /** 
     * INTERNAL
     * Returns true if the SELECT clause consists of a single expression
     * returning the base identification variable of the query and if the base
     * variable is defined as a range variable w/o FETCH JOINs.
     */
    private boolean isReadAllQuery(ParseTreeContext context) {
        if (!isSingleSelectExpression()) {
            // multiple expressions in the select clause => ReportQuery
            return false;
        }
        
        Node node = getFirstSelectExpressionNode();
        if (!node.isVariableNode()) {
            // Does not select an identification variable (e.g. projection or
            // aggregate function) =>  ReportQuery
            return false;
        }
        String variable = ((VariableNode)node).getCanonicalVariableName();
        
        // Note, the base variable in ParseTreeContext is not yet set =>
        // calculate it
        String baseVariable = getParseTree().getFromNode().getFirstVariable();
        if (!context.isRangeVariable(baseVariable) || 
            (context.getFetchJoins(baseVariable) != null)) {
            // Query's base variable is not a range variable or the base
            // variable has FETCH JOINs => ReportQuery
            return false;
        }
        
        // Use ReadAllQuery if the variable of the SELECT clause expression is
        // the base variable
        return baseVariable.equals(variable);
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(DatabaseQuery theQuery, GenerationContext context) {
        ObjectLevelReadQuery readQuery = (ObjectLevelReadQuery)theQuery;
        if (selectExpressions.isEmpty()) {
            return;
        }

        //set the distinct state
        //BUG 3168673: Don't set distinct state if we're using Count
        if (!(isSingleSelectExpression() && getFirstSelectExpressionNode().isCountNode())) {
            // Set the distinct state for the query
            if (usesDistinct()) {
                getParseTree().setDistinctState(ObjectLevelReadQuery.USE_DISTINCT);
                readQuery.setDistinctState(ObjectLevelReadQuery.USE_DISTINCT);
            }
        }

        if (readQuery instanceof ReportQuery) {
            ReportQuery reportQuery = (ReportQuery)readQuery;
            reportQuery.returnWithoutReportQueryResult();
            if (isSingleSelectExpression() && 
                !getFirstSelectExpressionNode().isConstructorNode()) {
                reportQuery.returnSingleAttribute();
            }
        } 
        SelectGenerationContext selectContext = (SelectGenerationContext)context;
        for (Iterator i = selectExpressions.iterator(); i.hasNext();) {
            Node node = (Node)i.next();
            if (selectingRelationshipField(node, context)) {
                selectContext.useOuterJoins();
            }
            node.applyToQuery(readQuery, context); 
            selectContext.dontUseOuterJoins();
        }

        //indicate on the query if "return null if primary key null"
        //This means we want nulls returned if we expect an outer join
        if (this.hasOneToOneSelected(context)) {
            readQuery.setProperty("return null if primary key is null", Boolean.TRUE);
        } else {
            readQuery.removeProperty("return null if primary key is null");
        }

    }

    /**
     * INTERNAL
     **/
    public boolean hasOneToOneSelected(GenerationContext context) {
        // Iterate the select expression and return true if one of it has a
        // oneToOne selected.
        for (Iterator i = selectExpressions.iterator(); i.hasNext();) {
            Node node = (Node)i.next();
            if (hasOneToOneSelected(node, context)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * INTERNAL
     * Answer true if there is a one-to-one relationship selected.
     * This includes a chain of relationships.
     * True: SELECT employee.address FROM ..... //Simple 1:1
     * True: SELECT a.b.c.d FROM ..... //where a->b, b->c and c->d are all 1:1.
     * False: SELECT OBJECT(employee) FROM ..... //simple SELECT
     * False: SELECT phoneNumber.areaCode FROM ..... //direct-to-field
     **/
    private boolean hasOneToOneSelected(Node node, GenerationContext context) {
        //BUG 3240484: Not SELECTing 1:1 if it's in a COUNT
        if (node.isCountNode()) {
            return false;
        }

        if (node.isAggregateNode()) {
            // delegate to aggregate expression
            return hasOneToOneSelected(node.getLeft(), context);
        }
         
        if (node.isVariableNode()){
            return !nodeRefersToObject(node, context);
        }

        if (node.isConstructorNode()) {
            List args = ((ConstructorNode)node).getConstructorItems();
            for (Iterator i = args.iterator(); i.hasNext();) {
                Node arg = (Node)i.next();
                if (hasOneToOneSelected(arg, context)) {
                    return true;
                }
            }
            return false;
        }
      
        // check whether it is a direct-to-field mapping
        return !selectingDirectToField(node, context);
    }

    /**
    * Verify that the selected alias is a valid alias. If it's not valid,
    * an Exception will be thrown, likely EJBQLException.aliasResolutionException.
    *
    * Valid: SELECT OBJECT(emp) FROM Employee emp WHERE ...
    * Invalid: SELECT OBJECT(badAlias) FROM Employee emp WHERE ...
    */
    public void verifySelectedAlias(GenerationContext context) {
        for (Iterator i = selectExpressions.iterator(); i.hasNext();) {
            Node node = (Node)i.next();
            //if the node is a DotNode, there is no selected alias
            if (node.isDotNode()) {
                return;
            }
            node.resolveClass(context);
        }
    }

    /**
    * Answer true if the variable name given as argument is SELECTed.
    *
    * True: "SELECT OBJECT(emp) ...." & variableName = "emp"
    * False: "SELECT OBJECT(somethingElse) ..." & variableName = "emp"
    */
    public boolean isSelected(String variableName) {
        for (Iterator i = selectExpressions.iterator(); i.hasNext();) {
            Node node = (Node)i.next();
            //Make sure we've SELECted a VariableNode
            if (node.isVariableNode() && 
                ((VariableNode)node).getCanonicalVariableName().equals(variableName)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSelectNode() {
        return true;
    }

    /**
     * INTERNAL
     * Check the select expression nodes for a path expression starting with a
     * unqualified field access and if so, replace it by a qualified field
     * access. 
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        for (int i = 0; i < selectExpressions.size(); i++) {
            Node item = (Node)selectExpressions.get(i);
            selectExpressions.set(i, item.qualifyAttributeAccess(context));
        }
        return this;
    }

    /**
     * INTERNAL
     * Validate node.
     */
    public void validate(ParseTreeContext context) {
        for (Iterator i = selectExpressions.iterator(); i.hasNext(); ) {
            Node item = (Node)i.next();
            item.validate(context);
        }
    }

    /**
     * resolveClass: Answer the class associated with my left node.
     */
    public Class resolveClass(GenerationContext context) {
        return getReferenceClass(context);
    }
    
    /**
     * INTERNAL
     * Return a TopLink expression generated using the left node
     */
    public Expression generateExpression(GenerationContext context) {
        return null;
    }

  /**
   * Compute the Reference class for this query 
   * @param context 
   * @return the class this query is querying for
   */
    public Class getReferenceClass(GenerationContext context) {
        return getClassOfFirstVariable(context);
    }
    
    /** */
    private Class getClassOfFirstVariable(GenerationContext context) {
        Class clazz = null;
        String variable = getParseTree().getFromNode().getFirstVariable();
        ParseTreeContext parseTreeContext = context.getParseTreeContext();
        if (parseTreeContext.isRangeVariable(variable)) {
            String schema = parseTreeContext.schemaForVariable(variable);
            // variables is defines in a range variable declaration, so there
            // is a schema name for this variable
            clazz = parseTreeContext.classForSchemaName(schema, context);
        } else {
            // variable is defined in a JOIN clause, so there is a a defining
            // node for the variable
            Node path = parseTreeContext.pathForVariable(variable);
            clazz = path.resolveClass(context);
        }
        return clazz;
    }

    /**
    * INTERNAL
    * Answer true if a variable in the IN clause is SELECTed
    */
    public boolean isVariableInINClauseSelected(GenerationContext context) {
        for (Iterator i = selectExpressions.iterator(); i.hasNext();) {
            Node node = (Node)i.next();
        
            if (node.isVariableNode()) {
                String variableNameForLeft = ((VariableNode)node).getCanonicalVariableName();
                if (!context.getParseTreeContext().isRangeVariable(variableNameForLeft)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * INTERNAL
     * Answer true if this node refers to an object described later in the EJBQL
     * True: SELECT p FROM Project p
     * False: SELECT p.id FROM Project p
     **/
    public boolean nodeRefersToObject(Node node, GenerationContext context) {
        if (!node.isVariableNode()){
            return false;
        }
        String name = ((VariableNode)node).getCanonicalVariableName();
        String alias = context.getParseTreeContext().schemaForVariable(name);
        if (alias != null){
            ClassDescriptor descriptor = context.getSession().getDescriptorForAlias(alias);
            if (descriptor != null){
                return true;
            }
        }
        return false;
    }

    /**
     * INTERNAL
     */
    private boolean selectingRelationshipField(Node node, GenerationContext context) {
        if ((node == null) || !node.isDotNode()) {
            return false;
        }
        TypeHelper typeHelper = context.getParseTreeContext().getTypeHelper();
        Node path = node.getLeft();
        AttributeNode attribute = (AttributeNode)node.getRight();
        return typeHelper.isRelationship(path.getType(), 
                                         attribute.getAttributeName());
    }

    /**
     * INTERNAL
     * Answer true if the SELECT ends in a direct-to-field.
     * true: SELECT phone.areaCode
     * false: SELECT employee.address
     */
    private boolean selectingDirectToField(Node node, GenerationContext context) {

        if ((node == null) || !node.isDotNode()) {     
            return false;
        }
        return ((DotNode)node).endsWithDirectToField(context);
    }

    /** 
     * Returns the first select expression node.
     */
    private Node getFirstSelectExpressionNode() {
        return selectExpressions.size() > 0 ? 
            (Node)selectExpressions.get(0) : null;
    }

    /** */
    private boolean isSingleSelectExpression() {
        return selectExpressions.size() == 1;
    }
    
    
}
