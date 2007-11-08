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
import oracle.toplink.essentials.expressions.Expression;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.databaseaccess.DatabaseCall;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: Print DELETE statement with non trivial WHERE clause
 * <p><b>Responsibilities</b>:<ul>
 * <li> Print DELETE statement.
 * </ul>
 * @author Andrei Ilitchev
 * @since TOPLink 10.1.3
 */
public class SQLDeleteAllStatement extends SQLDeleteStatement {

    protected Expression inheritanceExpression;

    protected SQLCall selectCallForExist;
    protected String tableAliasInSelectCallForExist;

    protected SQLCall selectCallForNotExist;
    protected String tableAliasInSelectCallForNotExist;
    
    // A pair of Vectors for join expression
    protected Vector aliasedFields;
    protected Vector originalFields;
    
    protected boolean shouldExtractWhereClauseFromSelectCallForExist;
    
    public void setSelectCallForExist(SQLCall selectCallForExist) {
        this.selectCallForExist = selectCallForExist;
    }
    public SQLCall getSelectCallForExist() {
        return selectCallForExist;
    }
    public void setSelectCallForNotExist(SQLCall selectCallForNotExist) {
        this.selectCallForNotExist = selectCallForNotExist;
    }
    public SQLCall getSelectCallForNotExist() {
        return selectCallForNotExist;
    }
    public void setTableAliasInSelectCallForExist(String tableAliasInSelectCallForExist) {
        this.tableAliasInSelectCallForExist = tableAliasInSelectCallForExist;
    }
    public String getTableAliasInSelectCallForExist() {
        return tableAliasInSelectCallForExist;
    }
    public void setTableAliasInSelectCallForNotExist(String tableAliasInSelectCallForNotExist) {
        this.tableAliasInSelectCallForNotExist = tableAliasInSelectCallForNotExist;
    }
    public String getTableAliasInSelectCallForNotExist() {
        return tableAliasInSelectCallForNotExist;
    }
    public void setPrimaryKeyFieldsForAutoJoin(Collection primaryKeyFields) {
        if(primaryKeyFields != null) {
            if(primaryKeyFields instanceof Vector) {
                setOriginalFieldsForJoin((Vector)primaryKeyFields);
            } else {
                setOriginalFieldsForJoin(new Vector(primaryKeyFields));
            }
            setAliasedFieldsForJoin((Vector)getOriginalFieldsForJoin().clone());
        } else {
            setOriginalFieldsForJoin(null);
            setAliasedFieldsForJoin(null);
        }
    }
    public void setOriginalFieldsForJoin(Vector originalFields) {
        this.originalFields = originalFields;
    }
    public Vector getOriginalFieldsForJoin() {
        return originalFields;
    }
    public void setAliasedFieldsForJoin(Vector aliasedFields) {
        this.aliasedFields = aliasedFields;
    }
    public Vector getAliasedFieldsForExpression() {
        return aliasedFields;
    }
    public void setInheritanceExpression(Expression inheritanceExpression) {
        this.inheritanceExpression = inheritanceExpression;
    }
    public Expression getInheritanceExpression() {
        return inheritanceExpression;
    }
    
    public void setShouldExtractWhereClauseFromSelectCallForExist(boolean shouldExtractWhereClauseFromSelectCallForExist) {
        this.shouldExtractWhereClauseFromSelectCallForExist = shouldExtractWhereClauseFromSelectCallForExist;
    }
    public boolean shouldExtractWhereClauseFromSelectCallForExist() {
        return shouldExtractWhereClauseFromSelectCallForExist;
    }

