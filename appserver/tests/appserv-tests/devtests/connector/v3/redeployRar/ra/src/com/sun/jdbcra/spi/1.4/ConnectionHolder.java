/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jdbcra.spi;

import java.sql.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Holds the java.sql.Connection object, which is to be 
 * passed to the application program.
 *
 * @version	1.0, 02/07/23
 * @author	Binod P.G
 */
public class ConnectionHolder implements Connection{ 

    private Connection con;
    
    private ManagedConnection mc;
    
    private boolean wrappedAlready = false;    
    
    private boolean isClosed = false;
    
    private boolean valid = true;
    
    private boolean active = false;
    /**
     * The active flag is false when the connection handle is
     * created. When a method is invoked on this object, it asks
     * the ManagedConnection if it can be the active connection
     * handle out of the multiple connection handles. If the
     * ManagedConnection reports that this connection handle
     * can be active by setting this flag to true via the setActive
     * function, the above method invocation succeeds; otherwise
     * an exception is thrown.
     */
    
    /**
     * Constructs a Connection holder.
     * 
     * @param	con	<code>java.sql.Connection</code> object.
     */
    public ConnectionHolder(Connection con, ManagedConnection mc) {
        this.con = con;
        this.mc  = mc;
    }
        
    /**
     * Returns the actual connection in this holder object.
     * 
     * @return	Connection object.
     */
    Connection getConnection() {
    	return con;
    }
    
    /**
     * Sets the flag to indicate that, the connection is wrapped already or not.
     *
     * @param	wrapFlag	
     */
    void wrapped(boolean wrapFlag){
        this.wrappedAlready = wrapFlag;
    }
    
    /**
     * Returns whether it is wrapped already or not.
     *
     * @return	wrapped flag.
     */
    boolean isWrapped(){
        return wrappedAlready;
    }
    
    /**
     * Returns the <code>ManagedConnection</code> instance responsible
     * for this connection.
     *
     * @return	<code>ManagedConnection</code> instance.
     */
    ManagedConnection getManagedConnection() {
        return mc;
    }
    
    /**
     * Replace the actual <code>java.sql.Connection</code> object with the one
     * supplied. Also replace <code>ManagedConnection</code> link.     
     *
     * @param	con <code>Connection</code> object.
     * @param	mc  <code> ManagedConnection</code> object.
     */
    void associateConnection(Connection con, ManagedConnection mc) {
    	this.mc = mc;
    	this.con = con;
    }
    
    /**
     * Clears all warnings reported for the underlying connection  object.
     *
     * @throws SQLException In case of a database error.      
     */
    public void clearWarnings() throws SQLException{    
	checkValidity();
        con.clearWarnings();
    }
    
    /**
     * Closes the logical connection.    
     *
     * @throws SQLException In case of a database error. 
     */
    public void close() throws SQLException{
        isClosed = true;
        mc.connectionClosed(null, this);        
    }
    
    /**
     * Invalidates this object.
     */
    public void invalidate() {
    	valid = false;
    }
    
    /**
     * Closes the physical connection involved in this.
     *
     * @throws SQLException In case of a database error.      
     */
    void actualClose() throws SQLException{    
        con.close();
    }
    
    /**
     * Commit the changes in the underlying Connection.
     *
     * @throws SQLException In case of a database error.      
     */
    public void commit() throws SQLException {
	checkValidity();    
    	con.commit();
    }
    
    /**
     * Creates a statement from the underlying Connection
     *
     * @return	<code>Statement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public Statement createStatement() throws SQLException {
	checkValidity();    
        return con.createStatement();
    }
    
    /**
     * Creates a statement from the underlying Connection.
     *
     * @param	resultSetType	Type of the ResultSet
     * @param	resultSetConcurrency	ResultSet Concurrency.
     * @return	<code>Statement</code> object.     
     * @throws SQLException In case of a database error.      
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
	checkValidity();    
        return con.createStatement(resultSetType, resultSetConcurrency);
    }
    
    /**
     * Creates a statement from the underlying Connection.
     *
     * @param	resultSetType	Type of the ResultSet
     * @param	resultSetConcurrency	ResultSet Concurrency.
     * @param	resultSetHoldability	ResultSet Holdability.     
     * @return	<code>Statement</code> object. 
     * @throws SQLException In case of a database error.          
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency, 
    				     int resultSetHoldabilty) throws SQLException {
	checkValidity();    				     
        return con.createStatement(resultSetType, resultSetConcurrency,
        			   resultSetHoldabilty);
    }
 
    /**
     * Retrieves the current auto-commit mode for the underlying <code> Connection</code>.
     *
     * @return The current state of connection's auto-commit mode.
     * @throws SQLException In case of a database error.      
     */
    public boolean getAutoCommit() throws SQLException {
	checkValidity();    
    	return con.getAutoCommit();
    }   
 
