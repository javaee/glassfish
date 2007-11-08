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
 * HAStore.java
 *
 * Created on April 22, 2002, 5:25 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

/**
* This class extends the abstract StoreBase class defined as part of 
* Apache Tomcat.  The HAStore class uses the HADB data store to provide
* HA support for session state under Tomcat.
*/

import org.apache.catalina.session.*;
import org.apache.catalina.util.*;
//import org.apache.catalina.authenticator.*;
import org.apache.catalina.*;
import java.sql.*;
import java.io.*;
import java.util.*;
import javax.naming.*; 
import javax.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

//import com.sun.hadb.jdbc.*;

import com.sun.enterprise.web.ServerConfigLookup;

import com.sun.enterprise.ComponentInvocation;
import com.sun.enterprise.Switch;
import com.sun.enterprise.InvocationManager;

import com.sun.enterprise.ee.web.authenticator.*;
import com.sun.enterprise.security.web.SingleSignOn;
import com.sun.enterprise.security.web.SingleSignOnEntry;
import com.sun.enterprise.ee.web.initialization.ServerConfigReader;
import com.sun.appserv.util.cache.BaseCache;
//Bug 4832603 : EJB Reference Failover
import com.sun.ejb.EJBUtils;
//end - Bug 4832603

/**
 *
 * @author  lwhite
 * @version 
 */
public class HAStore extends StoreBase implements HAStorePoolElement {
    
    protected static int _maxBaseCacheSize = 4096;
    protected static float _loadFactor = 0.75f;
    
    /**
     * Name to register for the background thread.
     */
    protected String threadName = "HAStore";
        
    /**
    * Our write-through cache of session objects
    */
    protected BaseCache sessions = new BaseCache();

    /**
    * get the sessions cache
    */ 
    public BaseCache getSessions() {
        return sessions;
    }
  
    /**
    * set the sessions cache
    * @param sesstable
    */ 
    public void setSessions(BaseCache sesstable) {
        sessions = sesstable;
    }
    
    public BaseCache getMainStoreCache() {
        PersistentManagerBase pmb = (PersistentManagerBase)this.getManager();
        HAStore myStore = (HAStore)pmb.getStore();
        return myStore.getSessions();
    }
    
    protected void putSessionInMainStoreCache(Session session) { 
        if(session == null) {
            return;
        }
        BaseCache theSessions = this.getMainStoreCache();
        theSessions.put(session.getIdInternal(), session);
    }
    
    protected Session getSessionFromMainStoreCache(String id) {    
        BaseCache theSessions = this.getMainStoreCache();
        return (Session)theSessions.get(id);
    }    
    
    /**
    * remove a session from the store cache
    * Hercules: added method
    */    
    public void removeSessionFromMainStoreCache(String id) {
        BaseCache theSessions = this.getMainStoreCache();    
        theSessions.remove(id);     
    }    
  
    /**
    * Our write-through cache of session objects
    */
    //protected Hashtable sessions = new Hashtable();

    /**
    * get the sessions cache
    */ 
    /*
    public Hashtable getSessions() {
      return sessions;
    }
    */

  
    /**
    * set the sessions cache
    * @param sesstable
    */ 
    /*
    public void setSessions(Hashtable sesstable) {
      sessions = sesstable;
    }
    **

  
    /**
    * The logger we use to log messages
    */
    //Logger logger;
   
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;
    
    /** chunk size for delete operations
     */
    private static int CHUNK_SIZE = 100;     
    
    /**
     * Controls the verbosity of the web container subsystem's debug messages.
     *
     * This value is non-zero only when the level is one of FINE, FINER
     * or FINEST.
     * 
     */
    protected int _debug = 0; 
    
    /**
     * The current level of logging verbosity for this object.
     */
    protected Level _logLevel = null;
    
    /**
     * get the utility class used to call into services from IOUtils
     */
    protected IOUtilsCaller getWebUtilsCaller() {
        if(webUtilsCaller == null) {
            WebIOUtilsFactory factory = new WebIOUtilsFactory();
            webUtilsCaller = factory.createWebIOUtil();            
        }
        return webUtilsCaller;
    }    
    
    /**
     * Set _debug flag and _logLevel based on the log level.
     */
    protected void setLogLevel() {
        Level level = _logger.getLevel();
        _logLevel = level;

        // Determine the appropriate value our debug level
        if (level.equals(Level.FINE))
            _debug = 1;
        else if (level.equals(Level.FINER))
            _debug = 2;
        else if (level.equals(Level.FINEST))
            _debug = 5;
        else
            _debug = 0;
    }    
    
    
    static
	{
            checkSessionCacheProperties();
	} 
    
    protected static boolean checkSessionCacheProperties() {
        boolean result = false;
	try
        {
            Properties props = System.getProperties();
            String cacheSize=props.getProperty("HTTP_SESSION_CACHE_MAX_BASE_CACHE_SIZE");
            if(null!=cacheSize) {
                _maxBaseCacheSize = (new Integer (cacheSize).intValue());
            }  
            String loadFactor=props.getProperty("HTTP_SESSION_CACHE_MAX_BASE_LOAD_FACTOR");
            if(null!=loadFactor) {
                _loadFactor = (new Float (loadFactor).floatValue());
            }
            /*
            System.out.println("_maxBaseCacheSize=" + _maxBaseCacheSize);
            System.out.println("_loadFactor=" + _loadFactor);
             */
        } catch(Exception e)
        {
            //do nothing accept defaults
        }
        return result;
    } 
    
    /**
     * A utility class used to call into services from IOUtils
     */
    private IOUtilsCaller webUtilsCaller = null;     
  
    /**
    * The table storing session data
    */
    protected String blobSessionTable = "blobsessions";  
  
    /**
    * The name of the HA store JDBC driver 
    */
    protected String driverName = "com.sun.hadb.jdbc.Driver";
  
    /**
    * The number of seconds to wait before timing out a transaction
    * Default is 5 minutes.
    */
    protected String timeoutSecs = new Long(5 * 60).toString();
    
    /**
    * The number of seconds to wait before timing out a transaction
    * Default here is 2 minutes. (trial use by clear)
    */    
    protected String timeoutSecs2 = new Long(2 * 60).toString();    

    /**
    * The database connection.
    */
    protected Connection conn = null; 
  
    /**
    * The helper class used to manage retryable errors from the HA store
    */
    protected HAErrorManager haErr = null;
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the load method
    */
    protected HAErrorManager haErrLoad = null;  
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the remove method
    */
    protected HAErrorManager haErrRemove = null;
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the processExpires method
    */
    protected HAErrorManager haErrRemoveExpired = null;    
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the clear method
    */
    protected HAErrorManager haErrClear = null;
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the keys method
    */
    protected HAErrorManager haErrKeys = null;    
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the getSize method
    */
    protected HAErrorManager haErrSize = null;    
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the sessionInStore method used for save
    */
    protected HAErrorManager haErrExists = null;  
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the sessionInStore method used for valveSave
    */
    protected HAErrorManager haErrExists2 = null;
    
    /**
    * The helper class used to manage retryable errors from the HA store
    * dedicated to the expiredKeys method
    */
    protected HAErrorManager haErrExpiredKeys = null;    
  
    /**
    * The helper class used to obtain connections; both
    * both cached and from the connection pool
    */
    protected ConnectionUtil connectionUtil = null;  
  
    /**
    * Various cached statements that we can reuse...
    */
    private PreparedStatement preparedKeysSql = null;
    private PreparedStatement preparedExpiredKeysSql = null;
    private PreparedStatement preparedRemoveExpiredKeysSql = null;
    private PreparedStatement preparedSizeSql = null;
    private PreparedStatement preparedDualSql = null;
    private PreparedStatement preparedLoadSql = null;  
    private PreparedStatement preparedInsertSql = null;
    private PreparedStatement preparedRemoveSql = null;
    private PreparedStatement preparedClearSql = null;
    private PreparedStatement preparedUpdateSql = null;
    /**
    * The prepared statement used by the HA store
    * dedicated to the sessionInStore method used for save
    */
    private PreparedStatement preparedExistsSql = null;
    /**
    * The prepared statement used by the HA store
    * dedicated to the sessionInStore method used for valveSave
    */
    private PreparedStatement preparedExistsSql2 = null;    
    private PreparedStatement preparedUpdateNoSessionSql = null;
  

