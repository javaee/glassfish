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

import oracle.toplink.essentials.expressions.*;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent an OR
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for an OR
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class OrNode extends LogicalOperatorNode {

    private Set leftOuterScopeVariables = null;
    private Set rightOuterScopeVariables = null;

    /**
     * Return a new OrNode.
     */
    public OrNode() {
        super();
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        Set saved = context.getOuterScopeVariables();
        if (left != null) {
            context.resetOuterScopeVariables();
            left.validate(context);
            leftOuterScopeVariables = context.getOuterScopeVariables();
        }
        if (right != null) {
            context.resetOuterScopeVariables();
            right.validate(context);
            rightOuterScopeVariables = context.getOuterScopeVariables();
        }
        context.resetOuterScopeVariables(saved);
        if ((left != null) && (right != null)) {
            left.validateParameter(context, right.getType());
            right.validateParameter(context, left.getType());
        }
        
        TypeHelper typeHelper = context.getTypeHelper();
        setType(typeHelper.getBooleanType());
    }

    /**
     * INTERNAL
     * Return a TopLink expression by 'OR'ing the expressions from the left and right nodes
     */
    public Expression generateExpression(GenerationContext context) {
        // Get the left expression
        Expression leftExpr = getLeft().generateExpression(context);
        leftExpr = appendOuterScopeVariableJoins(
            leftExpr, leftOuterScopeVariables, context);

        Expression rightExpr = getRight().generateExpression(context);
        rightExpr = appendOuterScopeVariableJoins(
            rightExpr, rightOuterScopeVariables, context);
        
        // Or it with whatever the right expression is
        return leftExpr.or(rightExpr);
    }

    /**
     * INTERNAL 
     */
    private Expression appendOuterScopeVariableJoins(
        Expression expr, Set outerScopeVariables, GenerationContext context) {
        if ((outerScopeVariables == null) || outerScopeVariables.isEmpty()) {
            // no outer scope variables => nothing to be done
            return expr;
        }
        Expression joins = context.joinVariables(outerScopeVariables);
        return appendExpression(expr, joins);
    }
    
}
