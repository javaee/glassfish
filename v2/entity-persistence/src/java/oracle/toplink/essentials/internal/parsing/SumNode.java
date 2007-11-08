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

import java.math.BigDecimal;
import java.math.BigInteger;

import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.expressions.Expression;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Model a SUM
 * <p><b>Responsibilities</b>:<ul>
 * <li> Apply itself to a query correctly
 * </ul>
 */
public class SumNode extends AggregateNode {

    /**
     * INTERNAL
     * Apply this node to the passed query
     */
    public void applyToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery.isReportQuery()) {
            ReportQuery reportQuery = (ReportQuery)theQuery;
            reportQuery.addAttribute(resolveAttribute(), 
                                     generateExpression(context), 
                                     calculateReturnType(context));
            
        }
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     */
    public void validate(ParseTreeContext context) {
        if (left != null) {
            left.validate(context);
            TypeHelper typeHelper = context.getTypeHelper();
            setType(calculateReturnType(left.getType(), typeHelper));
        }
    }

    /**
     * INTERNAL
     */
    protected Expression addAggregateExression(Expression expr) {
        return expr.sum();
    }

    /** 
     * INTERNAL
     * This method calculates the return type of the SUM operation.
     */
    protected Class calculateReturnType(GenerationContext context) {
        Class returnType = null;
        if (getLeft().isDotNode()){
            DotNode arg = (DotNode)getLeft();
            Class fieldType = arg.getTypeOfDirectToField(context);
            TypeHelper helper = context.getParseTreeContext().getTypeHelper();
            returnType = (Class)calculateReturnType(fieldType, helper);
        }
        return returnType;
    }

    /** 
     * INTERNAL
     * Helper method to calculate the return type of the SUM operation.
     */
    protected Object calculateReturnType(Object argType, TypeHelper helper) {
        Object returnType = null;
        if (helper.isIntegralType(argType)) {
            returnType = helper.getLongClassType();
        } else if (helper.isFloatingPointType(argType)) {
            returnType = helper.getDoubleClassType();
        } else if (helper.isBigIntegerType(argType)) {
            returnType = helper.getBigIntegerType();
        } else if (helper.isBigDecimalType(argType)) {
            returnType = helper.getBigDecimalType();
        }
        return returnType;
    }

    /**
     * INTERNAL
     * Get the string representation of this node.
     */
    public String getAsString() {
        return "SUM(" + left.getAsString() + ")";
    }
}
