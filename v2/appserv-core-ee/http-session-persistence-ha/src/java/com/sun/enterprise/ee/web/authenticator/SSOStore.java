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
 * SSOStore.java
 *
 * Created on January 9, 2003, 11:39 AM
 */

package com.sun.enterprise.ee.web.authenticator;

/**
 *
 * @author  Sridhar Satuloori
 */
import java.io.*;
import java.sql.*;
import java.util.*;
import java.security.Principal;
import javax.naming.*;
import javax.sql.*;

//import com.sun.hadb.jdbc.*;
//import com.sun.hadb.comm.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import com.sun.enterprise.ee.web.sessmgmt.*;
import com.sun.enterprise.security.web.SingleSignOnEntry;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.Switch;
import com.sun.enterprise.InvocationManager;


/*** This is not a Session Store, it is only a store for SSO records.
Extends from HAStore only to take advantage of getConnection framework. ***/

public class SSOStore implements StorePoolElement, SSOStorePoolElement{

    private static final String BASIC_AUTH = "BASIC";
    private static final String FORM_AUTH = "FORM";
    private static final String DIGEST_AUTH = "DIGEST";
    private static final String CLIENT_CERT_AUTH = "CLIENT-CERT";
    
    protected Container container = null;
    protected HASingleSignOn sso = null;    
    
    private boolean debug=false; //set this to true for debug info

    protected final String singleSignOnTable="singlesignon";
    String sessionHeaderTable = "sessionheader"; 
    String  blobSessionTable ="blobsessions";

    private PreparedStatement preparedLoadSSOSql = null;
    private PreparedStatement preparedInsertSSOSql = null;
    private PreparedStatement preparedUpdateSSOSql = null;
    private PreparedStatement preparedExistsSql = null;
    private PreparedStatement preparedRemoveSSOSql = null;
    private PreparedStatement preparedRemoveInactiveSql = null;
    private PreparedStatement preparedUpdateSessionTableSql = null;

    protected long timeout = 5 * 60;
    protected HAErrorManager haErr = null;
    protected HAErrorManager haErrAssociate = null;
    protected HAErrorManager haErrRemove = null;
    protected HAErrorManager haErrRemoveInactive = null;
    protected HAErrorManager haErrLoad = null;
    protected HAErrorManager haErrExists = null;    
    protected HAErrorManager haErrInsert = null;
    protected HAErrorManager haErrUpdate = null;

    protected String connString = null;
    protected Connection conn = null;
    protected String driverName = "com.sun.hadb.jdbc.Driver";

    /**
    * The helper class used to obtain connections; both
    * both cached and from the connection pool
    */
    protected ConnectionUtil connectionUtil = null;



    /** Creates a new instance of SSOStore */
    public SSOStore() {
	haErr = new HAErrorManager(timeout, "ssoThread");
        haErrAssociate = new HAErrorManager(timeout, "ssoThread");
        haErrRemove = new HAErrorManager(timeout, "ssoThread");
        haErrRemoveInactive = new HAErrorManager(timeout, "ssoThread");
        haErrLoad = new HAErrorManager(timeout, "ssoThread");
        haErrExists = new HAErrorManager(timeout, "ssoThread");
        haErrInsert = new HAErrorManager(timeout, "ssoThread");
        haErrUpdate = new HAErrorManager(timeout, "ssoThread");
    }
    
    /** Creates a new instance of SSOStore */
    public void setContainer(Container container) {
	this.container = container;
	debug("   container:    "+container);
    }
    
    /** set the sso */
    public void setSingleSignOn(HASingleSignOn haSSO) {
	this.sso = haSSO;
	debug("   sso:    "+sso);        
    }     

    /**
    * Return the instance of ConnectionUtil for this Store.
    */
    protected ConnectionUtil getConnectionUtil() {
        if(connectionUtil == null) {
            //connectionUtil = new ConnectionUtil(this.container);
            connectionUtil = new ConnectionUtil(this.container, this.sso);
        }
        return connectionUtil;
    }


