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

import java.io.*;
import oracle.toplink.essentials.queryframework.ObjectBuildingQuery;

/**
 * <b>Purpose:</b>Represents The FOR UPDATE pessimistically locking clause.
 * @author  Stephen McRitchie
 * @since   Oracle Toplink 10g AS
 */
public class ForUpdateClause implements Serializable, Cloneable {
    protected static final ForUpdateClause NO_LOCK_CLAUSE = new ForUpdateClause();
    short lockMode;

    public ForUpdateClause() {
        this.lockMode = ObjectBuildingQuery.NO_LOCK;
    }

    public ForUpdateClause(short lockMode) {
        this.lockMode = lockMode;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException never) {
            return null;
        }
    }

    public static ForUpdateClause newInstance(short lockMode) {
        if (lockMode == ObjectBuildingQuery.NO_LOCK) {
            return NO_LOCK_CLAUSE;
        } else {
            return new ForUpdateClause(lockMode);
        }
    }

    public boolean isForUpdateOfClause() {
        return false;
    }

    public boolean isReferenceClassLocked() {
        return true;
    }

    public short getLockMode() {
        return lockMode;
    }

    /**
     * INTERNAL:
     * Prints the as of clause for an expression inside of the FROM clause.
     */
    public void printSQL(ExpressionSQLPrinter printer, SQLSelectStatement statement) {
        // Append lock strings
        if (getLockMode() == ObjectBuildingQuery.LOCK) {
            printer.printString(printer.getSession().getPlatform().getSelectForUpdateString());
        } else if (lockMode == ObjectBuildingQuery.LOCK_NOWAIT) {
            printer.printString(printer.getSession().getPlatform().getSelectForUpdateString());
            printer.printString(printer.getSession().getPlatform().getSelectForUpdateNoWaitString());
        }
    }
}
