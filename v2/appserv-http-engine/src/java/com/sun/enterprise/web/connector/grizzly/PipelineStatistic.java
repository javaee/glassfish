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

package com.sun.enterprise.web.connector.grizzly;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This class is a placeholde for gathering statistic
 * from a <code>Pipeline</code>
 *
 * @author Jean-Francois Arcand
 */
public class PipelineStatistic {

    /**
     * The port of which we gather statistics
     */
    private int port = -1;
    
    
    /**
     * Is this object started?
     */ 
    private boolean started = false;
    
    
    /**
     * Maximum pending connection before refusing requests.
     */
    private int maxQueueSizeInBytes = -1;
    
    
    /**
     * The total number of connections queued during the lifetime of the 
     * pipeline
     */
    private int totalCount;
    
    
    /** 
     * The largest number of connections that have been in the pipeline
     * simultaneouly
     */
    private int peakCount;
    
    
    /** 
     * Total number of pipeline overflows
     */
    private int overflowCount;
    
    
    /**
     * The Thread Pool used when gathering count statistic.
     */
    private ScheduledThreadPoolExecutor countAverageExecutor;
    
    
    /**
     * Average number of connection queued in that last 1 minute
     */
    private Statistic lastMinuteStat = new Statistic(1 * 60);
    
    
    /**
     * Average number of connection queued in that last 5 minute
     */
    private Statistic lastFiveMinuteStat = new Statistic(5 * 60);
    
    
    /**
     * Average number of connection queued in that last 15 minute
     */
    private Statistic lastFifteenMinuteStat = new Statistic(15 * 60);
    
    
    /**
     * Placeholder to gather statistics.
     */
    private ConcurrentHashMap<Integer,Statistic> stats = 
            new ConcurrentHashMap<Integer,Statistic>();

    /**
     * The pipelines whose stats are being collected
     */
    private Pipeline processorPipeline;
 
    
    /**
     * <code>Future</code> instance in case we need to stop this object.
     */
    private Future futures[] = new Future[3];


    /**
     * Total number of connections that have been accepted.
     */
    private int totalAcceptCount;


    // -------------------------------------------------------------------//
    
    
    /**
     * Constructor
     *
     * @param port Port number for which pipeline (connection) stats will be
     * gathered
     */
    public PipelineStatistic(int port) {
        this.port = port;
        
        countAverageExecutor = new ScheduledThreadPoolExecutor(3,
            new GrizzlyThreadFactory("GrizzlyPipelineStat",
                port,Thread.NORM_PRIORITY));        
    }
    
    
    /**
     * Start gathering statistics.
     */
    public void start(){    
        if ( started ) return;
        
        futures[0] = countAverageExecutor.scheduleAtFixedRate(lastMinuteStat, 1 , 
                lastMinuteStat.getSeconds(), TimeUnit.SECONDS);
        futures[1] = countAverageExecutor.scheduleAtFixedRate(lastFiveMinuteStat, 1 , 
                lastFiveMinuteStat.getSeconds(), TimeUnit.SECONDS);
        futures[2] = countAverageExecutor.scheduleAtFixedRate(lastFifteenMinuteStat, 1 , 
                lastFifteenMinuteStat.getSeconds(), TimeUnit.SECONDS);    
        
        stats.put(lastMinuteStat.getSeconds(), lastMinuteStat);
        stats.put(lastFiveMinuteStat.getSeconds(), lastFiveMinuteStat);
        stats.put(lastFifteenMinuteStat.getSeconds(), lastFifteenMinuteStat);
        
        started = true;
    }
    
    
    /**
     * Stop gathering statistics.
     */
    public void stop(){
        if ( !started ) return;
        
        for (Future future: futures){
            future.cancel(true);
        }
               
        stats.clear();
        started = false;
    }    
    
    
    /**
     * Gather <code>Pipeline</code> statistic.
     */
    public boolean gather(int queueLength){
        if ( queueLength == maxQueueSizeInBytes){
            overflowCount++;
            return false;
        }
       
        if ( queueLength > 0 )
            totalCount++;
        
        // Track peak of this Pipeline
        if (queueLength > peakCount) {
            peakCount = queueLength;
        } 
        return true;
    }
 
    
    /**
     * Total number of pipeline overflow
     */
    public int getCountOverflows(){
        return overflowCount;
    }
     
     
    /**
     * Gets the largest number of connections that were in the queue
     * simultaneously.
     *
     * @return Largest number of connections that were in the queue
     * simultaneously
     */    
    public int getPeakQueued(){
       return peakCount;
    }    
    

