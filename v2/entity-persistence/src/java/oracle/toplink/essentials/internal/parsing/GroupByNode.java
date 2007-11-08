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
import java.util.*;

// TopLink Imports
import oracle.toplink.essentials.exceptions.EJBQLException;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.queryframework.ObjectLevelReadQuery;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent an GROUP BY
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for an GROUP BY
 * </ul>
 */
public class GroupByNode extends MajorNode {

    List groupByItems = null;

    /**
     * Return a new GroupByNode.
     */
    public GroupByNode() {
        super();
    }

    /**
     * INTERNAL
     * Validate the current node.
     */
    public void validate(ParseTreeContext context, SelectNode selectNode) {
        for (Iterator i = groupByItems.iterator(); i.hasNext(); ) {
            Node item = (Node)i.next();
            item.validate(context);
        }

        List selectExprs = selectNode.getSelectExpressions();
        // check select expressions
        for (Iterator i = selectExprs.iterator(); i.hasNext(); ) {
            Node selectExpr = (Node)i.next();
            if (!isValidSelectExpr(selectExpr)) {
                throw EJBQLException.invalidSelectForGroupByQuery(
                    context.getQueryInfo(), 
                    selectExpr.getLine(), selectExpr.getColumn(), 
                    selectExpr.getAsString(), getAsString());
            }
        }
    }

    /**
     * INTERNAL
     * Add an Group By Item to this node
     */
    private void addGroupByItem(Object theNode) {
        getGroupByItems().add(theNode);
    }

    /**
     * INTERNAL
     * Add the grouping expressions to the passed query
     */
    public void addGroupingToQuery(ObjectLevelReadQuery theQuery, GenerationContext context) {
        if (theQuery.isReportQuery()) {
            Iterator iter = getGroupByItems().iterator();
            while (iter.hasNext()) {
                Node nextNode = (Node)iter.next();
                ((ReportQuery)theQuery).addGrouping(nextNode.generateExpression(context));
            }
        }
    }

    /**
     * INTERNAL
     * Returns true if the sp
     */    
    public boolean isValidHavingExpr(Node expr) {
        if (expr.isDotNode() || expr.isVariableNode()) {
            return isGroupbyItem(expr);
        } else {
            // delegate to child node if any
            Node left = expr.getLeft();
            Node right = expr.getRight();
            return ((left == null) || isValidHavingExpr(left)) &&
                ((right == null) || isValidHavingExpr(right));
        }
    }

    /** 
     * INTERNAL
     * Returns true if the specified expr is a valid SELECT clause expression.
     */    
    private boolean isValidSelectExpr(Node expr) {
        if (expr.isAggregateNode()) {
            return true;
        } else if (expr.isConstructorNode()) {
            List args = ((ConstructorNode)expr).getConstructorItems();
            for (Iterator i = args.iterator(); i.hasNext(); ) {
                Node arg = (Node)i.next();
                if (!isValidSelectExpr(arg)) {
                    return false;
                }
            }
            return true;
        }
        return isGroupbyItem(expr);
    }

    /**
     * INTERNAL
     * Return true if the specified expr is a groupby item.
     */    
    private boolean isGroupbyItem(Node expr) {
        if (expr.isDotNode() || expr.isVariableNode()) {
            String exprRepr = expr.getAsString();
            for (Iterator i = groupByItems.iterator(); i.hasNext();) {
                Node item = (Node)i.next();
                String itemRepr = item.getAsString();
                if (exprRepr.equals(itemRepr)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * INTERNAL
     * Return the GROUP BY statements
     */
    public List getGroupByItems() {
        if (groupByItems == null) {
            setGroupByItems(new Vector());
        }
        return groupByItems;
    }

    /**
     * INTERNAL
     * Set the GROUP BY statements
     */
    public void setGroupByItems(List newItems) {
        groupByItems = newItems;
    }

    /** 
     * INTERNAL
     * Get the string representation of this node. 
     */
    public String getAsString() {
        StringBuffer repr = new StringBuffer();
        for (Iterator i = groupByItems.iterator(); i.hasNext(); ) {
            Node expr = (Node)i.next();
            if (repr.length() > 0) {
                repr.append(", ");
            }
            repr.append(expr.getAsString());
        }
        return "GROUP BY " + repr.toString();
    }
    
}
