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
package oracle.toplink.essentials.internal.databaseaccess;

import java.util.*;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.queryframework.Call;
import oracle.toplink.essentials.logging.SessionLog;
import oracle.toplink.essentials.sessions.SessionProfiler;
import oracle.toplink.essentials.internal.sessions.AbstractRecord;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * INTERNAL:
 * <code>DatasourceAccessor</code> is an abstract implementation
 * of the <code>Accessor</code> interface providing common functionality to the concrete database and EIS accessors.
 * It is responsible for
 * connecting,
 * transactions,
 * call execution
 *
 * @see Call
 * @see Login
 *
 * @author James
 * @since OracleAS TopLink 10<i>g</i> (10.0.3)
 */
public abstract class DatasourceAccessor implements Accessor {

    /** Store the reference to the driver level connection. */
    protected Object datasourceConnection;

    /** Store the login information that connected this accessor. */
    protected Login login;

    /**
     * Keep track of the number of concurrent active calls.
     * This is used for connection pooling for loadbalancing and for external connection pooling.
     */
    protected int callCount;

    /** Keep track if the accessor is within a transaction context */
    protected boolean isInTransaction;

    /** Keep track of whether the accessor is "connected". */
    protected boolean isConnected;

    /** PERF: Cache platform to avoid gets (small but can add up). */
    /** This is also required to ensure all accessors for a session are using the same platform. */
    protected DatasourcePlatform platform;

    /**
     *    Default Constructor.
     */
    public DatasourceAccessor() {
        this.isInTransaction = false;
        this.callCount = 0;
        this.isConnected = false;
    }

    /**
     * Clone the accessor.
     */
    public Object clone() {
        try {
            DatasourceAccessor accessor = (DatasourceAccessor)super.clone();
            return accessor;
        } catch (CloneNotSupportedException exception) {
            throw new InternalError("clone not supported");
        }
    }

    /**
     * To be called after JTS transaction has been completed (committed or rolled back)
     */
    public void afterJTSTransaction() {
        if (usesExternalTransactionController()) {
            setIsInTransaction(false);
            if ((getDatasourceConnection() != null) && usesExternalConnectionPooling()) {
                closeConnection();
                setDatasourceConnection(null);
            }
        }
    }

    /**
     * Set the transaction transaction status of the receiver.
     */
    protected void setIsInTransaction(boolean value) {
        isInTransaction = value;
    }

    /**
     * Return the transaction status of the receiver.
     */
    public boolean isInTransaction() {
        return isInTransaction;
    }

    /**
     * Return true if some external connection pool is in use.
     */
    public boolean usesExternalConnectionPooling() {
        if (getLogin() == null) {
            throw DatabaseException.databaseAccessorNotConnected();
        }
        return getLogin().shouldUseExternalConnectionPooling();
    }

    /**
     *    Begin a transaction on the database. If not using managed transaction begin a local transaction.
     */
    public void beginTransaction(AbstractSession session) throws DatabaseException {
        if (usesExternalTransactionController()) {
            setIsInTransaction(true);
            return;
        }

        session.log(SessionLog.FINER, SessionLog.TRANSACTION, "begin_transaction", (Object[])null, this);

        try {
            session.startOperationProfile(SessionProfiler.TRANSACTION);
            incrementCallCount(session);
            basicBeginTransaction(session);
            setIsInTransaction(true);
        } finally {
            decrementCallCount();
            session.endOperationProfile(SessionProfiler.TRANSACTION);
        }
    }

    /**
     * Begin the driver level transaction.
     */
    protected abstract void basicBeginTransaction(AbstractSession session);

    /**
     * Commit the driver level transaction.
     */
    protected abstract void basicCommitTransaction(AbstractSession session);

    /**
     * Rollback the driver level transaction.
     */
    protected abstract void basicRollbackTransaction(AbstractSession session);

