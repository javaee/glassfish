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

package com.sun.enterprise.web.ara.rules;

import com.sun.enterprise.web.ara.IsolationRulesExecutor;
import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.LinkedListPipeline;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.Task;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.TaskListener;
import com.sun.enterprise.web.connector.grizzly.WorkerThreadImpl;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Based on the application context-root, configure the <code>ReadTask</code>
 * <code>Pipeline</code> based on the policy metric defined in domain.xml. 
 *
 * @author Jeanfrancois Arcand
 */
public class HeapMemoryRule extends ThreadRatioRule implements TaskListener{

    /**
     * The memory allocated per application.
     */
    protected final static ConcurrentHashMap<String,Long> 
            memoryAllowed = new ConcurrentHashMap<String,Long>();

                       
    /**
     * The memory available at startup.
     */ 
    private static long availableMemory = -1L;
    
    
    /**
     * Cache application memory usage approximation.
     */
    protected final static ConcurrentHashMap<String,Long>
            appMemoryUsage = new ConcurrentHashMap<String,Long>();
    
    
    /**
     * Cache the context-root assocated with a <code>ReadTask</code>
     */
    protected final static ConcurrentHashMap<ReadTask, String>
            contextRootyCache = new ConcurrentHashMap<ReadTask,String>();
         
    
    /**
     * A customized <code>Pipeline</code> used to measure the memory used
     * by an application.
     */
    private static HeapMemoryRulePipeline hmrPipeline;

     
    /**
     * The default <code>Pipeline</code> used by application that are
     * allowed to execute.
     */
    private static Pipeline defaultPipeline;
    
    
    /**
     * The consolidated amount of memory an application is using.
     */
    private static ConcurrentHashMap<String,Long>
            consolidatedMemoryUsed = new ConcurrentHashMap<String,Long>();
    
        
    /**
     * Simple lock.
     */
    private Object[] lock = new Object[0];
    // ------------------------------------------------ Pipeline ------------//

    
    
