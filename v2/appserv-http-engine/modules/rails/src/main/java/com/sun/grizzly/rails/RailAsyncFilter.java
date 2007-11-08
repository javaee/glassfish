/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.grizzly.rails;

import com.sun.enterprise.web.connector.grizzly.AsyncExecutor;
import com.sun.enterprise.web.connector.grizzly.AsyncFilter;
import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.async.AsyncProcessorTask;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * AyncFilter that park the request if the number of Ruby runtime are all in use.
 *
 * @author Jeanfrancois Arcand
 */
public class RailAsyncFilter implements AsyncFilter {
    
    private BlockingQueue rubyRuntimeQueue;
    
    private LinkedBlockingQueue<AsyncProcessorTask> parkedRequest 
            = new LinkedBlockingQueue<AsyncProcessorTask>();
    
    public RailAsyncFilter() {
    }
    
    
    public boolean doFilter(AsyncExecutor asyncExecutor) {
        AsyncProcessorTask apt = (AsyncProcessorTask)asyncExecutor.getAsyncTask();
        
        if (rubyRuntimeQueue.size() == 0){
            parkedRequest.offer(apt);
            return false;
        } else {
            try{
                apt.getProcessorTask().invokeAdapter();
            } catch (IllegalStateException ex){
                parkedRequest.offer(apt);
                return false;
            }
            apt.setStage(AsyncTask.POST_EXECUTE);
            return true;
        }        
    }
    
    
    protected void setRubyRuntimeQueue(BlockingQueue rubyRuntimeQueue){
        this.rubyRuntimeQueue = rubyRuntimeQueue;        
    }
    
    
    protected void unpark(){        
        AsyncProcessorTask apt = parkedRequest.poll();
        if (apt == null) return;
        
        apt.setStage(AsyncTask.EXECUTE);
        try{
            apt.doTask();
        } catch (IllegalStateException e) {
            // Runtime was zero, add the token back to the queue.
             parkedRequest.offer(apt);
        } catch (IOException ex){
            ; //
        }          
    }

}
