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

import java.nio.channels.SelectionKey;
import java.util.LinkedList;

/**
 * Internal FIFO used by the Worker Threads to pass information
 * between <code>Task</code> objects.
 *
 * @author Jean-Francois Arcand
 */
public class LinkedListPipeline extends LinkedList<Task> implements Pipeline{


    /**
     * The number of thread waiting for a <code>Task</code>
     */
    protected int waitingThreads = 0;
    
    
    /**
     * The maximum number of Thread
     */
    protected int maxThreads = 20;
    

    /**
     * The minimum numbers of <code>WorkerThreadImpl</code>
     */
    protected int minThreads = 5;
                                                                                
    /**
     * The minimum numbers of spare <code>WorkerThreadImpl</code>
     */
    protected int minSpareThreads = 2;


    /**
     * The port used.
     */
    protected int port = 8080;
    

    /**
     * The number of <code>WorkerThreadImpl</code>
     */
    protected int threadCount = 0;
    

    /**
     * The name of this Pipeline
     */
    protected String name;
    
    
    /**
     * The Thread Priority
     */
    protected int priority = Thread.NORM_PRIORITY;
    
    
    /**
     * Has the pipeline already started
     */
    protected boolean isStarted = false; 
    

    /**
     * <code>WorkerThreadImpl</code> amanged by this pipeline.
     */
    protected transient WorkerThreadImpl[] workerThreads;
    
    
    /**
     * Maximum pending connection before refusing requests.
     */
    protected int maxQueueSizeInBytes = -1;
    
    
    /**
     * The increment number used when adding new thread.
     */
    protected int threadsIncrement = 1;
    
    
    /**
     * The request times out during transaction.
     */
    protected int threadsTimeout = Constants.DEFAULT_TIMEOUT;
        
    
    /**
     * The <code>PipelineStatistic</code> objects used when gathering statistics.
     */
    protected transient PipelineStatistic pipelineStat;
    
    // ------------------------------------------------------- Constructor -----/
    
    public LinkedListPipeline(){
        super();
    }
    
    public LinkedListPipeline(int maxThreads, int minThreads, String name, 
                              int port, int priority){
                        
        this.maxThreads = maxThreads;
        this.port = port;
        this.name = name;
        this.minThreads = minThreads;
        this.priority = priority;
        
        if ( minThreads < minSpareThreads )
            minSpareThreads = minThreads;
        
    }

    
    public LinkedListPipeline(int maxThreads, int minThreads, String name, 
                              int port){
                        
        this(maxThreads,minThreads,name,port,Thread.NORM_PRIORITY);
    }

   
    // ------------------------------------------------ Lifecycle ------------/
    
    /**
     * Init the <code>Pipeline</code> by initializing the required
     * <code>WorkerThreadImpl</code>. Default value is 10
     */
    public synchronized void initPipeline(){
        
        if (minThreads > maxThreads) {
            minThreads = maxThreads;
        }
        
        workerThreads = new WorkerThreadImpl[maxThreads];
        increaseWorkerThread(minThreads, false);        
   }

    
    /**
     * Start the <code>Pipeline</code> and all associated 
     * <code>WorkerThreadImpl</code>
     */
    public synchronized void startPipeline(){
        if (!isStarted) {
            for (int i=0; i < minThreads; i++){
                workerThreads[i].start();
            }
            isStarted = true;
        }
    }
    

    /**
     * Stop the <code>Pipeline</code> and all associated
     * <code>WorkerThreadImpl</code>
     */
    public synchronized void stopPipeline(){
        if (isStarted) {
            for (int i=0; i < threadCount; i++){
                workerThreads[i].terminate();
            }
            isStarted = false;
        }
        notifyAll();
    }


    /**
     * Create new <code>WorkerThreadImpl</code>. This method must be invoked
     * from a synchronized block.
     */
    protected void increaseWorkerThread(int increment, boolean startThread){        
        WorkerThreadImpl workerThread;
        int currentCount = threadCount;
        int increaseCount = threadCount + increment; 
        for (int i=currentCount; i < increaseCount; i++){
            workerThread = new WorkerThreadImpl(this, 
                    name + "WorkerThread-"  + port + "-" + i);
            workerThread.setPriority(priority);
            
            if (startThread)
                workerThread.start();
            
            workerThreads[i] = workerThread;
            threadCount++; 
        }
    }
    
    
    /**
     * Interrupt the <code>Thread</code> using it thread id
     */
    public synchronized boolean interruptThread(long threadID){
        ThreadGroup threadGroup = workerThreads[0].getThreadGroup();
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
        
        for (Thread thread: threads){
            if ( thread != null && thread.getId() == threadID ){                
                if ( Thread.State.RUNNABLE != thread.getState()){
                    try{
                        thread.interrupt();
                        return true;
                    } catch (Throwable t){
                        ; // Swallow any exceptions.
                    }
                }
            }
        }
        return false;
    }
    
    
    // ---------------------------------------------------- Queue ------------//
  
    
    /**
     * Add an object to this pipeline
     */
    public synchronized void addTask(Task task) {
        boolean rejected = false;
        int queueSize =  size();
        if ( maxQueueSizeInBytes != -1 && maxQueueSizeInBytes <= queueSize){
            task.cancelTask("Maximum Connections Reached: " 
                + maxQueueSizeInBytes + " -- Retry later", 
                    "HTTP/1.1 503 Service Unavailable");
            task.getSelectorThread().returnTask(task);           
            return;
        }
        
        addLast(task);
        notify();

        // Create worker threads if we know we will run out of them
        if (threadCount < maxThreads && waitingThreads < (queueSize + 1)){
            int left = maxThreads - threadCount;
            if (threadsIncrement > left){
                threadsIncrement = left;
            }
            increaseWorkerThread(threadsIncrement,true);
        }
    }


