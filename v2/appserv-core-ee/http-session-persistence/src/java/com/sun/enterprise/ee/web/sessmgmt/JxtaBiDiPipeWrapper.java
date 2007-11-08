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
 * JxtaBiDiPipeWrapper.java
 * for now (like the Jxta samples) we use
 * this primarily for receiving messages
 *
 * Created on February 7, 2006, 11:37 AM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.Messenger;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.membership.pse.StringAuthenticator;
import net.jxta.membership.InteractiveAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peer.PeerID;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.util.JxtaBiDiPipe;

import org.apache.catalina.LifecycleException;

import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author Larry White
 */
public class JxtaBiDiPipeWrapper implements PipeMsgListener, RendezvousListener, Runnable {
    
    private PeerGroup netPeerGroup = null;
    private PipeAdvertisement pipeAdv;
    private PeerID peerID;
    private JxtaBiDiPipe pipe;
    private boolean waitForRendezvous = false;
    private boolean stopped = false;
    private volatile boolean attemptingConnection = false;
    private String rendezvousLock = "Rendezvous Lock";
    private RendezVousService rendezvous;
    private final static String SenderMessage = "pipe_tutorial";
    private final static String InstanceNameMessage = "instance_name";
    private final static String completeLock = "completeLock";
    private final static String CLUSTER_MEMBERS = "cluster_members";
    private int count = 0; 
    
    private final static String MESSAGE_ID =
        ReplicationState.MESSAGE_ID;
    private final static String MESSAGE_COMMAND =
        ReplicationState.MESSAGE_COMMAND;
    private final static String RETURN_MSG_COMMAND = 
        ReplicationState.RETURN_MSG_COMMAND;
    private final static String MESSAGE_READY
        = ReplicationState.MESSAGE_READY;
    private final static String ReadyMessage 
        = ReplicationState.ReadyMessage;       

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
    * The helper class used to manage retryable errors from the HA store
    */
    protected JxtaConnectErrorManager errorMgr = null;
    
    /**
    * The number of seconds to wait before timing out a transaction
    * Default is 5 minutes.
    */
    protected String timeoutSecs = new Long(5 * 60).toString();
    
    private CountDownLatch doneSignal = new CountDownLatch(1);
    
    /** Creates a new instance of JxtaBiDiPipeWrapper */
    public JxtaBiDiPipeWrapper() {
        long timeout = new Long(timeoutSecs).longValue();
        errorMgr = new JxtaConnectErrorManager(timeout);
    }
    
    public JxtaConnectErrorManager getErrorManager() {
        long timeout = new Long(timeoutSecs).longValue();
        return new JxtaConnectErrorManager(timeout);
    }    
    
