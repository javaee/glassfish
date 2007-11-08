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
 * JxtaServerPipeWrapper.java
 *
 * Created on February 8, 2006, 1:38 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLDocument;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.Messenger;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.membership.InteractiveAuthenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;
import net.jxta.util.JxtaServerPipe;

import com.sun.enterprise.web.ServerConfigLookup;


/**
 *
 * @author Larry White
 */
public class JxtaServerPipeWrapper implements Runnable {
    
    /**
     *  Number of messages to send
     */
    public final static int ITERATIONS = 100;
    private PeerGroup netPeerGroup = null;
    private PipeAdvertisement pipeAdv;
    private JxtaServerPipe serverPipe;
    private final static MimeMediaType MEDIA_TYPE = new MimeMediaType("application/bin");
    private static final Logger _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private final static String SenderMessage = "pipe_tutorial";
    private final static String MESSAGE_MODE
        = ReplicationState.MESSAGE_MODE;
    private final static String MODE_STARTUP
        = ReplicationState.MODE_STARTUP;
    private Thread acceptThread = null;
    private boolean threadContinue = false; 
    
    /** Creates a new instance of JxtaServerPipeWrapper */
    public JxtaServerPipeWrapper() {
    }
    
    /**
     *  Starts jxta
     */
    private void startJxta(boolean isServer) {                 
        JxtaStarter jxtaStarter = JxtaStarter.createInstance();
        jxtaStarter.startJxta(isServer);
        this.netPeerGroup = jxtaStarter.getNetPeerGroup();        
    }    
    
    /**
     *  start
     *
     */
    public void start() {

        this.startJxta(true);

        try {
            this.pipeAdv = JxtaUtil.getPipeAdvertisement(this.getInstanceName());
            //System.out.println("this.netPeerGroup = " + this.netPeerGroup);          
            //System.out.println("this.pipeAdv = " + this.pipeAdv);            
            this.serverPipe = new JxtaServerPipe(this.netPeerGroup, this.pipeAdv);
            // we want to block until a connection is established
            this.serverPipe.setPipeTimeout(0);
        } catch (Exception e) {
            System.out.println("failed to bind to the JxtaServerPipe due to the following exception");
            e.printStackTrace();
            //System.exit(-1);
        }
        // run on this thread
        //this.run();
        this.acceptThread = new Thread(this);
        acceptThread.setDaemon(true);
        acceptThread.start();
    }
    
    /**
     *  stop
     */
    public void stop() {
        //FIXME finish this - decide if server pipe needs to be closed
        //likely not
        ReplicationHealthChecker.setStopping(true);
        this.stopAcceptThread();
        this.closeServerPipe();
        this.closePipes();
    }
    
    /**
     *  restart
     * @param partnerInstanceName
     * we are just tearing down the receiver pipes
     * for partnerInstanceName
     */
    public void restartLastGood(String partnerInstanceName) {
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }        
        //close existing connections first
        closeConnections();
        //FIXME may be able to cut this further
        
        //then re-start

