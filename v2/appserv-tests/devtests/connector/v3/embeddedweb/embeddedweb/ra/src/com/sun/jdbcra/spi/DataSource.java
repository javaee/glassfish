/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.jdbcra.spi;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.ResourceException;
import javax.naming.Reference;

import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Holds the <code>java.sql.Connection</code> object, which is to be 
 * passed to the application program.
 *
 * @version	1.0, 02/07/31
 * @author	Binod P.G
 */
public class DataSource implements javax.sql.DataSource, java.io.Serializable,
		com.sun.appserv.jdbcra.DataSource, javax.resource.Referenceable{ 
				   
    private ManagedConnectionFactory mcf;
    private ConnectionManager cm;				   				   
    private int loginTimeout;
    private PrintWriter logWriter;
    private String description;				   
    private Reference reference;
    
    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }
    private boolean debug = false;

    /**
     * Constructs <code>DataSource</code> object. This is created by the 
     * <code>ManagedConnectionFactory</code> object.
     *
     * @param	mcf	<code>ManagedConnectionFactory</code> object 
     *			creating this object.
     * @param	cm	<code>ConnectionManager</code> object either associated
     *			with Application server or Resource Adapter.
     */
    public DataSource (ManagedConnectionFactory mcf, ConnectionManager cm) {
    	this.mcf = mcf;
    	if (cm == null) {
    	    this.cm = (ConnectionManager) new com.sun.jdbcra.spi.ConnectionManager();
    	} else {
    	    this.cm = cm;
    	}    	
    }   
    
    /**
     * Retrieves the <code> Connection </code> object.
     * 
     * @return	<code> Connection </code> object.
     * @throws SQLException In case of an error.      
     */
    public Connection getConnection() throws SQLException {
    	try {
    	    return (Connection) cm.allocateConnection(mcf,null);    	    
    	} catch (ResourceException re) {
// This is temporary. This needs to be changed to SEVERE after TP
	    _logger.log(Level.WARNING, "jdbc.exc_get_conn", re.getMessage());
            re.printStackTrace();
    	    throw new SQLException (re.getMessage());
    	}
    }
    
    /**
     * Retrieves the <code> Connection </code> object.
     * 
     * @param	user	User name for the Connection.
     * @param	pwd	Password for the Connection.
     * @return	<code> Connection </code> object.
     * @throws SQLException In case of an error.      
     */
    public Connection getConnection(String user, String pwd) throws SQLException {
    	try {
    	    ConnectionRequestInfo info = new ConnectionRequestInfo (user, pwd);
    	    return (Connection) cm.allocateConnection(mcf,info);
    	} catch (ResourceException re) {
// This is temporary. This needs to be changed to SEVERE after TP
	    _logger.log(Level.WARNING, "jdbc.exc_get_conn", re.getMessage());
    	    throw new SQLException (re.getMessage());
    	}
    }    

    /**
     * Retrieves the actual SQLConnection from the Connection wrapper
     * implementation of SunONE application server. If an actual connection is
     * supplied as argument, then it will be just returned.
     *
     * @param con Connection obtained from <code>Datasource.getConnection()</code>
     * @return <code>java.sql.Connection</code> implementation of the driver.
     * @throws <code>java.sql.SQLException</code> If connection cannot be obtained.
     */
    public Connection getConnection(Connection con) throws SQLException {

        Connection driverCon = con; 
        if (con instanceof com.sun.jdbcra.spi.ConnectionHolder) {
           driverCon = ((com.sun.jdbcra.spi.ConnectionHolder) con).getConnection(); 
        } 

        return driverCon;
    }
    
    /**
     * Get the login timeout
     *
     * @return login timeout.
     * @throws	SQLException	If a database error occurs.
     */
    public int getLoginTimeout() throws SQLException{
    	return	loginTimeout;
    }
    
    /**
     * Set the login timeout
     *
     * @param	loginTimeout	Login timeout.
     * @throws	SQLException	If a database error occurs.
     */
    public void setLoginTimeout(int loginTimeout) throws SQLException{
    	this.loginTimeout = loginTimeout;
    }
    
    /** 
     * Get the logwriter object.
     *
     * @return <code> PrintWriter </code> object.
     * @throws	SQLException	If a database error occurs.
     */
    public PrintWriter getLogWriter() throws SQLException{
    	return	logWriter;
    }
        
    /**
     * Set the logwriter on this object.
     *
     * @param <code>PrintWriter</code> object.
     * @throws	SQLException	If a database error occurs.
     */
    public void setLogWriter(PrintWriter logWriter) throws SQLException{
    	this.logWriter = logWriter;
    }        
        
    /**
     * Retrieves the description.
     *
     * @return	Description about the DataSource.
     */
    public String getDescription() {
    	return description;
    }
    
    /**
     * Set the description.
     *
     * @param description Description about the DataSource.
     */
    public void setDescription(String description) {
    	this.description = description;
    }    
    
    /**
     * Get the reference.
     *
     * @return <code>Reference</code>object.
     */
    public Reference getReference() {
    	return reference;
    }
    
    /**
     * Get the reference.
     *
     * @param	reference <code>Reference</code> object.
     */
    public void setReference(Reference reference) {
    	this.reference = reference;
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
