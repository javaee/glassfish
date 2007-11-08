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
 * HAAttributeStore.java
 *
 * Created on October 7, 2002, 9:07 AM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import org.apache.catalina.session.*;
import org.apache.catalina.util.*;
import org.apache.catalina.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.sql.*;
import javax.sql.*;
//import com.sun.hadb.comm.Logger;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import com.sun.appserv.util.cache.BaseCache;
//import com.sun.hadb.jdbc.*;
import com.sun.enterprise.ee.web.initialization.ServerConfigReader;
//Bug 4832603 : EJB Reference Failover
import com.sun.ejb.EJBUtils;
//end - Bug 4832603

import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author  lwhite
 */
public class HAAttributeStore extends HAStore implements HAStorePoolElement {
    protected String sessionHeaderTable = "sessionheader";
    protected String sessionAttributeTable = "sessionattribute";

    private PreparedStatement preparedLoadSesHdrSql = null;
    private PreparedStatement preparedLoadSesAttrSql = null;
    private PreparedStatement preparedInsertSesHdrSql = null;
    private PreparedStatement preparedUpdateSesHdrSql = null;
    private PreparedStatement preparedRemoveSesHdrSql = null;
    private PreparedStatement preparedInsertSesAttrSql = null;
    private PreparedStatement preparedUpdateSesAttrSql = null;
    private PreparedStatement preparedRemoveSesAttrSql = null;
    private PreparedStatement preparedRemoveAllSesAttrSql = null;
                
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;


    private String tempAppId = null;

     /** Creates a new instance of HAAttributeStore */
     public HAAttributeStore() {
         info = "S1AS HAAttributeStore/1.0";
         blobSessionTable = "sessionheader";
         //threadName = "HAAttributeStore";
         storeName = "HAAttributeStore";

         long timeout = new Long(timeoutSecs).longValue();
         haErr = new HAErrorManager(timeout, threadName);
         haErrLoad = new HAErrorManager(timeout, threadName);                 
         haErrRemove = new HAErrorManager(timeout, threadName);

         if (_logger == null) {
             _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
         }                  

         /*
         try {
            Logger.setLogWriter(new PrintWriter(
                new FileOutputStream("logfile.out"), true));
            Logger.setLogLevel(Logger.FINE);
         }
     	 catch ( FileNotFoundException e ) {
       		log("Clustra JDBC log file could not be opened");
       	 }
         */
 
    }
	
