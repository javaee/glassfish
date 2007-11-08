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
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.apache.coyote.RequestGroupInfo;

/**
 * Wrapper object used by the WorkerThread
 *
 * @author Jean-Francois Arcand
 */
public interface Task extends Runnable, Callable{
      
    // Simple flag to avoid calling instanceof
    public static int ACCEPT_TASK = 0;
    public static int READ_TASK = 1;
    public static int PROCESSOR_TASK = 2;

    /**
     * Return this <code>Tash</code> type.
     */
    public int getType();
    
    /**
     * Execute the task.
     */
    public void doTask() throws IOException;


    /**
     * Cancel the task.
     */
    public void cancelTask(String message, String httpCode);


    /**
     * Set the <code>SelectionKey</code>
     */
    public void setSelectionKey(SelectionKey key);
    
    
    /**
     * Return the <code>SelectionKey</code> associated with this tasks.
     */
    public SelectionKey getSelectionKey();

    
    /**
     * Set the <code>SelectorThread</code> used by this task.
     */
    public void setSelectorThread(SelectorThread selectorThread);
    
    
    /**
     * Returns the <code>SelectorThread</code> used by this task.
     */
    public SelectorThread getSelectorThread();


    /**
     * Gets the <code>RequestGroupInfo</code> from this task.
     */    
    public RequestGroupInfo getRequestGroupInfo();


    /**
     * Returns <code>true</code> if monitoring has been enabled, false
     * otherwise.
     */
    public boolean isMonitoringEnabled();


    /**
     * Gets the <code>KeepAliveStats</code> associated with this task.
     */
    public KeepAliveStats getKeepAliveStats();

    
    /**
     * Add a <code>Task</code> to this class.
     */
    public void addTaskListener(TaskListener task);

    
    /**
     * Remove a <code>Task</code> to this class.
     */
    public void removeTaskListener(TaskListener task);
    
    
    /**
     * Execute this task by using the associated <code>Pipeline</code>.
     * If the <code>Pipeline</code> is null, the task's <code>doTask()</code>
     * method will be invoked.
     */   
    public void execute();
    
    
    /**
     * Recycle this task.
     */
    public void recycle();
    
    
    /**
     * Return the <code>ArrauList</code> containing the listeners.
     */
    public ArrayList getTaskListeners(); 


    /**
     * Remove all listeners
     */
    public void clearTaskListeners();


    /**
     * Recycle the Task after every doTask invokation.
     */
    public void setRecycle(boolean recycle);


    /**
     * Return <code>true</code> if this <code>Task</code> will be recycled.
     */
    public boolean getRecycle();

    
    /**
     * Set the pipeline on which Worker Threads will synchronize.
     */
    public void setPipeline(Pipeline pipeline);
    
    
    /**
     * Return the pipeline used by this object.
     */
    public Pipeline getPipeline();  
}
