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
import java.util.Vector;
import java.util.Collection;

import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.databaseaccess.DatabaseCall;
import oracle.toplink.essentials.internal.helper.DatabaseField;
import oracle.toplink.essentials.internal.helper.DatabaseTable;
import java.util.Map;

/**
 * @author Andrei Ilitchev
 * @since TOPLink/Java 1.0
 */
public class SQLUpdateAllStatementForOracleAnonymousBlock extends SQLModifyStatement {
    protected HashMap tables_databaseFieldsToValues;
    protected HashMap tablesToPrimaryKeyFields;
    protected SQLCall selectCall;
    
    protected static final String varSuffix = "_VAR";
    protected static final String typeSuffix = "_TYPE";
    protected static final String tab = "   ";
    protected static final String dbltab = tab + tab;
    protected static final String trpltab = dbltab + tab;
    
    public void setSelectCall(SQLCall selectCall) {
        this.selectCall = selectCall;
    }
    public SQLCall getSelectCall() {
        return selectCall;
    }
    public void setTablesToPrimaryKeyFields(HashMap tablesToPrimaryKeyFields) {
        this.tablesToPrimaryKeyFields = tablesToPrimaryKeyFields;
    }
    public HashMap getTablesToPrimaryKeyFields() {
        return tablesToPrimaryKeyFields;
    }
    public void setTables_databaseFieldsToValues(HashMap tables_databaseFieldsToValues) {
        this.tables_databaseFieldsToValues = tables_databaseFieldsToValues;
    }
    public HashMap getTables_databaseFieldsToValues() {
        return tables_databaseFieldsToValues;
    }

    /**
     * Append the string containing the SQL insert string for the given table.
     */
    public DatabaseCall buildCall(AbstractSession session) {
        SQLCall call = new SQLCall();
        call.returnNothing();
        
        Writer writer = new CharArrayWriter(100);
        
        Vector mainPrimaryKeys = new Vector();
        mainPrimaryKeys.addAll((Collection)tablesToPrimaryKeyFields.get(table));
        
        Vector allFields = (Vector)mainPrimaryKeys.clone();
        Iterator itDatabaseFieldsToValues = tables_databaseFieldsToValues.values().iterator();
        while(itDatabaseFieldsToValues.hasNext()) {
            Iterator itDatabaseFields = ((HashMap)itDatabaseFieldsToValues.next()).keySet().iterator();
            while(itDatabaseFields.hasNext()) {
                allFields.addElement(itDatabaseFields.next());
            }
        }
        
        try {
            //DECLARE
            writer.write("DECLARE\n");
            
            for(int i=0; i < allFields.size(); i++) {
                writeDeclareTypeAndVar(writer, (DatabaseField)allFields.elementAt(i));
            }

            //BEGIN
            writer.write("BEGIN\n");

            //  select t0.emp_id, concat('Even', t0.f_name), t1.salary + 1000 BULK COLLECT into EMPLOYEEE_EMP_ID_VAR, EMPLOYEEE_F_NAME_VAR, SALARY_SALARY_VAR from employee t0, salary t1 where t0.l_name like 'updateEmployeeTestUsingTempTable' and t0.f_name in ('0', '2') and t1.salary = 0 and t0.emp_id = t1.emp_id;
            String selectStr = selectCall.getSQLString();
            int index = selectStr.toUpperCase().indexOf(" FROM ");
            String firstPart = selectStr.substring(0, index);
            String secondPart = selectStr.substring(index, selectStr.length());
            
            writer.write(tab);
            writer.write(firstPart);
            writer.write(" BULK COLLECT INTO ");

            for(int i=0; i < allFields.size(); i++) {
                writeVar(writer, (DatabaseField)allFields.elementAt(i));
                if(i < allFields.size() - 1) {
                    writer.write(", ");
                }
            }
            writer.write(secondPart);
            writer.write(";\n");

            call.getParameters().addAll(selectCall.getParameters());
            call.getParameterTypes().addAll(selectCall.getParameterTypes());            

            DatabaseField firstMainPrimaryKey = (DatabaseField)mainPrimaryKeys.firstElement();
            writer.write(tab);
            writer.write("IF ");
            writeVar(writer, firstMainPrimaryKey);
            writer.write(".COUNT > 0 THEN\n");
            
            Iterator itEntries = tables_databaseFieldsToValues.entrySet().iterator();
            while(itEntries.hasNext()) {
                writeForAll(writer, firstMainPrimaryKey);
                writer.write(trpltab);
                writer.write("UPDATE ");
                Map.Entry entry = (Map.Entry)itEntries.next();
                DatabaseTable t = (DatabaseTable)entry.getKey();
                writer.write(t.getQualifiedName());
                writer.write(" SET ");
                HashMap databaseFieldsToValues = (HashMap)entry.getValue();
                int counter = 0;
                Iterator itDatabaseFields = databaseFieldsToValues.keySet().iterator();
                while(itDatabaseFields.hasNext()) {
                    counter++;
                    DatabaseField field = (DatabaseField)itDatabaseFields.next();
                    writer.write(field.getName());
                    writer.write(" = ");
                    writeVar(writer, field);
                    writer.write("(i)");
                    if(counter < databaseFieldsToValues.size()) {
                        writer.write(", ");
                    }
                }
                
                writer.write(" WHERE ");
                
                Vector tablePrimaryKeys = new Vector();
                tablePrimaryKeys.addAll((Collection)tablesToPrimaryKeyFields.get(t));
                for(int i=0; i < mainPrimaryKeys.size(); i++) {
                    DatabaseField tableField = (DatabaseField)tablePrimaryKeys.elementAt(i);
                    writer.write(tableField.getName());
                    writer.write(" = ");
                    DatabaseField mainField = (DatabaseField )mainPrimaryKeys.elementAt(i);
                    writeVar(writer, mainField);
                    writer.write("(i)");
                    if(i < mainPrimaryKeys.size()-1) {
                        writer.write(" AND ");
                    } else {
                        writer.write(";\n");
                    }
                }
            }

            writer.write(tab);
            writer.write("END IF;\n");
            
            writer.write(tab);
            DatabaseField outField = new DatabaseField("ROW_COUNT");
            outField.setType(Integer.class);
            call.appendOut(writer, outField);
            writer.write(" := ");
            writeVar(writer, firstMainPrimaryKey);
            writer.write(".COUNT;\n");
            
            writer.write("END;");

            call.setSQLString(writer.toString());
            
        } catch (IOException exception) {
            throw ValidationException.fileError(exception);
        }
                
        return call;
    }    
    
