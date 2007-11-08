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

/**
 * An interface used as a wrapper around any kind of thread pool
 *
 * @author Jean-Francois Arcand
 */
public interface Pipeline {


    /**
     * Invoked when the SelectorThread is about to expire a SelectionKey.
     * @return true if the SelectorThread should expire the SelectionKey, false
     *              if not.
     */
    public boolean expireKey(SelectionKey key);
    
    
    /**
     * Add an <code>Task</code> to be processed by this <code>Pipeline</code>
     */
    public void addTask(Task task) ;


    /**
     * Return a <code>Task</code> object available in the pipeline.
     */
    public Task getTask() ;
    
   
   /**
     * Return the number of waiting threads.
     */
    public int getWaitingThread();
    
    
    /** 
     * Return the number of threads used by this pipeline.
     */
    public int getMaxThreads();
    
    
    /**
     * Return the number of active threads.
     */
    public int getCurrentThreadCount() ;
      
      
    /**
     * Return the curent number of threads that are currently processing 
     * a task.
     */
    public int getCurrentThreadsBusy();
    
   
    /**
     * Init the <code>Pipeline</code> by initializing the required
     * <code>WorkerThread</code>. Default value is 10
     */
    public void initPipeline();


    /**
     * Return the name of this <code>Pipeline</code>
     */
    public String getName();


    /**
     * Start the <code>Pipeline</code>
     */
    public void startPipeline();
    

    /**
     * Stop the <code>Pipeline</code> 
     */
    public void stopPipeline();

    
    /**
     * Set the <code>Thread</code> priority used when creating new threads.
     */
    public void setPriority(int priority);
    
    
    /**
     * Set the maximum thread this pipeline can handle.
     */
    public void setMaxThreads(int maxThread);
    
    
    /**
     * Set the minimum thread this pipeline can handle.
     */    
    public void setMinThreads(int minThread);
    
    
    /**
     * Set the port this <code>Pipeline</code> is associated with.
     */
    public void setPort(int port);
    
    
    /**
     * Set the name of this <code>Pipeline</code>
     */
    public void setName(String name);
   
    
    /**
     * Set the maximum pending connection this <code>Pipeline</code>
     * can handle.
     */
    public void setQueueSizeInBytes(int maxQueueSizeInBytesCount);
    
   
    /**
     * Set the number the <code>Pipeline</code> will use when increasing the 
     * thread pool
     */
    public void setThreadsIncrement(int processorThreadsIncrement);
    
    
    /**
     * Set the timeout value a thread will use to times out the request.
     */
    public void setThreadsTimeout(int processorThreadsTimeout);
    

    /**
     * Set the <code>PipelineStatistic</code> object used
     * to gather statistic;
     */
    public void setPipelineStatistic(PipelineStatistic pipelineStatistic);
    
    
    /**
     * Return the <code>PipelineStatistic</code> object used
     * to gather statistic;
     */
    public PipelineStatistic getPipelineStatistic();


    /**
     * Returns the number of tasks in this <code>Pipeline</code>.
     *
     * @return Number of tasks in this <code>Pipeline</code>.
     */
    public int size();


    // ------------------- Not used, compatibility with 8.1 --------------/
    /**
     * Return the number of maximum spare thread.
     */
    public int getMaxSpareThreads();

    /**
     * Return the number of minimum spare thread.
     */
    public int getMinSpareThreads();


    /**
     * Set the number of minimum spare thread.
     */
    public void setMinSpareThreads(int minSpareThreads);
    
    
    /**
     * Interrup the <code>Thread</code> using it thread id
     */
    public boolean interruptThread(long threadId);
}
