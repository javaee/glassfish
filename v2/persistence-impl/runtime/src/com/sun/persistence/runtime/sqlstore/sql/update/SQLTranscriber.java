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
 * Created on April 25, 2005, 2:58 PM
 */

package com.sun.persistence.runtime.sqlstore.sql.update;

import com.sun.org.apache.jdo.store.Transcriber;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A class implementing this inteface binds a field value to a
 * <code>PreparedStatement</code>. Transcribers are stateless and can be used
 * in parallel.
 * @author Pramod Gopinath
 */
public interface SQLTranscriber extends Transcriber {

    /**
     * Binds a field value to a prepared statement.
     * @param ps the statement being used.
     * @param index the starting parameter index to be bound.
     * @param sqlType the sql column type, used to bind null values.
     * @param value the value to be bound to the statement.
     * @return The next parameter index to be bound.
     * @throws SQLException if a database access error occurs.
     */
    int store(PreparedStatement ps, int index, int sqlType, Object value)
            throws SQLException;

}
