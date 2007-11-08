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
package com.sun.enterprise.web.connector.grizzly.async;

import com.sun.enterprise.web.connector.grizzly.AsyncExecutor;
import com.sun.enterprise.web.connector.grizzly.AsyncFilter;
import com.sun.enterprise.web.connector.grizzly.AsyncHandler;
import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.Task;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Default implementation of <code>AsyncHandler</code>. This class handle 
 * the aysnchronous execution of a <code>ProcessorTask</code>. The request
 * processing is executed by doing:
 *
 * (1) Wrap the <code>ProcessorTask</code> using an instance of 
 *     <code>AsyncTask</code>
 * (2) Execute the <code>AsyncTask</code> using the wrapped
 *     <code>ProcessorTask</code> <code>Pipeline</code>
 * (3) If the <code>AsyncTask</code> has been interrupted but ready
 *     to be removed from the interrupted queue, remove it and execute the
 *     remaining operations.
 *
 * @author Jeanfrancois Arcand
 */
public class DefaultAsyncHandler implements AsyncHandler{
    
    /**
     * Cache instance of <code>AsyncTask</code>
     */
    private ConcurrentLinkedQueue<AsyncTask>
            asyncProcessors = new ConcurrentLinkedQueue<AsyncTask>();
    
    
    /**
     * A queue used to cache interrupted <code>AsyncTask</code>.
     */
    private ConcurrentLinkedQueue<AsyncTask>
            interrruptedQueue = new ConcurrentLinkedQueue<AsyncTask>();  
               
    
    /**
     * The <code>AsyncFilter</code> to execute asynchronous operations on 
     * a <code>ProcessorTask</code>.
     */
    private ArrayList<AsyncFilter> asyncFilters = 
            new ArrayList<AsyncFilter>();   
    
    
    /**
     * The <code>AsyncExecutor</code> class name to use.
     */ 
    private String asyncExecutorClassName 
        = "com.sun.enterprise.web.connector.grizzly.async.DefaultAsyncExecutor";
    // ------------------------------------------------- Constructor --------//
    
    
    public DefaultAsyncHandler() {
    }
    
    
    /**
     * Create an instance of <code>AsyncTask</code>
     */
    private AsyncTask newAsyncProcessorTask(){
        AsyncTask asyncTask = new AsyncProcessorTask();
        asyncTask.setAsyncExecutor(newAsyncExecutor(asyncTask));  
        return asyncTask;
    }
    
    
    /**
     * Create an instance of <code>DefaultAsyncExecutor</code>
     */    
    private AsyncExecutor newAsyncExecutor(AsyncTask asyncTask){
        
        Class className = null; 
        AsyncExecutor asyncExecutor = null;
        try{                              
            className = Class.forName(asyncExecutorClassName);
            asyncExecutor = (AsyncExecutor)className.newInstance();
        } catch (ClassNotFoundException ex){
            throw new RuntimeException(ex);
        } catch (InstantiationException ex){
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex){
            throw new RuntimeException(ex);
        }
        
        if ( asyncExecutor != null ){
            asyncExecutor.setAsyncTask(asyncTask);
            asyncExecutor.setAsyncHandler(this);
            
            for (AsyncFilter l : asyncFilters){
                asyncExecutor.addAsyncFilter(l);
            }
        }
        return asyncExecutor;
    }
    
    
    /**
     * Return an instance of <code>AsyncTask</code>, which is 
     * configured and ready to be executed.
     */
    private AsyncTask getAsyncProcessorTask(){
        AsyncTask asyncTask = asyncProcessors.poll();
        if ( asyncTask == null) {
            asyncTask = newAsyncProcessorTask();
        } else {
            asyncTask.recycle();
        }
        return asyncTask;
    }
    
    
    // ---------------------------------------------------- Interface -------//
    
    
    /**
     * Handle an instance of a <code>Task</code>. This method is invoked
     * first by a <code>ProcessorTask</code>, which delegate its execution to 
     * this handler. Second, this method is invoked once a 
     * <code>ProcessorTask</code> needs to be removed from the interrupted queue.
     */
    public void handle(Task task){
        
        AsyncTask apt = null;
        if (task.getType() == Task.PROCESSOR_TASK) {
            apt = getAsyncProcessorTask();
            apt.setProcessorTask((ProcessorTask)task);
            apt.setSelectorThread(task.getSelectorThread());
        }
        
        boolean wasInterrupted = interrruptedQueue.remove(task);
        if ( !wasInterrupted && apt == null) {
            String errorMsg = "";
            if ( task.getSelectionKey() != null ) {
                errorMsg = "Connection " + task.getSelectionKey().channel()
                            + " wasn't interrupted";
            } 
            throw new IllegalStateException(errorMsg);
        } else if ( apt == null ){
            apt = (AsyncTask)task;
        }
        apt.execute();
    }
    

    /**
     * Return th <code>Task</code> to the pool
     */
    public void returnTask(AsyncTask asyncTask){
        asyncProcessors.offer(asyncTask);
    }
    
    /**
     * Add a <code>Task</code> to the interrupted queue.
     */
    public void addToInterruptedQueue(AsyncTask task){
        interrruptedQueue.offer(task);
    }
    
    
    /**
     * Remove the <code>Task</code> from the interrupted queue.
     */
    public void removeFromInterruptedQueue(AsyncTask task){
        interrruptedQueue.remove(task);
    }
    
    
    /**
     * Set the <code>AsyncExecutor</code> used by this object.
     */
    public void setAsyncExecutorClassName(String asyncExecutorClassName){
        this.asyncExecutorClassName = asyncExecutorClassName;
    }
    
    
    /**
     * Get the code>AsyncExecutor</code> used by this object.
     */
    public String getAsyncExecutorClassName(){
        return asyncExecutorClassName;
    }
    
    
    /**
     * Add an <code>AsyncFilter</code>
     */
    public void addAsyncFilter(AsyncFilter asyncFilter) {
        asyncFilters.add(asyncFilter);
    }

    
    /**
     * Remove an <code>AsyncFilter</code>
     */
    public boolean removeAsyncFilter(AsyncFilter asyncFilter) {
        return asyncFilters.remove(asyncFilter);
    }
}
