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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;


/**
 * A wrapper around an <code>ThreadPoolExecutor</code>. This thread pool
 * is bounded by an <code>ArrayBlockingQueue</code>
 *
 * @author Jean-Francois Arcand
 */
public class ThreadPoolExecutorPipeline implements Pipeline,
                                                   RejectedExecutionHandler{
    

    /**
     * The number of thread waiting for a <code>Task</code>
     */
    private int waitingThreads = 0;
    
    
    /**
     * The maximum number of Thread
     */
    private int maxThreads = 20;
    

    /**
     * The minimum numbers of <code>WorkerThread</code>
     */
    private int minThreads = 10;

    
    /**
     * The port used.
     */
    private int port = 8080;
    

    /**
     * The number of <code>WorkerThread</code>
     */
    private int threadCount =0;
    

    /**
     * The name of this Pipeline
     */
    private String name;
    
    
    /**
     * The Thread Priority
     */
    private int priority = Thread.NORM_PRIORITY;
    
    
    /**
     * Has the pipeline already started
     */
    private boolean isStarted = false; 
    
    
    /**
     * <code>ExecutorService</code> wrapped by this pipeline.
     */
    private ThreadPoolExecutor workerThreads;
    
   
    /**
     * Connection queue
     */
    private ArrayBlockingQueue<Runnable> arrayBlockingQueue;
    
    
    /**
     * Maximum pending connection before refusing requests.
     */
    private int maxQueueSizeInBytes = -1;
    
    
    /**
     * maximum size of the connection queue, in bytes.
     */
    private int queueSizeInBytes = 4096;
    
    
    /**
     * The <code>PipelineStatistic</code> objects used when gathering statistics.
     */
    protected PipelineStatistic pipelineStat;
    // ------------------------------------------------ Lifecycle ------------/
    
    /**
     * Init the <code>Pipeline</code> by initializing the required
     * <code>ThreadPoolExecutor</code>. 
     */
    public void initPipeline(){
        
        if (isStarted){
            return;
        }
        isStarted = true;
        arrayBlockingQueue = 
                        new ArrayBlockingQueue<Runnable>(maxQueueSizeInBytes, true);
        
        workerThreads = new ThreadPoolExecutor(
                               maxThreads,
                               maxThreads,
                               0L,
                               TimeUnit.MILLISECONDS,
                               arrayBlockingQueue,
                               new GrizzlyThreadFactory(name,port,priority),
                               this);
    }

    /**
     * Start the <code>Pipeline</code> 
     */
    public void startPipeline(){
        if (isStarted){
            return;
        }
        ; // Do nothing
    }
    

    /**
     * Stop the <code>Pipeline</code>
     */
    public void stopPipeline(){
        if (!isStarted){
            return;
        }
        isStarted = false;
        workerThreads.shutdown();
    }
    
    // ---------------------------------------------------- Queue ------------//
  
    
    /**
     * Add an object to this pipeline
     */
    public void addTask(Task task){
        if (workerThreads.getQueue().size() > maxQueueSizeInBytes ){
            task.cancelTask("Maximum Connections Reached: " 
                            + pipelineStat.getQueueSizeInBytes()
                            + " -- Retry later", HtmlHelper.OK);
            task.getSelectorThread().returnTask(task);
            return;                                               
        }       
        workerThreads.execute((Runnable)task);
                
        if ( pipelineStat != null) {
            pipelineStat.gather(size());
        }        
    }


    /**
     * Return a <code>Task</code> object available in the pipeline.
     * 
     */
    public Task getTask() {
        return null;
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
     * Returns the number of tasks in this <code>Pipeline</code>.
     *
     * @return Number of tasks in this <code>Pipeline</code>.
     */
    public int size() {
        return workerThreads.getQueue().size();
    }

    
    /**
     * Interrupt the <code>Thread</code> using it thread id
     */
    public boolean interruptThread(long threadID){
        return ((GrizzlyThreadFactory)workerThreads.getThreadFactory())
            .interruptThread(threadID);
    }  
    // --------------------------------------------------Properties ----------//

    /**
     * Return the number of waiting threads.
     */
    public int getWaitingThread(){
        return workerThreads.getPoolSize() - workerThreads.getActiveCount();
    }
    
    
    /** 
     * Set the number of threads used by this pipeline.
     */
    public void setMaxThreads(int maxThreads){
        this.maxThreads = maxThreads;
    }    
    
    
    /** 
     * Return the number of threads used by this pipeline.
     */
    public int getMaxThreads(){
        return maxThreads;
    }
    
    
    /**
     * Return the current number of threads used.
     */
    public int getCurrentThreadCount() {
        return workerThreads.getPoolSize() ;
    }
      
      
    /**
     * Return the curent number of threads that are currently processing 
     * a task.
     */
    public int getCurrentThreadsBusy(){
        return workerThreads.getActiveCount();
    }
    
    
    /**
     * Return the maximum spare thread.
     */
    public int getMaxSpareThreads() {
        return getWaitingThread();
    }
    
    
    /**
     * Set the thread priority of the <code>Pipeline</code>
     */
    public void setPriority(int priority){
        this.priority = priority;
    }
    
    
    /**
     * Set the name of this <code>Pipeline</code>
     */
    public void setName(String name){
        this.name = name;
    }
    
    
    /**
     * Return the name of this <code>Pipeline</code>
     * @return the name of this <code>Pipeline</code>
     */
    public String getName(){
        return name+port;
    }    

    
    /**
     * Set the port used by this <code>Pipeline</code>
     * @param port the port used by this <code>Pipeline</code>
     */
    public void setPort(int port){
        this.port = port;
    }
    
    
    /**
     * Set the minimum thread this <code>Pipeline</code> will creates
     * when initializing.
     * @param minThreads the minimum number of threads.
     */
    public void setMinThreads(int minThreads){
        this.minThreads = minThreads;
    }
    
    
     /**
     * Set the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public void setQueueSizeInBytes(int maxQueueSizeInBytes){
        this.maxQueueSizeInBytes = maxQueueSizeInBytes;
        if ( pipelineStat != null )
            pipelineStat.setQueueSizeInBytes(maxQueueSizeInBytes);
    }
    
    
    /**
     * Get the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public int getQueueSizeInBytes(){
        return maxQueueSizeInBytes;
    }  
    
    
    public String toString(){
       return "name: " + name + " maxThreads: " + maxThreads 
                + " minThreads:" + minThreads;        
    }


    /**
     * When the <code>maxQueueSizeInBytesConnection</code> is reached, 
     * terminate <code>Task</code>
     */
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor){
        Task task = (Task)r;
        task.cancelTask("Maximum Connections Reached -- Retry later", 
                        HtmlHelper.OK);
        task.getSelectorThread().returnTask(task);
    }
    
    
    public void setThreadsIncrement(int threadsIncrement){
        ; // Not Supported
    }
    
    
    public void setThreadsTimeout(int threadsTimeout){
        ; // Not Supported
    }


     /**
     * Return the minimum spare thread.
     */
    public int getMinSpareThreads() {
        return 0;
    }


    /**
     * Set the minimum space thread this <code>Pipeline</code> can handle.
     */
    public void setMinSpareThreads(int minSpareThreads) {
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
