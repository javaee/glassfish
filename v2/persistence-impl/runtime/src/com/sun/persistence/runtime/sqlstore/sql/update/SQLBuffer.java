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
 * Created on April 28, 2005, 5:15 PM
 */


package com.sun.persistence.runtime.sqlstore.sql.update;

import com.sun.forte4j.modules.dbmodel.ColumnElement;

/**
 * @author pramodg
 */
public interface SQLBuffer {

    /**
     * Adds the column name and a parameter marker to the SQL buffer.
     * If the <code>whereClause</code> parameter is true, columns are added
     * to the WHERE clause, otherwise to the SET clause.
     * @param ce {@link ColumnElement} describing a database column.
     * @param whereClause If <code>true</code> the column is bound to the WHERE
     * clause of the SQL statement. If <code>false</code> the column is bound 
     * to the SET clause.
     */
    void appendColumn(ColumnElement ce, boolean whereClause);

}