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
import oracle.toplink.essentials.queryframework.*;
import oracle.toplink.essentials.sessions.DatasourceLogin;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.sessions.UnitOfWork;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sequencing.SequencingCallback;
import oracle.toplink.essentials.internal.sequencing.SequencingServer;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.internal.sessions.*;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * Implementation of Server
 * INTERNAL:
 * The public interface should be used.
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
 * @see UnitOfWork
 */
public class ServerSession extends DatabaseSessionImpl implements Server {
    protected ConnectionPool readConnectionPool;
    protected Map connectionPools;
    protected ConnectionPolicy defaultConnectionPolicy;
    protected int maxNumberOfNonPooledConnections;
    protected int numberOfNonPooledConnectionsUsed;
    public static final int MAX_WRITE_CONNECTIONS = 10;	
    public static final int MIN_WRITE_CONNECTIONS = 5;	
    
    /**
     * INTERNAL:
     * Create and return a new default server session.
     * Used for EJB SessionManager to instantiate a server session
     */
    public ServerSession() {
        super();
        this.connectionPools = new HashMap(10);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * By giving the login information on creation this allows the session to initialize itself
     * to the platform given in the login. This constructor does not return a connected session.
     * To connect the session to the database login() must be sent to it. The login(userName, password)
     * method may also be used to connect the session, this allows for the user name and password
     * to be given at login but for the other database information to be provided when the session is created.
     * By default the server session uses a default connection pool with 5 min 10 max number of connections
     * and a max number 50 non-pooled connections allowed.
     */
    public ServerSession(Login login) {
        this(new oracle.toplink.essentials.sessions.Project(login));
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the min and max number of connections for the default pool.
     */
    public ServerSession(Login login, int minNumberOfPooledConnection, int maxNumberOfPooledConnection) {
        this(new oracle.toplink.essentials.sessions.Project(login), minNumberOfPooledConnection, maxNumberOfPooledConnection);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the default connection policy to be used.
     * This policy is used on the "acquireClientSession()" protocol.
     */
    public ServerSession(Login login, ConnectionPolicy defaultConnectionPolicy) {
        this(new oracle.toplink.essentials.sessions.Project(login), defaultConnectionPolicy);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * By giving the project information on creation this allows the session to initialize itself
     * to the platform given in the login. This constructor does not return a connected session.
     * To connect the session to the database login() must be sent to it. The login(userName, password)
     * method may also be used to connect the session, this allows for the user name and password
     * to be given at login but for the other database information to be provided when the session is created.
     * By default the server session uses a default connection pool with 5 min 10 max number of connections
     * and a max number 50 non-pooled connections allowed.
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project) {
        this(project, MIN_WRITE_CONNECTIONS, MAX_WRITE_CONNECTIONS);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the min and max number of connections for the default pool.
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project, int minNumberOfPooledConnection, int maxNumberOfPooledConnection) {
        this(project, new ConnectionPolicy("default"));

        ConnectionPool pool = null;
        if (project.getDatasourceLogin().shouldUseExternalConnectionPooling()) {
            pool = new ExternalConnectionPool("default", project.getDatasourceLogin(), this);
        } else {
            pool = new ConnectionPool("default", project.getDatasourceLogin(), minNumberOfPooledConnection, maxNumberOfPooledConnection, this);
        }
        this.connectionPools.put("default", pool);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the min and max number of connections for the default pool.
     * Use the login from the project for the write pool. Use the passed
     * in login for the read pool, if specified, or the project login if not.
     *
     * @param project the project associated with this session
     * @param minNumberOfPooledConnection the minimum number of connections in the pool
     * @param maxNumberOfPooledConnection the maximum number of connections in the pool
     * @param readLogin the login used to create the read connection pool
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project, int minNumberOfPooledConnection, int maxNumberOfPooledConnection, oracle.toplink.essentials.sessions.Login readLogin) {
        this(project, new ConnectionPolicy("default"), readLogin);

        ConnectionPool pool = null;
        if (project.getDatasourceLogin().shouldUseExternalConnectionPooling()) {
            pool = new ExternalConnectionPool("default", project.getDatasourceLogin(), this);
        } else {
            pool = new ConnectionPool("default", project.getDatasourceLogin(), minNumberOfPooledConnection, maxNumberOfPooledConnection, this);
        }
        this.connectionPools.put("default", pool);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the min and max number of connections for the default pool.
     * Use the login from the project for the write pool. Use the passed
     * in login for the read pool, if specified, or the project login if not.
     * Use the sequenceLogin, if specified, for creating a connection pool
     * to be used by sequencing through SequencingConnectionHandler
     * sequenceLogin *MUST*:
     * 1. specify *NON-JTS* connections (such as NON_JTS driver or read-only datasource);
     * 2. sequenceLogin.shouldUseExternalTransactionController()==false
     *
     * @param project the project associated with this session
     * @param minNumberOfPooledConnection the minimum number of connections in the pool
     * @param maxNumberOfPooledConnection the maximum number of connections in the pool
     * @param readLogin the login used to create the read connection pool
     * @param sequenceLogin the login used to create a connection pool for sequencing
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project, int minNumberOfPooledConnection, int maxNumberOfPooledConnection, oracle.toplink.essentials.sessions.Login readLogin, oracle.toplink.essentials.sessions.Login sequenceLogin) {
        this(project, new ConnectionPolicy("default"), readLogin, sequenceLogin);

        ConnectionPool pool = null;
        if (project.getDatasourceLogin().shouldUseExternalConnectionPooling()) {
            pool = new ExternalConnectionPool("default", project.getDatasourceLogin(), this);
        } else {
            pool = new ConnectionPool("default", project.getDatasourceLogin(), minNumberOfPooledConnection, maxNumberOfPooledConnection, this);
        }
        this.connectionPools.put("default", pool);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the default connection policy to be used.
     * This policy is used on the "acquireClientSession()" protocol.
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project, ConnectionPolicy defaultConnectionPolicy) {
        super(project);
        this.connectionPools = new HashMap(10);
        this.defaultConnectionPolicy = defaultConnectionPolicy;
        this.maxNumberOfNonPooledConnections = 50;
        this.numberOfNonPooledConnectionsUsed = 0;
        setReadConnectionPool(project.getDatasourceLogin());
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the default connection policy to be used.
     * This policy is used on the "acquireClientSession()" protocol.
     * Use the readLogin, if specified, for logging into the read
     * connection pool.
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project, ConnectionPolicy defaultConnectionPolicy, oracle.toplink.essentials.sessions.Login readLogin) {
        super(project);
        this.connectionPools = new HashMap(10);
        this.defaultConnectionPolicy = defaultConnectionPolicy;
        this.maxNumberOfNonPooledConnections = 50;
        this.numberOfNonPooledConnectionsUsed = 0;
        Login login = (readLogin != null) ? readLogin : project.getDatasourceLogin();
        setReadConnectionPool(login);
    }

    /**
     * PUBLIC:
     * Create and return a new server session.
     * Configure the default connection policy to be used.
     * This policy is used on the "acquireClientSession()" protocol.
     * Use the readLogin, if specified, for logging into the read
     * connection pool.
     * Use the sequenceLogin, if specified, for creating a connection pool
     * to be used by sequencing through SequencingConnectionHandler
     * sequenceLogin *MUST*:
     * 1. specify *NON-JTS* connections (such as NON_JTS driver or read-only datasource);
     * 2. sequenceLogin.shouldUseExternalTransactionController()==false
     *
     */
    public ServerSession(oracle.toplink.essentials.sessions.Project project, ConnectionPolicy defaultConnectionPolicy, oracle.toplink.essentials.sessions.Login readLogin, oracle.toplink.essentials.sessions.Login sequenceLogin) {
        this(project, defaultConnectionPolicy, readLogin);

        if (sequenceLogin != null) {
            //** sequencing refactoring
            getSequencingControl().setShouldUseSeparateConnection(true);
            getSequencingControl().setLogin(sequenceLogin);
        }
    }

    /**
     * INTERNAL:
     * Allocate the client's connection resource.
     */
    public void acquireClientConnection(ClientSession clientSession) throws DatabaseException, ConcurrencyException {
        if (clientSession.getConnectionPolicy().isPooled()) {
            ConnectionPool pool = (ConnectionPool)getConnectionPools().get(clientSession.getConnectionPolicy().getPoolName());
            Accessor connection = pool.acquireConnection();
            clientSession.setWriteConnection(connection);
            getEventManager().postAcquireConnection(connection);
        } else {
            // This method is synchronized, so while is not required, only if.
            synchronized (this) {
                while (getNumberOfNonPooledConnectionsUsed() >= getMaxNumberOfNonPooledConnections()) {
                    try {
                        wait();// Notify is called when connections are released.
                    } catch (InterruptedException exception) {
                        throw ConcurrencyException.waitFailureOnServerSession(exception);
                    }
                }

                setNumberOfNonPooledConnectionsUsed(getNumberOfNonPooledConnectionsUsed() + 1);
                clientSession.setWriteConnection(clientSession.getLogin().buildAccessor());
                clientSession.connect();
            }
        }
    }

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * This method allows for a client session to be acquired sharing the same login as the server session.
     */
    public ClientSession acquireClientSession() throws DatabaseException {
        return acquireClientSession((ConnectionPolicy)getDefaultConnectionPolicy().clone());
    }

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * Each user/client connected to this server session must acquire there own client session
     * to communicate to the server through.
     * This method allows for a client session to be acquired sharing its connection from a pool
     * of connection allocated on the server session.
     * By default this uses a lazy connection policy.
     */
    public ClientSession acquireClientSession(String poolName) throws DatabaseException {
        return acquireClientSession(new ConnectionPolicy(poolName));
    }

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
    public ClientSession acquireClientSession(Login login) throws DatabaseException {
        return acquireClientSession(new ConnectionPolicy(login));
    }

    /**
     * PUBLIC:
     * Return a client session for this server session.
     * The connection policy specifies how the client session's connection will be acquired.
     */
    public ClientSession acquireClientSession(ConnectionPolicy connectionPolicy) throws DatabaseException, ValidationException {
        if (!isConnected()) {
            throw ValidationException.loginBeforeAllocatingClientSessions();
        }

        log(SessionLog.FINER, SessionLog.CONNECTION, "client_acquired");
        if (!connectionPolicy.isPooled() && (connectionPolicy.getLogin() == null)) {
            //the user has passed in a connection policy with no login info. Use the 
            //default info from the default connection policy
            connectionPolicy.setPoolName(getDefaultConnectionPolicy().getPoolName());
            connectionPolicy.setLogin(getDefaultConnectionPolicy().getLogin());
        }
        if (connectionPolicy.isPooled()) {
            ConnectionPool pool = (ConnectionPool)getConnectionPools().get(connectionPolicy.getPoolName());
            if (pool == null) {
                throw ValidationException.poolNameDoesNotExist(connectionPolicy.getPoolName());
            }
            connectionPolicy.setLogin((Login)pool.getLogin().clone());
        }
        ClientSession client = null;
        if (getProject().hasIsolatedClasses()) {
            client = new IsolatedClientSession(this, connectionPolicy);
        } else {
            client = new ClientSession(this, connectionPolicy);
        }
        if (!connectionPolicy.isLazy()) {
            acquireClientConnection(client);
        }

        return client;
    }

    /**
     * PUBLIC:
     * Return a unit of work for this session.
     * The unit of work is an object level transaction that allows
     * a group of changes to be applied as a unit.
     * First acquire a client session as server session does not allow direct units of work.
     *
     * @see UnitOfWork
     */
    public UnitOfWork acquireUnitOfWork() {
        return acquireClientSession().acquireUnitOfWork();
    }

    /**
     * PUBLIC:
     * Add the connection pool.
     * Connections are pooled to share and restrict the number of database connections.
     */
    public void addConnectionPool(String poolName, Login login, int minNumberOfConnections, int maxNumberOfConnections) throws ValidationException {
        if (minNumberOfConnections > maxNumberOfConnections) {
            throw ValidationException.maxSizeLessThanMinSize();
        }
        if (isConnected()) {
            throw ValidationException.poolsMustBeConfiguredBeforeLogin();
        }
        ConnectionPool pool = null;
        if (login.shouldUseExternalConnectionPooling()) {
            pool = new ExternalConnectionPool(poolName, login, this);
        } else {
            pool = new ConnectionPool(poolName, login, minNumberOfConnections, maxNumberOfConnections, this);
        }
        addConnectionPool(pool);
    }

    /**
     * PUBLIC:
     * Connection are pooled to share and restrict the number of database connections.
     */
    public void addConnectionPool(ConnectionPool pool) {
        pool.setOwner(this);
        getConnectionPools().put(pool.getName(), pool);

    }

    /**
     * INTERNAL:
     * Called after transaction is completed (committed or rolled back)
     */
    public void afterTransaction(boolean committed, boolean isExternalTransaction, Accessor accessor) {
        SequencingCallback callback = getSequencingHome().getSequencingCallback();
        if (callback != null) {
            callback.afterTransaction(accessor, committed);
        }
    }

    /**
     * INTERNAL:
     * Return a read connection from the read pool.
     * Note that depending on the type of pool this may be a shared or exclusive connection.
     * Each query execution is assigned a read connection.
     */
    public Accessor allocateReadConnection() {
        Accessor connection = getReadConnectionPool().acquireConnection();
        getEventManager().postAcquireConnection(connection);
        return connection;
    }

    /**
     * INTERNAL:
     * Startup the server session, also startup all of the connection pools.
     */
    public void connect() {
        // make sure pools correspond to their logins
        updateStandardConnectionPools();
        // Configure the read pool
        getReadConnectionPool().startUp();
        setAccessor(allocateReadConnection());
        releaseReadConnection(getAccessor());

        for (Iterator poolsEnum = getConnectionPools().values().iterator(); poolsEnum.hasNext();) {
            ((ConnectionPool)poolsEnum.next()).startUp();
        }
    }

    /**
     * INTERNAL:
     * Override to acquire the connection from the pool at the last minute
     */
    public Object executeCall(Call call, AbstractRecord translationRow, DatabaseQuery query) throws DatabaseException {
        RuntimeException exception = null;
        Object object = null;
        boolean accessorAllocated = false;
        if (query.getAccessor() == null) {
            query.setAccessor(this.allocateReadConnection());
            accessorAllocated = true;
        }
        try {
            object = query.getAccessor().executeCall(call, translationRow, this);
        } catch (RuntimeException caughtException) {
            exception = caughtException;
        } finally {
            if (call.isFinished()) {
                //don't release the cursoredStream connection until Stream is closed
                try {
                    if (accessorAllocated) {
                        releaseReadConnection(query.getAccessor());
                        query.setAccessor(null);
                    }
                } catch (RuntimeException releaseException) {
                    if (exception == null) {
                        throw releaseException;
                    }
                    //else ignore
                }
            }
            if (exception != null) {
                throw exception;
            }
        }
        return object;
    }

    /**
     * PUBLIC:
     * Return the pool by name.
     */
    public ConnectionPool getConnectionPool(String poolName) {
        return (ConnectionPool)getConnectionPools().get(poolName);
    }

    /**
     * INTERNAL:
     * Connection are pooled to share and restrict the number of database connections.
     */
    public Map getConnectionPools() {
        return connectionPools;
    }

    /**
     * PUBLIC:
     * The default connection policy is used by default by the acquireClientConnection() protocol.
     * By default it is a connection pool with min 5 and max 10 lazy pooled connections.
     */
    public ConnectionPolicy getDefaultConnectionPolicy() {
        if (defaultConnectionPolicy == null) {
            this.defaultConnectionPolicy = new ConnectionPolicy("default");
        }
        return defaultConnectionPolicy;
    }

    /**
     * PUBLIC:
     * Return the default connection pool.
     */
    public ConnectionPool getDefaultConnectionPool() {
        return getConnectionPool("default");
    }

    /**
     * INTERNAL:
     * Gets the session which this query will be executed on.
     * Generally will be called immediately before the call is translated,
     * which is immediately before session.executeCall.
     * <p>
     * Since the execution session also knows the correct datasource platform
     * to execute on, it is often used in the mappings where the platform is
     * needed for type conversion, or where calls are translated.
     * <p>
     * Is also the session with the accessor.  Will return a ClientSession if
     * it is in transaction and has a write connection.
     * @return a session with a live accessor
     * @param query may store session name or reference class for brokers case
     */
    public AbstractSession getExecutionSession(DatabaseQuery query) {
        if (query.isObjectLevelModifyQuery()) {
            throw QueryException.invalidQueryOnServerSession(query);
        }
        return this;
    }

    /**
     * PUBLIC:
     * Return the number of non-pooled database connections allowed.
     * This can be enforced to make up for the resource limitation of most JDBC drivers and database clients.
     * By default this is 50.
     */
    public int getMaxNumberOfNonPooledConnections() {
        return maxNumberOfNonPooledConnections;
    }

    /**
     * INTERNAL:
     * Return the current number of non-pooled connections in use.
     */
    public int getNumberOfNonPooledConnectionsUsed() {
        return numberOfNonPooledConnectionsUsed;
    }

    /**
     * INTERNAL:
     * Return the login for the read connection.  Used by the platform autodetect feature
     */
    protected Login getReadLogin(){
        return getReadConnectionPool().getLogin();
    }

    /**
     * PUBLIC:
     * Return the read connection pool.
     * The read connection pool handles allocating connection for read queries.
     * By default a ReadConnnectionPool with a single connection.  This is normally sufficient
     * as a JDBC connection can support concurrent reading.  Multiple connections can also
     * be specified and may improve concurrency on some JDBC drivers/databases.
     * If external connection pooling is used, an external connection pool will be used by default.
     * If your JDBC driver does not support concurrency corrently a normal ConnectionPool can be used
     * to ensure exclusive access to the read connection, note that this will give less concurrency.
     */
    public ConnectionPool getReadConnectionPool() {
        return readConnectionPool;
    }

    /**
     * PUBLIC:
     * Return if this session has been connected to the database.
     */
    public boolean isConnected() {
        if (getReadConnectionPool() == null) {
            return false;
        }

        return getReadConnectionPool().isConnected();
    }

    /**
     * INTERNAL:
     * Return if this session is a server session.
     */
    public boolean isServerSession() {
        return true;
    }

    /**
     * PUBLIC:
     * Shutdown the server session, also shutdown all of the connection pools.
     */
    public void logout() {
        super.logout();

        getReadConnectionPool().shutDown();

        for (Iterator poolsEnum = getConnectionPools().values().iterator(); poolsEnum.hasNext();) {
            ((ConnectionPool)poolsEnum.next()).shutDown();
        }
    }

    /**
     * INTERNAL:
     * Release the clients connection resource.
     */
    public void releaseClientSession(ClientSession clientSession) throws DatabaseException {
        if (clientSession.getConnectionPolicy().isPooled()) {
            ConnectionPool pool = (ConnectionPool)getConnectionPools().get(clientSession.getConnectionPolicy().getPoolName());
            getEventManager().preReleaseConnection(clientSession.getWriteConnection());
            pool.releaseConnection(clientSession.getWriteConnection());
            clientSession.setWriteConnection(null);
        } else {
            synchronized (this) {
                clientSession.disconnect();
                clientSession.setWriteConnection(null);
                setNumberOfNonPooledConnectionsUsed(getNumberOfNonPooledConnectionsUsed() - 1);
                notify();
            }
        }
    }

    /**
     * INTERNAL:
     * Release the read connection back into the read pool.
     */
    public void releaseReadConnection(Accessor connection) {
        getEventManager().preReleaseConnection(connection);
        getReadConnectionPool().releaseConnection(connection);
    }

    /**
     * INTERNAL:
     * Connection are pooled to share and restrict the number of database connections.
     */
    public void setConnectionPools(Map connectionPools) {
        this.connectionPools = connectionPools;
    }

    /**
     * PUBLIC:
     * The default connection policy is used by default by the acquireClientConnection() protocol.
     * By default it is a connection pool with min 5 and max 10 lazy pooled connections.
     */
    public void setDefaultConnectionPolicy(ConnectionPolicy defaultConnectionPolicy) {
        this.defaultConnectionPolicy = defaultConnectionPolicy;
    }

    /**
     * PUBLIC:
     * Creates and adds "default" connection pool using default parameter values
     */
    public void setDefaultConnectionPool() {
        addConnectionPool("default", getDatasourceLogin(), 5, 10);
    }

    /**
     * PUBLIC:
     * Set the number of non-pooled database connections allowed.
     * This can be enforced to make up for the resource limitation of most JDBC drivers and database clients.
     * By default this is 50.
     */
    public void setMaxNumberOfNonPooledConnections(int maxNumberOfNonPooledConnections) {
        this.maxNumberOfNonPooledConnections = maxNumberOfNonPooledConnections;
    }

    /**
     *  Set the current number of connections being used that are not from a connection pool.
     *  @param int
     */
    public void setNumberOfNonPooledConnectionsUsed(int numberOfNonPooledConnectionsUsed) {
        this.numberOfNonPooledConnectionsUsed = numberOfNonPooledConnectionsUsed;
    }

    /**
     * PUBLIC:
     * Set the read connection pool.
     * The read connection pool handles allocating connection for read queries.
     * By default a ReadConnnectionPool with a single connection.  This is normally sufficient
     * as a JDBC connection can support concurrent reading.  Multiple connections can also
     * be specified and may improve concurrency on some JDBC drivers/databases.
     * If external connection pooling is used, an external connection pool will be used by default.
     * If your JDBC driver does not support concurrency corrently a normal ConnectionPool can be used
     * to ensure exclusive access to the read connection, note that this will give less concurrency.
     */
    public void setReadConnectionPool(ConnectionPool readConnectionPool) {
        if (isConnected()) {
            throw ValidationException.cannotSetReadPoolSizeAfterLogin();
        }
        this.readConnectionPool = readConnectionPool;
        this.readConnectionPool.setOwner(this);
    }

    /**
     * PUBLIC:
     * Creates and sets the new read connection pool.
     */
    public void setReadConnectionPool(Login readLogin) throws ValidationException {
        if (isConnected()) {
            throw ValidationException.poolsMustBeConfiguredBeforeLogin();
        }
        ConnectionPool pool = null;
        if (readLogin.shouldUseExternalConnectionPooling()) {
            pool = new ExternalConnectionPool("read", readLogin, this);
        } else {
            pool = new ConnectionPool("read", readLogin, 2, 2, this);
        }
        this.readConnectionPool = pool;
    }

    /**
     * INTERNAL:
     * Updates standard connection pools. Should not be called after session is connected.
     * This is needed in case of pools' logins been altered after the pool has been created
     * (SessionManager does that)
     * All pools should be re-created in case their type doesn't match their login.
     * In addition, sequenceConnectionPool should be removed in case its login
     * has shouldUseExternaltransactionController()==true (see setSequenceConnectionPool)
     */
    protected void updateStandardConnectionPools() {
        if (getDefaultConnectionPool() != null) {
            if (getDefaultConnectionPool().isThereConflictBetweenLoginAndType()) {
                setDefaultConnectionPool();
            }
        }

        if (getReadConnectionPool() != null) {
            if (getReadConnectionPool().isThereConflictBetweenLoginAndType()) {
                setReadConnectionPool(getReadConnectionPool().getLogin());
            }
        }
    }

    /**
     * PUBLIC:
     * Configure the read connection pool.
     * The read connection pool handles allocating connection for read queries.
     * By default a ReadConnnectionPool with a single connection.  This is normally sufficient
     * as a JDBC connection can support concurrent reading.  Multiple connections can also
     * be specified and may improve concurrency on some JDBC drivers/databases.
     * If external connection pooling is used, an external connection pool will be used by default.
     * If your JDBC driver does not support concurrency corrently a normal ConnectionPool can be used
     * to ensure exclusive access to the read connection, note that this will give less concurrency.
     */
    public void useExclusiveReadConnectionPool(int minNumerOfConnections, int maxNumerOfConnections) {
        setReadConnectionPool(new ConnectionPool("read", getDatasourceLogin(), minNumerOfConnections, maxNumerOfConnections, this));
    }

    /**
     * PUBLIC:
     * Configure the read connection pool.
     * The read connection pool handles allocating connection for read queries.
     * By default a ReadConnnectionPool with a single connection.  This is normally sufficient
     * as a JDBC connection can support concurrent reading.  Multiple connections can also
     * be specified and may improve concurrency on some JDBC drivers/databases.
     * If external connection pooling is used, an external connection pool will be used by default.
     * If your JDBC driver does not support concurrency corrently a normal ConnectionPool can be used
     * to ensure exclusive access to the read connection, note that this will give less concurrency.
     */
    public void useExternalReadConnectionPool() {
        setReadConnectionPool(new ExternalConnectionPool("read", getDatasourceLogin(), this));
    }

    /**
     * PUBLIC:
     * Configure the read connection pool.
     * The read connection pool handles allocating connection for read queries.
     * By default a ReadConnnectionPool with a single connection.  This is normally sufficient
     * as a JDBC connection can support concurrent reading.  Multiple connections can also
     * be specified and may improve concurrency on some JDBC drivers/databases.
     * If external connection pooling is used, an external connection pool will be used by default.
     * If your JDBC driver does not support concurrency corrently a normal ConnectionPool can be used
     * to ensure exclusive access to the read connection, note that this will give less concurrency.
     */
    public void useReadConnectionPool(int minNumerOfConnections, int maxNumerOfConnections) {
        setReadConnectionPool(new ReadConnectionPool("read", getDatasourceLogin(), minNumerOfConnections, maxNumerOfConnections, this));
    }

    /**
     * INTERNAL:
     * Return SequencingServer object owned by the session.
     */
    public SequencingServer getSequencingServer() {
        return getSequencingHome().getSequencingServer();
    }
}
