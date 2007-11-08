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
import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Print UPDATE statement.
 * <p><b>Responsibilities</b>:<ul>
 * <li> Print UPDATE statement.
 * </ul>
 *    @author Dorin Sandu
 *    @since TOPLink/Java 1.0
 */
public class SQLUpdateStatement extends SQLModifyStatement {

    /**
     * Append the string containing the SQL insert string for the given table.
     */
    protected SQLCall buildCallWithoutReturning(AbstractSession session) {
        SQLCall call = new SQLCall();
        call.returnNothing();

        Writer writer = new CharArrayWriter(100);
        try {
            writer.write("UPDATE ");
            writer.write(getTable().getQualifiedName());
            writer.write(" SET ");

            Vector fieldsForTable = new Vector();
            for (Enumeration fieldsEnum = getModifyRow().keys(); fieldsEnum.hasMoreElements();) {
                DatabaseField field = (DatabaseField)fieldsEnum.nextElement();
                if (field.getTable().equals(getTable()) || (!field.hasTableName())) {
                    fieldsForTable.addElement(field);
                }
            }

            if (fieldsForTable.isEmpty()) {
                return null;
            }

            for (int i = 0; i < fieldsForTable.size(); i++) {
                DatabaseField field = (DatabaseField)fieldsForTable.elementAt(i);
                writer.write(field.getName());
                writer.write(" = ");
                call.appendModify(writer, field);

                if ((i + 1) < fieldsForTable.size()) {
                    writer.write(", ");
                }
            }

            if (!(getWhereClause() == null)) {
                writer.write(" WHERE ");
                ExpressionSQLPrinter printer = new ExpressionSQLPrinter(session, getTranslationRow(), call, false);
                printer.setWriter(writer);
                printer.printExpression(getWhereClause());
            }

            call.setSQLString(writer.toString());
            return call;
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }
}
