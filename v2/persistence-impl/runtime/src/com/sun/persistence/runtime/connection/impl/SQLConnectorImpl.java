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
 * SQLConnectorImpl.java
 *
 * Created on April 25, 2005, 11:15 AM
 */


package com.sun.persistence.runtime.connection.impl;

import com.sun.org.apache.jdo.ejb.EJBImplHelper;
import com.sun.persistence.runtime.LogHelperSQLStore;
import com.sun.persistence.runtime.connection.ConnectionFactory;
import com.sun.persistence.runtime.connection.SQLConnector;
import com.sun.persistence.support.JDODataStoreException;
import com.sun.persistence.support.JDOFatalInternalException;
import com.sun.persistence.support.JDOUserException;
import com.sun.persistence.utility.I18NHelper;
import com.sun.persistence.utility.logging.Logger;

import java.util.ResourceBundle;
import java.sql.Connection;
import javax.sql.DataSource;

/**
 * This class implements the <code>SQLConnector</code> interface, 
 * which is representing SQL database type of connector. 
 * You should have <code>ConnectionFactory</code> object or 
 * <code>DataSource</code> object ready in order to construct this object.
 *
 * @author jie leng
 */
public class SQLConnectorImpl implements SQLConnector {

    /**
     * Values for the datasource user and user password to access security
     * connections
     */
    private String username = null;
    private String password = null;

    /**
     * Flag that indicates type of the transaction. Optimistic transactions do
     * not hold data store locks until commit time.
     */
    private boolean optimistic = true;

    private boolean inFlush = false;

    private boolean rollbackOnly = false;

    private boolean isActiveTransaction = false;

    /**
     * Associated Connection
     */
    private Connection _connection = null;

    /**
     * Number of users (or threads) currently using this connection.
     */
    private int _connectionReferenceCount = 0;  
    
    /**
     * Connection Factory from which this transaction gets a Connection.
     * If this connector was constructed with ConnectionFactory object,
     * then the connector asks ConnectionFactory for a connection.
     */
    private ConnectionFactory _connectionFactory = null;

    /**
     * Associated DataSource from which this transaction gets a Connection.
     * If this connector was constructed with DataSource object,
     * then the connector asks DataSource for a connection.
     */
    private Object _dataSource = null;
    
    /**
     * Flag that indicates the type of the connection object (DataSource or
     * ConnectionFactory)
     */
    private boolean isDataSource = false;

    /**
     * I18N message handler
     */
    private transient final static ResourceBundle messages = I18NHelper.loadBundle(
            "com.sun.persistence.runtime.Bundle", // NOI18N
            SQLConnectorImpl.class.getClassLoader());

    /**
     * The logger
     */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /**
     * Creates a new instance of SQLConnectorImpl
     * @param connectionFactory A <code>ConnectionFactory</code>
     * object or a <code>DataSource</code> object.
     * @param username A string for the user of the database connection.
     * @param password A string for the password of the database connection.
     */
    public SQLConnectorImpl(Object connectionFactory, String username,
            String password) {
        if (connectionFactory instanceof ConnectionFactory) {
            _connectionFactory = (ConnectionFactory) connectionFactory;
        } else {
            _dataSource = connectionFactory;
            isDataSource = true;
        }
        this.username = username;
        this.password = password;
    }

    /**
     * Informs the Connector that a transaction is beginning.
     * @param optimistic If true, then an optimistic transaction is beginning.
     * @throws JDODataStoreException is [@link setRollbackOnly} has been invoked
     * on this Connector.
     */
    public void begin(boolean optimistic) {
        isActiveTransaction = true;
        this.optimistic = optimistic;
    }

    /**
     * Informs the Connector that the transaction has reached it's
     * beforeCompletion phase.
     * @throws JDODataStoreException is [@link setRollbackOnly} has been invoked
     * on this Connector.
     */
    public void beforeCompletion() {
        inFlush = true;
    }

    /**
     * Requests that the Connector send all pending database operations to the
     * store.
     * @throws JDODataStoreException is [@link setRollbackOnly} has been invoked
     * on this Connector.
     */
    public void flush() {
        inFlush = false;
        closeConnection();
    }

    /**
     * Requests that the Connector make all changes made since the previous
     * commit/rollback permanent and releases any database locks held by the
     * Connector.
     * @throws JDODataStoreException is [@link setRollbackOnly} has been invoked
     * on this Connector.
     */
    public void commit() {
        int error = commitConnection();
        if (error != INTERNAL_OK) {
            rollbackConnection();
            throw new JDOUserException(
                    I18NHelper.getMessage(
                            messages,
                            "connection.sqlconnectorimpl.commit.error")); // NOI18N
        }
        isActiveTransaction = false;
        closeConnection();
    }

    /**
     * Requests that the Connector drop all changes made since the previous
     * commit/rollback and releases any database locks currently held by this
     * Connector.
     */
    public void rollback() {
        try {
            rollbackConnection();
        } finally {
            isActiveTransaction = false;
            closeConnection();
        }
    }

    /**
     * Requests that the Connector put itself in a state such that the only
     * allowable operations is {@link com.sun.org.apache.jdo.store.Connector#getRollbackOnly}.
     * Once set, attempts to do any other operations will result in a
     * JDODataStoreException.
     */
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    /**
     * Indicates whether or not the connector can do operations other than
     * rollback.
     * @return <code>false</code> if the connector can do operations other than
     * rollback.
     */
    public boolean getRollbackOnly() {
        return rollbackOnly;
    }

