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

import com.sun.enterprise.web.connector.grizzly.KeepAliveStats;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.Task;
import com.sun.enterprise.web.connector.grizzly.TaskListener;

import org.apache.coyote.RequestGroupInfo;

/**
 * Wrap an instance of a <code>Task</code>
 *
 * @author Jeanfrancois Arcand
 */
public abstract class TaskWrapper implements Task {
       
    /**
     * The wrapped <code>Task</code> instance.
     */
    protected Task wrappedTask;
    
       
    public TaskWrapper(){}

    
    // ---------------------------------------------------- Abstract method --//
    
    
    /**
     * Execute the <code>Task</code>
     */
    public abstract void doTask() throws java.io.IOException;
   
    
    /**
     * Wrap an instance of a <code>Task</code>
     */
    public abstract Task wrap(Task task);
    
    
    /**
     * Execute the <code>Task<</code> using this thread or a <code>Pipeline</code>.
     */
    public abstract void execute();

    
    /**
     * Execute the <code>Task<</code> using this thread or a <code>Pipeline</code>.
     */
    public abstract void run();
      
    
    /**
     * Return the wrapped <code>Task</code>
     */
    public Task getWrappedTask(){
        return wrappedTask;
    }
    // -------------------------------------------------- Wrapped methods ---//
    
    
    public void addTaskListener(TaskListener task) {
        wrappedTask.addTaskListener(task);
    }

    public Object call() throws Exception{
        return wrappedTask.call();
    }

    public void cancelTask(String message, String httpCode) {
        wrappedTask.cancelTask(message,httpCode);
    }

    public void clearTaskListeners() {
        wrappedTask.clearTaskListeners();
    }


    public KeepAliveStats getKeepAliveStats() {
        return wrappedTask.getKeepAliveStats();
    }

    public boolean getRecycle() {
        return wrappedTask.getRecycle();
    }

    public RequestGroupInfo getRequestGroupInfo() {
        return wrappedTask.getRequestGroupInfo();
    }

    public java.nio.channels.SelectionKey getSelectionKey() {
        return wrappedTask.getSelectionKey();
    }

    public SelectorThread getSelectorThread() {
        return wrappedTask.getSelectorThread();
    }

    public java.util.ArrayList getTaskListeners() {
        return wrappedTask.getTaskListeners();
    }

    public int getType() {
        return wrappedTask.getType();
    }

    public boolean isMonitoringEnabled() {
        return wrappedTask.isMonitoringEnabled();
    }

    public void recycle() {
        wrappedTask.recycle();
    }

    public void removeTaskListener(TaskListener task) {
        wrappedTask.removeTaskListener(task);
    }


    public void setRecycle(boolean recycle) {
        wrappedTask.setRecycle(recycle);
    }

    public void setSelectionKey(java.nio.channels.SelectionKey key) {
        wrappedTask.setSelectionKey(key);
    }

    public void setSelectorThread(SelectorThread selectorThread) {
        wrappedTask.setSelectorThread(selectorThread);
    }
    
}