    /**
     * Used for load balancing and external pooling.
     */
    public synchronized void decrementCallCount() {
        setCallCount(getCallCount() - 1);
        if (usesExternalConnectionPooling() && (!isInTransaction()) && (getCallCount() == 0)) {
            try {
                closeConnection();
            } catch (DatabaseException ignore) {
            }
            // Don't allow for errors to be masked by disconnect.
        }
    }

    /**
     * Used for load balancing and external pooling.
     */
    public synchronized void incrementCallCount(AbstractSession session) {
        setCallCount(getCallCount() + 1);

        if (getCallCount() == 1) {
            // If the login is null, then this accessor has never been connected.
            if (getLogin() == null) {
                throw DatabaseException.databaseAccessorNotConnected();
            }

            // If the connection is no longer connected, it may have timed out.
            if (getDatasourceConnection() != null) {
                if (!isConnected()) {
                    if (isInTransaction()) {
                        throw DatabaseException.databaseAccessorNotConnected();
                    } else {
                        reconnect(session);
                    }
                }
            } else {
                // If ExternalConnectionPooling is used, the connection can be re-established.
                if (usesExternalConnectionPooling()) {
                    reconnect(session);
                } else {
                    throw DatabaseException.databaseAccessorNotConnected();
                }
            }
        }
    }

    /**
     * Connect to the database.
     * Exceptions are caught and re-thrown as TopLink exceptions.
     */
    protected void connect(Login login) throws DatabaseException {
        setDatasourceConnection(login.connectToDatasource(this));
        setIsConnected(true);
    }

    /**
     * Set whether the accessor has a connection to the "data store".
     */
    protected void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    /**
     * Used for load balancing and external pooling.
     */
    protected void setCallCount(int callCount) {
        this.callCount = callCount;
    }

    /**
     * Used for load balancing and external pooling.
     */
    public int getCallCount() {
        return callCount;
    }

    /**
     * Commit a transaction on the database. If using non-managed transaction commit the local transaction.
     */
    public void commitTransaction(AbstractSession session) throws DatabaseException {
        if (usesExternalTransactionController()) {
            // if there is no external TX controller, then that means we are currently not synchronized
            // with a global JTS transaction.  In this case, there won't be any 'afterCompletion'
            // callbacks so we have to release the connection here.  It is possible (WLS 5.1) to choose
            // 'usesExternalTransactionController' on the login, but still acquire a uow that WON'T be
            // synchronized with a global TX.
            if (session.getExternalTransactionController() == null) {
                setIsInTransaction(false);
                if ((getDatasourceConnection() != null) && usesExternalConnectionPooling()) {
                    closeConnection();
                    setDatasourceConnection(null);
                }
            }
            return;
        }

        session.log(SessionLog.FINER, SessionLog.TRANSACTION, "commit_transaction", (Object[])null, this);

        try {
            session.startOperationProfile(SessionProfiler.TRANSACTION);
            incrementCallCount(session);
            basicCommitTransaction(session);

            // true=="committed"; false=="not jts transactioin"
            session.afterTransaction(true, false);
            setIsInTransaction(false);
        } finally {
            decrementCallCount();
            session.endOperationProfile(SessionProfiler.TRANSACTION);
        }
    }

    /**
     * Connect to the datasource.  Through using a CCI ConnectionFactory.
     * Catch exceptions and re-throw as TopLink exceptions.
     */
    public void connect(Login login, AbstractSession session) throws DatabaseException {
        session.startOperationProfile(SessionProfiler.CONNECT);
        session.incrementProfile(SessionProfiler.TlConnects);

        try {
            if (session.shouldLog(SessionLog.CONFIG, SessionLog.CONNECTION)) {// Avoid printing if no logging required.
                session.log(SessionLog.CONFIG, SessionLog.CONNECTION, "connecting", new Object[] { login }, this);
            }
            setLogin(login);
            this.setDatasourcePlatform((DatasourcePlatform)session.getDatasourceLogin().getDatasourcePlatform());
            try {
                connect(login);
                setIsInTransaction(false);
            } catch (RuntimeException exception) {
                session.handleSevere(exception);
            }
            session.getEventManager().postConnect(this);
            incrementCallCount(session);
            try {
                buildConnectLog(session);
            } finally {
                decrementCallCount();
            }
        } finally {
            session.endOperationProfile(SessionProfiler.CONNECT);
        }
    }