    /* This load method is always called using a conn from the pool
    in both fg and bg */	
     public synchronized Session load(String id) throws IOException {
         if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("===== In NEW Load -- HAAttributeStore, id is " + id);
         }
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return null;
        }
        if ( id == null ) {
            if (_debug > 0) {
                debug("in load, id is null, returning null");
            }
            Thread.dumpStack();
            return null;
        }             

        // Check to see if it's in our cache first
        //Session session = (Session)sessions.get(id);
        Session session = this.getSessionFromMainStoreCache(id);
        if ( session != null ) {
            if (_debug > 0) {
                debug("Session " + id + " loaded from cache");
            }
            return session;
        }              

        //Session session = null;
        HADBConnectionGroup connGroup = null;
        try {
            connGroup = this.getConnectionsFromPool();
        } catch (IOException ioe) {
            //this means failure to obtain connection
            //we will ignore the exception but return null
            //from the load; protect the thread from crashing
        }
        if(connGroup == null) {
            if (_debug > 0) {
                debug("HAAttributeStore>>load: Failure to obtain connection from pool: returning null");
            }
            ServerConfigLookup config = new ServerConfigLookup();
            String connURL = config.getConnectionURLFromConfig();
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed using connection URL: " + connURL + " -- returning null. Check connection pool configuration.");
            return null;
        }

        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;

        try  {
            session = load(id, internalConn);
            try {
                if (externalConn != null)
                        externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
        }
        catch ( IOException e ) {
            // Calling routines don't print the stack trace :(
            e.printStackTrace();
            throw e;
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {}
            }
        }
        if (_debug > 0) {
            debug("loaded session " + id + " from ===> HAAttributeStore");
        }
        return session;

    } //end load(id)

    public Session load(String id, Connection connection) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in load +++++*** -- HAAttributeStore session id= " + id);
        }
        Session session = null;
        //debug("HAAttributeStore save(sess, connection) : session id = " + session.getIdInternal());	
        try {
            haErrLoad.txStart();
            while ( ! haErrLoad.isTxCompleted() ) {
                try {
                    session = doExecuteLoad(id, connection);
                    haErrLoad.txEnd();
                }
                catch ( SQLException e ) {
                    // close previous statements cached in inst vars
                    closeStatement(preparedLoadSesHdrSql);
                    preparedLoadSesHdrSql = null;
                    closeStatement(preparedLoadSesAttrSql);
                    preparedLoadSesAttrSql = null;                  
                    haErrLoad.checkError(e, connection);
                    if (_debug > 0) {
                            debug("Got a retryable exception from HAAttribute Store: " + e.getMessage());
                    }
                }
            }
        }
        catch(SQLException e) {
		try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 =
                    (IOException) new IOException("Error from HAAttribute Store: " + e.getMessage()).initCause(e);
            throw ex1;
        }
        catch ( HATimeoutException e ) {
            IOException ex1 =
                    (IOException) new IOException("Timeout from HAAttribute Store " + e.getMessage()).initCause(e);
            throw ex1;
        }
        finally {
            releaseForegroundLock(session);
        }
        return session;
    }
        
    public Session doExecuteLoad(String id, Connection connection) throws IOException, SQLException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in doExecuteLoad +++++*** -- HAAttributeStore session id= " + id);              
        }
        Session session = null;

        String shortAppId = null;
        ResultSet rst = null;
        //This load method must always be called with a connection
        //obtained from the connection pool in fg and bg -- therefore 
        //connection cannot be null.
        if (connection == null) {
            throw new IllegalStateException("Got a null Connection here : load() ==> HAAttributeStore");
        }

        /*Try to load session from store, if success, try to load
        each attribute of the loaded session -- the load operation 
        suceeds if both these operations return without any 
        exception.
        If no session exists in store, create a new session
        (in method getSession()), with this id and return the 
        newly created session*/	

        //String loadHdrSql = "SELECT id, valid, maxinactive, lastaccess, appid ,username , ssoid FROM " + sessionHeaderTable + " WHERE id = ?";
        String loadHdrSql = "SELECT id, valid, maxinactive, lastaccess, appid ,username , ssoid FROM " 
            + sessionHeaderTable + " WHERE id = ? AND appid = ?";
        try {           
            //Always prepare a statement out of the conn passed
            preparedLoadSesHdrSql = connection.prepareStatement(loadHdrSql);
            preparedLoadSesHdrSql.setString(1, id);
            preparedLoadSesHdrSql.setString(2, getApplicationId());
            rst = preparedLoadSesHdrSql.executeQuery();

            if ( rst == null || ! rst.next() ) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("No persisted data found for session " + id);
                }
                closeResultSet(rst);
                closeStatement(preparedLoadSesHdrSql);
                preparedLoadSesHdrSql = null;
                connection.rollback();
                return null;
            }                        

            session = getSession(rst);

            String appId = rst.getString(5);
            shortAppId = extractShortApplicationIdFromApplicationId(appId);

            session.setManager(manager);

            /*Close and null out the resultset to use the same 
            variable for loadAttributes */
            closeResultSet(rst);
            rst = null;

            //String loadAttrSql = "SELECT rowid, sessattrdata, attributename FROM " + sessionAttributeTable + " WHERE id = ?";
            String loadAttrSql = "SELECT rowid, sessattrdata, attributename FROM " 
                + sessionAttributeTable + " WHERE id = ? AND appid = ?";

            //Always prepare a statement out of the conn passed
            preparedLoadSesAttrSql = connection.prepareStatement(loadAttrSql);
            preparedLoadSesAttrSql.setString(1, id);
            preparedLoadSesAttrSql.setString(2, getApplicationId());
            rst = preparedLoadSesAttrSql.executeQuery();


            loadAttributes(session,rst);

            //Trying only one single commit
            connection.commit();

            if ( preparedLoadSesHdrSql != null) {
                preparedLoadSesHdrSql.close();
                preparedLoadSesHdrSql = null;
            }
            if ( preparedLoadSesAttrSql != null ) {
                preparedLoadSesAttrSql.close();
                preparedLoadSesAttrSql = null;
            }
        }
        /*
        catch(SQLException e) {
            //This may be rolled up from EITHER getSession()
             //OR loadAttributes()
            try{((Connection)conn).rollback();}catch(SQLException ee){}
            e.printStackTrace();
            throw new IOException("Error from HAAttributeStore: " + e.getMessage());
        }
         */
        catch(ClassNotFoundException cnfe) {
        /*This may be rolled up from loadAttributes() in case
        the class to which the attribute object belongs 
        cannot be resolved */
            cnfe.printStackTrace();
            //throw new IOException("Error from HAAttributeStore: " + cnfe.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + cnfe.getMessage()).initCause(cnfe);                    
            throw ex1;            
        }
        catch(IOException ioe) {
        /*This may be rolled up from loadAttributes() in case
        there is an error in reading the attribute blob using an
        ObjectInputStream */
            ioe.printStackTrace();
            //throw new IOException("Error from HAAttributeStore: " + ioe.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + ioe.getMessage()).initCause(ioe);                    
            throw ex1;                    
        } 
        finally {//Note : connection.close() will be called by calling method
            closeResultSet(rst);
            rst = null;
            //releaseForegroundLock(session);

            if (preparedLoadSesHdrSql != null) {
                try {
                    preparedLoadSesHdrSql.close();
                } catch (SQLException e) {}
                preparedLoadSesHdrSql = null;
            }

            if (preparedLoadSesAttrSql != null) {
                try {
                    preparedLoadSesAttrSql.close();
                } catch (SQLException e) {}
                preparedLoadSesAttrSql = null;
            }
        }

        if( !shortAppId.equals(getShortApplicationId()) ) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Loaded session does not belong in this container");
            }
            ((HAManagerBase)manager).removeSessionFromManagerCache(session);
            return null;
        }               

        /*Reaches here only if there are NO exceptions in all
        the db operations above and session is NOT NULL */
        ((HASession)session).setDirty(false);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Returning session from HAAttributeStore : " + ((StandardSession) session).getIdInternal());
        }
        //sessions.put(session.getIdInternal(), session); //++++MERGE
        this.putSessionInMainStoreCache(session);
        return session;
    }         
                                         
    public void releaseForegroundLock(Session session) {
        if (session == null)
            return;
        //undo the foreground lock that occured during load
        StandardSession stdSess = (StandardSession) session;
        if(stdSess != null && stdSess.isForegroundLocked()) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("in HAAttributeStore>>load before unlockForeground:lock = "
                    + stdSess.getSessionLock());
            }
            //reduce ref count by 1
            stdSess.unlockForeground();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("in HAAttributeStore>>load after unlockForeground:lock = " + stdSess.getSessionLock());
            }
        }
    }

    public void insertAttribute(Session session, String attribute, Connection connection, boolean foreground) throws IOException {
        insertAttributes(session, new String[] {attribute}, connection, foreground);
    }
        
    public void insertAttributes(Session session, String[] attributes, Connection connection, boolean foreground) throws IOException {

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in insertAttributes --HAAttributeStore");
        }

        /*
        String insertAttrSql = "INSERT INTO " + sessionAttributeTable +
        " (" + "rowid, sessattrdata, id, attributename)" +
        " VALUES (?, ?, ?, ?)";
         */
        String insertAttrSql = "INSERT INTO " + sessionAttributeTable +
        " (" + "rowid, sessattrdata, id, attributename, appid)" +
        " VALUES (?, ?, ?, ?, ?)";        
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("insertAttrSql : " + insertAttrSql);
        }

        PreparedStatement preparedInsertSesAttrSql = null;

        try {           
            if (!foreground) {
                if (this.preparedInsertSesAttrSql == null)
                    this.preparedInsertSesAttrSql = connection.prepareStatement(insertAttrSql);
                preparedInsertSesAttrSql = this.preparedInsertSesAttrSql;
            } else {
                preparedInsertSesAttrSql = connection.prepareStatement(insertAttrSql);
            }

            BufferedInputStream in = null;
            //int buflength = 0;
            IntHolder buflength = new IntHolder();
            for (int i=0; i<attributes.length; i++) {
                Object attr = ((StandardSession) session).getAttribute(attributes[i]);
                in = getInputStream(attr, buflength);
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("object buffer length = " + buflength.value);
                    _logger.finest("attributeval=" + ((StandardSession) session).getAttribute(attributes[i]));
                }
                preparedInsertSesAttrSql.setString(1, session.getIdInternal()+":"+ attributes[i]);
                preparedInsertSesAttrSql.setBinaryStream(2, in, buflength.value);
                preparedInsertSesAttrSql.setString(3, session.getIdInternal());
                preparedInsertSesAttrSql.setString(4, attributes[i]);
                preparedInsertSesAttrSql.setString(5, getApplicationId());
                //executeStatement(preparedInsertSesAttrSql, false);
                //LW:7/31/03 remove retry within retry
                preparedInsertSesAttrSql.executeUpdate();

                in.close();
                in = null;		
                ((ModifiedAttributeHASession) session).setAttributeStatePersistent(attributes[i], true);
                ((ModifiedAttributeHASession) session).setAttributeStateDirty(attributes[i], false);
            }
            if (foreground) {
                //connection.commit();
                preparedInsertSesAttrSql.close();
                preparedInsertSesAttrSql = null;
            }
        }
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;                    
        }
        finally {
            if (foreground && preparedInsertSesAttrSql != null) {
                try {
                    preparedInsertSesAttrSql.close();
                    preparedInsertSesAttrSql = null;
                } catch (SQLException se) {}
            }
        } 
    }        	
    
    public void updateAttribute(Session session, String attribute, Connection connection, boolean foreground) throws IOException {
        updateAttributes(session, new String[] {attribute}, connection, foreground);
    }
        
    public void updateAttributes(Session session, String[] attributes, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in updateAttributes --HAAttributeStore");
        }

        /*
        String updateAttrSql = "UPDATE " + sessionAttributeTable + 
        " SET sessattrdata = ?, attributename = ?" +
        " WHERE rowid = ?";
         */
        
        String updateAttrSql = "UPDATE " + sessionAttributeTable + 
        " SET sessattrdata = ?, attributename = ?" +
        " WHERE rowid = ? AND appid = ?";        

        PreparedStatement preparedUpdateSesAttrSql = null;

        try {
            if (!foreground) {
                if (this.preparedUpdateSesAttrSql == null)
                    this.preparedUpdateSesAttrSql = connection.prepareStatement(updateAttrSql);
                preparedUpdateSesAttrSql = this.preparedUpdateSesAttrSql;
            } else {
                preparedUpdateSesAttrSql = connection.prepareStatement(updateAttrSql);
            }
            BufferedInputStream in = null;
            //int buflength = 0;
            IntHolder buflength = new IntHolder();
            for (int i=0; i<attributes.length; i++) {
                in = getInputStream(((StandardSession) session).getAttribute(attributes[i]), buflength);
                preparedUpdateSesAttrSql.setBinaryStream(1, in, buflength.value);
                preparedUpdateSesAttrSql.setString(2, attributes[i]);
                preparedUpdateSesAttrSql.setString(3, session.getIdInternal() + ":" + attributes[i]);
                preparedUpdateSesAttrSql.setString(4, getApplicationId());
                //executeStatement(preparedUpdateSesAttrSql, false);
                //LW:7/31/03 remove retry within retry
                preparedUpdateSesAttrSql.executeUpdate();                                

                ((ModifiedAttributeHASession) session).setAttributeStatePersistent(attributes[i], true);
                ((ModifiedAttributeHASession) session).setAttributeStateDirty(attributes[i], false);
            }

            if (foreground) {
                //connection.commit();
                preparedUpdateSesAttrSql.close();
                preparedUpdateSesAttrSql = null;
            }
        }
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;                    
        }
        finally {
            if (foreground && preparedUpdateSesAttrSql != null) {
                try {
                    preparedUpdateSesAttrSql.close();
                    preparedUpdateSesAttrSql = null;
                } catch (SQLException se) {}
            }
        } 
    }        

    public void removeAttribute(Session session, String attribute, Connection connection, boolean foreground) throws IOException {
        removeAttributes(session, new String[] {attribute}, connection, foreground);
    }
        
    public void removeAttributes(Session session, String[] attributes, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in removeAttributes --HAAttributeStore");
        }

        //String removeAttrSql = "DELETE FROM " + sessionAttributeTable + 
        //        " WHERE rowid = ?";
        String removeAttrSql = "DELETE FROM " + sessionAttributeTable + 
                " WHERE rowid = ? AND appid = ?";        

        PreparedStatement preparedRemoveSesAttrSql = null;

        try {
            if (!foreground) {
                if (this.preparedRemoveSesAttrSql == null)
                    this.preparedRemoveSesAttrSql = connection.prepareStatement(removeAttrSql);
                preparedRemoveSesAttrSql = this.preparedRemoveSesAttrSql;
            } else {
                preparedRemoveSesAttrSql = connection.prepareStatement(removeAttrSql);
            }

            for (int i=0; i<attributes.length; i++) {
                preparedRemoveSesAttrSql.setString(1, session.getIdInternal() + ":" + attributes[i]);
                preparedRemoveSesAttrSql.setString(2, getApplicationId());
                //executeStatement(preparedRemoveSesAttrSql, false);
                //LW:7/31/03 remove retry within retry
                preparedRemoveSesAttrSql.executeUpdate();                                 
            }

            if (foreground) {
                //connection.commit();
                preparedRemoveSesAttrSql.close();
                preparedRemoveSesAttrSql = null;
            }
        }
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;                    
        }
        finally {
            if (foreground && preparedRemoveSesAttrSql != null) {
                try {
                    preparedRemoveSesAttrSql.close();
                    preparedRemoveSesAttrSql = null;
                } catch (SQLException se) {}
            }
        }
    }        
    
    public void removeAllAttributes(String sessionId, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in removeAllAttributes --HAAttributeStore");
        }

        //String removeAllAttrSql = "DELETE FROM " + sessionAttributeTable + 
        //        " WHERE id = ?";
        String removeAllAttrSql = "DELETE FROM " + sessionAttributeTable + 
                " WHERE id = ? AND appid = ?";        

        PreparedStatement preparedRemoveAllSesAttrSql = null;

        try {
            if (!foreground) {
                if (this.preparedRemoveAllSesAttrSql == null)
                    this.preparedRemoveAllSesAttrSql = connection.prepareStatement(removeAllAttrSql);
                preparedRemoveAllSesAttrSql = this.preparedRemoveAllSesAttrSql;
            } else {
                preparedRemoveAllSesAttrSql = connection.prepareStatement(removeAllAttrSql);
            }

            preparedRemoveAllSesAttrSql.setString(1, sessionId);
            preparedRemoveAllSesAttrSql.setString(2, getApplicationId());
            preparedRemoveAllSesAttrSql.executeUpdate();                    

            if (foreground) {
                //connection.commit();
                preparedRemoveAllSesAttrSql.close();
                preparedRemoveAllSesAttrSql = null;
            }
        }
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }
        finally {
            if (foreground && preparedRemoveAllSesAttrSql != null) {
                try {
                    preparedRemoveAllSesAttrSql.close();
                    preparedRemoveAllSesAttrSql = null;
                } catch (SQLException se) {}
            }
        }
    }       
        
    public void removeAttributes(String sessionId, String[] attributes, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in removeAttributes --HAAttributeStore");
        }

        //String removeAttrSql = "DELETE FROM " + sessionAttributeTable + 
        //        " WHERE rowid = ?";
        String removeAttrSql = "DELETE FROM " + sessionAttributeTable + 
                " WHERE rowid = ? AND appid = ?";        

        PreparedStatement preparedRemoveSesAttrSql = null;

        try {
            if (!foreground) {
                if (this.preparedRemoveSesAttrSql == null)
                    this.preparedRemoveSesAttrSql = connection.prepareStatement(removeAttrSql);
                preparedRemoveSesAttrSql = this.preparedRemoveSesAttrSql;
            } else {
                preparedRemoveSesAttrSql = connection.prepareStatement(removeAttrSql);
            }

            for (int i=0; i<attributes.length; i++) {
                preparedRemoveSesAttrSql.setString(1, sessionId + ":" + attributes[i]);
                preparedRemoveSesAttrSql.setString(2, getApplicationId());
                //executeStatement(preparedRemoveSesAttrSql, false);
                //LW:7/31/03 remove retry within retry
                preparedRemoveSesAttrSql.executeUpdate();                                 
            }

            if (foreground) {
                //connection.commit();
                preparedRemoveSesAttrSql.close();
                preparedRemoveSesAttrSql = null;
            }
        }
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }
        finally {
            if (foreground && preparedRemoveSesAttrSql != null) {
                try {
                    preparedRemoveSesAttrSql.close();
                    preparedRemoveSesAttrSql = null;
                } catch (SQLException se) {}
            }
        }
    }                
        
    /**
    * Save the specified Session into this Store.  Any previously saved
    * information for the associated session identifier is replaced.
    *
    * @param session Session to be saved
    *
    * @exception IOException if an input/output error occurs
    */
    public void save(Session session) throws IOException {
        if (_debug > 0) {
            debug("HAAttributeStore save(sess) : session id = " + session.getIdInternal());
        }
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        // begin 6470831 do not save if session is not valid
        if( !((StandardSession)session).getIsValid() ) {
            return;
        }
        // end 6470831
        boolean isMonitoringEnabled = ServerConfigReader.isMonitoringEnabled();
        //added for monitoring
        long startTime = 0L;
        HAManagerBase mgr = (HAManagerBase) this.getManager();
        WebModuleStatistics stats = mgr.getWebModuleStatistics();            
        if(isMonitoringEnabled) {
            startTime = System.currentTimeMillis();
        }
        //end added for monitoring
        Connection conn = this.getConnectionValidated(false);
        //Connection conn = this.getConnection(false);
        this.save(session, conn, false);
        //added for monitoring
        if(isMonitoringEnabled) {            
            long endTime = System.currentTimeMillis();
            stats.processBackgroundSave(endTime - startTime);
        }
        //end added for monitoring
    }

    public void save(Session session, Connection connection, boolean foreground) throws IOException {
        if (_debug > 0) {
            debug("HAAttributeStore save(sess, conn) : session id = " + session.getIdInternal());
        }
        try {
            haErr.txStart();
            while ( ! haErr.isTxCompleted() ) {
                try {
                    doExecuteSave(session, connection, foreground);
                    haErr.txEnd();
                }
                catch ( SQLException e ) {
                    haErr.checkError(e, connection);
                    if (_debug > 0) {
                        debug("Got a retryable exception from HAAttribute Store: " + e.getMessage());
                    }
                }
            }
        }
        catch(SQLException e) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 =
                    (IOException) new IOException("Error from HAAttribute Store: " + e.getMessage()).initCause(e);
            throw ex1;
        }
        catch ( HATimeoutException e ) {
            IOException ex1 =
                    (IOException) new IOException("Timeout from HAAttribute Store " + e.getMessage()).initCause(e);
            throw ex1;
        }

        // Put it in the cache
        //sessions.put(session.getIdInternal(), session);
        this.putSessionInMainStoreCache(session);
        if (_debug > 0) {
            debug("HAAttributeStore : Saved session " + session.getIdInternal() + " into HAAttributeStore");
        }
    }

    public void doExecuteSave(Session session, Connection connection, boolean foreground) throws IOException , SQLException {

        ModifiedAttributeHASession modAttrSession = 
            (ModifiedAttributeHASession) session;
        ArrayList addedAttrs = modAttrSession.getAddedAttributes();
        ArrayList modifiedAttrs = modAttrSession.getModifiedAttributes();
        ArrayList deletedAttrs = modAttrSession.getDeletedAttributes();

        Connection conn = connection;
        if (conn == null && !foreground) { 
            //conn = getConnection(false);
            conn = this.getConnectionValidated(false);
        }

        saveSessionHeader(session, conn, foreground);
        //conn.commit();
        String[] addedAttrNames = new String[addedAttrs.size()];
        for (int i=0; i<addedAttrs.size(); i++) { 
            addedAttrNames[i] = (String) addedAttrs.get(i);
        }
        insertAttributes(session, addedAttrNames, conn, foreground);
        String[] modifiedAttrNames = new String[modifiedAttrs.size()];
        for (int i=0; i<modifiedAttrs.size(); i++) { 
            modifiedAttrNames[i] = (String) modifiedAttrs.get(i);
        }
        updateAttributes(session, modifiedAttrNames, conn, foreground);
        String[] deletedAttrNames = new String[deletedAttrs.size()];
        for (int i=0; i<deletedAttrs.size(); i++) {
            deletedAttrNames[i] = (String) deletedAttrs.get(i);
        }
        removeAttributes(session, deletedAttrNames, conn, foreground);

        conn.commit();
        modAttrSession.resetAttributeState();
    }
               
    protected String[] getSessionAttributeNames(String id) throws IOException {
        ModifiedAttributeHASession sess = null;
        try {
            //Manager mgr = this.getManager();
            //sess = (ModifiedAttributeHASession) mgr.findSession(id);
            sess = (ModifiedAttributeHASession)load(id);
        } catch (IOException ex) {
            _logger.warning("HAAttributeStore>>getSessionAttributeNames failed load threw ex - remove aborted.");
            throw ex;
        }

        if(sess == null) {
            _logger.warning("HAAttributeStore>>getSessionAttributeNames failed load returned null - remove aborted.");
            throw new IOException("HAAttributeStore>>getSessionAttributeNames failed - remove aborted.");
        }

        ArrayList attrNames = new ArrayList();
        if(sess != null) {
            Enumeration en = sess.privateGetAttributeList();
            while(en.hasMoreElements()) {
                String nextAttrName = (String) en.nextElement();
                attrNames.add(nextAttrName);
            }
        }
        String[] template = new String[attrNames.size()];
        String[] result = (String[])attrNames.toArray(template);
        return result;
    }
        
    protected String[] getSessionAttributeNames(Session inSess) {
        ModifiedAttributeHASession sess = (ModifiedAttributeHASession) inSess;
        ArrayList attrNames = new ArrayList();
        if(sess != null) {
            Enumeration en = sess.privateGetAttributeList();
            while(en.hasMoreElements()) {
                String nextAttrName = (String) en.nextElement();
                attrNames.add(nextAttrName);
            }
        }
        String[] template = new String[attrNames.size()];
        String[] result = (String[])attrNames.toArray(template);
        if(result.length == 0) {
            if (_debug > 0) {
                _logger.warning("HAAttributeStore>>getSessionAttributeNames returning empty String[]");
            }
        }
        return result;
    }
    
    /**
     * Remove all Sessions from this Store.
     * Note: this is non-aggregate form of clear
     */
    public void clear() throws IOException  {
        if (_debug > 0) {
            debug("in clear");
        }

        //delete sessions for this app
        // Clear out the cache too
        sessions = new BaseCache();
        sessions.init(_maxBaseCacheSize, _loadFactor, null);

        String [] keys = keysSynchronized();
        for(int i=0; i<keys.length; i++) {
            //remove(keys[i]);
            removeSynchronized(keys[i]);
        } 

    }    
    
    /**
     * Called by our background reaper thread to check if Sessions
     * saved in our store are subject of being expired. If so expire
     * the Session and remove it from the Store.
     * Note: this is non-aggregate form of this method
     *
     */
    public void processExpires() {
        //System.out.println("IN new HAAttributeStore>>processExpires");
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        } 
        long timeNow = System.currentTimeMillis();
        String[] keys = null;

        //taking out this check because this method is run
        //now from manager thread
        /*
        if(!started)
            return;
         */

        try {
            keys = expiredKeysSynchronized();
            //System.out.println("HAAttributeStore>>processExpires: expiredKeys size = " + keys.length);
        } catch (IOException e) {
            log (e.toString());
            e.printStackTrace();
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            try {
                //remove(keys[i]);
                removeSynchronized(keys[i]);
            } catch (IOException ex) {}
        }
    }
    
    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store for this container that are eligible
     * to be expired. If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */  
    public synchronized String[] expiredKeysSynchronized() throws IOException  {
        //debug("in expiredKeys");
	return expiredKeys(blobSessionTable);
    }    
    
    //This is new version of new doRemove method
    public void doRemove(String id) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in remove --HAAttributeStore : session id=" + id);
        }
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        } 
        if ( id == null )  {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("In remove, got a null id");
            }
            return;
        }
        HADBConnectionGroup connGroup = null;
        try {
            connGroup = this.getConnectionsFromPool(true);
        } catch (IOException ex) {
            //this means failure to obtain connection
            //we will ignore the exception log it and return null
            //from the load; protect the thread from crashing
            Object[] params = { "HAStore>>doRemove" };
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure1", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure2", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure3", params);            
        }
        if(connGroup == null) {
            if (_debug > 0) {
                debug("HAStore>>remove: Failure to obtain connection from pool");
            }
            ServerConfigLookup config = new ServerConfigLookup();
            String connURL = config.getConnectionURLFromConfig();
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed using connection URL: " + connURL + " -- returning null. Check connection pool configuration.");
            return;
        }
        
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;
        
        try {
            doExecuteRemove(id, internalConn);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;                    
        }               
        catch(SQLException e) {
            //try{internalConn.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store doRemove: " + e.getMessage()).initCause(e);
            throw ex1;            
        } finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (java.sql.SQLException ex) {}
            }
            externalConn = null;            
        }                               

        // remove it from the cache
        sessions.remove(id);
        this.removeSessionFromMainStoreCache(id);
    }
    
    //This is new version of new doExecuteRemove method
    public void doExecuteRemove(String id, Connection connection) throws IOException, SQLException {

        try {
            haErrRemove.txStart();
            while ( ! haErrRemove.isTxCompleted() ) {
                try {
                    doExecuteRemove(id, connection, true);
                    haErrRemove.txEnd();
                }
                catch ( SQLException e ) {
                    haErrRemove.checkError(e, connection);
                    if (_debug > 0) {
                        debug("Got a retryable exception from HAAttribute Store: " + e.getMessage());
                    }
                }
            }
        }
        catch(SQLException e) {
            /* we just rollback & re-throw the SQLException and its caught above
            IOException ex1 =
                (IOException) new IOException("Error from HAAttribute Store: " + e.getMessage()).initCause(e);
            throw ex1;
             */
            try{connection.rollback();}catch(SQLException ee){}
            throw e;
        }
        catch ( HATimeoutException e ) {
            IOException ex1 =
                (IOException) new IOException("Timeout from HAAttribute Store: " + e.getMessage()).initCause(e);
            throw ex1;
        }

        // remove it from the cache
        sessions.remove(id);
    }    
     
    /*
    public synchronized void removeSynchronized(String id) throws IOException {
        doRemove(id);
    }
     */ 
    
    /**
     * The original synchronized version of the remove method
     * called from the singleton store which must use 
     * synchronized version
     *
     * @param id session id to remove
     * @exception IOException if an input/output error occurred
     */         
    public synchronized void removeSynchronized(String id) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in remove --HAAttributeStore : session id=" + id);
        }
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }  
        if ( id == null )  {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("In remove, got a null id");
            }
            return;
        }

        //try to load session; if it fails or does not exist simply return
        /* removed because it causes infinite loop for expired sessions
        ModifiedAttributeHASession sess = null;
        try {
            sess = (ModifiedAttributeHASession)load(id);
        } catch (IOException ex) {
            _logger.warning("HAAttributeStore>>remove: load of id: " + id + " returned null - remove aborted. Persistent session may not exist.");
        }

        if(sess == null) {
            return;
        }
         *end removed code
         */

        try {
            haErrRemove.txStart();
            while ( ! haErrRemove.isTxCompleted() ) {
                try {
                    //doExecuteRemove(sess);
                    doExecuteRemove(id);
                    haErrRemove.txEnd();
                }
                catch ( SQLException e ) {
                    haErrRemove.checkError(e, conn);
                    if (_debug > 0) {
                            debug("Got a retryable exception from HAAttribute Store: " + e.getMessage());
                    }
                }
            }
        }
        catch(SQLException e) {
		try{conn.rollback();}catch(SQLException ee){}
            IOException ex1 =
                (IOException) new IOException("Error from HAAttribute Store: " + e.getMessage()).initCause(e);
            throw ex1;
        }
        catch ( HATimeoutException e ) {
            IOException ex1 =
                (IOException) new IOException("Timeout from HAAttribute Store: " + e.getMessage()).initCause(e);
            throw ex1;
        }

        // remove it from the cache
        sessions.remove(id);
        this.removeSessionFromMainStoreCache(id);
    }
    
    public void doExecuteRemove(String id) throws IOException, SQLException {

        //String[] sessionAttrNames = this.getSessionAttributeNames(id);

        Connection conn = getConnection(false);
        this.removeAllAttributes(id, conn, false);

        //String removeHdrSql = "DELETE FROM "+ sessionHeaderTable + " WHERE id = ?";
        String removeHdrSql = "DELETE FROM "+ sessionHeaderTable 
            + " WHERE id = ? AND appid = ?";
        try {
            if (preparedRemoveSesHdrSql == null) {
                preparedRemoveSesHdrSql = conn.prepareStatement(removeHdrSql);
            }

            preparedRemoveSesHdrSql.setString(1, id);
            preparedRemoveSesHdrSql.setString(2, getApplicationId());
            //executeStatement(preparedRemoveSesHdrSql, false);
            //LW:7/31/03 remove retry within retry
            preparedRemoveSesHdrSql.executeUpdate();                 
            ((Connection)conn).commit();
        }
        catch(SQLException e) {
            /*
            try{((Connection)conn).rollback();}catch(SQLException ee){}
            //throw new IOException("Error in HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;
            */
            throw e;                 
        }
    }        
    
    public void doExecuteRemove(String id, Connection connection, boolean foreground) throws IOException, SQLException {

        Connection conn = connection;
        if (conn == null && !foreground) { 
            conn = this.getConnectionValidated(false);
        }
        this.removeAllAttributes(id, conn, foreground); 
        PreparedStatement preparedRemoveSesHdrSql = null;
        String removeHdrSql = "DELETE FROM "+ sessionHeaderTable 
            + " WHERE id = ? AND appid = ?";
        try {           
            if (!foreground) {
                if (this.preparedRemoveSesHdrSql == null)
                    this.preparedRemoveSesHdrSql = connection.prepareStatement(removeHdrSql);
                preparedRemoveSesHdrSql = this.preparedRemoveSesHdrSql ;
            } else {
                preparedRemoveSesHdrSql = connection.prepareStatement(removeHdrSql);
            }

            preparedRemoveSesHdrSql.setString(1, id);
            preparedRemoveSesHdrSql.setString(2, getApplicationId());
            //executeStatement(preparedRemoveSesHdrSql, false);
            //LW:7/31/03 remove retry within retry
            preparedRemoveSesHdrSql.executeUpdate();                 
            ((Connection)conn).commit();
            if (foreground && preparedRemoveSesHdrSql != null) {
                try {
                    preparedRemoveSesHdrSql.close();
                    preparedRemoveSesHdrSql = null;
                } catch (SQLException se) {}
            }
        }
        catch(SQLException e) {
            /*
            try{((Connection)conn).rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore doExecuteRemove: " + e.getMessage()).initCause(e);                    
            throw ex1;
            */
            try{((Connection)conn).rollback();}catch(SQLException ee){}
            throw e;                 
        } finally {
            if (foreground && preparedRemoveSesHdrSql != null) {
                try {
                    preparedRemoveSesHdrSql.close();
                    preparedRemoveSesHdrSql = null;
                } catch (SQLException se) {}
            }
        }        
    }       
        
    public void doExecuteRemove(Session sess) throws IOException, SQLException {

        String[] sessionAttrNames = this.getSessionAttributeNames(sess);

        HADBConnectionGroup connGroup = null;
        try {
            connGroup = this.getConnectionsFromPool();
        } catch (IOException ex) {
            //this means failure to obtain connection
            //we will ignore the exception but return null
            //from the load; protect the thread from crashing
        }
        if(connGroup == null) {
            if (_debug > 0) {
                debug("HAAttributeStore>>remove: Failure to obtain connection from pool");
            }
            ServerConfigLookup config = new ServerConfigLookup();
            String connURL = config.getConnectionURLFromConfig();
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed using connection URL: " + connURL + " -- returning null. Check connection pool configuration.");
            return;
        }

        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn; 
        //Connection conn = internalConn;
        //we are getting connection from pool instead
        //Connection conn = getConnection(false);
        this.removeAttributes(sess.getIdInternal(), sessionAttrNames, internalConn, true);

        /*
        LobConnection lobConn = (LobConnection)conn;
        String removeAttrSql = "DELETE FROM "+ sessionAttributeTable + " WHERE id = ?";
        try {
                if (preparedRemoveSesAttrSql == null) {
                        preparedRemoveSesAttrSql = lobConn.prepareLobStatement(removeAttrSql, lob);
                }

                preparedRemoveSesAttrSql.setString(1, id);
                executeStatement(preparedRemoveSesAttrSql, false);
                ((Connection)lobConn).commit();
        }
        catch(SQLException e) {
                try{((Connection)lobConn).rollback();}catch(SQLException ee){}
                throw new IOException("Error in HAAttributeStore: " + e.getMessage());
        }
         */


        //String removeHdrSql = "DELETE FROM "+ sessionHeaderTable + " WHERE id = ?";
        String removeHdrSql = "DELETE FROM "+ sessionHeaderTable 
            + " WHERE id = ? AND appid = ?";
        try {
            preparedRemoveSesHdrSql = internalConn.prepareStatement(removeHdrSql);

            /*
            if (preparedRemoveSesHdrSql == null) {
                preparedRemoveSesHdrSql = conn.prepareStatement(removeHdrSql);
            }
             */

            preparedRemoveSesHdrSql.setString(1, sess.getIdInternal());
            preparedRemoveSesHdrSql.setString(2, getApplicationId());
            //executeStatement(preparedRemoveSesHdrSql, false);
            //LW:7/31/03 remove retry within retry
            preparedRemoveSesHdrSql.executeUpdate();
            internalConn.commit();
            //((Connection)conn).commit();

            preparedRemoveSesHdrSql.close();
            preparedRemoveSesHdrSql = null;
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;                                 

        }
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){}
            try{internalConn.rollback();}catch(SQLException ee){}
            //throw new IOException("Error in HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }                
        finally {       
            if (preparedRemoveSesHdrSql != null) {
                try {
                  preparedRemoveSesHdrSql.close();
                } catch (SQLException e) {}
                preparedRemoveSesHdrSql = null;
            }                
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {}
            }
            //undo the foreground lock that occured during load
            /*
            StandardSession stdSess = (StandardSession) session;
            if(stdSess != null && stdSess.isForegroundLocked()) {
                _logger.finest("in HAAttributeStore>>doExecuteRemove before unlockForeground:lock = " + stdSess.getSessionLock());
                //reduce ref count by 1
                stdSess.unlockForeground();
                _logger.finest("in HAAttributeStore>>doExecuteRemove after unlockForeground:lock = " + stdSess.getSessionLock());
            } 
             */     
        }               

    }    

    public void valveSave(Session session) throws IOException {

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("XXXXXXX In valveSave -- HAAttributeStore, id is " + session.getIdInternal());
        }
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        // begin 6470831 do not save if session is not valid
        if( !((StandardSession)session).getIsValid() ) {
            return;
        }
        // end 6470831 
        HASession sess = (HASession) session;
        boolean previousDirtyFlag = sess.isDirty();


        HADBConnectionGroup connGroup = this.getConnectionsFromPool();
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;

        try  {
            save(session, internalConn, true);
            sess.setDirty(false);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
        }
        catch ( IOException e ) {
            sess.setDirty(previousDirtyFlag);
            // Calling routines don't print the stack trace :(
            e.printStackTrace();
            throw e;
        }
        finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {}
            }
        }
        if (_debug > 0) {
            debug("Saved session " + session.getIdInternal() + " into ===> HAAttributeStore");
        }

        // Put it in the cache
        //not for valveSave
        //sessions.put(session.getIdInternal(), session);
        //this.putSessionInMainStoreCache(session);

    } //end valveSave

    protected Session getSession(ResultSet rst) throws SQLException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getSession -- HAAttributeStore");
        }
        Session _session =  ((HAManagerBase)manager).createSession();
        String tmpStr = null;
        java.security.Principal pal=null;
        Container container = manager.getContainer();
        String id;

        _session.setId(tmpStr=rst.getString("id"));
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getSession -- HAAttributeStore : id =" + tmpStr);
        }
        tmpStr = rst.getString("valid");
        _session.setValid(tmpStr.equals("1") ? true:false);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getSession -- HAAttributeStore: valid = " + tmpStr);
        }
        _session.setMaxInactiveInterval(rst.getInt("maxinactive"));
        ((StandardSession)_session).setLastAccessedTime(rst.getLong("lastaccess"));

        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getSession -- HAAttributeStore: maxinactive = " + _session.getMaxInactiveInterval());
            _logger.finest("in getSession -- HAAttributeStore: lastAccessedTime = " + ((StandardSession)_session).getLastAccessedTimeInternal());
        }
        //ignore appid -- only required in save
        rst.getString("appid");

        //Get the username from database
        String username = rst.getString("username");
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HAAttributeStore.getSession()  id="+tmpStr+"  username ="+username+";");
        }
        if((username !=null) && (!username.equals(""))){
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Username retrived from DB is "+username);
            }
            pal = ((com.sun.web.security.RealmAdapter)container.getRealm()).createFailOveredPrincipal(username);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("principal created using username  "+pal);
            }
        }
        //--SRI
        //MERGE
        String ssoId = rst.getString("ssoid");
        if((ssoId !=null) && (!ssoId.equals("")))
            associate(ssoId, _session);
        //__MERGE	

        //Set remaining fields
        _session.setAuthType(null);
        _session.setPrincipal(pal);
        _session.setNew(false);


        return _session;
    }
        
    protected void loadAttributes(Session session, ResultSet rst)
            throws SQLException, ClassNotFoundException, IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in loadAttributes -- HAAttributeStore : session id=" + session.getIdInternal());
        }

        BufferedInputStream bis = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        Container container = manager.getContainer();

        if (container != null) {
                 loader = container.getLoader();
        }

        if (loader != null) {
                 classLoader = loader.getClassLoader();
        }

        String thisAttrName = null;
        String rowid = null;
        Object thisAttrVal = null;
        boolean zeroLengthBlob = false;
        while (rst.next()) { 
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN loadAttributes:while loop");
            }
            rowid = rst.getString(1);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("rowid retrieved======" + rowid);
            }
            Blob blob = rst.getBlob(2);
            thisAttrName = rst.getString(3);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Attr retrieved======" + thisAttrName);
            }

            if(blob.length() == 0) {
                zeroLengthBlob = true;
                ois = null;
            } else {
                zeroLengthBlob = false;
                bis = new BufferedInputStream(blob.getBinaryStream());

                //HERCULES: FIXME reverting back
                //need to re-examine EJBUtils, etc.
                //Bug 4832603 : EJB Reference Failover
                /* was this
                if (classLoader != null) {
                        ois = new CustomObjectInputStream(bis, classLoader);
                }
                else {
                        ois = new ObjectInputStream(bis);
                }
                 */
                // Bug 4853613 : Third param = enableResolveObject, Fourth param = failStatefulSession
                //ois = EJBUtils.getInputStream(bis, classLoader, true, true);
                //end - Bug 4832603

                if (classLoader != null) {
                    IOUtilsCaller caller = this.getWebUtilsCaller();
                    if(caller != null) {
                        try {
                            ois = caller.createObjectInputStream(bis, true, classLoader);
                        } catch (Exception ex) {}
                    }
                }
                if (ois == null) {
                    ois = new ObjectInputStream(bis); 
                }                            
            }

            if(ois != null) { //start if
                try {
                    thisAttrVal = ois.readObject();
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Setting Attribute: " + thisAttrName);
                    }
                    ((ModifiedAttributeHASession) session).setAttribute(thisAttrName, thisAttrVal);
                    ((ModifiedAttributeHASession) session).setAttributeStatePersistent(thisAttrName, true);
                    ((ModifiedAttributeHASession) session).setAttributeStateDirty(thisAttrName, false);
                }
                catch (Exception e) {
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("in loadAttr==>CATCH " + e.getMessage());
                    }
                }
                finally {
                    if (ois != null) {
                        try {
                            ois.close();
                            bis = null;
                        }
                        catch (IOException e) {
                        }
                    }
                }
            } else {
                //if we have an attribute with a value that was zero-length
                //null is impossible value so use zero-length String
                if(zeroLengthBlob) {
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Setting Attribute: " + thisAttrName + " to empty String");
                    }
                    ((ModifiedAttributeHASession) session).setAttribute(thisAttrName, "");
                    ((ModifiedAttributeHASession) session).setAttributeStatePersistent(thisAttrName, true);
                    ((ModifiedAttributeHASession) session).setAttributeStateDirty(thisAttrName, false);                            
                }
            } //end if
        } //end while 
    }       

    protected void saveSessionHeader(Session session, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in saveSessionHeader --HAAttributeStore");
        }
        boolean sessionIsPersistent = false;
        boolean sessionInStoreOk = false;
        try {
            if(!foreground) {
                sessionIsPersistent = sessionInStore(session);
            } else {
                sessionIsPersistent = sessionInStore(session, connection, sessionHeaderTable);
            }
            sessionInStoreOk = true;
        } catch (IOException ex) {
            sessionInStoreOk = false;
        }

	  /* assume false if failure - fix later
        if(!sessionInStoreOk) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAAttributeStore-save: sessionInStore failed: aborting saveSessionHeader ");
            }            
            return;
        }
        */ 

        //if (sessionInStore(session, connection, sessionHeaderTable))
        if(sessionIsPersistent) {
            updateSessionHeader(session, connection, foreground);
        } else {
            insertSessionHeader(session, connection, foreground);
        }
    }
    
    protected void insertSessionHeaderNew(Session session, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in insertSessionHeader --HAAttributeStore");
        }

        String insertHdrSql = "INSERT into " + sessionHeaderTable  
                        + " (id, valid, maxinactive, lastaccess,"  
                        + " appid,username, ssoid)" 
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedInsertSesHdrSql = null;

        try {
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {         
                try {
                    if (!foreground) {
                        if (this.preparedInsertSesHdrSql == null)
                            this.preparedInsertSesHdrSql = connection.prepareStatement(insertHdrSql);
                        preparedInsertSesHdrSql = this.preparedInsertSesHdrSql;
                    } else {
                        preparedInsertSesHdrSql = connection.prepareStatement(insertHdrSql);
                    }

                    preparedInsertSesHdrSql.setString(1, session.getIdInternal());
                    preparedInsertSesHdrSql.setString(2, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedInsertSesHdrSql.setInt(3, session.getMaxInactiveInterval());
                    preparedInsertSesHdrSql.setLong(4, ((StandardSession)session).getLastAccessedTimeInternal());
                    preparedInsertSesHdrSql.setString(5, getApplicationId());


                    //added column for userName --SRI
                    if(session.getPrincipal() !=null){
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("Attribute session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedInsertSesHdrSql.setString(6, session.getPrincipal().getName()); 
                    } else {
                        preparedInsertSesHdrSql.setString(6, ""); //FIXME: it should be SQL NULL
                        if (_debug > 0) {
                            debug("Attribute session.getPrincipal() ="+session.getPrincipal());
                        }
                    }
                    //end added column       --SRI

                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)
                        ssoId = "";
                    preparedInsertSesHdrSql.setString(7, ssoId);
                    //__MERGE

                    //executeStatement(preparedInsertSesHdrSql, false);
                    //LW:7/31/03 remove retry within retry
                    preparedInsertSesHdrSql.executeUpdate();                         

                    //((Connection)connection).commit();
                    if (foreground) {
                        preparedInsertSesHdrSql.close();
                        preparedInsertSesHdrSql = null;
                    }
                    haErr.txEnd();
                } catch (SQLException e) {
                    if (foreground) {
                        closeStatement(preparedInsertSesHdrSql);
                        preparedInsertSesHdrSql = null;
                    }                    
                    haErr.checkError(e, connection);
                }                    
            }                    
        }
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){}
            try{connection.rollback();}catch(SQLException ee){}
            e.printStackTrace();
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore insertSessionHeader: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAAttributeStore insertSessionHeader: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (foreground && preparedInsertSesHdrSql != null) {
                try {
                    preparedInsertSesHdrSql.close();
                    preparedInsertSesHdrSql = null;
                } catch (SQLException se) {}
            }
        }

    }    

    protected void insertSessionHeader(Session session, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in insertSessionHeader --HAAttributeStore");
        }

        String insertHdrSql = "INSERT into " + sessionHeaderTable  
                        + " (id, valid, maxinactive, lastaccess,"  
                        + " appid,username, ssoid)" 
                        + " VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedInsertSesHdrSql = null;

        try {            
            if (!foreground) {
                if (this.preparedInsertSesHdrSql == null)
                    this.preparedInsertSesHdrSql = connection.prepareStatement(insertHdrSql);
                preparedInsertSesHdrSql = this.preparedInsertSesHdrSql;
            } else {
                preparedInsertSesHdrSql = connection.prepareStatement(insertHdrSql);
            }

            preparedInsertSesHdrSql.setString(1, session.getIdInternal());
            preparedInsertSesHdrSql.setString(2, ((StandardSession)session).getIsValid()?"1":"0");
            preparedInsertSesHdrSql.setInt(3, session.getMaxInactiveInterval());
            preparedInsertSesHdrSql.setLong(4, ((StandardSession)session).getLastAccessedTimeInternal());
            preparedInsertSesHdrSql.setString(5, getApplicationId());


            //added column for userName --SRI
            if(session.getPrincipal() !=null){
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Attribute session.getPrincipal().getName() ="+session.getPrincipal().getName());
                }
                preparedInsertSesHdrSql.setString(6, session.getPrincipal().getName()); 
            } else {
                preparedInsertSesHdrSql.setString(6, ""); //FIXME: it should be SQL NULL
                if (_debug > 0) {
                    debug("Attribute session.getPrincipal() ="+session.getPrincipal());
                }
            }
            //end added column       --SRI

            //MERGE
            String ssoId = ((HASession)session).getSsoId();
            if (ssoId == null)
                ssoId = "";
            preparedInsertSesHdrSql.setString(7, ssoId);
            //__MERGE

            //executeStatement(preparedInsertSesHdrSql, false);
            //LW:7/31/03 remove retry within retry
            preparedInsertSesHdrSql.executeUpdate();                         

            //((Connection)connection).commit();
            if (foreground) {
                preparedInsertSesHdrSql.close();
                preparedInsertSesHdrSql = null;
            }	
        }
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){}
            try{connection.rollback();}catch(SQLException ee){}
            e.printStackTrace();
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore insertSessionHeader: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }
        finally {
            if (foreground && preparedInsertSesHdrSql != null) {
                try {
                    preparedInsertSesHdrSql.close();
                    preparedInsertSesHdrSql = null;
                } catch (SQLException se) {}
            }
        }

    }


    protected void updateSessionHeaderNew(Session session, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in updateSessionHeader --HAAttributeStore");
        }

        /*
        String updateHdrSql = "UPDATE " + sessionHeaderTable +
        " SET valid = ?, maxinactive = ?, lastaccess = ?, appid = ?," +
        " username = ?, ssoid = ? WHERE id = ?";
         */
        
        String updateHdrSql = "UPDATE " + sessionHeaderTable +
        " SET valid = ?, maxinactive = ?, lastaccess = ?," +
        " username = ?, ssoid = ? WHERE id = ? AND appid = ?";        

        PreparedStatement preparedUpdateSesHdrSql = null;
        try {
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {        
                try {
                    if (!foreground) {
                        if (this.preparedUpdateSesHdrSql == null)
                            this.preparedUpdateSesHdrSql = connection.prepareStatement(updateHdrSql);
                        preparedUpdateSesHdrSql = this.preparedUpdateSesHdrSql;
                    } else {
                        preparedUpdateSesHdrSql = connection.prepareStatement(updateHdrSql);
                    }

                    preparedUpdateSesHdrSql.setString(1, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedUpdateSesHdrSql.setInt(2, session.getMaxInactiveInterval());
                    preparedUpdateSesHdrSql.setLong(3, ((StandardSession)session).getLastAccessedTimeInternal());
                    //preparedUpdateSesHdrSql.setString(4, getApplicationId());

                    //added column for userName --SRI
                    if(session.getPrincipal() !=null){
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("Attribute session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedUpdateSesHdrSql.setString(4, session.getPrincipal().getName()); 
                    } else {
                        preparedUpdateSesHdrSql.setString(4, ""); //FIXME: it should be SQL NULL
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("Attribute session.getPrincipal() ="+session.getPrincipal());
                        }
                    }
                    //end added column       --SRI
                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)
                        ssoId = "";
                    preparedUpdateSesHdrSql.setString(5, ssoId);
                    //__MERGE


                    preparedUpdateSesHdrSql.setString(6, session.getIdInternal());
                    preparedUpdateSesHdrSql.setString(7, getApplicationId());
                    //executeStatement(preparedUpdateSesHdrSql, false);
                    //LW:7/31/03 remove retry within retry
                    preparedUpdateSesHdrSql.executeUpdate();                          

                    //((Connection)connection).commit();
                    if (foreground) {
                        preparedUpdateSesHdrSql.close();
                        preparedUpdateSesHdrSql = null;
                    }
                    haErr.txEnd();
                } catch (SQLException e) {
                    haErr.checkError(e, connection);
                }                    
            }                    
        }
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){}
            try{connection.rollback();}catch(SQLException ee){}
            e.printStackTrace();
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore updateSessionHeader: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAAttributeStore updateSessionHeader: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (foreground && preparedUpdateSesHdrSql != null) {
                try {
                    preparedUpdateSesHdrSql.close();
                    preparedUpdateSesHdrSql = null;
                } catch (SQLException se) {}
            }
        }
    }
    
    protected void updateSessionHeader(Session session, Connection connection, boolean foreground) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in updateSessionHeader --HAAttributeStore");
        }

        /*
        String updateHdrSql = "UPDATE " + sessionHeaderTable +
        " SET valid = ?, maxinactive = ?, lastaccess = ?, appid = ?," +
        " username = ?, ssoid = ? WHERE id = ?";
         */
        
        String updateHdrSql = "UPDATE " + sessionHeaderTable +
        " SET valid = ?, maxinactive = ?, lastaccess = ?," +
        " username = ?, ssoid = ? WHERE id = ? AND appid = ?";        

        PreparedStatement preparedUpdateSesHdrSql = null;

        try {            
            if (!foreground) {
                if (this.preparedUpdateSesHdrSql == null)
                    this.preparedUpdateSesHdrSql = connection.prepareStatement(updateHdrSql);
                preparedUpdateSesHdrSql = this.preparedUpdateSesHdrSql;
            } else {
                preparedUpdateSesHdrSql = connection.prepareStatement(updateHdrSql);
            }

            preparedUpdateSesHdrSql.setString(1, ((StandardSession)session).getIsValid()?"1":"0");
            preparedUpdateSesHdrSql.setInt(2, session.getMaxInactiveInterval());
            preparedUpdateSesHdrSql.setLong(3, ((StandardSession)session).getLastAccessedTimeInternal());
            //preparedUpdateSesHdrSql.setString(4, getApplicationId());

            //added column for userName --SRI
            if(session.getPrincipal() !=null){
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Attribute session.getPrincipal().getName() ="+session.getPrincipal().getName());
                }
                preparedUpdateSesHdrSql.setString(4, session.getPrincipal().getName()); 
            } else {
                preparedUpdateSesHdrSql.setString(4, ""); //FIXME: it should be SQL NULL
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Attribute session.getPrincipal() ="+session.getPrincipal());
                }
            }
            //end added column       --SRI
            //MERGE
            String ssoId = ((HASession)session).getSsoId();
            if (ssoId == null)
                ssoId = "";
            preparedUpdateSesHdrSql.setString(5, ssoId);
            //__MERGE


            preparedUpdateSesHdrSql.setString(6, session.getIdInternal());
            preparedUpdateSesHdrSql.setString(7, getApplicationId());
            //executeStatement(preparedUpdateSesHdrSql, false);
            //LW:7/31/03 remove retry within retry
            preparedUpdateSesHdrSql.executeUpdate();                          

            //((Connection)connection).commit();
            if (foreground) {
                preparedUpdateSesHdrSql.close();
                preparedUpdateSesHdrSql = null;
            }
        }
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){}
            try{connection.rollback();}catch(SQLException ee){}
            e.printStackTrace();
            //throw new IOException("Error from HAAttributeStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from HAAttributeStore updateSessionHeader: " + e.getMessage()).initCause(e);                    
            throw ex1;                 
        }
        finally {
            if (foreground && preparedUpdateSesHdrSql != null) {
                try {
                    preparedUpdateSesHdrSql.close();
                    preparedUpdateSesHdrSql = null;
                } catch (SQLException se) {}
            }
        }
    }    

    protected BufferedInputStream getInputStream(Object attribute, IntHolder length) throws IOException {
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        ObjectOutputStream oos = null;
        BufferedInputStream in = null;
        IOUtilsCaller utilsCaller = null;

        try {
            bos = new ByteArrayOutputStream();
            //HERCULES: FIXME reverting back
            //need to re-examine EJBUtils, etc.
            //Bug 4832603 : EJB Reference Failover
            /* This was the buggy code - this next line
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             */
            //oos = EJBUtils.getOutputStream(new BufferedOutputStream(bos), true);
            //end - Bug 4832603 

            if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                try {
                    oos = utilsCaller.createObjectOutputStream(new BufferedOutputStream(bos), true);
                } catch (Exception ex) {}
            }
            //use normal ObjectOutputStream if there is a failure during stream creation
            if(oos == null) {
                oos = new ObjectOutputStream(new BufferedOutputStream(bos)); 
            }                     

            oos.writeObject(attribute);
            oos.close();
            oos = null;

            byte[] obs = bos.toByteArray();
            bis = new ByteArrayInputStream(obs, 0, obs.length);
            in = new BufferedInputStream(bis, obs.length);
            length.value = obs.length;
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }

            if (bis != null) {
                bis.close();
            }
        }
        return in;
    }

    public int getSize() throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in getSize -- HAAttributeStore");
        }
        return getSize(sessionHeaderTable);
     }

    public String[] keys() throws IOException  {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in keys --HAAttributeStore");
        }
        return keys(sessionHeaderTable);
    }
    
    public synchronized String[] keysSynchronized() throws IOException  {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("in keys --HAAttributeStore");
        }
        return keys(sessionHeaderTable);
    }    
    
    protected void closeStatements() {
        PreparedStatement[] statements = 
           {preparedLoadSesHdrSql, preparedLoadSesAttrSql, preparedInsertSesHdrSql,
            preparedUpdateSesHdrSql, preparedRemoveSesHdrSql, preparedInsertSesAttrSql,
            preparedUpdateSesAttrSql, preparedRemoveSesAttrSql, 
            preparedRemoveAllSesAttrSql};        
        for(int i=0; i<statements.length; i++) {
            PreparedStatement nextStatement = 
                (PreparedStatement) statements[i];
            closeStatement(nextStatement);
        }
        super.closeStatements(); 
    }
    
    protected void clearStatementReferences() {
               
        preparedLoadSesHdrSql = null;
        preparedLoadSesAttrSql = null;
        preparedInsertSesHdrSql = null;
        preparedUpdateSesHdrSql = null;
        preparedRemoveSesHdrSql = null;
        preparedInsertSesAttrSql = null;
        preparedUpdateSesAttrSql = null;
        preparedRemoveSesAttrSql = null;
        preparedRemoveAllSesAttrSql = null;
        super.clearStatementReferences(); 
    }    

}
