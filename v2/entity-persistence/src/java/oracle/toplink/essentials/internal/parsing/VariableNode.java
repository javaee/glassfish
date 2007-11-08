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

import java.util.*;

// TopLink imports
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: The Superclass for for typed variables, local variables and remote variables
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for an AND in EJBQL
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class VariableNode extends Node {

    /** */
    private String variableName;
    
    /** */
    private String canonicalName;

    /**
     * VariableNode constructor comment.
     */
    public VariableNode() {
        super();
    }

    public VariableNode(String newVariableName) {
        setVariableName(newVariableName);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String newVariableName) {
        variableName = newVariableName;
        canonicalName = IdentificationVariableDeclNode.calculateCanonicalName(newVariableName);
    }

    /** */
    public String getCanonicalVariableName() {
        return canonicalName;
    }

    /**
     * INTERNAL
     * Is this node a VariableNode
     */
    public boolean isVariableNode() {
        return true;
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext generationContext) {
        String name = getCanonicalVariableName();
        ParseTreeContext context = generationContext.getParseTreeContext();
        if (theQuery instanceof ReportQuery) {
            ReportQuery reportQuery = (ReportQuery)theQuery;
            Expression expression = generationContext.expressionFor(name);
            if (expression == null) {
                expression = generateExpression(generationContext);
            }
            addAttributeWithFetchJoins(reportQuery, expression, generationContext);
        } else {
            addFetchJoins(theQuery, generationContext);
        }
    }

    /**
     * INTERNAL
     * Add the variable as ReportQuery item. The method checks for any JOIN
     * FETCH nodes of the current variable and adds them as part of the
     * ReportQuery item.
     */
    private void addAttributeWithFetchJoins(ReportQuery reportQuery, 
                                            Expression expression, 
                                            GenerationContext context) {
        String name = getCanonicalVariableName();
        List fetchJoinNodes = context.getParseTreeContext().getFetchJoins(name);
        if (fetchJoinNodes == null) {
            reportQuery.addAttribute(name, expression);
        } else {
            List fetchJoinExprs = new ArrayList(fetchJoinNodes.size());
            for (Iterator i = fetchJoinNodes.iterator(); i.hasNext(); ) {
                Node node = (Node)i.next();
                fetchJoinExprs.add(node.generateExpression(context));
            }
            reportQuery.addItem(name, expression, fetchJoinExprs);
        }
    }

    /**
     * INTERNAL
     * Check for any JOIN FETCH nodes of the current variable and add them as
     * joined attributes. This method is called in case of a non ReportQuery
     * instance.
     */
    private void addFetchJoins(ObjectLevelReadQuery theQuery, 
                               GenerationContext context) {
        String name = getCanonicalVariableName();
        List fetchJoinNodes = context.getParseTreeContext().getFetchJoins(name);
        if (fetchJoinNodes != null) {
            for (Iterator i = fetchJoinNodes.iterator(); i.hasNext(); ) {
                Node node = (Node)i.next();
                theQuery.addJoinedAttribute(node.generateExpression(context));
            }
        }
    }

    /** 
     * INTERNAL 
     * This node represent an unqualified field access in the case the method
     * is called and the variableName is not defined as identification variable.
     * The method returns a DotNode representing a qualified field access with
     * the base variable as left child node. The right child node is an
     * AttributeNode using the variableName as field name.
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        return context.isVariable(variableName) ? this :
            (Node)context.getNodeFactory().newQualifiedAttribute(
                getLine(), getColumn(), context.getBaseVariable(), variableName);
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        String name = getCanonicalVariableName();
        if (context.isRangeVariable(name)) {
            String schema = context.schemaForVariable(name);
            setType(typeHelper.resolveSchema(schema));
        } else {
            Node path = context.pathForVariable(name);
            if (path == null) {
                throw EJBQLException.aliasResolutionException(
                    context.getQueryInfo(), getLine(), getColumn(), name);
            } else {
                setType(path.getType());
            }
        }
        context.usedVariable(name);
        if (context.isDeclaredInOuterScope(name)) {
            context.registerOuterScopeVariable(name);
        }
    }

    public Expression generateBaseBuilderExpression(GenerationContext context) {
        //create builder, and add it, and answer it
        //BUG 3106877: Need to create builder using the actual class (if using parallel expressions)
        if (context.useParallelExpressions()) {
            return new ExpressionBuilder(this.resolveClass(context));
        } else {
            return new ExpressionBuilder();
        }
    }

    public Expression generateExpression(GenerationContext generationContext) {
        Expression myExpression = null;
        String name = getCanonicalVariableName();
        
        //is there a cached Expression?
        myExpression = generationContext.expressionFor(name);
        if (myExpression != null) {
            return myExpression;
        }

        //Either I have an alias type, or I'm an IN declaration
        if (generationContext.getParseTreeContext().isRangeVariable(name)) {
            myExpression = generateBaseBuilderExpression(generationContext);
        } else {
            myExpression = generateExpressionForAlias(generationContext);
        }

        generationContext.addExpression(myExpression, name);
        return myExpression;
    }

    public Expression generateExpressionForAlias(GenerationContext context) {
        // BUG 3105651: Verify if we need to resolve this alias, or just use
        // an empty ExpressionBuilder. See OrderByItemNode.generateExpression()
        // for more details
        if (context.getParseTree().getQueryNode().isSelectNode() && context.shouldCheckSelectNodeBeforeResolving() && (((SelectNode)context.getParseTree().getQueryNode()).isSelected(this.getCanonicalVariableName()))) {
            return new ExpressionBuilder();
        }

        Node nodeForAlias = getNodeForAlias(context);

        //assume that if there is no node available for the given variable, then
        //there must be an alias mismatch. Assume they know their attribute names better
        //than their alias names. - JGL
        if (nodeForAlias == null) {
            throw EJBQLException.aliasResolutionException(
                context.getParseTreeContext().getQueryInfo(), 
                getLine(), getColumn(), getVariableName());
        }

        //create builder, and answer it
        return nodeForAlias.generateExpression(context);
    }

    public Node getNodeForAlias(GenerationContext context) {
        //Node node = context.getParseTreeContext().nodeForIdentifier(getCanonicalVariableName());
        //return node != null ? ((IdentificationVariableDeclNode)node).getPath() : null;
        return context.getParseTreeContext().pathForVariable(getCanonicalVariableName());
    }

    /**
     * isAlias: Answer true if this variable represents an alias in the FROM clause.
     * i.e. "FROM Employee emp" declares "emp" as an alias
     */
    public boolean isAlias(GenerationContext context) {
        return isAlias(context.getParseTreeContext());
    }

    public boolean isAlias(ParseTreeContext context) {
        String classNameForAlias = context.schemaForVariable(getCanonicalVariableName());
        return classNameForAlias != null;
    }

    /**
     * resolveClass: Answer the class which corresponds to my variableName. This is the class for
     * an alias, where the variableName is registered to an alias.
     */
    public Class resolveClass(GenerationContext generationContext) {
        Class clazz = null;
        String name = getCanonicalVariableName();
        ParseTreeContext context = generationContext.getParseTreeContext();
        if (context.isRangeVariable(name)) {
            String schema = context.schemaForVariable(name);
            clazz = context.classForSchemaName(schema, generationContext);
        } else {
            DotNode path = (DotNode)context.pathForVariable(name);
            if (path == null) {
                throw EJBQLException.aliasResolutionException(
                    context.getQueryInfo(), getLine(), getColumn(), name);
            } else {
                clazz = path.resolveClass(generationContext);
            }
        }
        return clazz;
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer();
        toStringIndent(indent, buffer);
        buffer.append(toStringDisplayName() + "[" + getVariableName() + "]");
        return buffer.toString();
    }

    /**
     * INTERNAL
     * Get the string representation of this node.
     */
    public String getAsString() {
        return getVariableName();
    }

}
