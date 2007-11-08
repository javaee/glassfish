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

package com.sun.ejb.ee.sfsb.store;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.enterprise.ee.web.sessmgmt.ConnectionUtil;
import com.sun.enterprise.ee.web.sessmgmt.HADBConnectionGroup;
import com.sun.enterprise.ee.web.sessmgmt.HAErrorManager;
import com.sun.enterprise.ee.web.sessmgmt.HATimeoutException;
import com.sun.ejb.Container;
import com.sun.ejb.spi.sfsb.store.SFSBBeanState;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.hadb.jdbc.LOBDescr;
import com.sun.hadb.jdbc.LobConnection;
import com.sun.logging.LogDomains;
import java.util.ArrayList;

/** HAEjbStore.java
 * This Store directly interacts with the HADB to load/save/remove the beans.
 */
public class HAEjbStore extends EJBStoreBase {
    /** Logger for logging
     */
    private static final Logger _logger 
        = LogDomains.getLogger(LogDomains.EJB_LOGGER);
    
    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "HAEjbStore";    
    
    /**
     * Name to register for this Store, used for logging.
     */    
    private static final String storeName = "HAEjbStore";    

    static {
        //_logger = LogDomains.getLogger(LogDomains.EJB_LOGGER);
        //info = "HAEjbStore/1.0";
        //storeName = "HAEjbStore";        
    }

    /** Table name for failing over the sfsb state
     */
    public static final String blobSfsbTable = "statefulsessionbean";
    
    /** chunk size for delete operations
     */
    private static int CHUNK_SIZE = 100;    
    
    /** Prepared statement for insertsql (no commit)
     */
    private PreparedStatement preparedInsertSqlNoCommit = null;    
    
    /** Prepared statement for updatetsql (no commit)
     */
    private PreparedStatement preparedUpdateSqlNoCommit = null;    
    
    /** Prepared statement for expiredkeyssql
     */
    private PreparedStatement preparedExpiredKeysSql = null;
    
    /** Manager to which the store is assigned to
     */
    protected SFSBStoreManager manager;
    
    /** Container where this store belongs to
     */
    private Container container = null;
    
    /** clusterid of the instance
     */
    private String clusterID = null;
    
    /** containerid of this container
     */
    private String containerID = null;
    
    /**
     * The helper class used to obtain connections; both
     * both cached and from the connection pool
     */
    //protected ConnectionUtil connectionUtil = null;
    private ConnectionUtil connectionUtilEjb = null;

    /**
     * The database connection.
     */
    protected Connection conn = null;
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     */
    protected HAErrorManager haErr = null;
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for aggegate save
     */
    protected HAErrorManager haErrAggregateSave = null;    
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for load
     */
    protected HAErrorManager haErrLoad = null; 
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for remove
     */
    protected HAErrorManager haErrRemove = null;
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for remove
     */
    protected HAErrorManager haErrRemoveExpired = null;    
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for expiredKeys
     */
    protected HAErrorManager haErrExpiredKeys = null;
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for getBeanIds
     */
    protected HAErrorManager haErrGetBeanIds = null; 
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for removeAllBeansForContainer
     */
    protected HAErrorManager haErrRemoveAllBeans = null;    
    
    /**
     * The helper class used to manage retryable errors from the HAEjb store
     * for update last access time
     */
    protected HAErrorManager haErrUpdateAccessTime = null;    
    
    /**
     * Controls the verbosity of the web container subsystem's debug messages.
     *
     * This value is non-zero only when the level is one of FINE, FINER
     * or FINEST.
     *
     */
    protected int _debug = 1;

    /** LobDescr for insert
     */
    private LOBDescr insertLob = new LOBDescr();
    
    /** LobDescr for load
     */
    private LOBDescr loadLob = new LOBDescr();
    
    /** LobDescr for update
     */
    private LOBDescr updateLob = new LOBDescr();

    /** Initializing the store and LobDescr.
     */
    public HAEjbStore() {
        //info = "HAEjbStore/1.0";
        threadName = "HAEjbStore";
        //storeName = "HAEjbStore";


        //initialize the three lob descriptors that we use
        insertLob.setTableName(blobSfsbTable);
        insertLob.addKey("id", 1); // this is the primary key column
        insertLob.setLOBColumn("beandata", 4); //blob column name

        //initialize the three lob descriptors that we use
        loadLob.setTableName(blobSfsbTable);
        loadLob.addKey("id", 1); // this is the primary key column
        loadLob.setLOBColumn("beandata", 2); //blob column name

        updateLob.setTableName(blobSfsbTable);
        updateLob.addKey("id", 5); // prinmary key column name
        updateLob.setLOBColumn("beandata", 3); //BLOB column name
        long timeout = new Long(timeoutSecs).longValue();
        haErr = new HAErrorManager(timeout, threadName);
        haErrAggregateSave = new HAErrorManager(timeout, threadName);
        haErrLoad = new HAErrorManager(timeout, threadName);        
        haErrRemove = new HAErrorManager(timeout, threadName);
        haErrRemoveAllBeans = new HAErrorManager(timeout, threadName);
        haErrRemoveExpired = new HAErrorManager(timeout, threadName);
        haErrExpiredKeys = new HAErrorManager(timeout, threadName);
        haErrGetBeanIds = new HAErrorManager(timeout, threadName);
        haErrUpdateAccessTime = new HAErrorManager(timeout, threadName);

    }

    /** 
     * return if monitoring is enabled
     * @return if monitoring is enabled
     */    
    protected boolean isMonitoringEnabled() {                   
        BaseSFSBStoreManager mgr = (BaseSFSBStoreManager)getSFSBStoreManager();
        return mgr.isMonitoringEnabled();
    }
    
    /** 
     * return EJBModuleStatistics
     * @return EJBModuleStatistics
     */    
    protected EJBModuleStatistics getEJBModuleStatistics() {                   
        BaseSFSBStoreManager mgr = (BaseSFSBStoreManager)getSFSBStoreManager();
        return mgr.getEJBModuleStatistics();
    }    