    /**
     * Return a <code>Task</code> object available in the pipeline.
     * All Threads will synchronize on that method
     */
    public synchronized Task getTask() {
        if (size() - waitingThreads <= 0) {            
            try { 
                waitingThreads++; 
                wait();
            }  catch (InterruptedException e)  {
                Thread.currentThread().interrupted();
            }
            waitingThreads--;       
        }

        if (pipelineStat != null) {
            pipelineStat.gather(size());
        }       
        return poll();
    }

    
    /**
     * Invoked when the SelectorThread is about to expire a SelectionKey.
     * @return true if the SelectorThread should expire the SelectionKey, false
     *              if not.
     */
    public boolean expireKey(SelectionKey key){
       return true; 
    }    
    
    
    /**
     * Return <code>true</code> if the size of this <code>ArrayList</code>
     * minus the current waiting threads is lower than zero.
     */
    public boolean isEmpty() {
        return  (size() - waitingThreads <= 0);
    }
    
    // --------------------------------------------------Properties ----------//

    /**
     * Return the number of waiting threads.
     */
    public synchronized int getWaitingThread(){
        return waitingThreads;
    }
    
    
    /** 
     * Set the number of threads used by this pipeline.
     */
    public synchronized void setMaxThreads(int maxThreads){
        this.maxThreads = maxThreads;
    }    
    
    
    /** 
     * Return the number of threads used by this pipeline.
     */
    public synchronized int getMaxThreads(){
        return maxThreads;
    }
    
    
    public synchronized int getCurrentThreadCount() {
        return threadCount;
    }
      
      
    /**
     * Return the curent number of threads that are currently processing 
     * a task.
     */
    public synchronized int getCurrentThreadsBusy(){
        return (threadCount - waitingThreads);
    }
        

    /**
     * Return the maximum spare thread.
     */
    public synchronized int getMaxSpareThreads() {
        return maxThreads;
    }


    /**
     * Return the minimum spare thread.
     */
    public synchronized int getMinSpareThreads() {
        return minSpareThreads;
    }


    /**
     * Set the minimum space thread this <code>Pipeline</code> can handle.
     */
    public synchronized void setMinSpareThreads(int minSpareThreads) {
        this.minSpareThreads = minSpareThreads;
    }

    
    /**
     * Set the thread priority of the <code>Pipeline</code>
     */
    public synchronized void setPriority(int priority){
        this.priority = priority;
    }
    
    
    /**
     * Set the name of this <code>Pipeline</code>
     */
    public synchronized void setName(String name){
        this.name = name;
    }
    
    
    /**
     * Return the name of this <code>Pipeline</code>
     * @return the name of this <code>Pipeline</code>
     */
    public synchronized String getName(){
        return name+port;
    }    

    
    /**
     * Set the port used by this <code>Pipeline</code>
     * @param port the port used by this <code>Pipeline</code>
     */
    public synchronized void setPort(int port){
        this.port = port;
    }
    
    
    /**
     * Set the minimum thread this <code>Pipeline</code> will creates
     * when initializing.
     * @param minThreads the minimum number of threads.
     */
    public synchronized void setMinThreads(int minThreads){
        this.minThreads = minThreads;
    }
    
    
    public String toString(){
       return "name: " + name + " maxThreads: " + maxThreads 
                + " type: " + this.getClass().getName();        
    }
    
    
    /**
     * Set the number the <code>Pipeline</code> will use when increasing the 
     * thread pool
     */    
    public synchronized void setThreadsIncrement(int threadsIncrement){
        this.threadsIncrement = threadsIncrement;
    }
    
    
    /**
     * Set the timeout value a thread will use to times out the request.
     */   
    public synchronized void setThreadsTimeout(int threadsTimeout){
        this.threadsTimeout = threadsTimeout;
    }
    
    
    /** 
     * The number of <code>Task</code> currently queued
     * @return number of queued connections
     */
    public synchronized int getTaskQueuedCount(){
       return size();
    }

    
    /**
     * Set the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public synchronized void setQueueSizeInBytes(int maxQueueSizeInBytesCount){
        this.maxQueueSizeInBytes = maxQueueSizeInBytesCount;
    }
    
    
    /**
     * Get the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public synchronized int getQueueSizeInBytes(){
        return maxQueueSizeInBytes;
    }   
  
    
    /**
     * Set the <code>PipelineStatistic</code> object used
     * to gather statistic;
     */
    public void setPipelineStatistic(PipelineStatistic pipelineStatistic){
        this.pipelineStat = pipelineStatistic;
    }
    
    
    /**
     * Return the <code>PipelineStatistic</code> object used
     * to gather statistic;
     */
    public PipelineStatistic getPipelineStatistic(){
        return pipelineStat;
    }
    
}