    public Integer call() throws Exception {
        synchronized(lock){
            if (availableMemory == -1L){
                availableMemory = usedMemory();
            }

            if ( hmrPipeline == null ) {
                hmrPipeline = new HeapMemoryRulePipeline();
                hmrPipeline.initPipeline();
                hmrPipeline.startPipeline();
            }
        }
        
        String token = getContextRoot();
        Long memoryAllowedSize = memoryAllowed.get(token);
        Double allowedRatio = privilegedTokens.get(token);
        
        Pipeline pipeline = pipelines.get(token);
        synchronized(lock){
            if ( pipeline == null ){            
                if ( defaultPipeline == null) {
                    defaultPipeline = newPipeline(readTask.getPipeline()
                        .getMaxThreads(),readTask.getPipeline());
                }
                pipelines.put(token,defaultPipeline);
                readTask.setPipeline(defaultPipeline);
            } else {      
                readTask.setPipeline(pipeline); 
            }
        }
        
        // If no policy metry has been defined for this token and there
        // is free memory, allow execution.
        if ( memoryAllowedSize == null && allowedRatio == null){           
            if ( countReservedMemory() <= availableMemory) {
                return IsolationRulesExecutor.RULE_OK_NOCACHE; 
            }
            
            if ( allocationPolicy.equals(RESERVE) )
                return IsolationRulesExecutor.RULE_BLOCKED;
            else if ( allocationPolicy.equals(CEILING) ) 
                return IsolationRulesExecutor.RULE_DELAY;               
        }
        
        boolean isAllowed = 
                isAllowedToExecute(token,memoryAllowedSize,allowedRatio);
        
        pipeline = pipelines.get(token);
        if ( pipeline != null)
            readTask.setPipeline(pipeline); 
        
        if ( !isAllowed ) {
            if ( allocationPolicy.equals(RESERVE) )
                return IsolationRulesExecutor.RULE_BLOCKED;
            else if ( allocationPolicy.equals(CEILING) ) 
                return IsolationRulesExecutor.RULE_DELAY;    
        }
        return IsolationRulesExecutor.RULE_OK_NOCACHE;
    }

    
    /**
     * Determine if an application can execute based on its policy metric and
     * method.
     */
    protected boolean isAllowedToExecute(String token, Long memoryAllowedSize, 
            Double allowedRatio) throws Exception{          
        
        long currentMemory = usedMemory();
       
        // First request, needs to calculate the ratio.
        if ( memoryAllowedSize == null ){
            memoryAllowedSize = 
                    (availableMemory * allowedRatio.longValue())/100;
            // We must find the allowed size based on the ratio.
            memoryAllowed.put(token,memoryAllowedSize);
        } 
        
        if ( memoryAllowedSize > currentMemory) {
            return false;
        }

        // Do we know how much memory is used?
        Long usage = appMemoryUsage.get(token);
        contextRootyCache.put(readTask,token);
        
        // If null, we must execute the task to find how much memory 
        // the app consume.
        if ( usage == null) {
            pipelines.put(token,hmrPipeline);
            return true;
        }
        
        // Make sure we can get all memory consumed by the application
        Long currentAppUsage = consolidatedMemoryUsed.get(token);
        if ( currentAppUsage == null ) {
            currentAppUsage = 0L;
        }
        
        usage = currentAppUsage + usage;
        
        if ( usage > currentMemory ) return false;
        
        if ( usage > memoryAllowedSize ) return false;
            
        consolidatedMemoryUsed.put(token,usage);
        readTask.addTaskListener(this);
        return true;
    }
   
    
    /**
     * Return the total memory required by application which has a policy
     * metric defined.
     */
    private long countReservedMemory(){
        Iterator<Long> iterator = memoryAllowed.values().iterator();
        long count = 0L;
        while (iterator.hasNext()){
            count += iterator.next(); 
        }  
        return count;
    }
    
    
    /**
     * Predict the current memory 
     */
    private static long usedMemory(){
        Runtime.getRuntime().gc();
        return Runtime.getRuntime().totalMemory () 
            - Runtime.getRuntime().freeMemory ();
    }    

    
    /**
     * Reduce the memory usage count when an application complete its execution.
     */
    public void taskEvent(TaskEvent event) {
        if ( event.getStatus() == TaskEvent.COMPLETED){
        
            String token = contextRootyCache.remove(event.attachement());
            if ( token != null ) {
               Long count = consolidatedMemoryUsed.get(token);
               count -= appMemoryUsage.get(token);
               consolidatedMemoryUsed.put(token,count); 
            }
        }
    }

    
    /**
     * Customized <code>Pipeline</code> used to approximate the memory used 
     * by an application.
     */
    private static class HeapMemoryRulePipeline extends LinkedListPipeline{
                
        public void initPipeline(){
            workerThreads = new WorkerThreadImpl[1];
            WorkerThreadImpl workerThread = new WorkerThreadImpl(this, 
                    "HeapMemoryRuleThread"){
                
                public void run(){
                    while (true) {
                        try{
                            ReadTask t = 
                                (ReadTask)HeapMemoryRulePipeline.this.getTask();
                            if ( t != null){
                                long current = usedMemory();
                                t.run();    
                                long usage = usedMemory() - current;
                                if ( usage > 0){
                                    String token = 
                                       contextRootyCache.get(t);
                                    appMemoryUsage.put(token,usage);
                                    pipelines.remove(token);
                                }                                  
                            }
                        } catch (Throwable t) {
                            SelectorThread.logger().log(Level.SEVERE,
                                    "workerThread.httpException",t);
                        }
                    }
                }                              
            };
            workerThread.setPriority(priority);
            workerThreads[0] = workerThread;
            threadCount++; 
        }  
        
        
        /**
         * Start the <code>Pipeline</code> and all associated 
         * <code>WorkerThread</code>
         */
        public void startPipeline(){
            if (!isStarted) {
                workerThreads[0].start();
                isStarted = true;
            }
        }


        /**
         * Stop the <code>Pipeline</code> and all associated
         * <code>WorkerThread</code>
         */
        public void stopPipeline(){
            if (isStarted) {
                workerThreads[0].terminate();
                isStarted = false;
            }
        }    
        
        
        /**
         * Add an object to this pipeline
         */
        public synchronized void addTask(Task task) {
            addLast(task);
            notify();
        }    
    }
}