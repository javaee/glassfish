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

import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.mappings.DatabaseMapping;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: This is the superclass for all Nodes.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Answer default answers for all method calls
 * <li> Delegate most responsibilities to the sub-classes
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class Node {
    private int line;
    private int column;
    protected Node left = null;
    protected Node right = null;
    private Object type;
    public boolean shouldGenerateExpression;

    /**
     * Return a new Node.
     */
    public Node() {
        super();
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
    }

    /**
     * INTERNAL
     * Add my expression semantics to the parentExpression. Each subclass will add a different expression and
     * thus will need to override this method
     */
    public Expression addToExpression(Expression parentExpression, GenerationContext context) {
        return parentExpression;
    }

    /**
     * INTERNAL
     * Get the string representation of this node.
     * By default return toString()
     */
    public String getAsString() {
        return toString();
    }

    /** 
     * INTERNAL 
     * Check the child node for an unqualified field access and if so,
     * replace it by a qualified field access.
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        if (left != null) {
            left = left.qualifyAttributeAccess(context);
        }
        if (right != null) {
            right = right.qualifyAttributeAccess(context);
        }
        return this;
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        // Nothing to be validated here, but delegate to the child nodes.
        if (left != null) {
            left.validate(context);
        }
        if (right != null) {
            right.validate(context);
        }
    }

    /**
     * INTERNAL 
     */
    public void validateParameter(ParseTreeContext context, Object contextType) {
        // nothing to be done
    }

    /**
     * INTERNAL
     * Generate an expression for the node. Each subclass will generate a different expression and
     * thus will need to override this method
     */
    public Expression generateExpression(GenerationContext context) {
        return null;
    }

    /**
     * INTERNAL
     * Return the left node
     */
    public Node getLeft() {
        return left;
    }

    /**
     * INTERNAL
     * Return the right node
     */
    public Node getRight() {
        return right;
    }

    /**
     * INTERNAL
     * Does this node have a left
     */
    public boolean hasLeft() {
        return getLeft() != null;
    }

    /**
     * INTERNAL
     * Does this node have a right
     */
    public boolean hasRight() {
        return getRight() != null;
    }

    /**
     * INTERNAL
     * Is this node an Aggregate node
     */
    public boolean isAggregateNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a Dot node
     */
    public boolean isDotNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this a literal node
     */
    public boolean isLiteralNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a Multiply node
     */
    public boolean isMultiplyNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a Not node
     */
    public boolean isNotNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this a Parameter node
     */
    public boolean isParameterNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a Divide node
     */
    public boolean isDivideNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a Plus node
     */
    public boolean isPlusNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a Minus node
     */
    public boolean isMinusNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a VariableNode
     */
    public boolean isVariableNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node an AttributeNode
     */
    public boolean isAttributeNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a CountNode
     */
    public boolean isCountNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a ConstructorNode
     */
    public boolean isConstructorNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this node a SubqueryNode
     */
    public boolean isSubqueryNode() {
        return false;
    }

    /**
     * INTERNAL
     * Is this an escape node
     */
    public boolean isEscape() {
        return false;// no it is not
    }

    /**
     * resolveAttribute(): Answer the name of the attribute which is represented by the receiver.
     * Subclasses should override this.
     */
    public String resolveAttribute() {
        return "";
    }

    /**
     * resolveClass: Answer the class associated with the content of this node. Default is to return null.
     * Subclasses should override this.
     */
    public Class resolveClass(GenerationContext context) {
        return null;
    }
    
    /**
     * resolveClass: Answer the class associated with the content of this node. Default is to return null.
     * Subclasses should override this.
     */
    public Class resolveClass(GenerationContext context, Class ownerClass) {
        return null;
    }

    /**
     * resolveMapping: Answer the mapping associated with the contained nodes.
     * Subclasses should override this.
     */
    public DatabaseMapping resolveMapping(GenerationContext context) {
        return null;
    }

    /**
     * resolveMapping: Answer the mapping associated with the contained nodes. Use the provided
     * class as the context.
     * Subclasses should override this.
     */
    public DatabaseMapping resolveMapping(GenerationContext context, Class ownerClass) {
        return null;
    }

    /**
     * INTERNAL
     * Set the left node to the passed value
     */
    public void setLeft(Node newLeft) {
        left = newLeft;
    }

    /**
     * INTERNAL
     * Set the right for this node
     */
    public void setRight(Node newRight) {
        right = newRight;
    }

    public int getLine() {
        return line;
    }
    
    public void setLine(int line) {
        this.line = line;
    }

    public int getColumn() {
        return column;
    }
    
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * INTERNAL
     * Return the type of this node.
     */
    public Object getType() {
        return type;
    }

    /**
     * INTERNAL
     * Set this node's type.
     */
    public void setType(Object type) {
        this.type = type;
    }

    /**
     * INTERNAL
     * Returns left.and(right) if both are defined.
     */
    public Expression appendExpression(Expression left, Expression right) {
        Expression expr = null;
        if (left == null) {
            expr = right;
        } else if (right == null) {
            expr = left;
        } else {
            expr = left.and(right);
        }
        return expr;
    }
    
    public String toString() {
        try {
            return toString(1);
        } catch (Throwable t) {
            return t.toString();
        }
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(toStringDisplayName());
        buffer.append("\r\n");
        toStringIndent(indent, buffer);
        if (hasLeft()) {
            buffer.append("Left: " + getLeft().toString(indent + 1));
        } else {
            buffer.append("Left: null");
        }

        buffer.append("\r\n");
        toStringIndent(indent, buffer);
        if (hasRight()) {
            buffer.append("Right: " + getRight().toString(indent + 1));
        } else {
            buffer.append("Right: null");
        }
        return buffer.toString();
    }

    public String toStringDisplayName() {
        return getClass().toString().substring(getClass().toString().lastIndexOf('.') + 1, getClass().toString().length());
    }

    public void toStringIndent(int indent, StringBuffer buffer) {
        for (int i = 0; i < indent; i++) {
            buffer.append("  ");
        }
        ;
    }
}
