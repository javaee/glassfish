/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2002-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.connector.blackbox;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.IllegalStateException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author Tony Ng
 */
public class JdbcConnection implements Connection {

    // if mc is null, means connection is invalid
    private JdbcManagedConnection mc;
    private boolean supportsLocalTx;

    public JdbcConnection(JdbcManagedConnection mc,
                          boolean supportsLocalTx) {
        this.mc = mc;
        this.supportsLocalTx = supportsLocalTx;
    }

    public Statement createStatement() throws SQLException {
        Connection con = getJdbcConnection();
        return con.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        Connection con = getJdbcConnection();
        return con.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        if (!supportsLocalTx) {
            throw new SQLException("setAutoCommit not supported in NoTransaction level resource adapter");
        }
        Connection con = getJdbcConnection();
        con.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        if (!supportsLocalTx) {
            throw new SQLException("getAutoCommit not supported in NoTransaction level resource adapter");
        }
        Connection con = getJdbcConnection();
        return con.getAutoCommit();
    }

    public void commit() throws SQLException {
        if (!supportsLocalTx) {
            throw new SQLException("commit not supported in NoTransaction level resource adapter");
        }
        Connection con = getJdbcConnection();
        con.commit();
    }

    public void rollback() throws SQLException {
        if (!supportsLocalTx) {
            throw new SQLException("rollback not supported in NoTransaction level resource adapter");
        }
        Connection con = getJdbcConnection();
        con.rollback();
    }

    public void close() throws SQLException {
        if (mc == null) return;  // already be closed
        mc.removeJdbcConnection(this);
        mc.sendEvent(ConnectionEvent.CONNECTION_CLOSED, null, this);
        mc = null;
    }

    public boolean isClosed() throws SQLException {
        return (mc == null);
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        Connection con = getJdbcConnection();
        return con.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        Connection con = getJdbcConnection();
        con.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        Connection con = getJdbcConnection();
        return con.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        Connection con = getJdbcConnection();
        con.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        Connection con = getJdbcConnection();
        return con.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        Connection con = getJdbcConnection();
        con.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        Connection con = getJdbcConnection();
        return con.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        Connection con = getJdbcConnection();
        return con.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        Connection con = getJdbcConnection();
        con.clearWarnings();
    }

    public Map getTypeMap() throws SQLException {
        Connection con = getJdbcConnection();
        return con.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public PreparedStatement prepareStatement(String sql,
                                              int resultSetType,
                                              int resultSetConcurrency)
            throws SQLException {

        Connection con = getJdbcConnection();
        return con.prepareStatement(sql, resultSetType,
                resultSetConcurrency);
    }

    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency)
            throws SQLException {

        Connection con = getJdbcConnection();
        return con.createStatement(resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency)
            throws SQLException {

        Connection con = getJdbcConnection();
        return con.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    /////////////////////////////////////////////
    // THE FOLLOWING APIS ARE NEW FROM JDK 1.4 //        
    /////////////////////////////////////////////

    /////////////  BEGIN  JDK 1.4  //////////////

    public int getHoldability()
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.getHoldability();
    }

    public void setHoldability(int holdability)
            throws SQLException {
        Connection con = getJdbcConnection();
        con.setHoldability(holdability);
    }

    public Savepoint setSavepoint()
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.setSavepoint();
    }

    public Savepoint setSavepoint(String name)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint)
            throws SQLException {
        Connection con = getJdbcConnection();
        con.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint)
            throws SQLException {
        Connection con = getJdbcConnection();
        con.releaseSavepoint(savepoint);
    }

    public Statement createStatement(
            int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.createStatement(
                resultSetType,
                resultSetConcurrency,
                resultSetHoldability);
    }

    public PreparedStatement prepareStatement(
            String sql,
            int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareStatement(
                sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(
            String sql,
            int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareCall(
                sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(
            String sql,
            int autoGeneratedKeys)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(
            String sql,
            int[] columnIndexes)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(
            String sql,
            String[] columnNames)
            throws SQLException {
        Connection con = getJdbcConnection();
        return con.prepareStatement(sql, columnNames);
    }

    public Clob createClob() throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Blob createBlob() throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public NClob createNClob() throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SQLXML createSQLXML() throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isValid(int timeout) throws SQLException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getClientInfo(String name) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Properties getClientInfo() throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /////////////  END  JDK 1.4  //////////////
    
    public int getNetworkTimeout() throws SQLException {
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }
    
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }
    
    public void abort(Executor executor)  throws SQLException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }
    
    public String getSchema() throws SQLException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }
    
    public void setSchema(String schema) throws SQLException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }
    

    void associateConnection(JdbcManagedConnection newMc)
            throws ResourceException {

        try {
            checkIfValid();
        } catch (SQLException ex) {
            throw new IllegalStateException("Connection is invalid");
        }
        // dissociate handle with current managed connection
        mc.removeJdbcConnection(this);
        // associate handle with new managed connection
        newMc.addJdbcConnection(this);
        mc = newMc;
    }

    void checkIfValid() throws SQLException {
        if (mc == null) {
            throw new SQLException("Connection is invalid");
        }
    }

    Connection getJdbcConnection() throws SQLException {
        checkIfValid();
        try {
            return mc.getJdbcConnection();
        } catch (ResourceException ex) {
            throw new SQLException("Connection is invalid");
        }
    }

    void invalidate() {
        mc = null;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
