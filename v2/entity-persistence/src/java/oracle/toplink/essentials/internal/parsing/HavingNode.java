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
 * <p><b>Purpose</b>: This node represents a HAVING
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for HAVING
 * </ul>
 */
package oracle.toplink.essentials.internal.parsing;

// TopLink Imports
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.expressions.Expression;

public class HavingNode extends MajorNode {

    private Node having = null;

    /**
     * INTERNAL
     * Validate the current node.
     */
    public void validate(ParseTreeContext context, GroupByNode groupbyNode) {
        if (having != null) {
            having.validate(context);
            
            if ((groupbyNode != null) && !groupbyNode.isValidHavingExpr(having)) {
                throw EJBQLException.invalidHavingExpression(
                    context.getQueryInfo(),  having.getLine(), having.getColumn(),
                    having.getAsString(), groupbyNode.getAsString());
            }
        }
    }
    
    /**
     * INTERNAL
     * Add the having expression to the passed query
     */
    public void addHavingToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery.isReportQuery()) {
            Expression havingExpression = getHaving().generateExpression(context);
            ((ReportQuery)theQuery).setHavingExpression(havingExpression);
        }
    }

    /**
     * INTERNAL
     * Return the HAVING expression
     */
    public Node getHaving() {
        return having;
    }

    /**
     * INTERNAL
     * Set the HAVING expression
     */
    public void setHaving(Node having) {
        this.having = having;
    }
}
