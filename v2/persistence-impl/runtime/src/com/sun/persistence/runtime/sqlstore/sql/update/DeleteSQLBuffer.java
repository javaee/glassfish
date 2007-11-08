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
 * This class constructs the SQL DELETE statement for a given table. 
 * For each field restricting the delete adds the appropriate column name
 * to the WHERE clause and a '?' parameter for the actual value.
 * @author Pramod Gopinath
 * @author Markus Fuchs
 */
public class DeleteSQLBuffer implements SQLBuffer {

    //IF THIS IS CLASS IS CALLED WITHIN AN OUTER SYNCHRONISED BLOCK USE
    //STRINGBUILDER INSTEAD OF StringBuffer (1.5)
    /**
     * Associated table name.
     */
    private String tableName;

    /**
     * Clause restricting the delete.
     */
    private StringBuffer constraintClause;

    /**
     * Creates a new instance of DeleteSQLBuffer.
     * @param tableName The table name associated with this java object.
     */
    public DeleteSQLBuffer(String tableName) {
        this.tableName = tableName;
    }

    /**
     * {@inheritDoc}
     */
    public void appendColumn(ColumnElement ce, boolean whereClause) {
        if (!whereClause) {
            // Delete statements don't have a SET clause
            throw new UnsupportedOperationException(
                    "update.deleteSQLBuffer.setClause"); //NOI18N
        }
        addConstraint(ce);
    }

    /**
     * Adds the column name and a parameter marker to the WHERE clause.
     * @param ce {@link ColumnElement} representing the database column.
     */
    private void addConstraint(ColumnElement ce) {
        if (constraintClause == null) {
            constraintClause = new StringBuffer();
        } else {
            constraintClause.append(" and "); //NOI18N
        }
        constraintClause.append(ce.getName()).append(" = ?"); //NOI18N
    }

    /**
     * Returns the final SQL DELETE statement associated to the table.
     * @return The complete DELETE statement that has been created.
     */
    public String toString() {
        StringBuffer sqlString = new StringBuffer();
        sqlString.append("delete from ").//NOI18N
                append(tableName).append(" where ").//NOI18N
                append(constraintClause);
        return sqlString.toString();
    }

}
