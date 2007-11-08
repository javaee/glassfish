/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

/*
 * Created on April 28, 2005, 5:17 PM
 */


package com.sun.persistence.runtime.sqlstore.sql.update;

import com.sun.forte4j.modules.dbmodel.ColumnElement;

/**
 * This class constructs the SQL INSERT statement for a given table.
 * For each field that is mapped to the table add the column name
 * to the column clause and a '?' parameter for the values clause.
 * @author Pramod Gopinath
 * @author Markus Fuchs
 */
public class InsertSQLBuffer implements SQLBuffer {

    //IF THIS IS CLASS IS CALLED WITHIN AN OUTER SYNCHRONISED BLOCK USE
    //STRINGBUILDER INSTEAD OF StringBuffer (1.5)
    /**
     * Associated table name.
     */
    private String tableName;

    /**
     * Clause with column names to be inserted.
     */
    private StringBuffer columnClause;

    /**
     * Clause with parameter markers corresponding to the column names.
     */
    private StringBuffer valueClause;

    /**
     * Creates a new instance of InsertSQLBuffer.
     * @param tableName The table name associated with this java object.
     */
    public InsertSQLBuffer(String tableName) {
        this.tableName = tableName;
    }

    /**
     * {@inheritDoc}
     */
    public void appendColumn(ColumnElement ce, boolean whereClause) {
        if (whereClause) {
            // Insert statements don't have a WHERE clause
            throw new UnsupportedOperationException(
                    "update.insertSQLBuffer.noWhereClause"); //NOI18N
        }
        addColumn(ce);
    }

    /**
     * Adds the column name to the column clause and a '?' to the values
     * clause.
     * @param ce ColumnElement representing a database column.
     */
    private void addColumn(ColumnElement ce) {
        if (columnClause == null) {
            columnClause = new StringBuffer();
            valueClause = new StringBuffer();
        } else {
            columnClause.append(", "); //NOI18N
            valueClause.append(", "); //NOI18N
        }
        columnClause.append(ce.getName());
        valueClause.append('?'); //NOI18N
    }

    /**
     * Returns the final SQL INSERT statement associated to the table.
     * @return The complete INSERT statement that has been created.
     */
    public String toString() {
        StringBuffer sqlString = new StringBuffer();
        sqlString.append("insert into ").//NOI18N
                append(tableName).append(" (").append(columnClause).append(
                        ')').//NOI18N
                append(" values ").//NOI18N
                append('(').append(valueClause).append(')'); //NOI18N
        return sqlString.toString();
    }

}