    /**
     * Retrieves the underlying <code>Connection</code> object's catalog name.
     *
     * @return	Catalog Name.
     * @throws SQLException In case of a database error.      
     */
    public String getCatalog() throws SQLException {
	checkValidity();    
        return con.getCatalog();
    }
    
    /**
     * Retrieves the current holdability of <code>ResultSet</code> objects created
     * using this connection object.
     *
     * @return	holdability value.
     * @throws SQLException In case of a database error.      
     */
    public int getHoldability() throws SQLException {
	checkValidity();    
    	return	con.getHoldability();
    }
    
    /**
     * Retrieves the <code>DatabaseMetaData</code>object from the underlying
     * <code> Connection </code> object.
     *
     * @return <code>DatabaseMetaData</code> object.
     * @throws SQLException In case of a database error.      
     */
    public DatabaseMetaData getMetaData() throws SQLException {
	checkValidity();    
    	return con.getMetaData();
    }
 
    /**
     * Retrieves this <code>Connection</code> object's current transaction isolation level.
     *
     * @return Transaction level
     * @throws SQLException In case of a database error.      
     */   
    public int getTransactionIsolation() throws SQLException {
	checkValidity();    
        return con.getTransactionIsolation();
    }
    
    /**
     * Retrieves the <code>Map</code> object associated with 
     * <code> Connection</code> Object.
     *
     * @return	TypeMap set in this object.
     * @throws SQLException In case of a database error.      
     */
    public Map getTypeMap() throws SQLException {
	checkValidity();    
    	return con.getTypeMap();
    }

    /**
     * Retrieves the the first warning reported by calls on the underlying
     * <code>Connection</code> object.
     *
     * @return First <code> SQLWarning</code> Object or null.
     * @throws SQLException In case of a database error.      
     */
    public SQLWarning getWarnings() throws SQLException {
	checkValidity();    
    	return con.getWarnings();
    }
    
    /**
     * Retrieves whether underlying <code>Connection</code> object is closed.
     *
     * @return	true if <code>Connection</code> object is closed, false
     * 		if it is closed.
     * @throws SQLException In case of a database error.      
     */
    public boolean isClosed() throws SQLException {
    	return isClosed;
    }
    
    /**
     * Retrieves whether this <code>Connection</code> object is read-only.
     *
     * @return	true if <code> Connection </code> is read-only, false other-wise
     * @throws SQLException In case of a database error.      
     */
    public boolean isReadOnly() throws SQLException {
	checkValidity();    
    	return con.isReadOnly();
    }
    