    protected static void writeUniqueFieldName(Writer writer, DatabaseField field) throws IOException {
        // EMPLOYEE_EMP_ID
        writer.write(field.getTableName());
        writer.write("_");
        writer.write(field.getName());
    }
    
    protected static void writeType(Writer writer, DatabaseField field) throws IOException {
        // EMPLOYEE_EMP_ID_TYPE
        writeUniqueFieldName(writer, field);
        writer.write(typeSuffix);
    }
    
    protected static void writeVar(Writer writer, DatabaseField field) throws IOException {
        // EMPLOYEE_EMP_ID_VAR 
        writeUniqueFieldName(writer, field);
        writer.write(varSuffix);
    }
    
    protected static void writeDeclareTypeAndVar(Writer writer, DatabaseField field) throws IOException {
        //  TYPE EMPLOYEE_EMP_ID_TYPE IS TABLE OF EMPLOYEE.EMP_ID%TYPE;
        writer.write(tab);
        writer.write("TYPE ");
        writeType(writer, field);
        writer.write(" IS TABLE OF ");
        writer.write(field.getQualifiedName());
        writer.write("%TYPE;\n");
        
        //  EMPLOYEE_EMP_ID_VAR EMP_ID_TYPE;
        writer.write(tab);
        writeVar(writer, field);
        writer.write(" ");
        writeType(writer, field);
        writer.write(";\n");
    }

    protected static void writeForAll(Writer writer, DatabaseField field) throws IOException {
        //FORALL i IN EMPLOYEE_EMP_ID_VAR.FIRST..EMPLOYEE_EMP_ID_VAR.LAST
        writer.write(dbltab);
        writer.write("FORALL i IN ");
        writeVar(writer, field);
        writer.write(".FIRST..");
        writeVar(writer, field);
        writer.write(".LAST\n");
    }
}
