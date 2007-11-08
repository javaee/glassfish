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
 * ReplicationHealthChecker.java
 *
 * Created on July 19, 2006, 2:02 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;
import java.util.ArrayList;
import java.util.List;

import org.apache.catalina.LifecycleException;

import com.sun.enterprise.web.ReplicationReceiver;
import com.sun.enterprise.web.ServerConfigLookup;
import com.sun.enterprise.ee.cms.core.*;

/**
 *
 * @author Larry White
 */
public class ReplicationHealthChecker implements Runnable {
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger 
        = LogDomains.getLogger(LogDomains.WEB_LOGGER);

    /**
     * The flag which reflects whether replication partner is operational
     */    
    private static boolean _replicationPartnerOperationalFlag = true;
    
    /**
     * The flag which reflects whether pipes are connected
     */    
    private static boolean _replicationCommunicationOperationalFlag = false;    
    
    /**
     * The flag which reflects whether Replication health check is enabled
     * This is cached and updated by the health check thread
     */    
    private BooleanWrapper _healthCheckEnabledFlag = new BooleanWrapper();    
    
    /**
     * The singleton instance of ReplicationHealthChecker
     */    
    private static final ReplicationHealthChecker _soleInstance 
        = new ReplicationHealthChecker();
    
    /**
     * The singleton instance of ReplicationReceiver
     */    
    private static ReplicationReceiver _replicationReceiver = null;    
    
    /**
     * a monitor obj for synchronization
     */    
    private static final Object _monitor = new Object(); 
    
    /**
     * a monitor obj for unload thread synchronization
     */    
    private static final Object _unloadMonitor = new Object();     
    
    /**
     * a runtime health check error has been reported once
     */
    protected static boolean runtimeHealthCheckExceptionReported = false;
    
    /**
     * a runtime health failure error has been reported once
     */
    protected static boolean runtimeHealthFailureErrorReported = false;
    
    
    /**
     * a flag to indicate instance is stopping
     */
    private static final AtomicBoolean stoppingFlag = new AtomicBoolean(false);
    
    /**
     * a flag to indicate cluster is stopping
     */
    private static final AtomicBoolean clusterStoppingFlag = new AtomicBoolean(false); 
    
    /**
     * number of sender dispatch threads running
     * normally should be 2 - used to control countdownlatch
     * for instance shutdown - see ReplicationLifecycleImpl
     */
    private static final AtomicInteger dispatchThreadCount = new AtomicInteger(0);     
    
    /**
     * a flag to indicate instance is flushing
     */
    private static final AtomicBoolean flushingFlag = new AtomicBoolean(false); 

    /**
     * a flag to indicate instance thread waiting for flush
     */
    private static final AtomicBoolean flushThreadWaitingFlag = new AtomicBoolean(false); 
    
    /**
     * a countdown latch used by unload logic in ReplicationLifeCycleImpl
     */    
    private static CountDownLatch doneSignal = null;
    
    /**
     * The sleep interval in seconds
     */    
    private static int _sleepIntervalSeconds = 5;
    
    /**
     * the replication health check interval in seconds
     */
    protected int replicationHealthcheckIntervalInSeconds = -1;    
    
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
    protected volatile boolean threadDone = false; 
    
    /**
     * instance started time
     */
    private volatile long instanceStartTime = -1L;
    
    /**
     * Name to register for the background thread.
     */
    protected String _threadName = "ReplicationHealthChecker";
    
    /** Return the singleton instance
     *  lazily creates a new instance of ReplicationHealthChecker if not created yet
     */
    public static ReplicationHealthChecker getInstance() {
        return _soleInstance;
    }    
    