    /**
     * Converts the given SQL statement into the system's native SQL grammer.
     *
     * @param	sql	SQL statement , to be converted.
     * @return	Converted SQL string.
     * @throws SQLException In case of a database error.      
     */
    public String nativeSQL(String sql) throws SQLException {
	checkValidity();    
    	return con.nativeSQL(sql);
    }
    
    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param	sql	SQL Statement
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
	checkValidity();    
    	return con.prepareCall(sql);
    }
    
    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param	sql	SQL Statement
     * @param	resultSetType	Type of the ResultSet
     * @param	resultSetConcurrency	ResultSet Concurrency.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public CallableStatement prepareCall(String sql,int resultSetType, 
    					int resultSetConcurrency) throws SQLException{
	checkValidity();    					
    	return con.prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    
    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param	sql	SQL Statement
     * @param	resultSetType	Type of the ResultSet
     * @param	resultSetConcurrency	ResultSet Concurrency.
     * @param	resultSetHoldability	ResultSet Holdability.          
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public CallableStatement prepareCall(String sql, int resultSetType, 
    					 int resultSetConcurrency, 
    					 int resultSetHoldabilty) throws SQLException{
	checkValidity();    					 
    	return con.prepareCall(sql, resultSetType, resultSetConcurrency,
    			       resultSetHoldabilty);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending 
     * paramterized SQL statements to database
     *
     * @param	sql	SQL Statement
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
	checkValidity();    
    	return con.prepareStatement(sql);
    }
    
    /**
     * Creates a <code> PreparedStatement </code> object for sending 
     * paramterized SQL statements to database
     *
     * @param	sql	SQL Statement
     * @param	autoGeneratedKeys a flag indicating AutoGeneratedKeys need to be returned.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
	checkValidity();    
    	return con.prepareStatement(sql,autoGeneratedKeys);
    }    
      
    /**
     * Creates a <code> PreparedStatement </code> object for sending 
     * paramterized SQL statements to database
     *
     * @param	sql	SQL Statement
     * @param	columnIndexes an array of column indexes indicating the columns that should be
     *		returned from the inserted row or rows.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
	checkValidity();    
    	return con.prepareStatement(sql,columnIndexes);
    }          
 
    /**
     * Creates a <code> PreparedStatement </code> object for sending 
     * paramterized SQL statements to database
     *
     * @param	sql	SQL Statement
     * @param	resultSetType	Type of the ResultSet
     * @param	resultSetConcurrency	ResultSet Concurrency.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public PreparedStatement prepareStatement(String sql,int resultSetType, 
    					int resultSetConcurrency) throws SQLException{
	checkValidity();    					
    	return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    /**
     * Creates a <code> PreparedStatement </code> object for sending 
     * paramterized SQL statements to database
     *
     * @param	sql	SQL Statement
     * @param	resultSetType	Type of the ResultSet
     * @param	resultSetConcurrency	ResultSet Concurrency.
     * @param	resultSetHoldability	ResultSet Holdability.          
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, 
    					 int resultSetConcurrency, 
    					 int resultSetHoldabilty) throws SQLException {
	checkValidity();    					 
    	return con.prepareStatement(sql, resultSetType, resultSetConcurrency,
    				    resultSetHoldabilty);
    }           

    /**
     * Creates a <code> PreparedStatement </code> object for sending 
     * paramterized SQL statements to database
     *
     * @param	sql	SQL Statement
     * @param	columnNames Name of bound columns.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.      
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
	checkValidity();    
    	return con.prepareStatement(sql,columnNames);
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

    /**
     * Removes the given <code>Savepoint</code> object from the current transaction.
     *
     * @param	savepoint	<code>Savepoint</code> object
     * @throws SQLException In case of a database error.      
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
	checkValidity();    
    	con.releaseSavepoint(savepoint);
    } 
    
    /**
     * Rolls back the changes made in the current transaction.
     *
     * @throws SQLException In case of a database error.      
     */        
    public void rollback() throws SQLException {
	checkValidity();    
    	con.rollback();
    }
    
    /**
     * Rolls back the changes made after the savepoint.
     *
     * @throws SQLException In case of a database error.      
     */        
    public void rollback(Savepoint savepoint) throws SQLException {
	checkValidity();    
    	con.rollback(savepoint);
    }
        
    /**
     * Sets the auto-commmit mode of the <code>Connection</code> object.
     *
     * @param	autoCommit boolean value indicating the auto-commit mode.     
     * @throws SQLException In case of a database error.      
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
	checkValidity();    
    	con.setAutoCommit(autoCommit);
    }
    
    /**
     * Sets the catalog name to the <code>Connection</code> object
     *
     * @param	catalog	Catalog name.
     * @throws SQLException In case of a database error.      
     */
    public void setCatalog(String catalog) throws SQLException {
	checkValidity();    
    	con.setCatalog(catalog);
    }
    
    /**
     * Sets the holdability of <code>ResultSet</code> objects created 
     * using this <code>Connection</code> object.
     *
     * @param	holdability	A <code>ResultSet</code> holdability constant
     * @throws SQLException In case of a database error.      
     */
    public void setHoldability(int holdability) throws SQLException {
	checkValidity();    
     	con.setHoldability(holdability);
    }
    
    /**
     * Puts the connection in read-only mode as a hint to the driver to 
     * perform database optimizations.
     *
     * @param	readOnly  true enables read-only mode, false disables it.
     * @throws SQLException In case of a database error.      
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
	checkValidity();    
    	con.setReadOnly(readOnly);
    }
    
    /**
     * Creates and unnamed savepoint and returns an object corresponding to that.
     *
     * @return	<code>Savepoint</code> object.
     * @throws SQLException In case of a database error.
     */
    public Savepoint setSavepoint() throws SQLException {
	checkValidity();    
    	return con.setSavepoint();
    }
    
    /**
     * Creates a savepoint with the name and returns an object corresponding to that.
     *
     * @param	name	Name of the savepoint.
     * @return	<code>Savepoint</code> object.
     * @throws SQLException In case of a database error.      
     */
    public Savepoint setSavepoint(String name) throws SQLException {
	checkValidity();    
    	return con.setSavepoint(name);
    }    
    
    /**
     * Creates the transaction isolation level.
     *
     * @param	level transaction isolation level.
     * @throws SQLException In case of a database error.      
     */
    public void setTransactionIsolation(int level) throws SQLException {
	checkValidity();    
    	con.setTransactionIsolation(level);
    }     
    
    /**
     * Installs the given <code>Map</code> object as the tyoe map for this 
     * <code> Connection </code> object.
     *
     * @param	map	<code>Map</code> a Map object to install.
     * @throws SQLException In case of a database error.      
     */
    public void setTypeMap(Map map) throws SQLException {
	checkValidity();
    	con.setTypeMap(map);
    }

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
    
    
    /**
     * Checks the validity of this object
     */
    private void checkValidity() throws SQLException {
    	if (isClosed) throw new SQLException ("Connection closed");
    	if (!valid) throw new SQLException ("Invalid Connection");
    	if(active == false) {
    	    mc.checkIfActive(this);
    	}
    }
    
    /**
     * Sets the active flag to true
     *
     * @param	actv	boolean
     */
    void setActive(boolean actv) {
        active = actv;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
