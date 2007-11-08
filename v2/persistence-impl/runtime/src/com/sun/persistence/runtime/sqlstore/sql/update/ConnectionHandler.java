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
 * Created on April 21, 2005, 6:06 PM
 */

package com.sun.persistence.runtime.sqlstore.sql.update;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * This class holds a prepared statement and is newly created for each
 * statement.
 * @author Pramod Gopinath
 * @author Markus Fuchs
 */
// TODO: This class should be caching all prepared SQL statements bound to this
// connection/transaction. The caching would be based on the SQL text.
// TODO: Statement batching could be achieved by iterating and executing the
// cached statements at flush time.
public class ConnectionHandler {
    private PreparedStatement ps;
    private Connection connection;

    public ConnectionHandler(Connection connection) {
        this.connection = connection;
    }

    /**
     * For a given sql string get the Prepared Statement.
     * This would allow us to cache the prepared statements thus making
     * use of batching.
     * @param sqlString The SQL String which could be INSERT/DELETE/UPDATE
     * @return
     */
    public PreparedStatement getPreparedStatement(String sqlString)
            throws SQLException {
        if (ps == null) {
            //create the ps using the sql string and store it.
            ps = connection.prepareStatement(sqlString);
        }
        return ps;
    }
}
