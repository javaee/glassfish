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
 * ConnectionFactory.java
 *
 * Created on March 7, 2000, 5:09 PM
 */


package com.sun.persistence.runtime.connection;

import java.io.PrintWriter;
import java.sql.Connection;

/**
 * @author Craig Russell
 * @version 0.1
 */
public interface ConnectionFactory {

    /**
     * Returns java.sql.Connection
     * @return connection as java.sql.Connection
     */
    Connection getConnection();

    /**
     * Sets JDBC driver name
     * @param driverName JDBC driver name
     */
    void setDriverName(String driverName);

    /**
     * Returns JDBC driver name
     * @return driver name
     */
    String getDriverName();

    /**
     * Sets JDBC connection URL
     * @param URL connection URL
     */
    void setURL(String URL);

    /**
     * Returns connection URL
     * @return connection URL
     */
    String getURL();

    /**
     * Sets database user
     * @param userName database user
     */
    void setUserName(String userName);

    /**
     * Returns database user name
     * @return current database user name
     */
    String getUserName();

    /**
     * Sets database user password
     * @param password database user password
     */
    void setPassword(String password);

    /**
     * Sets minimum number of connections in the connection pool
     * @param minPool minimum number of connections
     */
    void setMinPool(int minPool);

    /**
     * Returns minimum number of connections in the connection pool
     * @return connection minPool
     */
    int getMinPool();

    /**
     * Sets maximum number of connections in the connection pool
     * @param maxPool maximum number of connections
     */
    void setMaxPool(int maxPool);

    /**
     * Returns maximum number of connections in the connection pool
     * @return connection maxPool
     */
    int getMaxPool();

    /**
     * Sets the amount of time, in milliseconds, between the connection
     * manager's attempts to get a pooled connection.
     * @param msInterval the interval between attempts to get a database
     * connection, in milliseconds.
     */
    void setMsInterval(int msInterval);

    /**
     * Returns the amount of time, in milliseconds, between the connection
     * manager's attempts to get a pooled connection.
     * @return the length of the interval between tries in milliseconds
     */
    int getMsInterval();

    /**
     * Sets the number of milliseconds to wait for an available connection from
     * the connection pool before throwing an exception
     * @param msWait number in milliseconds
     */
    void setMsWait(int msWait);

    /**
     * Returns the number of milliseconds to wait for an available connection
     * from the connection pool before throwing an exception
     * @return number in milliseconds
     */
    int getMsWait();

    /**
     * Sets the LogWriter to which messages should be sent
     * @param pw logWriter
     */
    void setLogWriter(PrintWriter logWriter);

    /**
     * Returns the LogWriter to which messages should be sent
     * @return logWriter
     */
    PrintWriter getLogWriter();

    /**
     * Sets the number of seconds to wait for a new connection to be established
     * to the data source
     * @param loginTimeout wait time in seconds
     */
    void setLoginTimeout(int loginTimeout);

    /**
     * Returns the number of seconds to wait for a new connection to be
     * established to the data source
     * @return wait time in seconds
     */
    int getLoginTimeout();

    /**
     * Sets transaction isolation level for all connections of this
     * ConnectionFactory. All validation is done by java.sql.Connection itself,
     * so e.g. while Oracle will not allow to set solation level to
     * TRANSACTION_REPEATABLE_READ, this method does not have any explicit
     * restrictions
     * @param level - one of the java.sql.Connection.TRANSACTION_* isolation
     * values
     */
    void setTransactionIsolation(int level);

    /**
     * Returns current transaction isolation level for connections of this
     * ConnectionFactory.
     * @return the current transaction isolation mode value as
     *         java.sql.Connection.TRANSACTION_*
     */
    int getTransactionIsolation();
}
