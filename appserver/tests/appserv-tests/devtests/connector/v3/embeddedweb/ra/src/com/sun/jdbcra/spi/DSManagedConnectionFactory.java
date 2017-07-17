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

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import com.sun.jdbcra.spi.ConnectionManager;
import com.sun.jdbcra.common.DataSourceSpec;
import com.sun.jdbcra.common.DataSourceObjectBuilder;
import com.sun.jdbcra.util.SecurityUtils;
import javax.resource.spi.security.PasswordCredential;
import com.sun.jdbcra.spi.ManagedConnectionFactory;
import com.sun.jdbcra.common.DataSourceSpec;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Data Source <code>ManagedConnectionFactory</code> implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/07/30
 * @author	Evani Sai Surya Kiran
 */

public class DSManagedConnectionFactory extends ManagedConnectionFactory {
   
    private transient javax.sql.DataSource dataSourceObj;
    
    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }
    private boolean debug = false;
    
    /**
     * Creates a new physical connection to the underlying EIS resource
     * manager.
     * 
     * @param	subject	<code>Subject</code> instance passed by the application server
     * @param	cxRequestInfo	<code>ConnectionRequestInfo</code> which may be created
     *       	             	as a result of the invocation <code>getConnection(user, password)</code>
     *       	             	on the <code>DataSource</code> object
     * @return	<code>ManagedConnection</code> object created
     * @throws	ResourceException	if there is an error in instantiating the
     *        	                 	<code>DataSource</code> object used for the
     *       				creation of the <code>ManagedConnection</code> object
     * @throws	SecurityException	if there ino <code>PasswordCredential</code> object
     *        	                 	satisfying this request 
     * @throws	ResourceAllocationException	if there is an error in allocating the 
     *						physical connection
     */
    public javax.resource.spi.ManagedConnection createManagedConnection(javax.security.auth.Subject subject,
        ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        if(logWriter != null) {
                logWriter.println("In createManagedConnection");
        }
        PasswordCredential pc = SecurityUtils.getPasswordCredential(this, subject, cxRequestInfo);
        
        if(dataSourceObj == null) {
            if(dsObjBuilder == null) {
                dsObjBuilder = new DataSourceObjectBuilder(spec);
            }

            System.out.println("V3-TEST : before getting datasource object");
            try {
                dataSourceObj = (javax.sql.DataSource) dsObjBuilder.constructDataSourceObject();
            } catch(ClassCastException cce) {
	        _logger.log(Level.SEVERE, "jdbc.exc_cce", cce);
                throw new javax.resource.ResourceException(cce.getMessage());
            }
        }
        
        java.sql.Connection dsConn = null;
        
        try {
	    /* For the case where the user/passwd of the connection pool is
	     * equal to the PasswordCredential for the connection request
	     * get a connection from this pool directly.
	     * for all other conditions go create a new connection
	     */
	    if ( isEqual( pc, getUser(), getPassword() ) ) {
	        dsConn = dataSourceObj.getConnection();
	    } else {
	        dsConn = dataSourceObj.getConnection(pc.getUserName(), 
		    new String(pc.getPassword()));
	    }
        } catch(java.sql.SQLException sqle) {
            sqle.printStackTrace();
	    _logger.log(Level.WARNING, "jdbc.exc_create_conn", sqle);
            throw new javax.resource.spi.ResourceAllocationException("The connection could not be allocated: " + 
                sqle.getMessage());
        } catch(Exception e){
            System.out.println("V3-TEST : unable to get connection");
            e.printStackTrace();   
        }
        System.out.println("V3-TEST : got connection");
        
        com.sun.jdbcra.spi.ManagedConnection mc = new com.sun.jdbcra.spi.ManagedConnection(null, dsConn, pc, this);
        //GJCINT
/*
        setIsolation(mc);
        isValid(mc);
*/
        System.out.println("V3-TEST : returning connection");
        return mc;
    }
    
    /**
     * Check if this <code>ManagedConnectionFactory</code> is equal to 
     * another <code>ManagedConnectionFactory</code>.
     * 
     * @param	other	<code>ManagedConnectionFactory</code> object for checking equality with
     * @return	true	if the property sets of both the 
     *			<code>ManagedConnectionFactory</code> objects are the same
     *		false	otherwise
     */
    public boolean equals(Object other) {
        if(logWriter != null) {
                logWriter.println("In equals");
        }
        /**
         * The check below means that two ManagedConnectionFactory objects are equal
         * if and only if their properties are the same.
         */
        if(other instanceof com.sun.jdbcra.spi.DSManagedConnectionFactory) {
            com.sun.jdbcra.spi.DSManagedConnectionFactory otherMCF = 
                (com.sun.jdbcra.spi.DSManagedConnectionFactory) other;
            return this.spec.equals(otherMCF.spec);
        }
        return false;
    }
    
    /**
     * Sets the server name.
     *
     * @param	serverName	<code>String</code>
     * @see	<code>getServerName</code>
     */
    public void setserverName(String serverName) {
        spec.setDetail(DataSourceSpec.SERVERNAME, serverName);
    }
    
    /**
     * Gets the server name.
     *
     * @return	serverName
     * @see	<code>setServerName</code>
     */
    public String getserverName() {
        return spec.getDetail(DataSourceSpec.SERVERNAME);
    }
    
    /**
     * Sets the server name.
     *
     * @param	serverName	<code>String</code>
     * @see	<code>getServerName</code>
     */
    public void setServerName(String serverName) {
        spec.setDetail(DataSourceSpec.SERVERNAME, serverName);
    }
    
    /**
     * Gets the server name.
     *
     * @return	serverName
     * @see	<code>setServerName</code>
     */
    public String getServerName() {
        return spec.getDetail(DataSourceSpec.SERVERNAME);
    }
    
    /**
     * Sets the port number.
     *
     * @param	portNumber	<code>String</code>
     * @see	<code>getPortNumber</code>
     */
    public void setportNumber(String portNumber) {
        spec.setDetail(DataSourceSpec.PORTNUMBER, portNumber);
    }
    
    /**
     * Gets the port number.
     *
     * @return	portNumber
     * @see	<code>setPortNumber</code>
     */
    public String getportNumber() {
        return spec.getDetail(DataSourceSpec.PORTNUMBER);
    }
    
    /**
     * Sets the port number.
     *
     * @param	portNumber	<code>String</code>
     * @see	<code>getPortNumber</code>
     */
    public void setPortNumber(String portNumber) {
        spec.setDetail(DataSourceSpec.PORTNUMBER, portNumber);
    }
    
    /**
     * Gets the port number.
     *
     * @return	portNumber
     * @see	<code>setPortNumber</code>
     */
    public String getPortNumber() {
        return spec.getDetail(DataSourceSpec.PORTNUMBER);
    }
    
    /**
     * Sets the database name.
     *
     * @param	databaseName	<code>String</code>
     * @see	<code>getDatabaseName</code>
     */
    public void setdatabaseName(String databaseName) {
        spec.setDetail(DataSourceSpec.DATABASENAME, databaseName);
    }
    
    /**
     * Gets the database name.
     *
     * @return	databaseName
     * @see	<code>setDatabaseName</code>
     */
    public String getdatabaseName() {
        return spec.getDetail(DataSourceSpec.DATABASENAME);
    }
    
    /**
     * Sets the database name.
     *
     * @param	databaseName	<code>String</code>
     * @see	<code>getDatabaseName</code>
     */
    public void setDatabaseName(String databaseName) {
        spec.setDetail(DataSourceSpec.DATABASENAME, databaseName);
    }
    
    /**
     * Gets the database name.
     *
     * @return	databaseName
     * @see	<code>setDatabaseName</code>
     */
    public String getDatabaseName() {
        return spec.getDetail(DataSourceSpec.DATABASENAME);
    }
    
    /**
     * Sets the data source name.
     *
     * @param	dsn <code>String</code>
     * @see	<code>getDataSourceName</code>
     */
    public void setdataSourceName(String dsn) {
        spec.setDetail(DataSourceSpec.DATASOURCENAME, dsn);
    }
    
    /**
     * Gets the data source name.
     *
     * @return	dsn
     * @see	<code>setDataSourceName</code>
     */
    public String getdataSourceName() {
        return spec.getDetail(DataSourceSpec.DATASOURCENAME);
    }
    
    /**
     * Sets the data source name.
     *
     * @param	dsn <code>String</code>
     * @see	<code>getDataSourceName</code>
     */
    public void setDataSourceName(String dsn) {
        spec.setDetail(DataSourceSpec.DATASOURCENAME, dsn);
    }
    
    /**
     * Gets the data source name.
     *
     * @return	dsn
     * @see	<code>setDataSourceName</code>
     */
    public String getDataSourceName() {
        return spec.getDetail(DataSourceSpec.DATASOURCENAME);
    }
    
    /**
     * Sets the description.
     *
     * @param	desc	<code>String</code>
     * @see	<code>getDescription</code>
     */
    public void setdescription(String desc) {
        spec.setDetail(DataSourceSpec.DESCRIPTION, desc);
    }
    
    /**
     * Gets the description.
     *
     * @return	desc
     * @see	<code>setDescription</code>
     */
    public String getdescription() {
        return spec.getDetail(DataSourceSpec.DESCRIPTION);
    }
    
    /**
     * Sets the description.
     *
     * @param	desc	<code>String</code>
     * @see	<code>getDescription</code>
     */
    public void setDescription(String desc) {
        spec.setDetail(DataSourceSpec.DESCRIPTION, desc);
    }
    
    /**
     * Gets the description.
     *
     * @return	desc
     * @see	<code>setDescription</code>
     */
    public String getDescription() {
        return spec.getDetail(DataSourceSpec.DESCRIPTION);
    }
    
    /**
     * Sets the network protocol.
     *
     * @param	nwProtocol	<code>String</code>
     * @see	<code>getNetworkProtocol</code>
     */
    public void setnetworkProtocol(String nwProtocol) {
        spec.setDetail(DataSourceSpec.NETWORKPROTOCOL, nwProtocol);
    }
    
    /**
     * Gets the network protocol.
     *
     * @return	nwProtocol
     * @see	<code>setNetworkProtocol</code>
     */
    public String getnetworkProtocol() {
        return spec.getDetail(DataSourceSpec.NETWORKPROTOCOL);
    }
    
    /**
     * Sets the network protocol.
     *
     * @param	nwProtocol	<code>String</code>
     * @see	<code>getNetworkProtocol</code>
     */
    public void setNetworkProtocol(String nwProtocol) {
        spec.setDetail(DataSourceSpec.NETWORKPROTOCOL, nwProtocol);
    }
    
    /**
     * Gets the network protocol.
     *
     * @return	nwProtocol
     * @see	<code>setNetworkProtocol</code>
     */
    public String getNetworkProtocol() {
        return spec.getDetail(DataSourceSpec.NETWORKPROTOCOL);
    }
    
    /**
     * Sets the role name.
     *
     * @param	roleName	<code>String</code>
     * @see	<code>getRoleName</code>
     */
    public void setroleName(String roleName) {
        spec.setDetail(DataSourceSpec.ROLENAME, roleName);
    }
    
    /**
     * Gets the role name.
     *
     * @return	roleName
     * @see	<code>setRoleName</code>
     */
    public String getroleName() {
        return spec.getDetail(DataSourceSpec.ROLENAME);
    }
    
    /**
     * Sets the role name.
     *
     * @param	roleName	<code>String</code>
     * @see	<code>getRoleName</code>
     */
    public void setRoleName(String roleName) {
        spec.setDetail(DataSourceSpec.ROLENAME, roleName);
    }
    
    /**
     * Gets the role name.
     *
     * @return	roleName
     * @see	<code>setRoleName</code>
     */
    public String getRoleName() {
        return spec.getDetail(DataSourceSpec.ROLENAME);
    }

    
    /**
     * Sets the login timeout.
     *
     * @param	loginTimeOut	<code>String</code>
     * @see	<code>getLoginTimeOut</code>
     */
    public void setloginTimeOut(String loginTimeOut) {
        spec.setDetail(DataSourceSpec.LOGINTIMEOUT, loginTimeOut);
    }
    
    /**
     * Gets the login timeout.
     *
     * @return	loginTimeout
     * @see	<code>setLoginTimeOut</code>
     */
    public String getloginTimeOut() {
        return spec.getDetail(DataSourceSpec.LOGINTIMEOUT);
    }
    
    /**
     * Sets the login timeout.
     *
     * @param	loginTimeOut	<code>String</code>
     * @see	<code>getLoginTimeOut</code>
     */
    public void setLoginTimeOut(String loginTimeOut) {
        spec.setDetail(DataSourceSpec.LOGINTIMEOUT, loginTimeOut);
    }
    
    /**
     * Gets the login timeout.
     *
     * @return	loginTimeout
     * @see	<code>setLoginTimeOut</code>
     */
    public String getLoginTimeOut() {
        return spec.getDetail(DataSourceSpec.LOGINTIMEOUT);
    }
    
    /**
     * Sets the delimiter.
     *
     * @param	delim	<code>String</code>
     * @see	<code>getDelimiter</code>
     */
    public void setdelimiter(String delim) {
        spec.setDetail(DataSourceSpec.DELIMITER, delim);
    }
    
    /**
     * Gets the delimiter.
     *
     * @return	delim
     * @see	<code>setDelimiter</code>
     */
    public String getdelimiter() {
        return spec.getDetail(DataSourceSpec.DELIMITER);
    }
    
    /**
     * Sets the delimiter.
     *
     * @param	delim	<code>String</code>
     * @see	<code>getDelimiter</code>
     */
    public void setDelimiter(String delim) {
        spec.setDetail(DataSourceSpec.DELIMITER, delim);
    }
    
    /**
     * Gets the delimiter.
     *
     * @return	delim
     * @see	<code>setDelimiter</code>
     */
    public String getDelimiter() {
        return spec.getDetail(DataSourceSpec.DELIMITER);
    }
    
    /**
     * Sets the driver specific properties.
     *
     * @param	driverProps	<code>String</code>
     * @see	<code>getDriverProperties</code>
     */
    public void setdriverProperties(String driverProps) {
        spec.setDetail(DataSourceSpec.DRIVERPROPERTIES, driverProps);
    }
    
    /**
     * Gets the driver specific properties.
     *
     * @return	driverProps
     * @see	<code>setDriverProperties</code>
     */
    public String getdriverProperties() {
        return spec.getDetail(DataSourceSpec.DRIVERPROPERTIES);
    }
    
    /**
     * Sets the driver specific properties.
     *
     * @param	driverProps	<code>String</code>
     * @see	<code>getDriverProperties</code>
     */
    public void setDriverProperties(String driverProps) {
        spec.setDetail(DataSourceSpec.DRIVERPROPERTIES, driverProps);
    }
    
    /**
     * Gets the driver specific properties.
     *
     * @return	driverProps
     * @see	<code>setDriverProperties</code>
     */
    public String getDriverProperties() {
        return spec.getDetail(DataSourceSpec.DRIVERPROPERTIES);
    }

    /*
     * Check if the PasswordCredential passed for this get connection
     * request is equal to the user/passwd of this connection pool.
     */
    private boolean isEqual( PasswordCredential pc, String user, 
        String password) {
        
	//if equal get direct connection else 
	//get connection with user and password.
	
	if (user == null && pc == null) {
	    return true;
	}
	
	if ( user == null && pc != null ) {
            return false;
	}

	if( pc == null ) {
	    return true;
	}
	
	if ( user.equals( pc.getUserName() ) ) {
	    if ( password == null && pc.getPassword() == null ) {
	        return true;
	    }
	}
	
	if ( user.equals(pc.getUserName()) && password.equals(pc.getPassword()) ) {
	    return true;
	} 
	
        
	return false;
    }
}
