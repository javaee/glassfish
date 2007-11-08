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

import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.expressions.*;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.mappings.DirectToFieldMapping;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: This node represents an 'DOT' (i.e. '.') on the input
 * stream. The left and right will depend on the input stream.
 * <p><b>Responsibilities</b>:<ul>
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class DotNode extends LogicalOperatorNode {

    private Object enumConstant;

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery.isReportQuery()){
            ReportQuery reportQuery = (ReportQuery)theQuery;
            reportQuery.addAttribute(resolveAttribute(), generateExpression(context));
            reportQuery.dontRetrievePrimaryKeys();
        }
    }

    /** 
     * INTERNAL 
     * Check the left child node for an unqualified field access. The method
     * delegates to the left most expression of multi-navigation path
     * expression. 
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        if (getLeft() != null) {
            setLeft(getLeft().qualifyAttributeAccess(context));
        }
        return this;
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     * Check for enum literals.
     */
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        String name = ((AttributeNode)right).getAttributeName();
        // check for fully qualified type names
        Node leftMost = getLeftMostNode();
        if (isDeclaredVariable(leftMost, context)) {
            left.validate(context);
            checkNavigation(left, context);
            Object type = typeHelper.resolveAttribute(left.getType(), name);
            if (type == null) {
                // could not resolve attribute
                throw EJBQLException.unknownAttribute(
                    context.getQueryInfo(), right.getLine(), right.getColumn(), 
                    name, typeHelper.getTypeName(left.getType()));
            }
            setType(type);
            right.setType(type);
        } else {
            // Check for enum literal access
            String typeName = left.getAsString();
            Object type = resolveEnumTypeName(typeName, typeHelper);
            if ((type != null) && typeHelper.isEnumType(type)) {
                enumConstant = typeHelper.resolveEnumConstant(type, name);
                if (enumConstant == null) {
                    throw EJBQLException.invalidEnumLiteral(context.getQueryInfo(),
                        right.getLine(), right.getColumn(), typeName, name);
                }
            } else {
                // left most node is not an identification variable and
                // dot expression doe not denote an enum literal access =>
                // unknown identification variable
                throw EJBQLException.aliasResolutionException(
                    context.getQueryInfo(), leftMost.getLine(), 
                    leftMost.getColumn(), leftMost.getAsString());
            }
            setType(type);
            right.setType(type);
        }
    }

    /** 
     * INTERNAL
     * Checks whether the left hand side of this dot node is navigable.
     */
    private void checkNavigation(Node node, ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        // Checks whether the type of the dot node allows a navigation.
        Object type = node.getType();
        if (!typeHelper.isEntityClass(type) && 
            !typeHelper.isEmbeddable(type) &&
            !typeHelper.isEnumType(type)) {
            throw EJBQLException.invalidNavigation(
                context.getQueryInfo(), node.getLine(), node.getColumn(),
                this.getAsString(), node.getAsString(), 
                typeHelper.getTypeName(type));
        }
        // Special check to disallow collection valued relationships
        if (node.isDotNode()) {
            Node left = node.getLeft();
            AttributeNode right = (AttributeNode)node.getRight();
            if (typeHelper.isCollectionValuedRelationship(
                    left.getType(), right.getAttributeName())) {
                throw EJBQLException.invalidCollectionNavigation(
                    context.getQueryInfo(), right.getLine(), right.getColumn(),
                    this.getAsString(), right.getAttributeName());
            }
        }
    }
    
    /** */
    private boolean isDeclaredVariable(Node node, ParseTreeContext context) {
        if (node.isVariableNode()) {
            String name = ((VariableNode)node).getCanonicalVariableName();
            return context.isVariable(name);
        }
        return false;
    }

    /**
     * INTERNAL
     * Return a TopLink expression by getting the required variables using the
     * left and right nodes
     * "emp.address.city" = builder.get("address").get("city")
     */
    public Expression generateExpression(GenerationContext context) {
        Node left = getLeft();
        Node right = getRight();

        if (enumConstant != null) {
            // enum literal access
            return new ConstantExpression(enumConstant, new ExpressionBuilder());
        } else {
            // Get the left expression
            Expression whereClause = left.generateExpression(context);
            
            // Calculate the mapping and pass it to the right expression
            if (right.isAttributeNode()) {
                ((AttributeNode)right).setMapping(resolveMapping(context));
            }
            
            // Or it with whatever the right expression is
            whereClause = right.addToExpression(whereClause, context);
            
            // and return the expression...
            return whereClause;
        }
    }

    /**
     * INTERNAL
     * Yes, this is a dot node
     */
    public boolean isDotNode() {
        return true;
    }

    /**
     * INTERNAL
     * ():
     * Answer true if the SELECTed node has a left and right, and the right represents
     * a direct-to-field mapping.
     */
    public boolean endsWithDirectToField(GenerationContext context) {
        DatabaseMapping mapping = resolveMapping(context);
        return (mapping != null) && mapping.isDirectToFieldMapping();
    }

    /**
     * INTERNAL
     * Returns the attribute type if the right represents a direct-to-field mapping.
     */
    public Class getTypeOfDirectToField(GenerationContext context) {
        DatabaseMapping mapping = resolveMapping(context);
        if ((mapping != null) && mapping.isDirectToFieldMapping()) {
            return ((DirectToFieldMapping)mapping).getAttributeClassification();
        }
        return null;
    }

    /**
     * INTERNAL
     * ():
     * Answer true if the node has a left and right, and the right represents
     * a collection mapping.
     */
    public boolean endsWithCollectionField(GenerationContext context) {
        DatabaseMapping mapping = resolveMapping(context);
        return (mapping != null) && mapping.isCollectionMapping();
    }

    /**
     * INTERNAL
     * Answer the name of the attribute which is represented by the receiver's
     * right node.
     */
    public String resolveAttribute() {
        return ((AttributeNode)getRight()).getAttributeName();
    }

    /**
     * INTERNAL
     * Answer the mapping resulting from traversing the receiver's nodes
     */
    public DatabaseMapping resolveMapping(GenerationContext context) {
        Class leftClass = getLeft().resolveClass(context);
        return getRight().resolveMapping(context, leftClass);
    }

    /**
    * resolveClass: Answer the class which results from traversing the mappings for the receiver's nodes
    */
    public Class resolveClass(GenerationContext context) {
        Class leftClass = getLeft().resolveClass(context);
        return getRight().resolveClass(context, leftClass);
    }
    
    /**
     * INTERNAL
     * Get the string representation of this node.
     */
    public String getAsString() {
        return left.getAsString() + "." + right.getAsString();
    }

    /**
     * INTERNAL
     * Return the left most node of a dot expr, so return 'a' for 'a.b.c'.
     */
    private Node getLeftMostNode() {
        return left.isDotNode() ? ((DotNode)left).getLeftMostNode() : left;
    }

    /**
     * INTERNAL
     * Returns the type representation for the specified type name. The method
     * looks for inner classes if it cannot resolve the type name.
     */
    private Object resolveEnumTypeName(String name, TypeHelper helper) {
        Object type = helper.resolveTypeName(name);
        if (type == null) {
            // check for inner enum type
            int index = name.lastIndexOf('.');
            if (index != -1) {
                name = name.substring(0, index) + '$' + name.substring(index+1);
                type = helper.resolveTypeName(name);
            }
        }
        return type;
    }
}
