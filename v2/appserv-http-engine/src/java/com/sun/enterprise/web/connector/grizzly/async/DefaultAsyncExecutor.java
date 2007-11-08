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
import com.sun.enterprise.web.connector.grizzly.AsyncFilter;
import com.sun.enterprise.web.connector.grizzly.AsyncHandler;
import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.ProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;

/**
 * Default implementation of the <code>AsyncExecutor</code>. This class will
 * execute a <code>ProcessorTask</code> asynchronously, by interrupting the 
 * process based on the logic defined in its associated <code>AsyncFilter</code>
 * If no <code>AsyncFilter</code> are defined, the <code>ProcessorTask</code>
 * will not be interrupted and executed synchronously.
 *
 * @author Jeanfrancois Arcand
 */
public class DefaultAsyncExecutor implements AsyncExecutor{

    private final static String ASYNC_FILTER = 
            "com.sun.enterprise.web.connector.grizzly.asyncFilters";

    
    /**
     * The <code>AsyncTask</code> used to wrap the 
     * <code>ProcessorTask</code>
     */
    private AsyncTask asyncProcessorTask;
       
    
    /**
     * The associated <code>ProcessorTask</code>
     */
    private ProcessorTask processorTask;
    
    
    /**
     * The <code>AsyncFilter</code> to execute asynchronous operations on 
     * a <code>ProcessorTask</code>.
     */
    private static String[] sharedAsyncFilters = null;;
    
    
    /**
     * The <code>AsyncFilter</code> to execute asynchronous operations on 
     * a <code>ProcessorTask</code>.
     */
    private ArrayList<AsyncFilter> asyncFilters = 
            new ArrayList<AsyncFilter>();
    
    
    /**
     * Do we need to invoke filters?
     */
    private boolean invokeFilter = true;

    
    /**
     * Loads filters implementation.
     */
    static {
        loadFilters();
    }
    
    
    /**
     * The <code>AsyncHandler</code> associated with this object.
     */
    private AsyncHandler asyncHandler;
    
    // --------------------------------------------------------------------- //
    
    public DefaultAsyncExecutor(){
        init();
    }
    
    
    private void init(){
        if (sharedAsyncFilters != null){
            for (String filterName: sharedAsyncFilters){
                asyncFilters.add(loadInstance(filterName));
            }
        }
    }
    
    // ------------------------------------------------Asynchrounous Execution --/
    
    /**
     * Pre-execute a <code>ProcessorTask</code> by parsing the request 
     * line.
     */
    public boolean preExecute() throws Exception{
        processorTask = asyncProcessorTask.getProcessorTask(); 
        if ( processorTask == null ){
            throw new IllegalStateException("Null ProcessorTask");
        }
        processorTask.preProcess();
        processorTask.parseRequest();
        return true;
    }
    
    
    /**
     * Interrupt the <code>ProcessorTask</code> if <code>AsyncFilter</code>
     * has been defined.
     * @return true if the execution can continue, false if delayed.
     */
    public boolean interrupt() throws Exception{
        if ( asyncFilters == null || asyncFilters.size() == 0 ) {
            execute();
            return false;
        } else {
            asyncHandler.addToInterruptedQueue(asyncProcessorTask); 
            return invokeFilters();
        }
    }
    
    
    /**
     * Interrupt the <code>ProcessorTask</code> if <code>AsyncFilter</code>
     * has been defined.
     * @return true if the execution can continue, false if delayed.
     */
    public boolean execute() throws Exception{
        processorTask.invokeAdapter();
        return true;
    }

    
    /**
     * Invoke the <code>AsyncFilter</code>
     */
    private boolean invokeFilters(){
        boolean continueExec = true;
        for (AsyncFilter asf: asyncFilters){
            continueExec = asf.doFilter(this);
            if ( !continueExec ){
                break;
            }
        }
        return continueExec;
    }
    
    
    /**
     * Post-execute the <code>ProcessorTask</code> by preparing the response,
     * flushing the response and then close or keep-alive the connection.
     */
    public boolean postExecute() throws Exception{
        processorTask.postResponse();
        processorTask.postProcess();        
        processorTask.terminateProcess();
        
        // De-reference so under stress we don't have a simili leak.
        processorTask = null;
        return false;
    }

      
    /**
     * Set the <code>AsyncTask</code>.
     */
    public void setAsyncTask(AsyncTask asyncProcessorTask){
        this.asyncProcessorTask = asyncProcessorTask;
    }
    
    
    /**
     * Return <code>AsyncTask</code>.
     */
    public AsyncTask getAsyncTask(){
        return asyncProcessorTask;
    }
    
    
    // --------------------------------------------------------- Util --------//  
    
    
    /**
     * Load the list of <code>AsynchFilter</code>.
     */
    protected static void loadFilters(){      
        if ( System.getProperty(ASYNC_FILTER) != null){
            StringTokenizer st = new StringTokenizer(
                    System.getProperty(ASYNC_FILTER),",");
            
            sharedAsyncFilters = new String[st.countTokens()];    
            int i = 0;
            while (st.hasMoreTokens()){
                sharedAsyncFilters[i++] = st.nextToken();                
            } 
        }   
    }    
    
    
    /**
     * Instanciate a class based on a property.
     */
    private static AsyncFilter loadInstance(String property){        
        Class className = null;                               
        try{                              
            className = Class.forName(property);
            return (AsyncFilter)className.newInstance();
        } catch (ClassNotFoundException ex){
            SelectorThread.logger().log(Level.WARNING,ex.getMessage(),ex);
        } catch (InstantiationException ex){
            SelectorThread.logger().log(Level.WARNING,ex.getMessage(),ex);            
        } catch (IllegalAccessException ex){
            SelectorThread.logger().log(Level.WARNING,ex.getMessage(),ex);            
        }
        return null;
    }   

    
    /**
     * Add an <code>AsyncFilter</code>
     */
    public void addAsyncFilter(AsyncFilter asyncFilter) {
        asyncFilters.add(asyncFilter);
    }

    
    /**
     * Remove an <code>AsyncFilter</code>
     */
    public boolean removeAsyncFilter(AsyncFilter asyncFilter) {
        return asyncFilters.remove(asyncFilter);
    }

    
    /**
     * Get the <code>AsyncHandler</code> who drive the asynchronous process.
     */
    public AsyncHandler getAsyncHandler() {
        return asyncHandler;
    }
    
    
    /**
     * Set the <code>AsyncHandler</code> who drive the asynchronous process.
     */
    public void setAsyncHandler(AsyncHandler asyncHandler) {
        this.asyncHandler = asyncHandler;
    }
}