    /**
     * Return SQL call for the statement, through generating the SQL string.
     */
    public DatabaseCall buildCall(AbstractSession session) {
        SQLCall call = (SQLCall)super.buildCall(session);

        Writer writer = new CharArrayWriter(100);
        try {
            // because where clause is null, 
            // call.sqlString == "DELETE FROM getTable().getQualifiedName()"
            writer.write(call.getSQLString());

            boolean whereWasPrinted = true;
            if(selectCallForExist != null) {
                if(shouldExtractWhereClauseFromSelectCallForExist) {
                    // Should get here only in case selectCallForExist doesn't have aliases and 
                    // targets the same table as the statement.
                    // Instead of making selectCallForExist part of " WHERE EXIST("
                    // just extract its where clause.
                    // Example: selectCallForExist.sqlString:
                    // "SELECT PROJ_ID FROM PROJECT WHERE (LEADER_ID IS NULL)
                    whereWasPrinted = writeWhere(writer, selectCallForExist, call);
                    // The result is:
                    // "WHERE (LEADER_ID IS NULL)"
                } else {
                    writer.write(" WHERE EXISTS(");
                    // EXIST Example: selectCallForExist.sqlString:
                    // "SELECT t0.EMP_ID FROM EMPLOYEE t0, SALARY t1 WHERE (((t0.F_NAME LIKE 'a') AND (t1.SALARY = 0)) AND (t1.EMP_ID = t0.EMP_ID))"
                    writeSelect(writer, selectCallForExist, tableAliasInSelectCallForExist, call);
                    // closing bracket for EXISTS
                    writer.write(")");
                    // The result is (target table is SALARY):
                    // "WHERE EXISTS(SELECT t0.EMP_ID FROM EMPLOYEE t0, SALARY t1 WHERE (((t0.F_NAME LIKE 'a') AND (t1.SALARY = 0)) AND (t1.EMP_ID = t0.EMP_ID)) AND t1.EMP_ID = SALARY.EMP_ID)"
                }
            } else if (inheritanceExpression != null) {
                writer.write(" WHERE ");
                // Example: (PROJ_TYPE = 'L')
                ExpressionSQLPrinter printer = new ExpressionSQLPrinter(session, getTranslationRow(), call, false);
                printer.setWriter(writer);
                printer.printExpression(inheritanceExpression);
                // The result is:
                // "(PROJ_TYPE = 'L')"
            } else {
                whereWasPrinted = false;
            }

            if(selectCallForNotExist != null) {
                if(whereWasPrinted) {
                    writer.write(" AND");
                } else {
                    writer.write(" WHERE");
                }
                writer.write(" NOT EXISTS(");
                // NOT EXIST Example: selectCall.sqlString:
                // "SELECT t0.EMP_ID FROM EMPLOYEE t0, SALARY t1 WHERE (t1.EMP_ID = t0.EMP_ID)"
                writeSelect(writer, selectCallForNotExist, tableAliasInSelectCallForNotExist, call);
                // closing bracket for EXISTS
                writer.write(")");
                // The result is (target table is EMPLOYEE):
                // "WHERE NOT EXISTS(SELECT t0.EMP_ID FROM EMPLOYEE t0, SALARY t1 WHERE ((t1.EMP_ID = t0.EMP_ID)) AND t0.EMP_ID = EMPLOYEE.EMP_ID)"
            }            

            call.setSQLString(writer.toString());
            
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
                
        return call;
    }
    
    protected void writeSelect(Writer writer, SQLCall selectCall, String tableAliasInSelectCall, SQLCall call) throws IOException {
        String str = selectCall.getSQLString();
        writer.write(str);
        
        boolean hasWhereClause = str.toUpperCase().indexOf(" WHERE ") >= 0;

        // join aliased fields to original fields                                               
        // Examples:
        //   table aliase provided: AND t0.EMP_ID = EMPLOYEE.EMP_ID
        //   table aliase not provided: AND EMP_ID = EMPLOYEE.EMP_ID
        for(int i=0; i < originalFields.size(); i++) {
            if(i==0 && !hasWhereClause) {
            // there is no where clause - should print WHERE
                writer.write(" WHERE ");
            } else {
                writer.write(" AND ");
            }
            if(tableAliasInSelectCall != null) {
                writer.write(tableAliasInSelectCall);
                writer.write('.');
            }
            writer.write(((DatabaseField)aliasedFields.elementAt(i)).getName());
            writer.write(" = ");
            writer.write(table.getQualifiedName());
            writer.write('.');
            writer.write(((DatabaseField)originalFields.elementAt(i)).getName());
        }

        // add parameters
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
