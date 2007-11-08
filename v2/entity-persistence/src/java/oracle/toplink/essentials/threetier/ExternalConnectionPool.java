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

import oracle.toplink.essentials.internal.databaseaccess.*;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: This subclass is intended to be used with external connection pools.
 * For these pools, TopLink does not control the pooling behaviour.
 * The login should have the usesExternalConnectionPooling set to "true".
 */
public class ExternalConnectionPool extends ConnectionPool {
    protected Accessor cachedConnection;

    /**
     * PUBLIC:
     * Build a new external connection pool.  The JDBC driver is responsible for pooling the connections.
     */
    public ExternalConnectionPool() {
        super();
    }

    /**
     * PUBLIC:
     * Build a new external connection pool.  The JDBC driver is responsible for pooling the connections.
     */
    public ExternalConnectionPool(String name, Login login, ServerSession owner) {
        super(name, login, 0, 0, owner);
    }

    /**
     * INTERNAL:
     * When we acquire a connection from an ExternalConnectionPool we build
     * a new connection (retrieve it from the external pool).
     */
    public synchronized Accessor acquireConnection() throws ConcurrencyException {
        return (Accessor)getCachedConnection().clone();
    }

    /**
     *  INTERNAL:
     *  Return the currently cached connection to the external connection pool
     *  @return oracle.toplink.essentials.internal.databaseaccess.Accessor
     */
    protected Accessor getCachedConnection() {
        return cachedConnection;
    }

    /**
     * INTERNAL:
     * Assume true as the driver is responsible for blocking.
     */
    public boolean hasConnectionAvailable() {
        return true;
    }

    /**
     * INTERNAL:
     * Checks for a conflict between pool's type and pool's login
     */
    public boolean isThereConflictBetweenLoginAndType() {
        return !getLogin().shouldUseExternalConnectionPooling();
    }

    /**
     * INTERNAL:
     * When you release an external connection, you simply let it go.
     */
    public synchronized void releaseConnection(Accessor connection) throws DatabaseException {
        getConnectionsUsed().removeElement(connection);
        connection.closeConnection();
        notify();
    }

    /**
     *  Set the currently cached connection to the external connection pool.
     *  @param oracle.toplink.essentials.internal.databaseaccess.Accessor
     */
    protected void setCachedConnection(Accessor cachedConnection) {
        this.cachedConnection = cachedConnection;
    }

    /**
     * INTERNAL:
     * This mehtod is a no-op for external pools.
     */
    public synchronized void shutDown() {
        //do nothing
        setIsConnected(false);
    }

    /**
     * INTERNAL:
     * Build the default connection.
     * This validates that connect will work and sets up the parent accessor to clone.
     */
    public synchronized void startUp() {
        setCachedConnection(buildConnection());
        setIsConnected(true);
    }
}
