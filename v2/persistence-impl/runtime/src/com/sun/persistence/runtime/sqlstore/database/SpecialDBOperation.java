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

package com.sun.persistence.runtime.sqlstore.database;

import java.util.List;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;

/**
 * SpecialDBOperation interface is defined for database specific operations.
 * @author Shing Wai Chan
 */
public interface SpecialDBOperation {

    /**
     * This method is called immediately after an instance implementing this
     * interface is created. The implementation can initialize itself using
     * supplied metaData.
     * @param metaData DatbaseMetaData of the database for which an instance
     * implementing this interface is ingratiated.
     * @param identifier Identifier of object used to obtain databaseMetaData.
     * This can be null in non managed environment.
     */
    public void initialize(DatabaseMetaData metaData,
        String identifier) throws SQLException;
    /**
     * Defines column type for result.
     * @param ps java.sql.PreparedStatement
     * @param columns List of ColumnElement corresponding to select clause
     */
    public void defineColumnTypeForResult(
        PreparedStatement ps, List columns) throws SQLException;

    /**
     * Binds specified value to parameter at specified index that is bound to
     * CHAR column.
     * @param ps java.sql.PreparedStatement
     * @param index Index of paramater marker in <code>ps</code>.
     * @param strVal value that needs to bound.
     * @param length length of the column to which strVal is bound.
     */
    public void bindFixedCharColumn(PreparedStatement ps,
         int index, String strVal, int length) throws SQLException;

}
