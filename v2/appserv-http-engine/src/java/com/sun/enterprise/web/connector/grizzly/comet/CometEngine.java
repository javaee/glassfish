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

package com.sun.enterprise.web.connector.grizzly.comet;

import com.sun.enterprise.web.connector.grizzly.AsyncHandler;
import com.sun.enterprise.web.connector.grizzly.AsyncTask;
import com.sun.enterprise.web.connector.grizzly.Pipeline;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.async.AsyncProcessorTask;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class allowing Comet support on top of Grizzly Asynchronous
 * Request Processing mechanism. This class is the entry point to any
 * component interested to execute Comet request style. Components can be
 * Servlets, JSP, JSF or pure Java class. A component interested to support
 * Comet request must do:
 *
 * (1) First, register the cometContext path on which Comet support will be applied:
 *     <code>CometEngine cometEngine = CometEngine.getEngine()</code>
 *     <code>CometContext cometContext = cometEngine.register(contextPath)</code>
 * (2) Second, add an instance of <code>CometHandler</code> to the
 *     <code>CometContext</code> returned by the register method:
 *     <code>cometContext.addCometHandler(handler);</code>
 * (3) Finally, you can notify other <code>CometHandler</code> by doing:
 *     <code>cometContext.notify(Object)(handler);</code>
 *
 * You can also select the stage where the request polling happens when
 * registering the cometContext path (see register(String,int);
 *
 *
 * @author Jeanfrancois Arcand
 */
public class CometEngine {
    
    /**
     * The token used to support BEFORE_REQUEST_PROCESSING polling.
     */
    public final static int BEFORE_REQUEST_PROCESSING = 0;
    
    
    /**
     * The token used to support AFTER_SERVLET_PROCESSING polling.
     */
    public final static int AFTER_SERVLET_PROCESSING = 1;
    
    
    /**
     * The token used to support BEFORE_RESPONSE_PROCESSING polling.
     */
    public final static int AFTER_RESPONSE_PROCESSING = 2;
    
    
    /**
     * Main logger
     */
    private final static Logger logger = SelectorThread.logger();
    
    
    /**
     * The <code>Pipeline</code> used to execute <code>CometTask</code>
     */
    private Pipeline pipeline;
    
    
    /**
     * The single instance of this class.
     */
    private static CometEngine cometEngine;
    
    
    /**
     * The current active <code>CometContext</code> keyed by context path.
     */
    private ConcurrentHashMap<String,CometContext> activeContexts;
    
    
    /**
     * Cache of <code>CometTask</code> instance
     */
    private ConcurrentLinkedQueue<CometTask> cometTasks;
    
    
    /**
     * Cache of <code>CometContext</code> instance.
     */
    private ConcurrentLinkedQueue<CometContext> cometContexts;
    
    
    /**
     * The <code>CometSelector</code> used to poll requests.
     */
    private CometSelector cometSelector;
    
    
    /**
     * The default class to use when deciding which NotificationHandler
     * to use. The default is DefaultNotificationHandler.
     */
    private static String notificationHandlerClassName = 
            DefaultNotificationHandler.class.getName();
    
    
    /**
     * Temporary repository that associate a Thread ID with a Key.
     * NOTE: A ThreadLocal might be more efficient.
     */
    private ConcurrentHashMap<Long,SelectionKey> threadsId;     
    
    
    /**
     * Store modified CometContext.
     */
    private ConcurrentHashMap<Long,CometContext> updatedCometContexts;
    
    // --------------------------------------------------------------------- //
    
    
    /**
     * Creat a singleton and initialize all lists required. Also create and
     * start the <code>CometSelector</code>
     */
    private CometEngine() {
        activeContexts = new ConcurrentHashMap<String,CometContext>();
        cometTasks = new ConcurrentLinkedQueue<CometTask>();
        cometContexts = new ConcurrentLinkedQueue<CometContext>();
        
        cometSelector = new CometSelector(this);
        cometSelector.start();
        
        threadsId = new ConcurrentHashMap<Long,SelectionKey>(); 
        updatedCometContexts = new ConcurrentHashMap<Long,CometContext>(); 
    }
    
    
    /**
     * Return a singleton of this Class.
     * @return CometEngine the singleton.
     */
    public synchronized final static CometEngine getEngine(){
        if (cometEngine == null) {
            cometEngine = new CometEngine();
        }
        return cometEngine;
    }
    
    
    /**
     * Unregister the <code>CometHandler</code> to the list of the
     * <code>CometContext</code>.
     */
    public CometContext unregister(String contextPath){
        CometContext cometContext = activeContexts.get(contextPath);
        try{
            cometContext.notify(cometContext,CometEvent.TERMINATE);
        } catch (IOException ex){
            logger.log(Level.WARNING,"unregister",ex);
        }
        finalizeContext(cometContext);
        
        return activeContexts.remove(contextPath);
    }
    
    
    /**
     * Register a context path with this <code>CometEngine</code>. The
     * <code>CometContext</code> returned will be of type
     * AFTER_SERVLET_PROCESSING, which means the request target (most probably
     * a Servlet) will be executed first and then polled.
     * @param contextPath the context path used to create the
     *        <code>CometContext</code>
     * @return CometContext a configured <code>CometContext</code>.
     */
    public CometContext register(String contextPath){
        return register(contextPath,AFTER_SERVLET_PROCESSING);
    }
    
    
    /**
     * Register a context path with this <code>CometEngine</code>. The
     * <code>CometContext</code> returned will be of type
     * <code>type</code>.
     * @param contextPath the context path used to create the
     *        <code>CometContext</code>
     * @return CometContext a configured <code>CometContext</code>.
     */
    public CometContext register(String contextPath, int type){
        CometContext cometContext = activeContexts.get(contextPath);
        if (cometContext == null){
            cometContext = cometContexts.poll();
            if (cometContext == null){
                cometContext = new CometContext(contextPath,type);
                cometContext.setCometSelector(cometSelector);
                NotificationHandler notificationHandler
                        = loadNotificationHandlerInstance
                             (notificationHandlerClassName);
                cometContext.setNotificationHandler(notificationHandler);
                if (notificationHandler != null && (notificationHandler 
                        instanceof DefaultNotificationHandler)){
                    ((DefaultNotificationHandler)notificationHandler)
                        .setPipeline(pipeline);
                }
            }
            activeContexts.put(contextPath,cometContext);
        }
        return cometContext;
    }
    
    
    /**
     * Handle an interrupted(or polled) request by matching the current context
     * path with the registered one.
     * If required, the bring the target component (Servlet) to the proper
     * execution stage and then notify the <code>CometHandler</code>
     * @param apt the current apt representing the request.
     * @return boolean true if the request can be polled.
     */
    protected boolean handle(AsyncProcessorTask apt) throws IOException{
        
        if (pipeline == null){
            pipeline = apt.getPipeline();
        }
        
        String contextPath = apt.getProcessorTask().getRequestURI();
        CometContext cometContext = null;       
        if (contextPath != null){
            cometContext = activeContexts.get(contextPath);
            if (cometContext != null){
                NotificationHandler notificationHandler = 
                        cometContext.getNotificationHandler();
                if (notificationHandler != null && (notificationHandler 
                        instanceof DefaultNotificationHandler)){
                    ((DefaultNotificationHandler)notificationHandler)
                        .setPipeline(pipeline);
                }
            }
        }
        
        /*
         * If the cometContext is null, it means the context has never 
         * been registered. The registration might happens during the
         * Servlet.service() execution so we need to keep a reference
         * to the current thread so we can later retrieve the associated
         * SelectionKey. The SelectionKey is required in order to park the
         * request.
         */
        boolean activateContinuation = true;      
        SelectionKey key = apt.getProcessorTask().getSelectionKey();
        threadsId.put(Thread.currentThread().getId(),key);
        
        int continuationType = (cometContext == null)?
            AFTER_SERVLET_PROCESSING:cometContext.continuationType;
                
        /*
         * Execute the Servlet.service method. CometEngine.register() or
         * CometContext.addCometHandler() might be invoked during the
         * execution.
         */
        executeServlet(continuationType,apt);

        /*
         * Will return a CometContext instance if and only if the 
         * Servlet.service() have invoked CometContext.addCometHandler().
         * If the returned CometContext is null, it means we need to 
         * execute a synchronous request.
         */
        cometContext = updatedCometContexts.remove(Thread.currentThread().getId());   
        
        CometTask cometTask = null;
        if (cometContext == null){
            activateContinuation = false;
        } else {
            cometTask = getCometTask(cometContext,key);
            cometTask.setSelectorThread(
                    apt.getProcessorTask().getSelectorThread());  
        }
        
        boolean parkRequest = true;
        if (activateContinuation) {
            // Disable keep-alive
            key.attach(null);
            cometContext.addAsyncProcessorTask(apt);
            cometContext.initialize(key);
            cometTask.setExpirationDelay(cometContext.getExpirationDelay());
            cometTask.setSelectorThread(apt.getSelectorThread());
            cometSelector.registerKey(key,cometTask);
        } else {
            parkRequest = false;
            if (cometTask != null){
                cometTask.recycle();
                cometTasks.offer(cometTask);
            }
        }
        return parkRequest;
    }
    
    
    /**
     * Tell the CometEngine to activate Grizzly ARP on that CometContext.
     * This method is called when CometContext.addCometHandler() is 
     * invoked.
     * @param threadId the Thread.getId().
     * @param cometContext An instance of CometContext.
     * @return key The SelectionKey associated with the current request.
     */
    protected SelectionKey activateContinuation(Long threadId,
            CometContext cometContext, boolean continueExecution){
        if (!continueExecution){
            updatedCometContexts.put(threadId,cometContext); 
        }
        return threadsId.remove(threadId);
    }
    
    
    /**
     * Return a clean and configured <code>CometTask</code>
     * @param cometContext the CometContext to clean
     * @param key The current <code>SelectionKey</code>
     * @return a new CometContext
     */
    protected CometTask getCometTask(CometContext cometContext,SelectionKey key){
        CometTask cometTask = cometTasks.poll();
        if (cometTask == null){
            cometTask = new CometTask();
        }
        cometTask.setCometContext(cometContext);
        cometTask.setSelectionKey(key);
        cometTask.setCometSelector(cometSelector);
        cometTask.setPipeline(pipeline);
        return cometTask;
    }
    
    
    /**
     * Cleanup the <code>CometContext</code>
     * @param cometContext the CometContext to clean
     */
    private void finalizeContext(CometContext cometContext) {
        Iterator<String> iterator = activeContexts.keySet().iterator();
        String contextPath;
        while(iterator.hasNext()){
            contextPath = iterator.next();
            if ( activeContexts.get(contextPath).equals(cometContext) ){
                activeContexts.remove(contextPath);
                break;
            }
        }
        
        ConcurrentLinkedQueue<AsyncProcessorTask> asyncTasks =
                cometContext.getAsyncProcessorTask();
        for (AsyncProcessorTask apt: asyncTasks){
            flushResponse(apt);
        }
        cometContext.recycle();
        cometContexts.offer(cometContext);
    }
    
    
    /**
     * Return the <code>CometContext</code> associated with the cometContext path.
     * XXX: This is not secure as a you can get a CometContext associated
     * with another cometContext path. But this allow interesting application...
     * MUST BE REVISTED.
     * @param contextPath the request's cometContext path.
     */
    public CometContext getCometContext(String contextPath){
        return activeContexts.get(contextPath);
    }
    
    
    /**
     * The <code>CometSelector</code> is expiring idle <code>SelectionKey</code>,
     * hence we need to resume the current request.
     * @param key the expired SelectionKey
     */
    protected void interrupt(SelectionKey key) {
        CometTask cometTask = (CometTask)key.attachment();
        
        key.attach(null);
        
        if (cometTask == null)
            throw new IllegalStateException("cometTask cannot be null");
                       
        SelectionKey akey = cometTask.getSelectionKey();
        
        try{
            if (!akey.isValid()) return;

            CometContext cometContext = cometTask.getCometContext();
            Iterator<AsyncProcessorTask> iterator =
                    cometContext.getAsyncProcessorTask().iterator();

            AsyncHandler ah = null;
            while (iterator.hasNext()){
                AsyncProcessorTask apt = iterator.next();
                ah = apt.getAsyncExecutor().getAsyncHandler();
                if (apt.getProcessorTask().getSelectionKey() == akey){
                    iterator.remove();
                    ah.removeFromInterruptedQueue(apt);

                    flushResponse(apt);            
                    if (akey != null){
                        akey.attach(null);
                    }
                    break;
                }
            }           
        } finally {
            cometTask.recycle();
            cometTasks.offer(cometTask);
        }
    }

    
    /**
     * Resume the long polling request by unblocking the current 
     * <code>SelectionKey</code>
     */
    protected void resume(SelectionKey key,CometContext cometContext) {
        Iterator<AsyncProcessorTask> iterator =
                cometContext.getAsyncProcessorTask().iterator();
        
        while (iterator.hasNext()){
            AsyncProcessorTask apt = iterator.next();            
            if (apt.getProcessorTask().getSelectionKey() == key){
                iterator.remove();
                apt.getAsyncExecutor().getAsyncHandler()
                      .removeFromInterruptedQueue(apt);
                flushResponse(apt);
                break;
            }
        }
    }
    
    
    /**
     * Complete the asynchronous request.
     */
    private void flushResponse(AsyncProcessorTask apt){
        apt.setStage(AsyncTask.POST_EXECUTE);
        try{
            apt.doTask();
        } catch (IllegalStateException ex){
            if (logger.isLoggable(Level.FINEST)){
                logger.log(Level.FINEST,"flushResponse failed",ex);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE,"flushResponse failed",ex);
        }
    }
    
    
    /**
     * Bring the cometContext path target (most probably a Servlet) to the processing
     * stage we need for Comet request processing.
     * @param cometContext The CometContext associated with the Servlet
     * @param apt the AsyncProcessorTask
     */
    private void executeServlet(int continuationType,
            AsyncProcessorTask apt){
        
        switch (continuationType){
            case BEFORE_REQUEST_PROCESSING:
                apt.setStage(AsyncTask.PRE_EXECUTE);
                break;
            case AFTER_SERVLET_PROCESSING:
                apt.getProcessorTask().invokeAdapter();
                return;
            case AFTER_RESPONSE_PROCESSING:
                apt.setStage(AsyncTask.POST_EXECUTE);
                break;
            default:
                throw new IllegalStateException("Invalid state");
        }
        
        try{
            apt.doTask();
        } catch (IOException ex){
            logger.log(Level.SEVERE,"executeServlet",ex);
        }
    }

    
    public static String getNotificationHandlerClassName() {
        return notificationHandlerClassName;
    }

    
    public static void setNotificationHandlerClassName(String aNotificationHandlerClassName) {
        notificationHandlerClassName = aNotificationHandlerClassName;
    }

    
    /**
     * Util to load classes using reflection.
     */
    private static NotificationHandler 
            loadNotificationHandlerInstance(String className){        
        Class clazz = null;                               
        try{                              
            clazz = Class.forName(className,true,
                    Thread.currentThread().getContextClassLoader());
            return (NotificationHandler)clazz.newInstance();
        } catch (Throwable t) {
            logger.log(Level.WARNING,"Invalid NotificationHandler: ",t);
        }   
        return new DefaultNotificationHandler();
    } 
    
        
    /**
     * Return the current logger.
     */
    public final static Logger logger(){
        return logger;
    }
    
    
    /**
     * Util to load classes using reflection.
     */
    private static NotificationHandler 
            loadNotificationHandlerInstance(String className){        
        Class clazz = null;                               
        try{                              
            clazz = Class.forName(className,true,
                    Thread.currentThread().getContextClassLoader());
            return (NotificationHandler)clazz.newInstance();
        } catch (Throwable t) {
            logger.log(Level.WARNING,"Invalid NotificationHandler: ",t);
        }   
        return new DefaultNotificationHandler();
    } 
}
