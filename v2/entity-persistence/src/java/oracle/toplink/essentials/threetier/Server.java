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

import oracle.toplink.essentials.sessions.*;
import oracle.toplink.essentials.exceptions.*;

/**
 * <p>
 * <b>Purpose</b>: A single session that supports multiple user/clients connection at the same time.
 * <p>
 * <b>Description</b>: This session supports a shared session that can be used by multiple users
 * or clients in a three-tiered application.  It brokers client sessions to allow read and write access
 * through a unified object cache.  The server session provides a shared read only database connection that
 * is used by all of its client for reads.  All changes to objects and the database must be done through
 * a unit of work acquired from the client session, this allows the changes to occur in a transactional object
 * space and under a exclusive database connection.
 * <p>
 * <b>Responsibilities</b>:
 *    <ul>
 *    <li> Connecting/disconnecting the default reading login.
 *    <li> Reading objects and maintaining the object cache.
 *    <li> Brokering client sessions.
 *    <li> Disabling database modification through the shared connection.
 *    </ul>
 * @see ClientSession
 * @see oracle.toplink.essentials.sessions.UnitOfWork UnitOfWork
 */
public interface Server extends oracle.toplink.essentials.sessions.DatabaseSession {

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * This method allows for a client session to be acquired sharing the same login as the server session.
     */
    public ClientSession acquireClientSession() throws DatabaseException;

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * This method allows for a client session to be acquired sharing its connection from a pool
     * of connection allocated on the server session.
     * By default this uses a lazy connection policy.
     */
    public ClientSession acquireClientSession(String poolName);

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * The client must provide its own login to use, and the client session returned
     * will have its own exclusive database connection.  This connection will be used to perform
     * all database modification for all units of work acquired from the client session.
     * By default this does not use a lazy connection policy.
     */
    public ClientSession acquireClientSession(Login login);

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * The connection policy specifies how the client session's connection will be acquired.
     */
    public ClientSession acquireClientSession(ConnectionPolicy connectionPolicy);

    /**
     * PUBLIC:
     * Add the connection pool.
     * Connections are pooled to share and restrict the number of database connections.
     */
    public void addConnectionPool(String poolName, Login login, int minNumberOfConnections, int maxNumberOfConnections);

    /**
     * PUBLIC:
     * Connection are pooled to share and restrict the number of database connections.
     */
    public void addConnectionPool(ConnectionPool pool);

    /**
     * PUBLIC:
     * Return the pool by name.
     */
    public ConnectionPool getConnectionPool(String poolName);

    /**
     * PUBLIC:
     * The default connection policy is used by default by the acquireClientConnection() protocol.
     * By default it is a connection pool with min 5 and max 10 lazy pooled connections.
     */
    public ConnectionPolicy getDefaultConnectionPolicy();

    /**
     * PUBLIC:
     * Return the default connection pool.
     */
    public ConnectionPool getDefaultConnectionPool();

    /**
     * PUBLIC:
     * Return the number of non-pooled database connections allowed.
     * This can be enforced to make up for the resource limitation of most JDBC drivers and database clients.
     * By default this is 50.
     */
    public int getMaxNumberOfNonPooledConnections();

    /**
     * PUBLIC:
     * Handles allocating connections for read queries.
     * <p>
     * By default a read connection pool is created and configured automatically in the
     * constructor.  A default read connection pool is one with two connections, and
     * does not support concurrent reads.
     * <p> The read connection pool is not used while in transaction.
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useExclusiveReadConnectionPool
     * @see #useExternalReadConnectionPool
     * @see #useReadConnectionPool
     */
    public ConnectionPool getReadConnectionPool();

    /**
     * PUBLIC:
     * Set the login.
     */
    public void setDatasourceLogin(Login login);

    /**
     * PUBLIC:
     * The default connection policy is used by default by the acquireClientConnection() protocol.
     * By default it is a connection pool with min 5 and max 10 lazy pooled connections.
     */
    public void setDefaultConnectionPolicy(ConnectionPolicy defaultConnectionPolicy);

    /**
     * PUBLIC:
     * Set the number of non-pooled database connections allowed.
     * This can be enforced to make up for the resource limitation of most JDBC drivers and database clients.
     * By default this is 50.
     */
    public void setMaxNumberOfNonPooledConnections(int maxNumberOfNonPooledConnections);

    /**
     * PUBLIC:
     * Sets the read connection pool directly.
     * <p>
     * Either {@link #useExclusiveReadConnectionPool} or {@link #useExternalReadConnectionPool} is
     * called in the constructor.  For a connection pool using concurrent reading
     * {@link #useReadConnectionPool} should be called on a new instance of <code>this</code>.
     *
     * @throws ValidationException if already connected
     */
    public void setReadConnectionPool(ConnectionPool readConnectionPool);

    /**
     * PUBLIC:
     * Sets the read connection pool to be a standard <code>ConnectionPool</code>.
     * <p>
     * Minimum and maximum number of connections is determined from the ConnectionPolicy.  The defaults are 2 for both.
     * <p>
     * Since the same type of connection pool is used as for writing, no
     * two users will use the same connection for reading at the same time.
     * <p>
     * This read connection pool is the default as some JDBC drivers do not support
     * concurrent reading.
     * <p>
     * Unless <code>this</code> {@link oracle.toplink.essentials.sessions.Session#hasExternalTransactionController hasExternalTransactionController()}
     * a read connection pool of this type will be setup in the constructor.
     * @see #getReadConnectionPool
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useReadConnectionPool
     * @see #useExternalReadConnectionPool
     */
    public void useExclusiveReadConnectionPool(int minNumberOfConnections, int maxNumberOfConnections);

    /**
     * PUBLIC:
     * Sets the read connection pool to be an <code>ExternalConnectionPool</code>.
     * <p>
     * This type of connection pool will be created and configured automatically if
     * an external transaction controller is used.
     * @see oracle.toplink.essentials.sessions.Session#hasExternalTransactionController
     * @see #getReadConnectionPool
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useReadConnectionPool
     * @see #useExclusiveReadConnectionPool
     */
    public void useExternalReadConnectionPool();

    /**
     * PUBLIC:
     * Sets the read connection pool to be a <code>ReadConnectionPool</code>.
     * <p>
     * Since read connections are not used for writing, multiple users can
     * theoretically use the same connection at the same time.  Most JDBC drivers
     * have concurrent reading which supports this.
     * <p>
     * Use this read connection pool to take advantage of concurrent reading.
     * <p>
     * @param minNumberOfConnections
     * @param maxNumberOfConnections As multiple readers can use the same connection
     * concurrently fewer connections are needed.
     * @see #getReadConnectionPool
     * @see #setReadConnectionPool(ConnectionPool)
     * @see #useExternalReadConnectionPool
     * @see #useExclusiveReadConnectionPool
     */
    public void useReadConnectionPool(int minNumberOfConnections, int maxNumberOfConnections);

}