    /** Creates a new instance of ReplicationHealthChecker */
    public ReplicationHealthChecker() {
        _threadName = "ReplicationHealthChecker";       
    }
    
    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(_threadName);
    }

    /**
     * Return the instance start time
     */
    public long getInstanceStartTime() {
        return(instanceStartTime);
    }
    
    /**
     * set the instance start time
     */
    public void setInstanceStartTime(long value) {
        instanceStartTime = value;
    }

    /**
     * is the server started
     */    
    public boolean isInstanceStarted() {
        return(getTimeSinceInstanceStart() >= 0L);
    }
    
    public boolean isTimeSinceInstanceStartLessThan(long duration) {
        if(!isInstanceStarted()) {
            return false;
        }
        return (getTimeSinceInstanceStart() < duration); 
    }
    
    /**
     * get the time since start time (millis)
     */
    public long getTimeSinceInstanceStart() {
        long sTime = getInstanceStartTime();
        if(sTime == -1L) {
            return -1L;
        } else {
            return (System.currentTimeMillis() - sTime);
        }
    }
    
    /**
     * Get the replication receiver
     */    
    public static ReplicationReceiver getReplicationReceiver() {
        return _replicationReceiver;
    }   
    
    /**
     * set the replication receiver
     */    
    public static void setReplicationReceiver(ReplicationReceiver replicationReceiver) {
        _replicationReceiver = replicationReceiver;
    }
    
    /**
     * Get the flag which reflects whether replication partner is operational
     */    
    public static boolean isReplicationPartnerOperational() {
        return _replicationPartnerOperationalFlag;
    }    

    /**
     * Set the flag which reflects whether replication partner is operational
     */    
    public static void setReplicationPartnerOperational(boolean value) {
        synchronized(_monitor) {
            if(!value) {
                System.out.println("setReplicationPartnerOperational:false stack dump follows:");
                //Thread.dumpStack();
            } else {
                System.out.println("setReplicationPartnerOperational:true stack dump follows:");
                //Thread.dumpStack();           
            }        
            _replicationPartnerOperationalFlag = value;
        }
    }
    
    /**
     * Get the flag which reflects whether replication partner is operational
     */    
    public static boolean isReplicationCommunicationOperational() {
        return _replicationCommunicationOperationalFlag;
    }

    /**
     * Set the flag which reflects whether replication partner is operational
     */    
    public static void setReplicationCommunicationOperational(boolean value) {
        //FIXME remove after testing
        if(!value) {
            System.out.println("setReplicationCommunicationOperational:false stack dump follows:");
            //Thread.dumpStack();
        } else {
            System.out.println("setReplicationCommunicationOperational:true stack dump follows:");
            //Thread.dumpStack();           
        }
        setReplicationCommunicationOperational(value, true);
    }
    
    /**
     * Set the flag which reflects whether replication partner is operational
     * @param value the value
     * @param report count this as a real failure and report; else just silent setter
     */    
    public static void setReplicationCommunicationOperational(boolean value, boolean report) {
        synchronized(_monitor) {
            _replicationCommunicationOperationalFlag = value;
            if(!value && !runtimeHealthFailureErrorReported && report) {
                //FIXME log health failure once here
                System.out.println("ReplicationHealthChecker:health failure:stopping replication: check instance");
                runtimeHealthFailureErrorReported = true;
            }
        }
    }
    
    public static void reportError(String message) {
        //do not log if we are deliberately in the midst of stopping
        if(isStopping()) {
            return;
        }
        //FIXME make a log msg
        synchronized(_monitor) {
            if(!runtimeHealthFailureErrorReported) {
                runtimeHealthFailureErrorReported = true;
                System.out.println(message);
            }
        }
    }
    
    private boolean doPipesExist() {
        //FIXME may need this to be more dynamic check later
        return isReplicationCommunicationOperational();
    }
    
    /**
     * can I do a successful ping on a bidi pipe
     */ 
    boolean doPipeTest() {
        ReplicationState testState = createHealthPingState();
        testState.setAckRequired(true);
        ReplicationState resultState = doTransmit(testState);
        return (resultState != null);
    }    
    
    /**
     * send health message to test connectivity
     */ 
    protected ReplicationState doTransmit(ReplicationState transmitState) {
        JxtaReplicationSender replicationSender = 
                JxtaReplicationSender.createInstance();
        ReplicationState resultState = 
                replicationSender.sendReplicationStateHC(transmitState);
        return resultState;
    } 
    
    ReplicationState createHealthPingState() {
        return new ReplicationState(
                ReplicationState.MODE_WEB, //mode
                "pingtest",     //id
                "pingappid",    //appid
                0L,             //version
                0L,             //lastAccess
                0L,             //maxInactiveInterval
                null,           //extraParam
                null,           //queryResult
                null,           //instanceName
                ReplicationState.HC_COMMAND, //command
                null,           //state
                null);          //trunkState
    } 
    
    /**
     * return boolean reflecting whether it is ok to proceed
     * with replication processing
     */    
    public static boolean isOkToProceed() {
        /* FIXME we can put this back later
        if( !isHealthCheckingEnabled() ) {
            return true;
        }
         */
        //flushing time is treated specially
        if(isFlushing()) {
            return true;
        }
        //cluster stopping time is treated specially
        if(isClusterStopping()) {
            return false;
        }        
        boolean condition = isReplicationPartnerOperational() 
            && isReplicationCommunicationOperational();
        if(condition) {
            return true;
        }
        synchronized(_monitor) {
            if(!condition && !runtimeHealthFailureErrorReported) {
                System.out.println("ReplicationHealthChecker:health failure");
                System.out.println("isReplicationPartnerOperational()=" + isReplicationPartnerOperational());
                System.out.println("isReplicationCommunicationOperational()=" + isReplicationCommunicationOperational());
                //Thread.dumpStack();
                runtimeHealthFailureErrorReported = true;
            }
        }
        return condition;
        /*
        return isReplicationPartnerOperational() 
            && isReplicationCommunicationOperational();
         */
    }    
    
    /**
     * Perform the Replication health check and take action as appropriate
     * defaults to doing full (not quick) check
     */    
    protected boolean doReplicationHealthCheck() {
        return doReplicationHealthCheck(false);
    }
    
    /**
     * Perform the Replication health check and take action as appropriate
     * @param quickCheck - skip shutdown logic
     */
    protected boolean doReplicationHealthCheck(boolean quickCheck) {
        
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
            _logger.finest("health check enabled - entering isReplicationPartnerOk()");
        }
        boolean replicationPartnerAlive = this.isReplicationPartnerOk();
        boolean lastReplicationPartnerState = isReplicationPartnerOperational();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationHealthCheck: replication partner is alive: " + replicationPartnerAlive);
        }
        //DbState may have reported non-operational state
        //which was really -20 (mis-configuration) so abort and return
        //true in this case (for now)
        //or if an HADB agent mis-configuration was detected
        /* FIXME fix this later
        if(runtimeHealthCheckExceptionReported || this.hasAgentConfigErrorOccurred()) {
            return true;
        } 
         */   
        
        //now check for existence of replication pipes
        //but only if replication was previously not alive
        //and replication is now reported alive (i.e. replication is newly healthy
        //otherwise there is no point
        boolean pipesExist = true;
        if(!lastReplicationPartnerState && replicationPartnerAlive) {
            pipesExist = doPipesExist();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("ReplicationHealthCheck: pipes exist: " + pipesExist);
            }
        }   
        boolean healthyWithPipes = replicationPartnerAlive && pipesExist;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("ReplicationHealthCheck: replica healthyWithPipes: " + healthyWithPipes);
        }
        ReplicationHealthChecker.setReplicationPartnerOperational(healthyWithPipes);
        //ReplicationHealthChecker.setReplicationPartnerOperational(replicationPartnerAlive);

        /* FIXME want similar code here
        this.issueHealthWarning(lastReplicationPartnerState, replicationPartnerAlive, pipesExist);
         */
        //only do cleanup is replication is newly unhealthy
        if(lastReplicationPartnerState && !replicationPartnerAlive) {
            //do replication cleanup
            //skip if doing quickCheck
            if(!quickCheck) {
                this.doReplicationShutdownCleanup();
            }
        }
        return isReplicationPartnerOperational();

    }
    
    /**
     * Perform the replication related cleanup
     * i.e. closing out pipes
     */    
    private void doReplicationShutdownCleanup() {
        //FIXME this should close the pipes
    }
    
    String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }    
    
    String getReshapeReplicateToInstanceName(String formerPartnerInstance) {
        String myName = this.getInstanceName();
        SimpleInstanceArranger arranger = new SimpleInstanceArranger();
        //FIXME put this back when ready to test non-standalone
        try {
            Thread.currentThread().sleep(3000L);
        } catch (InterruptedException ex) {
            //deliberate no-op
        }
        List instanceNames = getConservativeMemberList(formerPartnerInstance);       
 
        /*
        System.out.println("testing getConservativeMemberList");
        for(int i=0; i<instanceNames.size(); i++) {
            System.out.println("conservative-instancesNames[" + i + "] = " + instanceNames.get(i));
        }
         */        
        
/*
        ArrayList instanceNamesTest = this.getClusterInstanceNamesList();
    System.out.println("testing lookup.getServerNamesInCluster");
    for(int i=0; i<instanceNamesTest.size(); i++) {
        System.out.println("instancesNameTest[" + i + "] = " + instanceNamesTest.get(i));
        System.out.println("instancesNames[" + i + "] = " + instanceNames.get(i));
        boolean isEqual = 
            ((String)instanceNamesTest.get(i)).equalsIgnoreCase((String)instanceNames.get(i));
        System.out.println("index[" + i + "] = " + isEqual);
    }
 */       
        arranger.init(instanceNames);
        String result = arranger.getReplicaPeerName(myName);
        System.out.println("getReplicaPeerName = " + result);
        //return arranger.getReplicaPeerName(myName);
        return result;
    }    
    
    public void displayCurrentGroupMembers() {
        List coreMembers = this.getCurrentGroupMembersViaGMS();
        for(int i=0; i<coreMembers.size(); i++) {
            System.out.println("member[" + i + "]=" + coreMembers.get(i));
        }        
    } 
    
    public List getConservativeMemberList(String formerPartnerInstance) {
        List adminList = getCurrentGroupMembersViaAdmin();
        List gmsList = getCurrentGroupMembersViaGMS();
        /*
        System.out.println("testing getConservativeMemberList");
        for(int i=0; i<adminList.size(); i++) {
            System.out.println("adminList-instancesNames[" + i + "] = " + adminList.get(i));
        } 
        for(int i=0; i<gmsList.size(); i++) {
            System.out.println("gmsList-instancesNames[" + i + "] = " + gmsList.get(i));
        }
         */    
        if(formerPartnerInstance != null) {
            adminList.remove(formerPartnerInstance);
            gmsList.remove(formerPartnerInstance);
        }        
        if(gmsList.size() <= adminList.size()) {
            return gmsList;
        } else {
            return adminList;
        }
    }
    
    public List getCurrentGroupMembersViaAdmin() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        ArrayList instanceNames = lookup.getServerNamesInCluster();
        return instanceNames;
    }
    
    public List getCurrentGroupMembersViaGMS() {
        List coreMembers = new ArrayList();
        try {
            GroupManagementService gms = GMSFactory.getGMSModule(getClusterName());
            GroupHandle groupHandle = gms.getGroupHandle();
            coreMembers = groupHandle.getCurrentCoreMembers();
        }
        catch(GMSNotInitializedException ex1) {
            //FIXME what to do
        }
        catch(GMSNotEnabledException ex2) {
            //FIXME what to do
        } 
        catch(GMSException ex3) {
            //FIXME what to do
        } 
        return coreMembers;
    }     
    
    private String getClusterName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getClusterName();
    }      
    
   /**
     * do the health-check call to determine if partner is ok
     */    
    public boolean isReplicationPartnerOk() {
        //FIXME work out how to actively check health GMS
        boolean result = true;
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("isReplicationPartnerOk() begin:runtimeHealthCheckExceptionReported: " + runtimeHealthCheckExceptionReported);
        }
        return true;
    }
    
    protected int getReplicationHealthcheckIntervalInSecondsFromConfig() {
        //FIXME add config.getReplicationHealthcheckIntervalInSecondsFromConfig
        ServerConfigLookup config = new ServerConfigLookup();
        return config.getHaStoreHealthcheckIntervalInSecondsFromConfig();
    }
    
    protected int getReplicationHealthcheckIntervalInSeconds() {
        if(replicationHealthcheckIntervalInSeconds > 0) {
            return replicationHealthcheckIntervalInSeconds;
        }
        replicationHealthcheckIntervalInSeconds =
            this.getReplicationHealthcheckIntervalInSecondsFromConfig();
        return replicationHealthcheckIntervalInSeconds;
    }     
    
    boolean isIStarted() {
        return started;
    }
    
    public static boolean isStopping() {
        return stoppingFlag.get();
    }
    
    static void setStopping(boolean value) {
        if(value) {
            setReplicationCommunicationOperational(value, false);
        }        
        stoppingFlag.set(value);
    }
    
    public static boolean isFlushing() {
        return flushingFlag.get();
    }
    
    static void setFlushing(boolean value) {        
        flushingFlag.set(value);
    }
    
    public static boolean isFlushThreadWaiting() {
        return flushThreadWaitingFlag.get();
    }
    
    public static void incrementDispatchThreadCount() {        
        dispatchThreadCount.incrementAndGet();
    }
    
    public static int getDispatchThreadCount() {
        return dispatchThreadCount.get();
    }   
    
    public static void setFlushThreadWaiting(boolean value) {        
        flushThreadWaitingFlag.set(value);
    } 
    
    public synchronized static CountDownLatch getDoneSignal() {
        if(doneSignal == null) {
            int numberOfDispatchThreads = getDispatchThreadCount();
            doneSignal = new CountDownLatch(numberOfDispatchThreads);
        }
        return doneSignal;
    }     
    
    public static Object getUnloadMonitor() {
        return _unloadMonitor;
    }
    
    public boolean isPipeInitializationCalled() {
        if(_replicationReceiver == null) {
            return false;
        }
        return ((JxtaReplicationReceiver)_replicationReceiver).isPipeInitializationCalled();
    }    
    
    public static boolean isClusterStopping() {
        return clusterStoppingFlag.get();
    }
    
    static void setClusterStopping(boolean value) {        
        clusterStoppingFlag.set(value);
    }     
    
    Thread getThread() {
        return thread;
    }    
    
    /**
     * Sleep for the duration specified by the <code>_sleepIntervalSeconds</code>
     * property.
     */
    protected void threadSleep() {
        _sleepIntervalSeconds = 
            this.getReplicationHealthcheckIntervalInSeconds();
        try {
            Thread.sleep(_sleepIntervalSeconds * 1000L);            
        } catch (InterruptedException e) {
            ;
        }
    }    
    
    /**
     * The background thread that checks for replication partner health.
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            threadSleep();
            doReplicationHealthCheck();
        }
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
        /* FIXME for now just return true
        BooleanWrapper healthCheckEnabledWrapper 
            = getInstance().getHealthCheckEnabledWrapper();
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("isHealthCheckingEnabled() reporting: " 
                + getInstance().getHealthCheckEnabledWrapper().getValue());           
        }        
        return healthCheckEnabledWrapper.getValue();
         */
        return true;
    }
    
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
        
        //do not start if replication health check is not enabled
        //FIXME may want to reconsider this decision
        /* for now taking out
        if(!isHealthCheckingEnabled()) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Starting - Replication health checking not enabled");
            }
            return;
        }
         */
        if(started) {
            return;
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Replication health checking enabled");
        }
       
        /* FIXME do later
        this.registerAdminEvents();
         */
        
        // Start the background health-check thread
        threadStart();
        started = true;
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
        /* FIXME add this later
        this.unregisterAdminEvents();
         */
        // Stop the background health-check thread       
        threadStop();
        started = false;
    }    
    
    /**
     * Start the background thread that will periodically check
     * the health of replication.
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
