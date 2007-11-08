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
 * JxtaReplicationReceiver.java
 *
 * Created on December 20, 2005, 11:03 AM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import org.apache.catalina.Context;
import org.apache.catalina.Container;
import org.apache.catalina.Engine;
import org.apache.catalina.Manager;
import org.apache.catalina.LifecycleException;

import com.sun.appserv.ha.spi.*;

import com.sun.enterprise.web.EmbeddedWebContainer;
import com.sun.enterprise.web.ReplicationReceiver;
import com.sun.enterprise.web.ServerConfigLookup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import net.jxta.endpoint.Message;
import net.jxta.util.JxtaBiDiPipe;
import com.sun.enterprise.ee.cms.core.*;
import com.sun.enterprise.ee.cms.impl.client.*;

/**
 *
 * @author Larry White
 */
public class JxtaReplicationReceiver implements Runnable, ReplicationReceiver {

    private static final String REPLICATED_PERSISTENCE_TYPE 
        = "replicated";
    private static final String REPLICATED_STORE_FACTORY_CLASS 
        = "com.sun.enterprise.ee.web.sessmgmt.JxtaBackingStoreFactory";
    
    public final static String LOGGER_MEM_REP 
        = ReplicationState.LOGGER_MEM_REP;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    //private static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP);     
    
    /**
     * The singleton instance of JxtaReplicationReceiver
     */    
    private static JxtaReplicationReceiver _soleInstance = null; 
    
    /**
     * a monitor obj for synchronization
     */    
    private static final Object _monitor = new Object();
    
    protected final static String SENDER_PIPE 
        = AbstractPipeWrapper.SENDER_PIPE;
    protected final static String RECEIVER_PIPE
        = AbstractPipeWrapper.RECEIVER_PIPE;   
    
    /**
     * The embedded Catalina object.
     */
    protected EmbeddedWebContainer _embedded = null;
    
    /**
     * The map of applications used for routing
     */
    protected HashMap _appsMap = new HashMap();   
    
    /**
     * The sole instance of JxtaBiDiPipeWrapper
     */
    private JxtaBiDiPipeWrapper _jxtaBiDiPipeWrapper = null; 
    
    /**
     * The sole instance of JxtaServerPipeWrapper
     */
    protected JxtaServerPipeWrapper _jxtaServerPipeWrapper = null;    
    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;
    
    /**
     * Is this component in the midst of reinitializing?
     */
    protected boolean reinitializing = false;
    
    /**
     * has doReplicationPipeInitialization been called once?
     */
    protected volatile boolean pipeInitializationCalled = false;    
    
    /**
     * The thread.
     */
    protected Thread thread = null;
    
    /**
     * The thread completion semaphore.
     */
    protected volatile boolean threadDone = false;    
    
