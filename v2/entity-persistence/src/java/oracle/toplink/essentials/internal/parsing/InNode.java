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

// TopLink imports
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.ReportQuery;
import oracle.toplink.essentials.exceptions.EJBQLException;

/**
 * INTERNAL
 * <p><b>Purpose</b>: Represent an IN in EJBQL
 * <p><b>Responsibilities</b>:<ul>
 * <li> Generate the correct expression for an IN
 * </ul>
 *    @author Jon Driscoll and Joel Lucuik
 *    @since TopLink 4.0
 */
public class InNode extends SimpleConditionalExpressionNode {

    private List theObjects = null;

    //Was NOT indicated? "WHERE emp.lastName NOT IN (...)
    private boolean notIndicated = false;

    /**
     * InNode constructor comment.
     */
    public InNode() {
        super();
    }

    /**
     * INTERNAL
     * Add the passed node value to the collection of object for this node
     */
    public void addNodeToTheObjects(Node theNode) {
        getTheObjects().add(theNode);
    }

    /**
     * INTERNAL
     * Validate the current node and calculates its type.
     */
    public void validate(ParseTreeContext context) {
        Object leftType = null;
        TypeHelper typeHelper = context.getTypeHelper();

        if (left != null) {
            left.validate(context);
            leftType = left.getType();
        }
        for (Iterator i = getTheObjects().iterator(); i.hasNext();) {
            Node node = (Node)i.next();
            node.validate(context);
            node.validateParameter(context, leftType);
            Object nodeType = node.getType();
            if ((leftType != null) && !typeHelper.isAssignableFrom(leftType, nodeType))
                throw EJBQLException.invalidExpressionArgument(
                    context.getQueryInfo(), node.getLine(), node.getColumn(),
                    "IN", node.getAsString(), typeHelper.getTypeName(leftType));
        }

        setType(typeHelper.getBooleanType());
    }

    /**
     * INTERNAL
     * Return the TopLink expression for this node
     */
    public Expression generateExpression(GenerationContext context) {
        Expression whereClause = getLeft().generateExpression(context);
        List arguments = getTheObjects();
        Node firstArg = (Node)arguments.get(0);
        if (firstArg.isSubqueryNode()) {
            SubqueryNode subqueryNode = (SubqueryNode)firstArg;
            ReportQuery reportQuery = subqueryNode.getReportQuery(context);
            if (notIndicated()) {
                whereClause = whereClause.notIn(reportQuery);
            }
            else {
                whereClause = whereClause.in(reportQuery);
            }
        }
        else {
            Vector inArguments = new Vector(arguments.size());
            for (Iterator iter = arguments.iterator(); iter.hasNext();) {
                Node nextNode = (Node)iter.next();
                inArguments.add(nextNode.generateExpression(context));
            }
            if (inArguments.size() > 0) {
                if (notIndicated()) {
                    whereClause = whereClause.notIn(inArguments);
                } else {
                    whereClause = whereClause.in(inArguments);
                }
            }
        }
        return whereClause;
    }

    /**
     * INTERNAL
     * Return the collection of the objects used as parameters for this node
     */
    public List getTheObjects() {
        if (theObjects == null) {
            setTheObjects(new Vector());
        }
        return theObjects;
    }

    /**
     * INTERNAL
     * Set this node's object collection to the passed value
     */
    public void setTheObjects(List newTheObjects) {
        theObjects = newTheObjects;
    }

    /**
     * INTERNAL
     * Indicate if a NOT was found in the WHERE clause.
     * Examples:
     *        ...WHERE ... NOT IN(...)
     */
    public void indicateNot() {
        notIndicated = true;
    }

    public boolean notIndicated() {
        return notIndicated;
    }
}
