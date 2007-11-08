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

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.expressions.*;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent a date function: CURRENT_DATE, CURRENT_TIME,
 * CURRENT_TIMESTAMP.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for the date function
 * </ul>
 */
public class DateFunctionNode extends FunctionalExpressionNode {

    private Class type;

    /**
     * DateFunctionNode constructor.
     */
    public DateFunctionNode() {
        super();
    }

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery.isReportQuery()){
            ReportQuery reportQuery = (ReportQuery)theQuery;
            reportQuery.addAttribute("date", generateExpression(context), type);
        }
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        setType(type);
    }

    /**
     * INTERNAL
     * Generate the TopLink expression for this node
     */
    public Expression generateExpression(GenerationContext context) {
        Expression expr = context.getBaseExpression();
        if (expr == null) {
            expr = new ExpressionBuilder();
        }
        Expression result = null;
        if (type == Date.class) {
            result = expr.currentDateDate();
        } else if (type == Time.class) {
            result = expr.currentTime();
        } else if (type == Timestamp.class) {
            result = expr.currentDate();
        }
        return result;
    }

    /** */
    public void useCurrentDate() {
        type = Date.class;
    }

    /** */
    public void useCurrentTime() {
        type = Time.class;
    }

    /** */
    public void useCurrentTimestamp() {
        type = Timestamp.class;
    }
    
}
