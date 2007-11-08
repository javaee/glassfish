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
import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.TaskBase;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import java.util.logging.Level;


/**
 * A <code>Task</code> that wraps the execution of an asynchronous execution
 * of a <code>ProcessorTask</code>. Internaly, this class invoke the associated
 * <code>AsyncExecutor</code> method to execute the <code>ProcessorTask</code>
 * lifecycle operations.
 *
 * @author Jeanfrancois Arcand
 */
public class AsyncProcessorTask extends TaskBase implements AsyncTask {
 
    /**
     * The <code>AsyncExecutor</code> which drive the execution of the 
     * <code>ProcesssorTask</code>
     */
    private AsyncExecutor asyncExecutor;

    
    /**
     * The <code>ProcessorTask</code>
     */
    private ProcessorTask processorTask;
    
    
    /**
     * The current execution stage.
     */
    private int stage = AsyncTask.PRE_EXECUTE;
    
    
    /**
     * Execute the <code>AsyncExecutor</code> based on the <code>stage</code>
     * of the <code>ProcessorTask</code> execution.
     */
    public void doTask() throws java.io.IOException {
        boolean contineExecution = true;
        while ( contineExecution ) {
            try{
                switch(stage){
                    case AsyncTask.PRE_EXECUTE:
                       stage = AsyncTask.INTERRUPTED;                       
                       contineExecution = asyncExecutor.preExecute();
                       break;                
                    case AsyncTask.INTERRUPTED:
                       stage = AsyncTask.POST_EXECUTE;                        
                       contineExecution = asyncExecutor.interrupt();
                       break;  
                    case AsyncTask.EXECUTE:    
                       contineExecution = asyncExecutor.execute();
                       stage = AsyncTask.POST_EXECUTE;
                       break;                           
                    case AsyncTask.POST_EXECUTE:    
                       contineExecution = asyncExecutor.postExecute();
                       stage = AsyncTask.COMPLETED;
                       break;                
                }
            } catch (Throwable t){
                SelectorThread.logger().log(Level.SEVERE,t.getMessage(),t);
                if ( stage <= AsyncTask.INTERRUPTED) {
                    // We must close the connection.
                    stage = AsyncTask.POST_EXECUTE;
                } else {
                    stage = AsyncTask.COMPLETED;
                    throw new RuntimeException(t);
                }
            } finally {
                // If the execution is completed, return this task to the pool.
                if ( stage == AsyncTask.COMPLETED){
                    stage = AsyncTask.PRE_EXECUTE;
                    asyncExecutor.getAsyncHandler().returnTask(this);
                }
            }
        } 
    }

    
    /**
     * Not used.
     */
    public void taskEvent(TaskEvent event) {
    }
    
    /**
     * Return the <code>stage</code> of the current execution.
     */
    public int getStage(){
        return stage;
    }
    
    
    /**
     * Reset the object.
     */
    public void recycle(){
        stage = AsyncTask.PRE_EXECUTE;
        processorTask = null;
    }

    
    /**
     * Set the <code>AsyncExecutor</code> used by this <code>Task</code>
     * to delegate the execution of a <code>ProcessorTask</code>.
     */
    public void setAsyncExecutor(AsyncExecutor asyncExecutor){
        this.asyncExecutor = asyncExecutor;
    }
    
    
    /**
     * Get the <code>AsyncExecutor</code>.
     */
    public AsyncExecutor getAsyncExecutor(){
        return asyncExecutor;
    }
    
    
    /**
     * Set the <code>ProcessorTask</code> that needs to be executed
     * asynchronously.
     */
    public void setProcessorTask(ProcessorTask processorTask){
        this.processorTask = processorTask;
        if ( pipeline == null && processorTask != null) {
            setPipeline(processorTask.getPipeline());
        }        
    }
    
    
    /**
     * Return the <code>ProcessorTask</code>.
     */
    public ProcessorTask getProcessorTask(){
        return processorTask;
    }

    
    /**
     * 
     */
    public void setStage(int stage){
        this.stage = stage;
    }
}