        //this.startJxta(true);
        System.out.println("JxtaServerPipeWrapper restarting");
        try {
            this.pipeAdv = JxtaUtil.getPipeAdvertisement(this.getInstanceName());
            //System.out.println("this.netPeerGroup = " + this.netPeerGroup);          
            //System.out.println("this.pipeAdv = " + this.pipeAdv);
            this.serverPipe = new JxtaServerPipe(this.netPeerGroup, this.pipeAdv);
            // we want to block until a connection is established
            this.serverPipe.setPipeTimeout(0);
        } catch (Exception e) {
            System.out.println("failed to bind to the JxtaServerPipe due to the following exception");
            e.printStackTrace();
            //System.exit(-1);
        }
        // run on this thread
        //this.run();
        this.acceptThread = new Thread(this);
        acceptThread.setDaemon(true);
        acceptThread.start();
    }
    
    /**
     *  restart
     * @param partnerInstanceName
     * we are just tearing down the receiver pipes
     * for partnerInstanceName
     */
    public void restart(String partnerInstanceName) {
 //System.out.println("JxtaServerPipeWrapper>>restart:failedpartner=" + partnerInstanceName); 
 //System.out.println("JxtaServerPipeWrapper>>restart:stopping=" + ReplicationHealthChecker.isStopping());
        if(ReplicationHealthChecker.isStopping()) {
            return;
        }        
        //close existing connections first
        //closeConnections();
        //just closePipes receiving from partnerInstanceName
        this.closePipes(partnerInstanceName);
//System.out.println("JxtaServerPipeWrapper>>restart:finished stopping pooled pipes: instance:" + partnerInstanceName);
    }    
    
    /**
     *  closeConnections
     */
    void closeConnections() {
        ReplicationHealthChecker.setReplicationCommunicationOperational(false);
        //closed existing connections
        this.stopAcceptThread();
        this.closeServerPipe();
        this.closePipes();
    }    
    
    private void closeServerPipe() {
        try {
            this.serverPipe.close();
        } catch (IOException ex) {}
        //FIXME do not null out this.serverPipe remove after testing
        this.serverPipe = null;
    }
    
    public void closePipes() {
        //FIXME
        JxtaReceiverPipeManager pipeMgr 
            = JxtaReceiverPipeManager.createInstance();
        pipeMgr.closePooledPipes();
    }
    
    /**
     *  respondToFailure
     *
     *@param  failedPartnerInstance name of failed instance
     * may or may not be our partner
     */
    public void respondToFailure(String failedPartnerInstance) {
        this.closePipes(failedPartnerInstance);
    }    
    
    public void closePipes(String partnerInstanceName) {
        JxtaReceiverPipeManager pipeMgr 
            = JxtaReceiverPipeManager.createInstance();
        pipeMgr.closePooledPipes(partnerInstanceName);
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
    
    /**
     *  Main processing method for the JxtaServerPipeExample object
     */
    public void run() {
        //FIXME use next line when ready
        int numPipes = this.getNumberOfPipes();
        //System.out.println("JxtaServerPipeWrapper-numPipes=" + numPipes);
        /*
        String sourceInstanceName = initPipes(numPipes);
        sendTestMessages(sourceInstanceName);
         */
        initForNewPipeRequests();
    }     
    
    public String initPipes(int numPipes) {
        
        int i=0;
        JxtaBiDiPipe firstPipe = null;
        boolean connected = false;
        System.out.println("Waiting for JxtaBidiPipe connections on JxtaServerPipe");
        ArrayList pipeWrappers = new ArrayList();
        String sourceInstanceName = null;
        while (i < numPipes) {
            try {
                System.out.println("JxtaServerPipeWrapper:JxtaBidiPipe# " + i + " waiting to accept");
                JxtaBiDiPipe bipipe = serverPipe.accept();
                sourceInstanceName = bipipe.getRemotePeerAdvertisement().getName();
                //System.out.println("sourceInstanceName=" + sourceInstanceName);
                //for each pipe, create and register handler with manager
                //later this can be a collection or pool in manager
                if (bipipe != null) {
                    //ConnectionHandler handler = new ConnectionHandler(bipipe);
                    PipeWrapper pipeWrapper = new PipeWrapper("pipe#" + i, PipeWrapper.RECEIVER_PIPE, sourceInstanceName, bipipe);
                    bipipe.setMessageListener(pipeWrapper);
                    bipipe.setPipeEventListener(pipeWrapper);
                    if(i == 0) {
                        //first one is health pipe
                        //JxtaSenderPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper);
                        //JxtaReceiverPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper);
                        JxtaReceiverPipeManager.createInstance().setHealthPipeWrapper(pipeWrapper, sourceInstanceName);
                    } else {
                        //the rest go in the pool
                        pipeWrappers.add(pipeWrapper);
                    }

                    System.out.println("JxtaServerPipeWrapper:JxtaBidiPipe " + i + " accepted, ready for sending messages to the other end");
                    i++;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return sourceInstanceName;
            }
        }
        //System.out.println("sender pipe pool about to be initialized");
        //JxtaSenderPipeManager.createInstance().initPipePool(pipeWrappers);
        //JxtaReceiverPipeManager.createInstance().initPipePool(pipeWrappers);
        JxtaReceiverPipeManager.createInstance().initPipePool(pipeWrappers, sourceInstanceName);
        //System.out.println("sender pipe pool initialized for " + sourceInstanceName);
        return sourceInstanceName;
     
    }
    
    public void initForNewPipeRequests() {
      
        threadContinue = true;
        System.out.println("Waiting for additional JxtaBidiPipe connections on JxtaServerPipe");
        String sourceInstanceName = null;
        while (threadContinue) {
            try {
                System.out.println("JxtaServerPipeWrapper:JxtaBidiPipe# waiting to accept");
                JxtaBiDiPipe bipipe = serverPipe.accept();
                sourceInstanceName = bipipe.getRemotePeerAdvertisement().getName();           
                System.out.println("JxtaServerPipeWrapper:incoming pipe request accepted - sourceInstanceName=" + sourceInstanceName);
                System.out.println("JxtaServerPipeWrapper:incoming pipe request accepted - pipeAdv = " + bipipe.getRemotePeerAdvertisement().toString());
                //for each pipe, create and register handler with manager
                //later this can be a collection or pool in manager
                if (bipipe != null) {
                    PipeWrapper pipeWrapper = new PipeWrapper("pipe", PipeWrapper.RECEIVER_PIPE, sourceInstanceName, bipipe);
                    bipipe.setMessageListener(pipeWrapper);
                    bipipe.setPipeEventListener(pipeWrapper);
                    //JxtaReceiverPipeManager.createInstance().addPipeWrapper(pipeWrapper);
                    JxtaReceiverPipeManager.createInstance().addPipeWrapper(pipeWrapper, sourceInstanceName);                  
                    System.out.println("JxtaServerPipeWrapper:JxtaBidiPipe accepted added to pool(sourceInstanceName=" + sourceInstanceName + "), ready for sending messages to the other end");
                }
            } catch (java.net.SocketException se) {
                threadContinue = false;
            } catch (Exception e) {
                if(!ReplicationHealthChecker.isStopping()) {
                    e.printStackTrace();
                }
                return;
            }
        }     
     
    } 
    
    private void stopAcceptThread() {
        threadContinue = false;
        acceptThread.interrupt();
        try {
            acceptThread.join();
        } catch(InterruptedException e) {
            ;
        }
    }
    
    private void sendTestMessages(String sourceInstanceName) {
        System.out.println("using health pipe: sending 100 messages to the other end");
        PipeWrapper pipeWrapper = 
            JxtaReceiverPipeManager.createInstance().getHealthPipeWrapper(sourceInstanceName);
        JxtaBiDiPipe bipipe = pipeWrapper.getPipe();
        Thread thread = new Thread(new ConnectionHandler(bipipe), "Connection Handler Thread");
        thread.start();
    }    
    
    /**
     * wait for msgs
     */
    class ConnectionHandler implements Runnable, PipeMsgListener {
        JxtaBiDiPipe pipe = null;

        /**
         *Constructor for the ConnectionHandler object
         *
         * @param  pipe  Description of the Parameter
         */
        ConnectionHandler(JxtaBiDiPipe pipe) {
            this.pipe = pipe;
            pipe.setMessageListener(this);
        }
        
        JxtaBiDiPipe getPipe() {
            return this.pipe;
        }

        public void pipeMsgEvent(PipeMsgEvent event) {

            Message msg = null;
            try {
                // grab the message from the event
                msg = event.getMessage();
                if (msg == null) {
                     if (_logger.isLoggable(Level.FINEST)) {
                        _logger.finest("Received an empty message, returning");
                     }                    
                    return;
                }
                 if (_logger.isLoggable(Level.FINEST)) {
                    _logger.finest("Received a response");
                 }                 
                // get the message element named SenderMessage
                MessageElement msgElement = msg.getMessageElement(SenderMessage, SenderMessage);
                // Get message
                if (msgElement.toString() == null) {
                    System.out.println("null msg received");
                } else {
                    System.out.println("Message  :"+ msgElement.toString());
                }
            } catch (Exception e) {
                 if (_logger.isLoggable(Level.FINEST)) {
                    e.printStackTrace();
                 }                 
                return;
            }

        }

        /**
         *  Send a series of messages over a pipe
         *
         * @param  pipe  Description of the Parameter
         */
        private void sendTestMessages(JxtaBiDiPipe pipe) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            String instanceName = lookup.getServerName();            
            try {
                for (int i = 0; i < ITERATIONS; i++) {
                    Message msg = new Message();
                    String data = "Message #" + i + " From Instance " + instanceName;
                    msg.addMessageElement(MESSAGE_MODE,
                              new StringMessageElement(MESSAGE_MODE,
                                                       MODE_STARTUP,
                                                       null));
                    msg.addMessageElement(SenderMessage,
                                          new StringMessageElement(SenderMessage,
                                                                   data,
                                                                   null));                    
                    System.out.println("Sending :" + data);
                    pipe.sendMessage(msg);
                }
            } catch (Exception ie) {
                ie.printStackTrace();
            }
        }


        /**
         *  Main processing method for the ConnectionHandler object
         */
        public void run() {
            try {
                sendTestMessages(pipe);
                //pipe.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
    
    
}
