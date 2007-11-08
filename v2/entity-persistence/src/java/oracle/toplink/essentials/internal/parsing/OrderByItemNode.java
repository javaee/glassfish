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

/**
 * INTERNAL
 * <p><b>Purpose</b>: This node represents an ORDER BY item
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for ORDER BY
 * </ul>
 *    @author Jon Driscoll
 *    @since OracleAS TopLink 10<i>g</i> (9.0.4)
 */
package oracle.toplink.essentials.internal.parsing;

import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.expressions.Expression;

public class OrderByItemNode extends Node {
    private SortDirectionNode direction = null;
    private Node orderByItem = null;

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        TypeHelper typeHelper = context.getTypeHelper();
        if (orderByItem != null) {
            orderByItem.validate(context);
            Object type = orderByItem.getType();
            setType(type);
            if (!typeHelper.isOrderableType(type)) {
                throw EJBQLException.expectedOrderableOrderByItem(
                    context.getQueryInfo(), orderByItem.getLine(), orderByItem.getColumn(), 
                    orderByItem.getAsString(), typeHelper.getTypeName(type));
            }
        }
    }

    /** */
    public Expression generateExpression(GenerationContext context) {
        //BUG 3105651: Indicate to the VariableNodes in the subtree
        //that they should check the SelectNode before resolving.
        //If the variable involved is SELECTed, then we want an empty builder
        //instead (with an empty constructor).
        boolean oldCheckState = context.shouldCheckSelectNodeBeforeResolving();
        ((SelectGenerationContext)context).checkSelectNodeBeforeResolving(true);
        Expression orderByExpression = getOrderByItem().generateExpression(context);
        orderByExpression = getDirection().addToExpression(orderByExpression, context);
        ((SelectGenerationContext)context).checkSelectNodeBeforeResolving(oldCheckState);
        return orderByExpression;
    }

    public SortDirectionNode getDirection() {
        if (direction == null) {
            setDirection(new SortDirectionNode());
        }
        return direction;
    }

    public Node getOrderByItem() {
        return orderByItem;
    }

    public void setDirection(SortDirectionNode direction) {
        this.direction = direction;
    }

    public void setOrderByItem(Node orderByItem) {
        this.orderByItem = orderByItem;
    }
}
