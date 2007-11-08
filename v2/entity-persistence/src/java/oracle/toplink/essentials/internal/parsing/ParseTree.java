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


// Java stuff
import java.util.*;

// TopLink stuff
import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.localization.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL
 * <p><b>Purpose</b>: A ParseTree contains Node(s). This contains a root Node and provides traversal utilities.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Add parameters to the query
 * <li> Generate an expression for the query
 * <li> Answer true if the tree has parameters
 * <li> Maintain the primary class name for the query
 * <li> Maintain the root of the parse tree
 * <li> Maintain the context for the parse tree
 * <li> Maintainthe distinct state for the parse tree
 * <li> Print the contents of the parse tree on a string
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class ParseTree {
    private ParseTreeContext context;
    private QueryNode queryNode;
    private FromNode fromNode;
    private SetNode setNode;
    private WhereNode whereNode;
    private OrderByNode orderByNode = null;
    private GroupByNode groupByNode = null;
    private HavingNode havingNode = null;
    private ClassLoader classLoader = null;
    private short distinctState = ObjectLevelReadQuery.UNCOMPUTED_DISTINCT;
    private boolean validated = false;
    private Set unusedVariables = null;

    /**
     * Return a new ParseTree.
     */
    public ParseTree() {
        super();
    }

    /**
     * INTERNAL
     * Returns a DatabaseQuery instance for this ParseTree.
     */
    public DatabaseQuery createDatabaseQuery() {
        return (queryNode == null) ? null :
            queryNode.createDatabaseQuery(context);
    }

    /**
      * INTERNAL
      * Adjust the reference class of the passed query if necessary
      *
      * Need to test this for Employee, employee.getAddress(), report query
      */
    public void adjustReferenceClassForQuery(DatabaseQuery theQuery, GenerationContext generationContext) {
        Class referenceClass = getReferenceClass(theQuery, generationContext);
        if ((referenceClass != null) && (referenceClass != theQuery.getReferenceClass())) {
            if (theQuery.isObjectLevelReadQuery()) {
                // The referenceClass needs to be changed.
                // This should only happen in an ejbSelect...
                ((ObjectLevelReadQuery)theQuery).setReferenceClass(referenceClass);
                generationContext.setBaseQueryClass(referenceClass);
                ((ObjectLevelReadQuery)theQuery).changeDescriptor(generationContext.getSession());
            } else if (theQuery.isUpdateAllQuery()) {
                ((UpdateAllQuery)theQuery).setReferenceClass(referenceClass);
            } else if (theQuery.isDeleteAllQuery()) {
                ((DeleteAllQuery)theQuery).setReferenceClass(referenceClass);
            }
        }
    }

    /**
     * INTERNAL
     * Initialize the base expression in the generation context.
     */
    public void initBaseExpression(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        String variable = getFromNode().getFirstVariable();
        ParseTreeContext context = generationContext.getParseTreeContext();
        if (context.isRangeVariable(variable)) {
            Class referenceClass = theQuery.getReferenceClass();
            // Create a new expression builder for the reference class
            ExpressionBuilder builder = new ExpressionBuilder(referenceClass);
            // Use the expression builder as the default expression builder for the query
            theQuery.setExpressionBuilder(builder);
            // Add the expression builder to the expression cache in the context
            generationContext.setBaseExpression(variable, builder);
        } else {
            // Get the declaring node for the variable
            Node path = context.pathForVariable(variable);
            // Get the ExpressionBuilder of the range variable for the path
            Class baseClass = getBaseExpressionClass(path, generationContext);
            // Use the base ExpressionBuilder as the default for the query
            theQuery.setExpressionBuilder(new ExpressionBuilder(baseClass));
            // and change the reference class accordingly
            theQuery.setReferenceClass(baseClass);
            theQuery.changeDescriptor(generationContext.getSession());
            generationContext.setBaseQueryClass(baseClass);
            // Set the node expression as base expression
            generationContext.setBaseExpression(
                variable, path.generateExpression(generationContext));
        }
    }

    /**
     * INTERNAL
     * Initialize the base expression in the generation context.
     */
    public void initBaseExpression(ModifyAllQuery theQuery, GenerationContext generationContext) {
        ModifyNode queryNode = (ModifyNode)getQueryNode();
        String variable = queryNode.getCanonicalAbstractSchemaIdentifier();
        Class referenceClass = theQuery.getReferenceClass();
        // Create a new expression builder for the reference class
        ExpressionBuilder builder = new ExpressionBuilder(referenceClass);
        // Use the expression builder as the default expression builder for the query
        theQuery.setExpressionBuilder(builder);
        // Add the expression builder to the expression cache in the context
        generationContext.setBaseExpression(variable, builder);        
    }
    
    /** */
    private Class getBaseExpressionClass(Node node, GenerationContext generationContext) {
        ParseTreeContext context = generationContext.getParseTreeContext();
        Class clazz = null;
        if (node == null) {
            clazz = null;
        } else if (node.isDotNode()) {
            // DotNode: delegate to left
            clazz = getBaseExpressionClass(node.getLeft(), generationContext);
        } else if (node.isVariableNode()) {
            // VariableNode
            String variable = ((VariableNode)node).getVariableName();
            if (!context.isRangeVariable(variable)) {
                Node path = context.pathForVariable(variable);
                // Variable is defined in JOIN/IN clause => 
                // return the Class from its definition
                clazz = getBaseExpressionClass(path, generationContext);
            } else {
                // Variable is defined in range variable decl =>
                // return its class
                String schema = context.schemaForVariable(variable);
                if (schema != null) {
                    clazz = context.classForSchemaName(schema, generationContext);
                }
            }
        }
        return clazz;
    }

    /** 
     * INTERNAL
     * Validate the parse tree.
     */
    protected void validate(AbstractSession session, ClassLoader classLoader) {
        validate(new TypeHelperImpl(session, classLoader));
    }

    /** 
     * INTERNAL
     * Validate the parse tree.
     */
    public void validate(TypeHelper typeHelper) {
        ParseTreeContext context = getContext();
        context.setTypeHelper(typeHelper);
        validate(context);
    }
    
    /** 
     * INTERNAL
     * Validate the parse tree.
     */
    public void validate(ParseTreeContext context) {
        if (validated) {
            // already validated => return
            return;
        }
        
        validated = true;
        context.enterScope();
        if (fromNode != null) {
            fromNode.validate(context);
        }
        queryNode.validate(context);
        qualifyAttributeAccess(context);
        if (setNode != null) {
            setNode.validate(context);
        }
        if (whereNode != null) {
            whereNode.validate(context);
        }
        if (hasOrderBy()) {
            orderByNode.validate(context, (SelectNode)queryNode);
        }
        if (hasGroupBy()) {
            groupByNode.validate(context, (SelectNode)queryNode);
        }
        if (hasHaving()) {
            havingNode.validate(context, groupByNode);
        }
        // store the set od unused variable for later use
        unusedVariables = context.getUnusedVariables();
        context.leaveScope();
    }

    /** 
     * INTERNAL
     * This method handles any unqualified field access in bulk UPDATE and
     * DELETE statements. A UPDATE or DELETE statement may not define an
     * identification variable. In this case any field accessed from the
     * current class is not qualified with an identification variable, e.g.
     *   UPDATE Customer SET name = :newname
     * The method goes through the expressions of the SET clause and the WHERE
     * clause of such an DELETE and UPDATE statement and qualifies the field
     * access using the abstract schema name as qualifier.
     */
    protected void qualifyAttributeAccess(ParseTreeContext context) {
        if ((queryNode.isUpdateNode() || queryNode.isDeleteNode()) &&
            ((ModifyNode)queryNode).getAbstractSchemaIdentifier() == null) {
            if (setNode != null) {
                setNode.qualifyAttributeAccess(context);
            }
            if (whereNode != null) {
                whereNode.qualifyAttributeAccess(context);
            }
        }
    }

    /**
     * INTERNAL
     * Add the ordering to the passed query
     */
    public void addOrderingToQuery(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        if (hasOrderBy()) {
            ((OrderByNode)getOrderByNode()).addOrderingToQuery(theQuery, generationContext);
        }
    }

    /**
     * INTERNAL
     * Add the grouping to the passed query
     */
    public void addGroupingToQuery(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        if (hasGroupBy()) {
            ((GroupByNode)getGroupByNode()).addGroupingToQuery(theQuery, generationContext);
        }
    }

    /**
     * INTERNAL
     * Add the having to the passed query
     */
    public void addHavingToQuery(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        if (hasHaving()) {
            ((HavingNode)getHavingNode()).addHavingToQuery(theQuery, generationContext);
        }
    }

    /**
     * INTERNAL
     */
    public void addNonFetchJoinAttributes(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        ParseTreeContext context = generationContext.getParseTreeContext();
        for (Iterator i = unusedVariables.iterator(); i.hasNext();) {
            String variable = (String)i.next();
            Expression expr = null;
            if (!context.isRangeVariable(variable)) {
                Node path = context.pathForVariable(variable);
                expr = path.generateExpression(generationContext);
                theQuery.addNonFetchJoinedAttribute(expr);
            } else {
                // unused range variable => not supported yet
                throw EJBQLException.notYetImplemented(context.getQueryInfo(),
                    "Variable [" + variable + "] is defined in a range variable declaration, but not used in the rest of the query.");
            }
        }
    }

    /**
     * INTERNAL
     * Add the updates to the passed query
     */
    public void addUpdatesToQuery(UpdateAllQuery theQuery, GenerationContext generationContext) {
        if (getSetNode() != null) {
            ((SetNode)getSetNode()).addUpdatesToQuery(theQuery, generationContext);
        }
    }

    /**
     * INTERNAL
     * Add parameters to the query
     */
    public void addParametersToQuery(DatabaseQuery query) {
        //Bug#4646580  Add arguments to query
        if (context.hasParameters()) {
            TypeHelper typeHelper = context.getTypeHelper();
            for (Iterator i = context.getParameterNames().iterator(); i.hasNext();) {
                String param = (String)i.next();
                Object type = context.getParameterType(param);
                Class clazz = typeHelper.getJavaClass(type);
                if (clazz == null) {
                    clazz = Object.class;
                }
                query.addArgument(param, clazz);
            }
        }
    }
    
    /**
     * INTERNAL
     * Apply the select or update to the passed query.
     * If there is a single attribute being selected, add it to the query result set
     * If an aggregate is being used, add it to the query result set
     */
    public void applyQueryNodeToQuery(DatabaseQuery theQuery, GenerationContext generationContext) {
        getQueryNode().applyToQuery(theQuery, generationContext);
    }

    /**
     * INTERNAL
     * Build the context to be used when generating the expression from the parse tree
     */
    public GenerationContext buildContext(DatabaseQuery query, AbstractSession sessionForContext) {
        if (query.isObjectLevelReadQuery()) {
            return buildContextForReadQuery(sessionForContext);
        } else if (query.isUpdateAllQuery() || query.isDeleteAllQuery()) {
            return new GenerationContext(getContext(), sessionForContext, this);
        }
        return null;
    }

    /**
     * INTERNAL
     * Build the context to be used when generating the expression from the parse tree
     */
    public GenerationContext buildContextForReadQuery(AbstractSession sessionForContext) {
        return new SelectGenerationContext(getContext(), sessionForContext, this);
    }

    /**
     * INTERNAL
     * Build a context for the expression generation
     */
    public Expression generateExpression(DatabaseQuery readQuery, GenerationContext generationContext) {
        Expression selectExpression = getQueryNode().generateExpression(generationContext);
        if (getWhereNode() == null) {
            return selectExpression;
        }
        Expression whereExpression = getWhereNode().generateExpression(generationContext);

        selectExpression = getQueryNode().generateExpression(generationContext);
        if (selectExpression != null) {
            whereExpression = selectExpression.and(whereExpression);
        }
        return whereExpression;
    }

    /**
     * Return the context for this parse tree
     */
    public ParseTreeContext getContext() {
        return context;
    }

    /**
     * INTERNAL
     * Return the FROM Node
     */
    public FromNode getFromNode() {
        return fromNode;
    }

    /**
     * INTERNAL
     * Return a class loader
     * @return java.lang.ClassLoader
     */
    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            return oracle.toplink.essentials.internal.helper.ConversionManager.getDefaultManager().getLoader();
        } else {
            return classLoader;
        }
    }

    /**
     * INTERNAL
     * Return the OrderByNode
     */
    public OrderByNode getOrderByNode() {
        return orderByNode;
    }

    /**
     * INTERNAL
     * Return the GroupByNode
     */
    public GroupByNode getGroupByNode() {
        return groupByNode;
    }

    /**
     * INTERNAL
     * Return the HavingNode
     */
    public HavingNode getHavingNode() {
        return havingNode;
    }

    /**
     * getReferenceClass(): Answer the class which will be the reference class for the query.
     * Resolve this using the node parsed from the "SELECT" of the EJBQL query string
     */
    public Class getReferenceClass(DatabaseQuery query, GenerationContext generationContext) {
        if (getQueryNode() == null) {
            return null;
        }
        return getQueryNode().getReferenceClass(generationContext);
    }

    /**
     * INTERNAL
     * Return the root node for the tree
     */
    public QueryNode getQueryNode() {
        return queryNode;
    }

    /**
     * INTERNAL
     * Return the set node for the tree
     */
    public SetNode getSetNode() {
        return setNode;
    }

    /**
     * INTERNAL
     * Return the Where node
     */
    public WhereNode getWhereNode() {
        return whereNode;
    }

    /**
     * INTERNAL
     * Return the DISTINCT state for the tree
     */
    public short getDistinctState() {
        return distinctState;
    }

    /**
     * INTERNAL
     * Does this EJBQL have an Ordering Clause
     */
    public boolean hasOrderBy() {
        return getOrderByNode() != null;
    }

    /**
     * INTERNAL
     * Does this EJBQL have a Grouping Clause
     */
    public boolean hasGroupBy() {
        return getGroupByNode() != null;
    }

    /**
     * INTERNAL
     * Does this EJBQL have a Having Clause
     */
    public boolean hasHaving() {
        return getHavingNode() != null;
    }

    /**
     * INTERNAL:
     * Set the class loader for this parse tree
     * @param loader 
     */
    public void setClassLoader(ClassLoader loader){
        this.classLoader = loader;
    }

    /**
     * INTERNAL
     * Set the context for this parse tree
     */
    public void setContext(ParseTreeContext newContext) {
        context = newContext;
    }

    /**
     * INTERNAL
     * Set the FROM node for the query
     */
    public void setFromNode(FromNode fromNode) {
        this.fromNode = fromNode;
    }

    /**
     * INTERNAL
     * Set the Order by node
     */
    public void setOrderByNode(OrderByNode newOrderByNode) {
        orderByNode = newOrderByNode;
    }

    /**
     * INTERNAL
     * Set the Group by node
     */
    public void setGroupByNode(GroupByNode newGroupByNode) {
        groupByNode = newGroupByNode;
    }

    /**
     * INTERNAL
     * Set the Having node
     */
    public void setHavingNode(HavingNode newHavingNode) {
        havingNode = newHavingNode;
    }

    public void setSelectionCriteriaForQuery(DatabaseQuery theQuery, GenerationContext generationContext) {
        theQuery.setSelectionCriteria(generateExpression(theQuery, generationContext));
    }

    /**
     * INTERNAL
     * Set the Select node
     */
    public void setQueryNode(QueryNode newQueryNode) {
        queryNode = newQueryNode;
    }

    /**
     * INTERNAL
     * Set the Where node
     */
    public void setSetNode(SetNode newSetNode) {
        setNode = newSetNode;
    }

    /**
     * INTERNAL
     * Set the Where node
     */
    public void setWhereNode(WhereNode newWhereNode) {
        whereNode = newWhereNode;
    }

    /**
     * INTERNAL
     * Set the DISTINCT state for the tree
     */
    public void setDistinctState(short newDistinctState) {
        distinctState = newDistinctState;
    }

    /**
     * INTERNAL
     * Print the contents of the parse tree on a string
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getContext().toString());
        return ToStringLocalization.buildMessage("context", (Object[])null) + " " + buffer.toString();
    }

    /**
     * INTERNAL
     * Verify that the alias in the SELECT is valid.
     * Invalid: SELECT OBJECT(badAlias) FROM Employee employee....
     * Valid: SELECT OBJECT(employee) FROM Employee employee....
     */
    public void verifySelect(DatabaseQuery theQuery, GenerationContext generationContext) {
        if (theQuery.isObjectLevelReadQuery()) {
            //verify the selected alias,
            //this will throw an error if the alias is bad
            ((SelectNode)getQueryNode()).verifySelectedAlias(generationContext);
        }
    }

    /**
     * INTERNAL
     * Answer true if DISTINCT has been chosen.
     */
    public boolean usesDistinct() {
        return distinctState == ObjectLevelReadQuery.USE_DISTINCT;
    }
}
