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
 * Created on June 10, 2005, 17:10 PM
 */

package com.sun.persistence.runtime.sqlstore.sql.update;

import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * This class binds foreign key relationships or embedded fields to the sql
 * statement. The binding will be done by storing state fields,
 * e.g. the values corresponding to foreign key columns of the relationship.
 * The implementation is based on the assumption, that there is a one to one
 * correspondence btw. single state and each column of the key on the other
 * relationship side. This field is used to get the value for the foreign key
 * column. Foreign keys relationships are usually mapped to the primary key
 * fields on the other side. Foreign key columns are usually not mapped to
 * state fields, only the relationship is mapped.
 * Relationships or embedded fields might be null. In this case, there is no
 * associated instance. In order to bind null(s) to the prepared statement, we
 * remember the sql column type that corresponds to this parameter.
 * This class is used for relationships mapped to foreign keys or embedded
 * fields. Relationships mapped to a join table can not be handled by this
 * approach.
 * @author Markus Fuchs
 */
public class NullableFieldParameterBinder extends FieldParameterBinder {

   /**
    * Creates a new instance of NullableFieldParameterBinder.
    * @param whereClause true in case we are processing the where clause and
    * false for the set clause.
    * @param numOfFields number of fields to be bound.
    */
    public NullableFieldParameterBinder(boolean whereClause, int numOfFields) {
       super(whereClause, numOfFields);
    }

    /**
     * Binds null values to the statement parameters corresponding to a null
     * foreign key relationship or embedded field. In the foreign key case, the
     * length of the <code>fields</code> array matches exactly the number of
     * parameters to be bound. Multiple columns are bound, if the relationship
     * is mapped to a composite key.
     * @param ps the prepared statement.
     * @param bindingIndex the starting parameter binding index.
     * @return The next parameter index to be bound.
     * @see com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager#addRelationship
     */
    protected int bindNull(PreparedStatement ps, int bindingIndex) {
        for (int i = 0; i < fields.length; i++) {
            try {
                assert 0 != sqlTypes[i] : "Unkonwn column type!"; // NOI18N
                ps.setNull(bindingIndex++, sqlTypes[i]);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
        return bindingIndex;
    }

    /**
     * Appends null(s) to the parameter log corresponding to a null foreign key
     * relationship or embedded field. In the foreign key case, the length of
     * the <code>fields</code> array matches exactly the number of parameters,
     * i.e. columns to be bound. Multiple nulls are added, if the relationship
     * is mapped to a composite key.
     * @param parameterLog <code>StringBuffer</code> for the parameter values.
     * @see com.sun.persistence.runtime.sqlstore.impl.SQLStoreManager#addRelationship
     */
    protected void logNull(StringBuffer parameterLog) {
        for (int i = 0; i < fields.length; i++) {
            appendToLoggingString(parameterLog, null);
        }
    }

}
