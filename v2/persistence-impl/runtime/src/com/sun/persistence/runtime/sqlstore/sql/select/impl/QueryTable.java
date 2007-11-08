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


package com.sun.persistence.runtime.sqlstore.sql.select.impl;

import com.sun.persistence.api.model.mapping.MappingTable;

/**
 * Represents a table alias in a select query.
 * @author Mitesh Meswani
 */
public class QueryTable {
    /**
     * The underlying MappingTable
     */
    private MappingTable table;

    /**
     * Index of this table. Used for generating correlation id
     */
    private int index;

    //Uncomment this when joins need to be handled
    //List<JoinTargetQueryTable> nextTables;

    public QueryTable(MappingTable table, int index) {
        this.table = table;
        this.index = index;
    }

    /**
     * Get the correlation id for this table.
     * @return Get correlation id for this table.
     */
    public String getCorrelationId() {
        return "t" + index; //NOI18N
    }

    /**
     * Gets sql text that will be part of the from clause for this table
     * @return sql text that will be part of the from clause for this table
     */
    public StringBuffer getSQLTextFromClause() {
        StringBuffer sqlText = new StringBuffer(table.getName());
        sqlText.append(' ').append(getCorrelationId());
        // Join handling.
        //        if(nextTables != null) {
        //            String onClause = getOnClauseForNextTables();
        //        }
        return sqlText;
    }

    // Join handling
    //    String getOnClauseForNextTables() {
    //        iterate through nextTables and append corresponding on clause
    //    }

    /**
     * Compares whether the underlying MappingTable is same as the given
     * <code>table</code>
     * @param table The given table
     * @return true if the tables are same, false otherwise.
     */
    public boolean compareMappingTable(MappingTable table) {
        return this.table.equals(table);
    }
}