    /**
     * Close the connection to the driver level datasource.
     */
    protected abstract void closeDatasourceConnection();

    /**
     * Execute the call to driver level datasource.
     */
    protected abstract Object basicExecuteCall(Call call, AbstractRecord row, AbstractSession session);

    /**
     * Build a log string of any driver metadata that can be obtained.
     */
    protected abstract void buildConnectLog(AbstractSession session);

    /**
     * Return the login
     */
    public Login getLogin() {
        return login;
    }

    /**
     * SECURE:
     * set the login
     */
    protected void setLogin(Login login) {
        this.login = login;
    }

    /**
     *    Disconnect from the datasource.
     */
    public void disconnect(AbstractSession session) throws DatabaseException {
        session.log(SessionLog.CONFIG, SessionLog.CONNECTION, "disconnect", (Object[])null, this);

        if (getDatasourceConnection() == null) {
            return;
        }
        session.incrementProfile(SessionProfiler.TlDisconnects);
        session.startOperationProfile(SessionProfiler.CONNECT);
        closeDatasourceConnection();
        setDatasourceConnection(null);
        setIsInTransaction(false);
        session.endOperationProfile(SessionProfiler.CONNECT);
    }

    /**
     * Close the accessor's connection.
     * This is used only for external connection pooling
     * when it is intended for the connection to be reconnected in the future.
     */
    public void closeConnection() {
        try {
            if (getDatasourceConnection() != null) {
                if (isDatasourceConnected()) {
                    closeDatasourceConnection();
                }
                setDatasourceConnection(null);
            }
        } catch (DatabaseException exception) {
            // Ignore
            setDatasourceConnection(null);
        }
    }

    /**
     * Execute the call.
     * @return depending of the type either the row count, row or vector of rows.
     */
    public Object executeCall(Call call, AbstractRecord translationRow, AbstractSession session) throws DatabaseException {
        // If the login is null, then this accessor has never been connected.
        if (getLogin() == null) {
            throw DatabaseException.databaseAccessorNotConnected();
        }

        if (session.shouldLog(SessionLog.FINE, SessionLog.SQL)) {// pre-check to improve performance
            session.log(SessionLog.FINE, SessionLog.SQL, call.getLogString(this), (Object[])null, this, false);
        }

        Object result = basicExecuteCall(call, translationRow, session);

        return result;
    }

    /**
     * PUBLIC:
     * Reconnect to the database.  This can be used if the connection was disconnected or timedout.
     * This ensures that the security is checked as it is public.
     * Because the messages can take a long time to build,
     * pre-check whether messages should be logged.
     */
    public void reestablishConnection(AbstractSession session) throws DatabaseException {
        if (session.shouldLog(SessionLog.CONFIG, SessionLog.CONNECTION)) {// Avoid printing if no logging required.		
            Object[] args = { getLogin() };
            session.log(SessionLog.CONFIG, SessionLog.CONNECTION, "reconnecting", args, this);
        }
        reconnect(session);
        setIsInTransaction(false);
        session.getEventManager().postConnect(this);
    }

    /**
     * Attempt to save some of the cost associated with getting a fresh connection.
     * Assume the DatabaseDriver has been cached, if appropriate.
     * Note: Connections that are participating in transactions will not be refreshd.^M
     */
    protected void reconnect(AbstractSession session) throws DatabaseException {
        session.log(SessionLog.FINEST, SessionLog.CONNECTION, "reconnecting_to_external_connection_pool");
        session.startOperationProfile(SessionProfiler.CONNECT);
        connect(getLogin());
        session.endOperationProfile(SessionProfiler.CONNECT);
    }

    /**
     * Return the platform.
     */
    public DatasourcePlatform getDatasourcePlatform() {
        return platform;
    }

