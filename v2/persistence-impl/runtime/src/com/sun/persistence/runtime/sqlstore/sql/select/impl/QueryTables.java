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
import com.sun.persistence.runtime.sqlstore.sql.impl.ListText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the list of tables for a given query
 * @author Mitesh Meswani
 */
public class QueryTables {
    TableList tableList = new TableList();

    /**
     * Add given table table against given navigation id to the list of
     * tables
     * @param navigationId navigation id associated with the given table
     * @param table the given table
     * @param joinTarget flag Specifying whether the table is a target of a
     * join. This information is not used for now. It will be used once support
     * for joins is added.
     */
    public void add(String navigationId, MappingTable table,
            boolean joinTarget) {
        QueryTable qt = new QueryTable(table, tableList.size());

        //add to the list for this navigationId
        tableList.add(navigationId, qt);
    }

    /**
     * Add an alias for given navigation id. It is assumed that the table list
     * for the query contains an entry for given navigation id
     * @param navigationId given navigation id
     * @param aliasName given alias name
     * @see TableList#addAlias(String, String)
     */
    public void addAlias(String navigationId, String aliasName) {
        tableList.addAlias(aliasName, navigationId);
    }

    /**
     * Get the QueryTable that corresponds to the given table against given
     * navigationId in the list of tableList for the query
     * @param navigationId given navigation id
     * @param table given table
     * @return QueryTable that corresponds to the given table against given
     *         navigationId
     */
    public QueryTable getTable(String navigationId, MappingTable table) {
        return tableList.find(navigationId, table);
    }

    /**
     * Gets the sql text that corresponds to this table list
     * @return
     */
    public StringBuffer getSQLText() {
        return tableList.getSQLText();
    }

    /**
     * This is a helper class to manipulate list of tableList.
     */
    private static class TableList {
        /**
         * Map from navigation id to list of QueryTable. The list of QueryTable
         * for a given entry in the map contains primary and secondary tables
         * for the entity represented by the navigation id
         */
        private Map<String, List<QueryTable>> tables =
                new HashMap<String, List<QueryTable>>();

        /**
         * Total number of tables in this list. Please note that this is not
         * same as total number of entries in the map
         */
        private int size = 0;

        /**
         * Add given table to list of tableList for given navigationId
         * @param navigationId the given navigation id
         * @param table given table
         */
        void add(String navigationId, QueryTable table) {
            List<QueryTable> tableList = tables.get(navigationId);
            if (tableList == null) {
                tableList = new ArrayList<QueryTable>();
                tables.put(navigationId, tableList);
            }
            tableList.add(table);
            size++;
        }

        /**
         * Finds QueryTable corresponding to given table in list of tableList
         * for given navigation id
         * @param navigationId the given navigationId
         * @param table the given table
         * @return QueryTable corresponding to given table in list of tableList
         *         for given navigation id. Null if there is no list of
         *         tableList for given the navigationId or the list does not
         *         contain given table
         */
        QueryTable find(String navigationId, MappingTable table) {
            QueryTable queryTable = null;
            List<QueryTable> tableListForNavigationId = tables.get(
                    navigationId);
            if (tableListForNavigationId != null) {
                for (QueryTable tableFromList : tableListForNavigationId) {
                    if (tableFromList.compareMappingTable(table)) {
                        queryTable = tableFromList;
                        break;
                    }
                }
            }
            return queryTable;
        }

        /**
         * Adds alias for the given navigationId.
         * @param aliasName the given alias
         * @param navigationId the given navigationId.
         */
        void addAlias(String aliasName, String navigationId) {
            List<QueryTable> tableList = tables.get(navigationId);
            //The navigationId must be present in the tableList
            assert (tableList != null);
            tables.put(aliasName, tableList);
        }

        /**
         * Total number of tables in this list. Please note that this is not
         * same as total number of entries in the map
         * @return Total number of tables in this list
         */
        int size() {
            return size;
        }

        /**
         * Get Sql text that corresponds to <code>tableList</code>. The sql text
         * will be part of from clause.
         * @return Sql text that corresponds to <code>tableList</code>
         */
        StringBuffer getSQLText() {
            ListText fromListText = new ListText();
            Set<String> mapKeys = tables.keySet();
            for (String key : mapKeys) {
                List<QueryTable> tableList = tables.get(key);
                for (QueryTable table : tableList) {
                    StringBuffer tableText = table.getSQLTextFromClause();
                    fromListText.append(tableText);
                }
            }
            return fromListText.getBackingStringBuffer();
        }
    }

}