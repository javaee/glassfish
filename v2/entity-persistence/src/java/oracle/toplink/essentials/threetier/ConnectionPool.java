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
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.localization.*;

/**
 * <p>
 * <b>Purpose</b>: Used to specify how connection should be pooled in a server session.
 * @see ServerSession
 */
public class ConnectionPool {
    protected boolean isConnected;
    protected int maxNumberOfConnections;
    protected int minNumberOfConnections;
    protected Vector connectionsAvailable;
    protected Vector connectionsUsed;
    protected Login login;
    protected String name;
    protected ServerSession owner;

    /**
     * PUBLIC:
     * A connection pool is used to specify how connection should be pooled in a server session.
     */
    public ConnectionPool() {
        this.maxNumberOfConnections = 50;
        this.minNumberOfConnections = 3;
        resetConnections();
    }

    /**
     * PUBLIC:
     * A connection pool is used to specify how connection should be pooled in a server session.
     */
    public ConnectionPool(String name, Login login, int minNumberOfConnections, int maxNumberOfConnections, ServerSession owner) {
        this.login = login;
        this.owner = owner;
        this.name = name;
        this.maxNumberOfConnections = maxNumberOfConnections;
        this.minNumberOfConnections = minNumberOfConnections;
        resetConnections();
    }

    /**
     * INTERNAL:
     * Wait until a connection is avaiable and allocate the connection for the client.
     */
    public synchronized Accessor acquireConnection() throws ConcurrencyException {
        while (!hasConnectionAvailable()) {
            if (getTotalNumberOfConnections() < getMaxNumberOfConnections()) {
                Accessor connection = buildConnection();
                getConnectionsUsed().addElement(connection);
                return connection;
            }
            try {
                wait();// Notify is called when connections are released.
            } catch (InterruptedException exception) {
                throw ConcurrencyException.waitFailureOnClientSession(exception);
            }
        }

        Accessor connection = (Accessor)getConnectionsAvailable().firstElement();
        getConnectionsAvailable().removeElement(connection);
        getConnectionsUsed().addElement(connection);

        getOwner().updateProfile(getName(), new Integer(getConnectionsUsed().size()));
        return connection;
    }

    /**
     * INTERNAL:
     * Create a new connection, accessors are used as connections.
     */
    protected Accessor buildConnection() {
        Login localLogin = (Login)getLogin().clone();
        Accessor connection = localLogin.buildAccessor();
        connection.connect(localLogin, getOwner());

        return connection;
    }

    /**
     * INTERNAL:
     * returns the connections currently available for use in the pool
     */
    public Vector getConnectionsAvailable() {
        return connectionsAvailable;
    }

    /**
     *  Return a list of the connections that are being used.
     *  @return java.util.Vector
     **/
    protected Vector getConnectionsUsed() {
        return connectionsUsed;
    }

    /**
     * PUBLIC:
     * Return the login used to create connections.
     */
    public Login getLogin() {
        return login;
    }

    /**
     * PUBLIC:
     * Return the maximum number of connections allowed.
     * When the max is reached clients must wait for a connection to become available.
     */
    public int getMaxNumberOfConnections() {
        return maxNumberOfConnections;
    }

    /**
     * PUBLIC:
     * Return the minimum number of connections.
     * These connection will be create on startup.
     */
    public int getMinNumberOfConnections() {
        return minNumberOfConnections;
    }

    /**
     * PUBLIC:
     * Return the name of this pool.
     * Pools are identified by name to allow multiple connection pools.
     */
    public String getName() {
        return name;
    }

    /**
     *  Return the ServerSession that is the owner of this connection pool.
     *  @return oracle.toplink.essentials.threetier.ServerSession
     */
    protected ServerSession getOwner() {
        return owner;
    }

    /**
     * INTERNAL:
     * Return the total number of connections currently in use.
     */
    public int getTotalNumberOfConnections() {
        return getConnectionsUsed().size() + getConnectionsAvailable().size();
    }