    /**
     *  start
     *
     *@param  args  command line args
     */
    public void start() {

        this.startJxta(false);
        String partnerName = this.getReplicateToInstanceName();
        try {
            String value = System.getProperty("RDVWAIT", "false");
            this.waitForRendezvous = Boolean.valueOf(value).booleanValue();

            this.pipeAdv = JxtaUtil.getPipeAdvertisement(this.getReplicateToInstanceName());
            /* FIXME - moving this to run method
            int numPipes = this.getNumberOfPipes();
            System.out.println("JxtaBiDiPipeWrapper-numPipes=" + numPipes);            
            createPipes(numPipes);
            initializePropagatedPipes();
            sentTestPropagatedMessage();
             */
            
            /* FIXME: check later on this - comment this waiting out for now
            this.waitUntilCompleted();
            this.netPeerGroup.stopApp();
            this.netPeerGroup.unref();
             */
        } catch (Exception e) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("failed to bind the JxtaBiDiPipe due to the following exception");
                e.printStackTrace();
            }             
        }
        doneSignal = new CountDownLatch(1);
        // run on this thread
        //this.run();
        Thread acceptThread = new Thread(this);
        acceptThread.setDaemon(true);
        acceptThread.start();
        long startTime = System.currentTimeMillis();
        try {
            doneSignal.await(45, TimeUnit.SECONDS);
        } catch(InterruptedException ex) {}
        System.out.println("JxtaBiDiPipeWrapper after await: wait time = " + (System.currentTimeMillis() - startTime));
        //if our partner did not start we must reshape
        testStartupAndReshapeIfNecessary(partnerName);
        ReplicationResponseRepository.getInstance().start();
        try {
            JxtaReplicationSender.createInstance().start();
        } catch (LifecycleException ex) {
            ;
        }
        ReplicationHealthChecker healthChecker
            = ReplicationHealthChecker.getInstance();
        healthChecker.setInstanceStartTime(System.currentTimeMillis());
    }
    
    void testStartupAndReshapeIfNecessary(String partnerName) {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("testStartupAndReshapeIfNecessary...");
        }         
        //System.out.println("testStartupAndReshapeIfNecessary...");
        if(areCurrentPipesOk()) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("JxtaBiDiPipeWrapper>>startup completed successfully: no reshape needed");
            }             
            //System.out.println("JxtaBiDiPipeWrapper>>startup completed successfully: no reshape needed");
            return;
        }
        if(this.isAttemptingConnection()) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("JxtaBiDiPipeWrapper>>startup partner failed: reshape needed");
            }            
            //System.out.println("JxtaBiDiPipeWrapper>>startup partner failed: reshape needed");
            setAttemptingConnection(false);
            this.respondToFailure(partnerName, true);
        }
    }
    
    /**
     *  Main processing method
     */
    public void run() {
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper-run method");
        }
        //initialize propagated pipes earlier
        initializePropagatedPipes();        
        int numPipes = this.getNumberOfPipes();
        System.out.println("JxtaBiDiPipeWrapper>>run-numPipes=" + numPipes);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper-numPipes=" + numPipes);
        }
        ReplicationHealthChecker healthChecker = ReplicationHealthChecker.getInstance();
        String proposedPartner = healthChecker.getReshapeReplicateToInstanceName(null);
        System.out.println("about to connectToInstance: " + proposedPartner);
        setAttemptingConnection(true);
        boolean pipesCreated = attemptConnectionNumberOfTries(3);
        //boolean pipesCreated = connectToInstance(proposedPartner, false);
        //boolean pipesCreated = createPipes(numPipes);
        System.out.println("JxtaBiDiPipeWrapper:run:pipesCreated=" + pipesCreated);      
        //only continue if pipes were created else give up
        if(pipesCreated) {    
            //initializePropagatedPipes();       
            sentTestPropagatedMessage();

            //we are connected so now ready to replicate
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper:run() complete");
            }             
            ReplicationHealthChecker.setReplicationCommunicationOperational(true); 
        } else {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper>>run was unsuccessful: quitting run");
            }              
            ReplicationHealthChecker.setReplicationCommunicationOperational(false);
        }
        setAttemptingConnection(false);
        System.out.println("doneSignal.countdown()");
        doneSignal.countDown();
    }
    
    private boolean attemptConnectionNumberOfTries(int numberOfTries) {
        int count = 0;
        boolean result = false;
        ReplicationHealthChecker healthChecker = ReplicationHealthChecker.getInstance();
        while(count < numberOfTries) {
            String proposedPartner = healthChecker.getReshapeReplicateToInstanceName(null);
            System.out.println("about to connectToInstance: " + proposedPartner);
            result = connectToInstance(proposedPartner, false);
            if(result) {
                break;
            } else {
                closeNonPropagatedSenderConnections();
                count++;
            }
        }
        return result;
    }

    /**
     *  interrupt connection attempt
     *  sleep for 15 seconds to allow thread to complete
     */    
    private void interruptConnectionAttempt() {
        setAttemptingConnection(false);
        try {
            Thread.currentThread().sleep(15000L);
        } catch (Exception ex) {
            //nothing to do deliberately eating
            assert true;
        }
    }
    
    void setAttemptingConnection(boolean value) {
        attemptingConnection = value;
    }
    
    boolean isAttemptingConnection() {
        return attemptingConnection;
    }
    
    /**
     *  considered alone if replication communication is down and we are not
     *  currently attempting communication 
     */    
    boolean isAlone() {
        return !isAttemptingConnection() && !ReplicationHealthChecker.isReplicationCommunicationOperational();
    }
    
    String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }
    
    int getNumberOfPipes() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        int result = lookup.getNumberOfReplicationPipesFromConfig();
        /*
        if(result < 2) {
            result = 2;
        }
         */
        if(result < 1) {
            result = 1;
        }
        return result;
    }    
    
    String getReplicateToInstanceName() {
        String myName = this.getInstanceName();
        SimpleInstanceArranger arranger = getSimpleArranger();
 /*     This commented out code would work with clustered stand-alone
  *     instances - currently not using
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

        String result = arranger.getReplicaPeerName(myName);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("getReplicaPeerName = " + result);
        }         
        return result;
    }
    
    String getReplicatedFromInstanceName() {
        String myName = this.getInstanceName();
        SimpleInstanceArranger arranger = getSimpleArranger();
        String result = arranger.getReplicatedFromPeerName(myName);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("getReplicatedFromPeerName = " + result);
        }        
        return result;
    }

    /**
     *  get SimpleArranger initialized from domain.xml list
     *  of instances in the cluster 
     */    
    SimpleInstanceArranger getSimpleArranger() {
        SimpleInstanceArranger arranger = new SimpleInstanceArranger();
        ServerConfigLookup lookup = new ServerConfigLookup();
        ArrayList instanceNames = lookup.getServerNamesInCluster();        
        arranger.init(instanceNames);
        return arranger;
    }

    /**
     *  is our cluster exactly two instances replicating
     *  to each other 
     */    
    boolean isSizeTwoCluster() {
        String replicatedFrom = getReplicatedFromInstanceName();
        String replicateTo = getReplicateToInstanceName();
        if(replicatedFrom == null || replicateTo == null) {
            return false;
        }
        return (replicatedFrom.equals(replicateTo));
    }

    /**
     *  get list of cluster instance names from the
     *  property "cluster_members" in <availability-service> 
     */    
    ArrayList getClusterInstanceNamesList() {
        ArrayList instanceNames = new ArrayList();
        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceNamesString = 
            lookup.getAvailabilityServicePropertyString(CLUSTER_MEMBERS);
        if(instanceNamesString == null) {
            return instanceNames;
        }
        String[] instancesArray = instanceNamesString.split(",");
        List instancesList = Arrays.asList(instancesArray);
        for(int i=0; i<instancesList.size(); i++) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("getClusterInstanceNamesList:elem" + i + " = " + ((String)instancesList.get(i)).trim() );
            }             
            instanceNames.add( ((String)instancesList.get(i)).trim() );
        }
        return instanceNames;
    }   
    
    /**
     *  initialize both the propagated output and input pipes
     */
    private void initializePropagatedPipes() {
        initializePropagatedOutputPipe();
        initializePropagatedInputPipeWrapper();
    }

    /**
     *  initialize the propagated output pipe
     */    
    private void initializePropagatedOutputPipe() {
        OutputPipe op = this.createPropagatedOutputPipe();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("setting JxtaSenderPipeManagers bidipipewrapper = " + this);
        }
        JxtaSenderPipeManager.createInstance().setJxtaBiDiPipeWrapper(this);
        JxtaSenderPipeManager.createInstance().setPropagatedOutputPipe(op);
    }

    /**
     *  initialize the propagated input pipe
     */     
    private void initializePropagatedInputPipeWrapper() {
        InputPipeWrapper ipWrapper = this.createPropagatedInputPipeWrapper();
        JxtaReceiverPipeManager.createInstance().setPropagatedInputPipeWrapper(ipWrapper);
    }

    /**
     *  close both sender-side and receiver-side health pipes
     */    
    private void closeHealthPipes() {
        closeSenderHealthPipe();
        closeReceiverHealthPipe();
    }

    /**
     *  close sender-side health pipe
     */    
    private void closeSenderHealthPipe() {
        JxtaSenderPipeManager.createInstance().closeHealthPipeWrapper();
    }

    /**
     *  close receiver-side health pipe
     */    
    private void closeReceiverHealthPipe() {
        JxtaReceiverPipeManager.createInstance().closeHealthPipeWrapper();
    }    

    /**
     *  close both input and output propagated pipes
     */    
    private void closePropagatedPipes() {
        JxtaSenderPipeManager.createInstance().closePropagatedOutputPipe();
        JxtaReceiverPipeManager.createInstance().closePropagatedInputPipeWrapper();
    }     
    
    private void sentTestPropagatedMessage() {   
        OutputPipe outputPipe = 
            JxtaSenderPipeManager.createInstance().getPropagatedOutputPipe();
        sendTestPropagatedMessages(outputPipe);
    }
    
    /**
     *  Send a series of messages over a pipe
     *
     * @param  pipe  Description of the Parameter
     */
    private void sendTestPropagatedMessages(OutputPipe pipe) {
        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceName = lookup.getServerName();            
        try {
            for (int i = 0; i < 1; i++) {
                Message msg = new Message();
                String data = "Propagated Message #" + i + " From Instance " + instanceName;
                msg.addMessageElement(SenderMessage,
                                      new StringMessageElement(SenderMessage,
                                                               data,
                                                               null));
                msg.addMessageElement(InstanceNameMessage,
                                      new StringMessageElement(InstanceNameMessage,
                                                               instanceName,
                                                               null));
                if(i == 0) {
                    String readyMsgString = MESSAGE_READY;
                    msg.addMessageElement(ReadyMessage,
                                          new StringMessageElement(ReadyMessage,
                                                                   readyMsgString,
                                                                   null));
                    System.out.println("Sending :" + readyMsgString + ":" + instanceName);
                }
                System.out.println("Sending :" + data);
                pipe.send(msg);
            }
        } catch (Exception ie) {
            ie.printStackTrace();
        }
    }    
    
    OutputPipe createPropagatedOutputPipe() {

        ServerConfigLookup lookup = new ServerConfigLookup();
        String instanceName = lookup.getServerName();        
        PipeService pipeService = this.netPeerGroup.getPipeService();
        PipeAdvertisement pipeAdv = JxtaUtil.getPropagatedPipeAdvertisement();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("prop pipe adv: " + pipeAdv);
        }        
        OutputPipe op = null;
        try {
            //op = pipeService.createOutputPipe(pipeAdv, Collections.singleton(JxtaUtil.getPeerID(instanceName)), 10000); 
            op = pipeService.createOutputPipe(pipeAdv, 100);
        } catch (IOException ex) {
            //log this
        }
        return op;
    }
    
    private InputPipeWrapper createPropagatedInputPipeWrapper() {
        
        PipeService pipeService = this.netPeerGroup.getPipeService();
        PipeAdvertisement pipeAdv = JxtaUtil.getPropagatedPipeAdvertisement();
        InputPipe ip = null;
        InputPipeWrapper ipWrapper = new InputPipeWrapper();
        try {
            ip = pipeService.createInputPipe(pipeAdv, ipWrapper);
            ipWrapper.setPipe(ip);
        } catch (IOException ex) {
            //log this
        }
        return ipWrapper;
    }
    
     /**
     *  create n pipes (sender) and add them to
     * JxtaSenderPipeManager pool
     *
     *@param  numPipes
     *@returns boolean true if pipe creation successful - false otherwise
     */    
    private boolean createPipes(int numPipes) {
        PipeConnectionResult connectionResult = null;
        ArrayList pipeWrappers = new ArrayList();
        for (int i=0; i<numPipes; i++) {
            try {
                JxtaBiDiPipe nextPipe = new JxtaBiDiPipe();
                nextPipe.setReliable(true);
                PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.SENDER_PIPE, this.getReplicateToInstanceName(), nextPipe);
                /*
                if(i == 0) {
                    this.pipe = nextPipe;
                    //this.waitForRendezvousConnection();
                }
                 */
                //PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.SENDER_PIPE, this.getReplicateToInstanceName(), nextPipe);
                //this.waitForRendezvousConnection();
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("JxtaBiDiPipeWrapper: Attempting to establish a connection");
                }
                /*
                nextPipe.connect(this.netPeerGroup,
                                null,
                                this.pipeAdv,
                                //60000,
                                300000,
                                // register as a message listener the pipeWrapper
                                pipeWrapper);
                nextPipe.setPipeEventListener(pipeWrapper);
                 */
                System.out.println("JxtaBiDiPipeWrapper:run:before connectPipeWithRetries");          
                connectionResult = connectPipeWithRetries(nextPipe, pipeWrapper);
                System.out.println("JxtaBiDiPipeWrapper:run:after connectPipeWithRetries:connectionResult=" + connectionResult.isConnected());          
                pipeWrapper = connectionResult.getPipeWrapper();
                //pipeWrapper = connectPipeWithRetries(nextPipe, pipeWrapper);                
                //if we timed out without a connection break
                //or if connection attempts are interrupted
                if(!connectionResult.isConnected() || !isAttemptingConnection()) {
                    System.out.println("JxtaBiDiPipeWrapper>>unable to complete createPipes:breaking:i= " + i);
                    System.out.println("JxtaBiDiPipeWrapper>>createPipes:connectionResult: " + connectionResult.isConnected());
                    System.out.println("JxtaBiDiPipeWrapper>>createPipes:isAttemptingConnection(): " + isAttemptingConnection());                    
                    break;
                }                
                //at this point we need to keep references around until data xchange
                //is complete
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("JxtaBiDiPipe pipe # " + i + " created");
                }                
                if(i == 0) {
                    //JxtaReceiverPipeManager.createInstance().setReceiverPipe(this);
                    //JxtaReceiverPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper);
                    JxtaSenderPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper);
                } else {
                    pipeWrappers.add(pipeWrapper);
                }
            } catch (IOException ex) {
                //FIXME
            }
        }
        System.out.println("JxtaBiDiPipeWrapper>>createPipes stage one complete:connectionResultIsConnected:" + connectionResult.isConnected());
        if(connectionResult.isConnected()) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("sender pipe pool about to be initialized");
            }
            //JxtaReceiverPipeManager.createInstance().initPipePool(pipeWrappers);
            JxtaSenderPipeManager.createInstance().initPipePool(pipeWrappers);
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("sender pipe pool initialized");
            }
        }
        return connectionResult.isConnected();
    }
    
     /**
     *  create n pipes (sender) and add them to
     * JxtaSenderPipeManager pool
     *
     * @param  numPipes
     * @param  pipeAdvertisement to destination instance
     * @param  newPartnerInstance destination instance name
     */    
    private boolean createPipes(int numPipes, PipeAdvertisement pipeAdvertisement, String newPartnerInstance) {
System.out.println("JxtaBiDiPipeWrapper>>createPipes:newPartnerInstance: " + newPartnerInstance);
        PipeConnectionResult connectionResult = null;
        PeerID partnerPeerId = JxtaStarter.getPeerID(newPartnerInstance);
        ArrayList pipeWrappers = new ArrayList();
        for (int i=0; i<numPipes; i++) {
            try {
                JxtaBiDiPipe nextPipe = new JxtaBiDiPipe();
                nextPipe.setReliable(true);
                PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.SENDER_PIPE, newPartnerInstance, nextPipe);
                /*
                if(i == 0) {
                    this.pipe = nextPipe;
                    //this.waitForRendezvousConnection();
                }
                 */
                //PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.SENDER_PIPE, this.getReplicateToInstanceName(), nextPipe);
                //this.waitForRendezvousConnection(); 
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("JxtaBiDiPipeWrapper: Attempting to establish a connection");
                }
                
                connectionResult = connectPipeWithRetries(nextPipe, pipeWrapper, pipeAdvertisement, partnerPeerId);
                pipeWrapper = connectionResult.getPipeWrapper();                
                //if we timed out without a connection break
                //or if connection attempts are interrupted
                if(!connectionResult.isConnected() || !isAttemptingConnection()) {
                    System.out.println("JxtaBiDiPipeWrapper>>createPipes:breaking:i= " + i);
                    break;
                }                 
                //pipeWrapper = connectPipeWithRetries(nextPipe, pipeWrapper, pipeAdvertisement);                
                
                //at this point we need to keep references around until data xchange
                //is complete
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("JxtaBiDiPipe pipe # " + i + " created");
                }                
                if(i == 0) {
                    JxtaSenderPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper);
                } else {
                    pipeWrappers.add(pipeWrapper);
                }
            } catch (IOException ex) {
                //FIXME
            }
        }
