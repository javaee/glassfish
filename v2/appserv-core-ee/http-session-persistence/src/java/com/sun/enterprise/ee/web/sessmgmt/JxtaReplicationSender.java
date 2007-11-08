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
 * JxtaReplicationSender.java
 *
 * Created on December 20, 2005, 11:08 AM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import java.util.concurrent.CountDownLatch;
import org.apache.catalina.LifecycleException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.web.ServerConfigLookup;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.endpoint.StringMessageElement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.JxtaBiDiPipe;


/**
 *
 * @author Larry White
 */
public class JxtaReplicationSender implements Runnable {
    
    private static final String ID = ReplicationState.ID;
    
    private final static Level TRACE_LEVEL = Level.FINE;
    
    private final int NTHREADS = (getNumberOfSenderThreads() * 2);
    
    private final Executor exec
            = Executors.newFixedThreadPool(NTHREADS);
    
    private int getNumberOfSenderThreads() {
        //make equal to number of pipes
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getNumberOfReplicationPipesFromConfig();
    }
    
    public final static String LOGGER_MEM_REP 
        = ReplicationState.LOGGER_MEM_REP;    
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    //protected static final Logger _logger 
    //    = LogDomains.getLogger(LogDomains.WEB_LOGGER);
    private static final Logger _logger 
        = Logger.getLogger(LOGGER_MEM_REP); 
    
    /**
     * The singleton instance of JxtaReplicationSender
     */    
    private final static JxtaReplicationSender _soleInstance 
        = new JxtaReplicationSender();   
    
    /**
     * Has this component been started yet?
     */
    protected boolean started = false;
    
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
    protected String _threadName = "JxtaReplicationSender";

    /**
     * the TimerDispatchThread
     */    
    private TimerTask timerTask = new TimerDispatchThread();
    
    /**
     * Timer to run TimerDispatchThread
     */    
    private Timer timer = new Timer();

    /**
     * latency check counter
     */
    protected AtomicInteger _latencyCheckCounter = new AtomicInteger(-1);
    
    private volatile int _latencyCount = -1;
    
