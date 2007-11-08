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

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.coyote.RequestGroupInfo;

/**
 * Abstract implementation of a <code>Task</code> object.
 *
 * @author Jean-Francois Arcand
 */
public abstract class TaskBase implements Task, TaskListener{

    
    /**
     * This number represent a specific implementation of a <code>Task</code>
     * instance.
     */
    protected int type;
    

    /**
     * List of listeners
     */
    protected ArrayList<TaskListener> listeners;
      
    
    /**
     * The <code>Pipeline</code> object associated with this
     * <code>Task</code>
     */
    protected Pipeline pipeline;
    
    
    /**
     * The <code>SelectionKey</code> used by this task.
     */
    protected SelectionKey key;
    
    
    /**
     * Recycle this task
     */
    protected boolean recycle = true;
    
    
    /**
     * The <code>SelectorThread</code> who created this task.
     */
    protected SelectorThread selectorThread;


    // ------------------------------------------------------------------//
    
    public int getType(){
        return type;
    }

    
    /**
     * Set the <code>SelectorThread</code> object.
     */
    public void setSelectorThread(SelectorThread selectorThread){
        this.selectorThread = selectorThread;
    }
    
    
    /**
     * Return the <code>SelectorThread</code>
     */
    public SelectorThread getSelectorThread(){
        return selectorThread;
    }
    
    
    /**
     * Set the pipeline on which Worker Threads will synchronize.
     */
    public void setPipeline(Pipeline pipeline){
        this.pipeline = pipeline;
    }
    
    
    /**
     * Return the pipeline used by this object.
     */
    public Pipeline getPipeline(){
        return pipeline;
    }
   

    /**
     * Set the <code>SelectionKey</code>
     */
    public void setSelectionKey(SelectionKey key){
        this.key = key;
    }


    /**
     * Return the <code>SelectionKey</code> associated with this task.
     */
    public SelectionKey getSelectionKey(){
        return key;
    }


    /**
     * Gets the <code>RequestGroupInfo</code> from this task.
     */    
    public RequestGroupInfo getRequestGroupInfo() {
        return (selectorThread != null?
                selectorThread.getRequestGroupInfo() : null);
    }


    /**
     * Returns <code>true</code> if monitoring has been enabled, false
     * otherwise.
     */
    public boolean isMonitoringEnabled(){
        return (selectorThread != null ?
                selectorThread.isMonitoringEnabled() : false);
    }


    /**
     * Gets the <code>KeepAliveStats</code> associated with this task.
     */
    public KeepAliveStats getKeepAliveStats() {
        return (selectorThread != null?
                selectorThread.getKeepAliveStats() : null);
    }


    /**
     * Execute the task based on its <code>Pipeline</code>. If the 
     * <code>Pipeline</code> is null, then execute the task on using the 
     * calling thread.
     */
    public void execute(){
        if (pipeline != null){
            pipeline.addTask(this);
        } else {
            run();
        }
    }
    
    
    //------------------------------------------------------Task Listener ----//

    private void initListener(){
        if ( listeners == null ){
           listeners  = new ArrayList<TaskListener>();
        }
    }
    
    
    /**
     * Add the given <code>TaskListener</code> to this <code>Task</code>.
     */
    public void addTaskListener(TaskListener task){
        initListener();
        listeners.add(task);
    }

    
    /**
     *  Remove the given <code>TaskListener/code> from this
     * <code>Task</code>.
     */
    public void removeTaskListener(TaskListener task){
        if (listeners == null) return;
        listeners.remove(task);
    }
    
    
    /**
     * Clean all the listeners of this <code>Task</code>
     */
    public void clearTaskListeners(){
        if (listeners == null) return;
        listeners.clear();
    }
    
    
    /**
     * Notify listeners.
     */
    protected void fireTaskEvent(TaskEvent<?> event){
        if (listeners == null) return;        
        for (int i=0; i < listeners.size(); i++){
            listeners.get(i).taskEvent(event);
        }
    }
    
    
    /**
     * Recycle internal state.
     */
    public void recycle(){
       ;
    }
    
    
    /**
     * Return all listeners of this <code>Task</code>.
     *
     * @return ArrayList containing all <code>TaskListener</code>
     * instances registered with this <code>Task</code>
     */
    public ArrayList getTaskListeners(){
        initListener();
        return listeners;
    }
    

    /**
     * Some <code>Pipeline</code> implementation requires a instance of
     * <code>Runnable</code> instance.
     */
    public void run(){
        try{
            doTask();
        } catch (IOException ex){
            throw new RuntimeException(ex);
        }
    }
    

    /**
     * Declare whether this <code>Task</code> is recyclable. If so, this
     * <code>Task</code> will be recycled after every invocation of
     * <code>doTask()</code>.
     */
    public void setRecycle(boolean recycle){
        this.recycle = recycle;
    }
   
    
    /**
     * Return <code>true</code> if this <code>Task</code> is recyclable.
     */
    public boolean getRecycle(){
        return recycle;
    }


    /**
     * Return the current <code>Socket</code> used by this instance
     * @return socket the current <code>Socket</code> used by this instance
     */
    public Socket getSocket(){
        return null;
    }


    /**
     * Return the underlying <code>Channel</code>, independent of the NIO
     * mode we are using.
     */
    private SocketChannel getChannel(){
        if ( key == null ) {
            return getSocket().getChannel();
        } else {
            return (SocketChannel)key.channel();
        }
    }
     

    /**
     * Cancel the task.
     * @param message the HTTP message to included within the html page
     * @param code The http code to use. If null, automatically close the 
     *             connection without sending an error page.
     */
    public void cancelTask(String message, String code){
        SocketChannel channel = getChannel(); 

        if (code != null) {
            SelectorThread.logger().log(Level.WARNING,message);
            try {
                ByteBuffer byteBuffer = HtmlHelper.getErrorPage(message, code);
                OutputWriter.flushChannel(channel,byteBuffer);
            } catch (IOException ex){
                SelectorThread.logger().log(Level.FINE,"CancelTask failed", ex);
            }
        }
        
        if ( selectorThread.isEnableNioLogging() ){
            SelectorThread.logger().log(Level.INFO, "Cancelling SocketChannel " 
                    + getChannel());
        }
        
        if ( key != null){
            selectorThread.cancelKey(key);
        } else if ( getSocket() != null ){
            try{
                getSocket().close();
            } catch (IOException ex){
                ;
            }
        }
    }
    
    
    /**
     * By default, do nothing when a <code>Callable</code> is invoked.
     */
    public Object call() throws Exception{
       return null;
    }


    public void taskEvent(TaskEvent event){
        // Do nothing
    }
 
}
