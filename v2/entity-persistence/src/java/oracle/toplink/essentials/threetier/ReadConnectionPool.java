/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package oracle.toplink.essentials.threetier;

import java.util.*;
import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>:The read connection pool is used for read access through the server session.
 * Any of the connection pools can be used for the read pool however this is the default.
 * This pool allows for concurrent reads against the same JDBC connection and requires that
 * the JDBC connection support concurrent read access.
 */
public class ReadConnectionPool extends ConnectionPool {

    /**
     * PUBLIC:
     * Build a new read connection pool.
     */
    public ReadConnectionPool() {
        super();
    }

    /**
     * PUBLIC:
     * Build a new read connection pool.
     */
    public ReadConnectionPool(String name, Login login, int minNumberOfConnections, int maxNumberOfConnections, ServerSession owner) {
        super(name, login, minNumberOfConnections, maxNumberOfConnections, owner);
    }

    /**
     * INTERNAL:
     * Wait until a connection is avaiable and allocate the connection for the client.
     */
    public synchronized Accessor acquireConnection() throws ConcurrencyException {
        Accessor leastBusyConnection = null;

        // Search for an unused connection, also find the least busy incase all are used.
        for (Enumeration connectionsEnum = getConnectionsAvailable().elements();
                 connectionsEnum.hasMoreElements();) {
            Accessor connection = (Accessor)connectionsEnum.nextElement();
            if (connection.getCallCount() == 0) {
                connection.incrementCallCount(getOwner());
                return connection;
            }
            if ((leastBusyConnection == null) || (leastBusyConnection.getCallCount() > connection.getCallCount())) {
                leastBusyConnection = connection;
            }
        }

        // If still not at max, add a new connection.
        if (getTotalNumberOfConnections() < getMaxNumberOfConnections()) {
            Accessor connection = buildConnection();
            getConnectionsAvailable().addElement(connection);
            connection.incrementCallCount(getOwner());
            return connection;
        }

        // Use the least busy connection.
        leastBusyConnection.incrementCallCount(getOwner());
        return leastBusyConnection;
    }

    /**
     * INTERNAL:
     * Concurrent reads are supported.
     */
    public boolean hasConnectionAvailable() {
        return true;
    }

    /**
     * INTERNAL:
     * Because connections are not exclusive nothing is required.
     */
    public synchronized void releaseConnection(Accessor connection) throws DatabaseException {
        connection.decrementCallCount();
    }
}
