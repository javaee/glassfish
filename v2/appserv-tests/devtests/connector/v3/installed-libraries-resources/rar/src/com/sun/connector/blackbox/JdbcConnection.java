/*
 * Use of this J2EE Connectors Sample Source Code file is governed by
 * the following modified BSD license:
 * 
 * Copyright 2002 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * -Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * -Redistribution in binary form must reproduct the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind.
 * ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND
 * ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES OR LIABILITIES
 * SUFFERED BY LICENSEE AS A RESULT OF  OR RELATING TO USE, MODIFICATION
 * OR DISTRIBUTION OF THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL
 * SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA,
 * OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
 * LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that Software is not designed, licensed or intended
 * for use in the design, construction, operation or maintenance of any
 * nuclear facility.
 */

package com.sun.connector.blackbox;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.IllegalStateException;
import java.sql.*;
import java.util.Map;
import java.util.Properties;

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

    public void setTypeMap(Map map) throws SQLException {
        Connection con = getJdbcConnection();
        con.setTypeMap(map);
    }

    public Map getTypeMap() throws SQLException {
        Connection con = getJdbcConnection();
        return con.getTypeMap();
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