    /** gets the latency count limit */
    private int getLatencyCountLimit() {
        if(_latencyCount == -1) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            _latencyCount = lookup.getLatencyCountPropertyFromConfig();
            //System.out.println("JxtaReplicationSender: _latencyCount : " + _latencyCount);
        }
        return _latencyCount;
    }    
    
    /**
     * Return the thread name for this Store.
     */
    public String getThreadName() {
        return(_threadName);
    }
    
    /** Creates a new instance of JxtaReplicationSender */
    public JxtaReplicationSender() {
        _threadName = "JxtaReplicationSender";
        
        rwLock = new ReentrantReadWriteLock();
        rLock = rwLock.readLock();
        wLock = rwLock.writeLock();
        /*
        timerTask = new TimerDispatchThread();
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000L, 50L);
         */
    }    
    
    /** Return the singleton instance
     *  returns the sole instance of JxtaReplicationSender
     */
    public static JxtaReplicationSender createInstance() {
        return _soleInstance;
    }
    
    private boolean isWaitForAckConfigured() {
        if(_waitForAckConfigured == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            boolean waitForAckProp = lookup.getWaitForAckPropertyFromConfig();
            _waitForAckConfigured = new Boolean(waitForAckProp);
        }
        return _waitForAckConfigured.booleanValue();
    }    
    
    /**
     * is wait_for_ack_property = "true"
     */ 
    private Boolean _waitForAckConfigured = null;  
    
    public boolean isWaitForFastAckConfigured() {
        if(_waitForFastAckConfigured == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            boolean waitForFastAckProp = lookup.getWaitForFastAckPropertyFromConfig();
            _waitForFastAckConfigured = new Boolean(waitForFastAckProp);
        }
        //System.out.println("isWaitForFastAckConfigured = " + _waitForFastAckConfigured.booleanValue());
        return _waitForFastAckConfigured.booleanValue();
    }  
    
    /**
     * is wait_for_fast_ack_property = "true"
     */ 
    private Boolean _waitForFastAckConfigured = null;     

    /** This version sends the state by getting a pipe from the pool
     * then sending the message, then putting the pipe back in the pool
     * and then waiting for the result
     */    
    public ReplicationState sendReplicationState(ReplicationState state) {
        state.setSendStartTime(System.currentTimeMillis());
        LinkedBlockingQueue aQueue =
                ReplicationResponseRepository.putEmptyQueueEntry(state);
        //send message over pipe
        /*
        //FIXME this is just test code
        TestSender testSender = new TestSender(state);
        testSender.doIt();
         */
        //remove timing code for perf testing
        //long tempStart = System.currentTimeMillis();
        boolean success = sendOverPipe(state);
        /*
        long duration = System.currentTimeMillis() - tempStart;
        if(duration > 20) {
            System.out.println("sendOverPipe took " + duration + " msecs");
        }
         */
        if(!success) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("JxtaReplicationSender>>sendOverPipe failed - likely pool over-utilized");
            }             
            //System.out.println("JxtaReplicationSender>>sendOverPipe failed - likely pool over-utilized");
        }
        ReplicationState returnState = null;
        //block and wait for return message if send successful
        if(success) {            
            returnState = 
                    ReplicationResponseRepository.getEntry((String)state.getId());
        }
        return returnState;
    }
    
    //++++++++++++++++++++++START SPECIAL TEST CODE++++++++++++++++

    //++++++++++++++++++++++START BATCH CODE++++++++++++++++
    
    //this method defaults to no dupsAllowed
    public ReplicationState sendReplicationState(ReplicationState state, boolean wait) {
        sendReplicationState(state, wait, false);
        return null;  //FIXME
    }
    
    public ReplicationState sendReplicationState(ReplicationState state, boolean wait, boolean dupsAllowed) {
        replicateState(state, wait, dupsAllowed);
        
        return null;  //FIXME
    }
    
    //++++++++++++++++++++++END BATCH CODE++++++++++++++++
    
    /** This version sends the state by getting a pipe from the pool
     * then sending the message, then waiting for the result and
     * then putting the pipe back in the pool
     */    
    public ReplicationState sendReplicationStateKeepPrevious(ReplicationState state, boolean wait) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaReplicationSender>>sendReplicationState:wait:" + wait);
        }         
        //System.out.println("JxtaReplicationSender>>sendReplicationState:wait:" + wait);
        state.setSendStartTime(System.currentTimeMillis());
        ReplicationState returnState = null;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            //FIXME temp code remove
            System.out.println("health check bypassing replication now");
            return returnState;
        }
        int latencyCountLimit = this.getLatencyCountLimit();
        int latencyCount = incrementLatencyCheckCount();
        if( wait || (latencyCount == latencyCountLimit) ) {
            state.setAckRequired(true);
        }
        PipeWrapper pipeWrapper = null;
        try {
            /*
            if(wait) {
                LinkedBlockingQueue aQueue =
                        ReplicationResponseRepository.putEmptyQueueEntry(state);
            }
             */
            //for now add the queue entry - later can optimize that out too
            /* out temporarily for Suveen
            LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);
             */
            LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);            
            
            //long tempStart = System.currentTimeMillis();
            //get a pipe wrapper from the pool       
            try {
                pipeWrapper = this.getPipeWrapper();
            } catch (InterruptedException iex) {
                //FIXME log it
            }
            //FIXME this can be an over-taxed pool
            //decide whether to retry or just report error
            if(pipeWrapper == null) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("JxtaReplicationSender>>sendReplicationState failed - likely pool over-utilized");
                }                
                System.out.println("JxtaReplicationSender>>sendReplicationState failed - likely pool over-utilized");
            }
            //we have a failure - stop replicating
            if(pipeWrapper != null && pipeWrapper.isPipeClosed()) {
                ReplicationHealthChecker.setReplicationCommunicationOperational(false);
                ReplicationHealthChecker.reportError("sendReplicationState failed - pipeWrapper closed");
                return returnState;
            }            

            //send message over pipe
            boolean success = sendOverPipe(pipeWrapper, state);
            /* no sleep here
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {}
             */
            //System.out.println("sendReplication:success=" + success);          
            if(!success) {
                ReplicationHealthChecker.reportError("simple sendOverPipe failed, not a pool issue");
            }        
            //block and wait before putting pipe back in pool
            //int latencyCount = incrementLatencyCheckCount();
            //System.out.println("latencyCount = " + latencyCount + " latencyCountLimit = " + latencyCountLimit);
            if( wait || (latencyCount == latencyCountLimit) ) {
                //block and wait for return message
                returnState = 
                    ReplicationResponseRepository.getEntry((String)state.getId());
            } else {
                //if not waiting then just remove the entry
                //System.out.println("JxtaReplicationSender-not waiting so remove entry");
                ReplicationResponseRepository.removeEntry((String)state.getId());
            }
            /*
            long duration = System.currentTimeMillis() - tempStart;
            if(duration > 20) {
                System.out.println("sendReplicationState took " + duration + " msecs");
            }
             */            
        } finally {
            try {
                this.putPipeWrapper(pipeWrapper);
                //System.out.println("sendOverPipe:pipe back in pool ok");
            } catch (InterruptedException iex) {}
        }        
        return returnState;
    }
    
    private int incrementLatencyCheckCount() {
        return (Math.abs(_latencyCheckCounter.incrementAndGet()));
    }
    
    private boolean shouldWait(boolean wait, int latencyCount, int latencyCountLimit) {
        //System.out.println("shouldWait>>wait = " + wait + " latencyCount = " + latencyCount + " latencyCountLimit = " + latencyCountLimit);
        if(!wait && latencyCountLimit == 0) {
            return false;
        } else {
            return(wait || (latencyCount % latencyCountLimit) == 0);
        }
    }
    
    private synchronized int incrementLatencyCheckCountPrevious() { 
        int latencyCountLimit = this.getLatencyCountLimit();
        int latencyCount = _latencyCheckCounter.incrementAndGet();
        //System.out.println("after_increment_value = " + _latencyCheckCounter.get());
        if(latencyCount == latencyCountLimit) {
            _latencyCheckCounter.set(-1);
            return latencyCountLimit;
        } else {
            return _latencyCheckCounter.get();
        }
    }    
    
    /** This version sends the state by getting a pipe from the pool
     * then sending the message, then waiting for the result and
     * then putting the pipe back in the pool
     */    
    public ReplicationState sendReplicationStateSuveen(ReplicationState state, boolean wait) {
        //System.out.println("in new sendReplicationState:wait:" + wait);
        ReplicationState returnState = null;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            //FIXME temp code remove
            System.out.println("health check bypassing replication now");
            return returnState;
        }
        state.setSendStartTime(System.currentTimeMillis());
        PipeWrapper pipeWrapper = null;
        try {
            /*
            if(wait) {
                LinkedBlockingQueue aQueue =
                        ReplicationResponseRepository.putEmptyQueueEntry(state);
            }
             */
            //for now add the queue entry - later can optimize that out too
            /* out temporarily for Suveen
            LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);
             */            
            
            //long tempStart = System.currentTimeMillis();
            //get a pipe wrapper from the pool       
            try {
                pipeWrapper = this.getPipeWrapper();
            } catch (InterruptedException iex) {
                //FIXME log it
            }
            //FIXME this can be an over-taxed pool
            //decide whether to retry or just report error
            if(pipeWrapper == null) {
                System.out.println("sendReplicationState failed - likely pool over-utilized");
            }
            //we have a failure - stop replicating
            if(pipeWrapper != null && pipeWrapper.isPipeClosed()) {
                ReplicationHealthChecker.setReplicationCommunicationOperational(false);
                ReplicationHealthChecker.reportError("sendReplicationState failed - pipeWrapper closed");
            }            

            //send message over pipe
            boolean success = sendOverPipe(pipeWrapper, state);
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {}
            //System.out.println("sendReplication:success=" + success);          
            if(!success) {
                ReplicationHealthChecker.reportError("simple sendOverPipe failed, not a pool issue");
            }        
            //block and wait before putting pipe back in pool
            if(wait) {
                //block and wait for return message
                returnState = 
                    ReplicationResponseRepository.getEntry((String)state.getId());
            }
            /*
            long duration = System.currentTimeMillis() - tempStart;
            if(duration > 20) {
                System.out.println("sendReplicationState took " + duration + " msecs");
            }
             */            
        } finally {
            try {
                this.putPipeWrapper(pipeWrapper);
                //System.out.println("sendOverPipe:pipe back in pool ok");
            } catch (InterruptedException iex) {}
        }        
        return returnState;
    } 
    
    /** This version sends the state by getting a pipe from the pool
     * then sending the message, then waiting for the result and
     * then putting the pipe back in the pool
     * @param state the health check state message
     */    
    public ReplicationState sendReplicationStateHC(ReplicationState state) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaReplicationSender>>sendReplicationStateHC");
        }         
        //System.out.println("JxtaReplicationSender>>sendReplicationState");
        ReplicationState returnState = null;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            //FIXME temp code remove
            System.out.println("health check bypassing replication now");
            return returnState;
        }
        state.setSendStartTime(System.currentTimeMillis());
        PipeWrapper pipeWrapper = null;
        try {
            LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);            
            
            //long tempStart = System.currentTimeMillis();
            //get a pipe wrapper from the pool       
            try {
                pipeWrapper = this.getPipeWrapper();
            } catch (InterruptedException iex) {
                //FIXME log it
            }
            //FIXME this can be an over-taxed pool
            //decide whether to retry or just report error
            if(pipeWrapper == null) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("JxtaReplicationSender>>sendReplicationState failed - likely pool over-utilized");
                }                
                System.out.println("JxtaReplicationSender>>sendReplicationState failed - likely pool over-utilized");
            }
            //we have a failure - stop replicating
            if(pipeWrapper != null && pipeWrapper.isPipeClosed()) {
                ReplicationHealthChecker.setReplicationCommunicationOperational(false);
                ReplicationHealthChecker.reportError("sendReplicationState failed - pipeWrapper closed");
                return returnState;
            }            

            //send message over pipe
            boolean success = sendOverPipe(pipeWrapper, state);
            //System.out.println("sendReplication:success=" + success);          
            if(!success) {
                ReplicationHealthChecker.reportError("simple sendOverPipe failed, not a pool issue");
            }        
            //block and wait before putting pipe back in pool
                //block and wait for return message
            returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
            /*
            long duration = System.currentTimeMillis() - tempStart;
            if(duration > 20) {
                System.out.println("sendReplicationState took " + duration + " msecs");
            }
             */            
        } finally {
            try {
                this.putPipeWrapper(pipeWrapper);
                //System.out.println("sendOverPipe:pipe back in pool ok");
            } catch (InterruptedException iex) {}
        }        
        return returnState;
    }    
    
    /* return false if cannot send
     * else true
     * @param thePipe the JxtaBiDiPipe
     * @param state the state
     * @param isResponse
     */
    private boolean sendOverPipeLastGood(PipeWrapper pipeWrapper, ReplicationState state, boolean isResponse) {
        boolean result = false;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            return false;
        }
        if(pipeWrapper == null) {
            //no pipe wrapper - just return
            return false;
        }
        JxtaBiDiPipe thePipe = pipeWrapper.getPipe();
        Message theMsg = this.createMessage(state, isResponse);
        if(pipeWrapper.isPipeClosed()) {
            //no pipe - just return
            return false;
        }        
        try {
            thePipe.sendMessage(theMsg);
            result = true;
        } catch (NullPointerException ex1) {
            result = false;
        } catch (IOException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("IOException sending message: " + ex.getMessage());
            }            
            result = false;
        }
        return result;
    }     
    
    /* return false if cannot send
     * else true
     * @param thePipe the JxtaBiDiPipe
     * @param state the state
     * @param isResponse
     */
    private boolean sendOverPipe(PipeWrapper pipeWrapper, ReplicationState state, boolean isResponse) {
        boolean result = false;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            return false;
        }
        if(pipeWrapper == null) {
            //no pipe wrapper - just return
            return false;
        }
        JxtaBiDiPipe thePipe = pipeWrapper.getPipe();
        Message theMsg = this.createMessage(state, isResponse);
        if(pipeWrapper.isPipeClosed()) {
            //no pipe - just return
            return false;
        }
        result = sendMessageOverPipe(theMsg, thePipe, pipeWrapper);
        return result;
    }
    
    private boolean sendOverPipe(PipeWrapper pipeWrapper, ReplicationState state) { 
        return this.sendOverPipe(pipeWrapper, state, false);
    }

    /* return false if cannot send
     * else true
     * @param theMsg the message
     * @param thePipe the JxtaBiDiPipe
     * @param thePipeWrapper the PipeWrapper
     * this method assumes "direct connection" 
     * that sendMessage will block and
     * return or throw IOException
     */    
    private boolean sendMessageOverPipe(Message theMsg, JxtaBiDiPipe thePipe, PipeWrapper thePipeWrapper) {
        boolean result = true;
        try {
            result = thePipe.sendMessage(theMsg);
            if(result) {
                thePipeWrapper.messageSendSucceeded(theMsg);
            }
            //System.out.println("result = " + result);
        } catch (NullPointerException ex1) {
            result = false;
            System.out.println("JxtaReplicationSender:caught NPE");
            ex1.printStackTrace();
            thePipeWrapper.messageSendFailed(theMsg);
        } catch (IOException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("IOException sending message: " + ex.getMessage());
            }
            thePipeWrapper.messageSendFailed(theMsg);
            System.out.println("IOException sending message: " + ex.getMessage());
            result = false;
        }
        if(!result) {
            thePipeWrapper.messageSendFailed(theMsg);
            System.out.println("JxtaReplicationSender>>sendMessageOverPipe returning: " + result);
        }
        return result;
    } 

    /* return false if cannot send
     * else true
     * @param theMsg the message
     * @param thePipe the JxtaBiDiPipe
     * @param thePipeWrapper the PipeWrapper
     * this method assumes non "direct connection" 
     * that sendMessage will return true or false
     * or throw IOException
     */     
    private boolean sendMessageOverPipePrevious(Message theMsg, JxtaBiDiPipe thePipe, PipeWrapper thePipeWrapper) {
        boolean result = true;
        boolean continueTrying = true;
        long waitTime = 5L;
        while(continueTrying) {
            waitTime *= 2L;
            try {
                result = thePipe.sendMessage(theMsg);
                //System.out.println("result = " + result);
                //if fail sleep and prepare to try again
                if(!result && (waitTime < 1000L)) {
                    try {
                        Thread.currentThread().sleep(waitTime);
                    } catch (InterruptedException ex) {
                        //deliberate no-op
                    }
                } else {
                    continueTrying = false;
                    if(waitTime >= 1000L) {
                        result = false;
                        if(_logger.isLoggable(Level.FINE)) {
                            _logger.fine("JxtaReplicationSender:timed out trying to send message");
                        }                        
                        System.out.println("JxtaReplicationSender:timed out trying to send message");
                    } else {
                        result = true;
                    }
                }
            } catch (NullPointerException ex1) {
                result = false;
                continueTrying = false;
                System.out.println("JxtaReplicationSender:caught NPE");
                break;
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending message: " + ex.getMessage());
                }
                System.out.println("IOException sending message: " + ex.getMessage());
                result = false;
                continueTrying = false;
                break;
            }
        }
        if(!result) {
            System.out.println("JxtaReplicationSender>>sendMessageOverPipe returning: " + result);
        }
        return result;
    } 
    
    //++++++++++++++++++++++END SPECIAL TEST CODE++++++++++++++++
    
    //++++++++++++++++++++++START NON-BATCH CODE++++++++++++++++    
    /** This version sends the state by getting a pipe from the pool
     * then sending the message, then putting the pipe back in the pool
     * then waiting for the result if wait is true
     */      
    public ReplicationState sendReplicationStateNonBatch(ReplicationState state, boolean wait) {
        ReplicationState returnState = null;
        state.setSendStartTime(System.currentTimeMillis());

        if(wait) {
            LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);
        }
      
        //send message over pipe
        /*
        //FIXME this is just test code
        TestSender testSender = new TestSender(state);
        testSender.doIt();
         */        
        //long tempStart = System.currentTimeMillis();
        boolean success = sendOverPipe(state);
        /*
        long duration = System.currentTimeMillis() - tempStart;
        if(duration > 20) {
            System.out.println("sendOverPipe took " + duration + " msecs");
        }
         */
        if(!success) {
            System.out.println("sendOverPipe failed - likely pool over-utilized");
        }        
        
        if(wait) {
            //block and wait for return message
            returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
        }
        return returnState;
    } 
    
    public ReplicationState sendReplicationStateNonBatch(ReplicationState state, boolean wait, boolean dupsAllowed) { 
        return sendReplicationStateNonBatch(state, wait);
    } 
    
    //++++++++++++++++++++++END NON-BATCH CODE++++++++++++++++
    
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
        PipeWrapper thePipeWrapper = null;
        try {
            thePipeWrapper = this.getPipeWrapper();
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
                result = this.sendMessageOverPipe(theMsg, thePipe, thePipeWrapper);            
            } finally {
                try {
                    this.putPipeWrapper(thePipeWrapper);
                    //System.out.println("sendOverPipe:pipe back in pool ok");
                } catch (InterruptedException iex) {}
            }                
        } catch (InterruptedException iex2) {
            //FIXME log it
            result = false;
        }
        return result;
    }
    
    /* return false if cannot send
     * else true
     */
    private boolean sendOverPipeLastGood(ReplicationState state, boolean isResponse) {
        JxtaBiDiPipe thePipe = null;
        boolean result = false;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            return false;
        }
        PipeWrapper thePipeWrapper = null;
        try {
            thePipeWrapper = this.getPipeWrapper();
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
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending message: " + ex.getMessage());
                }                 
                result = false;
            } finally {
                try {
                    this.putPipeWrapper(thePipeWrapper);
                    //System.out.println("sendOverPipe:pipe back in pool ok");
                } catch (InterruptedException iex) {}
            }
        } catch (InterruptedException iex2) {
            //FIXME log it
            result = false;
        }
        return result;
    }
    
    private void sendOverPipePrevious(ReplicationState state, boolean isResponse) {
        JxtaBiDiPipe thePipe = null;
        PipeWrapper thePipeWrapper = null;
        try {
            thePipeWrapper = this.getPipeWrapper();
            if(thePipeWrapper == null) {
                return;
            }
            thePipe = thePipeWrapper.getPipe();
            //System.out.println("sendOverPipe:pipe from pool= " + thePipe);
            if(thePipe == null) {
                //no pipe to return to pool - just return
                return;
            }

            Message theMsg = this.createMessage(state, isResponse);
            try {
                thePipe.sendMessage(theMsg);
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending message: " + ex.getMessage());
                }                 
            } finally {
                try {
                    this.putPipeWrapper(thePipeWrapper);
                    //System.out.println("sendOverPipe:pipe back in pool ok");
                } catch (InterruptedException iex) {}
            }
        } catch (InterruptedException iex2) {
            //FIXME log it
        }
    } 
    
    boolean sendOverPipe(Message theMsg) {
        boolean result = false;
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return false;
        }
        
        JxtaBiDiPipe thePipe = null;
        PipeWrapper thePipeWrapper = null;
        try {
            thePipeWrapper = this.getPipeWrapper();
            if(thePipeWrapper == null) {
                return false;
            }
            thePipe = thePipeWrapper.getPipe();
            //System.out.println("sendOverPipe:pipe from pool= " + thePipe);
            if(thePipe == null) {
                //no pipe to return to pool - just return
                return false;
            }
            try {
                if(isWaitForFastAckConfigured()) {
                    thePipeWrapper.incrementQueuedMessageCount();
                    //System.out.println("about to call sendMessage with listener");
                    result = thePipe.sendMessage(theMsg, thePipeWrapper);
                    //result = thePipe.sendMessage(theMsg);
                    //doThreadedMessageCallback(theMsg, thePipeWrapper, result);
                    /*
                    if(result) {
                        thePipeWrapper.messageSendSucceeded(theMsg);
                    } else {
                        thePipeWrapper.messageSendFailed(theMsg);
                    }
                     */
                } else {
                    //System.out.println("about to call sendMessage without listener");
                    result = thePipe.sendMessage(theMsg);
                }
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending message: " + ex.getMessage());
                }
                /*
                if(isWaitForFastAckConfigured()) {
                    //thePipeWrapper.messageSendFailed(theMsg);
                    doThreadedMessageCallback(theMsg, thePipeWrapper, false);
                }
                 */
            } finally {
                try {
                    this.putPipeWrapper(thePipeWrapper);
                    //System.out.println("sendOverPipe:pipe back in pool ok");
                } catch (InterruptedException iex) {}
            }
        } catch (InterruptedException iex2) {
            //FIXME log it
        }
        return result;
    }     
    
    boolean sendOverPipeLastGood(Message theMsg) {
        boolean result = false;
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return false;
        }
        
        JxtaBiDiPipe thePipe = null;
        PipeWrapper thePipeWrapper = null;
        try {
            thePipeWrapper = this.getPipeWrapper();
            if(thePipeWrapper == null) {
                return false;
            }
            thePipe = thePipeWrapper.getPipe();
            //System.out.println("sendOverPipe:pipe from pool= " + thePipe);
            if(thePipe == null) {
                //no pipe to return to pool - just return
                return false;
            }
            try {
                //System.out.println("about to call sendMessage without listener");
                //result = thePipe.sendMessage(theMsg);
                if(isWaitForFastAckConfigured()) {
                    thePipeWrapper.incrementQueuedMessageCount();
                    //System.out.println("about to call sendMessage with listener");
                    result = thePipe.sendMessage(theMsg, thePipeWrapper);
                    //result = thePipe.sendMessage(theMsg);
                } else {
                    //System.out.println("about to call sendMessage without listener");
                    result = thePipe.sendMessage(theMsg);
                }
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending message: " + ex.getMessage());
                } 
            } finally {
                try {
                    this.putPipeWrapper(thePipeWrapper);
                    //System.out.println("sendOverPipe:pipe back in pool ok");
                } catch (InterruptedException iex) {}
            }
        } catch (InterruptedException iex2) {
            //FIXME log it
        }
        return result;
    }   
    
    private PipeWrapper getPipeWrapper() throws InterruptedException {
        JxtaSenderPipeManager senderPipeManager = 
                JxtaSenderPipeManager.createInstance();
        PipePool pool = senderPipeManager.getPipePool();
        if(pool == null) {
            return null;
        }
        PipeWrapper pipeWrapper = null;
        boolean goodPipeFound = false;
        while(!goodPipeFound) {
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
            if(!pipeWrapper.isPipeOverStressed()) {
                goodPipeFound = true;
                break;
            } else {
                pool.put(poolElement);
            }
        }
        return pipeWrapper;
    }

    private void putPipeWrapper(PipeWrapper thePipeWrapper) throws InterruptedException {
        //do not return pipe(Wrapper) to pool if pipe has been closed
        //or pipe wrapper is null
        //(caused by either shutdown or failure at pipe partner endpoint
        if(thePipeWrapper == null || thePipeWrapper.isPipeClosed()) {
            return;
        }
        JxtaSenderPipeManager senderPipeManager = 
                JxtaSenderPipeManager.createInstance();
        PipePool pool = senderPipeManager.getPipePool();
        pool.put((PipePoolElement)thePipeWrapper);
    }   
    
    private Message createMessage(ReplicationState state, boolean isResponse) {
        return ReplicationState.createMessage(state, isResponse);
    }

    public ReplicationState sendReplicationStateResponse(ReplicationState state) {
        //FIXME this is just test code
        /* no waiting queue here
        LinkedBlockingQueue aQueue =
                ReplicationResponseRepository.putEmptyQueueEntry(state);
         */
        //long tempStart = System.currentTimeMillis();
        //send message over pipe
        sendOverPipe(state, true);
        /*
        long duration = System.currentTimeMillis() - tempStart;
        if(duration > 20) {
            System.out.println("sendReplicationStateResponse took" + duration + " msecs");
        }
         */
        
        /*
        TestSender testSender = new TestSender(state);
        //testSender.doIt();
        testSender.run(); //just run in same thread
         */
        
        //block and wait for return message
        /* no blocking and waiting
        ReplicationState returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
        return returnState;
         */
        return state;
    } 
    
    public ReplicationState sendReplicationStateQueryResponse(ReplicationState state, String instanceName) {
        //FIXME has to go over created pipe to target instance, not sendOverPipe
        /* no waiting queue here
        LinkedBlockingQueue aQueue =
                ReplicationResponseRepository.putEmptyQueueEntry(state);
         */
        //send message over propagated pipe
        sendOverPropagatedPipe(state, instanceName, true);
        //sendOverPipe(state, true);
        
        /*
        TestSender testSender = new TestSender(state);
        //testSender.doIt();
        testSender.run(); //just run in same thread
         */
        
        //block and wait for return message
        /* no blocking and waiting
        ReplicationState returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
        return returnState;
         */
        return state;
    }

    private void sendOverPropagatedPipe(ReplicationState state, String instanceName, boolean isResponse) {
        /* FIXME testing with this check out
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return;
        }
         */
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaReplicationSender>>sendOverPropagatedPipe:toInstance=" + instanceName);
        }        
        //System.out.println("JxtaReplicationSender>>sendOverPropagatedPipe:toInstance=" + instanceName);       
        OutputPipe outputPipe = createPropagatedOutputPipe(instanceName);
        Message theMsg = this.createBroadcastMessage(state, isResponse, instanceName);
        //Message theMsg = this.createMessage(state, isResponse);
        //temporarily try multicast -ok this worked
        //this.sendOverPropagatedPipe(theMsg);

        //we will retry this once if necessary
        boolean needsRetry = true;
        int retryCount = 0;
        boolean sendResult = false;
        while(needsRetry) {
            needsRetry = false;
            try {
                sendResult = outputPipe.send(theMsg);
                //System.out.println("JxtaReplicationSender:unicast send result = " + sendResult);
                if(!sendResult) {
                    needsRetry = true;
                }
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending unicast message: " + ex.getMessage());
                }
                System.out.println("IOException sending unicast message: " + ex.getMessage());
                retryCount++;
                if(retryCount < 2) {
                    System.out.println("propagated unicast pipe send retrying");
                    needsRetry = true;
                    //sleep 750 msec and then retry
                    try {
                        Thread.currentThread().sleep(750);
                    } catch (InterruptedException iex) {};
                }
            }
        }

    }   
    
    private OutputPipe createPropagatedOutputPipe(String instanceName) {
        JxtaStarter jxtaStarter = JxtaStarter.createInstance();
        PeerGroup netPeerGroup = jxtaStarter.getNetPeerGroup();
        PipeService pipeService = netPeerGroup.getPipeService();
        PipeAdvertisement pipeAdv = JxtaUtil.getPropagatedPipeAdvertisement();
        //System.out.println("prop pipe adv: " + pipeAdv);
        OutputPipe op = null;
        try {
            op = pipeService.createOutputPipe(pipeAdv, Collections.singleton(JxtaStarter.getPeerID(instanceName)), 10000); 
            //op = pipeService.createOutputPipe(pipeAdv, 100);
        } catch (IOException ex) {
            if(_logger.isLoggable(Level.FINE)) {
                _logger.fine("IOException creating propagated pipe: " + ex.getMessage());
            } 
        }
        return op;
    }    
    
    public ReplicationState sendReplicationStateResponsePrevious(ReplicationState state) {
        //FIXME this is just test code
        /* no waiting queue here
        LinkedBlockingQueue aQueue =
                ReplicationResponseRepository.putEmptyQueueEntry(state);
         */
        //send message over pipe
        
        TestSender testSender = new TestSender(state);
        //testSender.doIt();
        testSender.run(); //just run in same thread
        
        //block and wait for return message
        /* no blocking and waiting
        ReplicationState returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
        return returnState;
         */
        return state;
    }
    
    //+++++++++++++++++++++START Propagated Pipe related+++++++++++++++++
    
    public ReplicationState sendReplicationStateQueryLastGood(ReplicationState state) {
        LinkedBlockingQueue aQueue =
                ReplicationResponseRepository.putEmptyQueueEntry(state);
        sendBroadcastQuery(state);
        //block and wait for return message
        ReplicationState returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId());
        return returnState;
    }
    
    private int getNumberExpectedRespondants() {
        ReplicationHealthChecker healthChecker = ReplicationHealthChecker.getInstance();
        List conservativeList = healthChecker.getConservativeMemberList(null);
        if(conservativeList == null) {
            return 0;
        } else {
            return conservativeList.size() - 1;
        }
    }    
    
    public ReplicationState sendReplicationStateQuery(ReplicationState state) {
        int numberExpectedResults = getNumberExpectedRespondants();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("expected respondants = " + numberExpectedResults);
        }         
        //System.out.println("expected respondants = " + numberExpectedResults);
        long startTime = System.currentTimeMillis();
        FederatedRequestProcessor federatedRequestProcessor = 
            new FederatedRequestProcessor(state, numberExpectedResults, 4000L, state.getVersion());
        ReplicationResponseRepository.putWrappedEmptyQueueEntry(state, federatedRequestProcessor);
        ReplicationState result = federatedRequestProcessor.doFederatedQuery();
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("federated query took " + (System.currentTimeMillis() - startTime) + " millis");
        }         
        //System.out.println("federated query took " + (System.currentTimeMillis() - startTime) + " millis");
        return result;
    }
    
    void sendBroadcastQuery(ReplicationState state) {
        Message broadcastMsg = createBroadcastMessage(state, false);
        sendOverPropagatedPipe(broadcastMsg);
    }
    
    private OutputPipe getPropagatedOutputPipe() {         
        return JxtaSenderPipeManager.createInstance().getPropagatedOutputPipe();
    }
    
    void sendOverPropagatedPipe(Message theMsg) {
        /* FIXME testing with this check out
        if( !ReplicationHealthChecker.isOkToProceed() ) {
            return;
        }
         */        
        OutputPipe outputPipe = getPropagatedOutputPipe();
        if(outputPipe != null) {
            try {
                outputPipe.send(theMsg);
            } catch (IOException ex) {
                if(_logger.isLoggable(Level.FINE)) {
                    _logger.fine("IOException sending message over propagated pipe: " + ex.getMessage());
                }         
            }
        }
    } 
    
    private Message createBroadcastMessage(ReplicationState state, boolean isResponse) {
        return ReplicationState.createBroadcastMessage(state, isResponse);
    } 
    
    private Message createBroadcastMessage(ReplicationState state, boolean isResponse, String instanceName) {
        return ReplicationState.createBroadcastMessage(state, isResponse, instanceName);
    }     
    
    //+++++++++++++++++++++++END Propagated Pipe related+++++++++++++++++
        
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
                _logger.finest("Starting - HADB health checking not enabled");
            }
            return;
        }
         */
        if(started) {
            return;
        }
        /*
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Replication health checking enabled");
        }
         */
       
        //this.registerAdminEvents();
        
        // FIXME this is moved to another class
        //Start the background health-check thread
        //threadStart();
        /*
        timerTask = new TimerDispatchThread();
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 1000L, 50L);
         */      
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
        //this.unregisterAdminEvents();
        // Stop the thread       
        //threadStop();
        /*
        timer.cancel();
         */
        started = false;
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
    
    //Start Mahesh code
    //
    private static int NUMBER_OF_REQUESTS_BEFORE_FLUSH = 100;

    private static final int NUMBER_OF_SESSIONS_PER_MESSAGE = 20;
    
    private static final int BULK_MESSAGE_LIMIT = 32*1024;
    
    private static final int INITIAL_CAPACITY = 2048;
    
    private static final float LOAD_FACTOR = 0.75F;
    
    private static final int CONCURRENCY_LEVEL = 100;
    
    private DispatchThread dispatchThread = new DispatchThread();
    
    private DispatchThreadNoDupsAllowed dispatchThreadNoDupsAllowed 
        = new DispatchThreadNoDupsAllowed();
    
    private Object dispatchThreadMonitorObject = new Object();
    
    private Object dispatchThreadNoDupsAllowedMonitorObject = new Object();
    
    private ReentrantReadWriteLock rwLock;
    
    private Lock rLock;
    
    private Lock wLock;   
    
    //volatile Map<ReplicationState, ReplicationState> currentMap = new ConcurrentHashMap<ReplicationState, ReplicationState>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    AtomicReference currentMap = new AtomicReference(new ConcurrentHashMap<ReplicationState, ReplicationState>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL));
    
    //volatile Map<ReplicationState, ReplicationState> currentMapNoDupsAllowed = new ConcurrentHashMap<ReplicationState, ReplicationState>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL);
    AtomicReference currentMapNoDupsAllowed = new AtomicReference(new ConcurrentHashMap<ReplicationState, ReplicationState>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL));
    
    private AtomicBoolean timeToChange = new AtomicBoolean(false);
    
    private AtomicBoolean timeToChangeNoDupsAllowed = new AtomicBoolean(false);
    
    private AtomicLong lastChangeTime = new AtomicLong(System.currentTimeMillis() - 1000L);
    
    private AtomicLong lastChangeTimeNoDupsAllowed = new AtomicLong(System.currentTimeMillis() - 1000L);
    
    private static AtomicInteger requestCounter = new AtomicInteger(0);
    
    private static AtomicInteger requestCounterNoDupsAllowed = new AtomicInteger(0);
    
    private static AtomicInteger _messageIDCounter = new AtomicInteger(0);
    
    private static long DEFAULT_TIME_THRESHOLD = 100L;

    /**
     * Add state to map - map is key = id // value = List of ReplicationState  
     *
     * @param map the map
     * @param state the state to be added
     * @param dupsAllowed - if dupsAllowed then List may contain more than
     *      one element, else only one (the latest added)
     * @return true if state with same id requiring ack already present
     */    
    private boolean addToMap(Map map, ReplicationState state, boolean dupsAllowed) {
        //believe no synchronization needed
        boolean result = false;
        String id = state.getId().toString();
        List stateList = (List)map.get(id);
        if(stateList == null) {
            stateList = new ArrayList();
            map.put(id, stateList);
        }
        if(!dupsAllowed && !stateList.isEmpty()) {
            //before replacing zeroth element, if it is ackrequired
            //then replacement must be too
            ReplicationState previousState = (ReplicationState)stateList.get(0);
            if(previousState.isAckRequired()) {
                state.setAckRequired(true);
            }
            stateList.set(0,state);
        } else {
            //check if there already exists an element on list with ackRequired
            //if so then set ackRequired for new one to false before adding
            //because we only want one state on each list requiring an ack
            if(doesListContainAckRequiredState(stateList)) {
                state.setAckRequired(false);
            }
            stateList.add(state);
        }
        return result;
    }
    
    /**
     * Add state to map - map is key = id // value = List of ReplicationState  
     *
     * @param map the map
     * @param state the state to be added
     * @param wait the incoming wait expectation of the state
     * @return true if calling thread will be waiting for ack
     */    
    private boolean addToMapDupsAllowed(AtomicReference map, ReplicationState state, boolean wait) {
        //believe no synchronization needed
        //start by letting wait guide state semantics
        state.setAckRequired(wait);
        String id = state.getId().toString();
        Map tempMap = (Map)map.get();
        //initial value
        boolean currentThreadShouldWait = false;
        boolean shouldContinue = true; 
        do{          
            List stateList = (List)tempMap.get(id);
            if(stateList == null) {
                stateList = new ArrayList();
                tempMap.put(id, stateList);
            }
            //is there another thread already waiting for ack
            boolean listAlreadyContainsAck = doesListContainAckRequiredState(stateList);
            if(state.isRemoveMethodState()) {
                //state is remove state case
                //remove clears out any previous states
                stateList.clear();
                //if list already contained an ack then this state should have 
                //ack required
                state.setAckRequired(listAlreadyContainsAck || wait); 
            } else {
                //state is not remove state case
                //check if there already exists an element on list with ackRequired
                //if so then set ackRequired for new one to false before adding
                //because we only want one state on each list requiring an ack
                if(listAlreadyContainsAck) {
                    state.setAckRequired(false);
                } else {
                    state.setAckRequired(wait);
                }
            }
            //we set up to wait only if wait && !listAlreadyContainsAck
            currentThreadShouldWait
                = wait && !listAlreadyContainsAck;
            if(currentThreadShouldWait) {
                LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);                
            }
            //now unconditionally add the state
            //flush method will now check if sent
            stateList.add(state);
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "addToMapDupsAllowed added state id: "
                    + state.getId() + "[ver: " + state.getVersion() + "] to mapHashCode: " + 
                    System.identityHashCode(tempMap));
            }                 
 
            
            if(tempMap == currentMap.get()) {
                shouldContinue = false;
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "addToMapDupsAllowed exiting... ");
                }                 
            } else {
                tempMap = (Map)currentMap.get();
            }            
        } while (shouldContinue);
        return currentThreadShouldWait;
    }
    
    private boolean addToMapNoDups(AtomicReference map, ReplicationState state, boolean wait) {
        Map tempMap = (Map)map.get();
        boolean currentThreadShouldWait = wait;
        boolean shouldContinue = true;
        do{
            //start by assuming wait parameter will guide this
            currentThreadShouldWait = wait;
            //start by assuming there is no other thread waiting
            boolean threadAlreadyWaiting = false;
            //check if existing state has thread waiting
            ReplicationState currentState = (ReplicationState)tempMap.get(state);                
            if(currentState != null && currentState.isAckRequired()) {
                threadAlreadyWaiting = true;
                state.setAckRequired(true);
            } else { //no thread waiting yet 
                state.setAckRequired(wait);
                //only in this case is waiting room set up
                if(wait) {
                    LinkedBlockingQueue aQueue =
                        ReplicationResponseRepository.putEmptyQueueEntry(state);
                    currentThreadShouldWait = true;
                }
            }         
            
            //now unconditionally add the state
            //flush method will now check if sent
            tempMap.put(state, state);
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "addToMapNoDups added state id: "
                    + state.getId() + "[ver: " + state.getVersion() + "] to mapHashCode: " + 
                    System.identityHashCode(tempMap));
            }                
            if(tempMap == currentMapNoDupsAllowed.get()) {
                shouldContinue = false;
                if (_logger.isLoggable(TRACE_LEVEL)) {
                    _logger.log(TRACE_LEVEL, "addToMapNoDups exiting... ");
                }                 
            } else {
                tempMap = (Map)currentMapNoDupsAllowed.get();
            }
        } while (shouldContinue);
        return currentThreadShouldWait;
    }
    
    private boolean doesListContainAckRequiredState(List stateList) {
        boolean result = false;
        for(int i=0; i<stateList.size(); i++) {
            ReplicationState nextState = (ReplicationState)stateList.get(i);
            if(nextState.isAckRequired()) {
                result = true;
            }
            if(result) {
                break;
            }
        }
        return result;
    }
    
    private boolean doesMapContainAckRequiredState(Map currentMap, ReplicationState state) {
        String id = state.getId().toString();
        List listForId = (List)currentMap.get(id);
        if(listForId == null) {
            return false;
        }
        return doesListContainAckRequiredState(listForId);
    }
    
    private void replicateState(ReplicationState state, boolean wait, boolean dupsAllowed) {
        //System.out.println("JxtaReplicationSender>>replicateState:wait = " + wait + " dupsAllowed = " + dupsAllowed);
        if(dupsAllowed) {
            replicateStateDupsAllowed(state, wait);
        } else {
            replicateStateNoDupsAllowed(state, wait);
        }
    }
    
    private void replicateStateDupsAllowed(ReplicationState state, boolean wait) {
        //rLock.lock();
        
        //System.out.println("JxtaReplicationSender>>replicateStateDupsAllowed:wait = " + wait);
        boolean messageInBulkAlreadyWaiting = false;        
        boolean needsToWait = false;
        int latencyCountLimit = this.getLatencyCountLimit();
        int latencyCount = incrementLatencyCheckCount();
        needsToWait = shouldWait(wait, latencyCount, latencyCountLimit);
        if(needsToWait) {          
            state.setAckRequired(needsToWait);
        }        
        
        try {
            boolean thisThreadShouldWait 
                = addToMapDupsAllowed(currentMap, state, needsToWait);
            //int requestCount = requestCounter.incrementAndGet();
            
            //long lastTimeChanged = lastChangeTime.get();
            if ( ((Map)currentMap.get()).size() >= NUMBER_OF_SESSIONS_PER_MESSAGE) { 
                boolean wakeupDispatcher = timeToChange.compareAndSet(false, true); //expect false  set  to true            
                if (wakeupDispatcher) {
                    dispatchThread.wakeup();
                }
            }

            if(thisThreadShouldWait) {
                //wait case
                //this handles the dups case
                //if there is already a state with same id requiring ack
                //then we do not wait
                //block and wait for return message
                ReplicationState returnState = 
                    ReplicationResponseRepository.getEntry((String)state.getId());
                if (returnState == null) {
                    //System.out.println("JxtaReplicationSender>>replicateStateDupsAllowed timed out returning null");
                } else {
                    //System.out.println("ack received: id = " + state.getId());
                    //System.out.println("JxtaReplicationSender>>replicateStateDupAllowed succeeded");
                }               
            }                                 
        } finally {
            //rLock.unlock();
        }        
    }
    
    private void replicateStateNoDupsAllowed(ReplicationState state, boolean wait) {
        //rLock.lock();
        //System.out.println("JxtaReplicationSender>>replicateStateNoDupsAllowed:wait = " + wait);
        boolean needsToWait = false;
        int latencyCountLimit = this.getLatencyCountLimit();
        int latencyCount = incrementLatencyCheckCount();
        needsToWait = shouldWait(wait, latencyCount, latencyCountLimit);

        if(needsToWait) {
            state.setAckRequired(needsToWait);            
        }
        try {
            //this method also controls whether this thread waits or not
            boolean thisThreadShouldWait 
                = addToMapNoDups(currentMapNoDupsAllowed, state, needsToWait);
            //int requestCount = requestCounterNoDupsAllowed.incrementAndGet();
            //long lastChangeTime = lastChangeTimeNoDupsAllowed.get();
            if ( ((Map)currentMapNoDupsAllowed.get()).size() >= NUMBER_OF_SESSIONS_PER_MESSAGE) {
                boolean wakeupDispatcher = timeToChangeNoDupsAllowed.compareAndSet(false, true); //expect false  set  to true            
                if (wakeupDispatcher) {
                    dispatchThreadNoDupsAllowed.wakeup();
                }
            }
            
            if(thisThreadShouldWait) {
                //wait or latencyCountLimit hit case
                //block and wait for return message
                ReplicationState returnState = 
                    ReplicationResponseRepository.getEntry((String)state.getId());
                if (returnState == null) {
                    //System.out.println("JxtaReplicationSender>>replicateStateNoDupsAllowed timed out returning null");
                } else {
                    //System.out.println("ack received: id = " + state.getId());
                    //System.out.println("JxtaReplicationSender>>replicateStateNoDupsAllowed succeeded");
                }               
            } 
           
        } finally {
            //rLock.unlock();
        }        
        
    }
    
    //this method does not have to be synchronized
    private boolean timeThresholdExceeded(long thresholdDuration, long previousTime) {
        if((System.currentTimeMillis() - previousTime) > thresholdDuration) {
            return true;
        } else {
            return false;
        }
        
    }
    
    //Called by the dispatcher
    void flushAllMessagesFromCurrentMap(boolean waitForAck) {
        //System.out.println("flushAllMessagesFromCurrentMap:begin");
        Map oldMap = null;
        try {
            oldMap = (Map)currentMap.get();
            currentMap.set(new ConcurrentHashMap<ReplicationState, ReplicationState>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL));
            lastChangeTime.set(System.currentTimeMillis());
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "flushAllMessagesFromCurrentMap flipped maps oldMapKey: "
                     + System.identityHashCode(oldMap) + "currentMap.get()Key):"
                     + System.identityHashCode(currentMap.get()));
            }             
            timeToChange.set(false);                        
        } finally {
        }
        
        //_logger.log(Level.INFO, ">>JxtaReplicationSender::flushAllMessages: " + oldMap.size());
        
        //Send sessions in currentMap into a message
        List<ReplicationState> list = new ArrayList<ReplicationState>(oldMap.size()+1);
        Iterator<List> iter = oldMap.values().iterator();
        int totalMessageSize = 0;        
        while (iter.hasNext()) {
            boolean needBulkAck = false;
            List nextIdList = (List)iter.next();
            //displayList(nextIdList);
            for(int i=0; i<nextIdList.size(); i++) {
                ReplicationState state = (ReplicationState)nextIdList.get(i);
                int stateSize = 0;
                if(state != null && !state.isSent()) {
                    //if any state requires ack then this bulk msg requires ack
                    if(state.isAckRequired()) {
                        needBulkAck = true;
                    }                
                    if(state.getState() != null) {
                        stateSize = state.getState().length;
                    }
                }
                if (totalMessageSize + stateSize > BULK_MESSAGE_LIMIT) {
                    doThreadedCreateMessageAndSend(list, needBulkAck);
                    list = new ArrayList<ReplicationState>(oldMap.size()+1);
                    //createMessageAndSend(list, needBulkAck, null);
                    //list.clear();
                    totalMessageSize = 0;
                }
                //mark state as being sent
                if(state != null && !state.isSent()) {
                    state.setSent(true);                
                    list.add(state); 
                    totalMessageSize += stateSize;
                }
            }
        }  
        
        if (list.size() > 0) {
            boolean needBulkAck = doesListContainAckRequiredState(list);
            doThreadedCreateMessageAndSend(list, needBulkAck);
            //createMessageAndSend(list, needBulkAck, null);
            //list.clear();
        }
        oldMap.clear();
    }
    
    //Called by the dispatcher
    void flushAllMessagesFromCurrentMapNoDupsAllowed(boolean waitForAck) {
        //System.out.println("flushAllMessagesFromCurrentMapNoDupsAllowed:begin");        
        Map oldMap = null;
        try {
            //wLock.lock();
            oldMap = (Map)currentMapNoDupsAllowed.get();
            currentMapNoDupsAllowed.set(new ConcurrentHashMap<ReplicationState, ReplicationState>(INITIAL_CAPACITY, LOAD_FACTOR, CONCURRENCY_LEVEL));
            //wLock.unlock();
            lastChangeTimeNoDupsAllowed.set(System.currentTimeMillis());
            if (_logger.isLoggable(TRACE_LEVEL)) {
                _logger.log(TRACE_LEVEL, "flushAllMessagesFromCurrentMapNoDupsAllowed flipped maps oldMapKey: "
                     + System.identityHashCode(oldMap) + "currentMapNoDupsAllowed.get()Key):"
                     + System.identityHashCode(currentMapNoDupsAllowed.get()));
            }             
            timeToChangeNoDupsAllowed.set(false);
        } finally {
        }
        
        //_logger.log(Level.INFO, ">>JxtaReplicationSender::flushAllMessagesFromCurrentMapNoDupsAllowed: " + oldMap.size());
        
        //Send sessions in currentMap into a message
        //System.out.println("flush full list size = " + oldMap.keySet().size());
        List<ReplicationState> list = new ArrayList<ReplicationState>(oldMap.size()+1);
        Iterator<ReplicationState> iter = oldMap.values().iterator();
        int totalMessageSize = 0;        
        //non order preserving
        while (iter.hasNext()) {            
            boolean needBulkAck = false;
            ReplicationState state = iter.next();
            int stateSize = 0;
            if(state != null && !state.isSent()) {
                //if any state requires ack then this bulk msg requires ack
                if(state.isAckRequired()) {
                    needBulkAck = true;
                }
                if(state.getState() != null) {
                    stateSize = state.getState().length;
                }                 
            }                       
            if (totalMessageSize + stateSize > BULK_MESSAGE_LIMIT) {
                doThreadedCreateMessageAndSend(list, needBulkAck); 
                list = new ArrayList<ReplicationState>(oldMap.size()+1); 
                //createMessageAndSend(list, needBulkAck, null);
                //list.clear();
                totalMessageSize = 0;
            }
            if(state != null && !state.isSent()) {
                //mark state as being sent
                state.setSent(true);
                list.add(state);
            }
            totalMessageSize += stateSize;
        }       
        
        if (list.size() > 0) {
            boolean needBulkAck = doesListContainAckRequiredState(list);
            doThreadedCreateMessageAndSend(list, needBulkAck);
            //createMessageAndSend(list, needBulkAck, null);
            //list.clear();
        }
        oldMap.clear();
    } 
    
    private void doThreadedCreateMessageAndSend(List list, boolean needBulkAck) {
        //System.out.println("doThreadedCreateMessageAndSend");
        BulkMessageSender bulkMessageSender 
            = new BulkMessageSender(list, needBulkAck);
        exec.execute(bulkMessageSender);
    }
    
    private void doThreadedMessageCallback(Message sentMessage, PipeWrapper pipeWrapper, boolean success) {
        //System.out.println("doThreadedMessageCallback");
        BulkMessageAcker bulkMessageAcker 
            = new BulkMessageAcker(sentMessage, pipeWrapper, success);
        exec.execute(bulkMessageAcker);
    }  
    
    private static void displayList(List aList, String listName) {
        _logger.log(Level.INFO, "displaying " + listName + " list");
        for(int i=0; i<aList.size(); i++) {
            _logger.log(Level.INFO, "displayStringList:elem[" + i + "] = " + aList.get(i));
        }
    }    
    
    private void createMessageAndSend(List<ReplicationState> list, boolean waitForAck, Object signalObject) {
        List<String> ackIdsList = ReplicationState.extractAckIdsList(list);
        //displayList(ackIdsList, "ackIdsList");
        byte[] data = null;
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(list);
            oos.flush();
        } catch (IOException ioEx) {
            //FIXME
        } finally {
            if (oos != null) {
                try {
                    oos.flush(); oos.close();
                } catch (Exception ex) {
                    //Ignore
                }
            }
            
            if (bos != null) {
                try {
                    bos.flush();
                    data = bos.toByteArray();
                    bos.close();
                } catch (Exception ex) {
                    //FIXME
                }
            }
        }
        //_logger.log(Level.INFO, "<<JxtaReplicationSender::flushAllMessages: About to send " + list.size() + " messages: " + data.length + " bytes...");
        if (_logger.isLoggable(TRACE_LEVEL)) {            
            List<String> allIdsList = ReplicationState.extractAllIdsList(list);
            displayList(allIdsList, "ids about to be sent");            
        }        
        int bulkMsgId = Math.abs(_messageIDCounter.incrementAndGet());
        sendBulkMessage(bulkMsgId, ackIdsList, data, waitForAck);        
        //_logger.log(Level.INFO, "<<JxtaReplicationSender::flushAllMessages: DONE!!");
    }
    
    //new from Larry a send method that does not spin and retry
    //on the same pipe in the event of return of false but instead
    //tries another pipe
    private boolean sendState(ReplicationState state) {
        return sendState(state, false);
    }    
    
    //new from Larry a send method that does not spin and retry
    //on the same pipe in the event of return of false but instead
    //tries another pipe
    private boolean sendState(ReplicationState state, boolean isResponse) {
        boolean result = true;
        state.setSendStartTime(System.currentTimeMillis());
        Message theMsg = this.createMessage(state, isResponse);
        boolean continueTrying = true;
        long waitTime = 5L;
        int counter = 0;
        
        while(continueTrying) {
            counter++;
            waitTime *= 2L;
            try {
                //this gets a new pipe, tries the send once, puts the pipe
                //back in the pool and returns the result
                result = sendOverPipe(theMsg);
                if(!result && (waitTime < 1000L)) {
                } else {
                    continueTrying = false;
                    if(waitTime >= 1000L) {
                        result = false;
                        if(_logger.isLoggable(Level.FINE)) {
                            _logger.fine("JxtaReplicationSender>>sendState:timed out trying to send message after trying to send on " + counter + " pipes");
                        }                        
                        System.out.println("JxtaReplicationSender>>sendState:timed out trying to send message after trying to send on " + counter + " pipes");
                    } else {
                        result = true;
                    }
                }
            } catch (NullPointerException ex1) {
                result = false;
                continueTrying = false;
                System.out.println("JxtaReplicationSender>>sendState:caught NPE");
                break;
            }
        }
        if(!result) {
            System.out.println("JxtaReplicationSender>>sendState returning: " + result);
        }
        return result;                  
    }
    
    private ReplicationState sendBulkMessage(long msgID, List<String> ackIdsList, byte[] data, boolean wait) {
        //System.out.println("sending bulk message: bulkMsgId = " + msgID);
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("JxtaReplicationSender>>sendBulkMessage:wait:" + wait);
        }         
        //System.out.println("JxtaReplicationSender>>sendBulkMessage:wait:" + wait);
        ReplicationState returnState = null;
        if(!ReplicationHealthChecker.isOkToProceed()) {
            System.out.println("health check bypassing replication now");
            return returnState;
        }
        
        boolean ackRequired = wait;      
        //long startTime = System.currentTimeMillis();
        ReplicationState state = ReplicationState.createBulkReplicationState(msgID, ackIdsList, data, ackRequired);
        if( (wait && !isWaitForFastAckConfigured()) 
            || ReplicationHealthChecker.isFlushThreadWaiting()) {
            LinkedBlockingQueue aQueue =
                    ReplicationResponseRepository.putEmptyQueueEntry(state);
        }
        //this will try to send on different pipes until timeout
        boolean success = sendState(state);
        //System.out.println("sendState:success=" + success);          
        if(!success) {
            ReplicationHealthChecker.reportError("JxtaReplicationSender>>sendState failed");
        }
        
        if(wait && !isWaitForFastAckConfigured()) {
            //block and wait for return message
            returnState = 
                ReplicationResponseRepository.getEntry((String)state.getId(), 5000L);
            /*
            long waitTime = System.currentTimeMillis() - startTime;
            if(waitTime > 15000L) {
                System.out.println("wait time = " + waitTime);
            }
             */
        }
        return returnState;
    }    
    
    private class DispatchThread implements Runnable {
        
        private volatile boolean done = false;
       
        private Thread thread;
        
        private LinkedBlockingQueue<Object> queue;
        
        public DispatchThread() {
            this.queue = new LinkedBlockingQueue<Object>();
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            thread.start();            
        }
        
        public void wakeup() {
            synchronized(dispatchThreadMonitorObject) {
                dispatchThreadMonitorObject.notify();
            }
            //queue.add(new Object());
        }
        
        public void run() {
            ReplicationHealthChecker.incrementDispatchThreadCount();
            boolean hasSignaled = false;
            while (! done) {
                try {
                    synchronized(dispatchThreadMonitorObject) {
                        dispatchThreadMonitorObject.wait(50L);
                    }
                    //Object ignorableToken = queue.take();                    
                    //System.out.println("dups emerging from wait...");
                    long lastChangeTimeDups = lastChangeTime.get();                    
                    if(!ReplicationHealthChecker.isFlushThreadWaiting()) {
                        //normal processing
                        if ( ((Map)currentMap.get()).size() > 0
                                && timeThresholdExceeded(DEFAULT_TIME_THRESHOLD, lastChangeTimeDups) ) {
                            flushAllMessagesFromCurrentMap(isWaitForAckConfigured());
                        }                        
                    } else { //only if flushThreadWaiting
                        //flush once more if you have data
                        try {
                            if(((Map)currentMap.get()).size() > 0) {
                                flushAllMessagesFromCurrentMap(isWaitForAckConfigured());
                            }
                        } finally {
                            //signal you are finished unloading if you have not previously done so
                            if(!hasSignaled) {
                                CountDownLatch doneSignal = ReplicationHealthChecker.getDoneSignal();
                                doneSignal.countDown();
                                hasSignaled = true;
                            }
                        }
                    }                     
                } catch (InterruptedException inEx) {
                    this.done = true;
                } catch (Throwable t) {
                    _logger.log(Level.INFO, "dups allowed thread exception:", t);
                }  finally {
                    //if done is set signal you are finished if you have not previously done so
                    if(done && !hasSignaled) {
                        CountDownLatch doneSignal = ReplicationHealthChecker.getDoneSignal();
                        doneSignal.countDown();
                        hasSignaled = true;
                    }
                }
            }
        }

    } 
    
    private class DispatchThreadNoDupsAllowed implements Runnable {
        
        private volatile boolean done = false;
       
        private Thread thread;
        
        private LinkedBlockingQueue<Object> queue;
        
        public DispatchThreadNoDupsAllowed() {
            this.queue = new LinkedBlockingQueue<Object>();
            this.thread = new Thread(this);
            this.thread.setDaemon(true);
            thread.start();
        }
        
        public void wakeup() {
            synchronized(dispatchThreadNoDupsAllowedMonitorObject) {
                dispatchThreadNoDupsAllowedMonitorObject.notify();
            }            
            //queue.add(new Object());
        }
        
        public void run() {
            ReplicationHealthChecker.incrementDispatchThreadCount();
            boolean hasSignaled = false;
            while (! done) {
                try {
                    synchronized(dispatchThreadNoDupsAllowedMonitorObject) {
                        dispatchThreadNoDupsAllowedMonitorObject.wait(50L);
                    }
                    //Object ignorableToken = queue.take();
                    //System.out.println("no dups emerging from wait...");                    
                    long lastChangeTimeNoDups = lastChangeTimeNoDupsAllowed.get();
                    if(!ReplicationHealthChecker.isFlushThreadWaiting()) {
                        //normal processing
                        if ( ((Map)currentMapNoDupsAllowed.get()).size() > 0
                                && timeThresholdExceeded(DEFAULT_TIME_THRESHOLD, lastChangeTimeNoDups) ) {                     
                            flushAllMessagesFromCurrentMapNoDupsAllowed(isWaitForAckConfigured());
                        }
                    } else { //only if flushThreadWaiting
                        //flush once more if you have data
                        try {
                            if(((Map)currentMapNoDupsAllowed.get()).size() > 0) {
                                flushAllMessagesFromCurrentMapNoDupsAllowed(isWaitForAckConfigured());
                            }
                        } finally {
                            //signal you are finished unloading if you have not previously done so
                            if(!hasSignaled) {                            
                                CountDownLatch doneSignal = ReplicationHealthChecker.getDoneSignal();
                                doneSignal.countDown();
                                hasSignaled = true;
                            }
                        }
                    }                            
                } catch (InterruptedException inEx) {
                    this.done = true;
                } catch (Throwable t) {
                    _logger.log(Level.INFO, "nodup thread exception:", t);
                }   finally {
                    //if done is set signal you are finished if you have not previously done so
                    if(done && !hasSignaled) {
                        CountDownLatch doneSignal = ReplicationHealthChecker.getDoneSignal();
                        doneSignal.countDown();
                        hasSignaled = true;
                    }
                }
            }
        }

    }
    
    private class TimerDispatchThread extends TimerTask {
        
        public TimerDispatchThread() {
        }
        
        public void run() {

            long lastChangeTimeNoDups = lastChangeTimeNoDupsAllowed.get();
            if ( ((Map)currentMapNoDupsAllowed.get()).size() > 0
                    && timeThresholdExceeded(DEFAULT_TIME_THRESHOLD, lastChangeTimeNoDups) ) {            
                boolean wakeupDispatcher = timeToChangeNoDupsAllowed.compareAndSet(false, true); //expect false  set  to true            
                if (wakeupDispatcher) {
                    //System.out.println("timer dispatching nodups...");
                    dispatchThreadNoDupsAllowed.wakeup();
                }                
            }
            
            long lastChangeTimeDups = lastChangeTime.get();
            if ( ((Map)currentMap.get()).size() > 0
                    && timeThresholdExceeded(DEFAULT_TIME_THRESHOLD, lastChangeTimeDups) ) {
                boolean wakeupDispatcher = timeToChange.compareAndSet(false, true); //expect false  set  to true            
                if (wakeupDispatcher) {
                    //System.out.println("timer dispatching dups...");
                    dispatchThread.wakeup();
                }                
            }
           
        }

    }
    
    private class BulkMessageSender implements Runnable {

        private List list = null;
        private boolean needBulkAck = false; 
        
        public BulkMessageSender(List list, boolean needBulkAck) {
            this.list = list;
            this.needBulkAck = needBulkAck;
        }
        
        public void run() {
            createMessageAndSend(list, needBulkAck, null);
            list.clear();
        }
    }
    
    private class BulkMessageAcker implements Runnable {

        private Message sentMessage = null;
        private PipeWrapper pipeWrapper = null;
        private boolean success = false; 
        
        public BulkMessageAcker(Message sentMessage, PipeWrapper pipeWrapper, boolean success) {
            this.sentMessage = sentMessage;
            this.pipeWrapper = pipeWrapper;
            this.success = success;
        }
        
        public void run() {
            if(success) {
                pipeWrapper.messageSendSucceeded(sentMessage);
            } else {
                pipeWrapper.messageSendFailed(sentMessage);
            }
        }
    }    
   
}
