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
package oracle.toplink.essentials.internal.expressions;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.descriptors.ClassDescriptor;

/**
 * Used for logical AND and OR.  This is not used by NOT.
 */
public class LogicalExpression extends CompoundExpression {

    /**
     * LogicalExpression constructor comment.
     */
    public LogicalExpression() {
        super();
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    public String descriptionOfNodeType() {
        return "Logical";
    }

    /**
     * INTERNAL:
     * Check if the object conforms to the expression in memory.
     * This is used for in-memory querying.
     * If the expression in not able to determine if the object conform throw a not supported exception.
     */
    public boolean doesConform(Object object, AbstractSession session, AbstractRecord translationRow, InMemoryQueryIndirectionPolicy valueHolderPolicy, boolean objectIsUnregistered) {
        // This should always be and or or.
        if (getOperator().getSelector() == ExpressionOperator.And) {
            return getFirstChild().doesConform(object, session, translationRow, valueHolderPolicy, objectIsUnregistered) && getSecondChild().doesConform(object, session, translationRow, valueHolderPolicy, objectIsUnregistered);
        } else if (getOperator().getSelector() == ExpressionOperator.Or) {
            return getFirstChild().doesConform(object, session, translationRow, valueHolderPolicy, objectIsUnregistered) || getSecondChild().doesConform(object, session, translationRow, valueHolderPolicy, objectIsUnregistered);
        }

        throw QueryException.cannotConformExpression();

    }

    /**
     * INTERNAL:
     * Extract the primary key from the expression into the row.
     * Ensure that the query is quering the exact primary key.
     * Return false if not on the primary key.
     */
    public boolean extractPrimaryKeyValues(boolean requireExactMatch, ClassDescriptor descriptor, AbstractRecord primaryKeyRow, AbstractRecord translationRow) {
        // If this is a primary key expression then it can only have and/or relationships.
        if (getOperator().getSelector() != ExpressionOperator.And) {
            // If this is an exact primary key expression it can not have ors.
            // After fixing bug 2782991 this must now work correctly.
            if (requireExactMatch || (getOperator().getSelector() != ExpressionOperator.Or)) {
                return false;
            }
        }
        boolean validExpression = getFirstChild().extractPrimaryKeyValues(requireExactMatch, descriptor, primaryKeyRow, translationRow);
        if (requireExactMatch && (!validExpression)) {
            return false;
        }
        return getSecondChild().extractPrimaryKeyValues(requireExactMatch, descriptor, primaryKeyRow, translationRow);
    }

    /**
     * INTERNAL:
     */
    public boolean isLogicalExpression() {
        return true;
    }
}
