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


// Java imports
import java.util.Hashtable;
import java.util.Set;

// TopLink imports
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.expressions.ExpressionBuilder;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Maintain the generation context for an EJBQL query
 * <p><b>Responsibilities</b>:<ul>
 * <li> Maintain a table of expression builders and alias's
 * <li> Maintain the base query class
 * <li> Maintain a handle to the session
 * <li> Maintain a handle to the parse tree
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class GenerationContext {
    protected AbstractSession session;
    protected ParseTreeContext parseTreeContext;
    protected Class baseQueryClass;
    protected Expression baseExpression;
    protected Hashtable expressions;
    protected ParseTree parseTree;
    protected boolean isNotIndicatedInMemberOf = false;

    //If a NOT MEMBER OF is encountered, we need to store the MEMBER OF
    //so that the right side of the member of can use the stored expression
    //from the left
    protected MemberOfNode memberOfNode = null;

    public GenerationContext() {
        super();
        expressions = new Hashtable();
    }

    public GenerationContext(ParseTreeContext newContext, AbstractSession newSession, ParseTree newParseTree) {
        super();
        parseTreeContext = newContext;
        session = newSession;
        expressions = new Hashtable();
        parseTree = newParseTree;
    }

    public void addExpression(Expression expression, String aliasName) {
        expressions.put(aliasName, expression);
    }

    public Expression expressionFor(String aliasName) {
        return (Expression)expressions.get(aliasName);
    }

    public Class getBaseQueryClass() {
        return baseQueryClass;
    }

    public ParseTreeContext getParseTreeContext() {
        return parseTreeContext;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public AbstractSession getSession() {
        return session;
    }

    public void setBaseQueryClass(java.lang.Class newBaseQueryClass) {
        baseQueryClass = newBaseQueryClass;
    }

    /** 
     * Caches the specified expression under the variable name for the base
     * query class.
     */
    public void setBaseExpression(String variable, Expression expr) {
        // Store the expression for faster access
        baseExpression = expr;
        
        // Store it into the cache
        addExpression(expr, variable);
    }

    /** */
    public Expression getBaseExpression() {
        return baseExpression;
    }

    public void setParseTree(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    public void setParseTreeContext(ParseTreeContext newParseTreeContext) {
        parseTreeContext = newParseTreeContext;
    }

    public void setSession(AbstractSession newSession) {
        session = newSession;
    }

    //Answer true if we need to use parallel expressions
    //This will be the case if a 1:1 is SELECTed in the EJBQL. 
    public boolean useParallelExpressions() {
        return false;
    }

    //Answer true if we want VariableNodes to check if they're
    //SELECTed first, to determine how to instantiate the ExpressionBuilder 
    public boolean shouldCheckSelectNodeBeforeResolving() {
        return false;
    }

    //Set and get the contained MemberOfNode. This is for handling NOT MEMBER OF.
    public void setMemberOfNode(MemberOfNode newMemberOfNode) {
        memberOfNode = newMemberOfNode;
    }

    public MemberOfNode getMemberOfNode() {
        return memberOfNode;
    }

    //Answer true if we have a MemberOfNode contained. This is for handling NOT MEMBER OF
    public boolean hasMemberOfNode() {
        return memberOfNode != null;
    }

    //Answer true if we should use outer joins in our get() (vs getAllowingNull())
    public boolean shouldUseOuterJoins() {
        return false;
    }

    /** */
    public Expression joinVariables(Set variables) {
        return null;
    }
}
