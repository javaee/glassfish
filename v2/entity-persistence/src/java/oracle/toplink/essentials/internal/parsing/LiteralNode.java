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

import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;

import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.expressions.*;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Superclass for literals (String, Integer, Float, Character, ...)
 * <p><b>Responsibilities</b>:<ul>
 * <li> Maintain the literal being represented
 * <li> Print to a string
 * <li> Answer if the node is completely built
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class LiteralNode extends Node {
    public java.lang.Object literal;

    /**
     * Return a new LiteralNode.
     */
    public LiteralNode() {
        super();
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery.isReportQuery()) {
            ReportQuery reportQuery = (ReportQuery)theQuery;
            reportQuery.addAttribute("CONSTANT", generateExpression(context));
        }
        
    }

    /**
     * INTERNAL
     * Generate the a new TopLink ConstantExpression for this node.
     */
    public Expression generateExpression(GenerationContext context) {
        Expression whereClause = new ConstantExpression(getLiteral(), new ExpressionBuilder());
        return whereClause;
    }

    /**
     * INTERNAL
     * Return the literal
     */
    public String getAsString() {
        return getLiteral().toString();
    }

    /**
     * Insert the method's description here.
     * Creation date: (12/21/00 10:51:48 AM)
     * @return java.lang.Object
     */
    public java.lang.Object getLiteral() {
        return literal;
    }

    /**
     * INTERNAL
     * Is this a literal node
     */
    public boolean isLiteralNode() {
        return true;
    }

    /**
     * Insert the method's description here.
     * Creation date: (12/21/00 10:51:48 AM)
     * @param newLiteral java.lang.Object
     */
    public void setLiteral(java.lang.Object newLiteral) {
        literal = newLiteral;
    }

    public String toString(int indent) {
        StringBuffer buffer = new StringBuffer();
        toStringIndent(indent, buffer);
        buffer.append(toStringDisplayName() + "[" + getLiteral() + "]");
        return buffer.toString();
    }
}