    /**
     * Returns a Connection. If there is no existing one, asks ConnectionFactory
     * for a new Connection
     */
    public synchronized Connection getConnection() {
        boolean debug = logger.isLoggable(Logger.FINEST);

        if (_connection == null) {

            // find a new connection
            if (_connectionFactory == null && _dataSource == null) {
                throw new JDOFatalInternalException(
                        I18NHelper.getMessage(
                                messages,
                                "connection.sqlconnectorimpl.getconnection.nullcf")); // NOI18N
            }

            _connection = this.getConnectionInternal();
        }

        _connectionReferenceCount++;

        if (debug) {
            Object[] items = new Object[]{_connection, Boolean.valueOf(
                    optimistic), new Integer(_connectionReferenceCount)};
            logger.finest("connection.sqlconnectorimpl.getconnection", items); // NOI18N
        }

        // We cannot depend on NON_MGD flag here as this method can be called
        // outside of an active transaction.
        if (!EJBImplHelper.isManaged()) {
            try {
                //
                // For active pessimistic transaction or a committing transaction, we need to set
                // auto-commit feature off.
                //
                if ((!optimistic && isActiveTransaction) || inFlush) {
                    // Set autocommit to false *only* if it's true
                    // I.e., don't set to false multiple times (Sybase
                    // throws exception in that case).
                    if (_connection.getAutoCommit()) {
                        _connection.setAutoCommit(false);
                    }
                } else {
                    _connection.setAutoCommit(true);
                }
            } catch (java.sql.SQLException e) {
                logger.log(Logger.WARNING, "runtime.exception.log", e);  // NOI18N
            }
        }

        return _connection;
    }

    /**
     * Replace a connection. Used in a managed environment only. In a J2EE RI
     * Connection need to be replaced at the beforeCompletion.
     */
    public void replaceConnection() {
        if (EJBImplHelper.isManaged()) {
            this.releaseConnection();
            this.closeConnection();
            this.getConnection();
        }
    }

    /**
     * Close a connection. Connection cannot be closed if it is part of the
     * commit/rollback operation or inside a pessimistic transaction
     */
    public synchronized void releaseConnection() {
        boolean debug = logger.isLoggable(Logger.FINEST);

        if (_connectionReferenceCount > 0) {
            _connectionReferenceCount--;
        }

        if (debug) {
            Object[] items = new Object[]{Boolean.valueOf(optimistic), Boolean.valueOf(
                    inFlush), new Integer(_connectionReferenceCount)};
            logger.finest(
                    "connection.sqlconnectorimpl.releaseconnection", items); // NOI18N
        }

        // Fix for bug 4479807: Do not keep connection in the managed environment.
        if ((!EJBImplHelper.isManaged() && optimistic == false)
                || inFlush) {
            // keep Connection. Do not close.
            return;
        }

        if (_connectionReferenceCount == 0) {
            //
            // For optimistic transaction, we only release the connection when
            // no one is using it.
            //
            closeConnection();
        }
    }

    private Connection getConnectionInternal() {
        if (isDataSource) {
            try {
                if (EJBImplHelper.isManaged()) {
                    // Delegate to the EJBImplHelper for details.
                    if (isActiveTransaction) {
                        return (Connection) EJBImplHelper.getConnection(
                                _dataSource, username, password);
                    } else {
                        return (Connection) EJBImplHelper.getNonTransactionalConnection(
                                _dataSource, username, password);
                    }
                } else if (username != null) {
                    return ((DataSource) _dataSource).getConnection(
                            username, password);
                } else {
                    return ((DataSource) _dataSource).getConnection();
                }

            } catch (java.sql.SQLException e) {
                String sqlState = e.getSQLState();
                int errorCode = e.getErrorCode();

                if (sqlState == null) {
                    throw new JDODataStoreException(
                            I18NHelper.getMessage(
                                    messages, "connection.connectionfactoryimpl.sqlexception", // NOI18N
                                    "null", "" + errorCode), e); // NOI18N
                } else {
                    throw new JDODataStoreException(
                            I18NHelper.getMessage(
                                    messages, "connection.connectionfactoryimpl.sqlexception", // NOI18N
                                    sqlState, "" + errorCode), e); // NOI18N
                }
            }
        } else {
            return _connectionFactory.getConnection();
        }
    }

    /**
     * Always Close a connection
     */
    private void closeConnection() {
        boolean debug = logger.isLoggable(Logger.FINEST);
        if (debug) {
            Object[] items = new Object[]{_connection};
            logger.finest("sqlstore.transactionimpl.closeconnection", items); // NOI18N
        }
        try {
            if (_connection != null) {
                _connection.close();
            }
        } catch (Exception e) {
            // Recover?
        }
        _connection = null;
    }

    private void rollbackConnection() {
        boolean debug = logger.isLoggable(Logger.FINEST);
        if (debug) {
            Object[] items = new Object[]{_connection};
            logger.finest(
                    "connection.sqlconnectorimpl.rollbackconnection", items); // NOI18N
        }
        if (_connection != null) {
            try {
                if (isDataSource) {
                    _connection.rollback();
                } else {
                    ((ConnectionImpl) _connection).internalRollback();
                }
            } catch (Exception e) {
                //Recover?
            }
        }
    }

    private int INTERNAL_ERROR = 1;
    private int INTERNAL_OK = 0;

    private int commitConnection() {
        if (_connection != null) {
            try {
                if (isDataSource) {
                    _connection.commit();
                } else {
                    ((ConnectionImpl) _connection).internalCommit();
                }
            } catch (Exception e) {
                return INTERNAL_ERROR;
            }
        }
        return INTERNAL_OK;
    }
}