    /** Return the instance of ConnectionUtil for this Store.
     * @return Returns the ConnectionUtil which handles the 
     * connection management with hadb
     */
    protected ConnectionUtil getConnectionUtil() {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "getConnectionUtil");
        }
        if (connectionUtilEjb == null) {
            connectionUtilEjb = new SFSBStoreConnectionUtil(container, manager);
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "getConnectionUtil", connectionUtilEjb);
        }
        return connectionUtilEjb;
    }

    /** return a HADBConnectionGroup from the pool
     * @throws IOException
     * @return return a HADBConnectionGroup
     */
    protected HADBConnectionGroup getConnectionsFromPool() throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        return util.getConnectionsFromPool();
    }

    /**
     * Execute a query that returns results.  This takes care of all the retry
     * logic needed to work against the Clustra HAEjb store
     *
     * @param stmt
     *   The statement you want to execute
     *
     * @param isQuery
     *   Set this to true if this is a query which returns a result set.
     *   Set this to false if tihs is a statement which has no results.
     *
     * @return
     *   The result set returned from executing this query
     *
     * @exception
     *    IOException if there was a problem executing the query
     */
    protected ResultSet executeStatement(PreparedStatement stmt,
                                         boolean isQuery) throws
        IOException {
        ResultSet rst = null;

        try {
            haErr.txStart();
            while (!haErr.isTxCompleted()) {
                try {
                    if (isQuery) {
                        rst = stmt.executeQuery();
                    }
                    else {
                        stmt.executeUpdate();
                    }
                    haErr.txEnd();
                }
                catch (SQLException e) {
                    haErr.checkError(e, conn);
                    if (_debug > 0) {
                        debug("Got a retryable exception from HAEjb store: " +
                              e.getMessage());
                    }
                }
            }
        }
        catch (SQLException e) {
            IOException ex1 =
                (IOException)new IOException("Error from HAEjbStore: " +
                                             e.getMessage()).
                initCause(e);
            throw ex1;
        }
        catch (HATimeoutException e) {
            IOException ex1 =
                (IOException)new IOException("Timeout from HAEjb store: " + e.getMessage()).initCause(
                e);
            throw ex1;
        }

        return rst;
    }

    /** prints the debug messages
     * @param message
     */
    protected void debug(String message) {
        System.out.println(message);
    }

    /** Returns the Container
     * @return
     */
    public Container getContainer() {
        return container;
    }

    /** Returns the containerID
     * @return
     */
    protected String getContainerId() {
        return this.containerID;
    }

    /** Sets the containerId for this container
     * @param containerId  */
    protected void setContainerId(String containerId) {
        this.containerID = containerId;
    }

    /** Sets the container
     * @param container  */
    public void setContainer(Container container) {
        this.container = container;
    }

    /** Saves the beanstate to the DB
     * @param sfsBean
     * @param mgr
     * @param isNew
     * @throws IOException
     */
    public void save(SFSBBeanState sfsBean, SFSBStoreManager mgr, boolean isNew) throws
        IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "save", new Object[] {sfsBean, mgr});
        }
        this.manager = mgr;
        save(sfsBean, isNew);
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "save");
        }
    }


    /** Saves the beanstate to the DB
     * @param sfsBean
     * @param isNew
     * @throws IOException  */
    public void save(SFSBBeanState sfsBean, boolean isNew) throws IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "save", sfsBean);
        }
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        EJBModuleStatistics stats = this.getEJBModuleStatistics();         
        try {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore.  save  " + sfsBean + "  bean isNew=" +
                            isNew);
            }
            
            //added for monitoring
            long getConnStartTime = 0L;
            if(this.isMonitoringEnabled()) {
                getConnStartTime = System.currentTimeMillis();
            }
            //end added for monitoring            
            
            //connGroup = this.getConnectionsFromPool();
            ConnectionUtil util = this.getConnectionUtil();
            //now using autocommit=true
            connGroup = util.getConnectionsFromPool(true);            
            
            //added for monitoring      
            if(this.isMonitoringEnabled()) {
                long getConnEndTime = System.currentTimeMillis();
                stats.processGetConnectionFromPool(getConnEndTime - getConnStartTime);
            }
            //end added for monitoring            

            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore:  connGroup is null");
                }
                return;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore.  save  : is .isnew =" + isNew);
            }

            if (isNew) {
                insertSFSBean(sfsBean, internalConn);
            }
            else {
                updateSFSBean(sfsBean, internalConn);
            }
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;
        }
        catch (IOException ex) {
            //this means failure to obtain connection
            ex.printStackTrace();
            throw ex;
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "save");
        }
    }    
    
    /** aggregate save method
     * @param beanStates
     * @throws IOException
     */
    public void save(SFSBBeanState[] beanStates, long startTime) throws IOException {
        long getConnDuration = 0L;
        long getConnStartTime = 0L;
        /*
        _logger.entering("HASFSBStoreManager", "checkpointSave",
        new Object[] {beanStates, new Boolean(transactionFlag)});
        _logger.log(Level.INFO, "HASFSBStoreManager.checkpointSave",
        new Object[] {beanStates, new Boolean(transactionFlag)});
        _logger.exiting("HASFSBStoreManager", "checkpointSave",
        new Object[] {beanStates, new Boolean(transactionFlag)});
         */
        if(this.isMonitoringEnabled()) {
            getConnStartTime = System.currentTimeMillis();
        }
        HADBConnectionGroup connGroup = this.getConnectionGroup();
        //if we cannot get a connection then quit
        if (connGroup == null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "HAEjbStore:  connGroup is null");
            }
            return;
        }
        
        if(this.isMonitoringEnabled()) {
            getConnDuration = System.currentTimeMillis() - getConnStartTime;
            if(beanStates.length != 0) {
                getConnDuration = getConnDuration / beanStates.length;
            } else {
                getConnDuration = 0;
            }
        }        
        save(beanStates, connGroup, startTime, getConnDuration);        
    }
    
    /** aggregate save method
     * @param beanStates
     * @param connGroup
     * @throws IOException
     */
    public void save(SFSBBeanState[] beanStates,
        HADBConnectionGroup connGroup, long startTime, long getConnDuration) throws IOException {
         
        Connection internalConn = null;
        Connection externalConn = null;
        long eachStartTime = startTime;
        long eachEndTime = 0L;

        try {
            haErrAggregateSave.txStart();

            while ( ! haErrAggregateSave.isTxCompleted() ) {         
        
                try {
                    internalConn = connGroup._internalConn;
                    externalConn = connGroup._externalConn;

                    for(int i=0; i<beanStates.length; i++) {
                        SFSBBeanState sfsBean = beanStates[i];
                        boolean isNew = sfsBean.isNew();
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.log(Level.FINEST,
                                        "HAEjbStore.  aggregate save  : is .isnew =" + isNew);
                        }
                        if (isNew) {
                            insertSFSBeanNoCommit(sfsBean, internalConn);
                        }
                        else {
                            updateSFSBeanNoCommit(sfsBean, internalConn);
                        }
                        if(this.isMonitoringEnabled()) {
                            //increment storage duration for each bean
                            eachEndTime = System.currentTimeMillis();
                            long storeDuration = eachEndTime - eachStartTime;
                            sfsBean.setTxCheckpointDuration(sfsBean.getTxCheckpointDuration() + storeDuration + getConnDuration);
                            eachStartTime = eachEndTime;
                        }
                    }
                    ( (Connection) internalConn).commit();

                    if(preparedInsertSqlNoCommit != null) {
                        preparedInsertSqlNoCommit.close();
                        preparedInsertSqlNoCommit = null;
                    }
                    if(preparedUpdateSqlNoCommit != null) {
                        preparedUpdateSqlNoCommit.close();
                        preparedUpdateSqlNoCommit = null;
                    }                                                         
                    externalConn.close();
                    externalConn = null;
                    haErrAggregateSave.txEnd();
                } catch (SQLException e) {
                    closePreparedStatement(preparedInsertSqlNoCommit);
                    preparedInsertSqlNoCommit = null;
                    closePreparedStatement(preparedUpdateSqlNoCommit);
                    preparedUpdateSqlNoCommit = null;                                        
                    //check for re-tryables
                    haErrAggregateSave.checkError(e, ((Connection) internalConn) );
                }
            }
        }                
        catch (java.sql.SQLException ex) {
            //handle fatal SQLException
            ex.printStackTrace();
            try {
                ( (Connection) internalConn).rollback();
                /*
                preparedInsertSqlNoCommit.close();
                preparedUpdateSqlNoCommit.close();
                preparedInsertSqlNoCommit = null;                
                preparedUpdateSqlNoCommit = null;
                 */                 
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "SQL Error from HAEjbStore-aggregate save: " + ex.getMessage()).
                              initCause(ex);
            throw ex1;            
        }        
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-aggregate save: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if(preparedInsertSqlNoCommit != null) {
                try {
                    preparedInsertSqlNoCommit.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                preparedInsertSqlNoCommit = null;                
            }
            if(preparedUpdateSqlNoCommit != null) {
                try {
                    preparedUpdateSqlNoCommit.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                preparedUpdateSqlNoCommit = null;                
            }            
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "save"); 
        }

    }    
    
    /** aggregate save method
     * @param beanStates
     * @param connGroup
     * @throws IOException
     */
    /* previous version of save - ok to remove after testing
    public void savePrevious(SFSBBeanState[] beanStates,
        HADBConnectionGroup connGroup, long startTime, long getConnDuration) throws IOException {
         
        Connection internalConn = null;
        Connection externalConn = null;
        long eachStartTime = startTime;
        long eachEndTime = 0L;
        try {
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;
                        
            for(int i=0; i<beanStates.length; i++) {
                SFSBBeanState sfsBean = beanStates[i];
                boolean isNew = sfsBean.isNew();
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST,
                                "HAEjbStore.  aggregate save  : is .isnew =" + isNew);
                }
                if (isNew) {
                    insertSFSBeanNoCommit(sfsBean, internalConn);
                }
                else {
                    updateSFSBeanNoCommit(sfsBean, internalConn);
                }
                if(this.isMonitoringEnabled()) {
                    //increment storage duration for each bean
                    eachEndTime = System.currentTimeMillis();
                    long storeDuration = eachEndTime - eachStartTime;
                    sfsBean.setTxCheckpointDuration(sfsBean.getTxCheckpointDuration() + storeDuration + getConnDuration);
                    eachStartTime = eachEndTime;
                }
            }
            ( (Connection) internalConn).commit();
            if(preparedInsertSqlNoCommit != null) {
                preparedInsertSqlNoCommit.close();
                preparedInsertSqlNoCommit = null;
            }
            if(preparedUpdateSqlNoCommit != null) {
                preparedUpdateSqlNoCommit.close();
                preparedUpdateSqlNoCommit = null;
            }                                                         
            externalConn.close();
            externalConn = null;
        }
        catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            try {
                ( (Connection) internalConn).rollback();
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "SQL Error from HAEjbStore-aggregate save: " + ex.getMessage()).
                              initCause(ex);
            throw ex1;            
        }        
        catch (IOException ex) {
            //this means failure to obtain connection
            ex.printStackTrace();
            throw ex;
        }
        finally {
            if(preparedInsertSqlNoCommit != null) {
                try {
                    preparedInsertSqlNoCommit.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                preparedInsertSqlNoCommit = null;                
            }
            if(preparedUpdateSqlNoCommit != null) {
                try {
                    preparedUpdateSqlNoCommit.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                preparedUpdateSqlNoCommit = null;                
            }            
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "save"); 
        }

    } 
     */ //end previous version of save      
    
    /** Gets an HADBConnectionGroup or null if it does not succeed
     * 
     * @return return a HADBConnectionGroup
     * @throws IOException  */
    public HADBConnectionGroup getConnectionGroup() throws IOException {
        HADBConnectionGroup connGroup = null;
        
        //added for monitoring           
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        long getConnStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            getConnStartTime = System.currentTimeMillis();
        }
        //end added for monitoring
        
        try {
            ConnectionUtil util = this.getConnectionUtil();
            connGroup = util.getConnectionsFromPool();

            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore:  connGroup is null");
                }
                return connGroup;
            } 
        } catch (IOException ex) {
            //this means failure to obtain connection
            ex.printStackTrace();
            throw ex;
        }
        
        //added for monitoring      
        if(this.isMonitoringEnabled()) {
            long getConnEndTime = System.currentTimeMillis();
            stats.processGetConnectionFromPool(getConnEndTime - getConnStartTime);
        }
        //end added for monitoring 
        
        return connGroup;
    }

    /** Loads the state of the bean from the DB
     * @param id
     * @throws IOException
     * @return
     */
    public SFSBBeanState loadBean(Object id) throws IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "loadBean", id);
        }
        if (id == null) {
             return null;
        }
        SFSBBeanState bean = null;
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        
        try {
            //added for monitoring
            long getConnStartTime = 0L;
            if(this.isMonitoringEnabled()) {
                getConnStartTime = System.currentTimeMillis();
            }
            //end added for monitoring             
            
            ConnectionUtil util = this.getConnectionUtil();
            //connGroup = this.getConnectionsFromPool();
            connGroup = util.getConnectionsFromPool(true);
            
            //added for monitoring      
            if(this.isMonitoringEnabled()) {
                long getConnEndTime = System.currentTimeMillis();
                stats.processGetConnectionFromPool(getConnEndTime - getConnStartTime);
            }
            //end added for monitoring             

            //if we cannot get a connection then quit
            if (connGroup == null) {
                _logger.log(Level.INFO, "HAEjbStore:  connGroup is null");

                return null;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;
            bean = loadSFSBean(id, internalConn);
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;

            return bean;
        }
        catch (IOException ex) {
            //this means failure to obtain connection
            ex.printStackTrace();
            //throw ex;
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "loadBean", bean);
        }
        return bean;
    }

    /** Given the ResultSet, The store creates the SFSBeanState object from the ResultSet data
     * @param rst
     * @return
     */
    protected SFSBBeanState getSFSBean(ResultSet rst) {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "getSFSBean", rst);
        }
        SFSBBeanState bean = null;
        String id = null;
        byte[] beandata = null;
        String clusterid = "";
        long lastaccess = 0;
        String containerId = null;
        BufferedInputStream bis = null;
        long longContId = 0;
        try {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "HAEjbStore: getSFSBean ResultSet =" + rst);
            }

            id = rst.getString(1); //bean  ID
            Blob blob = rst.getBlob(2); //beandata
            clusterid = rst.getString(3); //clusterid
            lastaccess = rst.getLong(4); //lastaccess
            containerId = rst.getString(5); //containerId
            //FIXME later this should be long in database
            longContId = (Long.valueOf(containerId)).longValue();
            //beandata = blob.getBytes( (long) 1, (int) blob.length());
            //replacing use of getBytes
            int blobLen = (int) blob.length();
            beandata = new byte[blobLen]; 
            DataInputStream dis = new DataInputStream(blob.getBinaryStream());
            try {
                dis.readFully (beandata, 0, blobLen);
            } catch (IOException ex) {
                _logger.log(Level.FINE,
                            "HAEjbStore: getSFSBean error getting blob data");                                            
            }
            
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore: SFSBBeanState id =" + id + " clusterid=" +
                            clusterid + " lastaccess=" + lastaccess +
                            " blob.length=" +
                            blob.length() + " beandata.length=" + beandata.length +
                            "   containerId=" + containerId);
            }

        }
        catch (SQLException sqe) {
            sqe.printStackTrace();
        }

        //bean = new SFSBBeanState(id, lastaccess, false, beandata);
        SFSBStoreManager mgr = this.manager;
        bean = new SFSBBeanState(clusterid, longContId, id, lastaccess,
                false, beandata, mgr);
        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "getSFSBean", bean);
        }
        return bean;
    }
    
    /** Given the ResultSet, The store creates the SFSBeanState object from the ResultSet data
     * @param rst
     * @param beanId
     * @return
     */
    protected SFSBBeanState getSFSBean(ResultSet rst, Object beanId) {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "getSFSBean", rst);
        }
        SFSBBeanState bean = null;
        String id = null;
        byte[] beandata = null;
        String clusterid = "";
        long lastaccess = 0;
        String containerId = null;
        BufferedInputStream bis = null;
        long longContId = 0;
        try {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST, "HAEjbStore: getSFSBean ResultSet =" + rst);
            }

            id = rst.getString(1); //bean  ID
            Blob blob = rst.getBlob(2); //beandata
            clusterid = rst.getString(3); //clusterid
            lastaccess = rst.getLong(4); //lastaccess
            containerId = rst.getString(5); //containerId
            //FIXME later this should be long in database
            longContId = (Long.valueOf(containerId)).longValue();
            //beandata = blob.getBytes( (long) 1, (int) blob.length());
            //replacing use of .getBytes           
            int blobLen = (int) blob.length();
            beandata = new byte[blobLen]; 
            DataInputStream dis = new DataInputStream(blob.getBinaryStream());
            try {
                dis.readFully (beandata, 0, blobLen);
            } catch (IOException ex) {
                _logger.log(Level.FINE,
                    "HAEjbStore: getSFSBean error getting blob data");                                            
            }            
            
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore: SFSBBeanState id =" + id + " clusterid=" +
                            clusterid + " lastaccess=" + lastaccess +
                            " blob.length=" +
                            blob.length() + " beandata.length=" + beandata.length +
                            "   containerId=" + containerId);
            }

        }
        catch (SQLException sqe) {
            sqe.printStackTrace();
        }

        SFSBStoreManager mgr = this.manager;
        bean = new SFSBBeanState(clusterid, longContId, beanId, lastaccess,
                false, beandata, mgr);
        
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "getSFSBean", bean);
        }
        return bean;
    }    
    
    
    /** This method contacts the DB using the given connection and queries the HADB for the required bean and creates the State object for the bean
     * @param id
     * @param connection
     * @throws IOException
     * @return
     */
    private SFSBBeanState loadSFSBean(Object id, Connection connection) throws
        IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "loadSFSBean", new Object[] {id,
                             connection});
        }
        PreparedStatement preparedLoadSql = null;
        String loadSql =
            "SELECT  id, beandata, clusterid, lastaccess, containerid FROM " +
            blobSfsbTable + " WHERE id = ?";
        ResultSet rst = null;
        SFSBBeanState sfsBean = null;
        try {

            haErrLoad.txStart();
            while ( ! haErrLoad.isTxCompleted() ) {                 
                try {                
                    preparedLoadSql = connection.prepareStatement(loadSql);
                    preparedLoadSql.setString(1, id.toString());
                    rst = preparedLoadSql.executeQuery();

                    if (rst == null || !rst.next()) {
                        debug("No persisted data found for session " + id);
                        if(_logger.isLoggable(Level.FINER)) {
                            _logger.exiting("HAEjbStore", "loadSFSBean", null);
                        }
                        closePreparedStatement(preparedLoadSql);                        
                        return null;
                    }

                    sfsBean = getSFSBean(rst, id);
                    // was sfsBean = getSFSBean(rst);
                    
                    closePreparedStatement(preparedLoadSql);
                    
                    haErrLoad.txEnd();
                } catch (SQLException e) {
                    closePreparedStatement(preparedLoadSql);
                    haErrLoad.checkError(e, connection);
                }                    
            }
                
        }
        catch (SQLException e) {
            /*This may be rolled up from EITHER getSession()
             OR loadAttributes() */
            try {
                connection.rollback();
                closePreparedStatement(preparedLoadSql);                
            }
            catch (SQLException ee) {}
            e.printStackTrace();
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-loadSFSBean: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        catch (Exception e) {
            try {
                connection.rollback();
            }
            catch (SQLException ee) {}
            e.printStackTrace();
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "loadSFSBean", sfsBean);
        }
        return sfsBean;
    }     
    
    /** This method contacts the DB using the given connection and updates the bean in the HADB
     * Note: this method also uses no commit but unlike the updateSFSBeanNoCommit
     * method, it is called using a connection with autocommit=true
     * @param sfsb
     * @param conn
     * @throws IOException
     */
    private void updateSFSBean(SFSBBeanState sfsb, Connection conn) throws
        IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "updateSFSBean", new Object[] {sfsb,
                             conn});
        }
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        PreparedStatement preparedUpdateSql = null;
        ResultSet rst = null;
        String updateSFSBSql = "UPDATE " + blobSfsbTable +
            " SET clusterid = ?, lastaccess = ?, beandata = ?, containerid = ? where id=?";

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "HAEjbStore: updateSFSBSql is " + updateSFSBSql);
            _logger.log(Level.FINEST,
                        "HAEjbStore: SFSBBeanState id " + sfsb.getId() +
                        "  class=  " +
                        (sfsb.getId()).getClass());
        }

        long startPrepTime = 0L;
        try {

            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {

                try {                    
                    if(this.isMonitoringEnabled()) {
                        startPrepTime = System.currentTimeMillis();
                    }                    

                    preparedUpdateSql = conn.prepareStatement(updateSFSBSql);                

                    String id = (sfsb.getId()).toString();
                    int length = sfsb.getState().length;
                    preparedUpdateSql.setString(5, id); //bean id
                    preparedUpdateSql.setBytes(3, sfsb.getState()); //beandata
                    preparedUpdateSql.setString(1, this.getClusterID()); //clusterid
                    preparedUpdateSql.setLong(2, sfsb.getLastAccess()); //lastaccess
                    preparedUpdateSql.setString(4, this.getContainerId()); //containerid

                    if(this.isMonitoringEnabled()) {
                        long endPrepTime = System.currentTimeMillis();
                        stats.processStatementPrepBlock(endPrepTime - startPrepTime);
                    }

                    long execStartTime = 0L;    
                    if(this.isMonitoringEnabled()) {
                        execStartTime = System.currentTimeMillis();
                    }
                    preparedUpdateSql.executeUpdate();

                    if(this.isMonitoringEnabled()) { 
                        long execEndTime = System.currentTimeMillis();
                        stats.processExecuteStatement(execEndTime - execStartTime);          
                    }                    
                    //commit deliberately removed - method using autocommit=true
                    //conn.commit();                    
                    preparedUpdateSql.close();
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST,
                            "HAEjbStore:updateSFSBean========================Committed");
                    }

                    haErr.txEnd();
                } catch (SQLException e) {
                    closePreparedStatement(preparedUpdateSql);
                    haErr.checkError(e, conn);
                }                    
            }            
        }
        catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore-updateSFSBean: EXCEPTION HERE  " +
                            preparedUpdateSql);
            }
            e.printStackTrace();
            try {
                //( (Connection) connection).rollback();
                conn.rollback();
                preparedUpdateSql.close();
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "Error from HAEjbStore-updateSFSBean: " + e.getMessage()).
                              initCause(e);
            throw ex1;
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-updateSFSBean: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        } 
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "updateSFSBean");
        }
    }    
    
    /** This method contacts the DB using the given connection and updates the bean in the HADB
     * Note: this method uses no commit but unlike the updateSFSBean
     * method, it is called using a connection with autocommit=false as part
     * of an aggregate save spanning 1:n beans
     * @param sfsb
     * @param conn
     * @throws SQLException
     */
    private void updateSFSBeanNoCommit(SFSBBeanState sfsb, Connection conn) throws
        SQLException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "updateSFSBeanNoCommit", new Object[] {sfsb,
                             conn});
        }
        //added for monitoring
        EJBModuleStatistics stats = this.getEJBModuleStatistics();        
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring         
        ResultSet rst = null;
        String updateSFSBSql = "UPDATE " + blobSfsbTable +
            " SET clusterid = ?, lastaccess = ?, beandata = ?, containerid = ? where id=?";

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "HAEjbStore: updateSFSBSql is " + updateSFSBSql);
            _logger.log(Level.FINEST,
                        "HAEjbStore: SFSBBeanState id " + sfsb.getId() +
                        "  class=  " +
                        (sfsb.getId()).getClass());
        }

        long startPrepTime = 0L;

        try {                    
            if(this.isMonitoringEnabled()) {
                startPrepTime = System.currentTimeMillis();
            }                     

            if (preparedUpdateSqlNoCommit == null) {
                preparedUpdateSqlNoCommit = conn.prepareStatement(updateSFSBSql);                
            }

            String id = (sfsb.getId()).toString();
            preparedUpdateSqlNoCommit.setString(5, id); //bean id
            preparedUpdateSqlNoCommit.setBytes(3, sfsb.getState()); //beandata
            preparedUpdateSqlNoCommit.setString(1, this.getClusterID()); //clusterid
            preparedUpdateSqlNoCommit.setLong(2, sfsb.getLastAccess()); //lastaccess
            preparedUpdateSqlNoCommit.setString(4, this.getContainerId()); //containerid

            if(this.isMonitoringEnabled()) {
                long endPrepTime = System.currentTimeMillis();
                stats.processStatementPrepBlock(endPrepTime - startPrepTime);
            }
            long execStartTime = 0L;    
            if(this.isMonitoringEnabled()) {
                execStartTime = System.currentTimeMillis();
            }                    
            //executeStatement(preparedUpdateSqlNoCommit, false);
            preparedUpdateSqlNoCommit.executeUpdate();
            if(this.isMonitoringEnabled()) { 
                long execEndTime = System.currentTimeMillis();
                stats.processExecuteStatement(execEndTime - execStartTime);
                stats.processCheckpointSave(execEndTime - saveStartTime);
            }                    
               
        }
        catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore-updateSFSBeanNoCommit:  " +
                            preparedUpdateSqlNoCommit);
            }
            //e.printStackTrace();
            throw e;
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "updateSFSBeanNoCommit");
        }
    }   
    
    /** This method contacts the DB using the given connection and inserts the bean in the HADB
     * Note: this method also uses no commit but unlike the insertSFSBeanNoCommit
     * method, it is called using a connection with autocommit=true
     * @param sfsb
     * @param conn
     * @throws IOException
     */
    private void insertSFSBean(SFSBBeanState sfsb, Connection conn) throws
        IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "insertSFSBean", new Object[] {sfsb,
                             conn});
        }
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        PreparedStatement preparedInsertSql = null;
        ResultSet rst = null;
        String insertSFSBSql = "INSERT INTO " + blobSfsbTable +
            " (id, clusterid, lastaccess, beandata, containerid) " +
            " VALUES (?, ?, ?, ?, ?) ";

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "HAEjbStore: insertSFSBSQL is " + insertSFSBSql);
            _logger.log(Level.FINEST,
                        "HAEjbStore: SFSBBeanState id " + sfsb.getId() +
                        "  class=  " +
                        (sfsb.getId()).getClass());
        }

        long startPrepTime = 0L;
        try {
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {
                try {                                        
                    if(this.isMonitoringEnabled()) {
                        startPrepTime = System.currentTimeMillis();
                    } 

                    preparedInsertSql = conn.prepareStatement(insertSFSBSql);                

                    String id = (sfsb.getId()).toString();
                    int length = sfsb.getState().length;
                    preparedInsertSql.setString(1, id);
                    preparedInsertSql.setBytes(4, sfsb.getState());
                    preparedInsertSql.setString(2, this.getClusterID());
                    preparedInsertSql.setLong(3, sfsb.getLastAccess());
                    preparedInsertSql.setString(5, this.getContainerId());

                    if(this.isMonitoringEnabled()) {
                        long endPrepTime = System.currentTimeMillis();
                        stats.processStatementPrepBlock(endPrepTime - startPrepTime);
                    }
                    long execStartTime = 0L;    
                    if(this.isMonitoringEnabled()) {
                        execStartTime = System.currentTimeMillis();
                    }                    
                    preparedInsertSql.executeUpdate();
                    if(this.isMonitoringEnabled()) { 
                        long execEndTime = System.currentTimeMillis();
                        stats.processExecuteStatement(execEndTime - execStartTime);          
                    }                    
                    //commit deliberately removed; using autocommit=true
                    //conn.commit();                    
                    preparedInsertSql.close();

                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST,
                                    "HAEjbStore:::::::::::::::::::::::: Committed");
                    }

                    haErr.txEnd();
                } catch (SQLException e) {
                    closePreparedStatement(preparedInsertSql);
                    haErr.checkError(e, conn);
                }                    
            }                 
        }
        catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore: EXCEPTION HERE  " + preparedInsertSql);
            }
            e.printStackTrace();
            try {
                //( (Connection) connection).rollback();
                conn.rollback();
                preparedInsertSql.close();
            }
            catch (Exception ee) {
                e.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "Error from HAEjbStore: " +
                e.getMessage()).initCause(e);
            throw ex1;
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjb store-insertSFSBean: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "insertSFSBean");
        }
    }
    
    /** This method contacts the DB using the given connection and inserts the bean in the HADB
     * Note: this method uses no commit but unlike the insertSFSBean
     * method, it is called using a connection with autocommit=false as part
     * of an aggregate save spanning 1:n beans
     * @param sfsb
     * @param conn
     * @throws SQLException
     */
    private void insertSFSBeanNoCommit(SFSBBeanState sfsb, Connection conn) throws
        SQLException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "insertSFSBeanNoCommit", new Object[] {sfsb,
                             conn});
        }
        EJBModuleStatistics stats = this.getEJBModuleStatistics();
        
        //added for monitoring
        long saveStartTime = 0L;
        if(this.isMonitoringEnabled()) {
            saveStartTime = System.currentTimeMillis();
        }
        //end added for monitoring        
        
        ResultSet rst = null;
        String insertSFSBSql = "INSERT INTO " + blobSfsbTable +
            " (id, clusterid, lastaccess, beandata, containerid) " +
            " VALUES (?, ?, ?, ?, ?) ";

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST, "HAEjbStore: insertSFSBSQL is " + insertSFSBSql);
            _logger.log(Level.FINEST,
                        "HAEjbStore: SFSBBeanState id " + sfsb.getId() +
                        "  class=  " +
                        (sfsb.getId()).getClass());
        }

        long startPrepTime = 0L;

        try {                    
            if(this.isMonitoringEnabled()) {
                startPrepTime = System.currentTimeMillis();
            }                 
            if (preparedInsertSqlNoCommit == null) {
                preparedInsertSqlNoCommit = conn.prepareStatement(insertSFSBSql);
            }

            String id = (sfsb.getId()).toString();
            preparedInsertSqlNoCommit.setString(1, id);
            preparedInsertSqlNoCommit.setBytes(4, sfsb.getState());
            preparedInsertSqlNoCommit.setString(2, this.getClusterID());
            preparedInsertSqlNoCommit.setLong(3, sfsb.getLastAccess());
            preparedInsertSqlNoCommit.setString(5, this.getContainerId());

            if(this.isMonitoringEnabled()) {
                long endPrepTime = System.currentTimeMillis();
                stats.processStatementPrepBlock(endPrepTime - startPrepTime);
            }
            long execStartTime = 0L;    
            if(this.isMonitoringEnabled()) {
                execStartTime = System.currentTimeMillis();
            }                    
            preparedInsertSqlNoCommit.executeUpdate();
            if(this.isMonitoringEnabled()) { 
                long execEndTime = System.currentTimeMillis();
                stats.processExecuteStatement(execEndTime - execStartTime);
                //note using execEndTime for calculating checkpoint save
                //duration too
                stats.processCheckpointSave(execEndTime - saveStartTime);
            }
        } catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore: insertSFSBeanNoCommit: " + preparedInsertSqlNoCommit);
            }
            //e.printStackTrace();
            throw e;
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "insertSFSBeanNoCommit");
        }
    }           
    
    /** This method updates only the last access time for a bean
     * @param sessionKey
     * @param time
     * @throws IOException
     */
    public void updateLastAccessTime(Object sessionKey, long time) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "HAEjbStore.updateLastAccessTime id = " + sessionKey);
        }
        //Object ids[];
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;         
        try {
            ConnectionUtil util = this.getConnectionUtil();
            //using autocommit=true
            connGroup = util.getConnectionsFromPool(true);                        
            
            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore>>updateLastAccessTime:  connGroup is null");
                }
                //return;
                throw new IOException("failed to obtain connection in updateLastAccessTime: failed to update last access time for bean id: " +
                    sessionKey.toString());
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;            
            this.updateLastAccessTimeForBean(sessionKey, time, internalConn);
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;            
        }
        catch (IOException ex) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                       "updateLastAccessTime: failed to update last access time for bean id: " +
                               sessionKey.toString());
            }
            ex.printStackTrace();
            throw ex;
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }        
    }       
    
    /** This method updates only the last access time for a bean
     * @param sessionKey
     * @param time
     * @param connection
     * @throws IOException
     */    
    private void updateLastAccessTimeForBean(Object sessionKey, long time, Connection connection) 
        throws IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "updateLastAccessTimeForBean", new Object[] {sessionKey,
                             connection});
        }
        ResultSet rst = null;
        String updateSFSBSql = "UPDATE " + blobSfsbTable +
            " SET lastaccess = ? where id=?";

        PreparedStatement preparedUpdateTSSql = null;

        /*
        _logger.log(Level.FINEST, "HAEjbStore: updateSFSBSql is " + updateSFSBSql);
        _logger.log(Level.FINEST,
                    "HAEjbStore: SFSBBeanState id " + sfsb.getId() +
                    "  class=  " +
                    (sfsb.getId()).getClass());
         */

        try {

            haErrUpdateAccessTime.txStart();

            while ( ! haErrUpdateAccessTime.isTxCompleted() ) {                 
                try {                
                    preparedUpdateTSSql = connection.prepareStatement(updateSFSBSql);
                    preparedUpdateTSSql.setLong(1, time); //lastaccess
                    preparedUpdateTSSql.setString(2, sessionKey.toString()); //bean id

                    preparedUpdateTSSql.executeUpdate();

                    //deliberately removing commit; using autocommit=true
                    //connection.commit();                    
                    preparedUpdateTSSql.close();
                    preparedUpdateTSSql = null;
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST,
                            "HAEjbStore:updateLastAccessTimeForBean========================Committed");
                    }

                    haErrUpdateAccessTime.txEnd();
                } catch (SQLException e) {
                    closePreparedStatement(preparedUpdateTSSql);
                    haErrUpdateAccessTime.checkError(e, connection);
                }                     
            }               
        }
        catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore-updateLastAccessTimeForBean: EXCEPTION HERE  " +
                            preparedUpdateTSSql);
            }
            e.printStackTrace();
            try {
                connection.rollback();
                preparedUpdateTSSql.close();
                preparedUpdateTSSql = null;
            }
            catch (Exception ee) {
                ee.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "Error from HAEjbStore-updateLastAccessTimeForBean: " + e.getMessage()).
                              initCause(e);
            throw ex1;
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-updateLastAccessTimeForBean: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "updateLastAccessTimeForBean");
        }
    }       

    /**
     * Remove the Session with the specified session identifier from
     * this Store, if present.  If no such Bean is present, this method
     * takes no action.
     *
     * @param id Bean identifier of the SFSB to be removed
     *
     * @exception IOException if an input/output error occurs
     */
    public boolean remove(Object id) throws IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "remove", id);
        }
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        boolean result = false;
        EJBModuleStatistics stats = this.getEJBModuleStatistics(); 

        try {
            
            //added for monitoring
            long getConnStartTime = 0L;
            if(this.isMonitoringEnabled()) {
                getConnStartTime = System.currentTimeMillis();
            }
            //end added for monitoring            
            
            ConnectionUtil util = this.getConnectionUtil();
            connGroup = this.getConnectionsFromPool();
            
            //added for monitoring      
            if(this.isMonitoringEnabled()) {
                long getConnEndTime = System.currentTimeMillis();
                stats.processGetConnectionFromPool(getConnEndTime - getConnStartTime);
            }
            //end added for monitoring            

            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore:  connGroup is null");
                }

                return false;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;
            result = removeSFSBean(id, internalConn);
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;
            if(_logger.isLoggable(Level.FINER)) {
                _logger.exiting("HAEjbStore", "remove", new Boolean(result));
            }
            return result;
        }
        catch (IOException ex) {
            //this means failure to obtain connection
            ex.printStackTrace();
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }
        if(_logger.isLoggable(Level.FINER)) {
            _logger.exiting("HAEjbStore", "remove", new Boolean(result));
        }
        return result;
    }
    
    /** this method contacts the DB using the given connection and removes the bean from the HADB.
     * @param id
     * @param connection
     * @throws IOException
     * @return
     */
    private boolean removeSFSBean(Object id, Connection connection) throws
        IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "removeSFSBean", new Object[] {id,
                             connection});
        }

        int result = 0;
        String removeSql = "DELETE FROM " + blobSfsbTable + " WHERE id = ?";
        PreparedStatement preparedRemoveSql = null;
        try {
            haErrRemove.txStart();

            while ( ! haErrRemove.isTxCompleted() ) {                 
                try {                
                    preparedRemoveSql = connection.prepareStatement(removeSql);
                    preparedRemoveSql.setString(1, id.toString());
                    result = preparedRemoveSql.executeUpdate();

                    //( (Connection) connection).commit();
                    connection.commit();
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST,
                            "HAEjbStore:removeSFSBean------------------------Committed");
                    }
                    preparedRemoveSql.close();

                    haErrRemove.txEnd();

                    if(_logger.isLoggable(Level.FINER)) {
                        _logger.exiting("HAEjbStore", "removeSFSBean", new Integer(result));
                    }

                } catch (SQLException e) {
                    closePreparedStatement(preparedRemoveSql);
                    haErrRemove.checkError(e, connection);
                }                    
            }                                 
        }
        catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore-removeSFSBean: EXCEPTION HERE  " +
                            preparedRemoveSql);
            }
            e.printStackTrace();
            try {
                //( (Connection) connection).rollback();
                connection.rollback();
                preparedRemoveSql.close();
            }
            catch (Exception ee) {
                e.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "Error from HAEjbStore-removeSFSBean: " + e.getMessage()).
                              initCause(e);
            throw ex1;
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-removeSFSBean: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }
        return (result > 0) ? true : false;
        //_logger.exiting("HAEjbStore", "removeSFSBean",new Boolean(false));
    }    
    
    /** 
     * This deletes all the beans corresponding to the "containerId" 
     * that should be expired 
     */
    public void removeExpired() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "HAEjbStore.removeExpired containerId = " + this.containerID);
        }
        Object ids[] = {};
        Connection connection = null;
        try {
            connection = getConnection(false);

            ids = expiredKeys(blobSfsbTable, connection);
            for (int i = 0; i < ids.length; i++) {
                this.removeSFSBean(ids[i], connection);

            }
        }
        catch (IOException ex) {
            _logger.log(Level.WARNING,
                   "removeExpired: failed to remove all the beans for this container " +
                           this.getContainerId());

        }
        //FIXME return the length later
        System.out.println("number of expired beans = " + ids.length);
        //return ids.length;

    }
    
    /** This method deletes all the beans corresponding to the "containerId"
     * that should be expired
     * @return number of removed beans
     */    
    public int removeExpiredSessions() {
        _logger.finest("IN HAEjbStore>>removeExpiredSessions");
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "HAEjbStore.removeExpiredSessions containerId = " + this.containerID);
        }
        //Object ids[] = {};
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        int result = 0;
        try {
            ConnectionUtil util = this.getConnectionUtil();
            connGroup = util.getConnectionsFromPool();            
            
            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore>>removeExpiredSessions:  connGroup is null");
                }
                return result;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;            
            //result = this.removeExpiredSFSBeans(blobSfsbTable, internalConn);
            result = this.removeExpiredSFSBeans(blobSfsbTable, internalConn, CHUNK_SIZE);
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;            
        }        
        catch (IOException ex) {
            _logger.log(Level.WARNING,
                   "removeExpiredSessions: failed to remove all the expired beans for this container " +
                           this.getContainerId());            
            ex.printStackTrace();
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }         
        
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HAEjbStore>>removeExpiredSessions():number of expired beans = " + result);
        }
        return result;

    }         
    
    /** this method contacts the DB using the given connection and removes the bean from the HADB.
     * @param sessionTable
     * @param connection
     * @throws IOException
     * @return
     */
    private int removeExpiredSFSBeansPrevious(String sessionTable, Connection connection) throws
        IOException {
        if(_logger.isLoggable(Level.FINER)) {
            _logger.entering("HAEjbStore", "removeExpiredSFSBeans", new Object[] {connection});
        }

        PreparedStatement preparedRemoveSql = null;
        int result = 0;
        long timeNow = System.currentTimeMillis();
        HASFSBStoreManager mgr = (HASFSBStoreManager) this.getSFSBStoreManager();
        long idleTimeoutInMillis = mgr.getIdleTimeoutInSeconds() * 1000;   
        long relevantTime = (long) (timeNow - idleTimeoutInMillis);
        //only selecting keys for this container
        String removeExpiredSql = "DELETE FROM " + sessionTable
            + " WHERE containerId = '" + getContainerId() + "'"
            + " AND lastaccess < ?";                

        try {
            haErrRemoveExpired.txStart();

            while ( ! haErrRemoveExpired.isTxCompleted() ) {                 
                try {                
                    preparedRemoveSql = connection.prepareStatement(removeExpiredSql);
                    preparedRemoveSql.setLong(1, relevantTime);
                    result = preparedRemoveSql.executeUpdate();

                    connection.commit();
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.log(Level.FINEST,
                            "HAEjbStore:removeExpiredSFSBeans------------------------Committed");
                    }
                    preparedRemoveSql.close();

                    haErrRemoveExpired.txEnd();

                } catch (SQLException e) {
                    closePreparedStatement(preparedRemoveSql);
                    haErrRemoveExpired.checkError(e, connection);
                } 
                if(_logger.isLoggable(Level.FINER)) {
                    _logger.exiting("HAEjbStore", "removeExpiredSFSBean", new Integer(result));
                }
            }                
                 
        } catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAEjbStore-removeExpiredSFSBeans: EXCEPTION HERE  " +
                            preparedRemoveSql);
            }
            e.printStackTrace();
            try {
                connection.rollback();
                preparedRemoveSql.close();
            }
            catch (Exception ee) {
                e.printStackTrace();
            }
            IOException ex1 = (IOException)new IOException(
                "Error from HAEjbStore-removeExpiredSFSBeans: " + e.getMessage()).
                              initCause(e);
            throw ex1;
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-removeExpiredSFSBeans: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }
        return result;
        //_logger.exiting("HAEjbStore", "removeSFSBean",new Boolean(false));
    } 
    
    /** this method contacts the DB using the given connection and removes the bean from the HADB.
     * @param sessionTable
     * @param connection
     * @throws IOException
     * @return
     */
    private int removeExpiredSFSBeans(String sessionTable, Connection connection, int chunk) 
        throws IOException {
        int result = 0;        
        if (connection == null) {
            return result;
        }
        
        ResultSet rst = null;
        PreparedStatement preparedKeysSql = null;
        
        long timeNow = System.currentTimeMillis();
        HASFSBStoreManager mgr = (HASFSBStoreManager) this.getSFSBStoreManager();
        long idleTimeoutInMillis = mgr.getIdleTimeoutInSeconds() * 1000;   
        long relevantTime = (long) (timeNow - idleTimeoutInMillis);
        
        //only selecting sfsb id's  for this container        
        String keysSql = "SELECT id FROM " + sessionTable
            + " WHERE containerId = ?"
            + " AND lastaccess < ? ORDER BY id";
        
        //only deleting sfsb id's  for this container
        PreparedStatement preparedDeleteSql = null;        
        String deleteSql = "DELETE FROM " + sessionTable
            + " WHERE id BETWEEN ? AND ? AND containerId = ?"
            + " AND lastaccess < ?";        
        
        try {

            haErrRemoveExpired.txStart();

            while ( ! haErrRemoveExpired.isTxCompleted() ) {                 
                try {
                    boolean moreLeft = true;
                    while (moreLeft) {
                        preparedKeysSql = connection.prepareStatement(keysSql);
                        preparedKeysSql.setString(1, this.getContainerId());
                        preparedKeysSql.setLong(2, relevantTime);
                        rst = preparedKeysSql.executeQuery();
                        
                        //test if set is empty
                        if (!rst.isBeforeFirst()) {
                            //the set was empty
                            rst.close();
                            moreLeft = false;
                            break;
                        }
                        
                        int rowno = 0;
                        String keyLow = null;
                        String keyHigh = null;
                        boolean chunkSizeReached = false;
                        while (rst.next()) {
                            if (rowno == 0) {
                                keyLow = rst.getString(1);
                            }
                            keyHigh = rst.getString(1);
                            if(++rowno >= chunk) {
                                chunkSizeReached = true;
                                break;
                            }                            
                        }
                        if(!chunkSizeReached) {
                            moreLeft = false;
                        }
                        rst.close();
                        
                        preparedDeleteSql = connection.prepareStatement(deleteSql);
                        preparedDeleteSql.setString(1, keyLow);
                        preparedDeleteSql.setString(2, keyHigh);
                        preparedDeleteSql.setString(3, this.getContainerId());
                        preparedDeleteSql.setLong(4, relevantTime);
                        int cnt = preparedDeleteSql.executeUpdate();
                        result += cnt;
                        connection.commit();
                    }

                    connection.commit();
                    closePreparedStatement(preparedKeysSql);
                    closePreparedStatement(preparedDeleteSql);

                    haErrRemoveExpired.txEnd();
                    return result;
                } catch (SQLException e) {
                    closePreparedStatement(preparedKeysSql);
                    closePreparedStatement(preparedDeleteSql);                    
                    haErrRemoveExpired.checkError(e, connection);
                }                     
            }                
                               
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
                closePreparedStatement(preparedKeysSql);
                closePreparedStatement(preparedDeleteSql);
            }
            catch (SQLException ee) {
                ee.printStackTrace();
            }
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-removeExpiredSFSBeans: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        

        return result;        
    }     

    /** 
     * This deletes all the beans corresponding to the "containerId" 
     */
    public void undeployContainerPrevious() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "HAEjbStore.undeployContainer containerId = " + this.containerID);
        }
        Object id[];
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        try {
            
            ConnectionUtil util = this.getConnectionUtil();
            connGroup = util.getConnectionsFromPool();            
            
            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore>>undeployContainer:  connGroup is null");
                }
                return;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;            

            id = getBeanIDsForContainer(internalConn);
            for (int i = 0; i < id.length; i++) {
                this.removeSFSBean(id[i], internalConn);
            }
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;            
        }        
        catch (IOException ex) {
            _logger.log(Level.WARNING,
                   "undeployContainer: failed to remove all the beans for this container " +
                           this.getContainerId());            
            ex.printStackTrace();
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }        

    }
    
    /**
     * This deletes all the beans corresponding to the "containerId" 
     */    
    public void undeployContainer() {
        _logger.finest("IN HAEjbStore>>undeployContainer");
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "HAEjbStore.undeployContainer containerId = " + this.containerID);
        }
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        try {
            ConnectionUtil util = this.getConnectionUtil();
            connGroup = util.getConnectionsFromPool();            
            
            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore>>undeployContainer:  connGroup is null");
                }
                return;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;            
            this.removeAllBeansForContainer(internalConn, CHUNK_SIZE);
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;            
        }        
        catch (IOException ex) {
            _logger.log(Level.WARNING,
                   "undeployContainer: failed to remove all the expired beans for this container " +
                           this.getContainerId());            
            ex.printStackTrace();
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }         
        
        return;

    }    
    
    /** Remove all the beanID's associated with the "containerId"
     * @param connection
     * @param chunk the chunk size to use for the delete operations
     */
    public void removeAllBeansForContainer(Connection connection, int chunk) 
        throws IOException {
            
        if (connection == null) {
            return;
        }
        ResultSet rst = null;
        PreparedStatement preparedKeysSql = null;
        //only selecting sfsb id's  for this container
        String keysSql = "SELECT id FROM " + blobSfsbTable +
                         "  WHERE containerid = ? ORDER BY id";
        //only deleting sfsb id's  for this container
        PreparedStatement preparedDeleteSql = null;
        String deleteSql = "DELETE FROM " + blobSfsbTable + 
                         " WHERE id BETWEEN ? AND ? AND containerid = ?"; 
        
        try {

            haErrRemoveAllBeans.txStart();

            while ( ! haErrRemoveAllBeans.isTxCompleted() ) {                 
                try {
                    boolean moreLeft = true;
                    while (moreLeft) {
                        preparedKeysSql = connection.prepareStatement(keysSql);
                        preparedKeysSql.setString(1, this.getContainerId());
                        rst = preparedKeysSql.executeQuery();
                        
                        //test if set is empty
                        if (!rst.isBeforeFirst()) {
                            //the set was empty
                            rst.close();
                            moreLeft = false;
                            break;
                        }                        
                        
                        int rowno = 0;
                        String keyLow = null;
                        String keyHigh = null;
                        boolean chunkSizeReached = false;
                        while (rst.next()) {
                            if (rowno == 0) {
                                keyLow = rst.getString(1);
                            }
                            keyHigh = rst.getString(1);
                            if(++rowno >= chunk) {
                                chunkSizeReached = true;
                                break;
                            }                            
                        }
                        if(!chunkSizeReached) {
                            moreLeft = false;
                        }
                        rst.close();
                        
                        preparedDeleteSql = connection.prepareStatement(deleteSql);
                        preparedDeleteSql.setString(1, keyLow);
                        preparedDeleteSql.setString(2, keyHigh);
                        preparedDeleteSql.setString(3, this.getContainerId());
                        int cnt = preparedDeleteSql.executeUpdate();
                        connection.commit();
                    }

                    connection.commit();
                    closePreparedStatement(preparedKeysSql);
                    closePreparedStatement(preparedDeleteSql);

                    haErrRemoveAllBeans.txEnd();
                    return;
                } catch (SQLException e) {
                    closePreparedStatement(preparedKeysSql);
                    closePreparedStatement(preparedDeleteSql);                    
                    haErrRemoveAllBeans.checkError(e, connection);
                }                     
            }                
                               
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
                closePreparedStatement(preparedKeysSql);
                closePreparedStatement(preparedDeleteSql);
            }
            catch (SQLException ee) {
                ee.printStackTrace();
            }
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-removeAllBeansForContainer: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        

        return;        
    }           
    
    /**
     * This returns the number of all the beans corresponding to the "containerId" 
     */    
    public int getContainerSize() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.log(Level.FINEST,
                        "HAEjbStore.getContainerSize containerId = " + this.containerID);
        }
        Object id[];
        HADBConnectionGroup connGroup = null;
        Connection internalConn = null;
        Connection externalConn = null;        
        int result = 0;
        try {
            ConnectionUtil util = this.getConnectionUtil();
            connGroup = util.getConnectionsFromPool();            
            
            //if we cannot get a connection then quit
            if (connGroup == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "HAEjbStore>>undeployContainer:  connGroup is null");
                }
                return result;
            }
            internalConn = connGroup._internalConn;
            externalConn = connGroup._externalConn;            

            id = getBeanIDsForContainer(internalConn);
            if(id != null) {
                result = id.length;
            }            
            try {
                externalConn.close();
            }
            catch (java.sql.SQLException ex) {
                ex.printStackTrace();
            }
            externalConn = null;             
        }
        catch (IOException ex) {
            _logger.log(Level.WARNING,
                   "getContainerSize: failed to get number of all the beans for this container " +
                           this.getContainerId());
            ex.printStackTrace();
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                }
                catch (java.sql.SQLException ex) {
                    ex.printStackTrace();
                }
                externalConn = null;
            }
        }        
        
        return result;

    }      
    
    /**
     * Return an array containing the beanID's of all beans
     * currently saved in this Store for this container that are eligible
     * to be expired.  If there are no such beans, a
     * zero-length array is returned.
     *
     * @param sessionTable the name of the main session table
     * @param connection a Connection
     * @throws IOException
     */
    public String[] expiredKeys(String sessionTable, Connection connection) throws IOException  {
        //System.out.println("in new HAEjbStore>>expiredKeys");
        //debug("in expiredKeys");
        /*FIXME: getContainerId must be turned into a String 
        string concatenation is doing it here
        */
        String[] result = {};
        long timeNow = System.currentTimeMillis();
        HASFSBStoreManager mgr = (HASFSBStoreManager) this.getSFSBStoreManager();
        long idleTimeoutInMillis = mgr.getIdleTimeoutInSeconds() * 1000;   
        long relevantTime = (long) (timeNow - idleTimeoutInMillis);
        //only selecting keys for this container
        String expiredKeysSql = "SELECT id FROM " + sessionTable
            + " WHERE containerId = '" + getContainerId() + "'"
            + " AND lastaccess < ?";       
        //System.out.println("HAEjbStore>>expiredKeys: SQL = " + expiredKeysSql);

        if (connection == null) {
            return result;
        }
        
        ResultSet rst = null;
        String keys[] = null;

        try {

            haErrExpiredKeys.txStart();

            while ( ! haErrExpiredKeys.isTxCompleted() ) {                 
                try {                
                    if (preparedExpiredKeysSql == null) {
                        preparedExpiredKeysSql = connection.prepareStatement(expiredKeysSql);
                    }
                    preparedExpiredKeysSql.setLong(1, relevantTime);
                    rst = preparedExpiredKeysSql.executeQuery();

                    if ( rst == null ) {
                        if (_debug > 0) {
                            debug("expiredKeys(): No rows returned, returning an empty array");
                        }
                        return new String[0];
                    }

                    ArrayList keysArray = new ArrayList();

                    for ( int i = 0 ; rst.next() ; i++ )  {
                        if (_debug > 0) {
                            debug("in expiredKeys, id is " + rst.getString(1));
                        }
                        keysArray.add(rst.getString(1));
                    }

                    keys = (String []) keysArray.toArray(new String[0]); 
                    ((Connection)connection).commit();
                    closePreparedStatement(preparedExpiredKeysSql);
                    preparedExpiredKeysSql = null;                    

                    haErrExpiredKeys.txEnd();
                } catch (SQLException e) {
                    closePreparedStatement(preparedExpiredKeysSql);
                    preparedExpiredKeysSql = null;
                    haErrExpiredKeys.checkError(e, connection);
                }                     
            }
               
        } 
        catch(SQLException e) {
            try{((Connection)connection).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HAEjbStore-expiredKeys: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-expiredKeys: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            closeResultSet(rst);
            closePreparedStatement(preparedExpiredKeysSql);
            preparedExpiredKeysSql = null;            
        }
        //FIXME remove after testing
        /*
        System.out.println("returning expiredKeys size = " + keys.length);
        for(int i=0; i<keys.length; i++) {
            String nextKey = keys[i];
            System.out.println("key[" + i + "]= " + nextKey);
        }
         */
        return keys;    
    }      
    
    /**
    * Helper routine that closes a result set
    * @param rst a ResultSet
    */
    protected void closeResultSet(ResultSet rst) {
        try {
            if (rst != null) {
                rst.close();
            }
        } 
        catch(SQLException e) {
        }
    }
    
    /** Get all the beanID's associated with the "containerId"
     * @return array of bean id's
     * @param connection
     */
    public Object[] getBeanIDsForContainer(Connection connection) 
        throws IOException {
        String[] result = {};
        ResultSet rst = null;
        String keys[] = null;

        //only selecting sfsb id's  for this container
        String keysSql = "SELECT id FROM " + blobSfsbTable +
                         " WHERE containerid = ?";

        PreparedStatement preparedKeysSql = null;
        if (connection == null) {
            return result;
        }
        
        try {

            haErrGetBeanIds.txStart();

            while ( ! haErrGetBeanIds.isTxCompleted() ) {                 
                try {                
                    preparedKeysSql = connection.prepareStatement(keysSql);
                    //preparedKeysSql.setLong(1, containerId);
                    preparedKeysSql.setString(1, this.getContainerId());
                    rst = preparedKeysSql.executeQuery();

                    if (rst == null) {
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.log(Level.FINEST,
                                "getBeanIDsForContainer: no beans found for this container " +
                                        this.getContainerId());
                        }
                        closePreparedStatement(preparedKeysSql);                         
                        return new String[0];
                    }

                    ArrayList keysArray = new ArrayList();

                    for (int i = 0; rst.next(); i++) {
                        if (_debug > 0) {
                            debug("in keys, id is " + rst.getString(1));
                        }
                        keysArray.add(rst.getString(1));
                    }

                    keys = (String[]) keysArray.toArray(new String[0]);
                    //( (Connection) connection).commit();
                    connection.commit();
                    closePreparedStatement(preparedKeysSql);                    

                    haErrGetBeanIds.txEnd();
                    return keys;
                } catch (SQLException e) {
                    closePreparedStatement(preparedKeysSql);
                    haErrGetBeanIds.checkError(e, connection);
                }                     
            }                
                               
        }
        /*
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
         */
        catch (SQLException e) {
            e.printStackTrace();
            try {
                //( (Connection) connection).rollback();
                connection.rollback();
                closePreparedStatement(preparedKeysSql);                
            }
            catch (SQLException ee) {
                ee.printStackTrace();
            }
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAEjbStore-getBeanIDsForContainer: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        

        return result;
    }       

    /** Check the connection associated with this store, if it's
     * <code>null</code> or closed try to reopen it.
     * set autoCommit to autoCommit
     * Returns <code>null</code> if the connection could not be established.
     *
     * @return <code>Connection</code> if the connection suceeded
     * @param autoCommit
     * @throws IOException  */
    protected Connection getConnection(boolean autoCommit) throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        //get returned connection into cached connection conn
        conn = util.getConnection(autoCommit);
        return conn;
    }

    /**
     * Helper routine that cleans up statements and cached connection
     * and resets sessions cache
     */
    public void cleanup() {
        closeStatements();
        closeConnection();
    }

    /**
     * Check the connection associated with this store, if it's not
     * <code>null</code> or open try to close it.
     *
     */
    protected void closeConnection() {
        try {
            if (conn != null && (!conn.isClosed())) {
                conn.close();
                conn = null;
            }
        }
        catch (java.sql.SQLException ex) {}
        conn = null;
        ConnectionUtil util = this.getConnectionUtil();
        if (util != null) {
            util.clearCachedConnection();
        }
    }

    /**
     * close all the PreparedStatements
     */    
    protected void closeStatements() {
        PreparedStatement[] statements 
            = {preparedExpiredKeysSql};
        for (int i = 0; i < statements.length; i++) {
            PreparedStatement nextStatement =
                (PreparedStatement) statements[i];
            closeStatement(nextStatement);
        }
        this.clearStatementReferences();

    }
    
    
    /**
     * close a PreparedStatement
     *
     * @param stmt a PreparedStatement
     */
    protected void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (java.sql.SQLException ex) {}
        }
    }
    
    /**
     * clear PreparedStatement references
     */    
    protected void clearStatementReferences() {
        preparedExpiredKeysSql = null;        
    }

    // Copied from closeStatement, we do not know whether closeStatement
    // (protected) is over-ridden in a sub-class
    // FIXME
    private void closePreparedStatement(final PreparedStatement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (java.sql.SQLException ex) {}
        }
    }
}