    /**
     * Name to register for the background thread.
     */
    protected String _threadName = "JxtaReplicationReceiver";
    
    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(_threadName);
    }
    
    /** Creates a new instance of JxtaReplicationReceiver */
    public JxtaReplicationReceiver(EmbeddedWebContainer embedded)  {
        this();
        _embedded = embedded;
        _appsMap = new HashMap();
    }    
    
    /** Creates a new instance of JxtaReplicationReceiver */
    public JxtaReplicationReceiver() {
        _threadName = "JxtaReplicationReceiver";
        /*
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }
         */        
    } 
    
    /** Return the singleton instance
     *  lazily creates a new instance of JxtaReplicationReceiver if not created yet
     * @param embedded the embedded web container
     */
    public static JxtaReplicationReceiver createInstance(EmbeddedWebContainer embedded) {
        synchronized (_monitor) {        
            if (_soleInstance == null) {
                _soleInstance = new JxtaReplicationReceiver(embedded);
            } else {
                _soleInstance._embedded = embedded;
            }
        }
        return _soleInstance;
    }   
    
    /** Return the singleton instance
     *  returns already created receiver, assumes already created
     */
    public static JxtaReplicationReceiver createInstance() {
        synchronized (_monitor) {        
            if (_soleInstance == null) {
                _soleInstance = new JxtaReplicationReceiver();
            }
        }
        return _soleInstance;        
    }
    
    private boolean isDAS() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.isDAS();
    }
    
    public void init() {
        //later can do DAS specific bootstrapping here - for now return
        if(this.isDAS()) {
            return;
        }
        //we are starting so not yet ready to replicate
        //the JxtaBiDiPipeWrapper run method will set to true when finished
        //now it is initialized as false so no need
        //ReplicationHealthChecker.setReplicationCommunicationOperational(false, false);
        //register our implementation
        Properties env = new Properties();
        fillPropertiesFromLifeCycle(env);        
        BackingStoreRegistry backingStoreRegistry 
                = BackingStoreRegistry.getInstance();
        try {
            backingStoreRegistry.register(REPLICATED_PERSISTENCE_TYPE, 
                REPLICATED_STORE_FACTORY_CLASS, env);
            //remove after testing
            /*
            backingStoreRegistry.registerFactory("registered_foo", 
                REPLICATED_STORE_FACTORY_CLASS);
             */            
        } catch (DuplicateFactoryRegistrationException ex) {
            System.out.println("duplicate persistence type: cannot register");
        }
        //System.out.println("JxtaReplicationReceiver initializing router:embedded = " + _embedded);        
        ReplicationMessageRouter router 
            = ReplicationMessageRouter.createInstance(_embedded);

        //FIXME: move from here to app load logic
        //remove after testing
        //doPipeInitialization();
    }    
    
    public void initPrevious() {
        //later can do DAS specific bootstrapping here - for now return
        if(this.isDAS()) {
            return;
        }
        //we are starting so not yet ready to replicate
        //the JxtaBiDiPipeWrapper run method will set to true when finished
        ReplicationHealthChecker.setReplicationCommunicationOperational(false, false);
        //register our implementation
        Properties env = new Properties();
        fillPropertiesFromLifeCycle(env);        
        BackingStoreRegistry backingStoreRegistry 
                = BackingStoreRegistry.getInstance();
        try {
            backingStoreRegistry.register(REPLICATED_PERSISTENCE_TYPE, 
                REPLICATED_STORE_FACTORY_CLASS, env);
            //remove after testing
            /*
            backingStoreRegistry.registerFactory("registered_foo", 
                REPLICATED_STORE_FACTORY_CLASS);
             */            
        } catch (DuplicateFactoryRegistrationException ex) {
            System.out.println("duplicate persistence type: cannot register");
        }
        ReplicationMessageRouter router 
            = ReplicationMessageRouter.createInstance(_embedded);
        //FIXME - for now server then bidipipe - only single
        //with artificial sleep in between
        JxtaServerPipeWrapper serverPipeWrapper = new JxtaServerPipeWrapper();
        _jxtaServerPipeWrapper = serverPipeWrapper;
        System.out.println("JxtaReplicationReceiver:about to call JxtaServerPipeWrapper.start()");
        serverPipeWrapper.start();
        try {
            Thread.currentThread().sleep(3000L);
        } catch (InterruptedException ex) {}
        JxtaBiDiPipeWrapper bidiPipeWrapper = new JxtaBiDiPipeWrapper();
        _jxtaBiDiPipeWrapper = bidiPipeWrapper;
        System.out.println("JxtaReplicationReceiver:about to call JxtaBiDiPipeWrapper.start()");     
        bidiPipeWrapper.start();
        ReplicationHealthChecker.setReplicationReceiver(this);
        checkAndRegisterWithGMS();
        System.out.println("JxtaReplicationReceiver:JxtaBiDiPipeWrapper.start() complete");
        started = true;

    }
    
    public void doPipeInitialization() {
        //this should only be called once
        synchronized(_monitor) {
            System.out.println("JxtaReplicationReceiver>>doPipeInitialization:previously called = " + pipeInitializationCalled);
            if(pipeInitializationCalled) {                
                return;
            } else {
                pipeInitializationCalled = true;
            }
        }
        //FIXME - for now server then bidipipe - only single
        //with artificial sleep in between
        JxtaServerPipeWrapper serverPipeWrapper = new JxtaServerPipeWrapper();
        _jxtaServerPipeWrapper = serverPipeWrapper;
        System.out.println("JxtaReplicationReceiver:about to call JxtaServerPipeWrapper.start()");
        serverPipeWrapper.start();
        try {
            Thread.currentThread().sleep(3000L);
        } catch (InterruptedException ex) {}
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("initializing _jxtaBiDiPipeWrapper");
        }        
        JxtaBiDiPipeWrapper bidiPipeWrapper = new JxtaBiDiPipeWrapper();
        _jxtaBiDiPipeWrapper = bidiPipeWrapper;
        System.out.println("JxtaReplicationReceiver:about to call JxtaBiDiPipeWrapper.start()");     
        bidiPipeWrapper.start();
        ReplicationHealthChecker.setReplicationReceiver(this);
        checkAndRegisterWithGMS();
        System.out.println("JxtaReplicationReceiver:JxtaBiDiPipeWrapper.start() complete");
        started = true;
    }
    
    protected void fillPropertiesFromLifeCycle(Properties env) {
        //FIXME
    }    
    
    private void checkAndRegisterWithGMS() {
        JxtaStarter jxtaStarter = JxtaStarter.createInstance();
        if(jxtaStarter.checkGMS()) {
            registerWithGMS();
        } 
    }
    
    private void registerWithGMS() {
        try {
            GroupManagementService gms = GMSFactory.getGMSModule(getClusterName());
            gms.addActionFactory(new JoinNotificationActionFactoryImpl(new JoinNotificationEventHandler()));
            gms.addActionFactory(new FailureNotificationActionFactoryImpl(new FailureNotificationEventHandler()));
            gms.addActionFactory(new FailureSuspectedActionFactoryImpl(new FailureSuspectedNotificationEventHandler()));
            gms.addActionFactory(new PlannedShutdownActionFactoryImpl(new PlannedShutdownNotificationEventHandler()));
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
    }
    
    public boolean isPipeInitializationCalled() {
        return pipeInitializationCalled;
    }
    
    private String getClusterName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getClusterName();
    }    
    
    public void reInit(String senderOrReceiverFailure, String partnerInstanceName) {
        System.out.println("JxtaReplicationReceiver>>reInit(): reinitializing=" 
                + reinitializing + " senderOrReceiverFailure=" + senderOrReceiverFailure
                + " partnerInstanceName=" + partnerInstanceName);
        //later can do DAS specific bootstrapping here - for now return
        if(this.isDAS()) {
            return;
        }
        //if already in middle of reinitializing - ignore
        synchronized(this) {
            if(reinitializing) {
                return;
            }
            reinitializing = true;
        }
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }
        
        //the JxtaBiDiPipeWrapper run method will set to true when finished
        //FIXME check if started is needed here and below - I think not
        //we are starting so not yet ready to replicate
        //started = false;
        
        //FIXME moving this to only set false if restarting sending side
        //ReplicationHealthChecker.setReplicationCommunicationOperational(false, false);
        //FIXME remove after testing - don't want this re-initialized on reInit
        /*
        ReplicationMessageRouter router 
            = ReplicationMessageRouter.createInstance(_embedded);
         */        
        //FIXME - for now server then bidipipe - only single
        //with artificial sleep in between
        boolean clusterSizeTwo = _jxtaBiDiPipeWrapper.isSizeTwoCluster();
    System.out.println("JxtaReplicationReceiver>>reInit(): clusterSizeTwo = " + clusterSizeTwo);
        if(clusterSizeTwo || isReceiverSideFailure(senderOrReceiverFailure)) {
            this.restartReceiverSide(partnerInstanceName);
        }
        //sleep 5 seconds between re-starting both sides
        if(clusterSizeTwo) {
            try {
                Thread.currentThread().sleep(5000L);
            } catch (InterruptedException ex) {}
        }

        if(clusterSizeTwo || isSenderSideFailure(senderOrReceiverFailure)) {
            //this.restartSenderSide();
            this.reshapeSenderSide(partnerInstanceName);
        }
        //FIXME check if started is needed here and above - I think not
        //started = true;
        //FIXME may have to move this to end of bidi thread when it completes
        //restart
        synchronized(this) { 
            if(reinitializing) {
                try {
                    Thread.currentThread().sleep(60000L);
                } catch (InterruptedException ex) {}
            }
            reinitializing = false;
        }

    }
    
    void restartReceiverSide(String partnerInstanceName) {
        /*FIXME remove after testing - keep existing _serverPipeWrapper do not re construct
        JxtaServerPipeWrapper serverPipeWrapper = new JxtaServerPipeWrapper();
        _jxtaServerPipeWrapper = serverPipeWrapper;
         */
   System.out.println("JxtaReplicationReceiver:about to call JxtaServerPipeWrapper.restart()"
           + "for partnerInstanceName:" + partnerInstanceName);
        //re-start receiving for partnerInstanceName
        _jxtaServerPipeWrapper.restart(partnerInstanceName);        
    }
    
    void restartSenderSide() {
        //restarting sender side so cannot replicate for now
        ReplicationHealthChecker.setReplicationCommunicationOperational(false, false);
        /*FIXME remove after testing - keep existing _jxtaBiDiPipeWrapper do not re construct        
        JxtaBiDiPipeWrapper bidiPipeWrapper = new JxtaBiDiPipeWrapper();
        _jxtaBiDiPipeWrapper = bidiPipeWrapper;
         */
   System.out.println("JxtaReplicationReceiver:about to call JxtaBiDiPipeWrapper.restart()");     
        _jxtaBiDiPipeWrapper.restart();
   System.out.println("JxtaReplicationReceiver:JxtaBiDiPipeWrapper.restart() complete");        
    }
    
    /**
     *  connectSenderSideToNew
     *
     *@param  newPartnerInstance name of new partner
     */
    public void connectSenderSideToNew(String newPartnerInstance) {
        _jxtaBiDiPipeWrapper.connectToNew(newPartnerInstance);
    }
    
    /**
     *  respondToFailure
     *
     *@param  failedPartnerInstance name of failed instance
     */
    public void respondToFailure(String failedPartnerInstance) {
        //do receiver side first
        _jxtaServerPipeWrapper.respondToFailure(failedPartnerInstance);
        _jxtaBiDiPipeWrapper.respondToFailure(failedPartnerInstance);        
    }    
    
    /**
     *  respondToFailure
     *
     *@param  failedPartnerInstance name of failed instance
     */
    public void respondToFailure(String failedPartnerInstance, boolean ignoreReceiverSideFailure) {
        //do receiver side first unless it should be ignored
        if(!ignoreReceiverSideFailure) {
            _jxtaServerPipeWrapper.respondToFailure(failedPartnerInstance);
        }
        _jxtaBiDiPipeWrapper.respondToFailure(failedPartnerInstance); 
    }     
    
    void reshapeSenderSide(String partnerInstanceName) {
        //reshaping sender side so cannot replicate for now
        ReplicationHealthChecker.setReplicationCommunicationOperational(false, false);
   System.out.println("JxtaReplicationReceiver:about to call JxtaBiDiPipeWrapper.reshape()");     
        _jxtaBiDiPipeWrapper.reshape(partnerInstanceName);
   System.out.println("JxtaReplicationReceiver:JxtaBiDiPipeWrapper.reshape() complete");        
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
        //initializeHealthCheckEnabledFlag();
        
        //do not start if HADB health check is not enabled
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
        /*FIXME move this to correct place
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Replication health checking enabled");
        }
         */
       
        //this.registerAdminEvents();
        
        // Start the background health-check thread
        //threadStart();
        
        started = true;
    }
    
    /**
     * Start the thread that will receive
     * replication messages
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
        ReplicationHealthChecker.setStopping(true);
        ReplicationHealthChecker.setReplicationCommunicationOperational(false, false);
   System.out.println("JxtaReplicationReceiver:about to call JxtaServerPipeWrapper.stop()");     
        _jxtaServerPipeWrapper.stop();
        try {
            Thread.currentThread().sleep(5000L);
        } catch (InterruptedException ex) {}
   System.out.println("JxtaReplicationReceiver:about to call JxtaBiDiPipeWrapper.stop()");     
        _jxtaBiDiPipeWrapper.stop();
        
        //un-register our implementation
        BackingStoreRegistry backingStoreRegistry 
            = BackingStoreRegistry.getInstance();
        backingStoreRegistry.remove(REPLICATED_PERSISTENCE_TYPE);
        
        //this.unregisterAdminEvents();
        // Stop the thread       
        //threadStop();
        
        started = false;
    }

    /**
     * flush the active caches 
     */    
    public void repairOnCurrentThread() {
        //flush caches during shutdown
        if(_jxtaBiDiPipeWrapper != null) {
            _jxtaBiDiPipeWrapper.repairOnCurrentThread();
        } else {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("not doing unload because no replication-enabled app was ever deployed");
            }
        }
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
     * The thread that processes received replication messages
     */
    public void run() {
        // Loop until the termination semaphore is set
        while (!threadDone) {
            //threadSleep();
            //doHADBHealthCheck();
        }
    } 
    
    public void processMessage(ReplicationState state) {
        //System.out.println("JxtaReplicationReceiver:processMessage");
        //send ack if not a return msg and is a void return
        if(!state.isReturnMessage() && state.isVoidMethodReturnState()) {
            //FIXME: can send acknowledgement back immediately
        }
        this.routeMessageForApp(state.getAppId(), state);
    }
    
    public void routeMessageForApp(String appName, ReplicationState message) {
        //System.out.println("JxtaReplicationReceiver:routeMessageForApp: " + appName);
        _logger.finest("IN JxtaReplicationReceiver:routeMessageForApp" + appName);
        ReplicationManager mgr = null;
        if((mgr = this.findApp(appName)) != null) {
            mgr.processMessage(message);
            return;
        }
        try {
            Engine[] engines = _embedded.getEngines();
            
            for(int h=0; h<engines.length; h++) {
                Container engine = (Container) engines[h];
                Container[] hosts = engine.findChildren();
                for(int i=0; i<hosts.length; i++) {
                    Container nextHost = hosts[i];
                    Container [] webModules = nextHost.findChildren();
                    for (int j=0; j<webModules.length; j++) {
                        Container nextWebModule = webModules[j];
                        Context ctx = (Context)nextWebModule;
                        //this code gets managers
                        Manager nextManager = nextWebModule.getManager();                       
                        if(nextManager instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManagerBase)nextManager).getApplicationId();
//System.out.println("nextAppName = " + nextAppName + ", appName = " + appName);
                            _logger.finest("nextAppName = " + nextAppName + ", appName = " + appName);
                            if(nextAppName.equals(appName)) {
                                _logger.finest("found our manager:" + nextManager.getClass().getName());
                                this.addApp(appName, (ReplicationManager)nextManager);
                                ((ReplicationManager)nextManager).processMessage(message);
                            }
                        }
                    }                    
                }                 
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }       
                
    }
    
    /**
     * Add this manager to the set of deployed app Managers.
     *
     * @param String appName name of application
     * @param ReplicationManager mgr to be added
     */
    public void addApp(String appName, ReplicationManager mgr) {

        synchronized (_appsMap) {
            _appsMap.put(appName, mgr);
        }
    }
    
    /**
     * Remove this manager from the set of deployed app Managers.
     *
     * @param String appName name of application
     */
    public void removeApp(String appName) {

        synchronized (_appsMap) {
            _appsMap.remove(appName);
        }

    }
    
    public ReplicationManager findApp(String appName) /*throws IOException*/ { 

        if (appName == null)
            return (null);
        synchronized (_appsMap) {
            ReplicationManager mgr = 
                (ReplicationManager) _appsMap.get(appName);
            return (mgr);
        }

    }    
    
    public void processQueryMessage(ReplicationState message, String returnInstance) {
        //System.out.println("JxtaReplicationReceiver:processQueryMessage");
        this.routeQueryMessageForApp(message.getAppId(), message, returnInstance);
    }    
    
    public void routeQueryMessageForApp(String appName, ReplicationState message, String returnInstance) {
        //System.out.println("JxtaReplicationReceiver:routeQueryMessageForApp: " + appName);
        _logger.finest("IN JxtaReplicationReceiver:routeQueryMessageForApp" + appName);
        try {
            Engine[] engines = _embedded.getEngines();
            
            for(int h=0; h<engines.length; h++) {
                Container engine = (Container) engines[h];
                Container[] hosts = engine.findChildren();
                for(int i=0; i<hosts.length; i++) {
                    Container nextHost = hosts[i];
                    Container [] webModules = nextHost.findChildren();
                    for (int j=0; j<webModules.length; j++) {
                        Container nextWebModule = webModules[j];
                        Context ctx = (Context)nextWebModule;
                        //this code gets managers
                        Manager nextManager = nextWebModule.getManager();                       
                        if(nextManager instanceof ReplicationManager) {
                            //let this manager process the message 
                            //if it is a ReplicationManager
                            //and app names match
                            String nextAppName = ((ReplicationManagerBase)nextManager).getApplicationId();
//System.out.println("nextAppName = " + nextAppName + ", appName = " + appName);
                            _logger.finest("nextAppName = " + nextAppName + ", appName = " + appName);
                            if(nextAppName.equals(appName)) {
                                _logger.finest("found our manager:" + nextManager.getClass().getName());
                                ((ReplicationManager)nextManager).processQueryMessage(message, returnInstance);
                            }
                        }
                    }                    
                }                 
            }
        } catch (Throwable th) {
            _logger.log(Level.SEVERE, "Exception thrown", th);
        }       
                
    }
    
    //code for sending back return response for non-void calls
    
    public ReplicationState sendReplicationStateResponse(ReplicationState state) {
        //FIXME this is just test code
        /* no waiting queue here
        LinkedBlockingQueue aQueue =
                ReplicationResponseRepository.putEmptyQueueEntry(state);
         */
        //send message over pipe
        //FIXME will need to add param for instance name to send back to
        sendOverPipe(state, true);
        /*
        long duration = System.currentTimeMillis() - tempStart;
        if(duration > 20) {
            System.out.println("sendReplicationStateResponse took" + duration + " msecs");
        }
         */
        
        //block and wait for return message
        /* no blocking and waiting
        ReplicationState returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
        return returnState;
         */
        return state;
    }
    
    private boolean sendOverPipe(ReplicationState state) { 
        return this.sendOverPipe(state, false);
    }   
    
    /* return false if cannot send
     * else true
     */
    private boolean sendOverPipe(ReplicationState state, boolean isResponse) {
        JxtaBiDiPipe thePipe = null;
        boolean result = false;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            return false;
        }
        String sourceInstanceName = state.getInstanceName();
    if(sourceInstanceName == null) {
        System.out.println("PROBLEM:sendOverPipe:isResponse=" + isResponse + " command=" + state.getCommand());
    }
        PipeWrapper thePipeWrapper = null;
        try {
            thePipeWrapper = this.getPipeWrapper(sourceInstanceName);
            if(thePipeWrapper == null) {
                return false;
            }
            thePipe = thePipeWrapper.getPipe();
            //System.out.println("sendOverPipe:pipe from pool= " + thePipe);
            if(thePipe == null) {
                //no pipe to return to pool - just return
                return false;
            }

            Message theMsg = this.createMessage(state, isResponse);
            try {
                thePipe.sendMessage(theMsg);
                result = true;
            } catch (IOException ex) {
                System.out.println("IOException sending message");
                ex.printStackTrace();
                result = false;
                //FIXME log message
            } finally {
                try {
                    this.putPipeWrapper(thePipeWrapper, sourceInstanceName);
                    //System.out.println("sendOverPipe:pipe back in pool ok");
                } catch (InterruptedException iex) {}
            }
        } catch (InterruptedException iex2) {
            //FIXME log it
            result = false;
        }
        return result;
    } 
    
    private Message createMessage(ReplicationState state, boolean isResponse) {
        return ReplicationState.createMessage(state, isResponse);
    }
    
    private PipeWrapper getPipeWrapper(String sourceInstanceName) throws InterruptedException {
        JxtaReceiverPipeManager receiverPipeManager = 
                JxtaReceiverPipeManager.createInstance();
        PipePool pool = receiverPipeManager.getPipePool(sourceInstanceName);
        //System.out.println("getPipeWrapper:sourceInstanceName=" + sourceInstanceName + " pool=" + pool);
        PipeWrapper pipeWrapper = null;
        if(pool == null) {
            System.out.println("getPipeWrapper:sourceInstanceName=" + sourceInstanceName + " pool=" + pool);
        }
        PipePoolElement poolElement = pool.take();
        //System.out.println("poolElement = " + poolElement);
        //return (PipeWrapper)pool.take();
        pipeWrapper = (PipeWrapper)poolElement;
        //pipe may be closed during shutdown or because
        //partner pipe endpoint has failed
        /*FIXME remove after testing
        if(pipeWrapper.isPipeClosed()) {
            return null;
        }
         */
        return pipeWrapper;
    }    
    
    private void putPipeWrapper(PipeWrapper thePipeWrapper, String sourceInstanceName) throws InterruptedException {
        //do not return pipe(Wrapper) to pool if pipe has been closed
        //or pipe wrapper is null
        //(caused by either shutdown or failure at pipe partner endpoint
        if(thePipeWrapper == null || thePipeWrapper.isPipeClosed()) {
            return;
        }
        JxtaReceiverPipeManager receiverPipeManager = 
                JxtaReceiverPipeManager.createInstance();
        PipePool pool = receiverPipeManager.getPipePool(sourceInstanceName);
        pool.put((PipePoolElement)thePipeWrapper);
    }    
    
    //end code for sending back return response for non-void calls
    
    boolean isSenderSideFailure(String senderOrReceiver) {
        return SENDER_PIPE.equals(senderOrReceiver);
    }
    
    boolean isReceiverSideFailure(String senderOrReceiver) {
        return RECEIVER_PIPE.equals(senderOrReceiver);
    }     
    
    public String getApplicationName(Context ctx) {
        return ctx.getName();
    }
    
    JxtaBiDiPipeWrapper getJxtaBiDiPipeWrapper() {
        return _jxtaBiDiPipeWrapper;
    }
    
}
