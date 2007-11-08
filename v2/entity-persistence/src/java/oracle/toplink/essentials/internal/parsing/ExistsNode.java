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

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.expressions.ConstantExpression;
import oracle.toplink.essentials.internal.queryframework.ReportItem;
import oracle.toplink.essentials.queryframework.ReportQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent an EXISTS subquery.
 */
public class ExistsNode extends Node {

    /** True in case of a NOT EXISTS (...) query. */
    private boolean notIndicated = false;

    /**
     * Return a new ExistsNode.
     */
    public ExistsNode() {
        super();
    }

    /**
     * INTERNAL
     * Validate node and calculate its type.
     * Change subquery SELECT clause.
     */
    public void validate(ParseTreeContext context) {
        if (left != null) {
            
            // change SELECT clause of subquery
            SubqueryNode subqueryNode = (SubqueryNode)getLeft();
            // validate changed subquery
            subqueryNode.validate(context);

            TypeHelper typeHelper = context.getTypeHelper();
            setType(typeHelper.getBooleanType());
        }
    }

    /**
     * INTERNAL
     * Generate the TopLink expression for this node
     */
    public Expression generateExpression(GenerationContext context) {
        SubqueryNode subqueryNode = (SubqueryNode)getLeft();
        ReportQuery reportQuery = subqueryNode.getReportQuery(context);
        // Replace the SELECT clause of the exists subquery by SELECT 1 to
        // avoid problems with databases not supporting mutiple columns in the
        // subquery SELECT clause in SQL.
        // The original select clause expressions might include relationship
        // navigations which should result in FK joins in the generated SQL,
        // e.g. ... EXISTS (SELECT o.customer FROM Order o ...). Add the
        // select clause expressions as non fetch join attributes to the
        // ReportQuery representing the subquery. This make sure the FK joins
        // get generated.  
        List items = reportQuery.getItems();
        for (Iterator i = items.iterator(); i.hasNext();) {
            ReportItem item = (ReportItem)i.next();
            Expression expr = item.getAttributeExpression();
            reportQuery.addNonFetchJoinedAttribute(expr);
        }
        reportQuery.clearItems();
        Expression one = new ConstantExpression(new Integer(1), new ExpressionBuilder());
        reportQuery.addItem("one", one);
        reportQuery.dontUseDistinct();
        Expression expr = context.getBaseExpression();
        return notIndicated() ? expr.notExists(reportQuery) : 
            expr.exists(reportQuery);
    }

    /**
     * INTERNAL
     * Indicate if a NOT was found in the WHERE clause.
     * Examples: WHERE ... NOT EXISTS(...)
     */
    public void indicateNot() {
        notIndicated = true;
    }

    public boolean notIndicated() {
        return notIndicated;
    }

}
