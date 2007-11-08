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

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a BETWEEN in EJBQL
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for a BETWEEN in EJBQL
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class BetweenNode extends SimpleConditionalExpressionNode {
    protected Node rightForBetween;
    protected Node rightForAnd;

    /**
     * BetweenNode constructor comment.
     */
    public BetweenNode() {
        super();
    }

    /** 
     * INTERNAL 
     * Check the child nodes for an unqualified field access and if there are
     * any, replace them by a qualified field access.
     */
    public Node qualifyAttributeAccess(ParseTreeContext context) {
        if (left != null) {
            left = left.qualifyAttributeAccess(context);
        }
        if (rightForBetween != null) {
            rightForBetween = rightForBetween.qualifyAttributeAccess(context);
        }
        if (rightForAnd != null) {
            rightForAnd = rightForAnd.qualifyAttributeAccess(context);
        }
        return this;
    }

    /**
     * INTERNAL
     * Validate node and calcualte its type.
     */
    public void validate(ParseTreeContext context) {
        Object type = null;
        if (left != null) {
            left.validate(context);
            type = left.getType();
        }
        if (rightForBetween != null) {
            rightForBetween.validate(context);
            rightForBetween.validateParameter(context, type);
        }
        if (rightForAnd != null) {
            rightForAnd.validate(context);
            rightForAnd.validateParameter(context, type);
        }
        TypeHelper typeHelper = context.getTypeHelper();
        setType(typeHelper.getBooleanType());
    }

    /**
     * INTERNAL
     * Return a TopLink expression by 'BETWEEN' and 'AND'ing the expressions from the left,
     * rightForBetween and rightForAnd nodes
     */
    public Expression generateExpression(GenerationContext context) {
        // Get the left expression
        Expression whereClause = getLeft().generateExpression(context);

        // Between it with whatever the rightForBetween expression and rightForAnd expressions are
        whereClause = whereClause.between(getRightForBetween().generateExpression(context), getRightForAnd().generateExpression(context));

        // and return the expression...
        return whereClause;
    }

    public Node getRightForAnd() {
        return rightForAnd;
    }

    public Node getRightForBetween() {
        return rightForBetween;
    }

    public boolean hasRightForAnd() {
        return rightForAnd != null;
    }

    public boolean hasRightForBetween() {
        return rightForBetween != null;
    }

    public void setRightForAnd(Node newRightForAnd) {
        rightForAnd = newRightForAnd;
    }

    public void setRightForBetween(Node newRightForBetween) {
        rightForBetween = newRightForBetween;
    }
}