System.out.println("JxtaBiDiPipeWrapper>>createPipes stage one complete:connectionResultIsConnected:" + connectionResult.isConnected());
        if(connectionResult.isConnected()) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("sender pipe pool about to be initialized");
            }            
            JxtaSenderPipeManager.createInstance().initPipePool(pipeWrappers);
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("sender pipe pool initialized");
            }             
        }
        return connectionResult.isConnected();
    }    
    
     /**
     *  create n pipes (sender) and add them to
     * JxtaSenderPipeManager pool
     *
     *@param  numPipes
     */    
    private void createPipesPrevious(int numPipes) {
        ArrayList pipeWrappers = new ArrayList();
        for (int i=0; i<numPipes; i++) {
            try {
                JxtaBiDiPipe nextPipe = new JxtaBiDiPipe();
                nextPipe.setReliable(true);
                PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.SENDER_PIPE, this.getReplicateToInstanceName(), nextPipe);
                /*
                if(i == 0) {
                    this.pipe = nextPipe;
                    this.waitForRendezvousConnection();
                }
                 */
                //PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.SENDER_PIPE, this.getReplicateToInstanceName(), nextPipe);
                //this.waitForRendezvousConnection();
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("JxtaBiDiPipeWrapper: Attempting to establish a connection");
                }
                nextPipe.connect(this.netPeerGroup,
                                null,
                                this.pipeAdv,
                                //60000,
                                300000,
                                // register as a message listener the pipeWrapper
                                pipeWrapper);
                nextPipe.setPipeEventListener(pipeWrapper);
                //at this point we need to keep references around until data xchange
                //is complete
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("JxtaBiDiPipe pipe # " + i + " created");
                }
                if(i == 0) {
                    JxtaSenderPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper);
                } else {
                    pipeWrappers.add(pipeWrapper);
                }
            } catch (IOException ex) {
                //FIXME
            }
        }
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("sender pipe pool about to be initialized");
        }
        JxtaSenderPipeManager.createInstance().initPipePool(pipeWrappers);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("sender pipe pool initialized");
        }
    }
    
    private PipeConnectionResult connectPipeWithRetries(JxtaBiDiPipe aPipe, PipeWrapper pipeWrapper) {
        return connectPipeWithRetries(aPipe, pipeWrapper, this.pipeAdv, this.peerID);
    }     
    
    private PipeConnectionResult connectPipeWithRetries(JxtaBiDiPipe aPipe, 
            PipeWrapper pipeWrapper, PipeAdvertisement pipeAdvertisement, PeerID partnerPeerId) {
        PipeConnectionResult result = new PipeConnectionResult(pipeWrapper);
        JxtaConnectErrorManager anErrorMgr = getErrorManager();
        try {
            anErrorMgr.txStart();
            while ( ! anErrorMgr.isTxCompleted()  && isAttemptingConnection() && ! ReplicationHealthChecker.isStopping() ) {                 
                try {
                    /*
                    try {
                        Thread.currentThread().sleep(1000L);
                    } catch(Exception ex) {;} 
                     */                   
                    //System.out.println("calling aPipe.connect");
                    aPipe.connect(this.netPeerGroup,
                            null,                            
                            pipeAdvertisement, //was this.pipeAdv,
                            //60000,
                            //300000,
                            10000,
                            // register as a message listener the pipeWrapper
                            pipeWrapper);
                    aPipe.setPipeEventListener(pipeWrapper); 
                    result.setConnected(true);
                    anErrorMgr.txEnd();
                    
                } catch (IOException e) {
                    //System.out.println("IOException during call to aPipe.connect - IOException:message: " + e.getMessage() + " cause: " + e.getCause());
                    //e.printStackTrace();
                    result.setConnected(false);
                    anErrorMgr.checkError(e);
                } catch (Exception e1) {
                    //System.out.println("calling aPipe.connect - other Exception");
                    //handle any other exception (e.g. during shutdown)
                    result.setConnected(false);
                    anErrorMgr.txEnd();
                }
            }  //end while
            anErrorMgr.txEnd();
        }   //end try
        catch (HATimeoutException tex) {
            // eat and log this exception
            //set pipeWrapper to null
            pipeWrapper = null;
            result.setPipeWrapper(null);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper connectPipeWithRetries() timed out: " + tex.getMessage());
            }
        } finally {
            anErrorMgr.txEnd();
        }        

        return result;
    } 
    
    private PipeWrapper connectPipeWithRetriesLastGood(JxtaBiDiPipe aPipe, PipeWrapper pipeWrapper) {
        return connectPipeWithRetriesLastGood(aPipe, pipeWrapper, this.pipeAdv);
    }     
    
    private PipeWrapper connectPipeWithRetriesLastGood(JxtaBiDiPipe aPipe, 
            PipeWrapper pipeWrapper, PipeAdvertisement pipeAdvertisement) {
        try {
            errorMgr.txStart();
            while ( ! errorMgr.isTxCompleted()  && ! ReplicationHealthChecker.isStopping() ) {                 
                try {                    
                    aPipe.connect(this.netPeerGroup,
                            null,                            
                            pipeAdvertisement, //was this.pipeAdv,
                            //60000,
                            //300000,
                            15000,
                            // register as a message listener the pipeWrapper
                            pipeWrapper);
                    aPipe.setPipeEventListener(pipeWrapper); 
                    errorMgr.txEnd();
                    
                } catch (IOException e) {
                    errorMgr.checkError(e);
                }
            }  //end while
            errorMgr.txEnd();
        }   //end try
        catch (HATimeoutException tex) {
            // eat and log this exception
            //set pipeWrapper to null
            pipeWrapper = null;
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper connectPipeWithRetries() timed out: " + tex.getMessage());
            }
        } finally {
            errorMgr.txEnd();
        }        

        return pipeWrapper;
    }          
    
     /**
     *  create n pipes (sender) and add them to
     * JxtaSenderPipeManager pool
     *
     *@return PipeWrapper
     */    
    PipeWrapper createPipe() {
        PipeWrapper pipeWrapper = null;
        try {
            JxtaBiDiPipe aPipe = new JxtaBiDiPipe();
            aPipe.setReliable(true);
            pipeWrapper = new PipeWrapper("created_pipe", PipeWrapper.SENDER_PIPE, this.getReplicateToInstanceName(), aPipe);

            //this.waitForRendezvousConnection(); 
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper: Attempting to establish a connection");
            }            
            aPipe.connect(this.netPeerGroup,
                            null,
                            this.pipeAdv,
                            //60000,
                            300000,
                            // register as a message listener the pipeWrapper
                            pipeWrapper);
            aPipe.setPipeEventListener(pipeWrapper);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("new JxtaBiDiPipe pipe created: " + aPipe);
            }
        } catch (IOException ex) {
            //FIXME
        }
        return pipeWrapper;
    }       
    
    /**
     *  Starts jxta
     */
    private void startJxta(boolean isServer) {         
        JxtaStarter jxtaStarter = JxtaStarter.createInstance();
        jxtaStarter.startJxta(isServer);
        this.netPeerGroup = jxtaStarter.getNetPeerGroup();
        RendezVousService theRendezvous = jxtaStarter.getRendezvous();
        if(theRendezvous != null) {
            theRendezvous.addListener(this);
        }
    }
    
    /**
     *  This is the PipeListener interface. Expect a call to this method
     *  When a message is received.
     *  when we get a message, print out the message on the console
     *
     *@param  event  message event
     */
    public void pipeMsgEvent(PipeMsgEvent event) {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper>>pipeMsgEvent");
        }
        Message msg = null;
        try {
            // grab the message from the event
            msg = event.getMessage();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper>>pipeMsgEvent:msg=" + msg);
            }           
            if (msg == null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Received an empty message, returning");
                }
                return;
            }

            // get the message element named SenderMessage
            MessageElement msgElement = msg.getMessageElement(SenderMessage, SenderMessage);
            MessageElement idMsgElement = msg.getMessageElement(MESSAGE_ID, MESSAGE_ID);
            if(msgElement != null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("msgElement=" + msgElement.toString());
                }                
            }
            if(idMsgElement != null) {
                if(_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("idMsgElement=" + idMsgElement.toString());
                }                 
            }            

            // Get message
            if (msgElement != null) {
                this.processStartupMessage(msg, msgElement);
            } else {
                if (idMsgElement != null) {
                    this.processIdMessage(msg, idMsgElement); 
                } else {
                    //this shouldn't happen
                }
            }
        } catch (Exception e) {
            if(_logger.isLoggable(Level.FINEST)) {
                e.printStackTrace();
            }             
            return;
        }
    }
    
    private void processStartupMessage(Message msg, MessageElement msgElement) {
        // Get message
        if (msgElement.toString() == null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("null msg received");
            }             
        } else {
            //Date date = new Date(System.currentTimeMillis());
            //System.out.println("Message  :"+ msgElement.toString());
            /*
            //send back response if it isn't already a response
            if( !(msgElement.toString()).startsWith("RETURN_MSG_COMMAND")) {
                Message returnMsg = new Message();
                String returnData = "ReturnMessage  :"+ msgElement.toString();
                returnMsg.addMessageElement(SenderMessage,
                                      new StringMessageElement(SenderMessage,
                                                               returnData,
                                                               null));
                System.out.println("SendingResponse :" + returnData);

                //pipe.sendMessage(returnMsg);
                this.sendMessage(returnMsg);
            }

            //end send back response
             */
            count ++;
        }
    }
    
    private void processIdMessage(Message msg, MessageElement idMsgElement) {
        if (idMsgElement.toString() == null) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("null msg received");
            }              
        } else {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("ID Message :"+ idMsgElement.toString());
            }              
            ReplicationState state = this.createReplicationState(msg);

            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("ID Message is response: " + state.isReturnMessage());
            }             
            if ( !state.isReturnMessage() ) {    
                //can do it here more quickly but for now comment out
                //this.sendResponse(msg);
            } else {
                //this is an incoming response
                //this is done elsewhere take out after testing
                //this.processIdMessageResponse(state);
            }
            this.finishProcessIdMessage(state);
        }        
    }
    
    private void sendMessage(Message msg) {
        JxtaReplicationSender jxtaReplicationSender =
            JxtaReplicationSender.createInstance();
        jxtaReplicationSender.sendOverPipe(msg);
    }
    
    private void sendResponse(Message msg) {
        Message responseMsg = 
            this.alterIncomingMessageToResponse(msg);
        this.sendMessage(responseMsg);
    }
    
    private Message alterIncomingMessageToResponse(Message msg) {
        msg.replaceMessageElement(MESSAGE_COMMAND,
                              new StringMessageElement(MESSAGE_COMMAND,
                                                       RETURN_MSG_COMMAND,
                                                       null));
        return msg;
    } 
    
    private ReplicationState createReplicationState(Message msg) {
        return ReplicationState.createReplicationState(msg); 
    }
    
    private void finishProcessIdMessage(ReplicationState state) {
        JxtaReplicationReceiver receiver = 
            JxtaReplicationReceiver.createInstance();
        receiver.processMessage(state);
    }       
    
    /**
     *  stop
     *
     */
    public void stop() {
        if(stopped) {
            return;
        }
        stopped = true;
        //flush caches during shutdown
        //repairOnCurrentThread();
        //no log message here
        ReplicationHealthChecker.setStopping(true);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper-stop() method");
        }
        try {
            JxtaReplicationSender.createInstance().stop();
        } catch (LifecycleException ex) {
            ;
        }
        closePipes();
        closeHealthPipes();
        closePropagatedPipes();
        //System.out.println("JxtaBiDiPipeWrapper stopping ReplicationResponseRepository");
        ReplicationResponseRepository.getInstance().stop();        
        //we are stopped
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper:stop() complete");
        }               
        //FIXME is this needed??
        synchronized(completeLock) {
            completeLock.notify();
        }        
    }
    
    public void closePipes() {
        JxtaSenderPipeManager pipeMgr 
            = JxtaSenderPipeManager.createInstance();
        pipeMgr.closePooledPipes();
    }

    /**
     *  closeNonPropagatedSenderConnections
     *
     */    
    private void closeNonPropagatedSenderConnections() {
        closePipes();
        closeSenderHealthPipe();       
    }
    
    /**
     *  closeConnections
     *
     */
    public void closeConnections() {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper-closeConnections() method");
        }      
        this.stop();
    }
    
    /**
     *  restart
     *
     *@param  args  command line args
     */
    public void restart() {
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper-restart() method");
        }        
        String newPartnerInstance = this.getReplicateToInstanceName();
        closeConnections();
        //this.startJxta(false);

        try {
            String value = System.getProperty("RDVWAIT", "false");
            this.waitForRendezvous = Boolean.valueOf(value).booleanValue();

            this.pipeAdv = JxtaUtil.getPipeAdvertisement(newPartnerInstance);
            this.peerID = JxtaStarter.getPeerID(newPartnerInstance);
            /* FIXME - moving this to run method
            int numPipes = this.getNumberOfPipes();
            System.out.println("JxtaBiDiPipeWrapper-numPipes=" + numPipes);            
            createPipes(numPipes);
            initializePropagatedPipes();
            sentTestPropagatedMessage();
             */
            
            /* FIXME: check later on this - comment this waiting out for now
            this.waitUntilCompleted();
            this.netPeerGroup.stopApp();
            this.netPeerGroup.unref();
             */
        } catch (Exception e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("failed to bind the JxtaBiDiPipe due to the following exception");
                e.printStackTrace();                
            }            
        }
        // run on this thread
        //this.run();
        Thread acceptThread = new Thread(this);
        acceptThread.setDaemon(true);
        acceptThread.start();
        try {
            //might have to put a timeout on this
            acceptThread.join();
        } catch (InterruptedException ex) {}
        //now do repair
        this.repair();
        
    }
    
    /**
     *  reshape
     *
     *@param  partnerInstanceName name of failed partner
     */
    public void reshape(String partnerInstanceName) {
        ReplicationHealthChecker healthChecker
            = ReplicationHealthChecker.getInstance();
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }        
        healthChecker.displayCurrentGroupMembers();
        String newPartnerInstance 
            = healthChecker.getReshapeReplicateToInstanceName(partnerInstanceName);
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("JxtaBiDiPipeWrapper>>reshape:newPartnerInstance: " + newPartnerInstance);            
        }
        System.out.println("JxtaBiDiPipeWrapper>>reshape:newPartnerInstance: " + newPartnerInstance);
        System.out.println("JxtaBiDiPipeWrapper>>reshape:currentPartnerInstance: " + this.getCurrentPartnerInstanceName());
        System.out.println("JxtaBiDiPipeWrapper>>reshape:isAttemptingConnection:" + this.isAttemptingConnection());
        //these following cases means nothing to do
        if( this.isAttemptingConnection() || newPartnerInstance == null             
            || this.isOurself(newPartnerInstance)) {
            return;
        }
        if( newPartnerInstance.equalsIgnoreCase(this.getCurrentPartnerInstanceName()) ) {
            //skip connect if proposed partner is already our partner
            //and our connection tests as ok
            boolean currentPipesOk = this.areCurrentPipesOk();
            System.out.println("JxtaBiDiPipeWrapper>>reshape:before reshape - currentPipesOk = " + currentPipesOk);
            if(currentPipesOk) {
                return;
            } 
        }
        /* FIXME was this - above - remove after testing
        if( this.isAttemptingConnection() || newPartnerInstance == null 
            || newPartnerInstance.equalsIgnoreCase(this.getCurrentPartnerInstanceName())
            || this.isOurself(newPartnerInstance)) {
            return;
        }
         */ 
        //not all connections; only sender side
        System.out.println("JxtaBiDiPipeWrapper>>reshape:beforeConnectToInstance:newPartnerInstance: " + newPartnerInstance);
        System.out.println("JxtaBiDiPipeWrapper>>reshape:beforeConnectToInstance:currentPartnerInstance: " + this.getCurrentPartnerInstanceName());        
        closeNonPropagatedSenderConnectionsAndConnectToInstance(newPartnerInstance);
        /* FIXME remove after testing needed to synchronize these 2 methods together
        closeNonPropagatedSenderConnections();        
        connectToInstance(newPartnerInstance);
         */ 
        System.out.println("JxtaBiDiPipeWrapper:end of reshape: new partner=" + getCurrentPartnerInstanceName());
    }
    
    /**
     *  respondToFailure
     *
     *@param  failedPartnerInstance name of failed instance
     * may or may not be our partner
     */
    public void respondToFailure(String failedPartnerInstance) {
        JxtaSenderPipeManager senderPipeMgr 
            = JxtaSenderPipeManager.createInstance();
        if(!senderPipeMgr.isOurPartnerInstance(failedPartnerInstance)) {
            return;
        }
        this.reshape(failedPartnerInstance);
    }
    
    /**
     *  respondToFailure
     *
     *@param  failedPartnerInstance name of failed instance
     * may or may not be our partner
     *@param force will force a reshape
     */
    public void respondToFailure(String failedPartnerInstance, boolean force) {
        if(!force) {
            JxtaSenderPipeManager senderPipeMgr 
                = JxtaSenderPipeManager.createInstance();
            if(!senderPipeMgr.isOurPartnerInstance(failedPartnerInstance)) {
                return;
            }
        }
        this.reshape(failedPartnerInstance);
    }         
    
    /**
     *  connectToNew
     *
     *@param  newPartnerInstance name of new partner
     */
    public void connectToNew(String newPartnerInstance) {
        ReplicationHealthChecker healthChecker
            = ReplicationHealthChecker.getInstance();
        /*
        if(ReplicationHealthChecker.isStopping() || !isAlone()) {
            return;
        }
         */
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:thisInstance = " + this.getInstanceName());
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:newPartnerInstance = " + newPartnerInstance);
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:currentPartnerInstance = " + this.getCurrentPartnerInstanceName());
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:isStopping = " + ReplicationHealthChecker.isStopping());
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }
        //FIXME remove after test moved into isBetterReplicationPartner method
        //and stop or improve the check for same partner
        /*
        if( newPartnerInstance == null || newPartnerInstance.equalsIgnoreCase(this.getCurrentPartnerInstanceName()) ) {
            return;
        }
         */        
        //this following case means nothing to do
        if(!isBetterReplicationPartner(newPartnerInstance)) {
            return;
        }
        //FIXME remove after testing
        boolean currentPartnerHealthy = this.areCurrentPipesOk();
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:before reconnect - currentPartnerHealthy = " + currentPartnerHealthy);
        //FIXME end remove after testing
        /* logic moved into connectToInstance
        if( newPartnerInstance.equalsIgnoreCase(this.getCurrentPartnerInstanceName())  && currentPartnerHealthy) {
            //skip connect if proposed partner is already our partner
            //and our connection tests as ok
            boolean currentPipesOk = this.areCurrentPipesOk();
            System.out.println("JxtaBiDiPipeWrapper>>connectToNew:before reconnect - currentPipesOk = " + currentPipesOk);
            if(currentPipesOk) {
                return;
            }
        } 
         */       
        healthChecker.displayCurrentGroupMembers();
        //need to interrupt waiting
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:isAttemptingConnection = " + isAttemptingConnection());
        if(isAttemptingConnection()) {
            //not all connections; only sender side
            //must use force reconnect to insure old connections get torn down
            //correctly
            interruptConnectionAttempt();
            closeNonPropagatedSenderConnectionsAndConnectToInstance(newPartnerInstance, true);
        } else {
            //not all connections; only sender side
            closeNonPropagatedSenderConnectionsAndConnectToInstance(newPartnerInstance);
        }
        /* FIXME was next 2 lines - needed to synchronize them 
        closeNonPropagatedSenderConnections();
        connectToInstance(newPartnerInstance); 
         */
        System.out.println("JxtaBiDiPipeWrapper:end of connectToNew: new partner=" + getCurrentPartnerInstanceName());
        //FIXME remove after test
        currentPartnerHealthy = this.areCurrentPipesOk();
        System.out.println("JxtaBiDiPipeWrapper>>connectToNew:after reconnect - currentPartnerHealthy = " + currentPartnerHealthy);
        //FIXME end remove after test
    }
    
    private synchronized void closeNonPropagatedSenderConnectionsAndConnectToInstance(String newPartnerInstance) {
        closeNonPropagatedSenderConnectionsAndConnectToInstance(newPartnerInstance, false);
    }    
    
    private synchronized void closeNonPropagatedSenderConnectionsAndConnectToInstance(String newPartnerInstance, boolean forceReconnect) {
        System.out.println("JxtaBiDiPipeWrapper>>closeNonPropagatedSenderConnectionsAndConnectToInstance:forceReconnect:" + forceReconnect);
        // first check if proposed connections already exist and are good
        if(!forceReconnect) {
            if(checkIfProposedConnectionAlreadyOk(newPartnerInstance)) {
                System.out.println("JxtaBiDiPipeWrapper>>closeNonPropagatedSenderConnectionsAndConnectToInstance:connections already good returning for: " + newPartnerInstance);
                return;
            }
        }
        System.out.println("JxtaBiDiPipeWrapper>>closeNonPropagatedSenderConnectionsAndConnectToInstance:doing close and connect for: " + newPartnerInstance);
        closeNonPropagatedSenderConnections();
        connectToInstance(newPartnerInstance); 
    }
    
    boolean checkIfProposedConnectionAlreadyOk(String newPartnerInstance) {
        String currentPartnerInstance = this.getCurrentPartnerInstanceName();
        System.out.println("JxtaBiDiPipeWrapper>>checkIfProposedConnectionAlreadyOk: " + newPartnerInstance);
        System.out.println("JxtaBiDiPipeWrapper>>checkIfProposedConnectionAlreadyOk:currentPartnerInstance: " + currentPartnerInstance);
        //check if we are already connected ok to newPartnerInstance
        if(currentPartnerInstance != null && currentPartnerInstance.equalsIgnoreCase(newPartnerInstance)) {
            //skip connect if proposed partner is already our partner
            //and our connection tests as ok
            boolean currentPipesOk = this.areCurrentPipesOk();
            System.out.println("JxtaBiDiPipeWrapper>>checkIfProposedConnectionAlreadyOk: - currentPipesOk = " + currentPipesOk);
            return(currentPipesOk);
        } else {
            return false;
        } 
    }
    
    boolean areCurrentPipesOk() {
        ReplicationHealthChecker healthChecker
            = ReplicationHealthChecker.getInstance();
        String currentInstanceName = this.getInstanceName();
        String currentPartnerInstanceName = this.getCurrentPartnerInstanceName();
        boolean currentPartnerHealthy = healthChecker.doPipeTest();
        System.out.println("JxtaBiDiPipeWrapper>>areCurrentPipesOk from: " + currentInstanceName + " to: " + currentPartnerInstanceName + ": " + currentPartnerHealthy);        
        return currentPartnerHealthy;
    }
    
    private boolean connectToInstance(String newPartnerInstance) {
        return connectToInstance(newPartnerInstance, true);
    }
    
    private boolean connectToInstance(String newPartnerInstance, boolean repairNeeded) {
        String currentPartnerInstance = this.getCurrentPartnerInstanceName();
        System.out.println("JxtaBiDiPipeWrapper>>connectToInstance: " + newPartnerInstance);
        System.out.println("JxtaBiDiPipeWrapper>>connectToInstance:currentPartnerInstance: " + currentPartnerInstance);
        //check if we are already connected ok to newPartnerInstance
        if(currentPartnerInstance != null && currentPartnerInstance.equalsIgnoreCase(newPartnerInstance)) {
            //skip connect if proposed partner is already our partner
            //and our connection tests as ok
            boolean currentPipesOk = this.areCurrentPipesOk();
            System.out.println("JxtaBiDiPipeWrapper>>connectToInstance:before connect - currentPipesOk = " + currentPipesOk);
            if(currentPipesOk) {
                return true;
            }            
        }
        
        boolean reshapeSuccessful = false;
        try {
            String value = System.getProperty("RDVWAIT", "false");
            this.waitForRendezvous = Boolean.valueOf(value).booleanValue();
            PipeAdvertisement pipeAdvertisement = JxtaUtil.getPipeAdvertisement(newPartnerInstance);
            System.out.println("pipeAdvertisement for: " + newPartnerInstance + " is: " + pipeAdvertisement);
            System.out.println("this.pipeAdv: " + this.pipeAdv);
            int numPipes = this.getNumberOfPipes();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("JxtaBiDiPipeWrapper>>connectToInstance-numPipes=" + numPipes); 
                _logger.finest("JxtaBiDiPipeWrapper>>connectToInstance:reshape pipeAdv=" + pipeAdvertisement);
            }
            System.out.println("JxtaBiDiPipeWrapper>>connectToInstance-numPipes=" + numPipes); 
            System.out.println("JxtaBiDiPipeWrapper>>connectToInstance:reshape pipeAdv=" + pipeAdvertisement);
            long startTime = System.currentTimeMillis();
            setAttemptingConnection(true);
            reshapeSuccessful = createPipes(numPipes, pipeAdvertisement, newPartnerInstance);
            setAttemptingConnection(false);
            System.out.println("JxtaBiDiPipeWrapper>>createPipes took " + (System.currentTimeMillis() - startTime) + " millis");
            //System.out.println("JxtaBiDiPipeWrapper>>connectToInstance:reshapeSuccessful=" + reshapeSuccessful);     
            //initializePropagatedPipes();
            //sentTestPropagatedMessage();
            
            /* FIXME: check later on this - comment this waiting out for now
            this.waitUntilCompleted();
            this.netPeerGroup.stopApp();
            this.netPeerGroup.unref();
             */
        } catch (Exception e) {
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("failed to bind the JxtaBiDiPipe due to the following exception");
                e.printStackTrace();
            }
            setAttemptingConnection(false);
        }
        // run on this thread
        //this.run();
        /* no we do this in-line in this thread (above)
        Thread acceptThread = new Thread(this);
        acceptThread.setDaemon(true);
        acceptThread.start();
        try {
            //might have to put a timeout on this
            acceptThread.join();
        } catch (InterruptedException ex) {}
         */
        //now do repair if reshape successful
        ReplicationHealthChecker.setReplicationCommunicationOperational(reshapeSuccessful);
        if(reshapeSuccessful) {            
            if(repairNeeded) {
                this.repair();
            }
        }
        return reshapeSuccessful;
    }
    
    private void repair() {
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }        
        //do the repair on background thread
        Thread repairThread = new Thread(new JxtaRepair());
        repairThread.setDaemon(true);
        repairThread.start();       
    }
    
    void repairOnCurrentThread() {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("repairOnCurrentThread");
        }         
        //System.out.println("repairOnCurrentThread");
        //if cluster is stopping do not proceed
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("repairOnCurrentThread:will skip if ClusterStopping: " + ReplicationHealthChecker.isClusterStopping());
        }         
        //System.out.println("repairOnCurrentThread:will skip if ClusterStopping: " + ReplicationHealthChecker.isClusterStopping());
        if(ReplicationHealthChecker.isClusterStopping()) {
            return;
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("repairOnCurrentThread: setting flushing true");
        }        
        //System.out.println("repairOnCurrentThread: setting flushing true");
        ReplicationHealthChecker.setFlushing(true);
        try {
            ReplicationMessageRouter router 
                = ReplicationMessageRouter.createInstance();
            router.repairApps(System.currentTimeMillis(), false);
        } finally {
            ReplicationHealthChecker.setFlushing(false);
        }
    }
    
    private String getCurrentPartnerInstanceName() {
        JxtaSenderPipeManager jxtaSenderPipeManager 
            = JxtaSenderPipeManager.createInstance();
        return jxtaSenderPipeManager.getPartnerInstanceName();
    }
    
    private boolean isOurself(String instanceName) {
        if(instanceName == null) {
            return false;
        } else {
            return instanceName.equalsIgnoreCase(this.getInstanceName());
        }
    }
    
    boolean isBetterReplicationPartner(String proposedPartnerName) {
        if(proposedPartnerName == null) {
            return false;
        }                
        String currentInstanceName = this.getInstanceName();
        String currentPartnerInstanceName = this.getCurrentPartnerInstanceName();
        SimpleInstanceArranger arranger = this.getSimpleArranger();
        return arranger.isBetterOrSameAsReplicationPartner(proposedPartnerName, 
            currentPartnerInstanceName, currentInstanceName);
        /* was this above allowing reconnect to current partner
        return arranger.isBetterReplicationPartner(proposedPartnerName, 
            currentPartnerInstanceName, currentInstanceName);
         */
    } 
    
    /**
     *  rendezvousEvent the rendezvous event
     *  This method is called when an rendevous event occurs, this example is
     *  only interested in a connection to a rendezvous.  Waiting for a rendezvous
     *  is beneficial when trying to connect to another node beyonf the local 
     *  sub-net, or where multicast is not supported.
     *
     *@param  event   rendezvousEvent
     */
    public void rendezvousEvent(RendezvousEvent event) {
        System.out.println(event.getType());
        if (event.getType() == event.RDVCONNECT ||
            event.getType() == event.RDVRECONNECT) {
            synchronized(rendezvousLock) {
                rendezvousLock.notify();
            }
        }
    }
    
    /**
     * awaits a rendezvous connection
     */
    private void waitForRendezvousConnection() {
        if (waitForRendezvous && !rendezvous.isConnectedToRendezVous()) {
            System.out.println("Waiting for Rendezvous Connection");
            try {
                synchronized(rendezvousLock) {
                    rendezvousLock.wait();
                }
                System.out.println("Connected to Rendezvous");
            } catch (InterruptedException e) {
                // got our notification
            }
        }
    }
    
    private void waitUntilCompleted() {
        try {
            System.out.println("Waiting for Messages.");
            synchronized(completeLock) {
                completeLock.wait();
            }
            pipe.close();
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
