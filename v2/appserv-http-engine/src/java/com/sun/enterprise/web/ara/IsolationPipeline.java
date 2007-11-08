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
package com.sun.enterprise.web.ara;


import com.sun.enterprise.web.connector.grizzly.LinkedListPipeline;
import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.Task;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.TaskListener;
import com.sun.enterprise.web.ara.algorithms.ContextRootAlgorithm;
import com.sun.enterprise.web.ara.rules.ThreadRatioRule;

import java.util.ArrayList;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.List;


/**
 * Customized <code>Pipeline</code> which wrap the original <code>Task</code>
 * instance with an instance of <code>IsolatedTask</code>
 *
 * @author Jeanfrancois Arcand
 */
public class IsolationPipeline extends LinkedListPipeline 
        implements TaskListener{
    
    private final static String ALGORITHM_CLASS = 
         "com.sun.enterprise.web.ara.isolationPipeline.algorithm";
    private final static String RULE_EXECUTOR_CLASS = 
         "com.sun.enterprise.web.ara.isolationPipeline.ruleExecutor";
            
            
    /**
     * Cache instance of <code>IsolatedTask</code>
     */
    private ConcurrentLinkedQueue<IsolatedTask> isolatedTasks;
    
      
    // ------------------------------------------------------ Constructor ---/
    
    
    public IsolationPipeline(){
    }
    
    
    /**
     * Initialize this pipeline by first initializing its parent, and then by
     * creating the caches and the rule executor engine.
     */
    public void initPipeline(){
        // 1. first, init this pipeline.
        super.initPipeline();
        
        // 2. Create cache
        isolatedTasks = new ConcurrentLinkedQueue<IsolatedTask>();
        
        // 3. Cache IsolatedTask
        for (int i=0; i < maxThreads; i++){
            isolatedTasks.offer(newIsolatedTask());
        }  
    }
    
    
    /**
     * Execute the wrapped <code>Task</code>
     */
    public void addTask(Task task) {    
        // SSL not yet supported.
        if (task.getType() == Task.READ_TASK){
            super.addTask(wrap(task));
        } else {
            super.addTask(task);
        }
    }
    

    /**
     * Wrap the current <code>Task</code> using an <code>IsolatedTask</code>
     */
    private Task wrap(Task task){
        IsolatedTask isolatedTask = isolatedTasks.poll();
        if ( isolatedTask == null){
            isolatedTask = newIsolatedTask();
        }
        isolatedTask.wrap(task);
        return isolatedTask;
    }

    
    /**
     * Create a new <code>IsolatedTask</code>
     */
    private IsolatedTask newIsolatedTask(){
        IsolatedTask task = new IsolatedTask();
        
        task.setAlgorithm(newAlgorithm());
        task.setRulesExecutor(newRulesExecutor());
        task.addTaskListener(this);
        task.pipeline = this;
        return task;
    }
    
    
    /**
     * Create a new <code>StreamAlgorithm</code>.
     */
    private StreamAlgorithm newAlgorithm(){
        return (StreamAlgorithm)loadInstance(ALGORITHM_CLASS);
    }
    
    
    /**
     * Create the new <code>RulesExecutor</code>
     */
    private RulesExecutor newRulesExecutor(){
        return (IsolationRulesExecutor)loadInstance(RULE_EXECUTOR_CLASS);
    }
    
       
    // ----------------------------------------------- Task Listener ---------//
    
    public void taskStarted(TaskEvent event) {
        ; // Do nothing.
    }

    
    /**
     * Return the <code>IsolatedTask</code> to the pool.
     */
    public void taskEvent(TaskEvent event) {
        if ( event.getStatus() == TaskEvent.COMPLETED)
            isolatedTasks.offer((IsolatedTask)event.attachement());
    }
    
    // ----------------------------------------------- Util ------------------//
    
    /**
     * Instanciate a class based on a property.
     */
    private Object loadInstance(String property){        
        Class className = null;                               
        Pipeline pipeline = null;                               
        try{                              
            className = Class.forName(property);
            return className.newInstance();
        } catch (ClassNotFoundException ex){
        } catch (InstantiationException ex){
        } catch (IllegalAccessException ex){
        }
        
        // Default
        if ( property.equals(ALGORITHM_CLASS)){
            return new ContextRootAlgorithm();
        } else if ( property.equals(RULE_EXECUTOR_CLASS)){
            return new IsolationRulesExecutor();
        }
        throw new IllegalStateException();
    }
    
}
