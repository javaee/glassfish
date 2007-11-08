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

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent the MEMBER-OF operator
 * <p><b>Responsibilities</b>:<ul>
 * <li> MEMBER OF is not supported.
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since since July 2003
 */
import oracle.toplink.essentials.expressions.*;

public class MemberOfNode extends BinaryOperatorNode {
    private boolean notIndicated = false;

    //If we're dealing with a NOT, we store the expression for the left. 
    //When we get to the one-to-many on the right, it will handle the noneOf using 
    //the receiver stored in the context. 
    //(i.e. secondLastRightExpression.noneOf(lastRightVariable, leftExpression)
    private Expression leftExpression = null;

    /**
     * Return a new MemberOfNode
     */
    public MemberOfNode() {
        super();
    }

    /**
     * INTERNAL makeNodeOneToMany:
     * Traverse to the leaf on theNode and mark as one to many
     */
    public void makeNodeOneToMany(Node theNode) {
        Node currentNode = theNode;
        do {
            if (!currentNode.hasRight()) {
                ((AttributeNode)currentNode).setRequiresCollectionAttribute(true);
                return;
            }
            currentNode = currentNode.getRight();
        } while (true);
    }

    /**
     * INTERNAL
     * Validate node and calculates its type.
     */
    public void validate(ParseTreeContext context) {
        super.validate(context);
        Node left = getLeft();
        if (left.isVariableNode() && ((VariableNode)left).isAlias(context)) {
            context.usedVariable(((VariableNode)left).getCanonicalVariableName());
        }
        left.validateParameter(context, right.getType());
        TypeHelper typeHelper = context.getTypeHelper();
        setType(typeHelper.getBooleanType());
    }
    
    public Expression generateExpression(GenerationContext context) {
        // Need to make sure one of the node is marked as a one to many
        if (getRight().isParameterNode()) {
            makeNodeOneToMany(getLeft());
        } else {
            makeNodeOneToMany(getRight());
        }

        //Handle NOT. Store the expression for the left, let VariableNode handle it.
        if (notIndicated()) {
            Expression resultFromRight = null;
            context.setMemberOfNode(this);
            this.setLeftExpression(getLeft().generateExpression(context));
            resultFromRight = getRight().generateExpression(context);
            //clean up
            context.setMemberOfNode(null);
            this.setLeftExpression(null);
            return resultFromRight;
        } else {
            //otherwise, handle like normal anyOf()
            return getRight().generateExpression(context).equal(getLeft().generateExpression(context));
        }
    }

    /**
     * INTERNAL
     * Indicate if a NOT was found in the WHERE clause.
     * Examples:
     *        ...WHERE ... NOT MEMBER OF
     */
    public void indicateNot() {
        notIndicated = true;
    }

    public boolean notIndicated() {
        return notIndicated;
    }

    //set and get the leftExpression. This is for NOT MEMBER OF.
    public void setLeftExpression(Expression newLeftExpression) {
        leftExpression = newLeftExpression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }
}
