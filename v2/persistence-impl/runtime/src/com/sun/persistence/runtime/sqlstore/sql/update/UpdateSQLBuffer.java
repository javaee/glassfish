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
 * This class constructs the SQL UPDATE statement for a given table.
 * For each modified field that is mapped to the table add the column name
 * and a '?' parameter to the SET- clause. Constraining fields are added to
 * the WHERE clause in the same way.
 * @author Pramod Gopinath
 * @author Markus Fuchs
 */
public class UpdateSQLBuffer implements SQLBuffer {

    //IF THIS IS CLASS IS CALLED WITHIN AN OUTER SYNCHRONISED BLOCK USE
    //STRINGBUILDER INSTEAD OF StringBuffer (1.5)
    /**
     * Associated table name.
     */
    private String tableName;

    /**
     * SET clause with columns to be updated.
     */
    private StringBuffer setClause;
    
    /**
     * Clause restricting the update.
     */
    private StringBuffer constraintClause;

    /**
     * Creates a new instance of UpdateSQLBuffer.
     * @param tableName The table name associated with this java object.
     */
    public UpdateSQLBuffer(String tableName) {
        this.tableName = tableName;
    }

    /**
     * {@inheritDoc}
     */
    public void appendColumn(ColumnElement ce, boolean whereClause) {
        if (!whereClause) {
            addColumn(ce);
        } else {
            addConstraint(ce);
        }
    }

    /**
     * Adds the column name and a parameter marker to the SET clause.
     * @param ce {@link ColumnElement} representing the database column.
     */
    private void addColumn(ColumnElement ce) {
        if (setClause == null) {
            setClause = new StringBuffer();
        } else {
            setClause.append(", ");
        }
        setClause.append(ce.getName()).append(" = ?"); //NOI18N
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
     * Returns the final SQL UPDATE statement associated to the table.
     * @return The complete UPDATE statement that has been created.
     */
    public String toString() {
        StringBuffer sqlString = new StringBuffer();
        sqlString.append("update ").//NOI18N
                append(tableName).append(" set ").//NOI18N
                append(setClause).append(" where ").//NOI18N
                append(constraintClause);
        return sqlString.toString();
    }

}