    /**
     * Set the platform.
     * This should be set to the session's platform, not the connections
     * which may not be configured correctly.
     */
    public void setDatasourcePlatform(DatasourcePlatform platform) {
        this.platform = platform;
    }

    /**
     * Return the driver level connection.
     */
    public Object getDatasourceConnection() {
        return datasourceConnection;
    }

    /**
     * Helper method to return the JDBC connection for DatabaseAccessor.
     * Was going to deprecate this, but since most clients are JDBC this is useful.
     */
    public java.sql.Connection getConnection() {
        return (java.sql.Connection)getDatasourceConnection();
    }

    /**
     * Return column information for the specified
     * database objects.
     */
    public Vector getColumnInfo(String catalog, String schema, String tableName, String columnName, AbstractSession session) throws DatabaseException {
        return new Vector();
    }

    /**
     * Return table information for the specified
     * database objects.
     */
    public Vector getTableInfo(String catalog, String schema, String tableName, String[] types, AbstractSession session) throws DatabaseException {
        return new Vector();
    }

    /**
     * If client requires to manually set connection they can use the connection manager.
     */
    protected void setDatasourceConnection(Object connection) {
        this.datasourceConnection = connection;
    }

    /**
     * Rollback the transaction on the datasource. If not using managed transaction rollback the local transaction.
     */
    public void rollbackTransaction(AbstractSession session) throws DatabaseException {
        if (usesExternalTransactionController()) {
            // if there is no external TX controller, then that means we are currently not synchronized
            // with a global JTS transaction.  In this case, there won't be any 'afterCompletion'
            // callbacks so we have to release the connection here.  It is possible (WLS 5.1) to choose
            // 'usesExternalTransactionController' on the login, but still acquire a uow that WON'T be
            // synchronized with a global TX.
            if (session.getExternalTransactionController() == null) {
                setIsInTransaction(false);
                if ((getDatasourceConnection() != null) && usesExternalConnectionPooling()) {
                    closeConnection();
                    setDatasourceConnection(null);
                }
            }
            return;
        }

        session.log(SessionLog.FINER, SessionLog.TRANSACTION, "rollback_transaction", (Object[])null, this);

        try {
            session.startOperationProfile(SessionProfiler.TRANSACTION);
            incrementCallCount(session);
            basicRollbackTransaction(session);
        } finally {
            // false=="rolled back"; false=="not jts transactioin"
            session.afterTransaction(false, false);
            setIsInTransaction(false);
            decrementCallCount();
            session.endOperationProfile(SessionProfiler.TRANSACTION);
        }
    }

    /**
     * Return true if some external transaction service is controlling transactions.
     */
    public boolean usesExternalTransactionController() {
        if (getLogin() == null) {
            throw DatabaseException.databaseAccessorNotConnected();
        }
        return getLogin().shouldUseExternalTransactionController();
    }

    /**
     *    Return true if the accessor is currently connected to a data source.
     *  Return false otherwise.
     */
    public boolean isConnected() {
        if ((getDatasourceConnection() == null) && (getLogin() == null)) {
            return false;
        }
        if (usesExternalConnectionPooling()) {
            return true;// As can always reconnect.
        }

        if (getDatasourceConnection() == null) {
            return false;
        }

        return isDatasourceConnected();
    }

    /**
     * Return if the driver level connection is connected.
     */
    protected abstract boolean isDatasourceConnected();

    /**
     * Added as a result of Bug 2804663 - satisfy the Accessor interface
     * implementation.
     */
    public void flushSelectCalls(AbstractSession session) {
        // By default do nothing.
    }

    /**
     * This method will be called after a series of writes have been issued to
     * mark where a particular set of writes has completed.  It will be called
     * from commitTransaction and may be called from writeChanges.   Its main
     * purpose is to ensure that the batched statements have been executed
     */
    public void writesCompleted(AbstractSession session) {
        //this is a no-op in this method as we do not batch on this accessor
    }
}
