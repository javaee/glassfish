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
 * AsyncHandlerProducer.java
 *
 * Created on June 19, 2006, 1:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.monitor.callflow;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 *
 * A performant implementation for AsyncHandler. 
 * It holds reference to six producer queues; one for each type of callflow table.
 * @author Harpreet Singh
 */
public class AsyncHandlerProducer implements AsyncHandlerIntf {

    private static final Logger logger =
            Logger.getLogger(AdminConstants.kLoggerName);
    /**
     * Sets the size of the Q's handling each of the tables. This
     * can be superseded for (END|START)Time table property 
     * CONTAINER_TIME_PROPERTY (Since END|START Time table roughly fills
     * 2-3x faster than other table
     */ 
    public static final String Q_SIZE_PROPERTY = 
            "com.sun.enterprise.callflow.qsize";
    private int qSize = 1000;
    public static final String CONTAINER_TIME_Q_SIZE_PROPERTY =
            "com.sun.enterprise.callflow.containertime.qsize";
    private int containerQSize = 1500;
    
    public static AsyncHandlerProducer _singleton;
    // RequestStart and RequestEnd Qs
    CallflowProducerQueue rsq;
    CallflowProducerQueue req;
    // MethodStart and MethodEnd Qs
    CallflowProducerQueue msq;
    CallflowProducerQueue meq;
    // StartTime and EndTime Qs
    CallflowProducerQueue stq;
    CallflowProducerQueue etq;
    // special callflow debug flag
    boolean traceOn = false;
    
    /**
     * The ConsumerQ. This queue is populated by instances of the ProducerQ.
     * The consumer thread goes over this Q and processes the Q to write 
     * information to the database.
     */
    private static final BlockingQueue consumerQ = new LinkedBlockingQueue (6);
    
    HandlerChain handlerChain;
    private ConsumerQProcessor consumerThread ;
    // Thread wrapper for ConsumerQProcessor. Eventually submit to the ORB
    private Thread consumer;
    boolean isThreadInitialized = false;
    private boolean enabled = false;
    /** Creates a new instance of AsyncHandlerProducer */
    private AsyncHandlerProducer() {
        setupQSizes ();        
        setupHandlerChain ();
        // create the chain that will be processed
        consumerThread = new ConsumerQProcessor (consumerQ, handlerChain);
        traceOn = TraceOnHelper.isTraceOn();        
    }

   //<editor-fold defaultstate="collapsed" desc="Q, Thread and Handler Setup">
    private void setupHandlerChain (){
        handlerChain = new HandlerChain ();
        handlerChain.addHandler (new DbHandler (DbAccessObjectImpl.getInstance()));
    }
    private void setupQSizes (){
        qSize = Math.abs(Integer.getInteger(Q_SIZE_PROPERTY, 1000));
        containerQSize = Math.abs (
                Integer.getInteger(CONTAINER_TIME_Q_SIZE_PROPERTY, 2000));
                
        if(this.containerQSize == containerQSize){// default values, prop not set
            if (qSize == this.qSize){// default values for qSize, not overridden
                // do not override the containerQsize
            } else{
                // container qSize is roughly 2-4 times qSize
                this.containerQSize = qSize * 3;                
            }
        }
        this.qSize = qSize;    
        if (traceOn){
            logger.log(Level.INFO, "Callflow : AsyncHandlerProducer: QSize = " +
                    this.qSize +
                    " Container Time Q Size =" + this.containerQSize);
        }
    }
    private void setupQs (){
        rsq = CallflowProducerQueue.getInstance (consumerQ, 
                "RequestStartProducerQ", qSize);
        req = CallflowProducerQueue.getInstance (consumerQ, 
                "RequestEndProducerQ", qSize);
        msq = CallflowProducerQueue.getInstance (consumerQ, 
                "MethodStartProducerQ", qSize);
        meq = CallflowProducerQueue.getInstance (consumerQ, 
                "MethodEndProducerQ", qSize);
        stq = CallflowProducerQueue.getInstance (consumerQ, 
                "StartTimeProducerQ", containerQSize);
        etq = CallflowProducerQueue.getInstance (consumerQ, "EndTimeProducerQ", 
                containerQSize);       
    }
    private void clearQs (){
        rsq = null;
        req = null;
        msq = null;
        meq = null;
        stq = null;
        etq = null;
    }
    
    public static final AsyncHandlerIntf getInstance (){
        if (_singleton == null){
            _singleton = new AsyncHandlerProducer ();
        }
        return _singleton;
    }
    private Thread createThread (){
        Thread t = new Thread (consumerThread, "CallFlow Async Consumer"); 
        t.setDaemon(true);
        return t;
    }
//</editor-fold>    
    public synchronized void enable() {
        if (!enabled){
            try{
                if (!isThreadInitialized){
                    consumer = null; // sanity
                    setupQs ();
                    consumer = createThread ();
                    consumer.start();
                    isThreadInitialized = true;
                }
            } catch (Exception e){ 
                // if consumer thread is already on. We will get an exception.
                // Should never get here.
                logger.log(Level.INFO, "Callflow - attempting to " +
                        "start asynchronous thread, but it is already on." +
                        "Restart Server !" , e);
            }
        }
        enabled = true;
        if (traceOn){            
            logger.log(Level.INFO, "Callflow: enable(AsyncProducerQ) Consumer" +
                    " thread started!");
        }
    }