    /**
    * Return the cluster id for this Store as defined in server.xml.
    */  
    protected String getClusterIdFromConfig() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getClusterIdFromConfig();
    }

    /**
    * The cluster id
    */  
    protected String clusterId = null;
  
    /**
    * Return the cluster id for this Store
    */ 
    protected String getClusterId() {
        if(clusterId == null)
            clusterId = getClusterIdFromConfig();
        return clusterId;
    }

    /**
    * Return the instance of ConnectionUtil for this Store.
    */   
    protected ConnectionUtil getConnectionUtil() {
        if(connectionUtil == null) {
            //connectionUtil = new ConnectionUtil(this.getManager().getContainer());
            connectionUtil = 
              new ConnectionUtil(this.getManager().getContainer(), this.getManager());          
        }
        return connectionUtil;
    }

    /**
    * The application id
    */  
    protected String applicationId = null;
  
    public String getApplicationId() {
        if(applicationId != null)
            return applicationId;
        Container container = manager.getContainer();
        StringBuffer sb = new StringBuffer(50);
        sb.append(this.getClusterId());
        ArrayList list = new ArrayList();
        while (container != null) {            
            if(container.getName() != null) {
                if(container.getName().equals("")) {
                    list.add(":/");
                } else {
                    list.add(":" + container.getName());
                }
            }            
            container = container.getParent();
        }
        for(int i=(list.size() -1); i>-1; i--) {
            String nextString = (String) list.get(i);
            sb.append(nextString);
        }
        applicationId = sb.toString();
        return applicationId;
        /*
        String result = sb.toString();
        applicationId = result;
        //remove after test
        //extractShortApplicationIdFromApplicationId(result);
        return result;
         */
    }
  
    /**
     * The short application id
     * just the cluster and app name
     * excluding the server name
     */  
    protected String shortApplicationId = null;  
  
    public String getShortApplicationId() {
        if(shortApplicationId != null)
            return shortApplicationId; 
        Container container = manager.getContainer();
        StringBuffer sb = new StringBuffer(50);
        sb.append(this.getClusterId());
        sb.append(":");
        if (container != null) {            
            if(container.getName() != null) {
                if(container.getName().equals("")) {
                    sb.append("/");
                } else {
                    sb.append(container.getName());
                }
            }            
        }
        container = container.getParent();
        shortApplicationId = sb.toString();
        return shortApplicationId;
        /*
        String result = sb.toString();
        shortApplicationId = result;
        return result;
         */        
    } 
    
    public String extractShortApplicationIdFromApplicationId(String appid) {
        
        StringTokenizer st = new StringTokenizer(appid, ":");
        ArrayList list = new ArrayList();
        while (st.hasMoreTokens()) {
            String nextToken = st.nextToken();
            list.add(nextToken);
            //System.out.println("nextToken = " + nextToken);
        }
        StringBuffer sb = new StringBuffer();
        int listSize = list.size();
        if(listSize > 2) {
            sb.append(list.get(0));
            sb.append(":");
            //sb.append(list.get(2));
            sb.append(list.get(listSize -1));
        }
        return sb.toString();

        /*
        String result = sb.toString();
        System.out.println("appid = " + appid);
        System.out.println("shortappid = " + getShortApplicationId());
        System.out.println("extractedShortAppId = " + result);
        System.out.println("calc short equals xtract:" + (getShortApplicationId().equals(result)) );
        return result;
         */

    }
  
    /**
    * How long to wait in seconds before giving up on a transaction
    */
    public void setTimeoutSecs(String timeoutSecs)  {
        debug("in setTimeoutSecs");
        String oldTimeoutSecs = this.timeoutSecs;
        this.timeoutSecs = timeoutSecs;
        support.firePropertyChange("timeoutSecs",
                                   oldTimeoutSecs,
                                   this.timeoutSecs);
    }
  
    /**
    * Return the time to wait in seconcs
    */
    public String getTimeoutSecs()  {
        debug("in getTimeoutSecs");
        return this.timeoutSecs;
    }
    
    protected long getTimeout() {
        return new Long(timeoutSecs).longValue();
    }

    protected void debug(String message) {
        log(message);
        System.out.println(message);
    }
  
    public HAStore()  {
        info = "S1AS7.0EE HAStore/1.0";
        threadName = "HAStore";
        storeName = "HAStore";

        long timeout = new Long(timeoutSecs).longValue();
        long timeout2 = new Long(timeoutSecs2).longValue();
        haErr = new HAErrorManager(timeout, threadName);
        haErrLoad = new HAErrorManager(timeout, threadName);                 
        haErrRemove = new HAErrorManager(timeout, threadName); 
        haErrRemoveExpired = new HAErrorManager(timeout, threadName);
        haErrExpiredKeys = new HAErrorManager(timeout, threadName);
        haErrKeys = new HAErrorManager(timeout, threadName);
        haErrSize = new HAErrorManager(timeout, threadName);
        //begin trial fix for 6526592
        haErrClear = new HAErrorManager(timeout2, threadName);
        //end trial fix for 6526592
        haErrExists = new HAErrorManager(timeout, threadName);
        haErrExists2 = new HAErrorManager(timeout, threadName);

        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        } 
        setLogLevel();

        //initialize sessions cache
        sessions = new BaseCache();
        sessions.init(_maxBaseCacheSize, _loadFactor, null);

        /*
        try {
          Logger.setLogWriter(new PrintWriter(
            new FileOutputStream("logfile.out"), true));
          Logger.setLogLevel(Logger.INFO);
        }
        catch ( FileNotFoundException e ) {
          log("Clustra JDBC log file could not be opened");
        }
         */
    }
  
    public int getSize() throws IOException {
        //debug("in getSize");
        return getSize(blobSessionTable);
    }
  
    /**
     * Return the number of Sessions present in this Store.
     *
     * @exception IOException if an input/output error occurs
     */
    public int getSize(String sessionTable) throws IOException {
        //debug("in getSize");

        int size = 0;
        //only want count for sessions for this app
        String sizeSql = "SELECT COUNT(id) FROM " + sessionTable
            + " WHERE appid = '" + getApplicationId() + "'";
        Connection conn = getConnection(false);
        ResultSet rst = null;

        try {
            if (preparedSizeSql == null) {
                preparedSizeSql = conn.prepareStatement(sizeSql);
            }

            rst = executeStatement(preparedSizeSql, true, haErrSize);

            if (rst.next()) {
                size = rst.getInt(1);
            }
            ((Connection)conn).commit();
        }
        catch(SQLException e) {
            try{((Connection)conn).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        finally {
            closeResultSet(rst);
        }

        return size;
    }

    /**
    * Return an array containing the session identifiers of all Sessions
    * currently saved in this Store.  If there are no such Sessions, a
    * zero-length array is returned.
    *
    * @exception IOException if an input/output error occurred
    */  
    public String[] keys() throws IOException  {
        //debug("in keys");
        return keys(blobSessionTable);
    }
    
    /**
     * Called by our background reaper thread to check if Sessions
     * saved in our store are subject of being expired. If so expire
     * the Session and remove it from the Store.
     * Note: this is an aggregate form of this method
     *
     */
    public void processStaleCachedSessions() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        if(!lookup.getStaleSessionCheckingFromConfig()) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("stale session checking disabled");
            }
            return;
        }
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }         
        Connection connection = null;
        try {
            connection = getConnectionValidated(false);
        } catch (IOException ex) {
        }
        //bail out if cannot get a valid connection
        if (connection == null) {
            return;
        }
        
        try { 
            this.removeStaleCachedSessions(connection, CHUNK_SIZE);            
        }        
        catch (IOException ex) {            
            ex.printStackTrace();
        }        
        
        return;        

    }    
    
    /**
     * Called by our background reaper thread to check if Sessions
     * saved in our store are subject of being expired. If so expire
     * the Session and remove it from the Store.
     * Note: this is an aggregate form of this method
     *
     */
    public void processExpires() {
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }         
        Connection connection = null;
        try {
            connection = getConnectionValidated(false);
        } catch (IOException ex) {
        }
        //bail out if cannot get a valid connection
        if (connection == null) {
            return;
        }
        
        try { 
            this.removeExpiredSessions(connection, CHUNK_SIZE);            
        }        
        catch (IOException ex) {            
            ex.printStackTrace();
        }        
        
        return;        

    }
    
    /** 
     * this method returns an array of cached session ids
     * @return String[] array of session ids
     */    
    private String[] getSessionIds() {
        BaseCache mainStoreCachedSessions = this.getMainStoreCache();
        String[] ids = null;
        int numberOfIds = mainStoreCachedSessions.getEntryCount();
        ArrayList idsList = new ArrayList(numberOfIds);
        Iterator keysIter = mainStoreCachedSessions.keys();
        while(keysIter.hasNext()) {
            //String nextKey = (String)keysIter.next();
            idsList.add((String)keysIter.next());
        }
        String[] template = new String[idsList.size()];
        ids = (String[])idsList.toArray(template); 
        return ids;
    }
    
    private void removeStaleCachedSessions(Connection connection, int chunksize) throws IOException {
        String[] ids = getSessionIds();
        boolean finished = false;
        int chunk = chunksize;
        int idsSize = ids.length;
        if(idsSize == 0) {
            //nothing to do
            return;
        }
        int startIdx = 0;
        int endIdx = 0;
        if (chunksize > idsSize) {
            chunk = idsSize;
        }
        endIdx = startIdx + chunk;
        while (!finished) {
            removeStaleCachedSessions(connection, ids, startIdx, endIdx);
            startIdx = endIdx + 1;
            endIdx = startIdx + chunk;
            if(startIdx >= idsSize - 1) {
                finished = true;
                break;
            }
            if(endIdx >= idsSize - 1) {
                endIdx = idsSize = 1;
            }
        }
    } 
    
    private void removeStaleCachedSessions(Connection connection, String[] ids, int startIdx, int endIdx) 
        throws IOException 
    {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("removeStaleCachedSessions: start=" + startIdx + " end=" + endIdx);
        }
        ResultSet rst = null;
        String[] keys = null;
        ArrayList keyList = new ArrayList(100);
        ArrayList staleIdList = new ArrayList(100);
        if (connection == null) {
            return;
        }
        
        String inClause = getInClause(ids, startIdx, endIdx);
        String keysSql = "SELECT id, lastaccess FROM " + blobSessionTable
            + " WHERE appid = '" + getApplicationId() + "'"
            + " AND id IN " + inClause;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("keysSql = " + keysSql);
        }
 
        try {
            haErrRemoveExpired.txStart();
            while ( ! haErrRemoveExpired.isTxCompleted() ) {                 
                try {
                    preparedKeysSql = connection.prepareStatement(keysSql);
                    rst = preparedKeysSql.executeQuery();

                    //test if set is empty
                    if (!rst.isBeforeFirst()) {
                        //the set was empty
                        rst.close();
                        break;
                    }

                    while (rst.next()) {                                      
                        String nextKey = rst.getString(1);
                        long nextLastAccessTime = rst.getLong(2);
                        ConfigChangeElement nextElem = 
                            new ConfigChangeElement(nextKey, new Long(nextLastAccessTime));
                        keyList.add(nextElem);                            
                    }
                    rst.close();
                    
                    BaseCache mainStoreCachedSessions = this.getMainStoreCache();
                    for(int i=0; i<keyList.size(); i++) {
                        //get persistent values
                        ConfigChangeElement nextElem = (ConfigChangeElement)keyList.get(i);
                        String nextId = nextElem.getName();
                        long nextPersistentLastAccessTime = ((Long)nextElem.getValue()).longValue();
                        //get cached values
                        Session cachedSession = (Session)mainStoreCachedSessions.get(nextId);
                        long nextCachedLastAccessTime = ((StandardSession)cachedSession).getLastAccessedTimeInternal();
                        //if cached last access time is < persistent last access time
                        //then add to list of ids to remove from cache - it is stale
                        if(nextCachedLastAccessTime < nextPersistentLastAccessTime) {
                            staleIdList.add(nextId);
                        }
                    }

                    connection.commit();
                    closeStatement(preparedKeysSql);
                    /* for testing
                    for(int i=0; i<staleIdList.size(); i++) {
                        System.out.println("staleIdList[" + i + "]=" + staleIdList.get(i));
                    }
                     */                    
                    String[] template = new String[staleIdList.size()];
                    keys = (String[])staleIdList.toArray(template);
                    /* for testing
                    System.out.println("String[] keys=" + keys);
                    System.out.println("keys.length=" + keys.length);
                    for(int i=0; i<keys.length; i++) {
                        System.out.println("keys[" + i + "]=" + keys[i]);
                    }
                     */
                    removeSessionsForCache(keys);
                    //remove from manager cache too
                    HAManagerBase mgr = (HAManagerBase)this.getManager();
                    mgr.removeSessionIdsFromManagerCache(keys);                   

                    haErrRemoveExpired.txEnd();                   
                    return;
                } catch (SQLException e) {
                    closeStatement(preparedKeysSql);
                    haErrRemoveExpired.checkError(e, connection);
                }                     
            }                
                               
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
                closeStatement(preparedKeysSql);
            }
            catch (SQLException ee) {
                ee.printStackTrace();
            }
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAStore-removeStaleCachedSessions: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }         
    }
    
    private String getInClause(String[] ids, int startIdx, int endIdx) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        for(int i=startIdx; i<endIdx; i++) {            
            if(i != startIdx) {
                sb.append(", ");
            }
            sb.append("'" + ids[i] + "'");            
        }
        sb.append(")");
        return sb.toString();
    }
    
    /**
    * remove a session from the store cache
    * Hercules: added method
    */    
    public void removeFromStoreCache(String id) {
        this.removeSessionFromMainStoreCache(id);        
    }      
    
    /** this method contacts the DB using the given connection and removes the bean from the HADB.
     * @param sessionTable
     * @param connection
     * @throws IOException
     * @return
     */
    private int removeExpiredSessions(Connection connection, int chunk) 
        throws IOException {
        //System.out.println("IN NEW removeExpiredSessions");
        int result = 0;
        String[] keys = null;
        ArrayList keyList = new ArrayList(100);
        if (connection == null) {
            return result;
        }
        
        ResultSet rst = null;
        
        long timeNow = System.currentTimeMillis();
        Manager mgr = this.getManager(); 
        long maxInactiveIntervalMillis = mgr.getMaxInactiveInterval() * 1000;     
        //long relevantTime = (long) (timeNow - maxInactiveIntervalMillis);
        long relevantTime = (long) (timeNow);
        
        //only selecting keys for this app 
        PreparedStatement preparedKeysSql = null;
        String keysSql = "SELECT id FROM " + blobSessionTable
            + " WHERE appid = '" + getApplicationId() + "'"
            + " AND ((lastaccess + (maxinactive * 1000)) < ? OR valid = '0')" + "ORDER BY id";
        
        //only deleting keys for this app
        PreparedStatement preparedDeleteSql = null;        
        String deleteSql = "DELETE FROM " + blobSessionTable
            + " WHERE id BETWEEN ? AND ? AND appid = '" + getApplicationId() + "'"
            + " AND ((lastaccess + (maxinactive * 1000)) < ? OR valid = '0')";        
        
        try {

            haErrRemoveExpired.txStart();

            while ( ! haErrRemoveExpired.isTxCompleted() ) {                 
                try {
                    boolean moreLeft = true;
                    while (moreLeft) {
                        preparedKeysSql = connection.prepareStatement(keysSql);
                        preparedKeysSql.setLong(1, relevantTime);
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
                            keyList.add(rst.getString(1));
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
                        preparedDeleteSql.setLong(3, relevantTime);
                        int cnt = preparedDeleteSql.executeUpdate();
                        result += cnt;
                        connection.commit();
                    }

                    connection.commit();
                    closeStatement(preparedKeysSql);
                    closeStatement(preparedDeleteSql);
                    /* for testing
                    for(int i=0; i<keyList.size(); i++) {
                        System.out.println("HAStore>>removeExpiredSessions: key[" + i + "]=" + keyList.get(i));
                    }
                     */
                    String[] template = new String[keyList.size()];
                    keys = (String[])keyList.toArray(template);
                    removeSessionsForCache(keys);

                    haErrRemoveExpired.txEnd();
                    _logger.finest("HAStore>>removeExpiredSessions: result = " + result);
                    return result;
                } catch (SQLException e) {
                    closeStatement(preparedKeysSql);
                    closeStatement(preparedDeleteSql);                    
                    haErrRemoveExpired.checkError(e, connection);
                }                     
            }                
                               
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
                closeStatement(preparedKeysSql);
                closeStatement(preparedDeleteSql);
            }
            catch (SQLException ee) {
                ee.printStackTrace();
            }
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAStore-removeExpiredSessions: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        _logger.finest("HAStore>>removeExpiredSessions: result = " + result);
        return result;        
    } 
    
    protected void removeSessionsForCache(String[] sessionIds) {
        HAManagerBase mgr = (HAManagerBase)this.getManager();
        BaseCache mainStoreCachedSessions = this.getMainStoreCache();
        synchronized(mainStoreCachedSessions) {
            for(int i=0; i<sessionIds.length; i++) {
                String nextId = sessionIds[i];
                StandardSession nextSess = null;
                try {
                    //SJSAS 6406580 START
                    //nextSess = (StandardSession)mgr.findSession(nextId);
                    nextSess = (StandardSession)mgr.findSessionFromCacheOnly(nextId);
                    //SJSAS 6406580 END                    
                } catch (IOException ex) {}                
                if(nextSess != null) {
                    //this should expire and fire the notifications
                    //but already removed from store
                    nextSess.expire(true, false);
                    // Take it out of the cache - remove does not handle nulls
                    if(nextId != null) {
                        mainStoreCachedSessions.remove(nextId);
                    }
                }                
            }
        }
    }       
  
    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store for this container.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] keys(String sessionTable) throws IOException  {
        //debug("in keys");
        String[] result = {};
        //only selecting keys for this app
        String keysSql = "SELECT id FROM " + sessionTable
            + " WHERE appid = '" + getApplicationId() + "'";

        try {
            Connection conn = getConnectionValidated(false);
        } catch (IOException ex) {
        }
        //bail out if cannot get a valid connection
        if (conn == null) {
            return result;
        }        

        ResultSet rst = null;
        String keys[] = null;

        try {
            if (preparedKeysSql == null) {
                preparedKeysSql = conn.prepareStatement(keysSql);
            }

            rst = executeStatement(preparedKeysSql, true, haErrKeys);
            if ( rst == null ) {
                if (_debug > 0) {                
                    debug("keys(): No rows returned, returning an empty array");
                }
                return new String[0];
            }

            ArrayList keysArray = new ArrayList();

            for ( int i = 0 ; rst.next() ; i++ )  {
                if (_debug > 0) {
                    debug("in keys, id is " + rst.getString(1));
                }
                keysArray.add(rst.getString(1));
            }

            keys = (String []) keysArray.toArray(new String[0]); 
            ((Connection)conn).commit();
        } 
        catch(SQLException e) {
            try{((Connection)conn).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store: " + e.getMessage()).initCause(e);
            throw ex1;             
        }
        finally {
            closeResultSet(rst);
        }

        return keys;    
    
    }
  
    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store for this container that are eligible
     * to be expired. If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */  
    public String[] expiredKeys() throws IOException  {
        //debug("in expiredKeys");
	return expiredKeys(blobSessionTable);
    } 
    
    /**
     * Return an array containing the session identifiers of all Sessions
     * currently saved in this Store for this container that are eligible
     * to be expired.  If there are no such Sessions, a
     * zero-length array is returned.
     *
     * @exception IOException if an input/output error occurred
     */
    public String[] expiredKeys(String sessionTable) throws IOException  {
        //System.out.println("in new HAStore>>expiredKeys");
        //debug("in expiredKeys");
        String[] result = {};
        long timeNow = System.currentTimeMillis();
        Manager mgr = this.getManager(); 
        long maxInactiveIntervalMillis = mgr.getMaxInactiveInterval() * 1000;     
        long relevantTime = (long) (timeNow - maxInactiveIntervalMillis);
        //only selecting keys for this app
        /*
        String expiredKeysSql = "SELECT id FROM " + sessionTable
            + " WHERE appid = '" + getApplicationId() + "'"
            + " AND (lastaccess < " + relevantTime + " OR valid = '0')";
         */
        String expiredKeysSql = "SELECT id FROM " + sessionTable
            + " WHERE appid = '" + getApplicationId() + "'"
            + " AND (lastaccess < ? OR valid = '0')";        
        //System.out.println("HAStore>>expiredKeys: SQL = " + expiredKeysSql);

        try {
            Connection conn = getConnectionValidated(false);
        } catch (IOException ex) {
        }
        //bail out if cannot get a valid connection
        if (conn == null) {
            return result;
        }
        
        ResultSet rst = null;
        String keys[] = null;

        try {

            haErrExpiredKeys.txStart();

            while ( ! haErrExpiredKeys.isTxCompleted() ) { 
                try {                    
                    if (preparedExpiredKeysSql == null) {
                        preparedExpiredKeysSql = conn.prepareStatement(expiredKeysSql);
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
                    ((Connection)conn).commit();

                    haErrExpiredKeys.txEnd();
                } catch (SQLException e) {
                    haErrExpiredKeys.checkError(e, conn);
                }                     
            }
               
        } 
        catch(SQLException e) {
            try{((Connection)conn).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store expiredKeys: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store expiredKeys " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            closeResultSet(rst);
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
    * Load and return the Session associated with the specified session
    * identifier from this Store, without removing it.  If there is no
    * such stored Session, return <code>null</code>.
    *
    * @param id Session identifier of the session to load
    *
    * @exception ClassNotFoundException if a deserialization error occurs
    * @exception IOException if an input/output error occurs
    */
    public synchronized Session load(String id) throws ClassNotFoundException, IOException {
        //debug("in load");
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN NEW LOAD METHOD");
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

        ResultSet rst = null;

        HADBConnectionGroup connGroup = null;
        try {
            connGroup = this.getConnectionsFromPool(true);
        } catch (IOException ex) {
            //this means failure to obtain connection
            //we will ignore the exception, log it and return null
            //from the load; protect the thread from crashing
            Object[] params = { "HAStore>>load" };
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure1", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure2", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure3", params);
        }
        if(connGroup == null) {
            if (_debug > 0) {
                debug("HAStore>>load: Failure to obtain connection from pool: returning null");
            }
            ServerConfigLookup config = new ServerConfigLookup();
            String connURL = config.getConnectionURLFromConfig();
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed using connection URL: " + connURL + " -- returning null. Check connection pool configuration.");
            return null;
        }

        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;
        String shortAppId = null;

        String loadSql = "SELECT id, sessdata, username , ssoid, appid FROM " + blobSessionTable +
        //MERGE was
        //String loadSql = "SELECT id, sessdata FROM " + blobSessionTable +
        //" WHERE id = ?";
        " WHERE id = ? AND appid = ?";

        try {

            haErrLoad.txStart();

            while ( ! haErrLoad.isTxCompleted() ) {                
                try {                

                    preparedLoadSql = internalConn.prepareStatement(loadSql);

                    preparedLoadSql.setString(1, id);
                    preparedLoadSql.setString(2, getApplicationId());
                    rst = preparedLoadSql.executeQuery();

                    //if ( ! rst.next() ) {
                    if ( rst == null || ! rst.next() ) {
                        if (_debug > 0) {
                            debug("No persisted data found for session " + id);
                        }
                        return null;
                    }

                    session = getSession(rst);
                    String appId = rst.getString(5);
                    shortAppId = extractShortApplicationIdFromApplicationId(appId);
                    //((Connection)conn).commit();  NO COMMIT NEEDED
                    preparedLoadSql.close();
                    preparedLoadSql = null;
                    try {
                        externalConn.close();
                    } catch (java.sql.SQLException ex) {}
                    externalConn = null;

                    haErrLoad.txEnd();
                } catch (SQLException e) {
                    closeStatement(preparedLoadSql);
                    preparedLoadSql = null;
                    haErrLoad.checkError(e, internalConn);
                }                     
            }
               
        } 
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){}
            try{internalConn.rollback();}catch(SQLException ee){}
            e.printStackTrace();
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store load: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store load " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }         
        finally {
            closeResultSet(rst);       
            if (preparedLoadSql != null) {
                try {
                  preparedLoadSql.close();
                } catch (SQLException e) {}
                preparedLoadSql = null;
            }                
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {}
            }
            //undo the foreground lock that occured during load
            StandardSession stdSess = (StandardSession) session;
            if(stdSess != null && stdSess.isForegroundLocked()) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("inHAStore>>load before unlockForeground:lock = " + stdSess.getSessionLock());
                }
                //reduce ref count by 1
                stdSess.unlockForeground();
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("inHAStore>>load after unlockForeground:lock = " + stdSess.getSessionLock());
                }
            }      
        }

        //FIXME for 5053485 remove after testing
        //if( !shortAppId.equals(getShortApplicationId()) ) {
        if( !getShortApplicationId().equals(shortAppId) ) {
            //debug("Loaded session does not belong in this container-shortAppId = " + shortAppId + " getShortApplicationId() = " + getShortApplicationId());
            ((HAManagerBase)manager).removeSessionFromManagerCache(session);
            return null;
        }
        
        this.putSessionInMainStoreCache(session);
        //sessions.put(session.getIdInternal(), session); 
        //debug("Loaded session " + id + " from HA Store");
        return session;
    }              
  
    /**
    * Given a result set containing session data, return a session
    * object
    *
    * @param rst
    *   The result set with the session data
    *
    * @return
    *   A newly created session for the given session data, and associated
    *   with this Manager
    */
    protected Session getSession(ResultSet rst) 
        throws SQLException, IOException, ClassNotFoundException 
    {
        Session _session = null;
        BufferedInputStream bis = null;
        Loader loader = null;    
        ClassLoader classLoader = null;
        ObjectInputStream ois = null;
        Container container = manager.getContainer();
        java.security.Principal pal=null; //MERGE chg added
            String ssoId = null;	
        IOUtilsCaller utilsCaller = null;
            
        try
        {
            //bis = new BufferedInputStream(rst.getBinaryStream(2));
            String id = rst.getString(1); //MERGE was next line 
            //rst.getString(1);
            Blob blob = rst.getBlob(2);

            //Get the username from database MERGE added code
            String username = rst.getString(3);
            ssoId = rst.getString(4);	
            //debug("HAStore.getSession()  id="+id+"  username ="+username+";");  
            if((username !=null) && (!username.equals(""))) {
                if (_debug > 0) {
                    debug("Username retrived from DB is "+username);
                }
                pal = ((com.sun.web.security.RealmAdapter)container.getRealm()).createFailOveredPrincipal(username);
                if (_debug > 0) {
                    debug("principal created using username  "+pal);
                }
            }
            //--SRI  

            bis = new BufferedInputStream(blob.getBinaryStream());
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("loaded seasion from hastore, length = "+blob.length());
            }
            if (container != null) {
                loader = container.getLoader();
            }

            if (loader != null) {
                classLoader = loader.getClassLoader();
            }
            
            //Bug 4832603 : EJB Reference Failover
            //HERCULES FIXME: for now reverting back
            //need to look at new EJBUtils and related serialization code
            /*
          if (classLoader != null) {
            ois = new CustomObjectInputStream(bis, classLoader);
          }
          else {
            ois = new ObjectInputStream(bis);
          }
          
            //ois = EJBUtils.getInputStream(bis, classLoader, true, true);
            //end - Bug 4832603  
             */          
            if (classLoader != null) {
                if( (utilsCaller = this.getWebUtilsCaller()) != null) {
                    try {
                        ois = utilsCaller.createObjectInputStream(bis, true, classLoader);
                    } catch (Exception ex) {}
                }
            }
            if (ois == null) {
                ois = new ObjectInputStream(bis); 
            }
            
            if(ois != null) {
                try {
                    _session = readSession(manager, ois);
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
            }
        }
        catch(ClassNotFoundException e)
        {
            System.err.println("getSession :"+e.getMessage());
            throw e;
        }
        catch(IOException e)
        {
            //System.err.println("getSession IOException :"+e.getMessage());
            throw e;
        }
        _session.setNew(false);
        //FIXME removed after review
        //((StandardSession) _session).notifyHttpSessionActivationListenersSessionDidActivate();
        //MERGE added next 2 lines
        _session.setPrincipal(pal);
            //MERGE
            if((ssoId !=null) && (!ssoId.equals("")))
                    associate(ssoId, _session);
            //__MERGE
        if (_debug > 0) {
            debug("getSession principal="+pal+" was added to session="+_session); 
        }
        return _session;
    }  
  
    protected void associate(String ssoId, Session _session) {
        if (_debug > 0) {
            debug("Inside associate() -- HAStore");
        }
        Container parent = manager.getContainer();
        SingleSignOn sso = null;
        while ((sso == null) && (parent != null)) {
            if (_debug > 0) {
                 debug("Inside associate()  while loop -- HAStore");
            }
        if (!(parent instanceof Pipeline)) {
            if (_debug > 0) {
                 debug("Inside associate()  parent instanceof Pipeline -- HAStore");
            }
            parent = parent.getParent();
            continue;
        }
        Valve valves[] = ((Pipeline) parent).getValves();
        for (int i = 0; i < valves.length; i++) {
            if (valves[i] instanceof SingleSignOn) {
                 if (_debug > 0) {
                    debug("Inside associate()  valves[i] instanceof SingleSignOn -- HAStore");
                 }
                 sso = (SingleSignOn) valves[i];
                 break;
             }
        }
        if (sso == null)
            parent = parent.getParent();
        }
        if (sso != null) {
            if (_debug > 0) {
                debug("Inside associate() sso != null");
            }
            //SingleSignOnEntry ssoEntry = ((HASingleSignOn)sso).lookup(ssoId);
            SingleSignOnEntry ssoEntry = ((HASingleSignOn)sso).lookupEntry(ssoId);
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Inside associate() ssoEntry = "+ssoEntry);
                }
                if(ssoEntry!=null)
                    ssoEntry.addSession(sso, _session);
        }

    }
    
    //SJSAS 6406580 START
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void remove(String id) throws IOException  {

        if (_debug > 0) {
            debug("in remove");
        }

        if ( id == null )  {
            if (_debug > 0) {
                debug("In remove, got a null id");
            }
            return;
        }
        Manager mgr = this.getManager();
        if(mgr instanceof HAManagerBase) {
            HAManagerBase pMgr = (HAManagerBase)mgr;
            pMgr.doRemove(id);
        } else {
            this.removeSynchronized(id);
        }
    }
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action. This is the synchronized version of the remove
    * method which must be used by the singleton store - store pool
    * elements use the doRemove method
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public synchronized void removeSynchronized(String id) throws IOException  {

        if (_debug > 0) {
            debug("in remove");
        }

        if ( id == null )  {
            if (_debug > 0) {
                debug("In remove, got a null id");
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
            Object[] params = { "HAStore>>remove" };
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
    
        // Take it out of the cache 
        sessions.remove(id);

        //String removeSql = "DELETE FROM "+ blobSessionTable + " WHERE id = ?";
        String removeSql = "DELETE FROM "+ blobSessionTable 
                + " WHERE id = ? AND appid = ?";

        try {

            haErrRemove.txStart();

            while ( ! haErrRemove.isTxCompleted() ) {
                try {                
                    //this is instVar but will always be re-prepared now                
                    preparedRemoveSql = internalConn.prepareStatement(removeSql);

                    preparedRemoveSql.setString(1, id);
                    preparedRemoveSql.setString(2, getApplicationId());
                    preparedRemoveSql.executeUpdate();

                    //((Connection)conn).commit();  NO COMMIT NEEDED           
                    preparedRemoveSql.close();
                    preparedRemoveSql = null;
                    try {
                        externalConn.close();
                    } catch (java.sql.SQLException ex) {}
                    externalConn = null;

                    haErrRemove.txEnd();
                } catch (SQLException e) {
                    closeStatement(preparedRemoveSql);
                    preparedRemoveSql = null;
                    haErrRemove.checkError(e, internalConn);
                }                     
            }
               
        } 
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){} 
            try{internalConn.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store remove: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store remove " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {       
            if (preparedRemoveSql != null) {
                try {
                  preparedRemoveSql.close();
                } catch (SQLException e) {}
                preparedRemoveSql = null;
            }                
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {}
            }
        }                     
        
        if (_debug > 0) {
            debug("Removed session " + id + " from HA Store");
        }
    }         
        
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action. (This is the non-synchronized version of remove
    * called by a store element from the pool, not the singleton store
    * which must use removeSynchronized
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */
    public void doRemove(String id) throws IOException  {
        
        if (_debug > 0) {
            debug("in remove");
        }

        if ( id == null )  {
            if (_debug > 0) {
                debug("In remove, got a null id");
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
            Object[] params = { "HAStore>>remove" };
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
    
        // Take it out of the cache 
        sessions.remove(id);

        //String removeSql = "DELETE FROM "+ blobSessionTable + " WHERE id = ?";
        String removeSql = "DELETE FROM "+ blobSessionTable 
                + " WHERE id = ? AND appid = ?";

        try {

            haErrRemove.txStart();

            while ( ! haErrRemove.isTxCompleted() ) {
                try {                
                    //this is instVar but will always be re-prepared now
                    preparedRemoveSql = internalConn.prepareStatement(removeSql);

                    preparedRemoveSql.setString(1, id);
                    preparedRemoveSql.setString(2, getApplicationId());
                    preparedRemoveSql.executeUpdate();

                    //((Connection)conn).commit();  NO COMMIT NEEDED           
                    preparedRemoveSql.close();
                    preparedRemoveSql = null;
                    try {
                        externalConn.close();
                    } catch (java.sql.SQLException ex) {}
                    externalConn = null;

                    haErrRemove.txEnd();
                } catch (SQLException e) {
                    closeStatement(preparedRemoveSql);
                    preparedRemoveSql = null;                    
                    haErrRemove.checkError(e, internalConn);
                }                     
            }
               
        } 
        catch(SQLException e) {
            //try{((Connection)conn).rollback();}catch(SQLException ee){} 
            try{internalConn.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store remove: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store remove " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {       
            if (preparedRemoveSql != null) {
                try {
                  preparedRemoveSql.close();
                } catch (SQLException e) {}
                preparedRemoveSql = null;
            }                
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex) {}
            }
        }                     
        
        if (_debug > 0) {
            debug("Removed session " + id + " from HA Store");
        }
    }
    //SJSAS 6406580 END           
    
    /**
    * Remove the Session with the specified session identifier from
    * this Store, if present.  If no such Session is present, this method
    * takes no action.
    *
    * @param id Session identifier of the Session to be removed
    *
    * @exception IOException if an input/output error occurs
    */   
    public synchronized void clear() throws IOException {
        if (_debug > 0) {
            debug("in clear");
        }
        //System.out.println("IN NEW VERSION OF CLEAR");
        // Clear out the cache too
        sessions = new BaseCache();
        sessions.init(_maxBaseCacheSize, _loadFactor, null);        
        
        HADBConnectionGroup connGroup = null;
        try {
            connGroup = this.getConnectionsFromPool(true);
        } catch (IOException ex) {
            //this means failure to obtain connection
            //we will ignore the exception but return null
            //from the load; protect the thread from crashing
            Object[] params = { "HAStore>>clear" };
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure1", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure2", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure3", params);            
        }
        if(connGroup == null) {
            if (_debug > 0) {
                debug("HAStore>>clear: Failure to obtain connection from pool");
            }
            ServerConfigLookup config = new ServerConfigLookup();
            String connURL = config.getConnectionURLFromConfig();
            _logger.warning("ConnectionUtil>>getConnectionsFromPool failed using connection URL: " + connURL + " -- returning null. Check connection pool configuration.");
            return;
        }
        
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;
        
        try {            
            this.removeAllSessions(internalConn, CHUNK_SIZE);
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
                   "clear: failed to remove all the sessions for this app " +
                           this.getApplicationId());            
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
    
    /** Remove all the sessions associated with the application
     * @param connection
     * @param chunk the chunk size to use for the delete operations
     */
    public void removeAllSessions(Connection connection, int chunk) 
        throws IOException {
            
        if (connection == null) {
            return;
        }
        //System.out.println("IN NEW removeAllSessions");
        int result = 0;        
        ResultSet rst = null;
        PreparedStatement preparedKeysSql = null;
        //only selecting sessions  for this application        
        String keysSql = "SELECT id FROM " + blobSessionTable +
                        " WHERE appid = ? ORDER BY id";        
        //only deleting sessions  for this container
        PreparedStatement preparedDeleteSql = null;
        String deleteSql = "DELETE FROM " + blobSessionTable + 
                         " WHERE id BETWEEN ? AND ? AND appid = ?"; 
        
        try {

            haErrClear.txStart();

            while ( ! haErrClear.isTxCompleted() ) {                 
                try {
                    boolean moreLeft = true;
                    while (moreLeft) {
                        preparedKeysSql = connection.prepareStatement(keysSql);
                        preparedKeysSql.setString(1, this.getApplicationId());
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
                        preparedDeleteSql.setString(3, this.getApplicationId());
                        int cnt = preparedDeleteSql.executeUpdate();
                        result += cnt;                        
                        connection.commit();
                    }

                    connection.commit();
                    closeStatement(preparedKeysSql);
                    closeStatement(preparedDeleteSql);

                    haErrClear.txEnd();
                    _logger.finest("HAStore>>removeAllSessions - successfully removed " + result + " sessions");                  
                    return;
                } catch (SQLException e) {
                    closeStatement(preparedKeysSql);
                    closeStatement(preparedDeleteSql);
                    preparedKeysSql = null;
                    preparedDeleteSql = null;
                    haErrClear.checkError(e, connection);
                }                     
            }                
                               
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection.rollback();
                closeStatement(preparedKeysSql);
                closeStatement(preparedDeleteSql);
            }
            catch (SQLException ee) {
                ee.printStackTrace();
            }
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAStore-removeAllSessions: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        _logger.finest("HAStore>removeAllSessions - successfully removed " + result + " sessions");
        return;        
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
      
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("XXXXXXX In save, id is " + session.getIdInternal());
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
        long startTime = 0L;
        //long endTime = 0L;

        //added for monitoring
        HAManagerBase mgr = (HAManagerBase) this.getManager();
        WebModuleStatistics stats = mgr.getWebModuleStatistics();
        if(isMonitoringEnabled) {
            startTime = System.currentTimeMillis();
        }
        //added for monitoring

        HASession sess = (HASession) session;
        boolean previousDirtyFlag = sess.isDirty();
        if (_debug > 0) {
            debug("in save");
        }

        Connection conn = getConnection(false);

        //IntHolder length = new IntHolder();
        //BufferedInputStream in = getInputStream(session, length);
        //moving this getByteArray call in the dirty update and insert case
        //and leaving it out of the non-dirty update -- performance improvement
        //byte[] in = getByteArray(session);
        byte[] in = null;
        
        boolean sessionIsPersistent = false;
        boolean sessionInStoreOk = false;
        try {
            sessionIsPersistent = sessionInStore(session);
            sessionInStoreOk = true;
        } catch (IOException ex) {
            sessionInStoreOk = false;
        }

        if(!sessionInStoreOk) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAStore-save: sessionInStore failed: aborting save ");
            }            
            return;
        } 
        
        try  {
            if(sessionIsPersistent) {
            //if ( sessionInStore(session) ) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Session is in store");
                }
                if (sess.isDirty()) {
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Session IS Dirty");
                    }
                    in = getByteArray(session);
                    updateSessionBlob(session, in);
                } else {
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Session is NOT Dirty");
                    }
                    updateSessionNoDataBlob(session, in);
                }
            }        
            else {
                in = getByteArray(session);
                insertSessionBlob(session, in);
            }
            sess.setDirty(false);
            if(isMonitoringEnabled) {
                long endTime = System.currentTimeMillis();
                stats.processBackgroundSave(endTime - startTime);
            }
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("session class= " + sess.getClass().getName());
                _logger.finest("after save: session dirty= " + sess.isDirty());
            }
        }
        catch ( IOException e ) {
            sess.setDirty(previousDirtyFlag);                
            // Calling routines don't print the stack trace :(
            e.printStackTrace();
            throw e;
        }
        finally {
            /*
            if (in != null) {
                in.close();
            }
             */
        }
        if (_debug > 0) {
            debug("Saved session " + session.getIdInternal() + " into HA Store");
        }

        // Put it in the cache
        this.putSessionInMainStoreCache(session);
        //sessions.put(session.getIdInternal(), session);
    
    }        
           
    /**
     * Save the specified Session into this Store.  Any previously saved
     * information for the associated session identifier is replaced.
     *
     * @param session Session to be saved
     *
     * @exception IOException if an input/output error occurs
     */
    public void valveSave(Session session) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("XXXXXXX In valveSave, id is " + session.getIdInternal());
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
        HASession sess = (HASession) session;       
        boolean previousDirtyFlag = sess.isDirty();
        if (_debug > 0) {
            debug("in valveSave");
        }
      
        //added for monitoring
        HAManagerBase mgr = (HAManagerBase) this.getManager();
        WebModuleStatistics stats = mgr.getWebModuleStatistics();
        long getConnStartTime = 0L;
        if(isMonitoringEnabled) {
            getConnStartTime = System.currentTimeMillis();
        }
        //end added for monitoring 
        HADBConnectionGroup connGroup = null;
        try {
            connGroup = this.getConnectionsFromPool(true);
        } catch (IOException ex) {
            //this means failure to obtain connection
            //we will ignore the exception log it but return
            //from the valveSave without storing the session
            Object[] params = { "HAStore>>valveSave" };
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure1", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure2", params);
            _logger.log(Level.SEVERE, "hastore.hadbConnectionFailure3", params);            
        }
        //added for monitoring      
        if(isMonitoringEnabled) {
            long getConnEndTime = System.currentTimeMillis();
            stats.processGetConnectionFromPool(getConnEndTime - getConnStartTime);
        }
        //end added for monitoring
      
        //if we cannot get a connection then quit
        if (connGroup == null) {
            return;
        }
        Connection internalConn = connGroup._internalConn;
        Connection externalConn = connGroup._externalConn;
        //IntHolder length = new IntHolder();
        //BufferedInputStream in = getInputStream(session, length);
        //moving this getByteArray call in the dirty update and insert case
        //and leaving it out of the non-dirty update -- performance improvement        
        //byte[] in = getByteArray(session);
        byte[] in = null;
        boolean sessionIsPersistent = false;
        boolean sessionInStoreOk = false;
        try {
            sessionIsPersistent = sessionInStore(session, internalConn);
            sessionInStoreOk = true;
        } catch (IOException ex) {
            sessionInStoreOk = false;
            if (externalConn != null) {
                try {
                    externalConn.close();
                } catch (Exception ex2) {}
            }            
        }     
        if(!sessionInStoreOk) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                    "HAStore-valveSave: sessionInStore failed: aborting save ");
            }            
            return;
        }
        try {
            //if ( sessionInStore(session, internalConn) ) {
            if(sessionIsPersistent) {
                //_logger.finest("Session is in store");
                if (sess.isDirty()) {
                    //_logger.finest("Session IS Dirty");
                    in = getByteArray(session);
                    updateSessionNewBlob(session, in, internalConn);
                } else {
                    //_logger.finest("Session is NOT Dirty");
                    updateSessionNoDataNewBlob(session, in, internalConn);
                }
            }
            else {
                //_logger.finest("Session NOT in store");
                in = getByteArray(session);
                insertSessionNewBlob(session, in, internalConn);
            }
            sess.setDirty(false);
            long getConnCloseStartTime = 0L;
            if(isMonitoringEnabled) {
                getConnCloseStartTime = System.currentTimeMillis();
            }            
            try {
                externalConn.close();
            } catch (java.sql.SQLException ex) {}
            externalConn = null;
            if(isMonitoringEnabled) {
                long getConnCloseEndTime = System.currentTimeMillis();
                stats.processPutConnectionIntoPool(getConnCloseEndTime - getConnCloseStartTime);
            }                        
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
            /*
            if (in != null) {
              in.close();
            }
             */
        }

        //debug("Saved session " + session.getIdInternal() + " into HA Store");

        // Put it in the cache
        //not for valveSave
        //sessions.put(session.getIdInternal(), session);
        this.putSessionInMainStoreCache(session);
        
    }    
    
    /**
    * Insert a session into the HA store
    *
    * @param session 
    *   The session to store
    *
    * @param in
    *   The stream of serialized data from the session
    *
    * @param length
    *   The length of the stream  
    */
    private void insertSessionNewBlob(Session session, byte[] in,
        Connection connection) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN insertSessionNewBlob");
        }
        //note: appid is newly added column
        String insertSql = "INSERT INTO " + blobSessionTable + " ("+
              "id, sessdata, valid, maxinactive, lastaccess, appid, username, ssoid) " +
              //MERGE was next line
              //"id, sessdata, valid, maxinactive, lastaccess, appid) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {

            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {                
                try {                
                    preparedInsertSql = connection.prepareStatement(insertSql);
                    preparedInsertSql.setString(1, session.getIdInternal());
                    //preparedInsertSql.setBinaryStream(2, in, buflength);
                    preparedInsertSql.setBytes(2, in);
                    preparedInsertSql.setString(3, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedInsertSql.setInt(4, session.getMaxInactiveInterval());
                    preparedInsertSql.setLong(5, ((StandardSession)session).getLastAccessedTimeInternal());
                    //added column
                    preparedInsertSql.setString(6, getApplicationId());
                    //end added column      
                    //added column for userName --SRI  MERGE
                    if(session.getPrincipal() !=null){
                        if (_debug > 0) {
                            debug(" session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedInsertSql.setString(7, session.getPrincipal().getName()); //null shpould be replaced with the username from session
                    } else {
                        preparedInsertSql.setString(7, ""); //FIXME: it should be SQL NULL
                        if (_debug > 0) {
                            debug(" session.getPrincipal() ="+session.getPrincipal());
                        }
                    }
                    //end added column       --SRI      
                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)  
                        ssoId = "";
                    preparedInsertSql.setString(8, ssoId);
                    //__MERGE 
                    preparedInsertSql.executeUpdate();
                    //((Connection)lobConn).commit(); DO NOT NEED COMMIT
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("inserted session, length = "+ in.length);
                    }
                    preparedInsertSql.close();
                    preparedInsertSql = null;

                    haErr.txEnd();
                } catch (SQLException e) {
                    closeStatement(preparedInsertSql);
                    preparedInsertSql = null;                    
                    haErr.checkError(e, connection);
                }                     
            }
               
        }
        catch ( SQLException e ) {
            //try{((Connection)lobConn).rollback();}catch(SQLException ee){} 
            try{connection.rollback();}catch(SQLException ee){}

            if(haErr.isPrimaryKeyViolation(e)) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Redirecting primary key violation from HA Store insertSessionNewBlob to update: " + e.getMessage());
                }
                this.updateSessionNewBlob(session, in, connection);
                return;
            }

            IOException ex1 = 
                (IOException) new IOException("Error from HA Store insertSessionNewBlob: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store insertSessionNewBlob " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }         
        finally {
            if (preparedInsertSql != null) {
                try {
                    preparedInsertSql.close();
                } catch (Exception ex) {}
            }
        }
    }
    
    /**
    * Update  a session into the HA store
    *
    * @param session 
    *   The session to store
    *
    * @param in
    *   The stream of serialized data from the session
    *
    * @param length
    *   The length of the stream  
    */
    private void updateSessionNewBlob(Session session, byte[] in,
        Connection connection) throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN updateSessionNewBlob");
        }
        HAManagerBase mgr = (HAManagerBase) this.getManager();
        WebModuleStatistics stats = mgr.getWebModuleStatistics();      
        boolean isMonitoringEnabled = ServerConfigReader.isMonitoringEnabled();
        //note: appid is newly added column      
        String updateSql = "UPDATE " + blobSessionTable +
              //" SET sessdata = ?, valid = ?, maxinactive = ?, lastaccess = ? , appid = ? , username = ?, ssoid=?" +
              " SET sessdata = ?, valid = ?, maxinactive = ?, lastaccess = ? , username = ?, ssoid=?" +
              //MERGE was next line
              //" SET sessdata = ?, valid = ?, maxinactive = ?, lastaccess = ? , appid = ? " +
              //"WHERE id = ?";
              "WHERE id = ? AND appid = ?";

        long startPrepTime = 0L;
        try {
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {
                try {                    
                    if(isMonitoringEnabled) {
                        startPrepTime = System.currentTimeMillis();
                    }                    
                    preparedUpdateSql = connection.prepareStatement(updateSql);
                    //preparedUpdateSql.setBinaryStream(1, in, buflength);
                    preparedUpdateSql.setBytes(1, in);
                    preparedUpdateSql.setString(2, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedUpdateSql.setInt(3, session.getMaxInactiveInterval());
                    preparedUpdateSql.setLong(4, ((StandardSession)session).getLastAccessedTimeInternal());
                    //added column
                    //preparedUpdateSql.setString(5, getApplicationId());
                    //end added column 

                    //added column for userName --sri  MERGE
                    if(session.getPrincipal() !=null){
                        if (_debug > 0) {
                            debug(" session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedUpdateSql.setString(5, session.getPrincipal().getName()); //null shpould be replaced with the username from session
                    } else {
                        //   preparedUpdateSql.setNull(6, java.sql.Types.VARCHAR); 
                        preparedUpdateSql.setString(5, ""); //FIXME Its hould be  SQL NULL
                        if (_debug > 0) {
                            debug(" session.getPrincipal() ="+session.getPrincipal());
                        }
                    }
                    //end added column       --sri    
                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)  
                        ssoId = "";
                    preparedUpdateSql.setString(6, ssoId);
                    //__MERGE 
                    preparedUpdateSql.setString(7, session.getIdInternal());
                    preparedUpdateSql.setString(8, getApplicationId());
                    if(isMonitoringEnabled) {
                        long endPrepTime = System.currentTimeMillis();
                        stats.processStatementPrepBlock(endPrepTime - startPrepTime);
                    }

                    long startTime = 0L;    
                    if(isMonitoringEnabled) {
                        startTime = System.currentTimeMillis();
                    }
                    preparedUpdateSql.executeUpdate();
                    if(isMonitoringEnabled) { 
                        long endTime = System.currentTimeMillis();
                        stats.processExecuteStatement(endTime - startTime);          
                    }
                    long commitStartTime = 0L;    
                    if(isMonitoringEnabled) {
                        commitStartTime = System.currentTimeMillis();
                    } 
                    /* DO NOT HAVE TO COMMIT
                    ((Connection)lobConn).commit();
                    if(isMonitoringEnabled) { 
                        long commitEndTime = System.currentTimeMillis();
                        stats.processCommit(commitEndTime - commitStartTime);          
                    } 
                     */
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("updated session, length = "+ in.length);
                    }
                    preparedUpdateSql.close();
                    preparedUpdateSql = null; 

                    haErr.txEnd();
                } catch (SQLException e) {
                    closeStatement(preparedUpdateSql);
                    preparedUpdateSql = null;                    
                    haErr.checkError(e, connection);
                }                    
            }

        }
        catch ( SQLException e ) {
            //try{((Connection)lobConn).rollback();}catch(SQLException ee){}
            try{connection.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store updateSessionNewBlob: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store updateSessionNewBlob: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally {
            if (preparedUpdateSql != null) {
                try {
                    preparedUpdateSql.close();
                } catch (Exception ex) {}
            }
        }    
    }
                       
    /**
    * Update  a session into the HA store
    * leave out the data; just update other fields
    *
    * @param session 
    *   The session to store
    *
    * @param in
    *   The stream of serialized data from the session
    *
    * @param length
    *   The length of the stream  
    */
    private void updateSessionNoDataNewBlob(Session session, byte[] in,
        Connection connection) throws IOException {
         
        //note: appid is newly added column         
        String updateSql = "UPDATE " + blobSessionTable +
              //" SET valid = ?, maxinactive = ?, lastaccess = ? , appid = ? ,username = ? , ssoid = ?" +
              " SET valid = ?, maxinactive = ?, lastaccess = ? , username = ? , ssoid = ?" +
              //MERGE was next line
              //" SET valid = ?, maxinactive = ?, lastaccess = ? , appid = ? " +
              //"WHERE id = ?";
              "WHERE id = ? AND appid = ?";

        try {
            
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {                 
                try {
                    preparedUpdateNoSessionSql = connection.prepareStatement(updateSql);
                    //leave out this column
                    //preparedUpdateNoSessionSql.setBinaryStream(1, in, buflength);
                    preparedUpdateNoSessionSql.setString(1, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedUpdateNoSessionSql.setInt(2, session.getMaxInactiveInterval());
                    preparedUpdateNoSessionSql.setLong(3, ((StandardSession)session).getLastAccessedTimeInternal());
                    //added column
                    //preparedUpdateNoSessionSql.setString(4, getApplicationId());
                    //end added column

                    //added column for userName --sri
                    if(session.getPrincipal() !=null){
                        if (_debug > 0) {
                            debug(" session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedUpdateNoSessionSql.setString(4, session.getPrincipal().getName()); 
                    } else
                        preparedUpdateNoSessionSql.setString(4,""); //null shpould be replaced with the username from session
                    //end added column       --sri      

                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)
                        ssoId = "";
                    preparedUpdateNoSessionSql.setString(5, ssoId);	
                    //__MERGE

                    preparedUpdateNoSessionSql.setString(6, session.getIdInternal());
                    preparedUpdateNoSessionSql.setString(7, getApplicationId());

                    preparedUpdateNoSessionSql.executeUpdate();
                    //connection.commit(); DO NOT NEED COMMIT
                    preparedUpdateNoSessionSql.close();
                    preparedUpdateNoSessionSql = null;

                    haErr.txEnd();
                } catch (SQLException e) {
                    closeStatement(preparedUpdateNoSessionSql);
                    preparedUpdateNoSessionSql = null;                    
                    haErr.checkError(e, connection);
                }                    
            }

        }
        catch ( SQLException e ) {
            try { connection.rollback(); } catch (Exception e1) {};
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store updateSessionNoDataNewBlob: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store updateSessionNoDataNewBlob: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }
        finally {
            if (preparedUpdateNoSessionSql != null) {
                try {
                    preparedUpdateNoSessionSql.close();
                } catch (Exception ex) {}
            }
        }     
    }    
  
    /**
    * Determine whether a session exists in the HA Store
    * @param session
    */
    public boolean sessionInStore(Session session) throws IOException  {  
        return sessionInStore(session, blobSessionTable);
    }
        
  
    /**
    * Determine whether a session exists in the HA Store
    * @param session
    * @param storeTable     
    */
    public boolean sessionInStore(Session session, String storeTable) throws IOException  {  
        //if HADB Health check fails then abort by throwing IOException
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            throw new IOException("Error from HA Store-sessionInStore: HADBHealthCheck reports HADB down");
        }        
        // Check the cache first.  Since we write every session to disk,
        // if it's in cache, it's in the store
        //FIXME after testing - next line replaces one following
        Session sess = this.getSessionFromMainStoreCache(session.getIdInternal());
        //Session sess = (Session)sessions.get(session.getIdInternal());
        if ( sess != null ) {
            return true;
        }

        Connection conn = getConnection(false);

        //String existsSql = "SELECT id FROM " + storeTable + " WHERE id = ?";
        String existsSql = "SELECT id FROM " + storeTable 
                + " WHERE id = ? AND appid = ?";
        ResultSet rs = null;
        boolean found = false;

        try {
            haErrExists.txStart();
            while ( ! haErrExists.isTxCompleted() ) {        
        
                try {
                    if (preparedExistsSql == null) {
                        preparedExistsSql = conn.prepareStatement(existsSql);
                    }

                    preparedExistsSql.setString(1, session.getIdInternal());
                    preparedExistsSql.setString(2, getApplicationId());
                    rs = preparedExistsSql.executeQuery();

                    if ( rs == null || ! rs.next() )  {
                        found = false;
                    }
                    else {
                        found = true;
                    }
                    ((Connection)conn).commit();
                    haErrExists.txEnd();
                } catch (SQLException e) {
                    //check for re-tryables
                    haErrExists.checkError(e, ((Connection)conn) );
                }
            }
        } catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAStore-sessionInStore: EXCEPTION HERE  " +
                            existsSql);
            }            
            try{((Connection)conn).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store-sessionInStore: " + e.getMessage()).initCause(e);
            throw ex1;            
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAStore-sessionInStore: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }         
        finally  {
            closeResultSet(rs);
        }

        return found;
    }   
  
    /**
    * Determine whether a session exists in the HA Store
    * @param session
    * @param conn
    */
    public boolean sessionInStore(Session session, Connection conn) throws IOException  {  
        return sessionInStore(session, conn, blobSessionTable);
    }  
  
    /**
    * Determine whether a session exists in the HA Store
    * called by valveSave
    * @param session
    * @param conn
    * @param storeTable
    */
    public boolean sessionInStore(Session session, Connection conn, String storeTable) throws IOException  {  
        //if HADB Health check fails then abort by throwing IOException
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            throw new IOException("Error from HA Store-sessionInStore: HADBHealthCheck reports HADB down");
        }        
        // Check the cache first.  Since we write every session to disk,
        // if it's in cache, it's in the store

        //get from the cache in the main store instance
        HAManagerBase mgr = (HAManagerBase)this.getManager();
        HAStore backgroundStore = (HAStore) mgr.getStore();
        BaseCache sesstbl = backgroundStore.getSessions();
        Session sess = (Session)sesstbl.get(session.getIdInternal());
        if ( sess != null ) {
          return true;
        }  

        //String existsSql = "SELECT id FROM " + storeTable + " WHERE id = ?";
        String existsSql = "SELECT id FROM " + storeTable 
                + " WHERE id = ? AND appid = ?";
        ResultSet rs = null;
        boolean found = false;
        
        try {
            haErrExists2.txStart();
            while ( ! haErrExists2.isTxCompleted() ) {                
                try {       
                    preparedExistsSql2 = conn.prepareStatement(existsSql);
                    preparedExistsSql2.setString(1, session.getIdInternal());
                    preparedExistsSql2.setString(2, getApplicationId());
                    //rs = executeStatement(preparedExistsSql2, true);
                    rs = preparedExistsSql2.executeQuery();                    

                    if ( rs == null || ! rs.next() )  {
                        found = false;
                    }
                    else {
                        found = true;
                    }
                    ((Connection)conn).commit();
                    preparedExistsSql2.close();
                    preparedExistsSql2 = null;
                    haErrExists2.txEnd();                
                } catch (SQLException e) {
                    closeStatement(preparedExistsSql2);
                    preparedExistsSql2 = null;                    
                    //check for re-tryables
                    haErrExists2.checkError(e, ((Connection)conn) );
                } 
            }
        } catch (SQLException e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.log(Level.FINEST,
                            "HAStore-sessionInStore: EXCEPTION HERE  " +
                            existsSql);
            }            
            try{((Connection)conn).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store-sessionInStore: " + e.getMessage()).initCause(e);
            throw ex1;            
        } catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HAStore-sessionInStore: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
        finally  {
            closeResultSet(rs);
            if (preparedExistsSql2 != null) {
                try {
                    preparedExistsSql2.close();
                } catch (SQLException e) {}
                preparedExistsSql2 = null;
            }
        }

        return found;
    }      
    
    /**
    * Insert a session into the HA store
    *
    * @param session 
    *   The session to store
    *
    * @param in
    *   The stream of serialized data from the session
    *
    * @param length
    *   The length of the stream  
    */
    private void insertSessionBlob(Session session, byte[] in) 
        throws IOException {
        //note: appid is newly added column
        String insertSql = "INSERT INTO " + blobSessionTable + " ("+
              "id, sessdata, valid, maxinactive, lastaccess, appid, username, ssoid) " +
              "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
              //MERGE was next 2 lines
              //"id, sessdata, valid, maxinactive, lastaccess, appid) " +
              //"VALUES (?, ?, ?, ?, ?, ?)";

        try {

            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {                
                try {                
                    if (preparedInsertSql == null) {
                        preparedInsertSql = conn.prepareStatement(insertSql);
                    }
                    preparedInsertSql.setString(1, session.getIdInternal());
                    preparedInsertSql.setBytes(2, in);
                    preparedInsertSql.setString(3, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedInsertSql.setInt(4, session.getMaxInactiveInterval());
                    preparedInsertSql.setLong(5, ((StandardSession)session).getLastAccessedTimeInternal());
                    //added column
                    preparedInsertSql.setString(6, getApplicationId());
                    //end added column      
                    //added column for userName --sri MERGE
                    if(session.getPrincipal() !=null){
                        if (_debug > 0) {
                            debug(" session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedInsertSql.setString(7, session.getPrincipal().getName()); 
                    } else
                        preparedInsertSql.setString(7, ""); //null shpould be replaced with the username from session
                    //end added column       --sri      
                                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)  
                        ssoId = "";
                    preparedInsertSql.setString(8, ssoId);
                    //__MERGE	

                    preparedInsertSql.executeUpdate();
                    //((Connection)lobConn).commit();  NO COMMIT NEEDED
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("inserted session, length = "+ in.length); 
                    }

                    haErr.txEnd();
                } catch (SQLException e) {
                    haErr.checkError(e, conn);
                }                    
            }
                  
        }
        catch ( SQLException e ) {
            //try{((Connection)lobConn).rollback();}catch(SQLException ee){} 
            try{conn.rollback();}catch(SQLException ee){}

            if(haErr.isPrimaryKeyViolation(e)) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Redirecting primary key violation from HA Store insertSessionNewBlob to update: " + e.getMessage());
                }
                this.updateSessionBlob(session, in);
                return;
            }
           
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store insertSessionBlob: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store insertSessionBlob: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }         
    }     
    
    /**
    * Update  a session into the HA store
    *
    * @param session 
    *   The session to store
    *
    * @param in
    *   The stream of serialized data from the session
    *
    * @param length
    *   The length of the stream  
    */
    private void updateSessionBlob(Session session, byte[] in) 
        throws IOException {
        //note: appid is newly added column      
        String updateSql = "UPDATE " + blobSessionTable +
              //" SET sessdata = ?, valid = ?, maxinactive = ?, lastaccess = ? , appid = ? , username=?, ssoid=?" +
              " SET sessdata = ?, valid = ?, maxinactive = ?, lastaccess = ? , username=?, ssoid=?" +              
              //MERGE was next line
              //" SET sessdata = ?, valid = ?, maxinactive = ?, lastaccess = ? , appid = ? " +
              //"WHERE id = ?";
              "WHERE id = ? AND appid = ?";

        try {
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {
                try {                
                    if (preparedUpdateSql == null) {
                        preparedUpdateSql = conn.prepareStatement(updateSql);
                    }
                    //preparedUpdateSql.setBinaryStream(1, in, buflength);
                    preparedUpdateSql.setBytes(1, in);
                    preparedUpdateSql.setString(2, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedUpdateSql.setInt(3, session.getMaxInactiveInterval());
                    preparedUpdateSql.setLong(4, ((StandardSession)session).getLastAccessedTimeInternal());
                    //added column
                    //preparedUpdateSql.setString(5, getApplicationId());
                    //end added column      
                   //added column for userName --sri MERGE
                    if(session.getPrincipal() !=null){
                        if (_debug > 0) {
                            debug(" session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedUpdateSql.setString(5, session.getPrincipal().getName()); 
                    } else
                        preparedUpdateSql.setString(5, ""); //FIXME: Should be SQL NULL
                   //end added column       --sri     
                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)  
                        ssoId = "";
                    preparedUpdateSql.setString(6, ssoId);
                    //__MERGE 
                    preparedUpdateSql.setString(7, session.getIdInternal());
                    preparedUpdateSql.setString(8, getApplicationId());

                    preparedUpdateSql.executeUpdate();
                    //((Connection)lobConn).commit(); COMMIT NOT NEEDED
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("updated session, length = "+ in.length);
                    }

                    haErr.txEnd();
                } catch (SQLException e) {
                    haErr.checkError(e, conn);
                }                     
            }
               
        }
        catch ( SQLException e ) {
            //try{((Connection)lobConn).rollback();}catch(SQLException ee){}
            try{conn.rollback();}catch(SQLException ee){}
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store updateSessionBlob: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store updateSessionBlob: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }         
    }              
    
    /**
    * Update  a session into the HA store
    * leave out the data; just update other fields
    *
    * @param session 
    *   The session to store
    *
    * @param in
    *   The stream of serialized data from the session
    *
    * @param length
    *   The length of the stream  
    */
    private void updateSessionNoDataBlob(Session session, byte[] in) 
        throws IOException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN updateSessionNoDataBlob");
        }

        //note: appid is newly added column 
        String updateSql = "UPDATE " + blobSessionTable +
              //" SET valid = ?, maxinactive = ?, lastaccess = ? , appid = ? , username= ? , ssoid=?" +
              " SET valid = ?, maxinactive = ?, lastaccess = ? , username= ? , ssoid=?" +
              //MERGE was next line
              //" SET valid = ?, maxinactive = ?, lastaccess = ? , appid = ? " +          
              //"WHERE id = ?";
              "WHERE id = ? AND appid = ?";

        try {
            haErr.txStart();

            while ( ! haErr.isTxCompleted() ) {                
                try {
                    if (preparedUpdateNoSessionSql == null) {
                        preparedUpdateNoSessionSql = conn.prepareStatement(updateSql);                        
                    }
                    //leave out this column
                    //preparedUpdateNoSessionSql.setBinaryStream(1, in, buflength);
                    preparedUpdateNoSessionSql.setString(1, ((StandardSession)session).getIsValid()?"1":"0");
                    preparedUpdateNoSessionSql.setInt(2, session.getMaxInactiveInterval());
                    preparedUpdateNoSessionSql.setLong(3, ((StandardSession)session).getLastAccessedTimeInternal());
                    //added column
                    //preparedUpdateNoSessionSql.setString(4, getApplicationId());
                    //end added column       
                    //added column for userName --sri MERGE
                    if(session.getPrincipal() !=null){
                        if (_debug > 0) {
                            debug(" session.getPrincipal().getName() ="+session.getPrincipal().getName());
                        }
                        preparedUpdateNoSessionSql.setString(4, session.getPrincipal().getName()); 
                    } else
                        preparedUpdateNoSessionSql.setString(4, ""); //FIXME: should be sql null
                    //end added column       --sri            
                                    //MERGE
                    String ssoId = ((HASession)session).getSsoId();
                    if (ssoId == null)  
                        ssoId = "";
                    preparedUpdateNoSessionSql.setString(5, ssoId);
                    //__MERGE
                    preparedUpdateNoSessionSql.setString(6, session.getIdInternal());
                    preparedUpdateNoSessionSql.setString(7, getApplicationId());

                    preparedUpdateNoSessionSql.executeUpdate();
                    //conn.commit(); COMMIT NOT NEEDED

                    haErr.txEnd();
                } catch (SQLException e) {
                    haErr.checkError(e, conn);
                }                     
            }
               
        }
        catch ( SQLException e ) {
            try{conn.rollback();}catch(SQLException ee){} 
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store updateSessionNoDataBlob: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch (HATimeoutException tex) {
            IOException ex2 =
                (IOException) new IOException("Timeout from HA Store updateSessionNoDataBlob: " + tex.getMessage()).initCause(tex);
            throw ex2;            
        }        
    }   

    /**
    * Create an input stream for the session that we can then pass to
    * the HA Store.
    *
    * @param session
    *   The session we are streaming
    *
    * @param length
    *   Will be set to the length of the input stream.
    *
    */
    protected BufferedInputStream getInputStream(Session session, IntHolder length)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ByteArrayInputStream bis = null;
        ObjectOutputStream oos = null;
        BufferedInputStream in = null;
        //FIXME removed after review
        //((StandardSession) session).notifyHttpSessionActivationListenersSessionWillPassivate();

        IOUtilsCaller utilsCaller = null;
        try {
            bos = new ByteArrayOutputStream();
            //HERCULES FIXME - for now reverting back
            //need to re-examine EJBUtils and related serialization classes
            //Bug 4832603 : EJB Reference Failover
            /*  was this
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             end was this */
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

            writeSession(session, oos);
            oos.close();
            oos = null;

            byte[] obs = bos.toByteArray();
            //for monitoring
            boolean isMonitoringEnabled = ServerConfigReader.isMonitoringEnabled();
            if(isMonitoringEnabled) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN HAStore>>getInputStream for monitoring");
                }
                HAManagerBase mgr = (HAManagerBase)this.getManager();
                WebModuleStatistics stats = mgr.getWebModuleStatistics();
                stats.processSessionSize(obs.length);
            }
            //end for monitoring
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
    
    /**
    * Create an input stream for the session that we can then pass to
    * the HA Store.
    *
    * @param session
    *   The session we are streaming
    *
    * @param length
    *   Will be set to the length of the input stream.
    *
    */
    protected byte[] getByteArray(Session session)
      throws IOException {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        //FIXME removed after review
        //((StandardSession) session).notifyHttpSessionActivationListenersSessionWillPassivate();

        IOUtilsCaller utilsCaller = null;
        byte[] obs;
        try {
            bos = new ByteArrayOutputStream();
            //HERCULES FIXME - for now reverting back
            //need to re-examine EJBUtils and related serialization classes
            //Bug 4832603 : EJB Reference Failover
            /*  was this
            oos = new ObjectOutputStream(new BufferedOutputStream(bos));
             end was this */
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

            writeSession(session, oos);
            oos.close();
            oos = null;

            obs = bos.toByteArray();
            //for monitoring
            boolean isMonitoringEnabled = ServerConfigReader.isMonitoringEnabled();
            if(isMonitoringEnabled) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("IN HAStore>>getInputStream for monitoring");
                }
                HAManagerBase mgr = (HAManagerBase)this.getManager();
                WebModuleStatistics stats = mgr.getWebModuleStatistics();
                stats.processSessionSize(obs.length);
            }
            //end for monitoring
        }
        finally {
            if ( oos != null )  {
                oos.close();
            }
        }

        return obs;
    }     
    
    /**
    * Execute a query that returns results.  This takes care of all the retry
    * logic needed to work against the Clustra HA Store
    *
    * @param stmt
    *   The statement you want to execute
    *
    * @param isQuery
    *   Set this to true if this is a query which returns a result set.
    *   Set this to false if this is a statement which has no results.
    *
    * @param errorMgr
    *   the instance of HAErrorManager to use 
    *
    * @return
    *   The result set returned from executing this query
    *
    * @exception
    *    IOException if there was a problem executing the query
    */  
    protected ResultSet executeStatement(PreparedStatement stmt, boolean isQuery, HAErrorManager errorMgr)
     throws IOException {
        ResultSet rst = null;

        try {
            errorMgr.txStart();
            while ( ! errorMgr.isTxCompleted() ) {
                try {
                    if ( isQuery ) {
                        rst = stmt.executeQuery();
                    }
                    else  {
                        stmt.executeUpdate();
                    }
                    errorMgr.txEnd();
                }
                catch ( SQLException e ) {
                    errorMgr.checkError(e, conn);
                    if (_debug > 0) {
                        debug("Got a retryable exception from HA Store: " + e.getMessage());
                    }
                }
            }
        }
        catch(SQLException e) {
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch ( HATimeoutException e ) {
            IOException ex1 = 
                (IOException) new IOException("Timeout from HA Store " + e.getMessage()).initCause(e);
            throw ex1;            
        }

        return rst;    
    }    

    /**
    * Execute a query that returns results.  This takes care of all the retry
    * logic needed to work against the Clustra HA Store
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
    protected ResultSet executeStatement(PreparedStatement stmt, boolean isQuery)
     throws IOException {
        ResultSet rst = null;

        try {
            haErr.txStart();
            while ( ! haErr.isTxCompleted() ) {
                try {
                    if ( isQuery ) {
                        rst = stmt.executeQuery();
                    }
                    else  {
                        stmt.executeUpdate();
                    }
                    haErr.txEnd();
                }
                catch ( SQLException e ) {
                    haErr.checkError(e, conn);
                    if (_debug > 0) {
                        debug("Got a retryable exception from HA Store: " + e.getMessage());
                    }
                }
            }
        }
        catch(SQLException e) {
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store: " + e.getMessage()).initCause(e);
            throw ex1;            
        }
        catch ( HATimeoutException e ) {
            IOException ex1 = 
                (IOException) new IOException("Timeout from HA Store " + e.getMessage()).initCause(e);
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
        } 
        catch(SQLException e) {
        }
    }

    /**
    * Helper routine that cleans up statements and cached connection
    * and resets sessions cache
    */    
    public void cleanup() {
        closeStatements();
        closeConnection();
        //this.setSessions(new Hashtable());
        this.setSessions(new BaseCache());
        sessions.init(_maxBaseCacheSize, _loadFactor, null);
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
        conn = null;
        ConnectionUtil util = this.getConnectionUtil();
        if(util != null) {
            util.clearCachedConnection();
        }
    }
  
    protected void closeStatements() {
        PreparedStatement[] statements = 
           {preparedKeysSql, preparedExpiredKeysSql, preparedRemoveExpiredKeysSql, preparedSizeSql, preparedDualSql, 
           preparedLoadSql, preparedInsertSql, preparedRemoveSql, preparedClearSql, 
           preparedUpdateSql, preparedExistsSql, preparedExistsSql2, preparedUpdateNoSessionSql};
        for(int i=0; i<statements.length; i++) {
            PreparedStatement nextStatement = 
                (PreparedStatement) statements[i];
            closeStatement(nextStatement);
        }
        this.clearStatementReferences(); 
    }
    
    protected void clearStatementReferences() {
        preparedKeysSql = null;
        preparedExpiredKeysSql = null;
        preparedRemoveExpiredKeysSql = null;
        preparedSizeSql = null;
        preparedDualSql = null;
        preparedLoadSql = null; 
        preparedInsertSql = null;
        preparedRemoveSql = null;
        preparedClearSql = null; 
        preparedUpdateSql = null;
        preparedExistsSql = null;
        preparedExistsSql2 = null;
        preparedUpdateNoSessionSql = null;         
    }
  
    protected void closeStatement(PreparedStatement stmt) {
        if (stmt != null) {
            try
            {
              stmt.close();
            } catch (java.sql.SQLException ex) {}
        }
    }
  
    //FIXME this is for junit test
    //probably remove it
    public void privateForTestSetConnection(Connection connection) {
        conn = connection;
    }
  
    public Connection privateGetConnection(boolean autocommit) throws IOException {
        return this.getConnection(autocommit);
    }
  
    /**
    * return a HADBConnectionGroup from the pool
    */    
    protected HADBConnectionGroup getConnectionsFromPool() throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        return util.getConnectionsFromPool();
    } 
    
    /**
    * return a HADBConnectionGroup from the pool
    * @param autoCommit
    */    
    protected HADBConnectionGroup getConnectionsFromPool(boolean autoCommit) throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        return util.getConnectionsFromPool(autoCommit);
    }    
  
    /**
     * Get a connection that has been validated
     * Returns <code>null</code> if the connection could not be established.
     *
     * @return <code>Connection</code> if the connection suceeded
     */
    protected Connection getConnectionValidated(boolean autoCommit) throws IOException {
        Connection connection = null;
        boolean keepTrying = true;
        int numRetries = 3;
        int count = 1;
        while(keepTrying) {
            //do the attempt
            connection = this.getConnection(autoCommit);
            boolean connectionValid = this.validateConnection(connection);
            if(connectionValid) {
                keepTrying = false;
            } else {
                try {
                    this.cleanupConnectionCaches();
                } catch (Exception ex) {}
                //this forces getting a new connection
                //conn is an instVar
                conn = null;
                this.threadSleep(100L);
            }
            count++;
            if(count == numRetries)
                keepTrying = false;
        }
        if(conn == null) {
            throw new IOException("Could not obtain viable connection");
        }
        try {
            conn.setAutoCommit(autoCommit);
        } catch ( SQLException ex ) {
        }
        return conn;
    }
  
    protected void cleanupConnectionCaches() {
        this.closeConnection();
        /*
        if(conn != null) {
            try {
                conn.close();
            } catch (Exception ex) {}
        }
         */
	this.closeStatements();
        //this.clearStatementReferences();        
        //done in closeConnection()
        conn = null;        
    }
  
    /**
     * this method runs an sql query against the connection
     * to test its viability
     */    
    protected boolean validateConnection(Connection connection) {
        boolean result = true;
        try {
            //this.getSizeForConnValidation(connection);
            this.testQueryForConnValidation(connection);
        } catch (IOException ex) {
            result = false;
        } catch (java.lang.NullPointerException ex1) {
            result = false;
        }
        return result;
    }
    
    /**
     * Return the number of Sessions present in this Store.
     *
     * This method is used simply to validate a connection
     *
     * @exception IOException if an input/output error occurs
     */
    public int getSizeForConnValidation(Connection connection) throws IOException {
        if (_debug > 0) {
            debug("in getSizeForConnValidation");
        }

        int size = 0;
        //only want count for sessions for this app
        String sizeSql = "SELECT COUNT(id) FROM " + blobSessionTable
            + " WHERE appid = '" + getApplicationId() + "'";
        ResultSet rst = null;

        try {
            if (preparedSizeSql == null) {
                preparedSizeSql = connection.prepareStatement(sizeSql);
            }

            rst = executeStatement(preparedSizeSql, true);

            if (rst.next()) {
                size = rst.getInt(1);
            }
            ((Connection)connection).commit();
        }
        catch(SQLException e) {
            try{((Connection)connection).rollback();}catch(SQLException ee){}  
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store: " + e.getMessage()).initCause(e);
            throw ex1;              
            
        }
        finally {
            closeResultSet(rst);
        }

        return size;
    }
    
    public int testQueryForConnValidation(Connection connection) throws IOException {
        try {
            connection.getMetaData();
            return 1;
        }
        catch(SQLException ex) {
            IOException ex1 = 
                (IOException) new IOException("Error from HA Store Connection Validation: " + ex.getMessage()).initCause(ex);
            throw ex1;
        }
    }
    
    protected void threadSleep(long sleepTime) {

        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            ;
        }

    }      
  
    /**
     * Check the connection associated with this store, if it's
     * <code>null</code> or closed try to reopen it.
     * Returns <code>null</code> if the connection could not be established.
     *
     * @return <code>Connection</code> if the connection suceeded
     */
    protected Connection getConnection() throws IOException {
        return getConnection(true); 
    } 
  
    /**
     * Check the connection associated with this store, if it's
     * <code>null</code> or closed try to reopen it.
     * set autoCommit to autoCommit
     * Returns <code>null</code> if the connection could not be established.
     *
     * @return <code>Connection</code> if the connection suceeded
     */  
    protected Connection getConnection(boolean autoCommit) throws IOException {
        ConnectionUtil util = this.getConnectionUtil();
        //get returned connection into cached connection conn
        conn = util.getConnection(autoCommit);
        return conn;
    }
    
    /**
     * not intended for public use
     * close the cached connection
     */ 
    public void privateCloseCachedConnection() {
        if(conn != null) {
            try {
                conn.close();
            } catch (Exception e) {}
        }
    }
    
}
