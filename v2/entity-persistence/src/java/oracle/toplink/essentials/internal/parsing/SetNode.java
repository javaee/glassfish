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
import oracle.toplink.essentials.queryframework.*;
import java.util.*;

/**
 * INTERNAL:
 * This node holds a list of all the updates that will occur in an Update Query.
 * Slightly differnt from other nodes since holds more than two children in a list.
 */
public class SetNode extends MajorNode {
    private List assignmentNodes = null;

    public SetNode() {
        super();
        assignmentNodes = new Vector();
    }

    /**
     * Iterate through the updates in this query and build expressions for them.  Set the
     * built expressions on the query.
     */
    public void addUpdatesToQuery(UpdateAllQuery theQuery, GenerationContext context) {
        Iterator iterator = assignmentNodes.iterator();
        while (iterator.hasNext()) {
            EqualsAssignmentNode node = (EqualsAssignmentNode)iterator.next();
            Expression leftExpression = getExpressionForNode(node.getLeft(), theQuery.getReferenceClass(), context);
            Expression rightExpression = getExpressionForNode(node.getRight(), theQuery.getReferenceClass(), context);
            theQuery.addUpdate(leftExpression, rightExpression);
        }
    }

    /** 
     * INTERNAL 
     * Check the update item node for a path expression starting with a
     * unqualified field access and if so, replace it by a qualified field
     * access. 
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        for (Iterator i = assignmentNodes.iterator(); i.hasNext(); ) {
            Node item = (Node)i.next();
            item.qualifyAttributeAccess(context);
        }
        return this;
    }
    
    /**
     * INTERNAL
     * Validate node.
     */
    public void validate(ParseTreeContext context) {
        for (Iterator i = assignmentNodes.iterator(); i.hasNext(); ) {
            Node item = (Node)i.next();
            item.validate(context);
        }
    }

    /**
     * Create an expression to represent one of the nodes on a SetToNode.
     * We will assume that set_to nodes change elements that are direct mappings on the reference
     * class of the query.
     */
    protected Expression getExpressionForNode(Node node, Class referenceClass, GenerationContext context) {
        Expression expression = null;
        if (node.isAttributeNode()) {
            // look up a preexisting expression based on the reference class of the query.
            String classVariable = context.getParseTreeContext().getVariableNameForClass(referenceClass, context);
            expression = context.expressionFor(classVariable);
            if (expression == null) {
                expression = new ExpressionBuilder();
                context.addExpression(expression, classVariable);
            }
            expression = node.addToExpression(expression, context);
        } else {
            expression = node.generateExpression(context);
        }
        return expression;
    }

    /**
     * INTERNAL
     */
    public void setAssignmentNodes(List nodes) {
        assignmentNodes = nodes;
    }

}
