/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */

/*
 * ConnectionUtil.java
 *
 * Created on February 14, 2003, 11:32 AM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.*;
import javax.naming.*;
import java.sql.*;
import javax.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import org.apache.catalina.*;

import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.ShutdownCleanupCapable;

import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.Switch;
import com.sun.enterprise.InvocationManager;
//FIXME: move this later to com.sun.appserv.jdbc.DataSource
//import com.sun.appserv.DataSource;
import com.sun.enterprise.resource.ResourceInstaller;

/**
 *
 * @author  lwhite
 */
public class ConnectionUtil {
    
    /**
     * The cached DataSource
     */
    private static DataSource _dataSource = null;     
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;     
    
    /**
    * The helper class used to manage retryable errors from the HA store
    */
    protected HAErrorManager haErr = null; 
    
    /**
    * The name of the HA store JDBC driver 
    */
    protected String driverName = "com.sun.hadb.jdbc.Driver";
    
    /**
    * The number of seconds to wait before timing out a transaction
    * Default is 5 minutes.
    */
    protected String timeoutSecs = new Long(5 * 60).toString();
    //protected String timeoutSecs = new Long(2).toString();        
    
    //protected Container container = null;
    protected Object container = null;
    //protected Manager manager = null;
    protected Object manager = null;
    protected Valve valve = null;
    
    /**
     * The database connection.
     */
    protected Connection conn = null;    
    
    /**
     * Name to register for the background thread.
     */
    protected String threadName = "ConnectionUtil";
    
    /** Creates a new instance of ConnectionUtil */
    public ConnectionUtil(Object cont) {
        //manager = mgr;
        container = cont;
        threadName = "ConnectionUtil";
        long timeout = new Long(timeoutSecs).longValue();
        haErr = new HAErrorManager(timeout, threadName); 
        
        if (_logger == null) {
            //FIXME use LogDomains.WEB_EE_LOGGER later
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }          
    }    
    
    /** Creates a new instance of ConnectionUtil */
    public ConnectionUtil(Container cont) {
        //manager = mgr;
        container = cont;
        threadName = "ConnectionUtil";
        long timeout = new Long(timeoutSecs).longValue();
        haErr = new HAErrorManager(timeout, threadName); 
        
        if (_logger == null) {
            //FIXME use LogDomains.WEB_EE_LOGGER later
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }          
    }
    
    /** Creates a new instance of ConnectionUtil */
    public ConnectionUtil(Container cont, Manager mgr) {
        this(cont);
        manager = mgr;        
    }    
    
    /** Creates a new instance of ConnectionUtil */
    public ConnectionUtil(Container cont, Valve aValve) {
        this(cont);
        valve = aValve;        
    }       
            
    /**
    * User for the connection
    */ 
    protected String user = null;
    protected String getConnUser() {
        if (user == null) {        
            ServerConfigLookup lookup = new ServerConfigLookup();
            user = lookup.getConnectionUserFromConfig();
        }
        return user;
    }
     
    /**
    * Password for the connection
    */
    protected String password = null;     
    protected String getConnPassword() {
        if (password == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            password = lookup.getConnectionPasswordFromConfig();
        }
        return password;
    }
    
    protected boolean configErrorFlag = false;
    
    protected boolean hasConfigErrorBeenReported() {
        return configErrorFlag;
    }
    
    protected void setConfigErrorFlag(boolean value) {
        configErrorFlag = value;
    }
    
    /**
    * connection url string for the connection
    */ 
    protected String connString = null;
    protected String getConnString() {
        if (connString == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            connString = lookup.getConnectionURLFromConfig();
        }
        return connString;
    }

    /**
    * connection data source string for the connection
    */ 
    protected String dataSourceString = null;    
    protected String getDataSourceNameFromConfig() {
        if (dataSourceString == null) {        
            ServerConfigLookup configLookup = new ServerConfigLookup();
            dataSourceString = configLookup.getHaStorePoolJndiNameFromConfig();
        }
        return dataSourceString;
    }
    
    /**
    * connection data source for the connection
    */ 
    protected DataSource dataSource = null;  

    public DataSource privateGetDataSource() throws javax.naming.NamingException {
        return this.getDataSource();
    }
    
