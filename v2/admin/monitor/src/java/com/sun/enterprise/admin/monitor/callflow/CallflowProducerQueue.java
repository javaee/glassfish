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
 * CallflowProducerQueue.java
 *
 * Created on June 19, 2006, 2:21 PM
 *
 */

package com.sun.enterprise.admin.monitor.callflow;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a ProducerQ that is populated by the Agent. A different asynchronous
 * thread is responsible for consuming items produced in this queue.
 * CallflowProducerQueue is created for each callflow table in the database i.e.
 * RequestStart, RequestEnd, MethodStart, MethodEnd, StartTime and EndTime tables.
 * An instance of the ConsumerQ is passed to this object on creation.
 * The producerQ contains TransferObjects that are eventually written to the
 * database.
 * @todo Make this class JMX capable.
 * @author Harpreet Singh
 */
public class CallflowProducerQueue {
    private static final Logger logger =
            Logger.getLogger(AdminConstants.kLoggerName);
    
    // special callflow debug flag
    boolean traceOn = false;        
    int qSize = 10;
    private String name;
    /**
     * Consumer notified when threshold is reached. Default is 80%.
     */
    double THRESHOLD_PERCENTAGE = 0.8;
    long threshold = 8;
    
    AtomicInteger CURRENT_SIZE = new AtomicInteger(0);
    AtomicBoolean NOTIFIED_CONSUMER = new AtomicBoolean(false);
    AtomicLong entriesProcessed = new AtomicLong(0);
    /**
     * Contains TransferObjects that are written to the database.
     */
    ConcurrentLinkedQueue<TransferObject> producerQ =
            new ConcurrentLinkedQueue<TransferObject> ();
    /**
     * ConsumerQueue. Producer pushes TransferObjects to ConsumerQ and notifies
     * the consumer thread to process the queue.
     */
    private BlockingQueue<CallflowProducerQueue> consumerQ;
    
    public static CallflowProducerQueue
            getInstance(BlockingQueue<CallflowProducerQueue> consumerQ, 
            String name, int qSize){
        return new CallflowProducerQueue(consumerQ, name, qSize);
    }
    /**
     * Creates a new instance of CallflowProducerQueue
     */
    private CallflowProducerQueue(BlockingQueue<CallflowProducerQueue> consumerQ,
            String name, int qSize) {
        this.consumerQ = consumerQ;
        this.name = name;
        this.qSize = qSize; 
        traceOn = TraceOnHelper.isTraceOn();
    }
    
    /**
     * Add a TransferObject to the producerQ. Notifies the consumer if Q is
     * above threshold percentage. Adds after reaching threshold percentage
     * are allowed.
     */
    public void add(TransferObject to){
        producerQ.add(to);
        int current = CURRENT_SIZE.incrementAndGet();
        if (traceOn){
            logger.log(Level.INFO, "Callflow:CallflowProducerQ.add : " + name +
                    " adding row ; QSize = "+ current +
                    " ThresholdSize = "+ threshold);
        }
        if (current >= threshold){
            flush();
        }
    }
    /**
     * Empties the producerQ and returns all the TransferObjects.
     */
    public TransferObject[] getAndRemoveAll(){
        int current = CURRENT_SIZE.get();
        if (current == 0){
            return null;
        }
        
        TransferObject[] to = new TransferObject[current];
        for (int i=0; i<current; i++){
            to[i] = producerQ.poll();
        }
        long numOfEntries = CURRENT_SIZE.longValue();
        CURRENT_SIZE.set(CURRENT_SIZE.get() - current);
        NOTIFIED_CONSUMER.set(false);
        entriesProcessed.addAndGet(numOfEntries);
        if (traceOn){
            logger.log(Level.INFO, "Callflow: CallflowProducerQ.getAndRemoveAll:"
                    + name + " Old Q Size = "+ current + " New Q Size = "+
                    CURRENT_SIZE.get() + " Notified ConsumerQ reset to false."+
                    " Entries Processed so far = "+ entriesProcessed.longValue());
        }
        return to;
    }
    
    /**
     * Allows flushing the producerQ's. This will be called when callflow is to
     * be disabled and all collected data needs to be explicitly flushed out.
     */
    public void flush() {
        if (CURRENT_SIZE.get () <=0){
            return;
        }
        boolean notified = NOTIFIED_CONSUMER.get();
        
        if (!notified){
            try {
                consumerQ.put(this);
                NOTIFIED_CONSUMER.set(true);
                if (traceOn){
                    logger.log(Level.INFO, "Callflow: CallflowProducerQ.flush:"
                            + name +" notifying ConsumerQ ");
                }
            } catch (InterruptedException ex) {
            }
        } else {
            if (traceOn){
                logger.log(Level.INFO, "Callflow: CallflowProducerQ.flush : "
                        + name + " "+ "ConsumerPreviouslyNotified? "+ notified+
                        " Not renotifying the consumer.");
            }
        }
    }
    
    /*
     * Make this queue configurable via JMX
     */
    public void setQSize(int size){
        this.qSize = size;
        calculateThreshold();
    }
    public int getQSize (){
        return this.qSize;
    }
    /*
     * Make this queue configurable via JMX
     */
    public int getCurrentSize() {
        return CURRENT_SIZE.intValue();
    }
    
    private void calculateThreshold(){
        threshold = Math.round(THRESHOLD_PERCENTAGE * qSize);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    /*
     * Returns the total number of entries written by this queue
     */
    public long getEntriesProcessed(){
        return entriesProcessed.longValue();
    }
}
