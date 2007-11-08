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
 * EEHADBHealthChecker.java
 *
 * Created on June 9, 2004, 9:52 AM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import org.apache.catalina.LifecycleException;
import com.sun.enterprise.admin.event.AdminEventListenerRegistry;
import com.sun.enterprise.admin.event.availability.AvailabilityServiceEvent;
import com.sun.enterprise.admin.event.availability.AvailabilityServiceEventListener;
import com.sun.enterprise.security.store.IdentityManager;
import com.sun.enterprise.server.ApplicationServer;
import com.sun.enterprise.server.ServerContext;
import com.sun.enterprise.web.HealthChecker;
import com.sun.enterprise.web.SchemaUpdater;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.web.WebContainer;
import com.sun.enterprise.web.WebContainerStartStopOperation;
import com.sun.ejb.Container;
import com.sun.ejb.containers.StatefulSessionContainer;
//import com.sun.hadb.jdbc.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import com.sun.enterprise.ee.web.sessmgmt.CleanupCapable;
import com.sun.ejb.spi.sfsb.store.SFSBStoreManager;
import com.sun.hadb.dbstate.DbState;

/**
 *
 * @author  lwhite
 */
public class EEHADBHealthChecker implements HealthChecker, Runnable {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null;

    /**
     * The flag which reflects whether HADB is operational
     */    
    private static boolean _hadbOperationalFlag = true;     
    
    /**
     * The flag which reflects whether HADB health check is enabled
     * This is cached and updated by the health check thread
     */    
    private BooleanWrapper _healthCheckEnabledFlag = new BooleanWrapper();    
    
    /**
     * The singleton instance of EEHADBHealthChecker
     */    
    private static EEHADBHealthChecker _soleInstance = null;
    
    /**
     * The flag which reflects whether HADB is operational
     */    
    private static int _sleepIntervalSeconds = 5;    
    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;    
    
    /**
     * The background thread.
     */
    protected Thread thread = null;
    
    /**
     * The background thread completion semaphore.
     */
    protected boolean threadDone = false;    
    
    /**
     * Name to register for the background thread.
     */
    protected String _threadName = "EEHADBHealthChecker";
    
    /** containerId vs. StoreManager */
    protected final static Hashtable _containerId2StoreManager = new Hashtable();    
    
    /**
     * Name to register for the background thread.
     */
    protected WebContainer _webContainer = null;
    
    /**
     * a runtime health check error has been reported once
     */
    protected static boolean runtimeHealthCheckExceptionReported = false;    
    
    /**
     * the hadb management agent connection url
     */
    protected String hadbAgentConnectionUrl = null;
    
    /**
     * an hadb management agent connection url error was reported once
     */
    protected boolean hadbAgentConnectionUrlErrorReported = false; 
    
    /**
     * the hadb database name
     */
    protected String hadbDatabaseName = null;
    
    /**
     * an hadb database name error was reported once
     */
    protected boolean hadbDatabaseNameErrorReported = false;
    
    /**
     * the hadb agent password
     */
    protected String hadbAgentPassword = null;
    
    /**
     * an hadb agent password error was reported once
     */
    protected boolean hadbAgentPasswordErrorReported = false;    
    
    /**
     * the hadb agent hosts list
     */
    protected String hadbAgentHosts = null;
    
    /**
     * an hadb agent hosts error was reported once
     */
    protected boolean hadbAgentHostsErrorReported = false; 
    
    /**
     * the hadb agent port
     */
    protected String hadbAgentPort = null;
    
    /**
     * an hadb agent port error was reported once
     */
    protected boolean hadbAgentPortErrorReported = false;
    
    /**
     * the ha store health check interval in seconds
     */
    protected int haStoreHealthcheckIntervalInSeconds = -1;
    
    /**
     * the admin listener
     */    
    protected AvailabilityServiceEventListener adminListener = null;
    
    //configuration attribute names
    
    protected static final String HEALTH_CHECK_INTERVAL 
        = "ha-store-healthcheck-interval-in-seconds";
    
