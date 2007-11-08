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
import java.util.HashMap;
import java.util.Iterator;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.expressions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import java.util.Collection;
import oracle.toplink.essentials.internal.databaseaccess.DatabaseCall;
import oracle.toplink.essentials.internal.helper.DatabaseField;

/**
 * @author Guy Pelletier
 * @since TOPLink/Java 1.0
 */
public class SQLUpdateAllStatement extends SQLModifyStatement {
    protected HashMap m_updateClauses;
    protected HashMap databaseFieldsToTableAliases;

    protected SQLCall selectCallForExist;
    protected String tableAliasInSelectCallForExist;
    protected Collection primaryKeyFields;
    protected boolean shouldExtractWhereClauseFromSelectCallForExist;
    
    public void setSelectCallForExist(SQLCall selectCallForExist) {
        this.selectCallForExist = selectCallForExist;
    }
    public SQLCall getSelectCallForExist() {
        return selectCallForExist;
    }
    public void setTableAliasInSelectCallForExist(String tableAliasInSelectCallForExist) {
        this.tableAliasInSelectCallForExist = tableAliasInSelectCallForExist;
    }
    public String getTableAliasInSelectCallForExist() {
        return tableAliasInSelectCallForExist;
    }
    public void setPrimaryKeyFieldsForAutoJoin(Collection primaryKeyFields) {
        this.primaryKeyFields = primaryKeyFields;
    }
    public Collection getPrimaryKeyFieldsForAutoJoin() {
        return primaryKeyFields;
    }
    public void setUpdateClauses(HashMap updateClauses) {
        m_updateClauses = updateClauses;
    }
    public HashMap getUpdateClauses() {
        return m_updateClauses;
    }
    public void setDatabaseFieldsToTableAliases(HashMap databaseFieldsToTableAliases) {
        this.databaseFieldsToTableAliases = databaseFieldsToTableAliases;
    }
    public HashMap getDatabaseFieldsToTableAliases() {
        return databaseFieldsToTableAliases;
    }
    public void setShouldExtractWhereClauseFromSelectCallForExist(boolean shouldExtractWhereClauseFromSelectCallForExist) {
        this.shouldExtractWhereClauseFromSelectCallForExist = shouldExtractWhereClauseFromSelectCallForExist;
    }
    public boolean shouldExtractWhereClauseFromSelectCallForExist() {
        return shouldExtractWhereClauseFromSelectCallForExist;
    }

    
    /**
     * Append the string containing the SQL insert string for the given table.
     */
    public DatabaseCall buildCall(AbstractSession session) {
        SQLCall call = buildSimple(session);
        if(selectCallForExist == null) {
            return call;
        }
        Writer writer = new CharArrayWriter(100);
        try {
            writer.write(call.getSQLString());

            if(selectCallForExist != null) {
                if(shouldExtractWhereClauseFromSelectCallForExist) {
                    // Should get here only in case selectCallForExist doesn't have aliases and 
                    // targets the same table as the statement.
                    // Instead of making selectCallForExist part of " WHERE EXIST("
                    // just extract its where clause.
                    // Example: selectCallForExist.sqlString:
                    // "SELECT PROJ_ID FROM PROJECT WHERE (LEADER_ID IS NULL)
                    writeWhere(writer, selectCallForExist, call);
                    // The result is:
                    // "WHERE (LEADER_ID IS NULL)"
                } else {
                    writer.write(" WHERE EXISTS(");
                    // EXIST Example: selectCall.sqlString:
                    // "SELECT t0.EMP_ID FROM EMPLOYEE t0, SALARY t1 WHERE (((t0.F_NAME LIKE 'a') AND (t1.SALARY = 0)) AND (t1.EMP_ID = t0.EMP_ID))"
                    writeSelect(writer, selectCallForExist, tableAliasInSelectCallForExist, call);
                    // closing bracket for EXISTS
                    writer.write(")");
                }
            }

            call.setSQLString(writer.toString());
            
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
                
        return call;
    }
    
    protected SQLCall buildSimple(AbstractSession session) {
        SQLCall call = new SQLCall();
        call.returnNothing();
        Writer writer = new CharArrayWriter(100);
        ExpressionSQLPrinter printer = new ExpressionSQLPrinter(session, getTranslationRow(), call, false);
        printer.setWriter(writer);

        try {
            // UPDATE //
            writer.write("UPDATE ");
            writer.write(getTable().getQualifiedName());
            // SET CLAUSE //
            writer.write(" SET ");

            Iterator i = m_updateClauses.keySet().iterator();
            boolean commaNeeded = false;

            while (i.hasNext()) {
                if (commaNeeded) {
                    writer.write(", ");
                }

                DatabaseField field = (DatabaseField)i.next();
                Object value = m_updateClauses.get(field);

                writer.write(field.getName());
                writer.write(" = ");
                if(value instanceof Expression) {
                    printer.printExpression((Expression)value);
                } else {
                    // must be SQLCall
                    SQLCall selCall = (SQLCall)value;
                    String tableAlias = (String)getDatabaseFieldsToTableAliases().get(field);
                    // should be SQLCall select
                    writer.write("(");
                    writeSelect(writer, selCall, tableAlias, call);
                    writer.write(")");
                }

                commaNeeded = true;
            }

            // WHERE CLAUSE //
            if (getWhereClause() != null) {
                writer.write(" WHERE ");
                printer.printExpression(getWhereClause());
            }

            call.setSQLString(writer.toString());
            return call;
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
    }

    protected void writeSelect(Writer writer, SQLCall selectCall, String tableAliasInSelectCall, SQLCall call) throws IOException {
        String str = selectCall.getSQLString();
        writer.write(str);
        
        boolean hasWhereClause = str.toUpperCase().indexOf(" WHERE ") >= 0;

        // Auto join
        // Example: AND t0.EMP_ID = EMP_ID
        Iterator it = getPrimaryKeyFieldsForAutoJoin().iterator();
        while(it.hasNext()) {
            if(!hasWhereClause) {
            // there is no where clause - should print WHERE
                writer.write(" WHERE ");
                hasWhereClause = true;
            } else {
                writer.write(" AND ");
            }
            String fieldName = ((DatabaseField)it.next()).getName();
            if(tableAliasInSelectCall != null) {
                writer.write(tableAliasInSelectCall);
                writer.write('.');
            }
            writer.write(fieldName);
            writer.write(" = ");
            writer.write(table.getQualifiedName());
            writer.write('.');
            writer.write(fieldName);
        }
        
        call.getParameters().addAll(selectCall.getParameters());
        call.getParameterTypes().addAll(selectCall.getParameterTypes());            
    }    

    protected boolean writeWhere(Writer writer, SQLCall selectCall, SQLCall call) throws IOException {
        String selectStr = selectCallForExist.getSQLString();

        int index = selectStr.toUpperCase().indexOf(" WHERE ");
        if(index < 0) {
            // no where clause - nothing to do
            return false;
        }

        // print the where clause
        String str = selectStr.substring(index);
        writer.write(str);

        // add parameters
        call.getParameters().addAll(selectCall.getParameters());
        call.getParameterTypes().addAll(selectCall.getParameterTypes());            

        return true;
    }
}
