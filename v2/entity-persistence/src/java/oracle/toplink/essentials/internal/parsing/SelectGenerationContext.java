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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 * An extension of GenerationContext the provides SELECT specfic behavior.
 * Used when building the query features that are not usable in other types of queries
 */
public class SelectGenerationContext extends GenerationContext {
    //if a 1:1 is SELECTed in the EJBQL, then we need to use parallel expressions
    //with each ExpressionBuilder created using "new ExpressionBuilder(MyClass.class)"
    private boolean useParallelExpressions = false;

    //BUG 3105651: If a variable is SELECTed, and it's in an ORDER BY, then 
    //we want the ExpressionBuilder to be instantiated using an empty constructor
    private boolean shouldCheckSelectNodeBeforeResolving = false;
    private boolean isNotIndicatedInMemberOf = false;

    //If a NOT MEMBER OF is encountered, we need to store the MEMBER OF
    //so that the right side of the member of can use the stored expression
    //from the left
    private MemberOfNode memberOfNode = null;

    //Do we want to use outer joins? get("address") vs getAllowingNull("address")
    private boolean shouldUseOuterJoins = false;

    //Outer SelectGenerationContext
    private GenerationContext outer = null;

    public SelectGenerationContext() {
        super();
    }

    /**
     * Constructor used to create the context for a subquery.
     */
    public SelectGenerationContext(GenerationContext outer, ParseTree newParseTree) {
        this(outer.getParseTreeContext(), outer.getSession(), newParseTree);
        this.outer = outer;
    }

    public SelectGenerationContext(ParseTreeContext newContext, AbstractSession newSession, ParseTree newParseTree) {
        super(newContext, newSession, newParseTree);

        //indicate if we want parallel expressions or not
        useParallelExpressions = this.computeUseParallelExpressions();
    }

    //Set and get the contained MemberOfNode. This is for handling NOT MEMBER OF.
    public void setMemberOfNode(MemberOfNode newMemberOfNode) {
        memberOfNode = newMemberOfNode;
    }

    public MemberOfNode getMemberOfNode() {
        return memberOfNode;
    }

    private boolean computeUseParallelExpressions() {
        boolean computedUseParallelExpressions;

        //use parallel expressions if I have a 1:1 selected, and the same class isn't
        //declared in the FROM
        computedUseParallelExpressions = ((SelectNode)this.parseTree.getQueryNode()).hasOneToOneSelected(this);
        //check if they've SELECTed a variable declared in the IN clause in the FROM,
        //or they've mapped more than one variable to the same type in the FROM
        computedUseParallelExpressions = computedUseParallelExpressions || ((SelectNode)this.parseTree.getQueryNode()).isVariableInINClauseSelected(this) || this.parseTree.getContext().hasMoreThanOneVariablePerType() || this.parseTree.getContext().hasMoreThanOneAliasInFrom();
        return computedUseParallelExpressions;
    }

    //Answer true if we need to use parallel expressions
    //This will be the case if a 1:1 is SELECTed in the EJBQL. 
    public boolean useParallelExpressions() {
        return useParallelExpressions;
    }

    //Indicate that we want VariableNodes to check if they're
    //SELECTed first, to determine how to instantiate the ExpressionBuilder 
    public void checkSelectNodeBeforeResolving(boolean shouldCheck) {
        shouldCheckSelectNodeBeforeResolving = shouldCheck;
    }

    //Answer true if we want VariableNodes to check if they're
    //SELECTed first, to determine how to instantiate the ExpressionBuilder 
    public boolean shouldCheckSelectNodeBeforeResolving() {
        return shouldCheckSelectNodeBeforeResolving;
    }

    //Answer true if we should use outer joins in our get() (vs getAllowingNull())
    public boolean shouldUseOuterJoins() {
        return shouldUseOuterJoins;
    }

    public void useOuterJoins() {
        shouldUseOuterJoins = true;
    }

    public void dontUseOuterJoins() {
        shouldUseOuterJoins = false;
    }

    //Answer true if we have a MemberOfNode contained. This is for handling NOT MEMBER OF
    public boolean hasMemberOfNode() {
        return memberOfNode != null;
    }

    /** */
    public GenerationContext getOuterContext() {
        return outer;
    }
    
    /** 
     * Iterate the set of variables declared in an outer scope and
     * connect the inner varaible expression with the outer one.
     */
    public Expression joinVariables(Set variables) {
        if ((outer == null) || (variables == null) || variables.isEmpty()) {
            // not an inner query or no variables to join
            return null;
        }
        Expression expr = null;
        for (Iterator i = variables.iterator(); i.hasNext(); ) {
            String name = (String)i.next();
            VariableNode var = new VariableNode(name);
            Expression innerExpr = var.generateExpression(this);
            Expression outerExpr = var.generateExpression(outer);
            Expression join = innerExpr.equal(outerExpr);
            expr = var.appendExpression(expr, join);
        }
        return expr;
    }
}