    /**
     * Gets the maximum size of the connection queue
     *
     * @return Maximum size of the connection queue
     */    
    public int getMaxQueued() {
        return maxQueueSizeInBytes;
    }


    /**
     * Gets the total number of connections that have been accepted.
     *
     * @return Total number of connections that have been accepted.
     */
    public int getCountTotalConnections() {
        return totalAcceptCount;
    }

    
    /**
     * Set the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public void setQueueSizeInBytes(int maxQueueSizeInBytesCount){
        this.maxQueueSizeInBytes = maxQueueSizeInBytesCount;
    }
    
    
    /**
     * Get the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public int getQueueSizeInBytes(){
        return maxQueueSizeInBytes;
    }
        

    /** 
     * Gets the total number of connections that have been queued.
     *
     * A given connection may be queued multiple times, so
     * <code>counttotalqueued</code> may be greater than or equal to
     * <code>counttotalconnections</code>.
     *
     * @return Total number of connections that have been queued
     */        
    public int getCountTotalQueued() {
        return totalCount;
    }
    

    /**
     * Gets the number of connections currently in the queue
     *
     * @return Number of connections currently in the queue
     */    
    public int getCountQueued() {
        int size = 0;

        if (processorPipeline != null) {
            size += processorPipeline.size();
        }

        return size;
    }

   
    /**
     * Gets the total number of ticks that connections have spent in the
     * queue.
     * 
     * A tick is a system-dependent unit of time.
     *
     * @return Total number of ticks that connections have spent in the
     * queue
     */
    public int getTicksTotalQueued() {
        return -1; // Not supported
    }

    
    /** 
     * Gets the average number of connections queued in the last 1 minute
     *
     * @return Average number of connections queued in the last 1 minute
     */    
    public int getCountQueued1MinuteAverage() {
        return getCountAverage(1);
    }


    /** 
     * Gets the average number of connections queued in the last 5 minutes
     *
     * @return Average number of connections queued in the last 5 minutes
     */    
    public int getCountQueued5MinuteAverage() {
        return getCountAverage(5);
    }


    /** 
     * Gets the average number of connections queued in the last 15 minutes
     *
     * @return Average number of connections queued in the last 15 minutes
     */    
    public int getCountQueued15MinuteAverage() {
        return getCountAverage(15);
    }


    // -------------------------------------------------------------------//
    // Package protected methods

    public void incrementTotalAcceptCount() {
        totalAcceptCount++;
    }

    void setProcessorPipeline(Pipeline processorPipeline) {
        this.processorPipeline = processorPipeline;
    }

    // -------------------------------------------------------------------//
    // Private methods

    /**
     * Gets the average number of connection queued in the last
     * <code>minutes</code> minutes.
     *
     * @param minutes The number of minutes for which the average number of
     * connections queued is requested
     * 
     * @return Average number of connections queued
     */
    private int getCountAverage(int minutes){
        Statistic stat = stats.get((minutes * 60));
        return (stat == null ? 0 : stat.average());
    }

    
    /**
     * Utility class to track average count.
     */
    class Statistic implements Runnable{
                
        int lastCount = 0;
        int average = 0;
        int seconds;
             
        public Statistic(int seconds){
            this.seconds = seconds;
        }

                               
        public void run() {
            average = totalCount - lastCount;
            lastCount = totalCount;
        }  
        
        public int average(){
            return average;
        }   
        
        
        public int getSeconds(){
            return seconds;
        }
    }
    
    
}