    protected static final String HEALTH_CHECK_ENABLED
        = "ha-store-healthcheck-enabled";
    
    protected static final String HA_AGENT_HOSTS
        = "ha-agent-hosts";
    
    protected static final String HA_AGENT_PORT
        = "ha-agent-port"; 
    
    protected static final String HA_AGENT_CONNECTION_URL
        = "ha-agent-connection-url";    
    
    protected static final String HA_STORE_NAME
        = "ha-store-name";
    
    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(_threadName);
    }    
    
    /** Creates a new instance of EEHADBHealthChecker */
    /*
    public EEHADBHealthChecker() {
        _threadName = "EEHADBHealthChecker"; 
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }         
    }
     */ 
    
    /** Creates a new instance of EEHADBHealthChecker */
    public EEHADBHealthChecker(WebContainer webContainer) {
        _threadName = "EEHADBHealthChecker";
        _webContainer = webContainer;
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }         
    } 
    
    /** Return the singleton instance
     *  lazily creates a new instance of EEHADBHealthChecker if not created yet
     */
    public static EEHADBHealthChecker createInstance(WebContainer webContainer) {
        if (_soleInstance == null) {
            _soleInstance = new EEHADBHealthChecker(webContainer);
        }
        return _soleInstance;
    }
    
    /** Return the singleton instance
     *  return null if not created yet - should not happen
     */
    public static EEHADBHealthChecker getInstance() {
        return _soleInstance;
    }
    
    //Dynamic reconfig

    public void registerAdminEvents() {    
        adminListener = 
            new EEAvailabilityServiceEventListener(this);
        AdminEventListenerRegistry.addEventListener(AvailabilityServiceEvent.eventType, adminListener);
    }

    public void unregisterAdminEvents() {
        AdminEventListenerRegistry.removeEventListener(adminListener);
        adminListener = null;
    }
    
    void resetConfigAttributes(ArrayList changedAttributes) {
        if(changedAttributes.size() == 0) {
            return;
        }
        synchronized(this) {
            for(int i=0; i<changedAttributes.size(); i++) {
                ConfigChangeElement nextElement = 
                    (ConfigChangeElement)changedAttributes.get(i);
                String nextAttrName = nextElement.getName();
                
                if(nextAttrName.equals(HA_STORE_NAME)) {
                    hadbDatabaseName = (String)nextElement.getValue();
                    hadbDatabaseNameErrorReported = false;
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("EEHADBHealthChecker:resetting ha database name = " 
                            + hadbDatabaseName);
                    }                    
                    continue;
                }
                
                if(nextAttrName.equals(HA_AGENT_HOSTS)) {
                    hadbAgentHosts = (String)nextElement.getValue();
                    hadbAgentHostsErrorReported = false;
                    hadbAgentConnectionUrl = null;
                    hadbAgentConnectionUrlErrorReported = false;
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("EEHADBHealthChecker:resetting ha agent hosts = " 
                            + hadbAgentHosts);
                    }                     
                    continue;
                } 
                
                if(nextAttrName.equals(HA_AGENT_PORT)) {
                    hadbAgentPort = (String)nextElement.getValue();
                    hadbAgentPortErrorReported = false;
                    hadbAgentConnectionUrl = null;
                    hadbAgentConnectionUrlErrorReported = false;
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("EEHADBHealthChecker:resetting ha agent port = " 
                            + hadbAgentPort);
                    }                    
                    continue;
                }

                if(nextAttrName.equals(HA_AGENT_CONNECTION_URL)) {
                    hadbAgentConnectionUrl = (String)nextElement.getValue();
                    hadbAgentConnectionUrlErrorReported = false;
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("EEHADBHealthChecker:resetting ha agent connection url = " 
                            + hadbAgentConnectionUrl);
                    }                               
                    continue;
                }                
                
                if(nextAttrName.equals(HEALTH_CHECK_ENABLED)) {
                    getHealthCheckEnabledWrapper().setValue(((Boolean)nextElement.getValue()).booleanValue());
                    if(_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("EEHADBHealthChecker:resetting healthCheckEnabled = " 
                            + getHealthCheckEnabledWrapper().getValue());
                    }                  
                    continue;
                }
                
                if(nextAttrName.equals(HEALTH_CHECK_INTERVAL)) {
                    int newInterval 
                        = ((Integer)nextElement.getValue()).intValue();
                    if(newInterval > 0) {
                        haStoreHealthcheckIntervalInSeconds = newInterval;
                        if(_logger.isLoggable(Level.FINEST)) {
                            _logger.finest("EEHADBHealthChecker:resetting haStoreHealthcheckIntervalInSeconds = " 
                                + newInterval);
                        }                        
                    }
                    continue;
                }                
                
            }
        }
    }    
    
    //end Dynamic reconfig
        
    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception IllegalStateException if this component has already been
     *  started
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        
        //initialize the wrapper
        initializeHealthCheckEnabledFlag();
        
        //do not start if HADB health check is not enabled
        //FIXME may want to reconsider this decision
        /* for now taking out
        if(!isHealthCheckingEnabled()) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Starting - HADB health checking not enabled");
            }
            return;
        }
         */
        if(started) {
            return;
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HADB health checking enabled");
        }
       
        this.registerAdminEvents();
        
        // Start the background health-check thread
        threadStart();
        started = true;
    }
    
    /**
     * Start the background thread that will periodically check
     * the health of HADB.
     */
    protected void threadStart() {
        if (thread != null)
            return;

        threadDone = false;
        thread = new Thread(this, getThreadName());
        thread.setDaemon(true);
        thread.start();
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
        if(!started) {
            return;
        }
        this.unregisterAdminEvents();
        // Stop the background health-check thread       
        threadStop();
        started = false;
    }
    
    boolean isStarted() {
        return started;
    }
    
    Thread getThread() {
        return thread;
    }    
    
    /**
     * Stop the background thread that is periodically checking for
     * session timeouts.
     */
    protected void threadStop() {
        if (thread == null)
            return;

        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }

        thread = null;
    }    
    
    /**
     * Sleep for the duration specified by the <code>_sleepIntervalSeconds</code>
     * property.
     */
    protected void threadSleep() {
        _sleepIntervalSeconds = 
            this.getHaStoreHealthcheckIntervalInSeconds();
        try {
            Thread.sleep(_sleepIntervalSeconds * 1000L);            
        } catch (InterruptedException e) {
            ;
        }
    } 
    
    /**
     * The background thread that checks for HADB health.
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            doHADBHealthCheck();
        }
    } 

    /**
     * Perform the HADB health check and take action as appropriate
     * defaults to doing full (not quick) check
     */    
    protected boolean doHADBHealthCheck() {
        return doHADBHealthCheck(false);
    }
    
    /**
     * Perform the HADB health check and take action as appropriate
     * @param quickCheck - skip shutdown logic
     */
    protected boolean doHADBHealthCheck(boolean quickCheck) {
        //if health checking not enabled skip all checking; return true
        if(!isHealthCheckingEnabled()) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("health check disabled - skipping");
            }        
            return true;
        }
        //to avoid incorrect log messages we need to check this
        //both here and after isDatabaseOk()
        if(runtimeHealthCheckExceptionReported) {
            return true;
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("health check enabled - entering isDatabaseOk()()");
        }
        boolean hadbAlive = this.isDatabaseOk();
        boolean lastHADBState = isHADBOperational();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HADBHealthCheck: HADB is alive: " + hadbAlive);
        }
        //DbState may have reported non-operational state
        //which was really -20 (mis-configuration) so abort and return
        //true in this case (for now)
        //or if an HADB agent mis-configuration was detected
        if(runtimeHealthCheckExceptionReported || this.hasAgentConfigErrorOccurred()) {
            return true;
        }    
        
        //now check for existence of HADB tables
        //but only if HADB was previously not alive
        //and HADB is reported alive (i.e. hadb is newly healthy
        //otherwise there is no point
        boolean tablesExist = true;
        if(!lastHADBState && hadbAlive) {
            tablesExist = doHADBTablesExist();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("HADBHealthCheck: HADB tables exist: " + tablesExist);
            }
        }   
        boolean healthyWithTables = hadbAlive && tablesExist;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("HADBHealthCheck: HADB healthyWithTables: " + healthyWithTables);
        }        
        EEHADBHealthChecker.setHADBOperational(healthyWithTables);
        //EEHADBHealthChecker.setHADBOperational(hadbAlive);

        this.issueHealthWarning(lastHADBState, hadbAlive, tablesExist);
        //only do cleanup is HADB is newly unhealthy
        if(lastHADBState && !hadbAlive) {
            //do web and ebj container side connection cleanup
            //skip if doing quickCheck
            if(!quickCheck) {
                this.doHADBShutdownCleanup();
            }
        }
        return isHADBOperational();

    }
    
    private void issueHealthWarning(boolean lastHADBState, boolean hadbAlive, boolean tablesExist) {
        //if HADB is newly unhealthy warn
        if(lastHADBState && !hadbAlive) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.hadbUnhealthy");            
        } 
        //if HADB is newly healthy also warn
        if(!lastHADBState && hadbAlive) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.hadbHealthy");
            if(!tablesExist) {
                _logger.log(Level.WARNING,
                    "hadbhealthchecker.hadbTablesMissing");                
            }
        }        
    }
    
    public static boolean doHADBTablesExist() {
        boolean result = true;
        ServerContext serverContext = ApplicationServer.getServerContext(); 
        SchemaUpdater updater =
            serverContext.getPluggableFeatureFactory().getSchemaUpdater();
        
        try {
            //updater.init();
            result = updater.doTablesExist();
        } catch (SQLException ex) {
            result = false;
        } catch (ClassNotFoundException ex1) {
            result = false;
        }
        return result;
    }
    
    public static boolean doQuickHADBHealthCheck() {
        boolean result = true;
        //if HADB already marked unhealthy return that
        if( !isHADBOperational() ) {
            result = false;
        } else {
            //else must do the check
            if (getInstance() != null) {
                result = getInstance().doHADBHealthCheck();
            }
        }
        return result;
    }

    /**
     * Perform the HADB related cleanup
     * i.e. closing out connections
     */    
    private void doHADBShutdownCleanup() {
        //do web container side connection cleanup
        if(getWebContainer() != null) {
            WebContainerStartStopOperation startStopOperation =
                this.getWebContainer().getWebContainerStartStopOperation();
            ArrayList shutdownCleanupCapablesList = startStopOperation.doPreStop();
            startStopOperation.doPostStop(shutdownCleanupCapablesList);              
        }

        //do ejb container side connection cleanup 
        //Collection sfsbStoreManagers = this.getSFSBStoreManagers();
        //this.closeCachedConnections(sfsbStoreManagers);
        Collection haSFSBStoreManagers = this.getHASFSBStoreManagers();
        this.closeCachedConnections(haSFSBStoreManagers);        
    }

    /**
     * get all SFSBStoreManagers
     */
    /* remove later
    private Collection getSFSBStoreManagers() {
        Collection result = new ArrayList();
        ApplicationRegistry appRegistry = ApplicationRegistry.getInstance();
        Collection ejbApps = appRegistry.privateGetAllEjbContainers();
        Iterator it = ejbApps.iterator();
        while(it.hasNext()) {
            Container nextContainer = (Container)it.next();
            if(nextContainer instanceof StatefulSessionContainer) {
                result.add( ((StatefulSessionContainer)nextContainer).getSFSBStoreManager());
            }
        }
        return result;
    }
     */

    /**
     * close all cached connections on the SFSBStoreManagers
     * @param sfsbStoreManagers (Collection)
     */     
    private void closeCachedConnections(Collection sfsbStoreManagers) {
        if(sfsbStoreManagers == null) {
            return;
        }
        Iterator it = sfsbStoreManagers.iterator();
        while(it.hasNext()) {
            Object nextMgr = it.next();
            if(nextMgr instanceof CleanupCapable) {
                ((CleanupCapable)nextMgr).doCleanup();
            }
        }
    }
    
    public static void addHASFSBStoreManager(String contId, SFSBStoreManager storeManager) {
        _containerId2StoreManager.put(contId, storeManager);
    }
    
    public static SFSBStoreManager removeHASFSBStoreManager(String contId) {
        return (SFSBStoreManager) _containerId2StoreManager.remove(contId);
    }
    
    private static SFSBStoreManager getHASFSBStoreManager(String containerId) {
        return (SFSBStoreManager) _containerId2StoreManager.get(containerId);
    }
    
    /**
     * Returns all the HASFSBStoreManagers available in this registry.
     *
     * @return   a collection of HASFSBStoreManagers
     */
    private static Collection getHASFSBStoreManagers() {
        Collection storeManagers = null;
        if (_containerId2StoreManager != null) {
            storeManagers = _containerId2StoreManager.values();
        }
        return storeManagers;
    }     
    
    /**
     * do the health-check call to HADB agent
     */    
    public boolean isDatabaseOk() {
        boolean result = true;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("isDatabaseOk() begin:runtimeHealthCheckExceptionReported: " + runtimeHealthCheckExceptionReported);
        }        
        //if runtime exception has occurred don't bother checking
        if(runtimeHealthCheckExceptionReported) {
            return result;
        }
        String connURL = this.getHadbAgentConnectionURL();
        String hadbDBName = this.getHadbDatabaseName();
        String hadbAgentPassword = this.getHadbAgentPassword();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("isDatabaseOk():HADB Connection URL: " + connURL);
            _logger.finest("isDatabaseOk():HADB DB Name: " + hadbDBName);
            _logger.finest("isDatabaseOk():HADB Agent Password: " + hadbAgentPassword);            
        }                
        //cannot check if you cannot connect - just return true
        if(connURL == null || hadbDBName == null || hadbAgentPassword == null) {
            return true;
        }

        try {
            DbState dbstate = 
                new DbState (connURL, hadbDBName, hadbAgentPassword);
            int state = dbstate.getState();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("dbstate reports state = " + state);           
            }                        
            //FIXME: this is wrong to base on this return code
            if(state==DbState.STATE_MA_NOT_REACHABLE) {
                _logger.log(Level.WARNING,
                    "hadbhealthchecker.generalConfigurationError");
                runtimeHealthCheckExceptionReported = true;
                return true;
            }
            result = dbstate.isAvailable();
        } catch (Exception ex) {
            if(!runtimeHealthCheckExceptionReported) {
                _logger.log(Level.WARNING,
                    "hadbhealthchecker.generalConfigurationError");
                runtimeHealthCheckExceptionReported = true;
                ex.printStackTrace();               
            }
        }
        return result;
    }    

    /*
    public boolean isDatabaseOk() {
        String hadbMgmtEnvPath = this.getHadbMgmtEnvPathFromConfig();
        String hadbDatabaseName = this.getHadbDatabaseNameFromConfig();
        //if hadbMgmtEnvPath or hadbDatabaseName are not properly configured
        //then act as if health checking is disabled
        if(hadbMgmtEnvPath == null || hadbDatabaseName == null) {
            _logger.severe("HADB Health Check improperly configured - not checking health");
            return true;
        }
        DatabaseFactory fact = DatabaseFactory.defaultFactory;
        _logger.finest("in isDatabaseOk(): dbFact = " + fact);
        int state = -1;
        Database db = null;
        try {
            //db = fact.getDatabase("/export/home2/hadm49/SUNWhadb/current/bin", "test49");
            db = fact.getDatabase(hadbMgmtEnvPath, hadbDatabaseName);
            _logger.finest("in isDatabaseOk(): db = " + db);
            
            if (db != null)
                state = db.getState();
            _logger.finest("in isDatabaseOk(): state = " + state);
            _logger.finest("state == Database.stateStopped: " + (state == Database.stateStopped));
            _logger.finest("state == Database.stateNonOperational: " + (state == Database.stateNonOperational));            
        } catch (MgtException ex) {ex.printStackTrace();}
        if (state == -1 || state == Database.stateStopped || state ==
            Database.stateNonOperational) {
            return false;
        } else {
            return true;
        }
    }
     */
    
    protected boolean hasAgentConfigErrorOccurred() {
        return (hadbAgentHostsErrorReported 
                || hadbAgentPasswordErrorReported
                || hadbAgentPortErrorReported
                || hadbDatabaseNameErrorReported
                || hadbAgentConnectionUrlErrorReported);
    }

    protected String getHadbAgentConnectionURL() {
        if(hadbAgentConnectionUrl != null) {
            return hadbAgentConnectionUrl;
        }
        hadbAgentConnectionUrl = this.getHadbAgentConnectionURLFromConfig();
        if(hadbAgentConnectionUrl == null && !hadbAgentConnectionUrlErrorReported) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.agentConnUrlError");
            hadbAgentConnectionUrlErrorReported = true;
        }  else {
            if(hadbAgentConnectionUrl != null) {
                hadbAgentConnectionUrlErrorReported = false;
            }
        }
        return hadbAgentConnectionUrl;
    }
    
    /**
     * Get the connectionURL for hadb agent(s) from domain.xml.
     */
    public String getHadbAgentConnectionURLFromConfig() {
        String url = null;
        StringBuffer sb = new StringBuffer();
        String hostsString = this.getHadbAgentHosts();
        String portString = this.getHadbAgentPort();
        if(hostsString != null && portString != null) { 
            sb.append(hostsString);
            sb.append(":");
            sb.append(portString);
            url = sb.toString();
        } else {
            url = null;
        }
        return url;
    }
    
    protected String getDASAdminPassword() {
        return IdentityManager.getPassword();
    }

    protected String getHadbAgentPassword() {
        if(hadbAgentPassword != null) {
            return hadbAgentPassword;
        }
        hadbAgentPassword = this.getHadbAgentPasswordFromConfig();
        
        //if ha-agent-password attribute is null
        //try the DAS admin password
        if(hadbAgentPassword == null) {
            hadbAgentPassword = getDASAdminPassword();
        }
        
        //if still null, then error
        if(hadbAgentPassword == null && !hadbAgentPasswordErrorReported) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.agentPasswordError");
            hadbAgentPasswordErrorReported = true;
        } else {
            if(hadbAgentPassword != null) {
                hadbAgentPasswordErrorReported = false;
            }
        }
        return hadbAgentPassword;
    }

    protected String getHadbAgentPasswordFromConfig() {
        ServerConfigLookup config = new ServerConfigLookup();
        return config.getHadbAgentPasswordFromConfig();
    }
    
    protected String getHadbAgentPort() {
        if(hadbAgentPort != null) {
            return hadbAgentPort;
        }
        hadbAgentPort = this.getHadbAgentPortFromConfig();
        if(hadbAgentPort == null && !hadbAgentPortErrorReported) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.agentPortError");
            hadbAgentPortErrorReported = true;
        } else {
            if(hadbAgentPort != null) {
                hadbAgentPortErrorReported = false;
            }
        }
        return hadbAgentPort;
    }    
    
    protected String getHadbAgentPortFromConfig() {
        ServerConfigLookup config = new ServerConfigLookup();
        return config.getHadbAgentPortFromConfig();
    }
    
    protected int getHaStoreHealthcheckIntervalInSecondsFromConfig() {
        ServerConfigLookup config = new ServerConfigLookup();
        return config.getHaStoreHealthcheckIntervalInSecondsFromConfig();
    }
    
    protected int getHaStoreHealthcheckIntervalInSeconds() {
        if(haStoreHealthcheckIntervalInSeconds > 0) {
            return haStoreHealthcheckIntervalInSeconds;
        }
        haStoreHealthcheckIntervalInSeconds =
            this.getHaStoreHealthcheckIntervalInSecondsFromConfig();
        return haStoreHealthcheckIntervalInSeconds;
    }    
    
    protected String getHadbAgentHosts() {
        if(hadbAgentHosts != null) {
            return hadbAgentHosts;
        }
        hadbAgentHosts = this.getHadbAgentHostsFromConfig();
        if(hadbAgentHosts == null && !hadbAgentHostsErrorReported) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.agentHostsError");
            hadbAgentHostsErrorReported = true;
        } else {
            if(hadbAgentHosts != null) {
                hadbAgentHostsErrorReported = false;
            }
        }
        return hadbAgentHosts;
    }     

    protected String getHadbAgentHostsFromConfig() {
        ServerConfigLookup config = new ServerConfigLookup();
        return config.getHadbAgentHostsFromConfig();
    }
    
    protected String getHadbDatabaseName() {
        if(hadbDatabaseName != null) {
            return hadbDatabaseName;
        }
        hadbDatabaseName = this.getHadbDatabaseNameFromConfig();
        if(hadbDatabaseName == null && !hadbDatabaseNameErrorReported) {
            _logger.log(Level.WARNING,
                "hadbhealthchecker.agentDatabaseNameError");
            hadbDatabaseNameErrorReported = true;
        } else {
            if(hadbDatabaseName != null) {
                hadbDatabaseNameErrorReported = false;
            }
        }
        return hadbDatabaseName;
    }    
    
    protected String getHadbDatabaseNameFromConfig() {
        ServerConfigLookup config = new ServerConfigLookup();
        return config.getHadbDatabaseNameFromConfig();
    }
    
    /**
     * This is done at start time
     */    
    private static void initializeHealthCheckEnabledFlag() {
        ServerConfigLookup config = new ServerConfigLookup();
        BooleanWrapper healthCheckEnabledWrapper 
            = getInstance().getHealthCheckEnabledWrapper();
        if(healthCheckEnabledWrapper != null) {
            healthCheckEnabledWrapper.setValue(config.getHadbHealthCheckFromConfig());
        }       
    }
    
    BooleanWrapper getHealthCheckEnabledWrapper() {
        return _healthCheckEnabledFlag;
    }
    
    private static boolean isHealthCheckingEnabled() {
        if(getInstance() == null) {
            return false;
        }
        BooleanWrapper healthCheckEnabledWrapper 
            = getInstance().getHealthCheckEnabledWrapper();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("isHealthCheckingEnabled() reporting: " 
                + getInstance().getHealthCheckEnabledWrapper().getValue());           
        }        
        return healthCheckEnabledWrapper.getValue();
    }    
    
    /**
     * return boolean reflecting whether it is ok to proceed
     * with HADB processing
     */    
    public static boolean isOkToProceed() {
        if( !isHealthCheckingEnabled() ) {
            return true;
        }
        return isHADBOperational();
    }    

    /**
     * Get the flag which reflects whether HADB is operational
     */    
    public static boolean isHADBOperational() {
        return _hadbOperationalFlag;
    }

    /**
     * Set the flag which reflects whether HADB is operational
     */    
    public static void setHADBOperational(boolean value) {
        _hadbOperationalFlag = value;
    }
    
    /**
     * Get the web container
     */    
    protected WebContainer getWebContainer() {
        return _webContainer;
    }

    /**
     * Set the web container
     */    
    public void setWebContainer(WebContainer webContainer) {
        _webContainer = webContainer;
    }    
        
    class BooleanWrapper {

        /** Creates a new instance of BooleanWrapper */
        public BooleanWrapper() {     
        }        
        /** Creates a new instance of BooleanWrapper */
        public BooleanWrapper(boolean value) {
            _value = new Boolean(value);       
        }        
        synchronized boolean getValue() {
            //default value false
            if(!isInitialized()) {
                return false;
            } else {
                return _value.booleanValue();
            }
        }
        synchronized void setValue(boolean value) {
            _value = new Boolean(value);
        }
        boolean isInitialized() {
            return _value != null;
        }
        Boolean _value = null;
    }
}