    /**
     * INTERNAL:
     * Wait until a connection is avaiable and allocate the connection for the client.
     */
    public boolean hasConnectionAvailable() {
        return !getConnectionsAvailable().isEmpty();
    }

    /**
     * INTERNAL:
     * Return if this pool has been connected to the database.
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * INTERNAL:
     * Checks for a conflict between pool's type and pool's login
     */
    public boolean isThereConflictBetweenLoginAndType() {
        return getLogin().shouldUseExternalConnectionPooling();
    }

    /**
     * INTERNAL:
     * Add the connection as single that a new connection is available.
     */
    public synchronized void releaseConnection(Accessor connection) throws DatabaseException {
        getConnectionsUsed().removeElement(connection);

        if (getTotalNumberOfConnections() < getMinNumberOfConnections()) {
            getConnectionsAvailable().addElement(connection);
        } else {
            connection.disconnect(getOwner());
        }

        notify();
    }

    /**
     * INTERNAL:
     * Reset the connections on shutDown and when the pool is started.
     */
    public void resetConnections() {
        this.connectionsUsed = new Vector();
        this.connectionsAvailable = new Vector();
    }

    /**
     *  INTERNAL:
     *  Set this list of connections available
     *  @param java.util.Vector
     */
    protected void setConnectionsAvailable(Vector connectionsAvailable) {
        this.connectionsAvailable = connectionsAvailable;
    }

    /**
     *  INTERNAL:
     *  Set the list of connections being used.
     *  @param java.util.Vector
     */
    protected void setConnectionsUsed(Vector connectionsUsed) {
        this.connectionsUsed = connectionsUsed;
    }

    /**
     * INTERNAL:
     * Set if this pool has been connected to the database.
     */
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * PUBLIC:
     * Set the login used to create connections.
     */
    public void setLogin(Login login) {
        this.login = login;
    }

    /**
     * PUBLIC:
     * Set the maximum number of connections allowed.
     * When the max is reached clients must wait for a connection to become available.
     */
    public void setMaxNumberOfConnections(int maxNumberOfConnections) {
        this.maxNumberOfConnections = maxNumberOfConnections;
    }

    /**
     * PUBLIC:
     * Set the minimum number of connections.
     * These connection will be create on startup.
     */
    public void setMinNumberOfConnections(int minNumberOfConnections) {
        this.minNumberOfConnections = minNumberOfConnections;
    }

    /**
     * PUBLIC:
     * Set the name of this pool.
     * Pools are identified by name to allow multiple connection pools.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *  Set the ServerSession that owns this connection pool
     *  @param oracle.toplink.essentials.threetier.ServerSession
     */
    protected void setOwner(ServerSession owner) {
        this.owner = owner;
    }

    /**
     * INTERNAL:
     * Disconnect all connections.
     */
    public synchronized void shutDown() {
        setIsConnected(false);

        for (Enumeration avaiableEnum = getConnectionsAvailable().elements();
                 avaiableEnum.hasMoreElements();) {
            try {
                ((Accessor)avaiableEnum.nextElement()).disconnect(getOwner());
            } catch (DatabaseException exception) {
                // Ignore.
            }
        }

        for (Enumeration usedEnum = getConnectionsUsed().elements(); usedEnum.hasMoreElements();) {
            try {
                ((Accessor)usedEnum.nextElement()).disconnect(getOwner());
            } catch (DatabaseException exception) {
                // Ignore.
            }
        }
        resetConnections();
    }

    /**
     * INTERNAL:
     * Allocate the minimum connections.
     */
    public synchronized void startUp() {
        for (int index = getMinNumberOfConnections(); index > 0; index--) {
            getConnectionsAvailable().addElement(buildConnection());
        }

        setIsConnected(true);
    }

    /**
     * INTERNAL:
     * return a string representation of this connection pool
     */
    public String toString() {
        Object[] args = { new Integer(getMinNumberOfConnections()), new Integer(getMaxNumberOfConnections()) };
        return Helper.getShortClassName(getClass()) + ToStringLocalization.buildMessage("min_max", args);
    }
}
