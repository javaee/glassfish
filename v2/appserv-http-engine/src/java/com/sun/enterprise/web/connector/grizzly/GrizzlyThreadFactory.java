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

import java.util.concurrent.ThreadFactory;

/**
 * Customized <code>ThreadFactory</code> used by the <code>Pipeline</code>
 * instance.
 *
 * @author Jean-Francois Arcand
 */
public class GrizzlyThreadFactory implements ThreadFactory{

    /**
     * The name used when creating threads
     */
    protected String name;
    
    /**
     * The port used when created threads' name.
     */
    protected int port;
    
    /**
     * The number of created threads.
     */
    protected int threadCount;
    
    
    /**
     * The priority used when creating threads.
     */
    protected int priority;

    
    /**
     * The <code>ThreadGroup</code> used.
     */
    private final static ThreadGroup threadGroup = new ThreadGroup("Grizzly");
    
    /**
     * Create an instance of <code>ThreadFactory</code>
     * @param name the name of thread who will be created by this factory
     * @param port the port of thread who will be created by this factory
     * @param priority the priority of thread who will be created by this factory 
     */
    public GrizzlyThreadFactory(String name, int port,int priority){
        this.name = name;
        this.port = port;
        this.priority = priority;
    }


    /**
     * Create a new thread.
     * @param r an instance of a <code>Task</code>.
     * @return a new Thread.
     */
    public Thread newThread(Runnable r){
        WorkerThreadImpl t = new WorkerThreadImpl(threadGroup,r);
        t.setName(name + "WorkerThread-"  + port + "-" + threadCount);
        t.setPriority(priority);
        t.setDaemon(true);
      
        threadCount++;
        return t;
    }

    
    /**
     * Return the <code>ThreadGroup</code> used by this factory
     */
    public ThreadGroup getThreadGroup(){
        return threadGroup;
    }
    
    
    /**
     * Interrupt the <code>Thread</code> using it thread id
     */
    public boolean interruptThread(long threadID){
        Thread[] threads = new Thread[threadGroup.activeCount()];
        threadGroup.enumerate(threads);
               
        for (Thread thread: threads){
            if ( thread != null && thread.getId() == threadID ){                
                if ( Thread.State.RUNNABLE != thread.getState()){
                    try{
                        thread.interrupt();
                        return true;
                    } catch (Throwable t){
                        ; // Swallow any exceptions.
                    }
                }
            }
        }
        return false;
    }
}
