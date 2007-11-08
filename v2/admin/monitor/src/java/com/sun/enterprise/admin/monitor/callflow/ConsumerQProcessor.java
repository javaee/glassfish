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
 * ConsumerQProcessor.java
 *
 * Created on June 22, 2006, 1:34 PM
 *
 */

package com.sun.enterprise.admin.monitor.callflow;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;

/**
 *
 * @todo Add logging code
 * @todo replace this thread by the ORB Thread Pool Thread
 * @author Harpreet Singh
 */
public class ConsumerQProcessor implements Runnable{
    
    BlockingQueue consumerQ;
    private DbAccessObject dbAccessObject;    
    boolean processQ = true;
    private int sleepTime = 30000;
    private static final Logger logger =
            Logger.getLogger(AdminConstants.kLoggerName);

    private static final String CONSUMER_SLEEP_TIME = 
            "com.sun.enterprise.callflow.sleepms";
    // special callflow debug flag
    boolean traceOn = false;
    // handle to the handler chain
    private HandlerChain chain;
    
    private long numOfQueuesProcessed = 0;
    private long numOfTimesQSlept = 0;
    /**
     * Creates a new instance of ConsumerQProcessor
     */
    public ConsumerQProcessor(BlockingQueue<CallflowProducerQueue> consumerQ, 
            HandlerChain chain) {
        this.consumerQ = consumerQ;
        this.chain = chain;
        dbAccessObject = DbAccessObjectImpl.getInstance();
        traceOn = TraceOnHelper.isTraceOn();
        sleepTime = Math.abs(Integer.getInteger(CONSUMER_SLEEP_TIME, 5000));
    }
    public void stopConsumerThread (){
        processQ = false;
        if (traceOn){
            logger.log (Level.INFO, "Callflow: ConsumerQProcessor.stopConsumerThread ");
        }
    }

    public void run() {
        if (traceOn)
            logger.log(Level.INFO, "Callflow: ConsumerQProcessor.entering run ");

        while (processQ){
            if(!consumerQ.isEmpty()){
                consume ();
            } else{
                try {
                    if (traceOn){
                        logger.log(Level.INFO, "Callflow: ConsumerQProcessor." +
                                "Q Empty, sleeping for "+ getSleepTime () + 
                                " ms. Sleeping for the "+getNumOfTimesQSlept()
                                + " th time.");
                    }
                    this.numOfTimesQSlept++;
                    Thread.sleep(getSleepTime());
                } catch (InterruptedException e){
                    if (processQ == false){
                        // do not quit abruptly. Process any items remaining in
                        // queues before quitting.
                        if (traceOn)
                            logger.log(Level.INFO, "Callflow: ConsumerQProcessor." +
                                "Disable Called, forcing consumption");

                        break;
                    }
                }
            }
            
        }
        // force consumption
        consume();
        if (traceOn){
            logger.log(Level.INFO, "Callflow: ConsumerQProcessor.exiting run. "+
                    " Num of Entries Processed for each table \n"+
                    this.dbAccessObject.getNumOfRequestsProcessedAsString()
                    +" \n");
            
        }
    }
    void consume (){
        int noOfQueues = consumerQ.size ();
        numOfQueuesProcessed += noOfQueues;
        if (traceOn){
            logger.log(Level.INFO, "Callflow: ConsumerQProcessor.consume. "+
                    "ConsumerQ.length ="+ noOfQueues + ". Total number of " +
                    " Q's Processed = "+ numOfQueuesProcessed);
        }

        for (int i = 0; i<noOfQueues; i++){
            CallflowProducerQueue q =  (CallflowProducerQueue) consumerQ.poll();
            if (q != null){
                if (traceOn){
                    logger.log(Level.INFO, "Callflow: ConsumerQProcessor.processProducerQ "+
                            q.getName());
                }
                TransferObject[] to = q.getAndRemoveAll();
                if (to != null){
                    Handler[] handler = chain.getHandlers();
                    for (int j=0;j<handler.length;j++ ){
                        if (traceOn){
                            logger.log(Level.INFO, "Callflow: ConsumerQProcessor.processProducerQ "+
                                    handler[j]);
                        }
                        handler[j].handle(to);
                    }
                }
            }
        }
   }
    /**
     * consumes the ConsumerQ. Returns the number of Q's processed
     */
//    int consume() {
//        int noOfQueues = consumerQ.size();
//        numOfQueuesProcessed += noOfQueues;
//        if (traceOn){
//            logger.log(Level.INFO, "Callflow: ConsumerQProcessor.consume. "+
//                    "ConsumerQ.length ="+ noOfQueues + ". Total number of " +
//                    " Q's Processed = "+ numOfQueuesProcessed);
//        }
//        for (int i = 0; i<noOfQueues; i++){
//            CallflowProducerQueue q =  (CallflowProducerQueue) consumerQ.poll();
//            if (q != null)
//                processProducerQ(q);
//        }
//        return noOfQueues;
//    }

//    void processProducerQ(CallflowProducerQueue q) {
//        if (traceOn){
//            logger.log (Level.INFO, "Callflow: ConsumerQProcessor.processProducerQ "+
//                    q.getName ());
//        }
//        TransferObject[] to = q.getAndRemoveAll();
//        if (to != null){
//            writeToDb(to, q.getName());
//        }
//    }
    
//    private void writeToDb(TransferObject[] to, String name) {
//        dbAccessObject.insert(to);
//        if (traceOn){            
//            for (int i=0; i<to.length; i++){
//                // just display one
//                logger.log (Level.INFO, "Writing TO to DB :" +name + " "+
//                        to[i].getClass().getName() );
//                break;
//            }
//        }
//    }

    public int getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    public long getNumOfQueuesProcessed() {
        return numOfQueuesProcessed;
    }

    public long getNumOfTimesQSlept() {
        return numOfTimesQSlept;
    }

    public String getCONSUMER_SLEEP_TIME() {
        return CONSUMER_SLEEP_TIME;
    }
}
