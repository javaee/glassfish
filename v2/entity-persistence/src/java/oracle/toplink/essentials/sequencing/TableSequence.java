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
package oracle.toplink.essentials.sequencing;

import java.io.StringWriter;
import oracle.toplink.essentials.internal.helper.DatabaseTable;
import oracle.toplink.essentials.queryframework.*;

/**
 * <p>
 * <b>Purpose</b>:
 * <p>
 */
public class TableSequence extends QuerySequence {
    /** Default sequence table name */
    protected static final String defaultTableName = "SEQUENCE";
    
    /** Hold the database table */
    protected DatabaseTable table;

    /** Hold the name of the column in the sequence table which specifies the sequence numeric value */
    protected String counterFieldName = "SEQ_COUNT";

    /** Hold the name of the column in the sequence table which specifies the sequence name */
    protected String nameFieldName = "SEQ_NAME";
    protected String qualifier = "";

    public TableSequence() {
        super(false, true);
        setTableName(defaultTableName);
    }

    public TableSequence(String name) {
        super(name, false, true);
        setTableName(defaultTableName);
    }

    public TableSequence(String name, int size) {
        super(name, size, false, true);
        setTableName(defaultTableName);
    }
    
    public TableSequence(String name, int size, int initialValue) {
        super(name, size, initialValue, false, true);
        setTableName(defaultTableName);
    }

    public TableSequence(String name, String tableName) {
        this(name);
        setTableName(tableName);
    }

    public TableSequence(String name, String tableName, String nameFieldName, String counterFieldName) {
        this(name);
        setTableName(tableName);
        setNameFieldName(nameFieldName);
        setCounterFieldName(counterFieldName);
    }

    public TableSequence(String name, int size, String tableName) {
        this(name, size);
        setTableName(tableName);
    }

    public TableSequence(String name, int size, String tableName, String nameFieldName, String counterFieldName) {
        this(name, size);
        setTableName(tableName);
        setNameFieldName(nameFieldName);
        setCounterFieldName(counterFieldName);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TableSequence) {
            TableSequence other = (TableSequence)obj;
            if (equalNameAndSize(this, other)) {
                return getTableName().equals(other.getTableName()) && getCounterFieldName().equals(other.getCounterFieldName()) && getNameFieldName().equals(other.getNameFieldName());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public String getCounterFieldName() {
        return counterFieldName;
    }

    public void setCounterFieldName(String name) {
        counterFieldName = name;
    }

    public String getNameFieldName() {
        return nameFieldName;
    }

    public void setNameFieldName(String name) {
        nameFieldName = name;
    }

    public DatabaseTable getTable() {
        return table;
    }
    
    public String getTableName() {
        return getTable().getQualifiedName();
    }

    public String getQualifiedTableName() {
        if (qualifier.equals("")) {
            return getTableName();
        } else {
            return qualifier + "." + getTableName();
        }
    }

    public void setTable(DatabaseTable table) {
        this.table = table;
    }
    
    public void setTableName(String name) {
        table = new DatabaseTable(name);
    }

    protected ValueReadQuery buildSelectQuery() {
        ValueReadQuery query = new ValueReadQuery();
        query.addArgument(getNameFieldName());
        StringWriter writer = new StringWriter();
        writer.write("SELECT " + getCounterFieldName());
        writer.write(" FROM " + getQualifiedTableName());
        writer.write(" WHERE " + getNameFieldName());
        writer.write(" = #" + getNameFieldName());
        query.setSQLString(writer.toString());

        return query;
    }

    protected DataModifyQuery buildUpdateQuery() {
        DataModifyQuery query = new DataModifyQuery();
        query.addArgument(getNameFieldName());
        query.addArgument("PREALLOC_SIZE");
        StringWriter writer = new StringWriter();
        writer.write("UPDATE " + getQualifiedTableName());
        writer.write(" SET " + getCounterFieldName());
        writer.write(" = " + getCounterFieldName());
        writer.write(" + #PREALLOC_SIZE");
        writer.write(" WHERE " + getNameFieldName() + " = #" + getNameFieldName());
        query.setSQLString(writer.toString());

        return query;
    }

    /**
    * INTERNAL:
    */
    public void onConnect() {
        super.onConnect();
        qualifier = getDatasourcePlatform().getTableQualifier();
    }

    /**
    * INTERNAL:
    */
    public void onDisconnect() {
        qualifier = "";
        super.onDisconnect();
    }
}
