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
 * HAManagerBase.java
 *
 * Created on December 5, 2002, 5:08 PM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.IOException;
import com.sun.enterprise.web.MonitorStatsCapable;
import com.sun.enterprise.web.ShutdownCleanupCapable;
import org.apache.catalina.session.PersistentManagerBase;
import org.apache.catalina.Session;
import org.apache.catalina.LifecycleException;
import java.util.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.ee.web.initialization.ServerConfigReader;


/**
 *
 * @author  lwhite
 */
public abstract class HAManagerBase extends PersistentManagerBase implements ShutdownCleanupCapable,
    MonitorStatsCapable, CleanupCapable {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null; 
    
    static {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
    }

    /** create and return a new session; delegates to session factory */
    protected Session createNewSession() {
        Session sess = getSessionFactory().createSession(this);
        return sess;
    }
    
    /**
     * Get new session class (new method in ManagerBase that we over-ride
     * instead of createNewSession()
     */
    public Session createEmptySession() {
        Session sess = getSessionFactory().createSession(this);
        return sess;
    }    
    
    /** insure that the store has a live cached connection */
    protected void insureValidConnection() throws java.io.IOException {
        HAStore store = (HAStore) getStore();
        store.getConnectionValidated(false);
    }
    
    /**
     * Invalidate all sessions that have expired.
     */
    protected void processExpires() {
        //adding step to first process stale cached sessions
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        } 
        try {
            this.insureValidConnection();
        } catch (java.io.IOException ex) {}
        if ((this.getStore() != null)
            && (this.getStore() instanceof HAStore)) {
            ((HAStore) this.getStore()).processStaleCachedSessions();
        }        

        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        try {
            this.insureValidConnection();
        } catch (java.io.IOException ex) {}
        super.processExpires();
    }    
    
    /**
     * Called by the background thread after active sessions have
     * been checked for expiration, to allow sessions to be
     * swapped out, backed up, etc.
     */
    //FIXME also need to better handle the exception here
    //over-ridden class doesn't have the exception
    public void processPersistenceChecks() {
        //we are being very cautious in this version
        //insuring the validity of the cached connection
        //before each background activity
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }         
        try {
            this.insureValidConnection();
        } catch (java.io.IOException ex) {} 
        processMaxIdleSwaps();

        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        try {
            this.insureValidConnection(); 
        } catch (java.io.IOException ex) {}             
        processMaxActiveSwaps();
        
        if( !EEHADBHealthChecker.isOkToProceed() ) {
            return;
        }        
        try {
            this.insureValidConnection();
        } catch (java.io.IOException ex) {}             
        processMaxIdleBackups();     
    }    

    
    /**
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     * This method checks the persistence store if persistence is enabled,
     * otherwise just uses the functionality from ManagerBase.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */
    public Session findSession(String id) throws IOException {
        WebModuleStatistics stats = this.getWebModuleStatistics();
        //SJSAS 6406580 START
        if(!this.isSessionIdValid(id)) {
            if(isMonitoringEnabled()) {
                stats.processCacheHit(false);
            }            
            return null;
        }
        //SJSAS 6406580 END        
        Session session = findSessionFromCacheOnly(id);
        if (session != null) {
            if(isMonitoringEnabled()) {
                stats.processCacheHit(true);
            }
            return (session);
        }

        // See if the Session is in the Store
        if(isMonitoringEnabled()) {
            stats.processCacheHit(false);
        }
        session = swapIn(id);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN HAManagerBase>>findSession: returned sess = " + (BaseHASession)session);        
        }
        return (session);

    }
    
    /**
     * This method is a clone of the ManagerBase>>findSession.
     * It is used here to avoid problems with calling super.findSession
     * from this class.
     * Return the active Session, associated with this Manager, with the
     * specified session id (if any); otherwise return <code>null</code>.
     *
     * @param id The session id for the session to be returned
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     * @exception IOException if an input/output error occurs while
     *  processing this request
     */  
    public Session findSessionFromCacheOnly(String id) throws IOException {

        if (id == null)
            return (null);
        synchronized (sessions) {
            Session session = (Session) sessions.get(id);
            return (session);
        }

    }
    
   //SJSAS 6406580 START 
   /**
   * does the remove of session
   *
   * @param sessionId 
   *   The session id to remove
   */    
    protected void doRemove(String sessionId) {
        
        long startTime = 0L;
        StorePool storePool = this.getStorePool();
        HAStorePoolElement haStore = null;

        try
        {                        
            haStore = (HAStorePoolElement) storePool.take();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("GOT HAStore from pool");
            }
            try
            {
                haStore.setManager(this);
                //_logger.finest("HAStore has manager = " + this);
                //_logger.finest("ENTERING haStore.doRemove");
                ((HAStore)haStore).doRemove(sessionId);
                //get the main store instance
                HAStore backgroundStore = (HAStore) this.getStore();
                //update this one's cache
                backgroundStore.getSessions().remove(sessionId);
                //_logger.finest("FINISHED haStore.doRemove");
            } catch (Exception ex) {
                //FIXME handle exception from remove
                //log error but allow processing to continue in spite of failure
                ex.printStackTrace();
            } finally {
                haStore.setManager(null);
                if(haStore != null) {
                    try
                    {
                        storePool.put((StorePoolElement)haStore);
                        //_logger.finest("PUT HAStore into pool");
                    } catch (InterruptedException ex1) {}
                }                    
            }                
        } catch (InterruptedException ex) {
            //FIXME handle exception from pool take
        } 
    }
    //SJSAS 6406580 END    
    
    protected boolean isMonitoringEnabled() {
        return ServerConfigReader.isMonitoringEnabled();
    }
    
    /** subclasses will over-ride this method */
    public String getMonitorAttributeValues() {
        return "testing..1..2..3";
    }
    
    public int getSessionsCacheSize() {
        return sessions.size();
    }
    
    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception IllegalStateException if this component has not been started
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
   public void stop() throws LifecycleException {
       super.stop();
       this.closeAllConnections();
   }
    
    public int doShutdownCleanup() {
        return this.closeAllConnections();
    }
    
    public int closeAllConnections() {
        //System.out.println("In HAManagerBase>>closeAllConnections");
        int count = 0;
        int iterations = 0;
        Set connections = _connectionsMap.keySet();
        Iterator it = connections.iterator();
        while(it.hasNext()) {
            iterations++;
            //System.out.println("closeAllConnections:iteration" + iterations);
            Connection nextConn = (Connection) it.next();
            //System.out.println("nextConn=" + nextConn);
            if(nextConn != null) {
                try {
                    nextConn.close();
                    count++;
                } catch (SQLException ex) {
                    //if any errors; give up trying to close other connections
                    break;
                }
            }
        }
        //System.out.println("manager closed " + count + " connections");
        return count;
    }
    
    /**
     * Clear all sessions from the Store.
     * extend to also close the store's cached connection
     */
    public void clearStore() {        
        super.clearStore();
        this.doCloseCachedConnection();
    }    
    
    public void doCloseCachedConnection() {
        HAStore theStore = (HAStore) this.getStore();
        theStore.closeConnection();
    }
    
    /** return the session factory */
    SessionFactory getSessionFactory() {
        return _sessionFactory;
    }
    
    /**
     * set the session factory
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        _sessionFactory = sessionFactory;
    } 
    
    /** return the web module statistics */
    public WebModuleStatistics getWebModuleStatistics() {
        return _statistics;
    }
    
    /**
     * set the web module statistics
     * @param statistics
     */    
    public void setWebModuleStatistics(WebModuleStatistics statistics) {
        _statistics = statistics;
    }
    
    public Map getConnectionsMap() {
        return _connectionsMap;
    }
    
    public synchronized void putConnection(Connection conn) {
        _connectionsMap.put(conn, null);
    }
    
    protected void removeSessionIdsFromManagerCache(String[] sessionIds) {
        synchronized(sessions) {
            for(int i=0; i<sessionIds.length; i++) {
                String nextId = sessionIds[i];
                // Take it out of the cache - remove does not handle nulls
                if(nextId != null) {
                    sessions.remove(nextId);
                }
            }
        }
    }    
    
    public void removeSessionFromManagerCache(Session session) {
        if(session == null) {
            return;
        }
	synchronized (sessions) {
            sessions.remove(session.getId());
        }
    }
    
    public void resetMonitorStats() {
        WebModuleStatistics stats = this.getWebModuleStatistics();
        stats.resetStats();
    }
    
    //SJSAS 6406580 START
    /** return the StorePool */
    public StorePool getStorePool() {
        return _pool;
    }
    
    /** set the StorePool */
    public void setStorePool(StorePool pool) {
        _pool = pool;
    }
    //SJSAS 6406580 END 
    
    public void doCleanup() {
        HAStore theStore = (HAStore)this.getStore();
        theStore.closeStatements();
    }
    
    protected SessionFactory _sessionFactory = null;
    protected WebModuleStatistics _statistics = new WebModuleStatistics();
    //private Map _connectionsMap = Collections.synchronizedMap( new IdentityHashMap(50));   
    private Map _connectionsMap = Collections.synchronizedMap( new WeakHashMap(50));        
    //SJSAS 6406580 START
    StorePool _pool = null;
    //SJSAS 6406580 END    
    
}