    /** Return an array containing the session identifiers of all Sessions
     * currently saved in this Store.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] keys() throws IOException {
        String[] retValue;
        
        retValue = null;
        return retValue;
    }
    
    /** Load and return the Session associated with the specified session
     * identifier from this Store, without removing it.  If there is no
     * such stored Session, return <code>null</code>.
     *
     * @param ssoId Session identifier of the session to load
     *
     * @exception ClassNotFoundException if a deserialization error occurs
     * @exception IOException if an input/output error occurs
     */
    public SingleSignOnEntry loadSSO(String ssoId) throws IOException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return null;
        }  
            HADBConnectionGroup connGroup = this.getConnectionsFromPool();
            Connection internalConn = connGroup._internalConn;
            Connection externalConn = connGroup._externalConn;

            SingleSignOnEntry _ssoEntry = null;

        try {
            _ssoEntry = loadSSO(ssoId, internalConn);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
        } catch ( IOException e ) {
            e.printStackTrace();
            throw e;
        } finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }

        debug("Loaded SSO " + ssoId );

        return _ssoEntry;

    }
    
    public synchronized SingleSignOnEntry loadSSO(String ssoId, Connection connection) throws IOException {
        
	if ( ssoId == null ) {
               debug("in load -- SSOStore, ssoId is null, returning null") ;
               Thread.dumpStack();
               return null;
        }
	
        long lastAccess = 0;
        String authType = null;
        String userName = null;
        String realmName = null;

        ResultSet rst = null;

        String loadSSOSql = "SELECT ssoid, lastaccess, authType, userName " +
                        "FROM " + singleSignOnTable + " WHERE ssoid = ?";

	try { 
            haErrLoad.txStart();
            while(!haErrLoad.isTxCompleted()) {
                try {                    
                    preparedLoadSSOSql = connection.prepareStatement(loadSSOSql);
                    preparedLoadSSOSql.setString(1, ssoId);
                    rst = preparedLoadSSOSql.executeQuery();

                    if(rst.next()){
                        rst.getString("ssoid");			
                        lastAccess = rst.getLong("lastaccess");
                        String authTypeAndRealmName = rst.getString("authType");
                        /*
                         * Parse value of authType column into it auth type and
                         * realm name components, see 6266183.
                         */
                        if (authTypeAndRealmName.startsWith(BASIC_AUTH)) {
                            authType = BASIC_AUTH;
                            realmName = authTypeAndRealmName.substring(
                                BASIC_AUTH.length());
                        } else if (authTypeAndRealmName.startsWith(FORM_AUTH)) {
                            authType = FORM_AUTH;
                            realmName = authTypeAndRealmName.substring(
                                FORM_AUTH.length());
                        } else if (authTypeAndRealmName.startsWith(DIGEST_AUTH)) {
                            authType = DIGEST_AUTH;
                            realmName = authTypeAndRealmName.substring(
                                DIGEST_AUTH.length());
                        } else if (authTypeAndRealmName.startsWith(CLIENT_CERT_AUTH)) {
                            authType = CLIENT_CERT_AUTH;
                            realmName = authTypeAndRealmName.substring(
                                CLIENT_CERT_AUTH.length());
                        }
                        userName = rst.getString("userName");
                    } else {
                        connection.commit();
                        if(preparedLoadSSOSql != null) {
                            preparedLoadSSOSql.close();
                            preparedLoadSSOSql = null;
                        }
                        return null; //the SSO object doent exist in the DB -sri
                    }

                    closeResultSet(rst);
                    connection.commit();
                    preparedLoadSSOSql.close();
                    preparedLoadSSOSql = null;                    
                    
                    haErrLoad.txEnd();
                } catch(SQLException e) {
                    closeStatement(preparedLoadSSOSql);
                    preparedLoadSSOSql = null;
                    haErrLoad.checkError(e, connection);                    
                }
            } //end while
        } //end outer try  
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore loadSSO: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore loadSSO: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedLoadSSOSql != null) {
                try {
                    preparedLoadSSOSql.close();
                    preparedLoadSSOSql = null;
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }

        Principal principal=null;

        return new HASingleSignOnEntry(principal, authType, userName, null,
                                       realmName);
    }        
    
    /** Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param ssoId ssoId to be stored
     * @param ssoEntry sso entry to be stored
     *
     * @exception IOException if an input/output error occurs
     */ 
    public void save(String ssoId,  SingleSignOnEntry ssoEntry) throws IOException {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        HADBConnectionGroup connGroup = this.getConnectionsFromPool();
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;

        try {
            save(ssoId, ssoEntry, internalConn);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
        } catch ( IOException e ) {
            e.printStackTrace();
            throw e;
        } finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }

        debug("Saved SSO " + ssoId + " into SSOStore");

    }

    public void savePrevious(String ssoId,  SingleSignOnEntry ssoEntry, Connection connection) throws IOException {

        //Connection conn = connection;

        String existsSql = "SELECT ssoid FROM " + singleSignOnTable + " WHERE ssoid = ?";
        ResultSet rs = null;
        boolean found = false;

        try {
            preparedExistsSql = connection.prepareStatement(existsSql);

            preparedExistsSql.setString(1, ssoId);
            rs = executeStatement(preparedExistsSql, true);

            if ( rs == null || ! rs.next() )  {
                found = false;
            }
            else {
                found = true;
            }

            connection.commit();
            preparedExistsSql.close();
            preparedExistsSql = null;

            if (!found)
                insertSSO(ssoId, ssoEntry, connection); 
            else
                updateSSO(ssoId, ssoEntry, connection);

        } catch(SQLException sqe){
                sqe.printStackTrace();
        } finally {
            try {
                if(preparedExistsSql != null) {
                    preparedExistsSql.close();
                    preparedExistsSql = null;
                }
            } catch (SQLException se) {}
        }
	
    }
    
    public void save(String ssoId, SingleSignOnEntry ssoEntry, Connection connection) throws IOException {

        boolean ssoEntryIsPersistent = false;
        boolean ssoEntryInStoreOk = false;
        try {
            ssoEntryIsPersistent = ssoEntryInStore(ssoId, connection);
            ssoEntryInStoreOk = true;
        } catch (IOException ex) {
            ssoEntryInStoreOk = false;           
        }     
        if(!ssoEntryInStoreOk) {           
            return;
        }
        try {
            if(ssoEntryIsPersistent) {
                //_logger.finest("SsoEntry is in store");
                updateSSO(ssoId, ssoEntry, connection);
            }
            else {
                //_logger.finest("SsoEntry NOT in store");
                insertSSO(ssoId, ssoEntry, connection);
            }
        } catch(IOException e){
            //stack trace created in calling method
            //e.printStackTrace();
            throw e;
        }
	
    }    
    
    /**
    * Determine whether a sso entry exists in the SSO Store
    * called by save
    * @param ssoId
    * @param connection
    */
    public boolean ssoEntryInStore(String ssoId, Connection connection) throws IOException  {   

        String existsSql = "SELECT ssoid FROM " + singleSignOnTable 
                + " WHERE ssoid = ?";
        ResultSet rs = null;
        boolean found = false;
        
        try {
            haErrExists.txStart();
            while ( ! haErrExists.isTxCompleted() ) {                
                try {       
                    preparedExistsSql = connection.prepareStatement(existsSql);
                    preparedExistsSql.setString(1, ssoId);
                    rs = preparedExistsSql.executeQuery();

                    if ( rs == null || ! rs.next() )  {
                        found = false;
                    }
                    else {
                        found = true;
                    }
                    ((Connection)connection).commit();
                    preparedExistsSql.close();
                    preparedExistsSql = null;
                    haErrExists.txEnd();                
                } catch (SQLException e) {
                    closeStatement(preparedExistsSql);
                    preparedExistsSql = null;
                    //check for re-tryables
                    haErrExists.checkError(e, ((Connection)connection) );
                } 
            }
        } catch (SQLException e) {            
            try{((Connection)connection).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore-ssoEntryInStore: " + e.getMessage()).initCause(e);
            throw ex1;            
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore-ssoEntryInStore: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally  {
            closeResultSet(rs);
            if (preparedExistsSql != null) {
                try {
                    preparedExistsSql.close();
                } catch (SQLException e) {}
                preparedExistsSql = null;
            }
        }

        return found;
    }     

    public void insertSSO(String ssoId, SingleSignOnEntry ssoEntry, Connection connection) throws IOException {

        debug("in insertSSO --SSOStore");

        String insertSSOSql = "INSERT into " + singleSignOnTable + "("
                        + "ssoid, lastaccess, authType, userName) "
                        + "VALUES (?, ?, ?, ?)";

        debug("insertSSO sso id '" + ssoId
              + "' for user '" + (ssoEntry.principal).getName()
              + "' with auth type '" + ssoEntry.authType
              + "' and realm name '" + ssoEntry.realmName + "'");
        
	try { 
            haErrInsert.txStart();
            while(!haErrInsert.isTxCompleted()) {
                try {                                       
                    preparedInsertSSOSql = connection.prepareStatement(insertSSOSql);

                    preparedInsertSSOSql.setString(1, ssoId);
                    preparedInsertSSOSql.setLong(2, ssoEntry.lastAccessTime);
                    /*
                     * Concatenate auth type and realm name, and store result in
                     * authType column. See 6266183
                     */
                    String authTypeAndRealmName = ssoEntry.authType + ssoEntry.realmName;
                    preparedInsertSSOSql.setString(3, authTypeAndRealmName);
                    preparedInsertSSOSql.setString(4, (ssoEntry.principal).getName());
                    preparedInsertSSOSql.executeUpdate();
                    connection.commit();
                    preparedInsertSSOSql.close();
                    preparedInsertSSOSql = null;                    
                    
                    haErrInsert.txEnd();
                } catch(SQLException e) {
                    closeStatement(preparedInsertSSOSql);
                    preparedInsertSSOSql = null;
                    haErrInsert.checkError(e, connection);                    
                }
            } //end while
        } //end outer try        
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore insertSSO: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore insertSSO: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedInsertSSOSql != null) {
                try {
                    preparedInsertSSOSql.close();
                    preparedInsertSSOSql = null;
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }

    }

    public void updateSSO(String ssoId, SingleSignOnEntry ssoEntry, Connection connection) throws IOException {

        debug("in updateSSO --SSOStore");

        String updateSSOSql = "UPDATE " + singleSignOnTable +
        " SET lastaccess = ?, authType = ?, userName = ?" +
        " WHERE ssoid = ?";

        debug("updateSSO sso id '" + ssoId
              + "' for user '" + (ssoEntry.principal).getName()
              + "' with auth type '" + ssoEntry.authType
              + "' and realm name '" + ssoEntry.realmName + "'");
        
	try { 
            haErrUpdate.txStart();
            while(!haErrUpdate.isTxCompleted()) {
                try {                                                           
                    preparedUpdateSSOSql = connection.prepareStatement(updateSSOSql);
                    preparedUpdateSSOSql.setLong(1, ssoEntry.lastAccessTime);
                    /*
                     * Concatenate auth type and realm name, and store result in
                     * authType column. See 6266183
                     */
                    String authTypeAndRealmName = ssoEntry.authType + ssoEntry.realmName;
                    preparedUpdateSSOSql.setString(2, authTypeAndRealmName);
                    preparedUpdateSSOSql.setString(3, (ssoEntry.principal).getName());
                    preparedUpdateSSOSql.setString(4, ssoId);
                    preparedUpdateSSOSql.executeUpdate();

                    connection.commit();
                    preparedUpdateSSOSql.close();
                    preparedUpdateSSOSql = null;                    
                    
                    haErrUpdate.txEnd();
                } catch(SQLException e) {
                    closeStatement(preparedUpdateSSOSql);
                    preparedUpdateSSOSql = null;                    
                    haErrUpdate.checkError(e, connection);                    
                }
            } //end while
        } //end outer try        
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore updateSSO: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore updateSSO: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedUpdateSSOSql != null) {
                try {
                    preparedUpdateSSOSql.close();
                    preparedUpdateSSOSql = null;
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
    }
    
    public void updateLastAccessTime(String ssoId, long lat) throws IOException {
        //This method is called by background thread
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        Connection conn = getConnection(false);

        updateLastAccessTime(ssoId,lat,conn);
    }

    public void updateLastAccessTime(String ssoId, long lat, Connection conn) throws IOException {

        debug("in updateLastAccessTime --SSOStore");

        String updateLatSql = "UPDATE " + singleSignOnTable +
        " SET lastaccess = ?"+" WHERE ssoid = ?";

        try {
            if (preparedUpdateSSOSql == null)
                preparedUpdateSSOSql = conn.prepareStatement(updateLatSql);

            preparedUpdateSSOSql.setLong(1, lat);
			debug("updateLastAccessTime sso id '" + ssoId + "' with  LastAccessTime "+ lat+" ");
            preparedUpdateSSOSql.setString(2, ssoId);
            executeStatement(preparedUpdateSSOSql, false);

            conn.commit();
        }
        catch (SQLException e) {
            closeStatement(preparedUpdateSSOSql);
            preparedUpdateSSOSql = null;
            try{conn.rollback();}catch(SQLException ee){ee.printStackTrace();}
            e.printStackTrace();
            //throw new IOException("Error from SSO Store updateLastAccessTime : " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from SSO Store updateLastAccessTime : " + e.getMessage()).initCause(e);                    
            throw ex1;             
        }
	/*finally {
            try {
                preparedUpdateSSOSql.close();
                preparedUpdateSSOSql = null;
            } catch (SQLException se) {}
	}*/
    }
    
    public void associate(StandardSession session, String ssoId) throws IOException {
	HADBConnectionGroup connGroup = this.getConnectionsFromPool();
	Connection internalConn = connGroup._internalConn;
	Connection externalConn = connGroup._externalConn;

	try {
            associate(session, ssoId, internalConn);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
	} catch ( IOException e ) {
            e.printStackTrace();
            throw e;
	} finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {ex.printStackTrace();}
            }
	}

	debug("Associated SSO " + ssoId + " with session " + ((StandardSession)session).getId());

     }

    public void associate(StandardSession session, String ssoId, Connection connection ) throws IOException {
        // this method updates the session table with the ssoID column.
        //or add this method in session which calls this method in respective store.
	String updateSql = null;
	String sessionTable = null;

	if((session instanceof com.sun.enterprise.ee.web.sessmgmt.FullHASession) || (session instanceof com.sun.enterprise.ee.web.sessmgmt.ModifiedHASession))
            sessionTable = blobSessionTable;
	else if ( session instanceof com.sun.enterprise.ee.web.sessmgmt.ModifiedAttributeHASession  )
            sessionTable = sessionHeaderTable;
	else
	return;

	updateSql = "UPDATE " +  sessionTable + " SET ssoid = ? WHERE id = ?";

	//Connection conn = connection;

	try { 
            haErrAssociate.txStart();
            while(!haErrAssociate.isTxCompleted()) {
                try {
                    preparedUpdateSessionTableSql = connection.prepareStatement(updateSql);
                    preparedUpdateSessionTableSql.setString(1, ssoId);	
                    preparedUpdateSessionTableSql.setString(2, ((StandardSession)session).getId());
                    preparedUpdateSessionTableSql.executeUpdate();

                    connection.commit();	
                    preparedUpdateSessionTableSql.close();
                    preparedUpdateSessionTableSql = null;
                    debug("Inside SSOStore.associate() : ....completed");
                    haErrAssociate.txEnd();
                } catch(SQLException e) {
                    closeStatement(preparedUpdateSessionTableSql);
                    preparedUpdateSessionTableSql = null;                  
                    haErrAssociate.checkError(e, connection);                    
                }
            } //end while
        } //end outer try
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore associate: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore associate: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedUpdateSessionTableSql != null) {
                try {
                    preparedUpdateSessionTableSql.close();
                    preparedUpdateSessionTableSql = null;
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
    }  
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param ssoid Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public synchronized void remove(String ssoId) throws IOException  {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        HADBConnectionGroup connGroup = this.getConnectionsFromPool();
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;

        try {
            remove(ssoId, internalConn);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
        } catch ( IOException e ) {
            e.printStackTrace();
            throw e;
        } finally {
            if (externalConn != null) {
            try {
                externalConn.close();
            } catch (Exception ex) {ex.printStackTrace();}
            }
        }

        debug("Removed SSO " + ssoId + " from SSOStore " );

    }

    public void remove(String ssoId, Connection connection) throws IOException  {
        debug("in remove -- SSOStore");

        if ( ssoId == null )  {
            debug("In remove -- SSOStore, got a null id");
            return;
        }

        String removeSSOSql = "DELETE FROM "+ singleSignOnTable + " WHERE ssoid = ?";

	try { 
            haErrRemove.txStart();
            while(!haErrRemove.isTxCompleted()) {
                try {
                    preparedRemoveSSOSql = connection.prepareStatement(removeSSOSql);
                    preparedRemoveSSOSql.setString(1, ssoId);
                    preparedRemoveSSOSql.executeUpdate();
                    
                    ((Connection)connection).commit();
                    preparedRemoveSSOSql.close();
                    preparedRemoveSSOSql = null;                                        
                    haErrRemove.txEnd();
                } catch(SQLException e) {
                    if(preparedRemoveSSOSql != null) {
                        try {
                            preparedRemoveSSOSql.close();
                        } catch (Exception ex) {ex.printStackTrace();}
                        preparedRemoveSSOSql = null;
                    }
                    haErrRemove.checkError(e, connection);                    
                }
            } //end while
        } //end outer try        
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore remove: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore remove: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedRemoveSSOSql != null) {
                try {
                    preparedRemoveSSOSql.close();                    
                } catch (Exception ex) {ex.printStackTrace();}
                preparedRemoveSSOSql = null;
            }
        }
    }   
  
    public void removeInActiveSessions(String ssoId) throws IOException  {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }
        HADBConnectionGroup connGroup = this.getConnectionsFromPool();
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;

        try {
            removeInActiveSessions(ssoId, internalConn);
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
        } catch ( IOException e ) {
            e.printStackTrace();
            throw e;
        } finally {
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }

        debug("Removed inactive sessions for SSO " + ssoId );

    }

    public void removeInActiveSessions(String ssoId, Connection connection) 
        throws IOException  {
        //or add this method in session which calls this method in respective store.
        debug("in removeInActiveSessions");

        if ( ssoId == null )  {
            debug("In removeInActiveSessions, got a null ssoId");
            return;
        }

        String removeInactiveSql1 = "DELETE FROM "+ blobSessionTable + " WHERE ssoid = ?";
        String removeInactiveSql2 = "DELETE FROM "+ sessionHeaderTable + " WHERE ssoid = ?";

	try { 
            haErrRemoveInactive.txStart();
            while(!haErrRemoveInactive.isTxCompleted()) {
                try {                    
                    preparedRemoveInactiveSql = connection.prepareStatement(removeInactiveSql1);

                    preparedRemoveInactiveSql.setString(1, ssoId);
                    preparedRemoveInactiveSql.executeUpdate();
                    ((Connection)connection).commit();
                    preparedRemoveInactiveSql.close();
                    preparedRemoveInactiveSql = null;                    
                    
                    haErrRemoveInactive.txEnd();
                } catch(SQLException e) {
                    closeStatement(preparedRemoveInactiveSql);
                    preparedRemoveInactiveSql = null;
                    haErrRemoveInactive.checkError(e, connection);                    
                }
            } //end while
        } //end outer try
        catch ( SQLException e ) {
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore removeInActiveSessions: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from SSOStore removeInActiveSessions: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedRemoveInactiveSql != null) {
                try {
                    preparedRemoveInactiveSql.close();
                    preparedRemoveInactiveSql = null;
                } catch (Exception ex) {ex.printStackTrace();}
            }
        }
    }  

    protected ResultSet executeStatement(PreparedStatement stmt, boolean isQuery) throws IOException {
        ResultSet rst = null;

        try {
            haErr.txStart();            
            while ( ! haErr.isTxCompleted() ) {
                try {                    
                    if ( isQuery ) {
                        rst = stmt.executeQuery();
                    } else  {
                        stmt.executeUpdate();
                    }
                    haErr.txEnd();
                } catch ( SQLException e ) {
                    haErr.checkError(e, conn);
                    debug("Got a retryable exception from HA Store: " + e.getMessage());
                    haErr.printRetryableMessage(e);
                }                    
            }
        } catch(SQLException e) {
            //throw new IOException("Error from SSOStore: " + e.getMessage());
            try{conn.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore: " + e.getMessage()).initCause(e);                    
            throw ex1;            
        } catch ( HATimeoutException e ) {
            //throw new IOException("Timeout from SSOStore");
            IOException ex1 = 
                (IOException) new IOException("Timeout from SSOStore " + e.getMessage()).initCause(e);                    
            throw ex1;            
        }

        return rst;
    }

    protected ResultSet executeStatementLastGood(PreparedStatement stmt, boolean isQuery) throws IOException {
        ResultSet rst = null;

        try {
            haErr.txStart();

            try {
                while ( ! haErr.isTxCompleted() ) {
                  if ( isQuery ) {
                      rst = stmt.executeQuery();
                  }
                  else  {
                      stmt.executeUpdate();
                  }
                  haErr.txEnd();
                }
            } catch ( SQLException e ) {
                haErr.checkError(e, conn);
                debug("Got a retryable exception from HA Store: " + e.getMessage());
                haErr.printRetryableMessage(e);
            }
        } catch(SQLException e) {
            //throw new IOException("Error from SSOStore: " + e.getMessage());
            IOException ex1 = 
                (IOException) new IOException("Error from SSOStore: " + e.getMessage()).initCause(e);                    
            throw ex1;            
        } catch ( HATimeoutException e ) {
            //throw new IOException("Timeout from SSOStore");
            IOException ex1 = 
                (IOException) new IOException("Timeout from SSOStore: " + e.getMessage()).initCause(e);                    
            throw ex1;             
        }
        return rst;
    }
 
    /**
    * Helper routine that closes a result set
    */
    protected void closeResultSet(ResultSet rst) {
        try {
            if (rst != null) {
              rst.close();
            }
        } catch(SQLException e) {}
    }

    protected String user = null;
    protected String password = null;

    protected Connection getConnection(boolean autoCommit) throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        //get returned connection into cached connection conn
        conn = util.getConnection(autoCommit);
        return conn;
        //return util.getConnection(autoCommit);
    }

    protected Connection getConnection() throws IOException {
        return getConnection(true);
    }


    public void debug(String s){
        if(debug)
        System.out.println("SSOStore: "+s);
    }


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
            if ( conn != null && (! conn.isClosed()) ) {
                conn.close();
                conn = null;
            }
        } catch (java.sql.SQLException ex) {}
    }
  
    protected void closeStatements() {
        PreparedStatement[] statements =
            {preparedLoadSSOSql, preparedInsertSSOSql, preparedUpdateSSOSql,
             preparedExistsSql, preparedRemoveSSOSql, preparedRemoveInactiveSql,
             preparedUpdateSessionTableSql};
        for(int i=0; i<statements.length; i++) {
            PreparedStatement nextStatement =
                (PreparedStatement) statements[i];
            closeStatement(nextStatement);
        }
    }

    protected void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
        try {
            stmt.close();
        } catch (java.sql.SQLException ex) {}
        }
    }

    protected HADBConnectionGroup getConnectionsFromPool() throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        return util.getConnectionsFromPool();
    }
  
}
