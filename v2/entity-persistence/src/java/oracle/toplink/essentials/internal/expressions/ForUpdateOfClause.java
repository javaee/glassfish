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

import java.util.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.ObjectBuildingQuery;

/**
 * <b>Purpose:</b>Represents The FOR UPDATE OF fine-grained pessimistically
 * locking clause.
 * @author  Stephen McRitchie
 * @since   Oracle Toplink 10g AS
 */
public class ForUpdateOfClause extends ForUpdateClause {
    protected Vector lockedExpressions;

    public ForUpdateOfClause() {
    }

    public void addLockedExpression(ObjectExpression expression) {
        getLockedExpressions().addElement(expression);
    }

    public Vector getLockedExpressions() {
        if (lockedExpressions == null) {
            lockedExpressions = oracle.toplink.essentials.internal.helper.NonSynchronizedVector.newInstance();
        }
        return lockedExpressions;
    }

    public boolean isForUpdateOfClause() {
        return true;
    }

    public boolean isReferenceClassLocked() {
        if (lockedExpressions == null) {
            return false;
        }

        // Normally the expressionBuilder is stored first but not necessarily 
        // when a child ForUpdateOfClause is built for a nested query, or if a 
        //user made this clause.
        for (int i = 0; i < lockedExpressions.size(); i++) {
            if (((Expression)lockedExpressions.elementAt(i)).isExpressionBuilder()) {
                return true;
            }
        }
        return false;
    }

    public void setLockedExpressions(Vector lockedExpressions) {
        this.lockedExpressions = lockedExpressions;
    }

    public void setLockMode(short lockMode) {
        this.lockMode = lockMode;
    }

    /**
     * INTERNAL:
     * Prints the as of clause for an expression inside of the FROM clause.
     */
    public void printSQL(ExpressionSQLPrinter printer, SQLSelectStatement statement) {
        // assert(lockedExpressions != null && lockedExpressions.size() > 0);
        // assert(	getLockMode() == ObjectBuildingQuery.LOCK || 
        //			getLockMode() == ObjectBuildingQuery.LOCK_NOWAIT);
        ExpressionBuilder clonedBuilder = statement.getBuilder();

        printer.printString(printer.getSession().getPlatform().getSelectForUpdateOfString());

        printer.setIsFirstElementPrinted(false);
        for (Enumeration enumtr = getLockedExpressions().elements(); enumtr.hasMoreElements();) {
            ObjectExpression next = (ObjectExpression)enumtr.nextElement();

            // Neccessary as this was determined in query framework.
            next = (ObjectExpression)next.rebuildOn(clonedBuilder);
            next.writeForUpdateOfFields(printer, statement);
        }
        if (lockMode == ObjectBuildingQuery.LOCK_NOWAIT) {
            printer.printString(printer.getSession().getPlatform().getSelectForUpdateNoWaitString());
        }
    }
}