    public synchronized void disable() {
        if (enabled){
            flushQs ();            
            consumerThread.stopConsumerThread();
            isThreadInitialized = false;
            enabled = false;
            forceConsumption();
            clearQs ();
        }
        if (traceOn){            
            logger.log (Level.INFO, "Callflow: (disable)AsynchProducerQ " +
                    "Consumer thread stopped!");
        }
    }
    
    // for long thread sleep times in the asyncthread, it is better to 
    // force consumption of the q, such that data is available right away
    // only called from disable
    private void forceConsumption (){
        int noOfQueues = consumerQ.size ();
        if (noOfQueues == 0){
            return;
        }
        if (traceOn){
            logger.log(Level.INFO, "Callflow: disable(AsyncHandlerProducer) "+
                    "forcing write of Qs to the handlers");
        }

        Handler[] handler = handlerChain.getHandlers();
        for (int i = 0; i<noOfQueues; i++){
            CallflowProducerQueue q =  (CallflowProducerQueue) consumerQ.poll();
            if (q != null){
                if (traceOn){
                    logger.log(Level.INFO, "Callflow: AsyncHandlerProducer.processProducerQ " +
                            "QName ="+
                            q.getName());
                }
                TransferObject[] to = q.getAndRemoveAll();
                if(to != null){
                    for ( int j=0; j<handler.length; j++){
                        handler[j].handle(to);                        
                    }
                }
            }
                
        }        
    }
    private void flushQs (){
        if(rsq != null )
           rsq.flush ();
        if (req != null)
         req.flush ();
        if (msq != null)
            msq.flush ();
        if(meq != null)
            meq.flush ();
        if (stq != null)
          stq.flush ();
        if (etq != null)
            etq.flush ();
    }

    public void handleRequestStart(String requestId, long timeStamp,
            long timeStampMillis, RequestType requestType, 
            String callerIPAddress, String remoteUser) {
        
        RequestStartTO rsto = new RequestStartTO();
        rsto.setRequestId(requestId);
        rsto.setTimeStamp(timeStamp);
        rsto.setTimeStampMillis(timeStampMillis);
        rsto.setRequestType(requestType);
        rsto.setIpAddress(callerIPAddress);
        //rsto.setRemoteUser(remoteUser); // not currently in db schema.
        rsq.add (rsto);
        if (traceOn){
            logger.log(Level.INFO, 
                    "Callflow: RequestStart (AsyncHandlerProducer) id = :"+
                    requestId +
                    " Type =" + requestType.toString());
        }
    }
    
    public void handleRequestEnd(String requestId, long timeStamp) {
        RequestEndTO reto = new RequestEndTO();
        reto.setRequestId(requestId);
        reto.setTimeStamp(timeStamp);
        req.add(reto);
        if (traceOn){
            logger.log(Level.INFO, "Callflow: RequestEnd(AsyncHandlerProducer) id="
                    +requestId);
        }
        
    }    
    
    public void handleMethodStart(String requestId, long timeStamp, 
            String methodName, ComponentType componentType, 
            String applicationName, String moduleName, String componentName, 
            String threadId, String transactionId, String securityId) {
        
        MethodStartTO msto =  new MethodStartTO();
        msto.setRequestId(requestId);
        msto.setTimeStamp(timeStamp);
        msto.setMethodName(methodName);
        msto.setComponentType(componentType);
        msto.setAppName(applicationName);
        msto.setModuleName(moduleName);
        msto.setComponentName(componentName);
        msto.setThreadId(threadId);
        msto.setTransactionId(transactionId);
        msto.setSecurityId(securityId);        
        msq.add(msto);        
        if (traceOn){
            logger.log(Level.INFO, "Callflow: MethodStart(AsyncHandlerProducer) " +
                    " id = "+requestId+ " applicationName = "+ applicationName);
        }
        
    }

    public void handleMethodEnd(String requestId, long timeStamp, Throwable exception) {
        MethodEndTO meto = new MethodEndTO();
        meto.setRequestId(requestId);
        meto.setTimeStamp(timeStamp);
        meto.setException(((exception == null) ? null : exception.toString()));
        meq.add (meto);
        if (traceOn){
            logger.log(Level.INFO, "Callflow: MethodEnd(AsyncHandlerProducer). " +
                    "id = "+requestId);
        }

    } 

    public void handleStartTime(String requestId, long timeStamp,
            ContainerTypeOrApplicationType type) {
        StartTimeTO stto = new StartTimeTO();
        stto.setRequestId(requestId);
        stto.setTimeStamp(timeStamp);
        stto.setContainerTypeOrApplicationType(type);
        stq.add(stto);        
        if (traceOn){
            logger.log(Level.INFO, "Callflow: StartTime(AsyncHandlerProducer)" +
                    "id = "+requestId);
        }

    }

    public void handleEndTime(String requestId, long timeStamp,
            ContainerTypeOrApplicationType type) {
        EndTimeTO etto = new EndTimeTO();
        etto.setRequestId(requestId);
        etto.setTimeStamp(timeStamp);
        etto.setContainerTypeOrApplicationType(type);
        etq.add(etto);        
        if (traceOn){
            logger.log(Level.INFO, "Callflow: EndTime(AsyncHandlerProducer) id="
                    +requestId);
        }
        
    }   
    public void flush (){
        flushQs ();
        forceConsumption ();
    }
}
