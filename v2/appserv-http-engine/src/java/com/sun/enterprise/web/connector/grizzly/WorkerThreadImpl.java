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

import java.nio.ByteBuffer;
import java.util.logging.Level;



/**
 * Simple worker thread used for processing HTTP requests. All threads are
 * synchronized using a <code>Pipeline</code> object
 *
 * @author Jean-Francois Arcand
 */
public class WorkerThreadImpl extends Thread implements WorkerThread{

    /**
     * What will be run.
     */
    protected Runnable target;
    
    
    /**
     * The <code>ByteBuffer</code> used when <code>Task</code> are executed.
     */
    protected ByteBuffer byteBuffer;
    
    
    /**
     * The <code>Pipeline</code> on which this thread synchronize.
     */
    protected Pipeline pipeline;

    /**
     * Looing variable.
     */
    protected volatile boolean doTask = true;

    
    /**
     * The <code>ThreadGroup</code> used.
     */
    protected final static ThreadGroup threadGroup = new ThreadGroup("Grizzly");
    
    /** 
     * Create a Thread that will synchronizes/block on 
     * <code>Pipeline</code> instance.
     */
    public WorkerThreadImpl(ThreadGroup threadGroup, Runnable runnable){
        super(threadGroup, runnable);                    
        setDaemon(true);
        target = runnable;
    }    
    
    
    /** 
     * Create a Thread that will synchronizes/block on 
     * <code>Pipeline</code> instance.
     */
    public WorkerThreadImpl(Pipeline pipeline, String name){
        super(threadGroup, name);                    
        this.pipeline = pipeline;
        setDaemon(true);        
    }

    
    /**
     * Execute a <code>Task</code>.
     */
    public void run(){

        if ( target != null ){
            target.run();
            return;
        }
        
        while (doTask) {
            try{
                // Wait for a Task to be added to the pipeline.
                Task t = pipeline.getTask();
                if ( t != null){
                    t.run();                
                    t = null;
                }
            } catch (Throwable t) {
                if ( byteBuffer != null){
                    byteBuffer.clear();
                }
                SelectorThread.logger().log(Level.FINE,
                        "workerThread.httpException",t);
            }
        }
    }
    
    
    /**
     * Stop this thread. If this Thread is performing atask, the task will be
     * completed.
     */
    public void terminate(){
        doTask = false;
    }
    
    
    /**
     * Set the <code>ByteBuffer</code> shared this thread
     */
    public void setByteBuffer(ByteBuffer byteBuffer){
        this.byteBuffer = byteBuffer;
    }
    
    
    /**
     * Return the <code>ByteBuffer</code> shared this thread
     */
    public ByteBuffer getByteBuffer(){
        return byteBuffer;
    }

}

