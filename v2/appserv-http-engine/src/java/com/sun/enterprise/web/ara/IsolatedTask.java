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

import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.ReadTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.SelectorThreadConfig;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.Task;
import com.sun.enterprise.web.connector.grizzly.TaskEvent;
import com.sun.enterprise.web.connector.grizzly.TaskListener;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * This task is used to configure an instance of <code>.ReadTask</code> based
 * on the <code>Rule</code> implementation.
 *
 * @author Jeanfrancois Arcand
 */
public class IsolatedTask extends TaskWrapper implements TaskListener{

    public final static int ISOLATED_TASK = 4;
             
    /**
     * The algorithm used to determine the context-root, the HTTP method,
     * the protocol etc.
     */
    protected StreamAlgorithm algorithm;
    
    
    /**
     * The <code>RuleExecutor</code> used to apply <code>Rule</code>
     */
    protected RulesExecutor rulesExecutor;
    
   
    /**
     * List of listeners
     */
    protected ArrayList<TaskListener> listeners = new ArrayList<TaskListener>();

    
    /**
     * The <code>ByteBuffer</code> initial position before applying the 
     * <code>Algorithm</code>
     */
    protected int initialBytePosition;
    
    
    /**
     * The <code>ByteBuffer</code> initial limit before applying the 
     * <code>Algorithm</code>
     */    
    protected int initialByteLimit;
    
    
    /**
     * The <code>TaskEvent</code> used between this task and it's attached
     * <code>ReadTask</code>
     */
    protected TaskEvent<IsolatedTask> taskEvent;
    

    /**
     * The Thread Pool wrapper.
     */
    protected Pipeline pipeline;    
    
    
    /**
     * Cache the <code>SelectionKey</code> to avoid parsing the
     * requests bytes more than once.
     */
    private static ConcurrentHashMap<SelectionKey,Pipeline> cacheKey = 
            new ConcurrentHashMap<SelectionKey,Pipeline>();
    
    
    public IsolatedTask(){
        taskEvent = new TaskEvent<IsolatedTask>();
        taskEvent.attach(this);      
        taskEvent.setStatus(TaskEvent.COMPLETED);
    }
    
    
    /**
     * Apply a set of <code>Rule</code>s to the current bytes requests using
     * an instance of <code>ReadTask</code> byte buffer. Once the 
     * <code>Rule</code> has been successfully applied, execute it.
     */
    public void doTask() throws IOException {   
        try {
            ReadTask readTask = (ReadTask)wrappedTask;
            ByteBuffer byteBuffer = readTask.getByteBuffer();     

            SocketChannel socketChannel = 
                        (SocketChannel)readTask.getSelectionKey().channel();
            Socket socket = socketChannel.socket();
           
            socketChannel.read(byteBuffer);
        
            int position = byteBuffer.position();
            int limit = byteBuffer.limit();

            // If we weren't able to parse the token, return to the 
            // SelectorThread
            boolean execute = false;
            
            if (algorithm.parse(byteBuffer)) {
                execute = rulesExecutor.execute(this);
                if ( execute ){                       
                    // Tell the ReadTask to not load bytes and re-use the one
                    // already loaded.
                    readTask.setBytesAvailable(true);
                    byteBuffer.limit(limit);
                    byteBuffer.position(position);

                    // Get notification once the task has completed.
                    readTask.addTaskListener(this);
                    readTask.execute();
                } else {
                    fireTaskEvent(taskEvent);
                }   
            } else {
                // Failed to read the URI. Close the connections.
                readTask.terminate(false);
                fireTaskEvent(taskEvent);
            }
        } catch (Exception ex){
            SelectorThread.logger()
                .log(Level.SEVERE,"IsolatedTask logic exception.",ex);
        }  
    }
        
    
    /**
     * Set the <code>RuleExecutor</code> instance used by this task.
     */
    public void setRulesExecutor(RulesExecutor rulesExecutor){
        this.rulesExecutor = rulesExecutor;
    }
    
    /**
     * Set the <code>Algorithm</code> used by this task.
     */
    public void setAlgorithm(StreamAlgorithm algorithm){
        this.algorithm = algorithm;
    }
    
    
    /**
     * Wrao the <code>Task</code> with this task.
     */
    public IsolatedTask wrap(Task task){
        wrappedTask = task;
        return this;
    }   
     
    
    // ------------------------------------------------------ Execution -----//
    
    
    /**
     * Execute that task using the current Thread.
     */
    public void execute(){
        run();
    }
    
    
    /**
     * Execute the logic required to isolate the task.
     */
    public void run(){
        try{
            doTask();
        } catch (IOException ex){
            throw new RuntimeException(ex);
        };
    }
    
    // ----------------------------------------------------- Task Listener ----/
    
    
     /**
     * Add the given <code>TaskListener</code> to this <code>Task</code>.
     */
    public void addTaskListener(TaskListener task){
        listeners.add(task);
    }

    
    /**
     *  Remove the given <code>TaskListener/code> from this
     * <code>Task</code>.
     */
    public void removeTaskListener(TaskListener task){
        listeners.remove(task);
    }
    
    
    /**
     * Clean all the listeners of this <code>Task</code>
     */
    public void clearTaskListeners(){
        listeners.clear();
    }

    
    /**
     * Notify listeners of that class that the processing has completed.
     */
    protected void fireTaskEvent(TaskEvent<?> event){
        for (int i=0; i < listeners.size(); i++){
            listeners.get(i).taskEvent(event);
        }
    }

    
    /**
     * Remove the <code>SelectionKey</code> from the cache.
     */
    public void taskEvent(TaskEvent event) {
        wrappedTask = null;
        fireTaskEvent(taskEvent);
        ((ReadTask)event.attachement()).setPipeline(pipeline);      
    }

    
    /**
     * This task type.
     */
    public int getType(){
        return ISOLATED_TASK;
    }

    
    /**
     * Set the <code>Thread</code> pool wrapper.
     */
    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    
    /**
     * Get the <code>Thread</code> pool wrapper.
     */
    public Pipeline getPipeline() {
        return pipeline;
    }
    
    
}