    /**
    * connection data source for the connection
    */
    protected DataSource getDataSource() throws javax.naming.NamingException {
        if(dataSource != null) {
            return dataSource;
        }

        if(_dataSource != null) {
            dataSource = _dataSource;
            return _dataSource;
        }        
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        //System.out.println("ConnectionUtil>>getDataSource: originalClassLoader=" + originalClassLoader);
        //_logger.finest("Getting initial context...");
	java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public java.lang.Object run() {
                    Thread.currentThread().setContextClassLoader(ConnectionUtil.class.getClassLoader());
                    return null;
                }
            }
        );
        
        InitialContext ctx = null;
        try {
            ctx = new InitialContext();
            
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("- Got initial context for pool successfully");
                _logger.finest("Getting datasource...");
            }
            String dsName = this.getDataSourceNameFromConfig();
            //DataSource ds = (javax.sql.DataSource)ctx.lookup(dsName);
            String systemDataSourceName = ResourceInstaller.getPMJndiName(dsName);
            DataSource ds = (javax.sql.DataSource)ctx.lookup(systemDataSourceName);
            //DataSource ds = (javax.sql.DataSource)ctx.lookup(dsName + "__pm");
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("- Got datasource for pool successfully");
            }
            dataSource = ds;
            setDataSource(ds);
            return ds;            
            
        } catch (Exception e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("ERROR CREATING INITCTX+++++++++");
            }
            e.printStackTrace();
            throw new javax.naming.NamingException(e.getMessage());
        } finally {
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public java.lang.Object run() {
                        Thread.currentThread().setContextClassLoader(originalClassLoader);
                        return null;
                    }
                }
            );            
        }            

    }     
    
    private static synchronized void setDataSource(DataSource ds) {
        _dataSource = ds;
    }
    
    /**
    * return a connection from the pool
    * can return null if non-retryable exception
    * is thrown during effort to get a connection
    * else will keep retrying until a connection is obtained
    */     
    protected Connection getConnectionFromPool() throws IOException {
        Connection conn = null;
        InvocationManager invmgr = null;
        ComponentInvocation ci = null;
        try {
            /*(
            invmgr = Switch.getSwitch().getInvocationManager();
            ci = new ComponentInvocation(this, container);
            invmgr.preInvoke(ci);
             */
            
            DataSource ds = this.getDataSource();
            conn = this.getConnectionRetry(ds);            
            //_logger.finest("Getting connection...");
            //conn = ds.getConnection();
           // _logger.finest("GOT CONNECTION: class= " + conn.getClass().getName());
            //conn.setAutoCommit(false);
            //_logger.finest("- Got connection from pool successfully");
        } catch (Exception ex) {
            //ex.printStackTrace();
            //throw new IOException("Unable to obtain connection from pool");
            IOException ex1 = 
                (IOException) new IOException("Unable to obtain connection from pool").initCause(ex);
            throw ex1;
        } finally { 
            /*
             invmgr.postInvoke(ci);
             */
        }
        
        return conn;      
    }
    
    /**
    * return a connection from the pool
    * can return null if non-retryable exception
    * is thrown during effort to get a connection
    * else will keep retrying until a connection is obtained
    */     
    protected Connection getConnectionFromPool(boolean autoCommit) throws IOException {
        Connection conn = null;
        InvocationManager invmgr = null;
        ComponentInvocation ci = null;
        try {
            /*
            invmgr = Switch.getSwitch().getInvocationManager();
            ci = new ComponentInvocation(this, container);
            invmgr.preInvoke(ci);
             */
            
            DataSource ds = this.getDataSource();
            conn = this.getConnectionRetry(ds, autoCommit);            
            //_logger.finest("Getting connection...");
            //conn = ds.getConnection();
           // _logger.finest("GOT CONNECTION: class= " + conn.getClass().getName());
            //conn.setAutoCommit(false);
            //_logger.finest("- Got connection from pool successfully");
        } catch (Exception ex) {
            //ex.printStackTrace();
            //throw new IOException("Unable to obtain connection from pool");
            IOException ex1 = 
                (IOException) new IOException("Unable to obtain connection from pool").initCause(ex);
            throw ex1;
        } finally { 
            /*
             invmgr.postInvoke(ci);
             */
        }
        
        return conn;      
    } 
    
    /**
    * this method is temporary until HADB fixes bug relating
    * to the length of the thread name - see callers of this
    * method in this class
    * this method calls getConnection on the data source after
    * insuring the thread name is short enough and then restores
    * the original thread name after the call
    */    
    private Connection doGetConnection(DataSource ds) throws SQLException {
 //System.out.println("IN NEW doGetConnection");
        Connection resultConn = null;
        String threadName = Thread.currentThread().getName();
        String shortString = this.truncateString(threadName, 63);
        Thread.currentThread().setName(shortString);
        //start 6468099
        final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        if(originalClassLoader == null) {
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public java.lang.Object run() {
                        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
                        return null;
                    }
                }
            );
        }
        //end 6468099        
        com.sun.appserv.jdbc.DataSource castDS 
            = (com.sun.appserv.jdbc.DataSource) ds;
        try {            
            //begin 6374243
            //resultConn = castDS.getNonTxConnection();
            //bail out and return null if thread is interrupted
            if(Thread.currentThread().isInterrupted()) {
                resultConn = null;
            } else {
                resultConn = castDS.getNonTxConnection();
                //resultConn = ds.getConnection();
            }
            //end 6374243            
        } finally {
            Thread.currentThread().setName(threadName);
            //start 6468099
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public java.lang.Object run() {
                        Thread.currentThread().setContextClassLoader(originalClassLoader);
                        return null;
                    }
                }
            );            
            //end 6468099            
        }
        return resultConn;
    }

    /**
    * this method is temporary until HADB fixes bug relating
    * to the length of the thread name - see callers of this
    * method in this class
    * this method truncates the inputStr to the newLength
    * after doing some safety checks on the newLength
    */    
    private String truncateString(String inputStr, int newLength) {
        int strLength = inputStr.length();
        String result = inputStr;
        if(newLength < strLength && newLength > 0) {
            result = inputStr.substring((strLength - newLength), strLength);
        }
        return result;
    }
        
    private Connection getConnectionRetry(DataSource ds) throws IOException {
        Connection resultConn = null;
      
        try {
            haErr.txStart();
            while ( ! haErr.isTxCompleted() ) {          
                try {
                    //_logger.finest("Getting connection...");
                    //FIXME: this call is a work-around for HADB bug
                    //when it is fixed can go back to following line
                    resultConn = this.doGetConnection(ds);
                    //resultConn = ds.getConnection();
                   // _logger.finest("GOT CONNECTION: class= " + conn.getClass().getName());
                    resultConn.setAutoCommit(false);                   
                    //_logger.finest("- Got connection from pool successfully"); 
                    haErr.txEnd();
                }
                catch ( SQLException e ) {
                    //haErr.checkError(e, null);
                    haErr.checkError(e, resultConn);                    
                    if (resultConn != null) {
                        //put resultConn back in pool
                        try {
                            resultConn.close();
                        } catch (Exception ex) {}
                    }
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Got a retryable exception from ConnectionUtil: " + e.getMessage());
                    }
                }                
            }            
        }
        catch(SQLException e) {        
            IOException ex1 = 
                (IOException) new IOException("Error from ConnectionUtil: " +
                    e.getMessage()).initCause(e);
            throw ex1;          
        }
        catch ( HATimeoutException e ) {
            IOException ex1 = 
                (IOException) new IOException("Timeout from ConnectionUtil").initCause(e);
            throw ex1;           
        }            
        if(resultConn == null) {
            _logger.warning("ConnectionUtil>>getConnectionRetry failed: returning null");
        }
        return resultConn;
    }
    
    private Connection getConnectionRetry(DataSource ds, boolean autoCommit) throws IOException {
        Connection resultConn = null;
      
        try {
            haErr.txStart();
            while ( ! haErr.isTxCompleted() ) {          
                try {
                    //_logger.finest("Getting connection...");
                    //FIXME: this call is a work-around for HADB bug
                    //when it is fixed can go back to following line
                    resultConn = this.doGetConnection(ds);
                    //resultConn = ds.getConnection();
                   // _logger.finest("GOT CONNECTION: class= " + conn.getClass().getName());
                    resultConn.setAutoCommit(autoCommit);
                    /* above line replaces following 3 lines
                    if(!autoCommit) {
                        resultConn.setAutoCommit(false);
                    }
                     */
                    //_logger.finest("- Got connection from pool successfully"); 
                    haErr.txEnd();
                }
                catch ( SQLException e ) {
                    //haErr.checkError(e, null);
                    haErr.checkError(e, resultConn);
                    if (resultConn != null) {
                        //put resultConn back in pool
                        try {
                            resultConn.close();
                        } catch (Exception ex) {}
                    }
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Got a retryable exception from ConnectionUtil: " + e.getMessage());
                    }
                }                
            }            
        }
        catch(SQLException e) {        
            IOException ex1 = 
                (IOException) new IOException("Error from ConnectionUtil: " +
                    e.getMessage()).initCause(e);
            throw ex1;          
        }
        catch ( HATimeoutException e ) {
            IOException ex1 = 
                (IOException) new IOException("Timeout from ConnectionUtil").initCause(e);
            throw ex1;           
        }            
        if(resultConn == null) {
            _logger.warning("ConnectionUtil>>getConnectionRetry failed: returning null");
        }
        return resultConn;
    }    
    
    /**
    * return a HADBConnectionGroup from the pool
    */    
    public HADBConnectionGroup getConnectionsFromPool() throws IOException {
        
        Connection conn = this.getConnectionFromPool();
        //if conn is null this is a failure to get a connection
        //even after repeated retries
        if(conn == null) {
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed: returning null");
            return null;
        }

        Connection internalConn = this.getInternalConnection(conn);
        HADBConnectionGroup connections = 
            new HADBConnectionGroup(internalConn, conn);
        //FIXME wrong to manage pool connections this way - remove after testing
        //System.out.println("ConnectionUtil:about to putConnection");
        //this.putConnection(internalConn);
        return connections;              
    } 
    
    /**
    * return a HADBConnectionGroup from the pool
    */    
    public HADBConnectionGroup getConnectionsFromPool(boolean autoCommit) throws IOException {
        
        Connection conn = this.getConnectionFromPool(autoCommit);
        //if conn is null this is a failure to get a connection
        //even after repeated retries
        if(conn == null) {
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed: returning null");
            return null;
        }

        Connection internalConn = this.getInternalConnection(conn);
        HADBConnectionGroup connections = 
            new HADBConnectionGroup(internalConn, conn);
        //FIXME wrong to manage pool connections this way - remove after testing
        //System.out.println("ConnectionUtil:about to putConnection");
        //this.putConnection(internalConn);
        return connections;              
    }     
 
    /**
    * return the internal connection from the JDBC external wrapper
    */      
    private Connection getInternalConnection(Connection connection) throws IOException {
        
        Connection internalConn = null;
        //FIXME: this will change to com.sun.appserv.jdbc.DataSource
        com.sun.appserv.jdbc.DataSource ds = null;
        try {
            ds = (com.sun.appserv.jdbc.DataSource) (this.getDataSource());
        } catch (Exception ex) {}
        
        try {            
            internalConn = ds.getConnection(connection);
        } catch (Exception ex) {
            //just warn and continue processing
            //_logger.log(Level.SEVERE, "webcontainer.hadbConnectionPoolNotReached");
            //ex.printStackTrace();
            //throw new IOException("Unable to obtain connection from pool");
            IOException ex1 = 
                (IOException) new IOException("Unable to obtain connection from pool").initCause(ex);
            throw ex1;                        
        }
        return internalConn;
    }         
    
    /**
     * Check the connection associated with this store, if it's
     * <code>null</code> or closed try to reopen it.
     * Returns <code>null</code> if the connection could not be established.
     *
     * @return <code>Connection</code> if the connection succeeded
     */
    public Connection getConnection() throws IOException {
        return getConnection(true); 
    } 
    
  /**
   * Check the connection associated with this store, if it's
   * <code>null</code> or closed try to reopen it.
   * set autoCommit to autoCommit
   * Returns <code>null</code> if the connection could not be established.
   *
   * @return <code>Connection</code> if the connection succeeded
   */
    public Connection getConnectionNew(boolean autoCommit) throws IOException {
        //FIXME: this method is wrong
        //for now is getting from the pool
        Connection internalConn = null;
        try {
            Connection externalConn = this.getConnectionFromPool();
            internalConn = this.getInternalConnection(externalConn);
            if(internalConn != null) {
                internalConn.setAutoCommit(autoCommit);
                this.putConnection(internalConn);
            }        
        } catch ( SQLException e ) {
            //e.printStackTrace();
            _logger.log(Level.SEVERE, "connectionutil.unableToOpenConnection", e.getMessage());
            _logger.log(Level.SEVERE, "connectionutil.failedToPersist");
            IOException ex1 = 
                (IOException) new IOException("Unable to open connection to HA Store: " + e.getMessage()).initCause(e);
            throw ex1;
        }
        
        return internalConn;
    }
    
  /**
   * Check the connection associated with this store, if it's
   * <code>null</code> or closed try to reopen it.
   * set autoCommit to autoCommit
   * Returns <code>null</code> if the connection could not be established.
   *
   * @return <code>Connection</code> if the connection succeeded
   */
    public Connection getConnection(boolean autoCommit) throws IOException {
        
        if(this.hasConfigErrorBeenReported()) {
            return null;
        }

        haErr.txStart();
        while ( ! haErr.isTxCompleted() ) {         
            try {
                if ( conn != null && (! conn.isClosed()) ) {
                    if(conn.getAutoCommit()) {
                        conn.setAutoCommit(autoCommit);
                    }
                    //return conn;
                }
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("VALUE-OF-CONN-STRING= " + getConnString());
                    _logger.finest("cached conn= " + conn);
                }
            } catch (Exception e1) {
                conn = null;
            }
            if(conn != null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("getConnection-near begin return conn from cache");
                }
                haErr.txEnd();
                break;
            }
            
            try {            
                try {
                    Class.forName(driverName);
                }
                catch ( ClassNotFoundException ex ) {              
                    IOException ex1 = 
                      (IOException) new IOException("Unable to find JDBC driver class " + driverName + ": " +
                      ex.getMessage()).initCause(ex);
                    throw ex1;
                }
            
                try {
                    Properties props = new Properties();
                    String theUser = this.getConnUser();
                    String thePassword = this.getConnPassword();
                    if(theUser == null || thePassword == null || getConnString() == null) {
                        _logger.log(Level.WARNING,
                            "connectionutil.configError");                        
                        this.setConfigErrorFlag(true);
                    } else {
                        this.setConfigErrorFlag(false);
                        //use these lines to get log of JDBC driver activity
                        //props.setProperty("loglevel", "FINEST");
                        //props.setProperty("logfile", "/tmp/mylogfile");
                        props.setProperty("user", user);
                        props.setProperty("password", password);

                        //FIXME: these lines relating to the thread name
                        // are a work-around until HADB bug is fixed
                        String threadName = Thread.currentThread().getName();
                        String shortString = this.truncateString(threadName, 63);
                        Thread.currentThread().setName(shortString);                  
                        conn = DriverManager.getConnection(getConnString(), props);
                        Thread.currentThread().setName(threadName);
                        conn.setAutoCommit(autoCommit);
                        //Bug 4836431
                        conn.setTransactionIsolation(java.sql.Connection.TRANSACTION_REPEATABLE_READ);
                    }
                    haErr.txEnd();
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("getConnection at middle - return created conn: " + conn);
                    }
                   //debug("Connected to " + connString);
                }
                catch ( SQLException ex ) {
                    haErr.checkError(ex, conn);
                    //debug("Got a retryable exception from HA Store: " + ex.getMessage())
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Got a retryable exception from HA Store: " + ex.getMessage());
                    }
                    System.out.println("Got a retryable exception from HA Store: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } catch ( SQLException e ) {
                //e.printStackTrace();
                _logger.log(Level.SEVERE, "connectionutil.unableToOpenConnection", e.getMessage());
                _logger.log(Level.SEVERE, "connectionutil.failedToPersist");
                //throw new IOException("Unable to open connection to HA Store: " + e.getMessage());
                IOException ex1 = 
                    (IOException) new IOException("Unable to open connection to HA Store: " + e.getMessage()).initCause(e);
                throw ex1;         
            } catch ( HATimeoutException e )  {
                //throw new IOException("Timed out attempting to open connection to HA Store");
                IOException ex2 = 
                    (IOException) new IOException("Timed out attempting to open connection to HA Store").initCause(e);
                throw ex2;                 
            }            
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("getConnection at end - return conn: " + conn);
        }
        if(conn != null)
            this.putConnection(conn);
        return conn;
    }    
    
    protected void putConnection(Connection conn) {
        if(manager instanceof ShutdownCleanupCapable) {
            ((ShutdownCleanupCapable)manager).putConnection(conn);
        } 
        //for SSO case
        if(valve instanceof ShutdownCleanupCapable) {
            ((ShutdownCleanupCapable)valve).putConnection(conn);
        }
    }
    
    public void clearCachedConnection() {
        conn = null;
    }           
    
}
